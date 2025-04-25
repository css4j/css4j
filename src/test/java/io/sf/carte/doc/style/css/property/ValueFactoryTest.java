/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnicodeRangeValue.CSSUnicodeValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.CSSOMParser;

public class ValueFactoryTest {

	Parser parser;

	@BeforeEach
	public void setUp() {
		parser = new CSSOMParser();
	}

	@Test
	public void testCreateCSSValue() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("1, 2, 3, 4");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		ValueList list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertTrue(list.isCommaSeparated());

		lunit = parsePropertyValue("1 2 3 4");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("1 2 3 4", list.getCssText());

		lunit = parsePropertyValue("/*0*/1/*1*/2/*2*/3/*3*/4/*4*/");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("1 2 3 4", list.getCssText());
	}

	@Test
	public void testCreateCSSValueContentSlash() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("url(img.png) / \"New!\"");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		ValueList list = (ValueList) value;
		assertEquals(3, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("url('img.png') / \"New!\"", list.getCssText());
	}

	@Test
	public void testCreateCSSValueBracketList() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("[first header-start]");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		ValueList list = (ValueList) value;
		assertEquals(2, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("first", list.item(0).getCssText());
		assertEquals("header-start", list.item(1).getCssText());

		lunit = parsePropertyValue("[first header-start], [main-start],[]");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(2, list.getLength());
		assertTrue(list.isCommaSeparated());
		assertEquals(CssType.LIST, list.item(0).getCssValueType());
		ValueList bracketlist = (ValueList) list.item(0);
		assertEquals(2, bracketlist.getLength());
		assertFalse(bracketlist.isCommaSeparated());
		assertEquals("first", bracketlist.item(0).getCssText());
		assertEquals("header-start", bracketlist.item(1).getCssText());
		assertEquals("[first header-start]", bracketlist.getCssText());
		assertEquals(CssType.LIST, list.item(1).getCssValueType());
		bracketlist = (ValueList) list.item(1);
		assertEquals(1, bracketlist.getLength());
		assertFalse(bracketlist.isCommaSeparated());
		assertEquals("main-start", bracketlist.item(0).getCssText());
		assertEquals("[main-start]", bracketlist.getCssText());
	}

	@Test
	public void testCreateCSSValueKeyword() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("inherit");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(CSSValue.Type.INHERIT, value.getPrimitiveType());
		assertEquals("inherit", value.getCssText());

		lunit = parsePropertyValue("unset");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(CSSValue.Type.UNSET, value.getPrimitiveType());
		assertEquals("unset", value.getCssText());

		lunit = parsePropertyValue("revert");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(CSSValue.Type.REVERT, value.getPrimitiveType());
		assertEquals("revert", value.getCssText());

		lunit = parsePropertyValue("initial");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(CSSValue.Type.INITIAL, value.getPrimitiveType());
		assertEquals("initial", value.getCssText());
	}

	@Test
	public void testCreateCSSValueVar() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("var(--foo, 3px)");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.PROXY, value.getCssValueType());
		assertEquals(CSSValue.Type.LEXICAL, value.getPrimitiveType());
		assertEquals("var(--foo, 3px)", value.getCssText());
	}

	@Test
	public void testCreateCSSValueUnicodeRange() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("U+22-28");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.UNICODE_RANGE, value.getPrimitiveType());
		UnicodeRangeValue range = (UnicodeRangeValue) value;
		TypedValue begin = range.getValue();
		assertEquals(Type.UNICODE_CHARACTER, begin.getPrimitiveType());
		assertEquals(0x22, ((CSSUnicodeValue) begin).getCodePoint());
		TypedValue end = range.getEndValue();
		assertEquals(Type.UNICODE_CHARACTER, end.getPrimitiveType());
		assertEquals(0x28, ((CSSUnicodeValue) end).getCodePoint());

		lunit = parsePropertyValue("U+2??");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(Type.UNICODE_RANGE, value.getPrimitiveType());
		range = (UnicodeRangeValue) value;
		assertNull(range.getEndValue());
		begin = range.getValue();
		assertEquals(Type.UNICODE_WILDCARD, begin.getPrimitiveType());
		assertEquals("2??", begin.getStringValue());
	}

	@Test
	public void testCreateCSSValueWrongCalc() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("calc(3*2)");
		LexicalUnit param = lunit.getParameters();
		param = param.getNextLexicalUnit();
		param = param.getNextLexicalUnit();
		param.remove();
		// calc(3*)
		try {
			factory.createCSSValue(lunit);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testCreateCSSValueWrongCalc2() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("calc(3*2)");
		LexicalUnit param = lunit.getParameters();
		param.remove();
		// calc(*2)
		try {
			factory.createCSSValue(lunit);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testCreateCSSValueListMix() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		LexicalUnit lunit = parsePropertyValue("bold 14px \"Courier New\", Arial, sans-serif");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		ValueList list = (ValueList) value;
		assertEquals(3, list.getLength());
		assertTrue(list.isCommaSeparated());
	}

	@Test
	public void testCreateCSSPrimitiveValue() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();

		LexicalUnit lunit = parsePropertyValue("1");
		PrimitiveValue value = factory.createCSSPrimitiveValue(lunit);
		assertTrue(value.isPrimitiveValue());
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSUnit.CSS_NUMBER, value.getUnitType());
		assertEquals(1f, ((CSSTypedValue) value).getFloatValue(CSSUnit.CSS_NUMBER));

		LexicalUnit inherit = parsePropertyValue("inherit");
		DOMException ex = assertThrows(DOMException.class, () -> factory.createCSSPrimitiveValue(inherit));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);

		LexicalUnit unset = parsePropertyValue("unset");
		ex = assertThrows(DOMException.class, () -> factory.createCSSPrimitiveValue(unset));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);

		LexicalUnit revert = parsePropertyValue("revert");
		ex = assertThrows(DOMException.class, () -> factory.createCSSPrimitiveValue(revert));
		assertEquals(DOMException.TYPE_MISMATCH_ERR, ex.code);
	}

	@Test
	public void testIsLengthSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px)");
		assertTrue(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("0.0");
		assertTrue(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("-1");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("-0.1");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("0%");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("calc(300px - 2%)");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("calc((300px - 2vw)/3)");
		assertTrue(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("sqrt(2px*2em)");
		assertTrue(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("sqrt(pow(2px,3))");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("-webkit-calc(100px - 6rem)");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("rgba(0, 0, 0, 0)");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));

		lunit = parsePropertyValue("linear-gradient(yellow, blue 20%, #0f0)");
		assertFalse(ValueFactory.isLengthSACUnit(lunit));
	}

	@Test
	public void testIsLengthSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2em)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthSACUnit(lunit));
	}

	@Test
	public void testIsLengthSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(var(--foo,2em) + 20px)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthSACUnit(lunit));
	}

	@Test
	public void testIsLengthSACUnitAttr() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("attr(data-foo type(<length>),2em)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthSACUnit(lunit));
	}

	@Test
	public void testIsLengthSACUnitAttrCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*attr(data-foo type(<length>),2em))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthSACUnit(lunit));
	}

	@Test
	public void testIsLengthPercentageSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px*2%/1% + 2*1em - 2px*1vw/1vmax + 2vw/1vmin*3px + 2%*1vh/1%)");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("0.0");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(30px*10%/1% - 2%*1%/2vmin)");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc((300px - 2%)/3)");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(1/3%*2vmax*3Q - (2%*3px + 1pt*2rex)/1em)");
		assertTrue(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("-webkit-calc(100% - 16px)");
		assertFalse(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("rgba(0, 0, 0, 0)");
		assertFalse(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isLengthPercentageSACUnit(lunit));

		lunit = parsePropertyValue("linear-gradient(yellow, blue 20%, #0f0)");
		assertFalse(ValueFactory.isLengthPercentageSACUnit(lunit));
	}

	@Test
	public void testIsLengthPercentageSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2em)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthPercentageSACUnit(lunit));
	}

	@Test
	public void testIsLengthPercentageSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(var(--foo,2em) + 5%)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthPercentageSACUnit(lunit));
	}

	@Test
	public void testIsLengthPercentageSACUnitAttr() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("attr(data-foo type(<length-percentage>),2%)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthPercentageSACUnit(lunit));
	}

	@Test
	public void testIsLengthPercentageSACUnitAttr2() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("attr(data-foo %,2%)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthPercentageSACUnit(lunit));
	}

	@Test
	public void testIsLengthPercentageSACUnitAttrCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*attr(data-foo type(<length-percentage>),2%))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isLengthPercentageSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1dpi");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));
		lunit = parsePropertyValue("calc(3dpi*2)");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(2*3dpi)");
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("2px");
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("sqrt(3dpi*2dpi)");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("calc(3dpi + 2dpi)");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("calc(1/3dpi*pow(2dpi,2))");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));

		lunit = parsePropertyValue("calc(1dpi*1dpi)");
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,3dpi)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*var(--foo,3dpi))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnitAttr() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("attr(data-foo type(<resolution>),3dpi)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnitAttrCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*attr(data-foo type(<resolution>),3dpi))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsPositiveSizeSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("1.2rem");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px)");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("calc(300px - 2vw)");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(300px - 2vw)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("sqrt(1px*2em)");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("calc(300deg - 0.2rad)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("0.0");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("-1");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("0%");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("-2px");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("0.6turn");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("hsl(120, 100%, 50%)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));

		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
	}

	@Test
	public void testIsPositiveSizeSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2px)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isPositiveSizeSACUnit(lunit));
	}

	@Test
	public void testIsLengthOrNumberSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px*2)");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(3px)");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(300px - 2vw)");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(300px*0.9 / 2vw)");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(96dpi*1.2/72dpi)");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(50Hz*2*1s)");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(50Hz/1s*100ms*abs(4ms))");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(5s/1Hz*100hz*abs(1khz))");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(1/2px*3em)");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(2*3s)");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("2futureUnit");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-0.1");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-1futureUnit");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-1em");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("120");
		assertTrue(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("rgb(0, 0, 0, 0)");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("cubic-bezier(0.25, 0.12, 0.35, 0.8)");
		assertFalse(ValueFactory.isLengthOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsSizeOrNumberSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px)");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(3px)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(300px - 2%)");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(300px / 2%)");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(2*3s)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-0.1");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-1%");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-1em");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("120");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("rgb(0, 0, 0, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("rgba(0, 0, 0, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("hsl(120, 100%, 50%)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsSizeOrNumberSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2px)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isSizeOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsSizeOrNumberSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*var(--foo,2px))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isSizeOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsPercentageOrNumberSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px)");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(3px)");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(300% - 1px)");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-1");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-0.1");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("-1%");
		assertFalse(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("0%");
		assertTrue(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("120");
		assertTrue(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("10%");
		assertTrue(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(2*3)");
		assertTrue(ValueFactory.isPercentageOrNumberSACUnit(lunit));

		lunit = parsePropertyValue("calc(2*10%)");
		assertTrue(ValueFactory.isPercentageOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsPercentageOrNumberSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2px)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isPercentageOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsPercentageOrNumberSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*var(--foo,2px))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isPercentageOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsAngleOrPercentageSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(2*3deg + 4rad/2rad*7deg - 2rad*3rad/4rad)");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(3deg*2 - 1/2rad*1grad*0.1turn)");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(3deg*2 - 1/2rad*1grad*0.1turn)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(3px)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(300% - 1px)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("2rad");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("120");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("10%");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("hsl(120deg, 100%, 50%)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("hsl(120deg 100% 50%)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("linear-gradient(135deg, yellow, blue)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(pi*1rad)");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(PI*1rad)");
		assertTrue(ValueFactory.isAngleOrPercentageSACUnit(lunit));

		lunit = parsePropertyValue("calc(e)");
		assertFalse(ValueFactory.isAngleOrPercentageSACUnit(lunit));
	}

	@Test
	public void testIsAngleOrPercentageSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2rad)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isAngleOrPercentageSACUnit(lunit));
	}

	@Test
	public void testIsAngleOrPercentageSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(2*var(--foo,2rad))");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isAngleOrPercentageSACUnit(lunit));
	}

	@Test
	public void testIsTimeSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));

		lunit = parsePropertyValue("2s");
		assertTrue(ValueFactory.isTimeSACUnit(lunit));

		lunit = parsePropertyValue("calc(2s*sin(45deg))");
		assertTrue(ValueFactory.isTimeSACUnit(lunit));

		lunit = parsePropertyValue("calc(1/50Hz)");
		assertTrue(ValueFactory.isTimeSACUnit(lunit));

		lunit = parsePropertyValue("-o-calc(1/50Hz)");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));

		lunit = parsePropertyValue("foo(3px)");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));

		lunit = parsePropertyValue("calc(300% - 1px)");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
	}

	@Test
	public void testIsTimeSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo,2s)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isTimeSACUnit(lunit));
	}

	@Test
	public void testIsTimeSACUnitVarCalc() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("calc(var(--foo,2s)*2)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isTimeSACUnit(lunit));
	}

	@Test
	public void testIsTimeSACUnitAttr() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("attr(data-foo type(<time>),2s)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isTimeSACUnit(lunit));
	}

	@Test
	public void testIsColorSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isColorSACUnit(lunit));
		lunit = parsePropertyValue("blue");
		assertTrue(ValueFactory.isColorSACUnit(lunit));
		lunit = parsePropertyValue("#111");
		assertTrue(ValueFactory.isColorSACUnit(lunit));
		lunit = parsePropertyValue("calc(3px)");
		assertFalse(ValueFactory.isColorSACUnit(lunit));
		lunit = parsePropertyValue("color(display-p3 1 1 1)");
		assertTrue(ValueFactory.isColorSACUnit(lunit));
	}

	@Test
	public void testIsColorSACUnitVar() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("var(--foo)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isColorSACUnit(lunit));
	}

	@Test
	public void testIsColorSACUnitVarRgb() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("rgb(var(--foo)/100%)");
		assertThrows(CSSLexicalProcessingException.class, () -> ValueFactory.isColorSACUnit(lunit));
	}

	@Test
	public void testParseMediaFeatureString() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		try {
			factory.parseMediaFeature("16/");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testCreateCSSValueCompat() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lunit = parsePropertyValue("40pt\\9");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.UNKNOWN, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((CSSTypedValue) value).getUnitType());
		assertEquals("40pt\\9", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompat2() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lunit = parsePropertyValue("foo 40pt\\9");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.UNKNOWN, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((CSSTypedValue) value).getUnitType());
		assertEquals("foo 40pt\\9", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompatIEValues() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lunit = parsePropertyValue(
				"progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.FUNCTION, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((CSSTypedValue) value).getUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5)", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompatIEValuesPlus() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		parser.setFlag(Parser.Flag.IEVALUES);
		LexicalUnit lunit = parsePropertyValue(
				"progid:DXImageTransform.Microsoft.Blur(pixelradius=5+1)");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.FUNCTION, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((CSSTypedValue) value).getUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur(pixelradius=5 + 1)", value.getCssText());
	}

	private LexicalUnit parsePropertyValue(String string) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(string));
	}

}
