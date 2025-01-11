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

import java.util.Iterator;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
public abstract class BitIndex {
	private final int size;
	protected final long globalMask;
	private static final int ELEMSIZE = Long.SIZE;

	private final long[] dataInd;
	private final short[] countsInd;

	protected final long[] data;
	protected final short[] counts;

	public BitIndex(String vocabulary) {
		this.size = ELEMSIZE * ELEMSIZE;
		data = new long[ELEMSIZE+1];
		counts = new short[ELEMSIZE+1];

		long wholeMask = 0;
		for (char c: vocabulary.toCharArray()) {
			wholeMask |= this.add(c);
		}
		globalMask = wholeMask;
		int indSize = Long.bitCount(globalMask);
		dataInd = new long[indSize];
		countsInd = new short[indSize];
		index();
	}

	private long add(char ch) {
		set(ch, true);
		int bucketInd = ch / ELEMSIZE;
		return (1L << bucketInd);
	}

	abstract int position(char ind);

	private int indexedPos(final long posmask) {
		long maskInd = posmask - 1;
		int ind = Long.bitCount(maskInd & globalMask);
		return ind;
	}

	protected void index() {
		long mask = globalMask;
		short counter = 0;
		int indIndex = 0;
		for (int pos = Long.numberOfTrailingZeros(mask); mask != 0; mask &= ~(1L << pos), pos = Long.numberOfTrailingZeros(mask) ) {
			counts[pos] = counter;
			long bucket = data[pos];
			countsInd[indIndex] = counter;
			dataInd[indIndex] = bucket;
			counter += Long.bitCount(bucket);
			indIndex++;
		}
	}

	public void set(int ind, boolean value) {
		checkBounds(ind);

		int pos = ind / ELEMSIZE;
		int off = ind % ELEMSIZE;
		long mask = (1L << off);
		if (value)
			data[pos] |= mask;
		else
			data[pos] &= ~mask;
	}

	public boolean get(int ind) {
		checkBounds(ind);

		int pos = ind / ELEMSIZE;
		int off = ind % ELEMSIZE;
		long mask = (1L << off);
		return (data[pos] & mask) != 0;
	}

	class IndexIter implements Iterator<Integer> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Integer next() {
			return 0;
		}
	}

	class Index implements Iterable<Integer> {
		@Override
		public Iterator<Integer> iterator() {
			return new IndexIter();
		}
	}

	public Index getIndexView(){
		return new Index();
	}

	public Iterator<Boolean> iterator() {
		return new Iter();
	}

	private void checkBounds(int ind) {
		if (ind >= size)
			throw new IndexOutOfBoundsException("Index " + ind + " is more than upper limit " + size);
	}

	class Iter implements Iterator<Boolean> {
		int index = 0;

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public Boolean next() {
			return get(index++);
		}
	}
}
