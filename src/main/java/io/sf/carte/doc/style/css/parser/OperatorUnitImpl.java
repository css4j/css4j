/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.StringList;

class OperatorUnitImpl extends LexicalUnitImpl {

	private static final long serialVersionUID = 1L;

	public OperatorUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	OperatorUnitImpl instantiateLexicalUnit() {
		return new OperatorUnitImpl(getLexicalUnitType());
	}

	@Override
	CharSequence currentToString() {
		String s;
		switch (getLexicalUnitType()) {
		case OPERATOR_COMMA:
			s = ",";
			break;
		case OPERATOR_SEMICOLON:
			s = ";";
			break;
		case OPERATOR_EXP:
			s = "^";
			break;
		case OPERATOR_GE:
			s = ">=";
			break;
		case OPERATOR_GT:
			s = ">";
			break;
		case OPERATOR_LE:
			s = "<=";
			break;
		case OPERATOR_LT:
			s = "<";
			break;
		case OPERATOR_MINUS:
			s = "-";
			break;
		case OPERATOR_MOD:
			s = "%";
			break;
		case OPERATOR_MULTIPLY:
			s = "*";
			break;
		case OPERATOR_PLUS:
			s = "+";
			break;
		case OPERATOR_SLASH:
			s = "/";
			break;
		case OPERATOR_TILDE:
			s = "~";
			break;
		default:
			s = "\ufffd";
		}
		return s;
	}

	@Override
	boolean addPrecedingComments(StringList comments) {
		return false;
	}

	@Override
	boolean addTrailingComments(StringList comments) {
		return false;
	}

}
