/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.TransformFunctions;

class TransformFunctionUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	private final TransformFunctions functionId;

	public TransformFunctionUnitImpl(TransformFunctions functionId) {
		super(LexicalType.TRANSFORM_FUNCTION);
		this.functionId = functionId;
	}

	@Override
	public TransformFunctions getTransformFunction() {
		return functionId;
	}

	@Override
	public int getContextIndex() {
		return functionId.ordinal();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case transformFunction:
		case transformList:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	TransformFunctionUnitImpl instantiateLexicalUnit() {
		return new TransformFunctionUnitImpl(getTransformFunction());
	}

}
