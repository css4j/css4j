/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.SACParserFactory;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class ValueFactoryTest {

	Parser parser;

	@Before
	public void setUp() {
		parser = SACParserFactory.createSACParser();
	}

	@Test
	public void testCreateCSSValue() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		InputSource source = new InputSource(new StringReader("1, 2, 3, 4"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		ValueList list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertTrue(list.isCommaSeparated());
		//
		source = new InputSource(new StringReader("1 2 3 4"));
		lunit = parser.parsePropertyValue(source);
		value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(4, list.getLength());
		assertFalse(list.isCommaSeparated());
		//
		source = new InputSource(new StringReader("[first header-start]"));
		lunit = parser.parsePropertyValue(source);
		value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(2, list.getLength());
		assertFalse(list.isCommaSeparated());
		assertEquals("first", list.item(0).getCssText());
		assertEquals("header-start", list.item(1).getCssText());
		//
		source = new InputSource(new StringReader("[first header-start], [main-start],[]"));
		lunit = parser.parsePropertyValue(source);
		value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		list = (ValueList) value;
		assertEquals(3, list.getLength());
		assertTrue(list.isCommaSeparated());
		assertEquals(CSSValue.CSS_VALUE_LIST, list.item(0).getCssValueType());
		ValueList bracketlist = (ValueList) list.item(0);
		assertEquals(2, bracketlist.getLength());
		assertFalse(bracketlist.isCommaSeparated());
		assertEquals("first", bracketlist.item(0).getCssText());
		assertEquals("header-start", bracketlist.item(1).getCssText());
		assertEquals("[first header-start]", bracketlist.getCssText());
		assertEquals(CSSValue.CSS_VALUE_LIST, list.item(1).getCssValueType());
		bracketlist = (ValueList) list.item(1);
		assertEquals(1, bracketlist.getLength());
		assertFalse(bracketlist.isCommaSeparated());
		assertEquals("main-start", bracketlist.item(0).getCssText());
		assertEquals("[main-start]", bracketlist.getCssText());
		assertEquals(CSSValue.CSS_VALUE_LIST, list.item(2).getCssValueType());
		bracketlist = (ValueList) list.item(2);
		assertEquals(0, bracketlist.getLength());
		assertFalse(bracketlist.isCommaSeparated());
		assertEquals("[]", bracketlist.getCssText());
	}

	@Test
	public void testIsSizeSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1px"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3px)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("0"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("calc(300px - 2%)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("calc((300px - 2%)/3)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("-webkit-calc(100% - 16px)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("3s"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(0, 0, 0, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(0 0 0 / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgba(0, 0, 0, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(120, 100%, 50%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(120, 100%, 50%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(120 100% 50% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(0, 0%, 0%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(0 0% 0% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(0, 0%, 0%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(205, 19%, 14%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(0, 0%, 0%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(0 0% 0% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
		source = new InputSource(new StringReader("linear-gradient(yellow, blue 20%, #0f0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeSACUnit(lunit));
	}

	@Test
	public void testIsResolutionSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1dpi"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3dpi)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isResolutionSACUnit(lunit));
		source = new InputSource(new StringReader("2px"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));
		source = new InputSource(new StringReader("0"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isResolutionSACUnit(lunit));
	}

	@Test
	public void testIsPositiveSizeSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1px"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("1.2rem"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3px)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("calc(300px - 2%)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("3s"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("0"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("-1"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("-2px"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("0.6turn"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(0, 0, 0, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(0 0 0 / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgba(0, 0, 0, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(10 0 0 / 0.8)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(120, 100%, 50%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(120 100% 50%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(120, 100%, 50%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(120 100% 50% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(0 0% 0% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(0, 0%, 0%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(205, 19%, 14%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(0, 0%, 0%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(0 0% 0% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPositiveSizeSACUnit(lunit));
	}

	@Test
	public void testIsSizeOrNumberSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1px"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3px)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("calc(300px - 2%)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("3s"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("0"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("120"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(0, 0, 0, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("rgb(0 0 0 / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("rgba(0, 0, 0, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(120, 100%, 50%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(120, 100%, 50%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(120 100% 50% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(0 0% 0% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hsla(0, 0%, 0%, 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(205, 19%, 14%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(0, 0%, 0%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
		source = new InputSource(new StringReader("hwb(0 0% 0% / 0)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isSizeOrNumberSACUnit(lunit));
	}

	@Test
	public void testIsPlainNumberOrPercentSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1px"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3px)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		source = new InputSource(new StringReader("calc(300% - 1px)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		source = new InputSource(new StringReader("3s"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		source = new InputSource(new StringReader("0"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		source = new InputSource(new StringReader("120"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
		source = new InputSource(new StringReader("10%"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isPlainNumberOrPercentSACUnit(lunit));
	}

	@Test
	public void testIsAngleSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1px"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3deg)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3px)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("calc(300% - 1px)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("3s"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("2rad"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("0"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("120"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("10%"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(120deg, 100%, 50%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("hsl(120deg 100% 50%)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
		source = new InputSource(new StringReader("linear-gradient(135deg, yellow, blue)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isAngleSACUnit(lunit));
	}

	@Test
	public void testIsTimeSACUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("1px"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
		source = new InputSource(new StringReader("2s"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isTimeSACUnit(lunit));
		source = new InputSource(new StringReader("foo(2s)"));
		lunit = parser.parsePropertyValue(source);
		assertTrue(ValueFactory.isTimeSACUnit(lunit));
		source = new InputSource(new StringReader("foo(3px)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
		source = new InputSource(new StringReader("calc(300% - 1px)"));
		lunit = parser.parsePropertyValue(source);
		assertFalse(ValueFactory.isTimeSACUnit(lunit));
	}

	@Test
	public void testFirstDimensionArgumentUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo(3.2em,4)"));
		LexicalUnit lunit = parser.parsePropertyValue(source);
		assertEquals(LexicalUnit.SAC_EM, ValueFactory.functionDimensionArgumentUnit(lunit));
		source = new InputSource(new StringReader("foo(3px,4)"));
		lunit = parser.parsePropertyValue(source);
		assertEquals(LexicalUnit.SAC_PIXEL, ValueFactory.functionDimensionArgumentUnit(lunit));
		source = new InputSource(new StringReader("foo(3.2,4)"));
		lunit = parser.parsePropertyValue(source);
		assertEquals(-1, ValueFactory.functionDimensionArgumentUnit(lunit));
		source = new InputSource(new StringReader("hwb(0, 0%, 0%)"));
		lunit = parser.parsePropertyValue(source);
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
		InputSource source = new InputSource(new StringReader("40pt\\9"));
		((Parser2) parser).setFlag(Parser2.Flag.IEVALUES);
		LexicalUnit lunit = parser.parsePropertyValue(source);
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_UNKNOWN, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("40pt\\9", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompat2() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		InputSource source = new InputSource(new StringReader("foo 40pt\\9"));
		((Parser2) parser).setFlag(Parser2.Flag.IEVALUES);
		LexicalUnit lunit = parser.parsePropertyValue(source);
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_UNKNOWN, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("foo 40pt\\9", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompatIEPrio() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		InputSource source = new InputSource(new StringReader("40pt!ie"));
		((Parser2) parser).setFlag(Parser2.Flag.IEPRIO);
		LexicalUnit lunit = parser.parsePropertyValue(source);
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_UNKNOWN, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("40pt!ie", value.getCssText());
	}

	@Test
	public void testCreateCSSValueCompatIEPrio2() throws CSSException, IOException {
		ValueFactory factory = new ValueFactory();
		InputSource source = new InputSource(new StringReader("foo 40pt!ie"));
		((Parser2) parser).setFlag(Parser2.Flag.IEPRIO);
		LexicalUnit lunit = parser.parsePropertyValue(source);
		StyleValue value = factory.createCSSValue(lunit);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_UNKNOWN, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertEquals("foo 40pt!ie", value.getCssText());
	}

}
