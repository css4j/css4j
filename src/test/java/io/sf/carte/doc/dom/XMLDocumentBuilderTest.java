/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.agent.TestEntityResolver;
import io.sf.carte.doc.dom.DOMDocument.LinkStyleProcessingInstruction;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class XMLDocumentBuilderTest {

	private TestDOMImplementation domImpl;
	private XMLDocumentBuilder builder;

	@BeforeEach
	public void setUp() {
		domImpl = new TestDOMImplementation(false);
		builder = new XMLDocumentBuilder(domImpl);
		builder.setIgnoreElementContentWhitespace(true);
		builder.setEntityResolver(new DefaultEntityResolver());
	}

	@Test
	public void testNewDocument() {
		Document document = builder.newDocument();
		assertFalse(document instanceof HTMLDocument);
	}

	@Test
	public void testParseInputSource() throws SAXException, IOException {
		HTMLDocument document = (HTMLDocument) parseDocument("entities.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());

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
		assertEquals("<!DOCTYPE html>", docType.toString());
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

		node = node.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.PROCESSING_INSTRUCTION_NODE, node.getNodeType());
		ProcessingInstruction pialt = (ProcessingInstruction) node;
		assertEquals("xml-stylesheet", pialt.getTarget());
		assertEquals(
				"alternate=\"yes\" href=\"foo?a=&quot;b&quot;&amp;c=&apos;d&apos;\" title=\"Dark\"",
				pialt.getData());
		assertTrue(pialt instanceof LinkStyleProcessingInstruction);
		LinkStyleProcessingInstruction lpi = (LinkStyleProcessingInstruction) pialt;
		assertEquals("yes", lpi.getPseudoAttribute("alternate"));
		assertEquals("foo?a=\"b\"&c='d'", lpi.getPseudoAttribute("href"));

		node = document.getDocumentElement().getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());

		// Entities etc.
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with \u221e", element.getTextContent());
		element = document.getElementById("doesnotexist");
		assertNotNull(element);
		assertEquals("list", element.getTextContent());
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));

		// SVG
		DOMElement svg = document.getElementById("svg1");
		assertNotNull(svg);
		assertEquals(TestConfig.SVG_NAMESPACE_URI, svg.getNamespaceURI());
		assertEquals("s", svg.getPrefix());
		assertEquals("1.1", svg.getAttributeNS(TestConfig.SVG_NAMESPACE_URI, "version"));
		DOMElement rect = svg.getFirstElementChild();
		assertNotNull(rect);
		assertEquals(TestConfig.SVG_NAMESPACE_URI, rect.getNamespaceURI());
		assertEquals("s", rect.getPrefix());
	}

	@Test
	public void testParseInputSourceURI() throws SAXException, IOException {
		InputSource is = new InputSource("http://www.example.com/xml/entities.xhtml");
		Reader re = loadClasspathReader("entities.xhtml");
		is.setCharacterStream(re);
		DOMDocument document = (DOMDocument) builder.parse(is);
		re.close();
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
		Node node = document.getFirstChild();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
	}

	@Test
	@Timeout(value = 900, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceXML() throws SAXException, IOException {
		domImpl.setXmlOnly(true);
		DOMDocument document = parseDocument("entities.xhtml");
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
		assertEquals("<!DOCTYPE html>", docType.toString());
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
		// Implied attributes are not present
		DOMElement style = document.getElementsByTagName("style").item(0);
		assertFalse(style.hasAttributeNS(DOMDocument.XML_NAMESPACE_URI, "space"));
		assertFalse(style.hasAttribute("xml:space"));
		// Entities etc.
		DOMElement element = document.getElementById("entity");
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

	@Test
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceXMLNotSpecifiedAttributes() throws SAXException, IOException {
		domImpl.setXmlOnly(true);
		builder.setIgnoreNotSpecifiedAttributes(false);
		DOMDocument document = parseDocument("entities.xhtml");
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
		assertEquals("<!DOCTYPE html>", docType.toString());
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
		// Implied attributes are present
		DOMElement style = document.getElementsByTagName("style").item(0);
		assertTrue(style.hasAttributeNS(DOMDocument.XML_NAMESPACE_URI, "space"));
		assertTrue(style.hasAttribute("xml:space"));
		assertEquals("preserve", style.getAttribute("xml:space"));
		assertEquals("preserve", style.getAttributeNS(DOMDocument.XML_NAMESPACE_URI, "space"));
		Attr attr = style.getAttributeNode("xml:space");
		assertNotNull(attr);
		assertFalse(attr.getSpecified());
		// Entities etc.
		DOMElement element = document.getElementById("entity");
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
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceOnlySystem() throws SAXException, IOException {
		HTMLDocument document = (HTMLDocument) parseDocument("entities-systemdtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml",
				document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
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
		assertEquals(
				"<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				docType.toString());
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
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with \u221e", element.getTextContent());
		element = document.getElementById("doesnotexist");
		assertNotNull(element);
		assertEquals("list", element.getTextContent());
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	@Test
	public void testParseInputSourceXMLOnlySystem() throws SAXException, IOException {
		domImpl.setXmlOnly(true);
		DOMDocument document = parseDocument("entities-systemdtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml",
				document.getDocumentURI());
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
		assertEquals(
				"<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				docType.toString());
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
		DOMElement element = document.getElementById("entity");
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

	@Test
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceNoEntityResolver()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			builder.getSAXParserFactory().setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXException e) {
		}
		HTMLDocument document = (HTMLDocument) parseDocument("entities-fulldtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-fulldtd.xhtml",
				document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
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
		assertEquals(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				docType.toString());
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
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with ", element.getTextContent());
		element = document.getElementById("doesnotexist");
		assertNotNull(element);
		assertEquals("list", element.getTextContent());
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	@Test
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceNoEntityResolverXML()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			builder.getSAXParserFactory().setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXException e) {
		}
		domImpl.setXmlOnly(true);
		DOMDocument document = parseDocument("entities-fulldtd.xhtml");
		assertNotNull(document);
		assertFalse(document instanceof HTMLDocument);
		assertEquals("http://www.example.com/xml/entities-fulldtd.xhtml",
				document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
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
		assertEquals(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				docType.toString());
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
		DOMElement docelm = document.getDocumentElement();
		node = docelm.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Entities etc.
		DOMElement body = docelm.getChildren().item(1);
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		assertEquals("ent\"ity", element.getClassName());
		element = body.getChildren().item(4);
		assertEquals("Paragraph with ", element.getTextContent());
	}

	@Test
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceNoEntityResolverOnlySystem()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		try {
			builder.getSAXParserFactory().setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXException e) {
		}
		HTMLDocument document = (HTMLDocument) parseDocument("entities-systemdtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml",
				document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
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
		assertEquals(
				"<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				docType.toString());
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
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
		element = document.getElementById("smip");
		assertNotNull(element);
		assertEquals("Paragraph with ", element.getTextContent());
		element = document.getElementById("doesnotexist");
		assertNotNull(element);
		assertEquals("list", element.getTextContent());
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	@Test
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceNoEntityResolverXMLOnlySystem()
			throws SAXException, ParserConfigurationException, IOException {
		domImpl.setXmlOnly(true);
		builder.setEntityResolver(null);
		try {
			builder.getSAXParserFactory().setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXException e) {
		}
		DOMDocument document = parseDocument("entities-systemdtd.xhtml");
		assertNotNull(document);
		assertFalse(document instanceof HTMLDocument);
		assertEquals("http://www.example.com/xml/entities-systemdtd.xhtml",
				document.getDocumentURI());
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
		assertEquals(
				"<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
				docType.toString());
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
		DOMElement docelm = document.getDocumentElement();
		node = docelm.getNextSibling();
		assertNotNull(node);
		assertEquals(Node.COMMENT_NODE, node.getNodeType());
		assertEquals(" A comment just after the document element ", node.getNodeValue());
		// Entities etc.
		DOMElement body = docelm.getChildren().item(1);
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		assertEquals("ent\"ity", element.getClassName());
		element = body.getChildren().item(4);
		assertEquals("Paragraph with ", element.getTextContent());
	}

	@Test
	@Timeout(value = 900, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceNoEntityResolverFullDTDFail()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		assertThrows(SAXParseException.class, () -> parseDocument("entities-fulldtd.xhtml"));
	}

	@Test
	@Timeout(value = 900, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceNoEntityResolverSystemDTDFail()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		assertThrows(SAXParseException.class, () -> parseDocument("entities-systemdtd.xhtml"));
	}

	@Test
	public void testParseInputSourceNoER_XXE()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		String text = "<!DOCTYPE foo SYSTEM \"http://www.example.com/etc/fakepasswd\"><foo>xxx&yyy;zzz</foo>";
		assertThrows(SAXParseException.class,
				() -> builder.parse(new InputSource(new StringReader(text))));
	}

	@Test
	public void testParseInputSourceXXE()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(new TestEntityResolver());
		String text = "<!DOCTYPE foo SYSTEM \"http://www.example.com/etc/fakepasswd\"><foo>xxx&yyy;zzz</foo>";
		assertThrows(SAXException.class,
				() -> builder.parse(new InputSource(new StringReader(text))));
	}

	@Test
	public void testParseInputSourceNoER_XXE2()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(null);
		String text = "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY yyy SYSTEM \"http://www.example.com/etc/fakepasswd\">]><foo>xxx&yyy;zzz</foo>";
		Document document = builder.parse(new InputSource(new StringReader(text)));
		assertNotNull(document);
		DocumentType dtype = document.getDoctype();
		assertNotNull(dtype);
		assertEquals("foo", dtype.getName());
		assertNull(dtype.getSystemId());
		assertNull(dtype.getEntities());
		org.w3c.dom.Element element = document.getDocumentElement();
		assertNotNull(element);
		assertEquals("xxxzzz", element.getTextContent());
	}

	@Test
	public void testParseInputSourceXXE2()
			throws SAXException, ParserConfigurationException, IOException {
		builder.setEntityResolver(new TestEntityResolver());
		String text = "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY yyy SYSTEM \"http://www.example.com/etc/fakepasswd\">]><foo>xxx&yyy;zzz</foo>";
		assertThrows(SAXException.class,
				() -> builder.parse(new InputSource(new StringReader(text))));
	}

	@Test
	public void testParseInputSourceFileNotFound()
			throws SAXException, ParserConfigurationException, IOException {
		TestEntityResolver resolver = new TestEntityResolver();
		builder.setEntityResolver(resolver);
		String text = "<!DOCTYPE foo SYSTEM \"https://www.example.com/does/not/exist\"><foo>xxx&yyy;zzz</foo>";
		assertThrows(FileNotFoundException.class,
				() -> builder.parse(new InputSource(new StringReader(text))));
	}

	@Test
	public void testParseInputSourceNoDTD() throws SAXException, IOException {
		HTMLDocument document = (HTMLDocument) parseDocument("entities-nodtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
		assertNull(document.getDoctype());
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
	}

	@Test
	public void testParseInputSourceNoDTDXML() throws SAXException, IOException {
		domImpl.setXmlOnly(true);
		DOMDocument document = parseDocument("entities-nodtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getBaseURI());
		assertNull(document.getDoctype());
		DOMElement docelm = document.getDocumentElement();
		// Entities etc.
		DOMElement body = docelm.getChildren().item(1);
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = body.getLastElementChild();
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
	}

	@Test
	public void testParseInputSourceNoResolverNoDTD() throws SAXException, IOException {
		builder.setEntityResolver(null);
		HTMLDocument document = (HTMLDocument) parseDocument("entities-nodtd.xhtml");
		assertNotNull(document);
		assertEquals("http://www.example.com/xml/entities-nodtd.xhtml", document.getDocumentURI());
		assertEquals("http://www.example.com/", document.getBaseURI());
		assertNull(document.getDoctype());
		DOMElement element = document.getElementById("entity");
		assertNotNull(element);
		assertEquals("<>", element.getTextContent());
		element = document.getElementById("entiamp");
		assertNotNull(element);
		assertEquals("&", element.getTextContent());
	}

	@Test
	public void testParseInputSourceBadVoidChild() throws SAXException, IOException {
		assertThrows(SAXParseException.class,
				() -> parseDocument(
						new StringReader(
								"<!DOCTYPE html><html><body><br id='brid'>foo</br></body></html>"),
						"badvoidchild.xhtml"));
	}

	@Test
	public void testParseInputSourceHtmlElement() throws SAXException, IOException {
		HTMLDocument document = (HTMLDocument) parseDocument(new StringReader(
			"<!DOCTYPE html><html><body id='bodyid'><br/></body></html><!-- Final comment -->"),
				"html.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		DOMElement element = document.getElementById("bodyid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		assertEquals("body", element.getNodeName());
		element = (DOMElement) element.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertSame(docElement, element);
		// Comment
		DOMNode comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
	public void testParseInputSourceImpliedHtmlElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		HTMLDocument document = (HTMLDocument) parseDocument(new StringReader(
			"<!DOCTYPE html><body><div id='divid'><br/></div></body><!-- Final comment -->"),
				"impliedhtml.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		DOMElement element = document.getElementById("divid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		element = (DOMElement) element.getParentNode();
		assertNotNull(element);
		assertEquals("body", element.getNodeName());
		// Comment
		DOMNode comment = element.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
		// Root element
		element = (DOMElement) element.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
	}

	@Test
	public void testParseInputSourceImpliedHeadElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		HTMLDocument document = (HTMLDocument) parseDocument(new StringReader(
			"<!DOCTYPE html><html><title id='titleid'>Some Title</title><meta charset='utf-8'/><script id='scriptid'></script></html><!-- Final comment -->"),
				"impliedhead.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		DOMElement element = document.getElementById("titleid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		DOMElement head = (DOMElement) element.getParentNode();
		assertNotNull(head);
		assertEquals("head", head.getNodeName());
		DOMElement script = document.getElementById("scriptid");
		assertNotNull(script);
		assertSame(head, script.getParentNode());
		assertEquals("meta", head.getChildren().item(1).getNodeName());
		element = (DOMElement) head.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
		// Comment
		DOMNode comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceImpliedBodyElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		HTMLDocument document = (HTMLDocument) parseDocument(new StringReader(
			"<!DOCTYPE html><html><div id='divid'><br/></div></html><!-- Final comment -->"),
				"impliedbody.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		// Body elements
		DOMElement div = document.getElementById("divid");
		assertNotNull(div);
		assertTrue(div.hasChildNodes());
		DOMElement body = (DOMElement) div.getParentNode();
		assertNotNull(body);
		assertEquals("body", body.getNodeName());
		assertSame(docElement, body.getParentNode());
		// Comment
		DOMNode comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceImpliedHeadBodyElement() throws SAXException, IOException {
		builder.setHTMLProcessing(true);
		HTMLDocument document = (HTMLDocument) parseDocument(new StringReader(
			"<!DOCTYPE html><html><title id='titleid'>Some Title</title><meta charset='utf-8'/><script id='scriptid'></script><div id='divid'><br/></div></html><!-- Final comment -->"),
				"impliedheadbody.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		DOMElement element = document.getElementById("titleid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		DOMElement head = (DOMElement) element.getParentNode();
		assertNotNull(head);
		assertEquals("head", head.getNodeName());
		DOMElement script = document.getElementById("scriptid");
		assertNotNull(script);
		assertSame(head, script.getParentNode());
		assertEquals("meta", head.getChildren().item(1).getNodeName());
		element = (DOMElement) head.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
		// Body elements
		DOMElement div = document.getElementById("divid");
		assertNotNull(div);
		assertTrue(div.hasChildNodes());
		DOMElement body = (DOMElement) div.getParentNode();
		assertNotNull(body);
		assertEquals("body", body.getNodeName());
		assertSame(docElement, body.getParentNode());
		// Comment
		DOMNode comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceXMLDTD() throws SAXException, IOException {
		DOMDocument document = parseDocument(new StringReader(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE foo [<!ATTLIST div id ID #IMPLIED>]><body><div id='divid'><br/></div></body><!-- Final comment -->"),
				"xml.xhtml");
		assertNotNull(document);
		assertFalse(document instanceof HTMLDocument);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		assertEquals("body", docElement.getNodeName());
		DOMElement element = document.getElementById("divid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		element = (DOMElement) element.getParentNode();
		assertNotNull(element);
		assertEquals("body", element.getNodeName());
		assertTrue(docElement == element);
		// Comment
		DOMNode comment = docElement.getNextSibling();
		assertNotNull(comment);
		assertEquals(" Final comment ", comment.getNodeValue());
	}

	@Test
	public void testParseInputSourceSVG() throws SAXException, IOException {
		DOMDocument document = parseDocument(new StringReader(
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"320\" height=\"320\"><rect width=\"120\" height=\"80\" x=\"12\" y=\"64\" fill=\"yellow\" stroke=\"grey\"></rect></svg>"),
				"svg.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		assertEquals("svg", docElement.getNodeName());
		assertEquals("svg", docElement.getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, docElement.getNamespaceURI());
		assertNull(docElement.getPrefix());
		assertTrue(docElement.hasAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns"));

		DOMElement element = docElement.getFirstElementChild();
		assertNotNull(element);
		assertFalse(element.hasChildNodes());
		assertEquals("rect", element.getNodeName());
		assertEquals("rect", element.getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, element.getNamespaceURI());
		assertNull(element.getPrefix());
		assertFalse(element.hasAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns"));
		// Convention: unprefixed attributes have an empty namespace, related to the owner element
		Attr nsattr = element.getAttributeNodeNS(TestConfig.SVG_NAMESPACE_URI, "x");
		assertNull(nsattr);
		nsattr = element.getAttributeNode("x");
		assertNotNull(nsattr);
		assertNull(nsattr.getNamespaceURI());
		assertNull(nsattr.getPrefix());

		Attr attr = element.getAttributeNode("x");
		assertSame(nsattr, attr);

		assertNull(element.getAttributeNodeNS(HTMLDocument.HTML_NAMESPACE_URI, "x"));
	}

	@Test
	public void testParseInputSourceSVG_NoNS() throws SAXException, IOException {
		DOMDocument document = parseDocument(new StringReader(
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg width=\"320\" height=\"320\"><rect width=\"120\" height=\"80\" x=\"12\" y=\"64\" fill=\"yellow\" stroke=\"grey\"></rect></svg>"),
				"svg.xhtml");
		assertNotNull(document);
		DOMElement docElement = document.getDocumentElement();
		assertNotNull(docElement);
		assertEquals("svg", docElement.getNodeName());
		assertEquals("svg", docElement.getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, docElement.getNamespaceURI());
		assertNull(docElement.getPrefix());
		assertFalse(docElement.hasAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns"));

		DOMElement element = docElement.getFirstElementChild();
		assertNotNull(element);
		assertFalse(element.hasChildNodes());
		assertEquals("rect", element.getNodeName());
		assertEquals("rect", element.getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, element.getNamespaceURI());
		assertNull(element.getPrefix());
		assertFalse(element.hasAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns"));
		// Convention: unprefixed attributes have an empty namespace, related to the owner element
		Attr nsattr = element.getAttributeNodeNS(TestConfig.SVG_NAMESPACE_URI, "x");
		assertNull(nsattr);
		nsattr = element.getAttributeNode("x");
		assertNotNull(nsattr);
		assertNull(nsattr.getNamespaceURI());
		assertNull(nsattr.getPrefix());

		Attr attr = element.getAttributeNode("x");
		assertSame(nsattr, attr);

		assertNull(element.getAttributeNodeNS("http://www.w3.org/1999/xhtml", "x"));
	}

	private DOMDocument parseDocument(String filename) throws SAXException, IOException {
		return parseDocument(loadClasspathReader(filename), filename);
	}

	private DOMDocument parseDocument(Reader re, String filename) throws SAXException, IOException {
		InputSource is = new InputSource(re);
		DOMDocument document = (DOMDocument) builder.parse(is);
		re.close();

		URI base;
		URI uri;
		try {
			base = new URI("http://www.example.com/xml/");
			uri = new URI(filename);
		} catch (URISyntaxException e) {
			throw new MalformedURLException(e.getMessage());
		}
		document.setDocumentURI(base.resolve(uri).toASCIIString());
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
		return XMLDocumentBuilderTest.class.getResourceAsStream(filename);
	}

}
