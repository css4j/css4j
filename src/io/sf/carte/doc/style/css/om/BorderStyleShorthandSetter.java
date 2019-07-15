/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

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
		return "none".equals(lcIdent) || getPropertyDatabase().isIdentifierValue("border-style", lcIdent);
	}

}