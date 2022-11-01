/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.TestDOMImplementation;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory.DummyCanvas;

public class SelectorMatcherTest {

	private Parser cssParser;

	private static CSSDocument doc;

	private TestDOMImplementation domImpl = new TestDOMImplementation(false);

	@Before
	public void setUp() {
		this.cssParser = new io.sf.carte.doc.style.css.parser.CSSParser();
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
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
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		SelectorMatcher svgmatcher = selectorMatcher(svg);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		assertTrue(svgmatcher.matches(selist) == -1);
		assertTrue(matcher.matches(svgselist) == -1);
		selidx = svgmatcher.matches(svgselist);
		assertTrue(selidx >= 0);
		// Specificity
		sp = new Specificity(svgselist.item(selidx), svgmatcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector1ElementNoNS() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"@namespace url('https://www.w3.org/1999/xhtml/'); p{color: blue;} |div{margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();
		DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xmldoc = docbuilder.newDocument();
		Element root = xmldoc.createElement("html");
		xmldoc.appendChild(root);
		Element p = xmldoc.createElement("p");
		p.setAttribute("id", "p1");
		p.setIdAttribute("id", true);
		root.appendChild(p);
		Element pns = xmldoc.createElementNS("https://www.w3.org/1999/xhtml/", "p");
		pns.setAttribute("id", "p2");
		pns.setIdAttribute("id", true);
		root.appendChild(pns);
		Element div = xmldoc.createElement("div");
		div.setAttribute("id", "div1");
		div.setIdAttribute("id", true);
		root.appendChild(div);
		Element divns = xmldoc.createElementNS("https://www.w3.org/1999/xhtml/", "div");
		divns.setAttribute("id", "div2");
		divns.setIdAttribute("id", true);
		root.appendChild(divns);
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		StylableDocumentWrapper wrappedDoc = cssFac.createCSSDocument(xmldoc);
		CSSElement wp = wrappedDoc.getElementById("p1");
		CSSElement wpns = wrappedDoc.getElementById("p2");
		CSSElement wdiv = wrappedDoc.getElementById("div1");
		CSSElement wdivns = wrappedDoc.getElementById("div2");
		assertFalse(wp.matches(selist, null));
		assertTrue(wpns.matches(selist, null));
		selist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		assertFalse(wdivns.matches(selist, null));
		assertTrue(wdiv.matches(selist, null));
	}

	@Test
	public void testMatchSelector1Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("[title]", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorUniversalAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("*[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("[title]", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorTypeAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title]", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		assertEquals("p[title='hi']", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("title", "hi ho");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		assertEquals("p[title~='hi']", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
		elm.setAttribute("title", "h");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		assertEquals("p[lang|='en']", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("lang", "en-US");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("lang", "en");
		assertTrue(matcher.matches(selist) >= 0);
		//
		elm.setAttribute("lang", "en_US");
		assertTrue(matcher.matches(selist) < 0);
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("lang", "EN-US");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("lang", "EN");
		assertTrue(matcher.matches(selist) >= 0);
		//
		elm.setAttribute("lang", "en_US");
		assertTrue(matcher.matches(selist) < 0);
		//
		elm = createTopLevelElement("div");
		elm.setAttribute("lang", "en");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginsAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title^=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title^='hi']", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("title", "hi");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HI HO");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
		elm.setAttribute("title", "h");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
		elm = createTopLevelElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorEndsAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title$=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title$='hi']", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
		elm.setAttribute("title", "i");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		assertEquals("p[title*='hi']", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI HO");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
		elm.setAttribute("title", "HI HO");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
		elm.setAttribute("title", "HOHI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		//
		elm.setAttribute("title", "H");
		assertEquals(-1, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLang2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(en-US) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-US");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-DE");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-Latn-DE");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
		//
		elm = createTopLevelElement("p");
		elm.setAttribute("lang", "de-DE");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		//
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector1ClassStrict() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleClass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
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
		sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorMultipleClass() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		elm = createTopLevelElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
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
		sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector2Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle("z.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("z.exampleclass", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector3Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector4Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass span", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector5Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
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
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		Element child2 = parent.getOwnerDocument().createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector1MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass secondclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector2MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.secondclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass secondclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector3MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", " firstclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector4MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "firstclass ");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector1Firstchild() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:first-child", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("div");
		parent.setAttribute("id", "parentid");
		Element firstChild = parent.getOwnerDocument().createElement("p");
		firstChild.setAttribute("id", "pid1");
		parent.appendChild(firstChild);
		Element elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "pid2");
		parent.appendChild(elm);
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		StylableDocumentWrapper doc = factory.createCSSDocument(elm.getOwnerDocument());
		SelectorMatcher matcher = new DOMSelectorMatcher((CSSElement) doc.getCSSNode(firstChild));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		matcher = new DOMSelectorMatcher((CSSElement) doc.getCSSNode(elm));
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector1Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(1, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector1IdStrict() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle("#exampleID {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleID", selectorListToString(selist, rule));
		Element elm = createTopLevelElement("p");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(1, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector2Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid span", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(1, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector2aId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
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
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		Element child2 = parent.getOwnerDocument().createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(1, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector3Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid>span", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(1, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector3bId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
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
		Element elm = createTopLevelElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(1, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		Element elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "childid1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorAdjacentNoMatch() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p+.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p+.exampleclass", selectorListToString(selist, rule));

		Element parent = createTopLevelElement("div");
		Element elm = parent.getOwnerDocument().createElement("p");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("div");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(-1, selidx);
		elm = parent.getOwnerDocument().createElement("div");
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
		CSSElement elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "childidp1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("pre"));
		CSSElement pre = doc.createElement("pre");
		pre.setAttribute("id", "idpre");
		parent.appendChild(pre);
		elm = parent.getOwnerDocument().createElement("p");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorDescendant() throws Exception {
		BaseCSSStyleSheet css = parseStyle("ul li a{padding:20px}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("ul li a", selectorListToString(selist, rule));
		CSSElement parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		CSSElement ul = parent.getOwnerDocument().createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		CSSElement li = parent.getOwnerDocument().createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		CSSElement p = parent.getOwnerDocument().createElement("p");
		li.appendChild(p);
		CSSElement a = parent.getOwnerDocument().createElement("a");
		a.setAttribute("id", "a1");
		p.appendChild(a);
		CSSElement a2 = parent.getOwnerDocument().createElement("a");
		a2.setAttribute("id", "a2");
		p.appendChild(a2);
		assertFalse(p.matches(selist, null));
		assertTrue(a.matches(selist, null));
		assertTrue(a2.matches(selist, null));
		CSSElement li2 = parent.getOwnerDocument().createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		CSSElement a3 = parent.getOwnerDocument().createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);
		assertTrue(a3.matches(selist, null));
		//
		SelectorMatcher matcher = selectorMatcher(a3);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(3, sp.names_pseudoelements_count);
		//
		CSSElement span = parent.getOwnerDocument().createElement("span");
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
		CSSElement ul = parent.getOwnerDocument().createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		CSSElement li = parent.getOwnerDocument().createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		CSSElement a = parent.getOwnerDocument().createElement("a");
		a.setAttribute("id", "a1");
		li.appendChild(a);
		CSSElement a2 = parent.getOwnerDocument().createElement("a");
		a2.setAttribute("id", "a2");
		li.appendChild(a2);
		assertTrue(a.matches(selist, null));
		assertTrue(a2.matches(selist, null));
		CSSElement li2 = parent.getOwnerDocument().createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		CSSElement a3 = parent.getOwnerDocument().createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);
		assertTrue(a3.matches(selist, null));
		//
		SelectorMatcher matcher = selectorMatcher(a3);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(3, sp.names_pseudoelements_count);
		//
		CSSElement span = parent.getOwnerDocument().createElement("span");
		a3.appendChild(span);
		assertFalse(span.matches(selist, null));
	}

	@Test
	public void testMatchSelectorPseudoClass1() throws Exception {
		Element parent = createTopLevelElement("div");
		Element elm = doc.createElement("p");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoClass2() throws Exception {
		Element parent = createTopLevelElement("div");
		Element elm = doc.createElement("p");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		Element parent = createTopLevelElement("div");
		Element elm = doc.createElement("p");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoNthSelector() throws Exception {
		Element parent = createTopLevelElement("div");
		Element elm = doc.createElement("p");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
		css = parseStyle("p:nth-last-child(1 of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(1 of p)", selectorListToString(selist, rule));
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
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
		sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
		parent.removeChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoOfType() throws Exception {
		Element parent = createTopLevelElement("div");
		Element elm = doc.createElement("p");
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
		assertTrue(matcher.matches(selist) >= 0);
		CSSElement lastp = doc.createElement("p");
		parent.appendChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("p:only-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:only-of-type", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) < 0);
		parent.removeChild(lastp);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoNthOfType() throws Exception {
		Element parent = createTopLevelElement("div");
		Element elm = doc.createElement("p");
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
		assertTrue(matcher.matches(selist) != -1);
		css = parseStyle("p:nth-last-of-type(1) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-of-type(1)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) != -1);
		CSSElement lastp = doc.createElement("p");
		parent.appendChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("p:nth-last-of-type(2) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-of-type(2)", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		parent.removeChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoAnyLink() throws Exception {
		Element a = doc.createElement("a");
		a.setAttribute("href", "foo");
		Element elm = doc.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = selectorMatcher(a);
		BaseCSSStyleSheet css = parseStyle(":any-link {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":any-link", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, matcher.matches(selist));
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
		//
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "bar");
		assertEquals(0, matcher.matches(selist));
		//
		a.removeAttribute("href");
		matcher = selectorMatcher(a);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoLink() throws Exception {
		Element a = doc.createElement("a");
		a.setAttribute("href", "foo");
		Element elm = doc.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = selectorMatcher(a);
		BaseCSSStyleSheet css = parseStyle(":link {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":link", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, matcher.matches(selist));
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
		//
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "bar");
		assertEquals(0, matcher.matches(selist));
		//
		a.removeAttribute("href");
		matcher = selectorMatcher(a);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoVisited() throws Exception {
		Element a = doc.createElement("a");
		a.setAttribute("href", "https://www.example.com/foo");
		Element elm = doc.getDocumentElement();
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
		//
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "https://www.example.com/bar");
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "https://www.example.com/foo");
		assertEquals(0, matcher.matches(selist));
		//
		a.removeAttribute("href");
		matcher = selectorMatcher(a);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoTarget() throws Exception {
		Element div = doc.createElement("div");
		div.setAttribute("id", "mytarget");
		Element elm = doc.getDocumentElement();
		elm.appendChild(div);
		SelectorMatcher matcher = selectorMatcher(div);
		BaseCSSStyleSheet css = parseStyle(":target {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":target", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
		//
		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoRoot() throws Exception {
		Element div = doc.createElement("div");
		Element elm = doc.getDocumentElement();
		elm.appendChild(div);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle(":root {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":root", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
		//
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoEmpty() throws Exception {
		Element div = doc.createElement("div");
		doc.getDocumentElement().appendChild(div);
		div.appendChild(doc.createTextNode(""));
		SelectorMatcher matcher = selectorMatcher(div);
		BaseCSSStyleSheet css = parseStyle("div:empty {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div:empty", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		Element p = doc.createElement("p");
		div.appendChild(p);
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
		div.removeChild(p);
		matcher = selectorMatcher(div);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		div.appendChild(doc.createTextNode("foo"));
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoBlank() throws Exception {
		Element div = doc.createElement("div");
		doc.getDocumentElement().appendChild(div);
		div.appendChild(doc.createTextNode("   "));
		SelectorMatcher matcher = selectorMatcher(div);
		BaseCSSStyleSheet css = parseStyle("div:blank {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div:blank", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		Element p = doc.createElement("p");
		div.appendChild(p);
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
		div.removeChild(p);
		matcher = selectorMatcher(div);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
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
		//
		Element parent = createTopLevelElement("p");
		parent.setAttribute("id", "p1");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		Element elm2 = parent.getOwnerDocument().createElement("img");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:has(+ p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:has(+p)", selectorListToString(selist, rule));
		//
		Element parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		Element elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		Element elm2 = parent.getOwnerDocument().createElement("p");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas3() throws Exception {
		BaseCSSStyleSheet css = parseStyle("div.exampleclass:has(p>span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div.exampleclass:has(p>span)", selectorListToString(selist, rule));
		//
		Element parent = createTopLevelElement("div");
		Element elm = parent.getOwnerDocument().createElement("p");
		parent.appendChild(elm);
		Element span = parent.getOwnerDocument().createElement("span");
		elm.appendChild(span);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
		elm.removeChild(span);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas4() throws Exception {
		BaseCSSStyleSheet css = parseStyle("div.exampleclass:has(span + p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("div.exampleclass:has(span+p)", selectorListToString(selist, rule));
		//
		Element parent = createTopLevelElement("div");
		Element elm = parent.getOwnerDocument().createElement("span");
		parent.appendChild(elm);
		Element elm2 = parent.getOwnerDocument().createElement("p");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
		//
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
		//
		Element body = createTopLevelElement("body");
		Element parent = doc.createElement("div");
		body.appendChild(parent);
		Element elm = doc.createElement("span");
		parent.appendChild(elm);
		Element elm2 = doc.createElement("p");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(3, sp.names_pseudoelements_count);
		//
		parent.removeChild(elm);
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoIs() throws Exception {
		BaseCSSStyleSheet css = parseStyle(":is(.exampleclass span, div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":is(.exampleclass span,div>span)", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
		parent.appendChild(elm);
		Element child2 = parent.getOwnerDocument().createElement("span");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoWhere() throws Exception {
		BaseCSSStyleSheet css = parseStyle(":where(.exampleclass span, div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(":where(.exampleclass span,div>span)", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = parent.getOwnerDocument().createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		Element child2 = parent.getOwnerDocument().createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoWhere2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("span:where(.foo .bar, div > .bar) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("span:where(.foo .bar,div>.bar)", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("div");
		parent.setAttribute("class", "foo");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("class", "bar");
		parent.appendChild(elm);
		Element p = parent.getOwnerDocument().createElement("p");
		parent.appendChild(p);
		Element child2 = parent.getOwnerDocument().createElement("span");
		p.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoNot() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:not(:last-child) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:not(:last-child)", selectorListToString(selist, rule));
		Element parent = createTopLevelElement("div");
		parent.setAttribute("id", "div1");
		Element elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "p1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		Element elm2 = parent.getOwnerDocument().createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(2, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoClassEnabledDisabled() throws Exception {
		Element parent = createTopLevelElement("form");
		Element elm = doc.createElement("input");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassReadWriteReadOnly() throws Exception {
		Element parent = createTopLevelElement("form");
		Element elm = doc.createElement("input");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("div:read-write {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("div:read-write", selectorListToString(selist, rule));
		Element div = doc.createElement("div");
		SelectorMatcher divmatcher = selectorMatcher(div);
		assertTrue(divmatcher.matches(selist) < 0);
		div.setAttribute("contenteditable", "true");
		selidx = divmatcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		sp = new Specificity(selist.item(selidx), divmatcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoClassPlaceholderShown() throws Exception {
		Element parent = createTopLevelElement("form");
		Element elm = doc.createElement("input");
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
		Element button = doc.createElement("button");
		button.setAttribute("type", "submit");
		button.setAttribute("disabled", "disabled");
		Element parent = createTopLevelElement("form");
		Element elm = doc.createElement("input");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		button.removeAttribute("disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassChecked() throws Exception {
		Element parent = createTopLevelElement("form");
		Element elm = doc.createElement("input");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.removeAttribute("checked");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassIndeterminate() throws Exception {
		Element parent = createTopLevelElement("form");
		Element elm = doc.createElement("input");
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
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
		//
		elm.setAttribute("indeterminate", "false");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassState() throws Exception {
		CSSElement head = doc.createElement("head");
		CSSElement style = doc.createElement("style");
		CSSElement elm = doc.createElement("p");
		style.setAttribute("type", "text/css");
		style.appendChild(doc.createTextNode("p:hover {text-decoration-line:underline; text-align: center;}"));
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		CSSElement body = doc.createElement("body");
		body.appendChild(elm);
		CSSElement span = doc.createElement("span");
		elm.appendChild(span);
		doc.getDocumentElement().appendChild(body);
		doc.setTargetMedium("screen");
		assertEquals("p:hover {text-decoration-line: underline; text-align: center; }", doc.getStyleSheet().toString());
		CSSComputedProperties styledecl = doc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(styledecl);
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
		DummyCanvas canvas = (DummyCanvas) doc.getCanvas();
		assertNotNull(canvas);
		List<String> statePseudoClasses = new LinkedList<String>();
		statePseudoClasses.add("hover");
		canvas.registerStatePseudoclasses(elm, statePseudoClasses);
		styledecl = doc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("underline", styledecl.getPropertyValue("text-decoration-line"));
		assertEquals("center", styledecl.getPropertyValue("text-align"));
		assertEquals("center", doc.getStyleSheet().getComputedStyle(span, null).getPropertyValue("text-align"));
		styledecl = doc.getStyleSheet().getComputedStyle(body, null);
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
		styledecl = doc.getStyleSheet().getComputedStyle(span, null);
		assertEquals("center", styledecl.getPropertyValue("text-align"));
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
	}

	@Test
	public void testFindStaticPseudoClasses() throws Exception {
		List<String> statePseudoClasses = new LinkedList<String>();
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
	static CSSElement createTopLevelElement(String name) {
		CSSElement elm = doc.createElement(name);
		doc.getDocumentElement().appendChild(elm);
		return elm;
	}

	static SelectorMatcher selectorMatcher(Element elm) {
		return new DOMSelectorMatcher((CSSElement) elm);
	}

	public BaseCSSStyleSheet parseStyle(String style) throws CSSParseException, IOException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.createStyleSheet(null, null);
		StringReader re = new StringReader(style);
		cssParser.setDocumentHandler(css.createSheetHandler(CSSStyleSheet.COMMENTS_IGNORE));
		cssParser.parseStyleSheet(re);
		return css;
	}

	private static String selectorListToString(SelectorList selist, CSSStyleDeclarationRule rule) {
		if (selist == null) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		buf.append(rule.selectorText(selist.item(0), false));
		int sz = selist.getLength();
		for (int i = 1; i < sz; i++) {
			buf.append(' ').append(rule.selectorText(selist.item(i), false));
		}
		return buf.toString();
	}

}
