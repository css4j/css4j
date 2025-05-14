/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * Represents a ({@code color()}) value in a XYZ color model.
 */
public interface XYZColor extends CSSColor {

	/**
	 * Get the {@code X} component of this color.
	 * 
	 * @return the {@code X} component.
	 */
	CSSPrimitiveValue getX();

	/**
	 * Get the {@code Y} component of this color.
	 * 
	 * @return the {@code Y} component.
	 */
	CSSPrimitiveValue getY();

	/**
	 * Get the {@code Z} component of this color.
	 * 
	 * @return the {@code Z} component.
	 */
	CSSPrimitiveValue getZ();

	@Override
	XYZColor clone();

}
