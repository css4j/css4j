/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSNumberValue;
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
	 * Evaluate the given mathematical function.
	 * <p>
	 * This method checks the result unit, assuming that the unit must match that of
	 * the returned primitive value (some functions may return values where the
	 * units are raised to a power greater than one, or lesser than zero).
	 * 
	 * @param function the mathematical function to evaluate.
	 * @return the result of evaluating the function, or the function itself if this
	 *         class does not know how to evaluate it.
	 * @throws DOMException if a problem was found evaluating the function, or the
	 *                      resulting unit is not a valid CSS unit.
	 */
	public CSSNumberValue evaluateFunction(CSSMathFunctionValue function) throws DOMException {
		Unit resultUnit = new Unit();
		CSSNumberValue result = evaluateFunction(function, resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
		}

		float fv = result.getFloatValue(result.getUnitType());
		if (Float.isNaN(fv)) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Result is not a number (NaN).");
		}

		if (function.isExpectingInteger()) {
			((CSSNumberValue) result).roundToInteger();
		}

		return result;
	}

	/**
	 * Evaluate the given mathematical function.
	 * 
	 * @param function   the mathematical function to evaluate.
	 * @param resultUnit the result unit(s).
	 * @return the result of evaluating the function, or the function itself if this
	 *         class does not know how to evaluate it.
	 * @throws DOMException if a problem was found evaluating the function.
	 */
	CSSNumberValue evaluateFunction(CSSMathFunctionValue function, Unit resultUnit)
			throws DOMException {
		CSSNumberValue typed;
		switch (function.getFunction()) {
		case MAX:
			typed = functionMax(function.getArguments(), resultUnit);
			break;
		case MIN:
			typed = functionMin(function.getArguments(), resultUnit);
			break;
		case CLAMP:
			typed = functionClamp(function.getArguments(), resultUnit);
			break;
		case SIN:
			typed = functionSin(function.getArguments(), resultUnit);
			break;
		case COS:
			typed = functionCos(function.getArguments(), resultUnit);
			break;
		case TAN:
			typed = functionTan(function.getArguments(), resultUnit);
			break;
		case ASIN:
			typed = functionASin(function.getArguments(), resultUnit);
			break;
		case ACOS:
			typed = functionACos(function.getArguments(), resultUnit);
			break;
		case ATAN:
			typed = functionATan(function.getArguments(), resultUnit);
			break;
		case ATAN2:
			typed = functionATan2(function.getArguments(), resultUnit);
			break;
		case POW:
			typed = functionPow(function.getArguments(), resultUnit);
			break;
		case SQRT:
			typed = functionSqrt(function.getArguments(), resultUnit);
			break;
		case HYPOT:
			typed = functionHypot(function.getArguments(), resultUnit);
			break;
		case ABS:
			typed = functionAbs(function.getArguments(), resultUnit);
			break;
		case SIGN:
			typed = functionSign(function.getArguments(), resultUnit);
			break;
		default:
			// Perhaps the caller was compiled against a later version
			// of this library
			throw new IllegalStateException("Unknown math function: " + function.getFunctionName());
		}
		return typed;
	}

	private CSSNumberValue functionMax(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.isEmpty()) {
			throw new DOMException(DOMException.SYNTAX_ERR, "max() functions take at least one argument");
		}

		Iterator<? extends CSSValue> it = arguments.iterator();
		CSSValue arg = it.next();
		CSSTypedValue typed = enforceTyped(arg);
		typed = evaluate(typed, resultUnit);
		boolean calculated = typed.isCalculatedNumber();
		float max = floatValue(typed, resultUnit);
		int exp = resultUnit.getExponent();
		short firstUnit = resultUnit.getUnitType();
		short maxUnit = firstUnit;
		float maxInSpecifiedUnit = max;
		while (it.hasNext()) {
			arg = it.next();
			typed = enforceTyped(arg);
			typed = evaluate(typed, resultUnit);
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
				calculated = typed.isCalculatedNumber();
			}
		}

		return createNumberValue(maxUnit, maxInSpecifiedUnit, calculated);
	}

	private CSSTypedValue enforceTyped(CSSValue arg) throws DOMException {
		if (arg.getCssValueType() != CssType.TYPED) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"Unexpected value: " + arg.getCssText());
		}

		CSSTypedValue typed = (CSSTypedValue) arg;
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

	private CSSNumberValue functionMin(CSSValueList<? extends CSSValue> arguments, Unit resultUnit)
			throws DOMException {
		if (arguments.isEmpty()) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"min() functions take at least one argument");
		}

		Iterator<? extends CSSValue> it = arguments.iterator();
		CSSValue arg = it.next();
		CSSTypedValue typed = enforceTyped(arg);
		typed = evaluate(typed, resultUnit);
		boolean calculated = typed.isCalculatedNumber();
		float min = floatValue(typed, resultUnit);
		int exp = resultUnit.getExponent();
		short firstUnit = resultUnit.getUnitType();
		short minUnit = firstUnit;
		float minInSpecifiedUnit = min;
		while (it.hasNext()) {
			arg = it.next();
			typed = enforceTyped(arg);
			typed = evaluate(typed, resultUnit);
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
				calculated = typed.isCalculatedNumber();
			}
		}

		return createNumberValue(minUnit, minInSpecifiedUnit, calculated);
	}

	private CSSNumberValue functionClamp(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 3) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Clamp functions take three arguments");
		}

		CSSTypedValue arg = typedArgument(arguments, 1);
		arg = evaluate(arg, resultUnit);
		boolean calculated = arg.isCalculatedNumber();
		float result = floatValue(arg, resultUnit);
		short centralUnit = resultUnit.getUnitType();
		int exp = resultUnit.getExponent();
		CSSTypedValue arg0 = typedArgument(arguments, 0);
		arg0 = evaluate(arg0, resultUnit);
		float min = floatValue(arg0, resultUnit);
		if (exp != resultUnit.getExponent()) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"clamp() arguments have incompatible dimensions.");
		}
		min = NumberValue.floatValueConversion(min, resultUnit.getUnitType(), centralUnit);
		CSSTypedValue arg2 = typedArgument(arguments, 2);
		arg2 = evaluate(arg2, resultUnit);
		float max = floatValue(arg2, resultUnit);
		if (exp != resultUnit.getExponent()) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"clamp() arguments have incompatible dimensions.");
		}
		max = NumberValue.floatValueConversion(max, resultUnit.getUnitType(), centralUnit);

		if (result > max) {
			result = max;
			calculated = arg2.isCalculatedNumber();
		}
		if (result < min) {
			result = min;
			calculated = arg0.isCalculatedNumber();
		}

		return createNumberValue(centralUnit, result, calculated);
	}

	private CSSNumberValue functionSin(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sin() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			int exp = resultUnit.getExponent();
			if (exp > 1 || exp < 0) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Argument unit is not angle nor plain number.");
			}
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSUnit.CSS_RAD);
		}
		float result = (float) Math.sin(fval);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		resultUnit.setExponent(0);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, true);
	}

	private CSSNumberValue functionCos(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "cos() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			int exp = resultUnit.getExponent();
			if (exp > 1 || exp < 0) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Argument unit is not angle nor plain number.");
			}
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSUnit.CSS_RAD);
		}
		float result = (float) Math.cos(fval);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		resultUnit.setExponent(0);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, true);
	}

	private CSSNumberValue functionTan(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "tan() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			int exp = resultUnit.getExponent();
			if (exp > 1 || exp < 0) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Argument unit is not angle nor plain number.");
			}
			fval = NumberValue.floatValueConversion(fval, resultUnit.getUnitType(), CSSUnit.CSS_RAD);
		}
		float result = (float) Math.tan(fval);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		resultUnit.setExponent(0);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, true);
	}

	private CSSNumberValue functionASin(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.asin(evalValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionACos(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.acos(evalValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionATan(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float f1 = evalValue(arg, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() argument must be dimensionless");
		}
		float result = (float) Math.atan(f1);
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionATan2(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() functions take two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		float f1 = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() arguments must be dimensionless");
		}
		float f2 = evalValue(arg2, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() arguments must be dimensionless");
		}
		float result = (float) Math.atan2(f1, f2);
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionPow(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() functions take two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		resultUnit.setUnitType(arg.getUnitType());
		float base = evalValue(arg, resultUnit);
		Unit expUnit = new Unit();
		float exponent = evalValue(arg2, expUnit);
		if (expUnit.getExponent() != 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() exponent cannot have a dimension");
		}
		float result = (float) Math.pow(base, exponent);
		resultUnit.setExponent(resultUnit.getExponent() * Math.round(exponent));

		return createNumberValue(resultUnit.getUnitType(), result, true);
	}

	private CSSNumberValue functionSqrt(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sqrt() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.sqrt(evalValue(arg, resultUnit));
		int exp = resultUnit.getExponent();
		if (exp % 2 != 0) {
			// Odd number
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "invalid CSS unit in sqrt() function");
		}
		resultUnit.setExponent(exp / 2);

		return createNumberValue(resultUnit.getUnitType(), result, true);
	}

	private CSSNumberValue functionHypot(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		int len = arguments.getLength();
		if (len == 2) {
			return functionHypot2(arguments, resultUnit);
		} else if (len == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "hypot() functions need at least one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float partial = evalValue(arg, resultUnit);
		double result = partial * partial;
		short firstUnit = resultUnit.getUnitType();
		for (int i = 1; i < len; i++) {
			arg = typedArgument(arguments, i);
			partial = evalValue(arg, resultUnit);
			if (firstUnit != resultUnit.getUnitType()) {
				partial = NumberValue.floatValueConversion(partial, resultUnit.getUnitType(), firstUnit);
			}
			result += partial * partial;
		}
		result = Math.sqrt(result);

		return createNumberValue(firstUnit, (float) result, true);
	}

	private CSSNumberValue functionHypot2(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		Unit arg2Type = new Unit();
		float f1 = evalValue(arg, resultUnit);
		float f2 = evalValue(arg2, arg2Type);
		if (resultUnit.getUnitType() != arg2Type.getUnitType()) {
			f2 = NumberValue.floatValueConversion(f2, arg2Type.getUnitType(), resultUnit.getUnitType());
		}
		float result = (float) Math.hypot(f1, f2);

		return createNumberValue(resultUnit.getUnitType(), result, true);
	}

	private CSSNumberValue functionAbs(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "abs() functions take one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue typed = evaluate(arg, resultUnit);
		float fval = floatValue(typed, resultUnit);
		float result = Math.abs(fval);

		return createNumberValue(resultUnit.getUnitType(), result, typed.isCalculatedNumber());
	}

	private CSSNumberValue functionSign(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sign() functions take one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = evalValue(arg, resultUnit);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		resultUnit.setExponent(0);
		float result = Math.signum(fval);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, false);
	}

	CSSTypedValue unknownFunction(CSSFunctionValue function, Unit resultUnit) {
		// Do not know how to evaluate
		resultUnit.setUnitType(CSSUnit.CSS_INVALID);
		return function;
	}

	private CSSTypedValue typedArgument(CSSValueList<? extends CSSValue> arguments,
			int index) {
		CSSValue arg = arguments.item(index);
		return enforceTyped(arg);
	}

	private float evalValue(CSSTypedValue value, Unit resultUnit) throws DOMException {
		CSSTypedValue typed = evaluate(value, resultUnit);
		return floatValue(typed, resultUnit);
	}

	private float floatValue(CSSTypedValue typed, Unit resultUnit) throws DOMException {
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

	/**
	 * Create a {@code CSSNumberValue} in the desired implementation.
	 * 
	 * @param unit                 the unit.
	 * @param valueInSpecifiedUnit the value in the given unit.
	 * @param calculated           whether the value was calculated. Implementations
	 *                             may ignore this parameter.
	 * @return the number value.
	 */
	protected CSSNumberValue createNumberValue(short unit, float valueInSpecifiedUnit, boolean calculated) {
		NumberValue value = NumberValue.createCSSNumberValue(unit, valueInSpecifiedUnit);
		value.setCalculatedNumber(calculated);
		return value;
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
	public CSSTypedValue evaluateExpression(CSSExpressionValue calc) throws DOMException {
		Unit resultUnit = new Unit();
		CSSTypedValue result = evaluateExpression(calc.getExpression(), resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
		}

		float fv = result.getFloatValue(result.getUnitType());
		if (Float.isNaN(fv)) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Result is not a number (NaN).");
		}

		if (calc.isExpectingInteger()) {
			((CSSNumberValue) result).roundToInteger();
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
	CSSTypedValue evaluateExpression(CSSExpression expression, Unit resultUnit) throws DOMException {
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
		float result = evalValue(evaluateExpression(op, resultUnit), resultUnit);
		if (op.isInverseOperation()) {
			result = -result;
		}
		short firstUnit = resultUnit.getUnitType();
		for (int i = 1; i < len; i++) {
			op = sumop.item(i);
			float partial = evalValue(evaluateExpression(op, resultUnit), resultUnit);
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
			float partial = evalValue(partialValue, resultUnit);
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

	private CSSTypedValue evaluate(CSSPrimitiveValue partialValue, Unit resultUnit) {
		CSSTypedValue typed;
		switch (partialValue.getPrimitiveType()) {
		case MATH_FUNCTION:
			typed = evaluateFunction((CSSMathFunctionValue) partialValue, resultUnit);
			break;
		case EXPRESSION:
			CSSExpression expr = ((CSSExpressionValue) partialValue).getExpression();
			typed = evaluateExpression(expr, resultUnit);
			break;
		case FUNCTION:
			typed = unknownFunction((CSSFunctionValue) partialValue, resultUnit);
			break;
		default:
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
	protected CSSTypedValue absoluteValue(CSSPrimitiveValue partialValue) throws DOMException {
		while (partialValue.getCssValueType() == CssType.PROXY) {
			CSSValue value = absoluteProxyValue(partialValue);
			if (value == null || !value.isPrimitiveValue()) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unable to evaluate: " + partialValue.getCssText());
			}
			partialValue = (CSSPrimitiveValue) value;
		}
		if (partialValue.getCssValueType() == CssType.TYPED) {
			return absoluteTypedValue((CSSTypedValue) partialValue);
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,
				"Unexpected value: " + partialValue.getCssText());
	}

	protected CSSTypedValue absoluteTypedValue(CSSTypedValue partialValue) {
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
