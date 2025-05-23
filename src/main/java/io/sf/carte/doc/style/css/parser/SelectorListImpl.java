/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Collection;
import java.util.ListIterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.NamespacePrefixMap;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * {@code List}-based implementation of {@link SelectorList}.
 */
class SelectorListImpl extends AbstractSACList<Selector> implements SelectorList {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty list with an initial capacity of 10.
	 */
	SelectorListImpl() {
		super();
	}

	/**
	 * Constructs a list containing the elements of the given collection.
	 *
	 * @param c the collection
	 * @throws NullPointerException if the collection is {@code null}
	 */
	SelectorListImpl(Collection<? extends Selector> c) {
		super(c);
	}

	/**
	 * Constructs an empty list with the given initial capacity.
	 *
	 * @param initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the initial capacity is negative
	 */
	SelectorListImpl(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public boolean contains(Selector sel) {
		return super.contains(sel);
	}

	@Override
	public boolean containsAll(SelectorList list) {
		if (list instanceof Collection) {
			Collection<?> c = (Collection<?>) list;
			return super.containsAll(c);
		}

		for (Selector sele : list) {
			if (!contains(sele)) {
				return false;
			}
		}
		return true;
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

	public void setNamespacePrefixMap(NamespacePrefixMap map) {
		for (Selector sel : this) {
			if (((AbstractSelector) sel).setNamespacePrefixMap(map)) {
				break;
			}
		}
	}

	@Override
	public SelectorListImpl replaceNested(SelectorList base) {
		SelectorListImpl clon = (SelectorListImpl) clone();
		ListIterator<Selector> it = clon.listIterator();
		while (it.hasNext()) {
			AbstractSelector sel = (AbstractSelector) it.next();
			MutableBoolean replaced = new MutableBoolean();
			Selector repl = sel.replace(base, replaced);
			if (!replaced.isTrue()) {
				repl = ((AbstractSelector) repl).descendant(base);
			}
			it.set(repl);
		}
		return clon;
	}

	SelectorListImpl replaceNestedArgument(SelectorList base, MutableBoolean replaced) {
		SelectorListImpl clon = (SelectorListImpl) clone();
		ListIterator<Selector> it = clon.listIterator();
		while (it.hasNext()) {
			AbstractSelector sel = (AbstractSelector) it.next();
			Selector repl = sel.replace(base, replaced);
			it.set(repl);
		}
		return clon;
	}

}
