/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.property.LCHColorValue;

/**
 * Represents a Lab ({@code lab()}) color value.
 */
public interface LABColorValue extends CSSColorValue {

	/**
	 * Get the {@code lab()} color represented by this value.
	 *
	 * @return the {@code lab()} color.
	 */
	LABColor getLABColor();

	/**
	 * Convert this value to {@code lch()}.
	 * 
	 * @return the converted value.
	 * @throws DOMException INVALID_STATE_ERR if the value cannot be converted.
	 */
	LCHColorValue toLCHColorValue() throws DOMException;

	@Override
	LABColorValue clone();

}
