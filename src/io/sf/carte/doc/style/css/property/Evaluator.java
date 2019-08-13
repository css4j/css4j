/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpression.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSFunctionValue;
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
		fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSPrimitiveValue.CSS_RAD);
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
		fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSPrimitiveValue.CSS_RAD);
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
		fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSPrimitiveValue.CSS_RAD);
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
		resultUnit.setExponent((short) (resultUnit.getExponent() * Math.round(exponent)));
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
		resultUnit.setExponent((short) (resultUnit.getExponent() / 2));
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
	 * @param expression the expression to evaluate.
	 * @return the result from evaluating the expression.
	 * @throws DOMException if a problem was found evaluating the expression.
	 */
	public ExtendedCSSPrimitiveValue evaluateExpression(CSSExpression expression) throws DOMException {
		Unit resultUnit = new Unit();
		ExtendedCSSPrimitiveValue result = evaluateExpression(expression, resultUnit);
		if (Math.abs(resultUnit.getExponent()) > 1) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
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
			AlgebraicExpression sum = (CSSExpression.AlgebraicExpression) expression;
			result = sum(sum.getOperands(), resultUnit);
			if (expression.getParentExpression() == null && expression.isInverseOperation()) {
				result = -result;
			}
			break;
		case PRODUCT:
			AlgebraicExpression prod = (CSSExpression.AlgebraicExpression) expression;
			result = multiply(prod.getOperands(), resultUnit);
			break;
		default:
			return evaluate(((CSSExpression.CSSOperandExpression) expression).getOperand(), resultUnit);
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(resultUnit.getUnitType(), result);
		return value;
	}

	private float sum(List<? extends CSSExpression> operands, Unit resultUnit) throws DOMException {
		Iterator<? extends CSSExpression> it = operands.iterator();
		if (!it.hasNext()) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Sum without operands.");
		}
		CSSExpression op = it.next();
		float result = floatValue(evaluateExpression(op, resultUnit), resultUnit);
		if (op.isInverseOperation()) {
			result = -result;
		}
		short firstUnit = resultUnit.getUnitType();
		while (it.hasNext()) {
			op = it.next();
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

	private float multiply(List<? extends CSSExpression> operands, Unit resultUnit) throws DOMException {
		float result = 1f;
		short firstUnit = CSSPrimitiveValue.CSS_NUMBER;
		int unitExp = 0;
		Iterator<? extends CSSExpression> it = operands.iterator();
		while (it.hasNext()) {
			CSSExpression op = it.next();
			ExtendedCSSPrimitiveValue partialValue = evaluateExpression(op, resultUnit);
			float partial = floatValue(partialValue, resultUnit);
			short partialUnit = resultUnit.getUnitType();
			if (partialUnit != CSSPrimitiveValue.CSS_NUMBER) {
				if (firstUnit == CSSPrimitiveValue.CSS_NUMBER) {
					firstUnit = partialUnit;
				} else {
					partial = NumberValue.floatValueConversion(partial, partialUnit, firstUnit);
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

	private ExtendedCSSPrimitiveValue evaluate(ExtendedCSSPrimitiveValue partialValue, Unit resultUnit) {
		short pType = partialValue.getPrimitiveType();
		if (pType == CSSPrimitiveValue2.CSS_FUNCTION) {
			partialValue = evaluateFunction((CSSFunctionValue) partialValue, resultUnit);
		} else if (pType == CSSPrimitiveValue2.CSS_EXPRESSION) {
			AbstractCSSExpression expr = ((ExpressionValue) partialValue).getExpression();
			partialValue = evaluateExpression(expr, resultUnit);
		} else {
			resultUnit.setUnitType(pType);
		}
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
