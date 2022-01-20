/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of {@link StringList} based on {@link ArrayList}.
 */
public class ArrayStringList extends ArrayList<String> implements StringList {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty list with a {@code ArrayList}'s default initial capacity.
	 */
	public ArrayStringList() {
		super();
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity of the list.
	 * @throws IllegalArgumentException if the specified initial capacity is negative.
	 */
	public ArrayStringList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in the
	 * order they are returned by the collection's iterator.
	 * 
	 * @param c the collection whose elements are to be placed into this list.
	 * @throws NullPointerException if the specified collection is null.
	 */
	public ArrayStringList(Collection<? extends String> c) {
		super(c);
	}

	@Override
	public String item(int index) {
		if (index >= 0 && index < size()) {
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
		return super.contains(str);
	}

}
