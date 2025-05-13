/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueFactory;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.PrimitiveValue.LexicalSetter;

/**
 * Factory of CSS values.
 * 
 * @author Carlos Amengual
 * 
 */
public class ValueFactory implements CSSValueFactory {

	private final short flags;

	public ValueFactory() {
		this((short)0);
	}

	public ValueFactory(short flags) {
		super();
		this.flags = flags;
	}

	/**
	 * Tests whether the unit type of the given NSAC lexical unit can apply to size
	 * (e.g. block size).
	 * 
	 * @param unit the NSAC lexical unit.
	 * @return <code>true</code> if it is a size type, false otherwise.
	 * @deprecated Use {@link #isLengthPercentageSACUnit(LexicalUnit)} instead.
	 */
	@Deprecated
	public static boolean isSizeSACUnit(LexicalUnit unit) {
		try {
			return isLengthPercentageSACUnit(unit);
		} catch (CSSLexicalProcessingException e) {
			return false;
		}
	}

	/**
	 * Test whether the value represents a non-negative length.
	 * 
	 * @param lunit the lexical unit to test.
	 * @return <code>true</code> if the value is a length in the [0-&#x221E;]
	 *         interval.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isLengthSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		switch (lunit.getLexicalUnitType()) {
		case DIMENSION:
			short unit = lunit.getCssUnit();
			if (!CSSUnit.isLengthUnitType(unit) && unit != CSSUnit.CSS_OTHER) {
				return false;
			}
			return lunit.getFloatValue() >= 0f;
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
		case ENV:
			CSSValueSyntax syntax = SyntaxParser.createSimpleSyntax("length");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		case INTEGER:
			return lunit.getIntegerValue() == 0;
		case REAL:
			return lunit.getFloatValue() == 0f;
		default:
		}

		return false;
	}

	/**
	 * Test whether the value represents a length or a percentage.
	 * <p>
	 * The value can be positive or negative.
	 * </p>
	 * 
	 * @param lunit the lexical unit to test.
	 * @return <code>true</code> if the value is a length or a percentage.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isLengthPercentageSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		short unit = lunit.getCssUnit();
		if (CSSUnit.isLengthUnitType(unit) || unit == CSSUnit.CSS_PERCENTAGE) {
			return true;
		}

		switch (lunit.getLexicalUnitType()) {
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
		case ENV:
			CSSValueSyntax syntax = SyntaxParser.createSimpleSyntax("length-percentage");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		case INTEGER:
			return lunit.getIntegerValue() == 0;
		case REAL:
			return lunit.getFloatValue() == 0f;
		default:
		}

		return false;
	}

	/**
	 * Tests whether the given NSAC value represents a length (including unknown
	 * units) or percentage greater or equal to zero (e.g. {@code font-size} or
	 * {@code column-width}).
	 * 
	 * @param lunit the lexical value.
	 * @return <code>true</code> if it is a non-negative {@code <length-percentage>}
	 *         type, <code>false</code> otherwise.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isPositiveSizeSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		switch (lunit.getLexicalUnitType()) {
		case DIMENSION:
			short unit = lunit.getCssUnit();
			if (!CSSUnit.isLengthUnitType(unit) && unit != CSSUnit.CSS_OTHER) {
				return false;
			}
		case PERCENTAGE:
			return lunit.getFloatValue() >= 0f;
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
		case ENV:
			CSSValueSyntax syntax = SyntaxParser.createSimpleSyntax("length-percentage");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		case INTEGER:
			return lunit.getIntegerValue() == 0;
		case REAL:
			return lunit.getFloatValue() == 0f;
		default:
		}

		return false;
	}

	/**
	 * Tests whether the given NSAC unit type is a non-negative {@code <length>} or
	 * {@code <number>} unit (or a unknown unit).
	 * 
	 * @param lunit the lexical value.
	 * @return <code>true</code> if it is a size or numeric type (including
	 *         percentage) in the [0-&#x221E;] interval, <code>false</code>
	 *         otherwise.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isLengthOrNumberSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		switch (lunit.getLexicalUnitType()) {
		case INTEGER:
			return lunit.getIntegerValue() >= 0;
		case DIMENSION:
			short unit = lunit.getCssUnit();
			if (!CSSUnit.isLengthUnitType(unit) && unit != CSSUnit.CSS_OTHER) {
				return false;
			}
		case REAL:
			return lunit.getFloatValue() >= 0f;
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
			CSSValueSyntax syntax = new SyntaxParser().parseSyntax("<length> | <number>");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		default:
		}

		return false;
	}

	/**
	 * Tests whether the given NSAC unit type is a non-negative
	 * {@code <length-percentage>} or {@code <number>} unit (or a unknown unit).
	 * 
	 * @param lunit the lexical value.
	 * @return <code>true</code> if it is a size or numeric type (including
	 *         percentage) in the [0-&#x221E;] interval, <code>false</code>
	 *         otherwise.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isSizeOrNumberSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		switch (lunit.getLexicalUnitType()) {
		case INTEGER:
			return lunit.getIntegerValue() >= 0;
		case DIMENSION:
			short unit = lunit.getCssUnit();
			if (!CSSUnit.isLengthUnitType(unit) && unit != CSSUnit.CSS_OTHER) {
				return false;
			}
		case REAL:
		case PERCENTAGE:
			return lunit.getFloatValue() >= 0f;
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
			CSSValueSyntax syntax = new SyntaxParser().parseSyntax("<length-percentage> | <number>");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		default:
		}

		return false;
	}

	/**
	 * Tests whether the given NSAC unit type is a non-negative number (real or
	 * integer) or a percentage.
	 * <p>
	 * Useful to check for {@code border-image-slice}.
	 * </p>
	 * 
	 * @param lunit the lexical value.
	 * @return <code>true</code> if is a number or a percentage in the [0-&#x221E;]
	 *         interval, <code>false</code> otherwise.
	 */
	public static boolean isPercentageOrNumberSACUnit(LexicalUnit lunit) {
		switch (lunit.getLexicalUnitType()) {
		case INTEGER:
			return lunit.getIntegerValue() >= 0;
		case REAL:
		case PERCENTAGE:
			return lunit.getFloatValue() >= 0f;
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
			CSSValueSyntax syntax = new SyntaxParser().parseSyntax("<percentage> | <number>");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		default:
		}

		return false;
	}

