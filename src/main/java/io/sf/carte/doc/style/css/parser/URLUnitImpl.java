/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class URLUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public URLUnitImpl() {
		super(LexicalType.URI);
	}

	@Override
	URLUnitImpl instantiateLexicalUnit() {
		return new URLUnitImpl();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		Category cat = syntax.getCategory();
		return cat == Category.url || cat == Category.image ? Match.TRUE : Match.FALSE;
	}

	@Override
	CharSequence currentToString() {
		StringBuilder buf = new StringBuilder(32);
		String quri;
		if (identCssText != null) {
			quri = identCssText;
		} else if (value != null) {
			quri = ParseHelper.quote(value, '\'');
		} else {
			quri = "";
		}
		buf.append("url(").append(quri);
		if (parameters != null) {
			buf.append(' ').append(parameters.toString());
		}
		buf.append(')');
		return buf;
	}

}
