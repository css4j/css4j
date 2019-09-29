/*

 Copyright (c) 2005-2019, Carlos Amengual.

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

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class FilteredIteratorTest {
	private static TestDOMImplementation domImpl;
	private DOMDocument document;

	@BeforeClass
	public static void setUpBeforeClass() {
		domImpl = new TestDOMImplementation(false);
	}

	@Before
	public void setUp() throws DOMException {
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		document = domImpl.createDocument(null, "html", doctype);
		DOMElement docelm = document.getDocumentElement();
		ProcessingInstruction pi = document.createProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"sheet.css\"");
		document.insertBefore(pi, docelm);
		Comment comment = document.createComment(" Comment ");
		document.insertBefore(comment, pi);
		DOMElement body = document.createElement("body");
		docelm.appendChild(body);
		DOMElement div = document.createElement("div");
		body.appendChild(div);
		Text textdiv = document.createTextNode("Post div");
		body.appendChild(textdiv);
		DOMElement p = document.createElement("p");
		body.appendChild(p);
		Text textp = document.createTextNode("Post p");
		body.appendChild(textp);
		Text textp2 = document.createTextNode("Post p2");
		body.appendChild(textp2);
		DOMElement span = document.createElement("span");
		body.appendChild(span);
		DOMElement ul = document.createElement("ul");
		body.appendChild(ul);
		Text textul = document.createTextNode("Post ul");
		body.appendChild(textul);
		Comment commentul = document.createComment(" Comment post-ul ");
		body.appendChild(commentul);
	}

	@Test
	public void testSetup() throws DOMException {
		assertNotNull(document.getFirstChild());
		assertNotNull(document.getLastChild());
		assertNotNull(document.getFirstElementChild());
		assertNotNull(document.getLastElementChild());
		DOMElement docelm = document.getDocumentElement();
		assertEquals("html", docelm.getTagName());
		assertTrue(document.getFirstElementChild() == docelm);
		assertTrue(document.getLastElementChild() == docelm);
		assertEquals(1, document.getChildElementCount());
	}

	@Test
	public void testIterator() throws DOMException {
		testIterator(document);
	}

	@Test
	public void testIterator2() throws DOMException {
		testIterator(document.getDocumentElement());
	}

	private void testIterator(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		BitSet mask = new BitSet(32);
		mask.set(0, 32);
		Iterator<DOMNode> it = parentNode.iterator(mask);
		int i = 0;
		while (it.hasNext()) {
			assertTrue(list.item(i) == it.next());
			i++;
		}
		assertEquals(list.getLength(), i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testIteratorRemove() throws DOMException {
		testIteratorRemove(document);
	}

	@Test
	public void testIteratorRemove2() throws DOMException {
		testIteratorRemove(document.getDocumentElement());
	}

	private void testIteratorRemove(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		int i = 0;
		BitSet mask = new BitSet(32);
		mask.set(0, 32);
		Iterator<DOMNode> it = parentNode.iterator(mask);
		while (it.hasNext()) {
			Node node = it.next();
			assertTrue(list.item(0) == node);
			assertTrue(parentNode.getFirstChild() == node);
			it.remove();
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
			i++;
		}
		assertEquals(len, i);
		assertEquals(0, list.getLength());
		assertNull(parentNode.getFirstChild());
		assertNull(parentNode.getLastChild());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testElementIterator() throws DOMException {
		NodeList list = document.getChildNodes();
		Iterator<DOMElement> it = document.elementIterator();
		assertTrue(it.hasNext());
		DOMElement node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertTrue(list.item(3) == node);
		assertTrue(document.getDocumentElement() == node);
		assertTrue(document.getFirstElementChild() == node);
		assertTrue(document.getLastElementChild() == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testElementIteratorRemove() throws DOMException {
		NodeList list = document.getChildNodes();
		Iterator<DOMElement> it = document.elementIterator();
		assertTrue(it.hasNext());
		DOMElement element = it.next();
		assertEquals(Node.ELEMENT_NODE, element.getNodeType());
		assertTrue(list.item(3) == element);
		assertTrue(document.getDocumentElement() == element);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		it.remove();
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertNull(document.getDocumentElement());
		assertEquals(3, list.getLength());
		Node node = document.getFirstChild(); // doctype
		assertNotNull(node);
		assertNull(node.getPreviousSibling());
		node = node.getNextSibling(); // comment
		assertNotNull(node);
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // pi
		assertNotNull(node);
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		assertNull(node.getNextSibling());
	}

	@Test
	public void testElementIterator2() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		Iterator<DOMElement> it = body.elementIterator();
		assertTrue(it.hasNext());
		DOMElement node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertTrue(list.item(0) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("p", node.getNodeName());
		assertTrue(list.item(2) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(list.item(5) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("ul", node.getNodeName());
		assertTrue(list.item(6) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testElementIteratorRemove2() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		Iterator<DOMElement> it = body.elementIterator();
		assertTrue(it.hasNext());
		DOMElement element = it.next();
		assertEquals(Node.ELEMENT_NODE, element.getNodeType());
		assertEquals("div", element.getNodeName());
		assertTrue(list.item(0) == element);
		assertTrue(element.getParentNode().getFirstChild() == element);
		assertTrue(((ParentNode) element.getParentNode()).getFirstElementChild() == element);
		assertTrue(it.hasNext());
		element = it.next();
		assertEquals(Node.ELEMENT_NODE, element.getNodeType());
		assertEquals("p", element.getNodeName());
		assertTrue(list.item(2) == element);
		assertTrue(it.hasNext());
		it.remove();
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		element = it.next();
		assertEquals(Node.ELEMENT_NODE, element.getNodeType());
		assertEquals("span", element.getNodeName());
		assertTrue(list.item(4) == element);
		assertTrue(it.hasNext());
		element = it.next();
		assertEquals(Node.ELEMENT_NODE, element.getNodeType());
		assertEquals("ul", element.getNodeName());
		assertTrue(list.item(5) == element);
		assertFalse(it.hasNext());
		it.remove();
		assertEquals(7, list.getLength());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		Node node = body.getFirstChild(); // div
		assertNotNull(node);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertNull(node.getPreviousSibling());
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // span
		assertNotNull(node);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // comment
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		assertTrue(body.getLastChild() == node);
		assertNull(node.getNextSibling());
	}

	@Test
	public void testElementTypeIterator() throws DOMException {
		NodeList list = document.getChildNodes();
		Iterator<DOMNode> it = document.typeIterator(Node.ELEMENT_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertTrue(list.item(3) == node);
		assertTrue(document.getDocumentElement() == node);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testElementTypeIteratorRemove() throws DOMException {
		NodeList list = document.getChildNodes();
		Iterator<DOMNode> it = document.typeIterator(Node.ELEMENT_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertTrue(list.item(3) == node);
		assertTrue(document.getDocumentElement() == node);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		it.remove();
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertNull(document.getDocumentElement());
		assertEquals(3, list.getLength());
		node = document.getFirstChild(); // doctype
		assertNotNull(node);
		assertNull(node.getPreviousSibling());
		node = node.getNextSibling(); // comment
		assertNotNull(node);
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // pi
		assertNotNull(node);
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		assertNull(node.getNextSibling());
	}

	@Test
	public void testElementTypeIteratorRemove2() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		Iterator<DOMNode> it = body.typeIterator(Node.ELEMENT_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertTrue(list.item(0) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("p", node.getNodeName());
		assertTrue(list.item(2) == node);
		assertTrue(it.hasNext());
		it.remove();
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(list.item(4) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("ul", node.getNodeName());
		assertTrue(list.item(5) == node);
		assertFalse(it.hasNext());
		it.remove();
		assertEquals(7, list.getLength());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		node = body.getFirstChild(); // div
		assertNotNull(node);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertNull(node.getPreviousSibling());
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // span
		assertNotNull(node);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // text
		assertNotNull(node);
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		node = node.getNextSibling(); // comment
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertTrue(node.getPreviousSibling().getNextSibling() == node);
		assertNull(node.getNextSibling());
	}

	@Test
	public void testElementTypeIterator2() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		Iterator<DOMNode> it = body.typeIterator(Node.ELEMENT_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertTrue(list.item(0) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("p", node.getNodeName());
		assertTrue(list.item(2) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(list.item(5) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("ul", node.getNodeName());
		assertTrue(list.item(6) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testPIIterator() throws DOMException {
		NodeList list = document.getChildNodes();
		Iterator<DOMNode> it = document.typeIterator(Node.PROCESSING_INSTRUCTION_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		assertTrue(list.item(2) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testTextIterator() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		Iterator<DOMNode> it = body.typeIterator(Node.TEXT_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(1) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(3) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(4) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(7) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testCommentIterator() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		Iterator<DOMNode> it = body.typeIterator(Node.COMMENT_NODE);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertTrue(list.item(8) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testFilteredIterator() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		NodeFilter filter = new NegativeNodeFilter(new ElementNameFilter("div"));
		Iterator<DOMNode> it = body.iterator(filter);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertTrue(list.item(0) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testFilteredIterator2() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		NodeFilter filter = new NegativeNodeFilter(new ElementNameFilter("p"));
		Iterator<DOMNode> it = body.iterator(filter);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("p", node.getNodeName());
		assertTrue(list.item(2) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testFilteredIterator3() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		NodeFilter filter = new ElementNameFilter("p");
		Iterator<DOMNode> it = body.iterator(filter);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertTrue(list.item(0) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(1) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(3) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(4) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(list.item(5) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("ul", node.getNodeName());
		assertTrue(list.item(6) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(7) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertTrue(list.item(8) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testFilteredIterator4() throws DOMException {
		DOMElement body = document.getDocumentElement().getFirstElementChild();
		// body has these children: div, text, p, text, text, span, ul, text, comment
		NodeList list = body.getChildNodes();
		NodeFilter filter = new ElementNameFilter("ul");
		Iterator<DOMNode> it = body.iterator(filter);
		assertTrue(it.hasNext());
		Node node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("div", node.getNodeName());
		assertTrue(list.item(0) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(1) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("p", node.getNodeName());
		assertTrue(list.item(2) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(3) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(4) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		assertEquals("span", node.getNodeName());
		assertTrue(list.item(5) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.TEXT_NODE, node.getNodeType());
		assertTrue(list.item(7) == node);
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertTrue(list.item(8) == node);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

}
