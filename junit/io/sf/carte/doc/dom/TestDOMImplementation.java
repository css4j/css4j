/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory;
import io.sf.carte.doc.style.css.om.TestStyleDatabase;
import io.sf.carte.doc.style.css.om.TestStyleFormattingFactory;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;

public class TestDOMImplementation extends CSSDOMImplementation {

	private final MockURLConnectionFactory urlFactory = new MockURLConnectionFactory();
	public String parserClass;
	private boolean xmlOnly = false;

	public TestDOMImplementation() {
		this(true, null);
	}

	public TestDOMImplementation(boolean defaultStyleSheet, String parserClass) {
		super();
		this.parserClass = parserClass;
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
			document.appendChild(document.createElementNS(namespaceURI, qualifiedName));
		}
		return document;
	}

	public DOMDocument createBestDocument(String namespaceURI, String qualifiedName, DocumentType doctype)
			throws DOMException {
		DOMDocument document;
		if (namespaceURI == null || namespaceURI.equals(HTMLDocument.HTML_NAMESPACE_URI)
				|| (namespaceURI.length() == 0 && doctype != null && "html".equalsIgnoreCase(doctype.getName()))) {
			document = new MyHTMLDocument(doctype);
		} else {
			document = new MyXMLDocument(doctype);
		}
		return document;
	}

	@Override
	protected Parser createSACParser() throws DOMException {
		if (parserClass == null) {
			return super.createSACParser();
		}
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Parser>() {
			@Override
			public Parser run() {
				try {
					return (Parser) Class.forName(parserClass).getConstructor().newInstance();
				} catch (Exception e) {
					throw new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
				}
			}
		});
	}

	public static HTMLDocument sampleHTMLDocument() throws IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.sampleHTMLReader();
		InputSource is = new InputSource(re);
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(new TestDOMImplementation(true, null));
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
		Reader re = DOMCSSStyleSheetFactoryTest.sampleXHTMLReader();
		InputSource is = new InputSource(re);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new TestDOMImplementation(true, null));
		builder.setIgnoreElementContentWhitespace(true);
		builder.setEntityResolver(new DefaultEntityResolver());
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
		Reader re = DOMCSSStyleSheetFactoryTest.sampleIEReader();
		InputSource is = new InputSource(re);
		TestDOMImplementation domImpl = new TestDOMImplementation(true, null);
		domImpl.getParserFlags().add(Parser2.Flag.STARHACK);
		domImpl.getParserFlags().add(Parser2.Flag.IEVALUES);
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(domImpl);
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

	public static CSSDocument simpleBoxDocument() throws IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.simpleBoxXHTMLReader();
		InputSource is = new InputSource(re);
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(new TestDOMImplementation());
		CSSDocument xhtmlDoc;
		try {
			xhtmlDoc = (CSSDocument) builder.parse(is);
		} catch (SAXException e) {
			return null;
		} finally {
			re.close();
		}
		xhtmlDoc.setDocumentURI("http://www.example.com/xhtml/simplebox.html");
		return xhtmlDoc;
	}

	private class TestDeviceFactory extends DummyDeviceFactory {
		private final StyleDatabase styleDb = new TestStyleDatabase();

		@Override
		public StyleDatabase getStyleDatabase(String targetMedium) {
			return styleDb;
		}
	}

	private class MyHTMLDocument extends HTMLDocument {
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
		 * @param url
		 *            the URL to open a connection to.
		 * @return the URL connection.
		 * @throws IOException
		 *             if the connection could not be opened.
		 */
		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return urlFactory.createConnection(url);
		}

	}

	private class MyXMLDocument extends DOMDocument {

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

	}

}
