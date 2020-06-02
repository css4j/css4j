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

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class AttrValueTest {

	@Test
	public void testSetStringValueShortString() {
		AttrValue value = new AttrValue((byte) 0);
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title");
		assertEquals("title", value.getStringValue());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title)", value.getCssText());
		assertEquals("attr(title)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title string");
		assertEquals("title string", value.getStringValue());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title string)", value.getCssText());
		assertEquals("attr(title string)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title string, 'foo'");
		assertEquals("title string, 'foo'", value.getStringValue());
		assertEquals("foo", ((CSSPrimitiveValue) value.getFallback()).getStringValue());
		assertEquals("attr(title string, 'foo')", value.getCssText());
		assertEquals("attr(title string,'foo')", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title");
		assertEquals("title", value.getStringValue());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "width length, 20em");
		assertEquals("width length, 20em", value.getStringValue());
		assertEquals("width", value.getAttributeName());
		assertEquals("length", value.getAttributeType());
		assertEquals("20em", value.getFallback().getCssText());
		assertEquals("attr(width length, 20em)", value.getCssText());
		assertEquals("attr(width length,20em)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "width length");
		assertEquals("width length", value.getStringValue());
		assertEquals("width", value.getAttributeName());
		assertEquals("length", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width length)", value.getCssText());
		assertEquals("attr(width length)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "width px, 20em");
		assertEquals("width px, 20em", value.getStringValue());
		assertEquals("width", value.getAttributeName());
		assertEquals("px", value.getAttributeType());
		assertEquals("20em", value.getFallback().getCssText());
		assertEquals("attr(width px, 20em)", value.getCssText());
		assertEquals("attr(width px,20em)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "width px");
		assertEquals("width px", value.getStringValue());
		assertEquals("width", value.getAttributeName());
		assertEquals("px", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width px)", value.getCssText());
		assertEquals("attr(width px)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "width %, 40%");
		assertEquals("width %, 40%", value.getStringValue());
		assertEquals("width", value.getAttributeName());
		assertEquals("%", value.getAttributeType());
		assertEquals("40%", value.getFallback().getCssText());
		assertEquals("attr(width %, 40%)", value.getCssText());
		assertEquals("attr(width %,40%)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "width %");
		assertEquals("width %", value.getStringValue());
		assertEquals("width", value.getAttributeName());
		assertEquals("%", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0%", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width %)", value.getCssText());
		assertEquals("attr(width %)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "elev angle, 20deg");
		assertEquals("elev angle, 20deg", value.getStringValue());
		assertEquals("elev", value.getAttributeName());
		assertEquals("angle", value.getAttributeType());
		assertEquals("20deg", value.getFallback().getCssText());
		assertEquals("attr(elev angle, 20deg)", value.getCssText());
		assertEquals("attr(elev angle,20deg)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "elev angle");
		assertEquals("elev angle", value.getStringValue());
		assertEquals("elev", value.getAttributeName());
		assertEquals("angle", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0deg", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(elev angle)", value.getCssText());
		assertEquals("attr(elev angle)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "elev deg, 20deg");
		assertEquals("elev deg, 20deg", value.getStringValue());
		assertEquals("elev", value.getAttributeName());
		assertEquals("deg", value.getAttributeType());
		assertEquals("20deg", value.getFallback().getCssText());
		assertEquals("attr(elev deg, 20deg)", value.getCssText());
		assertEquals("attr(elev deg,20deg)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "elev deg");
		assertEquals("elev deg", value.getStringValue());
		assertEquals("elev", value.getAttributeName());
		assertEquals("deg", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0deg", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(elev deg)", value.getCssText());
		assertEquals("attr(elev deg)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pause time, 2s");
		assertEquals("pause time, 2s", value.getStringValue());
		assertEquals("pause", value.getAttributeName());
		assertEquals("time", value.getAttributeType());
		assertEquals("2s", value.getFallback().getCssText());
		assertEquals("attr(pause time, 2s)", value.getCssText());
		assertEquals("attr(pause time,2s)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pause time");
		assertEquals("pause time", value.getStringValue());
		assertEquals("pause", value.getAttributeName());
		assertEquals("time", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0s", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pause time)", value.getCssText());
		assertEquals("attr(pause time)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pause s, 2s");
		assertEquals("pause s, 2s", value.getStringValue());
		assertEquals("pause", value.getAttributeName());
		assertEquals("s", value.getAttributeType());
		assertEquals("2s", value.getFallback().getCssText());
		assertEquals("attr(pause s, 2s)", value.getCssText());
		assertEquals("attr(pause s,2s)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pause s");
		assertEquals("pause s", value.getStringValue());
		assertEquals("pause", value.getAttributeName());
		assertEquals("s", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0s", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pause s)", value.getCssText());
		assertEquals("attr(pause s)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pitch frequency, 200Hz");
		assertEquals("pitch frequency, 200hz", value.getStringValue());
		assertEquals("pitch", value.getAttributeName());
		assertEquals("frequency", value.getAttributeType());
		assertEquals("200hz", value.getFallback().getCssText());
		assertEquals("attr(pitch frequency, 200hz)", value.getCssText());
		assertEquals("attr(pitch frequency,200hz)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pitch frequency");
		assertEquals("pitch frequency", value.getStringValue());
		assertEquals("pitch", value.getAttributeName());
		assertEquals("frequency", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0Hz", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pitch frequency)", value.getCssText());
		assertEquals("attr(pitch frequency)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pitch Hz, 200Hz");
		assertEquals("pitch Hz, 200hz", value.getStringValue());
		assertEquals("pitch", value.getAttributeName());
		assertEquals("Hz", value.getAttributeType());
		assertEquals("200hz", value.getFallback().getCssText());
		assertEquals("attr(pitch Hz, 200hz)", value.getCssText());
		assertEquals("attr(pitch Hz,200hz)", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "pitch Hz");
		assertEquals("pitch Hz", value.getStringValue());
		assertEquals("pitch", value.getAttributeName());
		assertEquals("Hz", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0Hz", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pitch Hz)", value.getCssText());
		assertEquals("attr(pitch Hz)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetStringValueShortStringError() {
		AttrValue value = new AttrValue((byte) 0);
		try {
			value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSPrimitiveValue.CSS_ATTR, " ");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "-");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "att-content string, attr(foo)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "att-content string, calc(attr(foo)/3)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "att-content string, calc(sqrt(attr(foo)/3))");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextString() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		assertEquals("title", value.getStringValue());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeType() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-title string)");
		assertEquals("data-title string", value.getStringValue());
		assertEquals("data-title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(data-title string)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallback() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-title string, \"My Title\")");
		assertEquals("data-title string, \"My Title\"", value.getStringValue());
		assertEquals("data-title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSPrimitiveValue.CSS_STRING, ((CSSPrimitiveValue) value.getFallback()).getPrimitiveType());
		assertEquals("\"My Title\"", fallback.getCssText());
		assertEquals("attr(data-title string, \"My Title\")", value.getCssText());
		assertEquals("attr(data-title string,\"My Title\")", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackList() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-index integer, 1 2)");
		assertEquals("data-index", value.getAttributeName());
		assertEquals("integer", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(org.w3c.dom.css.CSSValue.CSS_VALUE_LIST, fallback.getCssValueType());
		assertEquals("1 2", fallback.getCssText());
		assertEquals("attr(data-index integer, 1 2)", value.getCssText());
		assertEquals("attr(data-index integer,1 2)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackListComma() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-index integer, 1, 2)");
		assertEquals("data-index", value.getAttributeName());
		assertEquals("integer", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(org.w3c.dom.css.CSSValue.CSS_VALUE_LIST, fallback.getCssValueType());
		assertEquals("1, 2", fallback.getCssText());
		assertEquals("attr(data-index integer, 1, 2)", value.getCssText());
		assertEquals("attr(data-index integer,1,2)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURL() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(myuri url,'https://www.example.com/foo')");
		assertEquals("myuri url, 'https://www.example.com/foo'", value.getStringValue());
		assertEquals("myuri", value.getAttributeName());
		assertEquals("url", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSPrimitiveValue.CSS_STRING, ((CSSPrimitiveValue) value.getFallback()).getPrimitiveType());
		assertEquals("'https://www.example.com/foo'", fallback.getCssText());
		assertEquals("attr(myuri url, 'https://www.example.com/foo')", value.getCssText());
		assertEquals("attr(myuri url,'https://www.example.com/foo')", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURLBad() {
		AttrValue value = new AttrValue((byte) 0);
		try {
			value.setCssText("attr(myuri url,https://www.example.com/foo)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testParseAttr() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("margin-left:attr(leftmargin %)");
		CSSPrimitiveValue marginLeft = (CSSPrimitiveValue) style.getPropertyCSSValue("margin-left");
		assertNotNull(marginLeft);
		assertEquals("attr(leftmargin %)", marginLeft.getCssText());
		assertFalse(style.getStyleDeclarationErrorHandler().hasErrors());
	}

	@Test
	public void testParseFallbackCustomPropertyRecursiveAttr() {
		BaseCSSStyleDeclaration style = createStyleDeclaration();
		style.setCssText("margin-left:attr(noattr length,var(--foo,attr(noattr)))");
		assertTrue(style.getStyleDeclarationErrorHandler().hasErrors());
		assertNull(style.getPropertyCSSValue("margin-left"));
	}

	private static BaseCSSStyleDeclaration createStyleDeclaration() {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		CSSStyleDeclarationRule styleRule = sheet.createStyleRule();
		styleRule.setStyleDeclarationErrorHandler(new DefaultStyleDeclarationErrorHandler());
		return (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testEquals() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		AttrValue other = new AttrValue((byte) 0);
		other.setCssText("attr(title)");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("attr(href)");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testClone() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		AttrValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
