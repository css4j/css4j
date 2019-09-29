/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

/**
 * Compare <code>NodeList</code> and <code>NodeIterator</code> implementations
 * with a reference (JAXP).
 * <p>
 * Iterators cannot be fully compared due to differences in how attributes are
 * handled (see comments in source code).
 */
public class CollectionsTest {
	private static XMLDocumentBuilder builder;

	@BeforeClass
	public static void setUpBeforeClass() {
		TestDOMImplementation impl = new TestDOMImplementation(false);
		impl.setXmlOnly(true);
		builder = new XMLDocumentBuilder(impl);
		builder.getSAXParserFactory().setNamespaceAware(true);
		builder.setEntityResolver(new DefaultEntityResolver());
	}

	@Test
	public void testDocumentElement() throws DOMException, SAXException, IOException {
		DOMDocument xmlDoc = readTestDocument(true);
		DOMElement docelm = xmlDoc.getDocumentElement();
		NodeList list = docelm.getChildNodes();
		assertEquals(2, list.getLength());
		Node fcNode = docelm.getFirstChild();
		Node lcNode = docelm.getLastChild();
		DOMElement fcElement = docelm.getFirstElementChild();
		DOMElement lcElement = docelm.getLastElementChild();
		assertEquals("head", fcNode.getLocalName());
		assertEquals("head", fcElement.getTagName());
		assertEquals("body", lcNode.getLocalName());
		assertEquals("body", lcElement.getTagName());
		assertTrue(fcNode == fcElement);
		assertTrue(lcNode == lcElement);
		assertTrue(fcNode == lcElement.getPreviousSibling());
		assertTrue(fcNode == lcElement.getPreviousElementSibling());
		assertTrue(lcNode == fcElement.getNextSibling());
		assertTrue(lcNode == fcElement.getNextElementSibling());
		assertTrue(fcNode == list.item(0));
		assertTrue(lcNode == list.item(1));
		//
		DOMElement element = fcElement;
		list = element.getChildNodes();
		assertEquals(4, list.getLength());
		fcNode = element.getFirstChild();
		lcNode = element.getLastChild();
		fcElement = element.getFirstElementChild();
		lcElement = element.getLastElementChild();
		assertEquals("title", fcNode.getLocalName());
		assertEquals("title", fcElement.getTagName());
		assertEquals("base", lcNode.getLocalName());
		assertEquals("base", lcElement.getTagName());
		assertTrue(fcNode == fcElement);
		assertTrue(lcNode == lcElement);
		assertTrue(fcNode == list.item(0));
		assertTrue(lcNode == list.item(3));
	}

	@Test
	public void testReference() throws ParserConfigurationException, SAXException, IOException {
		compareToReferenceDocumentBuilder(true);
	}

	@Test
	public void testReferenceECW() throws ParserConfigurationException, SAXException, IOException {
		compareToReferenceDocumentBuilder(false);
	}

	private void compareToReferenceDocumentBuilder(boolean ignoreElementContentWhitespace)
			throws ParserConfigurationException, SAXException, IOException {
		DOMDocument xmlDoc = readTestDocument(ignoreElementContentWhitespace);
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		dbFac.setIgnoringElementContentWhitespace(ignoreElementContentWhitespace);
		dbFac.setNamespaceAware(true);
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		docb.setEntityResolver(new DefaultEntityResolver());
		Reader re = XMLDocumentBuilderTest.loadEntitiesReader();
		InputSource source = new InputSource(re);
		Document refdoc = docb.parse(source);
		re.close();
		refdoc.setDocumentURI("http://www.example.com/xml/entities.xhtml");
		DocumentType refdoctype = refdoc.getDoctype();
		DocumentType doctype = xmlDoc.getDoctype();
		if (refdoctype == null) {
			assertNull(doctype);
		} else {
			assertEquals(refdoctype.getName(), doctype.getName());
			assertEquals(refdoctype.getSystemId(), doctype.getSystemId());
			assertEquals(refdoctype.getPublicId(), doctype.getPublicId());
		}
		Element refde = refdoc.getDocumentElement();
		DOMElement de = xmlDoc.getDocumentElement();
		compareNodes(refde, de);
		compareNodeIterators(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ALL, null);
		compareNodeIterators(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT, null);
		compareNodeIterators(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null);
		compareNodeIterators(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ALL, null);
		compareNodeIterators(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ELEMENT, null);
		compareNodeIterators(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null);
		compareNodeIterators(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_COMMENT, null);
		NodeFilter filter = new ElementNameFilter("ul");
		compareNodeIterators(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_COMMENT, filter);
		compareTreeWalkers(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ALL, null);
		compareTreeWalkers(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT, null);
		compareTreeWalkers(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null);
		compareTreeWalkers(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ELEMENT, null);
		compareTreeWalkers(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_COMMENT, null);
		compareTreeWalkers(refdoc, refdoc.getElementById("ul1li1"), xmlDoc, xmlDoc.getElementById("ul1li1"),
				NodeFilter.SHOW_ATTRIBUTE, null);
		compareTreeWalkers(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_COMMENT, filter);
		compareTreeWalkers2(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ALL, null);
		compareTreeWalkers2(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT, null);
		compareTreeWalkers2(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null);
		compareTreeWalkers2(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ELEMENT, null);
		compareTreeWalkers2(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_COMMENT, null);
		compareTreeWalkers2(refdoc, refdoc.getElementById("ul1li1"), xmlDoc, xmlDoc.getElementById("ul1li1"),
				NodeFilter.SHOW_ATTRIBUTE, null);
		compareTreeWalkers2(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_COMMENT, filter);
		compareTreeWalkersChild(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ALL, null);
		compareTreeWalkersChild(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT, null);
		compareTreeWalkersChild(refdoc, refdoc, xmlDoc, xmlDoc, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null);
		compareTreeWalkersChild(refdoc, refdoc.getDocumentElement().getElementsByTagName("head").item(0), xmlDoc,
				xmlDoc.getDocumentElement().getFirstElementChild(), NodeFilter.SHOW_ELEMENT, null);
	}

