/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

class PowFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public PowFunctionUnitImpl(MathFunction functionID) {
		super(functionID);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) throws DOMException {
		if (parameters == null) {
			throw new DOMSyntaxException("Missing argument in pow() function.");
		}
		Dimension dim = analyzer.expressionDimension(parameters);
		if (dim != null) {
			if (dim.category != Category.number && dim.category != Category.integer) {
				LexicalUnitImpl comma = analyzer.getNextLexicalUnit();
				if (comma == null || comma.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
					throw new DOMSyntaxException("Expected comma in pow() function.");
				}
				LexicalUnitImpl expUnit = comma.nextLexicalUnit;
				if (expUnit == null) {
					throw new DOMSyntaxException("Missing argument in pow() function.");
				}
				switch (expUnit.getLexicalUnitType()) {
				case INTEGER:
					dim.exponent = dim.exponent * expUnit.getIntegerValue();
					break;
				case REAL:
					dim.exponent = Math.round(dim.exponent * expUnit.getFloatValue());
					dim.exponentAccuracy = 1; // Just in case
					break;
				case VAR:
					dim.exponentAccuracy = 2;
					break;
				case FUNCTION:
				case ATTR:
				case CALC:
				case SUB_EXPRESSION:
				case MATH_FUNCTION:
					DimensionalAnalyzer subAnal = new DimensionalAnalyzer();
					Dimension dimexp = subAnal.expressionDimension(expUnit);
					if (subAnal.getNextLexicalUnit() != null) {
						throw new DOMSyntaxException("Unexpected argument in pow() function: "
								+ subAnal.getNextLexicalUnit().toString());
					}
					if (dimexp == null) {
						dim.exponentAccuracy = 2;
						break;
					}
					if (dimexp.category == Category.number || dimexp.category == Category.integer) {
						if (dimexp.exponentAccuracy == 2) {
							dim.exponentAccuracy = 2;
						} else {
							dim.exponentAccuracy = 1;
						}
						break;
					}
				default:
					throw new DOMSyntaxException(
							"Invalid argument in pow() function: " + expUnit.getCssText());
				}
			} // No matter the exponent, a number is a number
		}
		return dim;
	}

	@Override
	PowFunctionUnitImpl instantiateLexicalUnit() {
		return new PowFunctionUnitImpl(getMathFunction());
	}

}
