/*

 Copyright (c) 2005-2021, Carlos Amengual.

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
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMNodeTest {
	TestDOMImplementation impl;
	DOMDocument xhtmlDoc;

	@Before
	public void setUp() {
		impl = new TestDOMImplementation();
		xhtmlDoc = impl.createDocument(null, null, null);
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		DOMElement elm = xhtmlDoc.createElement("html");
		xhtmlDoc.appendChild(elm);
	}

	@Test
	public void compareDocumentPosition1() {
		Node html = xhtmlDoc.getDocumentElement();
		DOMElement div1 = xhtmlDoc.createElement("div");
		DOMElement div2 = xhtmlDoc.createElement("div");
		html.appendChild(div1);
		html.appendChild(div2);
		assertEquals(Node.DOCUMENT_POSITION_PRECEDING, div1.compareDocumentPosition(div2));
		assertEquals(Node.DOCUMENT_POSITION_FOLLOWING, div2.compareDocumentPosition(div1));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINS + Node.DOCUMENT_POSITION_PRECEDING, html.compareDocumentPosition(div1));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINS + Node.DOCUMENT_POSITION_PRECEDING, html.compareDocumentPosition(div2));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINED_BY + Node.DOCUMENT_POSITION_FOLLOWING, div1.compareDocumentPosition(html));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINED_BY + Node.DOCUMENT_POSITION_FOLLOWING, div2.compareDocumentPosition(html));
		assertEquals(0, html.compareDocumentPosition(html));
		assertEquals(0, div1.compareDocumentPosition(div1));
		DOMElement p = xhtmlDoc.createElement("p");
		div1.appendChild(p);
		assertEquals(Node.DOCUMENT_POSITION_CONTAINS + Node.DOCUMENT_POSITION_PRECEDING, div1.compareDocumentPosition(p));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINED_BY + Node.DOCUMENT_POSITION_FOLLOWING, p.compareDocumentPosition(div1));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINS + Node.DOCUMENT_POSITION_PRECEDING, html.compareDocumentPosition(p));
		assertEquals(Node.DOCUMENT_POSITION_CONTAINED_BY + Node.DOCUMENT_POSITION_FOLLOWING, p.compareDocumentPosition(html));
		assertEquals(Node.DOCUMENT_POSITION_PRECEDING, p.compareDocumentPosition(div2));
		assertEquals(Node.DOCUMENT_POSITION_FOLLOWING, div2.compareDocumentPosition(p));
		DOMDocument otherdoc = impl.createDocument(null, "html", null);
		DOMElement otherDocElm = otherdoc.getDocumentElement();
		assertEquals(Node.DOCUMENT_POSITION_DISCONNECTED, div2.compareDocumentPosition(otherDocElm));
		assertEquals(Node.DOCUMENT_POSITION_DISCONNECTED, otherDocElm.compareDocumentPosition(div2));
	}

	@Test
	public void compareDocumentPosition2() {
		Node currnode = xhtmlDoc.getDocumentElement();
		buildTree(currnode, 0, 0, 3);
		currnode = xhtmlDoc.getDocumentElement();
		NodeList list = currnode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			comparePosition(list.item(i), list);
		}
	}

	private int buildTree(Node currnode, int baseindex, int depth, int maxindex) {
		DOMElement elm = null;
		for (int i = 0; i < maxindex; i++) {
			elm = xhtmlDoc.createElement("p");
			elm.setAttribute("index", Integer.toString(baseindex));
			currnode.appendChild(elm);
			baseindex++;
			if (depth + 1 < maxindex) {
				baseindex = buildTree(elm, baseindex, depth + 1, maxindex);
			}
		}
		return baseindex;
	}

	private void comparePosition(Node refnode, NodeList list) {
		if (refnode.getNodeType() == Node.ELEMENT_NODE) {
			DOMElement ref = (DOMElement) refnode;
			int refindex = Integer.parseInt(ref.getAttribute("index"));
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					DOMElement elm = (DOMElement) node;
					int index = Integer.parseInt(elm.getAttribute("index"));
					short comppos = ref.compareDocumentPosition(elm);
					short pos = 0;
					if (refindex < index) {
						pos += Node.DOCUMENT_POSITION_PRECEDING;
					} else if (refindex > index) {
						pos += Node.DOCUMENT_POSITION_FOLLOWING;
					}
					assertTrue(
							(Node.DOCUMENT_POSITION_FOLLOWING & comppos) == (Node.DOCUMENT_POSITION_FOLLOWING & pos));
					assertTrue(
							(Node.DOCUMENT_POSITION_PRECEDING & comppos) == (Node.DOCUMENT_POSITION_PRECEDING & pos));
				}
			}
		}
	}

	@Test
	public void normalize() {
		assertEquals(1, xhtmlDoc.getChildNodes().getLength());
		DOMElement elm = xhtmlDoc.createElement("body");
		DOMElement html = xhtmlDoc.getDocumentElement();
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(xhtmlDoc.createTextNode("\t     "));
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(elm);
		Text foo = xhtmlDoc.createTextNode("foo");
		Text bar = xhtmlDoc.createTextNode("bar");
		elm.appendChild(xhtmlDoc.createTextNode(" \t  "));
		elm.appendChild(foo);
		elm.appendChild(xhtmlDoc.createTextNode("     "));
		elm.appendChild(bar);
		elm.appendChild(xhtmlDoc.createTextNode("\n   "));
		elm.appendChild(xhtmlDoc.createTextNode("   "));
		assertEquals(6, elm.getChildNodes().getLength());
		elm.normalize();
		assertEquals(1, elm.getChildNodes().getLength());
		assertEquals("foo bar", elm.getChildNodes().item(0).getNodeValue());
		//
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(xhtmlDoc.createTextNode("\t     "));
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		xhtmlDoc.normalizeDocument();
		assertEquals(1, html.getChildNodes().getLength());
		assertEquals(1, elm.getChildNodes().getLength());
	}

	@Test
	public void normalize2() {
		assertEquals(1, xhtmlDoc.getChildNodes().getLength());
		DOMElement elm = xhtmlDoc.createElement("body");
		DOMElement html = xhtmlDoc.getDocumentElement();
		html.appendChild(xhtmlDoc.createComment(" Comment "));
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(xhtmlDoc.createTextNode("\t     "));
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(elm);
		Comment comment = xhtmlDoc.createComment("Hi");
		Text foo = xhtmlDoc.createTextNode("foo");
		Text bar = xhtmlDoc.createTextNode("bar \u212b");
		elm.appendChild(comment);
		elm.appendChild(xhtmlDoc.createTextNode(" \t  "));
		elm.appendChild(foo);
		elm.appendChild(xhtmlDoc.createTextNode("     "));
		elm.appendChild(xhtmlDoc.createTextNode("     "));
		elm.appendChild(bar);
		elm.appendChild(xhtmlDoc.createTextNode("\n   "));
		Comment comment2 = xhtmlDoc.createComment("Hi");
		elm.appendChild(comment2);
		assertEquals(8, elm.getChildNodes().getLength());
		elm.normalize();
		assertEquals(3, elm.getChildNodes().getLength());
		assertEquals(" foo bar \u00c5 ", elm.getChildNodes().item(1).getNodeValue());
		//
		//
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(xhtmlDoc.createTextNode("\t     "));
		html.appendChild(xhtmlDoc.createTextNode("\n     "));
		html.appendChild(xhtmlDoc.createComment(" Middle comment "));
		html.appendChild(xhtmlDoc.createTextNode(""));
		html.appendChild(xhtmlDoc.createComment(" End comment "));
		assertEquals(11, html.getChildNodes().getLength());
		xhtmlDoc.normalizeDocument();
		assertEquals(6, html.getChildNodes().getLength());
		assertEquals(3, elm.getChildNodes().getLength());
		assertEquals(" ", html.getChildNodes().item(3).getNodeValue());
		assertEquals(Node.COMMENT_NODE, html.getChildNodes().item(4).getNodeType());
		assertEquals(Node.COMMENT_NODE, html.getChildNodes().item(5).getNodeType());
	}

	@Test
	public void isSameNode() {
		DOMElement elm1 = xhtmlDoc.createElement("p");
		elm1.setAttribute("foo", "bar");
		Text foo1 = xhtmlDoc.createTextNode("foo");
		elm1.appendChild(foo1);
		assertTrue(elm1.isSameNode(elm1));
		assertFalse(elm1.isSameNode(foo1));
	}

	@Test
	public void isEqualNode() {
		DOMElement elm1 = xhtmlDoc.createElement("p");
		DOMElement elm2 = xhtmlDoc.createElement("p");
		assertTrue(elm1.isEqualNode(elm2));
		elm1.setAttribute("foo", "bar");
		assertFalse(elm1.isEqualNode(elm2));
		elm2.setAttribute("foo", "foo");
		assertFalse(elm1.isEqualNode(elm2));
		elm2.setAttribute("foo", "bar");
		assertTrue(elm1.isEqualNode(elm2));
		elm2.removeAttribute("foo");
		Text foo1 = xhtmlDoc.createTextNode("foo");
		elm1.appendChild(foo1);
		assertFalse(elm1.isEqualNode(elm2));
		elm2.setAttribute("foo", "bar");
		assertFalse(elm1.isEqualNode(elm2));
		Text foo2 = xhtmlDoc.createTextNode("foo");
		elm2.appendChild(foo2);
		assertTrue(elm1.isEqualNode(elm2));
		Text bar = xhtmlDoc.createTextNode("bar");
		elm2.replaceChild(bar, foo2);
		assertFalse(elm1.isEqualNode(elm2));
	}

	@Test
	public void getNodeValue() {
		DOMElement elm = xhtmlDoc.createElement("p");
		assertEquals("p", elm.getNodeName());
		assertNull(elm.getNodeValue());
		Text text = xhtmlDoc.createTextNode("foo");
		assertEquals("#text", text.getNodeName());
		assertEquals("foo", text.getNodeValue());
		text.setNodeValue("bar");
		assertEquals("bar", text.getNodeValue());
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("fooid");
		assertEquals("id", attr.getNodeName());
		assertEquals("fooid", attr.getNodeValue());
		attr.setNodeValue("barid");
		assertEquals("barid", attr.getNodeValue());
		CDATASection cdata = xhtmlDoc.createCDATASection("var j = 1");
		assertEquals("#cdata-section", cdata.getNodeName());
		assertEquals("var j = 1", cdata.getNodeValue());
		cdata.setNodeValue("foo");
		assertEquals("foo", cdata.getNodeValue());
		Comment comment = xhtmlDoc.createComment("***");
		assertEquals("#comment", comment.getNodeName());
		assertEquals("***", comment.getNodeValue());
		comment.setNodeValue("comment");
		assertEquals("comment", comment.getNodeValue());
		DocumentFragment fragment = xhtmlDoc.createDocumentFragment();
		assertEquals("#document-fragment", fragment.getNodeName());
		assertNull(fragment.getNodeValue());
		assertEquals("#document", xhtmlDoc.getNodeName());
		assertNull(xhtmlDoc.getNodeValue());
		EntityReference entref = xhtmlDoc.createEntityReference("amp");
		assertEquals("amp", entref.getNodeName());
		assertNull(entref.getNodeValue());
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"sheet.css\"");
		assertEquals("xml-stylesheet", pi.getNodeName());
		assertEquals("type=\"text/css\" href=\"sheet.css\"", pi.getNodeValue());
		DocumentType dt = impl.createDocumentType("xhtml", "-//W3C//DTD XHTML 1.1//EN", "w3c/xhtml11.dtd");
		assertEquals("xhtml", dt.getNodeName());
		assertNull(dt.getNodeValue());
	}

	@Test
	public void getPreviousSibling() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm1 = xhtmlDoc.createElement("div");
		html.appendChild(elm1);
		assertTrue(html == elm1.getParentNode());
		DOMElement elm2 = xhtmlDoc.createElement("p");
		html.appendChild(elm2);
		assertTrue(html == elm2.getParentNode());
		assertTrue(elm1 == elm2.getPreviousSibling());
		assertTrue(elm2 == elm1.getNextSibling());
		assertNull(elm1.getPreviousSibling());
		assertNull(elm2.getNextSibling());
	}

	@Test
	public void getFirstChild() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm1 = xhtmlDoc.createElement("div");
		html.appendChild(elm1);
		DOMElement elm2 = xhtmlDoc.createElement("p");
		html.appendChild(elm2);
		assertTrue(elm1 == html.getFirstChild());
		assertTrue(elm2 == html.getLastChild());
		assertNull(elm1.getFirstChild());
		assertNull(elm1.getLastChild());
	}

	@Test
	public void testDocumentFragment() throws DOMException {
		DocumentFragment fragment = xhtmlDoc.createDocumentFragment();
		DOMElement div = xhtmlDoc.createElement("div");
		div.appendChild(xhtmlDoc.createElement("span"));
		div.appendChild(xhtmlDoc.createTextNode("text under div"));
		div.appendChild(xhtmlDoc.createElement("i"));
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createElement("img"));
		p.appendChild(xhtmlDoc.createTextNode("text under p"));
		p.appendChild(xhtmlDoc.createElement("b"));
		fragment.appendChild(div);
		fragment.appendChild(p);
		assertTrue(fragment.getFirstChild() == div);
		assertTrue(fragment.getLastChild() == p);
		assertTrue(div.getNextSibling() == p);
		assertTrue(p.getPreviousSibling() == div);
		assertTrue(div.getNextElementSibling() == p);
		assertTrue(p.getPreviousElementSibling() == div);
	}

	private DocumentFragment createDocumentFragment() {
		DocumentFragment fragment = xhtmlDoc.createDocumentFragment();
		DOMElement div = xhtmlDoc.createElement("div");
		div.appendChild(xhtmlDoc.createElement("span"));
		div.appendChild(xhtmlDoc.createTextNode("text under div"));
		div.appendChild(xhtmlDoc.createElement("i"));
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createElement("img"));
		p.appendChild(xhtmlDoc.createTextNode("text under p"));
		p.appendChild(xhtmlDoc.createElement("b"));
		fragment.appendChild(div);
		fragment.appendChild(p);
		return fragment;
	}

	@Test
	public void prependChild() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm = xhtmlDoc.createElement("body");
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-foo", "bar");
		DocumentType docType = xhtmlDoc.getImplementation().createDocumentType("html", null, null);
		assertFalse(html.hasChildNodes());
		Node appended = html.prependChild(elm);
		assertTrue(html.hasChildNodes());
		assertTrue(appended == elm);
		assertTrue(html == elm.getParentNode());
		assertTrue(appended == html.getChildNodes().item(0));
		assertTrue(appended == html.getChildren().item(0));
		assertNull(html.getChildNodes().item(1));
		assertNull(html.getChildren().item(1));
		assertNull(html.getChildNodes().item(-1));
		assertNull(html.getChildren().item(-1));
		assertTrue(xhtmlDoc == elm.getOwnerDocument());
		assertTrue(xhtmlDoc == html.getOwnerDocument());
		Attr attr = xhtmlDoc.createAttribute("id");
		//
		try {
			elm.prependChild(attr);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.prependChild(xhtmlDoc);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.prependChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertNull(docType.getOwnerDocument());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.prependChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.prependChild(html);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.prependChild(impl.createDocument(null, null, null).createElement("a"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
		//
		DocumentFragment fragment = createDocumentFragment();
		try {
			((ParentNode) fragment).prependChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		elm.prependChild(pi);
		assertTrue(elm.hasChildNodes());
		assertTrue(pi == elm.getChildNodes().item(0));
		Text text = xhtmlDoc.createTextNode("foo");
		appended = elm.prependChild(text);
		assertTrue(appended == text);
		assertTrue(pi == text.getNextSibling());
		assertTrue(text == pi.getPreviousSibling());
		assertTrue(appended == elm.getChildNodes().item(0));
		assertNull(((NonDocumentTypeChildNode) text).getNextElementSibling());
		assertNull(((NonDocumentTypeChildNode) text).getPreviousElementSibling());
		// Test appending to void elements
		DOMElement head = xhtmlDoc.createElement("head");
		html.prependChild(head);
		assertTrue(head == html.getChildNodes().item(0));
		assertTrue(head == html.getChildren().item(0));
		DOMElement base = xhtmlDoc.createElement("base");
		head.prependChild(base);
		assertTrue(base == head.getChildNodes().item(0));
		assertTrue(base == head.getChildren().item(0));
		DOMElement base2 = xhtmlDoc.createElement("base");
		try {
			head.prependChild(base2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(base.hasChildNodes());
		try {
			base.prependChild(xhtmlDoc.createTextNode("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(base.hasChildNodes());
		DOMElement link = xhtmlDoc.createElement("link");
		try {
			link.prependChild(xhtmlDoc.createTextNode("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(link.hasChildNodes());
		DOMElement meta = xhtmlDoc.createElement("meta");
		try {
			meta.prependChild(xhtmlDoc.createTextNode("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(meta.hasChildNodes());
		assertNull(meta.getChildNodes().item(0));
		assertNull(meta.getChildren().item(0));
		// Document fragment
		Node first = fragment.getFirstChild();
		assertEquals(2, elm.getChildNodes().getLength());
		elm.prependChild(fragment);
		assertFalse(fragment == elm.getFirstChild());
		assertEquals(4, elm.getChildNodes().getLength());
		assertTrue(first == elm.getFirstChild());
		assertNull(fragment.getFirstChild());
		assertNull(fragment.getLastChild());
		assertNull(fragment.getParentNode());
	}

	@Test
	public void appendChild() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm = xhtmlDoc.createElement("body");
		ProcessingInstruction pi = xhtmlDoc.createProcessingInstruction("xml-foo", "bar");
		DocumentType docType = xhtmlDoc.getImplementation().createDocumentType("html", null, null);
		assertFalse(html.hasChildNodes());
		Node appended = html.appendChild(elm);
		assertTrue(html.hasChildNodes());
		assertTrue(appended == elm);
		assertTrue(html == elm.getParentNode());
		assertTrue(appended == html.getChildNodes().item(0));
		assertTrue(appended == html.getChildren().item(0));
		assertNull(html.getChildNodes().item(1));
		assertNull(html.getChildren().item(1));
		assertNull(html.getChildNodes().item(-1));
		assertNull(html.getChildren().item(-1));
		assertTrue(xhtmlDoc == elm.getOwnerDocument());
		assertTrue(xhtmlDoc == html.getOwnerDocument());
		Attr attr = xhtmlDoc.createAttribute("id");
		DocumentFragment fragment = createDocumentFragment();
		//
		try {
			docType.appendChild(fragment);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(fragment.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		//
		try {
			elm.appendChild(attr);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.appendChild(xhtmlDoc);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.appendChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertNull(docType.getOwnerDocument());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.appendChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.appendChild(html);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			elm.appendChild(impl.createDocument(null, null, null).createElement("a"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
		//
		EntityReference ref = xhtmlDoc.createEntityReference("amp");
		Attr id2 = xhtmlDoc.createAttribute("id");
		try {
			ref.appendChild(id2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(id2.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			ref.appendChild(xhtmlDoc);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			ref.appendChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		//
		Text text = xhtmlDoc.createTextNode("text inside elm");
		try {
			text.appendChild(xhtmlDoc);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(text);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(text.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(ref);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(ref.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(elm);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNotNull(elm.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(attr);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(pi);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(pi.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			text.appendChild(fragment);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(fragment.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		//
		try {
			attr.appendChild(text);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(text.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			attr.appendChild(pi);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(pi.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			attr.appendChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			attr.appendChild(fragment);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(fragment.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		//
		try {
			pi.appendChild(fragment);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(fragment.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		//
		try {
			pi.appendChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			fragment.appendChild(docType);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertNull(docType.getParentNode());
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		elm.appendChild(pi);
		assertTrue(elm.hasChildNodes());
		assertTrue(pi == elm.getChildNodes().item(0));
		appended = elm.appendChild(text);
		assertTrue(appended == text);
		assertTrue(pi == text.getPreviousSibling());
		assertTrue(text == pi.getNextSibling());
		assertTrue(appended == elm.getChildNodes().item(1));
		assertNull(((NonDocumentTypeChildNode) text).getNextElementSibling());
		assertNull(((NonDocumentTypeChildNode) text).getPreviousElementSibling());
		// Test appending to void elements
		DOMElement head = xhtmlDoc.createElement("head");
		html.appendChild(head);
		assertTrue(head == html.getChildNodes().item(1));
		assertTrue(head == html.getChildren().item(1));
		DOMElement base = xhtmlDoc.createElement("base");
		head.appendChild(base);
		assertTrue(base == head.getChildNodes().item(0));
		assertTrue(base == head.getChildren().item(0));
		DOMElement base2 = xhtmlDoc.createElement("base");
		try {
			head.appendChild(base2);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(base.hasChildNodes());
		try {
			base.appendChild(xhtmlDoc.createTextNode("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(base.hasChildNodes());
		DOMElement link = xhtmlDoc.createElement("link");
		try {
			link.appendChild(xhtmlDoc.createTextNode("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(link.hasChildNodes());
		DOMElement meta = xhtmlDoc.createElement("meta");
		try {
			meta.appendChild(xhtmlDoc.createTextNode("foo"));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertFalse(meta.hasChildNodes());
		assertNull(meta.getChildNodes().item(0));
		assertNull(meta.getChildren().item(0));
		// Document fragment
		Node last = fragment.getLastChild();
		assertEquals(2, elm.getChildNodes().getLength());
		elm.appendChild(fragment);
		assertFalse(fragment == elm.getLastChild());
		assertEquals(4, elm.getChildNodes().getLength());
		assertTrue(last == elm.getLastChild());
		assertNull(fragment.getFirstChild());
		assertNull(fragment.getLastChild());
		assertNull(fragment.getParentNode());
	}

	@Test
	public void insertBefore() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm1 = xhtmlDoc.createElement("p");
		html.appendChild(elm1);
		assertTrue(html == elm1.getParentNode());
		assertEquals(1, html.getChildNodes().getLength());
		DOMElement elm2 = xhtmlDoc.createElement("div");
		DOMElement elm = (DOMElement) html.insertBefore(elm2, elm1);
		assertTrue(elm2 == elm);
		assertTrue(elm2 == elm1.getPreviousSibling());
		assertTrue(elm2 == elm1.getPreviousElementSibling());
		assertTrue(elm1 == elm2.getNextSibling());
		assertTrue(elm1 == elm2.getNextElementSibling());
		assertEquals(2, html.getChildNodes().getLength());
		assertTrue(elm2 == html.getChildNodes().item(0));
		assertTrue(html == elm2.getParentNode());
		DOMElement elm3 = xhtmlDoc.createElement("div");
		elm = (DOMElement) html.insertBefore(elm3, null);
		assertTrue(elm == elm3);
		assertEquals(3, html.getChildNodes().getLength());
		assertTrue(elm3 == html.getChildNodes().item(2));
	}

	@Test
	public void insertBefore2() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		DOMElement span = xhtmlDoc.createElement("span");
		body.appendChild(span);
		assertTrue(body == span.getParentNode());
		assertFalse(span.hasChildNodes());
		Text text = xhtmlDoc.createTextNode("foo");
		span.appendChild(text);
		assertTrue(span.hasChildNodes());
		Text text2 = xhtmlDoc.createTextNode("bar");
		body.appendChild(text2);
		assertTrue(body == text2.getParentNode());
		DOMElement div = xhtmlDoc.createElement("div");
		div.appendChild(xhtmlDoc.createTextNode("inside div"));
		body.appendChild(div);
		assertTrue(body == div.getParentNode());
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("inside p"));
		body.appendChild(p);
		assertTrue(body == p.getParentNode());
		assertTrue(span.getNextSibling() == text2);
		assertTrue(text2.getNextSibling() == div);
		assertTrue(div.getNextSibling() == p);
		assertNull(p.getNextSibling());
		assertTrue(span.getNextElementSibling() == div);
		assertTrue(((NonDocumentTypeChildNode) text2).getNextElementSibling() == div);
		assertTrue(div.getNextElementSibling() == p);
		assertNull(p.getNextElementSibling());
		assertTrue(span == body.getFirstChild());
		assertTrue(span == body.getFirstElementChild());
		assertTrue(p == body.getLastChild());
		assertTrue(p == body.getLastElementChild());
		//
		ElementList listspan = body.getElementsByTagName("span");
		ElementList listdiv = body.getElementsByTagName("div");
		ElementList listp = body.getElementsByTagName("p");
		assertEquals(1, listspan.getLength());
		assertEquals(1, listdiv.getLength());
		assertEquals(1, listp.getLength());
		//
		assertTrue(p.getPreviousSibling() == div);
		assertTrue(p.getPreviousElementSibling() == div);
		assertTrue(div.getPreviousSibling() == text2);
		assertTrue(div.getPreviousElementSibling() == span);
		assertTrue(text2.getPreviousSibling() == span);
		assertTrue(((NonDocumentTypeChildNode) text2).getPreviousElementSibling() == span);
		assertNull(span.getPreviousSibling());
		assertNull(span.getPreviousElementSibling());
		//
		DOMElement p2 = xhtmlDoc.createElement("p");
		DOMElement elm = (DOMElement) body.insertBefore(p2, div);
		assertTrue(p2 == elm);
		assertTrue(p2 == div.getPreviousSibling());
		assertTrue(p2 == div.getPreviousElementSibling());
		assertTrue(div == p2.getNextSibling());
		assertTrue(div == p2.getNextElementSibling());
		assertEquals(5, body.getChildNodes().getLength());
		assertTrue(p2 == body.getChildNodes().item(2));
		assertTrue(body == p2.getParentNode());
		assertEquals(1, listspan.getLength());
		assertEquals(1, listdiv.getLength());
		assertEquals(2, listp.getLength());
		//
		DOMElement elm3 = xhtmlDoc.createElement("div");
		elm = (DOMElement) body.insertBefore(elm3, null);
		assertTrue(elm == elm3);
		assertEquals(6, body.getChildNodes().getLength());
		assertEquals(5, body.getChildren().getLength());
		assertTrue(span == body.getChildNodes().item(0));
		assertTrue(text2 == body.getChildNodes().item(1));
		assertTrue(p2 == body.getChildNodes().item(2));
		assertTrue(div == body.getChildNodes().item(3));
		assertTrue(p == body.getChildNodes().item(4));
		assertTrue(elm == body.getChildNodes().item(5));
		assertTrue(span == body.getChildren().item(0));
		assertTrue(p2 == body.getChildren().item(1));
		assertTrue(div == body.getChildren().item(2));
		assertTrue(p == body.getChildren().item(3));
		assertTrue(elm == body.getChildren().item(4));
		assertTrue(body == elm.getParentNode());
		assertTrue(p == elm.getPreviousSibling());
		assertTrue(p == elm.getPreviousElementSibling());
		assertTrue(elm == p.getNextSibling());
		assertTrue(elm == p.getNextElementSibling());
		assertNull(elm.getNextSibling());
		assertNull(elm.getNextElementSibling());
		assertEquals(1, listspan.getLength());
		assertEquals(2, listdiv.getLength());
		assertEquals(2, listp.getLength());
	}

	@Test
	public void insertBeforeMyself() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm1 = xhtmlDoc.createElement("div");
		html.appendChild(elm1);
		DOMElement elm2 = xhtmlDoc.createElement("p");
		html.appendChild(elm2);
		assertTrue(elm1 == elm2.getPreviousSibling());
		assertTrue(elm1 == elm2.getPreviousElementSibling());
		assertTrue(elm2 == elm1.getNextSibling());
		assertTrue(elm2 == elm1.getNextElementSibling());
		assertTrue(html == elm1.getParentNode());
		assertEquals(2, html.getChildNodes().getLength());
		assertTrue(elm1 == html.insertBefore(elm1, elm1));
		assertTrue(elm1 == elm2.getPreviousSibling());
		assertTrue(elm1 == elm2.getPreviousElementSibling());
		assertTrue(elm2 == elm1.getNextSibling());
		assertTrue(elm2 == elm1.getNextElementSibling());
		assertEquals(2, html.getChildNodes().getLength());
		assertTrue(elm2 == html.insertBefore(elm2, elm2));
		assertTrue(elm1 == elm2.getPreviousSibling());
		assertTrue(elm1 == elm2.getPreviousElementSibling());
		assertTrue(elm2 == elm1.getNextSibling());
		assertTrue(elm2 == elm1.getNextElementSibling());
	}

	@Test
	public void insertBeforeDF() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DocumentType docType = xhtmlDoc.getImplementation().createDocumentType("html", null, null);
		xhtmlDoc.insertBefore(docType, html);
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		DOMElement elm1 = xhtmlDoc.createElement("p");
		body.appendChild(elm1);
		DOMElement elm2 = xhtmlDoc.createElement("div");
		body.insertBefore(elm2, elm1);
		DOMElement elm3 = xhtmlDoc.createElement("span");
		body.insertBefore(elm3, elm1);
		assertEquals(3, body.getChildNodes().getLength());
		DocumentFragment fragment = createDocumentFragment();
		body.insertBefore(fragment, elm3);
		assertEquals(5, body.getChildNodes().getLength());
		assertEquals("p", body.getChildNodes().item(2).getNodeName());
		assertEquals("span", body.getChildNodes().item(3).getNodeName());
		assertNull(fragment.getFirstChild());
		assertNull(fragment.getLastChild());
		assertNull(fragment.getParentNode());
	}

	@Test
	public void removeChild() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm = xhtmlDoc.createElement("body");
		html.appendChild(elm);
		assertTrue(html == elm.getParentNode());
		assertTrue(elm == html.getFirstChild());
		assertTrue(elm == html.getLastChild());
		assertTrue(elm == html.getFirstElementChild());
		assertTrue(elm == html.getLastElementChild());
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("bodyId");
		assertFalse(elm.hasAttributes());
		elm.setAttributeNode(attr);
		assertTrue(elm.hasAttributes());
		assertFalse(elm.hasChildNodes());
		assertNull(attr.getParentNode());
		Text text = xhtmlDoc.createTextNode("foo");
		elm.appendChild(text);
		assertTrue(elm.hasChildNodes());
		assertTrue(elm == text.getParentNode());
		text = (Text) elm.removeChild(text);
		assertFalse(elm.hasChildNodes());
		assertNull(text.getParentNode());
		assertNull(text.getNextSibling());
		assertNull(text.getPreviousSibling());
		assertNull(((NonDocumentTypeChildNode) text).getNextElementSibling());
		assertNull(text.getPreviousSibling());
		assertNull(((NonDocumentTypeChildNode) text).getPreviousElementSibling());
		elm.removeAttribute(attr.getName());
		assertFalse(elm.hasAttributes());
		elm = (DOMElement) html.removeChild(elm);
		assertFalse(html.hasChildNodes());
		assertNull(html.getFirstChild());
		assertNull(html.getLastChild());
		assertNull(elm.getParentNode());
		assertNull(elm.getNextSibling());
		assertNull(elm.getPreviousSibling());
		assertNull(elm.getNextElementSibling());
		assertNull(elm.getPreviousSibling());
		assertNull(elm.getPreviousElementSibling());
	}

	@Test
	public void removeChild2() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		DOMElement elm = xhtmlDoc.createElement("span");
		body.appendChild(elm);
		assertTrue(body == elm.getParentNode());
		assertFalse(elm.hasChildNodes());
		Text text = xhtmlDoc.createTextNode("foo");
		elm.appendChild(text);
		assertTrue(elm.hasChildNodes());
		Text text2 = xhtmlDoc.createTextNode("bar");
		body.appendChild(text2);
		assertTrue(body == text2.getParentNode());
		DOMElement div = xhtmlDoc.createElement("div");
		div.appendChild(xhtmlDoc.createTextNode("inside div"));
		body.appendChild(div);
		assertTrue(body == div.getParentNode());
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("inside p"));
		body.appendChild(p);
		assertTrue(body == p.getParentNode());
		assertTrue(elm.getNextSibling() == text2);
		assertTrue(text2.getNextSibling() == div);
		assertTrue(div.getNextSibling() == p);
		assertNull(p.getNextSibling());
		assertTrue(elm.getNextElementSibling() == div);
		assertTrue(((NonDocumentTypeChildNode) text2).getNextElementSibling() == div);
		assertTrue(div.getNextElementSibling() == p);
		assertNull(p.getNextElementSibling());
		assertTrue(elm == body.getFirstChild());
		assertTrue(elm == body.getFirstElementChild());
		assertTrue(p == body.getLastChild());
		assertTrue(p == body.getLastElementChild());
		//
		ElementList listspan = body.getElementsByTagName("span");
		ElementList listdiv = body.getElementsByTagName("div");
		ElementList listp = body.getElementsByTagName("p");
		assertEquals(1, listspan.getLength());
		assertEquals(1, listdiv.getLength());
		assertEquals(1, listp.getLength());
		//
		assertTrue(p.getPreviousSibling() == div);
		assertTrue(p.getPreviousElementSibling() == div);
		assertTrue(div.getPreviousSibling() == text2);
		assertTrue(div.getPreviousElementSibling() == elm);
		assertTrue(text2.getPreviousSibling() == elm);
		assertTrue(((NonDocumentTypeChildNode) text2).getPreviousElementSibling() == elm);
		assertNull(elm.getPreviousSibling());
		assertNull(elm.getPreviousElementSibling());
		assertEquals(4, body.getChildNodes().getLength());
		assertTrue(elm == body.getChildNodes().item(0));
		assertTrue(text2 == body.getChildNodes().item(1));
		assertTrue(div == body.getChildNodes().item(2));
		//
		body.removeChild(div);
		assertTrue(body.hasChildNodes());
		assertNull(div.getParentNode());
		assertNull(div.getNextSibling());
		assertNull(div.getPreviousSibling());
		assertNull(div.getNextElementSibling());
		assertNull(div.getPreviousSibling());
		assertNull(div.getPreviousElementSibling());
		assertTrue(elm.getNextSibling() == text2);
		assertTrue(text2.getNextSibling() == p);
		assertNull(p.getNextSibling());
		assertTrue(elm.getNextElementSibling() == p);
		assertTrue(((NonDocumentTypeChildNode) text2).getNextElementSibling() == p);
		assertNull(p.getNextElementSibling());
		assertEquals(1, listspan.getLength());
		assertEquals(0, listdiv.getLength());
		assertEquals(1, listp.getLength());
		assertTrue(elm == body.getFirstChild());
		assertTrue(p == body.getLastChild());
		assertEquals(3, body.getChildNodes().getLength());
		assertTrue(elm == body.getChildNodes().item(0));
		assertTrue(p == body.getChildNodes().item(2));
		//
		elm = (DOMElement) body.removeChild(elm);
		assertTrue(body.hasChildNodes());
		assertNull(elm.getParentNode());
		assertNull(elm.getNextSibling());
		assertNull(elm.getPreviousSibling());
		assertNull(elm.getNextElementSibling());
		assertNull(elm.getPreviousSibling());
		assertNull(elm.getPreviousElementSibling());
		assertTrue(text2.getNextSibling() == p);
		assertNull(p.getNextSibling());
		assertTrue(text2 == p.getPreviousSibling());
		assertTrue(((NonDocumentTypeChildNode) text2).getNextElementSibling() == p);
		assertNull(p.getNextElementSibling());
		assertNull(p.getPreviousElementSibling());
		assertEquals(0, listspan.getLength());
		assertEquals(0, listdiv.getLength());
		assertEquals(1, listp.getLength());
		assertTrue(text2 == body.getFirstChild());
		assertTrue(p == body.getLastChild());
		assertTrue(p == body.getFirstElementChild());
		assertTrue(p == body.getLastElementChild());
		assertEquals(2, body.getChildNodes().getLength());
		assertTrue(text2 == body.getChildNodes().item(0));
		assertTrue(p == body.getChildNodes().item(1));
		//
		body.removeChild(text2);
		assertNull(text2.getParentNode());
		assertNull(text2.getNextSibling());
		assertNull(text2.getPreviousSibling());
		assertNull(((NonDocumentTypeChildNode) text2).getNextElementSibling());
		assertNull(text2.getPreviousSibling());
		assertNull(((NonDocumentTypeChildNode) text2).getPreviousElementSibling());
		assertNull(p.getNextElementSibling());
		assertNull(p.getPreviousElementSibling());
		assertNull(p.getNextSibling());
		assertNull(p.getPreviousSibling());
		assertEquals(0, listspan.getLength());
		assertEquals(0, listdiv.getLength());
		assertEquals(1, listp.getLength());
		assertTrue(p == body.getFirstChild());
		assertTrue(p == body.getLastChild());
		assertTrue(p == body.getFirstElementChild());
		assertTrue(p == body.getLastElementChild());
		assertEquals(1, body.getChildNodes().getLength());
		assertTrue(p == body.getChildNodes().item(0));
		//
		body.removeChild(p);
		assertFalse(body.hasChildNodes());
		assertEquals(0, body.getChildNodes().getLength());
		assertNull(body.getFirstChild());
		assertNull(body.getLastChild());
		assertNull(body.getFirstElementChild());
		assertNull(body.getLastElementChild());
		assertNull(p.getParentNode());
		assertNull(p.getNextSibling());
		assertNull(p.getPreviousSibling());
		assertNull(p.getNextElementSibling());
		assertNull(p.getPreviousSibling());
		assertNull(p.getPreviousElementSibling());
	}

	@Test
	public void replaceChild() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		body.setAttribute("id", "body1");
		html.appendChild(body);
		DOMElement body2 = xhtmlDoc.createElement("body");
		body2.setAttribute("id", "body2");
		DOMElement elm = (DOMElement) html.replaceChild(body2, body);
		assertEquals(body, elm);
		assertTrue(html == body2.getParentNode());
		assertNull(elm.getParentNode());
		assertNull(body.getParentNode());
		assertNull(body.getNextSibling());
		assertNull(body.getPreviousSibling());
		assertNull(body.getNextElementSibling());
		assertNull(body.getPreviousElementSibling());
		Text foo1 = xhtmlDoc.createTextNode("foo1");
		body2.appendChild(foo1);
		Text foo2 = xhtmlDoc.createTextNode("foo2");
		assertNull(foo2.getParentNode());
		Text text = (Text) body2.replaceChild(foo2, foo1);
		assertTrue(foo1 == text);
		assertEquals("foo1", text.getTextContent());
		assertEquals(body2, foo2.getParentNode());
		assertNull(text.getParentNode());
	}

	@Test
	public void replaceChild2() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		DOMElement span = xhtmlDoc.createElement("span");
		body.appendChild(span);
		assertTrue(body == span.getParentNode());
		assertFalse(span.hasChildNodes());
		Text text = xhtmlDoc.createTextNode("foo");
		span.appendChild(text);
		assertTrue(span.hasChildNodes());
		Text text2 = xhtmlDoc.createTextNode("bar");
		body.appendChild(text2);
		assertTrue(body == text2.getParentNode());
		DOMElement div = xhtmlDoc.createElement("div");
		div.appendChild(xhtmlDoc.createTextNode("inside div"));
		body.appendChild(div);
		assertTrue(body == div.getParentNode());
		DOMElement p = xhtmlDoc.createElement("p");
		p.appendChild(xhtmlDoc.createTextNode("inside p"));
		body.appendChild(p);
		//
		ElementList listspan = body.getElementsByTagName("span");
		ElementList listdiv = body.getElementsByTagName("div");
		ElementList listp = body.getElementsByTagName("p");
		assertEquals(1, listspan.getLength());
		assertEquals(1, listdiv.getLength());
		assertEquals(1, listp.getLength());
		//
		DOMElement div2 = xhtmlDoc.createElement("div");
		div2.setAttribute("id", "div2");
		DOMElement elm = (DOMElement) body.replaceChild(div2, div);
		assertTrue(div == elm);
		assertTrue(body == div2.getParentNode());
		assertNull(elm.getParentNode());
		assertNull(elm.getNextSibling());
		assertNull(elm.getPreviousSibling());
		assertNull(elm.getNextElementSibling());
		assertNull(elm.getPreviousElementSibling());
		//
		assertNull(span.getPreviousSibling());
		assertNull(span.getPreviousElementSibling());
		assertTrue(span.getNextSibling() == text2);
		assertTrue(text2.getNextSibling() == div2);
		assertTrue(div2.getNextSibling() == p);
		assertNull(p.getNextSibling());
		assertTrue(span.getNextElementSibling() == div2);
		assertTrue(((NonDocumentTypeChildNode) text2).getNextElementSibling() == div2);
		assertTrue(div2.getNextElementSibling() == p);
		assertNull(p.getNextElementSibling());
		assertTrue(span == body.getFirstChild());
		assertTrue(span == body.getFirstElementChild());
		assertTrue(p == body.getLastChild());
		assertTrue(p == body.getLastElementChild());
		assertEquals("div2", div2.getAttribute("id"));
		assertEquals(1, listspan.getLength());
		assertEquals(1, listdiv.getLength());
		assertEquals(1, listp.getLength());
		//
		assertEquals(4, body.getChildNodes().getLength());
		assertTrue(span == body.getChildNodes().item(0));
		assertTrue(text2 == body.getChildNodes().item(1));
		assertTrue(div2 == body.getChildNodes().item(2));
		assertTrue(p == body.getChildNodes().item(3));
		assertEquals(3, body.getChildren().getLength());
		assertTrue(span == body.getChildren().item(0));
		assertTrue(div2 == body.getChildren().item(1));
		assertTrue(p == body.getChildren().item(2));
	}

	@Test
	public void replaceByMyself() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DOMElement elm1 = xhtmlDoc.createElement("div");
		html.appendChild(elm1);
		DOMElement elm2 = xhtmlDoc.createElement("p");
		html.appendChild(elm2);
		assertTrue(elm1 == html.replaceChild(elm1, elm1));
		assertTrue(elm1 == elm2.getPreviousSibling());
		assertTrue(elm1 == elm2.getPreviousElementSibling());
		assertTrue(elm2 == elm1.getNextSibling());
		assertTrue(elm2 == elm1.getNextElementSibling());
		assertEquals(2, html.getChildNodes().getLength());
		assertTrue(elm2 == html.replaceChild(elm2, elm2));
		assertTrue(elm1 == elm2.getPreviousSibling());
		assertTrue(elm1 == elm2.getPreviousElementSibling());
		assertTrue(elm2 == elm1.getNextSibling());
		assertTrue(elm2 == elm1.getNextElementSibling());
	}

	@Test
	public void replaceChildDF() throws DOMException {
		DOMElement html = xhtmlDoc.getDocumentElement();
		DocumentType docType = xhtmlDoc.getImplementation().createDocumentType("html", null, null);
		xhtmlDoc.insertBefore(docType, html);
		DOMElement body = xhtmlDoc.createElement("body");
		html.appendChild(body);
		DOMElement elm1 = xhtmlDoc.createElement("p");
		body.appendChild(elm1);
		DOMElement elm2 = xhtmlDoc.createElement("div");
		body.insertBefore(elm2, elm1);
		DOMElement elm3 = xhtmlDoc.createElement("span");
		body.insertBefore(elm3, elm1);
		assertEquals(3, body.getChildNodes().getLength());
		DocumentFragment fragment = createDocumentFragment();
		body.replaceChild(fragment, elm3);
		assertEquals(4, body.getChildNodes().getLength());
		assertEquals("p", body.getChildNodes().item(2).getNodeName());
		assertNull(fragment.getFirstChild());
		assertNull(fragment.getLastChild());
		assertNull(fragment.getParentNode());
	}

	@Test
	public void cloneNode() {
		DOMElement elm = xhtmlDoc.createElement("p");
		elm.setAttribute("foo", "bar");
		Text foo = xhtmlDoc.createTextNode("foo");
		elm.appendChild(foo);
		DOMElement elmc = elm.cloneNode(true);
		assertEquals(elm.getTagName(), elmc.getTagName());
		assertEquals(elm.getAttribute("foo"), elmc.getAttribute("foo"));
		assertEquals(elm.getChildNodes().getLength(), elmc.getChildNodes().getLength());
		assertEquals(elm.getChildNodes().item(0).getNodeValue(), elmc.getChildNodes().item(0).getNodeValue());
	}

}
