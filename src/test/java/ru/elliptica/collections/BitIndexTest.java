package ru.elliptica.collections;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
class BitIndexTest {

	@Test
	void index() {

		String vocab = "ZEАJЮTЯЙAЪNЩQКLДЛФYGHXРЧWKMIВШBИаSFСУЫБМ";
		for (int i=0; i<60; i++) {
			double r = Math.random();
			char ch = 0;
			if (r < 0.5) {
				ch = (char)('A' + (52 * r));
			} else {
				r -= 0.5;
				ch = (char)('А' + (66 * r));
			}
		}
		Set<Character> chars = new TreeSet<>();
		for (char c: vocab.toCharArray()) {
			chars.add(c);
		}
		BitIndex index = new BitIndexComp(vocab);

		StringBuilder sb = new StringBuilder(chars.size());
		chars.forEach( sb::append );
		vocab = sb.toString();


		sb.setLength(0);
		for (char c = 'A'; c <= 'Z'; c++) {
			sb.append(c);
		}
		for (char c = 'А'; c <= 'Я'; c++) {
			sb.append(c);
		}
		for (char c = 'а'; c <= 'я'; c++) {
			sb.append(c);
		}

		String testStr = sb.toString();
		for (char c: testStr.toCharArray()) {
			int i = vocab.indexOf(c);
			int ind = index.position(c);
			if (i!=ind) {
				testStr.getClass();
			}
			assertEquals(i, ind);
		}
	}
}