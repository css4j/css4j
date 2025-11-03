/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Objects;

import io.sf.carte.doc.style.css.CSSValueSyntax;

class SyntaxUnitImpl extends LexicalUnitImpl {

	private static final long serialVersionUID = 1L;

	CSSValueSyntax syntax;

	public SyntaxUnitImpl() {
		super(LexicalType.SYNTAX);
	}

	@Override
	public CSSValueSyntax getSyntax() {
		return syntax;
	}

	@Override
	public String getStringValue() {
		return syntax.toString();
	}

	@Override
	public SyntaxUnitImpl shallowClone() {
		SyntaxUnitImpl clon = (SyntaxUnitImpl) super.shallowClone();
		clon.syntax = syntax;
		return clon;
	}

	@Override
	SyntaxUnitImpl instantiateLexicalUnit() {
		return new SyntaxUnitImpl();
	}

	@Override
	CharSequence currentToString() {
		return syntax.toString();
	}

	@Override
	void reset() {
		super.reset();
		syntax = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(syntax);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		SyntaxUnitImpl other = (SyntaxUnitImpl) obj;
		return Objects.equals(syntax, other.syntax);
	}

}
