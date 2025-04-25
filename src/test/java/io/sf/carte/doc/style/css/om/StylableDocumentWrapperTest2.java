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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapper.LinkStyleProcessingInstruction;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class StylableDocumentWrapperTest2 {

	private static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

	private static DocumentBuilder docbuilder;

	@BeforeAll
	public static void setUpBeforeClass()
			throws IOException, SAXException, ParserConfigurationException {
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		docbuilder = dbFac.newDocumentBuilder();
	}

	@Test
	public void testElement() {
		Document document = docbuilder.newDocument();
		Element element = document.createElement("html");
		document.appendChild(element);
		Element baseElm = document.createElement("base");
		element.appendChild(baseElm);

		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		CSSElement docElm = wrapped.getDocumentElement();
		assertNotNull(docElm);
		assertEquals("html", docElm.getTagName());

		docElm.setAttribute("lang", "en");
		assertEquals("en", docElm.getAttribute("lang"));
		assertNotNull(docElm.getAttributeNode("lang"));

		docElm.setAttributeNS(XML_NAMESPACE_URI, "xml:base", "http://www.example.com/");
		assertEquals("http://www.example.com/", docElm.getAttribute("xml:base"));
		assertEquals("http://www.example.com/", docElm.getAttributeNS(XML_NAMESPACE_URI, "base"));
		assertNotNull(docElm.getAttributeNode("xml:base"));

		assertEquals("http://www.example.com/", wrapped.getBaseURI());

		docElm.removeAttribute("lang");
		assertFalse(docElm.hasAttribute("lang"));

		// Nothing happens
		docElm.removeAttribute("lang");

		docElm.removeAttributeNS(XML_NAMESPACE_URI, "base");
		assertFalse(docElm.hasAttributeNS(XML_NAMESPACE_URI, "base"));

		// Nothing happens
		docElm.removeAttributeNS(XML_NAMESPACE_URI, "base");

		assertNull(wrapped.getBaseURI());

		CSSElement wbase = (CSSElement) wrapped.getElementsByTagName("base").item(0);
		assertNotNull(wbase);

		wbase.setAttribute("HREF", "https://www.example.net/base/");

		assertEquals("https://www.example.net/base/", wrapped.getBaseURI());

		wbase.removeAttribute("HREF");
		assertFalse(wbase.hasAttribute("HREF"));

		wbase.setAttribute("HreF", "https://www.example.org/newbase/");

		assertEquals("https://www.example.org/newbase/", wrapped.getBaseURI());

		wbase.removeAttribute("HreF");

		wrapped.setDocumentURI("https://www.example.com/foo/");
		wbase.setAttribute("Href", "/base/");

		assertEquals("https://www.example.com/base/", wrapped.getBaseURI());

		assertTrue(wbase.hasAttribute("Href"));
		wbase.removeAttribute("Href");
		assertFalse(wbase.hasAttribute("Href"));

		/*
		 * Attributes
		 */
		docElm.setAttribute("class", "foo");
		assertTrue(docElm.hasAttribute("class"));
		final Attr attr = docElm.getAttributeNode("class");
		assertNotNull(attr);
		assertEquals("class", attr.getName());
		assertEquals("foo", attr.getValue());
		assertEquals("class=\"foo\"", attr.toString());
		assertSame(attr, docElm.removeAttributeNode(attr));
		assertFalse(docElm.hasAttribute("class"));

		DOMException ex = assertThrows(DOMException.class, () -> docElm.removeAttributeNode(attr));
		assertEquals(DOMException.NOT_FOUND_ERR, ex.code);

		Attr title = wrapped.createAttribute("title");
		assertSame(title, docElm.setAttributeNode(title));
		assertTrue(docElm.hasAttribute("title"));

		Attr base = wrapped.createAttributeNS(XML_NAMESPACE_URI, "base");
		assertSame(base, docElm.setAttributeNodeNS(base));
		assertTrue(docElm.hasAttributeNS(XML_NAMESPACE_URI, "base"));
		assertSame(base, docElm.getAttributeNodeNS(XML_NAMESPACE_URI, "base"));
	}

	@Test
	public void testBaseAttribute() {
		Document document = docbuilder.newDocument();
		Element element = document.createElement("foo");
		document.appendChild(element);
		element.setAttributeNS(XML_NAMESPACE_URI, "xml:base", "http://www.example.com/");

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

		wrapped.getErrorHandler().reset();
		attr.setValue("file:/dev/zero");
		assertEquals("http://www.example.com/foo.html", wrapped.getBaseURI());
		assertEquals("file:/dev/zero", attr.getValue());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testBaseAttribute2() {
		Document document = docbuilder.newDocument();
		Element element = document.createElement("foo");
		document.appendChild(element);
		element.setAttributeNS(XML_NAMESPACE_URI, "xml:base", "http:\\www.example.com/");

		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		assertEquals("http:\\www.example.com/", element.getAttribute("xml:base"));
		assertNull(wrapped.getBaseURI());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		wrapped.getErrorHandler().reset();

		Attr attr = element.getAttributeNode("xml:base");
		assertNotNull(attr);

		attr.setValue("");
		assertNull(wrapped.getBaseURI());
		assertFalse(wrapped.getErrorHandler().hasErrors());
		wrapped.getErrorHandler().reset();

		attr.setValue("/bar/");
		assertNull(wrapped.getBaseURI());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasNodeErrors());
		wrapped.getErrorHandler().reset();

		wrapped.setDocumentURI("http://www.example.com/foo.html");
		assertEquals("http://www.example.com/bar/", wrapped.getBaseURI());
		assertFalse(wrapped.getErrorHandler().hasErrors());
		wrapped.getErrorHandler().reset();

		wrapped.setDocumentURI("http:\\www.example.com/foo.html");
		assertNull(wrapped.getBaseURI());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasNodeErrors());
		wrapped.getErrorHandler().reset();
	}

	@Test
	public void testBaseAttributeNoBase() {
		Document document = docbuilder.newDocument();
		Element element = document.createElement("foo");
		document.appendChild(element);

		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		assertEquals(0, element.getAttribute("xml:base").length());
		assertNull(wrapped.getBaseURI());
		assertNull(element.getAttributeNode("xml:base"));
	}

	@Test
	public void testBaseAttributeQueryString() {
		Document document = docbuilder.newDocument();
		Element element = document.createElement("foo");
		document.appendChild(element);
		element.setAttributeNS(XML_NAMESPACE_URI, "xml:base",
				"http://www.example.com/base?p=a&q=b");
		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		CSSElement docElm = wrapped.getDocumentElement();
		assertNotNull(docElm);
		assertEquals("foo", docElm.getTagName());
		assertEquals("http://www.example.com/base?p=a&q=b", docElm.getAttribute("xml:base"));
		Attr baseAttr = docElm.getAttributeNode("xml:base");
		assertNotNull(baseAttr);
		assertEquals("xml:base=\"http://www.example.com/base?p=a&amp;q=b\"", baseAttr.toString());
	}

	@SuppressWarnings("unchecked")
	@Timeout(value = 8000, unit = TimeUnit.MILLISECONDS)
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

		Attr iattr = element.getAttributeNode("href");
		assertNotNull(iattr);
		Attr iattr2 = element.getAttributeNodeNS(null, "href");
		assertSame(iattr, iattr2);

		// Wrap
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		CSSElement link = wrapped.getElementById("linkId");
		Attr href = link.getAttributeNode("href");
		assertNotNull(href);

		Attr attr = link.getAttributeNodeNS(null, "href");
		assertSame(href, attr);

		CSSStyleSheet<AbstractCSSRule> sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(3, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertEquals("http://www.example.com/css/common.css", href.getValue());
		assertFalse(wrapped.getErrorHandler().hasErrors());
		assertFalse(wrapped.getErrorHandler().hasPolicyErrors());

		href.setValue("file:/dev/zero");
		sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertEquals("file:/dev/zero", href.getValue());
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());

		wrapped.getErrorHandler().reset();
		href.setValue("jar:http://www.example.com/evil.jar!/file");
		sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());

		wrapped.getErrorHandler().reset();
		href.setValue("http://www.example.com/etc/fakepasswd");
		sheet = ((LinkStyle<AbstractCSSRule>) link).getSheet();
		assertNull(sheet);
		assertTrue(wrapped.getErrorHandler().hasErrors());
		assertTrue(wrapped.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testMetaElementDefaultStyle() throws SAXException, IOException {
		InputStream is = SampleCSS.sampleHTMLStream();
		Document doc;
		try {
			docbuilder.setEntityResolver(new DefaultEntityResolver());
			InputSource source = new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8));
			doc = docbuilder.parse(source);
		} finally {
			is.close();
		}
		doc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		Node head = doc.getElementsByTagName("head").item(0);
		Element meta = doc.createElement("meta");
		meta.setAttribute("id", "defStyle");
		meta.setIdAttribute("id", true);
		meta.setAttribute("http-equiv", "Default-Style");
		meta.setAttribute("content", "Alter 2");
		head.appendChild(meta);
		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(doc);
		assertEquals("Alter 2", wrapped.getSelectedStyleSheetSet());

		CSSElement wrappedMeta = wrapped.getElementById("defStyle");
		Attr content = wrappedMeta.getAttributeNode("content");
		content.setValue("Alter 1");
		assertEquals("Alter 1", wrapped.getSelectedStyleSheetSet());

		Attr httpEquiv = wrappedMeta.getAttributeNode("http-equiv");
		httpEquiv.setValue("foo");
		assertEquals("Default", wrapped.getSelectedStyleSheetSet());
	}

	@Test
	public void testStyleProcessingInstruction() {
		Document document = docbuilder.newDocument();
		ProcessingInstruction pi = document.createProcessingInstruction("xml-stylesheet",
				"title=\"a&amp;&lt;&quot;&gt;e&apos;z\" type=\"text/css\" href=\"style.css\"");
		document.appendChild(pi);
		Element element = document.createElement("svg");
		document.appendChild(element);

		TestCSSStyleSheetFactory cssFac = new TestCSSStyleSheetFactory();
		cssFac.setLenientSystemValues(false);
		StylableDocumentWrapper wrapped = cssFac.createCSSDocument(document);
		LinkStyleProcessingInstruction lpi = (LinkStyleProcessingInstruction) wrapped
				.getFirstChild();

		assertEquals("title=\"a&amp;&lt;&quot;&gt;e&apos;z\" type=\"text/css\" href=\"style.css\"",
				lpi.getData());
		assertEquals("text/css", lpi.getPseudoAttribute("type"));
		assertEquals("style.css", lpi.getPseudoAttribute("href"));
		assertEquals("a&<\">e'z", lpi.getPseudoAttribute("title"));
	}

	@Test
	public void testFontIOError() throws SAXException, IOException {
		InputStream is = SampleCSS.sampleHTMLStream();
		Document doc;
		try {
			docbuilder.setEntityResolver(new DefaultEntityResolver());
			InputSource source = new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8));
			doc = docbuilder.parse(source);
		} finally {
			is.close();
		}
		doc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		Node head = doc.getElementsByTagName("head").item(0);
		Element style = doc.createElement("style");
		style.setAttribute("type", "text/css");
		style.setTextContent(
				"@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf');}");
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
