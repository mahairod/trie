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

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
public class EnumTrie<En extends Enum<En>> extends Trie {

	public EnumTrie(En value) {
		this(value.getDeclaringClass());
	}

	public EnumTrie(Class<En> enumType) {
		super(strings(enumType));
	}

	private static <En extends Enum<En>> String[] strings(Class<En> enumType) {
		return toStrings(enumType.getEnumConstants(), UPCASE.KEEP);
	}

}
