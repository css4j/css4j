/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

public class TreeWalkerTest {
	private static TestDOMImplementation domImpl;
	private DOMDocument document;

	@BeforeClass
	public static void setUpBeforeClass() {
		domImpl = new TestDOMImplementation(false, null);
	}

	@Before
	public void setUp() throws DOMException {
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
	public void testTreeWalker() throws DOMException {
		TreeWalker tw = document.createTreeWalker(document, NodeFilter.SHOW_ALL, null);
		assertNull(tw.previousNode());
		assertNull(tw.parentNode());
		assertNull(tw.nextSibling());
		assertNull(tw.previousSibling());
		compareTree(document.getFirstChild(), tw);
		assertNull(tw.nextNode());
		assertNull(tw.nextSibling());
		assertTrue(document == tw.getRoot());
		assertEquals(NodeFilter.SHOW_ALL, tw.getWhatToShow());
		assertNull(tw.getNodeFilter());
		assertNull(tw.getFilter());
	}

	private void compareTree(Node node, TreeWalker tw) {
		if (node != null) {
			Node other = tw.nextNode();
			assertTrue(node == other);
			compareTree(node.getFirstChild(), tw);
			compareTree(node.getNextSibling(), tw);
		}
	}

	@Test
	public void testTreeWalkerShowAttribute() throws DOMException {
		TreeWalker tw = document.createTreeWalker(document.getDocumentElement().getAttributes().item(0),
				NodeFilter.SHOW_ATTRIBUTE, null);
		Attr attr = (Attr) tw.nextNode();
		assertNotNull(attr);
		assertEquals("lang", attr.getName());
		attr = (Attr) tw.nextNode();
		assertNotNull(attr);
		assertEquals("class", attr.getName());
		assertNull(tw.nextNode());
		attr = (Attr) tw.previousNode();
		assertNotNull(attr);
		assertEquals("lang", attr.getName());
		attr = (Attr) tw.previousNode();
		assertNotNull(attr);
		assertEquals("id", attr.getName());
		assertNull(tw.previousNode());
	}

	@Test
	public void testTreeWalkerShowElement() throws DOMException {
		TreeWalker tw = document.createTreeWalker(document, NodeFilter.SHOW_ELEMENT, null);
		compareElementTree(document.getDocumentElement(), tw);
		assertNull(tw.nextNode());
	}

	private void compareElementTree(NDTNode node, TreeWalker tw) {
		if (node != null) {
			Node other = tw.nextNode();
			assertTrue(node == other);
			compareElementTree(node.getFirstElementChild(), tw);
			compareElementTree(node.getNextElementSibling(), tw);
		}
	}

	@Test
	public void testTreeWalkerCount() throws DOMException {
		testTreeWalkerCount(document, NodeFilter.SHOW_ALL, null, 17);
		testTreeWalkerCount(document, NodeFilter.SHOW_COMMENT, null, 2);
		testTreeWalkerCount(document, NodeFilter.SHOW_ELEMENT, null, 8);
		testTreeWalkerCount(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, null, 10);
		testTreeWalkerCount(document, NodeFilter.SHOW_DOCUMENT_TYPE, null, 1);
		testTreeWalkerCount(document, NodeFilter.SHOW_TEXT, null, 5);
	}

	@Test
	public void testTreeWalkerCount2() throws DOMException {
		Node node = document.getDocumentElement();
		testTreeWalkerCount(node, NodeFilter.SHOW_ALL, null, 13);
		testTreeWalkerCount(node, NodeFilter.SHOW_COMMENT, null, 1);
		testTreeWalkerCount(node, NodeFilter.SHOW_ELEMENT, null, 7);
		testTreeWalkerCount(node, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, null, 8);
		testTreeWalkerCount(node, NodeFilter.SHOW_DOCUMENT_TYPE, null, 0);
		testTreeWalkerCount(node, NodeFilter.SHOW_TEXT, null, 5);
	}

	@Test
	public void testTreeWalkerCount3() throws DOMException {
		Node node = document.getDocumentElement().getLastElementChild();
		testTreeWalkerCount(node, NodeFilter.SHOW_ALL, null, 11);
		testTreeWalkerCount(node, NodeFilter.SHOW_COMMENT, null, 1);
		testTreeWalkerCount(node, NodeFilter.SHOW_ELEMENT, null, 5);
		testTreeWalkerCount(node, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, null, 6);
		testTreeWalkerCount(node, NodeFilter.SHOW_DOCUMENT_TYPE, null, 0);
		testTreeWalkerCount(node, NodeFilter.SHOW_TEXT, null, 5);
	}

	@Test
	public void testTreeWalkerCount4() throws DOMException {
		DOMElement div = document.getElementById("divId");
		testTreeWalkerCount(div, NodeFilter.SHOW_ALL, null, 0);
		testTreeWalkerCount(div, NodeFilter.SHOW_COMMENT, null, 0);
		testTreeWalkerCount(div, NodeFilter.SHOW_ELEMENT, null, 0);
	}

	@Test
	public void testTreeWalkerCountFilter() throws DOMException {
		NodeFilter filter = new ElementNameFilter("ul");
		testTreeWalkerCount(document, NodeFilter.SHOW_ALL, filter, 16);
		testTreeWalkerCount(document, NodeFilter.SHOW_COMMENT, filter, 2);
		testTreeWalkerCount(document, NodeFilter.SHOW_ELEMENT, filter, 7);
		testTreeWalkerCount(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, filter, 9);
		testTreeWalkerCount(document, NodeFilter.SHOW_DOCUMENT_TYPE, filter, 1);
		testTreeWalkerCount(document, NodeFilter.SHOW_TEXT, filter, 5);
		filter = new ElementNameFilter("body");
		testTreeWalkerCount(document.getDocumentElement(), NodeFilter.SHOW_ALL, filter, 12);
		filter = new NegativeNodeFilter(new ElementNameFilter("body"));
		testTreeWalkerCount(document.getDocumentElement(), NodeFilter.SHOW_ALL, filter, 1);
		testTreeWalkerCount(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL, filter, 0);
		filter = new ElementNameChildFilter("ul");
		testTreeWalkerCount(document, NodeFilter.SHOW_ALL, filter, 14);
		testTreeWalkerCount(document, NodeFilter.SHOW_COMMENT, filter, 2);
		testTreeWalkerCount(document, NodeFilter.SHOW_ELEMENT, filter, 6);
		testTreeWalkerCount(document, NodeFilter.SHOW_COMMENT | NodeFilter.SHOW_ELEMENT, filter, 8);
		testTreeWalkerCount(document, NodeFilter.SHOW_DOCUMENT_TYPE, filter, 1);
		testTreeWalkerCount(document, NodeFilter.SHOW_TEXT, filter, 5);
		filter = new ElementNameChildFilter("body");
		testTreeWalkerCount(document, NodeFilter.SHOW_ALL, filter, 5);
		testTreeWalkerCount(document.getDocumentElement(), NodeFilter.SHOW_ALL, filter, 1);
		filter = new ElementNameChildFilter("html");
		testTreeWalkerCount(document, NodeFilter.SHOW_ALL, filter, 3);
	}

	private void testTreeWalkerCount(Node rootNode, int whatToShow, NodeFilter filter, int count) throws DOMException {
		TreeWalker tw = document.createTreeWalker(rootNode, whatToShow, filter);
		int i = 0;
		while (tw.nextNode() != null) {
			i++;
		}
		assertEquals(count, i);
		assertNull(tw.nextNode());
		int rootmask = NodeFilter.maskTable[rootNode.getNodeType() - 1];
		if (i != 0 && ((rootmask & whatToShow) != rootmask
				|| (filter != null && filter.acceptNode(rootNode) != NodeFilter.FILTER_ACCEPT))) {
			i--;
		}
		while (tw.previousNode() != null) {
			i--;
		}
		assertEquals(0, i);
		assertNull(tw.previousNode());
		assertTrue(tw.getNodeFilter() == tw.getFilter());
	}

	@Test
	public void testTreeWalkerRemove() throws DOMException {
		testTreeWalkerRemove(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testTreeWalkerRemove2() throws DOMException {
		testTreeWalkerRemove(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testTreeWalkerRemove3() throws DOMException {
		DOMElement root = document.getDocumentElement();
		TreeWalker tw = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT, null);
		Node node1 = tw.nextNode();
		assertNotNull(node1);
		Node node2 = tw.nextNode();
		assertNotNull(node2);
		node1.getParentNode().removeChild(node1);
		assertTrue(root == tw.previousNode());
		assertTrue(node2 == tw.nextNode());
	}

	private void testTreeWalkerRemove(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		TreeWalker tw = document.createTreeWalker(rootNode, whatToShow, null);
		Node node1 = tw.nextNode();
		assertNotNull(node1);
		Node node2 = tw.nextNode();
		assertNotNull(node2);
		Node node3 = tw.nextNode();
		node2.getParentNode().removeChild(node2);
		assertTrue(node1 == tw.previousNode());
		assertTrue(node3 == tw.nextNode());
	}

	@Test
	public void testDescendingWalkerRemove() throws DOMException {
		testDescendingWalkerRemove(document, NodeFilter.SHOW_ALL);
	}

	@Test
	public void testDescendingWalkerRemove2() throws DOMException {
		testDescendingWalkerRemove(document.getDocumentElement(), NodeFilter.SHOW_ALL);
	}

	@Test
	public void testDescendingWalkerRemove3() throws DOMException {
		testDescendingWalkerRemove(document.getDocumentElement().getLastElementChild(), NodeFilter.SHOW_ALL);
	}

	private void testDescendingWalkerRemove(AbstractDOMNode rootNode, int whatToShow) throws DOMException {
		TreeWalker tw = document.createTreeWalker(rootNode, whatToShow, null);
		while (tw.nextNode() != null) {
		}
		Node node3 = tw.previousNode();
		assertNotNull(node3);
		Node node2 = tw.previousNode();
		assertNotNull(node2);
		Node node1 = tw.previousNode();
		node2.getParentNode().removeChild(node2);
		assertTrue(node3 == tw.nextNode());
		assertTrue(node1 == tw.previousNode());
	}

}
