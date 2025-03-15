/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.impl.CSSUtil;

class EnvUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public EnvUnitImpl() {
		super(LexicalType.ENV);
	}

	@Override
	EnvUnitImpl instantiateLexicalUnit() {
		return new EnvUnitImpl();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		LexicalUnitImpl param = parameters;
		String name = param.getStringValue();
		LexicalUnitImpl fallback;
		do {
			param = param.nextLexicalUnit;
			if (param == null) {
				fallback = null;
				break;
			}
			if (param.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
				fallback = param.nextLexicalUnit;
				break;
			}
		} while (true);

		return CSSUtil.matchEnv(rootSyntax, syntax, name, fallback);
	}

}
