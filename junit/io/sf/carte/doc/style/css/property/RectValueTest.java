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

import io.sf.carte.doc.style.css.CSSRectValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class RectValueTest {

	BaseCSSStyleDeclaration style;

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
		style.setCssText("clip: rect(2px 12em 3em 2pt);");
		RectValue value = (RectValue) style.getPropertyCSSValue("clip");
		assertTrue(value.equals(value));
		style.setCssText("clip: rect(2px 12em 3em 2pt);");
		RectValue value2 = (RectValue) style.getPropertyCSSValue("clip");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("clip: rect(2px 12em 3em 1pt);");
		value2 = (RectValue) style.getPropertyCSSValue("clip");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("clip: rect(2px 12em 3em 2pt); ");
		StyleValue cssval = style.getPropertyCSSValue("clip");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.RECT, cssval.getPrimitiveType());
		assertEquals("rect(2px, 12em, 3em, 2pt)", style.getPropertyValue("clip"));
		assertEquals("clip: rect(2px, 12em, 3em, 2pt); ", style.getCssText());
		assertEquals("clip:rect(2px,12em,3em,2pt)", style.getMinifiedCssText());
		CSSRectValue rect = (CSSRectValue) cssval;
		assertEquals("2px", rect.getTop().getCssText());
		assertEquals("12em", rect.getRight().getCssText());
		assertEquals("3em", rect.getBottom().getCssText());
		assertEquals("2pt", rect.getLeft().getCssText());
	}

	@Test
	public void testGetCssTextSeparator() {
		style.setCssText("clip: rect(2px, 12em, 3em, 2pt); ");
		StyleValue cssval = style.getPropertyCSSValue("clip");
		assertNotNull(cssval);
		assertEquals(CSSValue.Type.RECT, cssval.getPrimitiveType());
		assertEquals("rect(2px, 12em, 3em, 2pt)", style.getPropertyValue("clip"));
		assertEquals("clip: rect(2px, 12em, 3em, 2pt); ", style.getCssText());
		assertEquals("clip:rect(2px,12em,3em,2pt)", style.getMinifiedCssText());
		CSSRectValue rect = (CSSRectValue) cssval;
		assertEquals("2px", rect.getTop().getCssText());
		assertEquals("12em", rect.getRight().getCssText());
		assertEquals("3em", rect.getBottom().getCssText());
		assertEquals("2pt", rect.getLeft().getCssText());
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("clip: rect(2px 12em 3em 2pt); ");
		RectValue value = (RectValue) style.getPropertyCSSValue("clip");
		RectValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}