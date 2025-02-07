/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

/**
 * Functions that output an angle.
 */
class AngleFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public AngleFunctionUnitImpl(int functionIndex) {
		super(functionIndex);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		Dimension dim = new Dimension();
		dim.category = Category.angle;
		dim.exponent = 1;
		return dim;
	}

	@Override
	AngleFunctionUnitImpl instantiateLexicalUnit() {
		return new AngleFunctionUnitImpl(getMathFunctionIndex());
	}

}
