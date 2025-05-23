/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.om.DefaultStyleFormattingFactory;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import io.sf.carte.util.BufferSimpleWriter;

public class DOMWriterTest {

	@Test
	public void testSerializeToString() throws IOException, SAXException {
		TestDOMImplementation domImpl = new TestDOMImplementation();
		DocumentType doctype = domImpl.createDocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN",
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
		DOMDocument document = domImpl.createDocument(null, "html", doctype);
		DOMElement html = document.getDocumentElement();
		html.setAttribute("lang", "en");
		DOMElement head = document.createElement("head");
		DOMElement title = document.createElement("title");
		title.appendChild(document.createTextNode("Title"));
		head.appendChild(title);
		html.appendChild(head);
		DOMElement body = document.createElement("body");
		DOMElement div = document.createElement("div");
		div.appendChild(document.createTextNode("This document "));
		DOMElement italic = document.createElement("i");
		italic.appendChild(document.createTextNode("is"));
		div.appendChild(italic);
		div.appendChild(document.createTextNode(" "));
		DOMElement bold = document.createElement("b");
		bold.appendChild(document.createTextNode("not"));
		div.appendChild(bold);
		div.appendChild(document.createTextNode(" \u221e"));
		body.appendChild(document.createComment(" Comment before DIV "));
		body.appendChild(div);
		html.appendChild(body);
		DOMWriter domWriter = new DOMWriter();
		int[] entities = { 0x221e };
		domWriter.setEntityCodepoints(doctype, entities);
		domWriter.setIndentingUnit(1);
		String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html lang=\"en\">\n <head><title>Title</title></head>\n <body>\n  <!-- Comment before DIV -->\n  <div>This document <i>is</i> <b>not</b> &infin;</div>\n </body>\n</html>\n";
		assertEquals(expected, domWriter.serializeToString(document));
		// Normalize and re-test.
		document.normalizeDocument();
		assertEquals(expected, domWriter.serializeToString(document));
	}

	@Test
	public void testInjection() throws IOException, SAXException {
		TestDOMImplementation domImpl = new TestDOMImplementation();
		DocumentType doctype = domImpl.createDocumentType("html", null,
				"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><injection/><foo bar=\"");
		DOMDocument document = domImpl.createDocument(null, "html", doctype);
		DOMElement html = document.getDocumentElement();
		html.setAttribute("lang", "en\"><injection/><head");
		DOMElement body = document.createElement("body");
		DOMElement div = document.createElement("div");
		div.appendChild(document.createTextNode("<injection/>"));
		body.appendChild(div);
		html.appendChild(body);
		DOMWriter domWriter = new DOMWriter();
		domWriter.setIndentingUnit(1);
		assertEquals(
				"<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd&quot;&gt;&lt;injection/&gt;&lt;foo bar=&quot;\">\n<html lang=\"en&quot;&gt;&lt;injection/&gt;&lt;head\">\n <body>\n  <div>&lt;injection/&gt;</div>\n </body>\n</html>\n",
				domWriter.serializeToString(document));
	}

	@Test
	public void testSerializeSVG() throws IOException, SAXException {
		TestDOMImplementation domImpl = new TestDOMImplementation();
		DocumentType doctype = domImpl.createDocumentType("svg", "-//W3C//DTD SVG 1.1//EN",
				"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd");
		DOMDocument document = domImpl.createDocument(null, null, doctype);
		DOMElement docElm = document.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg");
		docElm.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", TestConfig.SVG_NAMESPACE_URI);
		document.appendChild(docElm);
		docElm.setAttribute("version", "1.1");
		docElm.setAttribute("width", "320");
		docElm.setAttribute("height", "320");
		DOMElement rect = document.createElement("rect");
		rect.setAttribute("width", "120");
		rect.setAttribute("height", "80");
		rect.setAttribute("x", "12");
		rect.setAttribute("y", "64");
		rect.setAttribute("fill", "yellow");
		rect.setAttribute("stroke", "grey");
		docElm.appendChild(rect);
		DOMWriter domWriter = new DOMWriter();
		domWriter.setIndentingUnit(1);
		String expected = "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"320\" height=\"320\"><rect width=\"120\" height=\"80\" x=\"12\" y=\"64\" fill=\"yellow\" stroke=\"grey\"/></svg>\n";
		assertEquals(expected, domWriter.serializeToString(document));
		// Normalize and re-test.
		document.normalizeDocument();
		assertEquals(expected, domWriter.serializeToString(document));
	}

