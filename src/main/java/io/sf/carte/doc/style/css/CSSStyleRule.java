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

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * A CSS style rule.
 */
public interface CSSStyleRule extends CSSDeclarationRule, CSSGroupingRule {

	/**
	 * Get a parsable serialization of the selector(s).
	 * 
	 * @return a parsable serialization of the selector list.
	 */
	String getSelectorText();

	/**
	 * Parse the given string and set the selector list according to it.
	 * 
	 * @param selectorText a text representation of a selector list, according to
	 *                     CSS syntax.
	 * @throws DOMException
	 */
	void setSelectorText(String selectorText) throws DOMException;

	/**
	 * The selectors of this style rule.
	 * 
	 * @return the selector list.
	 */
	SelectorList getSelectorList();

	/**
	 * Set the selectors of this style rule.
	 * 
	 * @param selectorList the selector list.
	 * @throws NullPointerException if {@code selectorList} is null.
	 * @throws IllegalArgumentException if {@code selectorList} is empty.
	 */
	void setSelectorList(SelectorList selectorList);

}
