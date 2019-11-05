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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.parser.RuleParserTest.TestRuleErrorHandler;

public class SheetParserTest {

	private static CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseMediaList() {
		SACMediaList list = CSSParser.parseMediaList("tv");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("tv", list.item(0));
		list = CSSParser.parseMediaList("tv, screen");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertEquals("tv", list.item(0));
		assertEquals("screen", list.item(1));
	}

	@Test
	public void testParseSheetRule() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader("p.myclass,:first-child{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseStyleSheet(source);
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetRule2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"hr[align=\"left\"]    {margin-left : 0 ;margin-right : auto;}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals("hr[align=\"left\"]", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetTwoRules() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
			".fooclass{zoom:expression(function(ele){ele.style.zoom = \"1\"; document.execCommand(\"BackgroundImageCache\", false, true); skip-me:skip-value}(this))}#fooid .fooclass{margin-right:auto;}");
		parser.parseStyleSheet(new InputSource(re));
		assertEquals(2, handler.selectors.size());
		assertEquals(".fooclass", handler.selectors.getFirst().toString());
		assertEquals("#fooid .fooclass", handler.selectors.get(1).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-right", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetTwoRulesTwoProperties() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = new StringReader(
			".fooclass{zoom:expression(function(ele){ele.style.zoom = \"1\"; document.execCommand(\"BackgroundImageCache\", false, true); }(this));margin-left:0}#fooid .fooclass{margin-right:auto;}");
		parser.parseStyleSheet(new InputSource(re));
		assertEquals(2, handler.selectors.size());
		assertEquals(".fooclass", handler.selectors.getFirst().toString());
		assertEquals("#fooid .fooclass", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetStyleRuleBad() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				".foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(6, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetStyleRuleBadNested() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"@media screen and (min-width: 768px){.foo{@transform : translateY(-5px);margin-left:0;margin-right:auto;}}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen and (min-width: 768px)", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.endMediaCount);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(43, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testMalformedStyleRule() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"input:not(){}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(11, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testMalformedStyleRule2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"p.myclass width:300px}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(22, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testMalformedStyleRule3() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"p.myclass color:rgb(120}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", handler.selectors.getFirst().toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		assertEquals("margin-right", handler.propertyNames.getLast());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_INTEGER, lu.getLexicalUnitType());
		assertEquals(0, lu.getIntegerValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(24, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetSelectorError() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"!,p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseStyleSheet(source);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(1, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseSheetNSRule() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"@namespace svg \"http://www.w3.org/2000/svg\";svg|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseStyleSheet(source);
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetSelectorErrorBadNSPrefix() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"foo|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseStyleSheet(source);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(1, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseSheetDuplicateSelector() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader("p, p {width: 80%}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.propertyNames.size());
		assertEquals("width", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("80%", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.selectors.size());
		SelectorList selist = handler.selectors.getFirst();
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertTrue(errorHandler.hasWarning());
	}

	@Test
	public void testParseSheetCommentWDoubleStar() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		InputSource source = new InputSource(new StringReader(
				".foo {\n/**just a comment**/margin-left:auto}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("*just a comment*", handler.comments.getFirst());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentWDoubleStar2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		InputSource source = new InputSource(new StringReader(
				".foo {  /**just a comment**/margin-left:auto}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("*just a comment*", handler.comments.getFirst());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentWStarNL() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				".foo {  /*Newline\nhere*/:;margin-left:auto}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(1, handler.comments.size());
		assertEquals("Newline\nhere", handler.comments.getFirst());
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.exception);
		assertEquals(2, errorHandler.exception.getLineNumber());
		assertEquals(7, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetErrorNL() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				".foo {\n:;margin-left:auto}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertEquals(0, handler.comments.size());
		assertTrue(errorHandler.hasError());
		assertNotNull(errorHandler.exception);
		assertEquals(2, errorHandler.exception.getLineNumber());
		assertEquals(1, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentEnd() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		InputSource source = new InputSource(new StringReader(
				".foo {  <!---just a --comment--->margin-left:auto}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSheetCommentEnd2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		InputSource source = new InputSource(new StringReader(
				".foo {  <!---just a --comment---->margin-left:auto}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin-left", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("auto", lu.getStringValue());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseDefaultNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\");"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSEOF() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\")"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url(;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(16, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseDefaultNSBad2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url();"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(16, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseDefaultNSDQ() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace \"\" url(\"https://www.w3.org/1999/xhtml/\");"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSNoURL() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace \"https://www.w3.org/1999/xhtml/\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURL() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURLEOF() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\""));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSEOF() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace xhtml url(\"https://www.w3.org/1999/xhtml/\")"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseSimpleNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
			"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.selectors.size());
		assertEquals("p", handler.selectors.getFirst().toString());
		assertEquals("svg|svg", handler.selectors.get(1).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.getFirst());
		assertEquals("margin-left", handler.propertyNames.get(1));
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("blue", lu.getStringValue());
		lu = handler.lexicalValues.getLast();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_POINT, lu.getLexicalUnitType());
		assertEquals(5, lu.getFloatValue(), 0.01f);
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseMinimalStyleSheet() throws CSSException, IOException {
		InputSource source = new InputSource(DOMCSSStyleSheetFactoryTest.loadSampleUserCSSReader());
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheet1() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = loadTestCSSReader("sheet1.css");
		InputSource source = new InputSource(re);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.atRules.size());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } li {display: list-item; }}",
				handler.atRules.getFirst());
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("print", handler.mediaRuleLists.get(0).toString());
		assertEquals("screen", handler.mediaRuleLists.get(1).toString());
		assertEquals(1, handler.importMedias.size());
		assertEquals("tv,screen and (orientation:landscape)", handler.importMedias.get(0).toString());
		assertEquals(1, handler.importURIs.size());
		assertEquals("tv.css", handler.importURIs.get(0));
		assertEquals(5, handler.comments.size());
		assertEquals(" Comment before li ", handler.comments.get(0));
		assertEquals(" Comment before frame ", handler.comments.get(1));
		assertEquals(" Comment before frameset ", handler.comments.get(2));
		assertEquals(" Comment before noframes ", handler.comments.get(3));
		assertEquals(19, handler.propertyNames.size());
		assertEquals("font-weight", handler.propertyNames.getFirst());
		assertEquals("float", handler.propertyNames.get(16));
		assertEquals("font-size", handler.propertyNames.get(17));
		assertEquals("border", handler.propertyNames.getLast());
		assertEquals(19, handler.lexicalValues.size());
		assertEquals("bold", handler.lexicalValues.getFirst().toString());
		assertEquals("left", handler.lexicalValues.get(16).toString());
		assertEquals("12pt", handler.lexicalValues.get(17).toString());
		assertEquals("solid orange", handler.lexicalValues.getLast().toString());
		assertEquals(19, handler.priorities.size());
		String prio = handler.priorities.get(10);
		assertNotNull(prio);
		assertEquals("important", prio);
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetBadMedia() throws CSSException, IOException {
		Reader re = loadTestCSSReader("badmedia.css");
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.atRules.size());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } li {display: list-item; }}",
				handler.atRules.getFirst());
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(1).toString());
		assertEquals(1, handler.importMedias.size());
		assertEquals("tv,screen and (orientation:landscape)", handler.importMedias.get(0).toString());
		assertEquals(1, handler.importURIs.size());
		assertEquals("tv.css", handler.importURIs.get(0));
		assertEquals(4, handler.comments.size());
		assertEquals(" Comment before li ", handler.comments.get(0));
		assertEquals(" Comment before frame ", handler.comments.get(1));
		assertEquals(" Comment before frameset ", handler.comments.get(2));
		assertEquals(" Comment before noframes ", handler.comments.get(3));
		assertEquals(19, handler.propertyNames.size());
		assertEquals("font-weight", handler.propertyNames.get(1));
		assertEquals("float", handler.propertyNames.get(16));
		assertEquals("font-size", handler.propertyNames.get(17));
		assertEquals("border", handler.propertyNames.getLast());
		assertEquals(19, handler.lexicalValues.size());
		assertEquals("bold", handler.lexicalValues.get(1).toString());
		assertEquals("left", handler.lexicalValues.get(16).toString());
		assertEquals("12pt", handler.lexicalValues.get(17).toString());
		assertEquals("solid orange", handler.lexicalValues.getLast().toString());
		assertEquals(19, handler.priorities.size());
		String prio = handler.priorities.get(11);
		assertNotNull(prio);
		assertEquals("important", prio);
		assertTrue(errorHandler.hasError());
		assertEquals(13, errorHandler.exception.getLineNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@media {div.foo{margin:1em}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.endMediaCount);
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
	public void testParseStyleSheetMediaRuleErrorRecovery() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }@media {div.foo{margin:1em}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("handheld,only screen and (max-width:1600px) .foo", handler.mediaRuleLists.get(0).toString());
		assertEquals("all", handler.mediaRuleLists.get(1).toString());
		assertEquals(2, handler.endMediaCount);
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(64, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetNestedMediaRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(2, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.get(0).toString());
		assertEquals("(max-width:1600px)", handler.mediaRuleLists.get(1).toString());
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
		assertEquals(2, handler.endMediaCount);
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetNestedMediaFontFaceRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen{@font-face{font-family:\"foo-family\";src:url(\"fonts/foo-file.svg#bar-icons\") format('svg')}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
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
		assertEquals(1, handler.endMediaCount);
		assertEquals(1, handler.endFontFaceCount);
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRuleIEHack() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@media screen\\0 {.foo{margin:1em}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen\\0", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.endMediaCount);
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
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
		assertEquals(1, handler.comments.size());
		assertEquals(108, handler.propertyNames.size());
		assertEquals("font-family", handler.propertyNames.getFirst());
		assertEquals("display", handler.propertyNames.getLast());
		assertEquals(108, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("Verdana", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals("Arial", lu.getStringValue());
		lu = lu.getNextLexicalUnit();
		assertEquals(LexicalUnit.SAC_OPERATOR_COMMA, lu.getLexicalUnitType());
		lu = lu.getNextLexicalUnit();
		assertEquals("Helvetica", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("block", handler.lexicalValues.getLast().toString());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetRemote() throws CSSException, IOException {
		if (TestConfig.REMOTE_TESTS) {
			TestDocumentHandler handler = new TestDocumentHandler();
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
	public void testParseStyleSheetRemoteInputSource() throws CSSException, IOException {
		if (TestConfig.REMOTE_TESTS) {
			TestDocumentHandler handler = new TestDocumentHandler();
			parser.setDocumentHandler(handler);
			TestErrorHandler errorHandler = new TestErrorHandler();
			parser.setErrorHandler(errorHandler);
			InputSource source = new InputSource();
			source.setURI("https://raw.githubusercontent.com/necolas/normalize.css/master/normalize.css");
			parser.parseStyleSheet(source);
			assertTrue(handler.comments.size() != 0);
			assertTrue(handler.selectors.size() != 0);
			assertTrue(handler.propertyNames.size() != 0);
			assertFalse(errorHandler.hasError());
			handler.checkRuleEndings();
		}
	}

	@Test
	public void testParseStyleSheetFontFaceRule() throws CSSException, IOException {
		InputSource source = new InputSource(loadCSSfromClasspath("/io/sf/carte/doc/agent/common.css"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(3, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals("font-family", handler.propertyNames.get(1));
		assertEquals("src", handler.propertyNames.get(2));
		assertEquals(3, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals("'Mechanical Bold'", handler.lexicalValues.get(1).toString());
		assertEquals("url('font/MechanicalBd.otf')", handler.lexicalValues.get(2).toString());
		assertEquals(3, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(1, handler.fontFaceCount);
		assertEquals(1, handler.atRules.size());
		assertEquals("@font-feature-values Foo Sans, Bar { @styleset { my-style: 2; } }",
				handler.atRules.getFirst());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetFontFaceRuleWrongChar() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"\ufeff@font-face{font-family:'FooSans';src:url(font/FooSans.woff2) format('woff2')}@import 'foo.css';"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(0, handler.fontFaceCount);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule() throws CSSException, IOException {
		Reader re = loadTestCSSReader("page.css");
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
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
		assertEquals(5, handler.pageRuleNames.size());
		assertNull(handler.pageRuleNames.getFirst());
		assertEquals("foo", handler.pageRuleNames.get(1));
		assertEquals("top-center", handler.pageRuleNames.get(2));
		assertEquals("bottom-center", handler.pageRuleNames.get(3));
		assertEquals("bar", handler.pageRuleNames.get(4));
		assertEquals(5, handler.pseudoPages.size());
		assertEquals(":first", handler.pseudoPages.get(0));
		assertEquals(":left", handler.pseudoPages.get(1));
		assertEquals(":right,:blank", handler.pseudoPages.get(4));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRule2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@page :first{margin-top:20%;@top-left{content:'foo';color:blue}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(2, handler.pageRuleNames.size());
		assertNull(handler.pageRuleNames.getFirst());
		assertEquals("top-left", handler.pageRuleNames.get(1));
		assertEquals(":first", handler.pseudoPages.getFirst());
		assertEquals(2, handler.endPageCount);
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
		assertEquals("startPage", handler.eventSeq.get(2));
		assertEquals("property", handler.eventSeq.get(3));
		assertEquals("property", handler.eventSeq.get(4));
		assertEquals("endPage", handler.eventSeq.get(5));
		assertEquals("endPage", handler.eventSeq.get(6));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@page :first{margin-top:20%;myname@top-left{content:'foo';color:blue}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.pageRuleNames.size());
		assertNull(handler.pageRuleNames.getFirst());
		assertEquals(":first", handler.pseudoPages.getFirst());
		assertEquals(1, handler.endPageCount);
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
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(35, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@page :first{margin-top:20%;myname @top-left{content:'foo';color:blue}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.pageRuleNames.size());
		assertNull(handler.pageRuleNames.getFirst());
		assertEquals(":first", handler.pseudoPages.getFirst());
		assertEquals(1, handler.endPageCount);
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
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(36, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleBad3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@page :first{margin-top:20%; top-left{content:'foo';color:blue}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.pageRuleNames.size());
		assertNull(handler.pageRuleNames.getFirst());
		assertEquals(":first", handler.pseudoPages.getFirst());
		assertEquals(1, handler.endPageCount);
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
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(38, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleNestedOnMediaRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media print {@page {margin-top: 20%;}h3 {width: 80%}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.pageRuleNames.size());
		assertNull(handler.pageRuleNames.getFirst());
		assertEquals(1, handler.endPageCount);
		assertEquals(1, handler.mediaRuleLists.size());
		SACMediaList medialist = handler.mediaRuleLists.getFirst();
		assertEquals(1, medialist.getLength());
		assertEquals("print", medialist.item(0));
		assertEquals(1, handler.endMediaCount);
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetOtherRules() throws CSSException, IOException {
		Reader re = loadTestCSSReader("other_rules.css");
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("red", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertEquals(5, handler.atRules.size());
		assertEquals("@viewport { width: device-width; }",
				handler.atRules.get(0));
		assertEquals("@keyframes slide-right { " + 
				"from {margin-left: 0px;} " + 
				"50% {margin-left: 110px; opacity: 1;} " + 
				"50% {opacity: 0.9;} " + 
				"to {margin-left: 200px;} }", handler.atRules.get(1));
		assertEquals("@keyframes important1 { from { margin-top: 50px; }" + 
				" 50% { margin-top: 150px !important; } /* ignored */" + 
				"to { margin-top: 100px; } }", handler.atRules.get(2));
		assertEquals("@counter-style foo { symbols: \\1F44D; suffix: \" \"; }",
				handler.atRules.get(3));
		assertEquals(
				"@font-feature-values Some Font { @swash { swishy: 1; flowing: 2; } @styleset { double-W: 14; sharp-terminals: 16 1; } }",
				handler.atRules.get(4));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseCharsetRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCharsetRule2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/* My sheet */\n@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.comments.size());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCharsetRuleBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@import url('foo.css');@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(24, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"p{display:block;}@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).item(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals(1, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(18, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad3() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("@media print {@page {margin-top: 20%;}h3 {width: 80%}}@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.pageRuleNames.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(55, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf')}@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(75, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseCharsetRuleBad5() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader("@font-feature-values Foo Sans, Bar {@styleset {my-style: 2;}}@charset \"utf-8\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.atRules.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(62, errorHandler.exception.getColumnNumber());
	}

	/*
	 * Make sure that parsing recovers from bad charset rule error.
	 * 
	 */
	@Test
	public void testParseCharsetRuleBadRecovery() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"p{display:block;}@charset \"utf-8\";span {color: blue;}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.atRules.size());
		assertEquals(2, handler.selectors.size());
		assertEquals("p", handler.selectors.get(0).item(0).toString());
		assertEquals("span", handler.selectors.get(1).item(0).toString());
		assertEquals(2, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.get(0));
		assertEquals("color", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(18, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseImportRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@import url('foo.css');"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRule2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url(foo.css);"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMedia() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url(foo.css) print;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));
		assertFalse(errorHandler.hasError());
	}
	@Test
	public void testParseImportRuleMedia2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url(foo.css) screen, tv;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQuery() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url('foo.css') (orientation:landscape);"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("(orientation:landscape)", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQuery2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url('foo.css') screen and (orientation:landscape);"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (orientation:landscape)", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleMediaQueryBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url('foo.css') screen and ((orientation:landscape);"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.importURIs.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(59, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseImportRuleNoUrl() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import 'foo.css';"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlMedia() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import 'foo.css' print;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlDQ() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import \"foo.css\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrlDQMedia() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import \"foo.css\" screen, tv;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"foo@import url('bar.css');"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(1, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseEmptyAtRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(2, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseCounterStyleRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@counter-style foo {symbols: \\1F44D;\n suffix: \" \";\n}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.atRules.size());
		assertEquals("@counter-style foo {symbols: \\1F44D; suffix: \" \"; }",
				handler.atRules.get(0));
	}

	@Test
	public void testParseNestedSupportsRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.atRules.size());
		assertEquals("@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}",
				handler.atRules.get(0));
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.getFirst().toString());
		assertEquals(1, handler.endMediaCount);
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePseudoClassNotEmpty() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"foo:not() {td {display: table-cell; } li {display: list-item; }}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.selectors.size());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(9, errorHandler.exception.getColumnNumber());
	}

	@Test
	public void testParseStyleSheetDefaultUserAgentSheet() throws CSSException, IOException {
		InputSource source = new InputSource(loadCSSfromClasspath("/io/sf/carte/doc/style/css/html.css"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(16, handler.comments.size());
		assertEquals("textarea", handler.selectors.get(73).toString());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(76).toString());
		assertEquals(111, handler.selectors.size());
		assertEquals(150, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("page-break-before", handler.propertyNames.getLast());
		assertEquals(150, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("avoid", handler.lexicalValues.getLast().toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetDefaultUserAgentSheetQuirks() throws CSSException, IOException {
		InputSource source = new InputSource(loadCSSfromClasspath("/io/sf/carte/doc/style/css/html-quirks.css"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(18, handler.comments.size());
		assertEquals("textarea", handler.selectors.get(74).toString());
		assertEquals("hr[align=\"left\"]", handler.selectors.get(78).toString());
		assertEquals(123, handler.selectors.size());
		assertEquals(168, handler.propertyNames.size());
		assertEquals("display", handler.propertyNames.getFirst());
		assertEquals("page-break-before", handler.propertyNames.getLast());
		assertEquals(168, handler.lexicalValues.size());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("block", lu.getStringValue());
		assertNull(lu.getNextLexicalUnit());
		assertEquals("avoid", handler.lexicalValues.getLast().toString());
		assertFalse(errorHandler.hasError());
		assertFalse(errorHandler.hasWarning());
	}

	@Test
	public void testParseStyleSheetUnexpectedChar() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/** Comment 1 **/\\n@media {div.foo{margin:1em}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.endMediaCount);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(19, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/** Comment 1 **/;@media {div.foo{margin:1em}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("all", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.endMediaCount);
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(18, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedCharHigh() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"/** Comment 1 **/\u7ff0@media {div.foo{margin:1em}}@import 'foo.css';"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.endMediaCount);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/** Comment 1 **/:first-child(){margin:1em}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.endMediaCount);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(30, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/** Comment 1 **/:first-child(foo){margin:1em}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.endMediaCount);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(30, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/** Comment 1 **/::first-line(){margin:1em}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.endMediaCount);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(30, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedChar6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("/** Comment 1 **/:first-line(){margin:1em}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.mediaRuleLists.size());
		assertEquals(0, handler.endMediaCount);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(29, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF1() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF1.css");
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
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
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("print", handler.mediaRuleLists.get(0).toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("font-size", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("10pt", handler.lexicalValues.get(0).toString());
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOF3() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOF3.css");
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
		assertEquals(1, handler.atRules.size());
		assertEquals(
				"@supports (display: table-cell) and (display: list-item) {td {display: table-cell; } li {display: list-item; }}",
				handler.atRules.getFirst());
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("svg", handler.namespaceMaps.keySet().iterator().next());
		assertEquals("http://www.w3.org/2000/svg", handler.namespaceMaps.get("svg"));
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseStyleSheetUnexpectedEOL1() throws CSSException, IOException {
		Reader re = loadTestCSSReader("unexpectedEOL1.css");
		InputSource source = new InputSource(re);
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
		source.getCharacterStream().close();
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
		assertNotNull(errorHandler.exception);
		assertEquals(3, errorHandler.exception.getLineNumber());
		assertEquals(25, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetUnexpectedEOLNL() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"body {\nbackground-color: red;\nfont-family: 'Times New\ncolor: blue;:;\nborder: none}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseStyleSheet(source);
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
		assertNotNull(errorHandler.exception);
		assertEquals(4, errorHandler.exception.getLineNumber());
		assertEquals(13, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		Reader re = loadTestCSSReader("comments.css");
		InputSource source = new InputSource(re);
		parser.parseStyleSheet(source);
		re.close();
		assertEquals(1, handler.selectors.size());
		assertEquals("body", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("background-color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());
		assertEquals(4, handler.comments.size());
		assertEquals(" pre-rule-1 ", handler.comments.get(0));
		assertEquals(" pre-rule-2 ", handler.comments.get(1));
		assertEquals(" pre-style-decl 1 ", handler.comments.get(2));
		assertEquals(" post-style-decl 1 ", handler.comments.get(3));
		assertEquals(1, handler.atRules.size());
		assertEquals("@viewport /* skip-vw 1 */{/* pre-viewport-decl */ width: /* skip-vw 2 */device-width; /* post-viewport-decl */}",
				handler.atRules.get(0));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"<!--/*--><![CDATA[/*><!--*/body{padding-top:2px}.foo {color:red}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());
		// The comments found do not apply to a valid rule
		assertEquals(0, handler.comments.size());
		assertTrue(errorHandler.hasError());
		assertEquals(10, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetComments3() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"<!--/*--><!/*><!--*/body{padding-top:2px}.foo {color:red}"));
		parser.parseStyleSheet(source);
		assertEquals(1, handler.selectors.size());
		assertEquals(".foo", handler.selectors.getFirst().toString());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("color", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertNotNull(lu);
		assertEquals(LexicalUnit.SAC_IDENT, lu.getLexicalUnitType());
		assertEquals("red", lu.getStringValue());
		// The comments found do not apply to a valid rule
		assertEquals(0, handler.comments.size());
		assertTrue(errorHandler.hasError());
		assertEquals(10, errorHandler.exception.getColumnNumber());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetAsteriskHack() throws CSSException, IOException {
		TestDeclarationHandler handler = new TestDeclarationHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errorHandler = new TestErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(".foo{*width: 80%}"));
		parser.parseStyleSheet(source);
		assertTrue(errorHandler.hasError());
		assertEquals(1, errorHandler.exception.getLineNumber());
		assertEquals(6, errorHandler.exception.getColumnNumber());
		errorHandler.reset();
		parser.setFlag(CSSParser.Flag.STARHACK);
		source = new InputSource(new StringReader(".foo{*width: 80%}"));
		parser.parseStyleSheet(source);
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(80, lu.getFloatValue(), 0.01);
	}

	private static Reader loadTestCSSReader(String filename) {
		return loadCSSfromClasspath("/io/sf/carte/doc/style/css/parser/" + filename);
	}

	private static Reader loadCSSfromClasspath(final String filename) {
		InputStream is = java.security.AccessController
			.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
				@Override
				public InputStream run() {
					return this.getClass().getResourceAsStream(filename);
				}
			});
		Reader re = null;
		if(is != null) {
			try {
				re = new InputStreamReader(is, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// Should not happen, but...
				re = new InputStreamReader(is);
			}
		}
		return re;
	}
}
