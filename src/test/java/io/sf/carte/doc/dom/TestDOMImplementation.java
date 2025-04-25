/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory;
import io.sf.carte.doc.style.css.om.SampleCSS;
import io.sf.carte.doc.style.css.om.TestStyleDatabase;
import io.sf.carte.doc.style.css.om.TestStyleFormattingFactory;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

public class TestDOMImplementation extends CSSDOMImplementation {

	private static final long serialVersionUID = 2L;

	private final MockURLConnectionFactory urlFactory = new MockURLConnectionFactory();
	private HashSet<String> visitedUris = new HashSet<>();
	private boolean xmlOnly = false;

	public TestDOMImplementation() {
		this(true);
	}

	public TestDOMImplementation(boolean defaultStyleSheet) {
		super();
		setDeviceFactory(new TestDeviceFactory());
		if (defaultStyleSheet) {
			setDefaultHTMLUserAgentSheet();
		}
	}

	public void setXmlOnly(boolean xmlOnly) {
		this.xmlOnly = xmlOnly;
	}

	public MockURLConnectionFactory getConnectionFactory() {
		return urlFactory;
	}

	public void setVisitedURI(String href) {
		visitedUris.add(href);
	}

	@Override
	protected StyleFormattingFactory createDefaultStyleFormattingFactory() {
		return new TestStyleFormattingFactory();
	}

