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
 * To support percentages within <code>calc()</code> expressions, must be
 * subclassed by something that supports a box model implementation.
 */
public class Evaluator {

	public Evaluator() {
		super();
	}

	/**
	 * Evaluate the given function.
	 * 
	 * @param function   the function to evaluate.
	 * @param resultType the desired result primitive unit type.
	 * @return the result of evaluating the function, or the function itself if this
	 *         class does not know how to evaluate it.
	 * @throws DOMException if a problem was found evaluating the function.
	 */
	public ExtendedCSSPrimitiveValue evaluateFunction(CSSFunctionValue function, short resultType) throws DOMException {
		String name = function.getFunctionName();
		if ("max".equalsIgnoreCase(name)) {
			return functionMax(function.getArguments(), resultType);
		} else if ("min".equalsIgnoreCase(name)) {
			return functionMin(function.getArguments(), resultType);
		} else if ("clamp".equalsIgnoreCase(name)) {
			return functionClamp(function.getArguments(), resultType);
		} else if ("sin".equalsIgnoreCase(name)) {
			return functionSin(function.getArguments(), resultType);
		} else if ("cos".equalsIgnoreCase(name)) {
			return functionCos(function.getArguments(), resultType);
		} else if ("tan".equalsIgnoreCase(name)) {
			return functionTan(function.getArguments(), resultType);
		} else if ("asin".equalsIgnoreCase(name)) {
			return functionASin(function.getArguments(), resultType);
		} else if ("acos".equalsIgnoreCase(name)) {
			return functionACos(function.getArguments(), resultType);
		} else if ("atan".equalsIgnoreCase(name)) {
			return functionATan(function.getArguments(), resultType);
		} else if ("atan2".equalsIgnoreCase(name)) {
			return functionATan2(function.getArguments(), resultType);
		} else if ("pow".equalsIgnoreCase(name)) {
			return functionPow(function.getArguments(), resultType);
		} else if ("sqrt".equalsIgnoreCase(name)) {
			return functionSqrt(function.getArguments(), resultType);
		} else {
			return function;
		}
	}

