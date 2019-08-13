/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2019 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * A CSS unicode range value.
 */
public interface CSSUnicodeRangeValue extends ExtendedCSSPrimitiveValue {

	/**
	 * Get the beginning of this unicode range.
	 * <p>
	 * The value can be an integer or a wildcard value.
	 * 
	 * @return the value.
	 */
	CSSPrimitiveValue2 getValue();

	/**
	 * Get the end of this unicode range.
	 * 
	 * @return the range end value, or null if a single unicode value was specified.
	 */
	CSSPrimitiveValue2 getEndValue();

	@Override
	CSSUnicodeRangeValue clone();

	/**
	 * A CSS unicode character value.
	 */
	public interface CSSUnicodeValue extends CSSPrimitiveValue2, ExtendedCSSValue {

		void setCodePoint(int codePoint);

		int getCodePoint();

	}
}
