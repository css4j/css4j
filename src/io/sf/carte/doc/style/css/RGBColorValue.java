/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.property.LABColorValue;

/**
 * Represents a RGB color value.
 */
public interface RGBColorValue extends CSSColorValue {

	/**
	 * Get the RGB(A) color represented by this value.
	 *
	 * @return the RGB color.
	 * @deprecated
	 * @see #getRGBColor()
	 */
	@Deprecated
	default RGBAColor getRGBColorValue() {
		return getRGBColor();
	}

	/**
	 * Get the RGB(A) color represented by this value.
	 *
	 * @return the RGB color.
	 */
	RGBAColor getRGBColor();

	/**
	 * Convert this value to a {@link LABColorValue}, if possible.
	 * 
	 * @return the converted {@code LABColorValue}.
	 * @throws DOMException INVALID_STATE_ERR if the components cannot be converted.
	 */
	LABColorValue toLABColorValue() throws DOMException;

	@Override
	RGBColorValue clone();

}
