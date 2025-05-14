/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.MediaQueryPredicate;

/**
 * Media type predicate lexical unit.
 */
class MediaPredicateUnit extends BooleanConditionUnit.Predicate implements MediaQueryPredicate {

	private static final long serialVersionUID = 1L;

	MediaPredicateUnit(String medium) {
		super(medium);
	}

	@Override
	public int getPredicateType() {
		return MediaQueryPredicate.MEDIA_TYPE;
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		Match match;
		switch (syntax.getCategory()) {
		case IDENT:
		case universal:
			match = Match.TRUE;
			break;
		default:
			match = Match.FALSE;
			break;
		}
		return match;
	}

}