	/**
	 * Tests whether the unit type of the given NSAC lexical unit is a resolution unit.
	 * 
	 * @param lunit
	 *            the NSAC lexical unit value.
	 * @return <code>true</code> if it is a resolution type, <code>false</code> otherwise.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isResolutionSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		if (CSSUnit.isResolutionUnitType(lunit.getCssUnit())) {
			return true;
		}

		switch (lunit.getLexicalUnitType()) {
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
			CSSValueSyntax syntax = SyntaxParser.createSimpleSyntax("resolution");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		default:
		}

		return false;
	}

	/**
	 * Tests whether the given NSAC unit type is an angle or percentage unit (or
	 * zero, which is to be interpreted as length 0).
	 * <p>
	 * This is useful for checking color stops in gradients.
	 * </p>
	 * 
	 * @param unit the lexical value.
	 * @return <code>true</code> if is an angle or percentage type,
	 *         <code>false</code> otherwise.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isAngleOrPercentageSACUnit(LexicalUnit unit)
			throws CSSLexicalProcessingException {
		LexicalType type;
		if (CSSUnit.isAngleUnitType(unit.getCssUnit())
				|| (type = unit.getLexicalUnitType()) == LexicalType.PERCENTAGE
				|| (type == LexicalType.INTEGER && unit.getIntegerValue() == 0)) {
			return true;
		}

		switch (type) {
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
			CSSValueSyntax syntax = new SyntaxParser().parseSyntax("<angle> | <percentage>");
			switch (unit.shallowMatch(syntax)) {
			case TRUE:
				ValueFactory.throwOnProxy(unit.getParameters());
				return true;
			case FALSE:
				return false;
			default:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		default:
		}

		return false;
	}

	/**
	 * Tests whether the given NSAC unit type is a time unit.
	 * 
	 * @param unit
	 *            the lexical value.
	 * @return <code>true</code> if is a time type, <code>false</code> otherwise.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isTimeSACUnit(LexicalUnit unit)
			throws CSSLexicalProcessingException {
		if (CSSUnit.isTimeUnitType(unit.getCssUnit())) {
			return true;
		}

		switch (unit.getLexicalUnitType()) {
		case CALC:
		case MATH_FUNCTION:
		case FUNCTION: // We check FUNCTION in case we got -webkit-calc() or similar
		case SUB_EXPRESSION:
			CSSValueSyntax syntax = SyntaxParser.createSimpleSyntax("time");
			switch (unit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(unit.getParameters());
				return true;
			case FALSE:
				return false;
			default:
			}
		case VAR:
		case ATTR:
			throw new CSSLexicalProcessingException("PROXY value found.");
		default:
		}

		return false;
	}

	/**
	 * Test whether the value represents a color.
	 * 
	 * @param lunit the lexical unit to test.
	 * @return true if the value is a color.
	 * @throws CSSLexicalProcessingException if a {@code PROXY} value was found.
	 */
	public static boolean isColorSACUnit(LexicalUnit lunit)
			throws CSSLexicalProcessingException {
		LexicalType type = lunit.getLexicalUnitType();
		// ATTR may match <color>, so we check for it first
		if (type != LexicalType.ATTR) {
			CSSValueSyntax syntax = SyntaxParser.createSimpleSyntax("color");
			switch (lunit.shallowMatch(syntax)) {
			case TRUE:
				throwOnProxy(lunit.getParameters());
				return true;
			case FALSE:
				return false;
			case PENDING:
			}
		}

		throw new CSSLexicalProcessingException("PROXY value found.");
	}

