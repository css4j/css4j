/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class StylableDocumentWrapperTest2 {

	private DocumentBuilder docbuilder;

	@Before
	public void setUp() throws IOException, SAXException, ParserConfigurationException {
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		docbuilder = dbFac.newDocumentBuilder();
	}

	@Test
	public void testBaseAttribute() {
		Document document = docbuilder.newDocument();
		Element element = document.createElement("foo");
		document.appendChild(element);
		element.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", "http://www.example.com/");
		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		assertEquals("http://www.example.com/", element.getAttribute("xml:base"));
		assertEquals("http://www.example.com/", wrapped.getBaseURI());
		Attr attr = element.getAttributeNode("xml:base");
		assertNotNull(attr);
		attr.setValue("jar:http://www.example.com/evil.jar!/file");
		assertNull(wrapped.getBaseURI());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
		//
		wrapped.getErrorHandler().reset();
		wrapped.setDocumentURI("http://www.example.com/foo.html");
		assertEquals("http://www.example.com/foo.html", wrapped.getBaseURI());
		assertEquals("jar:http://www.example.com/evil.jar!/file", attr.getValue());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
		attr.setValue("http://www.example.com/");
		assertEquals("http://www.example.com/", wrapped.getBaseURI());
		attr.setValue("jar:http://www.example.com/evil.jar!/file");
		assertEquals("http://www.example.com/foo.html", wrapped.getBaseURI());
		//
		wrapped.getErrorHandler().reset();
		attr.setValue("file:/dev/zero");
		assertEquals("http://www.example.com/foo.html", wrapped.getBaseURI());
		assertEquals("file:/dev/zero", attr.getValue());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
	}

	@SuppressWarnings("unchecked")
	@Test (timeout=8000)
	public void testLinkElement() {
		Document document = docbuilder.newDocument();
		Element docElement = document.createElement("html");
		document.appendChild(docElement);
		Element head = document.createElement("head");
		docElement.appendChild(head);
		Element element = document.createElement("link");
		head.appendChild(element);
		element.setAttribute("id", "linkId");
		element.setAttribute("rel", "stylesheet");
		element.setAttribute("href", "http://www.example.com/css/common.css");
		element.setIdAttribute("id", true);
		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		CSSElement link = wrapped.getElementById("linkId");
		Attr href = link.getAttributeNode("href");
		assertNotNull(href);
		//
		CSSStyleSheet<AbstractCSSRule> sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(3, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertEquals("http://www.example.com/css/common.css", href.getValue());
		assertFalse(wrapped.getErrorHandler().hasErrors());
		assertFalse(wrapped.getErrorHandler().hasPolicyErrors());
		//
		href.setValue("file:/dev/zero");
		sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertEquals("file:/dev/zero", href.getValue());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
		//
		wrapped.getErrorHandler().reset();
		href.setValue("jar:http://www.example.com/evil.jar!/file");
		sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testMetaElementDefaultStyle() throws SAXException, IOException {
		InputStream is = DOMCSSStyleSheetFactoryTest.sampleHTMLStream();
		Document doc;
		try {
			docbuilder.setEntityResolver(new DefaultEntityResolver());
			InputSource source = new InputSource(is);
			doc = docbuilder.parse(source);
		} finally {
			is.close();
		}
		doc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		Node head = doc.getElementsByTagName("head").item(0);
		Element meta = doc.createElement("meta");
		meta.setAttribute("http-equiv", "Default-Style");
		meta.setAttribute("content", "Alter 2");
		head.appendChild(meta);
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(doc);
		assertEquals("Alter 2", wrapped.getSelectedStyleSheetSet());
	}

	@Test
	public void testFontIOError() throws SAXException, IOException {
		InputStream is = DOMCSSStyleSheetFactoryTest.sampleHTMLStream();
		Document doc;
		try {
			docbuilder.setEntityResolver(new DefaultEntityResolver());
			InputSource source = new InputSource(is);
			doc = docbuilder.parse(source);
		} finally {
			is.close();
		}
		doc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		Node head = doc.getElementsByTagName("head").item(0);
		Element style = doc.createElement("style");
		style.setAttribute("type", "text/css");
		style.setTextContent("@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf');}");
		head.appendChild(style);
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(doc);
		CSSElement elm = wrapped.getElementById("firstH3");
		assertNotNull(elm);
		elm.getComputedStyle(null);
		ErrorHandler errHandler = wrapped.getErrorHandler();
		assertNotNull(errHandler);
		assertTrue(errHandler.hasIOErrors());
		assertTrue(errHandler.hasErrors());
	}

}
