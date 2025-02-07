/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

class RoundFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public RoundFunctionUnitImpl(int functionIndex) {
		super(functionIndex);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		if (parameters == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Missing arguments in round() function.");
		}
		LexicalUnitImpl arg = parameters;
		switch (arg.getLexicalUnitType()) {
		case IDENT:
			// Comma
			arg = arg.nextLexicalUnit;
			if (arg == null || (arg = arg.nextLexicalUnit) == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Missing argument in round() function.");
			}
			break;
		case VAR:
			return null;
		default:
			break;
		}

		if (arg.nextLexicalUnit != null) {
			arg = arg.shallowClone();
		}

		return analyzer.expressionDimension(arg);
	}

	@Override
	RoundFunctionUnitImpl instantiateLexicalUnit() {
		return new RoundFunctionUnitImpl(getMathFunctionIndex());
	}

}
