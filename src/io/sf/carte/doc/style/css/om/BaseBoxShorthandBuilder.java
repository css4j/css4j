/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Base logic for building box-like shorthands.
 * <p>
 * Although border-box properties share similarities with margin and padding, the cases
 * are enough different that only a minimal amount of code is shared.
 */
abstract class BaseBoxShorthandBuilder extends ShorthandBuilder {

	BaseBoxShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super(shorthandName, parentStyle);
	}

	/**
	 * 
	 * @param value
	 * @return 1 if the value is inherit, 5 if unset, 0 otherwise.
	 */
	byte keywordState(StyleValue value) {
		if (isInherit(value)) {
			return 1;
		}
		if (isCssKeywordValue("unset", value)) {
			return 5;
		}
		return 0;
	}

}
