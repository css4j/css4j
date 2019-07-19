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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;

import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class CounterValueTest {

	CSSStyleDeclarationRule styleRule;
	BaseCSSStyleDeclaration style;

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
		style.setCssText("content: counter(ListCounter) '. ';");
		CounterValue value = (CounterValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value));
		style.setCssText("content: counter(ListCounter) '. ';");
		CounterValue value2 = (CounterValue) style.getPropertyCSSValue("content");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("content: counter(ListCounter);");
		value2 = (CounterValue) style.getPropertyCSSValue("content");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("content: counter(ListCounter); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue.CSS_COUNTER, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counter(ListCounter)", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter); ", style.getCssText());
		assertEquals("content:counter(ListCounter)", style.getMinifiedCssText());
		CounterValue val = (CounterValue) cssval;
		Counter counter = val.getCounterValue();
		assertEquals("ListCounter", counter.getIdentifier());
		assertEquals("decimal", counter.getListStyle());
		assertEquals("", counter.getSeparator());
	}

	@Test
	public void testGetCssTextSeparator() {
		style.setCssText("content: counter(ListCounter) '. '; ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue.CSS_COUNTER, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counter(ListCounter) '. '", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter) '. '; ", style.getCssText());
		assertEquals("content:counter(ListCounter) '. '", style.getMinifiedCssText());
		CounterValue val = (CounterValue) cssval;
		Counter counter = val.getCounterValue();
		assertEquals("ListCounter", counter.getIdentifier());
		assertEquals("decimal", counter.getListStyle());
		assertEquals(". ", counter.getSeparator());
	}

	@Test
	public void testGetCssTextSeparators() {
		style.setCssText("content: '(' counter(ListCounter) ') '; ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSValue.CSS_VALUE_LIST, cssval.getCssValueType());
		ValueList list = (ValueList) cssval;
		assertEquals(2, list.getLength());
		assertEquals("'('", list.item(0).getCssText());
		cssval = list.item(1);
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, cssval.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_COUNTER, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("'(' counter(ListCounter) ') '", style.getPropertyValue("content"));
		assertEquals("content: '(' counter(ListCounter) ') '; ", style.getCssText());
		assertEquals("content:'(' counter(ListCounter) ') '", style.getMinifiedCssText());
		CounterValue val = (CounterValue) cssval;
		Counter counter = val.getCounterValue();
		assertEquals("ListCounter", counter.getIdentifier());
		assertEquals("decimal", counter.getListStyle());
		assertEquals(") ", counter.getSeparator());
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("content: counter(ListCounter, upper-latin); ");
		AbstractCSSValue cssval = style.getPropertyCSSValue("content");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue.CSS_COUNTER, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("counter(ListCounter, upper-latin)", style.getPropertyValue("content"));
		assertEquals("content: counter(ListCounter, upper-latin); ", style.getCssText());
		assertEquals("content:counter(ListCounter,upper-latin)", style.getMinifiedCssText());
		CounterValue val = (CounterValue) cssval;
		Counter counter = val.getCounterValue();
		assertEquals("ListCounter", counter.getIdentifier());
		assertEquals("upper-latin", counter.getListStyle());
		assertEquals("", counter.getSeparator());
	}

	@Test
	public void testClone() {
		style.setCssText("content: counter(ListCounter, upper-latin); ");
		CounterValue value = (CounterValue) style.getPropertyCSSValue("content");
		CounterValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
