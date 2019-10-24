/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * Base implementation for CSS typed values.
 * 
 */
abstract public class TypedValue extends PrimitiveValue implements CSSTypedValue {

	protected TypedValue(Type unitType) {
		super(unitType);
	}

	@Override
	public CssType getCssValueType() {
		return CssType.TYPED;
	}

	protected TypedValue(TypedValue copied) {
		super(copied);
	}

	@Override
	public void setFloatValue(short unitType, float floatValue) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a <number>.");
	}

	@Override
	public float getFloatValue(short unitType) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a Float");
	}

	@Override
	public void setStringValue(Type stringType, String stringValue) throws DOMException {
		if (stringType != getPrimitiveType()) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Type not supported.");
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Cannot be modified as a String");
	}

	@Override
	public String getStringValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a String");
	}

	@Override
	public RGBAColor getRGBColorValue() throws DOMException {
		if (getPrimitiveType() == Type.IDENT) {
			String ident = getStringValue().toLowerCase(Locale.ROOT);
			String spec;
			if ("transparent".equals(ident)) {
				spec = "rgba(0,0,0,0)";
			} else {
				spec = ColorIdentifiers.getInstance().getColor(ident);
			}
			if (spec != null) {
				ValueFactory factory = new ValueFactory();
				try {
					StyleValue val = factory.parseProperty(spec);
					if (val.getCssValueType() == CssType.TYPED
							&& val.getPrimitiveType() == Type.RGBCOLOR) {
						return ((TypedValue) val).getRGBColorValue();
					}
				} catch (DOMException e) {
				}
			}
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not an RGB Color");
	}

	@Override
	public boolean isCalculatedNumber() {
		return false;
	}

	/**
	 * Is this value a number set to a value of zero, or an absolute value less than 1e-5 ?
	 * 
	 * @return <code>true</code> if this is a number and is set to zero (or equivalently small value).
	 */
	@Override
	public boolean isNumberZero() {
		return false;
	}

	static boolean isCSSIdentifier(PrimitiveValue value, String ident) {
		return value.getPrimitiveType() == Type.IDENT
				&& ident.equalsIgnoreCase(((CSSTypedValue) value).getStringValue());
	}

	/**
	 * Determine whether the given value is or contains a given primitive type.
	 * 
	 * @param value         the value to check.
	 * @param primitiveType the primitive type.
	 * @return <code>true</code> if the given value is or contains an enclosed value
	 *         with that primitive type.
	 */
	static boolean isOrContainsType(StyleValue value, Type primitiveType) {
		CssType type = value.getCssValueType();
		if (value.isPrimitiveValue()) {
			return isOrContainsType((CSSPrimitiveValue) value, primitiveType);
		} else if (type == CssType.LIST) {
			return listContainsType((ValueList) value, primitiveType);
		}
		return false;
	}

	/**
	 * Determine whether the given list contains a value with the given primitive
	 * type.
	 * 
	 * @param list          the list to check.
	 * @param primitiveType the primitive type.
	 * @return <code>true</code> if the list contains a value with that primitive
	 *         type.
	 */
	private static boolean listContainsType(ValueList list, Type primitiveType) {
		for (StyleValue value : list) {
			if (isOrContainsType(value, primitiveType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine whether the given value is or contains a given primitive type.
	 * 
	 * @param value         the value to check.
	 * @param primitiveType the primitive type.
	 * @return <code>true</code> if the given value is or contains an enclosed value
	 *         with that primitive type.
	 */
	static boolean isOrContainsType(CSSPrimitiveValue value, Type primitiveType) {
		Type pType;
		pType = value.getPrimitiveType();
		if (pType == primitiveType) {
			return true;
		} else if (pType == Type.FUNCTION) {
			return functionContainsType((FunctionValue) value, primitiveType);
		} else if (pType == Type.EXPRESSION) {
			return expressionContainsType(((ExpressionValue) value).getExpression(), primitiveType);
		} else if (pType == Type.VAR) {
			LexicalUnit fallback = ((VarValue) value).getFallback();
			return fallback != null && isOrContainsType(fallback, primitiveType);
		}
		return false;
	}

	private static boolean functionContainsType(FunctionValue function, Type primitiveType) {
		LinkedCSSValueList list = function.getArguments();
		for (StyleValue value : list) {
			if (isOrContainsType(value, primitiveType)) {
				return true;
			}
		}
		return false;
	}

	private static boolean expressionContainsType(CSSExpression expr, Type primitiveType) {
		switch (expr.getPartType()) {
		case SUM:
		case PRODUCT:
			AlgebraicExpression ae = (AlgebraicExpression) expr;
			int len = ae.getLength();
			for (int i = 0; i < len; i++) {
				if (expressionContainsType(ae.item(i), primitiveType)) {
					return true;
				}
			}
			break;
		case OPERAND:
			OperandExpression operand = (OperandExpression) expr;
			CSSPrimitiveValue primi = operand.getOperand();
			if (isOrContainsType(primi, primitiveType)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isOrContainsType(LexicalUnit lunit, Type primitiveType) {
		if (primitiveType == Type.ATTR) {
			return hasLexicalUnitType(lunit, LexicalUnit.SAC_ATTR);
		}
		return false;
	}

	private static boolean hasLexicalUnitType(LexicalUnit lunit, short unitType) {
		do {
			if (lunit.getLexicalUnitType() == unitType) {
				return true;
			}
			lunit = lunit.getNextLexicalUnit();
		} while (lunit != null);
		return false;
	}

	/**
	 * Creates and returns a copy of this object.
	 * <p>
	 * The object will be the same except for the <code>subproperty</code> flag,
	 * that will be disabled in the clone object.
	 * </p>
	 * 
	 * @return a copy of this object.
	 */
	@Override
	abstract public TypedValue clone();

}
