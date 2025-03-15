/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class ImageFunctionUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public ImageFunctionUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	public int getContextIndex() {
		return getLexicalUnitType().ordinal() - LexicalType.GRADIENT.ordinal();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case image:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	ImageFunctionUnitImpl instantiateLexicalUnit() {
		return new ImageFunctionUnitImpl(getLexicalUnitType());
	}

}
