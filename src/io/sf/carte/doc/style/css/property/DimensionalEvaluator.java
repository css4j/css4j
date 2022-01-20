/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

/**
 * Determine the dimension of an expression.
 */
class DimensionalEvaluator extends Evaluator {

	private boolean hasPercentage;

	DimensionalEvaluator() {
		super();
	}

	Category dimensionalAnalysis(CSSExpression expression) {
		hasPercentage = false;
		Unit resultUnit = new Unit();
		evaluateExpression(expression, resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
		}
		//
		return getCategory(resultUnit.getUnitType());
	}

	@Override
	protected TypedValue absoluteTypedValue(TypedValue partialValue) {
		boolean isLength;
		short unit = partialValue.getUnitType();
		if (isLength = CSSUnit.isLengthUnitType(unit)) {
			// Use an absolute unit
			unit = CSSUnit.CSS_PX;
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			hasPercentage = true;
			return partialValue;
		}
		try {
			partialValue.getFloatValue(unit);
		} catch (DOMException e) {
			if (isLength) {
				// Must be relative unit
				NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_PX,
						partialValue.getFloatValue(partialValue.getUnitType()));
				number.setCalculatedNumber(true);
				partialValue = number;
			}
		}
		return partialValue;
	}

	/**
	 * Get the type category according to the given unit.
	 * 
	 * @param unit
	 * @return the category.
	 * @throws DOMException if the new unit is incompatible with a numeric category.
	 */
	private Category getCategory(short unit) throws DOMException {
		Category cat;
		if (unit == CSSUnit.CSS_NUMBER) {
			cat = Category.number;
		} else if (CSSUnit.isLengthUnitType(unit)) {
			if (hasPercentage) {
				cat = Category.lengthPercentage;
			} else {
				cat = Category.length;
			}
		} else if (CSSUnit.isAngleUnitType(unit)) {
			cat = Category.angle;
		} else if (CSSUnit.isTimeUnitType(unit)) {
			cat = Category.time;
		} else if (CSSUnit.isResolutionUnitType(unit)) {
			cat = Category.resolution;
		} else if (unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ) {
			cat = Category.frequency;
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			cat = Category.percentage;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Unknown unit: " + unit);
		}
		return cat;
	}

	@Override
	protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
		hasPercentage = true;
		return value.getFloatValue(CSSUnit.CSS_PERCENTAGE);
	}

}
