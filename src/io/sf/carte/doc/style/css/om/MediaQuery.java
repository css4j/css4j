/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.CalcValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.RatioValue;

class MediaQuery {

	public static final byte FEATURE_PLAIN = 0;
	public static final byte FEATURE_EQ = 1; // =
	public static final byte FEATURE_LT = 2; // <
	public static final byte FEATURE_LE = 3; // <=
	public static final byte FEATURE_GT = 4; // >
	public static final byte FEATURE_GE = 5; // >=
	public static final byte FEATURE_LT_AND_LT = 18; // a < foo < b
	public static final byte FEATURE_LT_AND_LE = 26; // a < foo <= b
	public static final byte FEATURE_LE_AND_LE = 27; // a <= foo <= b
	public static final byte FEATURE_LE_AND_LT = 19; // a <= foo < b
	public static final byte FEATURE_GT_AND_GT = 36; // a > foo > b
	public static final byte FEATURE_GE_AND_GT = 37; // a >= foo > b
	public static final byte FEATURE_GT_AND_GE = 44; // a > foo >= b
	public static final byte FEATURE_GE_AND_GE = 45; // a >= foo >= b

	private String mediaType = null;

	private boolean negativeQuery = false;

	private boolean onlyPrefix = false;

	private final LinkedHashMap<String, ExtendedCSSPrimitiveValue> featureList;

	private final HashMap<String, FeatureRange> featureRange;

