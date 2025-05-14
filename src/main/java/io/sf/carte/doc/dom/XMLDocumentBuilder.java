/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.util.HashSet;
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

	private XMLReader xmlReader = null;

	private EntityResolver resolver = null;

	private ErrorHandler errorHandler = null;

	private boolean strictErrorChecking = false;

	private boolean htmlProcessing = false;

	private boolean ignoreElementContentWhitespace = false;

	private boolean ignoreNotSpecifiedAttributes = true;

	private static final HashSet<String> headChildList;

	static {
		headChildList = new HashSet<>(8);
		headChildList.add("base");
		headChildList.add("link");
		headChildList.add("meta");
		headChildList.add("noscript");
		headChildList.add("script");
		headChildList.add("style");
		headChildList.add("template");
		headChildList.add("title");
	}

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

		if (domImpl instanceof CSSDOMImplementation) {
			this.strictErrorChecking = ((CSSDOMImplementation) domImpl).getStrictErrorChecking();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document parse(InputSource is) throws SAXException, IOException {
		XMLReader xmlReader;
		if (this.xmlReader == null) {
			xmlReader = createXMLReader();
		} else {
			xmlReader = this.xmlReader;
		}
		return parse(is, xmlReader);
	}

	private XMLReader createXMLReader() throws SAXException {
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
			try {
				xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
				xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
			}
		}
		return xmlReader;
	}

	private Document parse(InputSource is, XMLReader xmlReader) throws SAXException, IOException {
		boolean errorHandlerSet = true;

		XMLContentHandler handler;
		if (htmlProcessing) {
			handler = new XHTMLContentHandler(xmlReader);
		} else {
			handler = new XMLContentHandler(xmlReader);
		}
		handler.setSystemId(is.getSystemId());

		xmlReader.setContentHandler(handler);
		try {
			xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
		} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
		}

		if (errorHandler != null) {
			xmlReader.setErrorHandler(errorHandler);
		} else if (xmlReader.getErrorHandler() == null) {
			xmlReader.setErrorHandler(handler);
		} else {
			errorHandlerSet = false;
		}

		xmlReader.parse(is);

		Document document = handler.getDocument();

		// Help memory management
		xmlReader.setContentHandler(null);
		try {
			xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", null);
		} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
		}
		if (errorHandlerSet) {
			xmlReader.setErrorHandler(null);
		}

		return document;
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
		return createDocument("", null, null, null);
	}

	private Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype,
		String systemId) throws DOMException {
		if (doctype != null && "html".equals(doctype.getName()) && namespaceURI != null
			&& namespaceURI.length() == 0) {
			// This is HTML and we do not want to obtain a plain DOMDocument
			namespaceURI = null;
		}
		Document document = domImpl.createDocument(namespaceURI, qualifiedName, doctype);

		document.setStrictErrorChecking(strictErrorChecking);

		document.setDocumentURI(systemId);

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
	 * Configure the builder to process the document as XHTML.
	 * <p>
	 * Default is <code>false</code>.
	 * </p>
	 * 
	 * @param html set it to <code>true</code> to process the document as HTML.
	 */
	public void setHTMLProcessing(boolean html) {
		this.htmlProcessing = html;
	}

	/**
	 * Configure the builder to ignore (or not) element content whitespace when
	 * building the document.
	 * <p>
	 * Default is <code>false</code>.
	 * </p>
	 * 
	 * @param ignore set it to <code>true</code> to ignore element content
	 *               whitespace.
	 */
	public void setIgnoreElementContentWhitespace(boolean ignore) {
		this.ignoreElementContentWhitespace = ignore;
	}

	/**
	 * Configure the builder to ignore (or not) the attributes that were not
	 * <code>specified</code>, when building the document.
	 * <p>
	 * Default is <code>true</code>.
	 * </p>
	 * 
	 * @param ignore set it to <code>false</code> to set attributes that have a
	 *               default value but were not specified.
	 */
	public void setIgnoreNotSpecifiedAttributes(boolean ignore) {
		this.ignoreNotSpecifiedAttributes = ignore;
	}

	/**
	 * Set the <code>strictErrorChecking</code> flag on the documents created by the
	 * DOM implementation.
	 * <p>
	 * Default value is obtained from the DOM implementation if possible,
	 * <code>false</code> otherwise.
	 * </p>
	 * 
	 * @param strictErrorChecking the value of the <code>strictErrorChecking</code>
	 *                            flag.
	 */
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		this.strictErrorChecking = strictErrorChecking;
	}

	/**
	 * Set the {@code XMLReader} to be used when parsing.
	 * <p>
	 * If no {@code XMLReader} is set, one will be created by the
	 * {@code SAXParserFactory}.
	 * </p>
	 * 
	 * @param xmlReader the XMLReader.
	 */
	public void setXMLReader(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		if (domImpl instanceof CSSDOMImplementation) {
			strictErrorChecking = ((CSSDOMImplementation) domImpl).getStrictErrorChecking();
		} else {
			strictErrorChecking = false;
		}
		ignoreElementContentWhitespace = false;
		resolver = null;
		errorHandler = null;
	}

	private class XMLContentHandler implements ContentHandler, LexicalHandler, ErrorHandler {

		String systemId = null;

		Document document = null;

		DocumentType documentType = null;

		private LinkedList<MockNode> preDocTypeNodes = null;

		private LinkedList<MockNode> preDocElementNodes = null;

		Node currentNode = null;

		private Locator lastLocator = null;

		private boolean cdata = false;

		private boolean endDTD = true;

		private final boolean ignoreECW;

		final boolean isNativeDOM;

		final boolean hasAttributes2;

		private final AttributeProcessor attrProcessor;

		XMLContentHandler(XMLReader xmlReader) {
			super();
			ignoreECW = ignoreElementContentWhitespace;
			isNativeDOM = domImpl instanceof CSSDOMImplementation;
			hasAttributes2 = hasAttributes2Feature(xmlReader);
			if (ignoreNotSpecifiedAttributes) {
				attrProcessor = new AttributeProcessor();
			} else {
				attrProcessor = new NotSpecifiedAttributeProcessor();
			}
		}

		void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		private boolean hasAttributes2Feature(XMLReader xmlReader) {
			boolean result;
			try {
				result = xmlReader.getFeature("http://xml.org/sax/features/use-attributes2");
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
				result = false;
			}
			return result;
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
			if (document == null) {
				// This is the first element in the document.
				documentElement(uri, localName, qName, atts);
				insertPreDocElementNodes();
			} else {
				newElement(uri, localName, qName, atts);
			}
		}

		void documentElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			document = createDocument(uri, qName, documentType, systemId);
			Element element = document.getDocumentElement();
			currentNode = element;
			setAttributes(element, atts);
		}

		void newElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			Element element = document.createElementNS(uri, qName);
			setAttributes(element, atts);
			appendChild(element);
			currentNode = element;
		}

		void setAttributes(Element element, Attributes atts) throws SAXException {
			attrProcessor.setAttributes(element, atts);
		}

		private void insertPreDocElementNodes() {
			// Pre-docType stuff
			if (preDocTypeNodes != null) {
				Node refNode = documentType;
				if (refNode == null) {
					refNode = document.getDocumentElement();
				}
				for (MockNode mock : preDocTypeNodes) {
					insertMockNode(mock, refNode);
				}
			}
			// Pre-docElement stuff
			if (preDocElementNodes != null) {
				Element docElm = document.getDocumentElement();
				for (MockNode mock : preDocElementNodes) {
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
						preDocTypeNodes = new LinkedList<>();
					}
					preDocTypeNodes.add(new MockProcessingInstruction(target, data));
				} else {
					if (preDocElementNodes == null) {
						preDocElementNodes = new LinkedList<>();
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
						preDocTypeNodes = new LinkedList<>();
					}
					preDocTypeNodes.add(new MockCommentNode(comment));
				} else {
					if (preDocElementNodes == null) {
						preDocElementNodes = new LinkedList<>();
					}
					preDocElementNodes.add(new MockCommentNode(comment));
				}
			}
		}

		void error(String message) throws SAXException {
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
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}

		private class AttributeProcessor {

			void setAttributes(Element element, Attributes atts) throws SAXException {
				int len = atts.getLength();
				for (int i = 0; i < len; i++) {
					String namespaceURI = atts.getURI(i);
					String value = atts.getValue(i);
					if (isNativeDOM && DOMDocument.XML_NAMESPACE_URI.equals(namespaceURI)
							&& "space".equals(atts.getLocalName(i)) && "preserve".equalsIgnoreCase(value)) {
						((DOMElement) element).setRawText();
					}
					if (hasAttributes2) {
						Attributes2 atts2 = (Attributes2) atts;
						if (!atts2.isSpecified(i)) {
							continue;
						}
					}
					String attrQName = atts.getQName(i);
					Attr attr = document.createAttributeNS(namespaceURI, attrQName);
					attr.setValue(value);
					if (isNativeDOM) {
						((DOMNamedNodeMap<?>) element.getAttributes()).setNamedItemUnchecked(attr);
						// Native DOM does not use setIdAttributeNode() so we continue.
						continue;
					} else {
						element.getAttributes().setNamedItem(attr);
					}
					if ("ID".equals(atts.getType(i))
							|| ("id".equals(attrQName) && element.getNamespaceURI() != document.getNamespaceURI())) {
						element.setIdAttributeNode(attr, true);
					}
				}
			}

		}

		private class NotSpecifiedAttributeProcessor extends AttributeProcessor {

			@Override
			void setAttributes(Element element, Attributes atts) throws SAXException {
				int len = atts.getLength();
				for (int i = 0; i < len; i++) {
					String namespaceURI = atts.getURI(i);
					String value = atts.getValue(i);
					String attrQName = atts.getQName(i);
					Attr attr = document.createAttributeNS(namespaceURI, attrQName);
					attr.setValue(value);
					if (isNativeDOM) {
						if (hasAttributes2) {
							((DOMAttr) attr).specified = ((Attributes2) atts).isSpecified(i);
						}
						if (DOMDocument.XML_NAMESPACE_URI.equals(namespaceURI) && "space".equals(atts.getLocalName(i))
								&& "preserve".equalsIgnoreCase(value)) {
							((DOMElement) element).setRawText();
						}
						((DOMNamedNodeMap<?>) element.getAttributes()).setNamedItemUnchecked(attr);
						// Native DOM does not use setIdAttributeNode() so we continue.
						continue;
					} else {
						element.getAttributes().setNamedItem(attr);
					}
					if ("ID".equals(atts.getType(i))
							|| ("id".equals(attrQName) && element.getNamespaceURI() != document.getNamespaceURI())) {
						element.setIdAttributeNode(attr, true);
					}
				}
			}

		}

	}

	private class XHTMLContentHandler extends XMLContentHandler {

		private boolean headPending = true, bodyPending = true;

		private boolean headImplicit = false, bodyImplicit = false;

		XHTMLContentHandler(XMLReader xmlReader) {
			super(xmlReader);
		}

		@Override
		void documentElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			// We have to create the first element in the document. Are we in XHTML?
			final boolean isXHTML = (HTMLDocument.HTML_NAMESPACE_URI.equals(uri)
					|| (documentType != null && "html".equalsIgnoreCase(documentType.getName())));
			if (!"html".equals(localName) && isXHTML) {
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
				document = createDocument(uri, deQname, documentType, systemId);
				currentNode = document.getDocumentElement();
				newElement(uri, localName, qName, atts);
			} else {
				document = createDocument(uri, qName, documentType, systemId);
				Element element = document.getDocumentElement();
				currentNode = element;
				setAttributes(element, atts);
				if (!isXHTML) {
					headPending = false;
					bodyPending = false;
				}
			}
		}

		@Override
		void newElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			Element element = document.createElementNS(uri, qName);
			setAttributes(element, atts);

			/*
			 * Check implicit parents.
			 */
			// Check for implicit HEAD element
			if (headPending && currentNode.getParentNode() == document) {
				if ("head".equals(qName)) {
					appendChild(element);
					headPending = false;
					currentNode = element;
					return;
				} else if (isHeadChild(qName)) {
					Element head = document.createElementNS(uri, "head");
					currentNode.appendChild(head);
					head.appendChild(element);
					headImplicit = true;
					headPending = false;
					currentNode = element;
					return;
				}
			}

			// Check for implicit BODY element
			if (bodyPending && (currentNode.getParentNode() == document
					|| (headImplicit && currentNode.getParentNode().getParentNode() == document))) {
				if ("body".equals(qName)) {
					if (headImplicit) {
						currentNode = currentNode.getParentNode();
						headImplicit = false;
					} else {
						headPending = false;
					}
					bodyPending = false;
				} else if (!isHeadChild(qName)) {
					if (headImplicit) {
						currentNode = currentNode.getParentNode();
						headImplicit = false;
					} else {
						headPending = false;
					}
					Element body = document.createElementNS(uri, "body");
					currentNode.appendChild(body);
					body.appendChild(element);
					currentNode = element;
					bodyPending = false;
					bodyImplicit = true;
					return;
				}
			}

			appendChild(element);
			currentNode = element;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			currentNode = currentNode.getParentNode();
			if ((bodyImplicit || headImplicit) && "html".equals(currentNode.getNodeName())) {
				currentNode = currentNode.getParentNode();
			}
		}

	}

	private static boolean isHeadChild(String qName) {
		return headChildList.contains(qName);
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
