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
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

public class RuleParserTest {

	static CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseRule() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader("p.myclass,:first-child{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseRule(source);
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
	public void testParseRule2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"hr[align=\"left\"]    {margin-left : 0 ;margin-right : auto;}"));
		parser.parseRule(source);
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
	public void testParseRule3() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"input:not(){}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}"));
		parser.parseRule(source);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRuleSelectorError() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"!,p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseRule(source);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseRuleNS() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"svg|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		TestNamespaceMap nsmap = new TestNamespaceMap();
		nsmap.put("svg", "http://www.w3.org/2000/svg");
		parser.parseRule(source, nsmap);
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
	public void testParseRuleSelectorErrorBadNSPrefix() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(
				"foo|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}"));
		parser.parseRule(source);
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseRuleDuplicateSelector() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader("p, p {width: 80%}"));
		parser.parseRule(source);
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
	public void testParseRuleCommentWDoubleStar() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		InputSource source = new InputSource(new StringReader(
				".foo {\n/**just a comment**/margin-left:auto}"));
		parser.parseRule(source);
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
	public void testParseRuleCommentWDoubleStar2() throws CSSException, IOException {
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		InputSource source = new InputSource(new StringReader(
				".foo {  /**just a comment**/margin-left:auto}"));
		parser.parseRule(source);
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
	public void testParseDefaultNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\");"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSEOF() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url(\"https://www.w3.org/1999/xhtml/\")"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url(;"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSBad2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace url();"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSDQ() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace \"\" url(\"https://www.w3.org/1999/xhtml/\");"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseDefaultNSNoURL() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace \"https://www.w3.org/1999/xhtml/\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURL() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\";"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSNoURLEOF() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace xhtml \"https://www.w3.org/1999/xhtml/\""));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNSEOF() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@namespace xhtml url(\"https://www.w3.org/1999/xhtml/\")"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
	public void testParseStyleSheetMediaRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("@media {div.foo{margin:1em}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetNestedMediaRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
	public void testParseStyleSheetPageRuleNestedOnMediaRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media print {@page {margin-top: 20%;}h3 {width: 80%}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
	public void testParseImportRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import url('foo.css');"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(0, handler.importURIs.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleNoUrl() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@import 'foo.css';"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseImportRuleErrorRecovery() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				";@import url('bar.css');"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.importURIs.size());
		assertEquals("bar.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		SACMediaList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("all", list.item(0));
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
	}

	@Test
	public void testParseCounterStyleRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@counter-style foo {symbols: \\1F44D;\n suffix: \" \";\n}/*end comment*/"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertEquals(1, handler.atRules.size());
		assertEquals("@counter-style foo {symbols: \\1F44D; suffix: \" \"; }",
				handler.atRules.get(0));
		assertEquals(0, handler.comments.size());
	}

	@Test
	public void testParseNestedSupportsRule() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"@media screen {@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}}"));
		TestDocumentHandler handler = new TestDocumentHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
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
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		parser.parseRule(source);
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.selectors.size());
	}

	@Test
	public void testParseStyleSheetAsteriskHack() throws CSSException, IOException {
		TestDeclarationHandler handler = new TestDeclarationHandler();
		parser.setDocumentHandler(handler);
		TestRuleErrorHandler errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
		InputSource source = new InputSource(new StringReader(".foo{*width: 80%}"));
		parser.parseRule(source);
		assertTrue(errorHandler.hasError());
		errorHandler.reset();
		parser.setFlag(CSSParser.Flag.STARHACK);
		source = new InputSource(new StringReader(".foo{*width: 80%}"));
		parser.parseRule(source);
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalUnit.SAC_PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(80, lu.getFloatValue(), 0.01);
	}

	static class TestRuleErrorHandler extends TestErrorHandler {

		@Override
		public void error(CSSParseException exception) throws CSSException {
			if (this.exception != null) {
				throw new IllegalStateException("More than one error reported for single rule", exception);
			}
			super.error(exception);
		}

		@Override
		public void fatalError(CSSParseException exception) throws CSSException {
			if (this.exception != null) {
				throw new IllegalStateException("More than one fatal error reported for single rule", exception);
			}
			super.fatalError(exception);
		}

	}

}
