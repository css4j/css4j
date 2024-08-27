/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.Iterator;
import java.util.NoSuchElementException;

class SingleItemIterator implements Iterator<String>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String next;

	SingleItemIterator(String value) {
		next = value;
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String next() {
		if (hasNext()) {
			String value = next;
			next = null;
			return value;
		}
		throw new NoSuchElementException();
	}

}
