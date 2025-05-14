/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class AnchorSizeUnitImpl extends MathFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	CSSValueSyntax syntax;

	public AnchorSizeUnitImpl() {
		super(MathFunction.ANCHOR_SIZE);
	}

	@Override
	public Dimension dimension(DimensionalAnalyzer analyzer) {
		Dimension dim = new Dimension();
		dim.category = Category.length;
		dim.exponent = 1;
		return dim;
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		Category cat = syntax.getCategory();
		return cat == Category.length || cat == Category.lengthPercentage ? Match.TRUE
				: Match.FALSE;
	}

	@Override
	AnchorSizeUnitImpl instantiateLexicalUnit() {
		return new AnchorSizeUnitImpl();
	}

}
