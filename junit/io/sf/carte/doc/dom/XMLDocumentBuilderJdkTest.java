/*

 Copyright (c) 2005-2022, Carlos Amengual.

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.agent.TestEntityResolver;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class XMLDocumentBuilderJdkTest {
	private DOMImplementation domImpl;
	private XMLDocumentBuilder builder;

	@Before
	public void setUp() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		domImpl = registry.getDOMImplementation("XML 3.0");
		builder = new XMLDocumentBuilder(domImpl);
		builder.setIgnoreElementContentWhitespace(true);
		builder.setEntityResolver(new DefaultEntityResolver());
	}

	@Test(timeout=1000)
	public void testParseInputSourceXHTML() throws SAXException, IOException {
		Document document = parseDocument("entities.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities.xhtml", document.getBaseURI());
		Node node = document.getFirstChild();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A first comment before the DOCTYPE ", ((Comment) node).getData());
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		node = docType.getPreviousSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" Last comment before the DOCTYPE ", ((Comment) node).getData());
		assertEquals("html", docType.getName());
		node = docType.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the DOCTYPE ", ((Comment) node).getData());
		node = node.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		ProcessingInstruction pi = (ProcessingInstruction) node;
		assertEquals("xml-stylesheet", pi.getTarget());
		assertEquals("type=\"text/css\" href=\"style.css\"", pi.getData());
		node = document.getDocumentElement().getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Not specified attributes are not present
		Element style = (Element) document.getElementsByTagName("style").item(0);
		assertFalse(style.hasAttributeNS("http://www.w3.org/XML/1998/namespace", "space"));
		assertFalse(style.hasAttribute("xml:space"));
		// Entities etc.
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with \u221e", element.getTextContent());
		assertNotNull(document.getElementById("doesnotexist"));
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	@Test(timeout=1000)
	public void testParseInputSourceXMLNotSpecifiedAttributes() throws SAXException, IOException {
		builder.setIgnoreNotSpecifiedAttributes(false);
		Document document = parseDocument("entities.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities.xhtml", document.getBaseURI());
		Node node = document.getFirstChild();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A first comment before the DOCTYPE ", ((Comment) node).getData());
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		node = docType.getPreviousSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" Last comment before the DOCTYPE ", ((Comment) node).getData());
		assertEquals("html", docType.getName());
		node = docType.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the DOCTYPE ", ((Comment) node).getData());
		node = node.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		ProcessingInstruction pi = (ProcessingInstruction) node;
		assertEquals("xml-stylesheet", pi.getTarget());
		assertEquals("type=\"text/css\" href=\"style.css\"", pi.getData());
		node = document.getDocumentElement().getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Not specified attributes are present
		Element style = (Element) document.getElementsByTagName("style").item(0);
		assertTrue(style.hasAttributeNS("http://www.w3.org/XML/1998/namespace", "space"));
		assertTrue(style.hasAttribute("xml:space"));
		assertEquals("preserve", style.getAttribute("xml:space"));
		assertEquals("preserve", style.getAttributeNS("http://www.w3.org/XML/1998/namespace", "space"));
		Attr attr = style.getAttributeNode("xml:space");
		assertNotNull(attr);
		// Entities etc.
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with \u221e", element.getTextContent());
		assertNull(document.getElementById("not-here"));
		assertNotNull(document.getElementById("doesnotexist"));
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	@Test
	public void testParseInputSourceXHTMLOnlySystem() throws SAXException, IOException {
		Document document = parseDocument("entities-systemdtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml", document.getBaseURI());
		Node node = document.getFirstChild();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A first comment before the DOCTYPE ", ((Comment) node).getData());
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		node = docType.getPreviousSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" Last comment before the DOCTYPE ", ((Comment) node).getData());
		assertEquals("html", docType.getName());
		node = docType.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the DOCTYPE ", ((Comment) node).getData());
		node = node.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		ProcessingInstruction pi = (ProcessingInstruction) node;
		assertEquals("xml-stylesheet", pi.getTarget());
		assertEquals("type=\"text/css\" href=\"style.css\"", pi.getData());
		node = document.getDocumentElement().getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Entities etc.
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with \u221e", element.getTextContent());
		assertNotNull(document.getElementById("doesnotexist"));
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	@Test(timeout=1000)
	public void testParseInputSourceNoEntityResolverXHTML()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			builder.getSAXParserFactory().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
		} catch (SAXException e) {
		}
		Document document = parseDocument("entities-fulldtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-fulldtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities-fulldtd.xhtml", document.getBaseURI());
		Node node = document.getFirstChild();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A first comment before the DOCTYPE ", ((Comment) node).getData());
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		node = docType.getPreviousSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" Last comment before the DOCTYPE ", ((Comment) node).getData());
		assertEquals("html", docType.getName());
		node = docType.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the DOCTYPE ", ((Comment) node).getData());
		node = node.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		ProcessingInstruction pi = (ProcessingInstruction) node;
		assertEquals("xml-stylesheet", pi.getTarget());
		assertEquals("type=\"text/css\" href=\"style.css\"", pi.getData());
		Element docelm = document.getDocumentElement();
		node = docelm.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Entities etc.
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("entity");
		assertEquals("ent\"ity", element.getAttribute("class"));
		element = document.getElementById("smip");
		assertEquals("Paragraph with ", element.getTextContent());
	}

	@Test(timeout=1000)
	public void testParseInputSourceNoEntityResolverXHTMLOnlySystem()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			builder.getSAXParserFactory().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
		} catch (SAXException e) {
		}
		Document document = parseDocument("entities-systemdtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml", document.getBaseURI());
		Node node = document.getFirstChild();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A first comment before the DOCTYPE ", ((Comment) node).getData());
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		node = docType.getPreviousSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" Last comment before the DOCTYPE ", ((Comment) node).getData());
		assertEquals("html", docType.getName());
		node = docType.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the DOCTYPE ", ((Comment) node).getData());
		node = node.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		ProcessingInstruction pi = (ProcessingInstruction) node;
		assertEquals("xml-stylesheet", pi.getTarget());
		assertEquals("type=\"text/css\" href=\"style.css\"", pi.getData());
		Element docelm = document.getDocumentElement();
		node = docelm.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Entities etc.
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("entity");
		assertEquals("ent\"ity", element.getAttribute("class"));
		element = document.getElementById("smip");
		assertEquals("Paragraph with ", element.getTextContent());
	}

	@Test(timeout=900)
	public void testParseInputSourceNoEntityResolverFullDTDFail() throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			parseDocument("entities-fulldtd.xhtml");
			fail("Must throw exception");
		} catch (SAXParseException e) {
		}
	}

	@Test(timeout=900)
	public void testParseInputSourceNoEntityResolverSystemDTDFail() throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			parseDocument("entities-systemdtd.xhtml");
			fail("Must throw exception");
		} catch (SAXParseException e) {
		}
	}

	@Test
	public void testParseInputSourceXXE() throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(new TestEntityResolver());
		String text = "<!DOCTYPE foo SYSTEM \"http://www.example.com/etc/fakepasswd\"><foo>xxx&yyy;zzz</foo>";
		try {
			builder.parse(new InputSource(new StringReader(text)));
			fail("Must throw exception");
		} catch (SAXException e) {
		}
	}

	@Test
	public void testParseInputSourceXXE2() throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(new TestEntityResolver());
		String text = "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY yyy SYSTEM \"http://www.example.com/etc/fakepasswd\">]><foo>xxx&yyy;zzz</foo>";
		try {
			builder.parse(new InputSource(new StringReader(text)));
			fail("Must throw exception");
		} catch (SAXException e) {
		}
	}

	@Test
	public void testParseInputSourceFileNotFound() throws SAXException, ParserConfigurationException, IOException {
		TestEntityResolver resolver = new TestEntityResolver();
		builder.setEntityResolver(resolver);
		String text = "<!DOCTYPE foo SYSTEM \"https://www.example.com/does/not/exist\"><foo>xxx&yyy;zzz</foo>";
		try {
			builder.parse(new InputSource(new StringReader(text)));
			fail("Must throw exception");
		} catch (FileNotFoundException e) {
		}
	}

	@Test
	public void testParseInputSourceNoDTDXHTML() throws SAXException, IOException {
		Document document = parseDocument("entities-nodtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getBaseURI());
		assertNull(document.getDoctype());
		// Entities etc.
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
	}

	@Test
	public void testParseInputSourceNoResolverNoDTD() throws SAXException, IOException {
		builder.setEntityResolver(null);
		Document document = parseDocument("entities-nodtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getBaseURI());
		assertNull(document.getDoctype());
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
	}

	@Test
	public void testParseInputSourceHtmlElement() throws SAXException, IOException {
		Document document = parseDocument(new StringReader(
			"<!DOCTYPE html><html><body id='bodyid'><br/></body></html><!-- Final comment -->"),
				"html.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		Element element = document.getElementById("bodyid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		assertEquals("body", element.getNodeName());
		element = (Element) element.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertSame(docElement, element);
		// Comment
		Node comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test(timeout=1000)
	public void testParseInputSourceImpliedHtmlElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		Document document = parseDocument(new StringReader(
			"<!DOCTYPE html><body><div id='divid'><br/></div></body><!-- Final comment -->"),
				"impliedhtml.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		Element element = document.getElementById("divid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		element = (Element) element.getParentNode();
		assertNotNull(element);
		assertEquals("body", element.getNodeName());
		// Comment
		Node comment = element.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
		// Root element
		element = (Element) element.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
	}

	@Test
	public void testParseInputSourceImpliedHeadElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		Document document = parseDocument(new StringReader(
			"<!DOCTYPE html><html><title id='titleid'>Some Title</title><meta charset='utf-8'/><script id='scriptid'></script></html><!-- Final comment -->"),
				"impliedhead.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		Element element = document.getElementById("titleid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		Element head = (Element) element.getParentNode();
		assertNotNull(head);
		assertEquals("head", head.getNodeName());
		Element script = document.getElementById("scriptid");
		assertNotNull(script);
		assertSame(head, script.getParentNode());
		element = (Element) head.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
		// Comment
		Node comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceImpliedBodyElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		Document document = parseDocument(new StringReader(
			"<!DOCTYPE html><html><div id='divid'><br/></div></html><!-- Final comment -->"),
				"impliedbody.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		// Body elements
		Element div = document.getElementById("divid");
		assertNotNull(div);
		assertTrue(div.hasChildNodes());
		Element body = (Element) div.getParentNode();
		assertNotNull(body);
		assertEquals("body", body.getNodeName());
		assertSame(docElement, body.getParentNode());
		// Comment
		Node comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceImpliedHeadBodyElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		Document document = parseDocument(new StringReader(
			"<!DOCTYPE html><html><title id='titleid'>Some Title</title><meta charset='utf-8'/><script id='scriptid'></script><div id='divid'><br/></div></html><!-- Final comment -->"),
				"impliedheadbody.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		Element element = document.getElementById("titleid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		Element head = (Element) element.getParentNode();
		assertNotNull(head);
		assertEquals("head", head.getNodeName());
		Element script = document.getElementById("scriptid");
		assertNotNull(script);
		assertSame(head, script.getParentNode());
		element = (Element) head.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
		// Body elements
		Element div = document.getElementById("divid");
		assertNotNull(div);
		assertTrue(div.hasChildNodes());
		Element body = (Element) div.getParentNode();
		assertNotNull(body);
		assertEquals("body", body.getNodeName());
		assertSame(docElement, body.getParentNode());
		// Comment
		Node comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceXMLDTD() throws SAXException, IOException {
		Document document = parseDocument(new StringReader(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE foo [<!ATTLIST div id ID #IMPLIED>]><body><div id='divid'><br/></div></body><!-- Final comment -->"),
				"xml.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		assertEquals("body", docElement.getNodeName());
		Element element = document.getElementById("divid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		element = (Element) element.getParentNode();
		assertNotNull(element);
		assertEquals("body", element.getNodeName());
		assertTrue(docElement == element);
		// Comment
		Node comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceSVG() throws SAXException, IOException {
		Document document = parseDocument(new StringReader(
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"320\" height=\"320\"><rect width=\"120\" height=\"80\" x=\"12\" y=\"64\" fill=\"yellow\" stroke=\"grey\"></rect></svg>"),
				"svg.xhtml");
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		assertEquals("svg", docElement.getNodeName());
		assertEquals("svg", docElement.getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, docElement.getNamespaceURI());
		assertNull(docElement.getPrefix());
		assertTrue(docElement.hasAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns"));
		//
		Element element = (Element) docElement.getFirstChild();
		assertNotNull(element);
		assertFalse(element.hasChildNodes());
		assertEquals("rect", element.getNodeName());
		assertEquals("rect", element.getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, element.getNamespaceURI());
		assertNull(element.getPrefix());
		assertFalse(element.hasAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns"));
		// Convention: unprefixed attributes belong to a namespace partition in the owner element
		Attr nsattr = element.getAttributeNodeNS(TestConfig.SVG_NAMESPACE_URI, "x");
		assertNull(nsattr);
		nsattr = element.getAttributeNode("x");
		assertNotNull(nsattr);
		//
		assertNull(element.getAttributeNodeNS("http://www.w3.org/1999/xhtml", "x"));
	}

	private Document parseDocument(String filename) throws SAXException, IOException {
		return parseDocument(loadClasspathReader(filename), filename);
	}

	private Document parseDocument(Reader re, String filename) throws SAXException, IOException {
		InputSource is = new InputSource(re);
		Document document = builder.parse(is);
		re.close();
		URL base = new URL("http://www.example.com/xml/");
		document.setDocumentURI(new URL(base, filename).toExternalForm());
		return document;
	}

	public static Reader loadEntitiesReader() {
		return loadClasspathReader("entities.xhtml");
	}

	public static Reader loadEntitiesNoDTDReader() {
		return loadClasspathReader("entities-nodtd.xhtml");
	}

	private static Reader loadClasspathReader(final String filename) {
		InputStream is = loadClasspathStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

	private static InputStream loadClasspathStream(final String filename) {
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				return this.getClass().getResourceAsStream(filename);
			}
		});
	}

}