	static void throwOnProxy(LexicalUnit param) throws CSSLexicalProcessingException {
		while (param != null) {
			LexicalType type = param.getLexicalUnitType();
			if (type == LexicalType.VAR || type == LexicalType.ATTR) {
				throw new CSSLexicalProcessingException("PROXY value found.");
			}
			throwOnProxy(param.getParameters());
			param = param.getNextLexicalUnit();
		}
	}

	/**
	 * Create a default parser adequate for parsing properties.
	 * 
	 * @return a parser.
	 */
	protected Parser createParser() {
		return new CSSOMParser();
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
		return parseProperty(value, createParser());
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
		UnknownValue css;
		if (!isOperatorType(lunit.getLexicalUnitType())) {
			css = new UnknownValue();
			css.setCssText(value);
			css.newLexicalSetter().setLexicalUnit(lunit);
		} else {
			css = null;
		}
		return css;
	}

	private boolean isOperatorType(LexicalType luType) {
		switch (luType) {
		case OPERATOR_COMMA:
		case OPERATOR_EXP:
		case OPERATOR_GE:
		case OPERATOR_GT:
		case OPERATOR_LE:
		case OPERATOR_LT:
		case OPERATOR_MINUS:
		case OPERATOR_MULTIPLY:
		case OPERATOR_PLUS:
		case OPERATOR_SEMICOLON:
		case OPERATOR_SLASH:
		case OPERATOR_TILDE:
			return true;
		default:
			return false;
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
		return parseMediaFeature(feature, createMediaFeatureParser());
	}

	/**
	 * Create a parser adequate for parsing media features.
	 * 
	 * @return a parser.
	 */
	protected Parser createMediaFeatureParser() {
		return new CSSOMParser();
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
	 * The value is assumed to be stand-alone, independent.
	 * 
	 * @param lunit the lexical value.
	 * @return a {@code CSSValue} associated to the given lexical value, or
	 *         {@code null} if a bracket list was empty.
	 * @throws DOMException if the lexical unit had a wrong content to create a
	 *                      value.
	 */
	@Override
	public StyleValue createCSSValue(LexicalUnit lunit)
			throws DOMException {
		return createCSSValue(lunit, null);
	}

	/**
	 * Creates a CSSValue according to the given lexical value.
	 * <p>
	 * The value is assumed to be stand-alone, independent.
	 * 
	 * @param lunit the lexical value.
	 * @param style the (style) declaration that should handle errors, or
	 *              {@code null} to not handle errors.
	 * @return a {@code CSSValue} associated to the given lexical value, or
	 *         {@code null} if a bracket list was empty.
	 * @throws DOMException if the lexical unit had a wrong content to create a
	 *                      value.
	 */
	public StyleValue createCSSValue(LexicalUnit lunit, AbstractCSSStyleDeclaration style)
			throws DOMException {
		return createCSSValue(lunit, style, false);
	}

	/**
	 * Creates a CSSValue according to the given lexical value.
	 * 
	 * @param lunit       the lexical value.
	 * @param style       the (style) declaration that should handle errors, or
	 *                    {@code null} to not handle errors.
	 * @param subproperty the flag marking whether it is a sub-property.
	 * @return a {@code CSSValue} associated to the given lexical value, or
	 *         {@code null} if a bracket list was empty.
	 * @throws DOMException if the lexical unit had a wrong content to create a
	 *                      value.
	 */
	public StyleValue createCSSValue(LexicalUnit lunit, AbstractCSSStyleDeclaration style,
			boolean subproperty) throws DOMException {
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
						ValueItem item = createCSSValueItem(nlu, subproperty);
						if (item.hasWarnings() && style != null) {
							StyleDeclarationErrorHandler errHandler = style
									.getStyleDeclarationErrorHandler();
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
						ListValueItem listitem = parseBracketList(nlu, style, subproperty);
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
				// Contains a proxy that should be processed as a lexical value.
				LexicalSetter item = new LexicalValue().newLexicalSetter();
				item.setLexicalUnit(lunit);
				return item.getCSSValue();
			}
			return listOrFirstItem(list);
		} else {
			ValueItem item = createCSSValueItem(lunit, subproperty);
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
				if (!isIsolatedLexicalUnit(lunit)) {
					throw e;
				}
				LexicalSetter item = new LexicalValue().newLexicalSetter();
				item.setLexicalUnit(lunit);
				return item;
			}
		}
	}

