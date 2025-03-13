/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;

class ScalingFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public ScalingFunctionUnitImpl(MathFunction functionID) {
		super(functionID);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		return analyzer.expressionDimension(parameters);
	}

	@Override
	ScalingFunctionUnitImpl instantiateLexicalUnit() {
		return new ScalingFunctionUnitImpl(getMathFunction());
	}

}
