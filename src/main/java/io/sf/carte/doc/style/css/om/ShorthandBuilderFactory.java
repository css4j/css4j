/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

interface ShorthandBuilderFactory {

	/**
	 * Create a setter appropriate for the given shorthand property, value and
	 * priority.
	 * 
	 * @param style     the style declaration where the shorthand belongs.
	 * @param shorthand the shorthand property name.
	 * @return the builder.
	 */
	ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand);

}
