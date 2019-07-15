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

import org.w3c.dom.css.RGBColor;

/**
 * Adds an alpha channel to W3C's {@link RGBColor} interface.
 */
public interface RGBAColor extends RGBColor {

	/**
	 * Enumeration of color spaces.
	 */
	public enum ColorSpace {RGB, HSL, HWB}

	/**
	 * Get the alpha channel of this colour.
	 * 
	 * @return the alpha channel.
	 */
	ExtendedCSSPrimitiveValue getAlpha();
}
