/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a Lab ({@code lab()}) color value.
 * <p>
 * Instead of using this interface, you may want to just cast the result of
 * {@link CSSColorValue#getColor()} to the appropriate interface (like
 * {@link LABColor}) according to the given
 * {@link CSSColorValue#getColorModel()}.
 * </p>
 */
public interface LABColorValue extends CSSColorValue {

	/**
	 * Get the {@code lab()} color represented by this value.
	 *
	 * @return the {@code lab()} color.
	 */
	@Override
	LABColor getColor();

	@Override
	LABColorValue clone();

}
