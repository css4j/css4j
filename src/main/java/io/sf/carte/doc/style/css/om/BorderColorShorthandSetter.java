/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class BorderColorShorthandSetter extends BoxShorthandSetter {
	BorderColorShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "border-color");
	}

	@Override
	boolean isValueOfType(LexicalUnit value) {
		return testColor(value);
	}

	@Override
	boolean isIdentifierValue(String lcIdent) {
		return false;
	}

}