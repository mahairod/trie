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

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
public class CompressedTrie extends Trie {

	public CompressedTrie(String[] keywords, VocVersion version) {
		super(keywords, version);
	}

	class CompressedIndex extends Index {
		public CompressedIndex(byte[] index, BitSet[] shortCutMasks, String vocabulary, int stopNodeIndex) {
			super(index, shortCutMasks, -1, vocabulary, stopNodeIndex);
		}
	}

	protected Index buildIndexImpl() {
		Set<Character> chars = new TreeSet<>();
		final List<Node> orderedNodes = new ArrayList<>();
		String vocabulary = collectNodeData(orderedNodes, chars);

		int nodeCount = orderedNodes.size();
		final int[] offsets = new int[nodeCount];
		final byte[] index;
		{
			int offset = 0, ind = 0;
			for (Node node: orderedNodes) {
				offsets[ind++] = offset;
				offset += node.children.size() + 1;
			}
			index = new byte[offset];
		}

		Function<Integer, Integer> ind2Offs = node -> offsets[node];

		final BitSet[] shortCutMasks = new BitSet[index.length];
		final int stopNodeIndex = ind2Offs.apply( orderedNodes.indexOf(STOP) );

		int nodeInd = 0;
		// 0th node bucket is a root
		for (Node n : orderedNodes) {
			int nodeOffset = ind2Offs.apply(nodeInd++);
			int nodeSize = n.children.size() + 1;
			// 0th char index points to root, means: not present
			index[nodeOffset] = 0;
			final BitSet mask = shortCutMasks[nodeOffset] = new BitSetExt(nodeSize);
			int subnodesCount = 0;
			Set<Character> sortedKeys = new TreeSet<>(n.children.keySet());
			for (Character key: sortedKeys) {
				int chInd = 1 + vocabulary.indexOf(key);
				mask.set(chInd);
				Node target = n.children.get(key);
				int indInd = nodeOffset + subnodesCount + 1;
				index[indInd] = (byte) (int) ind2Offs.apply( orderedNodes.indexOf(target) );
				subnodesCount++;
			}
		}
		inIndexRebuildNow = true;
		Index indexVal = new CompressedIndex(index, shortCutMasks, vocabulary, stopNodeIndex);
		inIndexRebuildNow = false;
		return indexVal;
	}

	protected boolean containsIndex(String value) {
		final CompressedIndex data = (CompressedIndex) indexData;
		final int vallen = value.length();

		// start from root
		int nodeOffset = 0;
		final BitIndex vocIndex = data.vocIndex;
		final byte[] index = data.index;
		for (int i = 0; i < vallen; i++) {
			char c = value.charAt(i);
			int chInd = 1 + vocIndex.position(c);
			final BitSetExt mask = (BitSetExt) data.shortCutMasks[nodeOffset];
			if (!mask.get(chInd)) {
				return false;
			}
			// TODO
			int pos = mask.cardinalityBefore(chInd);
			nodeOffset = index[nodeOffset + pos + 1];
		}
		{
			byte nextNode = index[nodeOffset + data.STOP_SYMB_POS + 1];
			return data.stopNodeIndex == nextNode;
		}
	}

	class BitSetExt extends BitSet {
		private long[] wordsExp;
		public BitSetExt(int nbits) {
			super(nbits);
			try {
				Field wf = BitSet.class.getDeclaredField("words");
				wf.setAccessible(true);
				wordsExp = (long[]) wf.get(this);
			} catch (ReflectiveOperationException ex) {
			}
		}

		public int cardinalityBefore(int bitIndex) {
			int wordIndex = wordIndex(bitIndex);
			if (wordIndex >= length())
				return cardinality();

			int result = 0;
			for (int i = 0; i < wordIndex; i++) {
				result += Long.bitCount(wordsExp[i]);
			}
			int offs = bitIndex & 63;
			long lastWord = wordsExp[wordIndex];
			result += Long.bitCount( lastWord << (64-offs) );
			return result;
		}

		private static int wordIndex(int bitIndex) {
			return bitIndex >> 6;
		}
	}

}
