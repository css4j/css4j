/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

class BorderWidthShorthandSetter extends BoxShorthandSetter {
	BorderWidthShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "border-width");
	}

	@Override
	boolean isIdentifierValue(String lcIdent) {
		return getShorthandDatabase().isIdentifierValue("border-width", lcIdent);
	}

}