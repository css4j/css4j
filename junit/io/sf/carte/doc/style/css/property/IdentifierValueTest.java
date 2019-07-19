/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.parser.CSSParser;

public class IdentifierValueTest {

	@Test
	public void testFastPathConstructorString() {
		IdentifierValue value = new IdentifierValue("scroll");
		assertEquals("scroll", value.getStringValue());
		assertEquals("scroll", value.getCssText());
	}

	@Test
	public void testSetStringValueShortString() {
		IdentifierValue value = new IdentifierValue();
		value.setStringValue(CSSPrimitiveValue.CSS_IDENT, "scroll");
		assertEquals("scroll", value.getStringValue());
		assertEquals("scroll", value.getCssText());
		value.setStringValue(CSSPrimitiveValue.CSS_IDENT, "\uD83D\uDC4D");
		assertEquals("\uD83D\uDC4D", value.getStringValue());
		assertEquals("\uD83D\uDC4D", value.getCssText());
	}

	@Test
	public void testSetCssText() {
		IdentifierValue value = new IdentifierValue();
		value.setCssText("\\\\5b8b\\4f53");
		assertEquals("\\5b8b\u4f53", value.getStringValue());
		assertEquals("\\\\5b8b\\4f53", value.getCssText());
		assertEquals("\\\\5b8b\u4f53", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssText2() {
		IdentifierValue value = new IdentifierValue();
		value.setCssText("foo\\4f53");
		assertEquals("foo\u4f53", value.getStringValue());
		assertEquals("foo\\4f53", value.getCssText());
		assertEquals("foo\u4f53", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextPrivateUse() {
		IdentifierValue value = new IdentifierValue();
		value.setCssText("foo\\f435");
		assertEquals("foo\\f435", value.getStringValue());
		assertEquals("foo\\f435", value.getCssText());
		assertEquals("foo\\f435", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextUnassigned() {
		// This test may fail in the future...
		IdentifierValue value = new IdentifierValue();
		value.setCssText("foo\\e0999");
		assertEquals("foo\\e0999", value.getStringValue());
		assertEquals("foo\\e0999", value.getCssText());
		assertEquals("foo\\e0999", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetStringValueShortStringEscaped() {
		IdentifierValue value = new IdentifierValue();
		value.setStringValue(CSSPrimitiveValue.CSS_IDENT, "\t");
		assertEquals("\t", value.getStringValue());
		assertEquals("\\9", value.getCssText());
	}

	@Test
	public void testSetLexicalUnit() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		InputSource source = new InputSource(new StringReader("\\1F44D"));
		LexicalUnit lu = parser.parsePropertyValue(source);
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		IdentifierValue value = new IdentifierValue();
		assertEquals(CSSPrimitiveValue.CSS_IDENT, value.getPrimitiveType());
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("\\1F44D", value.getCssText());
		assertEquals("\uD83D\uDC4D", value.getMinifiedCssText(""));
		assertEquals("\uD83D\uDC4D", value.getStringValue());
	}

	@Test
	public void testSetLexicalUnit2() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		InputSource source = new InputSource(new StringReader("a\\3d b"));
		LexicalUnit lu = parser.parsePropertyValue(source);
		assertEquals("a=b", lu.getStringValue());
		IdentifierValue value = new IdentifierValue();
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("a\\3d b", value.getCssText());
		assertEquals("a\\=b", value.getMinifiedCssText(""));
		assertEquals("a=b", value.getStringValue());
	}

	@Test
	public void testSetLexicalUnit3() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		InputSource source = new InputSource(new StringReader("\\4f530"));
		LexicalUnit lu = parser.parsePropertyValue(source);
		assertEquals("\\4f530", lu.getStringValue());
		IdentifierValue value = new IdentifierValue();
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("\\4f530", value.getCssText());
		assertEquals("\\4f530", value.getMinifiedCssText(""));
		assertEquals("\\4f530", value.getStringValue());
	}

	@Test
	public void testSetLexicalUnitIEHack() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		InputSource source = new InputSource(new StringReader("screen\\0"));
		LexicalUnit lu = parser.parsePropertyValue(source);
		assertEquals("screen\ufffd", lu.getStringValue());
		IdentifierValue value = new IdentifierValue();
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("screen\\0", value.getCssText());
		assertEquals("screen\\fffd ", value.getMinifiedCssText(""));
		assertEquals("screen\ufffd", value.getStringValue());
	}

	@Test
	public void testEquals() {
		ValueFactory factory = new ValueFactory();
		AbstractCSSValue value = factory.parseProperty("scroll");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, value.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_IDENT, ((CSSPrimitiveValue) value).getPrimitiveType());
		assertTrue(value.equals(factory.parseProperty("scroll")));
		assertFalse(value.equals(factory.parseProperty("medium")));
		assertTrue(value.equals(factory.parseProperty("SCROLL")));
	}

	@Test
	public void testClone() {
		IdentifierValue value = new IdentifierValue();
		value.setStringValue(CSSPrimitiveValue.CSS_IDENT, "scroll");
		IdentifierValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
