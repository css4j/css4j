/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.stylesheets.LinkStyle;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;

public class AbstractStyleDatabaseTest {

	private TestCSSStyleSheetFactory factory;
	private CSSDocument cssdoc;
	private Node styleText;
	private AbstractCSSStyleSheet sheet;

	@Before
	public void setUp() throws DOMException, ParserConfigurationException, CSSMediaException {
		factory = new TestCSSStyleSheetFactory();
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		Document doc = dbFac.newDocumentBuilder().getDOMImplementation().createDocument(null, "html", null);
		Element head = doc.createElement("head");
		Element style = doc.createElement("style");
		style.setAttribute("id", "styleId");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setAttribute("media", "screen");
		style.setTextContent(" ");
		doc.getDocumentElement().appendChild(head);
		head.appendChild(style);
		Element body = doc.createElement("body");
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		doc.getDocumentElement().appendChild(body);
		cssdoc = factory.createCSSDocument(doc);
		cssdoc.setTargetMedium("screen");
		CSSElement cssStyle = cssdoc.getElementById("styleId");
		sheet = (AbstractCSSStyleSheet) ((LinkStyle) cssStyle).getSheet();
		styleText = cssStyle.getChildNodes().item(0);
	}

	@Test
	public void testFontFaceRule() throws IOException {
		styleText.setNodeValue(
				"@font-face{font-family:'OpenSans Regular';src:url('http://www.example.com/fonts/OpenSans-Regular.ttf') format('truetype')}");
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals(
				"@font-face {font-family: 'OpenSans Regular'; src: url('http://www.example.com/fonts/OpenSans-Regular.ttf') format('truetype'); }",
				ffrule.getCssText());
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf') format('truetype')",
				ffrule.getStyle().getPropertyValue("src"));
		//
		CSSElement body = cssdoc.getElementById("bodyId");
		body.getComputedStyle(null);
		assertTrue(factory.getDeviceFactory().getStyleDatabase("screen").isFontFaceName("opensans regular"));
	}

	@Test
	public void testFontFaceRuleHttpContentType() throws IOException {
		styleText.setNodeValue(
				"@font-face{font-family:'OpenSans Regular';src:url('http://www.example.com/fonts/OpenSans-Regular.ttf')}");
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		assertEquals(
				"@font-face {font-family: 'OpenSans Regular'; src: url('http://www.example.com/fonts/OpenSans-Regular.ttf'); }",
				ffrule.getCssText());
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf')",
				ffrule.getStyle().getPropertyValue("src"));
		//
		factory.getConnectionFactory().setHeader("ttf", "content-type", "application/font-ttf");
		CSSElement body = cssdoc.getElementById("bodyId");
		body.getComputedStyle(null);
		assertTrue(factory.getDeviceFactory().getStyleDatabase("screen").isFontFaceName("opensans regular"));
	}

	@Test
	public void testFontFaceRuleEvil() throws IOException {
		cssdoc.setDocumentURI("http://www.example.com/example.html");
		styleText.setNodeValue(
				"@font-face{font-family:'Hack Sans';src:url('jar:http://www.example.com/evil.jar!/fakefont.ttf') format('truetype')}");
		FontFaceRule ffrule = (FontFaceRule) sheet.getCssRules().item(0);
		assertEquals(2, ffrule.getStyle().getLength());
		//
		CSSElement body = cssdoc.getElementById("bodyId");
		body.getComputedStyle(null);
		assertFalse(factory.getDeviceFactory().getStyleDatabase("screen").isFontFaceName("hack sans"));
		assertTrue(cssdoc.getErrorHandler().hasErrors());
		assertTrue(cssdoc.getErrorHandler().hasPolicyErrors());
	}

}
