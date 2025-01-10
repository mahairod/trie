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

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
public class StringTrie extends Trie implements Predicate<String>, Collection<String> {

	private boolean caseSensitive;

	private AbstractCollection<String> delegate;

	public StringTrie(String[] keywords) {
		super(keywords);
	}

	public StringTrie(String[] keywords, boolean caseSensitive) {
		super(toStrings(keywords, caseSensitive ? UPCASE.KEEP : UPCASE.LOWER));
		this.caseSensitive = caseSensitive;
	}


	@Override
	public boolean test(String s) {
		return contains(s);
	}

	@Override
	public boolean contains(String value) {
		if (!caseSensitive)
			value = value.toLowerCase();
		return super.contains(value);
	}

	@Override
	public boolean contains(Object o) {
		return contains( Objects.toString(o) );
	}

	@Override
	public Iterator<String> iterator() {
		return new Iter();
	}

	{
		delegate = new AbstractCollection<>() {
			@Override
			public Iterator<String> iterator() {
				return StringTrie.this.iterator();
			}

			@Override
			public int size() {
				return StringTrie.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return StringTrie.this.contains(o);
			}
		};
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean add(String s) {
		return addStrings(List.of(s));
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		return addStrings(c);
	}

	@Override
	public boolean remove(Object o) {
		return removeStrings(List.of(o));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return removeStrings(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not intended to be supported");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not intended to be supported");
	}

	private boolean addStrings(Collection<? extends String> extraStrings) {
		if (containsAll(extraStrings)) {
			return false;
		}
		Set<String> newData = new HashSet<>();
		newData.addAll(this);
		newData.addAll(extraStrings);
		updateStrings(newData);
		return true;
	}

	private boolean removeStrings(Collection<?> toRemove) {
		Set<String> oldData = new HashSet<>();
		oldData.addAll(this);
		if (oldData.removeAll(toRemove)) {
			updateStrings(oldData);
			return true;
		}
		return false;
	}

}
