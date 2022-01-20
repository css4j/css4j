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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class ElementReferenceValueTest {

	@Test
	public void testEquals() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(#someId);");
		ExtendedCSSPrimitiveValue value = (ExtendedCSSPrimitiveValue) style.getPropertyCSSValue("background-image");
		assertTrue(value.equals(value));
		style.setCssText("background-image: element(#someId);");
		ExtendedCSSPrimitiveValue value2 = (ExtendedCSSPrimitiveValue) style.getPropertyCSSValue("background-image");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("background-image: element(#otherId);");
		value2 = (ExtendedCSSPrimitiveValue) style.getPropertyCSSValue("background-image");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testParse() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(#someId);");
		assertEquals("element(#someId)", style.getPropertyValue("background-image"));
		assertEquals("background-image: element(#someId); ", style.getCssText());
		assertEquals("background-image:element(#someId)", style.getMinifiedCssText());
		StyleValue cssval = style.getPropertyCSSValue("background-image");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_ELEMENT_REFERENCE, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("element(#someId)", cssval.getCssText());
		assertEquals("someId", ((CSSPrimitiveValue) cssval).getStringValue());
	}

	@Test
	public void testParse2() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("background-image: element(someId); ");
		assertNull(style.getPropertyCSSValue("background-image"));
	}

	@Test
	public void testSetCssText() {
		ElementReferenceValue value = new ElementReferenceValue();
		value.setCssText("element(#someId)");
		assertEquals("someId", value.getStringValue());
		assertEquals("element(#someId)", value.getCssText());
		assertEquals("element(#someId)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextError() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setCssText("foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError2() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setCssText("element(foo)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextError3() {
		ElementReferenceValue value = new ElementReferenceValue();
		try {
			value.setCssText("element(#foo bar)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("background-image: element(#someId); ");
		PrimitiveValue value = (PrimitiveValue) style.getPropertyCSSValue("background-image");
		assertNotNull(value);
		PrimitiveValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

	private static BaseCSSStyleDeclaration createStyleDeclaration() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		return (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

}
