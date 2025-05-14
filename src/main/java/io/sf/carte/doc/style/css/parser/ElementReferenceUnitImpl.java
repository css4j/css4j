/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

class ElementReferenceUnitImpl extends ImageFunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public ElementReferenceUnitImpl() {
		super(LexicalType.ELEMENT_REFERENCE);
	}

	@Override
	public int getContextIndex() {
		return LexicalType.ELEMENT_REFERENCE.ordinal() - LexicalType.GRADIENT.ordinal();
	}

	@Override
	CharSequence currentToString() {
		if (parameters != null) {
			return functionalSerialization("element");
		}
		if (value == null) {
			return "element(#)";
		}
		int len = value.length();
		StringBuilder buf = new StringBuilder(len + 10);
		buf.append("element(#").append(value).append(')');
		return buf;
	}

	@Override
	ElementReferenceUnitImpl instantiateLexicalUnit() {
		return new ElementReferenceUnitImpl();
	}

}
