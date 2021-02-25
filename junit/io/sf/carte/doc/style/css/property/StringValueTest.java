/*

 Copyright (c) 2005-2021, Carlos Amengual.

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

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.PrimitiveValue.LexicalSetter;

public class StringValueTest {

	@Test
	public void testSetStringValueShortString() {
		StringValue value = createCSSStringValue();
		value.setStringValue(CSSValue.Type.STRING, "Some text in \"double quotes\"");
		assertEquals("Some text in \"double quotes\"", value.getStringValue());
		assertEquals("'Some text in \"double quotes\"'", value.getCssText());
		assertEquals("'Some text in \"double quotes\"'", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSValue.Type.STRING, "Some text 'in quotes'");
		assertEquals("Some text 'in quotes'", value.getStringValue());
		assertEquals("\"Some text 'in quotes'\"", value.getCssText());
		assertEquals("\"Some text 'in quotes'\"", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSValue.Type.STRING, "Some text in \"double quote's\"");
		assertEquals("Some text in \"double quote's\"", value.getStringValue());
		assertEquals("'Some text in \"double quote\\'s\"'", value.getCssText());
		assertEquals("'Some text in \"double quote\\'s\"'", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSValue.Type.STRING, "&");
		assertEquals("&", value.getStringValue());
		assertEquals("'&'", value.getCssText());
		assertEquals("'&'", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSValue.Type.STRING, "foo");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
		assertEquals("'foo'", value.getMinifiedCssText(""));
		//
		value.setStringValue(CSSValue.Type.STRING, "\\5FAE\u8F6F");
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("'\\\\5FAE\u8F6F'", value.getCssText());
		assertEquals("'\\\\5FAE\u8F6F'", value.getMinifiedCssText(""));
		//
		try {
			value.setStringValue(CSSValue.Type.STRING, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testSetCssTextString() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"foo\"");
		assertEquals("foo", value.getStringValue());
		assertEquals("\"foo\"", value.getCssText());
		assertEquals("\"foo\"", value.getMinifiedCssText(""));
		//
		value.setCssText("foo");
		assertEquals("foo", value.getStringValue());
		assertEquals("\"foo\"", value.getCssText());
		assertEquals("\"foo\"", value.getMinifiedCssText(""));
		// Syntax matching
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<string>");
		assertEquals(Match.TRUE, value.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, value.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, value.matches(syn));
	}

	@Test
	public void testSetCssTextWhitespace() {
		StringValue value = createCSSStringValue();
		value.setCssText("\" \"");
		assertEquals(" ", value.getStringValue());
		assertEquals("\" \"", value.getCssText());
		assertEquals("\" \"", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextDoubleWhitespace() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"  \"");
		assertEquals("  ", value.getStringValue());
		assertEquals("\"  \"", value.getCssText());
		assertEquals("\"  \"", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"â†\u0090\"");
		assertEquals("â†\u0090", value.getStringValue());
		assertEquals("\"â†\\90 \"", value.getCssText());
		assertEquals("\"â†\\90\"", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped2() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"\\f107\"");
		assertEquals("\\f107", value.getStringValue());
		assertEquals("\"\\f107\"", value.getCssText());
		assertEquals("\"\\f107\"", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped3() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"\u5b8b\u4f53\"");
		assertEquals("\u5b8b\u4f53", value.getStringValue());
		assertEquals("\"\u5b8b\u4f53\"", value.getCssText());
		assertEquals("\"\u5b8b\u4f53\"", value.getMinifiedCssText(""));
		value.setStringValue(CSSValue.Type.STRING, value.getStringValue());
		assertEquals("\u5b8b\u4f53", value.getStringValue());
		assertEquals("\"\u5b8b\u4f53\"", value.getCssText());
		assertEquals("\"\u5b8b\u4f53\"", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped4() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"\\\\5FAE\\8F6F\"");
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("\"\\\\5FAE\\8F6F\"", value.getCssText());
		assertEquals("\"\\\\5FAE\u8F6F\"", value.getMinifiedCssText(""));
		value.setStringValue(CSSValue.Type.STRING, value.getStringValue());
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("\"\\\\5FAE\u8F6F\"", value.getCssText());
		assertEquals("\"\\\\5FAE\u8F6F\"", value.getMinifiedCssText(""));
	}

	@Test
	public void testSetCssTextStringEscaped5() {
		StringValue value = createCSSStringValue();
		value.setCssText("\"a \\A b\"");
		assertEquals("a \nb", value.getStringValue());
		assertEquals("\"a \\A b\"", value.getCssText());
		assertEquals("\"a \\a b\"", value.getMinifiedCssText(""));
	}

	@Test
	public void testLexicalSetter() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		StringReader re = new StringReader("\"\\\\5FAE\\8F6F\"");
		LexicalUnit lu = parser.parsePropertyValue(re);
		assertNotNull(lu);
		StringValue value = createCSSStringValue();
		LexicalSetter setter = value.newLexicalSetter();
		setter.setLexicalUnit(lu);
		assertNull(setter.getNextLexicalUnit());
		assertEquals("\\5FAE\u8F6F", value.getStringValue());
		assertEquals("\"\\\\5FAE\\8F6F\"", value.getCssText());
		assertEquals("\"\\\\5FAE\u8F6F\"", value.getMinifiedCssText(""));
	}

	@Test
	public void testLexicalSetter2() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		StringReader re = new StringReader("'\\200B'");
		LexicalUnit lu = parser.parsePropertyValue(re);
		assertNotNull(lu);
		StringValue value = createCSSStringValue();
		LexicalSetter setter = value.newLexicalSetter();
		setter.setLexicalUnit(lu);
		assertNull(setter.getNextLexicalUnit());
		assertEquals("\u200B", value.getStringValue());
		assertEquals("'\\200B'", value.getCssText());
		assertEquals("'\\200b'", value.getMinifiedCssText(""));
	}

	@Test
	public void testLexicalSetter3() throws CSSException, IOException {
		CSSParser parser = new CSSParser();
		StringReader re = new StringReader("\"\\1f4e5\"");
		LexicalUnit lu = parser.parsePropertyValue(re);
		assertNotNull(lu);
		StringValue value = createCSSStringValue();
		LexicalSetter setter = value.newLexicalSetter();
		setter.setLexicalUnit(lu);
		assertNull(setter.getNextLexicalUnit());
		assertEquals("\ud83d\udce5", value.getStringValue());
		assertEquals("\"\\1f4e5\"", value.getCssText());
		assertEquals("\"\\1f4e5\"", value.getMinifiedCssText(""));
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
		value.setCssText("'\\2020'");
		assertEquals("\u2020", value.getStringValue());
		assertEquals("'\\2020'", value.getCssText());
		assertEquals("'\u2020'", value.getMinifiedCssText(""));
	}

	@Test
	public void testGetCssText() {
		StringValue value = createCSSStringValue();
		value.setCssText("'foo'");
		assertEquals("foo", value.getStringValue());
		assertEquals("'foo'", value.getCssText());
		value.setCssText("\"foo\"");
		assertEquals("foo", value.getStringValue());
		assertEquals("\"foo\"", value.getCssText());
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
		//
		value.setCssText("\"foo\"");
		other.setCssText("'foo'");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
		other.setCssText("foo");
		assertTrue(value.equals(other));
		assertTrue(value.hashCode() == other.hashCode());
	}

	@Test
	public void testClone() {
		StringValue value = createCSSStringValue();
		value.setStringValue(CSSValue.Type.STRING, "Some text in \"double quotes\"");
		StringValue clon = value.clone();
		assertEquals(value.getCssValueType(), clon.getCssValueType());
		assertEquals(value.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(value.getStringValue(), clon.getStringValue());
		assertEquals(value.getCssText(), clon.getCssText());
		assertEquals(value.getMinifiedCssText(""), clon.getMinifiedCssText(""));
	}

	private StringValue createCSSStringValue() {
		return new StringValue();
	}
}
