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
public class BitIndexComp extends BitIndex {

	public BitIndexComp(String vocabulary) {
		super(vocabulary);
	}

	@Override
	final int position(char ind) {
		final int pos = ind >> 6;
		long posmask = (1L << pos);
		if ( (posmask & globalMask) == 0)
			return -1;

		final short counter = counts[pos];
		final int off = ind & (0b111111);

//		final int ipos = indexedPos(posmask);
//		short counter = countsInd[ipos];
//		long bucket = dataInd[ipos];
		final long maskPos = (1L << off);
		final long bucket = data[pos];
		if ( (maskPos & bucket) == 0)
			return -1;

		return counter + Long.bitCount((maskPos-1) & bucket);
	}

}
