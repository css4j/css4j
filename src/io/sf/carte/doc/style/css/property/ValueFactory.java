/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSOMParser;
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
	 * Tests whether the unit type of the given NSAC lexical unit can apply to size (e.g.
	 * block size).
	 * 
	 * @param unit
	 *            the NSAC lexical unit.
	 * @return <code>true</code> if it is a size type (including percentage and unknown dimension), false
	 *         otherwise.
	 */
	public static boolean isSizeSACUnit(LexicalUnit unit) {
		return sizeSACUnit(unit) != CSSUnit.CSS_INVALID;
	}

	/**
	 * Finds the unit type of the given NSAC lexical unit that can apply to size (e.g.
	 * block size).
	 * 
	 * @param unit
	 *            the NSAC lexical unit.
	 * @return the CSS unit type if it is a size type (including percentage),
	 *         CSS_INVALID otherwise.
	 */
	private static short sizeSACUnit(LexicalUnit unit) {
		LexicalType type = unit.getLexicalUnitType();
		short cssUnit;
		if (type == LexicalType.FUNCTION || type == LexicalType.VAR || type == LexicalType.ATTR) {
			cssUnit = functionDimensionArgumentUnit(unit);
		} else if (type == LexicalType.SUB_EXPRESSION) {
			cssUnit = subexpressionDimensionUnit(unit);
		} else {
			cssUnit = unit.getCssUnit();
		}
		switch (cssUnit) {
		case CSSUnit.CSS_PERCENTAGE:
		case CSSUnit.CSS_PX:
		case CSSUnit.CSS_PT:
		case CSSUnit.CSS_EM:
		case CSSUnit.CSS_EX:
		case CSSUnit.CSS_PC:
		case CSSUnit.CSS_CM:
		case CSSUnit.CSS_IN:
		case CSSUnit.CSS_MM:
		case CSSUnit.CSS_CAP:
		case CSSUnit.CSS_CH:
		case CSSUnit.CSS_IC:
		case CSSUnit.CSS_LH:
		case CSSUnit.CSS_QUARTER_MM:
		case CSSUnit.CSS_REM:
		case CSSUnit.CSS_RLH:
		case CSSUnit.CSS_TURN:
		case CSSUnit.CSS_VB:
		case CSSUnit.CSS_VH:
		case CSSUnit.CSS_VI:
		case CSSUnit.CSS_VMAX:
		case CSSUnit.CSS_VMIN:
		case CSSUnit.CSS_VW:
			return cssUnit;
		default:
			if (type == LexicalType.INTEGER && unit.getIntegerValue() == 0) {
				return CSSUnit.CSS_NUMBER;
			}
			return CSSUnit.CSS_INVALID;
		}
	}

	/**
	 * Tests whether the unit type of the given NSAC lexical unit is a resolution unit.
	 * 
	 * @param unit
	 *            the NSAC lexical unit value.
	 * @return <code>true</code> if it is a resolution type, <code>false</code> otherwise.
	 */
	public static boolean isResolutionSACUnit(LexicalUnit unit) {
		LexicalType type = unit.getLexicalUnitType();
		short cssUnit;
		if (type == LexicalType.FUNCTION || type == LexicalType.VAR || type == LexicalType.ATTR) {
			unit = firstDimensionArgument(unit);
			return unit != null && isResolutionSACUnit(unit);
		} else {
			cssUnit = unit.getCssUnit();
		}
		switch (cssUnit) {
		case CSSUnit.CSS_DPCM:
		case CSSUnit.CSS_DPI:
		case CSSUnit.CSS_DPPX:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given NSAC value could represent a size greater than zero (e.g. font
	 * size).
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if it is a size type (including percentage and unknown dimension), false
	 *         otherwise.
	 */
	public static boolean isPositiveSizeSACUnit(LexicalUnit unit) {
		final LexicalType utype = unit.getLexicalUnitType();
		short cssUnit;
		boolean function;
		if (utype == LexicalType.FUNCTION || utype == LexicalType.VAR || utype == LexicalType.ATTR) {
			cssUnit = functionDimensionArgumentUnit(unit);
			function = true;
		} else {
			cssUnit = unit.getCssUnit();
			function = false;
		}
		switch (cssUnit) {
		case CSSUnit.CSS_PERCENTAGE:
		case CSSUnit.CSS_PX:
		case CSSUnit.CSS_PT:
		case CSSUnit.CSS_EM:
		case CSSUnit.CSS_EX:
		case CSSUnit.CSS_PC:
		case CSSUnit.CSS_CM:
		case CSSUnit.CSS_OTHER:
		case CSSUnit.CSS_IN:
		case CSSUnit.CSS_MM:
		case CSSUnit.CSS_CAP:
		case CSSUnit.CSS_CH:
		case CSSUnit.CSS_IC:
		case CSSUnit.CSS_LH:
		case CSSUnit.CSS_QUARTER_MM:
		case CSSUnit.CSS_REM:
		case CSSUnit.CSS_RLH:
		case CSSUnit.CSS_VB:
		case CSSUnit.CSS_VH:
		case CSSUnit.CSS_VI:
		case CSSUnit.CSS_VMAX:
		case CSSUnit.CSS_VMIN:
		case CSSUnit.CSS_VW:
			return function || unit.getFloatValue() > 0f;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given NSAC unit type is a size or numeric unit.
	 * 
	 * @param unit the lexical value.
	 * @return <code>true</code> if it is a size or numeric type (including
	 *         percentage), false otherwise.
	 */
	public static boolean isSizeOrNumberSACUnit(LexicalUnit unit) {
		LexicalType type = unit.getLexicalUnitType();
		short cssUnit;
		if (type == LexicalType.FUNCTION || type == LexicalType.VAR || type == LexicalType.ATTR) {
			cssUnit = functionDimensionArgumentUnit(unit);
		} else if (type == LexicalType.INTEGER || type == LexicalType.REAL) {
			return true;
		} else {
			cssUnit = unit.getCssUnit();
		}
		switch (cssUnit) {
		case CSSUnit.CSS_PERCENTAGE:
		case CSSUnit.CSS_PX:
		case CSSUnit.CSS_PT:
		case CSSUnit.CSS_EM:
		case CSSUnit.CSS_EX:
		case CSSUnit.CSS_PC:
		case CSSUnit.CSS_CM:
		case CSSUnit.CSS_OTHER:
		case CSSUnit.CSS_IN:
		case CSSUnit.CSS_MM:
		case CSSUnit.CSS_CAP:
		case CSSUnit.CSS_CH:
		case CSSUnit.CSS_IC:
		case CSSUnit.CSS_LH:
		case CSSUnit.CSS_QUARTER_MM:
		case CSSUnit.CSS_REM:
		case CSSUnit.CSS_RLH:
		case CSSUnit.CSS_VB:
		case CSSUnit.CSS_VH:
		case CSSUnit.CSS_VI:
		case CSSUnit.CSS_VMAX:
		case CSSUnit.CSS_VMIN:
		case CSSUnit.CSS_VW:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given NSAC unit type is a plain number (real or integer) or a percentage.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is a plain number or a percentage, <code>false</code> otherwise.
	 */
	public static boolean isPlainNumberOrPercentSACUnit(LexicalUnit unit) {
		LexicalType type = unit.getLexicalUnitType();
		switch (type) {
		case PERCENTAGE:
		case INTEGER:
		case REAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given NSAC unit type is an angle unit.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is an angle type, <code>false</code> otherwise.
	 */
	public static boolean isAngleSACUnit(LexicalUnit unit) {
		LexicalType type = unit.getLexicalUnitType();
		short cssunit;
		if (type == LexicalType.FUNCTION || type == LexicalType.VAR || type == LexicalType.ATTR) {
			if (isColorFunction(unit)) {
				return false;
			}
			unit = firstDimensionArgument(unit);
			return unit != null && isAngleSACUnit(unit);
		} else if (type == LexicalType.INTEGER) {
			return unit.getIntegerValue() == 0;
		} else {
			cssunit = unit.getCssUnit();
		}
		switch (cssunit) {
		case CSSUnit.CSS_DEG:
		case CSSUnit.CSS_RAD:
		case CSSUnit.CSS_GRAD:
		case CSSUnit.CSS_TURN:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tests whether the given NSAC unit type is a time unit.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is a time type, <code>false</code> otherwise.
	 */
	public static boolean isTimeSACUnit(LexicalUnit unit) {
		LexicalType type = unit.getLexicalUnitType();
		short cssunit;
		if (type == LexicalType.FUNCTION || type == LexicalType.VAR || type == LexicalType.ATTR) {
			unit = firstDimensionArgument(unit);
			return unit != null && isTimeSACUnit(unit);
		} else if (type == LexicalType.INTEGER) {
			return unit.getIntegerValue() == 0;
		} else {
			cssunit = unit.getCssUnit();
		}
		switch (cssunit) {
		case CSSUnit.CSS_MS:
		case CSSUnit.CSS_S:
			return true;
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
			if (type != CSSUnit.CSS_INVALID) {
				return type;
			}
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
			if (type != CSSUnit.CSS_INVALID) {
				return type;
			}
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				if (lu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
					lu = lu.getNextLexicalUnit();
				}
			}
		}
		return -1;
	}

	/**
	 * If the supplied value represents a function, get the first argument that has
	 * an explicit dimension.
	 * 
	 * @param lunit the lexical value.
	 * @return the unit type of the first dimension argument, null if the value has
	 *         no dimension arguments.
	 */
	private static LexicalUnit firstDimensionArgument(LexicalUnit lunit) {
		LexicalUnit lu = lunit.getParameters();
		while (lu != null) {
			LexicalType sacType = lu.getLexicalUnitType();
			if (sacType == LexicalType.DIMENSION) {
				return lu;
			}
			lu = lu.getNextLexicalUnit();
		}
		return null;
	}

	private static boolean isColorFunction(LexicalUnit lunit) {
		String name = lunit.getFunctionName().toLowerCase(Locale.ROOT);
		return "hwb".equals(name) || name.endsWith("-gradient");
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
		return parseProperty(value, new CSSOMParser());
	}

	/**
	 * Parses a property value with the supplied parser.
	 * 
	 * @param value
	 *            the string containing the property value.
	 * @param parser
	 *            the NSAC parser.
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
	 *            the NSAC parser.
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
	 *            the NSAC parser.
	 * @return the CSSValue object containing the parsed value.
	 * @throws DOMException
	 *             if a problem was found parsing the property.
	 */
	public StyleValue parseProperty(String propertyName, String value, Parser parser) throws DOMException {
		Reader re = new StringReader(value);
		LexicalUnit lunit = null;
		try {
			lunit = parser.parsePropertyValue(re);
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
		case OPERATOR_COMMA:
		case OPERATOR_EXP:
		case OPERATOR_GE:
		case OPERATOR_GT:
		case OPERATOR_LE:
		case OPERATOR_LT:
		case OPERATOR_MINUS:
		case OPERATOR_MULTIPLY:
		case OPERATOR_PLUS:
		case OPERATOR_SLASH:
		case OPERATOR_TILDE:
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
	 * @return the PrimitiveValue object containing the parsed value.
	 * @throws DOMException if a problem was found parsing the feature.
	 */
	public PrimitiveValue parseMediaFeature(String feature) throws DOMException {
		return parseMediaFeature(feature, new CSSOMParser());
	}

	/**
	 * Parses a feature value.
	 * <p>
	 * 
	 * @param feature the string containing the feature value.
	 * @param parser the parser used to parse values.
	 * @return the PrimitiveValue object containing the parsed value.
	 * @throws DOMException if a problem was found parsing the feature.
	 */
	public PrimitiveValue parseMediaFeature(String feature, Parser parser) throws DOMException {
		Reader re = new StringReader(feature);
		LexicalUnit lunit = null;
		try {
			lunit = parser.parsePropertyValue(re);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		LexicalSetter item = createCSSPrimitiveValueItem(lunit, true, false);
		if (item.getNextLexicalUnit() != null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad feature: " + feature);
		}
		return item.getCSSValue();
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
			try {
				LexicalUnit nlu = lunit;
				do {
					StyleValue value;
					// Check for bracket list.
					if (nlu.getLexicalUnitType() != LexicalType.LEFT_BRACKET) {
						ValueItem item = createCSSValueItem(nlu, false);
						if (item.hasWarnings() && style != null) {
							StyleDeclarationErrorHandler errHandler = style.getStyleDeclarationErrorHandler();
							if (errHandler != null) {
								item.handleSyntaxWarnings(errHandler);
							}
						}
						value = item.getCSSValue();
						CssType cat = value.getCssValueType();
						if (cat == CssType.TYPED || cat == CssType.PROXY) {
							// Caution for ratio values.
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
						if (listitem != null) {
							value = listitem.getCSSValue();
							nlu = listitem.getNextLexicalUnit();
						} else {
							nlu = nlu.getNextLexicalUnit();
							continue;
						}
					}
					if (nlu != null) {
						if (nlu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
					if (value.getCssValueType() == CssType.LIST) {
						return listOrFirstItem((ValueList) value);
					}
					return value;
				}
			} catch (CSSLexicalProcessingException e) {
				// Contains a var() that should be processed as a lexical value.
				LexicalSetter item = new LexicalValue().newLexicalSetter();
				item.setLexicalUnit(lunit);
				return item.getCSSValue();
			}
			return listOrFirstItem(list);
		} else {
			ValueItem item = createCSSValueItem(lunit, false);
			if (item.hasWarnings() && style != null) {
				StyleDeclarationErrorHandler errHandler = style.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					item.handleSyntaxWarnings(errHandler);
				}
			}
			return item.getCSSValue();
		}
	}

	/**
	 * Parse a bracket list.
	 * 
	 * @param nlu         the lexical unit containing the first item in the bracket
	 *                    list.
	 * @param style       the style declaration to report issues to.
	 * @param subproperty <code>true</code> if the value must be a subproperty.
	 * @return the bracket list, or <code>null</code> if the list was empty.
	 */
	public ListValueItem parseBracketList(LexicalUnit nlu, AbstractCSSStyleDeclaration style, boolean subproperty) {
		ListValueItem listitem = new ListValueItem();
		listitem.list = ValueList.createBracketValueList();
		try {
			while (nlu.getLexicalUnitType() != LexicalType.RIGHT_BRACKET) {
				ValueItem item = createCSSValueItem(nlu, subproperty);
				if (item.hasWarnings() && style != null) {
					StyleDeclarationErrorHandler errHandler = style.getStyleDeclarationErrorHandler();
					if (errHandler != null) {
						item.handleSyntaxWarnings(errHandler);
					}
				}
				StyleValue value = item.getCSSValue();
				if (value.getCssValueType() == CssType.TYPED) {
					nlu = item.getNextLexicalUnit();
				} else {
					nlu = nlu.getNextLexicalUnit();
				}
				listitem.list.add(value);
				if (nlu != null) {
					if (nlu.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
			if (listitem.list.getLength() != 0) {
				listitem.nextLexicalUnit = nlu.getNextLexicalUnit();
			} else {
				listitem = null;
			}
		} catch (CSSLexicalProcessingException e) {
			// Contains a var() that should be processed as a lexical value.
			listitem.list.clear();
			LexicalSetter item = new LexicalValue().newLexicalSetter();
			item.setLexicalUnit(nlu);
			listitem.list.add(item.getCSSValue());
			listitem.nextLexicalUnit = null;
		}
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
		case INHERIT:
			KeywordValue value = InheritValue.getValue();
			if (subproperty)
				value = value.asSubproperty();
			return value;
		case UNSET:
			value = UnsetValue.getValue();
			if (subproperty)
				value = value.asSubproperty();
			return value;
		case REVERT:
			value = RevertValue.getValue();
			if (subproperty)
				value = value.asSubproperty();
			return value;
		case INITIAL:
			value = InitialValue.getValue();
			if (subproperty)
				value = value.asSubproperty();
			return value;
		default:
			try {
				return createCSSPrimitiveValueItem(lunit, !subproperty, subproperty);
			} catch (CSSLexicalProcessingException e) {
				// Contains a var() that should be processed as a lexical value.
				if (!isNotListLexicalUnit(lunit)) {
					throw e;
				}
				LexicalSetter item = new LexicalValue().newLexicalSetter();
				item.setLexicalUnit(lunit);
				return item;
			}
		}
	}

	/**
	 * Creates a PrimitiveValue according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return null.
	 * </p>
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @param subp
	 *            the flag marking whether it is a sub-property.
	 * @return the PrimitiveValue for the CSS primitive value.
	 * @throws DOMException
	 *             if a problem was found setting the lexical value to a CSS
	 *             primitive.
	 */
	PrimitiveValue createCSSPrimitiveValue(LexicalUnit lunit, boolean subp) throws DOMException {
		return createCSSPrimitiveValueItem(lunit, !subp, subp).getCSSValue();
	}

	/**
	 * Creates a LexicalSetter according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return null.
	 * </p>
	 * 
	 * @param lunit
	 *            the lexical value.
	 * @param ratioContext
	 *            {@code true} if we are in a context where ratio values could be expected.
	 * @param subp
	 *            the flag marking whether it is a sub-property.
	 * @return the LexicalSetter for the CSS primitive value.
	 * @throws DOMException
	 *             if a problem was found setting the lexical value to a CSS primitive.
	 */
	LexicalSetter createCSSPrimitiveValueItem(LexicalUnit lunit, boolean ratioContext, boolean subp)
			throws DOMException {
		LexicalType unitType = lunit.getLexicalUnitType();
		PrimitiveValue primi;
		LexicalSetter setter;
		try {
			typeLoop: switch (unitType) {
			case IDENT:
				primi = new IdentifierValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case STRING:
				primi = new StringValue(flags);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case URI:
				primi = new URIValue(flags);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case PERCENTAGE:
				primi = new PercentageValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case DIMENSION:
				switch (lunit.getCssUnit()) {
				case CSSUnit.CSS_CM:
				case CSSUnit.CSS_EM:
				case CSSUnit.CSS_EX:
				case CSSUnit.CSS_IN:
				case CSSUnit.CSS_MM:
				case CSSUnit.CSS_PC:
				case CSSUnit.CSS_PX:
				case CSSUnit.CSS_PT:
				case CSSUnit.CSS_CAP:
				case CSSUnit.CSS_CH:
				case CSSUnit.CSS_IC:
				case CSSUnit.CSS_LH:
				case CSSUnit.CSS_QUARTER_MM:
				case CSSUnit.CSS_REM:
				case CSSUnit.CSS_RLH:
				case CSSUnit.CSS_VB:
				case CSSUnit.CSS_VH:
				case CSSUnit.CSS_VI:
				case CSSUnit.CSS_VMAX:
				case CSSUnit.CSS_VMIN:
				case CSSUnit.CSS_VW:
					primi = new NumberValue();
					(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
					((NumberValue) primi).lengthUnitType = true;
					break typeLoop;
				default:
					primi = new NumberValue();
					(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
					break typeLoop;
				}
			case REAL:
				primi = new NumberValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				if (ratioContext) {
					// Check for ratio
					return checkForRatio(setter, subp);
				}
				break;
			case INTEGER:
				primi = new NumberValue();
				((NumberValue) primi).setIntegerValue(lunit.getIntegerValue());
				(setter = primi.newLexicalSetter()).nextLexicalUnit = lunit.getNextLexicalUnit();
				if (ratioContext) {
					// Check for ratio
					return checkForRatio(setter, subp);
				}
				break;
			case RGBCOLOR:
				primi = new RGBColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case HSLCOLOR:
				primi = new HSLColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case FUNCTION:
				String func = lunit.getFunctionName().toLowerCase(Locale.ROOT);
				if ("hwb".equals(func)) {
					primi = new HWBColorValue();
				} else if ("calc".equals(func)) {
					primi = new CalcValue();
					setter = primi.newLexicalSetter();
					setter.setLexicalUnit(lunit);
					if (ratioContext) {
						// Check for ratio
						return checkForRatio(setter, subp);
					}
					break;
				} else if (func.endsWith("linear-gradient") || func.endsWith("radial-gradient")
						|| func.endsWith("conic-gradient")) {
					primi = new GradientValue();
					setter = primi.newLexicalSetter();
					try {
						setter.setLexicalUnit(lunit);
					} catch (CSSLexicalProcessingException e) {
						// Contains a var() that should be processed as a lexical value.
						if (!isNotListLexicalUnit(lunit)) {
							throw e;
						}
						LexicalSetter item = new LexicalValue().newLexicalSetter();
						item.setLexicalUnit(lunit);
						return item;
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
				} else if ("env".equals(func)) {
					primi = new EnvVariableValue();
				} else {
					primi = new FunctionValue();
				}
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case VAR:
				if (!isIsolatedLexicalUnit(lunit)) {
					throw new CSSLexicalProcessingException("var() found.");
				}
				primi = new VarValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case ATTR:
				primi = new AttrValue(flags);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case UNICODE_RANGE:
				primi = new UnicodeRangeValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case UNICODE_WILDCARD:
				primi = new UnicodeWildcardValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case RECT_FUNCTION:
				primi = new RectValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case COUNTER_FUNCTION:
				primi = new CounterValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case COUNTERS_FUNCTION:
				primi = new CountersValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case ELEMENT_REFERENCE:
				primi = new ElementReferenceValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case OPERATOR_COMMA:
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

	private boolean isIsolatedLexicalUnit(LexicalUnit lunit) {
		return !lunit.isParameter() && lunit.getNextLexicalUnit() == null
				&& lunit.getPreviousLexicalUnit() == null;
	}

	private boolean isNotListLexicalUnit(LexicalUnit lunit) {
		return lunit.getNextLexicalUnit() == null
				&& lunit.getPreviousLexicalUnit() == null;
	}

	private LexicalSetter checkForRatio(LexicalSetter setter, boolean subp) throws DOMException {
		LexicalUnit nlu = setter.getNextLexicalUnit();
		if (nlu != null && nlu.getLexicalUnitType() == LexicalType.OPERATOR_SLASH) {
			nlu = nlu.getNextLexicalUnit();
			if (nlu != null) {
				LexicalSetter consec = createCSSPrimitiveValueItem(nlu, false, false);
				RatioValue ratio = new RatioValue();
				try {
					ratio.setAntecedentValue(setter.getCSSValue());
					ratio.setConsequentValue(consec.getCSSValue());
					setter = ratio.newLexicalSetter();
					setter.setLexicalUnit(consec.getNextLexicalUnit());
					ratio.setSubproperty(subp);
				} catch (DOMException e) {
					if (e.code == DOMException.INVALID_ACCESS_ERR) {
						throw e;
					}
				}
			} else {
				throw new DOMException(DOMException.SYNTAX_ERR, "Invalid ratio.");
			}
		}
		return setter;
	}

	public LexicalUnit appendValueString(StringBuilder buf, LexicalUnit lunit) {
		LexicalUnit nlu;
		LexicalType type = lunit.getLexicalUnitType();
		if (type == LexicalType.OPERATOR_COMMA) {
			nlu = lunit.getNextLexicalUnit();
			buf.append(',');
		} else if (type == LexicalType.OPERATOR_SLASH) {
			nlu = lunit.getNextLexicalUnit();
			buf.append(' ').append('/');
		} else if (type != LexicalType.LEFT_BRACKET) {
			ValueItem item = createCSSValueItem(lunit, true);
			nlu = item.getNextLexicalUnit();
			StyleValue cssValue = item.getCSSValue();
			String cssText = cssValue.getCssText();
			if (buf.length() != 0) {
				buf.append(' ');
			}
			buf.append(cssText);
		} else {
			nlu = lunit.getNextLexicalUnit();
			ListValueItem item = parseBracketList(nlu, null, false);
			if (item != null) {
				nlu = item.getNextLexicalUnit();
				buf.append(item.getCSSValue().getCssText());
			} else {
				nlu = nlu.getNextLexicalUnit();
			}
		}
		return nlu;
	}

	public LexicalUnit appendMinifiedValueString(StringBuilder buf, LexicalUnit lunit) {
		LexicalUnit nlu;
		LexicalType type = lunit.getLexicalUnitType();
		if (type == LexicalType.OPERATOR_COMMA) {
			nlu = lunit.getNextLexicalUnit();
			buf.append(',');
		} else if (type == LexicalType.OPERATOR_SLASH) {
			nlu = lunit.getNextLexicalUnit();
			buf.append('/');
		} else if (type != LexicalType.LEFT_BRACKET) {
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
			nlu = lunit.getNextLexicalUnit();
			ListValueItem item = parseBracketList(nlu, null, false);
			if (item != null) {
				nlu = item.getNextLexicalUnit();
				buf.append(item.getCSSValue().getMinifiedCssText(""));
			} else {
				nlu = nlu.getNextLexicalUnit();
			}
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

}
