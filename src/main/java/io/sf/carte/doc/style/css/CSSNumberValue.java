/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * A value that is a number with an optional unit.
 */
public interface CSSNumberValue extends CSSTypedValue {

	/**
	 * Sets whether this number is the result of a calculation.
	 * 
	 * @param calculated {@code true} if this number was calculated.
	 */
	void setCalculatedNumber(boolean calculated);

	/**
	 * Round this value to the nearest integer.
	 * 
	 * @throws DOMException TYPE_MISMATCH_ERR if this value is not a plain
	 *                      {@code <number>}.
	 */
	void roundToInteger() throws DOMException;

	/**
	 * Set the maximum fraction digits to use in the serialization of this number
	 * when it was not specified or is the result of a calculation.
	 * 
	 * @param maxFractionDigits the maximum fraction digits.
	 */
	void setMaximumFractionDigits(int maxFractionDigits);

}
