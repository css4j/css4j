/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2017 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * Interface representing a {@code rect()} function.
 */
public interface CSSRectValue {

	/**
	 * The top of the rectangle.
	 * 
	 * @return the top of the rectangle.
	 */
	CSSPrimitiveValue getTop();

	/**
	 * The right of the rectangle.
	 * 
	 * @return the right of the rectangle.
	 */
	CSSPrimitiveValue getRight();

	/**
	 * The bottom of the rectangle.
	 * 
	 * @return the bottom of the rectangle.
	 */
	CSSPrimitiveValue getBottom();

	/**
	 * The left of the rectangle.
	 * 
	 * @return the left of the rectangle.
	 */
	CSSPrimitiveValue getLeft();

}
