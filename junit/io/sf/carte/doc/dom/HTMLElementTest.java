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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;

import io.sf.carte.doc.DOMTokenList;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;

public class HTMLElementTest {
	TestDOMImplementation impl;
	HTMLDocument xhtmlDoc;

	@Before
	public void setUp() {
		impl = new TestDOMImplementation();
		DocumentType dtd = impl.createDocumentType("html", null, null);
		xhtmlDoc = (HTMLDocument) impl.createDocument(null, null, dtd);
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		CSSElement elm = xhtmlDoc.createElement("html");
		xhtmlDoc.appendChild(elm);
	}

	@Test
	public void testCreateElementError() {
		try {
			xhtmlDoc.createElement("p'");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSError() {
		try {
			xhtmlDoc.createElementNS("http://www.example.com/examplens", "e:p'");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSError2() {
		// Cause an error
		try {
			xhtmlDoc.createElementNS(null, "foo:bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
		}
	}

	@Test
	public void testCreateAttributeStyle() {
		Attr attr = xhtmlDoc.createAttribute("style");
		assertNotNull(attr);
		attr.setValue("display:block");
		assertNull(attr.getAttributes());
		assertEquals(0, attr.getChildNodes().getLength());
		assertNull(attr.getFirstChild());
		assertNull(attr.getLastChild());
		assertNull(attr.getParentNode());
		assertTrue(attr.getSpecified());
		assertEquals("style", attr.getName());
		assertEquals("style", attr.getNodeName());
		assertEquals("display: block; ", attr.getValue());
		assertEquals("display: block; ", attr.getNodeValue());
		HTMLElement html = xhtmlDoc.getDocumentElement();
		assertNull(html.getStyle());
		html.setAttributeNode(attr);
		CSSStyleDeclaration style = html.getStyle();
		assertNotNull(style);
		assertEquals("display: block; ", style.getCssText());
		assertEquals("display:block", style.getMinifiedCssText());
		assertEquals("display: block; ", html.getAttribute("style"));
		style.setCssText("margin-top: 10%");
		assertEquals("margin-top: 10%; ", html.getAttribute("style"));
	}

	@Test
	public void testCreateAttributeNS() {
		Attr attr = xhtmlDoc.createAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns");
		assertNotNull(attr);
		attr.setValue(HTMLDocument.HTML_NAMESPACE_URI);
		assertNull(attr.getAttributes());
		assertEquals(0, attr.getChildNodes().getLength());
		assertNull(attr.getFirstChild());
		assertNull(attr.getLastChild());
		assertNull(attr.getParentNode());
		assertTrue(attr.getSpecified());
		assertEquals("xmlns", attr.getName());
		assertEquals("http://www.w3.org/2000/xmlns/", attr.getNamespaceURI());
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, attr.getValue());
		assertEquals("xmlns", attr.getNodeName());
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, attr.getNodeValue());
		assertEquals("xmlns=\"" + HTMLDocument.HTML_NAMESPACE_URI + '\"', attr.toString());
	}

	@Test
	public void testCreateAttributeNS2() {
		Attr attr = xhtmlDoc.createAttributeNS("http://www.w3.org/2000/svg", "version");
		assertNotNull(attr);
		attr.setValue("1.1");
		assertNull(attr.getAttributes());
		assertEquals(0, attr.getChildNodes().getLength());
		assertNull(attr.getFirstChild());
		assertNull(attr.getLastChild());
		assertNull(attr.getParentNode());
		assertTrue(attr.getSpecified());
		assertEquals("http://www.w3.org/2000/svg", attr.getNamespaceURI());
		assertEquals("version", attr.getName());
		assertEquals("version", attr.getNodeName());
		assertEquals("1.1", attr.getValue());
		assertEquals("1.1", attr.getNodeValue());
		assertEquals("version=\"1.1\"", attr.toString());
	}

	@Test
	public void testCreateAttributeNSError() {
		try {
			xhtmlDoc.createAttributeNS("http://www.example.com/examplens", "xmlns");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		Attr attr = xhtmlDoc.createAttributeNS("http://www.example.com/examplens", "doc");
		try {
			attr.setPrefix("xmlns");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			attr.setPrefix("xml");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			attr.setPrefix("foo:");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xhtmlDoc.createAttributeNS(null, "foo:bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		attr = xhtmlDoc.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "doc");
		attr.setPrefix("xml");
	}

	@Test
	public void setAttribute() {
		CSSElement html = xhtmlDoc.getDocumentElement();
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
	public void setAttributeCI() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("BODY");
		html.appendChild(body);
		assertFalse(body.hasAttributes());
		body.setAttribute("FOO", "bar");
		assertTrue(body.hasAttributes());
		assertTrue(body.hasAttribute("foo"));
		assertEquals("bar", body.getAttribute("foo"));
		Attr attr = body.getAttributeNode("FOO");
		assertFalse(attr.isId());
		assertNull(attr.getSchemaTypeInfo().getTypeName());
		assertEquals("https://www.w3.org/TR/xml/", attr.getSchemaTypeInfo().getTypeNamespace());
		body.setAttribute("ID", "bodyId");
		assertTrue(body.hasAttributes());
		assertEquals(2, body.getAttributes().getLength());
		assertEquals("bodyId", body.getAttribute("ID"));
		attr = body.getAttributeNode("ID");
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
	public void setAttributeNode() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("bodyId");
		body.setAttributeNode(attr);
		assertTrue(body.hasAttributes());
		assertTrue(attr.isId());
		assertNull(attr.getParentNode());
		assertNotNull(attr.getOwnerElement());
		assertEquals("bodyId", body.getAttribute("id"));
		assertEquals(1, body.getAttributes().getLength());
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
		// Class attribute
		body.setAttribute("class", "fooclass");
		assertTrue(body.hasAttributes());
		assertEquals("fooclass", body.getAttribute("class"));
		assertFalse(body.getAttributeNode("class").isId());
		// Replace class attribute, first with another namespace
		attr = xhtmlDoc.createAttributeNS("http://www.example.com/examplens", "e:class");
		attr.setValue("barclass");
		assertEquals("class", attr.getLocalName());
		body.setAttributeNodeNS(attr);
		assertEquals("e:class=\"barclass\"", attr.toString());
		assertEquals("fooclass", body.getAttribute("class"));
		attr = xhtmlDoc.createAttribute("class");
		attr.setValue("barclass");
		body.setAttributeNode(attr);
		assertEquals("barclass", body.getAttribute("class"));
		Attr attr2 = body.getAttributeNode("class");
		assertTrue(attr == attr2);
	}

	@Test
	public void setAttributeNodeCI() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("ID");
		attr.setValue("bodyId");
		body.setAttributeNode(attr);
		assertTrue(body.hasAttributes());
		assertTrue(attr.isId());
		assertNull(attr.getParentNode());
		assertNotNull(attr.getOwnerElement());
		assertEquals("id", attr.getLocalName());
		assertEquals("bodyId", body.getAttribute("ID"));
		assertEquals(1, body.getAttributes().getLength());
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
		// Class attribute
		body.setAttribute("CLASS", "fooclass");
		assertTrue(body.hasAttributes());
		assertEquals("fooclass", body.getAttribute("CLASS"));
		assertFalse(body.getAttributeNode("class").isId());
		// Set CLASS attribute with another namespace
		attr = xhtmlDoc.createAttributeNS("http://www.example.com/examplens", "e:CLASS");
		attr.setValue("barclass");
		assertEquals("CLASS", attr.getLocalName());
		body.setAttributeNodeNS(attr);
		assertEquals("e:CLASS=\"barclass\"", attr.toString());
		//
		assertEquals("fooclass", body.getAttribute("CLASS"));
		attr = xhtmlDoc.createAttribute("CLASS");
		attr.setValue("barclass");
		assertEquals("class", attr.getName());
		body.setAttributeNode(attr);
		assertEquals("barclass", body.getAttribute("CLASS"));
		Attr attr2 = body.getAttributeNode("CLASS");
		assertTrue(attr == attr2);
	}

	@Test
	public void getAttributes() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		body.setAttribute("ID", "bodyId");
		AttributeNamedNodeMap nnm = body.getAttributes();
		assertNotNull(nnm);
		assertEquals(1, nnm.getLength());
		Attr attr = nnm.item(0);
		assertTrue(attr.isId());
		assertNull(attr.getParentNode());
		assertNotNull(attr.getOwnerElement());
		assertEquals("id", attr.getLocalName());
		assertSame(attr, nnm.getNamedItem("ID"));
		assertSame(attr, nnm.getNamedItemNS(null, "id"));
		assertNull(nnm.getNamedItemNS(HTMLDocument.HTML_NAMESPACE_URI, "id"));
		// Set the attribute to itself
		assertNull(nnm.setNamedItem(attr));
		assertEquals(1, nnm.getLength());
		// Remove
		Attr rmattr = nnm.removeNamedItem(attr.getName());
		assertTrue(rmattr == attr);
		assertFalse(body.hasAttributes());
		assertEquals(0, nnm.getLength());
		assertNull(attr.getOwnerElement());
		assertEquals("bodyId", attr.getValue());
		// Class attribute
		Attr classAttr = xhtmlDoc.createAttribute("CLASS");
		classAttr.setValue("fooclass");
		nnm.setNamedItem(classAttr);
		assertTrue(body.hasAttributes());
		assertEquals("fooclass", body.getAttribute("CLASS"));
		assertFalse(body.getAttributeNode("class").isId());
		assertSame(classAttr, nnm.getNamedItem("class"));
		// Set CLASS attribute with another namespace
		attr = xhtmlDoc.createAttributeNS("http://www.example.com/examplens", "e:CLASS");
		attr.setValue("barclass");
		nnm.setNamedItem(attr);
		assertSame(attr, nnm.getNamedItem("e:CLASS"));
		assertSame(attr, nnm.getNamedItemNS("http://www.example.com/examplens", "CLASS"));
		//
		assertEquals("fooclass", body.getAttribute("CLASS"));
		attr = xhtmlDoc.createAttribute("CLASS");
		attr.setValue("barclass");
		assertEquals("class", attr.getName());
		nnm.setNamedItem(attr);
		assertEquals("barclass", body.getAttribute("CLASS"));
		Attr attr2 = nnm.getNamedItem("CLASS");
		assertTrue(attr == attr2);
		assertEquals(2, nnm.getLength());
		//
		rmattr = nnm.removeNamedItem(attr.getName());
		assertTrue(rmattr == attr);
		assertEquals(1, nnm.getLength());
	}

	@Test
	public void setAttributeNodeClass() {
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
		assertEquals(fooelms.toString(), xhtmlDoc.getElementsByClassName("foo").toString());
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
	public void testSetAttributeNodeClassCI() {
		ElementList fooelms = xhtmlDoc.getElementsByClassName("foo");
		assertEquals(0, fooelms.getLength());
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("CLASS");
		attr.setValue("foo bar");
		assertEquals("class", attr.getName());
		assertEquals("foo bar", attr.getValue());
		body.setAttributeNode(attr);
		assertTrue(body.hasAttribute("CLASS"));
		assertNull(attr.getParentNode());
		assertTrue(body == attr.getOwnerElement());
		assertEquals("foo bar", attr.getValue());
		assertEquals(1, fooelms.getLength());
		assertEquals(fooelms.toString(), xhtmlDoc.getElementsByClassName("foo").toString());
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
		body.removeAttribute("CLASS");
		assertNull(attr.getOwnerElement());
		assertEquals("foo bar", attr.getValue());
		assertEquals(0, fooelms.getLength());
		assertEquals(0, barelms.getLength());
	}

	@Test
	public void getClassList() {
		DOMElement body = xhtmlDoc.createElement("body");
		DOMTokenList list = body.getClassList();
		assertNotNull(list);
		assertEquals(0, list.getLength());
		Attr attr = xhtmlDoc.createAttribute("class");
		attr.setValue("foo");
		body.setAttributeNode(attr);
		assertEquals(1, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("foo", list.getValue());
		attr.setValue("foo bar");
		assertEquals(2, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("foo bar", list.getValue());
		list.add("000");
		assertEquals(3, list.getLength());
		assertEquals("foo", list.item(0));
		assertEquals("000", list.item(2));
		assertEquals("foo bar 000", list.getValue());
		assertEquals("foo bar 000", attr.getValue());
		body.removeAttribute("class");
		assertEquals(0, list.getLength());
		assertEquals("foo bar 000", attr.getValue());
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
	}

	@Test
	public void matchesStringString() {
		DOMElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		body.setAttribute("id", "bodyId");
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
	public void createElement() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		CSSElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, body.getNamespaceURI());
		CSSElement svg = xhtmlDoc.createElementNS("http://www.w3.org/2000/svg", "svg");
		body.appendChild(svg);
		assertEquals("http://www.w3.org/2000/svg", svg.getNamespaceURI());
		CSSElement p = xhtmlDoc.createElementNS(null, "p");
		body.appendChild(p);
		assertEquals(HTMLDocument.HTML_NAMESPACE_URI, p.getNamespaceURI());
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
		CSSStyleDeclaration style = body.getStyle();
		assertNotNull(style);
		assertEquals(1, style.getLength());
		assertEquals("font-family: Arial; ", style.getCssText());
		style.setCssText("font-family: Helvetica");
		assertEquals("font-family: Helvetica; ", style.getCssText());
		assertEquals("font-family: Helvetica; ", body.getAttribute("style"));
		assertEquals("<body style=\"font-family: Helvetica; \" />", body.getStartTag());
		body.removeAttribute("style");
		// Upper case
		body.setAttribute("STYLE", "font-family: Arial");
		assertTrue(body.hasAttributes());
		assertTrue(body.hasAttribute("STYLE"));
		assertEquals("font-family: Arial; ", body.getAttribute("STYLE"));
		style = body.getStyle();
		assertNotNull(style);
		assertEquals(1, style.getLength());
		assertEquals("font-family: Arial; ", style.getCssText());
		body.removeAttribute("STYLE");
		assertNull(body.getStyle());
		//
		body.setAttribute("style", "font-family");
		assertEquals("<body style=\"font-family\" />", body.getStartTag());
	}

	@Test
	public void getChildren() {
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
	public void getAttributeNS() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		CSSElement body = xhtmlDoc.createElement("body");
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
		CSSElement svg = xhtmlDoc.createElementNS("http://www.w3.org/2000/svg", "svg");
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
	public void cloneNode() {
		CSSElement html = xhtmlDoc.getDocumentElement();
		CSSElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("bodyId");
		body.setAttributeNode(attr);
		body.setAttribute("class", "fooclass");
		DOMElement div = xhtmlDoc.createElement("div");
		body.appendChild(div);
		div.appendChild(xhtmlDoc.createTextNode("foo"));
		CSSElement elm = (CSSElement) body.cloneNode(false);
		assertEquals(body.getNodeName(), elm.getNodeName());
		assertTrue(body.getAttributes().equals(elm.getAttributes()));
		elm = (CSSElement) body.cloneNode(false);
		assertFalse(body.isEqualNode(elm));
		elm = (CSSElement) body.cloneNode(true);
		assertTrue(body.isEqualNode(elm));
		assertEquals("div", elm.getChildNodes().item(0).getNodeName());
		assertEquals("foo", elm.getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
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
		CSSElement elm = xhtmlDoc.createElement("p");
		Attr attr = xhtmlDoc.createAttribute("foo");
		attr.setValue("bar\"");
		elm.setAttributeNode(attr);
		assertEquals("<p foo=\"bar&quot;\"></p>", elm.toString());
	}

	@Test
	public void testToString2() {
		CSSElement elm = xhtmlDoc.createElement("link");
		Attr attr = xhtmlDoc.createAttribute("href");
		attr.setValue("http://www.example.com/");
		elm.setAttributeNode(attr);
		assertEquals("<link href=\"http://www.example.com/\" />", elm.toString());
	}

}
