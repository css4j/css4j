/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Random;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpression.AlgebraicPart;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

/**
 * Determine the dimension of an expression.
 * <p>
 * This class is not thread-safe.
 * </p>
 */
class DimensionalEvaluator extends Evaluator {

	private boolean hasPercentage;

	private transient Random random = null;

	private transient CSSExpression latestExpression;

	private transient boolean unknownFunction;

	DimensionalEvaluator() {
		super();
	}

	boolean hasUnknownFunction() {
		return unknownFunction;
	}

	@Override
	TypedValue evaluateExpression(CSSExpression expression, Unit resultUnit) throws DOMException {
		this.latestExpression = expression;
		return super.evaluateExpression(expression, resultUnit);
	}

	/**
	 * Compute the result unit type of the given expression.
	 * 
	 * @param expression the expression to analyze.
	 * @return the unit type of the result, as in {@link CSSUnit}.
	 * @throws DOMException if the resulting unit type is not a valid CSS unit.
	 */
	short computeUnitType(CSSExpression expression) throws DOMException {
		hasPercentage = false;
		Unit resultUnit = new Unit();
		evaluateExpression(expression, resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Resulting unit is not valid CSS unit.");
		}
		//
		return resultUnit.getUnitType();
	}

	/**
	 * Perform a dimensional analysis of the given expression and obtain the
	 * {@link Category} of the result.
	 * 
	 * @param expression the expression to analyze.
	 * @return the category of the result.
	 * @throws DOMException if the resulting unit type is unknown or not a valid CSS
	 *                      unit.
	 */
	Category dimensionalAnalysis(CSSExpression expression) throws DOMException {
		return getCategory(computeUnitType(expression));
	}

	/**
	 * Perform a dimensional analysis of the given function and obtain the
	 * {@link Category} of the result.
	 * 
	 * @param expression the function to analyze.
	 * @return the category of the result.
	 * @throws DOMException if the resulting unit type is unknown or not a valid CSS
	 *                      unit.
	 */
	Category dimensionalAnalysis(CSSFunctionValue function) throws DOMException {
		hasPercentage = false;
		Unit resultUnit = new Unit();
		evaluateFunction(function, resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Resulting unit is not valid CSS unit.");
		}
		//
		return getCategory(resultUnit.getUnitType());
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
			if (hasPercentage) {
				cat = Category.percentage;
			} else {
				cat = Category.number;
			}
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
	protected TypedValue absoluteTypedValue(TypedValue partialValue) {
		short unit = partialValue.getUnitType();
		if (CSSUnit.isRelativeLengthUnitType(unit)) {
			// Check if random is initialized
			if (random == null) {
				random = new Random();
			}
			// Use an absolute unit
			unit = CSSUnit.CSS_PX;
			// Multiply value by random number, to avoid accidental cancellation
			NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_PX,
					(float) (partialValue.getFloatValue(partialValue.getUnitType())
							* random.nextDouble(1.1d, 1.9d)));
			number.setCalculatedNumber(true);
			partialValue = number;
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			hasPercentage = true;
		}
		return partialValue;
	}

	@Override
	protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
		hasPercentage = true;
		return value.getFloatValue(CSSUnit.CSS_PERCENTAGE);
	}

	@Override
	TypedValue unknownFunction(CSSFunctionValue function, Unit resultUnit) {
		unknownFunction = true;
		return super.unknownFunction(function, resultUnit);
	}

	@Override
	short getPreferredUnit() {
		// Scan for preferred unit
		CSSExpression expression = latestExpression.getParentExpression();
		while (expression != null && expression.getPartType() == AlgebraicPart.PRODUCT) {
			expression = expression.getParentExpression();
		}

		short unit = CSSUnit.CSS_PERCENTAGE;
		if (expression != null) {
			// Now we must have a sum from which we may be able to infer the unit
			expression = findNonPercentOperand(expression);
			if (expression != null) {
				CSSOperandExpression operand = (CSSOperandExpression) expression;
				CSSPrimitiveValue value = operand.getOperand();
				if (value.getUnitType() != CSSUnit.CSS_INVALID) {
					unit = value.getUnitType();
					if (CSSUnit.isRelativeLengthUnitType(unit)) {
						unit = CSSUnit.CSS_PX;
					}
				}
			}
		}
		return unit;
	}

	private CSSExpression findNonPercentOperand(CSSExpression expression) {
		switch (expression.getPartType()) {
		case SUM:
			AlgebraicExpression sum = (AlgebraicExpression) expression;
			int len = sum.getLength();
			for (int i = 0; i < len; i++) {
				CSSExpression expr = sum.item(i);
				if (expr.getPartType() != AlgebraicPart.OPERAND) {
					expr = findNonPercentOperand(expr);
				}
				if (expr != null && expr.getPartType() == AlgebraicPart.OPERAND) {
					CSSOperandExpression operand = (CSSOperandExpression) expr;
					CSSPrimitiveValue value = operand.getOperand();
					if (value.getUnitType() != CSSUnit.CSS_PERCENTAGE
							&& value.getUnitType() != CSSUnit.CSS_INVALID) {
						expression = expr;
						break;
					}
				}
				expression = null;
			}
			break;
		case PRODUCT:
			AlgebraicExpression prod = (AlgebraicExpression) expression;
			int plen = prod.getLength();
			for (int i = 0; i < plen; i++) {
				CSSExpression expr = prod.item(i);
				if (expr.getPartType() != AlgebraicPart.OPERAND) {
					expr = findNonPercentOperand(expr);
				}
				if (expr != null && expr.getPartType() == AlgebraicPart.OPERAND) {
					CSSOperandExpression operand = (CSSOperandExpression) expr;
					CSSPrimitiveValue value = operand.getOperand();
					if (value.getUnitType() != CSSUnit.CSS_PERCENTAGE
							&& value.getUnitType() != CSSUnit.CSS_NUMBER
							&& value.getUnitType() != CSSUnit.CSS_INVALID) {
						expression = expr;
						break;
					}
				}
				expression = null;
			}
			break;
		default:
			// This isn't going to happen because callers already dealt with
			// operands.
		}
		return expression;
	}

}
