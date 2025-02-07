/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

class SqrtFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public SqrtFunctionUnitImpl(int functionIndex) {
		super(functionIndex);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		Dimension dim = analyzer.expressionDimension(parameters);
		if (dim != null) {
			if (dim.exponent % 2 != 0) {
				// Odd number
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Invalid CSS unit in sqrt() function");
			}
			dim.exponent = dim.exponent / 2;
		}
		return dim;
	}

	@Override
	SqrtFunctionUnitImpl instantiateLexicalUnit() {
		return new SqrtFunctionUnitImpl(getMathFunctionIndex());
	}

}
