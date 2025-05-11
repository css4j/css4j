/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.dom.DOMDocument.LinkStyleDefiner;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.FontFeatureValuesRule;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.property.StyleValue;

public class XHTMLDocumentTest {

	private HTMLDocument xmlDoc;

	@BeforeEach
	public void setUp() throws IOException {
		xmlDoc = TestDOMImplementation.sampleXHTMLDocument();
		xmlDoc.normalizeDocument();
	}

	@Test
	public void getDocType() {
		DocumentType docType = xmlDoc.getDoctype();
		assertNotNull(docType);
		assertEquals("html", docType.getName());
	}

	@Test
	public void getDocumentElement() {
		DOMElement elm = xmlDoc.getDocumentElement();
		assertNotNull(elm);
		assertEquals("html", elm.getTagName());
		assertEquals("<html xmlns=\"http://www.w3.org/1999/xhtml\">", elm.getStartTag());
	}

	@Test
	public void getNamespaceURI() {
		assertNull(xmlDoc.getNamespaceURI());
		Text text = xmlDoc.createTextNode("foo");
		assertNotNull(text);
		assertNull(text.getNamespaceURI());
		CDATASection cdata = xmlDoc.createCDATASection("foo");
		assertNotNull(cdata);
		assertNull(cdata.getNamespaceURI());
		Comment comment = xmlDoc.createComment("foo");
		assertNotNull(comment);
		assertNull(comment.getNamespaceURI());
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"sheet.css\"");
		assertNotNull(pi);
		assertNull(pi.getNamespaceURI());
		EntityReference amp = xmlDoc.createEntityReference("amp");
		assertNotNull(amp);
		assertNull(amp.getNamespaceURI());
	}

	@Test
	public void appendChild() throws DOMException {
		DOMElement elm = xmlDoc.createElement("head");
		try {
			xmlDoc.getDocumentElement().appendChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		elm = xmlDoc.createElement("body");
		try {
			xmlDoc.getDocumentElement().appendChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Text text = xmlDoc.createTextNode("text");
		DOMElement p = xmlDoc.createElement("p");
		try {
			text.appendChild(p);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Attr foo = xmlDoc.createAttribute("foo");
		try {
			text.appendChild(foo);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"sheet.css\"");
		try {
			text.appendChild(pi);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void appendChild2() throws DOMException {
		DOMDocument document = new TestDOMImplementation(false).createDocument(null, null, null);
		document.appendChild(document.getImplementation().createDocumentType("foo", null, null));
		try {
			document.appendChild(document.getImplementation().createDocumentType("foo", null, null));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testCreateElement() {
		DOMElement elm = xmlDoc.createElement("link");
		assertTrue(elm instanceof LinkStyle);
		elm = xmlDoc.createElement("LINK");
		assertTrue(elm instanceof LinkStyle);
		elm = xmlDoc.createElement("style");
		assertTrue(elm instanceof LinkStyle);
		elm = xmlDoc.createElement("STYLE");
		assertTrue(elm instanceof LinkStyle);
	}

	@Test
	public void testCreateElementNS() {
		DOMElement elm = xmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "link");
		assertTrue(elm instanceof LinkStyle);
		elm = xmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "LINK");
		assertTrue(elm instanceof LinkStyle);
		elm = xmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "style");
		assertTrue(elm instanceof LinkStyle);
		elm = xmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "STYLE");
		assertTrue(elm instanceof LinkStyle);
	}

	@Test
	public void testCreateAttribute() {
		DOMElement svg = xmlDoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		Attr attr = xmlDoc.createAttribute("viewBox");
		assertEquals("viewbox", attr.getName());

		attr.setValue("0 0 150 100");
		svg.setAttributeNode(attr);

		assertTrue(svg.hasAttribute("viewBox"));
		assertEquals("0 0 150 100", svg.getAttribute("viewBox"));

		Attr vb = svg.getAttributeNode("viewBox");
		assertNotNull(vb);
		assertSame(attr, vb);
	}

	@Test
	public void testCreateAttributeNSNull() {
		DOMElement svg = xmlDoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		Attr attr = xmlDoc.createAttributeNS(null, "viewBox");
		assertEquals("viewBox", attr.getName());

		attr.setValue("0 0 150 100");
		svg.setAttributeNodeNS(attr);

		assertTrue(svg.hasAttributeNS(null, "viewBox"));
		assertTrue(svg.hasAttribute("viewBox"));
		assertFalse(svg.hasAttribute("viewbox"));
		assertEquals("0 0 150 100", svg.getAttributeNS(null, "viewBox"));
		assertEquals("0 0 150 100", svg.getAttribute("viewBox"));

		Attr vb = svg.getAttributeNodeNS(null, "viewBox");
		assertNotNull(vb);
		assertSame(attr, vb);
	}

	@Test
	public void testCreateAttributeNS() {
		DOMElement svg = xmlDoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		Attr attr = xmlDoc.createAttributeNS(TestConfig.SVG_NAMESPACE_URI, "viewBox");
		assertEquals("s:viewBox", attr.getName());

		attr.setValue("0 0 150 100");
		svg.setAttributeNodeNS(attr);

		assertTrue(svg.hasAttributeNS(TestConfig.SVG_NAMESPACE_URI, "viewBox"));
		assertFalse(svg.hasAttributeNS(TestConfig.SVG_NAMESPACE_URI, "viewbox"));
		assertTrue(svg.hasAttribute("s:viewBox"));
		assertEquals("0 0 150 100", svg.getAttributeNS(TestConfig.SVG_NAMESPACE_URI, "viewBox"));
		assertEquals("0 0 150 100", svg.getAttribute("s:viewBox"));
		assertNull(svg.getAttributeNodeNS(TestConfig.SVG_NAMESPACE_URI, "viewbox"));

		Attr vb = svg.getAttributeNodeNS(TestConfig.SVG_NAMESPACE_URI, "viewBox");
		assertNotNull(vb);
		assertSame(attr, vb);
	}

	@Test
	public void testEntities1() {
		DOMElement elm = xmlDoc.getElementById("entity");
		assertNotNull(elm);
		assertEquals("span", elm.getTagName());
		assertEquals("<>", elm.getTextContent());
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(1, nl.getLength());
		Node node0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, node0.getNodeType());
		assertEquals("<>", node0.getNodeValue());
		assertEquals("&lt;&gt;", node0.toString());
		Attr classattr = elm.getAttributeNode("class");
		assertNotNull(classattr);
		assertEquals("ent\"ity", classattr.getValue());
		assertEquals("class=\"ent&quot;ity\"", classattr.toString());
	}

	@Test
	public void testEntities2() {
		DOMElement elm = xmlDoc.getElementById("entiacute");
		assertNotNull(elm);
		assertEquals("span", elm.getTagName());
		assertEquals("ítem", elm.getTextContent());
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(1, nl.getLength());
		Node ent0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, ent0.getNodeType());
		assertEquals("ítem", ent0.getNodeValue());
	}

	@Test
	public void testAttributeEntities() {
		DOMElement p = xmlDoc.createElement("p");
		Attr attr = xmlDoc.createAttribute("id");
		attr.setValue("para>Id");
		p.setAttributeNode(attr);
		assertEquals("para>Id", p.getAttribute("id"));
		assertEquals("para>Id", attr.getValue());
		assertEquals("id=\"para&gt;Id\"", attr.toString());
		attr.setValue("para<Id");
		assertEquals("para<Id", attr.getValue());
		assertEquals("id=\"para&lt;Id\"", attr.toString());

		p.setAttribute("class", "\"fooclass&");
		assertEquals("\"fooclass&", p.getAttribute("class"));
		attr = p.getAttributeNode("class");
		assertEquals("class=\"&quot;fooclass&amp;\"", attr.toString());

		p.setAttribute("foo", "bar\"");
		assertEquals("bar\"", p.getAttribute("foo"));
		attr = p.getAttributeNode("foo");
		assertEquals("foo=\"bar&quot;\"", attr.toString());
	}

	@Test
	public void testComment() {
		Comment c = xmlDoc.createComment(" A comment ");
		assertEquals(" A comment ", c.getData());
		assertEquals("<!-- A comment -->", c.toString());
	}

	@Test
	public void testBadComment() {
		try {
			xmlDoc.createComment("Bad-->comment");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testProcessingInstruction() {
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"style.xsl\"");
		assertEquals("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>", pi.toString());
	}

	@Test
	public void testBadProcessingInstruction() {
		try {
			xmlDoc.createProcessingInstruction("xml", "encoding=UTF-8");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xmlDoc.createProcessingInstruction("foo:xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xmlDoc.createProcessingInstruction("foo:xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"?>");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testText() {
		Text c = xmlDoc.createTextNode("A text node");
		assertEquals("A text node", c.getData());
		assertEquals("A text node", c.toString());
		Text d = c.splitText(7);
		assertEquals("A text ", c.getData());
		assertEquals("node", d.getData());

		DOMElement elm = xmlDoc.createElement("p");
		c = xmlDoc.createTextNode("A text node");
		elm.appendChild(c);
		assertNull(c.getNextSibling());
		d = c.splitText(7);
		assertEquals("A text ", c.getData());
		assertEquals("node", d.getData());
		assertTrue(elm == d.getParentNode());
		assertEquals(2, elm.getChildNodes().getLength());
		assertTrue(d == c.getNextSibling());

		c = xmlDoc.createTextNode("A text node<");
		assertEquals("A text node<", c.getData());
		assertEquals("A text node&lt;", c.toString());
	}

	@Test
	public void testCharacterData() {
		CDATASection c = xmlDoc.createCDATASection("A CDATA section");
		assertEquals("A CDATA section", c.getData());
		assertEquals("<![CDATA[A CDATA section]]>", c.toString());
		c = xmlDoc.createCDATASection("A CDATA section<");
		assertEquals("A CDATA section<", c.getData());
		assertEquals("<![CDATA[A CDATA section<]]>", c.toString());
	}

	@Test
	public void getChildNodes() {
		NodeList list = xmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(2, list.getLength());
	}

	@Test
	public void getElementById() {
		DOMElement elm = xmlDoc.getElementById("ul1");
		assertNotNull(elm);
		assertEquals("ul", elm.getTagName());
		assertNull(xmlDoc.getElementById("xxxxxx"));
	}

	@Test
	public void getElementsByTagName() {
		NodeList stylelist = xmlDoc.getElementsByTagName("style");
		assertNotNull(stylelist);
		assertEquals(2, stylelist.getLength());
		assertEquals("style", stylelist.item(0).getNodeName());
		NodeList list = xmlDoc.getElementsByTagName("li");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		list.item(0).getParentNode().appendChild(xmlDoc.createElement("li"));
		assertEquals(7, list.getLength());
		list = xmlDoc.getElementsByTagName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		Element html = xmlDoc.getDocumentElement();
		list = xmlDoc.getElementsByTagName("div");
		assertEquals(3, list.getLength());
		html.appendChild(xmlDoc.createElement("div"));
		assertEquals(4, list.getLength());
		NodeList stylelist2 = xmlDoc.getElementsByTagName("style");
		assertEquals(stylelist.toString(), stylelist2.toString());

		list = xmlDoc.getElementsByTagName("html");
		assertEquals(1, list.getLength());
		assertTrue(xmlDoc.getDocumentElement() == list.item(0));
	}

	@Test
	public void getElementsByClassName() {
		ElementList tablelist = xmlDoc.getElementsByClassName("tableclass");
		assertNotNull(tablelist);
		assertEquals(1, tablelist.getLength());
		DOMElement elem = tablelist.item(0);
		assertEquals("table", elem.getNodeName());
		ElementList list = elem.getElementsByTagName("tr").item(0)
				.getElementsByClassName("tablecclass");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByClassName("liclass");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		DOMElement li = xmlDoc.createElement("li");
		li.setAttribute("class", "liclass");
		list.item(0).getParentNode().appendChild(li);
		assertEquals(7, list.getLength());
		list = xmlDoc.getElementsByClassName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByClassName("smallitalic");
		assertEquals(1, list.getLength());
		DOMElement div = xmlDoc.createElement("div");
		list.item(0).appendChild(div);
		assertEquals(1, list.getLength());
		div.setAttribute("class", "smallitalic");
		assertEquals("smallitalic", div.getAttribute("class"));
		assertEquals(2, list.getLength());
		ElementList tablelist2 = xmlDoc.getElementsByClassName("tableclass");
		assertEquals(tablelist.toString(), tablelist2.toString());
	}

	@Test
	public void getElementsByTagNameNS() {
		ElementList list = xmlDoc.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "*");
		assertNotNull(list);
		assertEquals(8, list.getLength());
		DOMElement svg = list.item(0);
		assertEquals("s:svg", svg.getNodeName());
		assertEquals(
				"<s:svg xmlns:s=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 100 100\" id=\"svg1\" style=\"width: 90%; height: 90%; \">",
				svg.getStartTag());
		Attr version = svg.getAttributeNode("version");
		assertNull(version.getNamespaceURI());
		assertNull(version.getPrefix());
		assertEquals("s", svg.getPrefix());

		assertEquals("s:rect", list.item(1).getNodeName());
		list.item(0).appendChild(xmlDoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "circle"));
		assertEquals(9, list.getLength());
		ElementList svglist = xmlDoc.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		assertNotNull(svglist);
		assertEquals(1, svglist.getLength());
		assertEquals("s:svg", svglist.item(0).getNodeName());

		list = xmlDoc.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "rect");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		Node oldrect = list.item(0);
		assertEquals("s:rect", oldrect.getNodeName());

		// Create a <rect> element with a missing prefix
		DOMElement newrect = xmlDoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "rect");
		assertEquals("<s:rect/>", newrect.toString());
		oldrect.getParentNode().appendChild(newrect);
		assertEquals(Node.DOCUMENT_POSITION_PRECEDING, oldrect.compareDocumentPosition(newrect));
		assertEquals(2, list.getLength());
		Node node = svglist.item(0);
		assertEquals("s:svg", node.getNodeName());
		node.getParentNode().removeChild(node);
		assertEquals(0, svglist.getLength());

		list = xmlDoc.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
	}

	@Test
	public void getElementsByTagNameNS2() {
		DOMElement docElm = xmlDoc.getDocumentElement();
		ElementList list = docElm.getElementsByTagName("s:foreignObject");
		assertFalse(list.isEmpty());
		assertEquals(3, list.getLength());

		list = docElm.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "foreignObject");
		assertFalse(list.isEmpty());
		assertEquals(3, list.getLength());
	}

	@Test
	public void getTextContent() {
		DOMElement elm = xmlDoc.getElementsByTagName("style").item(0);
		assertNotNull(elm);
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1204, text.trim().length());

		xmlDoc.normalizeDocument();
		text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1204, text.trim().length());

		xmlDoc.getDomConfig().setParameter("use-computed-styles", true);
		xmlDoc.getStyleSheets();
		xmlDoc.normalizeDocument();
		text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1144, text.trim().length());
	}

	@Test
	public void getTextContent2() {
		DOMElement elm = xmlDoc.getElementById("para1");
		assertNotNull(elm);
		elm.appendChild(xmlDoc.createComment(" comment "));
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals("Paragraph <>", text);
	}

	@Test
	public void TextIsElementContentWhitespace() {
		Text text = xmlDoc.createTextNode("foo ");
		assertNotNull(text);
		assertNotNull(text.getData());
		assertFalse(text.isElementContentWhitespace());
		text = xmlDoc.createTextNode("\n \t\r");
		assertNotNull(text);
		assertNotNull(text.getData());
		assertTrue(text.isElementContentWhitespace());
	}

	@Test
	public void TextGetWholeText() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("One"));
		Text text = xmlDoc.createTextNode("Two");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("Three"));
		p.appendChild(xmlDoc.createTextNode(" Four"));
		assertEquals("OneTwoThree Four", text.getWholeText());
	}

	@Test
	public void TextGetWholeTextWithER1() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p1 "));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xmlDoc.createTextNode(" p3");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode(" p4"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("p1 &amp; p3 p4", text.getWholeText());
	}

	@Test
	public void TextGetWholeTextWithER2() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p1 "));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		p.appendChild(xmlDoc.createElement("span"));
		Text text = xmlDoc.createTextNode(" p3");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode(" p4"));
		assertEquals(" p3 p4", text.getWholeText());
	}

	@Test
	public void TextReplaceWholeText() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("One"));
		Text text = xmlDoc.createTextNode("Two");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("Three"));
		p.appendChild(xmlDoc.createTextNode(" Four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("foo", text.replaceWholeText("foo").getData());
		assertEquals(1, p.getChildNodes().getLength());
		assertNull(text.replaceWholeText(""));
		assertFalse(p.hasChildNodes());
	}

	@Test
	public void TextReplaceWholeTextWithER1() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p one"));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xmlDoc.createTextNode("p three");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("p four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("foo", text.replaceWholeText("foo").getData());
		assertEquals(1, p.getChildNodes().getLength());
	}

	@Test
	public void TextReplaceWholeTextWithER2() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p one"));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xmlDoc.createTextNode("p three");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("p four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertNull(text.replaceWholeText(""));
		assertFalse(p.hasChildNodes());
	}

	@Test
	public void TextReplaceWholeTextWithER3() {
		DOMElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p one"));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		p.appendChild(xmlDoc.createElement("span"));
		Text text = xmlDoc.createTextNode("p four");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("p five"));
		assertEquals(5, p.getChildNodes().getLength());
		text.replaceWholeText("foo");
		assertEquals(4, p.getChildNodes().getLength());
	}

	@Test
	public void getStyleSheet() {
		DocumentCSSStyleSheet defsheet = xmlDoc.getStyleSheetFactory().getDefaultStyleSheet(xmlDoc.getComplianceMode());
		assertNotNull(defsheet);
		// Obtain the number of rules in the default style sheet, to use it
		// as a baseline.
		int defSz = defsheet.getCssRules().getLength();
		DocumentCSSStyleSheet css = xmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xmlDoc.embeddedStyle.size() + xmlDoc.linkedStyle.size();
		assertEquals(7, countInternalSheets);
		assertEquals(7, xmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css", xmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xmlDoc.getStyleSheetSets().getLength());
		Iterator<LinkStyleDefiner> it = xmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals("background-color: red; ", ((StyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		CSSStyleDeclaration fontface = ((CSSDeclarationRule) sheet.getCssRules().item(1)).getStyle();
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf')", fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CssType.TYPED, ffval.getCssValueType());
		assertEquals(CSSValue.Type.URI, ffval.getPrimitiveType());
		assertTrue(((FontFeatureValuesRule) sheet.getCssRules().item(2)).getMinifiedCssText()
				.startsWith("@font-feature-values Foo Sans,Bar"));
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(defSz + 25, css.getCssRules().getLength());
		assertFalse(xmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.enableStyleSheetsForSet("Alter 1");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.enableStyleSheetsForSet("Alter 1");
		assertNull(xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Default");
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void getElementgetStyle() {
		DOMElement elm = xmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", elm.getAttribute("style"));
		CSSStyleDeclaration style = elm.getStyle();
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", style.getCssText());
		assertEquals(2, style.getLength());
		assertEquals("'Does Not Exist', Neither", style.getPropertyValue("font-family"));
		DocumentCSSStyleSheet sheet = xmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(19, styledecl.getLength());
		assertEquals("#000080", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		assertEquals("  foo  bar  ", styledecl.getPropertyValue("content"));
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
		assertFalse(xmlDoc.getErrorHandler().hasIOErrors());
		xmlDoc.getErrorHandler().reset();
		// Error in inline style
		style.setCssText("width:calc(80%-)");
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		StyleDeclarationErrorHandler eh = xmlDoc.getErrorHandler().getInlineStyleErrorHandler(elm);
		assertNotNull(eh);
		assertTrue(eh.hasErrors());
		// set attribute
		Attr attr = elm.getAttributeNode("style");
		assertNotNull(attr);
		attr.setValue("");
		assertEquals(0, style.getLength());
		assertFalse(eh.hasErrors());
	}

	@Test
	public void getElementgetComputedStylePresentationalAttribute() {
		DOMElement elm = xmlDoc.getElementById("fooimg");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(2, styledecl.getLength());
		assertEquals("200px", styledecl.getPropertyValue("width"));
		assertEquals("180px", styledecl.getPropertyValue("height"));
		elm.setAttribute("style", "width: 220px; height: 193px;");
		styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(2, styledecl.getLength());
		assertEquals("220px", styledecl.getPropertyValue("width"));
		assertEquals("193px", styledecl.getPropertyValue("height"));
		// Check error handling
		DOMElement parent = (DOMElement) elm.getParentNode();
		parent.setAttribute("bgcolor", "#90fz77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(11, styledecl.getLength());
		assertEquals("rgb(0 0 0 / 0)", styledecl.getPropertyValue("background-color"));
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xmlDoc.getErrorHandler().hasComputedStyleErrors(parent));
		xmlDoc.getErrorHandler().reset();
		parent.setAttribute("bgcolor", "#90ff77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("#90ff77", styledecl.getPropertyValue("background-color"));
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
		assertFalse(xmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getFontSizeMedia() throws CSSMediaException {
		CSSElement elm = xmlDoc.getElementById("span1");
		CSSComputedProperties style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(15f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		CSSElement para = xmlDoc.getElementById("para2");
		CSSComputedProperties stylePara = xmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertNotNull(stylePara);
		assertEquals(12f, stylePara.getComputedFontSize(), 1e-5);

		xmlDoc.setTargetMedium("screen");
		assertEquals("screen", xmlDoc.getStyleSheet().getTargetMedium());
		style = elm.getComputedStyle(null);
		assertNotNull(style);
		assertEquals(20f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		stylePara = xmlDoc.getStyleSheet().getComputedStyle(para, null);
		assertEquals(16f, stylePara.getComputedFontSize(), 1e-5);
		xmlDoc.setTargetMedium("all");
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@Test
	public void getOverrideStyle() {
		DOMElement elm = xmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("10px", style.getPropertyValue("margin-top"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; ",
				style.getCssText());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals(
				"display:table-row;vertical-align:middle;border-color:#808080;unicode-bidi:embed;margin:16pt;color:#f00;",
				style.getMinifiedCssText());
	}

	@Test
	public void testStyleElement() {
		DOMElement style = xmlDoc.getElementsByTagName("style").item(0);
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) style).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		style.setAttribute("media", "screen");
		AbstractCSSStyleSheet sheet2 = ((LinkStyleDefiner) style).getSheet();
		assertNotNull(sheet2);
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertTrue(sheet2.getCssRules().getLength() > 0);

		style.setTextContent(
				"body {font-size: /* pre */14pt/* after */; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = ((LinkStyleDefiner) style).getSheet();
		assertTrue(sheet2 == sheet);
		assertEquals(2, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == style);

		// Obtain a value to check comments
		StyleRule styleR = (StyleRule) sheet.getCssRules().item(0);
		StyleValue fontSize = styleR.getStyle().getPropertyCSSValue("font-size");
		assertNotNull(fontSize.getPrecedingComments());
		assertNotNull(fontSize.getTrailingComments());
		assertEquals(" pre ", fontSize.getPrecedingComments().item(0));
		assertEquals(" after ", fontSize.getTrailingComments().item(0));

		xmlDoc.getImplementation().setFlag(Parser.Flag.VALUE_COMMENTS_IGNORE);

		// Set the stylesheet again
		style.setTextContent(
				"body {font-size: /* pre */14pt/* after */; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = ((LinkStyleDefiner) style).getSheet();
		assertTrue(sheet2 == sheet);

		// Obtain the same value, no comments now
		styleR = (StyleRule) sheet.getCssRules().item(0);
		fontSize = styleR.getStyle().getPropertyCSSValue("font-size");
		assertNull(fontSize.getPrecedingComments());
		assertNull(fontSize.getTrailingComments());

		assertEquals(2, sheet.insertRule("h3 {font-family: Arial}", 2));
		style.normalize();
		assertEquals("body {font-size: 14pt; margin-left: 7%; }h1 {font-size: 2.4em; }h3 {font-family: Arial; }",
				style.getTextContent());

		Attr type = style.getAttributeNode("type");
		type.setNodeValue("foo");
		assertNull(((LinkStyleDefiner) style).getSheet());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());

		xmlDoc.getImplementation().unsetFlag(Parser.Flag.VALUE_COMMENTS_IGNORE);
	}

	@Test
	public void testRawText() {
		DOMElement style = xmlDoc.getElementsByTagName("style").item(0);
		// Test raw text behaviour
		Text text = xmlDoc.createTextNode("data");
		assertEquals("data", text.toString());
		text.setData("hello</style>");
		assertEquals("hello&lt;/style&gt;", text.toString());
		style.appendChild(text);
		assertEquals("hello&lt;/style>", text.toString());
		text.setData("hello</foo>");
		assertEquals("hello</foo>", text.toString());
	}

	@Test
	public void testLinkElement() {
		DOMElement link = xmlDoc.getElementsByTagName("link").item(0);
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		link.setAttribute("media", "screen");
		AbstractCSSStyleSheet sheet2 = ((LinkStyleDefiner) link).getSheet();
		assertNotNull(sheet2);
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		link.setAttribute("href", "http://www.example.com/css/alter1.css");
		sheet = ((LinkStyleDefiner) link).getSheet();
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
		link.setAttribute("href", "http://www.example.com/css/alter1.css");
		sheet = ((LinkStyleDefiner) link).getSheet();
		assertTrue(sheet2 == sheet);
		assertTrue(sheet.getOwnerNode() == link);
		assertFalse(xmlDoc.getErrorHandler().hasErrors());

		Attr href = link.getAttributeNode("href");
		assertNotNull(href);
		href.setValue("http://www.example.com/css/example.css");
		assertNotNull(((LinkStyleDefiner) link).getSheet());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		xmlDoc.getErrorHandler().reset();

		link.setAttribute("media", "screen only and");
		assertNull(((LinkStyleDefiner) link).getSheet());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testLinkElementBadMIMEType() {
		DOMElement link = xmlDoc.createElement("link");
		link.setAttribute("href", "http://www.example.com/css/background.png");
		assertNull(((LinkStyleDefiner) link).getSheet());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
		link.setAttribute("rel", "stylesheet");
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) link).getSheet();
		assertNull(sheet);
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasPolicyErrors());
		assertFalse(xmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testLinkElementNoMIMEType() {
		DOMElement link = xmlDoc.createElement("link");
		link.setAttribute("href", "http://www.example.com/etc/fakepasswd");
		assertNull(((LinkStyleDefiner) link).getSheet());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
		link.setAttribute("rel", "stylesheet");
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) link).getSheet();
		assertNull(sheet);
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasPolicyErrors());
		assertFalse(xmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testLinkElementFileNotFound() {
		DOMElement link = xmlDoc.createElement("link");
		link.setAttribute("href", "http://www.example.com/file/not/found.txt");
		assertNull(((LinkStyleDefiner) link).getSheet());
		assertFalse(xmlDoc.getErrorHandler().hasErrors());

		link.setAttribute("rel", "stylesheet");
		AbstractCSSStyleSheet sheet = ((LinkStyleDefiner) link).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasIOErrors());
		assertFalse(xmlDoc.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testBaseElement() {
		assertEquals("http://www.example.com/xhtml/xmlns.xhtml", xmlDoc.getDocumentURI());
		assertEquals("http://www.example.com/", xmlDoc.getBaseURI());
		DOMElement base = xmlDoc.getElementsByTagName("base").item(0);
		base.setAttribute("href", "http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xmlDoc.getBaseURI());
		// Changing an unrelated href attribute does nothing to base uri.
		DOMElement anchor = xmlDoc.getElementsByTagName("a").item(0);
		anchor.setAttribute("href", "http://www.example.com/foo/");
		assertEquals("http://www.example.com/foo/", anchor.getAttribute("href"));
		assertEquals("http://www.example.com/newbase/", xmlDoc.getBaseURI());
		// Setting href as attribute node.
		Attr attr = xmlDoc.createAttribute("href");
		attr.setValue("http://www.example.com/other/base/");
		base.setAttributeNode(attr);
		assertEquals("http://www.example.com/other/base/", xmlDoc.getBaseURI());
	}

	@Test
	public void testFontIOError() {
		DOMElement head = xmlDoc.getElementsByTagName("head").item(0);
		DOMElement style = xmlDoc.createElement("style");
		style.setAttribute("type", "text/css");
		style.setTextContent("@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf');}");
		head.appendChild(style);
		DOMElement elm = xmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		elm.getComputedStyle(null);
		ErrorHandler errHandler = xmlDoc.getErrorHandler();
		assertNotNull(errHandler);
		assertTrue(errHandler.hasIOErrors());
		assertTrue(errHandler.hasErrors());
	}

	@Test
	public void testSVG() {
		ElementList list = xmlDoc.getElementsByTagNameNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		assertNotNull(list);
		DOMElement svg = list.item(0);
		assertNotNull(svg);
		assertEquals("s", svg.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, svg.getNamespaceURI());

		Attr version = svg.getAttributeNode("version");
		assertNull(version.getNamespaceURI());

		Attr viewBox = svg.getAttributeNode("viewBox");
		assertNotNull(viewBox);
		assertNull(viewBox.getNamespaceURI());
		assertEquals("0 0 100 100", viewBox.getValue());
		assertEquals("0 0 100 100", svg.getAttributeNS(null, "viewBox"));

		ElementList childe = svg.getChildren();
		Iterator<DOMElement> it = childe.iterator();
		assertTrue(it.hasNext());
		DOMElement style = it.next();
		assertEquals("style", style.getLocalName());
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, style.getNamespaceURI());

		assertTrue(it.hasNext());
		DOMElement rect = it.next();
		assertEquals("rect", rect.getLocalName());
		assertEquals("s", rect.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, rect.getNamespaceURI());

		assertTrue(it.hasNext());
		DOMElement g1 = it.next();
		assertEquals("g", g1.getLocalName());
		assertEquals("s", g1.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, g1.getNamespaceURI());

		DOMElement fo1 = g1.getFirstElementChild();
		assertNotNull(fo1);
		assertEquals("foreignObject", fo1.getLocalName());
		assertEquals("s", fo1.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, fo1.getNamespaceURI());

		DOMElement div1 = fo1.getFirstElementChild();
		assertNotNull(div1);
		assertEquals("div", div1.getLocalName());
		assertNull(div1.getPrefix());
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, div1.getNamespaceURI());

		assertTrue(it.hasNext());
		DOMElement g2 = it.next();
		assertEquals("g", g2.getLocalName());
		assertEquals("s", g2.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, g2.getNamespaceURI());

		DOMElement fo2 = g2.getFirstElementChild();
		assertNotNull(fo2);
		assertEquals("foreignObject", fo2.getLocalName());
		assertEquals("s", fo2.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, fo2.getNamespaceURI());

		DOMElement div2 = fo2.getFirstElementChild();
		assertNotNull(div2);
		assertEquals("div", div2.getLocalName());
		assertNull(div2.getPrefix());
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, div2.getNamespaceURI());

		DOMElement span = div2.getFirstElementChild();
		assertNotNull(span);
		assertEquals("span", span.getLocalName());
		assertNull(span.getPrefix());
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, span.getNamespaceURI());

		// MathML
		assertTrue(it.hasNext());
		DOMElement gMath = it.next();
		assertEquals("g", gMath.getLocalName());
		assertEquals("s", gMath.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, gMath.getNamespaceURI());
		assertFalse(it.hasNext());

		DOMElement foMath = gMath.getFirstElementChild();
		assertNotNull(foMath);
		assertEquals("foreignObject", foMath.getLocalName());
		assertEquals("s", foMath.getPrefix());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, foMath.getNamespaceURI());

		DOMElement math = foMath.getFirstElementChild();
		assertNotNull(math);
		assertEquals("math", math.getLocalName());
		assertEquals("m", math.getPrefix());
		assertEquals("http://www.w3.org/1998/Math/MathML", math.getNamespaceURI());

		DOMElement sqrt = math.getFirstElementChild();
		assertNotNull(sqrt);
		assertEquals("sqrt", sqrt.getLocalName());
		assertEquals("m", sqrt.getPrefix());
		assertEquals("http://www.w3.org/1998/Math/MathML", sqrt.getNamespaceURI());

		DOMElement mn1 = sqrt.getFirstElementChild();
		assertNotNull(mn1);
		assertEquals("mn", mn1.getLocalName());
		assertEquals("m", mn1.getPrefix());
		assertEquals("http://www.w3.org/1998/Math/MathML", mn1.getNamespaceURI());
	}

	@Test
	public void testCascade() throws IOException {
		URL url = DOMCSSStyleSheetFactoryTest.class
				.getResource("/io/sf/carte/doc/style/css/om/user.css");
		assertNotNull(url);
		xmlDoc.getStyleSheetFactory().setUserStyleSheet(url.toExternalForm(), null);

		DOMElement elm = xmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		elm.getOverrideStyle(null).setCssText("color: darkmagenta ! important;");
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));

		// Clear the user sheet
		xmlDoc.getStyleSheetFactory().setUserStyleSheet(null, null);
	}

}
