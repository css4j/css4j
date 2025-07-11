/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSRatioValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryPredicate;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.parser.AbstractMediaQuery;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.NumberValue;

abstract class MediaQueryImpl extends AbstractMediaQuery {

	private static final long serialVersionUID = 1L;

	MediaQueryImpl() {
		super();
	}

	@Override
	protected void setMediaType(String mediaType) {
		super.setMediaType(mediaType);
	}

	@Override
	protected void setFeaturePredicate(BooleanCondition predicate) {
		super.setFeaturePredicate(predicate);
	}

	@Override
	protected void setNegative(boolean negative) {
		super.setNegative(negative);
	}

	@Override
	protected void setOnlyPrefix(boolean only) {
		super.setOnlyPrefix(only);
	}

	@Override
	protected boolean matchesPredicate(MediaQueryPredicate condition, CSSCanvas canvas) {
		if (condition.getPredicateType() == MediaQueryPredicate.MEDIA_FEATURE) {
			return condition instanceof MediaFeature
					&& matchesFeaturePredicate((MediaFeature) condition, canvas);
		}
		return true;
	}

	private boolean matchesFeaturePredicate(MediaFeature predicate, CSSCanvas canvas) {
		String feature = predicate.getName();
		CSSTypedValue value = predicate.getValue();
		predicate.getRangeSecondValue();
		byte type = predicate.getRangeType();
		if (type == 0 && value == null) {
			return featureBooleanMatch(feature, canvas);
		}
		if (type == MediaFeaturePredicate.FEATURE_PLAIN) {
			if (feature.startsWith("min-")) {
				// >=
				feature = feature.substring(4);
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				return featureRangeMatch(feature, MediaFeaturePredicate.FEATURE_GE, value, null, canvas);
			} else if (feature.startsWith("max-")) {
				// <=
				feature = feature.substring(4);
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				return featureRangeMatch(feature, MediaFeaturePredicate.FEATURE_LE, value, null, canvas);
			} else {
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				if (!isRangeFeature(feature)) {
					return canvas.matchesFeature(feature, value);
				} else {
					return featureRangeMatch(feature, MediaFeaturePredicate.FEATURE_EQ, value, null, canvas);
				}
			}
		} else {
			return featureRangeMatch(feature, type, value, predicate.getRangeSecondValue(), canvas);
		}
	}

	private static boolean featureBooleanMatch(String feature, CSSCanvas canvas) {
		if (isRangeFeature(feature)) {
			CSSTypedValue featured = canvas.getFeatureValue(feature);
			return !featured.isNumberZero();
		}
		return canvas.matchesFeature(feature, null);
	}

	private boolean featureRangeMatch(String feature, byte type, CSSTypedValue value,
			CSSTypedValue value2, CSSCanvas canvas) {
		CSSTypedValue featured = canvas.getFeatureValue(feature);
		if (featured == null) {
			return false;
		}
		short unitype = featured.getUnitType();
		float featureValue = featured.getFloatValue(unitype);
		float fval1, fval2 = 0;
		try {
			fval1 = valueInUnit(value, canvas, unitype);
		} catch (DOMException e) {
			return false;
		}
		if (type >= 6) {
			if (value2 == null) {
				return false;
			}
			try {
				fval2 = valueInUnit(value2, canvas, unitype);
			} catch (DOMException e) {
				return false;
			}
		}
		switch (type) {
		case MediaFeaturePredicate.FEATURE_EQ:
			return floatEquals(fval1, featureValue);
		case MediaFeaturePredicate.FEATURE_LT:
			return fval1 > featureValue;
		case MediaFeaturePredicate.FEATURE_LE:
			return fval1 >= featureValue;
		case MediaFeaturePredicate.FEATURE_GT:
			return fval1 < featureValue;
		case MediaFeaturePredicate.FEATURE_GE:
			return fval1 <= featureValue;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			return fval1 < featureValue && featureValue < fval2;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			return fval1 <= featureValue && featureValue < fval2;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			return fval1 < featureValue && featureValue <= fval2;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			return fval1 <= featureValue && featureValue <= fval2;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			return fval1 > featureValue && featureValue > fval2;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			return fval1 >= featureValue && featureValue > fval2;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			return fval1 > featureValue && featureValue >= fval2;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
			return fval1 >= featureValue && featureValue >= fval2;
		default:
			return false;
		}
	}

