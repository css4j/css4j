/*

 Copyright (c) 2005-2021, Carlos Amengual.

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.PageSelector;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;

public class SheetParserTest {

	private CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseMediaList() {
		NSACMediaQueryList list = new NSACMediaQueryList();
		list.parse(parser, "tv", null);
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("tv", list.item(0));
		list.setMediaText("tv, screen");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertEquals("tv", list.item(0));
		assertEquals("screen", list.item(1));
	}

	@Test
	public void testParseSheetRule() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
				"p.myclass,:first-child{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		parser.parseStyleSheet(re);
		//
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
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(52, loc.getColumnNumber());
		assertEquals(67, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(92, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetRule2() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("hr[align=\"left\"]    {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("hr[align=\"left\"]", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(38, loc.getColumnNumber());
		assertEquals(58, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetTwoRules() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
			".fooclass{zoom:expression(function(ele){ele.style.zoom = \"1\"; document.execCommand(\"BackgroundImageCache\", false, true); skip-me:skip-value}(this))}#fooid .fooclass{margin-right:auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(2, handler.selectors.size());
		assertEquals(".fooclass", handler.selectors.getFirst().toString());
		assertEquals("#fooid .fooclass", handler.selectors.get(1).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(183, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetTwoRulesTwoProperties() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
			".fooclass{zoom:expression(function(ele){ele.style.zoom = \"1\"; document.execCommand(\"BackgroundImageCache\", false, true); }(this));margin-left:0}#fooid .fooclass{margin-right:auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(2, handler.selectors.size());
		assertEquals(".fooclass", handler.selectors.getFirst().toString());
		assertEquals("#fooid .fooclass", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(144, loc.getColumnNumber());
		assertEquals(179, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetStyleRuleBad() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(".foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(49, loc.getColumnNumber());
		assertEquals(67, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetStyleRuleBad2() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(".foo{margin-left:0;{foo:bar;} :bar;margin-right:auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());
		assertEquals(53, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(20, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetStyleRuleBadNested() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
				"@media screen and (min-width: 768px){.foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen and (min-width: 768px)", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(86, loc.getColumnNumber());
		assertEquals(104, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(43, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testMalformedStyleRule() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
				"input:not(){}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(69, loc.getColumnNumber());
		assertEquals(89, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testMalformedStyleRule2() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
				"p.myclass width:300px}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(78, loc.getColumnNumber());
		assertEquals(98, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(22, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testMalformedStyleRule3() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
				"p.myclass color:rgb(120}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(80, loc.getColumnNumber());
		assertEquals(100, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(24, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetSelectorError() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("!,p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
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
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
				"@namespace svg \"http://www.w3.org/2000/svg\";svg|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("svg|p", handler.selectors.getFirst().toString());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals("width", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("Times New Roman", handler.lexicalValues.get(0).toString());
		assertEquals("yellow", handler.lexicalValues.get(1).toString());
		assertEquals("calc(100% - 3em)", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(79, loc.getColumnNumber());
		assertEquals(94, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(119, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetSelectorErrorBadNSPrefix() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("foo|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
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
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("p, p {width: 80%}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("80%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.selectors.size());
		SelectorList selist = handler.selectors.getFirst();
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(17, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseSheetCommentWDoubleStar() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		Reader re = new StringReader(".foo {\n/**just a comment**/margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("*just a comment*", handler.comments.getFirst());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentWDoubleStar2() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		Reader re = new StringReader(".foo {  /**just a comment**/margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("*just a comment*", handler.comments.getFirst());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(45, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentWStarNL() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(".foo {  /*Newline\nhere*/:;margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("Newline\nhere", handler.comments.getFirst());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(25, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(7, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetErrorNL() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(".foo {\n:;margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(0, handler.comments.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(19, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.getLastException());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(1, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCDORuleCDC() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		//
		Reader re = new StringReader("<!-- .foo {margin-left:auto} -->");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentCDOCDCRule() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		//
		Reader re = new StringReader("<!-- --> .foo {margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(32, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentCDORule() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		//
		Reader re = new StringReader("<!-- .foo {margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentCDCRule() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		//
		Reader re = new StringReader("--> .foo {margin-left:auto}");
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(0, handler.comments.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(27, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseDefaultNS() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\");");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSEOF() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\")");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSBad() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url(;");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(16, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseDefaultNSBad2() throws CSSException, IOException {
		Reader re = new StringReader("@namespace url();");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(16, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseDefaultNSDQ() throws CSSException, IOException {
		Reader re = new StringReader("@namespace \"\" url(\"https://www.w3.org/1999/xhtml/\");");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSNoURL() throws CSSException, IOException {
		Reader re = new StringReader("@namespace \"https://www.w3.org/1999/xhtml/\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURL() throws CSSException, IOException {
		Reader re = new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURLEOF() throws CSSException, IOException {
		Reader re = new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\"");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSEOF() throws CSSException, IOException {
		Reader re = new StringReader("@namespace xhtml url(\"https://www.w3.org/1999/xhtml/\")");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSimpleNS() throws CSSException, IOException {
		Reader re = new StringReader(
				"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.selectors.size());
		assertEquals("p", handler.selectors.getFirst().toString());
		assertEquals("svg|svg", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.getFirst());
		assertEquals("margin-left", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PT, lu.getCssUnit());
		assertEquals(5, lu.getFloatValue(), 0.01f);
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(65, loc.getColumnNumber());
		assertEquals(93, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseMinimalStyleSheet() throws CSSException, IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleUserCSSReader();
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
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
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(31, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheet1() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = loadTestCSSReader("sheet1.css");
		parser.parseStyleSheet(re);
		re.close();
		//
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: table-cell) and (display: list-item)",
				handler.supportsRuleLists.get(0).toString());
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("print", handler.mediaRuleLists.get(0).toString());
		assertEquals("screen", handler.mediaRuleLists.get(1).toString());
		assertEquals(1, handler.importMedias.size());
		assertEquals("tv,screen and (orientation: landscape)", handler.importMedias.get(0).toString());
		assertEquals(1, handler.importURIs.size());
		assertEquals("tv.css", handler.importURIs.get(0));
		//
		assertEquals(7, handler.comments.size());
		assertEquals(" After CDO ", handler.comments.get(0));
		assertEquals(" After CDC ", handler.comments.get(1));
		assertEquals(" Comment before li ", handler.comments.get(2));
		assertEquals(" Comment before frame ", handler.comments.get(3));
		assertEquals(" Comment before frameset ", handler.comments.get(4));
		assertEquals(" Comment before noframes ", handler.comments.get(5));
		//
		assertEquals(21, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("font-weight", handler.propertyNames.get(2));
		assertEquals("float", handler.propertyNames.get(18));
		assertEquals("font-size", handler.propertyNames.get(19));
		assertEquals("border", handler.propertyNames.getLast());
		assertEquals(21, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.getFirst().toString());
		assertEquals("bold", handler.lexicalValues.get(2).toString());
		assertEquals("left", handler.lexicalValues.get(18).toString());
		assertEquals("12pt", handler.lexicalValues.get(19).toString());
		assertEquals("solid orange", handler.lexicalValues.getLast().toString());
		assertEquals(21, handler.priorities.size());
		String prio = handler.priorities.get(12);
		assertNotNull(prio);
		assertEquals("important", prio);
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(82, loc.getColumnNumber());
		assertEquals(108, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(32, handler.ptyLocators.get(2).getColumnNumber());
		assertEquals(49, handler.ptyLocators.get(3).getColumnNumber());
		assertEquals(32, handler.ptyLocators.get(4).getColumnNumber());
		//
		assertEquals(1, handler.atRules.size());
		assertEquals("@-webkit-viewport {width: device-width; height: device-height}",
				handler.atRules.getFirst());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetBadMedia() throws CSSException, IOException {
		Reader re = loadTestCSSReader("badmedia.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		//
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: table-cell) and (display: list-item)",
				handler.supportsRuleLists.get(0).toString());
		assertEquals(14, handler.selectors.size());
		assertEquals("td", handler.selectors.get(0).toString());
		assertEquals("li", handler.selectors.get(1).toString());
		assertEquals("body", handler.selectors.get(2).toString());
		assertEquals("li", handler.selectors.get(6).toString());
		assertEquals("body", handler.selectors.get(12).toString());
		assertEquals(14, handler.endSelectors.size());
		//
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		//
		assertEquals(3, handler.mediaRuleLists.size());
		MediaQueryList mql = handler.mediaRuleLists.get(1);
		assertTrue(mql.isNotAllMedia());
		assertFalse(mql.isAllMedia());
		assertEquals("not all", mql.getMedia());
		assertEquals("screen", handler.mediaRuleLists.get(2).toString());
		assertEquals(1, handler.importMedias.size());
		assertEquals("tv,screen and (orientation: landscape)", handler.importMedias.get(0).toString());
		assertEquals(1, handler.importURIs.size());
		assertEquals("tv.css", handler.importURIs.get(0));
		//
		assertEquals(4, handler.comments.size());
		assertEquals(" Comment before li ", handler.comments.get(0));
		assertEquals(" Comment before frame ", handler.comments.get(1));
		assertEquals(" Comment before frameset ", handler.comments.get(2));
		assertEquals(" Comment before noframes ", handler.comments.get(3));
		//
		assertEquals(22, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));
		assertEquals("font-weight", handler.propertyNames.get(3));
		assertEquals("color", handler.propertyNames.get(14));
		assertEquals("float", handler.propertyNames.get(19));
		assertEquals("font-size", handler.propertyNames.get(20));
		assertEquals("border", handler.propertyNames.getLast());
		assertEquals(22, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.get(0).toString());
		assertEquals("list-item", handler.lexicalValues.get(1).toString());
		assertEquals("bold", handler.lexicalValues.get(3).toString());
		assertEquals("yellow", handler.lexicalValues.get(14).toString());
		assertEquals("left", handler.lexicalValues.get(19).toString());
		assertEquals("12pt", handler.lexicalValues.get(20).toString());
		assertEquals("solid orange", handler.lexicalValues.getLast().toString());
		assertEquals(22, handler.priorities.size());
		String prio = handler.priorities.get(13);
		assertNotNull(prio);
		assertEquals("important", prio);
		//
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
		//
		assertEquals(1, handler.atRules.size());
		assertEquals("@-webkit-viewport {width: device-width; height: device-height}",
				handler.atRules.getFirst());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(13, errorHandler.getLastException().getLineNumber());
		assertEquals(35, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRule() throws CSSException, IOException {
		Reader re = new StringReader("@media {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(27, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleNL() throws CSSException, IOException {
		Reader re = new StringReader("@media\n{div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleNL2() throws CSSException, IOException {
		Reader re = new StringReader("@media\nscreen{div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(26, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleEscaped() throws CSSException, IOException {
		Reader re = new StringReader("@\\6d edia screen {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleEscapedNL() throws CSSException, IOException {
		Reader re = new StringReader("@medi\\61\n screen {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleEscaped2() throws CSSException, IOException {
		Reader re = new StringReader("@medi\\61  screen {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleEscaped3() throws CSSException, IOException {
		Reader re = new StringReader("@\\6d edi\\61  screen {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleError() throws CSSException, IOException {
		Reader re = new StringReader("@+media screen {div.foo{margin:1em}}p{color:blue}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleError2() throws CSSException, IOException {
		Reader re = new StringReader("@.media screen {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleErrorRecovery() throws CSSException, IOException {
		Reader re = new StringReader(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }@media {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(2, handler.mediaRuleLists.size());
		MediaQueryList mq0 = handler.mediaRuleLists.get(0);
		assertEquals("handheld", mq0.toString());
		assertTrue(mq0.hasErrors());
		assertEquals("all", handler.mediaRuleLists.get(1).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(64, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetNestedMediaRule() throws CSSException, IOException {
		Reader re = new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetNestedMediaFontFaceRule() throws CSSException, IOException {
		Reader re = new StringReader(
				"@media screen{@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.fontFaceCount);
		assertEquals(2, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.get(0));
		assertEquals("src", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\"foo-family\"", handler.lexicalValues.get(0).toString());
		assertEquals("url(\"fonts/foo-file.svg#bar-icons\") format('svg')", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertNull(handler.priorities.get(1));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(50, loc.getColumnNumber());
		assertEquals(104, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleIEHack() throws CSSException, IOException {
		Reader re = new StringReader("@media screen\\0 {.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen\\0", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.get(0).item(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(0, handler.atRules.size());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetCommon() throws CSSException, IOException {
		Reader re = loadTestCSSReader("common.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		//
		assertEquals(1, handler.comments.size());
		assertEquals(108, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("display", handler.propertyNames.getLast());
		assertEquals(108, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
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
		assertEquals("block", handler.lexicalValues.getLast().toString());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(3, loc.getLineNumber());
		assertEquals(40, loc.getColumnNumber());
		assertEquals(21, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(20, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetRemote() throws CSSException, IOException {
		if (TestConfig.REMOTE_TESTS) {
			TestCSSHandler handler = new TestCSSHandler();
			parser.setDocumentHandler(handler);
			TestErrorHandler errorHandler = new TestErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.parseStyleSheet("https://raw.githubusercontent.com/necolas/normalize.css/master/normalize.css");
			assertTrue(handler.comments.size() != 0);
			assertTrue(handler.selectors.size() != 0);
			assertTrue(handler.propertyNames.size() != 0);
			assertFalse(errorHandler.hasError());
			handler.checkRuleEndings();
		}
	}

	@Test
	public void testParseStyleSheetFontFaceRule() throws CSSException, IOException {
		Reader re = loadCSSfromClasspath("/io/sf/carte/doc/agent/common.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		//
		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Foo Sans", handler.fontFeaturesNames.get(0)[0]);
		assertEquals(1, handler.featureMapNames.size());
		assertEquals("styleset", handler.featureMapNames.get(0));
		//
		assertEquals(4, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("font-family", handler.propertyNames.get(1));
		assertEquals("src", handler.propertyNames.get(2));
		assertEquals("my-style", handler.propertyNames.get(3));
		assertEquals(4, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("'OpenSans Regular'", handler.lexicalValues.get(1).toString());
		assertEquals("url('/fonts/OpenSans-Regular.ttf')", handler.lexicalValues.get(2).toString());
		assertEquals("2", handler.lexicalValues.get(3).toString());
		assertEquals(4, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertNull(handler.priorities.get(3));
		assertEquals(1, handler.fontFaceCount);
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		assertEquals(33, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(74, handler.ptyLocators.get(2).getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(9, loc.getLineNumber());
		assertEquals(14, loc.getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetFontFaceRuleWrongChar() throws CSSException, IOException {
		Reader re = new StringReader(
				"\ufeff@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}@import 'foo.css';");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(0, handler.fontFaceCount);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		//
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule() throws CSSException, IOException {
		Reader re = loadTestCSSReader("page.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(6, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("margin-top", handler.propertyNames.get(1));
		assertEquals("content", handler.propertyNames.get(2));
		assertEquals("margin-left", handler.propertyNames.get(3));
		assertEquals("content", handler.propertyNames.get(4));
		assertEquals("margin-right", handler.propertyNames.get(5));
		assertEquals(6, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("20%", handler.lexicalValues.get(1).toString());
		assertEquals("none", handler.lexicalValues.get(2).toString());
		assertEquals("10%", handler.lexicalValues.get(3).toString());
		assertEquals("counter(page)", handler.lexicalValues.get(4).toString());
		assertEquals(6, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.get(0).toString());
		assertEquals("foo:left", handler.pageRuleSelectors.get(1).toString());
		assertEquals("bar:right,:blank", handler.pageRuleSelectors.get(2).toString());
		assertEquals(2, handler.marginRuleNames.size());
		assertEquals("top-center", handler.marginRuleNames.get(0));
		assertEquals("bottom-center", handler.marginRuleNames.get(1));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(5, loc.getLineNumber());
		assertEquals(17, loc.getColumnNumber());
		assertEquals(30, handler.ptyLocators.get(2).getColumnNumber());
		assertEquals(18, handler.ptyLocators.get(3).getColumnNumber());
		assertEquals(42, handler.ptyLocators.get(4).getColumnNumber());
		assertEquals(19, handler.ptyLocators.get(5).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule2() throws CSSException, IOException {
		Reader re = new StringReader("@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		assertEquals(52, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(63, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule3() throws CSSException, IOException {
		Reader re = new StringReader(
				"@page LetterHead:first{margin-top:20%;@top-left{content:'foo';color:green}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		PageSelector ps = psl.item(0);
		assertEquals("LetterHead:first", ps.toString());
		assertEquals("LetterHead", ps.getCssText());
		assertEquals("LetterHead", ps.getName());
		assertEquals(PageSelector.Type.PAGE_TYPE, ps.getSelectorType());
		ps = ps.getNext();
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":first", ps.getCssText());
		assertEquals("first", ps.getName());
		assertNull(ps.getNext());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals("green", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(38, loc.getColumnNumber());
		assertEquals(62, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(74, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule4() throws CSSException, IOException {
		Reader re = new StringReader(
				"@page Foo:blank:first,:right{margin-top:20%;@top-left{content:'foo';color:green}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(2, psl.getLength());
		assertEquals("Foo:blank:first,:right", psl.toString());
		PageSelector ps = psl.item(0);
		assertEquals("Foo:blank:first", ps.toString());
		assertEquals("Foo", ps.getCssText());
		assertEquals("Foo", ps.getName());
		assertEquals(PageSelector.Type.PAGE_TYPE, ps.getSelectorType());
		ps = ps.getNext();
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":blank", ps.getCssText());
		assertEquals("blank", ps.getName());
		ps = ps.getNext();
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":first", ps.getCssText());
		assertEquals("first", ps.getName());
		assertNull(ps.getNext());
		//
		ps = psl.item(1);
		assertEquals(":right", ps.toString());
		assertEquals(":right", ps.getCssText());
		assertEquals("right", ps.getName());
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertNull(ps.getNext());
		//
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals("green", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(44, loc.getColumnNumber());
		assertEquals(68, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(80, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule5() throws CSSException, IOException {
		Reader re = new StringReader(
				"@page:first{margin-top:20%;@top-left{content:'foo';color:green}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals(":first", psl.toString());
		PageSelector ps = psl.item(0);
		assertEquals(PageSelector.Type.PSEUDO_PAGE, ps.getSelectorType());
		assertEquals(":first", ps.toString());
		assertEquals(":first", ps.getCssText());
		assertEquals("first", ps.getName());
		assertNull(ps.getNext());
		assertEquals(1, handler.marginRuleNames.size());
		assertEquals("top-left", handler.marginRuleNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("content", handler.propertyNames.get(1));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("'foo'", handler.lexicalValues.get(1).toString());
		assertEquals("green", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(7, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("startMargin", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endMargin", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(27, loc.getColumnNumber());
		assertEquals(51, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(63, handler.ptyLocators.get(2).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad() throws CSSException, IOException {
		Reader re = new StringReader("@page :first{margin-top:20%;myname@top-left{content:'foo';color:blue}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(35, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad2() throws CSSException, IOException {
		Reader re = new StringReader("@page :first{margin-top:20%;myname @top-left{content:'foo';color:blue}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(36, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad3() throws CSSException, IOException {
		Reader re = new StringReader("@page :first{margin-top:20%; top-left{content:'foo';color:blue}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		assertEquals(":first", handler.pageRuleSelectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(28, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(38, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad4() throws CSSException, IOException {
		Reader re = new StringReader(
				"@page LetterHead :first{margin-top:20%;@top-left{content:'foo';color:green}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(0, handler.pageRuleSelectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(0, handler.eventSeq.size());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(24, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBadNestedMarginBox() throws CSSException, IOException {
		Reader re = new StringReader(
				"@page LetterHead:first{margin-top:20%;@top-left @{content:'foo';color:green}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		assertEquals(0, handler.marginRuleNames.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(49, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBadNestedMarginBox2() throws CSSException, IOException {
		Reader re = new StringReader(
				"@page LetterHead:first{margin-top:20%;@top-foo{content:'foo';color:green}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		PageSelectorList psl = handler.pageRuleSelectors.getFirst();
		assertEquals(1, psl.getLength());
		assertEquals("LetterHead:first", psl.toString());
		assertEquals(0, handler.marginRuleNames.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(3, handler.eventSeq.size());
		assertEquals("startPage", handler.eventSeq.get(0));
		assertEquals("property", handler.eventSeq.get(1));
		assertEquals("endPage", handler.eventSeq.get(2));
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(47, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleNestedOnMediaRule() throws CSSException, IOException {
		Reader re = new StringReader("@media print {@page {margin-top: 20%;}h3 {width: 80%}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.pageRuleSelectors.size());
		assertNull(handler.pageRuleSelectors.getFirst());
		assertEquals(1, handler.mediaRuleLists.size());
		MediaQueryList medialist = handler.mediaRuleLists.getFirst();
		assertEquals(1, medialist.getLength());
		assertEquals("print", medialist.item(0));
		assertEquals(1, handler.selectors.size());
		assertEquals("h3", handler.selectors.getFirst().toString());
		assertEquals(1, handler.endSelectors.size());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-top", handler.propertyNames.get(0));
		assertEquals("width", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("20%", handler.lexicalValues.get(0).toString());
		assertEquals("80%", handler.lexicalValues.get(1).toString());
		assertEquals(2, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals("endPage", handler.eventSeq.get(3));
		assertEquals("startSelector", handler.eventSeq.get(4));
		assertEquals("endSelector", handler.eventSeq.get(6));
		assertEquals("endMedia", handler.eventSeq.get(7));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(37, loc.getColumnNumber());
		assertEquals(53, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetOtherRules() throws CSSException, IOException {
		Reader re = loadTestCSSReader("other_rules.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		//
		assertEquals(1, handler.viewportCount);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Some Font", handler.fontFeaturesNames.get(0)[0]);
		assertEquals(2, handler.featureMapNames.size());
		assertEquals("swash", handler.featureMapNames.get(0));
		assertEquals("styleset", handler.featureMapNames.get(1));
		//
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
		//
		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));
		//
		assertEquals(15, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
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
		assertEquals(15, handler.lexicalValues.size());
		assertEquals("device-width", handler.lexicalValues.get(0).toString());
		assertEquals("0px", handler.lexicalValues.get(1).toString());
		assertEquals("110px", handler.lexicalValues.get(2).toString());
		assertEquals("1", handler.lexicalValues.get(3).toString());
		assertEquals("0.9", handler.lexicalValues.get(4).toString());
		assertEquals("200px", handler.lexicalValues.get(5).toString());
		assertEquals("50px", handler.lexicalValues.get(6).toString());
		assertEquals("100px", handler.lexicalValues.get(7).toString());
		assertEquals("\\1F44D", handler.lexicalValues.get(8).toString());
		assertEquals("\" \"", handler.lexicalValues.get(9).toString());
		assertEquals("1", handler.lexicalValues.get(10).toString());
		assertEquals("2", handler.lexicalValues.get(11).toString());
		assertEquals("14", handler.lexicalValues.get(12).toString());
		assertEquals("16 1", handler.lexicalValues.get(13).toString());
		assertEquals("red", handler.lexicalValues.get(14).toString());
		assertEquals(15, handler.priorities.size());
		assertNull(handler.priorities.get(14));
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(22, loc.getColumnNumber());
		assertEquals(23, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(24, handler.ptyLocators.get(2).getColumnNumber());
		assertEquals(27, handler.ptyLocators.get(12).getColumnNumber());
		assertEquals(50, handler.ptyLocators.get(13).getColumnNumber());
		//
		assertEquals(1, handler.atRules.size());
		assertEquals("@-webkit-keyframes foo {from{margin-top: 50px; }to{margin-top: 100px;}}",
				handler.atRules.getFirst());
		//
		assertEquals(1, handler.comments.size());
		assertEquals(" ignored ", handler.comments.get(0));
		//
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseCharsetRule() throws CSSException, IOException {
		Reader re = new StringReader("@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCharsetRule2() throws CSSException, IOException {
		Reader re = new StringReader("/* My sheet */\n@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.comments.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCharsetRuleBad() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css');@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(24, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad2() throws CSSException, IOException {
		Reader re = new StringReader("p{display:block;}@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).item(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(18, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad3() throws CSSException, IOException {
		Reader re = new StringReader("@media print {@page {margin-top: 20%;}h3 {width: 80%}}@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.pageRuleSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(55, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad4() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf')}@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(41, loc.getColumnNumber());
		assertEquals(74, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(75, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad5() throws CSSException, IOException {
		Reader re = new StringReader(
				"@font-feature-values Foo Sans, Bar {@styleset {my-style: 2;}}@charset \"utf-8\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.fontFeaturesNames.size());
		assertEquals("Foo Sans", handler.fontFeaturesNames.get(0)[0]);
		assertEquals("Bar", handler.fontFeaturesNames.get(0)[1]);
		assertEquals(1, handler.featureMapNames.size());
		assertEquals("styleset", handler.featureMapNames.get(0));
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(59, loc.getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(62, errorHandler.getLastException().getColumnNumber());
	}

	/*
	 * Make sure that parsing recovers from bad charset rule error.
	 * 
	 */
	@Test
	public void testParseCharsetRuleBadRecovery() throws CSSException, IOException {
		Reader re = new StringReader("p{display:block;}@charset \"utf-8\";span {color: blue;}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(0, handler.atRules.size());
		assertEquals(2, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).item(0).toString());
		assertEquals("span", handler.selectors.get(1).item(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(16, loc.getColumnNumber());
		assertEquals(52, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(18, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRule() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css');");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRule2() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css);");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMedia() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css) print;");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMedia2() throws CSSException, IOException {
		Reader re = new StringReader("@import url(foo.css) screen, tv;");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQuery() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') (orientation:landscape);");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("(orientation: landscape)", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQuery2() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and (orientation:landscape);");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (orientation: landscape)", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and (800px<width<=1200px);");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (800px < width <= 1200px)", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4b() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and (resolution>=72dpi);");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (resolution >= 72dpi)", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryLevel4calc() throws CSSException, IOException {
		Reader re = new StringReader(
				"@import url('foo.css') screen and (800px<width<=calc(2400px/2 + 2*100px - 50px));");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (800px < width <= calc(2400px/2 + 2*100px - 50px))", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryBad() throws CSSException, IOException {
		Reader re = new StringReader("@import url('foo.css') screen and ((orientation:landscape);");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(59, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseImportRuleNoUrl() throws CSSException, IOException {
		Reader re = new StringReader("@import 'foo.css';");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlMedia() throws CSSException, IOException {
		Reader re = new StringReader("@import 'foo.css' print;");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlDQ() throws CSSException, IOException {
		Reader re = new StringReader("@import \"foo.css\";");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlDQMedia() throws CSSException, IOException {
		Reader re = new StringReader("@import \"foo.css\" screen, tv;");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleBad() throws CSSException, IOException {
		Reader re = new StringReader("foo@import url('bar.css');");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(1, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseEmptyAtRule() throws CSSException, IOException {
		Reader re = new StringReader("@;");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
	public void testParseCounterStyleRule() throws CSSException, IOException {
		Reader re = new StringReader("@counter-style foo {symbols: \\1F44D;\n suffix: \" \";\n}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		assertEquals("suffix", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\\1F44D", handler.lexicalValues.get(0).toString());
		assertEquals("\" \"", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		assertEquals(13, handler.ptyLocators.get(1).getColumnNumber());
		//
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePropertyRule() throws IOException {
		Reader re = new StringReader(
				"@property --my-length {syntax: '<length>'; inherits: false;\ninitial-value: 24px; ignore-me:0}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(4, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals("ignore-me", handler.propertyNames.get(3));
		assertEquals(4, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		assertEquals("24px", handler.lexicalValues.get(2).toString());
		assertEquals("0", handler.lexicalValues.get(3).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(42, loc.getColumnNumber());
		assertEquals(59, handler.ptyLocators.get(1).getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(2, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(2, loc.getLineNumber());
		assertEquals(33, loc.getColumnNumber());
		//
		assertEquals("endProperty", handler.eventSeq.get(5));
		//
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePropertyRuleNoNameError() throws IOException {
		Reader re = new StringReader(
				"@property {syntax: '<length>'; inherits: false;\ninitial-value: 24px}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(0, handler.customPropertyNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.atRules.size());
		//
		assertEquals(0, handler.eventSeq.size());
		//
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(11, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleBadNameError() throws IOException {
		Reader re = new StringReader(
				"@property 111 {syntax: '<length>'; inherits: false;\ninitial-value: 24px}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(0, handler.customPropertyNames.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.atRules.size());
		//
		assertEquals(0, handler.eventSeq.size());
		//
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(15, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleSyntaxDescriptorError() throws IOException {
		Reader re = new StringReader(
				"@property --my-length {syntax: '<foo>'; inherits: false;\ninitial-value: 24px}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("inherits", handler.propertyNames.get(0));
		assertEquals("initial-value", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("false", handler.lexicalValues.get(0).toString());
		assertEquals("24px", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(56, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(2, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		//
		assertEquals("endProperty-Discard", handler.eventSeq.get(3));
		//
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(20, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleInitialValueDescriptorError() throws IOException {
		Reader re = new StringReader(
				"@property --my-length {syntax: '<length>'; inherits: false;\ninitial-value: 72dpi}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		assertEquals("72dpi", handler.lexicalValues.get(2).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(42, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(1, loc.getLineNumber());
		assertEquals(59, loc.getColumnNumber());
		//
		assertEquals("endProperty-Discard", handler.eventSeq.get(4));
		//
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(21, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParsePropertyRuleInitialValueDescriptorErrorRelativeLength() throws IOException {
		Reader re = new StringReader(
				"@property --my-length {syntax: '<length>'; inherits: false;\ninitial-value: 2.1em}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.customPropertyNames.size());
		assertEquals("--my-length", handler.customPropertyNames.get(0));
		assertEquals(3, handler.propertyNames.size());
		assertEquals("syntax", handler.propertyNames.get(0));
		assertEquals("inherits", handler.propertyNames.get(1));
		assertEquals("initial-value", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("'<length>'", handler.lexicalValues.get(0).toString());
		assertEquals("false", handler.lexicalValues.get(1).toString());
		assertEquals("2.1em", handler.lexicalValues.get(2).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(42, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(1, loc.getLineNumber());
		assertEquals(59, loc.getColumnNumber());
		//
		assertEquals("endProperty-Discard", handler.eventSeq.get(4));
		//
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
		assertEquals(2, errorHandler.getLastException().getLineNumber());
		assertEquals(21, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseViewportRule() throws CSSException, IOException {
		Reader re = new StringReader("@viewport\n{width: device-width;}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.viewportCount);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("device-width", handler.lexicalValues.get(0).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(21, loc.getColumnNumber());
		//
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseKeyframesRule() throws CSSException, IOException {
		Reader re = new StringReader(
				"@keyframes slide-right {\nfrom {margin-left: 0px;}\n50% {margin-left: 110px; opacity: 1;}\n"
				+ "70% {opacity: 0.9;}\nto\n{margin-left: 200px;}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
		assertEquals(1, handler.keyframesNames.size());
		assertEquals("slide-right", handler.keyframesNames.get(0));
		assertEquals(4, handler.keyframeSelectors.size());
		assertEquals("from", handler.keyframeSelectors.get(0).toString());
		assertEquals("50%", handler.keyframeSelectors.get(1).toString());
		assertEquals("70%", handler.keyframeSelectors.get(2).toString());
		assertEquals("to", handler.keyframeSelectors.get(3).toString());
		assertEquals(5, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.get(0));
		assertEquals("margin-left", handler.propertyNames.get(1));
		assertEquals("opacity", handler.propertyNames.get(2));
		assertEquals("opacity", handler.propertyNames.get(3));
		assertEquals("margin-left", handler.propertyNames.get(4));
		assertEquals(5, handler.lexicalValues.size());
		assertEquals("0px", handler.lexicalValues.get(0).toString());
		assertEquals("110px", handler.lexicalValues.get(1).toString());
		assertEquals("1", handler.lexicalValues.get(2).toString());
		assertEquals("0.9", handler.lexicalValues.get(3).toString());
		assertEquals("200px", handler.lexicalValues.get(4).toString());
		assertEquals(0, handler.atRules.size());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(23, loc.getColumnNumber());
		loc = handler.ptyLocators.get(1);
		assertEquals(3, loc.getLineNumber());
		assertEquals(24, loc.getColumnNumber());
		loc = handler.ptyLocators.get(2);
		assertEquals(3, loc.getLineNumber());
		assertEquals(36, loc.getColumnNumber());
		loc = handler.ptyLocators.get(3);
		assertEquals(4, loc.getLineNumber());
		assertEquals(18, loc.getColumnNumber());
		loc = handler.ptyLocators.get(4);
		assertEquals(6, loc.getLineNumber());
		assertEquals(20, loc.getColumnNumber());
		//
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNestedSupportsRule() throws CSSException, IOException {
		Reader re = new StringReader(
				"@media screen {@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }@media (color) {.blue {color:blue}}}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		//
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
		assertEquals("screen", handler.mediaRuleLists.getFirst().toString());
		assertEquals("(color)", handler.mediaRuleLists.get(1).toString());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("display", handler.propertyNames.get(1));
		assertEquals("color", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("table-cell", handler.lexicalValues.get(0).toString());
		assertEquals("list-item", handler.lexicalValues.get(1).toString());
		assertEquals("blue", handler.lexicalValues.get(2).toString());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(1, loc.getLineNumber());
		assertEquals(102, loc.getColumnNumber());
		assertEquals(128, handler.ptyLocators.get(1).getColumnNumber());
		assertEquals(164, handler.ptyLocators.get(2).getColumnNumber());
		//
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePseudoClassNotEmpty() throws CSSException, IOException {
		Reader re = new StringReader("foo:not() {td {display: table-cell; } li {display: list-item; }}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(14, handler.comments.size());
		assertEquals("textarea", handler.selectors.get(73).toString());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(76).toString());
		assertEquals(115, handler.selectors.size());
		assertEquals(154, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("page-break-before", handler.propertyNames.getLast());
		assertEquals(154, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("avoid", handler.lexicalValues.getLast().toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetDefaultUserAgentSheetQuirks() throws CSSException, IOException {
		Reader re = loadCSSfromClasspath("/io/sf/carte/doc/style/css/html-quirks.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(16, handler.comments.size());
		assertEquals("textarea", handler.selectors.get(74).toString());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(78).toString());
		assertEquals(127, handler.selectors.size());
		assertEquals(172, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("page-break-before", handler.propertyNames.getLast());
		assertEquals(172, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("avoid", handler.lexicalValues.getLast().toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/\\n@media {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(19, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar2() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/;@media {div.foo{margin:1em}}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedCharHigh() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/\u7ff0@media {div.foo{margin:1em}}@import 'foo.css';");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar3() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/:first-child(){margin:1em}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar4() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/:first-child(foo){margin:1em}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar5() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/::first-line(){margin:1em}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar6() throws CSSException, IOException {
		Reader re = new StringReader("/** Comment 1 **/:first-line(){margin:1em}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF1() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF1.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF2() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF2.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("print", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("10pt", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF3() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF3.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
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
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertFalse(errorHandler.hasError());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOL1() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOL1.css");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		re.close();
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedEOLNL() throws CSSException, IOException {
		Reader re = new StringReader(
				"body {\nbackground-color: red;\nfont-family: 'Times New\ncolor: blue;:;\nborder: none}");
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
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
		assertEquals(13, errorHandler.getLastException().getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = loadTestCSSReader("comments.css");
		parser.parseStyleSheet(re);
		re.close();
		//
		assertEquals(1, handler.viewportCount);
		//
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.getFirst());
		assertEquals("background-color", handler.propertyNames.get(1));
		//
		assertEquals("device-width", handler.lexicalValues.get(0).toString());
		LexicalUnit lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());
		//
		Locator loc = handler.ptyLocators.get(0);
		assertEquals(2, loc.getLineNumber());
		assertEquals(86, loc.getColumnNumber());
		assertEquals(61, handler.ptyLocators.get(1).getColumnNumber());
		//
		assertEquals(8, handler.comments.size());
		assertEquals(" pre-rule-1 ", handler.comments.get(0));
		assertEquals(" pre-viewport-decl ", handler.comments.get(1));
		assertEquals(" post-viewport-decl ", handler.comments.get(2));
		assertEquals(" pre-rule-1-webkit ", handler.comments.get(3));
		assertEquals(" pre-rule-2 ", handler.comments.get(4));
		assertEquals(" pre-style-decl 1 ", handler.comments.get(5));
		assertEquals(" post-style-decl 1 ", handler.comments.get(6));
		assertEquals(" pre-webkit-kfs ", handler.comments.get(7));
		//
		assertEquals(2, handler.atRules.size());
		assertEquals(
				"@-webkit-viewport /* skip-vw 1-webkit */{/* pre-viewport-decl-webkit */ width: /* skip-vw 2-webkit */device-width; /* post-viewport-decl-webkit */}",
				handler.atRules.get(0));
		assertEquals(
				"@-webkit-keyframes important1 { /* pre-webkit-kf-list */from /* post-webkit-kfsel-from */{ /* pre-webkit-kf-from-decl */margin-top: 50px;/* post-webkit-kf-from-decl */ } /* post-webkit-kf-from */50% /* post-webkit-kfsel-50% */{/* pre-webkit-kf-50%-decl */margin-top: 150px !important; /* post-webkit-kf-50%-decl */} /* post-webkit-kf-50% */to/* post-webkit-kfsel-to */{ margin-top: 100px; }/* post-webkit-kf-to */ /* post-webkit-kf-list */}",
				handler.atRules.get(1));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments2() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("<!--/*--><![CDATA[/*><!--*/body{padding-top:2px}.foo {color:red}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(".foo", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("padding-top", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.get(1));

		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(2, lu.getFloatValue(), 1e-5);
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());

		// The comments found do not apply to a valid rule
		assertEquals(1, handler.comments.size());
		assertEquals("--><![CDATA[/*><!--", handler.comments.getFirst());

		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments3() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("<!--/*--><!/*><!--*/body{padding-top:2px}.foo {color:red}");
		parser.parseStyleSheet(re);

		assertEquals(2, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(".foo", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("padding-top", handler.propertyNames.getFirst());
		assertEquals("color", handler.propertyNames.get(1));

		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.DIMENSION, lu.getLexicalUnitType());
		assertEquals(CSSUnit.CSS_PX, lu.getCssUnit());
		assertEquals(2, lu.getFloatValue(), 1e-5);
		lu = handler.lexicalValues.get(1);
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());

		// The comments found do not apply to a valid rule
		assertEquals(1, handler.comments.size());
		assertEquals("--><!/*><!--", handler.comments.getFirst());

		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments4() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("p /*, article */, div {display:block}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("p,div", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		/*
		 * The comment cannot be related to a specific (N)SAC event, so we
		 * ignore it.
		 */
		assertEquals(0, handler.comments.size());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments5() throws CSSException, IOException {
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader("p, div,/* html5 */ article  {display:block}");
		parser.parseStyleSheet(re);
		assertEquals(1, handler.selectors.size());
		assertEquals("p,div,article", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalType.IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		/*
		 * The comment cannot be related to a specific (N)SAC event, so we
		 * ignore it.
		 */
		assertEquals(0, handler.comments.size());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetAsteriskHack() throws CSSException, IOException {
		TestDeclarationHandler handler = new TestDeclarationHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(".foo{*width: 80%}");
		parser.parseStyleSheet(re);
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.getLastException().getLineNumber());
		assertEquals(6, errorHandler.getLastException().getColumnNumber());
		errorHandler.reset();
		parser.setFlag(CSSParser.Flag.STARHACK);
		re = new StringReader(".foo{*width: 80%}");
		parser.parseStyleSheet(re);
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(80, lu.getFloatValue(), 0.01);
	}

	private static Reader loadTestCSSReader(String filename) {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/parser/" + filename);
	}

	private static Reader loadCSSfromClasspath(final String filename) {
		InputStream is = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				return this.getClass().getResourceAsStream(filename);
			}
		});
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}
}
