/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.Iterator;

/**
 * Iterates on a wrapped iterator, disallows {@link #remove()}.
 * 
 * @param <E>
 *            the element type.
 */
class UnmodifiableIterator<E> implements Iterator<E> {

	private Iterator<E> iterator;

	/**
	 * Construct a new iterator with the given element.
	 * 
	 * @param iterator the iterator.
	 * @throws NullPointerException if the iterator is {@code null}.
	 */
	UnmodifiableIterator(Iterator<E> iterator) {
		super();
		if (iterator == null) {
			throw new NullPointerException();
		}
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		return iterator.next();
	}

}
