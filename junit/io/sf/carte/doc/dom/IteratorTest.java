/*

 Copyright (c) 2005-2020, Carlos Amengual.

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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class IteratorTest {
	private static TestDOMImplementation domImpl;
	private DOMDocument document;

	@BeforeClass
	public static void setUpBeforeClass() {
		domImpl = new TestDOMImplementation(false, null);
	}

	@Before
	public void setUp() {
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
	public void testIterator() throws DOMException {
		testIterator(document);
	}

	@Test
	public void testIterator2() throws DOMException {
		testIterator(document.getDocumentElement());
	}

	@Test
	public void testIterator3() throws DOMException {
		testIterator(document.getDocumentElement().getFirstElementChild());
	}

	private void testIterator(ParentNode parentNode) throws DOMException {
		DOMNodeList list = parentNode.getChildNodes();
		int i = 0;
		Iterator<DOMNode> it = parentNode.iterator();
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
		// Iterable
		it = parentNode.iterator();
		for (DOMNode node : list) {
			assertTrue(node == it.next());
		}
		assertFalse(it.hasNext());
	}

	@Test
	public void testIteratorRemove() throws DOMException {
		testIteratorRemove(document);
	}

	@Test
	public void testIteratorRemove2() throws DOMException {
		testIteratorRemove(document.getDocumentElement());
	}

	@Test
	public void testIteratorRemove3() throws DOMException {
		testIteratorRemove(document.getDocumentElement().getFirstElementChild());
	}

	private void testIteratorRemove(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		int i = 0;
		Iterator<DOMNode> it = parentNode.iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			assertTrue(list.item(0) == node);
			it.remove();
			assertFalse(parentNode.getChildNodes().contains(node));
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
			i++;
		}
		assertEquals(len, i);
		assertEquals(0, list.getLength());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testIteratorRemoveSecondNode() throws DOMException {
		testIteratorRemoveSecondNode(document);
	}

	@Test
	public void testIteratorRemoveSecondNode3() throws DOMException {
		testIteratorRemoveSecondNode(document.getDocumentElement().getFirstElementChild());
	}

	private void testIteratorRemoveSecondNode(ParentNode parentNode) throws DOMException {
		Iterator<DOMNode> it = parentNode.iterator();
		it.next();
		DOMNode node = it.next();
		it.remove();
		assertFalse(parentNode.getChildNodes().contains(node));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testDescendingIterator() throws DOMException {
		testDescendingIterator(document);
	}

	@Test
	public void testDescendingIterator2() throws DOMException {
		testDescendingIterator(document.getDocumentElement());
	}

	@Test
	public void testDescendingIterator3() throws DOMException {
		testDescendingIterator(document.getDocumentElement().getFirstElementChild());
	}

	private void testDescendingIterator(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int i = list.getLength();
		Iterator<DOMNode> it = parentNode.descendingIterator();
		while (it.hasNext()) {
			i--;
			assertTrue(list.item(i) == it.next());
		}
		assertEquals(0, i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testDescendingIteratorRemove() throws DOMException {
		testDescendingIteratorRemove(document);
	}

	@Test
	public void testDescendingIteratorRemove2() throws DOMException {
		testDescendingIteratorRemove(document.getDocumentElement());
	}

	@Test
	public void testDescendingIteratorRemove3() throws DOMException {
		testDescendingIteratorRemove(document.getDocumentElement().getFirstElementChild());
	}

	private void testDescendingIteratorRemove(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		int i = 0;
		Iterator<DOMNode> it = parentNode.descendingIterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			assertTrue(list.item(list.getLength() - 1) == node);
			it.remove();
			assertFalse(parentNode.getChildNodes().contains(node));
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
			i++;
		}
		assertEquals(len, i);
		assertEquals(0, list.getLength());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testDescendingIteratorRemoveSecondNode() throws DOMException {
		testDescendingIteratorRemoveSecondNode(document);
	}

	@Test
	public void testDescendingIteratorRemoveSecondNode3() throws DOMException {
		testDescendingIteratorRemoveSecondNode(document.getDocumentElement().getFirstElementChild());
	}

	private void testDescendingIteratorRemoveSecondNode(ParentNode parentNode) throws DOMException {
		Iterator<DOMNode> it = parentNode.descendingIterator();
		it.next();
		DOMNode node = it.next();
		it.remove();
		assertFalse(parentNode.getChildNodes().contains(node));
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testElementIterator() throws DOMException {
		testElementIterator(document);
	}

	@Test
	public void testElementIterator2() throws DOMException {
		testElementIterator(document.getDocumentElement());
	}

	@Test
	public void testElementIterator3() throws DOMException {
		testElementIterator(document.getDocumentElement().getFirstElementChild());
	}

	private void testElementIterator(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int i = 0, j = 0;
		Iterator<DOMElement> it = parentNode.elementIterator();
		while (it.hasNext()) {
			Node node = list.item(i);
			while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
				i++;
				node = list.item(i);
			}
			assertTrue(node == it.next());
			i++;
			j++;
		}
		assertEquals(parentNode.getChildElementCount(), j);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		// Iterable
		ElementList elist = parentNode.getChildren();
		it = parentNode.elementIterator();
		for (DOMElement element : elist) {
			assertTrue(element == it.next());
		}
		assertFalse(it.hasNext());
	}

	@Test
	public void testElementIteratorRemoveSecondElement() throws DOMException {
		testElementIteratorRemoveSecondElement(document.getDocumentElement().getFirstElementChild());
	}

	private void testElementIteratorRemoveSecondElement(ParentNode parentNode) throws DOMException {
		Iterator<DOMElement> it = parentNode.elementIterator();
		it.next();
		Node node = it.next();
		it.remove();
		assertFalse(parentNode.getChildNodes().contains(node));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testAttributeIterator() throws DOMException {
		DOMElement element = document.getDocumentElement();
		element.setAttribute("id", "myId");
		element.setAttribute("class", "myClass1 myClass2");
		element.setAttribute("lang", "myLang");
		element.setAttribute("style", "display:inline");
		AttributeNamedNodeMap nnm = element.getAttributes();
		int i = 0;
		Iterator<Attr> it = nnm.iterator();
		while (it.hasNext()) {
			Attr node = nnm.item(i);
			assertTrue(node == it.next());
			i++;
		}
		assertEquals(nnm.getLength(), i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testAttributeIteratorRemove() throws DOMException {
		DOMElement element = document.getDocumentElement();
		element.setAttribute("id", "myId");
		element.setAttribute("class", "myClass1 myClass2");
		element.setAttribute("lang", "myLang");
		element.setAttribute("style", "display:inline");
		AttributeNamedNodeMap nnm = element.getAttributes();
		Iterator<Attr> it = nnm.iterator();
		it.next();
		Attr node = it.next();
		assertEquals("class", node.getName());
		it.remove(); // remove class
		assertNull(nnm.getNamedItemNS(node.getNamespaceURI(), node.getLocalName()));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals("lang", node.getName());
		assertTrue(node == nnm.item(1));
		node = it.next();
		assertEquals("style", node.getName());
		it.remove(); // remove style
		assertNull(nnm.getNamedItemNS(node.getNamespaceURI(), node.getLocalName()));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertEquals(2, nnm.getLength());
		assertFalse(it.hasNext());
		//
		it = nnm.iterator();
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals("id", node.getName());
		it.remove(); // remove id
		assertNull(nnm.getNamedItemNS(node.getNamespaceURI(), node.getLocalName()));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertEquals(1, nnm.getLength());
		assertTrue(it.hasNext());
		node = it.next();
		assertEquals("lang", node.getName());
		it.remove(); // remove lang
		assertNull(nnm.getNamedItemNS(node.getNamespaceURI(), node.getLocalName()));
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
		assertEquals(0, nnm.getLength());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testListIterator() throws DOMException {
		testListIterator(document);
	}

	@Test
	public void testListIterator2() throws DOMException {
		testListIterator(document.getDocumentElement());
	}

	@Test
	public void testListIterator3() throws DOMException {
		testListIterator(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIterator(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int i = 0;
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			assertTrue(list.item(i) == it.next());
			assertEquals(i, it.previousIndex());
			i++;
		}
		assertEquals(list.getLength(), i);
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			assertTrue(list.item(i) == it.previous());
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
		try {
			it.previous();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testListIteratorRemove() throws DOMException {
		testListIteratorRemove(document);
	}

	@Test
	public void testListIteratorRemove2() throws DOMException {
		testListIteratorRemove(document.getDocumentElement());
	}

	@Test
	public void testListIteratorRemove3() throws DOMException {
		testListIteratorRemove(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorRemove(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		int i = 0;
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			assertEquals(0, it.nextIndex());
			Node node = it.next();
			assertTrue(parentNode.getFirstChild() == node);
			assertTrue(list.item(0) == node);
			assertEquals(0, it.previousIndex());
			it.remove();
			assertFalse(parentNode.getChildNodes().contains(node));
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
			assertEquals(0, it.nextIndex());
			assertEquals(-1, it.previousIndex());
			i++;
			try {
				it.remove();
				fail("Must throw exception");
			} catch (IllegalStateException e) {
			}
		}
		assertEquals(len, i);
		assertEquals(0, list.getLength());
		assertNull(parentNode.getFirstChild());
		assertNull(parentNode.getLastChild());
		try {
			it.previous();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testListIteratorRemoveFirst() throws DOMException {
		testListIteratorRemoveFirst(document);
	}

	@Test
	public void testListIteratorRemoveFirst2() throws DOMException {
		testListIteratorRemoveFirst(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorRemoveFirst(ParentNode parentNode) throws DOMException {
		NodeListIterator it = parentNode.listIterator();
		Node node = it.next();
		assertTrue(node == parentNode.getFirstChild());
		it.remove();
		assertNull(node.getParentNode());
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		assertFalse(parentNode.getChildNodes().contains(node));
		node = it.next();
		assertTrue(node == parentNode.getFirstChild());
	}

	@Test
	public void testListIteratorRemoveLast() throws DOMException {
		testListIteratorRemoveLast(document);
	}

	@Test
	public void testListIteratorRemoveLast2() throws DOMException {
		testListIteratorRemoveLast(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorRemoveLast(ParentNode parentNode) throws DOMException {
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			it.next();
		}
		Node node = it.previous();
		assertTrue(node == parentNode.getLastChild());
		it.remove();
		assertNull(node.getParentNode());
		assertNull(node.getNextSibling());
		assertNull(node.getPreviousSibling());
		assertFalse(parentNode.getChildNodes().contains(node));
		node = it.previous();
		assertTrue(node == parentNode.getLastChild());
	}

	@Test
	public void testListIteratorRemoveDesc() throws DOMException {
		testListIteratorRemoveDesc(document);
	}

	@Test
	public void testListIteratorRemoveDesc2() throws DOMException {
		testListIteratorRemoveDesc(document.getDocumentElement());
	}

	@Test
	public void testListIteratorRemoveDesc3() throws DOMException {
		testListIteratorRemoveDesc(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorRemoveDesc(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			it.next();
		}
		int len = list.getLength();
		int i = len;
		while (it.hasPrevious()) {
			assertEquals(i, it.nextIndex());
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertTrue(list.item(i) == node);
			it.remove();
			assertFalse(parentNode.getChildNodes().contains(node));
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
		}
		assertEquals(0, i);
		assertNull(parentNode.getFirstChild());
		assertNull(parentNode.getLastChild());
		try {
			it.previous();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
		try {
			it.next();
			fail("Must throw exception");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testListIteratorRemoveAndSetError() throws DOMException {
		NodeListIterator it = document.listIterator();
		assertTrue(it.hasNext());
		it.next();
		it.remove();
		Comment foo = document.createComment(" foo" );
		try {
			it.set(foo);
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorRemoveAndSetErrorDesc() throws DOMException {
		NodeListIterator it = document.listIterator();
		while (it.hasNext()) {
			it.next();
		}
		assertTrue(it.hasPrevious());
		it.previous();
		it.remove();
		Comment foo = document.createComment(" foo" );
		try {
			it.set(foo);
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorSet() throws DOMException {
		testListIteratorSet(document);
	}

	@Test
	public void testListIteratorSet2() throws DOMException {
		testListIteratorSet(document.getDocumentElement());
	}

	@Test
	public void testListIteratorSet3() throws DOMException {
		testListIteratorSet(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorSet(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		int i = 0;
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			Node node = it.next();
			assertTrue(list.item(i) == node);
			assertEquals(i, it.previousIndex());
			Comment foo = document.createComment(" foo" );
			it.set(foo);
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
			i++;
		}
		assertEquals(len, i);
		assertEquals(len, list.getLength());
		assertEquals(i, it.nextIndex());
		assertEquals(i - 1, it.previousIndex());
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertTrue(list.item(i) == node);
			assertEquals(Node.COMMENT_NODE, node.getNodeType());
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
	}

	@Test
	public void testListIteratorSetDesc() throws DOMException {
		testListIteratorSetDesc(document);
	}

	@Test
	public void testListIteratorSetDesc2() throws DOMException {
		testListIteratorSetDesc(document.getDocumentElement());
	}

	@Test
	public void testListIteratorSetDesc3() throws DOMException {
		testListIteratorSetDesc(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorSetDesc(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			it.next();
		}
		int i = len;
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertTrue(list.item(i) == node);
			assertEquals(i, it.nextIndex());
			Comment foo = document.createComment(" foo" );
			it.set(foo);
			assertEquals(i, it.nextIndex());
			assertNull(node.getNextSibling());
			assertNull(node.getPreviousSibling());
		}
		assertEquals(0, i);
		assertEquals(len, list.getLength());
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			Node node = it.next();
			assertTrue(list.item(i) == node);
			assertEquals(Node.COMMENT_NODE, node.getNodeType());
			i++;
		}
		assertEquals(len, i);
	}

	@Test
	public void testListIteratorSetAndRemoveError() throws DOMException {
		NodeListIterator it = document.listIterator();
		assertTrue(it.hasNext());
		it.next();
		Comment foo = document.createComment(" foo" );
		it.set(foo);
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorSetAndRemoveErrorDesc() throws DOMException {
		NodeListIterator it = document.listIterator();
		while (it.hasNext()) {
			it.next();
		}
		assertTrue(it.hasPrevious());
		it.previous();
		Comment foo = document.createComment(" foo" );
		it.set(foo);
		try {
			it.remove();
			fail("Must throw exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testListIteratorAdd() throws DOMException {
		testListIteratorAdd(document);
	}

	@Test
	public void testListIteratorAdd2() throws DOMException {
		testListIteratorAdd(document.getDocumentElement());
	}

	@Test
	public void testListIteratorAdd3() throws DOMException {
		testListIteratorAdd(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorAdd(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		int i = 0;
		Comment foo = null;
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			assertEquals(i, it.nextIndex());
			Node node = it.next();
			assertEquals(i, it.previousIndex());
			assertTrue(foo != node);
			assertTrue(list.item(i) == node);
			foo = document.createComment(" foo" );
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
		len += len;
		assertEquals(len, i);
		assertEquals(len, list.getLength());
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertTrue(list.item(i) == node);
			if ((i & 1) == 1) {
				assertTrue(node.getNodeType() == Node.COMMENT_NODE);
			}
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
	}

	@Test
	public void testListIteratorAddDesc() throws DOMException {
		testListIteratorAddDesc(document);
	}

	@Test
	public void testListIteratorAddDesc2() throws DOMException {
		testListIteratorAddDesc(document.getDocumentElement());
	}

	@Test
	public void testListIteratorAddDesc3() throws DOMException {
		testListIteratorAddDesc(document.getDocumentElement().getFirstElementChild());
	}

	private void testListIteratorAddDesc(ParentNode parentNode) throws DOMException {
		NodeList list = parentNode.getChildNodes();
		int len = list.getLength();
		NodeListIterator it = parentNode.listIterator();
		while (it.hasNext()) {
			it.next();
		}
		int i = len;
		Comment foo = null;
		while (it.hasPrevious()) {
			i--;
			assertEquals(i, it.previousIndex());
			Node node = it.previous();
			assertTrue(list.item(i) == node);
			assertTrue(foo != node);
			assertEquals(i, it.nextIndex());
			foo = document.createComment(" foo" );
			it.add(foo);
			assertEquals(i + 1, it.nextIndex());
			assertEquals(i, it.previousIndex());
			assertTrue(foo == it.previous());
			assertEquals(i, it.nextIndex());
		}
		assertEquals(0, i);
		len += len;
		assertEquals(len, list.getLength());
		while (it.hasNext()) {
			Node node = it.next();
			assertTrue(list.item(i) == node);
			if ((i & 1) == 0) {
				assertTrue(node.getNodeType() == Node.COMMENT_NODE);
			}
			i++;
			assertEquals(i, it.nextIndex());
		}
		assertEquals(len, i);
	}

}
