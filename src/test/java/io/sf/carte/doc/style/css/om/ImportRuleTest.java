/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.MediaQueryList;

public class ImportRuleTest {

	private static TestCSSStyleSheetFactory factory;

	@BeforeAll
	public static void setUpBeforeClass() {
		factory = new TestCSSStyleSheetFactory();
	}

	@Test
	public void testGetStyleSheet() throws ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent(
				"@import 'http://www.example.com/css/default.css';p{margin-left:1em;}");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());
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
	public void testGetStyleSheetMedia() throws ParserConfigurationException, CSSMediaException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		doc.setDocumentURI("http://www.example.com/example.html");
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent(
				"@import 'http://www.example.com/css/alter2.css' screen;p{margin-left:1em;}");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		Element body = doc.createElement("body");
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		doc.getDocumentElement().appendChild(body);
		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());

		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		MediaQueryList mql = imp.getMedia();
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertEquals("screen", mql.getMedia());
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(1, list.getLength());
		assertEquals(CSSRule.STYLE_RULE, list.item(0).getType());
		assertEquals("@import url('http://www.example.com/css/alter2.css') screen; ",
				imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/alter2.css' screen;",
				imp.getMinifiedCssText());

		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		sheet.insertRule("@import url('css/default.css') supports(width:1px) (width>=400px)", 1);
		assertEquals(3, sheet.getCssRules().getLength());
		style.normalize();

		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertFalse(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());
		assertEquals(3, docsheet.getCssRules().getLength());

		// Rule 1
		AbstractCSSRule rule = docsheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mediaRule = (MediaRule) rule;
		assertEquals("screen", mediaRule.getMedia().getMedia());
		assertEquals(1, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleRule srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		CSSStyleDeclaration styleDecl = srule.getStyle();
		assertEquals(2, styleDecl.getLength());
		assertEquals("background-color", styleDecl.item(0));
		assertEquals("color", styleDecl.item(1));

		// Rule 2
		rule = docsheet.getCssRules().item(1);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		mediaRule = (MediaRule) rule;
		assertEquals("(width >= 400px)", mediaRule.getMedia().getMedia());
		assertEquals(1, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(9, styleDecl.getLength());
		assertEquals("olive", styleDecl.getPropertyValue("color"));

		// Rule 3
		rule = docsheet.getCssRules().item(2);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("p", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(1, styleDecl.getLength());
		assertEquals("margin-left", styleDecl.item(0));

		// Usage in computed style
		CSSElement cssBody = cssdoc.getElementById("bodyId");
		assertNotNull(cssBody);
		CSSComputedProperties gcs = cssBody.getComputedStyle(null);
		assertEquals(0, gcs.getLength());

		/*
		 * Target medium: screen
		 */
		cssdoc.setTargetMedium("screen");
		docsheet = cssdoc.getStyleSheet();
		assertEquals(3, docsheet.getCssRules().getLength());

		// Rule 1
		rule = docsheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		mediaRule = (MediaRule) rule;
		assertEquals("screen", mediaRule.getMedia().getMedia());
		assertEquals(1, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(2, styleDecl.getLength());
		assertEquals("background-color", styleDecl.item(0));
		assertEquals("color", styleDecl.item(1));

		// Rule 2
		rule = docsheet.getCssRules().item(1);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		mediaRule = (MediaRule) rule;
		assertEquals("(width >= 400px)", mediaRule.getMedia().getMedia());
		assertEquals(1, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(9, styleDecl.getLength());
		assertEquals("olive", styleDecl.getPropertyValue("color"));

		// Rule 3
		rule = docsheet.getCssRules().item(2);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("p", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(1, styleDecl.getLength());
		assertEquals("margin-left", styleDecl.item(0));

		// Usage in computed style
		gcs = cssBody.getComputedStyle(null);
		assertEquals(9, gcs.getLength());
		assertEquals("background-color", gcs.item(0));
		assertEquals("color", gcs.item(1));
	}

	@Test
	public void testGetStyleSheetMedia2() throws ParserConfigurationException, CSSMediaException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setAttribute("media", "screen");
		style.setTextContent(
				"@import 'http://www.example.com/css/alter2.css' screen;p{margin-left:1em;}");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		Element body = doc.createElement("body");
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		doc.getDocumentElement().appendChild(body);

		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(2, sheet.getCssRules().getLength());
		AbstractCSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.IMPORT_RULE, rule.getType());

		ImportRule imp = (ImportRule) rule;
		MediaQueryList mql = imp.getMedia();
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertEquals("screen", mql.getMedia());
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(1, list.getLength());
		assertEquals(CSSRule.STYLE_RULE, list.item(0).getType());
		assertEquals("@import url('http://www.example.com/css/alter2.css') screen; ",
				imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/alter2.css' screen;",
				imp.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());
		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertFalse(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());
		assertEquals(1, docsheet.getCssRules().getLength());

		// Rule 1
		rule = docsheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mediaRule = (MediaRule) rule;
		assertEquals("screen", mediaRule.getMedia().getMedia());
		assertEquals(2, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleRule srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		CSSStyleDeclaration styleDecl = srule.getStyle();
		assertEquals(2, styleDecl.getLength());
		assertEquals("background-color", styleDecl.item(0));
		assertEquals("color", styleDecl.item(1));

		// Rule 2
		rule = mediaRule.getCssRules().item(1);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("p", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(1, styleDecl.getLength());
		assertEquals("margin-left", styleDecl.item(0));

		// Usage in computed style
		CSSElement cssBody = cssdoc.getElementById("bodyId");
		assertNotNull(cssBody);
		CSSComputedProperties gcs = cssBody.getComputedStyle(null);
		assertEquals(0, gcs.getLength());

		/*
		 * Target medium: screen
		 */
		cssdoc.setTargetMedium("screen");
		docsheet = cssdoc.getStyleSheet();
		assertEquals(1, docsheet.getCssRules().getLength());

		// Rule 1
		rule = docsheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		mediaRule = (MediaRule) rule;
		assertEquals("screen", mediaRule.getMedia().getMedia());
		assertEquals(2, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(2, styleDecl.getLength());
		assertEquals("background-color", styleDecl.item(0));
		assertEquals("color", styleDecl.item(1));

		// Rule 2
		rule = mediaRule.getCssRules().item(1);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		srule = (CSSStyleRule) rule;
		assertEquals("p", srule.getSelectorText());
		styleDecl = srule.getStyle();
		assertEquals(1, styleDecl.getLength());
		assertEquals("margin-left", styleDecl.item(0));

		// Usage in computed style
		gcs = cssBody.getComputedStyle(null);
		assertEquals(2, gcs.getLength());
		assertEquals("background-color", gcs.item(0));
		assertEquals("color", gcs.item(1));
	}

	@Test
	public void testCircularDependency() throws DOMException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
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
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(2, list.getLength());
		assertEquals("@import url('http://www.example.com/css/circular.css'); ", imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/circular.css';",
				imp.getMinifiedCssText());

		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertTrue(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		assertFalse(imported.getErrorHandler().hasSacErrors());
		assertFalse(imported.getErrorHandler().hasSacWarnings());
		assertTrue(imported.getErrorHandler().hasOMErrors());
		assertFalse(imported.getErrorHandler().hasOMWarnings());

		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertTrue(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());
		assertFalse(cssdoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testCircularDependencyMedia() throws DOMException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent("@import 'http://www.example.com/css/circular.css' screen;");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);

		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(2, list.getLength());
		assertEquals("@import url('http://www.example.com/css/circular.css') screen; ",
				imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/circular.css' screen;",
				imp.getMinifiedCssText());

		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertTrue(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		assertFalse(imported.getErrorHandler().hasSacErrors());
		assertFalse(imported.getErrorHandler().hasSacWarnings());
		assertTrue(imported.getErrorHandler().hasOMErrors());
		assertFalse(imported.getErrorHandler().hasOMWarnings());

		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertTrue(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());
		assertFalse(cssdoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testDisallowedURL() throws DOMException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent("@import 'jar:http://www.example.com/evil.jar!/file';");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);

		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		assertFalse(cssdoc.getErrorHandler().hasErrors());
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(0, list.getLength());
		assertEquals("@import url('jar:http://www.example.com/evil.jar!/file'); ",
				imp.getCssText());
		assertEquals("@import 'jar:http://www.example.com/evil.jar!/file';",
				imp.getMinifiedCssText());

		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		assertFalse(imported.getErrorHandler().hasSacErrors());
		assertFalse(imported.getErrorHandler().hasSacWarnings());
		assertFalse(imported.getErrorHandler().hasOMErrors());
		assertFalse(imported.getErrorHandler().hasOMWarnings());

		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertFalse(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());

		assertFalse(cssdoc.getErrorHandler().hasIOErrors());
		assertFalse(cssdoc.getErrorHandler().hasMediaErrors());
		assertTrue(cssdoc.getErrorHandler().hasErrors());
		assertTrue(cssdoc.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testIOError() throws DOMException, ParserConfigurationException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent("@import 'http://www.example.com/css/nonexistent.css';");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);

		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(0, list.getLength());

		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		assertFalse(imported.getErrorHandler().hasSacErrors());
		assertFalse(imported.getErrorHandler().hasSacWarnings());
		assertFalse(imported.getErrorHandler().hasOMErrors());
		assertFalse(imported.getErrorHandler().hasOMWarnings());

		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertFalse(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());
		assertTrue(cssdoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testStandAloneIOError() throws DOMException, IOException {
		AbstractCSSStyleSheet sheet = factory.createMockStyleSheet(null, null,
				CSSStyleSheetFactory.ORIGIN_AUTHOR);
		sheet.parseStyleSheet(
				new StringReader("@import 'http://www.example.com/css/nonexistent.css';"));
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		assertEquals(0, imported.getCssRules().getLength());

		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		assertFalse(imported.getErrorHandler().hasSacErrors());
		assertFalse(imported.getErrorHandler().hasSacWarnings());
		assertFalse(imported.getErrorHandler().hasOMErrors());
		assertFalse(imported.getErrorHandler().hasOMWarnings());

		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());
		assertTrue(sheet.getDocumentErrorHandler().hasIOErrors());
	}

	/*
	 * Parse variant with explicit 'url()'
	 */
	@Test
	public void testParseVariant() throws DOMException, IOException {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"@import url('http://www.example.com/css/default.css');p{margin-left:1em;}");
		sheet.parseStyleSheet(re);
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());
		CSSRuleArrayList list = sheet.getCssRules();
		assertEquals(2, list.getLength());
		assertEquals(CSSRule.IMPORT_RULE, list.item(0).getType());
		ImportRule imp = (ImportRule) list.item(0);
		assertTrue(imp.getMedia().isAllMedia());
		assertEquals("@import url('http://www.example.com/css/default.css'); ", imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/default.css';", imp.getMinifiedCssText());
	}

	/*
	 * Parse variant with explicit 'url()' + media
	 */
	@Test
	public void testParseVariantMedia() throws DOMException, IOException {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		StringReader re = new StringReader(
				"@import url('http://www.example.com/css/default.css') screen and (min-width: 600px);p{margin-left:1em;}");
		sheet.parseStyleSheet(re);
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		CSSRuleArrayList list = sheet.getCssRules();
		assertEquals(2, list.getLength());
		assertEquals(CSSRule.IMPORT_RULE, list.item(0).getType());

		ImportRule imp = (ImportRule) list.item(0);
		assertFalse(imp.getMedia().isAllMedia());
		assertEquals("screen and (min-width: 600px)", imp.getMedia().getMedia());
		assertEquals(
				"@import url('http://www.example.com/css/default.css') screen and (min-width: 600px); ",
				imp.getCssText());
		assertEquals(
				"@import 'http://www.example.com/css/default.css' screen and (min-width:600px);",
				imp.getMinifiedCssText());
	}

	@Test
	public void testParseLayer() throws ParserConfigurationException, CSSMediaException {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null,
				"html", null);
		doc.setDocumentURI("http://www.example.com/css/");
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setAttribute("media", "screen");
		style.setTextContent(
				"@import 'alter2.css' layer(alter) supports(selector(col||td) and (color:#000)) (width<1200px);");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		Element body = doc.createElement("body");
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		doc.getDocumentElement().appendChild(body);

		StylableDocumentWrapper cssdoc = factory.createCSSDocument(doc);
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		assertNotNull(cssStyle);
		AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) cssStyle).getSheet();
		assertEquals(1, sheet.getCssRules().getLength());
		ImportRule imp = (ImportRule) sheet.getCssRules().item(0);
		MediaQueryList mql = imp.getMedia();
		assertNotNull(mql);
		assertFalse(mql.isAllMedia());
		assertFalse(mql.isNotAllMedia());
		assertEquals("(width < 1200px)", mql.getMedia());
		AbstractCSSStyleSheet imported = imp.getStyleSheet();
		assertNotNull(imported);
		CSSRuleArrayList list = imported.getCssRules();
		assertEquals(1, list.getLength());
		assertEquals(CSSRule.STYLE_RULE, list.item(0).getType());
		assertEquals(
				"@import url('alter2.css') layer(alter) supports(selector(col||td) and (color: #000)) (width < 1200px); ",
				imp.getCssText());
		assertEquals(
				"@import 'alter2.css' layer(alter) supports(selector(col||td) and (color:#000)) (width<1200px);",
				imp.getMinifiedCssText());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertFalse(sheet.getErrorHandler().hasSacWarnings());
		assertFalse(sheet.getErrorHandler().hasOMErrors());
		assertFalse(sheet.getErrorHandler().hasOMWarnings());

		DocumentCSSStyleSheet docsheet = cssdoc.getStyleSheet();
		assertFalse(docsheet.getErrorHandler().hasSacErrors());
		assertFalse(docsheet.getErrorHandler().hasSacWarnings());
		assertFalse(docsheet.getErrorHandler().hasOMErrors());
		assertFalse(docsheet.getErrorHandler().hasOMWarnings());
		assertEquals(1, docsheet.getCssRules().getLength());

		// Rule 1
		AbstractCSSRule rule = docsheet.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		MediaRule mediaRule = (MediaRule) rule;
		assertEquals("screen", mediaRule.getMedia().getMedia());
		assertEquals(1, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
		mediaRule = (MediaRule) rule;
		assertEquals("(width < 1200px)", mediaRule.getMedia().getMedia());
		assertEquals(1, mediaRule.getCssRules().getLength());
		rule = mediaRule.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleRule srule = (CSSStyleRule) rule;
		assertEquals("body", srule.getSelectorText());
		CSSStyleDeclaration styleDecl = srule.getStyle();
		assertEquals(2, styleDecl.getLength());
		assertEquals("background-color", styleDecl.item(0));
		assertEquals("color", styleDecl.item(1));
	}

	@Test
	public void testCreateImportRule() {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		MediaQueryList mql = factory.createMediaQueryList("screen", null);
		ImportRule imp = sheet.createImportRule(mql, "http://www.example.com/css/sheet.css");
		assertFalse(imp.getMedia().isAllMedia());
		assertEquals("screen", imp.getMedia().getMedia());
		assertEquals("http://www.example.com/css/sheet.css", imp.getHref());
		assertEquals("@import url('http://www.example.com/css/sheet.css') screen; ",
				imp.getCssText());
		assertEquals("@import 'http://www.example.com/css/sheet.css' screen;",
				imp.getMinifiedCssText());
	}

	@Test
	public void testEquals() {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		MediaQueryList mql = MediaFactory.createAllMedia();
		ImportRule imp = sheet.createImportRule(mql, "http://www.example.com/css/foo.css");
		ImportRule imp2 = sheet.createImportRule(mql, "http://www.example.com/css/foo.css");
		assertTrue(imp.equals(imp2));
		assertTrue(imp.hashCode() == imp2.hashCode());
		imp2 = sheet.createImportRule(mql, "http://www.example.com/css/bar.css");
		assertFalse(imp.equals(imp2));
		mql = new MediaQueryListImpl("screen");
		imp2 = sheet.createImportRule(mql, "http://www.example.com/css/foo.css");
		assertFalse(imp.equals(imp2));
	}

	@Test
	public void testCloneAbstractCSSStyleSheet() {
		AbstractCSSStyleSheet sheet = factory.createStyleSheet(null, null);
		MediaQueryList mql = MediaFactory.createAllMedia();
		ImportRule rule = sheet.createImportRule(mql, "http://www.example.com/css/foo.css");
		AbstractCSSStyleSheet newSheet = sheet.getStyleSheetFactory().createStyleSheet(null, null);
		ImportRule cloned = rule.clone(newSheet);
		assertFalse(rule == cloned);
		assertEquals(rule.getHref(), cloned.getHref());
		assertEquals(rule.getMedia().getMedia(), cloned.getMedia().getMedia());
		assertTrue(rule.equals(cloned));
		assertEquals(rule.hashCode(), cloned.hashCode());
	}

}
