/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.uparser.TokenProducer;

class ElementReferenceTH extends FunctionCallbackTH {

	ElementReferenceTH(LexicalProvider caller) {
		super(caller);
	}

	private boolean expectIdentifier = false;

	@Override
	protected void initializeBuffer() {
		buffer = new StringBuilder(20);
	}

	@Override
	public void word(int index, CharSequence word) {
		if (expectIdentifier) {
			super.word(index, word);
		} else if (currentlu.value == null && (ParseHelper.equalsIgnoreCase(word, "var")
				|| ParseHelper.equalsIgnoreCase(word, "attr"))) {
			FunctionCallbackValueTH h = new FunctionCallbackValueTH();
			h.setCurrentLexicalUnit(currentlu);
			h.word(index, word);
			yieldHandling(h);
		} else {
			unexpectedTokenError(index, word);
		}
	}

	@Override
	public void escaped(int index, int codePoint) {
		if (expectIdentifier) {
			super.escaped(index, codePoint);
		} else if (currentlu.value == null
				&& (codePoint == 'v' || codePoint == 'a' || codePoint == '6' || codePoint == '7')) {
			// Possible escaped \var, \attr, or hex \61 / \76
			FunctionCallbackValueTH h = new FunctionCallbackValueTH();
			h.setCurrentLexicalUnit(currentlu);
			h.escaped(index, codePoint);
			yieldHandling(h);
		} else {
			unexpectedCharError(index, codePoint);
		}
	}

	@Override
	void processBuffer(int index, int triggerCp) {
		int buflen = buffer.length();
		if (buflen != 0) {
			if (currentlu.value == null) {
				currentlu.value = unescapeBuffer(index);
				expectIdentifier = false;
			} else {
				unexpectedCharError(index, triggerCp);
			}
		} else if (expectIdentifier) {
			unexpectedCharError(index, triggerCp);
		}
	}

	@Override
	public void character(int index, int codePoint) {
		if (currentlu.value == null) {
			prevcp = 65;
			if (codePoint == 45) { // -
				buffer.append('-');
				return;
			} else if (codePoint == '_') {
				buffer.append('_');
				return;
			} else if (codePoint == '#') {
				if (!expectIdentifier) {
					expectIdentifier = true;
					return;
				}
			}
		}
		unexpectedCharError(index, codePoint);
	}

	@Override
	public void endFunctionArgument(int index) {
		if (!expectIdentifier && (currentlu.value != null || currentlu.parameters != null)) {
			super.endFunctionArgument(index);
		} else {
			unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
		}
	}

}
