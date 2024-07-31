/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

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

}
