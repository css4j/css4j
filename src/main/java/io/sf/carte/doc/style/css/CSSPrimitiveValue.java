/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * A CSS primitive value.
 * <p>
 * Primitive values include two categories: {@link CSSValue.CssType#TYPED
 * TYPED} and {@link CSSValue.CssType#PROXY PROXY} values.
 */
public interface CSSPrimitiveValue extends CSSValue {

	/**
	 * This value is expected to contain an integer.
	 * <p>
	 * If this value is a non-integer number, an exception will be thrown
	 * immediately.
	 * </p>
	 * <p>
	 * If this value is a {@code calc()} that ever produces a non-integer number,
	 * the value shall be rounded to the nearest integer.
	 * </p>
	 * <p>
	 * If the value is a proxy that ever produces other than an integer number
	 * (without {@code calc()} involvement), an exception should be thrown later.
	 * </p>
	 * 
	 * @throws DOMException TYPE_MISMATCH_ERR if the value is a constant number and
	 *                      not an integer.
	 */
	void setExpectInteger() throws DOMException;

	/**
	 * Test whether this is a numeric value that is less than zero.
	 * 
	 * @return <code>true</code> if this is a numeric value and its value is
	 *         negative.
	 */
	default boolean isNegativeNumber() {
		return false;
	}

	/**
	 * If this is a numeric value, get its unit type.
	 * 
	 * @return the unit type, {@code CSS_INVALID} if the unit or type are not valid.
	 */
	default short getUnitType() {
		return CSSUnit.CSS_INVALID;
	}

}
