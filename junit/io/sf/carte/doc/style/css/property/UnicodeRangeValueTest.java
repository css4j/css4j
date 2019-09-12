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

public class UnicodeRangeValueTest {

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
		style.setCssText("unicode-range: U+402;");
		UnicodeRangeValue value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value));
		style.setCssText("unicode-range: U+402;");
		UnicodeRangeValue value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("unicode-range: U+403;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		// Wildcard
		style.setCssText("unicode-range: U+???;");
		value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value));
		style.setCssText("unicode-range: U+???;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("unicode-range: U+2??;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
		// Range
		style.setCssText("unicode-range: U+22-28;");
		value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value));
		style.setCssText("unicode-range: U+22-28;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertTrue(value.equals(value2));
		assertEquals(value.hashCode(), value2.hashCode());
		style.setCssText("unicode-range: U+22-26;");
		value2 = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertFalse(value.equals(value2));
		assertFalse(value.hashCode() == value2.hashCode());
	}

	@Test
	public void testWildcardRange() {
		style.setCssText("unicode-range: U+???;");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_UNICODE_RANGE, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("unicode-range: U+???; ", style.getCssText());
		assertEquals("unicode-range:U+???", style.getMinifiedCssText());
		assertEquals("U+???", style.getPropertyValue("unicode-range"));
	}

	@Test
	public void testSetStringValueWildcard() {
		style.setCssText("unicode-range: U+???;");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		UnicodeRangeValue val = (UnicodeRangeValue) cssval;
		assertEquals("U+???", val.getCssText());
		PrimitiveValue unicode = val.getValue();
		assertNotNull(unicode);
		assertEquals(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD, unicode.getPrimitiveType());
		assertEquals("U+???", unicode.getCssText());
		assertEquals("???", unicode.getStringValue());
		try {
			unicode.setStringValue(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			unicode.setStringValue(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD, "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			unicode.setStringValue(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD, "foo");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			unicode.setStringValue(CSSPrimitiveValue.CSS_IDENT, "foo");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		unicode.setStringValue(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD, "2??");
		assertEquals("U+2??", unicode.getCssText());
		assertEquals("2??", unicode.getStringValue());
	}

	@Test
	public void testGetCssText() {
		style.setCssText("unicode-range: U+0027; ");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_UNICODE_RANGE, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("U+27", style.getPropertyValue("unicode-range"));
		assertEquals("unicode-range: U+27; ", style.getCssText());
		assertEquals("unicode-range:U+27", style.getMinifiedCssText());
		UnicodeRangeValue val = (UnicodeRangeValue) cssval;
		assertEquals("U+27", val.getCssText());
		try {
			val.setStringValue(CSSPrimitiveValue2.CSS_UNICODE_WILDCARD, "??");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		try {
			val.setStringValue(CSSPrimitiveValue2.CSS_UNICODE_RANGE, "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
	}

	@Test
	public void testGetCssText2() {
		style.setCssText("unicode-range: U+0025-00FF; ");
		StyleValue cssval = style.getPropertyCSSValue("unicode-range");
		assertNotNull(cssval);
		assertEquals(CSSPrimitiveValue2.CSS_UNICODE_RANGE, ((CSSPrimitiveValue) cssval).getPrimitiveType());
		assertEquals("U+25-ff", style.getPropertyValue("unicode-range"));
		assertEquals("unicode-range: U+25-ff; ", style.getCssText());
		assertEquals("unicode-range:U+25-ff", style.getMinifiedCssText());
		UnicodeRangeValue val = (UnicodeRangeValue) cssval;
		assertEquals("U+25-ff", val.getCssText());
	}

	@Test
	public void testClone() {
		BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration();
		style.setCssText("unicode-range: U+0025-00FF; ");
		UnicodeRangeValue value = (UnicodeRangeValue) style.getPropertyCSSValue("unicode-range");
		assertNotNull(value);
		UnicodeRangeValue clon = value.clone();
		assertNotNull(clon);
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getCssText(), clon.getCssText());
		assertTrue(value.equals(clon));
	}

}