	private ExtendedCSSPrimitiveValue functionMax(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		float result = Float.MIN_VALUE;
		Iterator<? extends ExtendedCSSValue> it = arguments.iterator();
		while (it.hasNext()) {
			ExtendedCSSValue arg = it.next();
			if (arg.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected value: " + arg.getCssText());
			}
			float partial = floatValue((ExtendedCSSPrimitiveValue) arg, resultType);
			if (result < partial) {
				result = partial;
			}
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionMin(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		float result = Float.MAX_VALUE;
		Iterator<? extends ExtendedCSSValue> it = arguments.iterator();
		while (it.hasNext()) {
			ExtendedCSSValue arg = it.next();
			if (arg.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected value: " + arg.getCssText());
			}
			float partial = floatValue((ExtendedCSSPrimitiveValue) arg, resultType);
			if (result > partial) {
				result = partial;
			}
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionClamp(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 3) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Clamp functions take three arguments");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float min = floatValue(arg, resultType);
		arg = primitiveArgument(arguments, 1);
		float result = floatValue(arg, resultType);
		arg = primitiveArgument(arguments, 2);
		float max = floatValue(arg, resultType);
		if (result > max) {
			result = max;
		}
		if (result < min) {
			result = min;
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionSin(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sin() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.sin(floatValue(arg, CSSPrimitiveValue.CSS_RAD));
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionCos(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "cos() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.cos(floatValue(arg, CSSPrimitiveValue.CSS_RAD));
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionTan(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "tan() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.tan(floatValue(arg, CSSPrimitiveValue.CSS_RAD));
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionASin(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "asin() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.asin(floatValue(arg, CSSPrimitiveValue.CSS_NUMBER));
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionACos(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "acos() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.acos(floatValue(arg, CSSPrimitiveValue.CSS_NUMBER));
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionATan(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.atan(floatValue(arg, CSSPrimitiveValue.CSS_NUMBER));
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionATan2(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "atan2() functions take two arguments");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		ExtendedCSSPrimitiveValue arg2 = primitiveArgument(arguments, 1);
		float result = (float) Math.atan2(floatValue(arg, CSSPrimitiveValue.CSS_NUMBER),
				floatValue(arg2, CSSPrimitiveValue.CSS_NUMBER));
		NumberValue value = new NumberValue();
		value.setFloatValue(CSSPrimitiveValue.CSS_RAD, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionPow(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 2) {
			throw new DOMException(DOMException.SYNTAX_ERR, "pow() functions take two arguments");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		ExtendedCSSPrimitiveValue arg2 = primitiveArgument(arguments, 1);
		float result = (float) Math.pow(floatValue(arg, resultType), floatValue(arg2, resultType));
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue functionSqrt(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			short resultType) throws DOMException {
		if (arguments.getLength() != 1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "sqrt() functions take one argument");
		}
		ExtendedCSSPrimitiveValue arg = primitiveArgument(arguments, 0);
		float result = (float) Math.sqrt(floatValue(arg, resultType));
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private ExtendedCSSPrimitiveValue primitiveArgument(ExtendedCSSValueList<? extends ExtendedCSSValue> arguments,
			int index) {
		ExtendedCSSValue arg = arguments.item(0);
		if (arg.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected value: " + arg.getCssText());
		}
		return (ExtendedCSSPrimitiveValue) arg;
	}

	private float floatValue(ExtendedCSSPrimitiveValue arg, short resultType) throws DOMException {
		float result;
		try {
			result = arg.getFloatValue(resultType);
		} catch (DOMException e) {
			short pType = arg.getPrimitiveType();
			if (pType == CSSPrimitiveValue2.CSS_FUNCTION) {
				result = evaluateFunction((CSSFunctionValue) arg, resultType).getFloatValue(resultType);
			} else if (pType == CSSPrimitiveValue2.CSS_EXPRESSION) {
				result = evaluateExpression(((ExpressionContainerValue) arg).getExpression(), resultType)
						.getFloatValue(resultType);
			} else {
				result = evaluate(arg, resultType);
			}
		}
		return result;
	}

	/**
	 * Evaluate the given expression.
	 * 
	 * @param expression the expression to evaluate.
	 * @param resultType the desired result primitive unit type.
	 * @return the result from evaluating the expression.
	 * @throws DOMException if a problem was found evaluating the expression.
	 */
	public ExtendedCSSPrimitiveValue evaluateExpression(CSSExpression expression, short resultType)
			throws DOMException {
		float result;
		switch (expression.getPartType()) {
		case SUM:
			AlgebraicExpression sum = (CSSExpression.AlgebraicExpression) expression;
			result = sum(sum.getOperands(), resultType);
			if (sum.isInverseOperation()) {
				result = -result;
			}
			break;
		case PRODUCT:
			AlgebraicExpression prod = (CSSExpression.AlgebraicExpression) expression;
			result = multiply(prod.getOperands(), resultType);
			if (prod.isInverseOperation()) {
				result = 1f / result;
			}
			break;
		default:
			return ((CSSExpression.CSSOperandExpression) expression).getOperand();
		}
		NumberValue value = new NumberValue();
		value.setFloatValue(resultType, result);
		return value;
	}

	private float sum(List<? extends CSSExpression> operands, short resultType) throws DOMException {
		float result = 0f;
		Iterator<? extends CSSExpression> it = operands.iterator();
		while (it.hasNext()) {
			CSSExpression op = it.next();
			float partial = evaluate(evaluateExpression(op, resultType), resultType);
			if (op.isInverseOperation()) {
				result -= partial;
			} else {
				result += partial;
			}
		}
		return result;
	}

	private float multiply(List<? extends CSSExpression> operands, short resultType) throws DOMException {
		float result = 1f;
		Iterator<? extends CSSExpression> it = operands.iterator();
		while (it.hasNext()) {
			CSSExpression op = it.next();
			float partial = evaluate(evaluateExpression(op, resultType), resultType);
			if (op.isInverseOperation()) {
				result /= partial;
			} else {
				result *= partial;
			}
		}
		return result;
	}

	protected float evaluate(ExtendedCSSPrimitiveValue value, short resultType) throws DOMException {
		float result;
		short type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_PERCENTAGE) {
			result = value.getFloatValue(resultType);
		} else {
			result = percentage(value, resultType);
		}
		return result;
	}

	protected float percentage(ExtendedCSSPrimitiveValue value, short resultType) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Unexpected percentage in calc()");
	}

}
