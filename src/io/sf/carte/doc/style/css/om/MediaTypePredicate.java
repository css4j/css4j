/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.parser.MediaPredicate;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * Media type predicate.
 * 
 */
class MediaTypePredicate extends BooleanConditionImpl.Predicate implements MediaPredicate {

	MediaTypePredicate(String medium) {
		super(medium);
	}

	@Override
	public short getPredicateType() {
		return 1;
	}

	@Override
	public boolean matches(MediaPredicate otherPredicate) {
		BooleanConditionImpl.Predicate other = (BooleanConditionImpl.Predicate) otherPredicate;
		return getPredicateType() == other.getPredicateType() && getName().equals(other.getName());
	}

	@Override
	public void appendText(StringBuilder buf) {
		appendFeatureName(buf);
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		appendFeatureName(buf);
	}

	private void appendFeatureName(StringBuilder buf) {
		buf.append(ParseHelper.escape(getName()));
	}

}
