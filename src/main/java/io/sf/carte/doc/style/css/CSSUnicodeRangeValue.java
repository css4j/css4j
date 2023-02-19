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
public interface CSSUnicodeRangeValue extends CSSTypedValue {

	/**
	 * Get the beginning of this unicode range.
	 * <p>
	 * The value can be an integer or a wildcard value.
	 * 
	 * @return the value.
	 */
	CSSTypedValue getValue();

	/**
	 * Get the end of this unicode range.
	 * 
	 * @return the range end value, or null if a single unicode value was specified.
	 */
	CSSTypedValue getEndValue();

	@Override
	CSSUnicodeRangeValue clone();

	/**
	 * A CSS unicode character value.
	 */
	public interface CSSUnicodeValue extends CSSTypedValue {

		/**
		 * Set the value of the unicode codepoint.
		 * 
		 * @param codePoint the codepoint.
		 */
		void setCodePoint(int codePoint);

		/**
		 * Get the value of the unicode codepoint.
		 * @return the codepoint.
		 */
		int getCodePoint();

	}
}
