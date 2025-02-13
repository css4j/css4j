/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

class UnitlessFunctionUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	CSSValueSyntax syntax;

	public UnitlessFunctionUnitImpl(int functionIndex) {
		super(functionIndex);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		Dimension dim = new Dimension();
		dim.category = Category.number;
		return dim;
	}

	@Override
	UnitlessFunctionUnitImpl instantiateLexicalUnit() {
		return new UnitlessFunctionUnitImpl(getMathFunctionIndex());
	}

}
