/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.SampleCSS;

public class SheetParserTest {

	private CSSParser parser;
	private TestCSSHandler handler;
	private TestErrorHandler errorHandler;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
		handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
	}

	@AfterEach
	public void tearDown() throws Exception {
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"p.myclass,:first-child{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals("width", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("Times New Roman", handler.lexicalValues.get(0).toString());
		assertEquals("yellow", handler.lexicalValues.get(1).toString());
		assertEquals("calc(100% - 3em)", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(52, loc.getColumnNumber());
		assertEquals(67, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(92, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetRule2() throws CSSException, IOException {
		Reader re = new StringReader(
			"hr[align=\"left\"]    {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(38, loc.getColumnNumber());
		assertEquals(58, handler.ptyLocators.get(1).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetRuleNoSemicolon() throws CSSException, IOException {
		Reader re = new StringReader(
			".align{margin-left:0} .cls{margin-right:auto");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.selectors.size());
		assertEquals(".align", handler.selectors.get(0).toString());
		assertEquals(".cls", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals(".align", handler.propertySelectors.get(0).toString());
		assertEquals("margin-right", handler.propertyNames.get(1));
		assertEquals(".cls", handler.propertySelectors.get(1).toString());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(21, loc.getColumnNumber());
		assertEquals(45, handler.ptyLocators.get(1).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetTwoRules() throws CSSException, IOException {
		Reader re = new StringReader(
			".fooclass{zoom:expression(function(ele){ele.style.zoom = \"1\"; document.execCommand(\"BackgroundImageCache\", false, true); skip-me:skip-value}(this))}#fooid .barclass{margin-right:auto;}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.selectors.size());
		assertEquals(".fooclass", handler.selectors.get(0).toString());
		assertEquals("#fooid .barclass", handler.selectors.get(1).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(183, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseSheetTwoRulesTwoProperties() throws CSSException, IOException {
		Reader re = new StringReader(
			".fooclass{zoom:expression(function(ele){ele.style.zoom = \"1\"; document.execCommand(\"BackgroundImageCache\", false, true); }(this));margin-left:0}#fooid .fooclass{margin-right:auto;}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.selectors.size());
		assertEquals(".fooclass", handler.selectors.get(0).toString());
		assertEquals("#fooid .fooclass", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(144, loc.getColumnNumber());
		assertEquals(179, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseSheetStyleRuleBad() throws CSSException, IOException {
		Reader re = new StringReader(
			".foo{@*transform translateY(-5px);margin-left:0;margin-right:auto;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(48, loc.getColumnNumber());
		assertEquals(66, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(7, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetStyleRuleBad2() throws CSSException, IOException {
		Reader re = new StringReader(".foo{margin-left:0;{foo:bar;} :bar;margin-right:auto;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());
		assertEquals(53, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(35, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetStyleRuleBadNested() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media screen and (min-width: 768px){.foo{@: : translateY(-5px);margin-left:0;margin-right:auto;}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen and (min-width: 768px)", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(78, loc.getColumnNumber());
		assertEquals(96, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(44, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testMalformedStyleRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"input:not(){}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header",
			handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(69, loc.getColumnNumber());
		assertEquals(89, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testMalformedStyleRule2() throws CSSException, IOException {
		Reader re = new StringReader(
			"p.myclass width:300px}{}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header",
			handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(80, loc.getColumnNumber());
		assertEquals(100, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(23, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testMalformedStyleRule3() throws CSSException, IOException {
		Reader re = new StringReader(
			"p.myclass color:rgb(120}}{}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header",
			handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-right", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(83, loc.getColumnNumber());
		assertEquals(103, handler.ptyLocators.get(1).getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(26, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetSelectorError() throws CSSException, IOException {
		Reader re = new StringReader(
			"!,p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(1, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetNSRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"@namespace svg \"http://www.w3.org/2000/svg\";svg|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals("svg|p", handler.selectors.get(0).toString());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals("width", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("Times New Roman", handler.lexicalValues.get(0).toString());
		assertEquals("yellow", handler.lexicalValues.get(1).toString());
		assertEquals("calc(100% - 3em)", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(79, loc.getColumnNumber());
		assertEquals(94, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(119, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetSelectorErrorBadNSPrefix() throws CSSException, IOException {
		Reader re = new StringReader(
			"foo|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(1, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetDuplicateSelector() throws CSSException, IOException {
		Reader re = new StringReader("p, p {width: 80%}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("80%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.selectors.size());
		SelectorList selist = handler.selectors.get(0);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(17, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseSheetCommentWDoubleStar() throws CSSException, IOException {
		// An asterisk before the '*/' may confuse the parser
		Reader re = new StringReader(".foo {\n/**just a comment**/margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("*just a comment*", handler.comments.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetCommentWDoubleStar2() throws CSSException, IOException {
		// An asterisk before the '*/' may confuse the parser
		Reader re = new StringReader(".foo {  /**just a comment**/margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("*just a comment*", handler.comments.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(45, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetCommentWStarNL() throws CSSException, IOException {
		Reader re = new StringReader(".foo {  /*Newline\nhere*/:;margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("Newline\nhere", handler.comments.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(25, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(8, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetErrorNL() throws CSSException, IOException {
		Reader re = new StringReader(".foo {\n:;margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(0, handler.comments.size());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());

		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(2, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSheetCDORuleCDC() throws CSSException, IOException {

		Reader re = new StringReader("<!-- .foo {margin-left:auto} -->");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetCDONLRuleNLCDC() throws CSSException, IOException {

		Reader re = new StringReader("<!-- \r.foo {margin-left:auto}\r -->");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetCommentCDOCDCRule() throws CSSException, IOException {

		Reader re = new StringReader("<!-- --> .foo {margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(32, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetCommentCDORule() throws CSSException, IOException {

		Reader re = new StringReader("<!-- .foo {margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSheetCommentCDCRule() throws CSSException, IOException {

		Reader re = new StringReader("--> .foo {margin-left:auto}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(27, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNS() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\");");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSEOF() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\")");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSBad() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url(;");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(17, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseDefaultNSBad2() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url();");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(15, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseDefaultNSDQ() throws CSSException, IOException {
		Reader re = new StringReader("@namespace \"\" url(\"https://www.w3.org/1999/xhtml/\");");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSNoURL() throws CSSException, IOException {
		Reader re = new StringReader("@namespace \"https://www.w3.org/1999/xhtml/\";");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURL() throws CSSException, IOException {
		Reader re = new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\";");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURLEOF() throws CSSException, IOException {
		Reader re = new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\"");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSEOF() throws CSSException, IOException {
		Reader re = new StringReader(
			"/* pre */@namespace xhtml url(\"https://www.w3.org/1999/xhtml/\")");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertEquals(1, handler.comments.size());
		assertEquals(" pre ", handler.comments.get(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSimpleNS() throws CSSException, IOException {
		Reader re = new StringReader(
			"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.namespaceMaps.size());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals("svg|svg", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, lu.getCssUnit());
		assertEquals(5f, lu.getFloatValue(), 0.01f);

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(65, loc.getColumnNumber());
		assertEquals(93, handler.ptyLocators.get(1).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseMinimalStyleSheet() throws CSSException, IOException {
		try (Reader re = SampleCSS.loadSampleUserCSSReader()) {
			parser.parseStyleSheet(re);
		}

		assertEquals(1, handler.selectors.size());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("peru", handler.lexicalValues.get(0).toString());
		assertEquals("blueviolet", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));
		assertEquals("important", handler.priorities.get(1));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(31, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheet1() throws CSSException, IOException {
		try (Reader re = loadTestCSSReader("sheet1.css")) {
			parser.parseStyleSheet(re);
		}

		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: table-cell) and (display: list-item)",
			handler.supportsRuleLists.get(0).toString());
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("print", handler.mediaRuleLists.get(0).toString());
		assertEquals("screen", handler.mediaRuleLists.get(1).toString());
		assertEquals(1, handler.importMedias.size());
		assertEquals("tv,screen and (orientation: landscape)",
			handler.importMedias.get(0).toString());
		assertEquals(1, handler.importURIs.size());
		assertEquals("tv.css", handler.importURIs.get(0));

		assertEquals(7, handler.comments.size());
		assertEquals(" After CDO ", handler.comments.get(0));
		assertEquals(" After CDC ", handler.comments.get(1));
		assertEquals(" Comment before li ", handler.comments.get(2));
		assertEquals(" Comment before frame ", handler.comments.get(3));
		assertEquals(" Comment before frameset ", handler.comments.get(4));
		assertEquals(" Comment before noframes ", handler.comments.get(5));

		assertEquals(21, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("font-weight", handler.propertyNames.get(2));
		assertEquals("float", handler.propertyNames.get(18));
		assertEquals("font-size", handler.propertyNames.get(19));
		assertEquals("border", handler.propertyNames.get(20));
		assertEquals(21, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.get(0).toString());
		assertEquals("bold", handler.lexicalValues.get(2).toString());
		assertEquals("left", handler.lexicalValues.get(18).toString());
		assertEquals("12pt", handler.lexicalValues.get(19).toString());
		assertEquals("solid orange", handler.lexicalValues.get(20).toString());
		assertEquals(21, handler.priorities.size());
		String prio = handler.priorities.get(12);
		assertNotNull(prio);
		assertEquals("important", prio);

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(82, loc.getColumnNumber());
		assertEquals(108, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(32, handler.ptyLocators.get(2).getColumnNumber());
		assertEquals(49, handler.ptyLocators.get(3).getColumnNumber());
		assertEquals(32, handler.ptyLocators.get(4).getColumnNumber());

		assertEquals(1, handler.atRules.size());
		assertEquals("@-webkit-viewport {width: device-width; height: device-height}",
			handler.atRules.get(0));

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetBadMedia() throws CSSException, IOException {
		try (Reader re = loadTestCSSReader("badmedia.css")) {
			parser.parseStyleSheet(re);
		}

		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: table-cell) and (display: list-item)",
			handler.supportsRuleLists.get(0).toString());
		assertEquals(14, handler.selectors.size());
		assertEquals("td", handler.selectors.get(0).toString());
		assertEquals("li", handler.selectors.get(1).toString());
		assertEquals("body", handler.selectors.get(2).toString());
		assertEquals("li", handler.selectors.get(6).toString());
		assertEquals("frame", handler.selectors.get(7).toString());
		// A wrong media query does not invalidate the whole list
		assertEquals("ul>li", handler.selectors.get(8).toString());
		assertEquals("frameset", handler.selectors.get(9).toString());
		assertEquals("noframes", handler.selectors.get(10).toString());
		assertEquals("[foo]", handler.selectors.get(11).toString());
		assertEquals("body", handler.selectors.get(12).toString());
		assertEquals(14, handler.endSelectors.size());

		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, handler.namespaceMaps.get("svg"));

		assertEquals(3, handler.mediaRuleLists.size());
		MediaQueryList mql = handler.mediaRuleLists.get(1);
		assertTrue(mql.isNotAllMedia());
		assertFalse(mql.isAllMedia());
		assertEquals("not all", mql.getMedia());
		assertEquals("screen", handler.mediaRuleLists.get(2).toString());
		assertEquals(1, handler.importMedias.size());
		assertEquals("tv,screen and (orientation: landscape)",
			handler.importMedias.get(0).toString());
		assertEquals(1, handler.importURIs.size());
		assertEquals("tv.css", handler.importURIs.get(0));

		assertEquals(4, handler.comments.size());
		assertEquals(" Comment before li ", handler.comments.get(0));
		assertEquals(" Comment before frame ", handler.comments.get(1));
		assertEquals(" Comment before frameset ", handler.comments.get(2));
		assertEquals(" Comment before noframes ", handler.comments.get(3));

		assertEquals(22, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));
		assertEquals("font-weight", handler.propertyNames.get(3));
		assertEquals("color", handler.propertyNames.get(14));
		assertEquals("display", handler.propertyNames.get(15));
		assertEquals("float", handler.propertyNames.get(19));
		assertEquals("font-size", handler.propertyNames.get(20));
		assertEquals("border", handler.propertyNames.get(21));
		assertEquals(22, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.get(0).toString());
		assertEquals("list-item", handler.lexicalValues.get(1).toString());
		assertEquals("bold", handler.lexicalValues.get(3).toString());
		assertEquals("yellow", handler.lexicalValues.get(14).toString());
		assertEquals("block", handler.lexicalValues.get(15).toString());
		assertEquals("left", handler.lexicalValues.get(19).toString());
		assertEquals("12pt", handler.lexicalValues.get(20).toString());
		assertEquals("solid orange", handler.lexicalValues.get(21).toString());
		assertEquals(22, handler.priorities.size());
		String prio = handler.priorities.get(13);
		assertNotNull(prio);
		assertEquals("important", prio);

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(82, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(2, loc.getLineNumber());
		assertEquals(108, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(4, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		assertEquals(32, handler.ptyLocators.get(3).getColumnNumber());
		assertEquals(49, handler.ptyLocators.get(4).getColumnNumber());
		assertEquals(32, handler.ptyLocators.get(5).getColumnNumber());
		assertEquals(52, handler.ptyLocators.get(6).getColumnNumber());

		assertEquals(1, handler.atRules.size());
		assertEquals("@-webkit-viewport {width: device-width; height: device-height}",
			handler.atRules.get(0));

		assertTrue(errorHandler.hasError());
		assertEquals(13, errorHandler.getLastException().getLineNumber());
		assertEquals(33, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetMediaRule() throws CSSException, IOException {
		Reader re = new StringReader("@media {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(27, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleNL() throws CSSException, IOException {
		Reader re = new StringReader("@media\n{div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleNL2() throws CSSException, IOException {
		Reader re = new StringReader("@media\nscreen{div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(26, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleEscaped() throws CSSException, IOException {
		Reader re = new StringReader("@\\6d edia screen {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleEscapedNL() throws CSSException, IOException {
		Reader re = new StringReader("@medi\\61\n screen {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleEscaped2() throws CSSException, IOException {
		Reader re = new StringReader("@medi\\61  screen {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleEscaped3() throws CSSException, IOException {
		Reader re = new StringReader("@\\6d edi\\61  screen {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleError() throws CSSException, IOException {
		Reader re = new StringReader("@+media screen {div.foo{margin:1em}}p{color:blue}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("blue", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleError2() throws CSSException, IOException {
		Reader re = new StringReader("@.media screen {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetMediaRuleErrorRecovery() throws CSSException, IOException {
		Reader re = new StringReader(
				"@media handheld,screen and (max-width:1600px) .foo}@media {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		MediaQueryList mql = handler.mediaRuleLists.get(0);
		assertEquals("handheld", mql.getMedia());
		assertTrue(mql.hasErrors());
		assertEquals(1, handler.endMediaCount);
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).getCssText());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(47, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetMediaRuleErrorRecovery2() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important;.bar{top: 1vw!important;}}@media {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.mediaRuleLists.size());
		MediaQueryList mq0 = handler.mediaRuleLists.get(0);
		assertEquals("handheld", mq0.toString());
		assertTrue(mq0.hasErrors());
		assertEquals("all", handler.mediaRuleLists.get(1).toString());
		assertEquals(2, handler.selectors.size());
		assertEquals(".bar", handler.selectors.get(0).toString());
		assertEquals("div.foo", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("top", handler.propertyNames.get(0));
		assertEquals("margin", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("1vw", handler.lexicalValues.get(0).toString());
		assertEquals("1em", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));
		assertNull(handler.priorities.get(1));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(64, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetMediaQueryError() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media handheld,only screen and (max-width:1600px .foo){bottom: 20px!important;.bar{top: 1vw!important;}}@media {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals(2, handler.selectors.size());
		assertEquals(2, handler.propertyNames.size());
		assertEquals(2, handler.lexicalValues.size());
		assertEquals(2, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(64, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetNestedMediaRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals("(max-width: 1600px)", handler.mediaRuleLists.get(1).toString());
		assertEquals(2, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).item(0).toString());
		assertEquals("div.foo", handler.selectors.get(1).item(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("bottom", handler.propertyNames.get(0));
		assertEquals("margin", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("20px", handler.lexicalValues.get(0).toString());
		assertEquals("1em", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertEquals("important", handler.priorities.get(0));
		assertNull(handler.priorities.get(1));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetNestedMediaFontFaceRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media screen{@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}}");
		parser.parseStyleSheet(re);

		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.fontFaceCount);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("src", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\"foo-family\"", handler.lexicalValues.get(0).toString());
		assertEquals("url(\"fonts/foo-file.svg#bar-icons\") format('svg')",
			handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertNull(handler.priorities.get(1));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(50, loc.getColumnNumber());
		assertEquals(104, handler.ptyLocators.get(1).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetCommon() throws CSSException, IOException {
		try (Reader re = loadTestCSSReader("common.css")) {
			parser.parseStyleSheet(re);
		}

		assertEquals(1, handler.comments.size());
		assertEquals(108, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(107));
		assertEquals(108, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("Verdana", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals("Arial", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalType.OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals("Helvetica", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("block", handler.lexicalValues.get(107).toString());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(3, loc.getLineNumber());
		assertEquals(40, loc.getColumnNumber());
		assertEquals(21, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(20, handler.ptyLocators.get(2).getColumnNumber());

		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetOtherRules() throws CSSException, IOException {
		Reader re = loadTestCSSReader("other_rules.css");
		parser.parseStyleSheet(re);
		re.close();

		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Some Font", handler.fontFeaturesNames.get(0)[0]);
		assertEquals(2, handler.featureMapNames.size());
		assertEquals("swash", handler.featureMapNames.get(0));
		assertEquals("styleset", handler.featureMapNames.get(1));

		assertEquals(2, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals("important1", handler.keyframesNames.get(1));
		assertEquals(7, handler.keyframeSelectors.size());
		assertEquals("from", handler.keyframeSelectors.get(0).toString());
		assertEquals("50%", handler.keyframeSelectors.get(1).toString());
		assertEquals("50%", handler.keyframeSelectors.get(2).toString());
		assertEquals("to", handler.keyframeSelectors.get(3).toString());
		assertEquals("from", handler.keyframeSelectors.get(4).toString());
		assertEquals("50%", handler.keyframeSelectors.get(5).toString());
		assertEquals("to", handler.keyframeSelectors.get(6).toString());

		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));

		assertEquals("size", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		assertEquals("opacity", handler.propertyNames.get(3));
		assertEquals("opacity", handler.propertyNames.get(4));
		assertEquals("margin-left", handler.propertyNames.get(5));
		assertEquals("margin-top", handler.propertyNames.get(6));
		assertEquals("margin-top", handler.propertyNames.get(7));
		assertEquals("symbols", handler.propertyNames.get(8));
		assertEquals("suffix", handler.propertyNames.get(9));
		assertEquals("swishy", handler.propertyNames.get(10));
		assertEquals("flowing", handler.propertyNames.get(11));
		assertEquals("double-W", handler.propertyNames.get(12));
		assertEquals("sharp-terminals", handler.propertyNames.get(13));
		assertEquals("background-color", handler.propertyNames.get(14));
		assertEquals(15, handler.propertyNames.size());

		assertEquals(15, handler.lexicalValues.size());
		assertEquals("4in", handler.lexicalValues.get(0).toString());
		assertEquals("0px", handler.lexicalValues.get(1).toString());
		assertEquals("110px", handler.lexicalValues.get(2).toString());
		assertEquals("1", handler.lexicalValues.get(3).toString());
		assertEquals("0.9", handler.lexicalValues.get(4).toString());
		assertEquals("200px", handler.lexicalValues.get(5).toString());
		assertEquals("50px", handler.lexicalValues.get(6).toString());
		assertEquals("100px", handler.lexicalValues.get(7).toString());
		assertEquals("\\1f44d ", handler.lexicalValues.get(8).toString());
		assertEquals("\" \"", handler.lexicalValues.get(9).toString());
		assertEquals("1", handler.lexicalValues.get(10).toString());
		assertEquals("2", handler.lexicalValues.get(11).toString());
		assertEquals("14", handler.lexicalValues.get(12).toString());
		assertEquals("16 1", handler.lexicalValues.get(13).toString());
		assertEquals("red", handler.lexicalValues.get(14).toString());
		assertEquals(15, handler.priorities.size());
		assertNull(handler.priorities.get(14));

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(12, loc.getColumnNumber());
		assertEquals(23, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(24, handler.ptyLocators.get(2).getColumnNumber());
		assertEquals(27, handler.ptyLocators.get(12).getColumnNumber());
		assertEquals(50, handler.ptyLocators.get(13).getColumnNumber());

		assertEquals(1, handler.atRules.size());
		assertEquals("@-webkit-keyframes foo {from{margin-top: 50px; }to{margin-top: 100px;}}",
			handler.atRules.get(0));

		assertEquals(1, handler.comments.size());
		assertEquals(" ignored ", handler.comments.get(0));

		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseEmptyAtRule() throws CSSException, IOException {
		Reader re = new StringReader("@;");
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(2, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseSupportsRule() throws CSSException, IOException {
		Reader re = new StringReader("@supports ((fill: 24dpi) and (fill: yellow)) {g {--pty:1}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(fill: 24dpi) and (fill: yellow)",
				handler.supportsRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals("g", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
	}

	@Test
	public void testParseSupportsRuleSelector() throws CSSException, IOException {
		Reader re = new StringReader("@supports (not selector(div col.foo||td)) {g {--pty:1}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("not selector(div col.foo||td)", handler.supportsRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals("g", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
	}

	@Test
	public void testParseNestedSupportsRule() throws CSSException, IOException {
		Reader re = new StringReader(
			"@media screen {@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }@media (color) {.blue {color:blue}}}}");
		parser.parseStyleSheet(re);

		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: flexbox) and (not (display: inline-grid))",
			handler.supportsRuleLists.get(0).toString());
		assertEquals(3, handler.selectors.size());
		assertEquals("td", handler.selectors.get(0).toString());
		assertEquals("li", handler.selectors.get(1).toString());
		assertEquals(".blue", handler.selectors.get(2).toString());
		assertEquals(3, handler.endSelectors.size());
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals("(color)", handler.mediaRuleLists.get(1).toString());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));
		assertEquals("color", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.get(0).toString());
		assertEquals("list-item", handler.lexicalValues.get(1).toString());
		assertEquals("blue", handler.lexicalValues.get(2).toString());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(102, loc.getColumnNumber());
		assertEquals(128, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(164, handler.ptyLocators.get(2).getColumnNumber());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePseudoClassNotEmpty() throws CSSException, IOException {
		Reader re = new StringReader(
			"foo:not() {td {display: table-cell; } li {display: list-item; }}");
		parser.parseStyleSheet(re);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(9, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetDefaultUserAgentSheet() throws CSSException, IOException {
		Reader re = loadCSSfromClasspath("/io/sf/carte/doc/style/css/html.css");
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(14, handler.comments.size());
		assertEquals(115, handler.selectors.size());
		assertEquals("textarea", handler.selectors.get(73).toString());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(76).toString());
		assertEquals(154, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("page-break-before", handler.propertyNames.get(153));
		assertEquals(154, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("avoid", handler.lexicalValues.get(153).toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetDefaultUserAgentSheetQuirks() throws CSSException, IOException {
		Reader re = loadCSSfromClasspath("/io/sf/carte/doc/style/css/html-quirks.css");
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(16, handler.comments.size());
		assertEquals("textarea", handler.selectors.get(74).toString());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(78).toString());
		assertEquals(127, handler.selectors.size());
		assertEquals(172, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("page-break-before", handler.propertyNames.get(171));
		assertEquals(172, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("avoid", handler.lexicalValues.get(171).toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/\\n@media {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(20, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar2() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/;@media {div.foo{margin:1em}}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(18, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedCharHigh() throws CSSException, IOException {
		Reader re = new StringReader(
			"/** Comment 1 **/\u7ff0@media {div.foo{margin:1em}}@import 'foo.css';");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar3() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/:first-child(){margin:1em}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(30, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar4() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/:first-child(foo){margin:1em}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(30, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar5() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/::first-line(){margin:1em}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(30, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar6() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/:first-line(){margin:1em}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(29, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF1() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF1.css");
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF2() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF2.css");
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("print", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("10pt", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF3() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF3.css");
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: table-cell) and (display: list-item)",
			handler.supportsRuleLists.get(0).toString());
		assertEquals(2, handler.selectors.size());
		assertEquals("td", handler.selectors.get(0).toString());
		assertEquals("li", handler.selectors.get(1).toString());
		assertEquals(2, handler.endSelectors.size());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.get(0).toString());
		assertEquals("list-item", handler.lexicalValues.get(1).toString());
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, handler.namespaceMaps.get("svg"));
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOL1() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOL1.css");
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("border", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("none", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(3, errorHandler.getLastException().getLineNumber());
		assertEquals(25, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOLNL() throws CSSException, IOException {
		Reader re = new StringReader(
			"body {\nbackground-color: red;\nfont-family: 'Times New\ncolor: blue;:;\nborder: none}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("border", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("none", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(4, errorHandler.getLastException().getLineNumber());
		assertEquals(14, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOLQuoted() throws CSSException, IOException {
		Reader re = new StringReader(
			"p {margin-left: 2pt; content: 'Hello\ncolor: blue; border: none}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("border", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("2pt", handler.lexicalValues.get(0).toString());
		assertEquals("none", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(37, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseStyleSheetComments() throws CSSException, IOException {
		Reader re = loadTestCSSReader("comments.css");
		parser.parseStyleSheet(re);
		re.close();

		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("background-color", handler.propertyNames.get(2));

		assertEquals("\"*\"", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		LexicalUnit lu = handler.lexicalValues.get(2);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());

		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(106, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(9, loc.getLineNumber());
		assertEquals(61, loc.getColumnNumber());

		assertEquals(8, handler.comments.size());
		assertEquals(" pre-rule-1 ", handler.comments.get(0));
		assertEquals(" pre-property-decl ", handler.comments.get(1));
		assertEquals(" post-property-decl ", handler.comments.get(2));
		assertEquals(" pre-rule-1-webkit ", handler.comments.get(3));
		assertEquals(" pre-rule-2 ", handler.comments.get(4));
		assertEquals(" pre-style-decl 1 ", handler.comments.get(5));
		assertEquals(" post-style-decl 1 ", handler.comments.get(6));
		assertEquals(" pre-webkit-kfs ", handler.comments.get(7));

		assertEquals(2, handler.atRules.size());
		assertEquals(
			"@-webkit-viewport /* skip-vw 1-webkit */{/* pre-viewport-decl-webkit */ width: /* skip-vw 2-webkit */device-width; /* post-viewport-decl-webkit */}",
			handler.atRules.get(0));
		assertEquals(
			"@-webkit-keyframes important1 { /* pre-webkit-kf-list */from /* post-webkit-kfsel-from */{ /* pre-webkit-kf-from-decl */margin-top: 50px;/* post-webkit-kf-from-decl */ } /* post-webkit-kf-from */50% /* post-webkit-kfsel-50% */{/* pre-webkit-kf-50%-decl */margin-top: 150px !important; /* post-webkit-kf-50%-decl */} /* post-webkit-kf-50% */to/* post-webkit-kfsel-to */{ margin-top: 100px; }/* post-webkit-kf-to */ /* post-webkit-kf-list */}",
			handler.atRules.get(1));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetComments2() throws CSSException, IOException {
		Reader re = new StringReader(
			"<!--/*--><![CDATA[/*><!--*/body{padding-top:2px}.foo {color:red}");
		parser.parseStyleSheet(re);
		assertEquals(2, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(".foo", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("padding-top", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(2, lu.getFloatValue(), 1e-5f);
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue()); // The comments found do not apply to a valid rule
		assertEquals(1, handler.comments.size());
		assertEquals("--><![CDATA[/*><!--", handler.comments.get(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetComments3() throws CSSException, IOException {
		Reader re = new StringReader("<!--/*--><!/*><!--*/body{padding-top:2px}.foo {color:red}");
		parser.parseStyleSheet(re);
		assertEquals(2, handler.selectors.size());
		assertEquals("body", handler.selectors.get(0).toString());
		assertEquals(".foo", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("padding-top", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(2, lu.getFloatValue(), 1e-5f);
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue()); // The comments found do not apply to a valid rule
		assertEquals(1, handler.comments.size());
		assertEquals("--><!/*><!--", handler.comments.get(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetComments4() throws CSSException, IOException {
		Reader re = new StringReader("p /*, article */, div {display:block}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("p,div", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		/*
		 * The comment cannot be related to a specific (N)SAC event, so we ignore it.
		 */
		assertEquals(0, handler.comments.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetComments5() throws CSSException, IOException {
		Reader re = new StringReader("p, div,/* html5 */ article  {display:block}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("p,div,article", handler.selectors.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		/*
		 * The comment cannot be related to a specific (N)SAC event, so we ignore it.
		 */
		assertEquals(0, handler.comments.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetComments6() throws CSSException, IOException {
		Reader re = new StringReader(
			"html{overflow-x:hidden!important}/* comment */[hidden]{display:none!important}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.selectors.size());
		assertEquals("html", handler.selectors.get(0).toString());
		assertEquals("[hidden]", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("overflow-x", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));

		LexicalUnit lu = handler.lexicalValues.get(0);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("hidden", lu.getStringValue());
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("none", lu.getStringValue());

		assertEquals("important", handler.priorities.get(0));
		assertEquals("important", handler.priorities.get(1));
		assertEquals(1, handler.comments.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetCustomRuleComments() throws CSSException, IOException {
		Reader re = new StringReader(
			"/* ignored */@-webkit-keyframes/* post-at-ident */foo {from{margin: 50px/* post-value */10px; }"
				+ " to {margin-top/* post-pty-name */:/* post-pty-colon */ 100px;/* post-semicolon */}}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.atRules.size());
		assertEquals(
			"@-webkit-keyframes/* post-at-ident */foo {from{margin: 50px/* post-value */10px; } to "
				+ "{margin-top/* post-pty-name */:/* post-pty-colon */ 100px;/* post-semicolon */}}",
			handler.atRules.get(0));

		assertEquals(1, handler.comments.size());
		assertEquals(" ignored ", handler.comments.get(0));
		assertFalse(errorHandler.hasError());
	}

	private static Reader loadTestCSSReader(String filename) {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/parser/" + filename);
	}

	private static Reader loadCSSfromClasspath(final String filename) {
		InputStream is = SheetParserTest.class.getResourceAsStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

}
