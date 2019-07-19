/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class AttrValueTest {

	@Test
	public void testSetStringValueShortString() {
		AttrValue value = new AttrValue((byte) 0);
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title");
		assertEquals("title", value.getStringValue());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
		assertEquals("attr(title)", value.getCssText());
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title string");
		assertEquals("title string", value.getStringValue());
		assertNull(value.getFallback());
		assertEquals("attr(title string)", value.getCssText());
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title string, 'foo'");
		assertEquals("title string, 'foo'", value.getStringValue());
		assertEquals("attr(title string, 'foo')", value.getCssText());
		value.setStringValue(CSSPrimitiveValue.CSS_ATTR, "title");
		assertEquals("title", value.getStringValue());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
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
	}

	@Test
	public void testSetCssTextString() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(title)");
		assertEquals("title", value.getStringValue());
		assertNull(value.getAttributeType());
		assertNull(value.getFallback());
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
		assertEquals("attr(data-title string)", value.getCssText());
	}

	@Test
	public void testSetCssTextStringAttributeTypeFallback() {
		AttrValue value = new AttrValue((byte) 0);
		value.setCssText("attr(data-title string, \"My Title\")");
		assertEquals("data-title string, \"My Title\"", value.getStringValue());
		assertEquals("data-title", value.getAttributeName());
		assertEquals("string", value.getAttributeType());
		AbstractCSSValue fallback = value.getFallback();
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, fallback.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_STRING, ((CSSPrimitiveValue) fallback).getPrimitiveType());
		assertEquals("\"My Title\"", fallback.getCssText());
		assertEquals("attr(data-title string, \"My Title\")", value.getCssText());
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
