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
public class BitIndexCached extends BitIndex {

	private int rt_bucket_pos = -1;
	private short rt_counter;
	private long rt_bucket;

	public BitIndexCached(String vocabulary) {
		super(vocabulary);
	}

	@Override
	final int position(char ind) {
		final int pos = ind >> 6;
		if (pos != rt_bucket_pos) {
			rt_bucket_pos = pos;
			final long bucket_posmask = (1L << pos);
			if ((bucket_posmask & globalMask) == 0) {
				rt_counter = -1;
				return -1;
			}

			rt_counter = counts[pos];
			rt_bucket = data[pos];
		} else {
			if (rt_counter < 0)
				return -1;
		}

		final int off = ind & (0b111111);
		long maskPos = (1L << off);
		if ( (maskPos & rt_bucket) == 0)
			return -1;

		long maskInd = maskPos - 1;
		int bucketCounter = Long.bitCount(maskInd & rt_bucket);
		return bucketCounter + rt_counter;
	}

}
