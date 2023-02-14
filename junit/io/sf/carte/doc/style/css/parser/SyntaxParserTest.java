/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Multiplier;
import io.sf.carte.doc.style.css.nsac.CSSException;

public class SyntaxParserTest {

	SyntaxParser parser;

	@BeforeEach
	public void setUp() {
		parser = new SyntaxParser();
	}

	@Test
	public void testCreateSimpleSyntax() {
		CSSValueSyntax syn = SyntaxParser.createSimpleSyntax("length-percentage");
		assertEquals("length-percentage", syn.getName());
		assertEquals(Category.lengthPercentage, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("<length-percentage>", syn.toString());
	}

	@Test
	public void testParseSyntaxUniversal() {
		CSSValueSyntax syn = parser.parseSyntax("*");
		assertEquals("*", syn.getName());
		assertEquals(Category.universal, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("*", syn.toString());
	}

	@Test
	public void testParseSyntax() {
		CSSValueSyntax syn = parser.parseSyntax("<Length>");
		assertEquals("length", syn.getName());
		assertEquals(Category.length, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("<length>", syn.toString());
		//
		syn = parser.parseSyntax("<Length-percentage>");
		assertEquals("length-percentage", syn.getName());
		assertEquals(Category.lengthPercentage, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("<length-percentage>", syn.toString());
	}

	@Test
	public void testParseSyntaxMultiplier() {
		CSSValueSyntax syn = parser.parseSyntax("<Length>#");
		assertEquals("length", syn.getName());
		assertEquals(Category.length, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("<length>#", syn.toString());
		//
		syn = parser.parseSyntax("<Length-percentage>#");
		assertEquals("length-percentage", syn.getName());
		assertEquals(Category.lengthPercentage, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("<length-percentage>#", syn.toString());
		//
		syn = parser.parseSyntax("<Length-percentage>#    |   <number>+");
		assertEquals("length-percentage", syn.getName());
		assertEquals(Category.lengthPercentage, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertEquals("<length-percentage># | <number>+", syn.toString());
		syn = syn.getNext();
		assertEquals("number", syn.getName());
		assertEquals(Category.number, syn.getCategory());
		assertEquals(Multiplier.PLUS, syn.getMultiplier());
		assertNull(syn.getNext());
		//
		syn = parser.parseSyntax("<Length-percentage>#|<number>+");
		assertEquals("length-percentage", syn.getName());
		assertEquals(Category.lengthPercentage, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertEquals("<length-percentage># | <number>+", syn.toString());
		syn = syn.getNext();
		assertEquals("number", syn.getName());
		assertEquals(Category.number, syn.getCategory());
		assertEquals(Multiplier.PLUS, syn.getMultiplier());
		assertNull(syn.getNext());
	}

	@Test
	public void testParseSyntaxIdent() {
		CSSValueSyntax syn = parser.parseSyntax("-foo");
		assertEquals("-foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("-foo", syn.toString());
	}

	@Test
	public void testParseSyntaxIdent2() {
		CSSValueSyntax syn = parser.parseSyntax("-foo-ident   |   -bar");
		assertEquals("-foo-ident", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertEquals("-foo-ident | -bar", syn.toString());
		syn = syn.getNext();
		assertEquals("-bar", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
	}

	@Test
	public void testParseSyntaxCustomIdent() {
		CSSValueSyntax syn = parser.parseSyntax("--foo");
		assertEquals("--foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("--foo", syn.toString());
	}

	@Test
	public void testParseSyntaxCustomIdent2() {
		CSSValueSyntax syn = parser.parseSyntax("--foo-ident   |   --bar");
		assertEquals("--foo-ident", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertEquals("--foo-ident | --bar", syn.toString());
		syn = syn.getNext();
		assertEquals("--bar", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
	}

	@Test
	public void testParseSyntaxIdentAuto() {
		CSSValueSyntax syn = parser.parseSyntax("auto");
		assertEquals("auto", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("auto", syn.toString());
	}

	@Test
	public void testParseSyntaxIdentEscaped() {
		CSSValueSyntax syn = parser.parseSyntax("\\+foo");
		assertEquals("+foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("\\+foo", syn.toString());
	}

	@Test
	public void testParseSyntaxIdentEscapedWS() {
		CSSValueSyntax syn = parser.parseSyntax("\\ foo");
		assertEquals(" foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("\\ foo", syn.toString());
	}

	@Test
	public void testParseSyntaxIdentEscapedHex() {
		CSSValueSyntax syn = parser.parseSyntax("\\31 foo");
		assertEquals("1foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("\\31 foo", syn.toString());
	}

	@Test
	public void testParseSyntaxIdentEscapedHex2() {
		CSSValueSyntax syn = parser.parseSyntax("\\1D700 f");
		assertEquals("\ud835\udf00f", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("\\1d700 f", syn.toString());
	}

	@Test
	public void testParseSyntaxIdentEscapedHex3() {
		CSSValueSyntax syn = parser.parseSyntax("\\1D700 \\+foo\\\\  | \\:#| \\#+");
		assertEquals("\ud835\udf00+foo\\", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NONE, syn.getMultiplier());
		assertEquals("\\1d700\\+foo\\\\ | \\:# | \\#+", syn.toString());
		//
		syn = syn.getNext();
		assertEquals(":", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertEquals("\\:# | \\#+", syn.toString());
		//
		syn = syn.getNext();
		assertEquals("#", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.PLUS, syn.getMultiplier());
		assertEquals("\\#+", syn.toString());
		assertNull(syn.getNext());
	}

	@Test
	public void testParseSyntaxIdentMultiplier() {
		CSSValueSyntax syn = parser.parseSyntax("-foo#");
		assertEquals("-foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertNull(syn.getNext());
		assertEquals("-foo#", syn.toString());
		//
		syn = parser.parseSyntax("-foo+   |   -bar#");
		assertEquals("-foo", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.PLUS, syn.getMultiplier());
		assertEquals("-foo+ | -bar#", syn.toString());
		syn = syn.getNext();
		assertEquals("-bar", syn.getName());
		assertEquals(Category.IDENT, syn.getCategory());
		assertEquals(Multiplier.NUMBER, syn.getMultiplier());
		assertNull(syn.getNext());
	}

	@Test
	public void testParseSyntaxError() {
		try {
			parser.parseSyntax("");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
		//
		try {
			parser.parseSyntax("  ");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
		//
		try {
			parser.parseSyntax(" | ");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
		//
		try {
			parser.parseSyntax("<color> | ");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
		//
		try {
			parser.parseSyntax("<color> | *");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
		//
		try {
			parser.parseSyntax("<color> #| <length>");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
		//
		try {
			parser.parseSyntax("\\1D700 \\+foo\\\\ \\:#");
			fail("Must throw exception.");
		} catch (CSSException e) {
		}
	}

}
