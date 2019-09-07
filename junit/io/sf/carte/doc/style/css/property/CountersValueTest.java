/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class CountersValueTest {

	private CSSStyleDeclarationRule styleRule;
	private BaseCSSStyleDeclaration style;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		styleRule = sheet.createCSSStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("content: counters(ListCounter,'. ');");
		CountersValue value = (CountersValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value));
		style.setCssText("content: counters(ListCounter,'. ');");
		CountersValue value2 = (CountersValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counters(ListCounter,'. ', decimal);");
		value2 = (CountersValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counters(ListCounter);");
		value2 = (CountersValue) style.getPropertyCSSValue("content");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("content: counters(ListCounter,'|');");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counters(ListCounter, '|')", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '|'); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'|')", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
		assertEquals("|", counter.getSeparator());
	}

	@Test
	public void testGetCssTextNone() {
		style.setCssText("content: counters(ListCounter,'. ',none);");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counters(ListCounter, '. ', none)", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '. ', none); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'. ',none)", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		AbstractCSSPrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSPrimitiveValue.CSS_IDENT, counterstyle.getPrimitiveType());
		assertEquals("none", counterstyle.getStringValue());
		assertEquals(". ", counter.getSeparator());
	}

	@Test
	public void testGetCssTextDefaultStyle() {
		style.setCssText("content: counters(ListCounter,'|', decimal);");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counters(ListCounter, '|')", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '|'); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'|')", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		AbstractCSSPrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSPrimitiveValue.CSS_IDENT, counterstyle.getPrimitiveType());
		assertEquals("decimal", counterstyle.getStringValue());
		assertEquals("|", counter.getSeparator());
	}

	@Test
	public void testGetCssTextSeparators() {
		style.setCssText("content: '(' counters(ListCounter,'.') ') ';");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.CSS_VALUE_LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(3, list.getLength());
		assertEquals("'('", list.item(0).getCssText());
		cssval = list.item(1);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("'(' counters(ListCounter, '.') ') '", style.getPropertyValue("content"));
		assertEquals("content: '(' counters(ListCounter, '.') ') '; ", style.getCssText());
		assertEquals("content:'(' counters(ListCounter,'.') ') '", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
		assertEquals(".", counter.getSeparator());
	}

	@Test
	public void testGetCssTextSeparators2() {
		style.setCssText("content: counters(ListCounter,'.') ':';");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.CSS_VALUE_LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(2, list.getLength());
		cssval = list.item(0);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counters(ListCounter, '.') ':'", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '.') ':'; ", style.getCssText());
		assertEquals("content:counters(ListCounter,'.') ':'", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		assertNull(counter.getCounterStyle());
		assertEquals(".", counter.getSeparator());
		assertEquals("':'", list.item(1).getCssText());
	}

	@Test
	public void testGetCssTextStyle() {
		style.setCssText("content: counters(ListCounter, '.', upper-latin);");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counters(ListCounter, '.', upper-latin)", style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '.', upper-latin); ", style.getCssText());
		assertEquals("content:counters(ListCounter,'.',upper-latin)", style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		AbstractCSSPrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSPrimitiveValue.CSS_IDENT, counterstyle.getPrimitiveType());
		assertEquals("upper-latin", counterstyle.getStringValue());
		assertEquals(".", counter.getSeparator());
	}

	@Test
	public void testGetCssTextStyleSymbols() {
		style.setCssText("content: counters(ListCounter, '.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7')); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_COUNTERS, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counters(ListCounter, '.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7'))",
				style.getPropertyValue("content"));
		assertEquals("content: counters(ListCounter, '.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7')); ",
				style.getCssText());
		assertEquals("content:counters(ListCounter,'.',symbols(cyclic '*' '\\2020' '\\2021' '\\A7'))",
				style.getMinifiedCssText());
		CountersValue counter = (CountersValue) cssval;
		assertEquals("ListCounter", counter.getName());
		AbstractCSSPrimitiveValue counterstyle = counter.getCounterStyle();
		assertEquals(CSSPrimitiveValue2.CSS_FUNCTION, counterstyle.getPrimitiveType());
		assertEquals("symbols", counterstyle.getStringValue());
		assertEquals(".", counter.getSeparator());
	}

	@Test
	public void testClone() {
		style.setCssText("content: counters(ListCounter,'.', upper-latin);");
		CountersValue value = (CountersValue) style.getPropertyCSSValue("content");
		CountersValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	@Test
	public void testClone2() {
		style.setCssText("content: counters(ListCounter,'.', symbols(cyclic '*' '\\2020' '\\2021' '\\A7'));");
		CountersValue value = (CountersValue) style.getPropertyCSSValue("content");
		CountersValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertFalse(value.getCounterStyle() == clon.getCounterStyle());
		assertTrue(value.equals(clon));
	}

}