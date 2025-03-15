/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

abstract class FunctionUnitImpl extends LexicalUnitImpl {

	private static final long serialVersionUID = 1L;

	public FunctionUnitImpl(LexicalType type) {
		super(type);
	}

	@Override
	public LexicalUnit getSubValues() {
		return null;
	}

	@Override
	CharSequence currentToString() {
		return functionalSerialization(value);
	}

	@Override
	public Match matches(CSSValueSyntax syntax) {
		if (syntax != null) {
			return matchSyntaxChain(syntax);
		}

		return Match.FALSE;
	}

	/*
	 * Do an abstract override so implementations do not forget to override.
	 */
	@Override
	abstract FunctionUnitImpl instantiateLexicalUnit();

}
