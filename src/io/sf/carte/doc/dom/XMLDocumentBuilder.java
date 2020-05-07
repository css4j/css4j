/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.LexicalHandler;

/**
 * Generic <code>DocumentBuilder</code> for XML documents.
 */
public class XMLDocumentBuilder extends DocumentBuilder {

	private final DOMImplementation domImpl;

	private final SAXParserFactory parserFactory;

	private EntityResolver resolver = null;

	private ErrorHandler errorHandler = null;

	private boolean strictErrorChecking = true;

	private boolean ignoreElementContentWhitespace = false;

	private boolean ignoreImpliedAttributes = true;

	public XMLDocumentBuilder(DOMImplementation domImpl) {
		this(domImpl, SAXParserFactory.newInstance());
		parserFactory.setNamespaceAware(true);
		try {
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			parserFactory.setFeature("http://xml.org/sax/features/xmlns-uris", true);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
		}
	}

	public XMLDocumentBuilder(DOMImplementation domImpl, SAXParserFactory parserFactory) {
		super();
		this.domImpl = domImpl;
		this.parserFactory = parserFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document parse(InputSource is) throws SAXException, IOException {
		SAXParser saxParser;
		try {
			saxParser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
		XMLReader xmlReader = saxParser.getXMLReader();
		if (resolver != null) {
			xmlReader.setEntityResolver(resolver);
		} else {
			// It is unsafe to operate without an EntityResolver
			xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
			xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		}
		MyContentHandler handler;
		if (ignoreImpliedAttributes) {
			handler = new MyContentHandler();
		} else {
			handler = new MyContentHandlerImpliedAttr();
		}
		xmlReader.setContentHandler(handler);
		xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
		if (errorHandler != null) {
			xmlReader.setErrorHandler(errorHandler);
		} else {
			xmlReader.setErrorHandler(handler);
		}
		xmlReader.parse(is);
		return handler.getDocument();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isNamespaceAware() {
		return parserFactory.isNamespaceAware();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidating() {
		return parserFactory.isValidating();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isXIncludeAware() {
		return parserFactory.isXIncludeAware();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEntityResolver(EntityResolver er) {
		this.resolver = er;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setErrorHandler(ErrorHandler eh) {
		errorHandler = eh;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document newDocument() {
		return createDocument(null, null, null);
	}

	private Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype)
			throws DOMException {
		if (doctype != null && "html".equals(doctype.getName()) && namespaceURI != null && namespaceURI.length() == 0) {
			// This is HTML and we do not want to obtain a plain DOMDocument
			namespaceURI = null;
		}
		Document document = domImpl.createDocument(namespaceURI, qualifiedName, doctype);
		if (!strictErrorChecking) {
			document.setStrictErrorChecking(false);
		}
		return document;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMImplementation getDOMImplementation() {
		return domImpl;
	}

	/**
	 * Get the <code>SAXParserFactory</code> object used by this builder.
	 * 
	 * @return the <code>SAXParserFactory</code> object.
	 */
	public SAXParserFactory getSAXParserFactory() {
		return parserFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Schema getSchema() {
		return parserFactory.getSchema();
	}

	/**
	 * Configure the builder to ignore (or not) element content whitespace when
	 * building the document.
	 * 
	 * @param ignore set it to <code>true</code> to ignore element content
	 *               whitespace.
	 */
	public void setIgnoreElementContentWhitespace(boolean ignore) {
		this.ignoreElementContentWhitespace  = ignore;
	}

	/**
	 * Same as <code>setIgnoreNotSpecifiedAttributes(boolean)</code>.
	 * 
	 * @param ignore set it to <code>true</code> to ignore <code>IMPLIED</code>
	 *               attributes.
	 * @deprecated
	 */
	public void setIgnoreImpliedAttributes(boolean ignore) {
		setIgnoreNotSpecifiedAttributes(ignore);
	}

	/**
	 * Configure the builder to ignore (or not) the attributes that were not
	 * <code>specified</code>, when building the document.
	 * 
	 * @param ignore set it to <code>false</code> to set attributes that have a
	 *               default value but were not specified.
	 */
	public void setIgnoreNotSpecifiedAttributes(boolean ignore) {
		this.ignoreImpliedAttributes = ignore;
	}

	/**
	 * Set the <code>strictErrorChecking</code> flag on the documents created by the
	 * DOM implementation.
	 * 
	 * @param strictErrorChecking the value of the <code>strictErrorChecking</code>
	 *                            flag.
	 */
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		this.strictErrorChecking = strictErrorChecking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		strictErrorChecking = true;
		ignoreElementContentWhitespace = false;
		resolver = null;
		errorHandler = null;
	}

	private class MyContentHandler implements ContentHandler, LexicalHandler, ErrorHandler {

		Document document = null;

		private DocumentType documentType = null;

		private LinkedList<MockNode> preDocTypeNodes = null;

		private LinkedList<MockNode> preDocElementNodes = null;

		Node currentNode = null;

		private Locator lastLocator = null;

		private boolean cdata = false;

		private boolean endDTD = true;

		private final boolean ignoreECW;

		final boolean isNativeDOM;

		MyContentHandler() {
			super();
			ignoreECW = ignoreElementContentWhitespace;
			isNativeDOM = domImpl instanceof CSSDOMImplementation;
		}

		Document getDocument() {
			return document;
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			lastLocator = locator;
		}

		@Override
		public void startDocument() throws SAXException {
			document = null;
		}

		@Override
		public void endDocument() throws SAXException {
			currentNode = null;
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			Element element;
			if (document == null) {
				// This is the first element in the document. Are we in XHTML?
				if (!"html".equals(localName) && (HTMLDocument.HTML_NAMESPACE_URI.equals(uri)
						|| (documentType != null && "html".equalsIgnoreCase(documentType.getName())))) {
					// Document is HTML but first element is not <html>:
					String deQname = "html";
					if (!qName.equalsIgnoreCase(localName)) {
						int idx = qName.indexOf(':', 1);
						if (idx != -1) {
							deQname = qName.substring(0, idx) + ":html";
						} else {
							error("Bad qName: " + qName);
						}
					}
					document = createDocument(uri, deQname, documentType);
					element = document.createElementNS(uri, qName);
					setAttributes(element, atts);
					currentNode = document.getDocumentElement().appendChild(element);
				} else {
					document = createDocument(uri, qName, documentType);
					element = document.getDocumentElement();
					currentNode = element;
					setAttributes(element, atts);
				}
				insertPreDocElementNodes();
			} else {
				element = document.createElementNS(uri, qName);
				setAttributes(element, atts);
				appendChild(element);
				currentNode = element;
			}
		}

		void setAttributes(Element element, Attributes atts) throws SAXException {
			int len = atts.getLength();
			for (int i = 0; i < len; i++) {
				Attributes2 atts2 = (Attributes2) atts;
				if (!atts2.isSpecified(i)) {
					if (DOMDocument.XML_NAMESPACE_URI.equals(atts.getURI(i)) && "space".equals(atts.getLocalName(i))
							&& "preserve".equalsIgnoreCase(atts.getValue(i)) && isNativeDOM) {
						((DOMElement) element).setRawText();
					}
					continue;
				}
				String attrQName = atts2.getQName(i);
				Attr attr = document.createAttributeNS(atts2.getURI(i), attrQName);
				attr.setValue(atts2.getValue(i));
				element.getAttributes().setNamedItem(attr);
				if ("ID".equals(atts2.getType(i))) {
					element.setIdAttributeNode(attr, true);
				}
			}
		}

		private void insertPreDocElementNodes() {
			// Pre-docType stuff
			if (preDocTypeNodes != null) {
				Node refNode = documentType;
				if (refNode == null) {
					refNode = document.getDocumentElement();
				}
				Iterator<MockNode> it = preDocTypeNodes.iterator();
				while (it.hasNext()) {
					MockNode mock = it.next();
					insertMockNode(mock, refNode);
				}
			}
			// Pre-docElement stuff
			if (preDocElementNodes != null) {
				Element docElm = document.getDocumentElement();
				Iterator<MockNode> it = preDocElementNodes.iterator();
				while (it.hasNext()) {
					MockNode mock = it.next();
					insertMockNode(mock, docElm);
				}
			}
		}

		private void insertMockNode(MockNode mock, Node refNode) {
			if (mock.getNodeType() == Node.COMMENT_NODE) {
				document.insertBefore(document.createComment(mock.getData()), refNode);
			} else {
				document.insertBefore(document.createProcessingInstruction(
						((MockProcessingInstruction) mock).getTarget(), mock.getData()), refNode);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			currentNode = currentNode.getParentNode();
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			String s = new String(ch, start, length);
			if (currentNode == null) {
				error("Text outside of root element: " + s);
			}
			Node node = currentNode.getLastChild();
			int type;
			if (node == null) {
				type = -1;
			} else {
				type = node.getNodeType();
			}
			if ((!cdata && type == Node.TEXT_NODE) || (cdata && type == Node.CDATA_SECTION_NODE)) {
				Text text = (Text) node;
				text.appendData(s);
			} else if (!cdata) {
				appendChild(document.createTextNode(s));
			} else {
				appendChild(document.createCDATASection(s));
			}
		}

		void appendChild(Node node) throws SAXException {
			try {
				currentNode.appendChild(node);
			} catch (DOMException e) {
				error("Error appending child " + node.getNodeName() + " to " + currentNode.getNodeName(), e);
			}
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			if (!ignoreECW && currentNode != null) {
				String s = new String(ch, start, length);
				Node node = currentNode.getLastChild();
				int type;
				if (node == null) {
					type = -1;
				} else {
					type = node.getNodeType();
				}
				if (cdata && type == Node.CDATA_SECTION_NODE) {
					Text text = (Text) node;
					text.appendData(s);
				} else if (!cdata) {
					appendChild(document.createTextNode(s));
				} else {
					appendChild(document.createCDATASection(s));
				}
			}
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			if (currentNode != null) {
				appendChild(document.createProcessingInstruction(target, data));
			} else if (document != null) {
				document.appendChild(document.createProcessingInstruction(target, data));
			} else if (endDTD) {
				if (documentType == null) {
					if (preDocTypeNodes == null) {
						preDocTypeNodes = new LinkedList<MockNode>();
					}
					preDocTypeNodes.add(new MockProcessingInstruction(target, data));
				} else {
					if (preDocElementNodes == null) {
						preDocElementNodes = new LinkedList<MockNode>();
					}
					preDocElementNodes.add(new MockProcessingInstruction(target, data));
				}
			}
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
		}

		@Override
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
			if (documentType != null || document != null) {
				error("DTD must be the first node in a document (except PIs and comments)");
			}
			documentType = domImpl.createDocumentType(name, publicId, systemId);
			endDTD = false;
		}

		@Override
		public void endDTD() throws SAXException {
			endDTD = true;
		}

		@Override
		public void startEntity(String name) throws SAXException {
		}

		@Override
		public void endEntity(String name) throws SAXException {
		}

		@Override
		public void startCDATA() throws SAXException {
			cdata = true;
		}

		@Override
		public void endCDATA() throws SAXException {
			cdata = false;
		}

		@Override
		public void comment(char[] ch, int start, int length) throws SAXException {
			String comment = new String(ch, start, length);
			if (currentNode != null) {
				appendChild(document.createComment(comment));
			} else if (document != null) {
				document.appendChild(document.createComment(comment));
			} else if (endDTD) {
				if (documentType == null) {
					if (preDocTypeNodes == null) {
						preDocTypeNodes = new LinkedList<MockNode>();
					}
					preDocTypeNodes.add(new MockCommentNode(comment));
				} else {
					if (preDocElementNodes == null) {
						preDocElementNodes = new LinkedList<MockNode>();
					}
					preDocElementNodes.add(new MockCommentNode(comment));
				}
			}
		}

		private void error(String message) throws SAXException {
			if (lastLocator == null) {
				throw new SAXException(message);
			} else {
				throw new SAXParseException(message, lastLocator);
			}
		}

		private void error(String message, Exception ex) throws SAXException {
			if (lastLocator == null) {
				throw new SAXException(message, ex);
			} else {
				throw new SAXParseException(message, lastLocator, ex);
			}
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}

	}

	private class MyContentHandlerImpliedAttr extends MyContentHandler {

		@Override
		void setAttributes(Element element, Attributes atts) throws SAXException {
			int len = atts.getLength();
			for (int i = 0; i < len; i++) {
				Attributes2 atts2 = (Attributes2) atts;
				if (DOMDocument.XML_NAMESPACE_URI.equals(atts.getURI(i)) && "space".equals(atts.getLocalName(i))
						&& "preserve".equalsIgnoreCase(atts.getValue(i)) && element instanceof DOMElement) {
					((DOMElement) element).setRawText();
				}
				String attrQName = atts2.getQName(i);
				Attr attr = document.createAttributeNS(atts2.getURI(i), attrQName);
				attr.setValue(atts2.getValue(i));
				if (isNativeDOM) {
					((DOMAttr) attr).specified = atts2.isSpecified(i);
					if (DOMDocument.XML_NAMESPACE_URI.equals(atts2.getURI(i)) && "space".equals(atts2.getLocalName(i))
							&& "preserve".equalsIgnoreCase(atts2.getValue(i))) {
						((DOMElement) element).setRawText();
					}
				}
				element.getAttributes().setNamedItem(attr);
				if ("ID".equals(atts2.getType(i))) {
					element.setIdAttributeNode(attr, true);
				}
			}
		}

	}

	abstract static class MockNode {

		String data = null;

		MockNode(String data) {
			this.data = data;
		}

		abstract short getNodeType();

		String getData() {
			return data;
		}
	}

	private static class MockCommentNode extends MockNode {

		MockCommentNode(String data) {
			super(data);
		}

		@Override
		short getNodeType() {
			return Node.COMMENT_NODE;
		}

	}

	private static class MockProcessingInstruction extends MockNode {

		String target = null;

		MockProcessingInstruction(String target, String data) {
			super(data);
			this.target = target;
		}

		@Override
		short getNodeType() {
			return Node.PROCESSING_INSTRUCTION_NODE;
		}

		String getTarget() {
			return target;
		}

	}

}
