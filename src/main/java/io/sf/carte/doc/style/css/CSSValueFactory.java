/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * Factory of CSS values.
 */
public interface CSSValueFactory {

	/**
	 * Create an object-model value.
	 * 
	 * @param lunit the lexical unit.
	 * @return the object-model value.
	 */
	CSSValue createCSSValue(LexicalUnit lunit);

	/**
	 * Creates a value item according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return
	 * null.
	 * </p>
	 * 
	 * @param lunit the lexical value.
	 * @param subp  the flag marking whether it is a sub-property.
	 * @return the value item for the CSS primitive value.
	 * @throws DOMException if a problem was found setting the lexical value to a
	 *                      CSS primitive.
	 */
	CSSPrimitiveValueItem createCSSPrimitiveValueItem(LexicalUnit lunit, boolean subp)
			throws DOMException;

}
