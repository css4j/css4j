/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Objects;

import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryPredicate;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * Media feature predicate lexical unit.
 */
class MediaFeaturePredicateUnit extends BooleanConditionUnit.Predicate
		implements MediaFeaturePredicate {

	private static final long serialVersionUID = 2L;

	private LexicalUnit value1 = null;
	private LexicalUnit value2 = null;
	private byte rangeType;

	MediaFeaturePredicateUnit(String featureName) {
		super(featureName);
	}

	@Override
	public int getPredicateType() {
		return MediaQueryPredicate.MEDIA_FEATURE;
	}

	@Override
	public void setRangeType(byte rangeType) {
		this.rangeType = rangeType;
	}

	@Override
	public byte getRangeType() {
		return rangeType;
	}

	@Override
	public void setValue(LexicalUnit value) {
		this.value1 = value;
	}

	@Override
	public void setValueRange(LexicalUnit value1, LexicalUnit value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public void appendText(StringBuilder buf) {
		String text1;
		if (value1 != null) {
			text1 = value1.toString();
		} else {
			text1 = null;
		}
		String text2;
		if (value2 != null) {
			text2 = value2.toString();
		} else {
			text2 = null;
		}
		MediaQueryHelper.appendFeatureText(ParseHelper.escape(getName()), rangeType, text1, text2, buf);
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		String text1;
		if (value1 != null) {
			text1 = value1.toString();
		} else {
			text1 = null;
		}
		String text2;
		if (value2 != null) {
			text2 = value2.toString();
		} else {
			text2 = null;
		}
		MediaQueryHelper.appendMinifiedFeatureText(ParseHelper.escape(getName()), rangeType, text1, text2, buf);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		byte efftype;
		if (rangeType == MediaFeaturePredicate.FEATURE_PLAIN) {
			// We handle 'feature: value' effectively as 'feature = value'
			efftype = MediaFeaturePredicate.FEATURE_EQ;
		} else {
			efftype = rangeType;
		}
		return prime * result + Objects.hash(getName(), efftype, value1, value2);
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		MediaFeaturePredicateUnit other = (MediaFeaturePredicateUnit) obj;
		byte efftype, otherefftype;
		if (rangeType == MediaFeaturePredicate.FEATURE_PLAIN) {
			// We handle 'feature: value' effectively as 'feature = value'
			efftype = MediaFeaturePredicate.FEATURE_EQ;
		} else {
			efftype = rangeType;
		}
		if (other.rangeType == MediaFeaturePredicate.FEATURE_PLAIN) {
			otherefftype = MediaFeaturePredicate.FEATURE_EQ;
		} else {
			otherefftype = other.rangeType;
		}
		return Objects.equals(getName(), other.getName()) && efftype == otherefftype
				&& Objects.equals(value1, other.value1) && Objects.equals(value2, other.value2);
	}

}
