/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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
	 * Check whether this value is primitive, that is, either a
	 * {@link io.sf.carte.doc.style.css.CSSValue.CssType#TYPED TYPED} or
	 * {@link io.sf.carte.doc.style.css.CSSValue.CssType#PROXY PROXY} value.
	 * 
	 * @return {@code true} if the value is {@code TYPED} or {@code PROXY}.
	 */
	@Override
	default boolean isPrimitiveValue() {
		return true;
	}

	/**
	 * If this is a {@link CSSValue.Type#NUMERIC NUMERIC} value, get its unit type.
	 * <p>
	 * You can also find the unit types of mathematical functions and {@code calc()}
	 * expressions, although this may be computationally expensive:
	 * </p>
	 * <ul>
	 * <li>For {@code calc()} expressions please use
	 * {@link CSSExpressionValue#computeUnitType()}.</li>
	 * <li>For mathematical functions use
	 * {@link CSSMathFunctionValue#computeUnitType()}.</li>
	 * </ul>
	 * 
	 * @return the unit type as in {@link CSSUnit}, or {@code CSS_INVALID} if the
	 *         type is not numeric or the unit is not valid.
	 */
	default short getUnitType() {
		return CSSUnit.CSS_INVALID;
	}

}
