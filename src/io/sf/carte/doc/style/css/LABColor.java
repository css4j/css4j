/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a LAB ({@code lab()}) color.
 */
public interface LABColor extends CSSColor {

	/**
	 * Get the lightness of this color.
	 * 
	 * @return the lightness component.
	 */
	CSSPrimitiveValue getLightness();

	/**
	 * Get the {@code a} (green-red) axis in the LAB space for this color.
	 * 
	 * @return the {@code a} component.
	 */
	CSSPrimitiveValue getA();

	/**
	 * Get the {@code b} (blue–yellow) axis in the LAB space for this color.
	 * 
	 * @return the {@code b} component.
	 */
	CSSPrimitiveValue getB();

	@Override
	LABColor clone();

}
