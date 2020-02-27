/*

 Copyright (c) 2005-2020, Carlos Amengual.

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
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSEnvVariableValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class EnvVariableValueTest {

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
		style.setCssText("foo: env(safe-area-inset-left); ");
		CSSEnvVariableValue value = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value));
		style.setCssText("foo: env(safe-area-inset-left); ");
		CSSEnvVariableValue value2 = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("foo: env(safe-area-inset-right); ");
		value2 = (CSSEnvVariableValue) style.getPropertyCSSValue("foo");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("foo: env(safe-area-inset-left); ");
		assertEquals("env(safe-area-inset-left)", style.getPropertyValue("foo"));
		assertEquals("foo: env(safe-area-inset-left); ", style.getCssText());
		assertEquals("foo:env(safe-area-inset-left)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(safe-area-inset-left)", val.getCssText());
		assertEquals("safe-area-inset-left", val.getName());
		assertNull(val.getFallback());
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("foo: env(safe-area-inset-left, 1px); ");
		assertEquals("env(safe-area-inset-left, 1px)", style.getPropertyValue("foo"));
		assertEquals("foo: env(safe-area-inset-left, 1px); ", style.getCssText());
		assertEquals("foo:env(safe-area-inset-left,1px)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("foo");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.ENV, cssval.getPrimitiveType());
		CSSEnvVariableValue val = (CSSEnvVariableValue) cssval;
		assertEquals("env(safe-area-inset-left, 1px)", val.getCssText());
		assertEquals("safe-area-inset-left", val.getName());
		CSSValue fb = val.getFallback();
		assertNotNull(fb);
		assertEquals(CssType.TYPED, fb.getCssValueType());
		assertEquals(Type.NUMERIC, fb.getPrimitiveType());
		assertEquals(CSSUnit.CSS_PX, ((CSSTypedValue) fb).getUnitType());
		assertEquals("1px", fb.getCssText());
	}

	@Test
	public void testSetCssText() {
		EnvVariableValue value = new EnvVariableValue();
		value.setCssText("env(safe-area-inset-left, 1px)");
		assertEquals("env(safe-area-inset-left, 1px)", value.getCssText());
		assertEquals("env(safe-area-inset-left,1px)", value.getMinifiedCssText(""));
		assertEquals("safe-area-inset-left", value.getName());
		assertNotNull(value.getFallback());
		assertEquals("1px", value.getFallback().getCssText());
		value.setCssText("env(safe-area-inset-left)");
		assertEquals("env(safe-area-inset-left)", value.getCssText());
		assertEquals("env(safe-area-inset-left)", value.getMinifiedCssText(""));
		assertEquals("safe-area-inset-left", value.getName());
		assertNull(value.getFallback());
	}

	@Test
	public void testSetCssTextError() {
		EnvVariableValue value = new EnvVariableValue();
		try {
			value.setCssText("foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("foo: env(safe-area-inset-left, 1px); ");
		EnvVariableValue value = (EnvVariableValue) style.getPropertyCSSValue("foo");
		assertNotNull(value);
		EnvVariableValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getName(), clon.getName());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.getFallback().equals(clon.getFallback()));
		assertTrue(value.equals(clon));
	}

}