	/**
	 * Creates a primitive value according to the given lexical value.
	 * <p>
	 * This method won't return a ratio value (callers must check for values
	 * spanning more than one lexical unit).
	 * </p>
	 * <p>
	 * If the lexical unit is a slash operator and is in {@code content} context,
	 * returns a {@code LexicalValue}.
	 * </p>
	 * <p>
	 * If the lexical unit is an operator and a parameter, returns an
	 * {@code UnknownValue}.
	 * </p>
	 * 
	 * @param lunit the lexical value.
	 * @return the primitive value.
	 * @throws CSSLexicalProcessingException if this value is part of a larger
	 *                                       lexical chain that should be handled as
	 *                                       a lexical value.
	 * @throws DOMException                  if the lexical unit does not represent
	 *                                       a valid primitive.
	 */
	@Override
	public PrimitiveValue createCSSPrimitiveValue(LexicalUnit lunit)
			throws DOMException {
		PrimitiveValue value;
		try {
			value = createCSSPrimitiveValueItem(lunit, false, true).getCSSValue();
		} catch (CSSLexicalProcessingException e) {
			if (!isIsolatedLexicalUnit(lunit)) {
				throw e;
			}
			LexicalSetter item = new LexicalValue().newLexicalSetter();
			item.setLexicalUnit(lunit);
			value = item.getCSSValue();
		}
		return value;
	}

