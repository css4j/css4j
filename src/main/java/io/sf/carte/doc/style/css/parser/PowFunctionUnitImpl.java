/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

class PowFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public PowFunctionUnitImpl(int functionIndex) {
		super(functionIndex);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) throws DOMException {
		if (parameters == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Missing argument in pow() function.");
		}
		Dimension dim = analyzer.expressionDimension(parameters.shallowClone());
		if (dim != null) {
			if (dim.category != Category.number) {
				LexicalUnitImpl comma = parameters.nextLexicalUnit;
				if (comma == null || comma.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Expected comma in pow() function.");
				}
				LexicalUnitImpl expUnit = comma.nextLexicalUnit;
				if (expUnit == null) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Missing argument in pow() function.");
				}
				if (expUnit.getLexicalUnitType() == LexicalType.INTEGER) {
					dim.exponent = dim.exponent * expUnit.getIntegerValue();
				} else if (expUnit.getLexicalUnitType() == LexicalType.REAL) {
					dim.exponent = Math.round(dim.exponent * expUnit.getFloatValue());
				}
			} // No matter the exponent, a number is a number
		}
		return dim;
	}

	@Override
	PowFunctionUnitImpl instantiateLexicalUnit() {
		return new PowFunctionUnitImpl(getMathFunctionIndex());
	}

}
