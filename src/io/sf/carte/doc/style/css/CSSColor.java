/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * A color.
 */
public interface CSSColor {

	/**
	 * Get the alpha channel of this color.
	 * 
	 * @return the alpha channel.
	 */
	CSSPrimitiveValue getAlpha();

	/**
	 * Clone this color.
	 * 
	 * @return a clone of this color.
	 */
	CSSColor clone();

}
