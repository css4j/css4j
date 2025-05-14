/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class VarFunctionUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public VarFunctionUnitImpl() {
		super(LexicalType.VAR);
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		return syntax.getCategory() == Category.universal ? Match.TRUE : Match.PENDING;
	}

	@Override
	VarFunctionUnitImpl instantiateLexicalUnit() {
		return new VarFunctionUnitImpl();
	}

}
