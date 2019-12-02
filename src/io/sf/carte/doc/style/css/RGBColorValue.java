/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a RGB color value.
 */
public interface RGBColorValue extends CSSColorValue {

	/**
	 * Get the RGB(A) color represented by this value.
	 *
	 * @return the RGB color value.
	 */
	RGBAColor getRGBColorValue();

	@Override
	RGBColorValue clone();

}