/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.uparser.TokenProducer;

class TypeFunctionTH extends FunctionCallbackTH {

	TypeFunctionTH(LexicalProvider caller) {
		super(caller);
	}

	@Override
	protected void initializeBuffer() {
		buffer = new StringBuilder(32);
	}

	@Override
	public void separator(int index, int codepoint) {
		if (getEscapedTokenIndex() != -1 && CSSParser.bufferEndsWithEscapedChar(buffer)) {
			buffer.append(' ');
		} else {
			setWhitespacePrevCp();
		}
	}

	@Override
	void processBuffer(int index, int triggerCp) {
		int buflen = buffer.length();
		if (buflen != 0) {
			assert currentlu.parameters == null;
			String syn = unescapeStringValue(index);
			buffer.setLength(0);
			SyntaxUnitImpl synLU = new SyntaxUnitImpl();
			try {
				synLU.syntax = new SyntaxParser().parseSyntax(syn);
			} catch (CSSException e) {
				handleError(index - buflen, ParseHelper.ERR_WRONG_VALUE,
						"Invalid syntax: " + syn);
			}
			currentlu.addFunctionParameter(synLU);
		}
	}

	@Override
	public void character(int index, int codePoint) {
		prevcp = 65;
		if (codePoint == '<' || codePoint == '>' || codePoint == TokenProducer.CHAR_HYPHEN_MINUS
				|| codePoint == TokenProducer.CHAR_VERTICAL_LINE) {
			buffer.append((char) codePoint);
		} else {
			unexpectedCharError(index, codePoint);
		}
	}

	@Override
	public void leftParenthesis(int index) {
		if (buffer.length() != 0) {
			String fname = unescapeBuffer(index);
			if ("var".equalsIgnoreCase(fname) || "attr".equalsIgnoreCase(fname)) {
				FunctionCallbackValueTH h = new FunctionCallbackValueTH();
				h.setCurrentLexicalUnit(currentlu);
				h.word(index, fname);
				h.leftParenthesis(index);
				yieldHandling(h);
				return;
			}
		}
		super.leftParenthesis(index);
	}

	@Override
	public void endFunctionArgument(int index) {
		if (currentlu.parameters != null) {
			super.endFunctionArgument(index);
		} else {
			unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
		}
	}

}