	@Override
	public DOMDocument createDocument(String namespaceURI, String qualifiedName, DocumentType doctype)
			throws DOMException {
		if (doctype != null && doctype.getParentNode() != null) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Doctype already in use");
		}
		DOMDocument document;
		if (!xmlOnly) {
			document = createBestDocument(namespaceURI, qualifiedName, doctype);
		} else {
			document = new MyXMLDocument(doctype);
		}
		// Create and append a document element, if provided
		if (qualifiedName != null && qualifiedName.length() != 0) {
			DOMElement docElm = document.createElementNS(namespaceURI, qualifiedName);
			if (docElm.getPrefix() != null && namespaceURI != null) {
				Attr attr = document.createAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI, "xmlns:" + docElm.getPrefix());
				attr.setValue(namespaceURI);
				docElm.setAttributeNodeNS(attr);
			}
			document.appendChild(docElm);
		}
		return document;
	}

	public DOMDocument createBestDocument(String namespaceURI, String qualifiedName, DocumentType doctype)
			throws DOMException {
		DOMDocument document;
		if (isHTMLDocument(namespaceURI, qualifiedName, doctype)) {
			document = new MyHTMLDocument(doctype);
		} else {
			document = new MyXMLDocument(doctype);
		}
		return document;
	}

	private static boolean isHTMLDocument(String namespaceURI, String qualifiedName, DocumentType doctype) {
		if (doctype != null) {
			return "html".equalsIgnoreCase(doctype.getName());
		}
		if (qualifiedName != null) {
			return "html".equalsIgnoreCase(qualifiedName);
		}
		/*
		 * Trick: if namespaceURI is null, create an HTML document, if it is the empty string, an
		 * XML one
		 */
		return namespaceURI == null || HTMLDocument.HTML_NAMESPACE_URI.equals(namespaceURI);
	}

	public static HTMLDocument sampleHTMLDocument() throws IOException {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		parser.setReportingDoctype(true);
		parser.setCommentPolicy(XmlViolationPolicy.ALLOW);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(true));
		builder.setHTMLProcessing(true);
		builder.setXMLReader(parser);
		Reader re = SampleCSS.sampleHTMLReader();
		InputSource is = new InputSource(re);
		HTMLDocument xhtmlDoc;
		try {
			xhtmlDoc = (HTMLDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/htmlsample.html");
		return xhtmlDoc;
	}

	public static HTMLDocument sampleXHTMLDocument() throws IOException {
		Reader re = SampleCSS.sampleXHTMLReader();
		InputSource is = new InputSource(re);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(true));
		builder.setIgnoreElementContentWhitespace(true);
		builder.setEntityResolver(new DefaultEntityResolver());
		builder.setHTMLProcessing(true);
		HTMLDocument xhtmlDoc;
		try {
			xhtmlDoc = (HTMLDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/xmlns.xhtml");
		return xhtmlDoc;
	}

	public static HTMLDocument sampleIEDocument() throws IOException {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		parser.setReportingDoctype(true);
		parser.setCommentPolicy(XmlViolationPolicy.ALLOW);
		TestDOMImplementation domImpl = new TestDOMImplementation(true);
		domImpl.getParserFlags().add(Parser.Flag.IEVALUES);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(domImpl);
		builder.setXMLReader(parser);

		Reader re = SampleCSS.sampleIEReader();
		InputSource is = new InputSource(re);
		HTMLDocument xhtmlDoc;
		try {
			xhtmlDoc = (HTMLDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/iesample.html");
		return xhtmlDoc;
	}

	public static DOMDocument simpleBoxDocument() throws IOException {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		parser.setReportingDoctype(true);
		parser.setCommentPolicy(XmlViolationPolicy.ALLOW);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(true));
		builder.setXMLReader(parser);
		Reader re = SampleCSS.simpleBoxXHTMLReader();
		InputSource is = new InputSource(re);
		DOMDocument xhtmlDoc;
		try {
			xhtmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/simplebox.html");
		return xhtmlDoc;
	}

	public static DOMDocument loadDocument(String filename) throws IOException {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(true));
		builder.setHTMLProcessing(true);
		builder.setXMLReader(parser);
		Reader re = new InputStreamReader(classpathStream(filename), StandardCharsets.UTF_8);
		InputSource is = new InputSource(re);
		DOMDocument htmlDoc;
		try {
			htmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		htmlDoc.setDocumentURI("http://www.example.com/" + filename);
		return htmlDoc;
	}

	public static DOMDocument loadXMLDocument(String filename) throws IOException {
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(true));
		builder.setEntityResolver(new DefaultEntityResolver());
		Reader re = new InputStreamReader(classpathStream(filename), StandardCharsets.UTF_8);
		InputSource is = new InputSource(re);
		DOMDocument xhtmlDoc;
		try {
			xhtmlDoc = (DOMDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		xhtmlDoc.setDocumentURI("http://www.example.com/" + filename);
		return xhtmlDoc;
	}

	private static InputStream classpathStream(final String filename) {
		return TestDOMImplementation.class.getResourceAsStream(filename);
	}

	private static class TestDeviceFactory extends DummyDeviceFactory {
		private final StyleDatabase styleDb = new TestStyleDatabase();

		@Override
		public StyleDatabase getStyleDatabase(String targetMedium) {
			return styleDb;
		}
	}

	private class MyHTMLDocument extends HTMLDocument {

		private static final long serialVersionUID = 1L;

		public MyHTMLDocument(DocumentType documentType) {
			super(documentType);
		}

		@Override
		protected CSSDOMImplementation getStyleSheetFactory() {
			return TestDOMImplementation.this;
		}

		@Override
		public CSSDOMImplementation getImplementation() {
			return TestDOMImplementation.this;
		}

		/**
		 * Opens a connection for the given URL.
		 * 
		 * @param url the URL to open a connection to.
		 * @return the URL connection.
		 * @throws IOException if the connection could not be opened.
		 */
		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return urlFactory.createConnection(url);
		}

		@Override
		public boolean isVisitedURI(String href) {
			return visitedUris.contains(href);
		}

	}

	private class MyXMLDocument extends DOMDocument {

		private static final long serialVersionUID = 1L;

		public MyXMLDocument(DocumentType documentType) {
			super(documentType);
		}

		@Override
		public CSSDOMImplementation getImplementation() {
			return TestDOMImplementation.this;
		}

		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return urlFactory.createConnection(url);
		}

		@Override
		protected CSSDOMImplementation getStyleSheetFactory() {
			return TestDOMImplementation.this;
		}

		@Override
		public boolean isVisitedURI(String href) {
			return visitedUris.contains(href);
		}

	}

}
