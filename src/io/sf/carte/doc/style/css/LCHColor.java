/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a {@code lch()} color.
 */
public interface LCHColor {

	/**
	 * Get the lightness of this color.
	 * 
	 * @return the lightness component.
	 */
	CSSPrimitiveValue getLightness();

	/**
	 * Get the chroma of this color.
	 * 
	 * @return the chroma component.
	 */
	CSSPrimitiveValue getChroma();

	/**
	 * Get the hue of this color.
	 * 
	 * @return the hue component.
	 */
	CSSPrimitiveValue getHue();

	/**
	 * Get the alpha channel of this color.
	 * 
	 * @return the alpha channel.
	 */
	CSSPrimitiveValue getAlpha();

	LCHColor clone();

}
