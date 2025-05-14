/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

/**
 * Build a margin shorthand from individual properties.
 */
class PaddingBuilder extends BoxShorthandBuilder {

	PaddingBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("padding", parentStyle);
	}

}
