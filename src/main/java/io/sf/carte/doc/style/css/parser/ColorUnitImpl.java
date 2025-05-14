/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class ColorUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public ColorUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	public int getContextIndex() {
		return getLexicalUnitType().ordinal() - LexicalType.RGBCOLOR.ordinal();
	}

	@Override
	ColorUnitImpl instantiateLexicalUnit() {
		return new ColorUnitImpl(getLexicalUnitType());
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case color:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

}
