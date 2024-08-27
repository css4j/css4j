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

import io.sf.carte.doc.style.css.CSSValueList;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.CSSValueSyntax.Multiplier;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Linked-list implementation of <code>CSSValueList</code>, comma-separated.
 */
public class LinkedCSSValueList extends LinkedList<StyleValue> implements CSSValueList<StyleValue> {

	private static final long serialVersionUID = 1L;

	/**
	 * Adds a value to the end of this list.
	 * 
	 * @param value
	 *            the value to be added.
	 */
	@Override
	public boolean add(StyleValue value) {
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
	public StyleValue item(int index) {
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
	public LinkedCSSValueList clone() {
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

	/**
	 * Replaces the value at the specified index with the supplied value.
	 * 
	 * @param index
	 *            the index of the value to be replaced.
	 * @param value
	 *            the value to replace the item at <code>index</code>.
	 * @return the item previously at the specified position.
	 * @throws IndexOutOfBoundsException if the index is invalid.
	 * @throws NullPointerException if the value is <code>null</code>.
	 */
	@Override
	public StyleValue set(int index, StyleValue value) {
		if (value == null) {
			throw new NullPointerException("Null value set to ValueList");
		}
		return super.set(index, value);
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
	public Match matches(CSSValueSyntax syntax) {
		Match result = Match.FALSE;
		if (!isEmpty() && syntax != null) {
			// If the list has one value, match directly on it
			if (getLength() == 1) {
				return item(0).matches(syntax);
			}
			// Check for universal
			if (syntax.getCategory() == Category.universal) {
				return Match.TRUE;
			}
			// Match according to multipliers (including implicit)
			do {
				Multiplier mult = syntax.getMultiplier();
				if (mult != Multiplier.PLUS) {
					Match match = ValueList.valuesMatch(iterator(), syntax);
					if (match == Match.TRUE) {
						return Match.TRUE;
					} else if (result == Match.FALSE) {
						result = match;
					}
				}
			} while ((syntax = syntax.getNext()) != null);
		}
		return result;
	}

}
