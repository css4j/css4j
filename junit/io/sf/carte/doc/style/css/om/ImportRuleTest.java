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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleDefiner;

public class ImportRuleTest {

	static {
		TestCSSStyleSheetFactory.setTestSACParser();
	}

	private static TestCSSStyleSheetFactory factory;

	@BeforeClass
	public static void setUpBeforeClass() {
		factory = new TestCSSStyleSheetFactory();
	}

	@Test
	public void testGetStyleSheet() throws ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent("@import 'http://www.example.com/css/default.css';");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		MediaQueryList mql = imp.getMedia();
		assertNotNull(mql);
		assertTrue(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(1, list.getLength());
		assertEquals(CSSRule.STYLE_RULE, list.item(0).getType());
		assertEquals("@import url('http://www.example.com/css/default.css'); ", imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/default.css';", imp.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());
	}

	@Test
	public void testCircularDependency() throws DOMException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent("@import 'http://www.example.com/css/circular.css';");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(0, list.getLength());
		assertEquals("@import url('http://www.example.com/css/circular.css'); ", imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/circular.css';", imp.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertTrue(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());
	}

	@Test
	public void testEquals() {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		MediaQueryList mql = MediaQueryFactory.createMediaList("all", null);
		ImportRule imp = sheet.createCSSImportRule(mql, "http://www.example.com/css/foo.css");
		ImportRule imp2 = sheet.createCSSImportRule(mql, "http://www.example.com/css/foo.css");
		assertTrue(imp.equals(imp2));
		assertTrue(imp.hashCode() == imp2.hashCode());
		imp2 = sheet.createCSSImportRule(mql, "http://www.example.com/css/bar.css");
		assertFalse(imp.equals(imp2));
		mql = MediaQueryFactory.createMediaList("screen", null);
		imp2 = sheet.createCSSImportRule(mql, "http://www.example.com/css/foo.css");
		assertFalse(imp.equals(imp2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		MediaQueryList mql = MediaQueryFactory.createMediaList("all", null);
		ImportRule rule = sheet.createCSSImportRule(mql, "http://www.example.com/css/foo.css");
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null,
				null);
		ImportRule cloned = rule.clone(newSheet);
		assertFalse(rule == cloned);
		assertEquals(rule.getHref(), cloned.getHref());
		assertEquals(rule.getMedia().getMediaText(), cloned.getMedia().getMediaText());
		assertTrue(rule.equals(cloned));
		assertEquals(rule.hashCode(), cloned.hashCode());
	}

}
