/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class CustomPropertyValueTest {

	private BaseCSSStyleDeclaration style;

	@Before
	public void setUp() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		style = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		style.setCssText("foo: var(--my-identifier); ");
		CustomPropertyValue value = (CustomPropertyValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value));
		style.setCssText("foo: var(--my-identifier); ");
		CustomPropertyValue value2 = (CustomPropertyValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("foo: var(--My-identifier); ");
		value2 = (CustomPropertyValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		style.setCssText("foo: var(--other-identifier); ");
		value2 = (CustomPropertyValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("foo: var(--my-identifier); ");
		assertEquals("var(--my-identifier)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-identifier); ", style.getCssText());
		assertEquals("foo:var(--my-identifier)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		CustomPropertyValue val = (CustomPropertyValue) cssval;
		assertEquals("var(--my-identifier)", val.getCssText());
		assertEquals("--my-identifier", val.getStringValue());
	}

	@Test
	public void testGetCssTextUpperCase() {
		style.setCssText("foo: var(--My-Identifier); ");
		assertEquals("var(--My-Identifier)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--My-Identifier); ", style.getCssText());
		assertEquals("foo:var(--My-Identifier)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		CustomPropertyValue val = (CustomPropertyValue) cssval;
		assertEquals("var(--My-Identifier)", val.getCssText());
		assertEquals("--My-Identifier", val.getName());
	}

	@Test
	public void testGetCssTextFallback() {
		style.setCssText("foo: var(--my-identifier,#f0c); ");
		assertEquals("var(--my-identifier, #f0c)", style.getPropertyValue("foo"));
		assertEquals("foo: var(--my-identifier, #f0c); ", style.getCssText());
		assertEquals("foo:var(--my-identifier,#f0c)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		CustomPropertyValue val = (CustomPropertyValue) cssval;
		assertEquals("var(--my-identifier, #f0c)", val.getCssText());
		assertEquals("#f0c", val.getFallback().getCssText());
		assertEquals("--my-identifier", val.getStringValue());
	}

	@Test
	public void testGetCssTextFallbackList() {
		style.setCssText("foo: var(--my-list, 1 2); ");
		assertEquals("var(--my-list, 1 2)", style.getPropertyValue("foo"));
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		CustomPropertyValue val = (CustomPropertyValue) cssval;
		StyleValue fallback = val.getFallback();
		assertEquals(org.w3c.dom.css.CSSValue.CSS_VALUE_LIST, fallback.getCssValueType());
		assertEquals("1 2", fallback.getCssText());
	}

	@Test
	public void testGetCssTextFallbackCommas() {
		style.setCssText("foo: var(--my-list, 1, 2); ");
		assertEquals("var(--my-list, 1, 2)", style.getPropertyValue("foo"));
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		CustomPropertyValue val = (CustomPropertyValue) cssval;
		StyleValue fallback = val.getFallback();
		assertEquals(org.w3c.dom.css.CSSValue.CSS_VALUE_LIST, fallback.getCssValueType());
		assertEquals("1, 2", fallback.getCssText());
	}

	@Test
	public void testSetCssText() {
		CustomPropertyValue value = new CustomPropertyValue();
		value.setCssText("var(--my-identifier)");
		assertEquals("var(--my-identifier)", value.getCssText());
		assertEquals("var(--my-identifier)", value.getMinifiedCssText(""));
		assertEquals("--my-identifier", value.getName());
		assertEquals("--my-identifier", value.getStringValue());
		//
		value.setCssText("var(--my-identifier, #f0c)");
		assertEquals("var(--my-identifier, #f0c)", value.getCssText());
		assertEquals("var(--my-identifier,#f0c)", value.getMinifiedCssText(""));
		assertEquals("--my-identifier", value.getName());
		assertEquals("--my-identifier", value.getStringValue());
		assertEquals("#f0c", value.getFallback().getCssText());
	}

	@Test
	public void testSetCssTextError() {
		CustomPropertyValue value = new CustomPropertyValue();
		try {
			value.setCssText("foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError2() {
		CustomPropertyValue value = new CustomPropertyValue();
		try {
			value.setCssText("var(--my-identifier, ;)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			value.setCssText("var(--my-identifier, ()");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			value.setCssText("var(--my-identifier, {)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			value.setCssText("var(--my-identifier, @)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		style.setCssText("foo: var(--my-identifier); ");
		CustomPropertyValue value = (CustomPropertyValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		CustomPropertyValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
