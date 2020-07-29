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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
		div.appendChild(document.createTextNode("This document is not \u221e"));
		body.appendChild(document.createComment(" Comment before DIV "));
		body.appendChild(div);
		html.appendChild(body);
		DOMWriter domWriter = new DOMWriter();
		int[] entities = { 0x221e };
		domWriter.setEntityCodepoints(doctype, entities);
		domWriter.setIndentingUnit(1);
		assertEquals(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html lang=\"en\">\n <head><title>Title</title></head>\n <body>\n  <!-- Comment before DIV -->\n  <div>This document is not &infin;</div>\n </body>\n</html>\n",
				domWriter.serializeToString(document));
	}

	@Test
	public void testSerializeDocument() throws IOException {
		DOMDocument document = TestDOMImplementation.sampleXHTMLDocument();
		BufferSimpleWriter writer = new BufferSimpleWriter(4096);
		DOMWriter.writeTree(document, writer);
		String expected = classPathFile("/io/sf/carte/doc/dom/domwriteroutput.html");
		expected = expected.replace("\r", "");
		String result = writer.toString();
		assertEquals(expected, result);
		DOMDocument pdoc = parseDocument(new StringReader(result));
		pdoc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		pdoc.normalize();
		document.normalize();
		assertEqualNodes(document, pdoc);
	}

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
		expected = expected.replace("\r", "");
		String result = writer.toString();
		assertEquals(expected, result);
		DOMDocument pdoc = parseDocument(new StringReader(result));
		pdoc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		pdoc.normalize();
		document.normalize();
		assertEqualNodes(document, pdoc);
	}

	void assertEqualNodes(Node first, Node arg) {
		if (arg != null) {
			assertEquals(first.getNodeType(), arg.getNodeType());
			assertEquals(first.getNodeName(), arg.getNodeName());
			assertEquals(first.getLocalName(), arg.getLocalName());
			assertEquals(first.getNamespaceURI(), arg.getNamespaceURI());
			assertEquals(first.getPrefix(), arg.getPrefix());
			assertTrue(stringEquals(first.getNodeValue(), arg.getNodeValue()));
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

	private static String classPathFile(String filename) {
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
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				return this.getClass().getResourceAsStream(filename);
			}
		});
	}

}
