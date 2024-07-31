/*

 Copyright (c) 2005-2024, Carlos Amengual.

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
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class RuleParserTest {

	CSSParser parser;
	TestCSSHandler handler;
	TestRuleErrorHandler errorHandler;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
		handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		errorHandler = new TestRuleErrorHandler();
		parser.setErrorHandler(errorHandler);
	}

	@Test
	public void testParseRule() throws CSSException, IOException {
		parseRule("p.myclass,:first-child{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
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
		parseRule("hr[align=\"left\"]    {margin-left : 0 ;margin-right : auto;}");
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRule3() throws CSSException, IOException {
		parseRule("input:not(){}body:not(.foo)[id*=substring] .header {margin-left : 0 ;margin-right : auto;}");
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
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRuleSelectorError() throws CSSException, IOException {
		parseRule("!,p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRuleNS() throws CSSException, IOException {
		TestNamespaceMap nsmap = new TestNamespaceMap();
		nsmap.put("svg", TestConfig.SVG_NAMESPACE_URI);
		parseRule("svg|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}", nsmap);
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
		parseRule("foo|p{font-family: Times New Roman; color: yellow; width: calc(100% - 3em);}");
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.propertyNames.size());
		assertEquals(0, handler.lexicalValues.size());
		assertEquals(0, handler.priorities.size());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRuleDuplicateSelector() throws CSSException, IOException {
		parseRule("p, p {width: 80%}");
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
		assertTrue(errorHandler.hasWarning());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRuleCommentWDoubleStar() throws CSSException, IOException {
		// An asterisk before the '*/' may confuse the parser
		parseRule(".foo {\n/**just a comment**/margin-left:auto}");
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseRuleCommentWDoubleStar2() throws CSSException, IOException {
		parser.setErrorHandler(errorHandler);
		// An asterisk before the '*/' may confuse the parser
		parseRule(".foo {  /**just a comment**/margin-left:auto}");
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseDefaultNS() throws CSSException, IOException {
		parseRule("@namespace url(\"https://www.w3.org/1999/xhtml/\");");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseDefaultNSEOF() throws CSSException, IOException {
		parseRule("@namespace url(\"https://www.w3.org/1999/xhtml/\")");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseDefaultNSBad() throws CSSException, IOException {
		parseRule("@namespace url(;");
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseDefaultNSBad2() throws CSSException, IOException {
		parseRule("@namespace url();");
		assertEquals(0, handler.namespaceMaps.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseDefaultNSDQ() throws CSSException, IOException {
		parseRule("@namespace \"\" url(\"https://www.w3.org/1999/xhtml/\");");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseDefaultNSNoURL() throws CSSException, IOException {
		parseRule("@namespace \"https://www.w3.org/1999/xhtml/\";");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get(""));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseNSNoURL() throws CSSException, IOException {
		parseRule("@namespace xhtml \"https://www.w3.org/1999/xhtml/\";");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseNSNoURLEOF() throws CSSException, IOException {
		parseRule("@namespace xhtml \"https://www.w3.org/1999/xhtml/\"");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseNSEOF() throws CSSException, IOException {
		parseRule("@namespace xhtml url(\"https://www.w3.org/1999/xhtml/\")");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals("https://www.w3.org/1999/xhtml/", handler.namespaceMaps.get("xhtml"));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseSimpleNS() throws CSSException, IOException {
		parseRule("@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		assertEquals(1, handler.namespaceMaps.size());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, handler.namespaceMaps.get("svg"));
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
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetMediaRule() throws CSSException, IOException {
		parseRule("@media {div.foo{margin:1em}}");
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
		parseRule(
				"@media handheld,only screen and (max-width:1600px) .foo{bottom: 20px!important; }@media {div.foo{margin:1em}}");
		assertEquals(2, handler.mediaRuleLists.size());
		MediaQueryList mql = handler.mediaRuleLists.get(0);
		assertTrue(mql.hasErrors());
		assertEquals("handheld", mql.getMedia());
		assertEquals("all", handler.mediaRuleLists.get(1).getMedia());
		assertEquals(2, handler.endMediaCount);
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("margin", handler.propertyNames.get(0));
		assertEquals(1, handler.lexicalValues.size());
		assertEquals("1em", handler.lexicalValues.get(0).getCssText());
		assertEquals(1, handler.priorities.size());
		assertNull(handler.priorities.get(0));
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetNestedMediaRule() throws CSSException, IOException {
		parseRule("@media screen {.foo{bottom: 20px!important; }@media (max-width:1600px){div.foo{margin:1em}}}");
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
		assertEquals(2, handler.endMediaCount);
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetPageRuleNestedOnMediaRule() throws CSSException, IOException {
		parseRule("@media print {@page {margin-top: 20%;}h3 {width: 80%}}");
		assertEquals(1, handler.pageRuleSelectors.size());
		assertNull(handler.pageRuleSelectors.getFirst());
		assertEquals(1, handler.endPageCount);
		assertEquals(1, handler.mediaRuleLists.size());
		MediaQueryList medialist = handler.mediaRuleLists.getFirst();
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
		parseRule("@import url('foo.css');");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRule2() throws CSSException, IOException {
		parseRule("@import url(foo.css);");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleMedia() throws CSSException, IOException {
		parseRule("@import url(foo.css) print;");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleMedia2() throws CSSException, IOException {
		parseRule("@import url(foo.css) screen, tv;");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleMediaQuery() throws CSSException, IOException {
		parseRule("@import url('foo.css') (orientation:landscape);");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("(orientation: landscape)", list.item(0));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleMediaQuery2() throws CSSException, IOException {
		parseRule("@import url('foo.css') screen and (orientation:landscape);");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("screen and (orientation: landscape)", list.item(0));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleMediaQueryBad() throws CSSException, IOException {
		parseRule("@import url('foo.css') screen and ((orientation:landscape);");
		assertEquals(0, handler.importURIs.size());
		assertTrue(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleNoUrl() throws CSSException, IOException {
		parseRule("@import 'foo.css';");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleNoUrlMedia() throws CSSException, IOException {
		parseRule("@import 'foo.css' print;");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(1, list.getLength());
		assertEquals("print", list.item(0));
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.streamEndcount);
	}

	@Test
	public void testParseImportRuleNoUrlDQ() throws CSSException, IOException {
		parseRule("@import \"foo.css\";");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseImportRuleNoUrlDQMedia() throws CSSException, IOException {
		parseRule("@import \"foo.css\" screen, tv;");
		assertEquals(1, handler.importURIs.size());
		assertEquals("foo.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(2, list.getLength());
		assertEquals("screen", list.item(0));
		assertEquals("tv", list.item(1));
		assertFalse(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseImportRuleBad() throws CSSException, IOException {
		parseRule("foo@import url('bar.css');");
		assertEquals(0, handler.importURIs.size());
		assertEquals(0, handler.importMedias.size());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseImportRuleErrorRecovery() throws CSSException, IOException {
		parseRule(";@import url('bar.css');");
		assertEquals(1, handler.importURIs.size());
		assertEquals("bar.css", handler.importURIs.get(0));
		assertEquals(1, handler.importMedias.size());
		MediaQueryList list = handler.importMedias.get(0);
		assertEquals(0, list.getLength());
		assertTrue(list.isAllMedia());
		assertEquals("all", list.getMedia());
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.endSelectors.size());
		assertTrue(errorHandler.hasError());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseCounterStyleRule() throws CSSException, IOException {
		parseRule("@counter-style foo {symbols: \\1F44D;\n suffix: \" \";\n}/*end comment*/");
		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		assertEquals("suffix", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\\1f44d ", handler.lexicalValues.get(0).toString());
		assertEquals("\" \"", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.comments.size());
		assertEquals(1, handler.endCounterStyleCount);
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseCounterStyleRuleError() throws CSSException, IOException {
		parseRule("@counter-style foo {symbols: \\1F44D;{foo} :bar;suffix: \" \";\n}");
		assertEquals(1, handler.counterStyleNames.size());
		assertEquals("foo", handler.counterStyleNames.get(0));
		assertEquals(2, handler.propertyNames.size());
		assertEquals("symbols", handler.propertyNames.get(0));
		assertEquals("suffix", handler.propertyNames.get(1));
		assertEquals(2, handler.lexicalValues.size());
		assertEquals("\\1f44d ", handler.lexicalValues.get(0).toString());
		assertEquals("\" \"", handler.lexicalValues.get(1).toString());
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.endCounterStyleCount);
		handler.checkRuleEndings();
		assertTrue(errorHandler.hasError());
		assertEquals(37, errorHandler.getLastException().getColumnNumber());
	}

	@Test
	public void testParseNestedSupportsRule() throws CSSException, IOException {
		parseRule(
				"@media screen {@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}}");
		assertEquals(0, handler.atRules.size());
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: flexbox) and (not (display: inline-grid))",
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
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.getFirst().toString());
		assertEquals(1, handler.endMediaCount);
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParseNestedSupportsAndCustomRule() throws CSSException, IOException {
		parseRule(
				"@media screen {@supports (display: flexbox) and (not (display: inline-grid)) {td {display: table-cell; } li {display: list-item; }}@-webkit-keyframes foo { from { background-position: 40px 0; } to { background-position: 0 0; } }}");
		assertEquals(1, handler.atRules.size());
		assertEquals(
				"@-webkit-keyframes foo { from { background-position: 40px 0; } to { background-position: 0 0; } }",
				handler.atRules.getFirst());
		assertEquals(1, handler.supportsRuleLists.size());
		assertEquals("(display: flexbox) and (not (display: inline-grid))",
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
		assertEquals(1, handler.mediaRuleLists.size());
		assertEquals("screen", handler.mediaRuleLists.getFirst().toString());
		assertEquals(1, handler.endMediaCount);
		handler.checkRuleEndings();
		assertFalse(errorHandler.hasError());
	}

	@Test
	public void testParsePseudoClassNotEmpty() throws CSSException, IOException {
		parseRule("foo:not() {td {display: table-cell; } li {display: list-item; }}");
		assertTrue(errorHandler.hasError());
		assertEquals(0, handler.selectors.size());
		handler.checkRuleEndings();
	}

	@Test
	public void testParseStyleSheetAsteriskHack() throws CSSException, IOException {
		parseRule(".foo{*width: 80%}");
		assertTrue(errorHandler.hasError());
		errorHandler.reset();
		parser.setFlag(CSSParser.Flag.STARHACK);
		parseRule(".foo{*width: 80%}");
		assertFalse(errorHandler.hasError());
		assertEquals(1, handler.propertyNames.size());
		assertEquals("*width", handler.propertyNames.getFirst());
		LexicalUnit lu = handler.lexicalValues.getFirst();
		assertEquals(LexicalType.PERCENTAGE, lu.getLexicalUnitType());
		assertEquals(80f, lu.getFloatValue(), 0.01f);
		handler.checkRuleEndings();
	}

	private void parseRule(String string) throws CSSParseException {
		try {
			parser.parseRule(new StringReader(string));
		} catch (IOException e) {
		}
	}

	private void parseRule(String string, NamespaceMap nsmap) throws CSSParseException {
		try {
			parser.parseRule(new StringReader(string), nsmap);
		} catch (IOException e) {
		}
	}

	class TestRuleErrorHandler extends TestErrorHandler {

		@Override
		public void error(CSSParseException exception) throws CSSException {
			/*
			 * Report if more than one exception is reported for a rule.
			 * Errors related to media queries can legitimately appear several times,
			 * so they are excluded.
			 */
			if (getLastException() != null && handler.mediaRuleLists.isEmpty()) {
				throw new IllegalStateException("More than one error reported for single rule", exception);
			}
			super.error(exception);
		}

	}

}