	private float valueInUnit(CSSTypedValue value, CSSCanvas canvas, short primitype)
			throws DOMException {
		float fval;
		switch (value.getPrimitiveType()) {
		case EXPRESSION:
			CSSExpressionValue evalue = (CSSExpressionValue) value;
			Evaluator ev = new MQEvaluator(canvas);
			fval = ev.evaluateExpression(evalue).getFloatValue(primitype);
			break;
		case RATIO:
			float ffirst, fsecond;
			CSSRatioValue ratio = (CSSRatioValue) value;
			CSSPrimitiveValue first = ratio.getAntecedentValue();
			CSSPrimitiveValue second = ratio.getConsequentValue();
			if (first.getUnitType() == CSSUnit.CSS_NUMBER) {
				ffirst = ((CSSTypedValue) first).getFloatValue();
			} else {
				// Calc
				ev = new MQEvaluator(canvas);
				ffirst = ev.evaluateExpression((CSSExpressionValue) first).getFloatValue(CSSUnit.CSS_NUMBER);
			}
			if (second.getUnitType() == CSSUnit.CSS_NUMBER) {
				fsecond = ((CSSTypedValue) second).getFloatValue();
			} else {
				// Calc
				ev = new MQEvaluator(canvas);
				fsecond = ev.evaluateExpression((CSSExpressionValue) second).getFloatValue(CSSUnit.CSS_NUMBER);
			}
			fval = ffirst / fsecond;
			break;
		case NUMERIC:
			return numericValueInUnit(value, canvas, primitype);
		default:
			throw new DOMInvalidAccessException("Unsupported type: " + value.getPrimitiveType());
		}
		return fval;
	}

	private static float numericValueInUnit(CSSTypedValue value, CSSCanvas canvas, short primitype)
			throws DOMException {
		float fval;
		StyleDatabase sdb;
		switch (value.getUnitType()) {
		case CSSUnit.CSS_EM:
			fval = value.getFloatValue(CSSUnit.CSS_EM);
			sdb = canvas.getStyleDatabase();
			float fontSize = sdb.getFontSizeFromIdentifier(null, "medium");
			fontSize = NumberValue.floatValueConversion(fontSize, sdb.getNaturalUnit(), primitype);
			fval *= fontSize;
			break;
		case CSSUnit.CSS_EX:
			fval = value.getFloatValue(CSSUnit.CSS_EX);
			sdb = canvas.getStyleDatabase();
			fontSize = sdb.getFontSizeFromIdentifier(null, "medium");
			float exSize = sdb.getExSizeInPt(null, fontSize);
			exSize = NumberValue.floatValueConversion(exSize, CSSUnit.CSS_PT, primitype);
			fval *= exSize;
			break;
		default:
			fval = value.getFloatValue(primitype);
		}
		return fval;
	}

	static boolean floatEquals(float value1, float value2) {
		return Math.abs(value2 - value1) < 7e-6;
	}

	protected abstract CSSNumberValue createNumberValue(short unit, float valueInSpecifiedUnit,
			boolean calculated);

	private class MQEvaluator extends Evaluator {

		private final CSSCanvas canvas;

		private final short expectedUnit;

		private MQEvaluator(CSSCanvas canvas) {
			this.canvas = canvas;
			this.expectedUnit = canvas.getStyleDatabase().getNaturalUnit();
		}

		@Override
		protected CSSNumberValue absoluteTypedValue(CSSTypedValue partialValue) {
			if (partialValue.getUnitType() != CSSUnit.CSS_NUMBER) {
				float fval = valueInUnit(partialValue, canvas, expectedUnit);
				return createNumberValue(expectedUnit, fval, true);
			}
			return super.absoluteTypedValue(partialValue);
		}

		@Override
		protected CSSNumberValue createNumberValue(short unit, float valueInSpecifiedUnit,
				boolean calculated) {
			return MediaQueryImpl.this.createNumberValue(unit, valueInSpecifiedUnit, calculated);
		}

	}

	/**
	 * Determine whether the two conditions match.
	 * 
	 * @param condition      the first condition.
	 * @param otherCondition the second consdition.
	 * @param negatedQuery   <code>0</code> if it is a direct match, <code>1</code>
	 *                       if the this predicate is reverse (negated),
	 *                       <code>2</code> if the given predicate is negated,
	 *                       <code>3</code> if both are negated.
	 * @return <code>1</code> if they match, <code>0</code> if don't, <code>2</code>
	 *         if the match should not be taken into account.
	 */
	@Override
	protected byte matches(BooleanCondition condition, BooleanCondition otherCondition, byte negatedQuery) {
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
			if (!(condition instanceof MediaPredicate)) {
				// var()
				return 0;
			}
			MediaPredicate predicate = (MediaPredicate) condition;
			if (predicate.getPredicateType() == MediaQueryPredicate.MEDIA_TYPE) {
				// Ignore. We already checked this with the mediaType field.
				return 2;
			}
			switch (otherCondition.getType()) {
			case PREDICATE:
				if (!(otherCondition instanceof MediaPredicate)) {
					// var()
					return 0;
				}
				MediaPredicate otherPredicate = (MediaPredicate) otherCondition;
				if (otherPredicate.getPredicateType() == MediaQueryPredicate.MEDIA_TYPE) {
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
			case SELECTOR_FUNCTION:
				return 1;
			case OTHER:
				return 0;
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
		case SELECTOR_FUNCTION:
			return 1;
		case OTHER:
			break;
		}
		return 0;
	}

}