	public MediaQuery() {
		super();
		featureList = new LinkedHashMap<String, ExtendedCSSPrimitiveValue>();
		featureRange = new HashMap<String, FeatureRange>();
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setNegative(boolean negative) {
		this.negativeQuery = negative;
	}

	public void setOnlyPrefix(boolean only) {
		this.onlyPrefix = only;
	}

	public boolean matches(String medium, CSSCanvas canvas) {
		if (mediaType != null) {
			if (mediaType.equals(medium)) {
				if (negativeQuery) {
					return false;
				}
			} else {
				if (!negativeQuery) {
					return false;
				}
			}
		}
		if (!featureList.isEmpty()) {
			if (canvas == null) {
				return false;
			}
			Iterator<Entry<String, ExtendedCSSPrimitiveValue>> it = featureList.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, ExtendedCSSPrimitiveValue> entry = it.next();
				String feature = entry.getKey();
				ExtendedCSSPrimitiveValue value = entry.getValue();
				FeatureRange range = featureRange.get(feature);
				byte type;
				if (range == null) {
					if (value == null) {
						if (featureBooleanMatch(feature, canvas)) {
							return true;
						} else {
							continue;
						}
					}
					type = FEATURE_PLAIN;
				} else {
					type = range.rangeType;
				}
				if (type == FEATURE_PLAIN) {
					if (feature.startsWith("min-")) {
						// >=
						feature = feature.substring(4);
						if (feature.startsWith("device-")) {
							feature = feature.substring(7);
						}
						if (featureRangeMatch(feature, FEATURE_GE, value, null, canvas)) {
							return true;
						}
					} else if (feature.startsWith("max-")) {
						// <=
						feature = feature.substring(4);
						if (feature.startsWith("device-")) {
							feature = feature.substring(7);
						}
						if (featureRangeMatch(feature, FEATURE_LE, value, null, canvas)) {
							return true;
						}
					} else {
						if (feature.startsWith("device-")) {
							feature = feature.substring(7);
						}
						if (!MediaQueryFactory.isRangeFeature(feature)) {
							if (canvas.matchesFeature(feature, value)) {
								return true;
							}
						} else if (featureRangeMatch(feature, FEATURE_EQ, value, null, canvas)) {
							return true;
						}
					}
				} else {
					if (featureRangeMatch(feature, type, value, range.value, canvas)) {
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	private boolean featureBooleanMatch(String feature, CSSCanvas canvas) {
		if (MediaQueryFactory.isRangeFeature(feature)) {
			ExtendedCSSPrimitiveValue featured = canvas.getFeatureValue(feature);
			return !featured.isNumberZero();
		}
		return canvas.matchesFeature(feature, null);
	}

	private boolean featureRangeMatch(String feature, byte type, CSSPrimitiveValue value, CSSPrimitiveValue value2,
			CSSCanvas canvas) {
		ExtendedCSSPrimitiveValue featured = canvas.getFeatureValue(feature);
		if (featured == null) {
			return false;
		}
		short primitype = ((CSSPrimitiveValue) featured).getPrimitiveType();
		float featureValue = ((CSSPrimitiveValue) featured).getFloatValue(primitype);
		float fval1, fval2 = 0;
		try {
			fval1 = valueInUnit(value, canvas, primitype);
		} catch (DOMException e) {
			return false;
		}
		if (type >= 6) {
			if (value2 == null) {
				return false;
			}
			try {
				fval2 = valueInUnit(value2, canvas, primitype);
			} catch (DOMException e) {
				return false;
			}
		}
		switch (type) {
		case FEATURE_EQ:
			return floatEquals(fval1, featureValue);
		case FEATURE_LT:
			return fval1 > featureValue;
		case FEATURE_LE:
			return fval1 >= featureValue || floatEquals(fval1, featureValue);
		case FEATURE_GT:
			return fval1 < featureValue;
		case FEATURE_GE:
			return fval1 <= featureValue || floatEquals(fval1, featureValue);
		case FEATURE_LT_AND_LT:
			return (fval1 < featureValue && featureValue < fval2);
		case FEATURE_LE_AND_LT:
			return (fval1 <= featureValue && featureValue < fval2);
		case FEATURE_LT_AND_LE:
			return (fval1 < featureValue && featureValue <= fval2);
		case FEATURE_LE_AND_LE:
			return (fval1 <= featureValue && featureValue <= fval2);
		case FEATURE_GT_AND_GT:
			return (fval1 > featureValue && featureValue > fval2);
		case FEATURE_GE_AND_GT:
			return (fval1 >= featureValue && featureValue > fval2);
		case FEATURE_GT_AND_GE:
			return (fval1 > featureValue && featureValue >= fval2);
		case FEATURE_GE_AND_GE:
			return (fval1 >= featureValue && featureValue >= fval2);
		default:
			return false;
		}
	}

	private float valueInUnit(CSSPrimitiveValue value, CSSCanvas canvas, short primitype)
	throws DOMException {
		float fval;
		StyleDatabase sdb;
		switch (value.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_EMS:
			fval = value.getFloatValue(CSSPrimitiveValue.CSS_EMS);
			sdb = canvas.getStyleDatabase();
			float fontSize = sdb.getFontSizeFromIdentifier(null, "medium");
			fontSize = NumberValue.floatValueConversion(fontSize, sdb.getNaturalUnit(), primitype);
			fval *= fontSize;
			break;
		case CSSPrimitiveValue.CSS_EXS:
			fval = value.getFloatValue(CSSPrimitiveValue.CSS_EXS);
			sdb = canvas.getStyleDatabase();
			fontSize = sdb.getFontSizeFromIdentifier(null, "medium");
			float exSize = sdb.getExSizeInPt(null, fontSize);
			exSize = NumberValue.floatValueConversion(exSize, CSSPrimitiveValue.CSS_PT, primitype);
			fval *= exSize;
			break;
		case CSSPrimitiveValue2.CSS_EXPRESSION:
			ExpressionValue evalue = (ExpressionValue) value;
			Evaluator ev = new MQEvaluator(canvas);
			fval = ev.evaluateExpression(evalue.getExpression()).getFloatValue(primitype);
			break;
		case CSSPrimitiveValue2.CSS_RATIO:
			float ffirst, fsecond;
			RatioValue ratio = (RatioValue) value;
			AbstractCSSPrimitiveValue first = ratio.getAntecedentValue();
			AbstractCSSPrimitiveValue second = ratio.getConsequentValue();
			if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
				ffirst = first.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			} else {
				// Calc
				ev = new MQEvaluator(canvas);
				ffirst = ev.evaluateExpression(((CalcValue) first).getExpression())
						.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			}
			if (second.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
				fsecond = second.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			} else {
				// Calc
				ev = new MQEvaluator(canvas);
				fsecond = ev.evaluateExpression(((CalcValue) second).getExpression())
						.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			}
			fval = ffirst / fsecond;
			break;
		default:
			fval = value.getFloatValue(primitype);
		}
		return fval;
	}

	private boolean floatEquals(float value1, float value2) {
		return Math.abs(value2 - value1) < 7e-6;
	}

	private class MQEvaluator extends Evaluator {

		private final CSSCanvas canvas;

		private final short expectedUnit;

		private MQEvaluator(CSSCanvas canvas) {
			this.canvas = canvas;
			this.expectedUnit = canvas.getStyleDatabase().getNaturalUnit();
		}

		@Override
		protected ExtendedCSSPrimitiveValue absoluteValue(ExtendedCSSPrimitiveValue partialValue) {
			if (partialValue.getPrimitiveType() != CSSPrimitiveValue.CSS_NUMBER) {
				float fval = valueInUnit(partialValue, canvas, expectedUnit);
				NumberValue number = new NumberValue();
				number.setFloatValue(expectedUnit, fval);
				return number;
			}
			return partialValue;
		}

	}

	public String getMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		if (!featureList.isEmpty()) {
			Iterator<Entry<String, ExtendedCSSPrimitiveValue>> it = featureList.entrySet().iterator();
			if (mediaType != null) {
				buf.append(" and (");
			} else {
				buf.append('(');
			}
			appendFeature(buf, it.next());
			while (it.hasNext()) {
				buf.append(" and (");
				appendFeature(buf, it.next());
			}
		}
		return buf.toString();
	}

	private void appendFeature(StringBuilder buf, Entry<String, ExtendedCSSPrimitiveValue> entry) {
		String feature = entry.getKey();
		CSSValue value = entry.getValue();
		FeatureRange range = featureRange.get(feature);
		byte type;
		if (range == null) {
			if (value == null) {
				appendFeatureName(buf, feature);
				buf.append(')');
				return;
			}
			type = FEATURE_PLAIN;
		} else {
			type = range.rangeType;
		}
		switch (type) {
		case FEATURE_PLAIN:
			appendFeatureName(buf, feature);
			if (value != null) {
				buf.append(": ");
				buf.append(value.getCssText());
			}
			break;
		case FEATURE_EQ:
			appendFeatureName(buf, feature);
			buf.append(" = ");
			buf.append(value.getCssText());
			break;
		case FEATURE_LT:
			appendFeatureName(buf, feature);
			buf.append(" < ");
			buf.append(value.getCssText());
			break;
		case FEATURE_LE:
			appendFeatureName(buf, feature);
			buf.append(" <= ");
			buf.append(value.getCssText());
			break;
		case FEATURE_GT:
			appendFeatureName(buf, feature);
			buf.append(" > ");
			buf.append(value.getCssText());
			break;
		case FEATURE_GE:
			appendFeatureName(buf, feature);
			buf.append(" >= ");
			buf.append(value.getCssText());
			break;
		case FEATURE_LT_AND_LE:
			buf.append(value.getCssText());
			buf.append(" < ");
			appendFeatureName(buf, feature);
			buf.append(" <= ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_LT_AND_LT:
			buf.append(value.getCssText());
			buf.append(" < ");
			appendFeatureName(buf, feature);
			buf.append(" < ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_LE_AND_LT:
			buf.append(value.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf, feature);
			buf.append(" < ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_LE_AND_LE:
			buf.append(value.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf, feature);
			buf.append(" <= ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_GT_AND_GT:
			buf.append(value.getCssText());
			buf.append(" > ");
			appendFeatureName(buf, feature);
			buf.append(" > ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_GE_AND_GT:
			buf.append(value.getCssText());
			buf.append(" >= ");
			appendFeatureName(buf, feature);
			buf.append(" > ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_GT_AND_GE:
			buf.append(value.getCssText());
			buf.append(" > ");
			appendFeatureName(buf, feature);
			buf.append(" >= ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_GE_AND_GE:
			buf.append(value.getCssText());
			buf.append(" >= ");
			appendFeatureName(buf, feature);
			buf.append(" >= ");
			buf.append(range.value.getCssText());
			break;
		}
		buf.append(')');
	}

	public String getMinifiedMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		if (!featureList.isEmpty()) {
			Iterator<Entry<String, ExtendedCSSPrimitiveValue>> it = featureList.entrySet().iterator();
			if (mediaType != null) {
				buf.append(" and(");
			} else {
				buf.append('(');
			}
			appendMinifiedFeature(buf, it.next());
			while (it.hasNext()) {
				buf.append(" and(");
				appendMinifiedFeature(buf, it.next());
			}
		}
		return buf.toString();
	}

	private void appendMinifiedFeature(StringBuilder buf, Entry<String, ExtendedCSSPrimitiveValue> entry) {
		String feature = entry.getKey();
		ExtendedCSSValue value = entry.getValue();
		FeatureRange range = featureRange.get(feature);
		byte type;
		if (range == null) {
			if (value == null) {
				appendFeatureName(buf, feature);
				buf.append(')');
				return;
			}
			type = FEATURE_PLAIN;
		} else {
			type = range.rangeType;
		}
		switch (type) {
		case FEATURE_PLAIN:
			appendFeatureName(buf, feature);
			if (value != null) {
				buf.append(':');
				buf.append(value.getMinifiedCssText(""));
			}
			break;
		case FEATURE_EQ:
			appendFeatureName(buf, feature);
			buf.append('=');
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_LT:
			appendFeatureName(buf, feature);
			buf.append('<');
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_LE:
			appendFeatureName(buf, feature);
			buf.append("<=");
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_GT:
			appendFeatureName(buf, feature);
			buf.append('>');
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_GE:
			appendFeatureName(buf, feature);
			buf.append(">=");
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_LT_AND_LE:
			buf.append(value.getCssText());
			buf.append('<');
			appendFeatureName(buf, feature);
			buf.append("<=");
			buf.append(range.value.getMinifiedCssText(""));
			break;
		case FEATURE_LE_AND_LT:
			buf.append(value.getCssText());
			buf.append("<=");
			appendFeatureName(buf, feature);
			buf.append('<');
			buf.append(range.value.getMinifiedCssText(""));
			break;
		case FEATURE_GE_AND_GT:
			buf.append(value.getCssText());
			buf.append(">=");
			appendFeatureName(buf, feature);
			buf.append('>');
			buf.append(range.value.getMinifiedCssText(""));
			break;
		case FEATURE_GT_AND_GE:
			buf.append(value.getCssText());
			buf.append('>');
			appendFeatureName(buf, feature);
			buf.append(">=");
			buf.append(range.value.getMinifiedCssText(""));
			break;
		}
		buf.append(')');
	}

	private void appendFeatureName(StringBuilder buf, String feature) {
		buf.append(ParseHelper.escape(feature));
	}

	static String escapeIdentifier(String medium) {
		return ParseHelper.escape(medium);
	}

	@Override
	public String toString() {
		return getMedia();
	}

	public void addFeature(String featureName, byte rangeType, ExtendedCSSPrimitiveValue value,
			ExtendedCSSPrimitiveValue rangevalue) {
		if (featureName == null) {
			throw new IllegalArgumentException("Null feature name");
		}
		featureList.put(featureName, value);
		if (rangeType != FEATURE_PLAIN && rangeType != FEATURE_EQ) {
			FeatureRange range = new FeatureRange(rangeType, rangevalue);
			featureRange.put(featureName, range);
		}
	}

	static class FeatureRange {
		ExtendedCSSPrimitiveValue value;
		byte rangeType;

		FeatureRange(byte rangeType, ExtendedCSSPrimitiveValue value) {
			super();
			this.rangeType = rangeType;
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result;
			if (rangeType == FEATURE_PLAIN) {
				// We handle 'feature: value' effectively as 'feature = value'
				result = FEATURE_EQ;
			} else {
				result = rangeType;
			}
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FeatureRange other = (FeatureRange) obj;
			byte efftype, otherefftype;
			if (rangeType == FEATURE_PLAIN) {
				// We handle 'feature: value' effectively as 'feature = value'
				efftype = FEATURE_EQ;
			} else {
				efftype = rangeType;
			}
			if (other.rangeType == FEATURE_PLAIN) {
				otherefftype = FEATURE_EQ;
			} else {
				otherefftype = other.rangeType;
			}
			if (efftype != otherefftype)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + (negativeQuery ? 1231 : 1237);
		result = prime * result + (onlyPrefix ? 1231 : 1237);
		if (featureList != null) {
			TreeSet<String> sorted = new TreeSet<String>(featureList.keySet());
			Iterator<String> it = sorted.iterator();
			while (it.hasNext()) {
				String feature = it.next();
				result = prime * result + feature.hashCode();
				CSSValue value = featureList.get(feature);
				if (value != null) {
					result = prime * result + value.hashCode();
					FeatureRange range;
					if (featureRange != null && (range = featureRange.get(feature)) != null) {
						result = prime * result + range.hashCode();
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaQuery other = (MediaQuery) obj;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		if (negativeQuery != other.negativeQuery)
			return false;
		if (onlyPrefix != other.onlyPrefix) // should the 'only' prefix be ignored?
			return false;
		if (featureList == null) {
			if (other.featureList != null)
				return false;
		} else if (other.featureList == null) {
			return false;
		} else if (featureList.size() != other.featureList.size()) {
			return false;
		} else {
			if (featureRange == null) {
				if (other.featureRange != null)
					return false;
			} else if (other.featureRange == null) {
				return false;
			}
			TreeSet<String> sorted = new TreeSet<String>(featureList.keySet());
			Iterator<String> it = sorted.iterator();
			while (it.hasNext()) {
				String feature = it.next();
				if (!other.featureList.containsKey(feature)) {
					return false;
				}
				CSSValue value = featureList.get(feature);
				CSSValue othervalue = other.featureList.get(feature);
				if (value != null) {
					if (othervalue == null)
						return false;
					if (!value.equals(othervalue))
						return false;
					FeatureRange range;
					if (featureRange != null && (range = featureRange.get(feature)) != null) {
						// We already took care that if featureRange is not null,
						// neither is other.featureRange
						if (!range.equals(other.featureRange.get(feature))) {
							return false;
						}
					}
				} else if (othervalue != null) {
					return false;
				}
			}
		}
		return true;
	}

}
