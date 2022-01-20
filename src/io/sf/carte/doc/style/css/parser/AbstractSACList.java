/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class AbstractSACList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 2L;

	protected AbstractSACList() {
		super();
	}

	protected AbstractSACList(Collection<? extends E> c) {
		super(c);
	}

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
