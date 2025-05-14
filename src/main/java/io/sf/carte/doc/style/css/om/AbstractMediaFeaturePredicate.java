/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSRatioValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValueFactory;
import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryPredicate;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * Media feature predicate implementation.
 * 
 */
abstract class AbstractMediaFeaturePredicate extends MediaPredicate implements MediaFeature {

	private static final long serialVersionUID = 1L;

	private CSSTypedValue value1 = null;
	private CSSTypedValue value2 = null;
	private byte rangeType;

	AbstractMediaFeaturePredicate(String featureName) {
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
	public CSSTypedValue getValue() {
		return value1;
	}

	@Override
	public void setValue(LexicalUnit value) {
		if (value != null) {
			CSSValue cssval = getValueFactory().createCSSValue(value);
			if (cssval.getCssValueType() != CssType.TYPED) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Expected typed value, got: " + cssval.getCssValueType());
			}
			this.value1 = (CSSTypedValue) cssval;
		} else {
			this.value1 = null;
		}
	}

	/**
	 * Get a value factory.
	 * 
	 * @return the value factory.
	 */
	abstract protected CSSValueFactory getValueFactory();

	@Override
	public CSSTypedValue getRangeSecondValue() {
		return value2;
	}

	@Override
	public void setValueRange(LexicalUnit value1, LexicalUnit value2) {
		CSSValueFactory factory = getValueFactory();
		CSSValue cssval = factory.createCSSValue(value1);
		if (cssval.getCssValueType() != CssType.TYPED) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Expected typed value, got: " + cssval.getCssValueType());
		}
		this.value1 = (CSSTypedValue) cssval;

