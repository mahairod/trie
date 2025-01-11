package ru.elliptica.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
class StringTrieTest {

	StringTrie tr;

	@BeforeEach
	void init() {
		tr = new StringTrie(Words.POSSIBLE_TRUE_VALS);
	}

	@Test
	void iterator() {
		System.out.println("Trie values: ");
		for (String v: tr) {
			System.out.println("\t" + v);
		}
	}

	@Test
	void contains() {
		enum VALS {TR, FL, T}
		assertTrue(tr.contains(VALS.T));
		assertFalse(tr.contains(VALS.TR));
	}

	@Test
	void toArray() {
		var arr = tr.toArray();
		assertNotNull(arr);
		assertTrue(arr.length == Words.POSSIBLE_TRUE_VALS.length);
	}

	@Test
	void testToArray() {
		var arr = tr.toArray(String[]::new);
		assertNotNull(arr);
		assertTrue(arr.length == Words.POSSIBLE_TRUE_VALS.length);
	}

	@Test
	void containsAll() {
		assertTrue(tr.containsAll(List.of()));
		assertTrue(tr.containsAll(List.of(Words.POSSIBLE_TRUE_VALS)));
		assertFalse(tr.containsAll(List.of(Words.POSSIBLE_TRUE_VALS.length)));
		assertTrue(tr.containsAll(List.of("t", "1")));
		// supports toString() conversion
		assertTrue(tr.containsAll(List.of("t", 1, true)));
		assertTrue(tr.containsAll(List.of(true)));
		// case-insensitive by default
		assertTrue(tr.containsAll(List.of("T")));
	}

	//@Test
	void add() throws InterruptedException {
//		assertThrows(UnsupportedOperationException.class, ()-> {
//			tr.add("qwer");
//		});
		Thread [] threads = new Thread[3];
		for (int i = 0; i < threads.length; i++) {
			int ind = i;
			var th = new Thread(()->{
				int fails = 0, succs = 0;
				while (true) {
					long started = System.nanoTime();
					boolean check = tr.containsAll(List.of(Words.POSSIBLE_TRUE_VALS));
					long duration1 = System.nanoTime() - started;
					boolean check2 = tr.contains("aaaa");
					if (check2) succs++; else fails++;

					System.out.println("T-" + ind + ", res: " + check + ", dur: " + duration1
						 + ", fail: " + fails + ", succ: " + succs);
				}
			});
			threads[i] = th;
			th.start();
		}
		char[] patt = new char[30];
		Arrays.fill(patt, 'a');
		for (int i = 1; i < patt.length; i++) {
			String s = String.valueOf(patt, 0, i);
			tr.add(s);
			Thread.sleep(100);
		}
		for (Thread th: threads) {
			th.interrupt();
		}

	}
}