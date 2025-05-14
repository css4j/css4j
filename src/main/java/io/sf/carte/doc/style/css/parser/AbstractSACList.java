/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class AbstractSACList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 2L;

	/**
	 * Constructs an empty list with an initial capacity of 10.
	 */
	protected AbstractSACList() {
		super();
	}

	/**
	 * Constructs a list containing the elements of the given collection.
	 *
	 * @param c the collection
	 * @throws NullPointerException if the collection is {@code null}
	 */
	protected AbstractSACList(Collection<? extends E> c) {
		super(c);
	}

	/**
	 * Constructs an empty list with the given initial capacity.
	 *
	 * @param initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the initial capacity is negative
	 */
	protected AbstractSACList(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public boolean add(E sel) {
		if (!contains(sel)) {
			return super.add(sel);
		}
		return false;
	}

	public int getLength() {
		return size();
	}

	public E item(int index) {
		if (index < 0 || index >= size())
			return null;
		return get(index);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		Iterator<E> it = iterator();
		if (it.hasNext()) {
			buf.append(it.next().toString());
			while (it.hasNext()) {
				buf.append(',').append(it.next().toString());
			}
		}
		return buf.toString();
	}

}