	@Test
	public void testSerializePrefix() throws IOException {
		TestDOMImplementation domImpl = new TestDOMImplementation();
		DOMDocument document = domImpl.createDocument(HTMLDocument.HTML_NAMESPACE_URI, "html", null);
		DOMElement html = document.getDocumentElement();
		DOMElement svg = document.createElementNS(TestConfig.SVG_NAMESPACE_URI, "s:svg");
		html.appendChild(svg);
		DOMWriter domWriter = new DOMWriter();
		domWriter.setIndentingUnit(1);
		String expected = "<html><s:svg xmlns:s=\"http://www.w3.org/2000/svg\"/></html>\n";
		assertEquals(expected, domWriter.serializeToString(document));

		svg.setAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns:s", TestConfig.SVG_NAMESPACE_URI);
		assertEquals(expected, domWriter.serializeToString(document));

		DOMElement rect = document.createElementNS(TestConfig.SVG_NAMESPACE_URI, "svg:rect");
		svg.appendChild(rect);
		expected = "<html><s:svg xmlns:s=\"http://www.w3.org/2000/svg\"><svg:rect xmlns:svg=\"http://www.w3.org/2000/svg\"/></s:svg></html>\n";
		assertEquals(expected, domWriter.serializeToString(document));

		rect.setAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns:svg", TestConfig.SVG_NAMESPACE_URI);
		assertEquals(expected, domWriter.serializeToString(document));
	}

	@Test
	public void testSerializeHTML() throws IOException {
		DOMDocument document = TestDOMImplementation.sampleHTMLDocument();
		BufferSimpleWriter writer = new BufferSimpleWriter(4096);

		DOMWriter.writeTree(document, writer);

		String expected = classPathFile("/io/sf/carte/doc/dom/domwriter-html.html");
		String canonical = classPathFile("/io/sf/carte/doc/dom/domwriter-html-normalized.html");
		expected = expected.replace("\r", "");
		canonical = canonical.replace("\r", "");
		String result = writer.toString();
		assertEquals(expected, result);

		// Format the styles
		DOMDocument pdoc = parseDocument(new StringReader(result));
		pdoc.setDocumentURI(document.getDocumentURI());
		StyleFormattingFactory factory = new DefaultStyleFormattingFactory();
		pdoc.getImplementation().setStyleFormattingFactory(factory);
		document.getImplementation().setStyleFormattingFactory(factory);

		// Normalize and re-test.
		pdoc.getStyleSheet();
		pdoc.normalizeDocument();
		document.normalizeDocument();

		// Do not check for equal nodes because of namespace issues

		DOMWriter domWriter = new DOMWriter();
		assertEquals(canonical, domWriter.serializeToString(document));
	}

	@Test
	public void testSerializeDocument() throws IOException {
		DOMDocument document = TestDOMImplementation.sampleXHTMLDocument();
		BufferSimpleWriter writer = new BufferSimpleWriter(4096);

		DOMWriter.writeTree(document, writer);

		String expected = classPathFile("/io/sf/carte/doc/dom/domwriteroutput.html");
		String canonical = classPathFile("/io/sf/carte/doc/dom/domwritercanonical.html");
		expected = expected.replace("\r", "");
		canonical = canonical.replace("\r", "");
		String result = writer.toString();
		assertEquals(expected, result);

		// Format the styles
		DOMDocument pdoc = parseDocument(new StringReader(result));
		pdoc.setDocumentURI(document.getDocumentURI());
		StyleFormattingFactory factory = new DefaultStyleFormattingFactory();
		pdoc.getImplementation().setStyleFormattingFactory(factory);
		document.getImplementation().setStyleFormattingFactory(factory);

		// Normalize and re-test.
		pdoc.getStyleSheet();
		pdoc.normalizeDocument();
		document.normalizeDocument();

		// We still have the same document nodes
		assertEqualNodes(document, pdoc);

		DOMWriter domWriter = new DOMWriter();
		assertEquals(canonical, domWriter.serializeToString(document));
	}

