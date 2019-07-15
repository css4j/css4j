/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

/**
 * Build a margin shorthand from individual properties.
 */
class PaddingBuilder extends BoxShorthandBuilder {

	PaddingBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("padding", parentStyle);
	}

}
