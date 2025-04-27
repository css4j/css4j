/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.uparser.TokenProducer;

/**
 * Callbacks that are finished by a right parenthesis can extend this.
 */
abstract class FunctionCallbackTH extends LexicalCallbackTH {

	FunctionCallbackTH(LexicalProvider caller) {
		super(caller);
		parendepth = 1;
	}

	@Override
	public void setCurrentLexicalUnit(LexicalUnitImpl currentlu) {
		this.currentlu = currentlu;
	}

	/**
	 * Add a non-function (nor expression) lexical unit as the current value.
	 * 
	 * @param lu the lexical unit to add.
	 * @return the lexical unit that should be processed as the current unit.
	 */
	@Override
	public LexicalUnitImpl addPlainLexicalUnit(LexicalUnitImpl lu) {
		currentlu.addFunctionParameter(lu);
		return lu;
	}

	@Override
	LexicalUnitImpl addFunctionOrExpressionUnit(LexicalUnitImpl lu) {
		currentlu.addFunctionParameter(lu);
		return lu;
	}

	@Override
	public boolean isCurrentUnitAFunction() {
		return true;
	}

	@Override
	public void rightSquareBracket(int index) {
		processBuffer(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
		caller.unexpectedRightSquareBracketError(index);
	}

	@Override
	public void rightCurlyBracket(int index) {
		processBuffer(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
		caller.unexpectedRightCurlyBracketError(index);
	}

	@Override
	public void rightParenthesis(int index) {
		processBuffer(index, ')');
		// Decrease caller parentheses depth, which must be 1 or higher,
		// otherwise this handler would have not been instantiated.
		// So we call decrParenDepth() which does not check the depth.
		caller.decrParenDepth();
		if (!isInError()) {
			endFunctionArgument(index);
			if (!isInError()) {
				yieldBack();
			}
		}
	}

	@Override
	public void endFunctionArgument(int index) {
		if (currentlu != null && currentlu.ownerLexicalUnit != null) {
			currentlu = currentlu.ownerLexicalUnit;
		} else {
			getCaller().endFunctionArgument(index);
		}
	}

	class FunctionCallbackValueTH extends CallbackValueTokenHandler {

		FunctionCallbackValueTH() {
			super();
			parendepth = FunctionCallbackTH.this.parendepth;
			functionToken = true;
		}

		@Override
		public boolean isCurrentUnitAFunction() {
			return true;
		}

		@Override
		protected void checkFunctionCallback(int index) {
			if (parendepth <= 0 && !isInError()) {
				FunctionCallbackTH.this.setCurrentLexicalUnit(getCurrentLexicalUnit());
				FunctionCallbackTH.this.rightParenthesis(index);
			}
		}

	}

}
