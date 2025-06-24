/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DocumentType;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.HTMLDocument;
import io.sf.carte.doc.dom.TestDOMImplementation;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory.DummyCanvas;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class SelectorMatcherTest {

	public static TestCSSStyleSheetFactory factory;

	private CSSParser cssParser;

	private DOMDocument doc;

	private TestDOMImplementation domImpl;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		factory = new TestCSSStyleSheetFactory();
	}

	@BeforeEach
	public void setUp() {
		this.cssParser = new io.sf.carte.doc.style.css.parser.CSSParser();
		domImpl = new TestDOMImplementation(false);
		setUpWithMode(CSSDocument.ComplianceMode.QUIRKS);
	}

	private void setUpWithMode(CSSDocument.ComplianceMode mode) {
		doc = createDocumentWithMode(mode);
		doc.setDocumentURI("http://www.example.com/mydoc.html#mytarget");
	}

	private DOMDocument createDocumentWithMode(CSSDocument.ComplianceMode mode) {
		DocumentType doctype = null;
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			doctype = domImpl.createDocumentType("html", null, null);
		}
		return domImpl.createDocument(null, "html", doctype);
	}

	@Test
	public void testMatchSelectorUniversal() throws Exception {
		BaseCSSStyleSheet css = parseStyle("* {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("*", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector1Element() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);

		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		assertFalse(elm.matches(selist, null));
	}

	@Test
	public void testMatchSelector1ElementUppercase() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("P");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);

		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		assertFalse(elm.matches(selist, null));
	}

	@Test
	public void testMatchSelector1ElementPrefix() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p", selectorListToString(selist, rule));
		CSSElement elm = doc.createElementNS("http://www.example.com/examplens", "pre:p");
		doc.getDocumentElement().appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);

		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		assertFalse(elm.matches(selist, null));
	}

	@Test
	public void testMatchSelector1ElementNS() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();
		SelectorList svgselist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		CSSElement svg = doc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		SelectorMatcher svgmatcher = selectorMatcher(svg);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);

		assertTrue(svgmatcher.matches(selist) == -1);
		assertTrue(matcher.matches(svgselist) == -1);
		selidx = svgmatcher.matches(svgselist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), svgmatcher);
	}

	@Test
	public void testMatchSelector1ElementNoNS() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"@namespace url('https://www.w3.org/1999/xhtml/'); p{color: blue;} |div{margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();

		DOMDocument xdoc = domImpl.createDocument("", null, null);
		assertFalse(xdoc instanceof HTMLDocument);

		CSSElement root = xdoc.createElement("html");
		xdoc.appendChild(root);

		CSSElement p = xdoc.createElement("p");
		p.setAttribute("id", "p1");
		root.appendChild(p);
		CSSElement pns = xdoc.createElementNS("https://www.w3.org/1999/xhtml/", "p");
		pns.setAttribute("id", "p2");
		root.appendChild(pns);
		CSSElement div = xdoc.createElement("div");
		div.setAttribute("id", "div1");
		root.appendChild(div);
		CSSElement divns = xdoc.createElementNS("https://www.w3.org/1999/xhtml/", "div");
		divns.setAttribute("id", "div2");
		root.appendChild(divns);

		assertFalse(p.matches(selist, null));
		assertTrue(pns.matches(selist, null));

		selist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		assertFalse(divns.matches(selist, null));
		assertTrue(div.matches(selist, null));
	}

	@Test
	public void testMatchSelector1Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("[title]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorUniversalAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("*[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("[title]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorTypeAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title=hi]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2AttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title='hi' i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "hi ho");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorOneOfAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title~=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title~=hi]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorOneOfAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title~='hi' i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "h");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "ho hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginHyphenAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[lang|=\"en\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[lang|=en]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("lang", "en-US");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("lang", "en");
		assertTrue(matcher.matches(selist) >= 0);

		elm.setAttribute("lang", "en_US");
		assertTrue(matcher.matches(selist) < 0);

		elm = createTopLevelElement("div");
		elm.setAttribute("lang", "en");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginHyphenAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[lang|=\"en\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[lang|='en' i]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("lang", "EN-US");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("lang", "EN");
		assertTrue(matcher.matches(selist) >= 0);

		elm.setAttribute("lang", "en_US");
		assertTrue(matcher.matches(selist) < 0);

		elm = createTopLevelElement("div");
		elm.setAttribute("lang", "en");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorEndsAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title$=\"hi ho\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title$='hi ho']", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "ho ho");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi ho ho");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginsAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title^=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title^=hi]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "hi");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginsAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title^=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title^='hi' i]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HI HO");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "h");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorEndsAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title$=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title$='hi' i]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "i");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorSubstringAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title*=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title*=hi]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorSubstringAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title*=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title*='hi' i]", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI HO");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "HI HO");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "HOHI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "H");
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLang() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(en) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(en)", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangString() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(de,'en-US') {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(de,en-US)", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangStringDQ() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(de, \"en-US\") {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(de,en-US)", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	/*
	 * Verify that a wrong selector serializes correctly
	 */
	@Test
	public void testMatchSelectorLangStringBadLang() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(de,'1-US') {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(de,'1-US')", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLang2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(en-US) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-US");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRange() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(\\*) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(\\*)", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRangeString() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang('*') {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang('*')", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRangeStringDQ() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(\"*\") {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(\"*\")", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRange2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(fr,\\*-Latn) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(fr,\\*-Latn)", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-DE");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRange3() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(fr-FR,de-\\*-DE) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(fr-FR,de-\\*-DE)", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-DE");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector1ClassStrict() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleClass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
		// STRICT
		setUpWithMode(CSSDocument.ComplianceMode.STRICT);
		css = parseStyle(".exampleclass {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleClass");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		matcher = selectorMatcher(elm);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorMultipleClass() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		// STRICT
		setUpWithMode(CSSDocument.ComplianceMode.STRICT);
		css = parseStyle(".exampleclass {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		matcher = selectorMatcher(elm);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector2Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle("z.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("z.exampleclass", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector3Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector4Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass span", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector5Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector6Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		CSSElement child2 = doc.createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector1MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass secondclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector2MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.secondclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass secondclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorTwoClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.secondclass.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass secondclass thirdclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorTwoClassesTwoPseudo() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"p.secondclass::before.firstclass::first-line {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass secondclass thirdclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		Condition pseudo = cssParser.parsePseudoElement("::before::first-line");
		matcher.setPseudoElement(pseudo);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 3, selist.item(selidx), matcher);

		// Fail the pseudo-element match
		pseudo = cssParser.parsePseudoElement("::first-line");
		matcher.setPseudoElement(pseudo);
		selidx = matcher.matches(selist);
		assertTrue(selidx == -1);
	}

	@Test
	public void testMatchSelector3MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", " firstclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector4MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass ");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector1Firstchild() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:first-child", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "parentid");
		CSSElement firstChild = doc.createElement("p");
		firstChild.setAttribute("id", "pid1");
		parent.appendChild(firstChild);
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "pid2");
		parent.appendChild(elm);

		SelectorMatcher matcher = selectorMatcher(firstChild);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector1Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector1IdStrict() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle("#exampleID {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleID", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("ID", "exampleid");
		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		// STRICT
		setUpWithMode(CSSDocument.ComplianceMode.STRICT);
		css = parseStyle("#exampleID {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("#exampleID", selectorListToString(selist, rule));
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleID");
		matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector2Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid span", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector2aId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector2bId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		CSSElement child2 = doc.createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector3Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid>span", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector3bId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorTypeId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p#exampleid", selectorListToString(selist, rule));
		CSSElement elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);

		elm = createTopLevelElement("div");
		elm.setAttribute("id", "exampleid");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorAdjacent() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass + p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass+p", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "childid1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		elm = doc.createElement("p");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorAdjacentNoMatch() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p+.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p+.exampleclass", selectorListToString(selist, rule));

		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(elm);
		elm = doc.createElement("div");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(-1, selidx);
		elm = doc.createElement("div");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		matcher = selectorMatcher(elm);
		selidx = matcher.matches(selist);
		assertEquals(-1, selidx);
	}

	@Test
	public void testMatchSelectorSubsequentSibling() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass ~ p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass~p", selectorListToString(selist, rule));

		rule = (StyleRule) parseStyle("p#childidp1.exampleclass ~ pre#idpre {color: blue;}")
				.getCssRules().item(0);
		SelectorList selist2 = rule.getSelectorList();

		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		parent.appendChild(doc.createElement("pre"));
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "childidp1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("pre"));
		CSSElement pre = doc.createElement("pre");
		pre.setAttribute("id", "idpre");
		parent.appendChild(pre);
		elm = doc.createElement("p");
		elm.setAttribute("id", "childidp2");
		parent.appendChild(elm);

		assertTrue(elm.matches(selist, null));
		assertFalse(pre.matches(selist, null));
		assertFalse(elm.matches(selist2, null));
		assertTrue(pre.matches(selist2, null));

		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorDescendant() throws Exception {
		BaseCSSStyleSheet css = parseStyle("ul li a{padding:20px}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("ul li a", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement ul = doc.createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		CSSElement li = doc.createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		CSSElement p = doc.createElement("p");
		li.appendChild(p);
		CSSElement a = doc.createElement("a");
		a.setAttribute("id", "a1");
		p.appendChild(a);
		CSSElement a2 = doc.createElement("a");
		a2.setAttribute("id", "a2");
		p.appendChild(a2);
		assertFalse(p.matches(selist, null));
		assertTrue(a.matches(selist, null));
		assertTrue(a2.matches(selist, null));
		CSSElement li2 = doc.createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		CSSElement a3 = doc.createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);
		assertTrue(a3.matches(selist, null));

		SelectorMatcher matcher = selectorMatcher(a3);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 3, selist.item(selidx), matcher);

		CSSElement span = doc.createElement("span");
		a3.appendChild(span);
		assertFalse(span.matches(selist, null));
	}

	@Test
	public void testMatchSelectorChild() throws Exception {
		BaseCSSStyleSheet css = parseStyle("*>ul>li>a{padding:20px}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("*>ul>li>a", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement ul = doc.createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		CSSElement li = doc.createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		CSSElement a = doc.createElement("a");
		a.setAttribute("id", "a1");
		li.appendChild(a);
		CSSElement a2 = doc.createElement("a");
		a2.setAttribute("id", "a2");
		li.appendChild(a2);
		assertTrue(a.matches(selist, null));
		assertTrue(a2.matches(selist, null));
		CSSElement li2 = doc.createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		CSSElement a3 = doc.createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);
		assertTrue(a3.matches(selist, null));

		SelectorMatcher matcher = selectorMatcher(a3);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 3, selist.item(selidx), matcher);

		CSSElement span = doc.createElement("span");
		a3.appendChild(span);
		assertFalse(span.matches(selist, null));
	}

	@Test
	public void testMatchSelectorPseudoClass1() throws Exception {
		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);
		css = parseStyle("p:last-child {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:last-child", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		css = parseStyle("p:only-child {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:only-child", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoClass2() throws Exception {
		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		css = parseStyle("p:last-child {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("p:only-child {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassNth() throws Exception {
		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:nth-child(1) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:nth-child(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		parent.insertBefore(doc.createElement("div"), elm);
		assertTrue(matcher.matches(selist) < 0);

		css = parseStyle("p:nth-last-child(1) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		parent.appendChild(doc.createElement("div"));
		assertTrue(matcher.matches(selist) < 0);

		css = parseStyle("p:nth-last-child(2) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(2)", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNthSelector() throws Exception {
		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(doc.createElement("div"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:nth-child(1 of p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);

		css = parseStyle("p:nth-last-child(1 of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(1 of p)", selectorListToString(selist, rule));
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);

		CSSElement lastp = doc.createElement("p");
		parent.appendChild(lastp);
		assertEquals(-1, matcher.matches(selist));
		SelectorMatcher lastpMatcher = selectorMatcher(lastp);
		assertEquals(0, lastpMatcher.matches(selist));
		css = parseStyle("p:nth-last-child(2 of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(2 of p)", selectorListToString(selist, rule));
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		assertEquals(-1, lastpMatcher.matches(selist));
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);

		parent.removeChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoOfType() throws Exception {
		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("P");
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(doc.createElement("div"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);

		BaseCSSStyleSheet css = parseStyle("p:first-of-type {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:first-of-type", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);

		css = parseStyle("p:last-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:last-of-type", selectorListToString(selist, rule));
		assertEquals(0, matcher.matches(selist));
		CSSElement lastdiv = doc.createElement("div");
		parent.appendChild(lastdiv);
		assertEquals(0, matcher.matches(selist));
		CSSElement lastp = doc.createElement("p");
		parent.appendChild(lastp);
		assertEquals(-1, matcher.matches(selist));

		css = parseStyle("p:only-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:only-of-type", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) < 0);
		parent.removeChild(lastp);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNthOfType() throws Exception {
		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(doc.createElement("div"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:nth-of-type(1) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:nth-of-type(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) != -1);
		css = parseStyle("p:first-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:first-of-type", selectorListToString(selist, rule));
		assertEquals(0, matcher.matches(selist));

		css = parseStyle("p:nth-last-of-type(1) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-of-type(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) != -1);
		CSSElement lastp = doc.createElement("P");
		parent.appendChild(lastp);
		assertEquals(-1, matcher.matches(selist));

		css = parseStyle("p:nth-last-of-type(2) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-of-type(2)", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		parent.removeChild(lastp);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoNthOfTypeSVG() throws Exception {
		doc = domImpl.createDocument(TestConfig.SVG_NAMESPACE_URI, "svg", null);
		CSSElement parent = doc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "g");
		doc.getDocumentElement().appendChild(parent);
		CSSElement elm = doc.createElement("rect");
		parent.appendChild(doc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "ellipse"));
		parent.appendChild(doc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "text"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "ellipse"));
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("rect:nth-of-type(1) {fill: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("rect:nth-of-type(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) != -1);
		css = parseStyle("rect:first-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("rect:first-of-type", selectorListToString(selist, rule));
		assertEquals(0, matcher.matches(selist));

		css = parseStyle("rect:nth-last-of-type(1) {fill: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("rect:nth-last-of-type(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) != -1);
		CSSElement lastp = doc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "RECT");
		parent.appendChild(lastp);
		assertEquals(-1, matcher.matches(selist));

		css = parseStyle("rect:nth-last-of-type(2) {fill: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("rect:nth-last-of-type(2)", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		parent.removeChild(lastp);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoAnyLink() throws Exception {
		CSSElement a = doc.createElement("a");
		a.setAttribute("href", "foo");
		CSSElement elm = doc.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = selectorMatcher(a);
		BaseCSSStyleSheet css = parseStyle(":any-link {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":any-link", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, matcher.matches(selist));
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "bar");
		assertEquals(0, matcher.matches(selist));

		a.removeAttribute("href");
		matcher = selectorMatcher(a);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoLink() throws Exception {
		CSSElement a = doc.createElement("a");
		a.setAttribute("href", "foo");
		CSSElement elm = doc.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = selectorMatcher(a);
		BaseCSSStyleSheet css = parseStyle(":link {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":link", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, matcher.matches(selist));
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "bar");
		assertEquals(0, matcher.matches(selist));

		a.removeAttribute("href");
		matcher = selectorMatcher(a);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoVisited() throws Exception {
		CSSElement a = doc.createElement("a");
		a.setAttribute("href", "https://www.example.com/foo");
		CSSElement elm = doc.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = selectorMatcher(a);
		BaseCSSStyleSheet css = parseStyle(":visited {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":visited", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(-1, selidx);
		domImpl.setVisitedURI("https://www.example.com/foo");
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
				"https://www.example.com/bar");
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
				"https://www.example.com/foo");
		assertEquals(0, matcher.matches(selist));

		a.removeAttribute("href");
		matcher = selectorMatcher(a);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoTarget() throws Exception {
		CSSElement div = doc.createElement("div");
		div.setAttribute("id", "mytarget");
		CSSElement elm = doc.getDocumentElement();
		elm.appendChild(div);
		SelectorMatcher matcher = selectorMatcher(div);
		BaseCSSStyleSheet css = parseStyle(":target {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":target", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoRoot() throws Exception {
		CSSElement div = doc.createElement("div");
		CSSElement elm = doc.getDocumentElement();
		elm.appendChild(div);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle(":root {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":root", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoEmpty() throws Exception {
		CSSElement div = doc.createElement("div");
		doc.getDocumentElement().appendChild(div);
		div.appendChild(doc.createTextNode(""));
		SelectorMatcher matcher = selectorMatcher(div);
		BaseCSSStyleSheet css = parseStyle("div:empty {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div:empty", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		CSSElement p = doc.createElement("p");
		div.appendChild(p);
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
		div.removeChild(p);
		matcher = selectorMatcher(div);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		div.appendChild(doc.createTextNode("foo"));
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoBlank() throws Exception {
		CSSElement div = doc.createElement("div");
		doc.getDocumentElement().appendChild(div);
		div.appendChild(doc.createTextNode("   "));
		SelectorMatcher matcher = selectorMatcher(div);
		BaseCSSStyleSheet css = parseStyle("div:blank {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div:blank", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		CSSElement p = doc.createElement("p");
		div.appendChild(p);
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
		div.removeChild(p);
		matcher = selectorMatcher(div);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		div.appendChild(doc.createTextNode("foo"));
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoHas1() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:has(> img) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:has(>img)", selectorListToString(selist, rule));

		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("id", "p1");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		CSSElement elm2 = doc.createElement("img");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);

		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:has(+ p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:has(+p)", selectorListToString(selist, rule));

		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		CSSElement elm2 = doc.createElement("p");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);

		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas3() throws Exception {
		BaseCSSStyleSheet css = parseStyle("div.exampleclass:has(p>span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div.exampleclass:has(p>span)", selectorListToString(selist, rule));

		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("p");
		parent.appendChild(elm);
		CSSElement span = doc.createElement("span");
		elm.appendChild(span);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 3, selist.item(selidx), matcher);

		elm.removeChild(span);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas4() throws Exception {
		BaseCSSStyleSheet css = parseStyle("div.exampleclass:has(span + p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div.exampleclass:has(span+p)", selectorListToString(selist, rule));

		CSSElement parent = createTopLevelElement("div");
		CSSElement elm = doc.createElement("span");
		parent.appendChild(elm);
		CSSElement elm2 = doc.createElement("p");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 3, selist.item(selidx), matcher);

		parent.removeChild(elm);
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas5() throws Exception {
		BaseCSSStyleSheet css = parseStyle("body>div.exampleclass:has(span + p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("body>div.exampleclass:has(span+p)", selectorListToString(selist, rule));

		CSSElement body = createTopLevelElement("body");
		CSSElement parent = doc.createElement("div");
		body.appendChild(parent);
		CSSElement elm = doc.createElement("span");
		parent.appendChild(elm);
		CSSElement elm2 = doc.createElement("p");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 4, selist.item(selidx), matcher);

		parent.removeChild(elm);
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas6() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:has(* img,#fooID) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:has(* img,#fooID)", selectorListToString(selist, rule));

		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("id", "p1");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		CSSElement elm2 = doc.createElement("span");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		CSSElement elm3 = doc.createElement("img");
		elm3.setAttribute("id", "childid3");
		elm2.appendChild(elm3);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 1, 1, selist.item(selidx), matcher);

		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoIs() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				":is(.exampleclass span[foo], div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":is(.exampleclass span[foo],div>span)", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		parent.appendChild(elm);
		CSSElement child2 = doc.createElement("span");
		child2.setAttribute("foo", "bar");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoIsID() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"span:is(.exampleclass span[foo], div > span, #fooID) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("span:is(.exampleclass span[foo],div>span,#fooID)",
				selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		parent.appendChild(elm);
		CSSElement child2 = doc.createElement("span");
		child2.setAttribute("foo", "bar");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoIsNested() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				":is(.fooclass span, div > span, :is(p#exampleid.exampleclass span#sp2Id.spcl)) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":is(.fooclass span,div>span,:is(p#exampleid.exampleclass span#sp2Id.spcl))",
				selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		parent.appendChild(elm);
		CSSElement child2 = doc.createElement("span");
		child2.setAttribute("class", "spcl");
		child2.setAttribute("id", "sp2Id");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(2, 2, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoWhere() throws Exception {
		BaseCSSStyleSheet css = parseStyle(":where(.exampleclass span, div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":where(.exampleclass span,div>span)", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = doc.createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		CSSElement child2 = doc.createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoWhere2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("span:where(.foo .bar, div > .bar) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("span:where(.foo .bar,div>.bar)", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("class", "foo");
		CSSElement elm = doc.createElement("span");
		elm.setAttribute("class", "bar");
		parent.appendChild(elm);
		CSSElement p = doc.createElement("p");
		parent.appendChild(p);
		CSSElement child2 = doc.createElement("span");
		p.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNot() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:not(:last-child) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:not(:last-child)", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "p1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		CSSElement elm2 = doc.createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNotId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:not(:last-child,p#noID) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:not(:last-child,p#noID)", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "p1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		CSSElement elm2 = doc.createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 1, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNotNested() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:not(:last-child, :not(p)) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:not(:last-child,:not(p))", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement elm = doc.createElement("p");
		elm.setAttribute("id", "p1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		CSSElement elm2 = doc.createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoClassEnabledDisabled() throws Exception {
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:disabled {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:disabled", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) >= 0);
		elm.removeAttribute("disabled");
		css = parseStyle("input:enabled {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("input:enabled", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassReadWriteReadOnly() throws Exception {
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:read-only {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:read-only", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) >= 0);
		elm.removeAttribute("disabled");
		css = parseStyle("input:read-write {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("input:read-write", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("div:read-write {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("div:read-write", selectorListToString(selist, rule));
		CSSElement div = doc.createElement("div");
		SelectorMatcher divmatcher = selectorMatcher(div);
		assertTrue(divmatcher.matches(selist) < 0);
		div.setAttribute("contenteditable", "true");
		selidx = divmatcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), divmatcher);
	}

	@Test
	public void testMatchSelectorPseudoClassPlaceholderShown() throws Exception {
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "text");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:placeholder-shown {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:placeholder-shown", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("placeholder", "Enter text");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClassDefault() throws Exception {
		CSSElement button = doc.createElement("button");
		button.setAttribute("type", "submit");
		button.setAttribute("disabled", "disabled");
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "submit");
		parent.appendChild(button);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:default", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		button.removeAttribute("disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassDefault2() throws Exception {
		CSSElement div = doc.createElement("div");
		CSSElement button = doc.createElement("button");
		button.setAttribute("type", "submit");
		button.setAttribute("disabled", "disabled");
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "submit");
		parent.appendChild(div);
		div.appendChild(button);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:default", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		button.removeAttribute("disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassDefault3() throws Exception {
		CSSElement div = doc.createElement("div");
		CSSElement button1 = doc.createElement("button");
		button1.setAttribute("type", "submit");
		CSSElement button2 = doc.createElement("button");
		button2.setAttribute("type", "submit");
		button2.setAttribute("disabled", "disabled");
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "submit");
		parent.appendChild(div);
		div.appendChild(button1);
		parent.appendChild(button2);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:default", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(-1, selidx);

		div.removeChild(button1);
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		button2.removeAttribute("disabled");
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoClassChecked() throws Exception {
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		elm.setAttribute("checked", "checked");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:checked {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:checked", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.removeAttribute("checked");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassIndeterminate() throws Exception {
		CSSElement parent = createTopLevelElement("form");
		CSSElement elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		elm.setAttribute("indeterminate", "true");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:indeterminate {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:indeterminate", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("indeterminate", "false");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassState() throws Exception {
		CSSElement head = doc.createElement("head");
		CSSElement style = doc.createElement("style");
		CSSElement elm = doc.createElement("p");
		style.setAttribute("type", "text/css");
		style.appendChild(doc
				.createTextNode("p:hover {text-decoration-line:underline; text-align: center;}"));
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		CSSElement body = doc.createElement("body");
		body.appendChild(elm);
		CSSElement span = doc.createElement("span");
		elm.appendChild(span);
		doc.getDocumentElement().appendChild(body);
		doc.setTargetMedium("screen");
		assertEquals("p:hover {text-decoration-line: underline; text-align: center; }",
				doc.getStyleSheet().toString());
		CSSComputedProperties styledecl = doc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(styledecl);
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
		DummyCanvas canvas = (DummyCanvas) doc.getCanvas();
		assertNotNull(canvas);
		List<String> statePseudoClasses = new LinkedList<>();
		statePseudoClasses.add("hover");
		canvas.registerStatePseudoclasses(elm, statePseudoClasses);
		styledecl = doc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("underline", styledecl.getPropertyValue("text-decoration-line"));
		assertEquals("center", styledecl.getPropertyValue("text-align"));
		assertEquals("center",
				doc.getStyleSheet().getComputedStyle(span, null).getPropertyValue("text-align"));
		styledecl = doc.getStyleSheet().getComputedStyle(body, null);
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
		styledecl = doc.getStyleSheet().getComputedStyle(span, null);
		assertEquals("center", styledecl.getPropertyValue("text-align"));
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
	}

	@Test
	public void testFindStaticPseudoClasses() throws Exception {
		List<String> statePseudoClasses = new LinkedList<>();
		BaseCSSStyleSheet css = parseStyle("div:blank {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.isEmpty());
		statePseudoClasses.clear();
		css = parseStyle("div:hover {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("div:hover", selectorListToString(selist, rule));
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.contains("hover"));
		statePseudoClasses.clear();
		css = parseStyle(":playing {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(":playing", selectorListToString(selist, rule));
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.contains("playing"));
		statePseudoClasses.clear();
		css = parseStyle("div > p {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("div>p", selectorListToString(selist, rule));
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.isEmpty());
		statePseudoClasses.clear();
		css = parseStyle("div p {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("div p", selectorListToString(selist, rule));
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.isEmpty());
	}

	/**
	 * Create an element and append to document element.
	 * 
	 * @param name the element name.
	 * @return the element.
	 */
	CSSElement createTopLevelElement(String name) {
		CSSElement elm = doc.createElement(name);
		doc.getDocumentElement().appendChild(elm);
		return elm;
	}

	protected SelectorMatcher selectorMatcher(CSSElement elm) {
		return new DOMSelectorMatcher(elm);
	}

	public BaseCSSStyleSheet parseStyle(String style) throws CSSParseException, IOException {
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		StringReader re = new StringReader(style);
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_IGNORE));
		cssParser.parseStyleSheet(re);
		return css;
	}

	private static String selectorListToString(SelectorList selist, StyleRule rule) {
		if (selist == null) {
			return null;
		}

		SelectorSerializer serializer = new SelectorSerializer(rule.getParentStyleSheet());
		StringBuilder buf = new StringBuilder();
		serializer.selectorText(buf, selist.item(0), false);
		int sz = selist.getLength();
		for (int i = 1; i < sz; i++) {
			buf.append(' ');
			serializer.selectorText(buf, selist.item(i), false);
		}
		return buf.toString();
	}

}
