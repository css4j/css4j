/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;

/**
 * Evaluates expressions with only percentages and plain numbers.
 */
public class PercentageEvaluator extends Evaluator {

	public PercentageEvaluator() {
		super(CSSUnit.CSS_PERCENTAGE);
	}

	@Override
	public TypedValue evaluateExpression(ExpressionValue calc) throws DOMException {
		TypedValue ret = super.evaluateExpression(calc);
		short unit = ret.getUnitType();
		if (unit != CSSUnit.CSS_PERCENTAGE && unit != CSSUnit.CSS_NUMBER) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unexpected calc() result.");
		}
		return ret;
	}

	@Override
	public TypedValue evaluateFunction(CSSMathFunctionValue function) throws DOMException {
		TypedValue ret = super.evaluateFunction(function);
		short unit = ret.getUnitType();
		if (unit != CSSUnit.CSS_PERCENTAGE && unit != CSSUnit.CSS_NUMBER) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unexpected calc() result.");
		}
		return ret;
	}

	/**
	 * Obtain the float value (in the requested absolute unit) corresponding to the
	 * given percentage value.
	 * 
	 * @param value      the percentage value.
	 * @param resultType the desired absolute result type.
	 * @return the absolute float value in the requested unit.
	 * @throws DOMException if the percentage could not be converted to the
	 *                      requested unit.
	 */
	@Override
	protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
		return value.getFloatValue(resultType);
	}

}
