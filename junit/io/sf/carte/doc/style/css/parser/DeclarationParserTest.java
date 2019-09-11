/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class DeclarationParserTest {

	private static Parser2 parser;
	private TestDeclarationHandler handler;
	private TestErrorHandler errorHandler;

	@Before
	public void setUp() {
		handler = new TestDeclarationHandler();
		errorHandler = new TestErrorHandler();
		parser = new CSSParser();
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(errorHandler);
	}

	@Test
	public void testParseStyleDeclaration() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family: Times New Roman"));
		parser.parseStyleDeclaration(source);
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		assertEquals("Times New Roman", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #x"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #,"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #:"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #@charset"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #-"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor8() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #_"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #."));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor10() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: ##"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor11() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: foo #;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor12() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: foo #"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(100% - 3em"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(100% - 3em;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(100% -"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(100% -;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("background-image: url(http://www.example.com/"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("background-image: url(http://www.example.com/;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("background-image: url("));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("background-image: url(;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBad() throws IOException {
		InputSource source = new InputSource(new StringReader("list-style: @"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier() throws IOException {
		InputSource source = new InputSource(new StringReader("list-style: -9foo_bar"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier2() throws IOException {
		InputSource source = new InputSource(new StringReader("list-style: 9foo_bar"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier3() throws IOException {
		InputSource source = new InputSource(new StringReader("list-style: foo\0"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier4() throws IOException {
		InputSource source = new InputSource(new StringReader("list-style: foo\u0000"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier5() throws IOException {
		InputSource source = new InputSource(new StringReader("list-style: foo\u0007"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedIdentifier() throws IOException {
		InputSource source = new InputSource(new StringReader("padding-bottom: \\35 px\\9"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-bottom", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclaration2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family : Times New Roman ; color : yellow ; "));
		parser.parseStyleDeclaration(source);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("yellow", lu.getStringValue());
		assertEquals("yellow", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclaration3() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("font-family: Times New Roman; color: yellow; width: calc(100% - 3em);"));
		parser.parseStyleDeclaration(source);
		assertEquals(3, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals("width", handler.propertyNames.get(2));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("yellow", lu.getStringValue());
		lu = handler.lexicalValues.get(2);
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationComments() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"-moz-foo:moz-bar;/*!rtl:ignore*/-o-foo:o-bar;/*!rtl:ignore*/foo:bar/*(skipped)!rtl:ignore*/;/*!rtl:ignore*/"));
		parser.parseStyleDeclaration(source);
		assertEquals(3, handler.propertyNames.size());
		assertEquals("-moz-foo", handler.propertyNames.getFirst());
		assertEquals("-o-foo", handler.propertyNames.get(1));
		assertEquals("foo", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("moz-bar", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("o-bar", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		lu = handler.lexicalValues.get(2);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
		assertEquals("bar", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore", handler.comments.get(0));
		assertEquals("!rtl:ignore", handler.comments.get(1));
		assertEquals("!rtl:ignore", handler.comments.get(2));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(100% - 3em !important;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: rgb(128, 0, 97 !important;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: # !important;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #!important;"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRange() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("unicode-range: U+416"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+416", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(1046, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRange2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("unicode-range: U+0025-00FF"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+25-ff", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeWildcard() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("unicode-range: U+4??"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+4??", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit2.SAC_UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeList() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("unicode-range: U+022, U+0025-00FF, U+4??, U+FF00"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+22, U+25-ff, U+4??, U+ff00", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(34, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit2.SAC_UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalUnit.SAC_INTEGER, subv.getLexicalUnitType());
		assertEquals(65280, subv.getIntegerValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeWildcard2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("unicode-range: U+???"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_UNICODERANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit2.SAC_UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("???", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeBadWildcard() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("unicode-range: U+??????"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedProperty() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("-\\31 zzz\\:_:1;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("-1zzz:_", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
		assertEquals(12, errorHandler.warningException.getColumnNumber());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:-\\31zzz\\:_"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("-1zzz:_", lu.getStringValue());
		assertEquals("-\\31zzz\\:_", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("symbols: \\1F44D;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1F44D", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("filter: \\:foo;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals(":foo", lu.getStringValue());
		assertEquals("\\:foo", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display: block\\9"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("block\t", lu.getStringValue());
		assertEquals("block\\9", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display: bl\\9 ock"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("bl\tock", lu.getStringValue());
		assertEquals("bl\\9 ock", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display: -\\9 block"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("-\tblock", lu.getStringValue());
		assertEquals("-\\9 block", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue6() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("font-family: \\5FAE\\8F6F\\96C5\\9ED1,Arial,\\5b8b\\4f53,sans-serif"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("微软雅黑", lu.getStringValue());
		assertEquals("\\5FAE\\8F6F\\96C5\\9ED1, Arial, \\5b8b\\4f53, sans-serif", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("宋体", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("sans-serif", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family: \\\\5FAE\\8F6F\\96C5\\9ED1,Arial"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\\5FAE软雅黑", lu.getStringValue());
		assertEquals("\\\\5FAE\\8F6F\\96C5\\9ED1, Arial", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue8() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family: \"\u5b8b\u4f53\",Arial"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\u5b8b\u4f53", lu.getStringValue());
		assertEquals("\"\u5b8b\u4f53\", Arial", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family:file\\:\\/\\/\\/dir\\/file"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("file:///dir/file", lu.getStringValue());
		assertEquals("file\\:\\/\\/\\/dir\\/file", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue10() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("list-style-type:symbols('*' '\\2020' '\\2021' '\\A7');"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("list-style-type", handler.propertyNames.getFirst());
		LexicalUnit lunit = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lunit.getLexicalUnitType());
		assertEquals("symbols", lunit.getFunctionName());
		LexicalUnit lu = lunit.getParameters();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("*", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\u2020", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\u2021", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\u00a7", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("symbols('*' '\\2020' '\\2021' '\\A7')", lunit.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: 600px\\9"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: 600px\\9"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("600px\\9", lu.getStringValue());
		assertEquals("600px\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: 600px\\0"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("600px\\0", lu.getStringValue());
		assertEquals("600px\\0", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack3Error() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(80% - 3cap)\\9"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(80% - 3cap)\\9"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("calc(80% - 3cap)\\9", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("calc(80% - 3cap)\\9", lu.toString());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color: #000\\9"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("#000\\9", lu.getStringValue());
		assertEquals("#000\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationNoBackslashHack() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family: Times New Roman\\9"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("Times New Roman\\9", lu.toString());
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("Roman\t", lu.getStringValue());
		assertEquals("Roman\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackFontFamily() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family: Times New Roman\\0/"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times New Roman\\0/", lu.toString());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Roman\ufffd", lu.getStringValue());
		assertEquals("Roman\\0/", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackFontFamily2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-family: Times New Roman\\0/"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Times New Roman\\0/", lu.toString());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("New Roman\\0", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color\0:#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color\u0007:#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color#:#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#color:#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError5() throws CSSException, IOException {
		// Test the recovery from previous error
		InputSource source = new InputSource(new StringReader("#color:#ff0;margin-right:0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color color:#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameStringError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("\"color\":#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameStringError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("background-\"color\":#ff0"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameWarning() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("\\30 color:#ff0"));
		parser.parseStyleDeclaration(source);
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("0color", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#ff0", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameBackslashHack() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("color\\9:#ff0"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color\u0009", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("#ff0", lu.toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationStarHackError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("*width:600px"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationStarHack() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("*width:60cap"));
		parser.setFlag(Parser2.Flag.STARHACK);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_CAP, lu.getLexicalUnitType());
		assertEquals(60, lu.getFloatValue(), 0.001);
		assertEquals("cap", lu.getDimensionUnitText());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationStarHack2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display:block;*width:600px"));
		parser.setFlag(Parser2.Flag.STARHACK);
		parser.parseStyleDeclaration(source);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("*width", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalUnit.SAC_PIXEL, lu.getLexicalUnitType());
		assertEquals(600, lu.getFloatValue(), 0.001);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("600px", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrioError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display:block!ie"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIEPrio() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width:60cap!ie"));
		parser.setFlag(Parser2.Flag.IEPRIO);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("60cap!ie", lu.getStringValue());
		assertEquals("60cap!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrio2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width:calc(80% - 3cap) ! ie"));
		parser.setFlag(Parser2.Flag.IEPRIO);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("calc(80% - 3cap)!ie", lu.getStringValue());
		assertEquals("calc(80% - 3cap)!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrio3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width:foo 60cap!ie"));
		parser.setFlag(Parser2.Flag.IEPRIO);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("foo 60cap!ie", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("foo 60cap!ie", lu.toString());
		assertNull(handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display:block!important!"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display:block!important!;"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width:60cap!important!"));
		parser.setFlag(Parser2.Flag.IEPRIOCHAR);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals("60cap", lu.getStringValue());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width:foo 60cap!important!"));
		parser.setFlag(Parser2.Flag.IEPRIOCHAR);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals("foo 60cap", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width:calc(80% - 3cap)!important!"));
		parser.setFlag(Parser2.Flag.IEPRIOCHAR);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals("calc(80% - 3cap)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationTab() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("font-size : larger\t;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedTab() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo : \\9;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\t", lu.getStringValue());
		assertEquals("\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationUnit() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("margin-right: 1px;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_PIXEL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("1px", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationUnitDimension() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("margin-right: 1foo;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_DIMENSION, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("foo", lu.getDimensionUnitText());
		assertEquals("1foo", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationZero() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("margin-right:0;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertEquals("0", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationOneFloat() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:1.0"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationMinusOneFloat() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:-1.0"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationDotOneFloat() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:.1"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(0.1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationMinusDotOneFloat() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:-.1"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_REAL, lu.getLexicalUnitType());
		assertEquals(-0.1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationZIndex() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("z-index:1;"));
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("z-index", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationMargin() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("margin: 0.5em auto;"));
		parser.parseStyleDeclaration(source);
		assertEquals("margin", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_EM, lu.getLexicalUnitType());
		assertEquals(0.5, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals("auto", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBorderColor() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("border-color: blue #a7f31a green;"));
		parser.parseStyleDeclaration(source);
		assertEquals("border-color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(167, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(243, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(26, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("green", lu.getStringValue());
		assertEquals("green", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBorderImage() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round;"));
		parser.parseStyleDeclaration(source);
		assertEquals("border-image", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		assertEquals("url('/img/border.png') 25% 30% 12% 20% fill/2pt/1 round", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(25, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(30, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(12, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(20, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("fill", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_POINT, lu.getLexicalUnitType());
		assertEquals(2, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("round", lu.getStringValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBackgroundImageNone() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("background-image: NONE;"));
		parser.parseStyleDeclaration(source);
		assertEquals("background-image", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("none", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationBackgroundClipIdent() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("background-clip: Content-Box;"));
		parser.parseStyleDeclaration(source);
		assertEquals("background-clip", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("content-box", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleTypeCustomIdent() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("list-style-type: MyStyle;"));
		parser.parseStyleDeclaration(source);
		assertEquals("list-style-type", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("MyStyle", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleCustomIdent() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("list-style: MyStyle;"));
		parser.parseStyleDeclaration(source);
		assertEquals("list-style", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("MyStyle", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleIdent() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("list-style: Upper-Roman;"));
		parser.parseStyleDeclaration(source);
		assertEquals("list-style", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("upper-roman", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationContent() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content: \\f435;"));
		parser.parseStyleDeclaration(source);
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("\\f435", lu.getStringValue()); // Private use character, must be escaped
		assertEquals("\\f435", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content: 'foo';"));
		parser.parseStyleDeclaration(source);
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("'foo'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content: 'foo\\a bar';"));
		parser.parseStyleDeclaration(source);
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("foo\nbar", lu.getStringValue());
		assertEquals("'foo\\a bar'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content: '\\\\5FAE\\8F6F';"));
		parser.parseStyleDeclaration(source);
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\\5FAE\u8F6F", lu.getStringValue());
		assertEquals("'\\\\5FAE\\8F6F'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content:\"\\000A0-\\000A0\""));
		parser.parseStyleDeclaration(source);
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\u00A0-\u00A0", lu.getStringValue());
		assertEquals("\"\\000A0-\\000A0\"", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentBackslash() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content: '\\\\';"));
		parser.parseStyleDeclaration(source);
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("\\", lu.getStringValue());
		assertEquals("'\\\\'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentBackslashBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("content: '\\';"));
		parser.parseStyleDeclaration(source);
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCalc() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("width: calc(100% - 3em)"));
		parser.parseStyleDeclaration(source);
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_PERCENTAGE, param.getLexicalUnitType());
		assertEquals(100f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_EM, param.getLexicalUnitType());
		assertEquals(3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationBezier() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:cubic-bezier(0.33, 0.1, 0.5, 1)"));
		parser.parseStyleDeclaration(source);
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBezierNegativeArg() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:cubic-bezier(-.33, -.1, -1, -.02)"));
		parser.parseStyleDeclaration(source);
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(-0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_INTEGER, param.getLexicalUnitType());
		assertEquals(-1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalUnit.SAC_REAL, param.getLexicalUnitType());
		assertEquals(-0.02f, param.getFloatValue(), 0.001);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(-0.33, -0.1, -1, -0.02)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("filter: --my-function(min-color = 5)"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("--my-function", lu.getFunctionName());
		assertEquals("--my-function(min-color=5)", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("min-color=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction2Error() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("filter: --my-function(min-color =5)"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("filter: --my-function(min-color =5)"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("--my-function", lu.getFunctionName());
		assertEquals("--my-function(min-color=5)", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("min-color=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgidError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals(
				"progid:DXImageTransform.Microsoft.gradient(startColorstr= '#bd0afa', endColorstr= '#d0df9f')",
				lu.toString());
		assertEquals("progid:DXImageTransform.Microsoft.gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("startColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("endColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("#d0df9f", lu.getStringValue());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid2Error() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f);"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f);"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("GradientType=0", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("StartColorStr=#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("EndColorStr=#d0df9f", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid3Error() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("filter: progid:DXImageTransform.Microsoft.Blur(pixelradius=5)"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid3() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("filter: progid:DXImageTransform.Microsoft.Blur(pixelradius=5)"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("pixelradius=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid4Error() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"_filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=scale,src='http://www.example.com/images/myimage.png');"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"_filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=scale,src='http://www.example.com/images/myimage.png');"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("_filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.AlphaImageLoader", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("enabled=true", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("sizingMethod=scale", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("src=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("http://www.example.com/images/myimage.png", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgidEscaped() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("filter: progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)"));
		parser.parseStyleDeclaration(source);
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpressionError() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("top:expression(iequirk = (document.body.scrollTop) + \"px\" )"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpressionError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("top:expression(= (document.body.scrollTop) + \"px\" )"));
		parser.setFlag(Parser2.Flag.IEVALUES); // Must throw exception despite the flag
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpression() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("top:expression(iequirk = (document.body.scrollTop) + \"px\" )"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals("top", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("iequirk=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_SUB_EXPRESSION, lu.getLexicalUnitType());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_IDENT, subv.getLexicalUnitType());
		assertEquals("document.body.scrollTop", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_PLUS, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("px", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2Error() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("zoom:expression(this.runtimeStyle['zoom'] = '1',this.innerHTML = '&#xe03a;')"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("zoom:expression(this.runtimeStyle['zoom']='1',this.innerHTML='&#xe03a;')"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("zoom", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("expression(this\\.runtimeStyle['zoom'] = '1', this.innerHTML= '&#xe03a;')", lu.toString());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("this.runtimeStyle", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("zoom", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("1", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("this.innerHTML=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("&#xe03a;", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2WS() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("zoom:expression(this.runtimeStyle['zoom'] = '1',this.innerHTML = '&#xe03a;')"));
		parser.setFlag(Parser2.Flag.IEVALUES);
		parser.parseStyleDeclaration(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("zoom", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("expression(this\\.runtimeStyle['zoom'] = '1', this.innerHTML= '&#xe03a;')", lu.toString());
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("this.runtimeStyle", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("zoom", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("1", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals("this.innerHTML=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_STRING_VALUE, lu.getLexicalUnitType());
		assertEquals("&#xe03a;", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpression3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"top:expression(eval(document.documentElement.scrollTop+(document.documentElement.clientHeight-this.offsetHeight)))"));
		parser.parseStyleDeclaration(source);
		assertEquals("top", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertEquals("eval", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("document.documentElement.scrollTop", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_PLUS, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_SUB_EXPRESSION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalUnit.SAC_IDENT, subv.getLexicalUnitType());
		assertEquals("document.documentElement.clientHeight-this.offsetHeight", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSquareBrackets() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("grid-template-rows: [header-top]"));
		parser.parseStyleDeclaration(source);
		assertEquals("grid-template-rows", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit2.SAC_LEFT_BRACKET, lu.getLexicalUnitType());
		assertEquals("[header-top]", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("header-top", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_RIGHT_BRACKET, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSquareBrackets2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("grid-template-rows: repeat(1, [] 10px)"));
		parser.parseStyleDeclaration(source);
		assertEquals("grid-template-rows", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("repeat(1,[] 10px)", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit2.SAC_RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_PIXEL, lu.getLexicalUnitType());
		assertEquals(10f, lu.getFloatValue(), 0.001f);
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSquareBracketsBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("[grid-template-rows: [header-top]"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		errorHandler.reset();
		source = new InputSource(new StringReader("grid-template-rows]: [header-top]"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		errorHandler.reset();
		source = new InputSource(new StringReader("[grid-template-rows]: [header-top]"));
		parser.parseStyleDeclaration(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}
}
