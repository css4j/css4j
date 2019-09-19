/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.parser.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.parser.MediaPredicate;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * Media Feature predicate implementation.
 * 
 */
class MediaFeaturePredicateImpl extends BooleanConditionImpl.Predicate implements MediaFeaturePredicate {

	private ExtendedCSSPrimitiveValue value1;
	private ExtendedCSSPrimitiveValue value2;
	private byte rangeType;

	MediaFeaturePredicateImpl(String featureName) {
		super(featureName);
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
	public ExtendedCSSPrimitiveValue getValue() {
		return value1;
	}

	@Override
	public void setValue(ExtendedCSSPrimitiveValue value) {
		this.value1 = value;
	}

	@Override
	public ExtendedCSSPrimitiveValue getRangeSecondValue() {
		return value2;
	}

	@Override
	public void setRangeSecondValue(ExtendedCSSPrimitiveValue value) {
		value2 = value;
	}

	@Override
	public boolean matches(MediaPredicate otherPredicate) {
		if (getPredicateType() != ((BooleanConditionImpl.Predicate) otherPredicate).getPredicateType()) {
			return false;
		}
		MediaFeaturePredicateImpl other = (MediaFeaturePredicateImpl) otherPredicate;
		String feature = getName();
		String oFeature = other.getName();
		byte type = getRangeType();
		byte oType = other.getRangeType();
		// Canonicalize types and feature names
		if (type == MediaQuery.FEATURE_PLAIN) {
			if (feature.startsWith("min-")) {
				feature = feature.substring(4);
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				type = MediaQuery.FEATURE_GE;
			} else if (feature.startsWith("max-")) {
				// <=
				feature = feature.substring(4);
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				type = MediaQuery.FEATURE_LE;
			} else {
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				type = MediaQuery.FEATURE_EQ;
			}
		}
		if (oType == MediaQuery.FEATURE_PLAIN) {
			if (oFeature.startsWith("min-")) {
				oFeature = oFeature.substring(4);
				if (oFeature.startsWith("device-")) {
					oFeature = oFeature.substring(7);
				}
				oType = MediaQuery.FEATURE_GE;
			} else if (oFeature.startsWith("max-")) {
				// <=
				oFeature = oFeature.substring(4);
				if (oFeature.startsWith("device-")) {
					oFeature = oFeature.substring(7);
				}
				oType = MediaQuery.FEATURE_LE;
			} else {
				if (oFeature.startsWith("device-")) {
					oFeature = oFeature.substring(7);
				}
				oType = MediaQuery.FEATURE_EQ;
			}
		}
		if (!feature.equals(oFeature)) {
			return false;
		}
		// Normalize type
		ExtendedCSSPrimitiveValue otherVal2 = other.value2;
		boolean noeq1 = false;
		boolean noeq2 = false;
		switch (type) {
		case MediaQuery.FEATURE_LT:
			if (oType == MediaQuery.FEATURE_LE) {
				oType = MediaQuery.FEATURE_LT;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_LT;
				noeq1 = true;
			}
			break;
		case MediaQuery.FEATURE_LE:
			if (oType == MediaQuery.FEATURE_LT) {
				type = MediaQuery.FEATURE_LT;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_LE;
			}
			break;
		case MediaQuery.FEATURE_GT:
			if (oType == MediaQuery.FEATURE_GE) {
				oType = MediaQuery.FEATURE_GT;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_GT;
				noeq1 = true;
			}
			break;
		case MediaQuery.FEATURE_GE:
			if (oType == MediaQuery.FEATURE_GT) {
				type = MediaQuery.FEATURE_GT;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_GE;
			}
			break;
		case MediaQuery.FEATURE_LT_AND_LT:
			if (oType == MediaQuery.FEATURE_LE_AND_LT) {
				oType = MediaQuery.FEATURE_LT_AND_LT;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_LT_AND_LE) {
				oType = MediaQuery.FEATURE_LT_AND_LT;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_LE_AND_LE) {
				oType = MediaQuery.FEATURE_LT_AND_LT;
				noeq1 = true;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_LT_AND_LT;
				noeq1 = true;
				noeq2 = true;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_LE_AND_LT:
			if (oType == MediaQuery.FEATURE_LT_AND_LT) {
				type = MediaQuery.FEATURE_LT_AND_LT;
			} else if (oType == MediaQuery.FEATURE_LE_AND_LE) {
				oType = MediaQuery.FEATURE_LE_AND_LT;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_LT_AND_LE) {
				type = MediaQuery.FEATURE_LT_AND_LT;
				oType = MediaQuery.FEATURE_LT_AND_LT;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_LE_AND_LT;
				noeq2 = true;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_LT_AND_LE:
			if (oType == MediaQuery.FEATURE_LT_AND_LT) {
				type = MediaQuery.FEATURE_LT_AND_LT;
			} else if (oType == MediaQuery.FEATURE_LE_AND_LE) {
				oType = MediaQuery.FEATURE_LT_AND_LE;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_LE_AND_LT) {
				type = MediaQuery.FEATURE_LT_AND_LT;
				oType = MediaQuery.FEATURE_LT_AND_LT;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_LT_AND_LE;
				noeq1 = true;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_LE_AND_LE:
			if (oType == MediaQuery.FEATURE_LT_AND_LT) {
				type = MediaQuery.FEATURE_LT_AND_LT;
			} else if (oType == MediaQuery.FEATURE_LT_AND_LE) {
				type = MediaQuery.FEATURE_LT_AND_LE;
			} else if (oType == MediaQuery.FEATURE_LE_AND_LT) {
				type = MediaQuery.FEATURE_LE_AND_LT;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_LE_AND_LE;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_GT_AND_GT:
			if (oType == MediaQuery.FEATURE_GE_AND_GT) {
				oType = MediaQuery.FEATURE_GT_AND_GT;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_GT_AND_GE) {
				oType = MediaQuery.FEATURE_GT_AND_GT;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_GE_AND_GE) {
				oType = MediaQuery.FEATURE_GT_AND_GT;
				noeq1 = true;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_GT_AND_GT;
				noeq1 = true;
				noeq2 = true;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_GE_AND_GT:
			if (oType == MediaQuery.FEATURE_GT_AND_GT) {
				type = MediaQuery.FEATURE_GT_AND_GT;
			} else if (oType == MediaQuery.FEATURE_GE_AND_GE) {
				oType = MediaQuery.FEATURE_GE_AND_GT;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_GT_AND_GE) {
				type = MediaQuery.FEATURE_GT_AND_GT;
				oType = MediaQuery.FEATURE_GT_AND_GT;
				noeq2 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_GE_AND_GT;
				noeq2 = true;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_GT_AND_GE:
			if (oType == MediaQuery.FEATURE_GT_AND_GT) {
				type = MediaQuery.FEATURE_GT_AND_GT;
			} else if (oType == MediaQuery.FEATURE_GE_AND_GE) {
				oType = MediaQuery.FEATURE_GT_AND_GE;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_GE_AND_GT) {
				type = MediaQuery.FEATURE_GT_AND_GT;
				oType = MediaQuery.FEATURE_GT_AND_GT;
				noeq1 = true;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_GT_AND_GE;
				noeq1 = true;
				otherVal2 = other.value1;
			}
			break;
		case MediaQuery.FEATURE_GE_AND_GE:
			if (oType == MediaQuery.FEATURE_GT_AND_GT) {
				type = MediaQuery.FEATURE_GT_AND_GT;
			} else if (oType == MediaQuery.FEATURE_GT_AND_GE) {
				type = MediaQuery.FEATURE_GT_AND_GE;
			} else if (oType == MediaQuery.FEATURE_GE_AND_GT) {
				type = MediaQuery.FEATURE_GE_AND_GT;
			} else if (oType == MediaQuery.FEATURE_EQ) {
				oType = MediaQuery.FEATURE_GE_AND_GE;
				otherVal2 = other.value1;
			}
			break;
		}
		if (type != oType) {
			return false;
		}
		// Values
		if (value1 == null) {
			// Boolean
			return other.value1 == null;
		} else if (other.value1 == null) {
			return false;
		}
		short pType = value1.getPrimitiveType();
		float fval1 = value1.getFloatValue(pType);
		float ofval1;
		try {
			ofval1 = other.value1.getFloatValue(pType);
		} catch (DOMException e) {
			return false;
		}
		float fval2 = Float.NaN;
		float ofval2 = Float.NaN;
		if (value2 != null) {
			if (otherVal2 == null) {
				return false; // That should never happen
			}
			pType = value2.getPrimitiveType();
			fval2 = value2.getFloatValue(pType);
			try {
				ofval2 = otherVal2.getFloatValue(pType);
			} catch (DOMException e) {
				return false;
			}
		}
		//
		switch (type) {
		case MediaQuery.FEATURE_EQ:
			return MediaQuery.floatEquals(fval1, ofval1);
		case MediaQuery.FEATURE_LT:
		case MediaQuery.FEATURE_LE:
			return noeq1 ? fval1 > ofval1 : fval1 >= ofval1;
		case MediaQuery.FEATURE_GT:
		case MediaQuery.FEATURE_GE:
			return noeq1 ? fval1 < ofval1 : fval1 <= ofval1;
		case MediaQuery.FEATURE_LT_AND_LT:
		case MediaQuery.FEATURE_LE_AND_LT:
		case MediaQuery.FEATURE_LT_AND_LE:
		case MediaQuery.FEATURE_LE_AND_LE:
			boolean first = noeq1 ? fval1 < ofval1 : fval1 <= ofval1;
			boolean second = noeq2 ? ofval2 < fval2 : ofval2 <= fval2;
			return first && second;
		case MediaQuery.FEATURE_GT_AND_GT:
		case MediaQuery.FEATURE_GE_AND_GT:
		case MediaQuery.FEATURE_GT_AND_GE:
		case MediaQuery.FEATURE_GE_AND_GE:
			first = noeq1 ? fval1 > ofval1 : fval1 >= ofval1;
			second = noeq2 ? ofval2 > fval2 : ofval2 >= fval2;
			return first && second;
		default:
			return false;
		}
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
		MediaFeaturePredicateImpl other = (MediaFeaturePredicateImpl) obj;
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
