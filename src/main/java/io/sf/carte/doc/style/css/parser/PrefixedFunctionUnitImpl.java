/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class PrefixedFunctionUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public PrefixedFunctionUnitImpl() {
		super(LexicalType.PREFIXED_FUNCTION);
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		return Match.FALSE;
	}

	@Override
	Match getUniversalMatch() {
		return Match.FALSE;
	}

	@Override
	PrefixedFunctionUnitImpl instantiateLexicalUnit() {
		return new PrefixedFunctionUnitImpl();
	}

}
