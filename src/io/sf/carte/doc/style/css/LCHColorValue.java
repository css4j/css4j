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
 * Represents a {@code lch()} color value.
 */
public interface LCHColorValue extends CSSColorValue {

	/**
	 * Get the {@code lch()} color represented by this value.
	 *
	 * @return the {@code lch()} color value.
	 */
	LCHColor getLCHColorValue();

	/**
	 * Convert this value to {@code lab()}.
	 * 
	 * @return the converted value.
	 * @throws DOMException INVALID_STATE_ERR if the value cannot be converted.
	 */
	LABColorValue toLABColorValue() throws DOMException;

	@Override
	LCHColorValue clone();

}
