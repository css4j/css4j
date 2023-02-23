/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Implementation of {@link StringList} based on {@link LinkedList}.
 */
public class LinkedStringList extends LinkedList<String> implements StringList {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty list.
	 */
	public LinkedStringList() {
		super();
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in the
	 * order they are returned by the collection's iterator.
	 * 
	 * @param c the collection whose elements are to be placed into this list.
	 * @throws NullPointerException if the specified collection is null.
	 */
	public LinkedStringList(Collection<? extends String> c) {
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

	@Override
	public LinkedStringList clone() {
		return new LinkedStringList(this);
	}

}
