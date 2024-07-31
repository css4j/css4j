/*

 Copyright (c) 2005-2024, Carlos Amengual.

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
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;

/**
 * Determine the dimension of an expression.
 * <p>
 * This class is not thread-safe.
 * </p>
 */
class DimensionalEvaluator extends Evaluator {

	private transient boolean hasPercentage;

	private transient Random random = null;

	private transient CSSExpression latestExpression;

	private transient CSSMathFunctionValue latestFunction;

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
	 * @throws DOMException if a problem was found evaluating the expression.
	 */
	short computeUnitType(CSSExpression expression) throws DOMException {
		hasPercentage = false;
		Unit resultUnit = new Unit();
		evaluateExpression(expression, resultUnit);

		short unit;
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			unit = CSSUnit.CSS_INVALID;
		} else {
			unit = resultUnit.getUnitType();
		}

		return unit;
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

	@Override
	TypedValue evaluateFunction(CSSMathFunctionValue function, Unit resultUnit)
			throws DOMException {
		this.latestFunction = function;
		return super.evaluateFunction(function, resultUnit);
	}

	/**
	 * Compute the result unit type of the given expression.
	 * 
	 * @param expression the expression to analyze.
	 * @return the unit type of the result, as in {@link CSSUnit}.
	 * @throws DOMException if a problem was found evaluating the function.
	 */
	short computeUnitType(CSSMathFunctionValue function) throws DOMException {
		hasPercentage = false;
		Unit resultUnit = new Unit();
		evaluateFunction(function, resultUnit);

		short unit = resultUnit.getUnitType();
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0 || (hasPercentage && unit != CSSUnit.CSS_PERCENTAGE
				&& unit != CSSUnit.CSS_NUMBER)) {
			unit = CSSUnit.CSS_INVALID;
		}

		return unit;
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
	Category dimensionalAnalysis(CSSMathFunctionValue function) throws DOMException {
		hasPercentage = false;
		Unit resultUnit = new Unit();
		evaluateFunction(function, resultUnit);

		short unit;
		switch (resultUnit.getExponent()) {
		case 0:
			// In case we got a sign()-like function
			// with a % argument
			hasPercentage = false;
		case 1:
			unit = resultUnit.getUnitType();
			break;
		default:
			unit = CSSUnit.CSS_INVALID;
		}

		return getCategory(unit);
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
	protected CSSValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
		if (partialValue.getPrimitiveType() == Type.ATTR) {
			checkRandom();
			AttrValue attr = (AttrValue) partialValue;
			String attrtype = attr.getAttributeType();
			if (attrtype != null) {
				int len = attrtype.length();
				if (len <= 2) {
					ValueFactory factory = new ValueFactory();
					NumberValue value = (NumberValue) factory.parseProperty('1' + attrtype);
					value.setFloatValue(value.getUnitType(), random.nextFloat() + 1.1f);
					return value;
				}
				if ("length".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_PX,
							random.nextFloat() + 1.1f);
				} else if ("percentage".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE,
							random.nextFloat() * 10f + 1.1f);
				} else if ("integer".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER,
							random.nextInt(15) + 1);
				} else if ("number".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER,
							random.nextFloat() + 1.1f);
				} else if ("angle".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_RAD,
							random.nextFloat() + 1.1f);
				} else if ("time".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_S,
							random.nextFloat() + 1.1f);
				} else if ("frequency".equals(attrtype)) {
					return NumberValue.createCSSNumberValue(CSSUnit.CSS_HZ,
							random.nextFloat() * 10f + 1.1f);
				}
			}
		}
		return super.absoluteProxyValue(partialValue);
	}

	@Override
	protected TypedValue absoluteTypedValue(TypedValue partialValue) {
		short unit = partialValue.getUnitType();
		if (CSSUnit.isRelativeLengthUnitType(unit)) {
			// Check if random is initialized
			checkRandom();
			// Use an absolute unit
			unit = CSSUnit.CSS_PX;
			// Multiply value by random number, to avoid accidental cancellation
			NumberValue number = NumberValue.createCSSNumberValue(CSSUnit.CSS_PX,
					partialValue.getFloatValue(partialValue.getUnitType())
							* (random.nextFloat() + 1.1f));
			number.setCalculatedNumber(true);
			partialValue = number;
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			hasPercentage = true;
		}
		return partialValue;
	}

	private void checkRandom() {
		if (random == null) {
			random = new Random();
		}
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
		short unit = CSSUnit.CSS_PERCENTAGE;

		// Scan for preferred unit
		if (latestExpression != null) {
			CSSExpression expression = latestExpression.getParentExpression();
			while (expression != null && expression.getPartType() == AlgebraicPart.PRODUCT) {
				expression = expression.getParentExpression();
			}

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
		}

		if (latestFunction != null && unit == CSSUnit.CSS_PERCENTAGE) { // Math function
			LinkedCSSValueList args = latestFunction.getArguments();
			int len = args.getLength();
			argLoop: for (int i = 0; i < len; i++) {
				short argUnit;
				StyleValue arg = args.get(i);
				switch (arg.getPrimitiveType()) {
				case NUMERIC:
					argUnit = ((TypedValue) arg).getUnitType();
					if (argUnit != CSSUnit.CSS_PERCENTAGE && argUnit != CSSUnit.CSS_INVALID
							&& argUnit != CSSUnit.CSS_OTHER) {
						unit = argUnit;
						if (CSSUnit.isRelativeLengthUnitType(unit)) {
							unit = CSSUnit.CSS_PX;
						}
						break argLoop;
					}
					break;
				case EXPRESSION:
					argUnit = ((ExpressionValue) arg).computeUnitType();
					if (argUnit != CSSUnit.CSS_INVALID) {
						unit = argUnit;
						break argLoop;
					}
					break;
				case MATH_FUNCTION:
					argUnit = ((CSSMathFunctionValue) arg).computeUnitType();
					if (argUnit != CSSUnit.CSS_INVALID) {
						unit = argUnit;
						break argLoop;
					}
					break;
				default:
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
