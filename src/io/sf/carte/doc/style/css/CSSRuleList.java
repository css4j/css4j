/*
 * This software extends interfaces defined by CSS Object Model
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.io.IOException;
import java.util.Iterator;

import io.sf.carte.util.SimpleWriter;

/**
 * A rule list with additional utility methods.
 */
public interface CSSRuleList<T extends CSSRule> extends org.w3c.dom.css.CSSRuleList, Iterable<T> {

	/**
	 * Remove all the rules in this list.
	 */
	void clear();

	/**
	 * Retrieve a CSS rule by ordinal index. The order in this collection represents
	 * the order of the rules in the CSS style sheet.
	 * 
	 * @param index the index in the collection.
	 * @return the rule at the <code>index</code> position, or <code>null</code> if
	 *         the index is less than zero or equal or greater to the list length.
	 */
	@Override
	T item(int index);

	@Override
	Iterator<T> iterator();

	String toMinifiedString();

	void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException;

}