/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> ѱ 2025.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2025 Anton Astafiev <anton@astafiev.me>. All rights reserved.
 *
 * Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package ru.elliptica.collections;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
public class Trie {
	// index stuff
	class Index {
		final byte[] index;
		final BitSet[] shortCutMasks;
		final int bucketSize;
		final char[] vocabulary;
		final int stopNodeIndex;
//		final int stopOffset;
		final BitIndex vocIndex;
		final int STOP_SYMB_POS;

		public Index(byte[] index, BitSet[] shortCutMasks, int bucketSize, String vocabulary, int stopNodeIndex) {
			this.index = index;
			this.shortCutMasks = shortCutMasks;
			this.bucketSize = bucketSize;
			this.vocabulary = vocabulary.toCharArray();
			this.stopNodeIndex = stopNodeIndex;
//			this.stopOffset =  stopNodeIndex * bucketSize;
			this.STOP_SYMB_POS = vocabulary.indexOf(STOP_SYMB);
			vocIndex = switch (indexVersion) {
				case CACHED -> new BitIndexCached(vocabulary);
				case COMPUTED_FULL_IND -> new BitIndexComp(vocabulary);
				case COMPUTED_NOCOND_IND -> new BitIndexCompNocond(vocabulary);
				default -> null;
			};
		}
	}

	public enum VocVersion {
		COMPUTED_FULL_IND, COMPUTED_COMPACT_IND, COMPUTED_NOCOND_IND, CACHED
	}

	public enum TrieIndexVersion {
		FLAT, COMPRESSED
	}

	protected final Index indexData;
	private VocVersion indexVersion = VocVersion.COMPUTED_NOCOND_IND;

	private static final char STOP_SYMB = '$';

	// tree stuff
	Node START;
	Node STOP = new Node(null, STOP_SYMB);
	private int numberOfWords = 0;

	// multi-thread
	final boolean isSyncEnabled = false;
	volatile boolean inIndexRebuildNow;
	private final Object updateGuard = new Object();

	List<Node> stops = new ArrayList<>();

	public Trie(String[] keywords) {
		this(keywords, false);
	}

	public Trie(String[] keywords, VocVersion version) {
		this(keywords, false, version);
		System.out.println("Trie version " + version);
	}

	protected Trie(String[] keywords, boolean skipIndex) {
		this(keywords, skipIndex, VocVersion.COMPUTED_NOCOND_IND);
	}

	protected Trie(String[] keywords, boolean skipIndex, VocVersion version) {
		updateTreeImpl(Arrays.asList(keywords));
		if (skipIndex) {
			indexData = null;
			return;
		}
		indexVersion = version;

		indexData = buildIndexImpl();
//		cleanAfterBuild();
	}

	protected void updateTreeImpl(Collection<String> keywords) {
		START = new Node(null, '^');
		for (String keyword : keywords) {
			Node cur = START;
			for (final char c : keyword.toCharArray()) {
				Node parent = cur;
				cur = cur.children.computeIfAbsent(c, ch -> new Node(parent, ch));
			}
			cur.children.put(STOP_SYMB, STOP);
			stops.add(cur);
		}
		mergeSuff();
		// TODO: size is async with index data
		numberOfWords = keywords.size();
	}

	private void cleanAfterBuild() {
		Node root = START;
		START = null;
		walkTreeUpwards(root, n -> n.children.clear());
	}

	void buildIndex() {
		buildIndexImpl();
	}

	private void walkTreeUpwards(Node curNode, Consumer<Node> consumer) {
		for (Node kid : curNode.children.values()) {
			walkTree(kid, consumer);
		}
		consumer.accept(curNode);
	}

	private void walkTree(Node curNode, Consumer<Node> consumer) {
		consumer.accept(curNode);
		for (Node kid : curNode.children.values()) {
			walkTree(kid, consumer);
		}
	}

	public boolean contains(String value) {
		Objects.nonNull(value);
//		if (value.isEmpty())
//			return false;

		if (true || isIndexPresent())
			return containsIndex(value);
		else
			return containsTree(value);
	}

	public final boolean containsAllStrings(Collection<String> c) {
		for (String str: c) {
			if (!contains(str)) {
				return false;
			}
		}
		return true;
	}

	public int size() {
		return numberOfWords;
	}

	public boolean isEmpty() {
		return size() > 0;
	}

