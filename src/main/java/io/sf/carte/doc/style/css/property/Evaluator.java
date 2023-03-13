/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueList;

/**
 * Expression/Function evaluator.
 * <p>
 * Evaluates <code>calc()</code> expressions as well as functions like
 * <code>min()</code>, <code>clamp()</code> or <code>cos()</code>.
 * <p>
 * To support percentages within <code>calc()</code> expressions, it must be
 * subclassed by something that supports a box model implementation, overriding
 * the {@link #percentage(CSSTypedValue, short)} method.
 */
public class Evaluator {

	private final short preferredUnit;

	/**
	 * Constructs an evaluator with a preferred unit of typographic points ({@code pt}).
	 */
	public Evaluator() {
		this(CSSUnit.CSS_PT);
	}

	/**
	 * Constructs an evaluator with the given preferred unit.
	 * 
	 * @param preferredUnit the preferred unit according to {@link CSSUnit}.
	 */
	public Evaluator(short preferredUnit) {
		super();
		this.preferredUnit = preferredUnit;
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
	public CSSTypedValue evaluateFunction(CSSFunctionValue function) throws DOMException {
		Unit resultUnit = new Unit();
		CSSTypedValue result = evaluateFunction(function, resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
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
	TypedValue evaluateFunction(CSSFunctionValue function, Unit resultUnit) throws DOMException {
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
		} else if ("abs".equalsIgnoreCase(name)) {
			return functionAbs(function.getArguments(), resultUnit);
		} else if ("sign".equalsIgnoreCase(name)) {
			return functionSign(function.getArguments(), resultUnit);
		} else {
			return unknownFunction(function, resultUnit);
		}
	}

	private TypedValue functionMax(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "max() functions take at least one argument");
		}
		Iterator<? extends CSSValue> it = arguments.iterator();
		CSSValue arg = it.next();
		TypedValue typed = enforceTyped(arg);
		float max = floatValue(typed, resultUnit);
		int exp = resultUnit.getExponent();
		short firstUnit = resultUnit.getUnitType();
		short maxUnit = firstUnit;
		float maxInSpecifiedUnit = max;
		while (it.hasNext()) {
			arg = it.next();
			typed = enforceTyped(arg);
			float partial = floatValue(typed, resultUnit);
			if (exp != resultUnit.getExponent()) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"max() arguments have incompatible dimensions.");
			}
			float partialInFirstUnit = NumberValue.floatValueConversion(partial, resultUnit.getUnitType(), firstUnit);
			if (max < partialInFirstUnit) {
				max = partialInFirstUnit;
				maxInSpecifiedUnit = partial;
				maxUnit = resultUnit.getUnitType();
			}
		}
		NumberValue value = NumberValue.createCSSNumberValue(maxUnit, maxInSpecifiedUnit);
		return value;
	}

	private TypedValue enforceTyped(CSSValue arg) throws DOMException {
		if (arg.getCssValueType() != CssType.TYPED) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"Unexpected value: " + arg.getCssText());
		}
		TypedValue typed = (TypedValue) arg;
		if (typed.getPrimitiveType() == Type.IDENT) {
			String s = typed.getStringValue();
			// We may have got Pi or E
			if ("pi".equalsIgnoreCase(s)) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) Math.PI);
			} else if ("e".equalsIgnoreCase(s)) {
				typed = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) Math.E);
			} else {
				throw new DOMException(DOMException.SYNTAX_ERR,
						"Unexpected value: " + arg.getCssText());
			}
		}
		return typed;
	}

	private TypedValue functionMin(CSSValueList<? extends CSSValue> arguments, Unit resultUnit)
			throws DOMException {
		if (arguments.getLength() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"min() functions take at least one argument");
		}
		Iterator<? extends CSSValue> it = arguments.iterator();
		CSSValue arg = it.next();
		TypedValue typed = enforceTyped(arg);
		float min = floatValue(typed, resultUnit);
		int exp = resultUnit.getExponent();
		short firstUnit = resultUnit.getUnitType();
		short minUnit = firstUnit;
		float minInSpecifiedUnit = min;
		while (it.hasNext()) {
			arg = it.next();
			typed = enforceTyped(arg);
			float partial = floatValue(typed, resultUnit);
			if (exp != resultUnit.getExponent()) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"min() arguments have incompatible dimensions.");
			}
			float partialInFirstUnit = NumberValue.floatValueConversion(partial,
					resultUnit.getUnitType(), firstUnit);
			if (min > partialInFirstUnit) {
				min = partialInFirstUnit;
				minInSpecifiedUnit = partial;
				minUnit = resultUnit.getUnitType();
			}
		}
		NumberValue value = NumberValue.createCSSNumberValue(minUnit, minInSpecifiedUnit);
		return value;
	}

	private TypedValue functionClamp(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 3) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Clamp functions take three arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 1);
		float result = floatValue(arg, resultUnit);
		short centralUnit = resultUnit.getUnitType();
		int exp = resultUnit.getExponent();
		arg = typedArgument(arguments, 0);
		float min = floatValue(arg, resultUnit);
		if (exp != resultUnit.getExponent()) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"clamp() arguments have incompatible dimensions.");
		}
		min = NumberValue.floatValueConversion(min, resultUnit.getUnitType(), centralUnit);
		arg = typedArgument(arguments, 2);
		float max = floatValue(arg, resultUnit);
		if (exp != resultUnit.getExponent()) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"clamp() arguments have incompatible dimensions.");
		}
		max = NumberValue.floatValueConversion(max, resultUnit.getUnitType(), centralUnit);
		if (result > max) {
			result = max;
		}
		if (result < min) {
			result = min;
		}
		NumberValue value = NumberValue.createCSSNumberValue(centralUnit, result);
		return value;
	}

	private TypedValue functionSin(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sin() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			int exp = resultUnit.getExponent();
			if (exp > 1 || exp < 0) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Argument unit is not angle nor plain number.");
			}
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSUnit.CSS_RAD);
		}
		float result = (float) Math.sin(fval);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_NUMBER, result);
		return value;
	}

	private TypedValue functionCos(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "cos() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			int exp = resultUnit.getExponent();
			if (exp > 1 || exp < 0) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Argument unit is not angle nor plain number.");
			}
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSUnit.CSS_RAD);
		}
		float result = (float) Math.cos(fval);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_NUMBER, result);
		return value;
	}

	private TypedValue functionTan(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "tan() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			int exp = resultUnit.getExponent();
			if (exp > 1 || exp < 0) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Argument unit is not angle nor plain number.");
			}
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSUnit.CSS_RAD);
		}
		float result = (float) Math.tan(fval);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_NUMBER, result);
		return value;
	}

	private TypedValue functionASin(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.asin(floatValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_RAD, result);
		return value;
	}

	private TypedValue functionACos(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.acos(floatValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_RAD, result);
		return value;
	}

	private TypedValue functionATan(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float f1 = floatValue(arg, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() argument must be dimensionless");
		}
		float result = (float) Math.atan(f1);
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_RAD, result);
		return value;
	}

	private TypedValue functionATan2(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() functions take two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		float f1 = floatValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() arguments must be dimensionless");
		}
		float f2 = floatValue(arg2, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() arguments must be dimensionless");
		}
		float result = (float) Math.atan2(f1, f2);
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_RAD, result);
		return value;
	}

	private TypedValue functionPow(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() functions take two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		resultUnit.setUnitType(arg.getUnitType());
		float base = floatValue(arg, resultUnit);
		Unit expUnit = new Unit();
		float exponent = floatValue(arg2, expUnit);
		if (expUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() exponent cannot have a dimension");
		}
		float result = (float) Math.pow(base, exponent);
		resultUnit.setExponent(resultUnit.getExponent() * Math.round(exponent));
		NumberValue value = NumberValue.createCSSNumberValue(resultUnit.getUnitType(), result);
		return value;
	}

	private TypedValue functionSqrt(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sqrt() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.sqrt(floatValue(arg, resultUnit));
		int exp = resultUnit.getExponent();
		if (exp % 2 != 0) {
			// Odd number
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "invalid CSS unit in sqrt() function");
		}
		resultUnit.setExponent(exp / 2);

		NumberValue value = NumberValue.createCSSNumberValue(resultUnit.getUnitType(), result);
		return value;
	}

	private TypedValue functionHypot(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		int len = arguments.getLength();
		if (len == 2) {
			return functionHypot2(arguments, resultUnit);
		} else if (len == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "hypot() functions need at least one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float partial = floatValue(arg, resultUnit);
		double result = partial * partial;
		short firstUnit = resultUnit.getUnitType();
		for (int i = 1; i < len; i++) {
			arg = typedArgument(arguments, i);
			partial = floatValue(arg, resultUnit);
			if (firstUnit != resultUnit.getUnitType()) {
				partial = NumberValue.floatValueConversion(partial, resultUnit.getUnitType(), firstUnit);
			}
			result += partial * partial;
		}
		result = Math.sqrt(result);
		NumberValue value = NumberValue.createCSSNumberValue(firstUnit, (float) result);
		return value;
	}

	private TypedValue functionHypot2(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		Unit arg2Type = new Unit();
		float f1 = floatValue(arg, resultUnit);
		float f2 = floatValue(arg2, arg2Type);
		if (resultUnit.getUnitType() != arg2Type.getUnitType()) {
			f2 = NumberValue.floatValueConversion(f2, arg2Type.getUnitType(), resultUnit.getUnitType());
		}
		float result = (float) Math.hypot(f1, f2);
		NumberValue value = NumberValue.createCSSNumberValue(resultUnit.getUnitType(), result);
		return value;
	}

	private TypedValue functionAbs(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "abs() functions take one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		float result = Math.abs(fval);
		NumberValue value = NumberValue.createCSSNumberValue(resultUnit.getUnitType(), result);
		return value;
	}

	private TypedValue functionSign(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sign() functions take one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = floatValue(arg, resultUnit);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		float result = Math.signum(fval);
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSUnit.CSS_NUMBER, result);
		return value;
	}

	TypedValue unknownFunction(CSSFunctionValue function, Unit resultUnit) {
		// Do not know how to evaluate, convert arguments to absolute anyway.
		resultUnit.setUnitType(CSSUnit.CSS_INVALID);

		function = function.clone();
		LinkedCSSValueList args = function.getArguments();
		int sz = args.getLength();
		for (int i = 0; i < sz; i++) {
			CSSValue value = args.item(i);
			if (value.getCssValueType() == CssType.TYPED) {
				args.set(i, absoluteValue((CSSPrimitiveValue) value));
			}
		}
		return (TypedValue) function;
	}

	private TypedValue typedArgument(CSSValueList<? extends CSSValue> arguments,
			int index) {
		CSSValue arg = arguments.item(index);
		return enforceTyped(arg);
	}

	private float floatValue(CSSTypedValue value, Unit resultUnit) throws DOMException {
		TypedValue typed = evaluate(value, resultUnit);
		short resultType = resultUnit.getUnitType();
		float result;
		short type = typed.getUnitType();
		if (type == CSSUnit.CSS_NUMBER) {
			result = typed.getFloatValue(CSSUnit.CSS_NUMBER);
		} else if (type != CSSUnit.CSS_PERCENTAGE) {
			result = typed.getFloatValue(resultType);
		} else {
			short preferredUnit = getPreferredUnit();
			result = percentage(typed, preferredUnit);
			resultUnit.setUnitType(preferredUnit);
		}
		return result;
	}

	short getPreferredUnit() {
		return preferredUnit;
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
	public TypedValue evaluateExpression(ExpressionValue calc) throws DOMException {
		Unit resultUnit = new Unit();
		TypedValue result = evaluateExpression(calc.getExpression(), resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
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
	TypedValue evaluateExpression(CSSExpression expression, Unit resultUnit) throws DOMException {
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
		NumberValue value = NumberValue.createCSSNumberValue(resultUnit.getUnitType(), result);
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
		short firstUnit = CSSUnit.CSS_NUMBER;
		int unitExp = 0;
		int len = product.getLength();
		for (int i = 0; i < len; i++) {
			CSSExpression op = product.item(i);
			CSSTypedValue partialValue = evaluateExpression(op, resultUnit);
			float partial = floatValue(partialValue, resultUnit);
			short partialUnit = resultUnit.getUnitType();
			if (partialUnit != CSSUnit.CSS_NUMBER) {
				if (firstUnit == CSSUnit.CSS_NUMBER) {
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
							firstUnit = CSSUnit.CSS_NUMBER;
						}
						result *= partial;
						continue;
					}
					partialUnit = firstUnit;
				}
			}
			if (op.isInverseOperation()) {
				if (partialUnit != CSSUnit.CSS_NUMBER) {
					unitExp--;
					if (unitExp != 0) {
						firstUnit = partialUnit;
					} else {
						firstUnit = CSSUnit.CSS_NUMBER;
					}
				}
				result /= partial;
				if (Float.isNaN(result)) {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Found NaN.");
				}
			} else {
				if (partialUnit != CSSUnit.CSS_NUMBER) {
					unitExp++;
					if (unitExp != 0) {
						firstUnit = partialUnit;
					} else {
						firstUnit = CSSUnit.CSS_NUMBER;
					}
				}
				result *= partial;
			}
		}
		if (unitExp < 0) {
			if (firstUnit == CSSUnit.CSS_HZ) {
				firstUnit = CSSUnit.CSS_S;
				unitExp = -unitExp;
			} else if (firstUnit == CSSUnit.CSS_KHZ) {
				firstUnit = CSSUnit.CSS_MS;
				unitExp = -unitExp;
			} else if (firstUnit == CSSUnit.CSS_S) {
				firstUnit = CSSUnit.CSS_HZ;
				unitExp = -unitExp;
			} else if (firstUnit == CSSUnit.CSS_MS) {
				firstUnit = CSSUnit.CSS_KHZ;
				unitExp = -unitExp;
			}
		}
		resultUnit.setUnitType(firstUnit);
		resultUnit.setExponent(unitExp);
		return result;
	}

	private float unitCancellation(float partial, short partialUnit, short firstUnit, DOMException exception)
			throws DOMException {
		if (partialUnit == CSSUnit.CSS_S || partialUnit == CSSUnit.CSS_MS) {
			if (firstUnit == CSSUnit.CSS_HZ) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSUnit.CSS_S);
			} else if (firstUnit == CSSUnit.CSS_KHZ) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSUnit.CSS_MS);
			}
		} else if (partialUnit == CSSUnit.CSS_HZ || partialUnit == CSSUnit.CSS_KHZ) {
			if (firstUnit == CSSUnit.CSS_S) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSUnit.CSS_HZ);
			} else if (firstUnit == CSSUnit.CSS_MS) {
				return NumberValue.floatValueConversion(partial, partialUnit, CSSUnit.CSS_KHZ);
			}
		}
		throw exception;
	}

	private TypedValue evaluate(CSSPrimitiveValue partialValue, Unit resultUnit) {
		TypedValue typed;
		CSSTypedValue.Type pType = partialValue.getPrimitiveType();
		if (pType == CSSValue.Type.FUNCTION) {
			typed = evaluateFunction((CSSFunctionValue) partialValue, resultUnit);
		} else if (pType == CSSValue.Type.EXPRESSION) {
			CSSExpression expr = ((ExpressionValue) partialValue).getExpression();
			typed = evaluateExpression(expr, resultUnit);
		} else {
			typed = absoluteValue(partialValue);
			resultUnit.setUnitType(typed.getUnitType());
		}
		return typed;
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
	protected TypedValue absoluteValue(CSSPrimitiveValue partialValue) throws DOMException {
		CssType type = partialValue.getCssValueType();
		while (type == CssType.PROXY) {
			CSSValue value = absoluteProxyValue(partialValue);
			if (value == null) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unable to evaluate: " + partialValue.getCssText());
			}
			partialValue = (CSSPrimitiveValue) value;
			type = value.getCssValueType();
		}
		if (type == CssType.TYPED) {
			return absoluteTypedValue((TypedValue) partialValue);
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Unexpected value: " + partialValue.getCssText());
	}

	protected TypedValue absoluteTypedValue(TypedValue partialValue) {
		return partialValue;
	}

	protected CSSValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Unexpected value: " + partialValue.getCssText());
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
	protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unexpected percentage in calc()");
	}

}
