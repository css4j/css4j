/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
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

	@Before
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
		//
		lunit = parsePropertyValue("1 2 3 4");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("1 2 3 4", list.getCssText());
		//
		lunit = parsePropertyValue("/*0*/1/*1*/2/*2*/3/*3*/4/*4*/");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("1 2 3 4", list.getCssText());
		//
		lunit = parsePropertyValue("[first header-start]");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(2, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("first", list.item(0).getCssText());
		assertEquals("header-start", list.item(1).getCssText());
		//
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
		//
		lunit = parsePropertyValue("unset");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(CSSValue.Type.UNSET, value.getPrimitiveType());
		assertEquals("unset", value.getCssText());
		//
		lunit = parsePropertyValue("revert");
		value = factory.createCSSValue(lunit);
		assertEquals(CssType.KEYWORD, value.getCssValueType());
		assertEquals(CSSValue.Type.REVERT, value.getPrimitiveType());
		assertEquals("revert", value.getCssText());
		//
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
		assertEquals(CSSValue.Type.VAR, value.getPrimitiveType());
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
		//
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
	public void testIsSizeSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("foo(3px)");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("var(--foo,2em)");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("calc(300px - 2%)");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("calc((300px - 2%)/3)");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("-webkit-calc(100% - 16px)");
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("rgb(0, 0, 0, 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("rgb(0 0 0 / 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("rgba(0, 0, 0, 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hsl(120, 100%, 50%)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hsla(120 100% 50% / 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hsl(0, 0%, 0%, 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hsl(0 0% 0% / 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hsla(0, 0%, 0%, 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hwb(205 19% 14%)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hwb(0 0% 0%)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("hwb(0 0% 0% / 0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		//
		lunit = parsePropertyValue("linear-gradient(yellow, blue 20%, #0f0)");
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1dpi");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));
		lunit = parsePropertyValue("foo(3dpi)");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));
		lunit = parsePropertyValue("var(--foo,3dpi)");
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));
		lunit = parsePropertyValue("2px");
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));
		lunit = parsePropertyValue("0");
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsPositiveSizeSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("1.2rem");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("foo(3px)");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("calc(300px - 2%)");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("var(--foo,2px)");
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("0");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("-1");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("-2px");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("0.6turn");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("rgb(0, 0, 0, 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("rgb(0 0 0 / 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("rgba(0, 0, 0, 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("rgb(10 0 0 / 0.8)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hsl(120, 100%, 50%)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hsl(120 100% 50%)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hsla(120 100% 50% / 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hsl(0 0% 0% / 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hsla(0, 0%, 0%, 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hwb(205 19% 14%)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hwb(0 0% 0%)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		lunit = parsePropertyValue("hwb(0 0% 0% / 0)");
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
	}

	@Test
	public void testIsSizeOrNumberSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("foo(3px)");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("calc(300px - 2%)");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("var(--foo, 2px)");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("120");
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("rgb(0, 0, 0, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("rgb(0 0 0 / 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("rgba(0, 0, 0, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hsl(120, 100%, 50%)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hsla(120, 100%, 50%, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hsla(120 100% 50% / 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hsl(0 0% 0% / 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hsla(0, 0%, 0%, 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hwb(205 19% 14%)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hwb(0 0% 0%)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		lunit = parsePropertyValue("hwb(0 0% 0% / 0)");
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsPlainNumberOrPercentSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("foo(3px)");
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("calc(300% - 1px)");
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("var(--foo, 1px)");
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("120");
		assertTrue(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		lunit = parsePropertyValue("10%");
		assertTrue(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
	}

	@Test
	public void testIsAngleSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("foo(3deg)");
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("foo(3px)");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("calc(300% - 1px)");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("3s");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("2rad");
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("0");
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("120");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("10%");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("hsl(120deg, 100%, 50%)");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("hsl(120deg 100% 50%)");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("linear-gradient(135deg, yellow, blue)");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("calc(pi*1rad)");
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("calc(PI*1rad)");
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		lunit = parsePropertyValue("calc(e)");
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
	}

	@Test
	public void testIsTimeSACUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("1px");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
		lunit = parsePropertyValue("2s");
		assertTrue(ValueFactory.isTimeSACUnit(lunit));
		lunit = parsePropertyValue("foo(2s)");
		assertTrue(ValueFactory.isTimeSACUnit(lunit));
		lunit = parsePropertyValue("foo(3px)");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
		lunit = parsePropertyValue("calc(300% - 1px)");
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
	}

	@Test
	public void testFirstDimensionArgumentUnit() throws CSSException, IOException {
		LexicalUnit lunit = parsePropertyValue("foo(3.2em,4)");
		assertEquals(CSSUnit.CSS_EM, ValueFactory.functionDimensionArgumentUnit(lunit));
		lunit = parsePropertyValue("foo(3px,4)");
		assertEquals(CSSUnit.CSS_PX, ValueFactory.functionDimensionArgumentUnit(lunit));
		lunit = parsePropertyValue("foo(3.2,4)");
		assertEquals(-1, ValueFactory.functionDimensionArgumentUnit(lunit));
		lunit = parsePropertyValue("radial-gradient(5em circle at top left, yellow, blue)");
		assertEquals(-1, ValueFactory.functionDimensionArgumentUnit(lunit));
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
	public void testCreateCSSValueCompatIEPrio() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		parser.setFlag(Parser.Flag.IEPRIO);
		LexicalUnit lunit = parsePropertyValue("40pt!ie");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.UNKNOWN, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((CSSTypedValue) value).getUnitType());
		assertEquals("40pt!ie", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompatIEPrio2() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		parser.setFlag(Parser.Flag.IEPRIO);
		LexicalUnit lunit = parsePropertyValue("foo 40pt!ie");
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.UNKNOWN, value.getPrimitiveType());
		assertEquals(CSSUnit.CSS_INVALID, ((CSSTypedValue) value).getUnitType());
		assertEquals("foo 40pt!ie", value.getCssText());
	}

	private LexicalUnit parsePropertyValue(String string) throws CSSParseException, IOException {
		return parser.parsePropertyValue(new StringReader(string));
	}

}
