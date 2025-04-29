/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Iterator;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

class EmptyUnitImpl extends LexicalUnitImpl {

	private static final long serialVersionUID = 1L;

	public EmptyUnitImpl() {
		super(LexicalType.EMPTY);
		this.value = "";
	}

	@Override
	EmptyUnitImpl instantiateLexicalUnit() {
		return new EmptyUnitImpl();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		return Match.PENDING;
	}

	@Override
	CharSequence currentToString() {
		CharSequence seq;
		StringList comments = getPrecedingComments();
		if (comments != null && !comments.isEmpty()) {
			Iterator<String> it = comments.iterator();
			seq = it.next();
			StringBuilder buf = new StringBuilder(seq.length() * comments.getLength());
			buf.append("/*").append(seq).append("*/");
			while (it.hasNext()) {
				String comment = it.next();
				buf.append("/*").append(comment).append("*/");
			}
			seq = buf;
		} else {
			seq = "";
		}
		return seq;
	}

}
