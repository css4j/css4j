/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2011-2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * Based on W3C's {@code RGBColor} interface.
 */
public interface RGBAColor {

	/**
	 * Get the red component of this colour.
	 * 
	 * @return the red component.
	 */
	CSSPrimitiveValue getRed();

	/**
	 * Get the green component of this colour.
	 * 
	 * @return the green component.
	 */
	CSSPrimitiveValue getGreen();

	/**
	 * Get the blue component of this colour.
	 * 
	 * @return the blue component.
	 */
	CSSPrimitiveValue getBlue();

	/**
	 * Get the alpha channel of this colour.
	 * 
	 * @return the alpha channel.
	 */
	CSSPrimitiveValue getAlpha();
}
