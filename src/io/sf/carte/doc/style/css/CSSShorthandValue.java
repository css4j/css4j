/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.util.Set;

/**
 * Shorthand value.
 * <p>
 * These values are of {@link CSSValue.CssType#SHORTHAND SHORTHAND} type.
 */
public interface CSSShorthandValue extends CSSValue {

	/**
	 * Is the value of important priority ?
	 * 
	 * @return <code>true</code> if the priority is important.
	 */
	boolean isImportant();

	/**
	 * Get the set of longhand properties that this shorthand is responsible for.
	 * 
	 * @return the set of longhand properties.
	 */
	Set<String> getLonghands();

	@Override
	default CssType getCssValueType() {
		return CssType.SHORTHAND;
	}

	@Override
	default Type getPrimitiveType() {
		return Type.INVALID;
	}

	@Override
	CSSShorthandValue clone();

}
