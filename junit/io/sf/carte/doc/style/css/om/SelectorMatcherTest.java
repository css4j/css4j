/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.TestDOMImplementation;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory.DummyCanvas;

public class SelectorMatcherTest {

	private Parser cssParser;

	private static CSSDocument doc;

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
		TestDOMImplementation impl = new TestDOMImplementation(false);
		DocumentType doctype = null;
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			doctype = impl.createDocumentType("html", null, null);
		}
		return impl.createDocument(null, "html", doctype);
	}

	@Test
	public void testMatchSelectorUniversal() throws Exception {
		BaseCSSStyleSheet css = parseStyle("* {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("*", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Element() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p", selectorListToString(selist, rule));
		CSSElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		elm = createElement("div");
		assertFalse(elm.matches(selist, null));
	}

	@Test
	public void testMatchSelector1ElementNS() throws Exception {
		BaseCSSStyleSheet css = parseStyle(
				"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();
		SelectorList svgselist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		CSSElement svg = doc.createElementNS("http://www.w3.org/2000/svg", "svg");
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		SelectorMatcher svgmatcher = selectorMatcher(svg);
		assertTrue(matcher.matches(selist) >= 0);
		assertTrue(svgmatcher.matches(selist) == -1);
		assertTrue(matcher.matches(svgselist) == -1);
		assertTrue(svgmatcher.matches(svgselist) >= 0);
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
		BaseCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title]", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test // SAC 1.3 cannot support this
	public void testMatchSelector2AttributeCI() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title='hi' i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HI");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title~=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title~='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[lang|=\"en\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[lang|='en']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("lang", "en-US");
		assertTrue(matcher.matches(selist) >= 0);
		elm.setAttribute("lang", "en");
		assertTrue(matcher.matches(selist) >= 0);
		elm.setAttribute("lang", "en_US");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector5Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title^=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title^='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi ho");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector6Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title$=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title$='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector7Attribute() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p[title*=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p[title*='hi']", selectorListToString(selist, rule));
		Element elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi ho");
		assertTrue(matcher.matches(selist) >= 0);
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
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
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
		assertTrue(matcher.matches(selist) >= 0);
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
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		// STRICT
		setUpWithMode(CSSDocument.ComplianceMode.STRICT);
		css = parseStyle(".exampleclass {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		elm = createElement("p");
		elm.setAttribute("class", "exampleClass");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorMultipleClass() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		Element elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		// STRICT
		setUpWithMode(CSSDocument.ComplianceMode.STRICT);
		css = parseStyle(".exampleclass {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(".exampleclass", selectorListToString(selist, rule));
		elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle("z.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("z.exampleclass", selectorListToString(selist, rule));
		Element elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector3Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(".exampleclass span", selectorListToString(selist, rule));
		Element parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector5Class() throws Exception {
		BaseCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createElement("p");
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
		Element parent = createElement("p");
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		elm.setAttribute("class", "firstclass secondclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.secondclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		elm.setAttribute("class", "firstclass secondclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		elm.setAttribute("class", " firstclass");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4MultipleClasses() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element elm = createElement("p");
		elm.setAttribute("class", "firstclass ");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Firstchild() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p:first-child", selectorListToString(selist, rule));
		Element parent = createElement("div");
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
		assertTrue(matcher.matches(selist) >= 0);
		matcher = new DOMSelectorMatcher((CSSElement) doc.getCSSNode(elm));
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector1Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid", selectorListToString(selist, rule));
		Element elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1IdStrict() throws Exception {
		// QUIRKS
		BaseCSSStyleSheet css = parseStyle("#exampleID {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleID", selectorListToString(selist, rule));
		Element elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
		elm = createElement("p");
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
		elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
		elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleID");
		matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid span", selectorListToString(selist, rule));
		Element parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2aId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createElement("p");
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
		Element parent = createElement("p");
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3Id() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("#exampleid>span", selectorListToString(selist, rule));
		Element parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3bId() throws Exception {
		BaseCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createElement("p");
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
	public void testMatchSelectorAdjacentSibling() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass + p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass + p", selectorListToString(selist, rule));
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorSubsequentSibling() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass ~ p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass~p", selectorListToString(selist, rule));
		CSSElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		doc.getDocumentElement().appendChild(parent);
		parent.appendChild(doc.createElement("pre"));
		CSSElement elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "childidp1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("pre"));
		CSSElement pre = doc.createElement("pre");
		parent.appendChild(pre);
		elm = parent.getOwnerDocument().createElement("p");
		elm.setAttribute("id", "childidp2");
		parent.appendChild(elm);
		assertTrue(elm.matches(selist, null));
		assertFalse(pre.matches(selist, null));
	}

	@Test
	public void testMatchSelectorDescendant() throws Exception {
		BaseCSSStyleSheet css = parseStyle("ul li a{padding:20px}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("ul li a", selectorListToString(selist, rule));
		CSSElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		doc.getDocumentElement().appendChild(parent);
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
		CSSElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		doc.getDocumentElement().appendChild(parent);
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
		CSSElement span = parent.getOwnerDocument().createElement("span");
		a3.appendChild(span);
		assertFalse(span.matches(selist, null));
	}

	@Test
	public void testMatchSelectorPseudoClass1() throws Exception {
		Element parent = doc.createElement("div");
		Element elm = doc.createElement("p");
		doc.getDocumentElement().appendChild(parent);
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClass2() throws Exception {
		Element parent = doc.createElement("div");
		Element elm = doc.createElement("p");
		doc.getDocumentElement().appendChild(parent);
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);
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
		Element parent = doc.createElement("div");
		Element elm = doc.createElement("p");
		doc.getDocumentElement().appendChild(parent);
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoNthSelector() throws Exception {
		Element parent = doc.createElement("div");
		Element elm = doc.createElement("p");
		doc.getDocumentElement().appendChild(parent);
		parent.appendChild(doc.createTextNode("foo"));
		parent.appendChild(doc.createElement("div"));
		parent.appendChild(elm);
		parent.appendChild(doc.createElement("div"));
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("p:nth-child(1 of p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);
		css = parseStyle("p:nth-last-child(1 of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(1 of p)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		CSSElement lastp = doc.createElement("p");
		parent.appendChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("p:nth-last-child(2 of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals("p:nth-last-child(2 of p)", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		parent.removeChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoOfType() throws Exception {
		Element parent = doc.createElement("div");
		Element elm = doc.createElement("p");
		doc.getDocumentElement().appendChild(parent);
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoNthOfType() throws Exception {
		Element parent = doc.createElement("div");
		Element elm = doc.createElement("p");
		doc.getDocumentElement().appendChild(parent);
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
		assertTrue(matcher.matches(selist) >= 0);
		parent.removeChild(lastp);
		assertTrue(matcher.matches(selist) < 0);
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
		assertTrue(matcher.matches(selist) >= 0);
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
		assertTrue(matcher.matches(selist) >= 0);
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
		assertTrue(matcher.matches(selist) >= 0);
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
		assertTrue(matcher.matches(selist) >= 0);
		div.appendChild(doc.createTextNode("foo"));
		matcher = selectorMatcher(div);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoHas1() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:has(> img) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		Element parent = createElement("p");
		parent.setAttribute("id", "p1");
		Element elm = parent.getOwnerDocument().createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		Element elm2 = parent.getOwnerDocument().createElement("img");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertTrue(matcher.matches(selist) < 0);
		parent.setAttribute("class", "exampleclass");
		assertTrue(matcher.matches(selist) >= 0);
		assertEquals("p.exampleclass:has(>img)", selectorListToString(selist, rule));
	}

	@Test
	public void testMatchSelectorPseudoHas2() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:has(+ p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		if (rule != null) {
			SelectorList selist = rule.getSelectorList();
			assertEquals("p.exampleclass:has( + p)", selectorListToString(selist, rule));
			Element parent = createElement("div");
			parent.setAttribute("id", "div1");
			Element elm = parent.getOwnerDocument().createElement("p");
			elm.setAttribute("id", "childid1");
			parent.appendChild(elm);
			Element elm2 = parent.getOwnerDocument().createElement("p");
			elm2.setAttribute("id", "childid2");
			parent.appendChild(elm2);
			SelectorMatcher matcher = selectorMatcher(elm);
			assertTrue(matcher.matches(selist) < 0);
			elm.setAttribute("class", "exampleclass");
			assertTrue(matcher.matches(selist) >= 0);
		}
	}

	@Test
	public void testMatchSelectorPseudoIs() throws Exception {
		BaseCSSStyleSheet css = parseStyle(":is(.exampleclass span, div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		if (rule != null) {
			SelectorList selist = rule.getSelectorList();
			assertEquals(":is(.exampleclass span,div>span)", selectorListToString(selist, rule));
			Element parent = createElement("p");
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
			assertTrue(matcher.matches(selist) >= 0);
		}
	}

	@Test
	public void testMatchSelectorPseudoNot() throws Exception {
		BaseCSSStyleSheet css = parseStyle("p.exampleclass:not(:last-child) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass:not(:last-child)", selectorListToString(selist, rule));
		Element parent = createElement("div");
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
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClassEnabledDisabled() throws Exception {
		Element parent = doc.createElement("form");
		Element elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		doc.getDocumentElement().appendChild(parent);
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
		assertTrue(matcher.matches(selist) >= 0);
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassReadWriteReadOnly() throws Exception {
		Element parent = doc.createElement("form");
		Element elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		doc.getDocumentElement().appendChild(parent);
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
		assertTrue(matcher.matches(selist) >= 0);
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
		assertTrue(divmatcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClassPlaceholderShown() throws Exception {
		Element parent = doc.createElement("form");
		Element elm = doc.createElement("input");
		elm.setAttribute("type", "text");
		doc.getDocumentElement().appendChild(parent);
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
		Element parent = doc.createElement("form");
		Element elm = doc.createElement("input");
		elm.setAttribute("type", "submit");
		doc.getDocumentElement().appendChild(parent);
		parent.appendChild(button);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:default", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		button.removeAttribute("disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassChecked() throws Exception {
		Element parent = doc.createElement("form");
		Element elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		elm.setAttribute("checked", "checked");
		doc.getDocumentElement().appendChild(parent);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:checked {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:checked", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
		elm.removeAttribute("checked");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassIndeterminate() throws Exception {
		Element parent = doc.createElement("form");
		Element elm = doc.createElement("input");
		elm.setAttribute("type", "checkbox");
		elm.setAttribute("indeterminate", "true");
		doc.getDocumentElement().appendChild(parent);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		BaseCSSStyleSheet css = parseStyle("input:indeterminate {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("input:indeterminate", selectorListToString(selist, rule));
		assertTrue(matcher.matches(selist) >= 0);
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

	static CSSElement createElement(String name) throws ParserConfigurationException {
		CSSElement elm = doc.createElement(name);
		doc.getDocumentElement().appendChild(elm);
		return elm;
	}

	static SelectorMatcher selectorMatcher(Element elm) {
		return new DOMSelectorMatcher((CSSElement) elm);
	}

	public BaseCSSStyleSheet parseStyle(String style) throws CSSException, IOException {
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