	/**
	 * Creates a PrimitiveValue according to the given lexical value.
	 * <p>
	 * This method either returns a value or throws an exception, but cannot return null.
	 * </p>
	 * <p>
	 * If the lexical unit is a slash operator and is in {@code content} context,
	 * returns a {@code LexicalValue}.
	 * </p>
	 * <p>
	 * If the lexical unit is an operator and a parameter, returns an
	 * {@code UnknownValue}.
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
	 * This method either returns a value or throws an exception, but cannot return
	 * null.
	 * </p>
	 * <p>
	 * If the lexical unit is a slash operator and is in {@code content} context,
	 * returns a {@code LexicalValue}.
	 * </p>
	 * <p>
	 * If the lexical unit is an operator and a parameter, returns an
	 * {@code UnknownValue}.
	 * </p>
	 * 
	 * @param lunit        the lexical value.
	 * @param ratioContext {@code true} if we are in a context where ratio values
	 *                     could be expected.
	 * @param subp         the flag marking whether it is a sub-property.
	 * @return the LexicalSetter for the CSS primitive value.
	 * @throws DOMException if a problem was found setting the lexical value to a
	 *                      CSS primitive.
	 */
	LexicalSetter createCSSPrimitiveValueItem(LexicalUnit lunit, boolean ratioContext, boolean subp)
			throws CSSLexicalProcessingException, DOMException {
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
				setter = primi.newLexicalSetter();
				setter.setLexicalUnit(lunit);
				break;
			case PERCENTAGE:
				primi = new PercentageValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case DIMENSION:
				primi = new NumberValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				if (CSSUnit.isLengthUnitType(lunit.getCssUnit())) {
					((NumberValue) primi).lengthUnitType = true;
				}
				break typeLoop;
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
			case LABCOLOR:
				primi = new LABColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LCHCOLOR:
				primi = new LCHColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case OKLABCOLOR:
				primi = new OKLABColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case OKLCHCOLOR:
				primi = new OKLCHColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case HWBCOLOR:
				primi = new HWBColorValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case CALC:
				primi = new CalcValue();
				setter = primi.newLexicalSetter();
				setter.setLexicalUnit(lunit);
				if (ratioContext) {
					// Check for ratio
					return checkForRatio(setter, subp);
				}
				break;
			case COLOR_FUNCTION:
				primi = new ColorFunction();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case MATH_FUNCTION:
				primi = new MathFunctionValue(lunit.getMathFunction());
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case COLOR_MIX:
				primi = new ColorMixFunction();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case VAR:
			case ATTR:
				if (!isIsolatedLexicalUnit(lunit)) {
					throw new CSSLexicalProcessingException(unitType + " found.");
				}
			case EMPTY:
				primi = new LexicalValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case ENV:
				primi = new EnvVariableValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case GRADIENT:
				String func = lunit.getFunctionName();
				if (func.endsWith("linear-gradient") || func.endsWith("radial-gradient")
						|| func.endsWith("conic-gradient")) {
					primi = new GradientValue();
					setter = primi.newLexicalSetter();
					setter.setLexicalUnit(lunit);
					break;
				}
				// pass-through
			case FUNCTION:
			case PREFIXED_FUNCTION:
				primi = new FunctionValue();
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
			case CUBIC_BEZIER_FUNCTION:
				primi = new CubicBezierFunction();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case LINEAR_FUNCTION:
				primi = new EasingFunction(Type.LINEAR);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case STEPS_FUNCTION:
				primi = new EasingFunction(Type.STEPS);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case SRC:
				primi = new FunctionValue(Type.SRC);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case CIRCLE_FUNCTION:
				primi = new ShapeFunction(Type.CIRCLE);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case ELLIPSE_FUNCTION:
				primi = new ShapeFunction(Type.ELLIPSE);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case INSET_FUNCTION:
				primi = new ShapeFunction(Type.INSET);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case PATH_FUNCTION:
				primi = new PathValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case POLYGON_FUNCTION:
				primi = new ShapeFunction(Type.POLYGON);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case SHAPE_FUNCTION:
				primi = new ShapeFunction(Type.SHAPE);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case XYWH_FUNCTION:
				primi = new ShapeFunction(Type.XYWH);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case TRANSFORM_FUNCTION:
				primi = new TransformFunction(lunit.getTransformFunction());
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case IMAGE_SET:
				primi = new FunctionValue(Type.IMAGE_SET);
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case ELEMENT_REFERENCE:
				primi = new ElementReferenceValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case UNICODE_RANGE:
				primi = new UnicodeRangeValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			// The next one is included for completeness, but won't happen in normal workflows
			case UNICODE_WILDCARD:
				primi = new UnicodeWildcardValue();
				(setter = primi.newLexicalSetter()).setLexicalUnit(lunit);
				break;
			case OPERATOR_COMMA:
			case OPERATOR_SEMICOLON:
				throw new DOMException(DOMException.SYNTAX_ERR,
					"A comma or semicolon is not a valid primitive");
			case INHERIT:
			case UNSET:
			case REVERT:
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						unitType + " keyword is not a primitive.");
			case OPERATOR_SLASH:
				if (isContentContext(lunit)) {
					primi = new LexicalValue();
					(setter = primi.newLexicalSetter()).setLexicalUnit(lunit.shallowClone());
					setter.nextLexicalUnit = lunit.getNextLexicalUnit();
					break;
				}
			case OPERATOR_PLUS:
			case OPERATOR_MINUS:
			case OPERATOR_MULTIPLY:
				if (!lunit.isParameter()) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"A '" + lunit.toString() + "' is not a valid primitive");
				}
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

		primi.setPrecedingComments(lunit.getPrecedingComments());
		primi.setTrailingComments(lunit.getTrailingComments());

		return setter;
	}

	private boolean isIsolatedLexicalUnit(LexicalUnit lunit) {
		return !lunit.isParameter() && lunit.getNextLexicalUnit() == null
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

	private boolean isContentContext(LexicalUnit lunit) {
		LexicalUnit prevu = lunit.getPreviousLexicalUnit();
		LexicalUnit nextu = lunit.getNextLexicalUnit();
		if (prevu != null && nextu != null && !lunit.isParameter()) {
			CSSValueSyntax synpre = (new SyntaxParser())
					.parseSyntax("<string> | <counter> | <image> | <custom-ident>");
			return prevu.shallowMatch(synpre) != Match.FALSE && nextu.shallowMatch(
					(new SyntaxParser()).parseSyntax("<string> | <counter>")) != Match.FALSE;
		}
		return false;
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
	public boolean hasFactoryFlag(short flag) {
		return (flags & flag) == flag;
	}

}
