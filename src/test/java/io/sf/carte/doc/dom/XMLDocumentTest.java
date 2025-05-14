/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.dom.DOMDocument.LinkStyleDefiner;
import io.sf.carte.doc.dom.DOMDocument.LinkStyleProcessingInstruction;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.FontFeatureValuesRule;
import io.sf.carte.doc.style.css.om.SampleCSS;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.om.StyleSheetList;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class XMLDocumentTest {
	private static XMLDocumentBuilder builder;
	private DOMDocument xmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() {
		TestDOMImplementation impl = new TestDOMImplementation(false);
		impl.setXmlOnly(true);
		builder = new XMLDocumentBuilder(impl);
		builder.setIgnoreElementContentWhitespace(true);
		builder.setEntityResolver(new DefaultEntityResolver());
	}

	@BeforeEach
	public void setUp() throws SAXException, IOException {
		Reader re = SampleCSS.sampleXMLReader();
		InputSource is = new InputSource(re);
		try {
			xmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			throw e;
		} finally {
			re.close();
		}
		xmlDoc.setDocumentURI("http://www.example.com/xml/xmlsample.xml");
	}

	@Test
	public void getDocumentElement() {
		assertFalse(xmlDoc instanceof HTMLDocument);
		DOMElement elm = xmlDoc.getDocumentElement();
		assertNotNull(elm);
		assertEquals("html", elm.getTagName());
		assertEquals("<html xml:base=\"http://www.example.com/\">", elm.getStartTag());
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
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-foo", "bar");
		assertNotNull(pi);
		assertNull(pi.getNamespaceURI());
		EntityReference amp = xmlDoc.createEntityReference("amp");
		assertNotNull(amp);
		assertNull(amp.getNamespaceURI());
	}

	@Test
	public void appendChild() throws DOMException {
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
				"type=\"text/xsl\" href=\"sheet.xsl\"");
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
	public void testContains() {
		DOMElement docelm = xmlDoc.getDocumentElement();
		assertTrue(xmlDoc.contains(xmlDoc));
		assertTrue(xmlDoc.contains(docelm));
		assertTrue(docelm.contains(docelm));
		assertFalse(docelm.contains(xmlDoc));
		DOMElement h1 = xmlDoc.getElementById("h1");
		DOMElement span1 = xmlDoc.getElementById("span1");
		assertTrue(xmlDoc.contains(h1));
		assertTrue(xmlDoc.contains(span1));
		assertTrue(docelm.contains(h1));
		assertTrue(docelm.contains(span1));
		assertFalse(h1.contains(docelm));
		assertFalse(span1.contains(docelm));
		assertFalse(h1.contains(xmlDoc));
		assertFalse(span1.contains(xmlDoc));
		assertFalse(h1.contains(span1));
		assertFalse(span1.contains(h1));
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

		try {
			p.setAttribute("foo=", "bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		p.setAttribute("foo", "bar=");
		assertEquals("bar=", p.getAttribute("foo"));
		attr = p.getAttributeNode("foo");
		assertEquals("foo=\"bar=\"", attr.toString());

		try {
			p.setAttribute("foo:", "bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void testElementNamespaceError() {
		try {
			xmlDoc.createElement("p:");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
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
	public void testProcessingInstructionBadMIMEType() {
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"http://www.example.com/css/background.png\"");
		LinkStyleDefiner link = (LinkStyleDefiner) pi;
		assertNull(link.getSheet());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testProcessingInstructionNoMIMEType() {
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"http://www.example.com/etc/fakepasswd\"");
		LinkStyleDefiner link = (LinkStyleDefiner) pi;
		assertNull(link.getSheet());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testProcessingInstructionFileNotFound() {
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"http://www.example.com/file/not/found.txt\"");
		LinkStyleDefiner link = (LinkStyleDefiner) pi;
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());

		assertTrue(xmlDoc.getErrorHandler().hasErrors());
		assertTrue(xmlDoc.getErrorHandler().hasIOErrors());
		assertFalse(xmlDoc.getErrorHandler().hasPolicyErrors());
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
	public void testCloneNode() {
		testCloneNode(xmlDoc.getFirstChild());
	}

	private void testCloneNode(Node node) {
		Node prev = node;
		while (node != null) {
			prev = node;
			Node cloned = node.cloneNode(true);
			assertTrue(node.isEqualNode(cloned));
			node = node.getNextSibling();
		}
		if (prev != null) {
			testCloneNode(prev.getFirstChild());
		}
	}

	@Test
	public void getChildNodes() {
		NodeList list = xmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(8, list.getLength());
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
		NodeList lilist = xmlDoc.getElementsByTagName("li");
		assertNotNull(lilist);
		assertEquals(6, lilist.getLength());
		Node ul = lilist.item(0).getParentNode();
		assertEquals("li", lilist.item(0).getNodeName());
		ul.appendChild(xmlDoc.createElement("li"));
		assertEquals(7, lilist.getLength());
		DOMElement div = xmlDoc.createElement("div");
		DOMElement li = xmlDoc.createElement("li");
		div.appendChild(li);
		ul.appendChild(div);
		assertEquals(8, lilist.getLength());
		ul.removeChild(div);
		assertEquals(7, lilist.getLength());
		NodeList list = xmlDoc.getElementsByTagName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		DOMElement html = xmlDoc.getDocumentElement();
		list = xmlDoc.getElementsByTagName("div");
		assertEquals(3, list.getLength());
		DOMElement div2 = xmlDoc.createElement("div");
		html.appendChild(div2);
		assertEquals(4, list.getLength());
		DOMDocument otherdoc = xmlDoc.getImplementation().createDocument(null, null, null);
		try {
			html.insertBefore(div2, li);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.insertBefore(otherdoc, div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		try {
			html.insertBefore(otherdoc.createTextNode("foo"), div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		try {
			div2.appendChild(div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		try {
			div2.appendChild(otherdoc);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		try {
			otherdoc.appendChild(div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		try {
			div2.appendChild(html);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.replaceChild(div2, li);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.replaceChild(div2, li);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.replaceChild(otherdoc, div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());
		try {
			html.replaceChild(otherdoc.createTextNode("foo"), div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(4, list.getLength());

		assertEquals(stylelist.toString(), xmlDoc.getElementsByTagName("style").toString());

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
		ElementList list = elem.getElementsByTagName("tr").item(0).getElementsByClassName("tablecclass");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByClassName("liclass");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		DOMElement li = xmlDoc.createElement("li");
		li.setAttribute("class", "liclass");
		Node ul = list.item(0).getParentNode();
		ul.appendChild(li);
		assertEquals(7, list.getLength());
		DOMElement div = xmlDoc.createElement("div");
		DOMElement li2 = xmlDoc.createElement("li");
		li2.setAttribute("class", "liclass");
		div.appendChild(li2);
		ul.appendChild(div);
		assertEquals(8, list.getLength());
		ul.removeChild(div);
		assertEquals(7, list.getLength());

		list = xmlDoc.getElementsByClassName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByClassName("smallitalic");
		assertEquals(1, list.getLength());
		div = xmlDoc.createElement("div");
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
		Attr version = svg.getAttributeNodeNS(null, "version");
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
		DOMElement newrect = xmlDoc.createElementNS(TestConfig.SVG_NAMESPACE_URI, "rect");
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
	public void testQuerySelector() {
		DOMElement elm = xmlDoc.getElementById("ul1");
		DOMElement qelm = xmlDoc.querySelector("#ul1");
		assertSame(elm, qelm);
		DOMElement li = elm.querySelector(".liclass");
		assertNotNull(li);
		assertEquals("ul1li1", li.getId());

		DOMElement span = elm.querySelector("#entiacute");
		assertNotNull(span);
		assertEquals("entiacute", span.getId());

		assertNull(elm.querySelector("#nosuchId"));
	}

	@Test
	public void testQuerySelectorAll() {
		DOMElement elm = xmlDoc.getElementById("ul1");
		ElementList qlist = xmlDoc.querySelectorAll("#ul1");
		assertEquals(1, qlist.getLength());
		assertSame(elm, qlist.item(0));

		qlist = elm.querySelectorAll("[href=\"li6dir\"]");
		assertEquals(1, qlist.getLength());
		assertEquals("a", qlist.item(0).getTagName());

		qlist = xmlDoc.querySelectorAll("#xxxxxx");
		assertEquals(0, qlist.getLength());
	}

	@Test
	public void testQuerySelectorAll2() {
		ElementList list = xmlDoc.getElementsByTagName("p");
		ElementList qlist = xmlDoc.querySelectorAll("p");
		int sz = list.getLength();
		assertEquals(sz, qlist.getLength());
		for (int i = 0; i < sz; i++) {
			assertTrue(qlist.contains(list.item(i)));
		}
		assertFalse(qlist.isEmpty());
	}

	@Test
	public void testQuerySelectorAllNS() {
		// From the spec:
		// 'Support for namespaces within selectors is not planned and will not be
		// added'
		try {
			xmlDoc.querySelectorAll("svg|*");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
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
		int countSheets = xmlDoc.embeddedStyle.size() + xmlDoc.linkedStyle.size();
		assertEquals(6, countSheets);
		assertEquals(6, xmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css", xmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xmlDoc.getStyleSheetSets().getLength());

		Iterator<LinkStyleDefiner> it = xmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);

		Node ownerNode = sheet.getOwnerNode();
		assertNotNull(ownerNode);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, ownerNode.getNodeType());
		assertEquals("xml-stylesheet", ownerNode.getNodeName());

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
		assertEquals(defSz + 22, css.getCssRules().getLength());
		assertFalse(xmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());

		// Internal sheet
		sheet = xmlDoc.getStyleSheets().item(5);
		ownerNode = sheet.getOwnerNode();
		assertNotNull(ownerNode);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, ownerNode.getNodeType());
		assertEquals("xml-stylesheet", ownerNode.getNodeName());
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

		StyleSheetList sheets = xmlDoc.getStyleSheets();
		assertEquals(6, sheets.getLength());
		sheets.remove("Alter 2");
		assertEquals(5, sheets.getLength());
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void testAlternateStyle() {
		xmlDoc.setSelectedStyleSheetSet("Alter 1");
		DOMElement body = xmlDoc.getElementsByTagName("body").item(0);
		ComputedCSSStyle style = body.getComputedStyle(null);
		assertEquals("#000080", style.getPropertyValue("color"));
		assertEquals("#ff0", style.getPropertyValue("background-color"));
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
	public void getComputedStyle() {
		DOMElement elm = xmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals("#808000", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		assertEquals(15, styledecl.getLength());

		ErrorHandler errHandler = xmlDoc.getErrorHandler();
		assertNotNull(errHandler);
		assertFalse(errHandler.hasErrors());
		assertFalse(errHandler.hasIOErrors());
	}

	@Test
	public void getOverrideStyle() {
		DOMElement elm = xmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("5pt", style.getPropertyValue("margin-top"));
		assertEquals("margin-top: 5pt; margin-right: 5pt; margin-bottom: 5pt; margin-left: 5pt; ", style.getCssText());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals("margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals("margin:16pt;color:#f00;", style.getMinifiedCssText());
	}

	@Test
	public void testEmbeddedStyle() {
		LinkStyleDefiner link = (LinkStyleDefiner) xmlDoc.getEmbeddedStyleNodeList().item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		link.setNodeValue("href=\"#style1\" media=\"screen\"");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertFalse(sheet2.equals(sheet));
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertTrue(sheet2.getCssRules().getLength() > 0);
		// Change content of the container element
		LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) link;
		String href = pi.getPseudoAttribute("href");
		assertTrue(href.length() > 1);
		assertEquals('#', href.charAt(0));
		DOMElement container = xmlDoc.getElementById(href.substring(1));
		assertNotNull(container);
		container.setTextContent("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = link.getSheet();
		assertNotNull(sheet);
		assertFalse(sheet2 == sheet);
		assertEquals(2, sheet.getCssRules().getLength());
	}

	@Test
	public void testEmbeddedStyle2() {
		LinkStyleDefiner link = (LinkStyleDefiner) xmlDoc.getEmbeddedStyleNodeList().item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		// Change text child of the container element
		LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) link;
		String href = pi.getPseudoAttribute("href");
		assertTrue(href.length() > 1);
		assertEquals('#', href.charAt(0));
		DOMElement container = xmlDoc.getElementById(href.substring(1));
		assertNotNull(container);
		Node text = null;
		NodeList list = container.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.TEXT_NODE && node.getNodeValue().trim().length() > 2) {
				text = node;
				break;
			}
		}
		assertNotNull(text);
		text.setNodeValue("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertFalse(sheet2 == sheet);
		assertEquals(2, sheet2.getCssRules().getLength());
	}

	@Test
	public void testLinkElement() {
		LinkStyleDefiner link = (LinkStyleDefiner) xmlDoc.getLinkedStyleNodeList().item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() != 0);
		link.setNodeValue("href=\"/css/common.css\" media=\"screen\"");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertFalse(sheet2.equals(sheet));
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertEquals(sheet.getCssRules().getLength(), sheet2.getCssRules().getLength());
		link.setNodeValue("href=\"http://www.example.com/css/alter1.css\" media=\"screen\"");
		sheet = link.getSheet();
		assertFalse(sheet2.equals(sheet));
		assertFalse(sheet.getCssRules().item(sheet.getCssRules().getLength() - 1)
				.equals(sheet2.getCssRules().item(sheet2.getCssRules().getLength() - 1)));

		xmlDoc.getErrorHandler().reset();
		link.setNodeValue("href=\"http://www.example.com/css/background.png\" media=\"all\"");
		assertNull(link.getSheet());
		assertTrue(xmlDoc.getErrorHandler().hasPolicyErrors());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());

		xmlDoc.getErrorHandler().reset();
		link.setNodeValue("href=\"http://www.example.com/etc/fakepasswd\" media=\"all\"");
		assertNull(link.getSheet());
		assertTrue(xmlDoc.getErrorHandler().hasPolicyErrors());
		assertTrue(xmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testBaseAttribute() throws MalformedURLException {
		assertEquals("http://www.example.com/xml/xmlsample.xml", xmlDoc.getDocumentURI());
		assertEquals("http://www.example.com/", xmlDoc.getBaseURI());
		Attr base = xmlDoc.getDocumentElement().getAttributeNode("xml:base");
		base.setValue("http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xmlDoc.getBaseURI());

		// Check that getURL works
		assertEquals("http://www.example.com/newbase/b?e=f&g=h",
				xmlDoc.getURL("b?e=f&g=h").toExternalForm());

		assertEquals(
				"https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400..900;1,400..900&family=Raleway:wght@200&display=swap",
				xmlDoc.getURL(
						"https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400..900;1,400..900&family=Raleway:wght@200&display=swap")
						.toExternalForm());

		// Web browsers admit the following
		assertEquals(
				"https://fonts.googleapis.com/css?family=Roboto+Slab:400,700,300%7CRoboto:400,500,700,300,900&subset=latin,greek,greek-ext,vietnamese,cyrillic-ext,latin-ext,cyrillic",
				xmlDoc.getURL(
						"https://fonts.googleapis.com/css?family=Roboto+Slab:400,700,300|Roboto:400,500,700,300,900&subset=latin,greek,greek-ext,vietnamese,cyrillic-ext,latin-ext,cyrillic")
						.toExternalForm());

		assertThrows(MalformedURLException.class, () -> xmlDoc.getURL("https:||www.example.com/"));

		assertThrows(MalformedURLException.class,
				() -> xmlDoc.getURL("https://www.example.com/a?b=c\\d"));

		assertThrows(MalformedURLException.class,
				() -> xmlDoc.getURL("https://www.example.com/a?b=\"c\""));

		// Changing an unrelated href attribute does nothing to base uri.
		DOMElement anchor = xmlDoc.getElementsByTagName("a").item(0);
		anchor.setAttribute("href", "http://www.example.com/foo/");
		assertEquals("http://www.example.com/foo/", anchor.getAttribute("href"));
		assertEquals("http://www.example.com/newbase/", xmlDoc.getBaseURI());
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

		Attr viewBox = svg.getAttributeNodeNS(null, "viewBox");
		assertNotNull(viewBox);
		assertNull(viewBox.getNamespaceURI());
		assertEquals("viewBox", viewBox.getName());
		assertEquals("0 0 100 100", viewBox.getValue());
		assertEquals("0 0 100 100", svg.getAttribute("viewBox"));
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
	public void testAppendStyleProcessingInstruction() {
		StyleSheetList list = xmlDoc.getStyleSheets();
		assertNotNull(list);
		assertEquals(6, list.getLength());
		DOMElement head = xmlDoc.getElementsByTagName("head").item(0);
		DOMElement style = xmlDoc.createElement("style");
		style.setAttribute("id", "styleFontFamily");
		style.setAttribute("type", "text/css");
		style.setTextContent("span.foospan{font-family:'Mechanical Bold'}");
		head.appendChild(style);
		list = xmlDoc.getStyleSheets();
		assertEquals(6, list.getLength());
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"#styleFontFamily\"");
		xmlDoc.insertBefore(pi, xmlDoc.getDocumentElement());

		list = xmlDoc.getStyleSheets();
		assertEquals(7, list.getLength());
		AbstractCSSStyleSheet item = list.item(6);
		CSSStyleSheet<?> sheet = ((LinkStyle<?>) pi).getSheet();
		assertSame(sheet, item);

		head.getComputedStyle(null);
		assertFalse(xmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testCascade() throws IOException {
		try (Reader re = SampleCSS.loadSampleUserCSSReader()) {
			xmlDoc.getStyleSheetFactory().setUserStyleSheet(re);
		}
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
		xmlDoc.getStyleSheetFactory().setUserStyleSheet(null);
		style = elm.getComputedStyle(null);
		assertEquals("rgb(0 0 0 / 0)", style.getPropertyValue("background-color"));
		assertEquals("#8b008b", style.getPropertyValue("color"));
	}

}
