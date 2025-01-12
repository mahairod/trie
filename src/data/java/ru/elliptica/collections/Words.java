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

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
class Words {
	static final String[] POSSIBLE_TRUE_VALS = {
		"1",
		"t",
		"$$",
		"true",
		"truth",
		"истина",
		"истинно",
		"истинна",
		"истинен",
		"истинны",
		"истинный",
		"правда",
		"верно",
		"верен",
		"верна",
		"верны",
		"верный",
		"сущий",
		"и",
		"verum",
		"verus",
		"veritable",
	};

	public static List<String> loadStrings() {
		try (InputStream ris = Words.class.getResourceAsStream("/oxford3n5t.txt") ){
			if (ris == null) {
				System.err.println("String resource not loaded");
				return Collections.emptyList();
			}
			return new BufferedReader(new InputStreamReader(ris)).lines().toList();
		} catch (IOException e) {
			System.err.println("String resource not loaded:" + e.getMessage());
			throw new RuntimeException(e);
		}

	}
}
