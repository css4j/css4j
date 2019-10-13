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

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.PrimitiveValue.LexicalSetter;

public class StringValueSQTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	@Test
	public void testSetStringValueShortString() {
		StringValue value = createCSSStringValue();
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "Some text in \"double quotes\"");
		assertEquals("Some text in \"double quotes\"", value.getStringValue());
		assertEquals("'Some text in \"double quotes\"'", value.getCssText());
		assertEquals("'Some text in \"double quotes\"'", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "Some text 'in quotes'");
		assertEquals("Some text 'in quotes'", value.getStringValue());
		assertEquals("\"Some text 'in quotes'\"", value.getCssText());
		assertEquals("\"Some text 'in quotes'\"", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "Some text in \"double quote's\"");
		assertEquals("Some text in \"double quote's\"", value.getStringValue());
		assertEquals("'Some text in \"double quote\\'s\"'", value.getCssText());
		assertEquals("'Some text in \"double quote\\'s\"'", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "&");
		assertEquals("&", value.getStringValue());
		assertEquals("'&'", value.getCssText());
		assertEquals("'&'", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "foo");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
		assertEquals("'foo'", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "\\5FAE\u8F6F");
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("'\\\\5FAE\u8F6F'", value.getCssText());
		assertEquals("'\\\\5FAE\u8F6F'", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextString() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"foo\"");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
		assertEquals("'foo'", value.getMinifiedCssText(""));
		value.setCssText("foo");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
		assertEquals("'foo'", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"â†\u0090\"");
		assertEquals("â†\u0090", value.getStringValue());
		assertEquals("'â†\\90'", value.getCssText());
		assertEquals("'â†\\90'", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped2() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"\\f107\"");
		assertEquals("\\f107", value.getStringValue());
		assertEquals("'\\f107'", value.getCssText());
		assertEquals("'\\f107'", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped3() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"\u5b8b\u4f53\"");
		assertEquals("\u5b8b\u4f53", value.getStringValue());
		assertEquals("'\u5b8b\u4f53'", value.getCssText());
		assertEquals("'\u5b8b\u4f53'", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, value.getStringValue());
		assertEquals("\u5b8b\u4f53", value.getStringValue());
		assertEquals("'\u5b8b\u4f53'", value.getCssText());
		assertEquals("'\u5b8b\u4f53'", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped4() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"\\\\5FAE\\8F6F\"");
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("'\\\\5FAE\u8F6F'", value.getCssText());
		assertEquals("'\\\\5FAE\u8F6F'", value.getMinifiedCssText(""));
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, value.getStringValue());
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("'\\\\5FAE\u8F6F'", value.getCssText());
		assertEquals("'\\\\5FAE\u8F6F'", value.getMinifiedCssText(""));
	}

	@Test
	public void testLexicalSetter() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		InputSource source = new InputSource(new StringReader("\"\\\\5FAE\\8F6F\""));
		LexicalUnit lu = parser.parsePropertyValue(source);
		assertNotNull(lu);
		StringValue value = createCSSStringValue();
		LexicalSetter setter = value.newLexicalSetter();
		setter.setLexicalUnit(lu);
		assertNull(setter.getNextLexicalUnit());
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("'\\\\5FAE\u8F6F'", value.getCssText());
		assertEquals("'\\\\5FAE\u8F6F'", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringProgId() {
		StringValue value = createCSSStringValue();
		value.setCssText("progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", value.getStringValue());
		assertEquals("'progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)'", value.getCssText());
	}

	@Test
	public void testSetCssTextStringISO10646() {
		StringValue value = createCSSStringValue();
		value.setCssText("\\26");
		assertEquals("&", value.getStringValue());
		assertEquals("'\\26'", value.getCssText());
		assertEquals("'&'", value.getMinifiedCssText(""));
		value.setCssText("\\200B");
		assertEquals("\u200B", value.getStringValue());
		assertEquals("'\\200B'", value.getCssText());
		assertEquals("'\\200b'", value.getMinifiedCssText(""));
		value.setCssText("\\200  B");
		assertTrue("Ȁ B".equals(value.getStringValue()));
		assertEquals("'\\200  B'", value.getCssText());
		assertEquals("'\u0200 B'", value.getMinifiedCssText(""));
	}

	@Test
	public void testGetCssText() {
		StringValue value = createCSSStringValue();
		value.setCssText("'foo'");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
		value.setCssText("\"foo\"");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
	}

	@Test
	public void testEquals() {
		StringValue value = createCSSStringValue();
		value.setCssText("\\26");
		StringValue other = createCSSStringValue();
		other.setCssText("\\26");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("foo");
		assertFalse(value.equals(other));
		assertFalse(value.hashCode() == other.hashCode());
	}

	@Test
	public void testClone() {
		StringValue value = createCSSStringValue();
		value.setStringValue(CSSPrimitiveValue.CSS_STRING, "Some text in \"double quotes\"");
		StringValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getMinifiedCssText(""), clon.getMinifiedCssText(""));
	}

	private StringValue createCSSStringValue() {
		byte flags = AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE;
		return new StringValue(flags);
	}
}
