/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Collection;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class SelectorListImpl extends AbstractSACList<Selector> implements SelectorList {

	private static final long serialVersionUID = 1L;

	SelectorListImpl() {
		super();
	}

	SelectorListImpl(Collection<? extends Selector> c) {
		super(c);
	}

	SelectorListImpl(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public Selector replace(int index, Selector selector) throws DOMException {
		if (index < 0 || index >= getLength()) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, "Wrong index: " + index);
		}
		if (selector == null) {
			throw new NullPointerException("Null selector");
		}
		return set(index, selector);
	}

}
