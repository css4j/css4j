/*

 Copyright (c) 2005-2022, Carlos Amengual.

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
		//
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
		if (replacementUnit.getLexicalUnitType() == LexicalType.EMPTY) {
			remove();
			return 1;
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
		case LABCOLOR:
		case LCHCOLOR:
		case HWBCOLOR:
		case COLOR_FUNCTION:
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
	public Match matches(CSSValueSyntax syntax) {
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
		//
		CSSValueSyntax comp = syntax;
		do {
			//
			Multiplier mult = comp.getMultiplier();
			Category cat = comp.getCategory();
			if (cat == Category.universal) {
				return Match.TRUE;
			}
			// If there is a next unit but no multiplier defined, skip this syntax
			if (mult != Multiplier.NONE || nextLexicalUnit == null || cat == Category.transformList) {
				Match result;
				if ((result = matchesComponent(syntax, comp)) != Match.FALSE) {
					return result;
				}
			}
			comp = comp.getNext();
		} while (comp != null);
		return Match.FALSE;
	}

	private Match matchesComponent(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		LexicalUnitImpl lu = this;
		Match prevmatch = Match.FALSE;
		do {
			Match match = matchesComponent(lu, rootSyntax, syntax);
			if (match == Match.FALSE) {
				return Match.FALSE;
			}
			if (prevmatch != Match.PENDING) {
				prevmatch = match;
			}
			//
			lu = lu.nextLexicalUnit;
			if (lu == null) {
				break;
			}
			//
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

	private static Match matchesComponent(LexicalUnitImpl lexicalUnit, CSSValueSyntax rootSyntax,
			CSSValueSyntax syntax) {
		LexicalType type = lexicalUnit.getLexicalUnitType();
		switch (type) {
		case OPERATOR_COMMA:
			if (syntax.getMultiplier() != Multiplier.NUMBER || lexicalUnit.nextLexicalUnit == null) {
				return Match.FALSE;
			}
			// Unlikely to reach this (unless consecutive commas when testing a # multiplier)
			return Match.TRUE;
		case INHERIT:
		case INITIAL:
		case UNSET:
		case REVERT:
		case OPERATOR_SEMICOLON:
		case OPERATOR_PLUS:
		case OPERATOR_MINUS:
		case OPERATOR_MULTIPLY:
		case OPERATOR_SLASH:
		case OPERATOR_MOD:
		case OPERATOR_EXP:
		case OPERATOR_LT:
		case OPERATOR_GT:
		case OPERATOR_LE:
		case OPERATOR_GE:
		case OPERATOR_TILDE:
			return Match.FALSE;
		default:
			break;
		}
		//
		Category cat = syntax.getCategory();
		if (cat == Category.universal) {
			return Match.TRUE;
		}
		//
		switch (type) {
		case IDENT:
			return matchBoolean(cat == Category.customIdent
					|| (cat == Category.IDENT && syntax.getName().equals(lexicalUnit.getStringValue()))
					|| (cat == Category.color && ColorIdentifiers.getInstance()
							.isColorIdentifier(lexicalUnit.getStringValue().toLowerCase(Locale.ROOT))));
		case STRING:
			return matchBoolean(cat == Category.string);
		case ATTR:
			return matchAttr(lexicalUnit, rootSyntax, syntax);
		case URI:
			return matchBoolean(cat == Category.url || cat == Category.image);
		case DIMENSION:
			return matchBoolean(ParseHelper.matchesDimension(lexicalUnit.getCssUnit(), cat));
		case PERCENTAGE:
			return matchBoolean(cat == Category.percentage || cat == Category.lengthPercentage);
		case REAL:
			return matchBoolean(cat == Category.number);
		case INTEGER:
			return matchBoolean(cat == Category.integer || cat == Category.number);
		case RGBCOLOR:
		case HSLCOLOR:
		case LABCOLOR:
		case LCHCOLOR:
		case HWBCOLOR:
		case COLOR_FUNCTION:
			return matchBoolean(cat == Category.color);
		case CALC:
			return isNumericCategory(cat) ? matchesOperandOfSyntax(lexicalUnit.getParameters(), syntax) : Match.FALSE;
		case FUNCTION:
			return matchFunction(lexicalUnit, syntax);
		case ELEMENT_REFERENCE:
			return matchBoolean(cat == Category.image);
		case VAR:
			return Match.PENDING;
		case UNICODE_RANGE:
		case UNICODE_WILDCARD:
			return matchBoolean(cat == Category.unicodeRange);
		default:
		}
		return Match.FALSE;
	}

	private static Match matchBoolean(boolean b) {
		return b ? Match.TRUE : Match.FALSE;
	}

	private static Match matchAttr(LexicalUnitImpl lexicalUnit, CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		Match result = Match.FALSE;
		LexicalUnit param = lexicalUnit.getParameters();
		if (param != null) {
			String dataType;
			LexicalUnit fallback = null;
			param = param.getNextLexicalUnit();
			if (param != null) {
				switch (param.getLexicalUnitType()) {
				case OPERATOR_COMMA:
					dataType = "string";
					fallback = param.getNextLexicalUnit();
					break;
				case IDENT:
					// We got a data type spec
					dataType = param.getStringValue().toLowerCase(Locale.ROOT);
					param = param.getNextLexicalUnit();
					if (param != null) {
						if (param.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
							return Match.FALSE; // Should never happen
						}
						fallback = param.getNextLexicalUnit();
					}
					break;
				case VAR:
					return Match.PENDING;
				default:
					return Match.FALSE; // Should never happen
				}
			} else { // Implicit "string"
				return syntax.getCategory() == Category.string ? Match.TRUE : Match.FALSE;
			}
			// Now, let's try matching the attr datatype and the fallback (if available).
			CSSValueSyntax typeMatch = null;
			CSSValueSyntax comp = rootSyntax;
			topLoop: do {
				boolean attrTypeMatch = ParseHelper.matchAttrType(dataType, comp.getCategory());
				if (attrTypeMatch) {
					result = Match.PENDING;
					typeMatch = comp;
				}
				//
				if (fallback != null) {
					// We got a fallback
					CSSValueSyntax fallbackComp = rootSyntax;
					do {
						Match match = fallback.matches(fallbackComp);
						if (match == Match.FALSE) {
							// url is a special case
							if (attrTypeMatch && "url".equals(dataType)
								&& fallback.getLexicalUnitType() == LexicalType.STRING
								&& hasNoSiblings(lexicalUnit)
								&& fallback.getNextLexicalUnit() == null) {
								result = Match.TRUE;
							} else {
								continue;
							}
						} else if (match == Match.PENDING) {
							result = Match.PENDING;
							if (attrTypeMatch) {
								continue;
							}
						} else { // TRUE
							if (!attrTypeMatch) {
								result = Match.PENDING;
								// Perhaps we'll have better luck matching attr datatype with next syntax
								continue topLoop;
							} else if (hasNoSiblings(lexicalUnit) || (typeMatch == fallbackComp)) {
								result = Match.TRUE;
							} // Here, attrTypeMatch is true and 'result' should be PENDING
						}
						return result;
					} while ((fallbackComp = fallbackComp.getNext()) != null);
				} else if (attrTypeMatch) {
					return Match.TRUE;
				}
			} while ((comp = comp.getNext()) != null);
		}
		return result;
	}

	private static boolean hasNoSiblings(LexicalUnit lexicalUnit) {
		return lexicalUnit.getNextLexicalUnit() == null
				&& lexicalUnit.getPreviousLexicalUnit() == null;
	}

	private static Match matchFunction(LexicalUnitImpl lexicalUnit, CSSValueSyntax syntax) {
		Category cat = syntax.getCategory();
		String func = lexicalUnit.getFunctionName().toLowerCase(Locale.ROOT);
		if (func.endsWith("-gradient") || func.equals("image") || func.equals("image-set")
			|| func.equals("cross-fade")) {
			return matchBoolean(cat == Category.image);
		} else if (func.equals("env")) {
			return Match.PENDING;
		} else if (isNumericCategory(cat)) {
			if (isCompatibleNumberFunction(func, cat)) {
				return Match.TRUE;
			}
			return matchesOperandOfSyntax(lexicalUnit.getParameters(), syntax);
		} else {
			return matchBoolean(ParseHelper.isTransformFunction(func));
		}
	}

	/**
	 * If the supplied value represents the chain of operands of an expression,
	 * determine if the expression has operands that could be consistent with the
	 * requested category.
	 * 
	 * @param lunit the lexical value containing the first operand.
	 * @param cat   the data type category.
	 * @return the match that would be expected from the expression.
	 */
	private static Match matchesOperandOfSyntax(LexicalUnit lunit, CSSValueSyntax syntax) {
		Category cat = syntax.getCategory();
		Match expected = Match.FALSE;
		while (lunit != null) {
			LexicalType sacType = lunit.getLexicalUnitType();
			if (sacType == LexicalType.DIMENSION) {
				if (ParseHelper.matchesDimension(lunit.getCssUnit(), cat) || cat == Category.number) {
					if (expected != Match.PENDING) {
						expected = Match.TRUE;
					}
				} else if (cat == Category.percentage) {
					return Match.FALSE;
				} else { // Probably FALSE, but...
					expected = Match.PENDING;
				}
			} else if (sacType == LexicalType.REAL) {
				if (cat == Category.number && expected != Match.PENDING) {
					expected = Match.TRUE;
				}
			} else if (sacType == LexicalType.INTEGER) {
				if ((cat == Category.integer || cat == Category.number) && expected != Match.PENDING) {
					expected = Match.TRUE;
				}
			} else if (sacType == LexicalType.PERCENTAGE) {
				if (ParseHelper.matchesDimension(CSSUnit.CSS_PERCENTAGE, cat)) {
					if (expected != Match.PENDING) {
						expected = Match.TRUE;
					}
				} else {
					return Match.FALSE;
				}
			} else if (sacType == LexicalType.CALC) {
				Match match = matchesOperandOfSyntax(lunit.getParameters(), syntax);
				if (match != Match.FALSE) {
					if (expected != Match.PENDING) {
						expected = match;
					}
				} else {
					return Match.FALSE;
				}
			} else if (sacType == LexicalType.VAR) {
				expected = Match.PENDING;
				break;
			} else if (sacType == LexicalType.SUB_EXPRESSION) {
				Match match = matchesOperandOfSyntax(lunit.getSubValues(), syntax);
				if (match != Match.FALSE) {
					if (expected != Match.PENDING) {
						expected = match;
					}
				} else {
					return Match.FALSE;
				}
			} else if (sacType == LexicalType.ATTR || sacType == LexicalType.FUNCTION) {
				LexicalUnit attr = lunit;
				if (lunit.getNextLexicalUnit() != null) {
					attr = lunit.shallowClone();
				}
				Match match = attr.matches(syntax);
				if (match == Match.FALSE) {
					expected = Match.FALSE;
					break;
				}
				if (expected != Match.PENDING) {
					expected = match;
				}
			}
			lunit = lunit.getNextLexicalUnit();
		}
		return expected;
	}

	private static boolean isCompatibleNumberFunction(String func, Category cat) {
		switch (cat) {
		case number:
			return "sin".equals(func) || "cos".equals(func) || "tan".equals(func)
					|| "sign".equals(func);
		case angle:
			return "asin".equals(func) || "acos".equals(func) || "atan".equals(func)
					|| "atan2".equals(func);
		default:
			break;
		}
		return false;
	}

	private static boolean isNumericCategory(Category cat) {
		switch (cat) {
		case number:
		case integer:
		case length:
		case percentage:
		case lengthPercentage:
		case angle:
		case time:
		case frequency:
		case resolution:
		case flex:
			return true;
		default:
			break;
		}
		return false;
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