	boolean containsTree(String value) {
		value += STOP_SYMB;
		Node cur = START;
		for (final char c : value.toCharArray()) {
			cur = cur.children.get(c);
			if (cur == null) {
				return false;
			}
		}
		return cur.character == STOP_SYMB;
	}

	protected boolean containsIndex(String value) {
		final Index data = indexData;
		final int vallen = value.length();

		// start from root
		int nodeOffset = 0;
		final BitIndex vocIndex = data.vocIndex;
		final byte[] index = data.index;
		byte curNode = 0;
		for (int i = 0; i < vallen; i++) {
			char c = value.charAt(i);
			int chInd = 1 + vocIndex.position(c);

			curNode = index[nodeOffset + chInd];
			if (curNode == 0) {
				return false;
			}
			nodeOffset = curNode * data.bucketSize;
		}
		{
			byte nextNode = index[nodeOffset + data.STOP_SYMB_POS + 1];
			return data.stopNodeIndex == nextNode;
		}
	}

	private int vocIndex(char[] symbols, char patt) {
		return Arrays.binarySearch(symbols, patt);
	}

	protected void updateStrings(Collection<String> replaceStrings) {
		synchronized (updateGuard) {
			if (!isIndexPresent()) {
				throw new IllegalStateException("Can't update. Index is missing");
			}
			updateTreeImpl(replaceStrings);
			buildIndexImpl();
			cleanAfterBuild();
		}
	}

	private boolean isIndexPresent() {
		if (isSyncEnabled) {
			return !inIndexRebuildNow;
		} else {
			return indexData != null;
		}
	}

	protected enum UPCASE {
		KEEP, LOWER, UPPER
	}

