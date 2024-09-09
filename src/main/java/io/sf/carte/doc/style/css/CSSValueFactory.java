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
	 * Creates a primitive value according to the given lexical value.
	 * <p>
	 * This method won't return a ratio value (callers must check for values
	 * spanning more than one lexical unit).
	 * </p>
	 * <p>
	 * The behavior when the lexical unit is an operator is
	 * implementation-dependent, but operators that aren't parameters nor
	 * sub-values, unless in {@code content} context, should throw exceptions.
	 * </p>
	 * 
	 * @param lunit the lexical value.
	 * @return the primitive value.
	 * @throws DOMException if the lexical unit does not represent a valid
	 *                      primitive.
	 */
	CSSPrimitiveValue createCSSPrimitiveValue(LexicalUnit lunit)
			throws DOMException;

}