	private void compareNodes(Node refde, Node de) {
		compareNodesBasic(refde, de);
		// Attributes
		NamedNodeMap refnnm = refde.getAttributes();
		if (refnnm != null) {
			compareAttributes(refnnm, de.getAttributes());
		}
		// Child nodes
		Node reffcNode = refde.getFirstChild();
		Node reflcNode = refde.getLastChild();
		Node fcNode = de.getFirstChild();
		Node lcNode = de.getLastChild();
		if (reffcNode != null) {
			assertNotNull(fcNode);
			compareNodesBasic(reffcNode, fcNode);
			compareNodesBasic(reflcNode, lcNode);
			NodeList list = de.getChildNodes();
			NodeList reflist = refde.getChildNodes();
			int len = list.getLength();
			assertEquals(reflist.getLength(), len);
			assertTrue(fcNode == list.item(0));
			assertTrue(lcNode == list.item(len - 1));
			if (de.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement fcElement = ((DOMElement) de).getFirstElementChild();
				DOMElement lcElement = ((DOMElement) de).getLastElementChild();
				if (fcNode.getNodeType() == Node.ELEMENT_NODE) {
					assertTrue(fcNode == fcElement);
				}
				if (lcNode.getNodeType() == Node.ELEMENT_NODE) {
					assertTrue(lcNode == lcElement);
				}
			}
			// List items
			for (int i = 0; i < len; i++) {
				compareNodes(reflist.item(i), list.item(i));
			}
		}
	}

	private void compareNodesBasic(Node refde, Node de) {
		assertEquals(refde.getNodeType(), de.getNodeType());
		assertEquals(refde.getNodeName(), de.getNodeName());
	}

	private void compareAttributes(NamedNodeMap refnnm, NamedNodeMap nnm) {
		assertNotNull(nnm);
		int nnmlen = nnm.getLength();
		for (int i = 0; i < nnmlen; i++) {
			Attr refitem = (Attr) refnnm.item(i);
			Node named = nnm.getNamedItemNS(refitem.getNamespaceURI(), refitem.getLocalName());
			if (refitem.getSpecified() || named != null) {
				assertNotNull(named);
				Node named2 = nnm.getNamedItem(named.getNodeName());
				assertTrue(named == named2);
				assertTrue(refitem == refnnm.getNamedItem(named.getNodeName()));
				compareNodesBasic(refitem, named);
				if (!"style".equalsIgnoreCase(refitem.getNodeName())) {
					assertEquals(refitem.getNodeValue(), named.getNodeValue());
				}
			}
		}
	}

	private void compareNodeIterators(Document refDoc, Node refroot, DOMDocument doc, AbstractDOMNode root,
			int whatToShow, NodeFilter filter) {
		org.w3c.dom.traversal.NodeIterator refit = ((DocumentTraversal) refDoc).createNodeIterator(refroot, whatToShow,
				(org.w3c.dom.traversal.NodeFilter) filter, false);
		NodeIterator it = doc.createNodeIterator(root, whatToShow, filter);
		while (it.hasNext()) {
			DOMNode node = it.next();
			Node refnode = refit.nextNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
			// We do not compare attributes due to an issue with Xerces
			// (and another with the specification itself):
			//
			// a) Attributes do not always follow the specified order in Xerces.
			// b) Its iterator of attributes only traverses the root node
			//    (different interpretation of the specification).
		}
		assertNull(refit.nextNode());
		while (it.hasPrevious()) {
			DOMNode node = it.previous();
			Node refnode = refit.previousNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
		}
		assertNull(refit.previousNode());
	}

