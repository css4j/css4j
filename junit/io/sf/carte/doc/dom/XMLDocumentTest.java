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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.dom.DOMDocument.LinkStyleDefiner;
import io.sf.carte.doc.dom.DOMDocument.LinkStyleProcessingInstruction;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSDeclarationRule;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.FontFeatureValuesRule;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class XMLDocumentTest {
	private static XMLDocumentBuilder builder;
	private DOMDocument xmlDoc;

	@BeforeClass
	public static void setUpBeforeClass() {
		TestDOMImplementation impl = new TestDOMImplementation(false, null);
		impl.setXmlOnly(true);
		builder = new XMLDocumentBuilder(impl);
		builder.setIgnoreElementContentWhitespace(true);
		builder.setEntityResolver(new DefaultEntityResolver());
	}

	@Before
	public void setUp() throws SAXException, IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.sampleXMLReader();
		InputSource is = new InputSource(re);
		try {
			xmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			throw e;
		} finally {
			re.close();
		}
		xmlDoc.setDocumentURI("http://www.example.com/xml/xmlsample.xml");
	}

	@Test
	public void getDocumentElement() {
		assertFalse(xmlDoc instanceof HTMLDocument);
		CSSElement elm = xmlDoc.getDocumentElement();
		assertNotNull(elm);
		assertEquals("html", elm.getTagName());
	}

	@Test
	public void getNamespaceURI() {
		assertNull(xmlDoc.getNamespaceURI());
		Text text = xmlDoc.createTextNode("foo");
		assertNotNull(text);
		assertNull(text.getNamespaceURI());
		CDATASection cdata = xmlDoc.createCDATASection("foo");
		assertNotNull(cdata);
		assertNull(cdata.getNamespaceURI());
		Comment comment = xmlDoc.createComment("foo");
		assertNotNull(comment);
		assertNull(comment.getNamespaceURI());
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-foo", "bar");
		assertNotNull(pi);
		assertNull(pi.getNamespaceURI());
		EntityReference amp = xmlDoc.createEntityReference("amp");
		assertNotNull(amp);
		assertNull(amp.getNamespaceURI());
	}

	@Test
	public void appendChild() throws DOMException {
		Text text = xmlDoc.createTextNode("text");
		CSSElement p = xmlDoc.createElement("p");
		try {
			text.appendChild(p);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		Attr foo = xmlDoc.createAttribute("foo");
		try {
			text.appendChild(foo);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"sheet.xsl\"");
		try {
			text.appendChild(pi);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void appendChild2() throws DOMException {
		DOMDocument document = new TestDOMImplementation(false, null).createDocument(null, null, null);
		document.appendChild(document.getImplementation().createDocumentType("foo", null, null));
		try {
			document.appendChild(document.getImplementation().createDocumentType("foo", null, null));
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code);
		}
	}

	@Test
	public void testContains() {
		DOMElement docelm = xmlDoc.getDocumentElement();
		assertTrue(xmlDoc.contains(xmlDoc));
		assertTrue(xmlDoc.contains(docelm));
		assertTrue(docelm.contains(docelm));
		assertFalse(docelm.contains(xmlDoc));
		DOMElement h1 = xmlDoc.getElementById("h1");
		DOMElement span1 = xmlDoc.getElementById("span1");
		assertTrue(xmlDoc.contains(h1));
		assertTrue(xmlDoc.contains(span1));
		assertTrue(docelm.contains(h1));
		assertTrue(docelm.contains(span1));
		assertFalse(h1.contains(docelm));
		assertFalse(span1.contains(docelm));
		assertFalse(h1.contains(xmlDoc));
		assertFalse(span1.contains(xmlDoc));
		assertFalse(h1.contains(span1));
		assertFalse(span1.contains(h1));
	}

	@Test
	public void testEntities1() {
		CSSElement elm = xmlDoc.getElementById("entity");
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
		CSSElement elm = xmlDoc.getElementById("entiacute");
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
	public void testAttributeEntities() {
		CSSElement p = xmlDoc.createElement("p");
		Attr attr = xmlDoc.createAttribute("id");
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
		try {
			p.setAttribute("foo=", "bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		p.setAttribute("foo", "bar=");
		assertEquals("bar=", p.getAttribute("foo"));
		attr = p.getAttributeNode("foo");
		assertEquals("foo=\"bar=\"", attr.toString());
		//
		try {
			p.setAttribute("foo:", "bar");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void testElementNamespaceError() {
		try {
			xmlDoc.createElement("p:");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void testComment() {
		Comment c = xmlDoc.createComment(" A comment ");
		assertEquals(" A comment ", c.getData());
		assertEquals("<!-- A comment -->", c.toString());
	}

	@Test
	public void testBadComment() {
		try {
			xmlDoc.createComment("Bad-->comment");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testProcessingInstruction() {
		ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"style.xsl\"");
		assertEquals("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>", pi.toString());
	}

	@Test
	public void testBadProcessingInstruction() {
		try {
			xmlDoc.createProcessingInstruction("xml", "encoding=UTF-8");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xmlDoc.createProcessingInstruction("foo:xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
		try {
			xmlDoc.createProcessingInstruction("foo:xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"?>");
			fail("Must throw an exception");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
		}
	}

	@Test
	public void testText() {
		Text c = xmlDoc.createTextNode("A text node");
		assertEquals("A text node", c.getData());
		assertEquals("A text node", c.toString());
		Text d = c.splitText(7);
		assertEquals("A text ", c.getData());
		assertEquals("node", d.getData());
		//
		DOMElement elm = xmlDoc.createElement("p");
		c = xmlDoc.createTextNode("A text node");
		elm.appendChild(c);
		assertNull(c.getNextSibling());
		d = c.splitText(7);
		assertEquals("A text ", c.getData());
		assertEquals("node", d.getData());
		assertTrue(elm == d.getParentNode());
		assertEquals(2, elm.getChildNodes().getLength());
		assertTrue(d == c.getNextSibling());
		//
		c = xmlDoc.createTextNode("A text node<");
		assertEquals("A text node<", c.getData());
		assertEquals("A text node&lt;", c.toString());
	}

	@Test
	public void testCharacterData() {
		CDATASection c = xmlDoc.createCDATASection("A CDATA section");
		assertEquals("A CDATA section", c.getData());
		assertEquals("<![CDATA[A CDATA section]]>", c.toString());
		c = xmlDoc.createCDATASection("A CDATA section<");
		assertEquals("A CDATA section<", c.getData());
		assertEquals("<![CDATA[A CDATA section<]]>", c.toString());
	}

	@Test
	public void testCloneNode() {
		testCloneNode(xmlDoc.getFirstChild());
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
		NodeList list = xmlDoc.getChildNodes();
		assertNotNull(list);
		assertEquals(8, list.getLength());
	}

	@Test
	public void getElementById() {
		CSSElement elm = xmlDoc.getElementById("ul1");
		assertNotNull(elm);
		assertEquals("ul", elm.getTagName());
		assertNull(xmlDoc.getElementById("xxxxxx"));
	}

	@Test
	public void getElementsByTagName() {
		NodeList stylelist = xmlDoc.getElementsByTagName("style");
		assertNotNull(stylelist);
		assertEquals(1, stylelist.getLength());
		assertEquals("style", stylelist.item(0).getNodeName());
		NodeList lilist = xmlDoc.getElementsByTagName("li");
		assertNotNull(lilist);
		assertEquals(6, lilist.getLength());
		Node ul = lilist.item(0).getParentNode();
		assertEquals("li", lilist.item(0).getNodeName());
		ul.appendChild(xmlDoc.createElement("li"));
		assertEquals(7, lilist.getLength());
		DOMElement div = xmlDoc.createElement("div");
		DOMElement li = xmlDoc.createElement("li");
		div.appendChild(li);
		ul.appendChild(div);
		assertEquals(8, lilist.getLength());
		ul.removeChild(div);
		assertEquals(7, lilist.getLength());
		NodeList list = xmlDoc.getElementsByTagName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		DOMElement html = xmlDoc.getDocumentElement();
		list = xmlDoc.getElementsByTagName("div");
		assertEquals(1, list.getLength());
		DOMElement div2 = xmlDoc.createElement("div");
		html.appendChild(div2);
		assertEquals(2, list.getLength());
		DOMDocument otherdoc = xmlDoc.getImplementation().createDocument(null, null, null);
		try {
			html.insertBefore(div2, li);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.insertBefore(otherdoc, div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		try {
			html.insertBefore(otherdoc.createTextNode("foo"), div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		try {
			div2.appendChild(div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		try {
			div2.appendChild(otherdoc);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		try {
			otherdoc.appendChild(div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		try {
			div2.appendChild(html);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.replaceChild(div2, li);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.replaceChild(div2, li);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		assertEquals(7, lilist.getLength());
		try {
			html.replaceChild(otherdoc, div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		try {
			html.replaceChild(otherdoc.createTextNode("foo"), div2);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		assertEquals(2, list.getLength());
		//
		assertTrue(stylelist == xmlDoc.getElementsByTagName("style"));
		//
		list = xmlDoc.getElementsByTagName("html");
		assertEquals(1, list.getLength());
		assertTrue(xmlDoc.getDocumentElement() == list.item(0));
	}

	@Test
	public void getElementsByClassName() {
		ElementList tablelist = xmlDoc.getElementsByClassName("tableclass");
		assertNotNull(tablelist);
		assertEquals(1, tablelist.getLength());
		CSSElement elem = tablelist.item(0);
		assertEquals("table", elem.getNodeName());
		ElementList list = ((DOMElement) elem.getElementsByTagName("tr").item(0)).getElementsByClassName("tablecclass");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByClassName("liclass");
		assertNotNull(list);
		assertEquals(6, list.getLength());
		assertEquals("li", list.item(0).getNodeName());
		DOMElement li = xmlDoc.createElement("li");
		li.setAttribute("class", "liclass");
		Node ul = list.item(0).getParentNode();
		ul.appendChild(li);
		assertEquals(7, list.getLength());
		DOMElement div = xmlDoc.createElement("div");
		DOMElement li2 = xmlDoc.createElement("li");
		li2.setAttribute("class", "liclass");
		div.appendChild(li2);
		ul.appendChild(div);
		assertEquals(8, list.getLength());
		ul.removeChild(div);
		assertEquals(7, list.getLength());
		//
		list = xmlDoc.getElementsByClassName("xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
		list = xmlDoc.getElementsByClassName("smallitalic");
		assertEquals(1, list.getLength());
		div = xmlDoc.createElement("div");
		list.item(0).appendChild(div);
		assertEquals(1, list.getLength());
		div.setAttribute("class", "smallitalic");
		assertEquals("smallitalic", div.getAttribute("class"));
		assertEquals(2, list.getLength());
		ElementList tablelist2 = xmlDoc.getElementsByClassName("tableclass");
		assertTrue(tablelist == tablelist2);
	}

	@Test
	public void getElementsByTagNameNS() {
		ElementList list = xmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "*");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		DOMElement svg = list.item(0);
		assertEquals("s:svg", svg.getNodeName());
		Attr version = svg.getAttributeNodeNS("http://www.w3.org/2000/svg", "version");
		assertEquals("http://www.w3.org/2000/svg", version.getNamespaceURI());
		assertEquals("s", svg.getPrefix());
		assertEquals("s:rect", list.item(1).getNodeName());
		list.item(0).appendChild(xmlDoc.createElementNS("http://www.w3.org/2000/svg", "circle"));
		assertEquals(3, list.getLength());
		ElementList svglist = xmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "svg");
		assertNotNull(svglist);
		assertEquals(1, svglist.getLength());
		assertEquals("s:svg", svglist.item(0).getNodeName());
		list = xmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "rect");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		Node oldrect = list.item(0);
		assertEquals("s:rect", oldrect.getNodeName());
		DOMElement newrect = xmlDoc.createElementNS("http://www.w3.org/2000/svg", "rect");
		oldrect.getParentNode().appendChild(newrect);
		assertEquals(Node.DOCUMENT_POSITION_PRECEDING, oldrect.compareDocumentPosition(newrect));
		assertEquals(2, list.getLength());
		Node node = svglist.item(0);
		assertEquals("s:svg", node.getNodeName());
		node.getParentNode().removeChild(node);
		assertEquals(0, svglist.getLength());
		list = xmlDoc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "xxxxxx");
		assertNotNull(list);
		assertEquals(0, list.getLength());
	}

	@Test
	public void testQuerySelectorAll() {
		CSSElement elm = xmlDoc.getElementById("ul1");
		ElementList qlist = xmlDoc.querySelectorAll("#ul1");
		assertEquals(1, qlist.getLength());
		assertTrue(elm == qlist.item(0));
		qlist = xmlDoc.querySelectorAll("#xxxxxx");
		assertEquals(0, qlist.getLength());
	}

	@Test
	public void testQuerySelectorAll2() {
		ElementList list = xmlDoc.getElementsByTagName("p");
		ElementList qlist = xmlDoc.querySelectorAll("p");
		int sz = list.getLength();
		assertEquals(sz, qlist.getLength());
		for (int i = 0; i < sz; i++) {
			assertTrue(qlist.contains(list.item(i)));
		}
	}

	@Test
	public void testQuerySelectorAllNS() {
		// From the spec:
		// 'Support for namespaces within selectors is not planned and will not be added'
		try {
			xmlDoc.querySelectorAll("svg|*");
			fail("Must throw exception");
		} catch (DOMException e) {
			assertEquals(DOMException.NAMESPACE_ERR, e.code);
		}
	}

	@Test
	public void getTextContent() {
		CSSElement elm = xmlDoc.getElementsByTagName("style").item(0);
		assertNotNull(elm);
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1106, text.trim().length());
		//
		xmlDoc.normalizeDocument();
		text = elm.getTextContent();
		assertNotNull(text);
		assertEquals(1106, text.trim().length());
	}

	@Test
	public void getTextContent2() {
		DOMElement elm = xmlDoc.getElementById("para1");
		assertNotNull(elm);
		elm.appendChild(xmlDoc.createComment(" comment "));
		String text = elm.getTextContent();
		assertNotNull(text);
		assertEquals("Paragraph <>", text);
	}

	@Test
	public void TextIsElementContentWhitespace() {
		Text text = xmlDoc.createTextNode("foo ");
		assertNotNull(text);
		assertNotNull(text.getData());
		assertFalse(text.isElementContentWhitespace());
		text = xmlDoc.createTextNode("\n \t\r");
		assertNotNull(text);
		assertNotNull(text.getData());
		assertTrue(text.isElementContentWhitespace());
	}

	@Test
	public void TextGetWholeText() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("One"));
		Text text = xmlDoc.createTextNode("Two");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("Three"));
		p.appendChild(xmlDoc.createTextNode(" Four"));
		assertEquals("OneTwoThree Four", text.getWholeText());
	}

	@Test
	public void TextGetWholeTextWithER1() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p1 "));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xmlDoc.createTextNode(" p3");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode(" p4"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("p1 &amp; p3 p4", text.getWholeText());
	}

	@Test
	public void TextGetWholeTextWithER2() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p1 "));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		p.appendChild(xmlDoc.createElement("span"));
		Text text = xmlDoc.createTextNode(" p3");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode(" p4"));
		assertEquals(" p3 p4", text.getWholeText());
	}

	@Test
	public void TextReplaceWholeText() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("One"));
		Text text = xmlDoc.createTextNode("Two");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("Three"));
		p.appendChild(xmlDoc.createTextNode(" Four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("foo", text.replaceWholeText("foo").getData());
		assertEquals(1, p.getChildNodes().getLength());
		assertNull(text.replaceWholeText(""));
		assertFalse(p.hasChildNodes());
	}

	@Test
	public void TextReplaceWholeTextWithER1() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p one"));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xmlDoc.createTextNode("p three");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("p four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertEquals("foo", text.replaceWholeText("foo").getData());
		assertEquals(1, p.getChildNodes().getLength());
	}

	@Test
	public void TextReplaceWholeTextWithER2() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p one"));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		Text text = xmlDoc.createTextNode("p three");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("p four"));
		assertEquals(4, p.getChildNodes().getLength());
		assertNull(text.replaceWholeText(""));
		assertFalse(p.hasChildNodes());
	}

	@Test
	public void TextReplaceWholeTextWithER3() {
		CSSElement p = xmlDoc.createElement("p");
		p.appendChild(xmlDoc.createTextNode("p one"));
		EntityReference amp = xmlDoc.createEntityReference("amp");
		p.appendChild(amp);
		p.appendChild(xmlDoc.createElement("span"));
		Text text = xmlDoc.createTextNode("p four");
		p.appendChild(text);
		p.appendChild(xmlDoc.createTextNode("p five"));
		assertEquals(5, p.getChildNodes().getLength());
		text.replaceWholeText("foo");
		assertEquals(4, p.getChildNodes().getLength());
	}

	@Test
	public void getStyleSheet() {
		DocumentCSSStyleSheet defsheet = xmlDoc.getStyleSheetFactory()
				.getDefaultStyleSheet(xmlDoc.getComplianceMode());
		assertNotNull(defsheet);
		// Obtain the number of rules in the default style sheet, to use it
		// as a baseline.
		int defSz = defsheet.getCssRules().getLength();
		DocumentCSSStyleSheet css = xmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xmlDoc.embeddedStyle.size() + xmlDoc.linkedStyle.size();
		assertEquals(6, countInternalSheets);
		assertEquals(6, xmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css", xmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xmlDoc.getStyleSheetSets().getLength());
		Iterator<LinkStyleDefiner> it = xmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertFalse(sheet.getErrorHandler().hasSacErrors());
		assertEquals("background-color: red; ", ((CSSStyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		AbstractCSSStyleDeclaration fontface = ((BaseCSSDeclarationRule) sheet.getCssRules().item(1)).getStyle();
		assertEquals("url('http://www.example.com/css/font/MechanicalBd.otf')", fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ffval.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) ffval).getPrimitiveType());
		assertTrue(((FontFeatureValuesRule) sheet.getCssRules().item(2)).getMinifiedCssText()
				.startsWith("@font-feature-values Foo Sans,Bar"));
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertEquals(defSz + 20, css.getCssRules().getLength());
		assertFalse(xmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.enableStyleSheetsForSet("Alter 1");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.enableStyleSheetsForSet("Alter 1");
		assertNull(xmlDoc.getSelectedStyleSheetSet());
		xmlDoc.setSelectedStyleSheetSet("Default");
		assertEquals("Default", xmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void getComputedStyle() {
		CSSElement elm = xmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals("#808000", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		assertEquals(15, styledecl.getLength());
	}

	@Test
	public void getOverrideStyle() {
		CSSElement elm = xmlDoc.getElementById("tablerow1");
		assertNotNull(elm);
		CSSComputedProperties style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("5pt", style.getPropertyValue("margin-top"));
		assertEquals("margin-top: 5pt; margin-right: 5pt; margin-bottom: 5pt; margin-left: 5pt; ", style.getCssText());
		elm.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", elm.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", elm.getOverrideStyle(null).getCssText());
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals("margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals("margin:16pt;color:#f00;", style.getMinifiedCssText());
	}

	@Test
	public void testEmbeddedStyle() {
		LinkStyleDefiner link = (LinkStyleDefiner) xmlDoc.getEmbeddedStyleNodeList().item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		link.setNodeValue("href=\"#style1\" media=\"screen\"");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertFalse(sheet2.equals(sheet));
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertTrue(sheet2.getCssRules().getLength() > 0);
		// Change content of the container element
		LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) link;
		String href = pi.getPseudoAttribute("href");
		assertTrue(href.length() > 1);
		assertEquals('#', href.charAt(0));
		DOMElement container = xmlDoc.getElementById(href.substring(1));
		assertNotNull(container);
		container.setTextContent("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = link.getSheet();
		assertNotNull(sheet);
		assertFalse(sheet2 == sheet);
		assertEquals(2, sheet.getCssRules().getLength());
	}

	@Test
	public void testEmbeddedStyle2() {
		LinkStyleDefiner link = (LinkStyleDefiner) xmlDoc.getEmbeddedStyleNodeList().item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		// Change text child of the container element
		LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) link;
		String href = pi.getPseudoAttribute("href");
		assertTrue(href.length() > 1);
		assertEquals('#', href.charAt(0));
		DOMElement container = xmlDoc.getElementById(href.substring(1));
		assertNotNull(container);
		Node text = null;
		NodeList list = container.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.TEXT_NODE && node.getNodeValue().trim().length() > 2) {
				text = node;
				break;
			}
		}
		assertNotNull(text);
		text.setNodeValue("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertFalse(sheet2 == sheet);
		assertEquals(2, sheet2.getCssRules().getLength());
	}

	@Test
	public void testLinkElement() {
		LinkStyleDefiner link = (LinkStyleDefiner) xmlDoc.getLinkedStyleNodeList().item(0);
		CSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() != 0);
		link.setNodeValue("href=\"/css/common.css\" media=\"screen\"");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertFalse(sheet2.equals(sheet));
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertEquals(sheet.getCssRules().getLength(), sheet2.getCssRules().getLength());
		link.setNodeValue("href=\"http://www.example.com/css/alter1.css\" media=\"screen\"");
		sheet = link.getSheet();
		assertFalse(sheet2.equals(sheet));
		assertFalse(sheet.getCssRules().item(sheet.getCssRules().getLength() - 1).equals(
				sheet2.getCssRules().item(sheet2.getCssRules().getLength() - 1)));
	}

	@Test
	public void testBaseAttribute() {
		assertEquals("http://www.example.com/xml/xmlsample.xml", xmlDoc.getDocumentURI());
		assertEquals("http://www.example.com/", xmlDoc.getBaseURI());
		Attr base = xmlDoc.getDocumentElement().getAttributeNode("xml:base");
		base.setValue("http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xmlDoc.getBaseURI());
		// Changing an unrelated href attribute does nothing to base uri.
		CSSElement anchor = xmlDoc.getElementsByTagName("a").item(0);
		anchor.setAttribute("href", "http://www.example.com/foo/");
		assertEquals("http://www.example.com/foo/", anchor.getAttribute("href"));
		assertEquals("http://www.example.com/newbase/", xmlDoc.getBaseURI());
	}

	@Test
	public void testCascade() throws IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleUserCSSReader();
		xmlDoc.getStyleSheetFactory().setUserStyleSheet(re);
		re.close();
		CSSElement elm = xmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		elm.getOverrideStyle(null).setCssText("color: darkmagenta ! important;");
		style = xmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));
	}
}
