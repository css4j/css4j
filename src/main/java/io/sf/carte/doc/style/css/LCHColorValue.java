/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a color value with the LCh color model, like {@code lch()} or
 * {@code oklch()}.
 * <p>
 * It is not guaranteed that all colors with a LCh model shall implement this
 * interface, therefore its usage is not recommended.
 * </p>
 * 
 * @deprecated Instead of using this interface, just cast the result of
 *             {@link CSSColorValue#getColor()} to the appropriate interface
 *             (like {@link LCHColor}) according to the given
 *             {@link CSSColorValue#getColorModel()}.
 */
@Deprecated
public interface LCHColorValue extends CSSColorValue {

	/**
	 * Get the {@code lch()} or {@code oklch()} color represented by this value.
	 *
	 * @return the {@code lch()} or {@code oklch()} color.
	 */
	@Override
	LCHColor getColor();

	@Override
	LCHColorValue clone();

}
