/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.ExtendedCSSValueList;

/**
 * Expression/Function evaluator.
 * <p>
 * Evaluates <code>calc()</code> expressions as well as functions like
 * <code>min()</code>, <code>clamp()</code> or <code>cos()</code>.
 * <p>
 * To support percentages within <code>calc()</code> expressions, it must be
 * subclassed by something that supports a box model implementation, overriding
 * the {@link #percentage(ExtendedCSSPrimitiveValue, short)} method.
 */
public class Evaluator {

	public Evaluator() {
		super();
	}

	/**
	 * Evaluate the given function.
	 * <p>
	 * This method checks the result unit, assuming that the unit must match that of
	 * the returned primitive value (some functions may return values where the
	 * units are raised to a power greater than one, or lesser than zero).
	 * 
	 * @param function the function to evaluate.
	 * @return the result of evaluating the function, or the function itself if this
	 *         class does not know how to evaluate it.
	 * @throws DOMException if a problem was found evaluating the function, or the
	 *                      resulting unit is not a valid CSS unit.
	 */
	public ExtendedCSSPrimitiveValue evaluateFunction(CSSFunctionValue function) throws DOMException {
		Unit resultUnit = new Unit();
		ExtendedCSSPrimitiveValue result = evaluateFunction(function, resultUnit);
		if (Math.abs(resultUnit.getExponent()) > 1) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
		}
		return result;
	}

	/**
	 * Evaluate the given function.
	 * 
	 * @param function   the function to evaluate.
	 * @param resultUnit the result unit(s).
	 * @return the result of evaluating the function, or the function itself if this
	 *         class does not know how to evaluate it.
	 * @throws DOMException if a problem was found evaluating the function.
	 */
	ExtendedCSSPrimitiveValue evaluateFunction(CSSFunctionValue function, Unit resultUnit) throws DOMException {
		String name = function.getFunctionName();
		if ("max".equalsIgnoreCase(name)) {
			return functionMax(function.getArguments(), resultUnit);
		} else if ("min".equalsIgnoreCase(name)) {
			return functionMin(function.getArguments(), resultUnit);
		} else if ("clamp".equalsIgnoreCase(name)) {
			return functionClamp(function.getArguments(), resultUnit);
		} else if ("sin".equalsIgnoreCase(name)) {
			return functionSin(function.getArguments(), resultUnit);
		} else if ("cos".equalsIgnoreCase(name)) {
			return functionCos(function.getArguments(), resultUnit);
		} else if ("tan".equalsIgnoreCase(name)) {
			return functionTan(function.getArguments(), resultUnit);
		} else if ("asin".equalsIgnoreCase(name)) {
			return functionASin(function.getArguments(), resultUnit);
		} else if ("acos".equalsIgnoreCase(name)) {
			return functionACos(function.getArguments(), resultUnit);
		} else if ("atan".equalsIgnoreCase(name)) {
			return functionATan(function.getArguments(), resultUnit);
		} else if ("atan2".equalsIgnoreCase(name)) {
			return functionATan2(function.getArguments(), resultUnit);
		} else if ("pow".equalsIgnoreCase(name)) {
			return functionPow(function.getArguments(), resultUnit);
		} else if ("sqrt".equalsIgnoreCase(name)) {
			return functionSqrt(function.getArguments(), resultUnit);
		} else if ("hypot".equalsIgnoreCase(name)) {
			return functionHypot(function.getArguments(), resultUnit);
		} else {
			// Do not know how to evaluate, convert arguments to absolute anyway.
			function = function.clone();
			LinkedCSSValueList args = function.getArguments();
			int sz = args.getLength();
			for (int i = 0; i < sz; i++) {
				ExtendedCSSValue value = args.item(i);
				if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					args.set(i, (StyleValue) absoluteValue((ExtendedCSSPrimitiveValue) value));
				}
			}
			return function;
		}
	}

	private ExtendedCSSPrimitiveValue functionMax(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "max() functions take at least one argument");
		}
		Iterator<? extends ExtendedCSSValue> it = arguments.iterator();
		ExtendedCSSValue arg = it.next();
		enforcePrimitiveType(arg);
		float max = floatValue((ExtendedCSSPrimitiveValue) arg, resultUnit);
		short firstUnit = resultUnit.getUnitType();
		short maxUnit = firstUnit;
		float maxInSpecifiedUnit = max;
		while (it.hasNext()) {
			arg = it.next();
			enforcePrimitiveType(arg);
			float partial = floatValue((ExtendedCSSPrimitiveValue) arg, resultUnit);
			float partialInFirstUnit = NumberValue.floatValueConversion(partial, resultUnit.getUnitType(), firstUnit);
			if (max < partialInFirstUnit) {
				max = partialInFirstUnit;
				maxInSpecifiedUnit = partial;
				maxUnit = resultUnit.getUnitType();
			}
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(maxUnit, maxInSpecifiedUnit);
		return value;
	}

	private void enforcePrimitiveType(ExtendedCSSValue arg) throws DOMException {
		if (arg.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected value: " + arg.getCssText());
		}
	}

	private ExtendedCSSPrimitiveValue functionMin(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "min() functions take at least one argument");
		}
		Iterator<? extends ExtendedCSSValue> it = arguments.iterator();
		ExtendedCSSValue arg = it.next();
		enforcePrimitiveType(arg);
		float min = floatValue((ExtendedCSSPrimitiveValue) arg, resultUnit);
		short firstUnit = resultUnit.getUnitType();
		short minUnit = firstUnit;
		float minInSpecifiedUnit = min;
		while (it.hasNext()) {
			arg = it.next();
			enforcePrimitiveType(arg);
			float partial = floatValue((ExtendedCSSPrimitiveValue) arg, resultUnit);
			float partialInFirstUnit = NumberValue.floatValueConversion(partial, resultUnit.getUnitType(), firstUnit);
			if (min > partialInFirstUnit) {
				min = partialInFirstUnit;
				minInSpecifiedUnit = partial;
				minUnit = resultUnit.getUnitType();
			}
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(minUnit, minInSpecifiedUnit);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionClamp(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 3) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Clamp functions take three arguments");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 1);
		float result = floatValue(arg, resultUnit);
		short centralUnit = resultUnit.getUnitType();
		arg = primitiveArgument(arguments, 0);
		float min = floatValue(arg, resultUnit);
		min = NumberValue.floatValueConversion(min, resultUnit.getUnitType(), centralUnit);
		arg = primitiveArgument(arguments, 2);
		float max = floatValue(arg, resultUnit);
		max = NumberValue.floatValueConversion(max, resultUnit.getUnitType(), centralUnit);
		if (result > max) {
			result = max;
		}
		if (result < min) {
			result = min;
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(centralUnit, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionSin(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sin() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSPrimitiveValue.CSS_NUMBER) {
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSPrimitiveValue.CSS_RAD);
		}
		float result = (float) Math.sin(fval);
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_NUMBER);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionCos(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "cos() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSPrimitiveValue.CSS_NUMBER) {
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSPrimitiveValue.CSS_RAD);
		}
		float result = (float) Math.cos(fval);
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_NUMBER);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionTan(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "tan() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSPrimitiveValue.CSS_NUMBER) {
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSPrimitiveValue.CSS_RAD);
		}
		float result = (float) Math.tan(fval);
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_NUMBER);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionASin(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.asin(floatValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionACos(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.acos(floatValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionATan(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float f1 = floatValue(arg, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() argument must be dimensionless");
		}
		float result = (float) Math.atan(f1);
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionATan2(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() functions take two arguments");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		ExtendedCSSPrimitiveValue arg2 = primitiveArgument(arguments, 1);
		float f1 = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSPrimitiveValue.CSS_NUMBER) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() arguments must be dimensionless");
		}
		float f2 = floatValue(arg2, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() arguments must be dimensionless");
		}
		float result = (float) Math.atan2(f1, f2);
		resultUnit.setUnitType(CSSPrimitiveValue.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionPow(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() functions take two arguments");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		ExtendedCSSPrimitiveValue arg2 = primitiveArgument(arguments, 1);
		resultUnit.setUnitType(arg.getPrimitiveType());
		float base = floatValue(arg, resultUnit);
		Unit expUnit = new Unit();
		float exponent = floatValue(arg2, expUnit);
		if (expUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() exponent cannot have a dimension");
		}
		float result = (float) Math.pow(base, exponent);
		resultUnit.setExponent(resultUnit.getExponent() * Math.round(exponent));
		NumberValue value = new NumberValue();
		value.setFloatValue(resultUnit.getUnitType(), result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionSqrt(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sqrt() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.sqrt(floatValue(arg, resultUnit));
		NumberValue value = new NumberValue();
		int exp = resultUnit.getExponent();
		if (exp % 2 != 0) {
			// Odd number
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "invalid CSS unit in sqrt() function");
		}
		resultUnit.setExponent(exp / 2);
		value.setFloatValue(resultUnit.getUnitType(), result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionHypot(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		int len = arguments.getLength();
		if (len == 2) {
			return functionHypot2(arguments, resultUnit);
		} else if (len == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "hypot() functions need at least one argument.");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float partial = floatValue(arg, resultUnit);
		double result = partial * partial;
		short firstUnit = resultUnit.getUnitType();
		for (int i = 1; i < len; i++) {
			arg = primitiveArgument(arguments, i);
			partial = floatValue(arg, resultUnit);
			if (firstUnit != resultUnit.getUnitType()) {
				partial = NumberValue.floatValueConversion(partial, resultUnit.getUnitType(), firstUnit);
			}
			result += partial * partial;
		}
		result = Math.sqrt(result);
		NumberValue value = new NumberValue();
		value.setFloatValue(firstUnit, (float) result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionHypot2(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			Unit resultUnit) throws DOMException {
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		ExtendedCSSPrimitiveValue arg2 = primitiveArgument(arguments, 1);
		Unit arg2Type = new Unit();
		float f1 = floatValue(arg, resultUnit);
		float f2 = floatValue(arg2, arg2Type);
		if (resultUnit.getUnitType() != arg2Type.getUnitType()) {
			f2 = NumberValue.floatValueConversion(f2, arg2Type.getUnitType(), resultUnit.getUnitType());
		}
		float result = (float) Math.hypot(f1, f2);
		NumberValue value = new NumberValue();
		value.setFloatValue(resultUnit.getUnitType(), result);
		return value;
	}

	private ExtendedCSSPrimitiveValue primitiveArgument(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			int index) {
		ExtendedCSSValue arg = arguments.item(index);
		enforcePrimitiveType(arg);
		return (ExtendedCSSPrimitiveValue) arg;
	}

	private float floatValue(ExtendedCSSPrimitiveValue value, Unit resultUnit) throws DOMException {
		value = evaluate(value, resultUnit);
		short resultType = resultUnit.getUnitType();
		float result;
		short type = value.getPrimitiveType();
		if (type == CSSPrimitiveValue.CSS_NUMBER) {
			result = value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
		} else if (type != CSSPrimitiveValue.CSS_PERCENTAGE) {
			result = value.getFloatValue(resultType);
		} else {
			result = percentage(value, CSSPrimitiveValue.CSS_PT);
			resultUnit.setUnitType(CSSPrimitiveValue.CSS_PT);
		}
		return result;
	}

	/**
	 * Evaluate the given expression.
	 * <p>
	 * This method checks the result unit, assuming that the unit must match that of
	 * the returned primitive value (some functions may return values where the
	 * units are raised to a power greater than one, or lesser than zero).
	 * 
	 * @param calc the expression value to evaluate.
	 * @return the result from evaluating the expression.
	 * @throws DOMException if a problem was found evaluating the expression.
	 */
	public ExtendedCSSPrimitiveValue evaluateExpression(ExpressionValue calc) throws DOMException {
		Unit resultUnit = new Unit();
		ExtendedCSSPrimitiveValue result = evaluateExpression(calc.getExpression(), resultUnit);
		if (Math.abs(resultUnit.getExponent()) > 1) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
		}
		if (calc.mustRoundResult() && result.isCalculatedNumber()) {
			((NumberValue) result).roundToInteger();
		}
		return result;
	}

	/**
	 * Evaluate the given expression.
	 *
	 * @param expression the expression to evaluate.
	 * @param resultUnit the result unit(s).
	 * @return the result from evaluating the expression.
	 * @throws DOMException if a problem was found evaluating the expression.
	 */
	ExtendedCSSPrimitiveValue evaluateExpression(CSSExpression expression, Unit resultUnit) throws DOMException {
		float result;
		switch (expression.getPartType()) {
		case SUM:
			AlgebraicExpression sum = (AlgebraicExpression) expression;
			result = sum(sum, resultUnit);
			if (expression.getParentExpression() == null && expression.isInverseOperation()) {
				result = -result;
			}
			break;
		case PRODUCT:
			AlgebraicExpression prod = (AlgebraicExpression) expression;
			result = multiply(prod, resultUnit);
			break;
		default:
			return evaluate(((CSSOperandExpression) expression).getOperand(), resultUnit);
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(resultUnit.getUnitType(), result);
		value.setCalculatedNumber(true);
		value.setAbsolutizedUnit();
		return value;
	}

	private float sum(AlgebraicExpression sumop, Unit resultUnit) throws DOMException {
		int len = sumop.getLength();
		if (len == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Sum without operands.");
		}
		CSSExpression op = sumop.item(0);
		float result = floatValue(evaluateExpression(op, resultUnit), resultUnit);
		if (op.isInverseOperation()) {
			result = -result;
		}
		short firstUnit = resultUnit.getUnitType();
		for (int i = 1; i < len; i++) {
			op = sumop.item(i);
			float partial = floatValue(evaluateExpression(op, resultUnit), resultUnit);
			short partialUnit = resultUnit.getUnitType();
			if (firstUnit != partialUnit) {
				partial = NumberValue.floatValueConversion(partial, partialUnit, firstUnit);
			}
			if (op.isInverseOperation()) {
				result -= partial;
			} else {
				result += partial;
			}
		}
		resultUnit.setUnitType(firstUnit);
		return result;
	}

	private float multiply(AlgebraicExpression product, Unit resultUnit) throws DOMException {
		float result = 1f;
		short firstUnit = CSSPrimitiveValue.CSS_NUMBER;
		int unitExp = 0;
		int len = product.getLength();
		for (int i = 0; i < len; i++) {
			CSSExpression op = product.item(i);
			ExtendedCSSPrimitiveValue partialValue = evaluateExpression(op, resultUnit);
			float partial = floatValue(partialValue, resultUnit);
			short partialUnit = resultUnit.getUnitType();
			if (partialUnit != CSSPrimitiveValue.CSS_NUMBER) {
				if (firstUnit == CSSPrimitiveValue.CSS_NUMBER) {
					firstUnit = partialUnit;
				} else {
					try {
						partial = NumberValue.floatValueConversion(partial, partialUnit, firstUnit);
					} catch (DOMException e) {
						if (!op.isInverseOperation()) {
							partial = unitCancellation(partial, partialUnit, firstUnit, e);
							unitExp--;
						} else {
							throw e;
						}
						if (unitExp == 0) {
							firstUnit = CSSPrimitiveValue.CSS_NUMBER;
						}
						result *= partial;
						continue;
					}
					partialUnit = firstUnit;
				}
			}
			if (op.isInverseOperation()) {
				if (partialUnit != CSSPrimitiveValue.CSS_NUMBER) {
					unitExp--;
					if (unitExp != 0) {
						firstUnit = partialUnit;
					} else {
						firstUnit = CSSPrimitiveValue.CSS_NUMBER;
					}
				}
				result /= partial;
				if (Float.isNaN(result)) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Found NaN.");
				}
			} else {
				if (partialUnit != CSSPrimitiveValue.CSS_NUMBER) {
					unitExp++;
					if (unitExp != 0) {
						firstUnit = partialUnit;
					} else {
						firstUnit = CSSPrimitiveValue.CSS_NUMBER;
					}
				}
				result *= partial;
			}
		}
		if (unitExp < 0) {
			if (firstUnit == CSSPrimitiveValue.CSS_HZ) {
				firstUnit = CSSPrimitiveValue.CSS_S;
				unitExp = -unitExp;
			} else if (firstUnit == CSSPrimitiveValue.CSS_KHZ) {
				firstUnit = CSSPrimitiveValue.CSS_MS;
				unitExp = -unitExp;
			} else if (firstUnit == CSSPrimitiveValue.CSS_S) {
				firstUnit = CSSPrimitiveValue.CSS_HZ;
				unitExp = -unitExp;
			} else if (firstUnit == CSSPrimitiveValue.CSS_MS) {
				firstUnit = CSSPrimitiveValue.CSS_KHZ;
				unitExp = -unitExp;
			}
		}
		resultUnit.setUnitType(firstUnit);
		resultUnit.setExponent(unitExp);
		return result;
	}

	private float unitCancellation(float partial, short partialUnit, short firstUnit, DOMException exception)
			throws DOMException {
		if (partialUnit == CSSPrimitiveValue.CSS_S || partialUnit == CSSPrimitiveValue.CSS_MS) {
			if (firstUnit == CSSPrimitiveValue.CSS_HZ) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSPrimitiveValue.CSS_S);
			} else if (firstUnit == CSSPrimitiveValue.CSS_KHZ) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSPrimitiveValue.CSS_MS);
			}
		} else if (partialUnit == CSSPrimitiveValue.CSS_HZ || partialUnit == CSSPrimitiveValue.CSS_KHZ) {
			if (firstUnit == CSSPrimitiveValue.CSS_S) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSPrimitiveValue.CSS_HZ);
			} else if (firstUnit == CSSPrimitiveValue.CSS_MS) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSPrimitiveValue.CSS_KHZ);
			}
		}
		throw exception;
	}

	private ExtendedCSSPrimitiveValue evaluate(ExtendedCSSPrimitiveValue partialValue, Unit resultUnit) {
		short pType = partialValue.getPrimitiveType();
		if (pType == CSSPrimitiveValue2.CSS_FUNCTION) {
			partialValue = evaluateFunction((CSSFunctionValue) partialValue, resultUnit);
		} else if (pType == CSSPrimitiveValue2.CSS_EXPRESSION) {
			CSSExpression expr = ((ExpressionValue) partialValue).getExpression();
			partialValue = evaluateExpression(expr, resultUnit);
		} else {
			partialValue = absoluteValue(partialValue);
			resultUnit.setUnitType(partialValue.getPrimitiveType());
		}
		return partialValue;
	}

	/**
	 * Obtain an absolute (numeric) value, starting from a primitive value.
	 * <p>
	 * If the supplied value is already absolute, or it is not known how to express
	 * it in absolute units, return it.
	 * 
	 * @param partialValue the value that has to be expressed in absolute units.
	 * @return the value in absolute units.
	 */
	protected ExtendedCSSPrimitiveValue absoluteValue(ExtendedCSSPrimitiveValue partialValue) {
		return partialValue;
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
	protected float percentage(ExtendedCSSPrimitiveValue value, short resultType) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unexpected percentage in calc()");
	}

}
