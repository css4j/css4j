/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class LexicalUnitImpl implements LexicalUnit {

	// How many lexical units we accept in #replaceBy()/#countReplaceBy() argument
	private static final int LEXICAL_REPLACE_LIMIT = 0x40000;

	private LexicalType unitType;

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

	public LexicalUnitImpl(LexicalType unitType) {
		super();
		this.unitType = unitType;
	}

	@Override
	public LexicalType getLexicalUnitType() {
		return unitType;
	}

	void setUnitType(LexicalType unitType) {
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
		if (nlu.ownerLexicalUnit != null) {
			throw new IllegalArgumentException("Argument is a parameter of another unit.");
		}
		if (nlu.getPreviousLexicalUnit() != null) {
			throw new IllegalArgumentException("Argument unit has a previous unit.");
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
	public LexicalUnit replaceBy(LexicalUnit replacementUnit) throws CSSBudgetException {
		if (replacementUnit == null) {
			return remove();
		}
		LexicalUnitImpl rlu = (LexicalUnitImpl) replacementUnit;
		if (rlu.ownerLexicalUnit != null) {
			throw new IllegalArgumentException("Replacement unit is a parameter.");
		}
		// Set the owner
		if (ownerLexicalUnit != null) {
			if (rlu.previousLexicalUnit != null) {
				throw new IllegalArgumentException("Replacement unit has a previous unit.");
			}
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
		// previous unit
		if (previousLexicalUnit != null) {
			previousLexicalUnit.nextLexicalUnit = rlu;
			rlu.previousLexicalUnit = previousLexicalUnit;
		}
		// next unit(s)
		if (nextLexicalUnit != null) {
			int counter = 0;
			LexicalUnitImpl lu = rlu;
			LexicalUnitImpl lastlu;
			do {
				lastlu = lu;
				lu = lu.nextLexicalUnit;
				// Check for possible DoS attack
				counter++;
				if (counter >= LEXICAL_REPLACE_LIMIT) {
					throw new CSSBudgetException("Exceeded limit of lexical units: " + LEXICAL_REPLACE_LIMIT);
				}
			} while (lu != null);
			nextLexicalUnit.previousLexicalUnit = lastlu;
			lastlu.nextLexicalUnit = nextLexicalUnit;
			nextLexicalUnit = null;
		}
		previousLexicalUnit = null;
		return replacementUnit;
	}

	@Override
	public int countReplaceBy(LexicalUnit replacementUnit) throws CSSBudgetException {
		if (replacementUnit == null) {
			remove();
			return 0;
		}
		int counter = 0;
		LexicalUnitImpl rlu = (LexicalUnitImpl) replacementUnit;
		if (rlu.ownerLexicalUnit != null) {
			throw new IllegalArgumentException("Replacement unit is a parameter.");
		}
		// Set the owner
		if (ownerLexicalUnit != null) {
			if (rlu.previousLexicalUnit != null) {
				throw new IllegalArgumentException("Replacement unit has a previous unit.");
			}
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
		// previous unit
		if (previousLexicalUnit != null) {
			previousLexicalUnit.nextLexicalUnit = rlu;
			rlu.previousLexicalUnit = previousLexicalUnit;
		}
		// next unit(s)
		if (nextLexicalUnit != null) {
			LexicalUnitImpl lu = rlu;
			LexicalUnitImpl lastlu;
			do {
				lastlu = lu;
				lu = lu.nextLexicalUnit;
				// Check for possible DoS attack
				counter++;
				if (counter >= LEXICAL_REPLACE_LIMIT) {
					throw new CSSBudgetException("Exceeded limit of lexical units: " + LEXICAL_REPLACE_LIMIT);
				}
			} while (lu != null);
			nextLexicalUnit.previousLexicalUnit = lastlu;
			lastlu.nextLexicalUnit = nextLexicalUnit;
			nextLexicalUnit = null;
		}
		previousLexicalUnit = null;
		return counter;
	}

	@Override
	public LexicalUnit remove() {
		if (previousLexicalUnit != null) {
			previousLexicalUnit.nextLexicalUnit = nextLexicalUnit;
		}
		LexicalUnitImpl rlu = nextLexicalUnit;
		if (nextLexicalUnit != null) {
			nextLexicalUnit.previousLexicalUnit = previousLexicalUnit;
			nextLexicalUnit = null;
		}
		// Check possible reference by owner
		if (ownerLexicalUnit != null) {
			if (previousLexicalUnit == null) {
				ownerLexicalUnit.parameters = rlu;
			}
			ownerLexicalUnit = null;
		}
		previousLexicalUnit = null;
		return rlu;
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
		if (unitType != LexicalType.SUB_EXPRESSION) {
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
		if (unitType == LexicalType.ATTR) {
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
		if (unitType == LexicalType.SUB_EXPRESSION || unitType == LexicalType.UNICODE_RANGE) {
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
		/*
		 * If you change any of the following, you may need to change it in
		 * LexicalValue.serializeMinifiedSequence() as well.
		 */
		StringBuilder buf = new StringBuilder();
		LexicalUnitImpl lu = this;
		boolean needSpaces = false;
		while (lu != null) {
			switch(lu.getLexicalUnitType()) {
			case OPERATOR_EXP:
			case OPERATOR_GE:
			case OPERATOR_GT:
			case OPERATOR_LE:
			case OPERATOR_LT:
			case OPERATOR_MULTIPLY:
			case OPERATOR_SLASH:
			case OPERATOR_TILDE:
			case LEFT_BRACKET:
				needSpaces = false;
			case OPERATOR_COMMA:
			case OPERATOR_SEMICOLON:
				break;
			case RIGHT_BRACKET:
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
		case INTEGER:
			return Integer.toString(intValue);
		case PERCENTAGE:
		case REAL:
		case DIMENSION:
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
		case RGBCOLOR:
			if (identCssText != null) {
				return identCssText;
			}
		case FUNCTION:
			return functionalSerialization(value);
		case CALC:
		case RECT_FUNCTION:
		case VAR:
		case ATTR:
		case HSLCOLOR:
		case COUNTER_FUNCTION:
		case COUNTERS_FUNCTION:
		case CUBIC_BEZIER_FUNCTION:
		case STEPS_FUNCTION:
			return functionalSerialization(value.toLowerCase(Locale.ROOT));
		case SUB_EXPRESSION:
			buf = new StringBuilder();
			buf.append('(');
			LexicalUnit lu = this.parameters;
			if (lu != null) {
				buf.append(lu.toString());
			}
			buf.append(')');
			return buf.toString();
		case IDENT:
			return identCssText != null ? identCssText : value;
		case STRING:
			return identCssText;
		case URI:
			String quri;
			if (identCssText != null) {
				quri = identCssText;
			} else {
				quri = ParseHelper.quote(value, '\'');
			}
			return "url(" + quri + ")";
		case INHERIT:
			return "inherit";
		case INITIAL:
			return "initial";
		case UNSET:
			return "unset";
		case REVERT:
			return "revert";
		case ELEMENT_REFERENCE:
			if (value == null) {
				return "element(#)";
			}
			int len = value.length();
			buf = new StringBuilder(len + 10);
			buf.append("element(#").append(value).append(')');
			return buf.toString();
		case UNICODE_RANGE:
			buf = new StringBuilder();
			lu = this.parameters;
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalType.INTEGER) {
					buf.append("U+").append(Integer.toHexString(lu.getIntegerValue()));
				} else {
					buf.append("U+").append(lu.getStringValue());
				}
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					buf.append('-');
					if (lu.getLexicalUnitType() == LexicalType.INTEGER) {
						buf.append(Integer.toHexString(lu.getIntegerValue()));
					} else {
						buf.append(lu.getStringValue());
					}
				}
			}
			return buf.toString();
		case UNICODE_WILDCARD:
			return getStringValue();
		case OPERATOR_COMMA:
			return ",";
		case OPERATOR_SEMICOLON:
			return ";";
		case OPERATOR_EXP:
			return "^";
		case OPERATOR_GE:
			return ">=";
		case OPERATOR_GT:
			return ">";
		case OPERATOR_LE:
			return "<=";
		case OPERATOR_LT:
			return "<";
		case OPERATOR_MINUS:
			return "-";
		case OPERATOR_MOD:
			return "%";
		case OPERATOR_MULTIPLY:
			return "*";
		case OPERATOR_PLUS:
			return "+";
		case OPERATOR_SLASH:
			return "/";
		case OPERATOR_TILDE:
			return "~";
		case LEFT_BRACKET:
			return "[";
		case RIGHT_BRACKET:
			return "]";
		case COMPAT_IDENT:
		case COMPAT_PRIO:
			return ParseHelper.escapeControl(value);
		default:
		}
		return "";
	}

	private CharSequence functionalSerialization(String fname) {
		StringBuilder buf = new StringBuilder();
		buf.append(fname).append('(');
		LexicalUnit lu = this.parameters;
		if (lu != null) {
			buf.append(lu.toString());
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + unitType.hashCode();
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
	public LexicalUnitImpl shallowClone() {
		LexicalUnitImpl clon = new LexicalUnitImpl(unitType);
		clon.cssUnit = cssUnit;
		clon.intValue = intValue;
		clon.floatValue = floatValue;
		clon.dimensionUnitText = dimensionUnitText;
		clon.identCssText = identCssText;
		clon.value = value;
		if (parameters != null) {
			clon.parameters = parameters.clone(clon);
		}
		return clon;
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
