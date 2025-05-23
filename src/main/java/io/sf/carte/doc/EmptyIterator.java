/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc;

import java.util.Iterator;
import java.util.NoSuchElementException;

class EmptyIterator<T> implements Iterator<T>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		throw new NoSuchElementException();
	}

}
