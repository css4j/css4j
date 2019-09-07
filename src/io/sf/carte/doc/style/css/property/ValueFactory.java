/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.SACParserFactory;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.PrimitiveValue.LexicalSetter;

/**
 * Factory of CSS values.
 * 
 * @author Carlos Amengual
 * 
 */
public class ValueFactory {

	private final byte flags;

	public ValueFactory() {
		this((byte)0);
	}

	public ValueFactory(byte flags) {
		super();
		this.flags = flags;
	}

	/**
	 * Tests whether the unit type of the given SAC lexical unit can apply to size (e.g.
	 * block size).
	 * 
	 * @param unit
	 *            the SAC lexical unit.
	 * @return <code>true</code> if it is a size type (including percentage and unknown dimension), false
	 *         otherwise.
	 */
	public static boolean isSizeSACUnit(LexicalUnit unit) {
		return sizeSACUnit(unit) != 0;
	}

	/**
	 * Finds the unit type of the given SAC lexical unit that can apply to size (e.g.
	 * block size).
	 * 
	 * @param unit
	 *            the SAC lexical unit.
	 * @return the unit type if it is a size type (including percentage and unknown dimension), 0
	 *         otherwise.
	 */
	private static short sizeSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_FUNCTION) {
			type = functionDimensionArgumentUnit(unit);
		} else if (type == LexicalUnit.SAC_SUB_EXPRESSION) {
			type = subexpressionDimensionUnit(unit);
		}
		switch (type) {
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_CENTIMETER:
		case LexicalUnit.SAC_DIMENSION:
		case LexicalUnit.SAC_INCH:
		case LexicalUnit.SAC_MILLIMETER:
		case LexicalUnit2.SAC_CAP:
		case LexicalUnit2.SAC_CH:
		case LexicalUnit2.SAC_IC:
		case LexicalUnit2.SAC_LH:
		case LexicalUnit2.SAC_QUARTER_MILLIMETER:
		case LexicalUnit2.SAC_REM:
		case LexicalUnit2.SAC_RLH:
		case LexicalUnit2.SAC_TURN:
		case LexicalUnit2.SAC_VB:
		case LexicalUnit2.SAC_VH:
		case LexicalUnit2.SAC_VI:
		case LexicalUnit2.SAC_VMAX:
		case LexicalUnit2.SAC_VMIN:
		case LexicalUnit2.SAC_VW:
			return type;
		case LexicalUnit.SAC_INTEGER:
			return unit.getIntegerValue() == 0 ? type : 0;
		default:
			return 0;
		}
	}

	/**
	 * Tests whether the unit type of the given SAC lexical unit is a resolution unit.
	 * 
	 * @param unit
	 *            the SAC lexical unit value.
	 * @return <code>true</code> if it is a resolution type, <code>false</code> otherwise.
	 */
	public static boolean isResolutionSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_FUNCTION) {
			type = firstArgumentUnit(unit);
		}
		switch (type) {
		case LexicalUnit2.SAC_DOTS_PER_CENTIMETER:
		case LexicalUnit2.SAC_DOTS_PER_INCH:
		case LexicalUnit2.SAC_DOTS_PER_PIXEL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given SAC value can represent a size greater than zero (e.g. font
	 * size).
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if it is a size type (including percentage and unknown dimension), false
	 *         otherwise.
	 */
	public static boolean isPositiveSizeSACUnit(LexicalUnit unit) {
		final short utype = unit.getLexicalUnitType();
		short type = utype;
		if (utype == LexicalUnit.SAC_FUNCTION) {
			type = functionDimensionArgumentUnit(unit);
		}
		switch (type) {
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_CENTIMETER:
		case LexicalUnit.SAC_DIMENSION:
		case LexicalUnit.SAC_INCH:
		case LexicalUnit.SAC_MILLIMETER:
		case LexicalUnit2.SAC_CAP:
		case LexicalUnit2.SAC_CH:
		case LexicalUnit2.SAC_IC:
		case LexicalUnit2.SAC_LH:
		case LexicalUnit2.SAC_QUARTER_MILLIMETER:
		case LexicalUnit2.SAC_REM:
		case LexicalUnit2.SAC_RLH:
		case LexicalUnit2.SAC_VB:
		case LexicalUnit2.SAC_VH:
		case LexicalUnit2.SAC_VI:
		case LexicalUnit2.SAC_VMAX:
		case LexicalUnit2.SAC_VMIN:
		case LexicalUnit2.SAC_VW:
			return utype == LexicalUnit.SAC_FUNCTION || unit.getFloatValue() > 0f;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given SAC unit type is a size or numeric unit.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if it is a size or numeric type (including percentage and unknown dimension), false
	 *         otherwise.
	 */
	public static boolean isSizeOrNumberSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_FUNCTION) {
			type = functionDimensionArgumentUnit(unit);
		}
		switch (type) {
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_CENTIMETER:
		case LexicalUnit.SAC_DIMENSION:
		case LexicalUnit.SAC_INCH:
		case LexicalUnit.SAC_MILLIMETER:
		case LexicalUnit.SAC_INTEGER:
		case LexicalUnit.SAC_REAL:
		case LexicalUnit2.SAC_CAP:
		case LexicalUnit2.SAC_CH:
		case LexicalUnit2.SAC_IC:
		case LexicalUnit2.SAC_LH:
		case LexicalUnit2.SAC_QUARTER_MILLIMETER:
		case LexicalUnit2.SAC_REM:
		case LexicalUnit2.SAC_RLH:
		case LexicalUnit2.SAC_VB:
		case LexicalUnit2.SAC_VH:
		case LexicalUnit2.SAC_VI:
		case LexicalUnit2.SAC_VMAX:
		case LexicalUnit2.SAC_VMIN:
		case LexicalUnit2.SAC_VW:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given SAC unit type is a plain number (real or integer) or a percentage.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is a plain number or a percentage, <code>false</code> otherwise.
	 */
	public static boolean isPlainNumberOrPercentSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		switch (type) {
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_INTEGER:
		case LexicalUnit.SAC_REAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given SAC unit type is numeric.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is a numeric type, <code>false</code> otherwise.
	 */
	public static boolean isNumericSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_FUNCTION) {
			type = firstArgumentUnit(unit);
		}
		switch (type) {
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_INTEGER:
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
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
		case LexicalUnit2.SAC_CAP:
		case LexicalUnit2.SAC_CH:
		case LexicalUnit2.SAC_IC:
		case LexicalUnit2.SAC_LH:
		case LexicalUnit2.SAC_QUARTER_MILLIMETER:
		case LexicalUnit2.SAC_REM:
		case LexicalUnit2.SAC_RLH:
		case LexicalUnit2.SAC_TURN:
		case LexicalUnit2.SAC_VB:
		case LexicalUnit2.SAC_VH:
		case LexicalUnit2.SAC_VI:
		case LexicalUnit2.SAC_VMAX:
		case LexicalUnit2.SAC_VMIN:
		case LexicalUnit2.SAC_VW:
		case LexicalUnit2.SAC_DOTS_PER_CENTIMETER:
		case LexicalUnit2.SAC_DOTS_PER_INCH:
		case LexicalUnit2.SAC_DOTS_PER_PIXEL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given SAC unit type is an angle unit.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is an angle type, <code>false</code> otherwise.
	 */
	public static boolean isAngleSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_FUNCTION) {
			if (isColorFunction(unit)) {
				return false;
			}
			type = firstArgumentUnit(unit);
		}
		switch (type) {
		case LexicalUnit.SAC_DEGREE:
		case LexicalUnit.SAC_RADIAN:
		case LexicalUnit.SAC_GRADIAN:
		case LexicalUnit2.SAC_TURN:
			return true;
		case LexicalUnit.SAC_INTEGER:
			if (unit.getIntegerValue() == 0) {
				return true;
			}
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given SAC unit type is a time unit.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is a time type, <code>false</code> otherwise.
	 */
	public static boolean isTimeSACUnit(LexicalUnit unit) {
		short type = unit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_FUNCTION) {
			type = firstArgumentUnit(unit);
			if (type == LexicalUnit.SAC_INTEGER) {
				return false;
			}
		}
		switch (type) {
		case LexicalUnit.SAC_MILLISECOND:
		case LexicalUnit.SAC_SECOND:
			return true;
		case LexicalUnit.SAC_INTEGER:
			if (unit.getIntegerValue() == 0) {
				return true;
			}
		default:
			return false;
		}
	}

	/**
	 * If the supplied value represents a function and not a color, get the unit type of the
	 * first dimension argument found.
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @return the unit type of the first dimension argument found, or -1 if the value has no
	 *         dimension arguments or is not a function.
	 */
	static short functionDimensionArgumentUnit(LexicalUnit lunit) {
		if (isColorFunction(lunit)) {
			return -1;
		}
		LexicalUnit lu = lunit.getParameters();
		while (lu != null) {
			short type = sizeSACUnit(lu);
			if (type != 0) {
				return type;
			}
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
				}
			}
		}
		return -1;
	}

	/**
	 * Get the unit type of the first dimension sub-value found.
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @return the unit type of the first dimension sub-value found, or -1 if the value has no
	 *         dimension sub-values.
	 */
	static short subexpressionDimensionUnit(LexicalUnit lunit) {
		LexicalUnit lu = lunit.getSubValues();
		while (lu != null) {
			short type = sizeSACUnit(lu);
			if (type != 0) {
				return type;
			}
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
				}
			}
		}
		return -1;
	}

	/**
	 * If the supplied value represents a function, get the unit type of the first
	 * argument.
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @return the unit type of the first argument, or -1 if the value has
	 *         no arguments or is not a function.
	 */
	private static short firstArgumentUnit(LexicalUnit lunit) {
		LexicalUnit lu = lunit.getParameters();
		if (lu != null) {
			return lu.getLexicalUnitType();
		}
		return -1;
	}

	private static boolean isColorFunction(LexicalUnit lunit) {
		String name = lunit.getFunctionName().toLowerCase(Locale.ROOT);
		// We may be using a parser that does not map "rgba" to RGBCOLOR.
		if ("hsl".equals(name) || "hsla".equals(name) || "hwb".equals(name) || name.endsWith("-gradient")
				|| "rgba".equals(name)) {
			return true;
		}
		return false;
	}

	/**
	 * Parses a property value. Assumes that the property is not a shorthand
	 * sub-property.
	 * <p>
	 * 
	 * @param value
	 *            the string containing the property value.
	 * @return the CSSValue object containing the parsed value.
	 * @throws DOMException
	 *             if a problem was found parsing the property.
	 */
	public StyleValue parseProperty(String value) throws DOMException {
		return parseProperty(value, SACParserFactory.createSACParser());
	}

	/**
	 * Parses a property value with the supplied parser.
	 * 
	 * @param value
	 *            the string containing the property value.
	 * @param parser
	 *            the SAC parser.
	 * @return the CSSValue object containing the parsed value.
	 * @throws DOMException
	 *             if a problem was found parsing the property.
	 */
	public StyleValue parseProperty(String value, Parser parser) throws DOMException {
		return parseProperty("", value, parser);
	}

	/**
	 * Parses a property value with the supplied parser.
	 * 
	 * @param propertyName
	 *            the string containing the property name.
	 * @param value
	 *            the string containing the property value.
	 * @param parser
	 *            the SAC parser.
	 * @return the CSSValue object containing the parsed value.
	 * @throws DOMException
	 *             if a problem was found parsing the property.
	 */
	public StyleValue parseProperty(String propertyName, String value, CSSParser parser) throws DOMException {
		Reader re = new StringReader(value);
		LexicalUnit lunit = null;
		try {
			lunit = parser.parsePropertyValue(propertyName, re);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		StyleValue css = createCSSValue(lunit);
		if (css == null) {
			css = createUnknownValue(value, lunit);
		}
		return css;
	}

	/**
	 * Parses a property value with the supplied parser.
	 * 
	 * @param value
	 *            the string containing the property value.
	 * @param parser
	 *            the SAC parser.
	 * @return the CSSValue object containing the parsed value.
	 * @throws DOMException
	 *             if a problem was found parsing the property.
	 */
	public StyleValue parseProperty(String propertyName, String value, Parser parser) throws DOMException {
		InputSource source = new InputSource();
		Reader re = new StringReader(value);
		source.setCharacterStream(re);
		LexicalUnit lunit = null;
		try {
			lunit = parser.parsePropertyValue(source);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		StyleValue css = createCSSValue(lunit);
		if (css == null) {
			css = createUnknownValue(value, lunit);
		}
		return css;
	}

	private UnknownValue createUnknownValue(String value, LexicalUnit lunit) {
		switch (lunit.getLexicalUnitType()) {
		case LexicalUnit.SAC_OPERATOR_COMMA:
		case LexicalUnit.SAC_OPERATOR_EXP:
		case LexicalUnit.SAC_OPERATOR_GE:
		case LexicalUnit.SAC_OPERATOR_GT:
		case LexicalUnit.SAC_OPERATOR_LE:
		case LexicalUnit.SAC_OPERATOR_LT:
		case LexicalUnit.SAC_OPERATOR_MINUS:
		case LexicalUnit.SAC_OPERATOR_MULTIPLY:
		case LexicalUnit.SAC_OPERATOR_PLUS:
		case LexicalUnit.SAC_OPERATOR_SLASH:
		case LexicalUnit.SAC_OPERATOR_TILDE:
			return null;
		default:
			UnknownValue css = new UnknownValue();
			css.setCssText(value);
			((PrimitiveValue) css).newLexicalSetter().setLexicalUnit(lunit);
			return css;
		}
	}

	/**
	 * Parses a feature value.
	 * <p>
	 * 
	 * @param feature the string containing the feature value
	 * @return the CSSPrimitiveValue object containing the parsed value.
	 * @throws DOMException if a problem was found parsing the feature.
	 */
	public PrimitiveValue parseMediaFeature(String feature) throws DOMException {
		Parser parser = SACParserFactory.createSACParser();
		InputSource source = new InputSource();
		Reader re = new StringReader(feature);
		source.setCharacterStream(re);
		LexicalUnit lunit = null;
		try {
			lunit = parser.parsePropertyValue(source);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		PrimitiveValue css;
		try {
			css = createCSSPrimitiveValue(lunit, false);
		} catch (DOMException e) {
			// Hack ?
			return createUnknownValue(feature, lunit);
		}
		LexicalUnit nlu = lunit.getNextLexicalUnit();
		if (nlu != null && nlu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH) {
			nlu = nlu.getNextLexicalUnit();
			if (nlu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Ratio lacks second value.");
			}
			// ratio
			PrimitiveValue css2 = createCSSPrimitiveValue(nlu, false);
			RatioValue ratio = new RatioValue();
			ratio.setAntecedentValue(css);
			ratio.setConsequentValue(css2);
			return ratio;
		}
		return css;
	}

	/**
	 * Creates a CSSValue according to the given lexical value.
	 * <p>
	 * The value is assumed to be stand-alone, independent of a shorthand property.
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @return a CSSValue associated to the given lexical value, or null if the lexical unit
	 *         was not appropriate.
	 * @throws DOMException
	 *             if the lexical unit had a wrong content to create a value.
	 */
	public StyleValue createCSSValue(LexicalUnit lunit)
			throws DOMException {
		return createCSSValue(lunit, null);
	}

	/**
	 * Creates a CSSValue according to the given lexical value.
	 * <p>
	 * The value is assumed to be stand-alone, independent of a shorthand property.
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @param style
	 *            the (style) declaration that should handle errors, or null to not
	 *            handle errors.
	 * @return a CSSValue associated to the given lexical value, or null if the lexical unit
	 *         was not appropriate.
	 * @throws DOMException
	 *             if the lexical unit had a wrong content to create a value.
	 */
	public StyleValue createCSSValue(LexicalUnit lunit, AbstractCSSStyleDeclaration style)
			throws DOMException {
		if (lunit.getNextLexicalUnit() != null) {
			ValueList superlist = null; // Comma-separated values
			ValueList list = ValueList.createWSValueList(); // Whitespace-separated
																	// values
			LexicalUnit nlu = lunit;
			do {
				StyleValue value;
				// Check for bracket list.
				if (nlu.getLexicalUnitType() != LexicalUnit2.SAC_LEFT_BRACKET) {
					ValueItem item = createCSSValueItem(nlu, false);
					if (item.hasWarnings() && style != null) {
						StyleDeclarationErrorHandler errHandler = style.getStyleDeclarationErrorHandler();
						if (errHandler != null) {
							item.handleSyntaxWarnings(errHandler);
						}
					}
					value = item.getCSSValue();
					if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
						nlu = item.getNextLexicalUnit();
					} else {
						nlu = nlu.getNextLexicalUnit();
					}
				} else { // Bracket list
					nlu = nlu.getNextLexicalUnit();
					// Better check for null now
					if (nlu == null) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Unmatched '['");
					}
					ListValueItem listitem = parseBracketList(nlu, style, false);
					value = listitem.getCSSValue();
					nlu = listitem.getNextLexicalUnit();
				}
				if (nlu != null) {
					if (nlu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						nlu = nlu.getNextLexicalUnit();
						if (superlist == null) {
							superlist = ValueList.createCSValueList();
						}
						if (list.getLength() > 0) {
							list.add(value);
							superlist.add(list);
							// New list
							list = ValueList.createWSValueList();
						} else {
							superlist.add(value);
						}
					} else {
						list.add(value);
					}
					continue;
				} else {
					if (superlist == null) {
						list.add(value);
					} else {
						if (list.getLength() > 0) {
							list.add(value);
							superlist.add(list);
						} else {
							superlist.add(value);
						}
					}
				}
				break;
			} while (nlu != null);
			if (superlist != null) {
				StyleValue value = listOrFirstItem(superlist);
				// if superlist is not null, cannot be empty
				// so value cannot be null here
				if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
					return listOrFirstItem((ValueList) value);
				}
				return value;
			}
			return listOrFirstItem(list);
		} else {
			return createCSSValueItem(lunit, false).getCSSValue();
		}
	}

	/**
	 * Parse a bracket list.
	 * 
	 * @param nlu         the lexical unit containing the bracket list.
	 * @param style       the style declaration to report issues to.
	 * @param subproperty <code>true</code> if the value must be a subproperty.
	 * @return the bracket list.
	 */
	public ListValueItem parseBracketList(LexicalUnit nlu, AbstractCSSStyleDeclaration style, boolean subproperty) {
		ListValueItem listitem = new ListValueItem();
		listitem.list = ValueList.createBracketValueList();
		while (nlu.getLexicalUnitType() != LexicalUnit2.SAC_RIGHT_BRACKET) {
			ValueItem item = createCSSValueItem(nlu, subproperty);
			if (item.hasWarnings() && style != null) {
				StyleDeclarationErrorHandler errHandler = style.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					item.handleSyntaxWarnings(errHandler);
				}
			}
			StyleValue value = item.getCSSValue();
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				nlu = item.getNextLexicalUnit();
			} else {
				nlu = nlu.getNextLexicalUnit();
			}
			listitem.list.add(value);
			if (nlu != null) {
				if (nlu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					// Ignoring
					nlu = nlu.getNextLexicalUnit();
					if (nlu == null) {
						throw new DOMException(DOMException.SYNTAX_ERR, "Unmatched '['");
					}
				}
			} else {
				throw new DOMException(DOMException.SYNTAX_ERR, "Unmatched '['");
			}
		}
		listitem.nextLexicalUnit = nlu.getNextLexicalUnit();
		return listitem;
	}

	static class BasicValueItem implements ValueItem {

		LexicalUnit nextLexicalUnit = null;

		@Override
		public StyleValue getCSSValue() {
			return null;
		}

		@Override
		public LexicalUnit getNextLexicalUnit() {
			return nextLexicalUnit;
		}

		@Override
		public boolean hasWarnings() {
			return false;
		}

		@Override
		public void handleSyntaxWarnings(StyleDeclarationErrorHandler handler) {
		}
		
	}

	/**
	 * The return value of
	 * {@link ValueFactory#parseBracketList(LexicalUnit, AbstractCSSStyleDeclaration, boolean)}.
	 */
	public static class ListValueItem extends BasicValueItem {

		private ValueList list = null;

		@Override
		public ValueList getCSSValue() {
			return list;
		}

	}

	private static StyleValue listOrFirstItem(ValueList list) {
		int ll = list.getLength();
		if (ll > 1) {
			return list;
		} else if (ll == 1) {
			return list.item(0);
		} else {
			return null;
		}
	}

	/**
	 * Creates a CSSValue simple item according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return null.
	 * </p>
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @param subproperty
	 *            true if the value is created under the umbrella of a shorthand set.
	 * @return a ValueItem associated to the given lexical value.
	 * @throws DOMException
	 *             if a problem was found setting the lexical value to a CSS value.
	 */
	public ValueItem createCSSValueItem(LexicalUnit lunit, boolean subproperty) throws DOMException {
		switch (lunit.getLexicalUnitType()) {
		case LexicalUnit.SAC_INHERIT:
			InheritValue value = InheritValue.getValue();
			if (subproperty)
				value = value.asSubproperty();
			return value;
		default:
			return createCSSPrimitiveValueItem(lunit, subproperty);
		}
	}

	/**
	 * Creates an AbstractCSSPrimitiveValue according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return null.
	 * </p>
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @param subp
	 *            the flag marking whether it is a sub-property.
	 * @return the AbstractCSSPrimitiveValue for the CSS primitive value.
	 * @throws DOMException
	 *             if a problem was found setting the lexical value to a CSS
	 *             primitive.
	 */
	PrimitiveValue createCSSPrimitiveValue(LexicalUnit lunit, boolean subp) throws DOMException {
		return createCSSPrimitiveValueItem(lunit, subp).getCSSValue();
	}

	/**
	 * Creates a LexicalSetter according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return null.
	 * </p>
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @param subp
	 *            the flag marking whether it is a sub-property.
	 * @return the LexicalSetter for the CSS primitive value.
	 * @throws DOMException
	 *             if a problem was found setting the lexical value to a CSS primitive.
	 */
	LexicalSetter createCSSPrimitiveValueItem(LexicalUnit lunit, boolean subp) throws DOMException {
		short unitType = lunit.getLexicalUnitType();
		PrimitiveValue primi;
		LexicalSetter setter;
		try {
			switch (unitType) {
			case LexicalUnit.SAC_IDENT:
				primi = new IdentifierValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_ATTR:
				primi = new AttrValue(flags);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_STRING_VALUE:
				primi = new StringValue(flags);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_URI:
				primi = new URIValue(flags);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_PERCENTAGE:
				primi = new PercentageValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_CENTIMETER:
			case LexicalUnit.SAC_DIMENSION:
			case LexicalUnit.SAC_EM:
			case LexicalUnit.SAC_EX:
			case LexicalUnit.SAC_INCH:
			case LexicalUnit.SAC_MILLIMETER:
			case LexicalUnit.SAC_PICA:
			case LexicalUnit.SAC_PIXEL:
			case LexicalUnit.SAC_POINT:
			case LexicalUnit2.SAC_CAP:
			case LexicalUnit2.SAC_CH:
			case LexicalUnit2.SAC_IC:
			case LexicalUnit2.SAC_LH:
			case LexicalUnit2.SAC_QUARTER_MILLIMETER:
			case LexicalUnit2.SAC_REM:
			case LexicalUnit2.SAC_RLH:
			case LexicalUnit2.SAC_VB:
			case LexicalUnit2.SAC_VH:
			case LexicalUnit2.SAC_VI:
			case LexicalUnit2.SAC_VMAX:
			case LexicalUnit2.SAC_VMIN:
			case LexicalUnit2.SAC_VW:
				primi = new NumberValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				((NumberValue) primi).lengthUnitType = true;
				break;
			case LexicalUnit.SAC_REAL:
			case LexicalUnit.SAC_DEGREE:
			case LexicalUnit.SAC_GRADIAN:
			case LexicalUnit.SAC_RADIAN:
			case LexicalUnit2.SAC_TURN:
			case LexicalUnit.SAC_HERTZ:
			case LexicalUnit.SAC_KILOHERTZ:
			case LexicalUnit.SAC_MILLISECOND:
			case LexicalUnit.SAC_SECOND:
			case LexicalUnit2.SAC_DOTS_PER_CENTIMETER:
			case LexicalUnit2.SAC_DOTS_PER_INCH:
			case LexicalUnit2.SAC_DOTS_PER_PIXEL:
			case LexicalUnit2.SAC_FR:
				primi = new NumberValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_INTEGER:
				primi = new NumberValue();
				((NumberValue) primi).setIntegerValue(lunit.getIntegerValue());
				(setter = primi.newLexicalSetter()).nextLexicalUnit = lunit.getNextLexicalUnit();
				break;
			case LexicalUnit.SAC_RGBCOLOR:
				primi = new ColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_FUNCTION:
				String func = lunit.getFunctionName().toLowerCase(Locale.ROOT);
				if ("rgb".equals(func) || "rgba".equals(func) || "hsl".equals(func) || "hsla".equals(func)
						|| "hwb".equals(func)) {
					primi = new ColorValue();
				} else if ("calc".equals(func)) {
					primi = new CalcValue();
				} else if (func.endsWith("linear-gradient") || func.endsWith("radial-gradient")
						|| func.endsWith("conic-gradient")) {
					primi = new GradientValue();
					setter = primi.newLexicalSetter();
					try {
						setter.setLexicalUnit(lunit);
					} catch (RuntimeException e) {
						if (func.charAt(0) == '-') {
							primi = new FunctionValue();
							setter = primi.newLexicalSetter();
							setter.setLexicalUnit(lunit);
						} else {
							throw e;
						}
					}
					break;
				} else if ("var".equals(func)) {
					// special case
					primi = createCustomProperty(lunit);
				} else if ("env".equals(func)) {
					primi = new EnvVariableValue();
				} else {
					primi = new FunctionValue();
				}
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_UNICODERANGE:
				primi = new UnicodeRangeValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit2.SAC_UNICODE_WILDCARD:
				primi = new UnicodeWildcardValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_RECT_FUNCTION:
				primi = new OMCSSRectValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_COUNTER_FUNCTION:
				primi = new CounterValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_COUNTERS_FUNCTION:
				primi = new CountersValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit2.SAC_ELEMENT_REFERENCE:
				primi = new ElementReferenceValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LexicalUnit.SAC_OPERATOR_COMMA:
				throw new DOMException(DOMException.SYNTAX_ERR, "A comma is not a valid primitive");
			default:
				// Unknown value
				primi = new UnknownValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
			}
		} catch (DOMException e) {
			throw e;
		} catch (RuntimeException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		primi.setSubproperty(subp);
		return setter;
	}

	private PrimitiveValue createCustomProperty(LexicalUnit lunit) {
		LexicalUnit lu = lunit.getParameters();
		if (lu == null || lu.getLexicalUnitType() != LexicalUnit.SAC_IDENT) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Variable name must be an identifier");
		}
		lu = lu.getNextLexicalUnit();
		PrimitiveValue primi;
		if (lu != null) {
			if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Fallback must be separated by comma");
			}
			lu = lu.getNextLexicalUnit();
			if (lu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "No fallback found after comma");
			}
			StyleValue fallback = createCSSValue(lu);
			primi = new CustomPropertyValue(fallback);
		} else {
			primi = new CustomPropertyValue();
		}
		return primi;
	}

	public LexicalUnit appendValueString(StringBuilder buf, LexicalUnit lunit) {
		LexicalUnit nlu;
		short type = lunit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_OPERATOR_COMMA) {
			nlu = lunit.getNextLexicalUnit();
			buf.append(',');
		} else if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
			nlu = lunit.getNextLexicalUnit();
			buf.append(' ').append('/');
		} else if (type != LexicalUnit2.SAC_LEFT_BRACKET) {
			ValueItem item = createCSSValueItem(lunit, true);
			nlu = item.getNextLexicalUnit();
			StyleValue cssValue = item.getCSSValue();
			String cssText = cssValue.getCssText();
			if (buf.length() != 0) {
				buf.append(' ');
			}
			buf.append(cssText);
		} else {
			ListValueItem item = parseBracketList(lunit.getNextLexicalUnit(), null, false);
			nlu = item.getNextLexicalUnit();
			buf.append(item.getCSSValue().getCssText());
		}
		return nlu;
	}

	public LexicalUnit appendMinifiedValueString(StringBuilder buf, LexicalUnit lunit) {
		LexicalUnit nlu;
		short type = lunit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_OPERATOR_COMMA) {
			nlu = lunit.getNextLexicalUnit();
			buf.append(',');
		} else if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
			nlu = lunit.getNextLexicalUnit();
			buf.append('/');
		} else if (type != LexicalUnit2.SAC_LEFT_BRACKET) {
			ValueItem item = createCSSValueItem(lunit, true);
			nlu = item.getNextLexicalUnit();
			StyleValue cssValue = item.getCSSValue();
			String cssText = cssValue.getMinifiedCssText("");
			int len = buf.length();
			char c;
			if (len != 0 && (c = buf.charAt(len - 1)) != ',' && c != '/') {
				buf.append(' ');
			}
			buf.append(cssText);
		} else {
			ListValueItem item = parseBracketList(lunit.getNextLexicalUnit(), null, false);
			nlu = item.getNextLexicalUnit();
			buf.append(item.getCSSValue().getMinifiedCssText(""));
		}
		return nlu;
	}

	/**
	 * Check whether the factory has the given flag set.
	 * 
	 * @param flag the flag.
	 * @return <code>true</code> if the flag is set.
	 */
	public boolean hasFactoryFlag(byte flag) {
		return (flags & flag) == flag;
	}

	/**
	 * Translate a SAC lexical type into a CSS primitive unit type.
	 * @param lunit 
	 *            the lexical unit.
	 * @return the unit type according to <code>CSSPrimitiveValue</code>.
	 */
	static short domPrimitiveType(LexicalUnit lunit) {
		short sacType = lunit.getLexicalUnitType();
		short primiType;
		if (sacType == LexicalUnit.SAC_FUNCTION) {
			LexicalUnit lu = lunit.getParameters();
			while (lu != null) {
				if (isNumericSACUnit(lu)) {
					return CSSPrimitiveValue.CSS_NUMBER;
				}
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
						lu = lu.getNextLexicalUnit();
					}
				}
			}
			primiType = CSSPrimitiveValue.CSS_UNKNOWN;
		} else {
			primiType = domPrimitiveType(sacType);
		}
		return primiType;
	}

	/**
	 * Translate a SAC lexical type into a CSS primitive unit type.
	 * @param lunit 
	 *            the lexical unit.
	 * @return the unit type according to <code>CSSPrimitiveValue</code>.
	 */
	public static short domPrimitiveType(short sacType) {
		short primiType;
		switch (sacType) {
		case LexicalUnit.SAC_ATTR:
			primiType = CSSPrimitiveValue.CSS_ATTR;
			break;
		case LexicalUnit.SAC_IDENT:
			primiType = CSSPrimitiveValue.CSS_IDENT;
			break;
		case LexicalUnit.SAC_STRING_VALUE:
			primiType = CSSPrimitiveValue.CSS_STRING;
			break;
		case LexicalUnit.SAC_URI:
			primiType = CSSPrimitiveValue.CSS_URI;
			break;
		case LexicalUnit.SAC_CENTIMETER:
			primiType = CSSPrimitiveValue.CSS_CM;
			break;
		case LexicalUnit.SAC_DEGREE:
			primiType = CSSPrimitiveValue.CSS_DEG;
			break;
		case LexicalUnit.SAC_DIMENSION:
			primiType = CSSPrimitiveValue.CSS_DIMENSION;
			break;
		case LexicalUnit.SAC_EM:
			primiType = CSSPrimitiveValue.CSS_EMS;
			break;
		case LexicalUnit.SAC_EX:
			primiType = CSSPrimitiveValue.CSS_EXS;
			break;
		case LexicalUnit.SAC_GRADIAN:
			primiType = CSSPrimitiveValue.CSS_GRAD;
			break;
		case LexicalUnit.SAC_HERTZ:
			primiType = CSSPrimitiveValue.CSS_HZ;
			break;
		case LexicalUnit.SAC_INCH:
			primiType = CSSPrimitiveValue.CSS_IN;
			break;
		case LexicalUnit.SAC_KILOHERTZ:
			primiType = CSSPrimitiveValue.CSS_KHZ;
			break;
		case LexicalUnit.SAC_MILLIMETER:
			primiType = CSSPrimitiveValue.CSS_MM;
			break;
		case LexicalUnit.SAC_MILLISECOND:
			primiType = CSSPrimitiveValue.CSS_MS;
			break;
		case LexicalUnit.SAC_PERCENTAGE:
			primiType = CSSPrimitiveValue.CSS_PERCENTAGE;
			break;
		case LexicalUnit.SAC_PICA:
			primiType = CSSPrimitiveValue.CSS_PC;
			break;
		case LexicalUnit.SAC_PIXEL:
			primiType = CSSPrimitiveValue.CSS_PX;
			break;
		case LexicalUnit.SAC_POINT:
			primiType = CSSPrimitiveValue.CSS_PT;
			break;
		case LexicalUnit.SAC_RADIAN:
			primiType = CSSPrimitiveValue.CSS_RAD;
			break;
		case LexicalUnit.SAC_SECOND:
			primiType = CSSPrimitiveValue.CSS_S;
			break;
		case LexicalUnit.SAC_REAL:
		case LexicalUnit.SAC_INTEGER:
			primiType = CSSPrimitiveValue.CSS_NUMBER;
			break;
		case LexicalUnit.SAC_RGBCOLOR:
			primiType = CSSPrimitiveValue.CSS_RGBCOLOR;
			break;
		case LexicalUnit2.SAC_CAP:
			primiType = CSSPrimitiveValue2.CSS_CAP;
			break;
		case LexicalUnit2.SAC_CH:
			primiType = CSSPrimitiveValue2.CSS_CH;
			break;
		case LexicalUnit2.SAC_IC:
			primiType = CSSPrimitiveValue2.CSS_IC;
			break;
		case LexicalUnit2.SAC_LH:
			primiType = CSSPrimitiveValue2.CSS_LH;
			break;
		case LexicalUnit2.SAC_QUARTER_MILLIMETER:
			primiType = CSSPrimitiveValue2.CSS_QUARTER_MM;
			break;
		case LexicalUnit2.SAC_REM:
			primiType = CSSPrimitiveValue2.CSS_REM;
			break;
		case LexicalUnit2.SAC_RLH:
			primiType = CSSPrimitiveValue2.CSS_RLH;
			break;
		case LexicalUnit2.SAC_TURN:
			primiType = CSSPrimitiveValue2.CSS_TURN;
			break;
		case LexicalUnit2.SAC_VB:
			primiType = CSSPrimitiveValue2.CSS_VB;
			break;
		case LexicalUnit2.SAC_VH:
			primiType = CSSPrimitiveValue2.CSS_VH;
			break;
		case LexicalUnit2.SAC_VI:
			primiType = CSSPrimitiveValue2.CSS_VI;
			break;
		case LexicalUnit2.SAC_VMAX:
			primiType = CSSPrimitiveValue2.CSS_VMAX;
			break;
		case LexicalUnit2.SAC_VMIN:
			primiType = CSSPrimitiveValue2.CSS_VMIN;
			break;
		case LexicalUnit2.SAC_VW:
			primiType = CSSPrimitiveValue2.CSS_VW;
			break;
		case LexicalUnit2.SAC_DOTS_PER_CENTIMETER:
			primiType = CSSPrimitiveValue2.CSS_DPCM;
			break;
		case LexicalUnit2.SAC_DOTS_PER_INCH:
			primiType = CSSPrimitiveValue2.CSS_DPI;
			break;
		case LexicalUnit2.SAC_DOTS_PER_PIXEL:
			primiType = CSSPrimitiveValue2.CSS_DPPX;
			break;
		case LexicalUnit2.SAC_FR:
			primiType = CSSPrimitiveValue2.CSS_FR;
			break;
		case LexicalUnit.SAC_UNICODERANGE:
			primiType = CSSPrimitiveValue2.CSS_UNICODE_RANGE;
			break;
		case LexicalUnit2.SAC_UNICODE_WILDCARD:
			primiType = CSSPrimitiveValue2.CSS_UNICODE_WILDCARD;
			break;
		case LexicalUnit2.SAC_ELEMENT_REFERENCE:
			primiType = CSSPrimitiveValue2.CSS_ELEMENT_REFERENCE;
			break;
		case LexicalUnit.SAC_RECT_FUNCTION:
			primiType = CSSPrimitiveValue.CSS_RECT;
			break;
		case LexicalUnit.SAC_COUNTER_FUNCTION:
			primiType = CSSPrimitiveValue.CSS_COUNTER;
			break;
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
			primiType = CSSPrimitiveValue2.CSS_COUNTERS;
			break;
		default:
			primiType = CSSPrimitiveValue.CSS_UNKNOWN;
		}
		return primiType;
	}

}
