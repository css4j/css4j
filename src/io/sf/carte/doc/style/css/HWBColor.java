/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a hwb color.
 */
public interface HWBColor {

	/**
	 * Get the hue of this color.
	 * 
	 * @return the hue component.
	 */
	CSSPrimitiveValue getHue();

	/**
	 * Get the whiteness of this color.
	 * 
	 * @return the whiteness component.
	 */
	CSSPrimitiveValue getWhiteness();

	/**
	 * Get the blackness of this color.
	 * 
	 * @return the blackness component.
	 */
	CSSPrimitiveValue getBlackness();

	/**
	 * Get the alpha channel of this color.
	 * 
	 * @return the alpha channel.
	 */
	CSSPrimitiveValue getAlpha();

	HWBColor clone();

}
