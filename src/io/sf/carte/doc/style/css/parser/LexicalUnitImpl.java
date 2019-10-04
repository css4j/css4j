/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;
import java.util.Objects;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class LexicalUnitImpl implements LexicalUnit {

	private short unitType;

	int intValue = 0;

	float floatValue = 0;

	String dimensionUnitText = "";

	String value = null;

	String identCssText = null;

	LexicalUnitImpl previousLexicalUnit = null;

	LexicalUnitImpl nextLexicalUnit = null;

	LexicalUnitImpl parameters = null;

	LexicalUnitImpl ownerLexicalUnit = null;

	public LexicalUnitImpl(short unitType) {
		super();
		this.unitType = unitType;
	}

	@Override
	public short getLexicalUnitType() {
		return unitType;
	}

	void setUnitType(short unitType) {
		this.unitType = unitType;
	}

	@Override
	public LexicalUnit getNextLexicalUnit() {
		return nextLexicalUnit;
	}

	@Override
	public LexicalUnit getPreviousLexicalUnit() {
		return previousLexicalUnit;
	}

	@Override
	public int getIntegerValue() {
		return intValue;
	}

	@Override
	public float getFloatValue() {
		return floatValue;
	}

	@Override
	public String getDimensionUnitText() {
		return dimensionUnitText;
	}

	@Override
	public String getFunctionName() {
		return value;
	}

	@Override
	public LexicalUnit getParameters() {
		if (unitType != LexicalUnit.SAC_SUB_EXPRESSION) {
			return parameters;
		}
		return null;
	}

	@Override
	public String getStringValue() {
		if (unitType == LexicalUnit.SAC_ATTR) {
			StringBuilder buf = new StringBuilder();
			LexicalUnit lu = this.parameters;
			if (lu != null) {
				buf.append(lu.toString());
			}
			return buf.toString();
		}
		return value;
	}

	@Override
	public LexicalUnit getSubValues() {
		if (unitType == LexicalUnit.SAC_SUB_EXPRESSION || unitType == LexicalUnit.SAC_UNICODERANGE) {
			return parameters;
		} else {
			return null;
		}
	}

	void addFunctionParameter(LexicalUnitImpl paramUnit) {
		paramUnit.ownerLexicalUnit = this;
		if (parameters == null) {
			parameters = paramUnit;
		} else {
			LexicalUnit lu = parameters;
			while (lu.getNextLexicalUnit() != null) {
				lu = lu.getNextLexicalUnit();
			}
			LexicalUnitImpl luimpl = (LexicalUnitImpl) lu;
			luimpl.nextLexicalUnit = paramUnit;
			paramUnit.previousLexicalUnit = luimpl;
		}
	}

	void reset() {
		intValue = 0;
		floatValue = 0;
		dimensionUnitText = "";
		parameters = null;
		nextLexicalUnit = null;
	}

	@Override
	public String getCssText() {
		return currentToString().toString();
	}

	@Override
	public String toString() {
		if (nextLexicalUnit == null) {
			// Save a buffer creation
			return currentToString().toString();
		}
		StringBuilder buf = new StringBuilder();
		LexicalUnitImpl lu = this;
		boolean needSpaces = false;
		while (lu != null) {
			switch(lu.getLexicalUnitType()) {
			case LexicalUnit.SAC_OPERATOR_EXP:
			case LexicalUnit.SAC_OPERATOR_GE:
			case LexicalUnit.SAC_OPERATOR_GT:
			case LexicalUnit.SAC_OPERATOR_LE:
			case LexicalUnit.SAC_OPERATOR_LT:
			case LexicalUnit.SAC_OPERATOR_MULTIPLY:
			case LexicalUnit.SAC_OPERATOR_SLASH:
			case LexicalUnit.SAC_OPERATOR_TILDE:
			case LexicalUnit.SAC_LEFT_BRACKET:
				needSpaces = false;
			case LexicalUnit.SAC_OPERATOR_COMMA:
				break;
			case LexicalUnit.SAC_RIGHT_BRACKET:
				needSpaces = true;
				break;
			default:
				if (needSpaces) {
					buf.append(' ');
				} else {
					needSpaces = true;
				}
			}
			buf.append(lu.currentToString());
			lu = lu.nextLexicalUnit;
		}
		return buf.toString();
	}

	CharSequence currentToString() {
		switch (unitType) {
		case LexicalUnit.SAC_INTEGER:
			return Integer.toString(intValue);
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_REAL:
		case LexicalUnit.SAC_CENTIMETER:
		case LexicalUnit.SAC_DEGREE:
		case LexicalUnit.SAC_DIMENSION:
		case LexicalUnit.SAC_GRADIAN:
		case LexicalUnit.SAC_HERTZ:
		case LexicalUnit.SAC_INCH:
		case LexicalUnit.SAC_KILOHERTZ:
		case LexicalUnit.SAC_MILLIMETER:
		case LexicalUnit.SAC_MILLISECOND:
		case LexicalUnit.SAC_RADIAN:
		case LexicalUnit.SAC_SECOND:
		case LexicalUnit.SAC_CAP:
		case LexicalUnit.SAC_CH:
		case LexicalUnit.SAC_DOTS_PER_CENTIMETER:
		case LexicalUnit.SAC_DOTS_PER_INCH:
		case LexicalUnit.SAC_DOTS_PER_PIXEL:
		case LexicalUnit.SAC_IC:
		case LexicalUnit.SAC_LH:
		case LexicalUnit.SAC_QUARTER_MILLIMETER:
		case LexicalUnit.SAC_REM:
		case LexicalUnit.SAC_RLH:
		case LexicalUnit.SAC_TURN:
		case LexicalUnit.SAC_VB:
		case LexicalUnit.SAC_VH:
		case LexicalUnit.SAC_VI:
		case LexicalUnit.SAC_VMAX:
		case LexicalUnit.SAC_VMIN:
		case LexicalUnit.SAC_VW:
		case LexicalUnit.SAC_FR:
			StringBuilder buf = new StringBuilder();
			if(floatValue % 1 != 0) {
			    buf.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				buf.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			if (dimensionUnitText != null) {
				buf.append(dimensionUnitText);
			}
			return buf.toString();
		case LexicalUnit.SAC_RGBCOLOR:
			if (identCssText != null) {
				return identCssText;
			}
		case LexicalUnit.SAC_FUNCTION:
		case LexicalUnit.SAC_RECT_FUNCTION:
		case LexicalUnit.SAC_ATTR:
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
			buf = new StringBuilder();
			buf.append(value).append('(');
			LexicalUnit lu = this.parameters;
			if (lu != null) {
				buf.append(lu.toString());
			}
			buf.append(')');
			return buf.toString();
		case LexicalUnit.SAC_SUB_EXPRESSION:
			buf = new StringBuilder();
			buf.append('(');
			lu = this.parameters;
			if (lu != null) {
				buf.append(lu.toString());
			}
			buf.append(')');
			return buf.toString();
		case LexicalUnit.SAC_IDENT:
			return identCssText != null ? identCssText : value;
		case LexicalUnit.SAC_STRING_VALUE:
			return identCssText;
		case LexicalUnit.SAC_URI:
			String quri;
			if (identCssText != null) {
				quri = identCssText;
			} else {
				quri = ParseHelper.quote(value, '\'');
			}
			return "url(" + quri + ")";
		case LexicalUnit.SAC_ELEMENT_REFERENCE:
			if (value == null) {
				return "element(#)";
			}
			int len = value.length();
			buf = new StringBuilder(len + 10);
			buf.append("element(#").append(value).append(')');
			return buf.toString();
		case LexicalUnit.SAC_UNICODERANGE:
			buf = new StringBuilder();
			lu = this.parameters;
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_INTEGER) {
					buf.append("U+").append(Integer.toHexString(lu.getIntegerValue()));
				} else {
					buf.append("U+").append(lu.getStringValue());
				}
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					buf.append('-');
					if (lu.getLexicalUnitType() == LexicalUnit.SAC_INTEGER) {
						buf.append(Integer.toHexString(lu.getIntegerValue()));
					} else {
						buf.append(lu.getStringValue());
					}
				}
			}
			return buf.toString();
		case LexicalUnit.SAC_UNICODE_WILDCARD:
			return getStringValue();
		case LexicalUnit.SAC_OPERATOR_COMMA:
			return ",";
		case LexicalUnit.SAC_OPERATOR_EXP:
			return "^";
		case LexicalUnit.SAC_OPERATOR_GE:
			return ">=";
		case LexicalUnit.SAC_OPERATOR_GT:
			return ">";
		case LexicalUnit.SAC_OPERATOR_LE:
			return "<=";
		case LexicalUnit.SAC_OPERATOR_LT:
			return "<";
		case LexicalUnit.SAC_OPERATOR_MINUS:
			return "-";
		case LexicalUnit.SAC_OPERATOR_MOD:
			return "%";
		case LexicalUnit.SAC_OPERATOR_MULTIPLY:
			return "*";
		case LexicalUnit.SAC_OPERATOR_PLUS:
			return "+";
		case LexicalUnit.SAC_OPERATOR_SLASH:
			return "/";
		case LexicalUnit.SAC_OPERATOR_TILDE:
			return "~";
		case LexicalUnit.SAC_LEFT_BRACKET:
			return "[";
		case LexicalUnit.SAC_RIGHT_BRACKET:
			return "]";
		case LexicalUnit.SAC_COMPAT_IDENT:
		case LexicalUnit.SAC_COMPAT_PRIO:
			return ParseHelper.escapeControl(value);
		}
		return "";
	}


	@Override
	public int hashCode() {
		return Objects.hash(dimensionUnitText, floatValue, identCssText, intValue, parameters, unitType, value);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LexicalUnitImpl other = (LexicalUnitImpl) obj;
		return Objects.equals(dimensionUnitText, other.dimensionUnitText)
				&& Float.floatToIntBits(floatValue) == Float.floatToIntBits(other.floatValue)
				&& Objects.equals(identCssText, other.identCssText) && intValue == other.intValue
				&& Objects.equals(ownerLexicalUnit, other.ownerLexicalUnit)
				&& Objects.equals(parameters, other.parameters) && unitType == other.unitType
				&& Objects.equals(value, other.value);
	}

}
