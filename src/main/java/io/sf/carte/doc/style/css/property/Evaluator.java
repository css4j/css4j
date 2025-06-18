/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.DOMNotSupportedException;
import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue.MathFunction;
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
 * the {@link #percentage(CSSNumberValue, short)} method.
 */
public class Evaluator {

	private static final FunctionEvaluator[] funcEvaluators = loadFunctionEvaluators();

	private final short preferredUnit;

	/**
	 * Constructs an evaluator with a preferred unit of typographic points ({@code pt}).
	 */
	public Evaluator() {
		this(CSSUnit.CSS_PT);
	}

	private static FunctionEvaluator[] loadFunctionEvaluators() {
		FunctionEvaluator[] evals = new FunctionEvaluator[MathFunction.OTHER.ordinal()];

		evals[MathFunction.ABS.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionAbs(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.CLAMP.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionClamp(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.MAX.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionMax(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.MIN.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionMin(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.ROUND.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionRound(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.MOD.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionMod(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.REM.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionRem(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.HYPOT.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionHypot(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.HYPOT2.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionHypot2(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.LOG.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionLog(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.EXP.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionExp(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.SQRT.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionSqrt(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.POW.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionPow(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.SIGN.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionSign(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.SIN.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionSin(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.COS.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionCos(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.TAN.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionTan(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.ASIN.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionASin(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.ACOS.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionACos(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.ATAN.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionATan(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.ATAN2.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionATan2(function.getArguments(), resultUnit);
			}

		};

		evals[MathFunction.ANCHOR_SIZE.ordinal()] = new FunctionEvaluator() {

			@Override
			public CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
					Unit resultUnit) {
				return eval.functionAnchorSize(function.getArguments(), resultUnit);
			}

		};

		return evals;
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

	interface FunctionEvaluator {

		CSSNumberValue evaluateFunction(Evaluator eval, CSSMathFunctionValue function,
				Unit resultUnit);

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
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Resulting unit is not valid CSS unit.");
		}

		float fv = result.getFloatValue(result.getUnitType());
		if (Float.isNaN(fv)) {
			throw new DOMInvalidAccessException(
					"Result is not a number (NaN).");
		}

		if (function.isExpectingInteger()) {
			result.roundToInteger();
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
		return funcEvaluators[function.getFunctionIndex()].evaluateFunction(this,
				function, resultUnit);
	}

	private CSSNumberValue functionMax(CSSValueList<? extends CSSValue> arguments, Unit resultUnit)
			throws DOMException {
		if (arguments.isEmpty()) {
			throw new DOMSyntaxException("max() functions take at least one argument");
		}

		Iterator<? extends CSSValue> it = arguments.iterator();
		CSSValue arg = it.next();
		CSSTypedValue typed = enforceTyped(arg);
		CSSNumberValue number = evaluate(typed, resultUnit);
		boolean calculated = number.isCalculatedNumber();
		float max = floatValue(number, resultUnit);
		int exp = resultUnit.getExponent();
		short firstUnit = resultUnit.getUnitType();
		short maxUnit = firstUnit;
		float maxInSpecifiedUnit = max;
		while (it.hasNext()) {
			arg = it.next();
			typed = enforceTyped(arg);
			number = evaluate(typed, resultUnit);
			float partial = floatValue(number, resultUnit);
			if (exp != resultUnit.getExponent()) {
				throw new DOMInvalidAccessException(
						"max() arguments have incompatible dimensions.");
			}
			float partialInFirstUnit = NumberValue.floatValueConversion(partial,
					resultUnit.getUnitType(), firstUnit);
			if (max < partialInFirstUnit) {
				max = partialInFirstUnit;
				maxInSpecifiedUnit = partial;
				maxUnit = resultUnit.getUnitType();
				calculated = number.isCalculatedNumber();
			}
		}

		return createNumberValue(maxUnit, maxInSpecifiedUnit, calculated);
	}

	private CSSTypedValue enforceTyped(CSSValue arg) throws DOMException {
		if (arg.getCssValueType() != CssType.TYPED) {
			throw new DOMSyntaxException(
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
				throw new DOMSyntaxException(
						"Unexpected value: " + arg.getCssText());
			}
		}

		return typed;
	}

	private CSSNumberValue functionMin(CSSValueList<? extends CSSValue> arguments, Unit resultUnit)
			throws DOMException {
		if (arguments.isEmpty()) {
			throw new DOMSyntaxException(
					"min() functions take at least one argument");
		}

		Iterator<? extends CSSValue> it = arguments.iterator();
		CSSValue arg = it.next();
		CSSTypedValue typed = enforceTyped(arg);
		CSSNumberValue number = evaluate(typed, resultUnit);
		boolean calculated = number.isCalculatedNumber();
		float min = floatValue(number, resultUnit);
		int exp = resultUnit.getExponent();
		short firstUnit = resultUnit.getUnitType();
		short minUnit = firstUnit;
		float minInSpecifiedUnit = min;
		while (it.hasNext()) {
			arg = it.next();
			typed = enforceTyped(arg);
			number = evaluate(typed, resultUnit);
			float partial = floatValue(number, resultUnit);
			if (exp != resultUnit.getExponent()) {
				throw new DOMInvalidAccessException(
						"min() arguments have incompatible dimensions.");
			}
			float partialInFirstUnit = NumberValue.floatValueConversion(partial,
					resultUnit.getUnitType(), firstUnit);
			if (min > partialInFirstUnit) {
				min = partialInFirstUnit;
				minInSpecifiedUnit = partial;
				minUnit = resultUnit.getUnitType();
				calculated = number.isCalculatedNumber();
			}
		}

		return createNumberValue(minUnit, minInSpecifiedUnit, calculated);
	}

	private CSSNumberValue functionClamp(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 3) {
			throw new DOMSyntaxException("Clamp functions take three arguments");
		}

		CSSTypedValue arg = typedArgument(arguments, 1);
		CSSNumberValue number = evaluate(arg, resultUnit);
		boolean calculated = number.isCalculatedNumber();
		float result = floatValue(number, resultUnit);
		short centralUnit = resultUnit.getUnitType();
		int exp = resultUnit.getExponent();
		CSSTypedValue arg0 = typedArgument(arguments, 0);
		CSSNumberValue number0 = evaluate(arg0, resultUnit);
		float min = floatValue(number0, resultUnit);
		if (exp != resultUnit.getExponent()) {
			throw new DOMInvalidAccessException(
					"clamp() arguments have incompatible dimensions.");
		}
		min = NumberValue.floatValueConversion(min, resultUnit.getUnitType(), centralUnit);
		CSSTypedValue arg2 = typedArgument(arguments, 2);
		CSSNumberValue number2 = evaluate(arg2, resultUnit);
		float max = floatValue(number2, resultUnit);
		if (exp != resultUnit.getExponent()) {
			throw new DOMInvalidAccessException(
					"clamp() arguments have incompatible dimensions.");
		}
		max = NumberValue.floatValueConversion(max, resultUnit.getUnitType(), centralUnit);

		if (result > max) {
			result = max;
			calculated = number2.isCalculatedNumber();
		}
		if (result < min) {
			result = min;
			calculated = number0.isCalculatedNumber();
		}

		return createNumberValue(centralUnit, result, calculated);
	}

	private CSSNumberValue functionRound(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		int len = arguments.getLength();
		if (len > 3 || len < 1) {
			throw new DOMSyntaxException("round() functions take up to three arguments");
		}

		String strategy;
		CSSTypedValue argA, argB = null;

		CSSValue arg0 = arguments.item(0);
		if (arg0.getPrimitiveType() == Type.IDENT) {
			strategy = ((CSSTypedValue) arg0).getStringValue().toLowerCase(Locale.ROOT);
			if (len == 1) {
				throw new DOMSyntaxException("Missing operand in round() function");
			}
			argA = typedArgument(arguments, 1);
			if (len == 3) {
				argB = typedArgument(arguments, 2);
			}
		} else {
			argA = typedArgument(arguments, 0);
			if (len == 2) {
				argB = typedArgument(arguments, 1);
			}
			strategy = "nearest";
		}

		CSSNumberValue numberA = evaluate(argA, resultUnit);
		float a = floatValue(numberA, resultUnit);
		short unit = resultUnit.getUnitType();

		float b;

		if (argB != null) {
			int aexp = resultUnit.getExponent();
			CSSNumberValue numberB = evaluate(argB, resultUnit);
			b = floatValue(numberB, resultUnit);

			if (aexp != resultUnit.getExponent()) {
				throw new DOMInvalidAccessException(
						"round() arguments have incompatible dimensions.");
			}

			a = NumberValue.floatValueConversion(a, unit, resultUnit.getUnitType());
			unit = resultUnit.getUnitType();
		} else {
			b = 1f;
		}

		float result = round(strategy, a, b);

		return createNumberValue(unit, result, true);
	}

	private static float round(String strategy, double a, double b) {
		double result;
		switch (strategy) {
		default:
		case "nearest":
			result = Math.round(a / b) * b;
			break;
		case "up":
			result = Math.ceil(a / b) * b;
			break;
		case "down":
			result = Math.floor(a / b) * b;
			break;
		case "to-zero":
			result = ((long) (a / b)) * b;
			break;
		}
		return (float) result;
	}

	private CSSNumberValue functionMod(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMSyntaxException("mod() functions take two arguments");
		}

		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);

		resultUnit.setUnitType(arg.getUnitType());
		Unit unit2 = new Unit();
		float f1 = evalValue(arg, resultUnit);
		float f2 = evalValue(arg2, unit2);

		if (unit2.getExponent() != resultUnit.getExponent()) {
			throw new DOMSyntaxException(
					"mod() arguments have different units");
		}

		short unit = unit2.getUnitType();

		f1 = NumberValue.floatValueConversion(f1, resultUnit.getUnitType(), unit);
		resultUnit.setUnitType(unit);

		float result = (float) (f1 - (Math.floor(f1 / f2) * f2));

		return createNumberValue(unit, result, true);
	}

	private CSSNumberValue functionRem(CSSValueList<? extends CSSValue> arguments, Unit resultUnit)
			throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMSyntaxException("rem() functions take two arguments");
		}

		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);

		resultUnit.setUnitType(arg.getUnitType());
		Unit unit2 = new Unit();
		float f1 = evalValue(arg, resultUnit);
		float f2 = evalValue(arg2, unit2);

		if (unit2.getExponent() != resultUnit.getExponent()) {
			throw new DOMSyntaxException("rem() arguments have different units");
		}

		short unit = unit2.getUnitType();

		f1 = NumberValue.floatValueConversion(f1, resultUnit.getUnitType(), unit);
		resultUnit.setUnitType(unit);

		float result = f1 - (((int) (f1 / f2)) * f2);

		return createNumberValue(unit, result, true);
	}

	private CSSNumberValue functionSin(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("sin() functions take one argument");
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
			throw new DOMSyntaxException("cos() functions take one argument");
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
			throw new DOMSyntaxException("tan() functions take one argument");
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
			throw new DOMSyntaxException("asin() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.asin(evalValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMSyntaxException("asin() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionACos(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("acos() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.acos(evalValue(arg, resultUnit));
		if (resultUnit.getExponent() != 0) {
			throw new DOMSyntaxException("acos() argument must be dimensionless");
		}
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionATan(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("atan() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float f1 = evalValue(arg, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMSyntaxException("atan() argument must be dimensionless");
		}
		float result = (float) Math.atan(f1);
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionATan2(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMSyntaxException("atan2() functions take two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		float f1 = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			throw new DOMSyntaxException("atan2() arguments must be dimensionless");
		}
		float f2 = evalValue(arg2, resultUnit);
		if (resultUnit.getExponent() != 0) {
			throw new DOMSyntaxException("atan2() arguments must be dimensionless");
		}
		float result = (float) Math.atan2(f1, f2);
		resultUnit.setUnitType(CSSUnit.CSS_RAD);
		resultUnit.setExponent(1);

		return createNumberValue(CSSUnit.CSS_RAD, result, true);
	}

	private CSSNumberValue functionExp(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("exp() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);

		float f1 = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			throw new DOMSyntaxException("exp() argument must be dimensionless");
		}

		float result = (float) Math.exp(f1);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, true);
	}

	private CSSNumberValue functionLog(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		int len = arguments.getLength();
		if (len > 2 || len < 1) {
			throw new DOMSyntaxException("log() functions take one or two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float argument = evalValue(arg, resultUnit);
		if (resultUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
			throw new DOMSyntaxException("log() argument must be dimensionless");
		}

		float result;
		if (len == 2) {
			CSSTypedValue arg2 = typedArgument(arguments, 1);
			Unit expUnit = new Unit();
			float base = evalValue(arg2, expUnit);
			if (expUnit.getUnitType() != CSSUnit.CSS_NUMBER) {
				throw new DOMSyntaxException("log() base cannot have a dimension");
			}
			result = (float) (Math.log(argument) / Math.log(base));
		} else {
			result = (float) Math.log(argument);
		}

		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, true);
	}

	private CSSNumberValue functionPow(CSSValueList<? extends CSSValue> arguments, Unit resultUnit)
			throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMSyntaxException("pow() functions take two arguments");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSTypedValue arg2 = typedArgument(arguments, 1);
		resultUnit.setUnitType(arg.getUnitType());
		float base = evalValue(arg, resultUnit);
		Unit expUnit = new Unit();
		float exponent = evalValue(arg2, expUnit);
		if (expUnit.getExponent() != 0) {
			throw new DOMSyntaxException("pow() exponent cannot have a dimension");
		}
		float result = (float) Math.pow(base, exponent);
		float resultExp = resultUnit.getExponent() * exponent;
		int exp = Math.round(resultExp);
		if (Math.abs(resultExp - exp) > 0.06f) {
			throw new DOMSyntaxException(
					"Result with fractional dimension not supported in pow().");
		}
		resultUnit.setExponent(exp);

		return createNumberValue(resultUnit.getUnitType(), result, true);
	}

	private CSSNumberValue functionSqrt(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("sqrt() functions take one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float result = (float) Math.sqrt(evalValue(arg, resultUnit));
		int exp = resultUnit.getExponent();
		if (exp % 2 != 0) {
			// Odd number
			throw new DOMInvalidAccessException("invalid CSS unit in sqrt() function");
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
			throw new DOMSyntaxException("hypot() functions need at least one argument.");
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
			throw new DOMSyntaxException("abs() functions take one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		CSSNumberValue number = evaluate(arg, resultUnit);
		float fval = floatValue(number, resultUnit);
		float result = Math.abs(fval);

		return createNumberValue(resultUnit.getUnitType(), result, number.isCalculatedNumber());
	}

	private CSSNumberValue functionSign(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("sign() functions take one argument.");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);
		float fval = evalValue(arg, resultUnit);
		resultUnit.setUnitType(CSSUnit.CSS_NUMBER);
		resultUnit.setExponent(0);
		float result = Math.signum(fval);

		return createNumberValue(CSSUnit.CSS_NUMBER, result, false);
	}

	private CSSNumberValue functionAnchorSize(CSSValueList<? extends CSSValue> arguments,
			Unit resultUnit) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMSyntaxException("anchor-size() functions takes one argument");
		}
		CSSTypedValue arg = typedArgument(arguments, 0);

		if (arg.getPrimitiveType() != Type.IDENT) {
			throw new DOMSyntaxException("anchor-size() arguments must be identifiers.");
		}

		resultUnit.setUnitType(CSSUnit.CSS_PX);
		resultUnit.setExponent(1);

		float result = anchorSize(arg.getStringValue());

		return createNumberValue(CSSUnit.CSS_PX, result, true);
	}

	protected float anchorSize(String ident) throws DOMException {
		throw new DOMNotSupportedException("Do not know the anchor-size of: " + ident);
	}

	CSSNumberValue unknownFunction(CSSFunctionValue function, Unit resultUnit) {
		throw new DOMNotSupportedException("Do not know how to compute: " + function.getCssText());
	}

	private CSSTypedValue typedArgument(CSSValueList<? extends CSSValue> arguments,
			int index) {
		CSSValue arg = arguments.item(index);
		return enforceTyped(arg);
	}

	private float evalValue(CSSTypedValue value, Unit resultUnit) throws DOMException {
		CSSNumberValue typed = evaluate(value, resultUnit);
		return floatValue(typed, resultUnit);
	}

	private float floatValue(CSSNumberValue typed, Unit resultUnit) throws DOMException {
		short resultType = resultUnit.getUnitType();
		float result;
		short type = typed.getUnitType();
		if (type == CSSUnit.CSS_NUMBER) {
			result = typed.getFloatValue();
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
	public CSSNumberValue evaluateExpression(CSSExpressionValue calc) throws DOMException {
		Unit resultUnit = new Unit();
		CSSNumberValue result = evaluateExpression(calc.getExpression(), resultUnit);
		int exp = resultUnit.getExponent();
		if (exp > 1 || exp < 0) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Resulting unit is not valid CSS unit.");
		}

		float fv = result.getFloatValue(result.getUnitType());
		if (Float.isNaN(fv)) {
			throw new DOMInvalidAccessException("Result is not a number (NaN).");
		}

		if (calc.isExpectingInteger()) {
			result.roundToInteger();
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
	CSSNumberValue evaluateExpression(CSSExpression expression, Unit resultUnit) throws DOMException {
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
			throw new DOMSyntaxException("Sum without operands.");
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
					throw new DOMInvalidAccessException("Found NaN.");
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

	private CSSNumberValue evaluate(CSSPrimitiveValue partialValue, Unit resultUnit) {
		CSSNumberValue number;
		switch (partialValue.getPrimitiveType()) {
		case MATH_FUNCTION:
			number = evaluateFunction((CSSMathFunctionValue) partialValue, resultUnit);
			break;
		case EXPRESSION:
			CSSExpression expr = ((CSSExpressionValue) partialValue).getExpression();
			number = evaluateExpression(expr, resultUnit);
			break;
		case FUNCTION:
			number = unknownFunction((CSSFunctionValue) partialValue, resultUnit);
			break;
		case IDENT:
			CSSTypedValue typed = replaceParameter(((CSSTypedValue) partialValue).getStringValue());
			if (typed.getPrimitiveType() == Type.NUMERIC) {
				return (CSSNumberValue) typed;
			}
		default:
			number = absoluteValue(partialValue);
			resultUnit.setUnitType(number.getUnitType());
		}
		return number;
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
	protected CSSNumberValue absoluteValue(CSSPrimitiveValue partialValue) throws DOMException {
		while (partialValue.getCssValueType() == CssType.PROXY) {
			CSSValue value = absoluteProxyValue(partialValue);
			if (value == null || !value.isPrimitiveValue()) {
				throw new DOMInvalidAccessException(
						"Unable to evaluate: " + partialValue.getCssText());
			}
			partialValue = (CSSPrimitiveValue) value;
		}
		if (partialValue.getCssValueType() == CssType.TYPED) {
			return absoluteTypedValue((CSSTypedValue) partialValue);
		}
		throw new DOMInvalidAccessException(
				"Unexpected value: " + partialValue.getCssText());
	}

	protected CSSNumberValue absoluteTypedValue(CSSTypedValue partialValue) {
		if (partialValue.getPrimitiveType() == Type.NUMERIC) {
			return (CSSNumberValue) partialValue;
		}
		throw new DOMInvalidAccessException(
				"Unexpected value: " + partialValue.getCssText());
	}

	protected CSSTypedValue replaceParameter(String param) throws DOMException {
		throw new DOMInvalidAccessException(
				"Unexpected parameter: " + param);
	}

	protected CSSValue absoluteProxyValue(CSSPrimitiveValue partialValue) throws DOMException {
		throw new DOMInvalidAccessException(
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
	protected float percentage(CSSNumberValue value, short resultType) throws DOMException {
		throw new DOMNotSupportedException("Unexpected percentage in calc()");
	}

}
