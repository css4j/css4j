/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import io.sf.carte.doc.DOMTokenList;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;

public class DOMElementTest {

	private TestDOMImplementation impl;
	private DOMDocument xhtmlDoc;

	@Before
	public void setUp() {
		impl = new TestDOMImplementation();
		xhtmlDoc = impl.createDocument("", null, null);
		assertFalse(xhtmlDoc instanceof HTMLDocument);
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		DocumentType doctype = impl.createDocumentType("html", null, null);
		DOMElement elm = xhtmlDoc.createElement("html");
		xhtmlDoc.appendChild(doctype);
		xhtmlDoc.appendChild(elm);
	}

	@Test
	public void testSetNodeValue() {
		DOMElement p = xhtmlDoc.createElement("p");
		p.setNodeValue("foo"); // No effect
	}

	@Test
	public void testGetAttributes() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		AttributeNamedNodeMap attrs = body.getAttributes();
		assertNotNull(attrs);
		assertEquals(0, attrs.getLength());
		Attr idattr = xhtmlDoc.createAttribute("id");
		idattr.setValue("bodyId");
		body.setAttributeNode(idattr);
		Node node = attrs.getNamedItem("id");
		assertNotNull(node);
		assertEquals(Node.ATTRIBUTE_NODE, node.getNodeType());
		assertEquals("id", node.getNodeName());
		assertEquals("bodyId", node.getNodeValue());
		assertNull(node.getNextSibling());
		assertNull(node.getParentNode());
		assertTrue(idattr == node);
		assertEquals(1, attrs.getLength());
		assertTrue(idattr == attrs.item(0));
		Attr attr = xhtmlDoc.createAttribute("lang");
		attr.setValue("en");
		attrs.setNamedItem(attr);
		assertEquals(2, attrs.getLength());
		assertTrue(body.hasAttribute("lang"));
		assertTrue(idattr.getNextSibling() == attr);
		assertTrue(idattr == attr.getPreviousSibling());
		assertTrue(idattr == attrs.getNamedItem("id"));
		assertTrue(attr == attrs.getNamedItem("lang"));
		assertTrue(attr == attrs.removeNamedItem("lang"));
		assertEquals(1, attrs.getLength());
		assertTrue(idattr == attrs.getNamedItem("id"));
		assertTrue(idattr == attrs.item(0));
		//
		attrs.setNamedItem(attr);
		Attr xmlns = xhtmlDoc.createAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns");
		xmlns.setValue(HTMLDocument.HTML_NAMESPACE_URI);
		attrs.setNamedItem(xmlns);
		assertEquals(3, attrs.getLength());
		assertTrue(body.hasAttribute("xmlns"));
		Attr version = xhtmlDoc.createAttributeNS("http://www.w3.org/2000/svg", "version");
		version.setValue("1.1");
		attrs.setNamedItem(version);
		assertEquals(4, attrs.getLength());
		assertTrue(body.hasAttribute("version"));
		assertTrue(body.hasAttributeNS("http://www.w3.org/2000/svg", "version"));
		attrs.removeNamedItemNS("http://www.w3.org/2000/svg", "version");
		assertEquals(3, attrs.getLength());
		assertFalse(body.hasAttribute("version"));
		assertFalse(body.hasAttributeNS("http://www.w3.org/2000/svg", "version"));
		// Iterable
		int i = 0;
		for (Attr attrnode : attrs) {
			assertTrue(attrnode == attrs.item(i));
			i++;
		}
		assertNull(attrs.item(i));
	}

	@Test
	public void testGetAttributeNS() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		Attr idattr = xhtmlDoc.createAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "id");
		idattr.setValue("bodyId");
		body.setAttributeNode(idattr);
		assertNull(idattr.getParentNode());
		assertEquals("bodyId", body.getAttribute("id"));
		assertEquals("bodyId", body.getAttributeNode("id").getValue());
		assertEquals("bodyId", body.getAttributeNodeNS(HTMLDocument.HTML_NAMESPACE_URI, "id").getValue());
		assertTrue(body.hasAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "id"));
		assertFalse(body.hasAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "foo"));
		assertEquals("", body.getAttribute("foo"));
		assertNull(body.getAttributeNode("foo"));
		assertEquals("", body.getAttributeNS("http://www.w3.org/2000/svg", "id"));
		assertNull(body.getAttributeNodeNS("http://www.w3.org/2000/svg", "id"));
		assertNotNull(idattr.getOwnerElement());
		html.appendChild(body);
		body.removeAttribute("foo");
		assertTrue(body.hasAttributes());
		body.removeAttributeNS("http://www.w3.org/2000/svg", "id");
		assertTrue(body.hasAttribute("id"));
		idattr = body.removeAttributeNode(idattr);
		assertNull(idattr.getOwnerElement());
		assertFalse(body.hasAttributes());
		body.setAttributeNode(idattr);
		assertTrue(body.hasAttribute("id"));
		assertNotNull(idattr.getOwnerElement());
		body.removeAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "id");
		assertFalse(body.hasAttribute("id"));
		assertFalse(body.hasAttributes());
		assertNull(idattr.getOwnerElement());
		DOMElement svg = xhtmlDoc.createElementNS("http://www.w3.org/2000/svg", "svg");
		Attr version = xhtmlDoc.createAttributeNS("http://www.w3.org/2000/svg", "version");
		version.setValue("1.1");
		assertEquals("http://www.w3.org/2000/svg", version.getNamespaceURI());
		svg.setAttributeNodeNS(version);
		assertNotNull(version.getOwnerElement());
		assertEquals("1.1", svg.getAttribute("version"));
		assertEquals("1.1", svg.getAttributeNode("version").getValue());
		assertEquals("1.1", svg.getAttributeNodeNS("http://www.w3.org/2000/svg", "version").getValue());
		assertEquals("", svg.getAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "version"));
		assertNull(svg.getAttributeNodeNS(HTMLDocument.HTML_NAMESPACE_URI, "version"));
		assertFalse(svg.hasAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "version"));
		assertTrue(svg.hasAttributeNS("http://www.w3.org/2000/svg", "version"));
		svg.removeAttributeNS(HTMLDocument.HTML_NAMESPACE_URI, "version");
		assertTrue(svg.hasAttributeNS("http://www.w3.org/2000/svg", "version"));
		svg.removeAttributeNS("http://www.w3.org/2000/svg", "version");
		assertFalse(svg.hasAttributeNS("http://www.w3.org/2000/svg", "version"));
		assertNull(version.getOwnerElement());
		body.appendChild(svg);
	}

	@Test
	public void testSetAttribute() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		assertFalse(body.hasAttributes());
		body.setAttribute("foo", "bar");
		assertTrue(body.hasAttributes());
		assertEquals("bar", body.getAttribute("foo"));
		Attr attr = body.getAttributeNode("foo");
		assertFalse(attr.isId());
		assertNull(attr.getSchemaTypeInfo().getTypeName());
		assertEquals("https://www.w3.org/TR/xml/", attr.getSchemaTypeInfo().getTypeNamespace());
		body.setAttribute("id", "bodyId");
		assertTrue(body.hasAttributes());
		body.setIdAttribute("id", true);
		assertEquals(2, body.getAttributes().getLength());
		assertEquals("bodyId", body.getAttribute("id"));
		attr = body.getAttributeNode("id");
		assertTrue(attr.isId());
		assertEquals("ID", attr.getSchemaTypeInfo().getTypeName());
		assertEquals("https://www.w3.org/TR/xml/", attr.getSchemaTypeInfo().getTypeNamespace());
		body.setAttribute("id", "newId");
		assertEquals("newId", body.getAttribute("id"));
		assertTrue(attr == body.getAttributeNode("id"));
		assertEquals(2, body.getAttributes().getLength());
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
	}

	@Test
	public void testSetIdAttributeError() {
		DOMElement p = xhtmlDoc.createElement("p");
		try {
			p.setIdAttribute("id", true);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		p.setAttribute("id", "fooId");
		p.setIdAttribute("id", true);
		try {
			p.setIdAttributeNS("http://www.example.com/examplens", "id", true);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
	}

	@Test
	public void testSetAttributeNode() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("bodyId");
		assertFalse(attr.isId());
		body.setAttributeNode(attr);
		body.setIdAttributeNode(attr, true);
		assertTrue(body.hasAttributes());
		assertTrue(attr.isId());
		assertNull(attr.getParentNode());
		assertNotNull(attr.getOwnerElement());
		assertEquals("bodyId", body.getAttribute("id"));
		assertEquals(1, body.getAttributes().getLength());
		assertEquals("bodyId", body.getId());
		assertNull(attr.lookupPrefix(HTMLDocument.HTML_NAMESPACE_URI));
		assertEquals(DOMDocument.XML_NAMESPACE_URI, attr.lookupNamespaceURI("xml"));
		assertNull(attr.lookupNamespaceURI("foo"));
		// Set the attribute to itself
		assertNull(body.setAttributeNode(attr));
		assertEquals(1, body.getAttributes().getLength());
		// Remove
		Attr rmattr = body.removeAttributeNode(attr);
		assertTrue(rmattr == attr);
		assertFalse(body.hasAttributes());
		assertEquals(0, body.getAttributes().getLength());
		assertNull(attr.getOwnerElement());
		assertEquals("bodyId", attr.getValue());
		assertEquals("", body.getId());
		// Class attribute
		body.setAttribute("class", "fooclass");
		assertTrue(body.hasAttributes());
		assertEquals("fooclass", body.getAttribute("class"));
		assertFalse(body.getAttributeNode("class").isId());
		// Replace class attribute, first with another namespace
		attr = xhtmlDoc.createAttributeNS("http://www.example.com/examplens", "e:class");
		attr.setValue("barclass");
		assertEquals("class", attr.getLocalName());
		assertEquals("http://www.example.com/examplens", attr.getNamespaceURI());
		body.setAttributeNodeNS(attr);
		assertEquals("e:class=\"barclass\"", attr.toString());
		assertEquals("http://www.example.com/examplens", attr.lookupNamespaceURI("e"));
		assertEquals("e", attr.lookupPrefix("http://www.example.com/examplens"));
		assertNull(attr.lookupPrefix(null));
		assertEquals("fooclass", body.getAttribute("class"));
		attr = xhtmlDoc.createAttribute("class");
		attr.setValue("barclass");
		body.setAttributeNode(attr);
		assertEquals("barclass", body.getAttribute("class"));
		Attr attr2 = body.getAttributeNode("class");
		assertTrue(attr == attr2);
		assertNull(attr.getSchemaTypeInfo().getTypeName());
		// Attribute from another element
		try {
			html.setAttributeNode(attr);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INUSE_ATTRIBUTE_ERR, e.code);
		}
		try {
			html.setAttributeNodeNS(attr);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INUSE_ATTRIBUTE_ERR, e.code);
		}
		// Another document
		DOMDocument otherdoc = xhtmlDoc.getImplementation().createDocument(null, null, null);
		Attr otherdocAttr = otherdoc.createAttribute("foo");
		try {
			body.setAttributeNode(otherdocAttr);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
		try {
			body.setAttributeNodeNS(otherdocAttr);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
	}

	@Test
	public void testSetAttributeNodeNS() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		Attr xsi = xhtmlDoc.createAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns:xsi");
		xsi.setValue("http://www.w3.org/2001/XMLSchema-instance");
		assertFalse(xsi.isId());
		html.setAttributeNode(xsi);
		assertTrue(html.hasAttributes());
		assertNull(xsi.getParentNode());
		assertNotNull(xsi.getOwnerElement());
		Attr attr = xhtmlDoc.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation");
		attr.setValue("http://www.w3.org/1999/xhtml https://www.w3.org/2002/08/xhtml/xhtml1-transitional.xsd");
		html.setAttributeNode(attr);
		assertTrue(html.hasAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
		assertTrue(html.hasAttribute("xsi:schemaLocation"));
		assertNotNull(html.getAttributeNodeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
		assertNotNull(html.getAttributeNode("xsi:schemaLocation"));
		assertEquals("http://www.w3.org/1999/xhtml https://www.w3.org/2002/08/xhtml/xhtml1-transitional.xsd",
				html.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
		assertEquals("http://www.w3.org/1999/xhtml https://www.w3.org/2002/08/xhtml/xhtml1-transitional.xsd",
				html.getAttribute("xsi:schemaLocation"));
		assertEquals(2, html.getAttributes().getLength());
		assertEquals("http://www.w3.org/2001/XMLSchema-instance", attr.lookupNamespaceURI("xsi"));
		assertEquals("xsi", attr.lookupPrefix("http://www.w3.org/2001/XMLSchema-instance"));
		attr.setPrefix("sch");
		assertTrue(html.hasAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
		assertTrue(html.hasAttribute("sch:schemaLocation"));
		assertNotNull(html.getAttributeNodeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
		assertNotNull(html.getAttributeNode("sch:schemaLocation"));
		assertEquals("http://www.w3.org/1999/xhtml https://www.w3.org/2002/08/xhtml/xhtml1-transitional.xsd",
				html.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
		assertEquals("http://www.w3.org/1999/xhtml https://www.w3.org/2002/08/xhtml/xhtml1-transitional.xsd",
				html.getAttribute("sch:schemaLocation"));
	}

	@Test
	public void testSetAttributeNodeNSXML() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		Attr xml = xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "xml:lang");
		xml.setValue("en");
		assertEquals(DOMDocument.XML_NAMESPACE_URI, xml.getNamespaceURI());
		assertEquals("lang", xml.getLocalName());
		assertEquals("xml:lang", xml.getName());
		assertFalse(xml.isId());
		html.setAttributeNode(xml);
		assertTrue(html.hasAttributes());
		assertNull(xml.getParentNode());
		assertNotNull(xml.getOwnerElement());
		assertTrue(html.hasAttributeNS(DOMDocument.XML_NAMESPACE_URI, "lang"));
		Attr attr = xhtmlDoc.createAttribute("lang");
		attr.setValue("en");
		html.setAttributeNode(attr);
		assertTrue(html.hasAttributeNS(DOMDocument.XML_NAMESPACE_URI, "lang"));
		assertTrue(html.hasAttributeNS(null, "lang"));
		assertTrue(html.hasAttribute("lang"));
		assertTrue(attr == html.getAttributeNode("lang"));
		assertEquals(2, html.getAttributes().getLength());
		// Recognize xml:id as an implicit 'id' attribute.
		Attr idattr = xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "xml:id");
		idattr.setValue("foo");
		html.setAttributeNode(idattr);
		assertTrue(idattr.isId());
		assertTrue(html.hasAttributeNS(DOMDocument.XML_NAMESPACE_URI, "id"));
		assertTrue(idattr == html.getAttributeNodeNS(DOMDocument.XML_NAMESPACE_URI, "id"));
		assertEquals("foo", html.getId());
		assertEquals(3, html.getAttributes().getLength());
		assertTrue(idattr == html.removeAttributeNode(idattr));
		assertEquals("", html.getId());
		assertEquals(2, html.getAttributes().getLength());
		//
		try {
			xhtmlDoc.createAttributeNS("http://www.example.com/ns", "xml:foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void testSetAttributeNodeClass() {
		ElementList fooelms = xhtmlDoc.getElementsByClassName("foo");
		assertEquals(0, fooelms.getLength());
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("class");
		attr.setValue("foo bar");
		assertEquals("class", attr.getName());
		assertEquals("foo bar", attr.getValue());
		body.setAttributeNode(attr);
		assertTrue(body.hasAttribute("class"));
		assertNull(attr.getParentNode());
		assertTrue(body == attr.getOwnerElement());
		assertEquals("foo bar", attr.getValue());
		assertEquals(1, fooelms.getLength());
		assertTrue(fooelms == xhtmlDoc.getElementsByClassName("foo"));
		assertTrue(body == fooelms.item(0));
		ElementList barelms = xhtmlDoc.getElementsByClassName("bar");
		assertEquals(1, barelms.getLength());
		ElementList foobarelms = xhtmlDoc.getElementsByClassName("bar foo");
		assertEquals(1, foobarelms.getLength());
		assertTrue(body == foobarelms.item(0));
		body.getClassList().remove("bar");
		assertEquals(0, barelms.getLength());
		assertEquals(0, foobarelms.getLength());
		body.getClassList().toggle("bar");
		assertEquals(1, barelms.getLength());
		body.removeAttribute("class");
		assertNull(attr.getOwnerElement());
		assertEquals("foo bar", attr.getValue());
		assertEquals(0, fooelms.getLength());
		assertEquals(0, barelms.getLength());
	}

	@Test
	public void testGetClassList() {
		assertEquals(CSSDocument.ComplianceMode.STRICT, xhtmlDoc.getComplianceMode());
		DOMElement body = xhtmlDoc.createElement("body");
		DOMTokenList list = body.getClassList();
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertFalse(list.contains("foo"));
		assertFalse(body.hasAttribute("class"));
		Attr attr = xhtmlDoc.createAttribute("class");
		attr.setValue("foo");
		body.setAttributeNode(attr);
		assertEquals(1, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("foo", list.getValue());
		assertTrue(list.contains("foo"));
		attr.setValue("foo bar");
		assertEquals(2, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("foo bar", list.getValue());
		assertTrue(list.contains("foo"));
		list.add("000");
		assertEquals(3, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("000", list.item(2));
		assertEquals("foo bar 000", list.getValue());
		assertEquals("foo bar 000", attr.getValue());
		assertTrue(list.contains("foo"));
		list.toggle("111");
		assertEquals(4, list.getLength());
		list.toggle("111");
		assertEquals(3, list.getLength());
		list.replace("000", "111");
		assertEquals("foo bar 111", list.getValue());
		list.remove("111");
		assertEquals(2, list.getLength());
		assertEquals("foo bar", list.getValue());
		body.removeAttribute("class");
		assertEquals(0, list.getLength());
		assertEquals("foo bar", attr.getValue());
	}

	@Test
	public void testGetClassListQuirks() {
		xhtmlDoc.removeChild(xhtmlDoc.getDoctype());
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, xhtmlDoc.getComplianceMode());
		DOMElement body = xhtmlDoc.createElement("body");
		DOMTokenList list = body.getClassList();
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertFalse(list.contains("foo"));
		assertFalse(body.hasAttribute("class"));
		Attr attr = xhtmlDoc.createAttribute("class");
		attr.setValue("foo");
		body.setAttributeNode(attr);
		assertEquals(1, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("foo", list.getValue());
		assertTrue(list.contains("foo"));
		attr.setValue("foo bar");
		assertEquals(2, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("foo bar", list.getValue());
		assertTrue(list.contains("foo"));
		list.add("000");
		assertEquals(3, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("000", list.item(2));
		assertEquals("foo bar 000", list.getValue());
		assertEquals("foo bar 000", attr.getValue());
		list.toggle("111");
		assertEquals(4, list.getLength());
		list.toggle("111");
		assertEquals(3, list.getLength());
		list.replace("000", "111");
		assertEquals("foo bar 111", list.getValue());
		list.remove("111");
		assertEquals(2, list.getLength());
		assertEquals("foo bar", list.getValue());
		body.removeAttribute("class");
		assertEquals(0, list.getLength());
		assertEquals("foo bar", attr.getValue());
	}

	@Test
	public void testGetClassList2() {
		DOMElement body = xhtmlDoc.createElement("body");
		DOMTokenList list = body.getClassList();
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list.add("foo");
		assertTrue(body.hasAttribute("class"));
		assertEquals("foo", body.getAttribute("class"));
		list.add("foo");
		assertEquals("foo", body.getAttribute("class"));
		list.add("bar");
		assertEquals("foo bar", body.getAttribute("class"));
		try {
			list.add(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			list.add("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.SYNTAX_ERR, e.code);
		}
		try {
			list.add("foo bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		list.replace("bar", "faa");
		assertEquals("foo faa", body.getAttribute("class"));
		list.replace("faa", "foo");
		assertEquals("foo", body.getAttribute("class"));
		body.setClassName("bar");
		assertEquals("bar", body.getAttribute("class"));
		assertEquals("bar", list.getValue());
	}

	@Test
	public void matchesStringString() {
		assertFalse(xhtmlDoc instanceof HTMLDocument);
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		DOMElement div1 = xhtmlDoc.createElement("div");
		DOMElement div2 = xhtmlDoc.createElement("div");
		body.appendChild(div1);
		body.appendChild(div2);
		assertFalse(body.matches(".foo", null));
		DOMTokenList list = body.getClassList();
		assertNotNull(list);
		assertEquals(0, list.getLength());
		Attr attr = xhtmlDoc.createAttribute("class");
		attr.setValue("foo");
		body.setAttributeNode(attr);
		assertTrue(body.matches(".foo", null));
		assertTrue(body.matches("#bodyId", null));
		attr.setValue("foo bar");
		assertTrue(body.matches(".bar", null));
		assertTrue(div1.matches(".bar div", null));
		assertTrue(div1.matches("body > div", null));
		assertTrue(div1.matches("div:first-child", null));
		assertFalse(div2.matches("div:first-child", null));
		assertTrue(div2.matches("div:last-child", null));
		assertTrue(div1.matches("div:first-line", "::first-line"));
		body.removeAttribute("class");
		assertFalse(body.matches(".bar", null));
	}

	@Test
	public void matchesStringStringQuirks() {
		xhtmlDoc.removeChild(xhtmlDoc.getDoctype());
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, xhtmlDoc.getComplianceMode());
		assertFalse(xhtmlDoc instanceof HTMLDocument);
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		body.setAttribute("id", "bodyId");
		body.setIdAttribute("id", true);
		DOMElement div1 = xhtmlDoc.createElement("div");
		DOMElement div2 = xhtmlDoc.createElement("div");
		body.appendChild(div1);
		body.appendChild(div2);
		assertFalse(body.matches(".foo", null));
		DOMTokenList list = body.getClassList();
		assertNotNull(list);
		assertEquals(0, list.getLength());
		Attr attr = xhtmlDoc.createAttribute("class");
		attr.setValue("foo");
		body.setAttributeNode(attr);
		assertTrue(body.matches(".foo", null));
		assertTrue(body.matches("#bodyId", null));
		attr.setValue("foo bar");
		assertTrue(body.matches(".bar", null));
		assertTrue(div1.matches(".bar div", null));
		assertTrue(div1.matches("body > div", null));
		assertTrue(div1.matches("div:first-child", null));
		assertFalse(div2.matches("div:first-child", null));
		assertTrue(div2.matches("div:last-child", null));
		assertTrue(div1.matches("div:first-line", "::first-line"));
		body.removeAttribute("class");
		assertFalse(body.matches(".bar", null));
	}

	@Test
	public void matchesStringString2() {
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		body.setAttribute("id", "bodyId");
		DOMElement div1 = xhtmlDoc.createElement("div");
		DOMElement div2 = xhtmlDoc.createElement("div");
		body.appendChild(div1);
		body.appendChild(div2);
		assertTrue(div2.matches("div:last-child", null));
		assertFalse(div1.matches("div:last-child", null));
		assertFalse(body.matches("div:last-child", null));
		ElementList elements = xhtmlDoc.querySelectorAll("div:last-child");
		assertNotNull(elements);
		assertEquals(1, elements.getLength());
		assertTrue(div2 == elements.item(0));
		elements = xhtmlDoc.querySelectorAll("div:first-child");
		assertNotNull(elements);
		assertEquals(1, elements.getLength());
		assertTrue(div1 == elements.item(0));
		elements = xhtmlDoc.querySelectorAll("#nosuchID");
		assertNotNull(elements);
		assertEquals(0, elements.getLength());
	}

	@Test
	public void testGetStyle() {
		DOMElement body = xhtmlDoc.createElement("body");
		assertNull(body.getStyle());
		assertFalse(body.hasAttributes());
		xhtmlDoc.getDocumentElement().appendChild(body);
		body.setAttribute("style", "font-family: Arial");
		assertTrue(body.hasAttributes());
		assertTrue(body.hasAttribute("style"));
		assertEquals("font-family: Arial; ", body.getAttribute("style"));
		ExtendedCSSStyleDeclaration style = body.getStyle();
		assertNotNull(style);
		assertEquals(1, style.getLength());
		assertEquals("font-family: Arial; ", style.getCssText());
		style.setCssText("font-family: Helvetica");
		assertEquals("font-family: Helvetica; ", style.getCssText());
		assertEquals("font-family: Helvetica; ", body.getAttribute("style"));
		body.removeAttribute("style");
	}

	@Test
	public void testCreateElement() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		assertNull(body.getNamespaceURI());
		DOMElement svg = xhtmlDoc.createElementNS("http://www.w3.org/2000/svg", "svg");
		body.appendChild(svg);
		assertEquals("http://www.w3.org/2000/svg", svg.getNamespaceURI());
		DOMElement p = xhtmlDoc.createElementNS(null, "p");
		body.appendChild(p);
		assertNull(p.getNamespaceURI());
	}

	@Test
	public void testGetChildren() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		DOMElement div1 = xhtmlDoc.createElement("div");
		body.appendChild(div1);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		DOMElement div2 = xhtmlDoc.createElement("div");
		body.appendChild(div2);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		DOMElement div3 = xhtmlDoc.createElement("div");
		body.appendChild(div3);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		body.appendChild(xhtmlDoc.createComment("This is a comment"));
		DOMElement div4 = xhtmlDoc.createElement("div");
		body.appendChild(div4);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		ElementList list = body.getChildren();
		assertNotNull(list);
		assertEquals(4, list.getLength());
		assertTrue(div1 == list.item(0));
		assertTrue(div2 == list.item(1));
		assertTrue(div3 == list.item(2));
		assertTrue(div4 == list.item(3));
		assertNull(list.item(4));
		assertTrue(div1 == body.getFirstElementChild());
		assertTrue(div4 == body.getLastElementChild());
		assertEquals(list.getLength(), body.getChildElementCount());
		assertTrue(list == body.getChildren());
		list = xhtmlDoc.getChildren();
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertTrue(html == list.item(0));
		assertEquals(1, xhtmlDoc.getChildElementCount());
		list = html.getChildren();
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertTrue(body == list.item(0));
		assertNull(list.item(1));
		assertNull(list.item(-1));
		assertEquals(1, html.getChildElementCount());
		assertTrue(body == html.getFirstElementChild());
		assertTrue(body == html.getLastElementChild());
		assertTrue(list == html.getChildren());
	}

	@Test
	public void testGetChildNodes() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		Text first = xhtmlDoc.createTextNode("\n   \n");
		body.appendChild(first);
		DOMElement div1 = xhtmlDoc.createElement("div");
		body.appendChild(div1);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		DOMElement div2 = xhtmlDoc.createElement("div");
		body.appendChild(div2);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		DOMElement div3 = xhtmlDoc.createElement("div");
		body.appendChild(div3);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		body.appendChild(xhtmlDoc.createComment("This is a comment"));
		DOMElement div4 = xhtmlDoc.createElement("div");
		body.appendChild(div4);
		body.appendChild(xhtmlDoc.createTextNode("\n   \n"));
		Text last = xhtmlDoc.createTextNode("\n   \n");
		body.appendChild(last);
		DOMNodeList list = body.getChildNodes();
		assertNotNull(list);
		assertEquals(11, list.getLength());
		assertTrue(first == list.item(0));
		assertTrue(div1 == list.item(1));
		assertTrue(div2 == list.item(3));
		assertTrue(div3 == list.item(5));
		assertTrue(div4 == list.item(8));
		assertTrue(last == list.item(10));
		assertNull(list.item(11));
		assertNull(list.item(-1));
		assertTrue(first == body.getFirstChild());
		assertTrue(last == body.getLastChild());
		assertTrue(list == body.getChildNodes());
		assertTrue(list.contains(div4));
		assertFalse(list.contains(html));
		//
		list = xhtmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertTrue(html == list.item(1));
		assertTrue(xhtmlDoc.getDoctype() == xhtmlDoc.getFirstChild());
		assertFalse(list.contains(div4));
		assertTrue(list.contains(html));
		list = html.getChildNodes();
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertTrue(body == list.item(0));
		assertNull(list.item(1));
		assertNull(list.item(-1));
		assertTrue(body == html.getFirstChild());
		assertTrue(body == html.getLastChild());
		assertTrue(list == html.getChildNodes());
		assertTrue(list.contains(body));
		assertFalse(list.contains(div4));
	}

	@Test
	public void testCloneNode() {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("bodyId");
		body.setAttributeNode(attr);
		body.setIdAttributeNode(attr, true);
		body.setAttribute("class", "fooclass");
		DOMElement div = xhtmlDoc.createElement("div");
		body.appendChild(div);
		div.appendChild(xhtmlDoc.createTextNode("foo"));
		DOMElement elm = body.cloneNode(false);
		assertEquals(body.getNodeName(), elm.getNodeName());
		assertTrue(body.getAttributes().equals(elm.getAttributes()));
		Attr cloneId = elm.getAttributeNode("id");
		assertTrue(cloneId.isId());
		assertFalse(body.isEqualNode(elm));
		elm.setIdAttributeNode(cloneId, false);
		assertFalse(cloneId.isId());
		elm = body.cloneNode(true);
		assertTrue(body.isEqualNode(elm));
		assertEquals("div", elm.getChildNodes().item(0).getNodeName());
		assertEquals("foo", elm.getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
	}

	@Test
	public void testGetElementById() {
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("myId");
		Attr fooAttr = xhtmlDoc.createAttribute("foo");
		fooAttr.setValue("bar");
		body.setAttributeNode(attr);
		body.setAttributeNode(fooAttr);
		body.setIdAttributeNode(attr, true);
		assertTrue(body == xhtmlDoc.getElementById("myId"));
		body.setIdAttributeNode(attr, false);
		assertNull(xhtmlDoc.getElementById("myId"));
		body.setIdAttributeNode(fooAttr, true);
		assertTrue(body == xhtmlDoc.getElementById("bar"));
		body.setIdAttributeNode(fooAttr, false);
		assertNull(xhtmlDoc.getElementById("bar"));
		// test for xml:id
		attr = xhtmlDoc.createAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:id");
		attr.setValue("xmlId");
		body.setAttributeNode(attr);
		assertEquals("xmlId", body.getAttributeNS("http://www.w3.org/XML/1998/namespace", "id"));
		assertTrue(body == xhtmlDoc.getElementById("xmlId"));
	}

	@Test
	public void testGetStartTag() {
		DOMElement elm = xhtmlDoc.createElement("p");
		Attr attr = xhtmlDoc.createAttribute("foo");
		attr.setValue("bar\"");
		elm.setAttributeNode(attr);
		assertEquals("<p foo=\"bar&quot;\" />", elm.getStartTag());
	}

	@Test
	public void testGetTagName() {
		DOMElement elm = xhtmlDoc.createElement("p");
		assertEquals("p", elm.getTagName());
		elm = xhtmlDoc.createElementNS("http://www.example.com/examplens", "e:p");
		assertEquals("p", elm.getLocalName());
		assertEquals("e:p", elm.getTagName());
	}

	@Test
	public void testToString() {
		DOMElement elm = xhtmlDoc.createElement("p");
		Attr attr = xhtmlDoc.createAttribute("foo");
		attr.setValue("bar\"");
		elm.setAttributeNode(attr);
		assertEquals("<p foo=\"bar&quot;\"></p>", elm.toString());
	}

	@Test
	public void testToString2() {
		DOMElement elm = xhtmlDoc.createElement("link");
		Attr attr = xhtmlDoc.createAttribute("href");
		attr.setValue("http://www.example.com/");
		elm.setAttributeNode(attr);
		DocumentType docType = xhtmlDoc.getDoctype();
		xhtmlDoc.removeChild(docType);
		assertEquals("<link href=\"http://www.example.com/\"></link>", elm.toString());
		DocumentType newDocType = impl.createDocumentType("html", "-//W3C//DTD XHTML 1.0 Transitional//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		assertEquals(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				newDocType.toString());
		xhtmlDoc.prependChild(newDocType);
		assertEquals("<link href=\"http://www.example.com/\" />", elm.toString());
	}

}
