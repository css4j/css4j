/*

 Copyright (c) 2005-2020, Carlos Amengual.

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

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;

public class DeclarationParserTest {

	private Parser parser;
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
	public void testParseStyleDeclarationEOF() throws CSSException, IOException {
		parseStyleDeclaration("font-family: Times New Roman");
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		assertEquals("Times New Roman", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEOFBad() throws CSSException, IOException {
		parseStyleDeclaration("color");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEOFBad2() throws CSSException, IOException {
		parseStyleDeclaration("color:");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEOFBad3() throws CSSException, IOException {
		parseStyleDeclaration("color :");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEmptyEOF() throws CSSException, IOException {
		parseStyleDeclaration("--Box-shadow-inset:");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEmptyWS_EOF() throws CSSException, IOException {
		parseStyleDeclaration("--Box-shadow-inset :");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEmptyWS_WS_EOF() throws CSSException, IOException {
		parseStyleDeclaration("--Box-shadow-inset : ");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationColorOver255() throws CSSException, IOException {
		parseStyleDeclaration("color:rgb(300,400,500);");
		assertEquals("color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(300, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(400, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(500, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationColorRealOver255() throws CSSException, IOException {
		parseStyleDeclaration("color:rgb(300.1 400.2 500.3);");
		assertEquals("color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(300.1f, param.getFloatValue(), 1e-6);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(400.2f, param.getFloatValue(), 1e-6);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(500.3f, param.getFloatValue(), 1e-6);
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor() throws CSSException, IOException {
		parseStyleDeclaration("color: #;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor2() throws CSSException, IOException {
		parseStyleDeclaration("color: #");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor3() throws CSSException, IOException {
		parseStyleDeclaration("color: #x");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor4() throws CSSException, IOException {
		parseStyleDeclaration("color: #,");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor5() throws CSSException, IOException {
		parseStyleDeclaration("color: #:");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor6() throws CSSException, IOException {
		parseStyleDeclaration("color: #@charset");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor7() throws CSSException, IOException {
		parseStyleDeclaration("color: #-");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor8() throws CSSException, IOException {
		parseStyleDeclaration("color: #_");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor9() throws CSSException, IOException {
		parseStyleDeclaration("color: #.");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor10() throws CSSException, IOException {
		parseStyleDeclaration("color: ##");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor11() throws CSSException, IOException {
		parseStyleDeclaration("color: foo #;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadColor12() throws CSSException, IOException {
		parseStyleDeclaration("color: foo #");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc() throws CSSException, IOException {
		parseStyleDeclaration("width: calc(100% - 3em");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc2() throws CSSException, IOException {
		parseStyleDeclaration("width: calc(100% - 3em;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc3() throws CSSException, IOException {
		parseStyleDeclaration("width: calc(100% -");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadCalc4() throws CSSException, IOException {
		parseStyleDeclaration("width: calc(100% -;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl() throws CSSException, IOException {
		parseStyleDeclaration("background-image: url(http://www.example.com/");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl2() throws CSSException, IOException {
		parseStyleDeclaration("background-image: url(http://www.example.com/;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl3() throws CSSException, IOException {
		parseStyleDeclaration("background-image: url(");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadUrl4() throws CSSException, IOException {
		parseStyleDeclaration("background-image: url(;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBad() throws IOException {
		parseStyleDeclaration("list-style: @");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBad2() throws IOException {
		parseStyleDeclaration("list-style;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBad3() throws IOException {
		parseStyleDeclaration("list-style:;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEmpty() throws IOException {
		parseStyleDeclaration("--My-property:;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--My-property", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier() throws IOException {
		parseStyleDeclaration("list-style: -9foo_bar");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier2() throws IOException {
		parseStyleDeclaration("list-style: 9foo_bar");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier3() throws IOException {
		parseStyleDeclaration("list-style: foo\0");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier4() throws IOException {
		parseStyleDeclaration("list-style: foo\u0000");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier5() throws IOException {
		parseStyleDeclaration("list-style: foo\u0007");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclaration2() throws CSSException, IOException {
		parseStyleDeclaration("font-family : Times New Roman ; color : yellow ; ");
		assertEquals(2, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("yellow", lu.getStringValue());
		assertEquals("yellow", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclaration3() throws CSSException, IOException {
		parseStyleDeclaration("font-family: Times New Roman; color: yellow; width: calc(100% - 3em);");
		assertEquals(3, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals("width", handler.propertyNames.get(2));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("yellow", lu.getStringValue());
		lu = handler.lexicalValues.get(2);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationFont() throws CSSException, IOException {
		parseStyleDeclaration("font:bold 14px/32px \"Courier New\", Arial, sans-serif");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("font", handler.propertyNames.getFirst());
		LexicalUnit lunit = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lunit.getLexicalUnitType());
		assertEquals("bold", lunit.getStringValue());
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(14f, lu.getFloatValue(), 1e-7);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(32f, lu.getFloatValue(), 1e-7);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("Courier New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("sans-serif", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("bold 14px/32px \"Courier New\", Arial, sans-serif", lunit.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationComments() throws CSSException, IOException {
		parseStyleDeclaration(
				"-moz-foo:moz-bar;/*!rtl:ignore*/-o-foo:o-bar;/*!rtl:ignore*/foo:bar/*(skipped)!rtl:ignore*/;/*!rtl:ignore*/");
		assertEquals(3, handler.propertyNames.size());
		assertEquals("-moz-foo", handler.propertyNames.getFirst());
		assertEquals("-o-foo", handler.propertyNames.get(1));
		assertEquals("foo", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("moz-bar", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("o-bar", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		lu = handler.lexicalValues.get(2);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
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
		parseStyleDeclaration("width: calc(100% - 3em !important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant2() throws CSSException, IOException {
		parseStyleDeclaration("color: rgb(128, 0, 97 !important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant3() throws CSSException, IOException {
		parseStyleDeclaration("color: # !important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBadImportant4() throws CSSException, IOException {
		parseStyleDeclaration("color: #!important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRange() throws CSSException, IOException {
		parseStyleDeclaration("unicode-range: U+416");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+416", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(1046, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRange2() throws CSSException, IOException {
		parseStyleDeclaration("unicode-range: U+0025-00FF");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+25-ff", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeWildcard() throws CSSException, IOException {
		parseStyleDeclaration("unicode-range: U+4??");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+4??", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeList() throws CSSException, IOException {
		parseStyleDeclaration("unicode-range: U+022, U+0025-00FF, U+4??, U+FF00");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+22, U+25-ff, U+4??, U+ff00", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(34, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(37, subv.getIntegerValue());
		subv = subv.getNextLexicalUnit();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(255, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		subv = lu.getSubValues();
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(65280, subv.getIntegerValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeWildcard2() throws CSSException, IOException {
		parseStyleDeclaration("unicode-range: U+???");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("???", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationRangeBadWildcard() throws CSSException, IOException {
		parseStyleDeclaration("unicode-range: U+??????");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedIdentifier() throws IOException {
		parseStyleDeclaration("padding-bottom: \\35 px\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-bottom", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedIdentifierBackslash() throws IOException {
		parseStyleDeclaration("--foo: j\\\\");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("j\\", lu.getStringValue());
		assertEquals("j\\\\", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedProperty() throws CSSException, IOException {
		parseStyleDeclaration("-\\31 zzz\\:_:1;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("-1zzz:_", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
		assertEquals(12, errorHandler.warningException.getColumnNumber());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyBad() throws CSSException, IOException {
		parseStyleDeclaration("foo:-\\31zzz\\:_");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-1zzz:_", lu.getStringValue());
		assertEquals("-\\31zzz\\:_", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue() throws CSSException, IOException {
		parseStyleDeclaration("symbols: \\1F44D;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1F44D", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue2() throws CSSException, IOException {
		parseStyleDeclaration("filter: \\:foo;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(":foo", lu.getStringValue());
		assertEquals("\\:foo", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue3() throws CSSException, IOException {
		parseStyleDeclaration("display: block\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block\t", lu.getStringValue());
		assertEquals("block\\9", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue4() throws CSSException, IOException {
		parseStyleDeclaration("display: bl\\9 ock");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bl\tock", lu.getStringValue());
		assertEquals("bl\\9 ock", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue5() throws CSSException, IOException {
		parseStyleDeclaration("display: -\\9 block");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-\tblock", lu.getStringValue());
		assertEquals("-\\9 block", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue6() throws CSSException, IOException {
		parseStyleDeclaration("font-family: \\5FAE\\8F6F\\96C5\\9ED1,Arial,\\5b8b\\4f53,sans-serif");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("微软雅黑", lu.getStringValue());
		assertEquals("\\5FAE\\8F6F\\96C5\\9ED1, Arial, \\5b8b\\4f53, sans-serif", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("宋体", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("sans-serif", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue7() throws CSSException, IOException {
		parseStyleDeclaration("font-family: \\\\5FAE\\8F6F\\96C5\\9ED1,Arial");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\5FAE软雅黑", lu.getStringValue());
		assertEquals("\\\\5FAE\\8F6F\\96C5\\9ED1, Arial", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue8() throws CSSException, IOException {
		parseStyleDeclaration("font-family: \"\u5b8b\u4f53\",Arial");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\u5b8b\u4f53", lu.getStringValue());
		assertEquals("\"\u5b8b\u4f53\", Arial", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue9() throws CSSException, IOException {
		parseStyleDeclaration("font-family:file\\:\\/\\/\\/dir\\/file");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("file:///dir/file", lu.getStringValue());
		assertEquals("file\\:\\/\\/\\/dir\\/file", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue10() throws CSSException, IOException {
		parseStyleDeclaration("font-family:file\\:c\\:\\\\\\\\dir\\\\file");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("file:c:\\\\dir\\file", lu.getStringValue());
		assertEquals("file\\:c\\:\\\\\\\\dir\\\\file", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue11() throws CSSException, IOException {
		parseStyleDeclaration("list-style-type:symbols('*' '\\2020' '\\2021' '\\A7');");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("list-style-type", handler.propertyNames.getFirst());
		LexicalUnit lunit = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lunit.getLexicalUnitType());
		assertEquals("symbols", lunit.getFunctionName());
		LexicalUnit lu = lunit.getParameters();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("*", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\u2020", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\u2021", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\u00a7", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("symbols('*' '\\2020' '\\2021' '\\A7')", lunit.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackError() throws CSSException, IOException {
		parseStyleDeclaration("width: 600px\\9");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("width: 600px\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("600px\\9", lu.getStringValue());
		assertEquals("600px\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("width: 600px\\0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("600px\\0", lu.getStringValue());
		assertEquals("600px\\0", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack3Error() throws CSSException, IOException {
		parseStyleDeclaration("width: calc(80% - 3cap)\\9");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack3() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("width: calc(80% - 3cap)\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("calc(80% - 3cap)\\9", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("calc(80% - 3cap)\\9", lu.toString());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack4() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("color: #000\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("#000\\9", lu.getStringValue());
		assertEquals("#000\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationNoBackslashHack() throws CSSException, IOException {
		parseStyleDeclaration("font-family: Times New Roman\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("Times New Roman\\9", lu.toString());
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
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
		parseStyleDeclaration("font-family: Times New Roman\\0/");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times New Roman\\0/", lu.toString());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman\ufffd", lu.getStringValue());
		assertEquals("Roman\\0/", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackFontFamily2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("font-family: Times New Roman\\0/");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times New Roman\\0/", lu.toString());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("New Roman\\0", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError() throws CSSException, IOException {
		parseStyleDeclaration("color\0:#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError2() throws CSSException, IOException {
		parseStyleDeclaration("color\u0007:#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError3() throws CSSException, IOException {
		parseStyleDeclaration("color#:#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError4() throws CSSException, IOException {
		parseStyleDeclaration("#color:#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError5() throws CSSException, IOException {
		// Test the recovery from previous error
		parseStyleDeclaration("#color:#ff0;margin-right:0");
		assertTrue(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError6() throws CSSException, IOException {
		parseStyleDeclaration("color color:#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError7() throws CSSException, IOException {
		parseStyleDeclaration("9color:#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameStringError() throws CSSException, IOException {
		parseStyleDeclaration("\"color\":#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameStringError2() throws CSSException, IOException {
		parseStyleDeclaration("background-\"color\":#ff0");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameWarning() throws CSSException, IOException {
		parseStyleDeclaration("\\30 color:#ff0");
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("0color", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#ff0", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameWarningWS() throws CSSException, IOException {
		parseStyleDeclaration("\\30 color :#ff0");
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("0color", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#ff0", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameBackslashHack() throws CSSException, IOException {
		parseStyleDeclaration("color\\9:#ff0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color\u0009", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(255, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(0, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("#ff0", lu.toString());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationStarHackError() throws CSSException, IOException {
		parseStyleDeclaration("*width:600px");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationStarHack() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.STARHACK);
		parseStyleDeclaration("*width:60cap");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_CAP, lu.getCssUnit());
		assertEquals(60, lu.getFloatValue(), 0.001);
		assertEquals("cap", lu.getDimensionUnitText());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationStarHack2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.STARHACK);
		parseStyleDeclaration("display:block;*width:600px");
		assertEquals(2, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("*width", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(600, lu.getFloatValue(), 0.001);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("600px", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrioError() throws CSSException, IOException {
		parseStyleDeclaration("display:block!ie");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIEPrio() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:60cap!ie");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("60cap!ie", lu.getStringValue());
		assertEquals("60cap!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrio2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:calc(80% - 3cap) ! ie");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("calc(80% - 3cap)!ie", lu.getStringValue());
		assertEquals("calc(80% - 3cap)!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrio3() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:foo 60cap!ie");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("foo 60cap!ie", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("foo 60cap!ie", lu.toString());
		assertNull(handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioError() throws CSSException, IOException {
		parseStyleDeclaration("display:block!important!");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioError2() throws CSSException, IOException {
		parseStyleDeclaration("display:block!important!;");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:60cap!important!");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("60cap", lu.getStringValue());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:foo 60cap!important!");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("foo 60cap", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio3() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:calc(80% - 3cap)!important!");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("calc(80% - 3cap)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.getFirst());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationTab() throws CSSException, IOException {
		parseStyleDeclaration("font-size : larger\t;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEscapedTab() throws CSSException, IOException {
		parseStyleDeclaration("foo : \\9;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\t", lu.getStringValue());
		assertEquals("\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationUnit() throws CSSException, IOException {
		parseStyleDeclaration("margin-right: 1px;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("1px", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationUnitDimension() throws CSSException, IOException {
		parseStyleDeclaration("margin-right: 1foo;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("foo", lu.getDimensionUnitText());
		assertEquals("1foo", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationZero() throws CSSException, IOException {
		parseStyleDeclaration("margin-right:0;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertEquals("0", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationOneFloat() throws CSSException, IOException {
		parseStyleDeclaration("foo:1.0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationMinusOneFloat() throws CSSException, IOException {
		parseStyleDeclaration("foo:-1.0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationDotOneFloat() throws CSSException, IOException {
		parseStyleDeclaration("foo:.1");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationMinusDotOneFloat() throws CSSException, IOException {
		parseStyleDeclaration("foo:-.1");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-0.1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationZIndex() throws CSSException, IOException {
		parseStyleDeclaration("z-index:1;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("z-index", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationMargin() throws CSSException, IOException {
		parseStyleDeclaration("margin: 0.5em auto;");
		assertEquals("margin", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(0.5, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals("auto", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBorderColor() throws CSSException, IOException {
		parseStyleDeclaration("border-color: blue #a7f31a green;");
		assertEquals("border-color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(167, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(243, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(26, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("green", lu.getStringValue());
		assertEquals("green", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBorderImage() throws CSSException, IOException {
		parseStyleDeclaration("border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round;");
		assertEquals("border-image", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		assertEquals("url('/img/border.png') 25% 30% 12% 20% fill/2pt/1 round", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(25, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(30, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(12, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(20, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("fill", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, lu.getCssUnit());
		assertEquals(2, lu.getFloatValue(), 0.01);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("round", lu.getStringValue());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBackgroundImageGradient() throws CSSException, IOException {
		parseStyleDeclaration("background-image: linear-gradient(35deg);");
		assertEquals("background-image", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("linear-gradient", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(35, param.getFloatValue(), 1e-6);
		assertNull(param.getNextLexicalUnit());
		assertEquals("linear-gradient(35deg)", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationBackgroundImageNone() throws CSSException, IOException {
		parseStyleDeclaration("background-image: NONE;");
		assertEquals("background-image", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("none", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationBackgroundClipIdent() throws CSSException, IOException {
		parseStyleDeclaration("background-clip: Content-Box;");
		assertEquals("background-clip", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("content-box", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleTypeCustomIdent() throws CSSException, IOException {
		parseStyleDeclaration("list-style-type: MyStyle;");
		assertEquals("list-style-type", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("MyStyle", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleCustomIdent() throws CSSException, IOException {
		parseStyleDeclaration("list-style: MyStyle;");
		assertEquals("list-style", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("MyStyle", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleIdent() throws CSSException, IOException {
		parseStyleDeclaration("list-style: Upper-Roman;");
		assertEquals("list-style", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("upper-roman", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationContent() throws CSSException, IOException {
		parseStyleDeclaration("content: \\f435;");
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\f435", lu.getStringValue()); // Private use character, must be escaped
		assertEquals("\\f435", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString() throws CSSException, IOException {
		parseStyleDeclaration("content: 'foo';");
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("'foo'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString2() throws CSSException, IOException {
		parseStyleDeclaration("content: 'foo\\a bar';");
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo\nbar", lu.getStringValue());
		assertEquals("'foo\\a bar'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString3() throws CSSException, IOException {
		parseStyleDeclaration("content: '\\\\5FAE\\8F6F';");
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\\5FAE\u8F6F", lu.getStringValue());
		assertEquals("'\\\\5FAE\\8F6F'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString4() throws CSSException, IOException {
		parseStyleDeclaration("content:\"\\000A0-\\000A0\"");
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\u00A0-\u00A0", lu.getStringValue());
		assertEquals("\"\\000A0-\\000A0\"", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentBackslash() throws CSSException, IOException {
		parseStyleDeclaration("content: '\\\\';");
		assertEquals("content", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\\", lu.getStringValue());
		assertEquals("'\\\\'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentBackslashBad() throws CSSException, IOException {
		parseStyleDeclaration("content: '\\';");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCalc() throws CSSException, IOException {
		parseStyleDeclaration("width: calc(100% - 3em)");
		assertEquals("width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("calc", lu.getFunctionName());
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 0.01);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 0.01);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationBezier() throws CSSException, IOException {
		parseStyleDeclaration("foo:cubic-bezier(0.33, 0.1, 0.5, 1)");
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBezierNegativeArg() throws CSSException, IOException {
		parseStyleDeclaration("foo:cubic-bezier(-.33, -.1, -1, -.02)");
		assertEquals("foo", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.33f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.02f, param.getFloatValue(), 0.001);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(-0.33, -0.1, -1, -0.02)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSteps() throws CSSException, IOException {
		parseStyleDeclaration("animation-timing-function:steps(2, start)");
		assertEquals("animation-timing-function", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("steps", lu.getFunctionName());
		assertEquals(LexicalType.STEPS_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(2, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("start", param.getStringValue());
		assertNull(param.getNextLexicalUnit());
		assertEquals("steps(2, start)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEmptyFunction() throws CSSException, IOException {
		parseStyleDeclaration("filter:mask()");
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("mask", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertNull(lu.getParameters());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("filter: --my-function(min-color = 5)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("--my-function", lu.getFunctionName());
		assertEquals("--my-function(min-color=5)", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("min-color=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction2Error() throws CSSException, IOException {
		parseStyleDeclaration("filter: --my-function(min-color =5)");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("filter: --my-function(min-color =5)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("--my-function", lu.getFunctionName());
		assertEquals("--my-function(min-color=5)", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("min-color=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgidError() throws CSSException, IOException {
		parseStyleDeclaration(
				"filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(startColorstr= '#bd0afa', endColorstr= '#d0df9f')",
				lu.toString());
		assertEquals("progid:DXImageTransform.Microsoft.gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("startColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("endColorstr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("#d0df9f", lu.getStringValue());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid2Error() throws CSSException, IOException {
		parseStyleDeclaration(
				"filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f);");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f);");
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Gradient", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("GradientType=0", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("StartColorStr=#bd0afa", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("EndColorStr=#d0df9f", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid3Error() throws CSSException, IOException {
		parseStyleDeclaration("filter: progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid3() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("filter: progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.Blur", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("pixelradius=5", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid4Error() throws CSSException, IOException {
		parseStyleDeclaration(
				"_filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=scale,src='http://www.example.com/images/myimage.png');");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid4() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"_filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=scale,src='http://www.example.com/images/myimage.png');");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("_filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.AlphaImageLoader", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("enabled=true", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("sizingMethod=scale", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("src=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("http://www.example.com/images/myimage.png", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgidEscaped() throws CSSException, IOException {
		parseStyleDeclaration("filter: progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals("filter", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpressionError() throws CSSException, IOException {
		parseStyleDeclaration("top:expression(iequirk = (document.body.scrollTop) + \"px\" )");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpressionError2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES); // Must throw exception despite the flag
		parseStyleDeclaration("top:expression(= (document.body.scrollTop) + \"px\" )");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpression() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("top:expression(iequirk = (document.body.scrollTop) + \"px\" )");
		assertEquals("top", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("iequirk=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.SUB_EXPRESSION, lu.getLexicalUnitType());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.IDENT, subv.getLexicalUnitType());
		assertEquals("document.body.scrollTop", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_PLUS, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("px", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2Error() throws CSSException, IOException {
		parseStyleDeclaration("zoom:expression(this.runtimeStyle['zoom'] = '1',this.innerHTML = '&#xe03a;')");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("zoom:expression(this.runtimeStyle['zoom']='1',this.innerHTML='&#xe03a;')");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("zoom", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("expression(this\\.runtimeStyle['zoom'] = '1', this.innerHTML= '&#xe03a;')", lu.toString());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("this.runtimeStyle", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("zoom", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("1", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("this.innerHTML=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("&#xe03a;", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2WS() throws CSSException, IOException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("zoom:expression(this.runtimeStyle['zoom'] = '1',this.innerHTML = '&#xe03a;')");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("zoom", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals("expression(this\\.runtimeStyle['zoom'] = '1', this.innerHTML= '&#xe03a;')", lu.toString());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("this.runtimeStyle", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("zoom", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("1", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("this.innerHTML=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("&#xe03a;", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpression3() throws CSSException, IOException {
		parseStyleDeclaration(
				"top:expression(eval(document.documentElement.scrollTop+(document.documentElement.clientHeight-this.offsetHeight)))");
		assertEquals("top", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("expression", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("eval", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("document.documentElement.scrollTop", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_PLUS, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.SUB_EXPRESSION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.IDENT, subv.getLexicalUnitType());
		assertEquals("document.documentElement.clientHeight-this.offsetHeight", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSquareBrackets() throws CSSException, IOException {
		parseStyleDeclaration("grid-template-rows: [header-top]");
		assertEquals("grid-template-rows", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		assertEquals("[header-top]", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("header-top", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSquareBrackets2() throws CSSException, IOException {
		parseStyleDeclaration("grid-template-rows: repeat(1, [] 10px)");
		assertEquals("grid-template-rows", handler.propertyNames.getFirst());
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("repeat(1,[] 10px)", lu.toString());
		lu = lu.getParameters();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(10f, lu.getFloatValue(), 0.001f);
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSquareBracketsBad() throws CSSException, IOException {
		parseStyleDeclaration("[grid-template-rows: [header-top]");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		errorHandler.reset();
		parseStyleDeclaration("grid-template-rows]: [header-top]");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		errorHandler.reset();
		parseStyleDeclaration("[grid-template-rows]: [header-top]");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
	}

	private void parseStyleDeclaration(String string) throws CSSParseException, IOException {
		parser.parseStyleDeclaration(new StringReader(string));
		assertTrue(handler.streamEnded);
	}

}
