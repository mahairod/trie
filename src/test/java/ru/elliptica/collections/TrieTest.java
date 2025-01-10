package ru.elliptica.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
class TrieTest {

	@Test
	void suff() {
		new Trie(Words.POSSIBLE_TRUE_VALS).printSuff();
	}

	@Test
	void makesuff() {
		Trie tr = new Trie(Words.POSSIBLE_TRUE_VALS, true);
//		tr.printTree();
		for (String kw : Words.POSSIBLE_TRUE_VALS) {
			assertTrue(tr.contains(kw));
			assertFalse(tr.contains(kw + 'q'));
			assertFalse(tr.contains('%' + kw));
		}
	}

	@Test
	void testIndex() {
		Trie tr = new Trie(Words.POSSIBLE_TRUE_VALS, true);
		Trie trind = new Trie(Words.POSSIBLE_TRUE_VALS);
		List<String> examples = new ArrayList<>();
		for (String word: Words.POSSIBLE_TRUE_VALS) {
			examples.add(word);
			examples.add(word + '$');
			examples.add(word + '@');
			examples.add('@' + word);
			int randPos = (int) (Math.random() * word.length());
			randPos = Math.max(randPos, 0);
			String infixed = word.substring(0, randPos) + "@" + word.substring(randPos);
			examples.add(infixed);
		}
		for (String example: examples) {
			assertSame( tr.containsTree(example), trind.containsIndex(example) );
		}
	}
}