/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Objects;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * Media Feature predicate.
 * 
 */
public class MediaFeaturePredicate extends BooleanCondition.Predicate {

	private ExtendedCSSPrimitiveValue value1;
	private ExtendedCSSPrimitiveValue value2;
	private byte rangeType;

	MediaFeaturePredicate(String featureName) {
		super(featureName);
	}

	public void setRangeType(byte rangeType) {
		this.rangeType = rangeType;
	}

	public byte getRangeType() {
		return rangeType;
	}

	public ExtendedCSSPrimitiveValue getValue() {
		return value1;
	}

	public void setValue(ExtendedCSSPrimitiveValue value) {
		this.value1 = value;
	}

	public ExtendedCSSPrimitiveValue getRangeSecondValue() {
		return value2;
	}

	public void setRangeSecondValue(ExtendedCSSPrimitiveValue value) {
		value2 = value;
	}

	@Override
	public void appendText(StringBuilder buf) {
		buf.append('(');
		switch (rangeType) {
		case MediaQuery.FEATURE_PLAIN:
		case MediaQuery.FEATURE_EQ:
			appendFeatureName(buf);
			if (value1 != null) {
				buf.append(": ");
				buf.append(value1.getCssText());
			}
			break;
		case MediaQuery.FEATURE_LT:
			appendFeatureName(buf);
			buf.append(" < ");
			buf.append(value1.getCssText());
			break;
		case MediaQuery.FEATURE_LE:
			appendFeatureName(buf);
			buf.append(" <= ");
			buf.append(value1.getCssText());
			break;
		case MediaQuery.FEATURE_GT:
			appendFeatureName(buf);
			buf.append(" > ");
			buf.append(value1.getCssText());
			break;
		case MediaQuery.FEATURE_GE:
			appendFeatureName(buf);
			buf.append(" >= ");
			buf.append(value1.getCssText());
			break;
		case MediaQuery.FEATURE_LT_AND_LE:
			buf.append(value1.getCssText());
			buf.append(" < ");
			appendFeatureName(buf);
			buf.append(" <= ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_LT_AND_LT:
			buf.append(value1.getCssText());
			buf.append(" < ");
			appendFeatureName(buf);
			buf.append(" < ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_LE_AND_LT:
			buf.append(value1.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf);
			buf.append(" < ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_LE_AND_LE:
			buf.append(value1.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf);
			buf.append(" <= ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_GT_AND_GT:
			buf.append(value1.getCssText());
			buf.append(" > ");
			appendFeatureName(buf);
			buf.append(" > ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_GE_AND_GT:
			buf.append(value1.getCssText());
			buf.append(" >= ");
			appendFeatureName(buf);
			buf.append(" > ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_GT_AND_GE:
			buf.append(value1.getCssText());
			buf.append(" > ");
			appendFeatureName(buf);
			buf.append(" >= ");
			buf.append(value2.getCssText());
			break;
		case MediaQuery.FEATURE_GE_AND_GE:
			buf.append(value1.getCssText());
			buf.append(" >= ");
			appendFeatureName(buf);
			buf.append(" >= ");
			buf.append(value2.getCssText());
			break;
		}
		buf.append(')');
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		buf.append('(');
		switch (rangeType) {
		case MediaQuery.FEATURE_PLAIN:
		case MediaQuery.FEATURE_EQ:
			appendFeatureName(buf);
			if (value1 != null) {
				buf.append(':');
				buf.append(value1.getMinifiedCssText(""));
			}
			break;
		case MediaQuery.FEATURE_LT:
			appendFeatureName(buf);
			buf.append('<');
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_LE:
			appendFeatureName(buf);
			buf.append("<=");
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_GT:
			appendFeatureName(buf);
			buf.append('>');
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_GE:
			appendFeatureName(buf);
			buf.append(">=");
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_LT_AND_LE:
			buf.append(value1.getCssText());
			buf.append('<');
			appendFeatureName(buf);
			buf.append("<=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_LT_AND_LT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('<');
			appendFeatureName(buf);
			buf.append('<');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_LE_AND_LT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append("<=");
			appendFeatureName(buf);
			buf.append('<');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_LE_AND_LE:
			buf.append(value1.getMinifiedCssText(""));
			buf.append("<=");
			appendFeatureName(buf);
			buf.append("<=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_GT_AND_GT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('>');
			appendFeatureName(buf);
			buf.append('>');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_GE_AND_GT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append(">=");
			appendFeatureName(buf);
			buf.append('>');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_GT_AND_GE:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('>');
			appendFeatureName(buf);
			buf.append(">=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaQuery.FEATURE_GE_AND_GE:
			buf.append(value1.getMinifiedCssText(""));
			buf.append(">=");
			appendFeatureName(buf);
			buf.append(">=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		}
		buf.append(')');
	}

	private void appendFeatureName(StringBuilder buf) {
		buf.append(ParseHelper.escape(getName()));
	}

	@Override
	public int hashCode() {
		byte efftype;
		if (rangeType == MediaQuery.FEATURE_PLAIN) {
			// We handle 'feature: value' effectively as 'feature = value'
			efftype = MediaQuery.FEATURE_EQ;
		} else {
			efftype = rangeType;
		}
		return 17 + 31 * Objects.hash(getName(), efftype, value1, value2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MediaFeaturePredicate other = (MediaFeaturePredicate) obj;
		byte efftype, otherefftype;
		if (rangeType == MediaQuery.FEATURE_PLAIN) {
			// We handle 'feature: value' effectively as 'feature = value'
			efftype = MediaQuery.FEATURE_EQ;
		} else {
			efftype = rangeType;
		}
		if (other.rangeType == MediaQuery.FEATURE_PLAIN) {
			otherefftype = MediaQuery.FEATURE_EQ;
		} else {
			otherefftype = other.rangeType;
		}
		return Objects.equals(getName(), other.getName()) && efftype == otherefftype
				&& Objects.equals(value1, other.value1) && Objects.equals(value2, other.value2);
	}

}
