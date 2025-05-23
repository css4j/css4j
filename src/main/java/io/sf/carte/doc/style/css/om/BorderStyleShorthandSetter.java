/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class BorderStyleShorthandSetter extends BoxShorthandSetter {
	BorderStyleShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "border-style");
	}

	@Override
	boolean isValueOfType(LexicalUnit value) {
		return false;
	}

	@Override
	boolean isIdentifierValue(String lcIdent) {
		return "none".equals(lcIdent) || getShorthandDatabase().isIdentifierValue("border-style", lcIdent);
	}

}