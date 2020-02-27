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

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class AttrValueTest {

	@Test
	public void testSetCssTextString() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		assertEquals("title", value.getAttributeName());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title)", value.getCssText());
		assertEquals("attr(title)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(title string)");
		assertEquals("title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title string)", value.getCssText());
		assertEquals("attr(title string)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(title string, 'foo')");
		assertEquals("title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		assertEquals("foo", ((CSSTypedValue) value.getFallback()).getStringValue());
		assertEquals("attr(title string, 'foo')", value.getCssText());
		assertEquals("attr(title string,'foo')", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(title)");
		assertEquals("title", value.getAttributeName());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		//
		value.setCssText("attr(width length, 20em)");
		assertEquals("width", value.getAttributeName());
		assertEquals("length", value.getAttributeType());
		assertEquals("20em", value.getFallback().getCssText());
		assertEquals("attr(width length, 20em)", value.getCssText());
		assertEquals("attr(width length,20em)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width length)");
		assertEquals("width", value.getAttributeName());
		assertEquals("length", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width length)", value.getCssText());
		assertEquals("attr(width length)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width px, 20em)");
		assertEquals("width", value.getAttributeName());
		assertEquals("px", value.getAttributeType());
		assertEquals("20em", value.getFallback().getCssText());
		assertEquals("attr(width px, 20em)", value.getCssText());
		assertEquals("attr(width px,20em)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width px)");
		assertEquals("width", value.getAttributeName());
		assertEquals("px", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width px)", value.getCssText());
		assertEquals("attr(width px)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width %, 40%)");
		assertEquals("width", value.getAttributeName());
		assertEquals("%", value.getAttributeType());
		assertEquals("40%", value.getFallback().getCssText());
		assertEquals("attr(width %, 40%)", value.getCssText());
		assertEquals("attr(width %,40%)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(width %)");
		assertEquals("width", value.getAttributeName());
		assertEquals("%", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0%", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(width %)", value.getCssText());
		assertEquals("attr(width %)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(elev angle, 20deg)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("angle", value.getAttributeType());
		assertEquals("20deg", value.getFallback().getCssText());
		assertEquals("attr(elev angle, 20deg)", value.getCssText());
		assertEquals("attr(elev angle,20deg)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(elev angle)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("angle", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0deg", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(elev angle)", value.getCssText());
		assertEquals("attr(elev angle)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(elev deg, 20deg)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("deg", value.getAttributeType());
		assertEquals("20deg", value.getFallback().getCssText());
		assertEquals("attr(elev deg, 20deg)", value.getCssText());
		assertEquals("attr(elev deg,20deg)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(elev deg)");
		assertEquals("elev", value.getAttributeName());
		assertEquals("deg", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0deg", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(elev deg)", value.getCssText());
		assertEquals("attr(elev deg)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pause time, 2s)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("time", value.getAttributeType());
		assertEquals("2s", value.getFallback().getCssText());
		assertEquals("attr(pause time, 2s)", value.getCssText());
		assertEquals("attr(pause time,2s)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pause time)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("time", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0s", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pause time)", value.getCssText());
		assertEquals("attr(pause time)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pause s, 2s)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("s", value.getAttributeType());
		assertEquals("2s", value.getFallback().getCssText());
		assertEquals("attr(pause s, 2s)", value.getCssText());
		assertEquals("attr(pause s,2s)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pause s)");
		assertEquals("pause", value.getAttributeName());
		assertEquals("s", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0s", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pause s)", value.getCssText());
		assertEquals("attr(pause s)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pitch frequency, 200Hz)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("frequency", value.getAttributeType());
		assertEquals("200hz", value.getFallback().getCssText());
		assertEquals("attr(pitch frequency, 200hz)", value.getCssText());
		assertEquals("attr(pitch frequency,200hz)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pitch frequency)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("frequency", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0Hz", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pitch frequency)", value.getCssText());
		assertEquals("attr(pitch frequency)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pitch Hz, 200Hz)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("Hz", value.getAttributeType());
		assertEquals("200hz", value.getFallback().getCssText());
		assertEquals("attr(pitch Hz, 200hz)", value.getCssText());
		assertEquals("attr(pitch Hz,200hz)", value.getMinifiedCssText(""));
		//
		value.setCssText("attr(pitch Hz)");
		assertEquals("pitch", value.getAttributeName());
		assertEquals("Hz", value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("0Hz", AttrValue.defaultFallback(value.getAttributeType()).getCssText());
		assertEquals("attr(pitch Hz)", value.getCssText());
		assertEquals("attr(pitch Hz)", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringError() {
		AttrValue value = new AttrValue((byte) 0);
		try {
			value.setCssText("attr()");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setCssText("attr( )");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setCssText("attr(-)");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setCssText("attr(att-content string, attr(foo))");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setCssText("attr(att-content string, calc(attr(foo)/3))");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		//
		try {
			value.setCssText("attr(att-content string, calc(sqrt(attr(foo)/3)))");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextString2() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		assertEquals("title", value.getAttributeName());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals(0, AttrValue.defaultFallback(value.getAttributeType()).getStringValue().length());
		assertEquals("attr(title)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeType() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-title string)");
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
		assertEquals("data-title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSValue.Type.STRING, ((CSSTypedValue) value.getFallback()).getPrimitiveType());
		assertEquals("\"My Title\"", fallback.getCssText());
		assertEquals("attr(data-title string, \"My Title\")", value.getCssText());
		assertEquals("attr(data-title string,\"My Title\")", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallbackURL() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(myuri url,'https://www.example.com/foo')");
		assertEquals("myuri", value.getAttributeName());
		assertEquals("url", value.getAttributeType());
		StyleValue fallback = value.getFallback();
		assertEquals(CSSValue.Type.STRING, ((CSSTypedValue) value.getFallback()).getPrimitiveType());
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
		StyleValue marginLeft = style.getPropertyCSSValue("margin-left");
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
		assertEquals(value.getAttributeName(), clon.getAttributeName());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
