/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2005-2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.LinkedList;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.ExtendedCSSValueList;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Linked-list implementation of <code>ExtendedCSSValueList</code>.
 */
public class LinkedCSSValueList extends LinkedList<AbstractCSSValue> implements ExtendedCSSValueList<AbstractCSSValue> {

	private static final long serialVersionUID = 1L;

	/**
	 * Adds a value to the end of this list.
	 * 
	 * @param value
	 *            the value to be added.
	 */
	@Override
	public boolean add(AbstractCSSValue value) {
		return super.add(value);
	}

	/**
	 * Removes all the items from this list.
	 */
	@Override
	public void clear() {
		super.clear();
	}

	/**
	 * Is this list empty ?
	 * 
	 * @return <code>true</code> if this list has no items, <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public AbstractCSSValue item(int index) {
		return get(index);
	}

	/**
	 * Creates and returns a copy of this object.
	 * <p>
	 * The list is cloned, but its contents are not.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	public ExtendedCSSValueList<AbstractCSSValue> clone() {
		LinkedCSSValueList copy = new LinkedCSSValueList();
		copy.addAll(this);
		return copy;
	}

	@Override
	public int getLength() {
		return size();
	}

	@Override
	public String getCssText() {
		if (isEmpty()) {
			return "";
		}
		BufferSimpleWriter sw = new BufferSimpleWriter(size() * 24 + 16);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		if (isEmpty()) {
			return "";
		}
		StringBuilder buf = new StringBuilder(size() * 24 + 16);
		buf.append(item(0).getMinifiedCssText(propertyName));
		int sz = size();
		for (int i = 1; i < sz; i++) {
			buf.append(',').append(item(i).getMinifiedCssText(propertyName));
		}
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		if (!isEmpty()) {
			get(0).writeCssText(wri);
			int sz = size();
			for (int i = 1; i < sz; i++) {
				wri.write(',');
				wri.write(' ');
				get(i).writeCssText(wri);
			}
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"This value has to be modified by accessing its elements.");
	}

	@Override
	public short getCssValueType() {
		return CSSValue.CSS_VALUE_LIST;
	}

}