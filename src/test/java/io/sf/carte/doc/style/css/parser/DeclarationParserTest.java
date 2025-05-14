/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.StringList;
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

	@BeforeEach
	public void setUp() {
		handler = new TestDeclarationHandler();
		errorHandler = new TestErrorHandler();
		parser = new CSSParser();
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(errorHandler);
	}

	@Test
	public void testParseStyleDeclarationEOF() throws CSSException {
		parseStyleDeclaration("font-family: Times New Roman");
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		assertEquals("Times New Roman", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEOFBad() throws CSSException {
		parseStyleDeclaration("color");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEOFBad2() throws CSSException {
		parseStyleDeclaration("color:");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEOFBad3() throws CSSException {
		parseStyleDeclaration("color :");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationEmptyEOF() throws CSSException {
		parseStyleDeclaration("--Box-shadow-inset:");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmptyCommentEOF() throws CSSException {
		parseStyleDeclaration("--Box-shadow-inset:/* empty */");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());

		StringList comments = lu.getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.getLength());
		assertEquals(" empty ", comments.item(0));

		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmptyWS_EOF() throws CSSException {
		parseStyleDeclaration("--Box-shadow-inset :");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmptyWS_WS_EOF() throws CSSException {
		parseStyleDeclaration("--Box-shadow-inset : ");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmptyWS_CommentEOF() throws CSSException {
		parseStyleDeclaration("--Box-shadow-inset: /* empty */ ");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--Box-shadow-inset", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());

		StringList comments = lu.getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.getLength());
		assertEquals(" empty ", comments.item(0));

		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationColorOver255() throws CSSException {
		parseStyleDeclaration("color:rgb(300,400,500);");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationColorRealOver255() throws CSSException {
		parseStyleDeclaration("color:rgb(300.1 400.2 500.3);");
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("rgb", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(300.1f, param.getFloatValue(), 1e-6f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(400.2f, param.getFloatValue(), 1e-6f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(500.3f, param.getFloatValue(), 1e-6f);
		assertNull(param.getNextLexicalUnit());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor() throws CSSException {
		parseStyleDeclaration("color: #;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor2() throws CSSException {
		parseStyleDeclaration("color: #");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor3() throws CSSException {
		parseStyleDeclaration("color: #x");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor4() throws CSSException {
		parseStyleDeclaration("color: #,");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor5() throws CSSException {
		parseStyleDeclaration("color: #:");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor6() throws CSSException {
		parseStyleDeclaration("color: #@charset");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor7() throws CSSException {
		parseStyleDeclaration("color: #-");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor8() throws CSSException {
		parseStyleDeclaration("color: #_");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor9() throws CSSException {
		parseStyleDeclaration("color: #.");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor10() throws CSSException {
		parseStyleDeclaration("color: ##");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor11() throws CSSException {
		parseStyleDeclaration("color: foo #;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadColor12() throws CSSException {
		parseStyleDeclaration("color: foo #");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadCalc() throws CSSException {
		parseStyleDeclaration("width: calc(100% - 3em");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadCalc2() throws CSSException {
		parseStyleDeclaration("width: calc(100% - 3em;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadCalc3() throws CSSException {
		parseStyleDeclaration("width: calc(100% -");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadCalc4() throws CSSException {
		parseStyleDeclaration("width: calc(100% -;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadUrl() throws CSSException {
		parseStyleDeclaration("background-image: url(http://www.example.com/");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadUrl2() throws CSSException {
		parseStyleDeclaration("background-image: url(http://www.example.com/;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadUrl3() throws CSSException {
		parseStyleDeclaration("background-image: url(");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadUrl4() throws CSSException {
		parseStyleDeclaration("background-image: url(;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadUrlModifier() throws CSSException {
		parseStyleDeclaration(
				"background-image: url('image.png' format(+;width:0;));height:100px;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());

		assertEquals("height", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(100f, lu.getFloatValue(), 1e-5f);
		assertNull(lu.getNextLexicalUnit());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(43, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleDeclarationBad() {
		parseStyleDeclaration("list-style: @");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBad2() {
		parseStyleDeclaration("list-style;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBad3() {
		parseStyleDeclaration("list-style:;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmpty() {
		parseStyleDeclaration("--My-property:;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--My-property", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmptyComment() {
		parseStyleDeclaration("--My-property:/* empty */;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--My-property", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.EMPTY, lu.getLexicalUnitType());
		assertEquals("", lu.getStringValue());
		assertEquals("", lu.toString());

		StringList comments = lu.getPrecedingComments();
		assertNotNull(comments);
		assertEquals(1, comments.getLength());
		assertEquals(" empty ", comments.item(0));

		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier() {
		parseStyleDeclaration("list-style: -9foo_bar");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier2() {
		parseStyleDeclaration("list-style: 9foo_bar");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier3() {
		parseStyleDeclaration("list-style: foo\0");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier4() {
		parseStyleDeclaration("list-style: foo\u0000");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadIdentifier5() {
		parseStyleDeclaration("list-style: foo\u0007");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclaration2() throws CSSException {
		parseStyleDeclaration("font-family : Times New Roman ; color : yellow ; ");
		assertEquals(2, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclaration3() throws CSSException {
		parseStyleDeclaration(
				"font-family: Times New Roman; color: yellow; width: calc(100% - 3em);");
		assertEquals(3, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals("width", handler.propertyNames.get(2));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationFont() throws CSSException {
		parseStyleDeclaration("font:bold 14px/32px \"Courier New\", Arial, sans-serif");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("font", handler.propertyNames.get(0));
		LexicalUnit lunit = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lunit.getLexicalUnitType());
		assertEquals("bold", lunit.getStringValue());
		LexicalUnit lu = lunit.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(14f, lu.getFloatValue(), 1e-7f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(32f, lu.getFloatValue(), 1e-7f);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationComments() throws CSSException {
		parseStyleDeclaration(
				"-moz-foo:moz-bar;/*!rtl:ignore*/-o-foo:o-bar;/*!rtl:ignore*/foo:bar/*(skipped)!rtl:ignore*/;/*!rtl:ignore*/");
		assertEquals(3, handler.propertyNames.size());
		assertEquals("-moz-foo", handler.propertyNames.get(0));
		assertEquals("-o-foo", handler.propertyNames.get(1));
		assertEquals("foo", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertEquals("(skipped)!rtl:ignore", lu.getTrailingComments().item(0));
		assertNull(lu.getNextLexicalUnit());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore", handler.comments.get(0));
		assertEquals("!rtl:ignore", handler.comments.get(1));
		assertEquals("!rtl:ignore", handler.comments.get(2));

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationImportant() throws CSSException {
		parseStyleDeclaration("symbols: \\1F44D!important;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1f44d ", lu.toString());
		assertEquals(1, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationImportantEscaped() throws CSSException {
		parseStyleDeclaration("symbols: \\1F44D!i\\6dportant;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1f44d ", lu.toString());
		assertEquals(1, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationImportantEscaped2() throws CSSException {
		parseStyleDeclaration("symbols: \\1F44D!\\49\\6dportan\\74;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1f44d ", lu.toString());
		assertEquals(1, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationImportantEscaped3() throws CSSException {
		parseStyleDeclaration("color:#ddd!\\49 \\6d portan\\74;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#ddd", lu.toString());
		assertEquals(1, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationImportantEscaped4() throws CSSException {
		parseStyleDeclaration("color:#ddd! \\49 \\6d portan\\74;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#ddd", lu.toString());
		assertEquals(1, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportant() throws CSSException {
		parseStyleDeclaration("width: calc(100% - 3em !important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportant2() throws CSSException {
		parseStyleDeclaration("color: rgb(128, 0, 97 !important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportant3() throws CSSException {
		parseStyleDeclaration("color: # !important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportant4() throws CSSException {
		parseStyleDeclaration("color: #!important;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportant5() throws CSSException {
		parseStyleDeclaration("margin: 1em!imp;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportant6() throws CSSException {
		parseStyleDeclaration("margin: 1em!important!;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBadImportantDoubleEscape() throws CSSException {
		parseStyleDeclaration("margin: 1em!\\\\69mportant;");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationRange() throws CSSException {
		parseStyleDeclaration("unicode-range: U+416");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+416", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.INTEGER, subv.getLexicalUnitType());
		assertEquals(1046, subv.getIntegerValue());
		assertNull(subv.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationRange2() throws CSSException {
		parseStyleDeclaration("unicode-range: U+0025-00FF");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationRangeWildcard() throws CSSException {
		parseStyleDeclaration("unicode-range: U+4??");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+4??", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("4??", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationRangeList() throws CSSException {
		parseStyleDeclaration("unicode-range: U+022, U+0025-00FF, U+4??, U+FF00");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationRangeWildcard2() throws CSSException {
		parseStyleDeclaration("unicode-range: U+???");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("unicode-range", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.UNICODE_RANGE, lu.getLexicalUnitType());
		assertEquals("U+???", lu.toString());
		LexicalUnit subv = lu.getSubValues();
		assertNotNull(subv);
		assertEquals(LexicalType.UNICODE_WILDCARD, subv.getLexicalUnitType());
		assertEquals("???", subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationRangeBadWildcard() throws CSSException {
		parseStyleDeclaration("unicode-range: U+??????");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedIdentifier() {
		parseStyleDeclaration("padding-bottom: \\35 px\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-bottom", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("5px\t", lu.getStringValue());
		assertEquals("\\35 px\\9 ", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedIdentifierBackslash() {
		parseStyleDeclaration("--foo: j\\\\");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("j\\", lu.getStringValue());
		assertEquals("j\\\\", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedProperty() throws CSSException {
		parseStyleDeclaration("-\\31 zzz\\:_:1;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("-1zzz:_", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
		assertEquals(12, errorHandler.warningException.getColumnNumber());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyBad() throws CSSException {
		parseStyleDeclaration("foo:-\\31zzz\\:_");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-1zzz:_", lu.getStringValue());
		assertEquals("\\-1zzz\\:_", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue() throws CSSException {
		parseStyleDeclaration("symbols: \\1F44D;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\uD83D\uDC4D", lu.getStringValue());
		assertEquals("\\1f44d ", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue2() throws CSSException {
		parseStyleDeclaration("filter: \\:foo;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals(":foo", lu.getStringValue());
		assertEquals("\\:foo", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue3() throws CSSException {
		parseStyleDeclaration("display: block\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block\t", lu.getStringValue());
		assertEquals("block\\9 ", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue4() throws CSSException {
		parseStyleDeclaration("display: bl\\9 ock");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bl\tock", lu.getStringValue());
		assertEquals("bl\\9 ock", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue5() throws CSSException {
		parseStyleDeclaration("display: -\\9 block");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("-\tblock", lu.getStringValue());
		assertEquals("-\\9 block", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue6() throws CSSException {
		parseStyleDeclaration(
				"font-family: \\5FAE\\8F6F\\96C5\\9ED1,Arial,\\5b8b\\4f53,sans-serif");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("微软雅黑", lu.getStringValue());
		assertEquals("\\5fae\\8f6f\\96c5\\9ed1 , Arial, \\5b8b\\4f53 , sans-serif", lu.toString());
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue7() throws CSSException {
		parseStyleDeclaration("font-family: \\\\5FAE\\8F6F\\96C5\\9ED1,Arial");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\5FAE软雅黑", lu.getStringValue());
		assertEquals("\\\\5FAE\\8f6f\\96c5\\9ed1 , Arial", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Arial", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue8() throws CSSException {
		parseStyleDeclaration("font-family: \"\u5b8b\u4f53\",Arial");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue9() throws CSSException {
		parseStyleDeclaration("font-family:file\\:\\/\\/\\/dir\\/file");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("file:///dir/file", lu.getStringValue());
		assertEquals("file\\:\\/\\/\\/dir\\/file", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue10() throws CSSException {
		parseStyleDeclaration("font-family:file\\:c\\:\\\\\\\\dir\\\\file");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("file:c:\\\\dir\\file", lu.getStringValue());
		assertEquals("file\\:c\\:\\\\\\\\dir\\\\file", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedPropertyValue11() throws CSSException {
		parseStyleDeclaration("list-style-type:symbols('*' '\\2020' '\\2021' '\\A7');");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("list-style-type", handler.propertyNames.get(0));
		LexicalUnit lunit = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackError() throws CSSException {
		parseStyleDeclaration("width: 600px\\9");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("width: 600px\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("600px\\9", lu.getStringValue());
		assertEquals("600px\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("width: 600px\\0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("600px\\0", lu.getStringValue());
		assertEquals("600px\\0", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack3Error() throws CSSException {
		parseStyleDeclaration("width: calc(80% - 3cap)\\9");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack3() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("width: calc(80% - 3cap)\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("calc(80% - 3cap)\\9", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("calc(80% - 3cap)\\9", lu.toString());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHack4() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("color: #000\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("#000\\9", lu.getStringValue());
		assertEquals("#000\\9", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationNoBackslashHack() throws CSSException {
		parseStyleDeclaration("font-family: Times New Roman\\9");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("Times New Roman\\9 ", lu.toString());
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("Roman\t", lu.getStringValue());
		assertEquals("Roman\\9 ", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackFontFamily() throws CSSException {
		parseStyleDeclaration("font-family: Times New Roman\\0/");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Times New Roman\\0 /", lu.toString());
		assertEquals("Times", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals("New", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Roman\ufffd", lu.getStringValue());
		assertEquals("Roman\\0 /", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackslashHackFontFamily2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("font-family: Times New Roman\\0/");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationPropertyNameError() throws CSSException {
		parseStyleDeclaration("color\0:#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameError2() throws CSSException {
		parseStyleDeclaration("color\u0007:#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameErrorNumberSignAtEnd() throws CSSException {
		parseStyleDeclaration("color#:#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameErrorInitialNumberSign() throws CSSException {
		parseStyleDeclaration("#color:#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameErrorInitialNumberSignRecovery()
			throws CSSException {
		// Test the recovery from previous error
		parseStyleDeclaration("#color:#ff0;margin-right:0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameErrorTwoNames() throws CSSException {
		parseStyleDeclaration("color color:#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameErrorInvalidIdent() throws CSSException {
		parseStyleDeclaration("9color:#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameErrorAmpersand() throws CSSException {
		parseStyleDeclaration("color &:#ff0;text-align:left");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(1, handler.propertyNames.size());
		assertEquals("text-align", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameStringError() throws CSSException {
		parseStyleDeclaration("\"color\":#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameStringError2() throws CSSException {
		parseStyleDeclaration("background-\"color\":#ff0");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameWarning() throws CSSException {
		parseStyleDeclaration("\\30 color:#ff0");
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());

		assertEquals(1, handler.propertyNames.size());
		assertEquals("0color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());

		assertEquals("#ff0", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameWarningWS() throws CSSException {
		parseStyleDeclaration("\\30 color :#ff0");
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());

		assertEquals(1, handler.propertyNames.size());
		assertEquals("0color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());

		assertEquals("#ff0", lu.toString());
	}

	@Test
	public void testParseStyleDeclarationPropertyNameBackslashHack() throws CSSException {
		parseStyleDeclaration("color\\9:#ff0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color\u0009", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationStarHackError() throws CSSException {
		parseStyleDeclaration("*width:600px");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationStarHack() throws CSSException {
		parser.setFlag(Parser.Flag.STARHACK);
		parseStyleDeclaration("*width:60cap");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_CAP, lu.getCssUnit());
		assertEquals(60f, lu.getFloatValue(), 0.001f);
		assertEquals("cap", lu.getDimensionUnitText());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());

		assertEquals(1, errorHandler.getLastWarning().getLineNumber());
		assertEquals(1, errorHandler.getLastWarning().getColumnNumber());
	}

	@Test
	public void testParseStyleDeclarationStarHack2() throws CSSException {
		parser.setFlag(Parser.Flag.STARHACK);
		parseStyleDeclaration("*width:600px;display:block;");
		assertEquals(2, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));

		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(600f, lu.getFloatValue(), 0.001f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("600px", lu.toString());

		lu = handler.lexicalValues.get(1);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrioError() throws CSSException {
		parseStyleDeclaration("display:block!ie");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIEPrio() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:60cap!ie");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("60cap!ie", lu.getStringValue());
		assertEquals("60cap!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrioSemicolon() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:60cap!ie;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("60cap!ie", lu.getStringValue());
		assertEquals("60cap!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrio2() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:calc(80% - 3cap) ! ie");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("calc(80% - 3cap)!ie", lu.getStringValue());
		assertEquals("calc(80% - 3cap)!ie", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertNull(handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEPrio3() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIO);
		parseStyleDeclaration("width:foo 60cap!ie");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("foo 60cap!ie", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("foo 60cap!ie", lu.toString());
		assertNull(handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioError() throws CSSException {
		parseStyleDeclaration("display:block!important!");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioError2() throws CSSException {
		parseStyleDeclaration("display:block!important!;");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:60cap!important!");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("60cap", lu.getStringValue());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrioSemicolon() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:60cap!important!;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("60cap", lu.getStringValue());
		assertEquals("60cap", lu.toString());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio2() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:foo 60cap!important!");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("foo 60cap", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIECharPrio3() throws CSSException {
		parser.setFlag(Parser.Flag.IEPRIOCHAR);
		parseStyleDeclaration("width:calc(80% - 3cap)!important!");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.COMPAT_PRIO, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("calc(80% - 3cap)", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("important", handler.priorities.get(0));

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationNL() throws CSSException {
		parseStyleDeclaration("font-size : larger\n;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationTab() throws CSSException {
		parseStyleDeclaration("font-size : larger\t;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("larger", lu.getStringValue());
		assertEquals("larger", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEscapedTab() throws CSSException {
		parseStyleDeclaration("foo : \\9;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\t", lu.getStringValue());
		assertEquals("\\9 ", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationSemicolonError() throws CSSException {
		parseStyleDeclaration("foo;color:blue;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationSemicolonWarning() throws CSSException {
		parseStyleDeclaration("foo:bar;;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("bar", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationUnit() throws CSSException {
		parseStyleDeclaration("margin-right: 1px;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("1px", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationUnitExpNotationPlus() throws CSSException {
		parseStyleDeclaration("margin-right: 1.1e+01px;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(11f, lu.getFloatValue(), 0.01f);
		assertEquals("px", lu.getDimensionUnitText());
		assertEquals("11px", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationUnitExpNotationMinus() throws CSSException {
		parseStyleDeclaration("margin-right: 11e-01em;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(1.1f, lu.getFloatValue(), 0.01f);
		assertEquals("em", lu.getDimensionUnitText());
		assertEquals("1.1em", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationUnitDimension() throws CSSException {
		parseStyleDeclaration("margin-right: 1foo;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
		assertEquals("foo", lu.getDimensionUnitText());
		assertEquals("1foo", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationZerolessDimension() throws CSSException {
		parseStyleDeclaration("padding-left:.0083ex");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(0.0083f, lu.getFloatValue(), 1e-7f);
		assertEquals("0.0083ex", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationPlusDimension() throws CSSException {
		parseStyleDeclaration("padding-left:+.0083ex");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(0.0083f, lu.getFloatValue(), 1e-7f);
		assertEquals("0.0083ex", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationMinusDimension() throws CSSException {
		parseStyleDeclaration("padding-left:-.0083ex");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(-0.0083f, lu.getFloatValue(), 1e-7f);
		assertEquals("-0.0083ex", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationZero() throws CSSException {
		parseStyleDeclaration("margin-right:0;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		assertEquals("0", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationOneFloat() throws CSSException {
		parseStyleDeclaration("foo:1.0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationMinusOneFloat() throws CSSException {
		parseStyleDeclaration("foo:-1.0");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationDotOneFloat() throws CSSException {
		parseStyleDeclaration("foo:.1");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(0.1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationMinusDotOneFloat() throws CSSException {
		parseStyleDeclaration("foo:-.1");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.REAL, lu.getLexicalUnitType());
		assertEquals(-0.1f, lu.getFloatValue(), 0.01f);
	}

	@Test
	public void testParseStyleDeclarationZIndex() throws CSSException {
		parseStyleDeclaration("z-index:1;");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("z-index", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(1, lu.getIntegerValue());
		assertEquals("1", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationPlusError() throws CSSException {
		parseStyleDeclaration("z-index:+ 1deg");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationMinusError() throws CSSException {
		parseStyleDeclaration("z-index:- 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProductError() throws CSSException {
		parseStyleDeclaration("z-index:* 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationMargin() throws CSSException {
		parseStyleDeclaration("margin: 0.5em auto;");
		assertEquals("margin", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, lu.getCssUnit());
		assertEquals(0.5f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals("auto", lu.toString());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBorderColor() throws CSSException {
		parseStyleDeclaration("border-color: blue #a7f31a green;");
		assertEquals("border-color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBorderImage() throws CSSException {
		parseStyleDeclaration(
				"border-image: url('/img/border.png') 25% 30% 12% 20% fill / 2pt / 1 round;");
		assertEquals("border-image", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("/img/border.png", lu.getStringValue());
		assertEquals("url('/img/border.png') 25% 30% 12% 20% fill/2pt/1 round", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(25f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(30f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(12f, lu.getFloatValue(), 1e-5f);
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, lu.getCssUnit());
		assertEquals(20f, lu.getFloatValue(), 1e-5f);
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
		assertEquals(2f, lu.getFloatValue(), 1e-5f);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBackgroundImage() throws CSSException {
		parseStyleDeclaration(
				"background-image: url('ignore.png';)/* ignore */;background-image: url('/img/back.png');");
		assertEquals("background-image", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.URI, lu.getLexicalUnitType());
		assertEquals("/img/back.png", lu.getStringValue());
		assertEquals("url('/img/back.png')", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNull(lu);
	}

	@Test
	public void testParseStyleDeclarationBackgroundImageGradient() throws CSSException {
		parseStyleDeclaration("background-image: linear-gradient(35deg);");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("background-image", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.GRADIENT, lu.getLexicalUnitType());
		assertEquals("linear-gradient", lu.getFunctionName());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(35f, param.getFloatValue(), 1e-6f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("linear-gradient(35deg)", lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationBackgroundImageNone() throws CSSException {
		parseStyleDeclaration("background-image: NONE;");
		assertEquals("background-image", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("none", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationBackgroundImageGradient2() throws CSSException {
		parseStyleDeclaration(
				"background-image: linear-gradient(180deg,transparent,99%,rgba(0,0,0,.5));");
		assertEquals("background-image", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.GRADIENT, lu.getLexicalUnitType());
		assertEquals("linear-gradient", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals(180f, param.getFloatValue(), 1e-6f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(99f, param.getFloatValue(), 1e-6f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());

		assertNull(param.getNextLexicalUnit());
		assertEquals("linear-gradient(180deg, transparent, 99%, rgba(0, 0, 0, 0.5))",
				lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationPrefixedGradient() throws CSSException {
		parseStyleDeclaration(
				"background-image: -o-linear-gradient(top,transparent,99%,rgba(0,0,0,0.5));");
		assertEquals("background-image", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.PREFIXED_FUNCTION, lu.getLexicalUnitType());
		assertEquals("-o-linear-gradient", lu.getFunctionName());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("top", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("transparent", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(99f, param.getFloatValue(), 1e-6f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());

		assertNull(param.getNextLexicalUnit());
		assertEquals("-o-linear-gradient(top, transparent, 99%, rgba(0, 0, 0, 0.5))",
				lu.toString());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationBackgroundClipIdent() throws CSSException {
		parseStyleDeclaration("background-clip: Content-Box;");
		assertEquals("background-clip", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("content-box", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleTypeCustomIdent() throws CSSException {
		parseStyleDeclaration("list-style-type: MyStyle;");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("list-style-type", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("MyStyle", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleCustomIdent() throws CSSException {
		parseStyleDeclaration("list-style: MyStyle;");
		assertEquals("list-style", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("MyStyle", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationListStyleIdent() throws CSSException {
		parseStyleDeclaration("list-style: Upper-Roman;");
		assertEquals("list-style", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("upper-roman", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
	}

	@Test
	public void testParseStyleDeclarationContent() throws CSSException {
		parseStyleDeclaration("content: \\f435;");
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("\\f435", lu.getStringValue()); // Private use character, must be escaped
		assertEquals("\\f435", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString() throws CSSException {
		parseStyleDeclaration("content: 'foo';");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("'foo'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentStringComment() throws CSSException {
		parseStyleDeclaration("content: 'foo' /* after */;");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("'foo'", lu.toString());

		assertNotNull(lu.getTrailingComments());
		StringList after = lu.getTrailingComments();
		assertEquals(1, after.getLength());
		assertEquals(" after ", after.item(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentStringComments() throws CSSException {
		String cssText = "content: /* before */'foo' /* after */;";
		parseStyleDeclaration(cssText);
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());
		assertEquals("'foo'", lu.toString());

		assertNotNull(lu.getPrecedingComments());
		StringList pre = lu.getPrecedingComments();
		assertEquals(1, pre.getLength());
		assertEquals(" before ", pre.item(0));

		assertNotNull(lu.getTrailingComments());
		StringList after = lu.getTrailingComments();
		assertEquals(1, after.getLength());
		assertEquals(" after ", after.item(0));

		assertFalse(errorHandler.hasError());
	}

	/*
	 * Now ignore comments
	 */
	@Test
	public void testParseStyleDeclarationContentStringIgnoreComments() throws CSSException {
		String cssText = "content: /* before */'foo' /* after */;";

		parser.setFlag(Parser.Flag.VALUE_COMMENTS_IGNORE);

		parseStyleDeclaration(cssText);
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo", lu.getStringValue());

		assertNull(lu.getPrecedingComments());
		assertNull(lu.getTrailingComments());

		assertFalse(errorHandler.hasError());

		parser.unsetFlag(Parser.Flag.VALUE_COMMENTS_IGNORE);
	}

	@Test
	public void testParseStyleDeclarationContentString2() throws CSSException {
		parseStyleDeclaration("content: 'foo\\a bar';");
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("foo\nbar", lu.getStringValue());
		assertEquals("'foo\\a bar'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString3() throws CSSException {
		parseStyleDeclaration("content: '\\\\5FAE\\8F6F';");
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\\5FAE\u8F6F", lu.getStringValue());
		assertEquals("'\\\\5FAE\\8F6F'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentString4() throws CSSException {
		parseStyleDeclaration("content:\"\\000A0-\\000A0\"");
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\u00A0-\u00A0", lu.getStringValue());
		assertEquals("\"\\000A0-\\000A0\"", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentBackslash() throws CSSException {
		parseStyleDeclaration("content: '\\\\';");
		assertEquals("content", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.STRING, lu.getLexicalUnitType());
		assertEquals("\\", lu.getStringValue());
		assertEquals("'\\\\'", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationContentBackslashBad() throws CSSException {
		parseStyleDeclaration("content: '\\';");
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCalc() throws CSSException {
		parseStyleDeclaration("width: calc(100% - 3em)");
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcExpNotationPlus() throws CSSException {
		parseStyleDeclaration("width: calc(100% - 1.2e+01mm)");
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_MM, param.getCssUnit());
		assertEquals(12f, param.getFloatValue(), 1e-5f);
		assertEquals("mm", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 12mm)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcExpNotation() throws CSSException {
		parseStyleDeclaration("width: calc(100% - 3e-01em)");
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(0.3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 0.3em)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcEscaped() throws CSSException {
		parseStyleDeclaration("width: ca\\4c c(100% - 3em)");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcPlus() throws CSSException {
		parseStyleDeclaration("width: calc(80% + 3em)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(80f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(80% + 3em)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcWSPlusError() throws CSSException {
		parseStyleDeclaration("width: calc(90%+3em)");
		assertEquals(0, handler.propertyNames.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcWSMinusError() throws CSSException {
		parseStyleDeclaration("width: calc(90%-3em)");
		assertEquals(0, handler.propertyNames.size());

		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationBezier() throws CSSException {
		parseStyleDeclaration("foo:cubic-bezier(0.33, 0.1, 0.5, 1)");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
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
	public void testParseStyleDeclarationBezierExpNotation() throws CSSException {
		parseStyleDeclaration("foo:cubic-bezier(0.033E+01, 1e-1, 5E-1, 1E0)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.5f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(1f, param.getFloatValue(), 1e-5f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, 0.1, 0.5, 1)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationBezierNegativeArg() throws CSSException {
		parseStyleDeclaration("foo:cubic-bezier(.33, -.1, 1, -.02)");
		assertEquals("foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("cubic-bezier", lu.getFunctionName());
		assertEquals(LexicalType.CUBIC_BEZIER_FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.33f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.1f, param.getFloatValue(), 0.001f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(-0.02f, param.getFloatValue(), 0.001f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("cubic-bezier(0.33, -0.1, 1, -0.02)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationSteps() throws CSSException {
		parseStyleDeclaration("animation-timing-function:steps(2, start)");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("animation-timing-function", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationStepsEscaped() throws CSSException {
		parseStyleDeclaration("animation-timing-function:st\\45ps(2, start)");
		assertEquals("animation-timing-function", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationCustomPropertyCalc() throws CSSException {
		parseStyleDeclaration("--rot:calc(100% - 3em)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.PERCENTAGE, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PERCENTAGE, param.getCssUnit());
		assertEquals(100f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MINUS, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(3f, param.getFloatValue(), 1e-5f);
		assertEquals("em", param.getDimensionUnitText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(100% - 3em)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyCalc2() throws CSSException {
		parseStyleDeclaration("--rot:calc(10deg * 3.2)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, param.getCssUnit());
		assertEquals("deg", param.getDimensionUnitText());
		assertEquals(10f, param.getFloatValue(), 1e-5f);
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(3.2f, param.getFloatValue(), 1e-5f);
		assertEquals(0, param.getDimensionUnitText().length());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(10deg*3.2)", lu.toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCalcZerolessDimension() throws CSSException {
		parseStyleDeclaration("padding-left:calc(.0083ex)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(0.0083f, param.getFloatValue(), 1e-7f);
		assertEquals("0.0083ex", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(0.0083ex)", lu.getCssText());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCalcPlusDimension() throws CSSException {
		parseStyleDeclaration("padding-left:calc(+.0083ex)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(0.0083f, param.getFloatValue(), 1e-7f);
		assertEquals("0.0083ex", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(0.0083ex)", lu.getCssText());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleDeclarationCalcPlusDimensionProd() throws CSSException {
		parseStyleDeclaration("padding-left:calc(+.0083ex*+.25)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(0.0083f, param.getFloatValue(), 1e-7f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.25f, param.getFloatValue(), 1e-7f);
		assertEquals("0.25", param.getCssText());

		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(0.0083ex*0.25)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcPlusDimensionDiv() throws CSSException {
		parseStyleDeclaration("padding-left:calc(+.0083ex/+.25)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(0.0083f, param.getFloatValue(), 1e-7f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(0.25f, param.getFloatValue(), 1e-7f);
		assertEquals("0.25", param.getCssText());

		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(0.0083ex/0.25)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcMinusDimension() throws CSSException {
		parseStyleDeclaration("padding-left:calc(-.0083ex)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(-0.0083f, param.getFloatValue(), 1e-7f);
		assertEquals("-0.0083ex", param.getCssText());
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(-0.0083ex)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcMinusDimensionProd() throws CSSException {
		parseStyleDeclaration("padding-left:calc(-.0083ex*-.25)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(-0.0083f, param.getFloatValue(), 1e-7f);
		assertEquals("-0.0083ex", param.getCssText());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(-0.25f, param.getFloatValue(), 1e-7f);
		assertEquals("-0.25", param.getCssText());

		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(-0.0083ex*-0.25)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcMinusDimensionDiv() throws CSSException {
		parseStyleDeclaration("padding-left:calc(-.0083ex/-.25)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, param.getCssUnit());
		assertEquals(-0.0083f, param.getFloatValue(), 1e-7f);
		assertEquals("-0.0083ex", param.getCssText());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_SLASH, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(-0.25f, param.getFloatValue(), 1e-7f);
		assertEquals("-0.25", param.getCssText());

		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(-0.0083ex/-0.25)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcMinusInteger() throws CSSException {
		parseStyleDeclaration("padding-left:calc(-1*-2em)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-2f, param.getFloatValue(), 1e-7f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(-1*-2em)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcMinusIntegerWS() throws CSSException {
		parseStyleDeclaration("padding-left:calc(-1 * -2em)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("padding-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(-1, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EM, param.getCssUnit());
		assertEquals(-2f, param.getFloatValue(), 1e-7f);
		assertNull(param.getNextLexicalUnit());
		assertEquals("calc(-1*-2em)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCalcVar() throws CSSException {
		parseStyleDeclaration("margin-top:calc(-1*(-.9px + var(--offset-top,-.6pt)))");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("margin-top", handler.propertyNames.get(0));

		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.CALC, lu.getLexicalUnitType());
		assertEquals("calc", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);

		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_NUMBER, param.getCssUnit());
		assertEquals(-1, param.getIntegerValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.SUB_EXPRESSION, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		param = param.getSubValues();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, param.getCssUnit());
		assertEquals(-0.9f, param.getFloatValue(), 1e-7f);

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_PLUS, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.VAR, param.getLexicalUnitType());
		assertNull(param.getNextLexicalUnit());

		param = param.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--offset-top", param.getStringValue());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());

		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.DIMENSION, param.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, param.getCssUnit());
		assertEquals(-0.6f, param.getFloatValue(), 1e-7f);

		assertNull(param.getNextLexicalUnit());

		assertEquals("calc(-1*(-0.9px + var(--offset-top, -0.6pt)))", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyPlusDimension() throws CSSException {
		parseStyleDeclaration("--foo:+.0083ex");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(0.0083f, lu.getFloatValue(), 1e-7f);
		assertEquals("0.0083ex", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyMinusDimension() throws CSSException {
		parseStyleDeclaration("--foo:-.0083ex");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, lu.getCssUnit());
		assertEquals(-0.0083f, lu.getFloatValue(), 1e-7f);
		assertEquals("-0.0083ex", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyMinusSpaceDimension() throws CSSException {
		parseStyleDeclaration("--foo:- .0083ex");
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("--foo", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.OPERATOR_MINUS, lu.getLexicalUnitType());
		LexicalUnit nlu = lu.getNextLexicalUnit();
		assertNotNull(nlu);
		assertEquals(LexicalType.DIMENSION, nlu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_EX, nlu.getCssUnit());
		assertEquals(0.0083f, nlu.getFloatValue(), 1e-7f);
		assertEquals("0.0083ex", nlu.getCssText());
		assertEquals("- 0.0083ex", lu.toString());
		assertNull(nlu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyPlus() throws CSSException {
		parseStyleDeclaration("--rot:+ 1deg");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.OPERATOR_PLUS, lu.getLexicalUnitType());
		assertEquals("+", lu.getCssText());
		assertEquals("+ 1deg", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, lu.getCssUnit());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("1deg", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyPlusError() throws CSSException {
		parseStyleDeclaration("--rot:+ + 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyMinus() throws CSSException {
		parseStyleDeclaration("--rot:- 1deg");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.OPERATOR_MINUS, lu.getLexicalUnitType());
		assertEquals("-", lu.getCssText());
		assertEquals("- 1deg", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, lu.getCssUnit());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("1deg", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyMinusError() throws CSSException {
		parseStyleDeclaration("--rot:- - 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyPlusMinusError() throws CSSException {
		parseStyleDeclaration("--rot:+ - 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyAsterisk() throws CSSException {
		parseStyleDeclaration("--rot:* 1deg");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, lu.getLexicalUnitType());
		assertEquals("*", lu.getCssText());
		assertEquals("*1deg", lu.toString());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, lu.getCssUnit());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("1deg", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyAsterisk2() throws CSSException {
		parseStyleDeclaration("--rot:75 * 1deg");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(75, lu.getIntegerValue());
		assertEquals("75", lu.getCssText());
		assertEquals("75*1deg", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_MULTIPLY, lu.getLexicalUnitType());
		assertEquals("*", lu.getCssText());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, lu.getCssUnit());
		assertEquals(1f, lu.getFloatValue(), 1e-5f);
		assertEquals("1deg", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertySlash() throws CSSException {
		parseStyleDeclaration("--rot: / 2");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		assertEquals("/", lu.getCssText());
		assertEquals("/2", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(2, lu.getIntegerValue());
		assertEquals("2", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertySlash2() throws CSSException {
		parseStyleDeclaration("--rot:90deg / 2");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, lu.getCssUnit());
		assertEquals(90f, lu.getFloatValue(), 1e-5f);
		assertEquals("90deg", lu.getCssText());
		assertEquals("90deg/2", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_SLASH, lu.getLexicalUnitType());
		assertEquals("/", lu.getCssText());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(2, lu.getIntegerValue());
		assertEquals("2", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyExpNotationPlus() throws CSSException {
		parseStyleDeclaration("--rot:+ 3.2e+01deg");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--rot", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.OPERATOR_PLUS, lu.getLexicalUnitType());
		assertEquals("+", lu.getCssText());
		assertEquals("+ 32deg", lu.toString());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_DEG, lu.getCssUnit());
		assertEquals(32f, lu.getFloatValue(), 1e-5f);
		assertEquals("32deg", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyCommaPlusError() throws CSSException {
		parseStyleDeclaration("--rot:, + 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyCommaMinusError() throws CSSException {
		parseStyleDeclaration("--rot:, - 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyCommaAsteriskError() throws CSSException {
		parseStyleDeclaration("--rot:, * 1deg");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomPropertyVar() throws CSSException {
		parseStyleDeclaration("--lh:((var(--lh-unit) + 8) / var(--lh-unit))");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("--lh", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.SUB_EXPRESSION, lu.getLexicalUnitType());
		LexicalUnit sub = lu.getSubValues();
		assertEquals(LexicalType.SUB_EXPRESSION, sub.getLexicalUnitType());
		LexicalUnit sub2 = sub.getSubValues();
		assertEquals(LexicalType.VAR, sub2.getLexicalUnitType());
		assertEquals("var", sub2.getFunctionName());

		LexicalUnit param = sub2.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--lh-unit", param.getStringValue());

		sub2 = sub2.getNextLexicalUnit();
		assertNotNull(sub2);
		assertEquals(LexicalType.OPERATOR_PLUS, sub2.getLexicalUnitType());

		sub2 = sub2.getNextLexicalUnit();
		assertNotNull(sub2);
		assertEquals(LexicalType.INTEGER, sub2.getLexicalUnitType());
		assertEquals(8, sub2.getIntegerValue());
		assertNull(sub2.getNextLexicalUnit());

		sub = sub.getNextLexicalUnit();
		assertNotNull(sub);
		assertEquals(LexicalType.OPERATOR_SLASH, sub.getLexicalUnitType());

		sub = sub.getNextLexicalUnit();
		assertNotNull(sub);
		assertEquals(LexicalType.VAR, sub.getLexicalUnitType());

		assertNull(lu.getNextLexicalUnit());

		assertEquals("((var(--lh-unit) + 8)/var(--lh-unit))", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationVar() throws CSSException {
		parseStyleDeclaration("color:var(--my-color,#3fa)");
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--my-color", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#3fa", param.toString());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var(--my-color, #3fa)", lu.toString());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationVarEscaped() throws CSSException {
		parseStyleDeclaration("color:v\\41r(--my-color,#3fa)");
		assertEquals("color", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.VAR, lu.getLexicalUnitType());
		assertEquals("var", lu.getFunctionName());
		assertNull(lu.getNextLexicalUnit());
		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.IDENT, param.getLexicalUnitType());
		assertEquals("--my-color", param.getStringValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.RGBCOLOR, param.getLexicalUnitType());
		assertEquals("#3fa", param.toString());
		assertNull(param.getNextLexicalUnit());
		assertEquals("var(--my-color, #3fa)", lu.toString());
		assertEquals("var(--my-color, #3fa)", lu.getCssText());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationEmptyFunction() throws CSSException {
		parseStyleDeclaration("filter:mask()");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("mask", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());
		assertNull(lu.getParameters());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationFunctionEscaped() throws CSSException {
		parseStyleDeclaration("filter:\\-(.1)");
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("-", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		assertNull(param.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationFunctionEscapedHex() throws CSSException {
		parseStyleDeclaration("filter:\\1b(.1)");
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("\u001b", lu.getFunctionName());
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.REAL, param.getLexicalUnitType());
		assertEquals(0.1f, param.getFloatValue(), 0.001f);
		assertNull(param.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationFunctionEscapedString() throws CSSException {
		parseStyleDeclaration("filter:\'foo\'(.1)");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationFunctionStringError() throws CSSException {
		parseStyleDeclaration("filter:\"foo\"(.1)");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("filter: --my-function(min-color = 5)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.PREFIXED_FUNCTION, lu.getLexicalUnitType());
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
	public void testParseStyleDeclarationCustomFunction2Error() throws CSSException {
		parseStyleDeclaration("filter: --my-function(min-color =5)");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationCustomFunction2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("filter: --my-function(min-color =5)");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("filter", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.PREFIXED_FUNCTION, lu.getLexicalUnitType());
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
	public void testParseStyleDeclarationProgidError() throws CSSException {
		parseStyleDeclaration(
				"filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#bd0afa', endColorstr='#d0df9f')");
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals(
				"progid:DXImageTransform.Microsoft.gradient(startColorstr= '#bd0afa', endColorstr= '#d0df9f')",
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
	public void testParseStyleDeclarationProgid2Error() throws CSSException {
		parseStyleDeclaration(
				"filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f);");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#bd0afa,EndColorStr=#d0df9f);");
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertEquals("StartColorStr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#bd0afa", lu.getCssText());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.COMPAT_IDENT, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_INVALID, lu.getCssUnit());
		assertEquals("EndColorStr=", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RGBCOLOR, lu.getLexicalUnitType());
		assertEquals("#d0df9f", lu.getCssText());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationProgid3Error() throws CSSException {
		parseStyleDeclaration("filter: progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid3() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("filter: progid:DXImageTransform.Microsoft.Blur(pixelradius=5)");
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationProgid4Error() throws CSSException {
		parseStyleDeclaration(
				"_filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=scale,src='http://www.example.com/images/myimage.png');");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
	}

	@Test
	public void testParseStyleDeclarationProgid4() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"_filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=scale,src='http://www.example.com/images/myimage.png');");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("_filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationProgidEscaped() throws CSSException {
		parseStyleDeclaration(
				"filter: progid\\:DXImageTransform\\.Microsoft\\.gradient\\(enabled\\=false\\)");
		assertEquals("filter", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("progid:DXImageTransform.Microsoft.gradient(enabled=false)",
				lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationIEExpressionError() throws CSSException {
		parseStyleDeclaration("top:expression(iequirk = (document.body.scrollTop) + \"px\" )");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpressionError2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES); // Must throw exception despite the flag
		parseStyleDeclaration("top:expression(= (document.body.scrollTop) + \"px\" )");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpression() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration("top:expression(iequirk = (document.body.scrollTop) + \"px\" )");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("top", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
	public void testParseStyleDeclarationIEExpression2Error() throws CSSException {
		parseStyleDeclaration(
				"zoom:expression(this.runtimeStyle['zoom'] = '1',this.innerHTML = '&#xe03a;')");
		assertTrue(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());

		assertEquals(0, handler.propertyNames.size());
	}

	@Test
	public void testParseStyleDeclarationIEExpression2() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"zoom:expression(this.runtimeStyle['zoom']='1',this.innerHTML='&#xe03a;')");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("zoom", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("expression(this\\.runtimeStyle['zoom'] = '1', this.innerHTML= '&#xe03a;')",
				lu.toString());
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
	public void testParseStyleDeclarationIEExpression2WS() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"zoom:expression(this.runtimeStyle['zoom'] = '1',this.innerHTML = '&#xe03a;')");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("zoom", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals("expression(this\\.runtimeStyle['zoom'] = '1', this.innerHTML= '&#xe03a;')",
				lu.toString());
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
	public void testParseStyleDeclarationIEExpression3() throws CSSException {
		parser.setFlag(Parser.Flag.IEVALUES);
		parseStyleDeclaration(
				"top:expression(eval(document.documentElement.scrollTop+(document.documentElement.clientHeight-this.offsetHeight)))");
		assertEquals(1, handler.propertyNames.size());
		assertEquals("top", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertEquals("document.documentElement.clientHeight-this.offsetHeight",
				subv.getStringValue());
		assertNull(subv.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationSquareBrackets() throws CSSException {
		parseStyleDeclaration("grid-template-rows: [header-top]");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("grid-template-rows", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationSquareBrackets2() throws CSSException {
		parseStyleDeclaration("grid-template-rows: repeat(1, [] 10px)");
		assertEquals("grid-template-rows", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
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
		assertEquals(10f, lu.getFloatValue(), 1e-5f);
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationSquareBracketList() throws CSSException {
		parseStyleDeclaration(
				"grid-template-columns:[main-start] repeat(5,minmax(0,1fr)) [main-end toolbar-start]repeat(1,minmax(0,1fr)) [toolbar-end];");
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("grid-template-columns", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("main-start", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("repeat(5, minmax(0, 1fr))", lu.getCssText());

		LexicalUnit param = lu.getParameters();
		assertNotNull(param);
		assertEquals(LexicalType.INTEGER, param.getLexicalUnitType());
		assertEquals(5, param.getIntegerValue());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.OPERATOR_COMMA, param.getLexicalUnitType());
		param = param.getNextLexicalUnit();
		assertNotNull(param);
		assertEquals(LexicalType.FUNCTION, param.getLexicalUnitType());
		assertEquals("minmax(0, 1fr)", param.toString());
		assertNull(param.getNextLexicalUnit());
		LexicalUnit param2 = param.getParameters();
		assertNotNull(param2);
		assertEquals(LexicalType.INTEGER, param2.getLexicalUnitType());
		assertEquals(0, param2.getIntegerValue());
		param2 = param2.getNextLexicalUnit();
		assertNotNull(param2);
		assertEquals(LexicalType.OPERATOR_COMMA, param2.getLexicalUnitType());
		param2 = param2.getNextLexicalUnit();
		assertNotNull(param2);
		assertEquals(LexicalType.DIMENSION, param2.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_FR, param2.getCssUnit());
		assertEquals(1f, param2.getFloatValue(), 1e-5f);
		assertNull(param2.getNextLexicalUnit());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("main-end", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("toolbar-start", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.FUNCTION, lu.getLexicalUnitType());
		assertEquals("repeat(1, minmax(0, 1fr))", lu.getCssText());

		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.LEFT_BRACKET, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("toolbar-end", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertNotNull(lu);
		assertEquals(LexicalType.RIGHT_BRACKET, lu.getLexicalUnitType());
		assertNull(lu.getNextLexicalUnit());

		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleDeclarationSquareBracketsBad() throws CSSException {
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

	private void parseStyleDeclaration(String string) throws CSSParseException {
		try {
			parser.parseStyleDeclaration(new StringReader(string));
		} catch (IOException e) {
			// Cannot happen
			e.printStackTrace();
		}
		assertEquals(1, handler.streamEndcount);
	}

}
