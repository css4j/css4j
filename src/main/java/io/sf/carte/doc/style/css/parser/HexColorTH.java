/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.uparser.TokenProducer;

class HexColorTH extends LexicalCallbackTH {

	HexColorTH(LexicalProvider caller) {
		super(caller);
	}

	@Override
	public void word(int index, CharSequence word) {
		if (!parseHexColor(word)) {
			unexpectedTokenError(index, word);
		} else {
			yieldBack();
		}
	}

	private boolean parseHexColor(CharSequence word) {
		int buflen = word.length();
		try {
			if (buflen == 3) {
				addFunctionOrExpressionUnit(new RGBColorUnitImpl());
				currentlu.value = "rgb";
				parseHexComponent(word, 0, 1, true);
				parseHexComponent(word, 1, 2, true);
				parseHexComponent(word, 2, 3, true);
				recoverOwnerUnit(word);
			} else if (buflen == 6) {
				addFunctionOrExpressionUnit(new RGBColorUnitImpl());
				currentlu.value = "rgb";
				parseHexComponent(word, 0, 2, false);
				parseHexComponent(word, 2, 4, false);
				parseHexComponent(word, 4, 6, false);
				recoverOwnerUnit(word);
			} else if (buflen == 8) {
				addFunctionOrExpressionUnit(new RGBColorUnitImpl());
				currentlu.value = "rgb";
				parseHexComponent(word, 0, 2, false);
				parseHexComponent(word, 2, 4, false);
				parseHexComponent(word, 4, 6, false);
				int comp = hexComponent(word, 6, 8, false);
				newFunctionArgument(LexicalType.OPERATOR_SLASH);
				newNumberArgument(LexicalType.REAL).floatValue = comp / 255f;
				recoverOwnerUnit(word);
			} else if (buflen == 4) {
				addFunctionOrExpressionUnit(new RGBColorUnitImpl());
				currentlu.value = "rgb";
				parseHexComponent(word, 0, 1, true);
				parseHexComponent(word, 1, 2, true);
				parseHexComponent(word, 2, 3, true);
				int comp = hexComponent(word, 3, 4, true);
				newFunctionArgument(LexicalType.OPERATOR_SLASH);
				newNumberArgument(LexicalType.REAL).floatValue = comp / 255f;
				recoverOwnerUnit(word);
			} else {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private void parseHexComponent(CharSequence word, int start, int end, boolean doubleDigit) {
		int comp = hexComponent(word, start, end, doubleDigit);
		newNumberArgument(LexicalType.INTEGER).intValue = comp;
	}

	private static int hexComponent(CharSequence word, int start, int end, boolean doubleDigit) {
		String s;
		if (doubleDigit) {
			CharSequence seq = word.subSequence(start, end);
			s = new StringBuilder(2).append(seq).append(seq).toString();
		} else {
			s = word.subSequence(start, end).toString();
		}
		return Integer.parseInt(s, 16);
	}

	private LexicalUnitImpl newNumberArgument(LexicalType sacType) {
		LexicalUnitImpl lu = newFunctionArgument(sacType);
		lu.setCssUnit(CSSUnit.CSS_NUMBER);
		return lu;
	}

	private LexicalUnitImpl newFunctionArgument(LexicalType type) {
		LexicalUnitImpl arg = new LexicalUnitImpl(type);
		currentlu.addFunctionParameter(arg);
		return arg;
	}

	private void recoverOwnerUnit(CharSequence word) {
		currentlu.identCssText = "#" + word;
		if (currentlu.ownerLexicalUnit != null) {
			currentlu = currentlu.ownerLexicalUnit;
		}
	}

	@Override
	void processBuffer(int index, int triggerCp) {
	}

	@Override
	public void separator(int index, int codePoint) {
		unexpectedCharError(index, codePoint);
	}

	@Override
	void handleSemicolon(int index) {
		reportError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ';'.");
		caller.character(index, TokenProducer.CHAR_SEMICOLON);
	}

	@Override
	public void escaped(int index, int codePoint) {
		unexpectedCharError(index, codePoint);
	}

	@Override
	public void commented(int index, int commentType, String comment) {
		unexpectedTokenError(index, comment);
	}

	@Override
	public void endOfStream(int len) {
		unexpectedEOFError(len);
		caller.endOfStream(len);
	}

}
