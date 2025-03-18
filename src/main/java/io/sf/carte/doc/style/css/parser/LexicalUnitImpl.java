/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.CSSValueSyntax.Multiplier;
import io.sf.carte.doc.style.css.impl.CSSUtil;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;

class LexicalUnitImpl implements LexicalUnit, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

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
		if (nextUnit == null || nextUnit.getLexicalUnitType() == LexicalType.EMPTY) {
			return;
		}

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
		if (replacementUnit == null || replacementUnit.getLexicalUnitType() == LexicalType.EMPTY) {
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
		return countReplaceBy(replacementUnit, 0);
	}

	private int countReplaceBy(LexicalUnit replacementUnit, int counter) throws CSSBudgetException {
		LexicalUnitImpl rlu = (LexicalUnitImpl) replacementUnit;
		if (rlu.getLexicalUnitType() == LexicalType.EMPTY) {
			counter++;
			LexicalUnitImpl nrlu = rlu.nextLexicalUnit;
			if (nrlu != null) {
				nrlu.previousLexicalUnit = null;
				counter = countReplaceBy(nrlu, counter);
				if (counter >= LEXICAL_REPLACE_LIMIT) {
					throw new CSSBudgetException("Exceeded limit of lexical units: " + LEXICAL_REPLACE_LIMIT);
				}
				return counter;
			}
			remove();
			return counter;
		}
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
		return parameters;
	}

	@Override
	public boolean isParameter() {
		return ownerLexicalUnit != null;
	}

	@Override
	public String getStringValue() {
		return value;
	}

	@Override
	public LexicalUnit getSubValues() {
		return null;
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
			case EMPTY:
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
		if (parameters != null) {
			return functionalSerialization(value);
		}

		switch (unitType) {
		case INTEGER:
			return Integer.toString(intValue);
		case PERCENTAGE:
		case REAL:
		case DIMENSION:
			StringBuilder buf = new StringBuilder();
			if (floatValue % 1 != 0) {
				buf.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				buf.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			if (dimensionUnitText != null) {
				buf.append(dimensionUnitText);
			}
			return buf;
		case IDENT:
			return identCssText != null ? identCssText : value;
		case STRING:
			return identCssText;
		case URI:
			String quri;
			if (identCssText != null) {
				quri = identCssText;
			} else if (value != null) {
				quri = ParseHelper.quote(value, '\'');
			} else {
				quri = "";
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

	CharSequence functionalSerialization(String fname) {
		StringBuilder buf = new StringBuilder(32);
		buf.append(fname).append('(');
		LexicalUnit lu = this.parameters;
		if (lu != null) {
			buf.append(lu.toString());
		}
		buf.append(')');
		return buf;
	}

	@Override
	public Match matches(CSSValueSyntax syntax) {
		if (syntax != null) {
			switch (getLexicalUnitType()) {
			case INHERIT:
			case INITIAL:
			case UNSET:
			case REVERT:
				// If a keyword is followed by something, it matches nothing
				return nextLexicalUnit == null ? Match.PENDING : Match.FALSE;
			default:
				break;
			}

			return matchSyntaxChain(syntax);
		}

		return Match.FALSE;
	}

	Match matchSyntaxChain(CSSValueSyntax syntax) {
		CSSValueSyntax comp = syntax;
		do {
			Multiplier mult = comp.getMultiplier();
			Category cat = comp.getCategory();
			if (cat == Category.universal) {
				return getUniversalMatch();
			}
			// If there is a next unit but no multiplier defined, skip this syntax
			if (mult != Multiplier.NONE || nextLexicalUnit == null
					|| cat == Category.transformList) {
				Match result;
				if ((result = matchesComponent(syntax, comp)) != Match.FALSE) {
					return result;
				}
			}
			comp = comp.getNext();
		} while (comp != null);

		return Match.FALSE;
	}

	Match getUniversalMatch() {
		return Match.TRUE;
	}

	private Match matchesComponent(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		LexicalUnitImpl lu = this;
		Match prevmatch = Match.FALSE;

		do {
			Match match = lu.componentMatch(rootSyntax, syntax);
			if (match == Match.FALSE) {
				return Match.FALSE;
			}
			if (prevmatch != Match.PENDING) {
				prevmatch = match;
			}

			lu = lu.nextLexicalUnit;
			if (lu == null) {
				break;
			}

			if (syntax.getMultiplier() == Multiplier.NUMBER) {
				if (lu.unitType == LexicalType.OPERATOR_COMMA) {
					lu = lu.nextLexicalUnit;
					if (lu == null) {
						break;
					}
				} else if (syntax.getCategory() != Category.transformList && lu.unitType != LexicalType.VAR
						&& lu.previousLexicalUnit.unitType != LexicalType.VAR) {
					return Match.FALSE;
				}
			}
		} while (true);

		return prevmatch;
	}

	@Override
	public Match shallowMatch(CSSValueSyntax syntax) {
		Match prevmatch = Match.FALSE;
		CSSValueSyntax comp = syntax;
		do {
			Match match = componentMatch(syntax, comp);
			if (match == Match.TRUE) {
				prevmatch = Match.TRUE;
				break;
			}
			if (prevmatch != Match.PENDING) {
				prevmatch = match;
			}
		} while ((comp = comp.getNext()) != null);
		return prevmatch;
	}

	private Match componentMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		switch (unitType) {
		case OPERATOR_COMMA:
			if (syntax.getMultiplier() != Multiplier.NUMBER
					|| nextLexicalUnit == null) {
				return Match.FALSE;
			}
			// Unlikely to reach this (unless consecutive commas when testing a # multiplier)
			return Match.TRUE;
		default:
			break;
		}

		return typeMatch(rootSyntax, syntax);
	}

	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		LexicalType type = getLexicalUnitType();
		Category cat = syntax.getCategory();
		switch (cat) {
		case number:
			if (type == LexicalType.REAL) {
				return Match.TRUE;
			}
			// pass-through
		case integer:
			if (type == LexicalType.INTEGER) {
				return Match.TRUE;
			}
			break;
		case percentage:
		case lengthPercentage:
			if (type == LexicalType.PERCENTAGE) {
				return Match.TRUE;
			}
			// pass-through
		case length:
		case angle:
		case time:
		case frequency:
		case resolution:
		case flex:
			// Dimension
			if (type == LexicalType.DIMENSION && unitMatchesCategory(getCssUnit(), cat)) {
				return Match.TRUE;
			}
			break;
		case color:
			if (type == LexicalType.IDENT && ColorIdentifiers.getInstance()
					.isColorIdentifier(getStringValue().toLowerCase(Locale.ROOT))) {
				return Match.TRUE;
			}
			break;
		case IDENT:
			if (syntax.getName().equals(getStringValue())) {
				return Match.TRUE;
			}
			break;
		case customIdent:
			if (type == LexicalType.IDENT) {
				return Match.TRUE;
			}
			break;
		case image:
			if (type == LexicalType.FUNCTION) {
				if (CSSUtil
						.isUnimplementedImageFunction(getStringValue().toLowerCase(Locale.ROOT))) {
					return Match.TRUE;
				}
				break;
			}
			// pass-through
		case url:
			if (type == LexicalType.URI || type == LexicalType.SRC) {
				return Match.TRUE;
			}
			break;
		case easingFunction:
			if (type == LexicalType.IDENT && "linear".equalsIgnoreCase(getStringValue())) {
				return Match.TRUE;
			}
			break;
		case string:
			if (type == LexicalType.STRING) {
				return Match.TRUE;
			}
			break;
		case universal:
			return getUniversalMatch();
		default:
		}

		return Match.FALSE;
	}

	/**
	 * Determine whether the given unit matches the syntax category.
	 * 
	 * @param unit the dimension unit.
	 * @param cat  the grammar type category to check.
	 * @return true if the unit matches the syntax category.
	 */
	static boolean unitMatchesCategory(short unit, Category cat) {
		switch (cat) {
		case length:
			return CSSUnit.isLengthUnitType(unit);
		case lengthPercentage:
			return CSSUnit.isLengthUnitType(unit) || unit == CSSUnit.CSS_PERCENTAGE;
		case percentage:
			return unit == CSSUnit.CSS_PERCENTAGE;
		case angle:
			return CSSUnit.isAngleUnitType(unit);
		case time:
			return CSSUnit.isTimeUnitType(unit);
		case resolution:
			return CSSUnit.isResolutionUnitType(unit);
		case flex:
			return unit == CSSUnit.CSS_FR;
		case frequency:
			return unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ;
		case integer:
		case number:
			// This probably never returns true (only actual dimensions reach this)
			return unit == CSSUnit.CSS_NUMBER;
		default:
			break;
		}
		return false;
	}

	/**
	 * Match the syntax category of a type with a given category.
	 * 
	 * @param calcContext  {@code true} if we are in {@code calc()} context.
	 * @param lenientLP    {@code true} for lenient {@code length-percentage}.
	 * @param typeCategory the category of the type being checked.
	 * @param cat          the grammar syntax type category to check.
	 * @return the match of the syntax category.
	 */
	static Match categoryMatch(boolean calcContext, boolean lenientLP,
			Category typeCategory, Category cat) {
		if (typeCategory == cat
				|| (typeCategory == Category.length && cat == Category.lengthPercentage)
				|| (typeCategory == Category.percentage && cat == Category.lengthPercentage)
				|| (typeCategory == Category.integer && cat == Category.number)
				// If the lexical unit is a calc() parameter, <number> is rounded to <integer>
				|| (calcContext && (typeCategory == Category.number && cat == Category.integer))
				|| (typeCategory == Category.url && cat == Category.image)
				|| (cat == Category.url && typeCategory == Category.image)) {
			return Match.TRUE;
		}

		if (lenientLP && ((typeCategory == Category.lengthPercentage && cat == Category.length)
				|| (typeCategory == Category.lengthPercentage && cat == Category.percentage))) {
			return Match.PENDING;
		}

		return Match.FALSE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + unitType.hashCode();
		result = prime * result + cssUnit;
		result = prime * result + ((dimensionUnitText == null) ? 0 : dimensionUnitText.hashCode());
		result = prime * result + Float.floatToIntBits(floatValue);
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
		LexicalUnitImpl clon = instantiateLexicalUnit();
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

	LexicalUnitImpl instantiateLexicalUnit() {
		return new LexicalUnitImpl(unitType);
	}

	@Override
	public LexicalUnitImpl clone() {
		return clone(null);
	}

	private LexicalUnitImpl clone(LexicalUnitImpl newOwner) {
		LexicalUnitImpl clon = shallowClone();
		if (nextLexicalUnit != null) {
			clon.nextLexicalUnit = nextLexicalUnit.clone(newOwner);
			clon.nextLexicalUnit.previousLexicalUnit = clon;
		}
		clon.ownerLexicalUnit = newOwner;
		return clon;
	}

}
