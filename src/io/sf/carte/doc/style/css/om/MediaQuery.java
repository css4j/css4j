/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.parser.BooleanCondition;
import io.sf.carte.doc.style.css.parser.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.CalcValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
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

	private static final HashSet<String> rangeFeatureSet;

	static {
		final String[] rangeFeatures = { "aspect-ratio", "color", "color-index", "height", "monochrome", "resolution",
				"width" };
		rangeFeatureSet = new HashSet<String>(rangeFeatures.length);
		Collections.addAll(rangeFeatureSet, rangeFeatures);
	}

	private String mediaType = null;

	private boolean negativeQuery = false;

	private boolean onlyPrefix = false;

	private BooleanCondition predicate = null;

	public MediaQuery() {
		super();
	}

	void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public BooleanCondition getCondition() {
		return predicate;
	}

	void setFeaturePredicate(BooleanCondition predicate) {
		this.predicate = predicate;
	}

	public boolean isNegative() {
		return negativeQuery;
	}

	void setNegative(boolean negative) {
		this.negativeQuery = negative;
	}

	void setOnlyPrefix(boolean only) {
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
		if (predicate != null) {
			if (canvas == null) {
				return false;
			}
			return matchesCondition(predicate, canvas);
		}
		return true;
	}

	private static boolean matchesCondition(BooleanCondition condition, CSSCanvas canvas) {
		switch (condition.getType()) {
		case AND:
			Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				BooleanCondition subcond = it.next();
				if (!matchesCondition(subcond, canvas)) {
					return false;
				}
			}
			return true;
		case NOT:
			return !matchesCondition(condition.getNestedCondition(), canvas);
		case OR:
			it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				BooleanCondition subcond = it.next();
				if (matchesCondition(subcond, canvas)) {
					return true;
				}
			}
			break;
		default:
			if (((BooleanConditionImpl.Predicate) condition).getPredicateType() == 0) {
				return matchesPredicate((MediaFeaturePredicate) condition, canvas);
			}
			return true;
		}
		return false;
	}

	private static boolean matchesPredicate(MediaFeaturePredicate predicate, CSSCanvas canvas) {
		String feature = predicate.getName();
		ExtendedCSSPrimitiveValue value = predicate.getValue();
		predicate.getRangeSecondValue();
		byte type = predicate.getRangeType();
		if (type == 0 && value == null) {
			return featureBooleanMatch(feature, canvas);
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
				if (!isRangeFeature(feature)) {
					if (canvas.matchesFeature(feature, value)) {
						return true;
					}
				} else if (featureRangeMatch(feature, FEATURE_EQ, value, null, canvas)) {
					return true;
				}
			}
		} else {
			if (featureRangeMatch(feature, type, value, predicate.getRangeSecondValue(), canvas)) {
				return true;
			}
		}
		return false;
	}

	private static boolean featureBooleanMatch(String feature, CSSCanvas canvas) {
		if (isRangeFeature(feature)) {
			ExtendedCSSPrimitiveValue featured = canvas.getFeatureValue(feature);
			return !featured.isNumberZero();
		}
		return canvas.matchesFeature(feature, null);
	}

	private static boolean featureRangeMatch(String feature, byte type, CSSPrimitiveValue value,
			CSSPrimitiveValue value2, CSSCanvas canvas) {
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
			return fval1 >= featureValue;
		case FEATURE_GT:
			return fval1 < featureValue;
		case FEATURE_GE:
			return fval1 <= featureValue;
		case FEATURE_LT_AND_LT:
			return fval1 < featureValue && featureValue < fval2;
		case FEATURE_LE_AND_LT:
			return fval1 <= featureValue && featureValue < fval2;
		case FEATURE_LT_AND_LE:
			return fval1 < featureValue && featureValue <= fval2;
		case FEATURE_LE_AND_LE:
			return fval1 <= featureValue && featureValue <= fval2;
		case FEATURE_GT_AND_GT:
			return fval1 > featureValue && featureValue > fval2;
		case FEATURE_GE_AND_GT:
			return fval1 >= featureValue && featureValue > fval2;
		case FEATURE_GT_AND_GE:
			return fval1 > featureValue && featureValue >= fval2;
		case FEATURE_GE_AND_GE:
			return fval1 >= featureValue && featureValue >= fval2;
		default:
			return false;
		}
	}

	private static float valueInUnit(CSSPrimitiveValue value, CSSCanvas canvas, short primitype) throws DOMException {
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
			fval = ev.evaluateExpression(evalue).getFloatValue(primitype);
			break;
		case CSSPrimitiveValue2.CSS_RATIO:
			float ffirst, fsecond;
			RatioValue ratio = (RatioValue) value;
			PrimitiveValue first = ratio.getAntecedentValue();
			PrimitiveValue second = ratio.getConsequentValue();
			if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
				ffirst = first.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			} else {
				// Calc
				ev = new MQEvaluator(canvas);
				ffirst = ev.evaluateExpression((CalcValue) first).getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			}
			if (second.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
				fsecond = second.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			} else {
				// Calc
				ev = new MQEvaluator(canvas);
				fsecond = ev.evaluateExpression((CalcValue) second).getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			}
			fval = ffirst / fsecond;
			break;
		default:
			fval = value.getFloatValue(primitype);
		}
		return fval;
	}

	static boolean floatEquals(float value1, float value2) {
		return Math.abs(value2 - value1) < 7e-6;
	}

	private static class MQEvaluator extends Evaluator {

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

	private static boolean isRangeFeature(String string) {
		return rangeFeatureSet.contains(string);
	}

	/**
	 * Check whether the given query is partially or totaly contained by this one.
	 * <p>
	 * If query A matches B, then if a medium matches B it will also match A. The
	 * opposite may not be true.
	 * 
	 * @param other the other query to check against.
	 * @return <code>true</code> if the other query is partially or totally
	 *         contained by this one.
	 */
	boolean matches(MediaQuery other) {
		if (other.isNotAllMedia()) {
			return false;
		}
		boolean isAllMedium = (mediaType == null || "all".equals(mediaType));
		if (negativeQuery) {
			if (isAllMedium) {
				if (predicate == null) {
					return false;
				}
			} else if (mediaType.equals(other.mediaType)) {
				if (!other.negativeQuery) {
					return false;
				}
			} else if (other.negativeQuery) {
				return false;
			}
		} else if (!isAllMedium && (other.negativeQuery || !mediaType.equals(other.mediaType))) {
			return false;
		}
		if (predicate == null) {
			return true;
		} else if (other.predicate == null) {
			return false;
		}
		byte negatedQuery;
		if (negativeQuery) {
			if (!other.negativeQuery) {
				negatedQuery = 1;
			} else {
				negatedQuery = 0;
			}
		} else if (other.negativeQuery) {
			negatedQuery = 2;
		} else {
			negatedQuery = 0;
		}
		return matches(predicate, other.predicate, negatedQuery) != 0;
	}

	private static byte matches(BooleanCondition condition, BooleanCondition otherCondition, byte negatedQuery) {
		switch (condition.getType()) {
		case AND:
			Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				BooleanCondition subcond = it.next();
				if (matches(subcond, otherCondition, negatedQuery) == 0) {
					return 0;
				}
			}
			return 1;
		case OR:
			it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				BooleanCondition subcond = it.next();
				if (matches(subcond, otherCondition, negatedQuery) == 1) {
					return 1;
				}
			}
			break;
		case PREDICATE:
			MediaPredicate predicate = (MediaPredicate) condition;
			if (predicate.getPredicateType() == MediaPredicate.MEDIA_TYPE) {
				// Ignore. We already checked this with the mediaType field.
				return 2;
			}
			switch (otherCondition.getType()) {
			case PREDICATE:
				MediaPredicate otherPredicate = (MediaPredicate) otherCondition;
				if (otherPredicate.getPredicateType() == MediaPredicate.MEDIA_TYPE) {
					// Ignore. We already checked this with the mediaType field.
					return 2;
				}
				if (predicate.matches(otherPredicate, negatedQuery)) {
					return 1;
				}
				return 0;
			case AND:
				it = otherCondition.getSubConditions().iterator();
				while (it.hasNext()) {
					BooleanCondition subcond = it.next();
					if (matches(condition, subcond, negatedQuery) == 1) {
						// left-side condition is met
						return 1;
					}
				}
				break;
			case OR:
				it = otherCondition.getSubConditions().iterator();
				while (it.hasNext()) {
					BooleanCondition subcond = it.next();
					if (matches(condition, subcond, negatedQuery) == 0) {
						return 0;
					}
				}
				// All left-side conditions are met
				return 1;
			case NOT:
				if (negatedQuery == 0) {
					negatedQuery = 2;
				} else if (negatedQuery == 1) {
					negatedQuery = 3;
				} else if (negatedQuery == 2) {
					negatedQuery = 0;
				} else { // 3
					negatedQuery = 1;
				}
				return matches(condition, otherCondition.getNestedCondition(), negatedQuery);
			}
			break;
		case NOT:
			if (negatedQuery == 0) {
				negatedQuery = 1;
			} else if (negatedQuery == 1) {
				negatedQuery = 0;
			} else if (negatedQuery == 2) {
				negatedQuery = 3;
			} else { // 3
				negatedQuery = 2;
			}
			return matches(condition.getNestedCondition(), otherCondition, negatedQuery);
		}
		return 0;
	}

	boolean isAllMedia() {
		return (mediaType == null || "all".equalsIgnoreCase(mediaType)) && !negativeQuery && predicate == null;
	}

	boolean isNotAllMedia() {
		return mediaType != null && "all".equalsIgnoreCase(mediaType) && negativeQuery && predicate == null;
	}

	public String getMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (predicate != null) {
			predicate.appendText(buf);
		} else if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		return buf.toString();
	}

	public String getMinifiedMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (predicate != null) {
			predicate.appendMinifiedText(buf);
		} else if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		return buf.toString();
	}

	static String escapeIdentifier(String medium) {
		return ParseHelper.escape(medium);
	}

	@Override
	public String toString() {
		return getMedia();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + (negativeQuery ? 1231 : 1237);
		result = prime * result + (onlyPrefix ? 1231 : 1237);
		if (predicate != null) {
			result = prime * result + predicate.hashCode();
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
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (other.predicate == null) {
			return false;
		} else if (!predicate.equals(other.predicate)) {
			return false;
		}
		return true;
	}

}
