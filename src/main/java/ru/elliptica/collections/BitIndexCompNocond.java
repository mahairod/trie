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
public class BitIndexCompNocond extends BitIndex {

	public BitIndexCompNocond(String vocabulary) {
		super(vocabulary);
	}

	private int maskPosCardinal(int pos, long origMask) {
		long posmask = (1L << pos);
		long bitFlag = (origMask >>> pos) & 1;
		final long nullifyMask = -bitFlag;
		final long checkedMask = nullifyMask & origMask;

		long maskInd = posmask - 1;
		int ind = Long.bitCount(maskInd & checkedMask);

		return ind + (int)bitFlag - 1;
	}

	@Override
	final int position(char ch) {
		final int ind = ch;
		int pos = ind >> 6;
		// check missing item
		// -1 if missing
		{
			int bitFlag = (int) ((globalMask >> pos) & 1);
//			int nullifyMask = -bitFlag;
			pos = (pos * bitFlag) + bitFlag;
		}

		// TODO: (65+pos) % 65 -> 0..63, 64 ?
		// TODO: (66+pos) % 65 -> 0, 1..64 ?
		long bucket = data[pos];
		short counter;

		final int off = ind & (0b111111);

		int bucketCounter;// = maskPosCardinal(off, bucket);
		{
			long posmask = (1L << off);
			long bitFlag = (bucket >>> off) & 1;
			final long nullifyMask = -bitFlag;
			final long checkedMask = nullifyMask & bucket;
			counter = counts[pos];

			long maskInd = posmask - 1;
			int ind2 = Long.bitCount(maskInd & checkedMask);

			bucketCounter = ind2 + (int)bitFlag - 1;
		}
		return bucketCounter + counter;
	}

	@Override
	protected final void index() {
		long mask = globalMask;
		short counter = 0;
		for (int pos = Long.numberOfTrailingZeros(mask); mask != 0; mask &= ~(1L << pos), pos = Long.numberOfTrailingZeros(mask) ) {
			counts[pos+1] = counter;
			long bucket = data[pos+1];
			counter += (short) Long.bitCount(bucket);
		}
	}

	@Override
	public void set(int ind, boolean value) {
		super.set(ind + Long.SIZE, value);
	}

}
