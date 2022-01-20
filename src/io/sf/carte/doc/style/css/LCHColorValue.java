/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a {@code lch()} color value.
 * <p>
 * Instead of using this interface, you may want to just cast the result of
 * {@link CSSColorValue#getColor()} to the appropriate interface (like
 * {@link LCHColor}) according to the given
 * {@link CSSColorValue#getColorModel()}.
 * </p>
 */
public interface LCHColorValue extends CSSColorValue {

	/**
	 * Get the {@code lch()} color represented by this value.
	 *
	 * @return the {@code lch()} color.
	 */
	@Override
	CSSColor getColor();

	@Override
	LCHColorValue clone();

}
