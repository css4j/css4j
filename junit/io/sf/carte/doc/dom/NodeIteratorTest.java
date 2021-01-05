/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class NodeIteratorTest {
	private static TestDOMImplementation domImpl;
	private DOMDocument document;

	@BeforeClass
	public static void setUpBeforeClass() {
		domImpl = new TestDOMImplementation(false);
	}

	@Before
	public void setUp() {
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		document = domImpl.createDocument(null, "html", doctype);
		DOMElement docelm = document.getDocumentElement();
		docelm.setAttribute("id", "htmlId");
		docelm.setAttribute("lang", "en");
		docelm.setAttribute("class", "htmlClass");
		ProcessingInstruction pi = document.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"sheet.css\"");
		document.insertBefore(pi, docelm);
		Comment comment = document.createComment(" Comment ");
		document.insertBefore(comment, pi);
		docelm.appendChild(document.createElement("head"));
		DOMElement body = document.createElement("body");
		body.setAttribute("id", "bodyId");
		body.setAttribute("class", "bodyClass");
		docelm.appendChild(body);
		DOMElement div = document.createElement("div");
		div.setAttribute("id", "divId");
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
		DOMElement li = document.createElement("li");
		ul.appendChild(li);
		Text textli = document.createTextNode("Post li");
		ul.appendChild(textli);
		Text textul = document.createTextNode("Post ul");
		body.appendChild(textul);
		Comment commentul = document.createComment(" Comment post-ul ");
		body.appendChild(commentul);
	}

	@Test
	public void testIterator() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		assertFalse(it.hasPrevious());
		assertNull(it.previousNode());
		compareTree(document, it);
		assertFalse(it.hasNext());
		assertNull(it.nextNode());
		assertTrue(document == it.getRoot());
		assertEquals(NodeFilter.SHOW_ALL, it.getWhatToShow());
		assertNull(it.getNodeFilter());
		assertNull(((org.w3c.dom.traversal.NodeIterator) it).getFilter());
	}

	private void compareTree(Node node, NodeIterator it) {
		if (node != null) {
			Node other = it.next();
			assertTrue(node == other);
			compareTree(node.getFirstChild(), it);
			compareTree(node.getNextSibling(), it);
		}
	}

	@Test
	public void testIteratorShowAttribute() throws DOMException {
		NodeIterator it = document.createNodeIterator(document.getDocumentElement().getAttributes().item(0),
				NodeFilter.SHOW_ATTRIBUTE, null);
		assertTrue(it.hasNext());
		Attr attr = (Attr) it.next();
		assertEquals("id", attr.getName());
		assertTrue(it.hasNext());
		attr = (Attr) it.next();
		assertEquals("lang", attr.getName());
		assertTrue(it.hasNext());
		attr = (Attr) it.next();
		assertEquals("class", attr.getName());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("class", attr.getName());
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("lang", attr.getName());
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("id", attr.getName());
		assertFalse(it.hasPrevious());
	}

	@Test
	public void testIteratorShowAttributeRemove() throws DOMException {
		DOMElement docelm = document.getDocumentElement();
		NodeIterator it = document.createNodeIterator(docelm.getAttributes().item(0), NodeFilter.SHOW_ATTRIBUTE, null);
		assertTrue(it.hasNext());
		Attr attr = (Attr) it.next();
		assertEquals("id", attr.getName());
		assertTrue(it.hasNext());
		attr = (Attr) it.next();
		assertEquals("lang", attr.getName());
		assertTrue(it.hasNext());
		attr = (Attr) it.next();
		assertEquals("class", attr.getName());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		assertTrue(it.hasPrevious());
		it.remove();
		assertFalse(docelm.hasAttributeNS(attr.getNamespaceURI(), attr.getLocalName()));
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("lang", attr.getName());
		it.remove();
		assertFalse(docelm.hasAttributeNS(attr.getNamespaceURI(), attr.getLocalName()));
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("id", attr.getName());
		assertFalse(it.hasPrevious());
		// Cannot remove root node
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertFalse(it.hasPrevious());
		assertTrue(it.hasNext());
	}

	@Test
	public void testIteratorShowAttributeAdd() throws DOMException {
		DOMElement docelm = document.getDocumentElement();
		NodeIterator it = document.createNodeIterator(docelm.getAttributes().item(0), NodeFilter.SHOW_ATTRIBUTE, null);
		try {
			it.add(document.createAttribute("foo"));
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertTrue(it.hasNext());
		Attr attr = (Attr) it.next();
		assertEquals("id", attr.getName());
		it.add(document.createAttribute("foo"));
		assertTrue(it.hasNext());
		attr = (Attr) it.next();
		assertEquals("lang", attr.getName());
		assertTrue(it.hasNext());
		attr = (Attr) it.next();
		assertEquals("class", attr.getName());
		assertFalse(it.hasNext());
		it.add(document.createAttribute("bar"));
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("bar", attr.getName());
		assertTrue(it.hasPrevious());
		it.remove();
		assertFalse(docelm.hasAttributeNS(attr.getNamespaceURI(), attr.getLocalName()));
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("class", attr.getName());
		it.remove();
		assertFalse(docelm.hasAttributeNS(attr.getNamespaceURI(), attr.getLocalName()));
		attr = (Attr) it.previous();
		assertEquals("lang", attr.getName());
		it.remove();
		assertFalse(docelm.hasAttributeNS(attr.getNamespaceURI(), attr.getLocalName()));
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("foo", attr.getName());
		it.remove();
		assertFalse(docelm.hasAttributeNS(attr.getNamespaceURI(), attr.getLocalName()));
		assertTrue(it.hasPrevious());
		attr = (Attr) it.previous();
		assertEquals("id", attr.getName());
		assertFalse(it.hasPrevious());
		// Cannot remove root node
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertFalse(it.hasPrevious());
		assertTrue(it.hasNext());
	}

	@Test
	public void testIteratorShowElement() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ELEMENT, null);
		compareElementTree(document.getDocumentElement(), it);
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	private void compareElementTree(NDTNode node, NodeIterator it) {
		if (node != null) {
			Node other = it.next();
			assertTrue(node == other);
			compareElementTree(node.getFirstElementChild(), it);
			compareElementTree(node.getNextElementSibling(), it);
		}
	}

	@Test
	public void testIteratorCount() throws DOMException {
		testIteratorCount(document, NodeFilter.SHOW_ALL, null, 18);
		testIteratorCount(document, NodeFilter.SHOW_COMMENT, null, 2);
		testIteratorCount(document, NodeFilter.SHOW_ELEMENT, null, 8);
		testIteratorCount(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, null, 10);
		testIteratorCount(document, NodeFilter.SHOW_DOCUMENT_TYPE, null, 1);
		testIteratorCount(document, NodeFilter.SHOW_TEXT, null, 5);
	}

	@Test
	public void testIteratorCount2() throws DOMException {
		Node node = document.getDocumentElement();
		testIteratorCount(node, NodeFilter.SHOW_ALL, null, 14);
		testIteratorCount(node, NodeFilter.SHOW_COMMENT, null, 1);
		testIteratorCount(node, NodeFilter.SHOW_ELEMENT, null, 8);
		testIteratorCount(node, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, null, 9);
		testIteratorCount(node, NodeFilter.SHOW_DOCUMENT_TYPE, null, 0);
		testIteratorCount(node, NodeFilter.SHOW_TEXT, null, 5);
	}

	@Test
	public void testIteratorCount3() throws DOMException {
		Node node = document.getDocumentElement().getLastElementChild();
		testIteratorCount(node, NodeFilter.SHOW_ALL, null, 12);
		testIteratorCount(node, NodeFilter.SHOW_COMMENT, null, 1);
		testIteratorCount(node, NodeFilter.SHOW_ELEMENT, null, 6);
		testIteratorCount(node, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, null, 7);
		testIteratorCount(node, NodeFilter.SHOW_DOCUMENT_TYPE, null, 0);
		testIteratorCount(node, NodeFilter.SHOW_TEXT, null, 5);
	}

	@Test
	public void testIteratorCount4() throws DOMException {
		DOMElement div = document.getElementById("divId");
		testIteratorCount(div, NodeFilter.SHOW_ALL, null, 1);
		testIteratorCount(div, NodeFilter.SHOW_COMMENT, null, 0);
		testIteratorCount(div, NodeFilter.SHOW_ELEMENT, null, 1);
	}

	@Test
	public void testIteratorCountFilter() throws DOMException {
		NodeFilter filter = new ElementNameFilter("ul");
		testIteratorCount(document, NodeFilter.SHOW_ALL, filter, 17);
		testIteratorCount(document, NodeFilter.SHOW_COMMENT, filter, 2);
		testIteratorCount(document, NodeFilter.SHOW_ELEMENT, filter, 7);
		testIteratorCount(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, filter, 9);
		testIteratorCount(document, NodeFilter.SHOW_DOCUMENT_TYPE, filter, 1);
		testIteratorCount(document, NodeFilter.SHOW_TEXT, filter, 5);
		filter = new ElementNameFilter("body");
		testIteratorCount(document.getDocumentElement(), NodeFilter.SHOW_ALL, filter, 13);
		filter = new NegativeNodeFilter(new ElementNameFilter("body"));
		testIteratorCount(document.getDocumentElement(), NodeFilter.SHOW_ALL, filter, 1);
		testIteratorCount(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL, filter, 1);
		filter = new ElementNameChildFilter("ul");
		testIteratorCount(document, NodeFilter.SHOW_ALL, filter, 15);
		testIteratorCount(document, NodeFilter.SHOW_COMMENT, filter, 2);
		testIteratorCount(document, NodeFilter.SHOW_ELEMENT, filter, 6);
		testIteratorCount(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, filter, 8);
		testIteratorCount(document, NodeFilter.SHOW_DOCUMENT_TYPE, filter, 1);
		testIteratorCount(document, NodeFilter.SHOW_TEXT, filter, 5);
		filter = new ElementNameChildFilter("body");
		testIteratorCount(document, NodeFilter.SHOW_ALL, filter, 6);
		testIteratorCount(document.getDocumentElement(), NodeFilter.SHOW_ALL, filter, 2);
		filter = new ElementNameChildFilter("html");
		testIteratorCount(document, NodeFilter.SHOW_ALL, filter, 4);
	}

	private void testIteratorCount(Node rootNode, int whatToShow, NodeFilter filter, int count) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, filter);
		int i = 0;
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			it.next();
			i++;
		}
		assertEquals(count, i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		while (it.hasPrevious()) {
			it.previous();
			i--;
		}
		assertEquals(0, i);
		try {
			it.previous();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		assertTrue(it.getNodeFilter() == ((org.w3c.dom.traversal.NodeIterator) it).getFilter());
	}

	@Test
	public void testIteratorRemove() throws DOMException {
		testIteratorRemove(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testIteratorRemove2() throws DOMException {
		testIteratorRemove(document.getDocumentElement(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testIteratorRemove3() throws DOMException {
		testIteratorRemove(document.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testIteratorRemove4() throws DOMException {
		testIteratorRemove(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	private void testIteratorRemove(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		it.next(); // Skip root node
		while (it.hasNext()) {
			Node node = it.next();
			AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
			it.remove();
			assertFalse(parent.getChildNodes().contains(node));
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
		}
		it = document.createNodeIterator(rootNode, whatToShow, null);
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		assertEquals(1, i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testIteratorRemoveSecondNode() throws DOMException {
		testIteratorRemoveSecondNode(document, NodeFilter.SHOW_ALL, 17);
	}

	@Test
	public void testIteratorRemoveSecondNode3() throws DOMException {
		testIteratorRemoveSecondNode(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL, 11);
	}

	private void testIteratorRemoveSecondNode(AbstractDOMNode rootNode, int whatToShow, int count) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		it.next();
		Node node = it.next();
		it.remove();
		assertFalse(rootNode.getChildNodes().contains(node));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		it = document.createNodeIterator(rootNode, whatToShow, null);
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		assertEquals(count, i);
	}

	@Test
	public void testDescendingIteratorRemove() throws DOMException {
		testDescendingIteratorRemove(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testDescendingIteratorRemove2() throws DOMException {
		testDescendingIteratorRemove(document.getDocumentElement(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testDescendingIteratorRemove3() throws DOMException {
		testDescendingIteratorRemove(document.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testDescendingIteratorRemove4() throws DOMException {
		testDescendingIteratorRemove(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	private void testDescendingIteratorRemove(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		while (it.hasNext()) {
			it.next();
		}
		while (it.hasPrevious()) {
			Node node = it.previous();
			if (node != rootNode) {
				AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
				it.remove();
				assertFalse(parent.getChildNodes().contains(node));
				assertNull(node.getNextSibling());
				assertNull(node.getPreviousSibling());
			}
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
		}
		it = document.createNodeIterator(rootNode, whatToShow, null);
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		assertEquals(1, i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testDescendingIteratorRemoveSecondNode() throws DOMException {
		testDescendingIteratorRemoveSecondNode(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testDescendingIteratorRemoveSecondNode3() throws DOMException {
		testDescendingIteratorRemoveSecondNode(document.getDocumentElement().getLastElementChild(),
				NodeFilter.SHOW_ALL);
	}

	private void testDescendingIteratorRemoveSecondNode(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		while (it.hasNext()) {
			it.next();
		}
		it.previous();
		Node node = it.previous();
		AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
		it.remove();
		assertFalse(parent.getChildNodes().contains(node));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorRemove() throws DOMException {
		testListIteratorRemove(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testListIteratorRemove2() throws DOMException {
		testListIteratorRemove(document.getDocumentElement(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testListIteratorRemove3() throws DOMException {
		testListIteratorRemove(document.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testListIteratorRemove4() throws DOMException {
		testListIteratorRemove(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	private void testListIteratorRemove(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		it.next(); // Skip root node
		while (it.hasNext()) {
			assertEquals(1, it.nextIndex());
			Node node = it.next();
			assertEquals(1, it.previousIndex());
			AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
			it.remove();
			assertFalse(parent.getChildNodes().contains(node));
			assertEquals(1, it.nextIndex());
			assertEquals(0, it.previousIndex());
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
		}
		assertTrue(it.hasPrevious());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		assertTrue(rootNode == it.previous());
		try {
			it.previous();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testListIteratorRemoveRoot() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		it.next();
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		it = document.createNodeIterator(document.getDocumentElement(), NodeFilter.SHOW_ALL, null);
		it.next();
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorRemoveSecond() throws DOMException {
		testListIteratorRemoveSecond(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testListIteratorRemoveSecond2() throws DOMException {
		testListIteratorRemoveSecond(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	private void testListIteratorRemoveSecond(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		it.next();
		Node node = it.next();
		AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
		it.remove();
		assertFalse(parent.getChildNodes().contains(node));
		assertNull(node.getParentNode());
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		node = it.next();
		assertTrue(node == rootNode.getFirstChild());
	}

	@Test
	public void testListIteratorRemoveLast() throws DOMException {
		testListIteratorRemoveLast(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testListIteratorRemoveLast2() throws DOMException {
		testListIteratorRemoveLast(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	private void testListIteratorRemoveLast(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		NodeIterator it = document.createNodeIterator(rootNode, whatToShow, null);
		while (it.hasNext()) {
			it.next();
		}
		Node node = it.previous();
		AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
		it.remove();
		assertFalse(parent.getChildNodes().contains(node));
		assertNull(node.getParentNode());
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		it.previous();
	}

	@Test
	public void testListIteratorRemoveAndSetError() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		Node node = it.next();
		it.remove();
		assertFalse(document.getChildNodes().contains(node));
		Comment foo = document.createComment(" foo ");
		try {
			it.set(foo);
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorRemoveAndSetErrorDesc() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		while (it.hasNext()) {
			it.next();
		}
		assertTrue(it.hasPrevious());
		Node node = it.previous();
		AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
		it.remove();
		assertFalse(parent.getChildNodes().contains(node));
		Comment foo = document.createComment(" foo ");
		try {
			it.set(foo);
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorSet() throws DOMException {
		int i = 0;
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_COMMENT, null);
		try {
			it.set(document.createComment(" foo "));
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			Node node = it.next();
			assertEquals(i, it.previousIndex());
			Comment foo = document.createComment(" foo ");
			it.set(foo);
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
			i++;
		}
		assertEquals(2, i);
		assertEquals(i, it.nextIndex());
		assertEquals(i - 1, it.previousIndex());
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			if (Node.COMMENT_NODE == node.getNodeType()) {
				assertEquals(" foo ", node.getNodeValue());
			}
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
	}

	@Test
	public void testListIteratorSet2() throws DOMException {
		int i = 0;
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_TEXT, null);
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			it.next();
			assertEquals(i, it.previousIndex());
			Comment foo = document.createComment(" foo ");
			it.set(foo);
			i++;
		}
		assertEquals(7, i);
		assertEquals(i, it.nextIndex());
		assertEquals(i - 1, it.previousIndex());
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			short type = node.getNodeType();
			if (Node.COMMENT_NODE == type) {
				assertEquals(" foo ", node.getNodeValue());
			} else if (Node.TEXT_NODE == type) {
				fail("Text nodes should have been replaced");
			}
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
	}

	@Test
	public void testListIteratorSet3() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_TEXT, null);
		it.next();
		it.next();
		try {
			it.set(document);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		DOMDocument otherdoc = document.getImplementation().createDocument(null, null, null);
		try {
			it.set(otherdoc);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Comment bar = otherdoc.createComment(" bar ");
		try {
			it.set(bar);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
	}

	@Test
	public void testListIteratorSetDesc() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		int len = i;
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertEquals(i, it.nextIndex());
			short type = node.getNodeType();
			if (type != Node.ELEMENT_NODE && node != it.getRoot()) {
				Comment foo = document.createComment(" foo ");
				it.set(foo);
				assertEquals(i, it.nextIndex());
				assertNull(node.getNextSibling());
				assertNull(node.getPreviousSibling());
			}
		}
		assertEquals(0, i);
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			Node node = it.next();
			if (node != it.getRoot()) {
				short type = node.getNodeType();
				if (type != Node.COMMENT_NODE && type != Node.ELEMENT_NODE) {
					fail("Unexpected type: " + type);
				}
			}
			i++;
		}
		assertEquals(len, i);
	}

	@Test
	public void testListIteratorSetAndRemoveError() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		Comment foo = document.createComment(" foo ");
		it.set(foo);
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorSetAndRemoveErrorDesc() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		while (it.hasNext()) {
			it.next();
		}
		assertTrue(it.hasPrevious());
		it.previous();
		Comment foo = document.createComment(" foo ");
		it.set(foo);
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorAdd() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		try {
			it.add(document.createComment(" foo "));
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		int i = 0;
		Comment foo = null;
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			Node node = it.next();
			assertEquals(i, it.previousIndex());
			assertTrue(foo != node);
			foo = document.createComment(" foo ");
			it.add(foo);
			assertEquals(i + 1, it.previousIndex());
			assertEquals(i + 2, it.nextIndex());
			assertTrue(foo == it.previous());
			assertEquals(i, it.previousIndex());
			assertEquals(i + 1, it.nextIndex());
			assertTrue(foo == it.next());
			assertEquals(i + 1, it.previousIndex());
			i += 2;
		}
		assertEquals(36, i);
		try {
			it.add(document);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		DOMDocument otherdoc = document.getImplementation().createDocument(null, null, null);
		try {
			it.add(otherdoc);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		foo = otherdoc.createComment(" bar ");
		try {
			it.add(foo);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code);
		}
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			it.previous();
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
	}

	@Test
	public void testListIteratorAddDesc() throws DOMException {
		NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ALL, null);
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		Comment foo = null;
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertTrue(foo != node);
			assertEquals(i, it.nextIndex());
			if (node.getNodeType() != Node.DOCUMENT_NODE) {
				foo = document.createComment(" foo ");
				it.add(foo);
				assertEquals(i + 1, it.nextIndex());
				assertEquals(i, it.previousIndex());
				assertTrue(foo == it.previous());
				assertEquals(i, it.nextIndex());
			}
		}
		assertEquals(0, i);
		while (it.hasNext()) {
			it.next();
			i++;
			assertEquals(i, it.nextIndex());
		}
	}

}
