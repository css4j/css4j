/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class UnicodeRangeUnitImpl extends UnicodeUnitImpl {

	private static final long serialVersionUID = 1L;

	public UnicodeRangeUnitImpl() {
		super(LexicalType.UNICODE_RANGE);
	}

	@Override
	public LexicalUnit getSubValues() {
		return parameters;
	}

	@Override
	CharSequence currentToString() {
		StringBuilder buf = new StringBuilder();
		LexicalUnit lu = this.parameters;
		if (lu != null) {
			if (lu.getLexicalUnitType() == LexicalType.INTEGER) {
				buf.append("U+").append(Integer.toHexString(lu.getIntegerValue()));
			} else {
				buf.append("U+").append(lu.getStringValue());
			}
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				buf.append('-');
				if (lu.getLexicalUnitType() == LexicalType.INTEGER) {
					buf.append(Integer.toHexString(lu.getIntegerValue()));
				} else {
					buf.append(lu.getStringValue());
				}
			}
		}
		return buf;
	}

	@Override
	UnicodeRangeUnitImpl instantiateLexicalUnit() {
		return new UnicodeRangeUnitImpl();
	}

}
