/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Represents a color value with the Lab color model, like ({@code lab()}) or
 * {@code oklab()}.
 * <p>
 * It is not guaranteed that all colors with a LCh model shall implement this
 * interface, therefore its usage is not recommended.
 * </p>
 * 
 * @deprecated Instead of using this interface, just cast the result of
 *             {@link CSSColorValue#getColor()} to the appropriate interface
 *             (like {@link LABColor}) according to the given
 *             {@link CSSColorValue#getColorModel()}.
 *
 */
@Deprecated
public interface LABColorValue extends CSSColorValue {

	/**
	 * Get the {@code lab()} or {@code oklab()} color represented by this value.
	 *
	 * @return the {@code lab()} or {@code oklab()} color.
	 */
	@Override
	LABColor getColor();

	@Override
	LABColorValue clone();

}
