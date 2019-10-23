/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class IdentifierValueTest {

	@Test
	public void testFastPathConstructorString() {
		IdentifierValue value = new IdentifierValue("scroll");
		assertEquals("scroll", value.getStringValue());
		assertEquals("scroll", value.getCssText());
		assertFalse(value.isCalculatedNumber());
		assertFalse(value.isNegativeNumber());
		assertFalse(value.isNumberZero());
		assertFalse(value.isSystemDefault());
	}

	@Test
	public void testSetStringValueShortString() {
		IdentifierValue value = new IdentifierValue();
		value.setStringValue(CSSValue.Type.IDENT, "scroll");
		assertEquals("scroll", value.getStringValue());
		assertEquals("scroll", value.getCssText());
		//
		value.setStringValue(CSSValue.Type.IDENT, "\uD83D\uDC4D");
		assertEquals("\uD83D\uDC4D", value.getStringValue());
		assertEquals("\uD83D\uDC4D", value.getCssText());
		//
		try {
			value.setStringValue(CSSValue.Type.STRING, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_MODIFICATION_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSValue.Type.IDENT, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		//
		try {
			value.setStringValue(CSSValue.Type.IDENT, "");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
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
	public void testSetCssTextError() {
		IdentifierValue value = new IdentifierValue();
		try {
			value.setCssText(null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		//
		try {
			value.setCssText("");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
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
		value.setStringValue(CSSValue.Type.IDENT, "\t");
		assertEquals("\t", value.getStringValue());
		assertEquals("\\9", value.getCssText());
	}

	@Test
	public void testSetLexicalUnit() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		StringReader re = new StringReader("\\1F44D");
		LexicalUnit lu = parser.parsePropertyValue(re);
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		IdentifierValue value = new IdentifierValue();
		assertEquals(CSSValue.Type.IDENT, value.getPrimitiveType());
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("\\1F44D", value.getCssText());
		assertEquals("\uD83D\uDC4D", value.getMinifiedCssText(""));
		assertEquals("\uD83D\uDC4D", value.getStringValue());
	}

	@Test
	public void testSetLexicalUnit2() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		StringReader re = new StringReader("a\\3d b");
		LexicalUnit lu = parser.parsePropertyValue(re);
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
		StringReader re = new StringReader("\\4f530");
		LexicalUnit lu = parser.parsePropertyValue(re);
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
		StringReader re = new StringReader("screen\\0");
		LexicalUnit lu = parser.parsePropertyValue(re);
		assertEquals("screen\ufffd", lu.getStringValue());
		IdentifierValue value = new IdentifierValue();
		value.newLexicalSetter().setLexicalUnit(lu);
		assertEquals("screen\\0", value.getCssText());
		assertEquals("screen\\fffd ", value.getMinifiedCssText(""));
		assertEquals("screen\ufffd", value.getStringValue());
	}

	@Test
	public void testEquals() {
		CSSParser parser = new CSSParser();
		ValueFactory factory = new ValueFactory();
		StyleValue value = factory.parseProperty("scroll", parser);
		assertEquals(CssType.TYPED, value.getCssValueType());
		assertEquals(CSSValue.Type.IDENT, value.getPrimitiveType());
		assertTrue(value.equals(factory.parseProperty("scroll", parser)));
		assertFalse(value.equals(factory.parseProperty("medium", parser)));
		assertFalse(value.equals(factory.parseProperty("SCROLL", parser)));
		assertTrue(value.equals(factory.parseProperty("background-attachment", "SCROLL", parser)));
	}

	@Test
	public void testClone() {
		IdentifierValue value = new IdentifierValue();
		value.setStringValue(CSSValue.Type.IDENT, "scroll");
		IdentifierValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
	}

}
