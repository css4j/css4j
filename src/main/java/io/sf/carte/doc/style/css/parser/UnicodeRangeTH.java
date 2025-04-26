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

class UnicodeRangeTH extends LexicalCallbackTH {

	UnicodeRangeTH(LexicalProvider caller) {
		super(caller);
	}

	@Override
	protected void initializeBuffer() {
		buffer = new StringBuilder(10);
	}

	@Override
	void processBuffer(int index, int triggerCp) {
		int buflen = buffer.length();
		if (buflen == 0) {
			return;
		}
		LexicalUnitImpl lu1;
		LexicalUnitImpl lu2 = null;
		String s = rawBuffer();
		int idx = s.indexOf('-');
		if (idx == -1) {
			byte check = rangeLengthCheck(s);
			if (check == 1) {
				lu1 = new LexicalUnitImpl(LexicalType.INTEGER);
				lu1.intValue = Integer.parseInt(s, 16);
				lu1.setCssUnit(CSSUnit.CSS_NUMBER);
			} else if (check == 2) {
				lu1 = new UnicodeWildcardUnitImpl();
				lu1.value = s;
			} else {
				handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Invalid unicode range: " + s);
				return;
			}
		} else if (idx > 0 && idx < s.length() - 1) {
			String range1 = s.substring(0, idx);
			String range2 = s.substring(idx + 1);
			byte check = rangeLengthCheck(range1);
			if (check == 1) {
				lu1 = new LexicalUnitImpl(LexicalType.INTEGER);
				lu1.intValue = Integer.parseInt(range1, 16);
				lu1.setCssUnit(CSSUnit.CSS_NUMBER);
			} else {
				handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Invalid unicode range: " + s);
				return;
			}
			check = rangeLengthCheck(range2);
			if (check == 1) {
				lu2 = new LexicalUnitImpl(LexicalType.INTEGER);
				lu2.intValue = Integer.parseInt(range2, 16);
				lu2.setCssUnit(CSSUnit.CSS_NUMBER);
			} else {
				handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Invalid unicode range: " + s);
				return;
			}
		} else {
			handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN,
					"Invalid unicode range: " + s);
			return;
		}
		LexicalUnitImpl range = new UnicodeRangeUnitImpl();
		range.addFunctionParameter(lu1);
		if (lu2 != null) {
			range.addFunctionParameter(lu2);
		}

		addPlainLexicalUnit(range);

		yieldBack();
	}

	private static byte rangeLengthCheck(String range) {
		byte wildcardCount = 0;
		int len = range.length();
		if (len < 7) {
			for (int i = 0; i < len; i++) {
				if (range.charAt(i) == '?') {
					wildcardCount++;
				} else if (wildcardCount != 0) {
					return 0;
				}
			}
			if (wildcardCount == 0) {
				return (byte) 1;
			}
			if (wildcardCount != 6) {
				return (byte) 2;
			}
		}
		return 0;
	}

	@Override
	public void separator(int index, int codePoint) {
		if (buffer.length() != 0) {
			processBuffer(index, codePoint);
		}
		setWhitespacePrevCp();
	}

	@Override
	public void character(int index, int codePoint) {
		if (codePoint == 45) { // -
			buffer.append('-');
			codePoint = 65;
		} else if (codePoint == 44) { // ,
			processBuffer(index, codePoint);
			if (!isInError()) {
				// getCaller().character(index, codepoint);
				LexicalUnitImpl lu = new LexicalUnitImpl(LexicalType.OPERATOR_COMMA);
				getCaller().addPlainLexicalUnit(lu);
			}
		} else if (codePoint == TokenProducer.CHAR_QUESTION_MARK && buffer.length() < 6) {
			buffer.append('?');
		} else if (codePoint == TokenProducer.CHAR_SEMICOLON) {
			handleSemicolon(index);
		} else {
			unexpectedCharError(index, codePoint);
		}

		prevcp = codePoint;
	}

	@Override
	public void escaped(int index, int codePoint) {
		unexpectedCharError(index, codePoint);
	}

}
