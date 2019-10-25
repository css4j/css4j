/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class LexicalUnitImpl implements LexicalUnit {

	private short unitType;

	private short cssUnit = CSSUnit.CSS_INVALID;

	int intValue = 0;

	float floatValue = Float.NaN;

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
	public short getCssUnit() {
		return cssUnit;
	}

	void setCssUnit(short cssUnit) {
		this.cssUnit = cssUnit;
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
	public void insertNextLexicalUnit(LexicalUnit nextUnit) throws CSSException {
		LexicalUnitImpl nlu = (LexicalUnitImpl) nextUnit;
		if (nlu.getPreviousLexicalUnit() != null) {
			throw new CSSException("Parameter unit has a previous unit.");
		}
		nlu.previousLexicalUnit = this;
		nlu.ownerLexicalUnit = ownerLexicalUnit;
		LexicalUnitImpl lu = nlu;
		LexicalUnitImpl lastlu;
		do {
			lastlu = lu;
			lu = lu.nextLexicalUnit;
		} while (lu != null);
		lastlu.nextLexicalUnit = nextLexicalUnit;
		if (nextLexicalUnit != null) {
			nextLexicalUnit.previousLexicalUnit = lastlu;
		}
		nextLexicalUnit = nlu;
	}

	@Override
	public LexicalUnit replaceBy(LexicalUnit replacementUnit) {
		LexicalUnitImpl rlu = (LexicalUnitImpl) replacementUnit;
		if (previousLexicalUnit != null) {
			previousLexicalUnit.nextLexicalUnit = rlu;
			rlu.previousLexicalUnit = previousLexicalUnit;
		}
		if (nextLexicalUnit != null) {
			LexicalUnitImpl lu = rlu;
			LexicalUnitImpl lastlu;
			do {
				lastlu = lu;
				lu = lu.nextLexicalUnit;
			} while (lu != null);
			nextLexicalUnit.previousLexicalUnit = lastlu;
			lastlu.nextLexicalUnit = nextLexicalUnit;
			nextLexicalUnit = null;
		}
		// Set the owner
		if (ownerLexicalUnit != null) {
			LexicalUnitImpl lu = rlu;
			do {
				lu.ownerLexicalUnit = ownerLexicalUnit;
				lu = lu.nextLexicalUnit;
			} while (lu != null);
			if (previousLexicalUnit == null) {
				ownerLexicalUnit.parameters = rlu;
			}
			ownerLexicalUnit = null;
		}
		previousLexicalUnit = null;
		return replacementUnit;
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
	public boolean isParameter() {
		return ownerLexicalUnit != null;
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
		case LexicalUnit.SAC_REAL:
		case LexicalUnit.SAC_DIMENSION:
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
		case LexicalUnit.SAC_VAR:
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
		case LexicalUnit.SAC_INHERIT:
			return "inherit";
		case LexicalUnit.SAC_INITIAL:
			return "initial";
		case LexicalUnit.SAC_UNSET:
			return "unset";
		case LexicalUnit.SAC_REVERT:
			return "revert";
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
		final int prime = 31;
		int result = 1;
		result = prime * result + unitType;
		result = prime * result + cssUnit;
		result = prime * result + ((dimensionUnitText == null) ? 0 : dimensionUnitText.hashCode());
		result = prime * result + Float.floatToIntBits(floatValue);
		result = prime * result + ((identCssText == null) ? 0 : identCssText.hashCode());
		result = prime * result + intValue;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		LexicalUnitImpl lu = parameters;
		while (lu != null) {
			result = prime * result + lu.hashCode();
			lu = lu.nextLexicalUnit;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LexicalUnitImpl other = (LexicalUnitImpl) obj;
		if (unitType != other.unitType) {
			return false;
		}
		if (cssUnit != other.cssUnit) {
			return false;
		}
		if (dimensionUnitText == null) {
			if (other.dimensionUnitText != null) {
				return false;
			}
		} else if (!dimensionUnitText.equals(other.dimensionUnitText)) {
			return false;
		}
		if (Float.floatToIntBits(floatValue) != Float.floatToIntBits(other.floatValue)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		if (identCssText == null) {
			if (other.identCssText != null) {
				return false;
			}
		} else if (!identCssText.equals(other.identCssText)) {
			return false;
		}
		if (intValue != other.intValue) {
			return false;
		}
		if (parameters == null) {
			return other.parameters == null;
		} else if (other.parameters == null) {
			return false;
		}
		LexicalUnitImpl lu = parameters;
		LexicalUnitImpl olu = other.parameters;
		while (lu != null) {
			if (!lu.equals(olu)) {
				return false;
			}
			lu = lu.nextLexicalUnit;
			olu = olu.nextLexicalUnit;
		}
		return olu == null;
	}

	@Override
	public LexicalUnitImpl clone() {
		return clone(null);
	}

	private LexicalUnitImpl clone(LexicalUnitImpl newOwner) {
		LexicalUnitImpl clon = new LexicalUnitImpl(unitType);
		clon.cssUnit = cssUnit;
		clon.intValue = intValue;
		clon.floatValue = floatValue;
		clon.dimensionUnitText = dimensionUnitText;
		clon.identCssText = identCssText;
		clon.value = value;
		if (nextLexicalUnit != null) {
			clon.nextLexicalUnit = nextLexicalUnit.clone(newOwner);
			clon.nextLexicalUnit.previousLexicalUnit = clon;
		}
		clon.ownerLexicalUnit = newOwner;
		if (parameters != null) {
			clon.parameters = parameters.clone(clon);
		}
		return clon;
	}

}
