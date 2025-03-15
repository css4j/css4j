/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class SubExpressionUnitImpl extends ExpressionUnitImpl {

	private static final long serialVersionUID = 1L;

	public SubExpressionUnitImpl() {
		super(LexicalType.SUB_EXPRESSION);
	}

	@Override
	CharSequence currentToString() {
		StringBuilder buf = new StringBuilder(20);
		boolean saExpr = (previousLexicalUnit == null
				|| previousLexicalUnit.getLexicalUnitType() == LexicalType.OPERATOR_COMMA)
				&& (nextLexicalUnit == null
						|| nextLexicalUnit.getLexicalUnitType() == LexicalType.OPERATOR_COMMA);
		if (!saExpr) {
			buf.append('(');
		}
		LexicalUnit lu = this.parameters;
		if (lu != null) {
			buf.append(lu.toString());
		}
		if (!saExpr) {
			buf.append(')');
		}
		return buf;
	}

	@Override
	SubExpressionUnitImpl instantiateLexicalUnit() {
		return new SubExpressionUnitImpl();
	}

}
