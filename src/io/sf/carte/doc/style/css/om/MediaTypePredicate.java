/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * Media type predicate.
 * 
 */
class MediaTypePredicate extends MediaPredicate {

	MediaTypePredicate(String medium) {
		super(medium);
	}

	@Override
	public short getPredicateType() {
		return MediaPredicate.MEDIA_TYPE;
	}

	@Override
	public boolean matches(MediaPredicate otherPredicate, byte negatedQuery) {
		BooleanConditionImpl.Predicate other = (BooleanConditionImpl.Predicate) otherPredicate;
		if (getPredicateType() == other.getPredicateType()) {
			return false;
		}
		boolean negated = negatedQuery == 1 || negatedQuery == 2;
		if (getName().equals(other.getName()) || "all".equals(getName())) {
			return negated;
		}
		return !negated;
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
