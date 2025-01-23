/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * This interface listens to properties found by NSAC event handlers.
 *
 * @author Carlos Amengual
 *
 */
interface LexicalPropertyListener {

	/**
	 * Set a CSS property, based on lexical value.
	 * 
	 * @param propertyName the name of the property.
	 * @param value        the lexical value.
	 * @param important    <code>true</code> if the priority is important.
	 * 
	 * @throws DOMException if some error or inconsistency is found in the value.
	 */
	void setProperty(String propertyName, LexicalUnit value, boolean important) throws DOMException;

	/**
	 * Set a CSS property as a raw lexical value.
	 * 
	 * @param propertyName the name of the property.
	 * @param value        the lexical value.
	 * @param important    <code>true</code> if the priority is important.
	 * 
	 * @throws DOMException if some error or inconsistency is found in the value.
	 */
	void setLexicalProperty(String propertyName, LexicalUnit value, boolean important) throws DOMException;

}
