/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.dom.DOMDocument.LinkStyleDefiner;
import io.sf.carte.doc.dom.HTMLDocument.LinkElement;
import io.sf.carte.doc.dom.HTMLDocument.StyleElement;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSDeclarationRule;
import io.sf.carte.doc.style.css.om.CSSRuleArrayList;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.DefaultErrorHandler;
import io.sf.carte.doc.style.css.om.FontFeatureValuesRule;
import io.sf.carte.doc.style.css.om.StyleRule;

public class HTMLDocumentTest {
	HTMLDocument xhtmlDoc;

	@Before
	public void setUp() throws IOException {
		xhtmlDoc = TestDOMImplementation.sampleHTMLDocument();
		xhtmlDoc.normalizeDocument();
	}

	@Test
	public void getDocumentElement() {
		DOMElement elm = xhtmlDoc.getDocumentElement();
		assertNotNull(elm);
		assertEquals("html", elm.getTagName());
	}

	@Test
	public void getNamespaceURI() {
		assertNull(xhtmlDoc.getNamespaceURI());
		Text text = xhtmlDoc.createTextNode("foo");
		assertNotNull(text);
		assertNull(text.getNamespaceURI());
		CDATASection cdata = xhtmlDoc.createCDATASection("foo");
		assertNotNull(cdata);
		assertNull(cdata.getNamespaceURI());
		Comment comment = xhtmlDoc.createComment("foo");
		assertNotNull(comment);
		assertNull(comment.getNamespaceURI());
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"sheet.css\"");
		assertNotNull(pi);
		assertNull(pi.getNamespaceURI());
		EntityReference amp = xhtmlDoc.createEntityReference("amp");
		assertNotNull(amp);
		assertNull(amp.getNamespaceURI());
	}

	@Test
	public void appendChild() throws DOMException {
		DOMElement elm = xhtmlDoc.createElement("head");
		try {
			xhtmlDoc.getDocumentElement().appendChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		elm = xhtmlDoc.createElement("body");
		try {
			xhtmlDoc.getDocumentElement().appendChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Text text = xhtmlDoc.createTextNode("text");
		DOMElement p = xhtmlDoc.createElement("p");
		try {
			text.appendChild(p);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Attr foo = xhtmlDoc.createAttribute("foo");
		try {
			text.appendChild(foo);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-stylesheet",
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
			document.appendChild(document.getImplementation().createDocumentType("bar", null, null));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testCloneNode() {
		CSSDOMImplementation domImpl = xhtmlDoc.getImplementation();
		DOMDocument document = domImpl.createDocument(null, null, null);
		DOMDocument cloned = document.cloneNode(false);
		assertTrue(document.isEqualNode(cloned));
		assertTrue(document.getClass() == cloned.getClass());
		DocumentType docType = domImpl.createDocumentType("html", null, null);
		document = domImpl.createDocument(null, null, docType);
		assertTrue(document.isEqualNode(document.cloneNode(true)));
		cloned = document.cloneNode(false);
		assertNull(cloned.getDoctype());
		assertNull(cloned.getDocumentElement());
		assertTrue(document.getClass() == cloned.getClass());
		DOMElement docElm = document.createElement("html");
		docElm.setAttribute("id", "myId");
		docElm.setIdAttribute("id", true);
		document.appendChild(docElm);
		assertTrue(document.isEqualNode(document.cloneNode(true)));
	}

	@Test
	public void testCloneNode2() {
		assertTrue(xhtmlDoc.isEqualNode(xhtmlDoc.cloneNode(true)));
	}

	@Test
	public void testContains() {
		HTMLElement docelm = xhtmlDoc.getDocumentElement();
		assertTrue(xhtmlDoc.contains(xhtmlDoc));
		assertTrue(xhtmlDoc.contains(docelm));
		assertTrue(docelm.contains(docelm));
		assertFalse(docelm.contains(xhtmlDoc));
		DOMElement h1 = xhtmlDoc.getElementById("h1");
		DOMElement span1 = xhtmlDoc.getElementById("span1");
		assertTrue(xhtmlDoc.contains(h1));
		assertTrue(xhtmlDoc.contains(span1));
		assertTrue(docelm.contains(h1));
		assertTrue(docelm.contains(span1));
		assertFalse(h1.contains(docelm));
		assertFalse(span1.contains(docelm));
		assertFalse(h1.contains(xhtmlDoc));
		assertFalse(span1.contains(xhtmlDoc));
		assertFalse(h1.contains(span1));
		assertFalse(span1.contains(h1));
	}

	@Test
	public void testCreateElement() {
		DOMElement elm = xhtmlDoc.createElement("link");
		assertTrue(elm instanceof LinkStyle);
		assertTrue(elm.isVoid());
		elm = xhtmlDoc.createElement("LINK");
		assertTrue(elm instanceof LinkStyle);
		elm = xhtmlDoc.createElement("style");
		assertTrue(elm instanceof LinkStyle);
		assertFalse(elm.isVoid());
		elm = xhtmlDoc.createElement("STYLE");
		assertTrue(elm instanceof LinkStyle);
		//
		HTMLElement html = (HTMLElement) xhtmlDoc.createElement("html");
		assertFalse(html.isVoid());
		try {
			elm.appendChild(html);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			xhtmlDoc.createElement(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElement("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElement("\u0000");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElement("<");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElement(">");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNS() {
		DOMElement elm = xhtmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "link");
		assertTrue(elm instanceof LinkStyle);
		elm = xhtmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "LINK");
		assertTrue(elm instanceof LinkStyle);
		elm = xhtmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "style");
		assertTrue(elm instanceof LinkStyle);
		elm = xhtmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "STYLE");
		assertTrue(elm instanceof LinkStyle);
		try {
			xhtmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(HTMLDocument.HTML_NAMESPACE_URI, "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(DOMDocument.XML_NAMESPACE_URI, "x:");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(DOMDocument.XML_NAMESPACE_URI, ":x");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(DOMDocument.XML_NAMESPACE_URI, ":");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(DOMDocument.XML_NAMESPACE_URI, "\u0000");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(DOMDocument.XML_NAMESPACE_URI, "<");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createElementNS(DOMDocument.XML_NAMESPACE_URI, ">");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testAttributes() {
		DOMElement p = xhtmlDoc.createElement("p");
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("theId");
		p.setAttributeNode(attr);
		Attr cloned = (Attr) attr.cloneNode(false);
		assertNotNull(cloned);
		assertEquals(attr.getName(), cloned.getName());
		assertEquals(attr.getNamespaceURI(), cloned.getNamespaceURI());
		assertEquals(attr.getValue(), cloned.getValue());
		//
		try {
			xhtmlDoc.createAttribute(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttribute("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttribute("\u0000");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttribute("<");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttribute(">");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttribute("\"");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		//
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, ":");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "x:");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, ":x");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, ">");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "<");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "\"");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testEntities1() {
		DOMElement elm = xhtmlDoc.getElementById("entity");
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
		DOMElement elm = xhtmlDoc.getElementById("entiacute");
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
	public void testEntities3() {
		DOMElement elm = xhtmlDoc.getElementById("inflink");
		assertNotNull(elm);
		assertEquals("a", elm.getTagName());
		assertEquals("List item \u221e", elm.getTextContent());
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(1, nl.getLength());
		Node ent0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, ent0.getNodeType());
		assertEquals("List item \u221e", ent0.getNodeValue());
	}

	@Test
	public void testAttributeEntities() {
		DOMElement p = xhtmlDoc.createElement("p");
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("para>Id");
		p.setAttributeNode(attr);
		assertEquals("para>Id", p.getAttribute("id"));
		assertEquals("para>Id", attr.getValue());
		assertEquals("id=\"para&gt;Id\"", attr.toString());
		attr.setValue("para<Id");
		assertEquals("para<Id", attr.getValue());
		assertEquals("id=\"para&lt;Id\"", attr.toString());
		//
		p.setAttribute("class", "\"fooclass&");
		assertEquals("\"fooclass&", p.getAttribute("class"));
		attr = p.getAttributeNode("class");
		assertEquals("class=\"&quot;fooclass&amp;\"", attr.toString());
		//
		p.setAttribute("foo", "bar\"");
		assertEquals("bar\"", p.getAttribute("foo"));
		attr = p.getAttributeNode("foo");
		assertEquals("foo=\"bar&quot;\"", attr.toString());
	}

	@Test
	public void testSetAttributeError() {
		DOMElement p = xhtmlDoc.createElement("p");
		try {
			p.setAttribute("foo=", "bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			p.setIdAttribute("id", true);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementError() {
		try {
			xhtmlDoc.createElement("p'");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testComment() {
		Comment c = xhtmlDoc.createComment(" A comment ");
		assertEquals(" A comment ", c.getData());
		assertEquals("<!-- A comment -->", c.toString());
		// Cloning
		Node clone = c.cloneNode(false);
		assertNotNull(clone);
		assertEquals(c.getNodeType(), clone.getNodeType());
		assertEquals(c.getNodeName(), clone.getNodeName());
		assertEquals(c.getNodeValue(), clone.getNodeValue());
	}

	@Test
	public void testBadComment() {
		try {
			xhtmlDoc.createComment("Bad-->comment");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testProcessingInstruction() {
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"style.xsl\"");
		assertEquals("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>", pi.toString());
		// Cloning
		Node clone = pi.cloneNode(false);
		assertNotNull(clone);
		assertEquals(pi.getNodeType(), clone.getNodeType());
		assertEquals(pi.getNodeName(), clone.getNodeName());
		assertEquals(pi.getNodeValue(), clone.getNodeValue());
	}

	@Test
	public void testBadProcessingInstruction() {
		try {
			xhtmlDoc.createProcessingInstruction("xml", "encoding=UTF-8");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createProcessingInstruction("foo:xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createProcessingInstruction("foo:xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"?>");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCloneDocumentNode() {
		HTMLDocument doc = xhtmlDoc.cloneNode(false);
		assertNull(doc.getDoctype());
		assertNull(doc.getDocumentElement());
		assertTrue(xhtmlDoc.getImplementation() == doc.getImplementation());
	}

	@Test
	public void testCloneNodeDeep() {
		testCloneNode(xhtmlDoc.getFirstChild());
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
		NodeList list = xhtmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(1, list.getLength());
	}

	@Test
	public void getElementById() {
		HTMLElement elm = (HTMLElement) xhtmlDoc.getElementById("ul1");
		assertNotNull(elm);
		assertEquals("ul", elm.getTagName());
		assertNull(xhtmlDoc.getElementById("xxxxxx"));
		assertEquals("ul1", elm.getAttribute("id"));
		assertEquals("ul1", elm.getId());
		// The following is ignored
		elm.setIdAttribute("id", true);
		elm.setIdAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "id", true);
		elm.setId("id");
		// Test other attributes
		Attr svgid = xhtmlDoc.createAttributeNS("http://www.w3.org/2000/svg", "svgid");
		try {
			elm.setIdAttributeNS("http://www.w3.org/2000/svg", "svgid", true);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		try {
			elm.setIdAttributeNode(svgid, true);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		elm.setAttributeNode(svgid);
		elm.setIdAttributeNS("http://www.w3.org/2000/svg", "svgid", true);
		elm.setIdAttributeNode(svgid, true);
		//
		elm.setAttribute("foo", "bar");
		try {
			elm.setIdAttribute("foo", true);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
		try {
			elm.setIdAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "foo", true);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
		Attr attr = xhtmlDoc.createAttribute("foo");
		elm.setAttributeNode(attr);
		try {
			elm.setIdAttributeNode(attr, true);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
	}

	@Test
	public void getElementsByTagName() {
		NodeList stylelist = xhtmlDoc.getElementsByTagName("style");
		assertNotNull(stylelist);
		assertEquals(1, stylelist.getLength());
		assertEquals("style", stylelist.item(0).getNodeName());
		assertNull(stylelist.item(-1));
		assertNull(stylelist.item(1));
		NodeList list = xhtmlDoc.getElementsByTagName("li");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		list.item(0).getParentNode().appendChild(xhtmlDoc.createElement("li"));
		assertEquals(7, list.getLength());
		list = xhtmlDoc.getElementsByTagName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		HTMLElement html = xhtmlDoc.getDocumentElement();
		list = xhtmlDoc.getElementsByTagName("div");
		assertEquals(1, list.getLength());
		html.appendChild(xhtmlDoc.createElement("div"));
		assertEquals(2, list.getLength());
		NodeList stylelist2 = xhtmlDoc.getElementsByTagName("style");
		assertTrue(stylelist == stylelist2);
		//
		list = xhtmlDoc.getElementsByTagName("html");
		assertEquals(1, list.getLength());
		assertTrue(xhtmlDoc.getDocumentElement() == list.item(0));
	}

	@Test
	public void getElementsByClassName() {
		ElementList tablelist = xhtmlDoc.getElementsByClassName("tableclass");
		assertNotNull(tablelist);
		assertEquals(1, tablelist.getLength());
		DOMElement elem = tablelist.item(0);
		assertEquals("table", elem.getNodeName());
		ElementList list = ((HTMLElement) elem.getElementsByTagName("tr").item(0)).getElementsByClassName("tableclass");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xhtmlDoc.getElementsByClassName("liclass");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		DOMElement li = xhtmlDoc.createElement("li");
		li.setAttribute("class", "liclass");
		list.item(0).getParentNode().appendChild(li);
		assertEquals(7, list.getLength());
		list = xhtmlDoc.getElementsByClassName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xhtmlDoc.getElementsByClassName("smallitalic");
		assertEquals(1, list.getLength());
		DOMElement div = xhtmlDoc.createElement("div");
		list.item(0).appendChild(div);
		assertEquals(1, list.getLength());
		div.setAttribute("class", "smallitalic");
		assertEquals("smallitalic", div.getAttribute("class"));
		assertEquals(2, list.getLength());
		div.setAttribute("class", "nothing");
		assertEquals(1, list.getLength());
		ElementList tablelist2 = xhtmlDoc.getElementsByClassName("tableclass");
		assertTrue(tablelist == tablelist2);
	}

	@Test
	public void getElementsByTagNameNS() {
		ElementList list = xhtmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "*");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		DOMElement svg = list.item(0);
		assertEquals("svg", svg.getNodeName());
		Attr version = svg.getAttributeNode("version");
		assertNull(version.getNamespaceURI());
		assertNull(svg.getPrefix());
		assertEquals("rect", list.item(1).getNodeName());
		list.item(0).appendChild(xhtmlDoc.createElementNS("http://www.w3.org/2000/svg", "circle"));
		assertEquals(3, list.getLength());
		ElementList svglist = xhtmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "svg");
		assertNotNull(svglist);
		assertEquals(1, svglist.getLength());
		assertEquals("svg", svglist.item(0).getNodeName());
		list = xhtmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "rect");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		Node oldrect = list.item(0);
		assertEquals("rect", oldrect.getNodeName());
		DOMElement newrect = xhtmlDoc.createElementNS("http://www.w3.org/2000/svg", "rect");
		oldrect.getParentNode().appendChild(newrect);
		assertEquals(Node.DOCUMENT_POSITION_PRECEDING, oldrect.compareDocumentPosition(newrect));
		assertEquals(2, list.getLength());
		Node node = svglist.item(0);
		assertEquals("svg", node.getNodeName());
		node.getParentNode().removeChild(node);
		assertEquals(0, svglist.getLength());
		list = xhtmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
	}

	@Test
	public void testQuerySelectorAll() {
		DOMElement elm = xhtmlDoc.getElementById("ul1");
		ElementList qlist = xhtmlDoc.querySelectorAll("#ul1");
		assertEquals(1, qlist.getLength());
		assertTrue(elm == qlist.item(0));
		qlist = xhtmlDoc.querySelectorAll("#xxxxxx");
		assertEquals(0, qlist.getLength());
	}

	@Test
	public void testQuerySelectorAll2() {
		ElementList list = xhtmlDoc.getElementsByTagName("p");
		ElementList qlist = xhtmlDoc.querySelectorAll("p");
		int sz = list.getLength();
		assertEquals(sz, qlist.getLength());
		for (int i = 0; i < sz; i++) {
			assertTrue(qlist.contains(list.item(i)));
		}
	}

	@Test
	public void testQuerySelectorAllNS() {
		// From the spec:
		// 'Support for namespaces within selectors is not planned and will not be
		// added'
		try {
			xhtmlDoc.querySelectorAll("svg|*");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void getTextContent() {
		DOMElement elm = xhtmlDoc.getElementsByTagName("style").item(0);
		assertNotNull(elm);
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1106, text.trim().length());
	}

	@Test
	public void TextIsElementContentWhitespace() {
		Text text = xhtmlDoc.createTextNode("foo ");
		assertNotNull(text);
		assertNotNull(text.getData());
		assertFalse(text.isElementContentWhitespace());
		text = xhtmlDoc.createTextNode("\n \t\r");
		assertNotNull(text);
		assertNotNull(text.getData());
		assertTrue(text.isElementContentWhitespace());
	}

	@Test
	public void TextGetWholeText() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("One"));
		Text text = xhtmlDoc.createTextNode("Two");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode("Three"));
		p.appendChild(xhtmlDoc.createTextNode(" Four"));
		assertEquals("OneTwoThree Four", text.getWholeText());
	}

	@Test
	public void TextGetWholeTextWithER1() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("p1 "));
		EntityReference amp = xhtmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xhtmlDoc.createTextNode(" p3");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode(" p4"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("p1 &amp; p3 p4", text.getWholeText());
	}

	@Test
	public void TextGetWholeTextWithER2() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("p1 "));
		EntityReference amp = xhtmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		p.appendChild(xhtmlDoc.createElement("span"));
		Text text = xhtmlDoc.createTextNode(" p3");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode(" p4"));
		assertEquals(" p3 p4", text.getWholeText());
	}

	@Test
	public void TextReplaceWholeText() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("One"));
		Text text = xhtmlDoc.createTextNode("Two");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode("Three"));
		p.appendChild(xhtmlDoc.createTextNode(" Four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("foo", text.replaceWholeText("foo").getData());
		assertEquals(1, p.getChildNodes().getLength());
		assertNull(text.replaceWholeText(""));
		assertFalse(p.hasChildNodes());
	}

	@Test
	public void TextReplaceWholeTextWithER1() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("p one"));
		EntityReference amp = xhtmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xhtmlDoc.createTextNode("p three");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode("p four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("foo", text.replaceWholeText("foo").getData());
		assertEquals(1, p.getChildNodes().getLength());
	}

	@Test
	public void TextReplaceWholeTextWithER2() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("p one"));
		EntityReference amp = xhtmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xhtmlDoc.createTextNode("p three");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode("p four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertNull(text.replaceWholeText(""));
		assertFalse(p.hasChildNodes());
	}

	@Test
	public void TextReplaceWholeTextWithER3() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("p one"));
		EntityReference amp = xhtmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		p.appendChild(xhtmlDoc.createElement("span"));
		Text text = xhtmlDoc.createTextNode("p four");
		p.appendChild(text);
		p.appendChild(xhtmlDoc.createTextNode("p five"));
		assertEquals(5, p.getChildNodes().getLength());
		text.replaceWholeText("foo");
		assertEquals(4, p.getChildNodes().getLength());
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
		assertEquals(6, countInternalSheets);
		assertEquals(6, xhtmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css", xhtmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());
		Iterator<LinkStyleDefiner> it = xhtmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals("background-color: red; ", ((StyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		AbstractCSSStyleDeclaration fontface = ((BaseCSSDeclarationRule) sheet.getCssRules().item(1)).getStyle();
		assertEquals("url('http://www.example.com/css/font/MechanicalBd.otf')", fontface.getPropertyValue("src"));
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
		assertEquals(defSz + 20, css.getCssRules().getLength());
		assertFalse(xhtmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertNull(xhtmlDoc.getLastStyleSheetSet());
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertNull(xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Default");
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		assertEquals("Default", xhtmlDoc.getLastStyleSheetSet());
	}

	@Test
	public void getElementgetStyle() {
		DOMElement elm = xhtmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", elm.getAttribute("style"));
		CSSStyleDeclaration style = elm.getStyle();
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", style.getCssText());
		assertEquals(2, style.getLength());
		assertEquals("'Does Not Exist', Neither", style.getPropertyValue("font-family"));
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(19, styledecl.getLength());
		assertEquals("#000080", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		assertEquals("  foo  bar  ", styledecl.getPropertyValue("content"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasIOErrors());
		xhtmlDoc.getErrorHandler().reset();
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
		// Error in inline style
		style.setCssText("width:calc(80%-)");
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		StyleDeclarationErrorHandler eh = xhtmlDoc.getErrorHandler().getInlineStyleErrorHandler(elm);
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
		DOMElement elm = xhtmlDoc.getElementById("fooimg");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
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
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(parent));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		xhtmlDoc.getErrorHandler().reset();
		parent.setAttribute("bgcolor", "#90ff77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("#90ff77", styledecl.getPropertyValue("background-color"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void getOverrideStyle() {
		DOMElement elm = xhtmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("10px", style.getPropertyValue("margin-top"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; ",
				style.getCssText());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
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
	public void testComputedStyleAttr() {
		DOMElement elm = xhtmlDoc.getElementById("firstH3");
		/*
		 * attr() value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin,.6em)");
		ComputedCSSStyle style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12.96f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasIOErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value in calc(), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:calc(attr(leftmargin,.6em)*2)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(25.92f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value, do not reparse.
		 */
		elm.setAttribute("leftmargin", " .8em");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSValue.Type.STRING, marginLeft.getPrimitiveType());
		assertEquals(" .8em", marginLeft.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value, expected type.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin length)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(17.28f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value, string expected type, do not reparse.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin string)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSValue.Type.STRING, marginLeft.getPrimitiveType());
		assertEquals(" .8em", marginLeft.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value in calc().
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:calc(attr(leftmargin length,.6em)*2)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(34.56f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() invalid value type (em vs color).
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin color)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		// Currentcolor
		assertEquals(CSSValue.Type.COLOR, marginLeft.getPrimitiveType());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() invalid value type (color), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin color,.4em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8.64f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() valid value type (number).
		 */
		elm.setAttribute("leftmargin", "0.3");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin number)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0.3f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() invalid value type (number).
		 */
		elm.setAttribute("leftmargin", "0.3px");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin number)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() invalid value type (integer).
		 */
		elm.setAttribute("leftmargin", "0.3");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin integer)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() valid value type (integer).
		 */
		elm.setAttribute("leftmargin", "3");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin integer)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (angle-deg).
		 */
		elm.setAttribute("leftmargin", "3deg");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin angle)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_DEG), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (angle-rad).
		 */
		elm.setAttribute("leftmargin", "3rad");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin angle)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_RAD), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (time).
		 */
		elm.setAttribute("leftmargin", "3s");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin time)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_S), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (time II).
		 */
		elm.setAttribute("leftmargin", "3ms");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin time)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_MS), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (frequency).
		 */
		elm.setAttribute("leftmargin", "0.3Hz");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin frequency)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0.3f, marginLeft.getFloatValue(CSSUnit.CSS_HZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (frequency II).
		 */
		elm.setAttribute("leftmargin", "0.3kHz");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin frequency)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0.3f, marginLeft.getFloatValue(CSSUnit.CSS_KHZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (pt).
		 */
		elm.setAttribute("leftmargin", "15 ");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (px).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin px)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PX), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (em).
		 */
		elm.setAttribute("leftmargin", "1.6 ");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(34.56f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (deg).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin deg)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_DEG), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (grad).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin grad)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_GRAD), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (rad).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin rad)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_RAD), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (s).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin s)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_S), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (ms).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin ms)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_MS), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (Hz).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin Hz)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_HZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (kHz).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin kHz)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_KHZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unknown unit-value type.
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin foo)");
		style = elm.getComputedStyle(null);
		assertNull(style.getPropertyCSSValue("foo"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() unknown unit-value type (II).
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() valid value type (uri).
		 */
		elm.setAttribute("myuri", "https://www.example.com/foo");
		elm.getOverrideStyle(null).setCssText("background-image:attr(myuri url)");
		style = elm.getComputedStyle(null);
		CSSTypedValue value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("https://www.example.com/foo", value.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (relative uri).
		 */
		elm.setAttribute("myuri", "foo");
		elm.getOverrideStyle(null).setCssText("background-image:attr(myuri url)");
		style = elm.getComputedStyle(null);
		value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("http://www.example.com/foo", value.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() missing url value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("background-image:attr(noattr url,\"https://www.example.com/bar\")");
		style = elm.getComputedStyle(null);
		value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("https://www.example.com/bar", value.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() invalid value type (number).
		 */
		elm.setAttribute("leftmargin", "foo");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin number)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		// Default fallback
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() invalid value type (ident vs number), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin number,.4em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8.64f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() percentage invalid value type.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin %)");
		assertEquals("margin-left: attr(leftmargin %); ", elm.getOverrideStyle(null).getCssText());
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		// Default fallback
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() percentage invalid value type, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin %, 1.2em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(25.92f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() percentage value type.
		 */
		elm.setAttribute("leftmargin", "2");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin %)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() percentage value type (II).
		 */
		elm.setAttribute("leftmargin", "2%");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin %)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() recursive.
		 */
		elm.setAttribute("leftmargin", "attr(leftmargin length)");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin length)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() recursive with custom property (I).
		 */
		elm.getOverrideStyle(null)
				.setCssText("margin-left:attr(noattr length,var(--foo));--foo:attr(noattr,var(margin-left))");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() recursive with custom property (II).
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(noattr length,var(--foo));--foo:attr(noattr)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
	}

	@Test
	public void testCompatComputedStyle() {
		DOMElement elm = xhtmlDoc.getElementById("cell12");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertNull(style);
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("5pt", styledecl.getPropertyValue("margin-left"));
		assertEquals("4pt", styledecl.getPropertyValue("padding-top"));
		assertEquals("6pt", styledecl.getPropertyValue("padding-left"));
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasIOErrors());
	}

	@Test
	public void testStyleElement() {
		StyleElement style = (StyleElement) xhtmlDoc.getElementsByTagName("style").item(0);
		AbstractCSSStyleSheet sheet = style.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == style);
		// Change media
		style.setAttribute("media", "screen");
		AbstractCSSStyleSheet sheet2 = style.getSheet();
		assertNotNull(sheet2);
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertTrue(sheet2.getCssRules().getLength() > 0);
		style.setTextContent("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = style.getSheet();
		assertTrue(sheet2 == sheet);
		assertEquals(2, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == style);
		//
		assertEquals(2, sheet.insertRule("h3 {font-family: Arial}", 2));
		style.normalize();
		assertEquals("body {font-size: 14pt; margin-left: 7%; }h1 {font-size: 2.4em; }h3 {font-family: Arial; }",
				style.getTextContent());
		//
		Attr type = style.getAttributeNode("type");
		type.setNodeValue("foo");
		assertNull(style.getSheet());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		//
		type.setNodeValue("text/css");
		assertNotNull(style.getSheet());
		Attr media = style.getAttributeNode("media");
		media.setNodeValue("&%/(*");
		assertNull(style.getSheet());
		media.setNodeValue("screen");
		sheet = style.getSheet();
		assertNotNull(sheet);
		// Test insertBefore
		int sz = sheet.getCssRules().getLength();
		assertEquals(3, sz);
		Text text = xhtmlDoc.createTextNode("@namespace svg url('http://www.w3.org/2000/svg');\n");
		style.insertBefore(text, style.getFirstChild());
		int szp1 = sz + 1;
		assertEquals(szp1, sheet.getCssRules().getLength());
		sheet = style.getSheet();
		CSSRuleArrayList rules = sheet.getCssRules();
		assertEquals(szp1, rules.getLength());
		assertEquals(CSSRule.NAMESPACE_RULE, rules.item(0).getType());
		// Replace
		Text text2 = xhtmlDoc.createTextNode(
				"@font-feature-values Some Font, Other Font {@swash{swishy:1;flowing:2;}@styleset{double-W:14;sharp-terminals:16 1;}}\n");
		style.replaceChild(text2, text);
		assertEquals(szp1, sheet.getCssRules().getLength());
		sheet = style.getSheet();
		rules = sheet.getCssRules();
		assertEquals(szp1, rules.getLength());
		assertEquals(CSSRule.FONT_FEATURE_VALUES_RULE, rules.item(0).getType());
		// Remove
		try {
			style.removeChild(text);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		style.removeChild(text2);
		assertEquals(sz, sheet.getCssRules().getLength());
		sheet = style.getSheet();
		rules = sheet.getCssRules();
		assertEquals(sz, rules.getLength());
		assertNotEquals(CSSRule.FONT_FEATURE_VALUES_RULE, rules.item(0).getType());
	}

	@Test
	public void testStyleElement2() {
		StyleElement style = (StyleElement) xhtmlDoc.createElement("style");
		style.setAttribute("type", "text/css");
		AbstractCSSStyleSheet sheet = style.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
		//
		style.setAttribute("type", "");
		sheet = style.getSheet();
		assertNull(sheet);
		//
		style.removeAttributeNode(style.getAttributeNode("type"));
		sheet = style.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
		//
		style.setAttribute("type", "text/xsl");
		sheet = style.getSheet();
		assertNull(sheet);
		//
		style.removeAttribute("type");
		sheet = style.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
		style.setTextContent("body {color: blue;}");
		assertEquals(1, sheet.getCssRules().getLength());
	}

	@Test
	public void testRawText() {
		StyleElement style = (StyleElement) xhtmlDoc.getElementsByTagName("style").item(0);
		// Test raw text behaviour
		Text text = xhtmlDoc.createTextNode("data");
		assertEquals("data", text.toString());
		text.setData("hello</style>");
		assertEquals("hello&lt;/style&gt;", text.toString());
		style.appendChild(text);
		assertEquals("hello&lt;/style>", text.toString());
		text.setData("hello</foo>");
		assertEquals("hello</foo>", text.toString());
		// clone
		StyleElement cloned = (StyleElement) style.cloneNode(true);
		assertTrue(cloned.isRawText());
		assertTrue(style.isEqualNode(cloned));
		AbstractCSSStyleSheet sheet = style.getSheet();
		AbstractCSSStyleSheet clonesheet = cloned.getSheet();
		assertNotNull(clonesheet);
		assertEquals(sheet.getCssRules().getLength(), clonesheet.getCssRules().getLength());
		cloned = (StyleElement) style.cloneNode(false);
		assertTrue(cloned.isRawText());
		clonesheet = cloned.getSheet();
		assertNotNull(clonesheet);
		assertEquals(0, clonesheet.getCssRules().getLength());
	}

	@Test
	public void testRawTextScript() {
		DOMElement script = xhtmlDoc.createElement("script");
		assertTrue(script.isRawText());
		assertTrue(script.cloneNode(false).isRawText());
	}

	@Test
	public void testLinkElement() {
		LinkElement link = (LinkElement) xhtmlDoc.getElementsByTagName("link").item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == link);
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		link.setAttribute("media", "screen");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		link.setAttribute("href", "http://www.example.com/css/alter1.css");
		sheet = link.getSheet();
		assertTrue(sheet2 == sheet);
		assertTrue(sheet.getOwnerNode() == link);
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		//
		Attr href = link.getAttributeNode("href");
		assertNotNull(href);
		href.setValue("http://www.example.com/css/example.css");
		assertNotNull(((LinkStyle<AbstractCSSRule>) link).getSheet());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		xhtmlDoc.getErrorHandler().reset();
		//
		link.setAttribute("media", "screen only and");
		assertNull(link.getSheet());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testLinkElement2() {
		LinkElement link = (LinkElement) xhtmlDoc.createElement("link");
		link.setAttribute("href", "http://www.example.com/foo");
		assertNull(link.getSheet());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		link.setAttribute("rel", "stylesheet");
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == link);
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
	}

	@Test
	public void testBaseElement() {
		assertEquals("http://www.example.com/xhtml/htmlsample.html", xhtmlDoc.getDocumentURI());
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURL().toExternalForm());
		DOMElement base = xhtmlDoc.getElementsByTagName("base").item(0);
		assertEquals("http://www.example.com/", base.getBaseURI());
		assertTrue(base.isVoid());
		base.setAttribute("href", "http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xhtmlDoc.getBaseURI());
		assertEquals("http://www.example.com/newbase/", base.getBaseURI());
		// Changing an unrelated href attribute does nothing to base uri.
		DOMElement anchor = xhtmlDoc.getElementsByTagName("a").item(0);
		anchor.setAttribute("href", "http://www.example.com/foo/");
		assertEquals("http://www.example.com/foo/", anchor.getAttribute("href"));
		assertEquals("http://www.example.com/newbase/", xhtmlDoc.getBaseURI());
		// Setting href as attribute node.
		Attr attr = xhtmlDoc.createAttribute("href");
		attr.setValue("http://www.example.com/other/base/");
		base.setAttributeNode(attr);
		assertEquals("http://www.example.com/other/base/", xhtmlDoc.getBaseURI());
		// Disconnect base
		DOMNode parent = base.getParentNode();
		parent.removeChild(base);
		attr.setValue("http://www.example.com/yet/another/base/");
		parent.appendChild(base);
		assertEquals("http://www.example.com/yet/another/base/", xhtmlDoc.getBaseURI());
	}

	@Test
	public void testMetaElement() {
		DOMElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("http-equiv", "Content-Type");
		meta.setAttribute("content", "text/html; charset=utf-8");
		assertTrue(meta.isVoid());
		try {
			xhtmlDoc.getDocumentElement().appendChild(meta);
			fail("Should throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		DOMElement head = xhtmlDoc.getElementsByTagName("head").item(0);
		head.appendChild(meta);
	}

	@Test
	public void testMetaElement2() {
		DOMElement paragraph = xhtmlDoc.createElement("p");
		DOMElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("http-equiv", "Content-Type");
		meta.setAttribute("content", "text/html; charset=utf-8");
		try {
			paragraph.appendChild(meta);
			fail("Should throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("itemprop", "name");
		meta.setAttribute("content", "foo");
		paragraph.appendChild(meta);
	}

	@Test
	public void testMetaElement3() {
		xhtmlDoc.setStrictErrorChecking(false);
		DOMElement paragraph = xhtmlDoc.createElement("p");
		DOMElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("http-equiv", "Content-Type");
		meta.setAttribute("content", "text/html; charset=utf-8");
		paragraph.appendChild(meta);
	}

	@Test
	public void testMetaElementDefaultSheetSet() {
		DOMElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("http-equiv", "Default-Style");
		meta.setAttribute("content", "Alter 1");
		DOMElement head = xhtmlDoc.getElementsByTagName("head").item(0);
		head.appendChild(meta);
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void testMetaElementReferrerPolicy() {
		assertEquals("same-origin", xhtmlDoc.getReferrerPolicy());
		DOMElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("name", "referrer");
		meta.setAttribute("content", "no-referrer");
		DOMElement head = xhtmlDoc.getElementsByTagName("head").item(0);
		head.appendChild(meta);
		assertEquals("no-referrer", xhtmlDoc.getReferrerPolicy());
	}

	@Test
	public void testImgElement() {
		DOMElement element = xhtmlDoc.createElement("img");
		element.setAttribute("width", "300px");
		assertTrue(element.hasPresentationalHints());
		assertTrue(element.isVoid());
	}

	@Test
	public void testFontElement() {
		DOMElement element = xhtmlDoc.createElement("font");
		element.setAttribute("size", "12pt");
		assertTrue(element.hasPresentationalHints());
		assertFalse(element.isVoid());
	}

	@Test
	public void testCascade() throws IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleUserCSSReader();
		xhtmlDoc.getStyleSheetFactory().setUserStyleSheet(re);
		re.close();
		DOMElement elm = xhtmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		elm.getOverrideStyle(null).setCssText("color: darkmagenta ! important;");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));
	}

	@Test
	public void testReferrerPolicy() throws IOException {
		TestDOMImplementation impl = (TestDOMImplementation) xhtmlDoc.getImplementation();
		MockURLConnectionFactory urlfac = impl.getConnectionFactory();
		ElementList linkels = xhtmlDoc.getElementsByTagName("link");
		assertReferrer(urlfac, linkels.item(0), "http://www.example.com/");
		assertReferrer(urlfac, linkels.item(1), "http://www.example.com/xhtml/htmlsample.html");
		urlfac.clearAssertions();
	}

	private void assertReferrer(MockURLConnectionFactory urlfac, DOMElement link, String referrer) {
		urlfac.assertReferrer(link.getAttribute("href"), referrer);
		assertNotNull(((LinkStyle<AbstractCSSRule>) (LinkElement) link).getSheet());
		DefaultErrorHandler handler = (DefaultErrorHandler) xhtmlDoc.getErrorHandler();
		LinkedHashMap<Exception, CSSStyleSheet> errorMap = handler.getLinkedSheetErrors();
		assertNull(errorMap);
	}

	@Test
	public void testReplaceChild() throws IOException {
		HTMLElement html = xhtmlDoc.getDocumentElement();
		DOMElement head = html.getElementsByTagName("head").item(0);
		DOMElement body = html.getElementsByTagName("body").item(0);
		DOMElement newBody = xhtmlDoc.createElement("body");
		DOMElement newHead = xhtmlDoc.createElement("head");
		try {
			html.replaceChild(newBody, head);
			fail("Should throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			html.replaceChild(newHead, body);
			fail("Should throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testIsSafeOrigin() throws MalformedURLException {
		URL url = new URL(xhtmlDoc.getDocumentURI());
		assertTrue(xhtmlDoc.isSafeOrigin(url));
	}
}
