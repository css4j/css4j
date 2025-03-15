/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class BasicShapeUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public BasicShapeUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	public int getContextIndex() {
		return getLexicalUnitType().ordinal() - LexicalType.RECT_FUNCTION.ordinal();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case basicShape:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	BasicShapeUnitImpl instantiateLexicalUnit() {
		return new BasicShapeUnitImpl(getLexicalUnitType());
	}

}