	private void compareTreeWalkers(Document refDoc, Node refroot, DOMDocument doc, AbstractDOMNode root,
			int whatToShow, NodeFilter filter) {
		org.w3c.dom.traversal.TreeWalker reftw = ((DocumentTraversal) refDoc).createTreeWalker(refroot, whatToShow,
				(org.w3c.dom.traversal.NodeFilter) filter, false);
		TreeWalker tw = doc.createTreeWalker(root, whatToShow, filter);
		DOMNode node;
		while ((node = tw.nextNode()) != null) {
			Node refnode = reftw.nextNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
			// We do not compare attributes due to differences in
			// behaviour with Xerces.
		}
		assertNull(reftw.nextNode());
		while ((node = tw.previousNode()) != null) {
			Node refnode = reftw.previousNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
		}
		assertNull(reftw.previousNode());
	}

	private void compareTreeWalkers2(Document refDoc, Node refroot, DOMDocument doc, AbstractDOMNode root,
			int whatToShow, NodeFilter filter) {
		org.w3c.dom.traversal.TreeWalker reftw = ((DocumentTraversal) refDoc).createTreeWalker(refroot, whatToShow,
				(org.w3c.dom.traversal.NodeFilter) filter, false);
		TreeWalker tw = doc.createTreeWalker(root, whatToShow, filter);
		DOMNode node;
		while ((node = tw.nextNode()) != null) {
			Node refnode = reftw.nextNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
			compareSiblings(reftw, tw);
			tw.setCurrentNode(node);
			reftw.setCurrentNode(refnode);
		}
		assertNull(reftw.nextNode());
		while ((node = tw.previousNode()) != null) {
			Node refnode = reftw.previousNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
			compareSiblings(reftw, tw);
			tw.setCurrentNode(node);
			reftw.setCurrentNode(refnode);
		}
		assertNull(reftw.previousNode());
	}

	private void compareSiblings(org.w3c.dom.traversal.TreeWalker reftw, TreeWalker tw) {
		Node sibling, refsibling;
		while ((sibling = tw.nextSibling()) != null) {
			refsibling = reftw.nextSibling();
			assertNotNull(refsibling);
			compareNodesBasic(refsibling, sibling);
		}
		while ((sibling = tw.previousSibling()) != null) {
			refsibling = reftw.previousSibling();
			assertNotNull(refsibling);
			compareNodesBasic(refsibling, sibling);
		}
	}

	private void compareTreeWalkersChild(Document refDoc, Node refroot, DOMDocument doc, AbstractDOMNode root,
			int whatToShow, NodeFilter filter) {
		org.w3c.dom.traversal.TreeWalker reftw = ((DocumentTraversal) refDoc).createTreeWalker(refroot, whatToShow,
				(org.w3c.dom.traversal.NodeFilter) filter, false);
		TreeWalker tw = doc.createTreeWalker(root, whatToShow, filter);
		DOMNode node;
		while ((node = tw.nextNode()) != null) {
			Node refnode = reftw.nextNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
			compareChild(reftw, tw);
			tw.setCurrentNode(node);
			reftw.setCurrentNode(refnode);
		}
		assertNull(reftw.nextNode());
		while ((node = tw.previousNode()) != null) {
			Node refnode = reftw.previousNode();
			assertNotNull(refnode);
			compareNodesBasic(refnode, node);
			compareChild(reftw, tw);
			tw.setCurrentNode(node);
			reftw.setCurrentNode(refnode);
		}
		assertNull(reftw.previousNode());
	}

	private void compareChild(org.w3c.dom.traversal.TreeWalker reftw, TreeWalker tw) {
		Node child, refchild;
		while ((child = tw.firstChild()) != null) {
			refchild = reftw.firstChild();
			assertNotNull(refchild);
			compareNodesBasic(refchild, child);
			compareSiblings(reftw, tw);
			tw.setCurrentNode(child);
			reftw.setCurrentNode(refchild);
		}
		assertNull(reftw.firstChild());
		while ((child = tw.parentNode()) != null) {
			refchild = reftw.parentNode();
			assertNotNull(refchild);
			compareNodesBasic(refchild, child);
			compareSiblings(reftw, tw);
			tw.setCurrentNode(child);
			reftw.setCurrentNode(refchild);
		}
		assertNull(reftw.parentNode());
		while ((child = tw.lastChild()) != null) {
			refchild = reftw.lastChild();
			assertNotNull(refchild);
			compareNodesBasic(refchild, child);
		}
		assertNull(reftw.lastChild());
		while ((child = tw.parentNode()) != null) {
			refchild = reftw.parentNode();
			assertNotNull(refchild);
			compareNodesBasic(refchild, child);
			compareSiblings(reftw, tw);
			tw.setCurrentNode(child);
			reftw.setCurrentNode(refchild);
		}
		assertNull(reftw.parentNode());
	}

	private DOMDocument readTestDocument(boolean ignoreElementContentWhitespace) throws SAXException, IOException {
		DOMDocument xmlDoc;
		builder.setIgnoreElementContentWhitespace(ignoreElementContentWhitespace);
		Reader re = XMLDocumentBuilderTest.loadEntitiesReader();
		InputSource is = new InputSource(re);
		try {
			xmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			throw e;
		} finally {
			re.close();
		}
		xmlDoc.setDocumentURI("http://www.example.com/xml/entities.xhtml");
		return xmlDoc;
	}

}