		cssval = factory.createCSSValue(value2);
		if (cssval.getCssValueType() != CssType.TYPED) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Expected typed value, got: " + cssval.getCssValueType());
		}
		this.value2 = (CSSTypedValue) cssval;
	}

	@Override
	public boolean matches(MediaPredicate otherPredicate, byte negatedQuery) {
		if (getPredicateType() != otherPredicate.getPredicateType()) {
			return false;
		}

		AbstractMediaFeaturePredicate other = (AbstractMediaFeaturePredicate) otherPredicate;
		String feature = getName();
		String oFeature = other.getName();
		byte type = getRangeType();
		byte oType = other.getRangeType();

		// Canonicalize types and feature names
		if (type == MediaFeaturePredicate.FEATURE_PLAIN) {
			if (feature.startsWith("min-")) {
				feature = feature.substring(4);
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				type = MediaFeaturePredicate.FEATURE_GE;
			} else if (feature.startsWith("max-")) {
				// <=
				feature = feature.substring(4);
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				type = MediaFeaturePredicate.FEATURE_LE;
			} else {
				if (feature.startsWith("device-")) {
					feature = feature.substring(7);
				}
				if (value1 != null) {
					type = MediaFeaturePredicate.FEATURE_EQ;
				} else {
					type = MediaFeaturePredicate.FEATURE_GT;
				}
			}
		}

		if (oType == MediaFeaturePredicate.FEATURE_PLAIN) {
			if (oFeature.startsWith("min-")) {
				oFeature = oFeature.substring(4);
				if (oFeature.startsWith("device-")) {
					oFeature = oFeature.substring(7);
				}
				oType = MediaFeaturePredicate.FEATURE_GE;
			} else if (oFeature.startsWith("max-")) {
				// <=
				oFeature = oFeature.substring(4);
				if (oFeature.startsWith("device-")) {
					oFeature = oFeature.substring(7);
				}
				oType = MediaFeaturePredicate.FEATURE_LE;
			} else {
				if (oFeature.startsWith("device-")) {
					oFeature = oFeature.substring(7);
				}
				if (other.value1 != null) {
					oType = MediaFeaturePredicate.FEATURE_EQ;
				} else {
					oType = MediaFeaturePredicate.FEATURE_GT;
				}
			}
		}

		if (!feature.equals(oFeature)) {
			return false;
		}

		// Negate condition?
		if (negatedQuery == 2) {
			oType = negateType(oType);
		} else if (negatedQuery == 1) {
			type = negateType(type);
		}

		// Normalize type
		CSSTypedValue otherVal1 = other.value1;
		CSSTypedValue otherVal2 = other.value2;
		boolean noeq1 = false;
		boolean noeq2 = false;
		switch (type) {
		case MediaFeaturePredicate.FEATURE_EQ:
			if (value1.getPrimitiveType() == CSSValue.Type.IDENT) {
				if (otherVal1 != null && otherVal1.getPrimitiveType() == CSSValue.Type.IDENT
						&& value1.getStringValue().equalsIgnoreCase(otherVal1.getStringValue())) {
					return negatedQuery == 0 || negatedQuery == 3;
				}
				return negatedQuery == 1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT:
			if (oType == MediaFeaturePredicate.FEATURE_LE) {
				oType = MediaFeaturePredicate.FEATURE_LT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_LT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GT || oType == MediaFeaturePredicate.FEATURE_GT_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT || oType == MediaFeaturePredicate.FEATURE_GE_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_LT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT || oType == MediaFeaturePredicate.FEATURE_LT_AND_LT) {
				oType = MediaFeaturePredicate.FEATURE_LT;
				otherVal1 = otherVal2;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LE || oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_LT;
				noeq1 = true;
				otherVal1 = otherVal2;
			}
			break;
		case MediaFeaturePredicate.FEATURE_LE:
			if (oType == MediaFeaturePredicate.FEATURE_LT) {
				type = MediaFeaturePredicate.FEATURE_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_LE;
			} else if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GT || oType == MediaFeaturePredicate.FEATURE_GT_AND_GE) {
				type = MediaFeaturePredicate.FEATURE_LT;
				oType = MediaFeaturePredicate.FEATURE_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT || oType == MediaFeaturePredicate.FEATURE_GE_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_LE;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT || oType == MediaFeaturePredicate.FEATURE_LT_AND_LT) {
				type = MediaFeaturePredicate.FEATURE_LT;
				oType = MediaFeaturePredicate.FEATURE_LT;
				otherVal1 = otherVal2;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LE || oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_LE;
				otherVal1 = otherVal2;
			}
			break;
		case MediaFeaturePredicate.FEATURE_GT:
			if (oType == MediaFeaturePredicate.FEATURE_GE) {
				oType = MediaFeaturePredicate.FEATURE_GT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_GT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LT || oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT || oType == MediaFeaturePredicate.FEATURE_LE_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_GT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT || oType == MediaFeaturePredicate.FEATURE_GT_AND_GT) {
				oType = MediaFeaturePredicate.FEATURE_GT;
				otherVal1 = otherVal2;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GE || oType == MediaFeaturePredicate.FEATURE_GT_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_GT;
				noeq1 = true;
				otherVal1 = otherVal2;
			}
			break;
		case MediaFeaturePredicate.FEATURE_GE:
			if (oType == MediaFeaturePredicate.FEATURE_GT) {
				type = MediaFeaturePredicate.FEATURE_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_GE;
			} else if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LT || oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				type = MediaFeaturePredicate.FEATURE_GT;
				oType = MediaFeaturePredicate.FEATURE_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT || oType == MediaFeaturePredicate.FEATURE_LE_AND_LE) {
				type = MediaFeaturePredicate.FEATURE_GT;
				oType = MediaFeaturePredicate.FEATURE_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT || oType == MediaFeaturePredicate.FEATURE_GT_AND_GT) {
				type = MediaFeaturePredicate.FEATURE_GT;
				oType = MediaFeaturePredicate.FEATURE_GT;
				otherVal1 = otherVal2;
			} else if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GE || oType == MediaFeaturePredicate.FEATURE_GE_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_GE;
				otherVal1 = otherVal2;
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT) {
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				noeq1 = true;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				noeq1 = true;
				noeq2 = true;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LT) {
				type = MediaFeaturePredicate.FEATURE_LT_AND_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_LE_AND_LT;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				type = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_LE_AND_LT;
				noeq2 = true;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LT) {
				type = MediaFeaturePredicate.FEATURE_LT_AND_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LE) {
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LE;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT) {
				type = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_LT_AND_LE;
				noeq1 = true;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LT) {
				type = MediaFeaturePredicate.FEATURE_LT_AND_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_LT_AND_LE) {
				type = MediaFeaturePredicate.FEATURE_LT_AND_LE;
			} else if (oType == MediaFeaturePredicate.FEATURE_LE_AND_LT) {
				type = MediaFeaturePredicate.FEATURE_LE_AND_LT;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_LE_AND_LE;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT) {
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				noeq1 = true;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				noeq1 = true;
				noeq2 = true;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GT) {
				type = MediaFeaturePredicate.FEATURE_GT_AND_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_GE_AND_GT;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GE) {
				type = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				noeq2 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_GE_AND_GT;
				noeq2 = true;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GT) {
				type = MediaFeaturePredicate.FEATURE_GT_AND_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GE) {
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GE;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT) {
				type = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GT;
				noeq1 = true;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_GT_AND_GE;
				noeq1 = true;
				otherVal2 = otherVal1;
			}
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
			if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GT) {
				type = MediaFeaturePredicate.FEATURE_GT_AND_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_GT_AND_GE) {
				type = MediaFeaturePredicate.FEATURE_GT_AND_GE;
			} else if (oType == MediaFeaturePredicate.FEATURE_GE_AND_GT) {
				type = MediaFeaturePredicate.FEATURE_GE_AND_GT;
			} else if (oType == MediaFeaturePredicate.FEATURE_EQ) {
				oType = MediaFeaturePredicate.FEATURE_GE_AND_GE;
				otherVal2 = otherVal1;
			}
			break;
		}
		if (type != oType) {
			return false;
		}

		// Values
		float fval1;
		float denom = 1f;
		boolean isRatio = false;
		short pType = CSSUnit.CSS_NUMBER;
		if (value1 == null) {
			// Boolean
			if (otherVal1 == null) {
				boolean negated = negatedQuery == 1 || negatedQuery == 2;
				return !negated;
			}
			fval1 = 0f;
		} else {
			pType = value1.getUnitType();
			if (value1.getPrimitiveType() != CSSValue.Type.RATIO) {
				/*
				 * If it is not a number, throws an exception.
				 */
				fval1 = value1.getFloatValue(pType);
			} else {
				CSSRatioValue ratio = (CSSRatioValue) value1;
				CSSPrimitiveValue ante = ratio.getAntecedentValue();
				CSSPrimitiveValue cons = ratio.getConsequentValue();
				pType = ante.getUnitType();
				if (pType != CSSUnit.CSS_NUMBER || cons.getUnitType() != CSSUnit.CSS_NUMBER) {
					// We may have a custom property or calc() here.
					return false;
				}
				try {
					fval1 = ((CSSTypedValue) ante).getFloatValue(pType);
					denom = ((CSSTypedValue) cons).getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
				isRatio = true;
			}
		}

		float ofval1;
		if (otherVal1 == null) {
			ofval1 = 0f;
		} else {
			if (otherVal1.getPrimitiveType() == CSSValue.Type.RATIO) {
				CSSRatioValue ratio = (CSSRatioValue) otherVal1;
				CSSPrimitiveValue ante = ratio.getAntecedentValue();
				CSSPrimitiveValue cons = ratio.getConsequentValue();
				if (ante.getUnitType() != CSSUnit.CSS_NUMBER || cons.getUnitType() != CSSUnit.CSS_NUMBER) {
					// We may have a custom property or calc() here.
					return false;
				}
				float odenom;
				try {
					ofval1 = ((CSSTypedValue) ante).getFloatValue(pType);
					odenom = ((CSSTypedValue) cons).getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
				fval1 *= odenom;
			} else {
				try {
					ofval1 = otherVal1.getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
			}
			if (isRatio) {
				ofval1 *= denom;
			}
		}

		float fval2 = Float.NaN;
		float ofval2 = Float.NaN;
		if (value2 != null) {
			if (otherVal2 == null) {
				return false; // That should never happen
			}

			boolean isRatio2 = false;
			pType = value2.getUnitType();
			if (value2.getPrimitiveType() != CSSValue.Type.RATIO) {
				try {
					fval2 = value2.getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
			} else {
				CSSRatioValue ratio = (CSSRatioValue) value2;
				CSSPrimitiveValue ante = ratio.getAntecedentValue();
				CSSPrimitiveValue cons = ratio.getConsequentValue();
				pType = ante.getUnitType();
				if (pType != CSSUnit.CSS_NUMBER || cons.getUnitType() != CSSUnit.CSS_NUMBER) {
					return false;
				}
				try {
					fval2 = ((CSSTypedValue) ante).getFloatValue(pType);
					denom = ((CSSTypedValue) cons).getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
				isRatio2 = true;
			}
			if (otherVal2.getPrimitiveType() == CSSValue.Type.RATIO) {
				CSSRatioValue ratio = (CSSRatioValue) otherVal2;
				CSSPrimitiveValue ante = ratio.getAntecedentValue();
				CSSPrimitiveValue cons = ratio.getConsequentValue();
				if (ante.getUnitType() != CSSUnit.CSS_NUMBER || cons.getUnitType() != CSSUnit.CSS_NUMBER) {
					return false;
				}
				float odenom;
				try {
					ofval2 = ((CSSTypedValue) ante).getFloatValue(pType);
					odenom = ((CSSTypedValue) cons).getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
				fval2 *= odenom;
			} else {
				try {
					ofval2 = otherVal2.getFloatValue(pType);
				} catch (DOMException e) {
					return false;
				}
			}
			if (isRatio2) {
				ofval2 *= denom;
			}
		}

		switch (type) {
		case MediaFeaturePredicate.FEATURE_EQ:
			boolean negated = negatedQuery == 1 || negatedQuery == 2;
			if (MediaQueryImpl.floatEquals(fval1, ofval1)) {
				return !negated;
			}
			return negated;
		case MediaFeaturePredicate.FEATURE_LT:
		case MediaFeaturePredicate.FEATURE_LE:
			return noeq1 ? fval1 > ofval1 : fval1 >= ofval1;
		case MediaFeaturePredicate.FEATURE_GT:
		case MediaFeaturePredicate.FEATURE_GE:
			return noeq1 ? fval1 < ofval1 : fval1 <= ofval1;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			boolean first = noeq1 ? fval1 < ofval1 : fval1 <= ofval1;
			boolean second = noeq2 ? ofval2 < fval2 : ofval2 <= fval2;
			return first && second;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
			first = noeq1 ? fval1 > ofval1 : fval1 >= ofval1;
			second = noeq2 ? ofval2 > fval2 : ofval2 >= fval2;
			return first && second;
		default:
			return false;
		}
	}

	private static byte negateType(byte type) {
		switch (type) {
		case MediaFeaturePredicate.FEATURE_LT:
			type = MediaFeaturePredicate.FEATURE_GE;
			break;
		case MediaFeaturePredicate.FEATURE_LE:
			type = MediaFeaturePredicate.FEATURE_GT;
			break;
		case MediaFeaturePredicate.FEATURE_GT:
			type = MediaFeaturePredicate.FEATURE_LE;
			break;
		case MediaFeaturePredicate.FEATURE_GE:
			type = MediaFeaturePredicate.FEATURE_LT;
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			type = MediaFeaturePredicate.FEATURE_GT_AND_GT;
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			type = MediaFeaturePredicate.FEATURE_GT_AND_GE;
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			type = MediaFeaturePredicate.FEATURE_GE_AND_GT;
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			type = MediaFeaturePredicate.FEATURE_GE_AND_GE;
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
			type = MediaFeaturePredicate.FEATURE_LT_AND_LT;
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			type = MediaFeaturePredicate.FEATURE_LT_AND_LE;
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			type = MediaFeaturePredicate.FEATURE_LE_AND_LT;
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			type = MediaFeaturePredicate.FEATURE_LE_AND_LE;
			break;
		}
		return type;
	}

	@Override
	public void appendText(StringBuilder buf) {
		buf.append('(');
		switch (rangeType) {
		case MediaFeaturePredicate.FEATURE_PLAIN:
		case MediaFeaturePredicate.FEATURE_EQ:
			appendFeatureName(buf);
			if (value1 != null) {
				buf.append(": ");
				buf.append(value1.getCssText());
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT:
			appendFeatureName(buf);
			buf.append(" < ");
			buf.append(value1.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_LE:
			appendFeatureName(buf);
			buf.append(" <= ");
			buf.append(value1.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_GT:
			appendFeatureName(buf);
			buf.append(" > ");
			buf.append(value1.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_GE:
			appendFeatureName(buf);
			buf.append(" >= ");
			buf.append(value1.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			buf.append(value1.getCssText());
			buf.append(" < ");
			appendFeatureName(buf);
			buf.append(" <= ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			buf.append(value1.getCssText());
			buf.append(" < ");
			appendFeatureName(buf);
			buf.append(" < ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			buf.append(value1.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf);
			buf.append(" < ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			buf.append(value1.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf);
			buf.append(" <= ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			buf.append(value1.getCssText());
			buf.append(" > ");
			appendFeatureName(buf);
			buf.append(" > ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			buf.append(value1.getCssText());
			buf.append(" >= ");
			appendFeatureName(buf);
			buf.append(" > ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			buf.append(value1.getCssText());
			buf.append(" > ");
			appendFeatureName(buf);
			buf.append(" >= ");
			buf.append(value2.getCssText());
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
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
		case MediaFeaturePredicate.FEATURE_PLAIN:
		case MediaFeaturePredicate.FEATURE_EQ:
			appendFeatureName(buf);
			if (value1 != null) {
				buf.append(':');
				buf.append(value1.getMinifiedCssText(""));
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT:
			appendFeatureName(buf);
			buf.append('<');
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_LE:
			appendFeatureName(buf);
			buf.append("<=");
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_GT:
			appendFeatureName(buf);
			buf.append('>');
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_GE:
			appendFeatureName(buf);
			buf.append(">=");
			buf.append(value1.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('<');
			appendFeatureName(buf);
			buf.append("<=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('<');
			appendFeatureName(buf);
			buf.append('<');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append("<=");
			appendFeatureName(buf);
			buf.append('<');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			buf.append(value1.getMinifiedCssText(""));
			buf.append("<=");
			appendFeatureName(buf);
			buf.append("<=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('>');
			appendFeatureName(buf);
			buf.append('>');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			buf.append(value1.getMinifiedCssText(""));
			buf.append(">=");
			appendFeatureName(buf);
			buf.append('>');
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			buf.append(value1.getMinifiedCssText(""));
			buf.append('>');
			appendFeatureName(buf);
			buf.append(">=");
			buf.append(value2.getMinifiedCssText(""));
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
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
		if (rangeType == MediaFeaturePredicate.FEATURE_PLAIN) {
			// We handle 'feature: value' effectively as 'feature = value'
			efftype = MediaFeaturePredicate.FEATURE_EQ;
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
		if (!(obj instanceof AbstractMediaFeaturePredicate)) {
			return false;
		}
		AbstractMediaFeaturePredicate other = (AbstractMediaFeaturePredicate) obj;
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