	/**
	 * Test serialization with entities.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void testSerializeDocument2() throws IOException, SAXException {
		DOMDocument document = TestDOMImplementation.sampleXHTMLDocument();
		BufferSimpleWriter writer = new BufferSimpleWriter(4096);

		int[] codePointsToReplace = { 237, 8734 }; // 'í','∞'
		DOMWriter domWriter = new DOMWriter();
		assertEquals(codePointsToReplace.length,
				domWriter.setEntityCodepoints(document.getDoctype(), codePointsToReplace));

		domWriter.writeNode(document, writer);

		String expected = classPathFile("/io/sf/carte/doc/dom/domwriteroutput2.html");
		String canonical = classPathFile("/io/sf/carte/doc/dom/domwritercanonical2.html");
		expected = expected.replace("\r", "");
		canonical = canonical.replace("\r", "");
		String result = writer.toString();
		assertEquals(expected, result);

		// Format the styles
		DOMDocument pdoc = parseDocument(new StringReader(result));
		pdoc.setDocumentURI(document.getDocumentURI());
		StyleFormattingFactory factory = new DefaultStyleFormattingFactory();
		pdoc.getImplementation().setStyleFormattingFactory(factory);
		document.getImplementation().setStyleFormattingFactory(factory);

		// Normalize and re-test.
		pdoc.getStyleSheet();
		pdoc.normalizeDocument();
		document.normalizeDocument();

		// We still have the same document nodes
		assertEqualNodes(document, pdoc);

		assertEquals(canonical, domWriter.serializeToString(document));
	}

	void assertEqualNodes(Node first, Node arg) {
		if (arg != null) {
			assertEquals(first.getNodeType(), arg.getNodeType(), "Different node type.");
			assertEquals(first.getNodeName(), arg.getNodeName(), "Different node name.");
			assertEquals(first.getLocalName(), arg.getLocalName(), "Different local name.");
			assertEquals(first.getNamespaceURI(), arg.getNamespaceURI(),
					"Different namespace URI for node " + first.getNodeName());
			assertEquals(first.getPrefix(), arg.getPrefix(),
					"Different prefix for node " + first.getNodeName());
			assertTrue(stringEquals(first.getNodeValue(), arg.getNodeValue()),
					"Different node value.");
			Node node = first.getFirstChild();
			Node othernode = arg.getFirstChild();
			while (node != null) {
				if (!isIgnorableNode(node)) {
					assertNotNull(othernode);
					if (!isIgnorableNode(othernode)) {
						assertEqualNodes(node, othernode);
					} else {
						othernode = othernode.getNextSibling();
						continue;
					}
				} else {
					node = node.getNextSibling();
					continue;
				}
				node = node.getNextSibling();
				othernode = othernode.getNextSibling();
			}
			while (othernode != null) {
				if (!isIgnorableNode(othernode)) {
					fail("Found an additional node: " + othernode.toString());
				}
				othernode = othernode.getNextSibling();
			}
			NamedNodeMap nmap = first.getAttributes();
			NamedNodeMap othernmap = arg.getAttributes();
			if (nmap == null) {
				assertNull(othernmap);
				return;
			} else {
				int sz = nmap.getLength();
				if (sz == othernmap.getLength()) {
					extloop: for (int i = 0; i < sz; i++) {
						Node attr = nmap.item(i);
						for (int j = 0; j < sz; j++) {
							if (attr.isEqualNode(othernmap.item(j))) {
								continue extloop;
							}
						}
						fail("Attributes not equal");
					}
					return;
				}
			}
		} else {
			assertNull(first);
			return;
		}
		fail("Comparison failed");
	}

	private boolean stringEquals(String str1, String str2) {
		if (str1 == str2) {
			return true;
		}
		if (str1 == null) {
			return false;
		}
		return str2 != null && str1.trim().equals(str2.trim());
	}

	private boolean isIgnorableNode(Node node) {
		return node.getNodeType() == Node.TEXT_NODE
				&& (((Text) node).isElementContentWhitespace() || node.getNodeValue().trim().length() == 0);
	}

	private DOMDocument parseDocument(Reader re) throws IOException {
		InputSource is = new InputSource(re);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(false));
		builder.setIgnoreElementContentWhitespace(true);
		builder.setHTMLProcessing(true);
		builder.setEntityResolver(new DefaultEntityResolver());
		DOMDocument xhtmlDoc;
		try {
			xhtmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		return xhtmlDoc;
	}

	static String classPathFile(String filename) {
		Reader re = classpathReader(filename);
		StringBuilder buf = new StringBuilder();
		char[] bbuf = new char[4096];
		int len;
		try {
			while ((len = re.read(bbuf)) != -1) {
				buf.append(bbuf, 0, len);
			}
		} catch (IOException e) {
			return null;
		} finally {
			try {
				re.close();
			} catch (IOException e) {
			}
		}
		return buf.toString();
	}

	private static Reader classpathReader(final String filename) {
		InputStream is = classpathStream(filename);
		Reader re = null;
		if (is != null) {
			re = new InputStreamReader(is, StandardCharsets.UTF_8);
		}
		return re;
	}

	private static InputStream classpathStream(final String filename) {
		return DOMWriterTest.class.getResourceAsStream(filename);
	}

}
