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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.LinkStyle;
import org.w3c.dom.stylesheets.StyleSheet;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.StyleSheetList;

public class DOMDocumentTest {

	private static TestDOMImplementation domImpl;

	@BeforeClass
	public static void setUpBeforeClass() {
		domImpl = new TestDOMImplementation(false, null);
	}

	@Test
	public void testCreateDocument() {
		DOMDocument document = domImpl.createDocument(null, null, null);
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, document.getComplianceMode());
		DocumentType doctype = domImpl.createDocumentType("html", null, null);
		document = domImpl.createDocument(null, null, doctype);
		assertEquals(CSSDocument.ComplianceMode.STRICT, document.getComplianceMode());
		assertFalse(document.hasAttributes());
	}

	@Test
	public void testGetOwnerDocument() {
		DOMDocument document = domImpl.createDocument(null, null, null);
		assertNull(document.getOwnerDocument());
	}

	@Test
	public void testSetNodeValue() {
		DOMDocument document = domImpl.createDocument(null, null, null);
		document.setNodeValue("foo"); // No effect
	}

	@Test
	public void testText() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		Text c = document.createTextNode("A text node");
		assertEquals("A text node", c.getData());
		assertEquals("A text node", c.toString());
		Text d = c.splitText(7);
		assertEquals("A text ", c.getData());
		assertEquals("node", d.getData());
		try {
			c.splitText(100);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		try {
			c.splitText(-100);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		//
		DOMElement elm = document.createElement("p");
		c = document.createTextNode("A text node");
		elm.appendChild(c);
		assertNull(c.getNextSibling());
		d = c.splitText(7);
		assertEquals("A text ", c.getData());
		assertEquals("node", d.getData());
		assertTrue(elm == d.getParentNode());
		assertEquals(2, elm.getChildNodes().getLength());
		assertTrue(d == c.getNextSibling());
		//
		c = document.createTextNode("A text node<");
		assertEquals("A text node<", c.getData());
		assertEquals("A text node&lt;", c.toString());
		c.appendData("foo>");
		assertEquals("A text node<foo>", c.getData());
		assertEquals("A text node&lt;foo&gt;", c.toString());
		//
		c.deleteData(11, 20);
		assertEquals("A text node", c.getData());
		assertEquals("A text node", c.toString());
		//
		c.replaceData(0, 1, "My");
		assertEquals("My text node", c.getData());
		assertEquals("My text node", c.toString());
		//
		c.deleteData(0, 3);
		assertEquals("text node", c.getData());
		assertEquals("text node", c.toString());
		//
		try {
			document.createTextNode(null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			c.replaceWholeText(null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			c.deleteData(-1, 3);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		//
		try {
			c.deleteData(1, -3);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		//
		try {
			c.deleteData(20, 3);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		//
		c.insertData(0, "The ");
		assertEquals("The text node", c.getData());
		assertEquals("The text node", c.toString());
		//
		c.insertData(13, " is now larger");
		assertEquals("The text node is now larger", c.getData());
		//
		c.deleteData(9, 5);
		assertEquals("The text is now larger", c.getData());
		c.insertData(9, "node ");
		assertEquals("The text node is now larger", c.getData());
		c.deleteData(26, 200);
		assertEquals("The text node is now large", c.getData());
		//
		try {
			c.insertData(-1, "foo");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		//
		try {
			c.insertData(30, "foo");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		// Cloning
		Node clone = c.cloneNode(false);
		assertNotNull(clone);
		assertEquals(c.getNodeType(), clone.getNodeType());
		assertEquals(c.getNodeName(), clone.getNodeName());
		assertEquals(c.getNodeValue(), clone.getNodeValue());
	}

	@Test
	public void testCharacterData() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		CDATASection c = document.createCDATASection("A CDATA section");
		assertEquals("A CDATA section", c.getData());
		assertEquals("<![CDATA[A CDATA section]]>", c.toString());
		assertEquals(15, c.getLength());
		c = document.createCDATASection("A CDATA section<");
		assertEquals("A CDATA section<", c.getData());
		assertEquals("<![CDATA[A CDATA section<]]>", c.toString());
		// Cloning
		Node clone = c.cloneNode(false);
		assertNotNull(clone);
		assertEquals(c.getNodeType(), clone.getNodeType());
		assertEquals(c.getNodeName(), clone.getNodeName());
		assertEquals(c.getNodeValue(), clone.getNodeValue());
		//
		try {
			document.createCDATASection(null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			c.setData("]]>");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createCDATASection("]]>");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		//
		try {
			c.substringData(-1, 4);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		try {
			c.substringData(1, -1);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		try {
			c.substringData(67, 1);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		assertEquals("A ", c.substringData(0, 2));
		assertEquals("A CDATA section<", c.substringData(0, 200));
		try {
			c.replaceData(67, 1, "foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INDEX_SIZE_ERR, e.code);
		}
		c.replaceData(15, 1, "");
		c.replaceData(0, 1, "My");
		assertEquals("My CDATA section", c.getData());
		assertEquals("My CDATA section", c.getWholeText());
		try {
			c.appendChild(document.createComment(" hi "));
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testCloneNode() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		DOMDocument cloned = document.cloneNode(false);
		assertTrue(document.isEqualNode(cloned));
		assertTrue(document.getClass() == cloned.getClass());
		DocumentType docType = domImpl.createDocumentType("foo", null, "http://www.example.com/foo.dtd");
		document = domImpl.createDocument("http://www.example.com/examplens", "foo", docType);
		DOMElement docElm = document.getDocumentElement();
		docElm.setAttribute("id", "myId");
		docElm.setIdAttribute("id", true);
		assertTrue(document.isEqualNode(document.cloneNode(true)));
		cloned = document.cloneNode(false);
		assertNull(cloned.getDoctype());
		assertNull(cloned.getDocumentElement());
		assertTrue(document.getClass() == cloned.getClass());
		ProcessingInstruction pi = document.createProcessingInstruction("foo", "bar");
		document.prependChild(pi);
		Comment comment = document.createComment(" A comment ");
		document.prependChild(comment);
		document.appendChild(document.createComment(" End comment "));
		cloned = document.cloneNode(true);
		assertTrue(document.isEqualNode(cloned));
		assertTrue(document.getClass() == cloned.getClass());
	}

	@Test
	public void testCreateElementNS() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		DOMElement element = document.createElementNS(null, "element");
		element.setAttribute("eId", "myId");
		element.setIdAttribute("eId", true);
		assertEquals("myId", element.getAttribute("eId"));
		assertEquals("myId", element.getId());
		assertTrue(element.getAttributeNode("eId").isId());
		element.setAttribute("foo", "bar");
		assertEquals("bar", element.getAttribute("foo"));
		assertFalse(element.getAttributeNode("foo").isId());
		Attr attr = document.createAttribute("noattr");
		try {
			element.setIdAttributeNode(attr, true);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		document.appendChild(element);
		assertTrue(element == document.getDocumentElement());
		//
		assertEquals("<element eId=\"myId\" foo=\"bar\"></element>", element.toString());
	}

	@Test
	public void testCreateElementNSHighChar() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		DOMElement element = document.createElementNS(null, "\u208c");
		assertEquals("\u208c", element.getLocalName());
		assertEquals("\u208c", element.getTagName());
	}

	@Test
	public void testCreateElementNSSurrogate() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		DOMElement element = document.createElementNS(null, "\ud83c\udf52");
		assertEquals("\ud83c\udf52", element.getLocalName());
		assertEquals("\ud83c\udf52", element.getTagName());
	}

	@Test
	public void testCreateElementError() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		try {
			document.createElement("p'");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElement(null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElement("");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElement("-p");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElement(".p");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElement(":");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSError() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		try {
			document.createElementNS("http://www.example.com/examplens", "e:p'");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSError2() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		// Cause an error
		try {
			document.createElementNS(null, "foo:bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSError3() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		// Cause an error
		try {
			document.createElementNS("http://www.example.com/examplens", null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElementNS("http://www.example.com/examplens", "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElementNS("http://www.example.com/examplens", "foo:");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElementNS("http://www.example.com/examplens", ":foo");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSHighCharError() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		try {
			document.createElementNS(null, "\u26a1");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateElementNSInjectionError() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		// Cause an error
		try {
			document.createElementNS(null, "\"");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElementNS(null, "div><iframe");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createElementNS(null, "foo disableProtection");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testCreateComment() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		Comment comment = document.createComment("My comment");
		assertNotNull(comment);
		assertEquals("My comment", comment.getData());
		assertEquals("<!--My comment-->", comment.toString());
		//
		comment = document.createComment("<--");
		assertNotNull(comment);
		assertEquals("<--", comment.getData());
		assertEquals("<!--<---->", comment.toString());
		try {
			document.createComment("-->");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createComment(null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			comment.appendChild(document.createComment(" hi "));
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testCreateDocumentFragment() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", "foo", null);
		DocumentFragment df = document.createDocumentFragment();
		assertNotNull(df);
		df.isEqualNode(df.cloneNode(false));
		df.isEqualNode(df.cloneNode(true));
		Comment comment = document.createComment("My comment");
		df.appendChild(comment);
		DOMElement div = document.createElement("div");
		df.appendChild(div);
		df.appendChild(document.createComment(" another comment "));
		DOMElement p = document.createElement("p");
		p.setAttribute("lang", "en");
		p.setAttribute("class", "para");
		df.appendChild(p);
		DOMElement span = document.createElement("span");
		span.appendChild(document.createTextNode("foo"));
		p.appendChild(span);
		df.isEqualNode(df.cloneNode(true));
		//
		assertEquals(
				"<!--My comment--><div></div><!-- another comment --><p lang=\"en\" class=\"para\"><span>foo</span></p>",
				df.toString());
		//
		ElementList elist = ((ParentNode) df).getElementsByTagName("span");
		assertNotNull(elist);
		assertEquals(1, elist.getLength());
		assertEquals("span", elist.item(0).getTagName());
		//
		elist = ((ParentNode) df).getElementsByClassName("para");
		assertNotNull(elist);
		assertEquals(1, elist.getLength());
		assertTrue(p == elist.item(0));
		p.setAttribute("class", "foo");
		assertEquals(0, elist.getLength());
		// Append
		DOMElement docElm = document.getDocumentElement();
		docElm.appendChild(df);
		assertTrue(comment == docElm.getFirstChild());
		assertTrue(div == docElm.getFirstElementChild());
		assertTrue(p == docElm.getLastChild());
		DOMNodeList list = docElm.getChildNodes();
		assertEquals(4, list.getLength());
		assertNull(df.getFirstChild());
	}

	@Test
	public void testCreateAttribute() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		DOMElement docElm = document.createElementNS("http://www.example.com/examplens", "doc");
		document.appendChild(docElm);
		Attr attr = document.createAttribute("lang");
		assertNotNull(attr);
		attr.setValue("en");
		assertNull(attr.getAttributes());
		assertNull(attr.getNextSibling());
		assertNull(attr.getPreviousSibling());
		assertNull(attr.getFirstChild());
		assertNull(attr.getLastChild());
		assertNull(attr.getParentNode());
		assertEquals(0, attr.getChildNodes().getLength());
		assertTrue(attr.getSpecified());
		assertNull(attr.getNamespaceURI());
		assertEquals("lang", attr.getName());
		assertEquals("en", attr.getValue());
		assertEquals("lang", attr.getNodeName());
		assertEquals("en", attr.getNodeValue());
		assertEquals("lang=\"en\"", attr.toString());
		docElm.setAttributeNodeNS(attr);
		//
		try {
			document.createAttribute(null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttribute("");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			attr.insertBefore(attr, null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Attr attr2 = document.createAttribute("foo");
		attr2.setValue("bar");
		try {
			attr.insertBefore(attr2, null);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			attr.removeChild(attr2);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		try {
			attr.replaceChild(attr2, attr);
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		docElm.setAttributeNodeNS(attr2);
		Attr attr3 = document.createAttribute("id");
		attr3.setValue("myId");
		docElm.setAttributeNodeNS(attr3);
		assertNull(attr.getPreviousSibling());
		assertTrue(attr.getNextSibling() == attr2);
		assertTrue(attr == attr2.getPreviousSibling());
		assertTrue(attr2.getNextSibling() == attr3);
		assertTrue(attr2 == attr3.getPreviousSibling());
		assertNull(attr3.getNextSibling());
		assertNull(attr3.getParentNode());
		//
		attr = document.createAttribute("foo");
		attr.setValue("foo\u00a0bar&\"");
		assertEquals("foo=\"foo&nbsp;bar&amp;&quot;\"", attr.toString());
	}

	@Test
	public void testCreateAttributeNS() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		Attr attr = document.createAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns");
		assertNotNull(attr);
		attr.setValue(HTMLDocument.HTML_NAMESPACE_URI);
		assertNull(attr.getAttributes());
		assertEquals(0, attr.getChildNodes().getLength());
		assertNull(attr.getNextSibling());
		assertNull(attr.getPreviousSibling());
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
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		Attr attr = document.createAttributeNS("http://www.w3.org/2000/svg", "version");
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
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		try {
			document.createAttributeNS("http://www.example.com/examplens", "xmlns");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		Attr attr = document.createAttributeNS("http://www.example.com/examplens", "doc");
		try {
			attr.setPrefix("xmlns");
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
			attr.setPrefix("xml");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		// Verify that the right thing is possible
		attr = document.createAttributeNS(DOMDocument.XML_NAMESPACE_URI, "doc");
		attr.setPrefix("xml");
		// Other attempts to create invalid attributes
		try {
			document.createAttributeNS(null, "foo:bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			document.createAttributeNS("", "foo:bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			document.createAttributeNS("http://www.example.com/examplens", null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS("http://www.example.com/examplens", "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS("http://www.example.com/examplens", ":bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			document.createAttributeNS("http://www.example.com/examplens", "foo:");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
		try {
			document.createAttributeNS(null, null);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS(null, "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS("", "");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS(null, "'");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS(null, "\"");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS(null, "><iframe><a ");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createAttributeNS(null, "foo disableProtection");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testProcessingInstruction() {
		DOMDocument document = domImpl.createDocument(null, null, null);
		ProcessingInstruction pi = document.createProcessingInstruction("xml-foo",
				"pseudoattr=\"value\"");
		assertEquals("<?xml-foo pseudoattr=\"value\"?>", pi.toString());
		assertFalse(pi instanceof LinkStyle);
		assertNull(pi.getNextSibling());
		assertNull(pi.getPreviousSibling());
		assertNull(pi.getFirstChild());
		assertNull(pi.getLastChild());
		//
		try {
			pi.setData("?>");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createProcessingInstruction("xml-foo", "?>");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createProcessingInstruction("xml-foo ?>",
					"<DOCTYPE SYSTEM='http://www.example.com/malicious.dtd'>");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createProcessingInstruction(null, "bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			document.createProcessingInstruction("", "bar");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		Node clone = pi.cloneNode(true);
		assertTrue(pi.isEqualNode(clone));
	}

	@Test
	public void testStyleProcessingInstruction() {
		ProcessingInstruction pi = domImpl.createDocument(null, null, null)
				.createProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"style.css\"");
		assertEquals("<?xml-stylesheet type=\"text/css\" href=\"style.css\"?>", pi.toString());
		assertTrue(pi instanceof LinkStyle);
	}

	@Test (timeout=8000)
	public void testStyleProcessingInstructionEvil() {
		DOMDocument document = domImpl.createDocument("", null, null);
		ProcessingInstruction pi = document.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"http://www.example.com/css/common.css\"");
		assertEquals("<?xml-stylesheet type=\"text/css\" href=\"http://www.example.com/css/common.css\"?>",
				pi.toString());
		document.appendChild(pi);
		assertTrue(pi instanceof LinkStyle);
		CSSStyleSheet sheet = (CSSStyleSheet) ((LinkStyle) pi).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(3, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == pi);
		assertEquals("type=\"text/css\" href=\"http://www.example.com/css/common.css\"", pi.getData());
		assertFalse(document.getErrorHandler().hasErrors());
		assertFalse(document.getErrorHandler().hasPolicyErrors());
		//
		pi.setData("type=\"text/css\" href=\"file:/dev/zero\"");
		sheet = (CSSStyleSheet) ((LinkStyle) pi).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == pi);
		assertEquals("type=\"text/css\" href=\"file:/dev/zero\"", pi.getData());
		assertTrue(document.getErrorHandler().hasErrors());
		assertTrue(document.getErrorHandler().hasPolicyErrors());
		//
		document.getErrorHandler().reset();
		pi.setData("type=\"text/css\" href=\"jar:http://www.example.com/evil.jar!/file\"");
		sheet = (CSSStyleSheet) ((LinkStyle) pi).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == pi);
		assertEquals("type=\"text/css\" href=\"jar:http://www.example.com/evil.jar!/file\"", pi.getData());
		assertTrue(document.getErrorHandler().hasErrors());
		assertTrue(document.getErrorHandler().hasPolicyErrors());
		//
		document.getErrorHandler().reset();
		DOMElement root = document.createElement("html");
		document.appendChild(root);
		root.setAttributeNS(DOMDocument.XML_NAMESPACE_URI, "xml:base", "jar:http://www.example.com/evil.jar!/dir/file");
		sheet = (CSSStyleSheet) ((LinkStyle) pi).getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == pi);
		assertTrue(document.getErrorHandler().hasErrors());
		assertTrue(document.getErrorHandler().hasPolicyErrors());
		//
		StyleSheetList list = document.getStyleSheets();
		assertNotNull(list);
		assertEquals(1, list.getLength());
		AbstractCSSStyleSheet item = list.item(0);
		assertSame(sheet, item);
	}

	@Test
	public void testFontIOError() {
		DOMDocument document = domImpl.createDocument("", "foo", null);
		DOMElement element = document.getDocumentElement();
		element.setAttributeNS(DOMDocument.XML_NAMESPACE_URI, "xml:base", "http://www.example.com/");
		DOMElement style = document.createElement("style");
		style.setAttribute("id", "styleBadFont");
		style.setIdAttribute("id", true);
		style.setAttribute("type", "text/css");
		style.setTextContent("@font-face{font-family:'Mechanical Bold';src:url('font/MechanicalBd.otf');}");
		element.appendChild(style);
		ProcessingInstruction pi = document.createProcessingInstruction("xml-stylesheet",
				"type=\"text/css\" href=\"#styleBadFont\"");
		document.insertBefore(pi, element);
		//
		StyleSheet sheet = ((LinkStyle) pi).getSheet();
		StyleSheetList list = document.getStyleSheets();
		assertNotNull(list);
		assertEquals(1, list.getLength());
		AbstractCSSStyleSheet item = list.item(0);
		assertSame(sheet, item);
		//
		ErrorHandler errHandler = document.getErrorHandler();
		assertNotNull(errHandler);
		assertFalse(errHandler.hasErrors());
		element.getComputedStyle(null);
		//
		assertTrue(errHandler.hasIOErrors());
		assertTrue(errHandler.hasErrors());
	}

	@Test
	public void testStyleXSLProcessingInstruction() {
		ProcessingInstruction pi = domImpl.createDocument(null, null, null)
				.createProcessingInstruction("xml-stylesheet", "type=\"application/xsl\" href=\"sheet.xsl\"");
		assertEquals("<?xml-stylesheet type=\"application/xsl\" href=\"sheet.xsl\"?>", pi.toString());
		assertTrue(pi instanceof LinkStyle);
	}

	@Test
	public void getElementsByTagName() {
		DOMDocument document = domImpl.createDocument("", "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement elem1 = document.createElement("div");
		elem1.setAttribute("id", "div1");
		elem1.setIdAttribute("id", true);
		docElm.appendChild(elem1);
		DOMElement elem2 = document.createElement("div");
		elem2.setAttribute("id", "div2");
		elem2.setIdAttribute("id", true);
		elem1.appendChild(elem2);
		DOMElement elem3 = document.createElement("div");
		elem3.setAttribute("id", "div3");
		elem3.setIdAttribute("id", true);
		elem2.appendChild(elem3);
		DOMElement elem4 = document.createElement("div");
		elem4.setAttribute("id", "div4");
		elem4.setIdAttribute("id", true);
		docElm.appendChild(elem4);
		ElementList list = document.getElementsByTagName("div");
		assertNotNull(list);
		assertEquals(4, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem2, list.item(1));
		assertSame(elem3, list.item(2));
		assertSame(elem4, list.item(3));
		assertNull(list.item(4));
		//
		assertTrue(list.contains(elem1));
		assertTrue(list.contains(elem2));
		assertTrue(list.contains(elem3));
		assertTrue(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		Iterator<DOMElement> it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem2, it.next());
		assertSame(elem3, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem4.getElementsByTagName("div");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem4));
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void getElementsByTagNameNS() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement elem1 = document.createElementNS("http://www.example.com/examplens", "div");
		elem1.setAttribute("id", "div1");
		elem1.setIdAttribute("id", true);
		docElm.appendChild(elem1);
		DOMElement elem2 = document.createElementNS("http://www.example.com/examplens", "div");
		elem2.setAttribute("id", "div2");
		elem2.setIdAttribute("id", true);
		elem1.appendChild(elem2);
		DOMElement elem3 = document.createElementNS("http://www.example.com/examplens", "div");
		elem3.setAttribute("id", "div3");
		elem3.setIdAttribute("id", true);
		elem2.appendChild(elem3);
		DOMElement elem4 = document.createElementNS("http://www.example.com/examplens", "div");
		elem4.setAttribute("id", "div4");
		elem4.setIdAttribute("id", true);
		docElm.appendChild(elem4);
		ElementList list = document.getElementsByTagNameNS("http://www.example.com/examplens", "div");
		assertNotNull(list);
		assertEquals(4, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem2, list.item(1));
		assertSame(elem3, list.item(2));
		assertSame(elem4, list.item(3));
		assertNull(list.item(4));
		//
		Iterator<DOMElement> it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem2, it.next());
		assertSame(elem3, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void getElementsByTagNameNSAsterisk() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement elem1 = document.createElementNS("http://www.example.com/examplens", "div");
		elem1.setAttribute("id", "div1");
		elem1.setIdAttribute("id", true);
		docElm.appendChild(elem1);
		DOMElement elem2 = document.createElementNS("http://www.example.com/examplens", "div");
		elem2.setAttribute("id", "div2");
		elem2.setIdAttribute("id", true);
		elem1.appendChild(elem2);
		DOMElement elem3 = document.createElementNS("http://www.example.com/examplens", "div");
		elem3.setAttribute("id", "div3");
		elem3.setIdAttribute("id", true);
		elem2.appendChild(elem3);
		DOMElement elem4 = document.createElementNS("http://www.example.com/examplens", "div");
		elem4.setAttribute("id", "div4");
		elem4.setIdAttribute("id", true);
		docElm.appendChild(elem4);
		ElementList list = document.getElementsByTagNameNS("*", "div");
		assertNotNull(list);
		assertEquals(4, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem2, list.item(1));
		assertSame(elem3, list.item(2));
		assertSame(elem4, list.item(3));
		assertNull(list.item(4));
		//
		Iterator<DOMElement> it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem2, it.next());
		assertSame(elem3, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void getElementsByTagNameNSMixed() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement elem1 = document.createElementNS("http://www.example.com/differentns", "div");
		elem1.setAttribute("id", "div1");
		elem1.setIdAttribute("id", true);
		docElm.appendChild(elem1);
		DOMElement elem2 = document.createElementNS("http://www.example.com/differentns", "div");
		elem2.setAttribute("id", "div2");
		elem2.setIdAttribute("id", true);
		elem1.appendChild(elem2);
		DOMElement elem3 = document.createElementNS("http://www.example.com/examplens", "div");
		elem3.setAttribute("id", "div3");
		elem3.setIdAttribute("id", true);
		elem2.appendChild(elem3);
		DOMElement elem4 = document.createElementNS("http://www.example.com/differentns", "div");
		elem4.setAttribute("id", "div4");
		elem4.setIdAttribute("id", true);
		docElm.appendChild(elem4);
		//
		ElementList list = document.getElementsByTagNameNS("http://www.example.com/examplens", "div");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem3, list.item(0));
		assertNull(list.item(1));
		//
		Iterator<DOMElement> it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem3, it.next());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByTagNameNS("http://www.example.com/differentns", "div");
		assertNotNull(list);
		assertEquals(3, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem2, list.item(1));
		assertSame(elem4, list.item(2));
		assertNull(list.item(3));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem2, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem1.getElementsByTagNameNS("http://www.example.com/differentns", "div");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem2, list.item(0));
		assertNull(list.item(1));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem2, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem1.getElementsByTagNameNS("http://www.example.com/examplens", "div");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem3, list.item(0));
		assertNull(list.item(1));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem3, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem3.getElementsByTagNameNS("http://www.example.com/examplens", "div");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void getElementsByTagNameNSMixedAsterisk() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement elem1 = document.createElementNS("http://www.example.com/differentns", "div");
		elem1.setAttribute("id", "div1");
		elem1.setIdAttribute("id", true);
		docElm.appendChild(elem1);
		DOMElement elem2 = document.createElementNS("http://www.example.com/differentns", "div");
		elem2.setAttribute("id", "div2");
		elem2.setIdAttribute("id", true);
		elem1.appendChild(elem2);
		DOMElement elem3 = document.createElementNS("http://www.example.com/examplens", "div");
		elem3.setAttribute("id", "div3");
		elem3.setIdAttribute("id", true);
		elem2.appendChild(elem3);
		DOMElement elem4 = document.createElementNS("http://www.example.com/differentns", "div");
		elem4.setAttribute("id", "div4");
		elem4.setIdAttribute("id", true);
		docElm.appendChild(elem4);
		//
		ElementList list = document.getElementsByTagNameNS("*", "div");
		assertNotNull(list);
		assertEquals(4, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem2, list.item(1));
		assertSame(elem3, list.item(2));
		assertSame(elem4, list.item(3));
		assertNull(list.item(4));
		//
		Iterator<DOMElement> it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem2, it.next());
		assertSame(elem3, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem1.getElementsByTagNameNS("*", "div");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem2, list.item(0));
		assertSame(elem3, list.item(1));
		assertNull(list.item(2));
		assertEquals(elem2.getStartTag() + ',' + elem3.getStartTag(), list.toString());
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem2, it.next());
		assertSame(elem3, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem2.getElementsByTagNameNS("*", "div");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem3, list.item(0));
		assertNull(list.item(1));
		assertEquals(elem3.getStartTag(), list.toString());
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem3, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem3.getElementsByTagNameNS("*", "div");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		assertEquals("", list.toString());
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void getElementsByClassName() {
		DOMDocument document = domImpl.createDocument("", "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement elem1 = document.createElement("div");
		elem1.setAttribute("class", "foo bar");
		docElm.appendChild(elem1);
		DOMElement elem2 = document.createElement("p");
		elem2.setAttribute("class", "foo");
		elem1.appendChild(elem2);
		DOMElement elem3 = document.createElement("span");
		elem3.setAttribute("class", "foo");
		elem2.appendChild(elem3);
		DOMElement elem4 = document.createElement("section");
		elem4.setAttribute("class", "bar foo otherclass");
		docElm.appendChild(elem4);
		ElementList list = document.getElementsByClassName("foo");
		assertNotNull(list);
		assertEquals(4, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem2, list.item(1));
		assertSame(elem3, list.item(2));
		assertSame(elem4, list.item(3));
		assertNull(list.item(4));
		//
		assertTrue(list.contains(elem1));
		assertTrue(list.contains(elem2));
		assertTrue(list.contains(elem3));
		assertTrue(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		Iterator<DOMElement> it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem2, it.next());
		assertSame(elem3, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByClassName("bar");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem4, list.item(1));
		assertNull(list.item(2));
		//
		assertTrue(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertTrue(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByClassName("foo bar");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem4, list.item(1));
		assertNull(list.item(2));
		//
		assertTrue(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertTrue(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByClassName("bar foo");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem1, list.item(0));
		assertSame(elem4, list.item(1));
		assertNull(list.item(2));
		//
		assertTrue(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertTrue(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem1, it.next());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByClassName("otherclass");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem4, list.item(0));
		assertNull(list.item(1));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertTrue(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem4, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByClassName("noclass");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertFalse(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = document.getElementsByClassName("");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertFalse(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem2.getElementsByClassName("bar");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertFalse(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem3.getElementsByClassName("foo");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		assertNull(list.item(-1));
		assertNull(list.item(0));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem3));
		assertFalse(list.contains(elem4));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
		//
		list = elem2.getElementsByClassName("foo");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertNull(list.item(-1));
		assertSame(elem3, list.item(0));
		assertNull(list.item(1));
		//
		assertFalse(list.contains(elem1));
		assertFalse(list.contains(elem2));
		assertFalse(list.contains(elem4));
		assertTrue(list.contains(elem3));
		assertFalse(list.contains(docElm));
		//
		it = list.iterator();
		assertTrue(it.hasNext());
		assertSame(elem3, it.next());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail("Must throw exception.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testImportNode() {
		// Prepare the original document
		DOMDocument document = domImpl.createDocument(null, "doc", null);
		DOMElement docElm = document.getDocumentElement();
		DOMElement head = document.createElement("body");
		head.appendChild(document.createComment(" head comment "));
		docElm.appendChild(head);
		DOMElement body = document.createElement("body");
		Attr idAttr = document.createAttribute("id");
		idAttr.setValue("bodyId");
		body.setAttributeNode(idAttr);
		body.setIdAttributeNode(idAttr, true);
		assertTrue(idAttr.isId());
		body.setAttribute("lang", "en");
		Text text = document.createTextNode("text");
		body.appendChild(text);
		docElm.appendChild(body);
		//
		DOMDocument doc2 = domImpl.createDocument(null, null, null);
		Node docElm2 = doc2.importNode(docElm, true);
		assertFalse(docElm == docElm2);
		assertNull(docElm2.getParentNode());
		assertEquals(2, docElm2.getChildNodes().getLength());
		Node head2 = docElm2.getFirstChild();
		assertFalse(head == head2);
		Node body2 = docElm2.getLastChild();
		assertFalse(body == body2);
		assertTrue(doc2 == head2.getOwnerDocument());
		assertTrue(doc2 == body2.getOwnerDocument());
		assertTrue(docElm2 == head2.getParentNode());
		assertTrue(docElm2 == body2.getParentNode());
		Node text2 = body2.getFirstChild();
		assertTrue(doc2 == text2.getOwnerDocument());
		assertTrue(body2 == text2.getParentNode());
		Attr idAttr2 = (Attr) body2.getAttributes().getNamedItem("id");
		assertTrue(doc2 == idAttr2.getOwnerDocument());
		assertTrue(idAttr2.isId());
		assertEquals(2, body2.getAttributes().getLength());
		assertEquals(1, body2.getChildNodes().getLength());
		// DocumentFragment
		DocumentFragment df = document.createDocumentFragment();
		DOMElement div = document.createElement("div");
		idAttr = document.createAttribute("id");
		idAttr.setValue("divId");
		div.setAttributeNode(idAttr);
		div.setIdAttributeNode(idAttr, true);
		div.setAttribute("lang", "en");
		Text textbd = document.createTextNode("text below div");
		div.appendChild(textbd);
		div.appendChild(document.createComment(" Comment below div "));
		div.appendChild(document.createElement("span"));
		df.appendChild(div);
		text = document.createTextNode("text");
		df.appendChild(text);
		DOMElement p = document.createElement("p");
		df.appendChild(p);
		df.appendChild(document.createComment(" Comment "));
		Node df2 = doc2.importNode(df, true);
		assertFalse(df == df2);
		assertNull(df2.getParentNode());
		assertEquals(4, df2.getChildNodes().getLength());
		Node textbd2 = df2.getFirstChild();
		assertFalse(textbd == textbd2);
	}

	@Test
	public void testImportNodeForeign() throws ParserConfigurationException {
		// Prepare the original document
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = dbFac.newDocumentBuilder();
		DOMImplementation impl = docb.getDOMImplementation();
		org.w3c.dom.Document document = impl.createDocument(null, "doc", null);
		Element docElm = document.getDocumentElement();
		Element head = document.createElement("body");
		head.appendChild(document.createComment(" head comment "));
		docElm.appendChild(head);
		Element body = document.createElement("body");
		Attr idAttr = document.createAttribute("id");
		idAttr.setValue("bodyId");
		body.setAttributeNode(idAttr);
		body.setIdAttributeNode(idAttr, true);
		assertTrue(idAttr.isId());
		body.setAttribute("lang", "en");
		Text text = document.createTextNode("text");
		body.appendChild(text);
		docElm.appendChild(body);
		//
		DOMDocument doc2 = domImpl.createDocument(null, null, null);
		Node docElm2 = doc2.importNode(docElm, true);
		assertFalse(docElm == docElm2);
		assertNull(docElm2.getParentNode());
		assertEquals(2, docElm2.getChildNodes().getLength());
		Node head2 = docElm2.getFirstChild();
		assertFalse(head == head2);
		Node body2 = docElm2.getLastChild();
		assertFalse(body == body2);
		assertTrue(doc2 == head2.getOwnerDocument());
		assertTrue(doc2 == body2.getOwnerDocument());
		assertTrue(docElm2 == head2.getParentNode());
		assertTrue(docElm2 == body2.getParentNode());
		Node text2 = body2.getFirstChild();
		assertTrue(doc2 == text2.getOwnerDocument());
		assertTrue(body2 == text2.getParentNode());
		Attr idAttr2 = (Attr) body2.getAttributes().getNamedItem("id");
		assertTrue(doc2 == idAttr2.getOwnerDocument());
		assertTrue(idAttr2.isId());
		assertEquals(2, body2.getAttributes().getLength());
		assertEquals(1, body2.getChildNodes().getLength());
		// DocumentFragment
		DocumentFragment df = document.createDocumentFragment();
		Element div = document.createElement("div");
		idAttr = document.createAttribute("id");
		idAttr.setValue("divId");
		div.setAttributeNode(idAttr);
		div.setIdAttributeNode(idAttr, true);
		div.setAttribute("lang", "en");
		Text textbd = document.createTextNode("text below div");
		div.appendChild(textbd);
		div.appendChild(document.createComment(" Comment below div "));
		div.appendChild(document.createElement("span"));
		df.appendChild(div);
		text = document.createTextNode("text");
		df.appendChild(text);
		Element p = document.createElement("p");
		df.appendChild(p);
		df.appendChild(document.createComment(" Comment "));
		Node df2 = doc2.importNode(df, true);
		assertFalse(df == df2);
		assertNull(df2.getParentNode());
		assertEquals(4, df2.getChildNodes().getLength());
		Node textbd2 = df2.getFirstChild();
		assertFalse(textbd == textbd2);
	}

	@Test
	public void testAppendPrependChild() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		// Replace document element
		DOMElement docelm = document.createElementNS(null, "doc");
		DOMElement element = document.createElementNS(null, "element");
		document.appendChild(docelm);
		assertTrue(document == docelm.getParentNode());
		try {
			document.appendChild(element);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertNull(element.getParentNode());
		assertTrue(document == docelm.getParentNode());
		assertTrue(docelm == document.getDocumentElement());
		try {
			document.prependChild(element);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertNull(element.getParentNode());
		assertTrue(document == docelm.getParentNode());
		assertTrue(docelm == document.getDocumentElement());
		// Document type
		DocumentType dtd = domImpl.createDocumentType("doc", null, null);
		document.prependChild(dtd);
		DocumentType dtd2 = domImpl.createDocumentType("element", null, null);
		try {
			document.appendChild(dtd2);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertTrue(dtd == document.getDoctype());
		assertTrue(document == dtd.getParentNode());
		assertNull(dtd2.getParentNode());
		try {
			document.prependChild(dtd2);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertTrue(dtd == document.getDoctype());
		assertTrue(document == dtd.getParentNode());
		assertNull(dtd2.getParentNode());
	}

	@Test
	public void testInsertBefore() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		Comment comment = document.createComment(" Comment ");
		document.appendChild(comment);
		// Insert document element
		DOMElement docelm = document.createElementNS(null, "doc");
		DOMElement element = document.createElementNS(null, "element");
		try {
			document.insertBefore(element, docelm);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		document.insertBefore(docelm, comment);
		assertTrue(docelm == document.getDocumentElement());
		assertTrue(document == docelm.getParentNode());
		assertTrue(comment == docelm.getNextSibling());
		assertNull(docelm.getPreviousSibling());
		try {
			document.insertBefore(element, docelm);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		assertNull(element.getNextSibling());
		// Document types
		DocumentType dtd = domImpl.createDocumentType("doc", null, null);
		DocumentType dtd2 = domImpl.createDocumentType("element", null, null);
		try {
			document.insertBefore(dtd2, dtd);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		document.insertBefore(dtd, docelm);
		assertTrue(dtd == document.getDoctype());
		assertTrue(document == dtd.getParentNode());
		assertTrue(docelm == dtd.getNextSibling());
		assertNull(dtd.getPreviousSibling());
		assertTrue(dtd == docelm.getPreviousSibling());
		try {
			document.insertBefore(dtd2, dtd);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			document.insertBefore(element, dtd);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			document.insertBefore(dtd2, docelm);
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		//
		Comment comment1 = document.createComment(" First comment ");
		document.insertBefore(comment1, dtd);
		assertTrue(comment1 == dtd.getPreviousSibling());
		assertTrue(docelm == dtd.getNextSibling());
		assertNull(comment1.getPreviousSibling());
		assertTrue(dtd == comment1.getNextSibling());
		assertTrue(document == comment1.getParentNode());
		//
		DOMElement bar = document.createElementNS(null, "bar");
		docelm.appendChild(bar);
		DOMElement foo = document.createElementNS(null, "foo");
		assertTrue(foo == docelm.insertBefore(foo, bar));
		assertTrue(bar == foo.getNextElementSibling());
		assertTrue(foo == bar.getPreviousElementSibling());
	}

	@Test
	public void testReplaceChild() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", null, null);
		// Replace document element
		DOMElement docelm = document.createElementNS(null, "doc");
		DOMElement element = document.createElementNS(null, "element");
		try {
			document.replaceChild(element, docelm);
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		document.appendChild(docelm);
		assertTrue(docelm == document.getDocumentElement());
		document.replaceChild(element, docelm);
		assertTrue(element == document.getDocumentElement());
		assertNull(docelm.getParentNode());
		assertTrue(document == element.getParentNode());
		document.replaceChild(docelm, element);
		assertNull(element.getParentNode());
		assertTrue(document == docelm.getParentNode());
		assertTrue(docelm == document.getDocumentElement());
		// Document types
		DocumentType dtd = domImpl.createDocumentType("doc", null, null);
		DocumentType dtd2 = domImpl.createDocumentType("element", null, null);
		try {
			document.replaceChild(dtd2, dtd);
		} catch (DOMException e) {
			assertEquals(DOMException.NOT_FOUND_ERR, e.code);
		}
		document.appendChild(dtd);
		assertTrue(dtd == document.getDoctype());
		document.replaceChild(dtd2, dtd);
		assertTrue(dtd2 == document.getDoctype());
		assertTrue(document == dtd2.getParentNode());
		assertNull(dtd.getParentNode());
		document.replaceChild(dtd, dtd2);
		assertTrue(dtd == document.getDoctype());
		assertTrue(document == dtd.getParentNode());
		assertNull(dtd2.getParentNode());
		// Try to replace a different node type to add more than one DTD or document element
		try {
			document.replaceChild(element, dtd);
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		try {
			document.replaceChild(dtd2, docelm);
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testEscapeCloseTag() {
		String text = "";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "<";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "</";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "</script";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "</scrip";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "</scri";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "</foo>";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "bar</foo>";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "</script>";
		assertEquals("&lt;/script>", DOMDocument.escapeCloseTag("script", text));
		text = "hello</script>";
		assertEquals("hello&lt;/script>", DOMDocument.escapeCloseTag("script", text));
		text = "hello</script>bye";
		assertEquals("hello&lt;/script>bye", DOMDocument.escapeCloseTag("script", text));
		text = "hello</ script >bye";
		assertEquals("hello&lt;/ script >bye", DOMDocument.escapeCloseTag("script", text));
		text = "hello</\rscript\r>bye";
		assertEquals("hello&lt;/\rscript\r>bye", DOMDocument.escapeCloseTag("script", text));
		text = "hello</\nscript\n>bye";
		assertEquals("hello&lt;/\nscript\n>bye", DOMDocument.escapeCloseTag("script", text));
		text = "hello</\r\nscript\r\n>bye";
		assertEquals("hello&lt;/\r\nscript\r\n>bye", DOMDocument.escapeCloseTag("script", text));
		text = "hello</    script    >bye";
		assertEquals("hello&lt;/    script    >bye", DOMDocument.escapeCloseTag("script", text));
		text = "hello</    script    bye";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "hello</scrip>bye";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "hello<script>bye";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
		text = "hello</script bye";
		assertEquals(text, DOMDocument.escapeCloseTag("script", text));
	}

	@Test
	public void testBaseAttribute() {
		DOMDocument document = domImpl.createDocument("", "foo", null);
		DOMElement element = document.getDocumentElement();
		element.setAttributeNS(DOMDocument.XML_NAMESPACE_URI, "xml:base", "http://www.example.com/");
		assertEquals("http://www.example.com/", element.getAttribute("xml:base"));
		assertEquals("http://www.example.com/", document.getBaseURI());
		Attr attr = element.getAttributeNode("xml:base");
		assertNotNull(attr);
		attr.setValue("jar:http://www.example.com/evil.jar!/file");
		assertNull(document.getBaseURI());
		assertTrue(document.getErrorHandler().hasErrors());
		assertTrue(document.getErrorHandler().hasPolicyErrors());
		//
		document.getErrorHandler().reset();
		document.setDocumentURI("http://www.example.com/foo.html");
		assertEquals("http://www.example.com/foo.html", document.getBaseURI());
		assertEquals("jar:http://www.example.com/evil.jar!/file", attr.getValue());
		assertTrue(document.getErrorHandler().hasErrors());
		assertTrue(document.getErrorHandler().hasPolicyErrors());
		//
		document.getErrorHandler().reset();
		attr.setValue("file:/dev/zero");
		assertEquals("http://www.example.com/foo.html", document.getBaseURI());
		assertTrue(document.getErrorHandler().hasErrors());
		assertTrue(document.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testLookupNamespaceURI() {
		DOMDocument document = domImpl.createDocument("http://www.example.com/examplens", "x:doc", null);
		DOMElement docelm = document.getDocumentElement();
		assertEquals("<x:doc xmlns:x=\"http://www.example.com/examplens\"></x:doc>", docelm.toString());
		assertEquals("http://www.example.com/examplens", docelm.lookupNamespaceURI("x"));
		assertNull(docelm.lookupNamespaceURI("z"));
		assertEquals("http://www.example.com/examplens", document.lookupNamespaceURI("x"));
		assertNull(document.lookupNamespaceURI("z"));
		//
		document = domImpl.createDocument("", null, null);
		assertNull(document.lookupNamespaceURI("x"));
	}

}
