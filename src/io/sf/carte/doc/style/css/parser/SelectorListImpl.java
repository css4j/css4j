/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

class SelectorListImpl extends LinkedList<Selector> implements SelectorList {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(Selector sel) {
		if (!contains(sel)) {
			return super.add(sel);
		}
		return false;
	}

	@Override
	public void addLast(Selector sel) {
		if (!contains(sel))
			super.addLast(sel);
	}

	@Override
	public int getLength() {
		return size();
	}

	@Override
	public Selector item(int index) {
		if (index < 0 || index >= size())
			return null;
		return get(index);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		Iterator<Selector> it = iterator();
		if (it.hasNext()) {
			buf.append(it.next().toString());
			while (it.hasNext()) {
				buf.append(',').append(it.next().toString());
			}
		}
		return buf.toString();
	}
}