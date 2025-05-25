/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class WrapperSelectorMatcherTest {

	static TestCSSStyleSheetFactory factory;

	private Parser cssParser;

	private StylableDocumentWrapper cssdoc;

	private Document plaindoc;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		factory = new TestCSSStyleSheetFactory();
	}

	@BeforeEach
	public void setUp() throws Exception {
		this.cssParser = new io.sf.carte.doc.style.css.parser.CSSParser();
		setUpWithMode(CSSDocument.ComplianceMode.QUIRKS);
	}

	private void setUpWithMode(CSSDocument.ComplianceMode mode) throws Exception {
		createDocumentWithMode(mode);
	}

	private void createDocumentWithMode(CSSDocument.ComplianceMode mode) throws Exception {
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		DOMImplementation impl = registry.getDOMImplementation("XML 3.0");
		DocumentType doctype = null;
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			doctype = impl.createDocumentType("html", null, null);
		}
		plaindoc = impl.createDocument(null, "html", doctype);
		plaindoc.setDocumentURI("http://www.example.com/mydoc.html#mytarget");
	}

	@Test
	public void testMatchSelectorUniversal() throws Exception {
		BaseCSSStyleSheet css = parseStyle("* {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("*", selectorListToString(selist, rule));
		Element elm = createElement("p");
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
		Element elm = createElement("p");
		Element div = createElement("div");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ElementUppercase() throws Exception {
		Element elm = createElement("P");
		Element div = createElement("div");
		SelectorMatcher matcher = selectorMatcher(elm);

		BaseCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p", selectorListToString(selist, rule));
		int selidx = matcher.matches(selist);
		assertEquals(0, matcher.matches(selist));

		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ElementPrefix() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p", selectorListToString(selist, rule));

		String nsURI = "http://www.example.com/examplens";
		Element elm = plaindoc.createElementNS(nsURI, "pre:P");
		plaindoc.getDocumentElement().appendChild(elm);
		Element div = createElement("div");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, matcher.matches(selist));

		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ElementNS() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();
		SelectorList svgselist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		Element svg = plaindoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		SelectorMatcher svgmatcher = selectorMatcher(svg);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);

		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		assertEquals(-1, svgmatcher.matches(selist));
		assertEquals(-1, matcher.matches(svgselist));
		selidx = svgmatcher.matches(svgselist);
		assertEquals(0, selidx);

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
		Element root = plaindoc.getDocumentElement();
		Element p = plaindoc.createElement("p");
		p.setAttribute("id", "p1");
		p.setIdAttribute("id", true);
		root.appendChild(p);
		Element pns = plaindoc.createElementNS("https://www.w3.org/1999/xhtml/", "p");
		pns.setAttribute("id", "p2");
		pns.setIdAttribute("id", true);
		root.appendChild(pns);
		Element div = plaindoc.createElement("div");
		div.setAttribute("id", "div1");
		div.setIdAttribute("id", true);
		root.appendChild(div);
		Element divns = plaindoc.createElementNS("https://www.w3.org/1999/xhtml/", "div");
		divns.setAttribute("id", "div2");
		divns.setIdAttribute("id", true);
		root.appendChild(divns);
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		StylableDocumentWrapper wrappedDoc = cssFac.createCSSDocument(plaindoc);
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
		Element elm = createElement("p");
		Element elm2 = createElement("p");
		elm2.setAttribute("title", "hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(elm2);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
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
		Element elm = createElement("p");
		Element elm2 = createElement("p");
		elm2.setAttribute("title", "hi");
		Element elm3 = createElement("p");
		elm3.setAttribute("Title", "hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(elm2);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);

		matcher = selectorMatcher(elm3);
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		sp = new Specificity(selist.item(selidx), matcher);
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
		Element elm = createElement("p");
		Element elm2 = createElement("p");
		elm2.setAttribute("title", "hi");
		Element elm3 = createElement("p");
		elm3.setAttribute("Title", "hi");
		Element div = createElement("div");
		div.setAttribute("title", "hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(elm2);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(elm3);
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		Element elm2 = createElement("p");
		elm2.setAttribute("title", "hi");
		Element elm3 = createElement("p");
		elm3.setAttribute("Title", "hi");
		Element div = createElement("div");
		div.setAttribute("title", "hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		matcher = selectorMatcher(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(elm3);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2AttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title='hi' i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		Element elm0 = createElement("p");
		elm0.setAttribute("title", "HI");
		Element elm1 = createElement("p");
		elm1.setAttribute("title", "hi ho");
		Element div = createElement("div");
		div.setAttribute("title", "hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(elm0);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(elm1);
		assertEquals(-1, matcher.matches(selist));

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorOneOfAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title~=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title~='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		Element elm1 = createElement("p");
		elm1.setAttribute("title", "ho hi");
		Element div = createElement("div");
		div.setAttribute("title", "hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);

		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(elm1);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorOneOfAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title~='hi' i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		Element elm0 = createElement("p");
		elm0.setAttribute("title", "HO HI");
		Element elm1 = createElement("p");
		elm1.setAttribute("title", "HI");
		Element elm2 = createElement("p");
		elm2.setAttribute("title", "h");
		Element div = createElement("div");
		div.setAttribute("title", "ho hi");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		matcher = selectorMatcher(elm0);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(elm1);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		matcher = selectorMatcher(elm2);
		assertEquals(-1, matcher.matches(selist));

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginHyphenAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[lang|=\"en\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[lang|='en']", selectorListToString(selist, rule));
		Element elm = createElement("p");
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
	}

	@Test
	public void testMatchSelectorBeginHyphenAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[lang|=\"en\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[lang|='en' i]", selectorListToString(selist, rule));
		Element elm = createElement("p");
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
	}

	@Test
	public void testMatchSelectorBeginsAttribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title^=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title^='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
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
	}

	@Test
	public void testMatchSelectorBeginsAttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title^=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title^='hi' i]", selectorListToString(selist, rule));
		Element elm = createElement("p");
		Element elm1 = createElement("p");
		elm1.setAttribute("title", "HI HO");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		matcher = selectorMatcher(elm1);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelector1Lang() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(en) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:lang(en)", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector2Lang() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:lang(en) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		elm.setAttribute("lang", "en-US");
		Element div = createElement("div");
		div.setAttribute("lang", "en-US");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(1, sp.names_pseudoelements_count);

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		Element elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
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
		Element elm = createElement("p");
		elm.setAttribute("class", "exampleClass");
		Element elm1 = createElement("p");
		elm1.setAttribute("class", "exampleClass");
		Element elm2 = createElement("p");
		elm2.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		matcher = selectorMatcher(elm1);
		assertEquals(0, matcher.matches(selist));
		matcher = selectorMatcher(elm2);
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
		// STRICT
		cssdoc = null;
		setUpWithMode(CSSDocument.ComplianceMode.STRICT);
		css = parseStyle(".exampleclass {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		elm1 = createElement("p");
		elm1.setAttribute("class", "exampleClass");
		elm2 = createElement("p");
		elm2.setAttribute("class", "exampleclass");
		matcher = selectorMatcher(elm1);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(elm2);
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(1, sp.attrib_classes_count);
		assertEquals(0, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorTypeId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p#exampleid", selectorListToString(selist, rule));
		Element div = createElement("div");
		div.setAttribute("id", "exampleid");
		Element elm = createElement("p");
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

		matcher = selectorMatcher(div);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorAdjacent() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass + p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass+p", selectorListToString(selist, rule));
		Element parent = createElement("div");
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
	public void testMatchSelectorSubsequentSibling() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass ~ p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass~p", selectorListToString(selist, rule));
		Element parent = createElement("div");
		parent.setAttribute("id", "div1");
		plaindoc.getDocumentElement().appendChild(parent);
		parent.appendChild(plaindoc.createElement("pre"));
		Element p = parent.getOwnerDocument().createElement("p");
		p.setAttribute("id", "childidp1");
		p.setAttribute("class", "exampleclass");
		parent.appendChild(p);
		parent.appendChild(plaindoc.createElement("pre"));
		Element pre = plaindoc.createElement("pre");
		parent.appendChild(pre);
		Element p2 = parent.getOwnerDocument().createElement("p");
		p2.setAttribute("id", "childidp2");
		parent.appendChild(p2);

		SelectorMatcher matcher = selectorMatcher(pre);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(p);
		assertEquals(-1, matcher.matches(selist));
		matcher = selectorMatcher(p2);
		int selidx = matcher.matches(selist);
		assertEquals(0, selidx);
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
		Element parent = createElement("div");
		parent.setAttribute("id", "div1");
		Element ul = parent.getOwnerDocument().createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		Element li = parent.getOwnerDocument().createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		Element p = parent.getOwnerDocument().createElement("p");
		li.appendChild(p);
		Element a = parent.getOwnerDocument().createElement("a");
		a.setAttribute("id", "a1");
		p.appendChild(a);
		Element a2 = parent.getOwnerDocument().createElement("a");
		a2.setAttribute("id", "a2");
		p.appendChild(a2);
		Element li2 = parent.getOwnerDocument().createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		Element a3 = parent.getOwnerDocument().createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);

		SelectorMatcher matcher = selectorMatcher(p);
		int selidx = matcher.matches(selist);
		assertEquals(-1, selidx);
		matcher = selectorMatcher(li);
		selidx = matcher.matches(selist);
		assertEquals(-1, selidx);
		matcher = selectorMatcher(a);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		matcher = selectorMatcher(a2);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		matcher = selectorMatcher(a3);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		Specificity sp = new Specificity(selist.item(selidx), matcher);
		assertEquals(0, sp.id_count);
		assertEquals(0, sp.attrib_classes_count);
		assertEquals(3, sp.names_pseudoelements_count);
	}

	@Test
	public void testMatchSelectorPseudoOfType() throws Exception {
		Element parent = createElement("div");
		parent.appendChild(plaindoc.createTextNode("foo"));
		parent.appendChild(plaindoc.createElement("div"));
		Element elm = plaindoc.createElement("p");
		parent.appendChild(elm);
		parent.appendChild(plaindoc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);

		BaseCSSStyleSheet css = parseStyle("p:first-of-type {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:first-of-type", selectorListToString(selist, rule));
		assertEquals(0, matcher.matches(selist));

		css = parseStyle("p:last-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:last-of-type", selectorListToString(selist, rule));
		assertEquals(0, matcher.matches(selist));
		Element lastdiv = plaindoc.createElement("div");
		parent.appendChild(lastdiv);
		assertEquals(0, matcher.matches(selist));
		Element lastp = plaindoc.createElement("P");
		parent.appendChild(lastp);
		assertEquals(-1, matcher.matches(selist));

		css = parseStyle("p:only-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:only-of-type", selectorListToString(selist, rule));
		assertEquals(-1, matcher.matches(selist));
		parent.removeChild(lastp);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNthOfType() throws Exception {
		Element parent = createElement("div");
		parent.appendChild(plaindoc.createTextNode("foo"));
		parent.appendChild(plaindoc.createElement("div"));
		Element elm = plaindoc.createElement("P");
		parent.appendChild(elm);
		parent.appendChild(plaindoc.createElement("div"));

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
		Element lastp = plaindoc.createElement("p");
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

	private Element createElement(String name) {
		Element elm = plaindoc.createElement(name);
		plaindoc.getDocumentElement().appendChild(elm);
		return elm;
	}

	private SelectorMatcher selectorMatcher(Element elm) {
		if (cssdoc == null) {
			TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
			cssFac.setLenientSystemValues(false);
			cssdoc = cssFac.createCSSDocument(plaindoc);
		}
		CSSElement csselm = (CSSElement) cssdoc.getCSSNode(elm);
		return new WrapperSelectorMatcher(csselm, elm);
	}

	public BaseCSSStyleSheet parseStyle(String style) throws CSSException, IOException {
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
