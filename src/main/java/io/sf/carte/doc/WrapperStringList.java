/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.UnaryOperator;

/**
 * An unmodifiable {@link StringList} that wraps a {@link Collection}.
 */
public class WrapperStringList implements StringList, Iterable<String>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final Collection<String> collection;

	/**
	 * Constructs a list containing the elements of the specified collection, in the
	 * order they are returned by the collection's iterator.
	 * 
	 * @param c the collection whose elements are to be wrapped by this list.
	 * @throws NullPointerException if the specified collection is {@code null}.
	 */
	public WrapperStringList(Collection<String> c) {
		super();
		if (c == null) {
			throw new NullPointerException();
		}
		collection = c;
	}

	@Override
	public String item(int index) {
		if (index >= 0 && index < collection.size()) {
			return get(index);
		}
		return null;
	}

	@Override
	public int getLength() {
		return size();
	}

	@Override
	public boolean contains(String str) {
		return collection.contains(str);
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return collection.contains(o);
	}

	@Override
	public Iterator<String> iterator() {
		return new UnmodifiableIterator<>(collection.iterator());
	}

	@Override
	public Object[] toArray() {
		return collection.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return collection.toArray(a);
	}

	@Override
	public boolean add(String e) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean addAll(int index, Collection<? extends String> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public void replaceAll(UnaryOperator<String> operator) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public void sort(Comparator<? super String> c) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public String get(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid index: " + index);
		}
		int i = 0;
		for (String s : collection) {
			if (i == index) {
				return s;
			}
			i++;
		}
		throw new IndexOutOfBoundsException("Invalid index: " + index);
	}

	@Override
	public String set(int index, String element) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public void add(int index, String element) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public String remove(int index) {
		throw new UnsupportedOperationException("Unmodifiable list.");
	}

	@Override
	public int indexOf(Object o) {
		int i = 0;
		for (String s : collection) {
			if (o == s) {
				return i;
			}
			i++;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int i = 0, last = -1;
		for (String s : collection) {
			if (o == s) {
				last = i;
			}
			i++;
		}
		return last;
	}

	@Override
	public ListIterator<String> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<String> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StringList subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || fromIndex > toIndex || toIndex > size()) {
			throw new IndexOutOfBoundsException();
		}
		String[] a = collection.toArray(new String[0]);
		a = Arrays.copyOfRange(a, fromIndex, toIndex);
		return new WrapperStringList(Collections.unmodifiableList(Arrays.asList(a)));
	}

	@Override
	public Spliterator<String> spliterator() {
		return collection.spliterator();
	}

	@Override
	public WrapperStringList clone() {
		return new WrapperStringList(this);
	}

}
