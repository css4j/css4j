/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;

class SqrtFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public SqrtFunctionUnitImpl(MathFunction functionID) {
		super(functionID);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		Dimension dim = analyzer.expressionDimension(parameters);
		if (dim != null) {
			if (dim.exponent % 2 != 0) {
				// Odd number
				if (dim.exponentAccuracy == 0) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Invalid CSS unit in sqrt() function");
				}
			} else {
				dim.exponent = dim.exponent / 2;
			}
		}
		return dim;
	}

	@Override
	SqrtFunctionUnitImpl instantiateLexicalUnit() {
		return new SqrtFunctionUnitImpl(getMathFunction());
	}

}