	protected static String[] toStrings(Object[] values, UPCASE upcase) {
		String[] res = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			String str = values[i].toString();
			res[i] = switch (upcase) {
				case KEEP -> str;
				case LOWER -> str.toLowerCase();
				case UPPER -> str.toUpperCase();
			};
		}
		return res;
	}

	protected String collectNodeData(List<Node> orderedNodes, Set<Character> chars) {
		{
			var allNodes = new IdentityHashMap<Node, Object>();
			walkTree(START, n -> {
				if (allNodes.put(n, n) == null) {
					orderedNodes.add(n);
				}
				chars.add(n.character);
			});
		}
		StringBuilder sb = new StringBuilder(chars.size());
		chars.forEach(sb::append);
		String vocabulary = sb.toString();
		return vocabulary;
	}

	protected Index buildIndexImpl() {
		Set<Character> chars = new TreeSet<>();
		final List<Node> orderedNodes = new ArrayList<>();
		String vocabulary = collectNodeData(orderedNodes, chars);

		int bucketSize = vocabulary.length() + 1;
		int nodeCount = orderedNodes.size();

		byte[] index = new byte[bucketSize * nodeCount];
		int stopNodeIndex = orderedNodes.indexOf(STOP);
		BitSet[] shortCutMasks = new BitSet[nodeCount];

		int maskInd = 0;
		// 0th node bucket is a root
		int nodeOffset = 0;
		for (Node n : orderedNodes) {
			// 0th char index points to root, means: not present
			index[nodeOffset] = 0;
			final BitSet mask = shortCutMasks[maskInd++] = new BitSet(bucketSize);
			for (var entry : n.children.entrySet()) {
				int chInd = 1 + vocabulary.indexOf(entry.getKey());
				mask.set(chInd);
				Node target = entry.getValue();
				index[nodeOffset + chInd] = (byte) orderedNodes.indexOf(target);
			}
			nodeOffset += bucketSize;
		}
		inIndexRebuildNow = true;
		Index indexVal = new Index(index, shortCutMasks, bucketSize, vocabulary, stopNodeIndex);
		inIndexRebuildNow = false;
		return indexVal;
	}

	Trie mergeSuff() {
		List<Node> incomes = stops;
		while (!incomes.isEmpty()) {
			Map<Node, Node> nextIncomes = new IdentityHashMap<>();
			while (!incomes.isEmpty()) {
				Node curNode = incomes.remove(0);
				boolean merged = false;
				while ((incomes.contains(curNode))) {
					Node identical = incomes.remove(incomes.indexOf(curNode));
					// merge
					identical.parent.children.put(curNode.character, curNode);
					merged = true;
					nextIncomes.put(identical.parent, null);
				}
				if (merged)
					nextIncomes.put(curNode.parent, null);

//				for (Iterator<Node> it = incomes.iterator(); false && it.hasNext(); ) {
//					Node n = it.next();
//					if (n.character == curNode.character) {
//						it.remove();
//						if (n.parent == null || n == curNode)
//							continue;
//						if (!n.children.keySet().equals(curNode.children.keySet()))
//							continue;
//						boolean equal = true;
//						// merge only fully equal sets
//						for (char key : n.children.keySet()) {
//							Node curKid = curNode.children.get(key);
//							Node nKid = n.children.get(key);
//							equal = equal && (curKid == nKid);
//						}
//						if (!equal) continue;
//						// merge
//						n.parent.children.put(curNode.character, curNode);
//						nextIncomes.put(n.parent, null);
//					}
//				}
			}
			incomes = new ArrayList<>(nextIncomes.keySet());
		}
		return this;
	}

	void printTree() {
		printNode(" ", START);
	}

	void printNode(String indent, Node node) {
		print(indent + node.character + "\n");
		for (Node kid : node.children.values()) {
			printNode(indent + "  ", kid);
		}
	}

	void printSuff() {
		int ind = 0;
		for (Node stop : stops) {
			System.out.print("Stop #" + ind++ + ": ");
			while (stop != null) {
				System.out.print(stop.character);
				stop = stop.parent;
			}
			System.out.println();
		}
	}

	void print(String val) {
		System.out.print(val);
	}

	static class Node {
		Node parent;
		HashMap<Character, Node> children;
		final char character;

		public Node(Node parent, char character) {
			children = new HashMap<>();
			this.parent = parent;
			this.character = character;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Node))
				return false;
			Node on = (Node) obj;
			if (character == on.character && children.keySet().equals(on.children.keySet())) {
				for (char key : children.keySet()) {
					Node curKid = children.get(key);
					Node nKid = on.children.get(key);
					if (curKid != nKid)
						return false;
				}
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return character + children.hashCode();
		}

		@Override
		public String toString() {
			return "Node<" + character + ">[" + children.size() + ']';
		}
	}

	class Iter implements Iterator<String> {
		LinkedList<Integer> indices = new LinkedList<>();
		LinkedList<Integer> nodes = new LinkedList<>();
		String curCtx = "";
		Index data = null;
		{
			indices.add(0);
			nodes.add(0);
		}
		private void bindData() {
			if (data != null)
				return;
			data = indexData;
		}

		private int lastNode() {
			return nodes.getLast();
		};

		@Override
		public boolean hasNext() {
			bindData();
			int start = nodeOffs(0);
			int stop = nodeOffs(data.bucketSize);
			int curInd = nodeOffs(indices.getLast());
			boolean res = findFirstSetPos(curInd+1, stop) > 0;
			if (!res && indices.size()>1) {
				indices.pollLast();
				nodes.pollLast();
				curCtx = curCtx.substring(0, curCtx.length()-1);
				return hasNext();
			}
			return res;
		}

		@Override
		public String next() {
			bindData();
			int nextNode = 0;
			do {
				int start = nodeOffs(0);
				int stop = nodeOffs(data.bucketSize);
				int curInd = nodeOffs(indices.getLast());
				int nodePos = findFirstSetPos(curInd+1, stop);
				nextNode = data.index[nodePos];
				int nodeOffs = nodePos - start;
				indices.set(indices.size()-1, nodeOffs);
				if (nextNode != data.stopNodeIndex) {
					char symb = data.vocabulary[nodeOffs - 1];
					curCtx += (symb);
					nodes.add(nextNode);
					indices.add(0);
				}
			} while (nextNode != data.stopNodeIndex);
			return curCtx;
		}

		private int findFirstSetPos(int start, int stop) {
			int nodeStart = nodeOffs(0);
			if (stop <= nodeStart + data.bucketSize && start >= nodeStart) {
				BitSet mask = data.shortCutMasks[lastNode()];
				int startOffs = start - nodeStart;
				int next = mask.nextSetBit(startOffs);
				return next>=0 ? nodeStart+next : -1;
			}
			for (int i = start; i < stop; i++) {
				if (data.index[i]>0)
					return i;
			}
			return -1;
		}

		private int nodeOffs(int offset) {
			return lastNode() * data.bucketSize + offset;
		}

	}
}

