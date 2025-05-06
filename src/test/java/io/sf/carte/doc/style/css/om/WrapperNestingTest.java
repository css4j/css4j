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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class WrapperNestingTest {

	static Document refXhtmlDoc;

	private StylableDocumentWrapper xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass()
			throws IOException, SAXException, ParserConfigurationException {
		InputStream is = WrapperNestingTest.class
				.getResourceAsStream("/io/sf/carte/doc/agent/nesting.html");
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		docb.setEntityResolver(new DefaultEntityResolver());
		InputSource source = new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8));
		refXhtmlDoc = docb.parse(source);
		is.close();
		refXhtmlDoc.getDocumentElement().normalize();
	}

	@BeforeEach
	public void setUp() {
		Document doc = (Document) refXhtmlDoc.cloneNode(true);
		doc.setDocumentURI("http://www.example.com/xhtml/nesting.html");
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		xhtmlDoc = cssFac.createCSSDocument(doc);
	}

	@Test
	public void getStyleSheet() {
		DocumentCSSStyleSheet defsheet = xhtmlDoc.getStyleSheetFactory()
				.getDefaultStyleSheet(xhtmlDoc.getComplianceMode());
		assertNotNull(defsheet);
		// Obtain the number of rules in the default style sheet, to use it
		// as a baseline.
		int defSz = defsheet.getCssRules().getLength();
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xhtmlDoc.embeddedStyle.size() + xhtmlDoc.linkedStyle.size();
		assertEquals(2, countInternalSheets);
		assertEquals(2, xhtmlDoc.getStyleSheets().getLength());
		assertEquals(0, xhtmlDoc.getStyleSheetSets().getLength());
		assertFalse(xhtmlDoc.hasStyleIssues());

		Iterator<AbstractCSSStyleSheet> it = xhtmlDoc.getStyleSheets().iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next();
		assertNotNull(sheet);
		assertEquals(13, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals("margin-right: 1%; ",
				((StyleRule) sheet.getCssRules().item(4)).getStyle().getCssText());

		assertEquals(defSz + 16, css.getCssRules().getLength());
		assertFalse(xhtmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void testStyleUL() throws CSSMediaException {
		xhtmlDoc.setTargetMedium("screen");
		CSSElement elm = xhtmlDoc.getElementById("ul1");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getComputedStyle(null);
		assertEquals("margin-left: 18px; ", style.getCssText());
		assertEquals(1, style.getLength());

		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testCascade() throws IOException {
		BaseCSSStyleSheet sheet = (BaseCSSStyleSheet) xhtmlDoc.getStyleSheets().item(1);

		// Obtain the rule where a value is declared
		CSSParser parser = new CSSParser();
		SelectorList selist = parser.parseSelectors("g.label:nth-last-child(2)");
		StyleRule rule = sheet.getFirstStyleRule(selist);
		assertNotNull(rule);

		AbstractCSSStyleDeclaration declStyle = rule.getStyle();
		StyleValue declMarginLeft = declStyle.getPropertyCSSValue("background-color");
		assertEquals("#eec", declMarginLeft.getCssText());

		/*
		 * Get an element that obtains the above value as computed style
		 */
		//CSSElement elm = xhtmlDoc.getElementById("foAspan");
		CSSElement elm = (CSSElement) xhtmlDoc.getElementsByTagName("g").item(1);
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getComputedStyle(null);
		assertEquals("#eec", style.getPropertyValue("background-color"));

		// Overwrite the property's value
		declStyle.setProperty("background-color", "#0ff", null);

		style = elm.getComputedStyle(null);
		// The new value is not there yet
		assertEquals("#eec", style.getPropertyValue("background-color"));

		// Rebuild the cascade
		xhtmlDoc.rebuildCascade();
		style = elm.getComputedStyle(null);
		assertEquals("#0ff", style.getPropertyValue("background-color"));

		// Test STYLE element normalization
		CSSElement styleEl = (CSSElement) rule.getParentStyleSheet().getOwnerNode();
		String cssText = styleEl.getTextContent();
		assertTrue(cssText.contains("#eec"));
		assertFalse(cssText.contains("#0ff"));
		styleEl.normalize();
		cssText = styleEl.getTextContent();
		assertTrue(cssText.contains("#0ff"));
		assertFalse(cssText.contains("#eec"));
	}

	@Test
	public void testVisitors() throws IOException {
		StyleCountVisitor visitor = new StyleCountVisitor();
		xhtmlDoc.getStyleSheets().acceptStyleRuleVisitor(visitor);
		assertEquals(38, visitor.getCount());

		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		xhtmlDoc.getStyleSheets().acceptDeclarationRuleVisitor(visitorP);
		assertEquals(125, visitorP.getCount());

		visitorP.reset();
		xhtmlDoc.getStyleSheets().acceptDescriptorRuleVisitor(visitorP);
		assertEquals(0, visitorP.getCount());
	}

}
