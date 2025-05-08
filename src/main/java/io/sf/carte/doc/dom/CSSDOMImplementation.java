/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.AnonymousStyleDeclaration;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CompatInlineStyle;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheet;
import io.sf.carte.doc.style.css.om.DOMComputedStyle;
import io.sf.carte.doc.style.css.om.DOMDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.DefaultErrorHandler;
import io.sf.carte.doc.style.css.om.InlineStyle;

/**
 * CSS-enabled DOM implementation.
 */
public class CSSDOMImplementation extends BaseCSSStyleSheetFactory implements DOMImplementation {

	private static final long serialVersionUID = 1L;

	private boolean strictErrorChecking = true;

	private BaseDocumentCSSStyleSheet defStyleSheet = null;
	private BaseDocumentCSSStyleSheet defQStyleSheet = null;

	/**
	 * User-agent style sheet for standards (strict) mode.
	 */
	private BaseDocumentCSSStyleSheet uaStyleSheet = null;

	/**
	 * User-agent style sheet for quirks mode.
	 */
	private BaseDocumentCSSStyleSheet uaQStyleSheet = null;

	/**
	 * Constructs a CSSDOMImplementation that uses a standard (no flags) CSS parser.
	 * 
	 */
	public CSSDOMImplementation() {
		this(EnumSet.noneOf(Parser.Flag.class));
	}

	/**
	 * Constructs a CSSDOMImplementation where the given flags will be applied when parsing
	 * CSS.
	 * <p>
	 * The flags are copied, so further changes to the <code>EnumSet</code> aren't going to be
	 * acknowledged by this implementation.
	 * 
	 * @param parserFlags
	 *            the NSAC parser flags.
	 */
	public CSSDOMImplementation(EnumSet<Parser.Flag> parserFlags) {
		super(parserFlags);
	}

	/**
	 * Creates an empty, plain XML document with no HTML capabilities.
	 * 
	 * @return the XML document.
	 */
	public DOMDocument newDocument() {
		MyXMLDocument doc = new MyXMLDocument(null);
		doc.setStrictErrorChecking(strictErrorChecking);
		return doc;
	}

	/**
	 * Creates an HTML-specific DOM Document with an html element and DOCTYPE.
	 * 
	 * @return the HTML document.
	 */
	public HTMLDocument newHTMLDocument() {
		DocumentType doctype = createDocumentType("html", null, null);
		return (HTMLDocument) createDocument(HTMLDocument.HTML_NAMESPACE_URI, "html", doctype);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMDocument createDocument(String namespaceURI, String qualifiedName,
			DocumentType doctype) throws DOMException {
		if (doctype != null && doctype.getParentNode() != null) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Doctype already in use");
		}
		DOMDocument document;
		if (isHTMLDocument(namespaceURI, qualifiedName, doctype)) {
			document = createHTMLDocument(doctype);
		} else {
			document = createXMLDocument(doctype);
		}
		document.setStrictErrorChecking(strictErrorChecking);
		// Create and append a document element, if provided
		if (qualifiedName != null && qualifiedName.length() != 0) {
			DOMElement docElm = document.createElementNS(namespaceURI, qualifiedName);
			if (docElm.getPrefix() != null && namespaceURI != null) {
				Attr attr = document.createAttributeNS(DOMDocument.XMLNS_NAMESPACE_URI,
						"xmlns:" + docElm.getPrefix());
				attr.setValue(namespaceURI);
				docElm.setAttributeNodeNS(attr);
			}
			document.appendChild(docElm);
		}
		return document;
	}

	protected DOMDocument createXMLDocument(DocumentType doctype) {
		return new MyXMLDocument(doctype);
	}

	protected HTMLDocument createHTMLDocument(DocumentType doctype) {
		return new MyHTMLDocument(doctype);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) throws DOMException {
		if (qualifiedName == null) {
			throw new NullPointerException("Null DTD qName.");
		}
		if (!DOMDocument.isValidName(qualifiedName)) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid qName: " + qualifiedName);
		}
		return new DocumentTypeImpl(qualifiedName, publicId, systemId);
	}

	/**
	 * This method is deprecated and not supported.
	 * 
	 * @param feature ignored.
	 * @param version ignored.
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public Object getFeature(String feature, String version) {
		return null;
	}

	/**
	 * This method is not supported.
	 * 
	 * @param feature ignored.
	 * @param version ignored.
	 * @return Always <code>true</code>.
	 */
	@Override
	public boolean hasFeature(String feature, String version) {
		return true;
	}

	protected boolean getStrictErrorChecking() {
		return strictErrorChecking;
	}

	/**
	 * Set the <code>strictErrorChecking</code> flag on the documents created by this
	 * implementation. Default is {@code true}.
	 * 
	 * @param strictErrorChecking
	 *            the value of the <code>strictErrorChecking</code> flag.
	 */
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		this.strictErrorChecking = strictErrorChecking;
	}

	@Override
	protected BaseDocumentCSSStyleSheet createDocumentStyleSheet(int origin) {
		return new MyDocumentCSSStyleSheet(null, origin);
	}

	@Override
	protected BaseCSSStyleSheet createRuleStyleSheet(AbstractCSSRule ownerRule, String title,
			MediaQueryList mediaList) {
		return new MyCSSStyleSheet(title, null, mediaList, ownerRule, ownerRule.getOrigin());
	}

	@Override
	protected BaseCSSStyleSheet createLinkedStyleSheet(Node ownerNode, String title, MediaQueryList mediaList) {
		NamedNodeMap nnm;
		if (title == null && ownerNode != null && (nnm = ownerNode.getAttributes()) != null) {
			Node titleattr = nnm.getNamedItem("title");
			if (titleattr != null) {
				title = titleattr.getNodeValue();
			}
		}
		return new MyCSSStyleSheet(title, ownerNode, mediaList, null, CSSStyleSheetFactory.ORIGIN_AUTHOR);
	}

	@Override
	public AbstractCSSStyleDeclaration createAnonymousStyleDeclaration(Node node) {
		MyAnonymousStyleDeclaration style = new MyAnonymousStyleDeclaration(node);
		return style;
	}

	/**
	 * Gets the User Agent default CSS style sheet to be used by this factory in the given
	 * mode.
	 * 
	 * @param mode
	 *            the compliance mode.
	 * @return the default style sheet, or an empty sheet if no User Agent sheet was defined.
	 */
	@Override
	public BaseDocumentCSSStyleSheet getUserAgentStyleSheet(CSSDocument.ComplianceMode mode) {
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			if (uaStyleSheet == null) {
				// Create an empty one
				uaStyleSheet = createDocumentStyleSheet(ORIGIN_USER_AGENT);
			}
			return uaStyleSheet;
		}
		if (uaQStyleSheet == null) {
			// Create an empty one
			uaQStyleSheet = createDocumentStyleSheet(ORIGIN_USER_AGENT);
		}
		return uaQStyleSheet;
	}

	@Override
	public void setDefaultHTMLUserAgentSheet() {
		this.uaStyleSheet = htmlDefaultSheet();
		this.uaQStyleSheet = htmlQuirksDefaultSheet();
		defStyleSheet = null;
		defQStyleSheet = null;
	}

	@Override
	protected BaseDocumentCSSStyleSheet getDefaultStyleSheet(CSSDocument.ComplianceMode mode) {
		if (defStyleSheet == null) {
			mergeUserSheets();
		}
		BaseDocumentCSSStyleSheet sheet;
		if (mode == CSSDocument.ComplianceMode.STRICT) {
			sheet = defStyleSheet;
		} else {
			sheet = defQStyleSheet;
		}
		return sheet;
	}

	private void mergeUserSheets() {
		defStyleSheet = getUserAgentStyleSheet(CSSDocument.ComplianceMode.STRICT).clone();
		defQStyleSheet = getUserAgentStyleSheet(CSSDocument.ComplianceMode.QUIRKS).clone();
		AbstractCSSStyleSheet usersheet = getUserNormalStyleSheet();
		if (usersheet != null) {
			defStyleSheet.addStyleSheet(usersheet);
			defQStyleSheet.addStyleSheet(usersheet);
		}
	}

	class MyCSSStyleSheet extends DOMCSSStyleSheet {

		private static final long serialVersionUID = 1L;

		MyCSSStyleSheet(String title, Node ownerNode, MediaQueryList media, AbstractCSSRule ownerRule, int origin) {
			super(title, ownerNode, media, ownerRule, origin);
		}

		@Override
		protected DOMCSSStyleSheet createCSSStyleSheet(String title, Node ownerNode, MediaQueryList media,
				AbstractCSSRule ownerRule, int origin) {
			return new MyCSSStyleSheet(title, ownerNode, media, ownerRule, origin);
		}

		@Override
		public BaseCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

		@Override
		protected void setMedia(MediaQueryList media) throws DOMException {
			super.setMedia(media);
		}

		@Override
		protected void setTitle(String title) {
			super.setTitle(title);
		}

	}

	private class MyDocumentCSSStyleSheet extends DOMDocumentCSSStyleSheet {

		private static final long serialVersionUID = 1L;

		MyDocumentCSSStyleSheet(String medium, int origin) {
			super(medium, origin);
		}

		@Override
		protected DOMDocumentCSSStyleSheet createDocumentStyleSheet(String medium, int origin) {
			return new MyDocumentCSSStyleSheet(medium, origin);
		}

		@Override
		protected ComputedCSSStyle createComputedCSSStyle() {
			return new MyDOMComputedStyle(this);
		}

		@Override
		public BaseCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

	}

	@Override
	protected InlineStyle createInlineStyle(Node owner) {
		InlineStyle style;
		if (!hasCompatValueFlags()) {
			style = new MyInlineStyle(owner);
		} else {
			style = new MyCompatInlineStyle(owner);
		}
		return style;
	}

	class MyInlineStyle extends InlineStyle {

		private static final long serialVersionUID = 1L;

		MyInlineStyle(Node node) {
			super();
			setOwnerNode(node);
		}

		MyInlineStyle(InlineStyle copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

		@Override
		public InlineStyle clone() {
			return new MyInlineStyle(this);
		}

	}

	class MyCompatInlineStyle extends CompatInlineStyle {

		private static final long serialVersionUID = 1L;

		MyCompatInlineStyle(Node node) {
			super();
			setOwnerNode(node);
		}

		MyCompatInlineStyle(CompatInlineStyle copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

		@Override
		public InlineStyle clone() {
			return new MyCompatInlineStyle(this);
		}

	}

	class MyDOMComputedStyle extends DOMComputedStyle {

		private static final long serialVersionUID = 1L;

		MyDOMComputedStyle(BaseDocumentCSSStyleSheet parentSheet) {
			super(parentSheet);
		}

		private MyDOMComputedStyle(ComputedCSSStyle copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

		@Override
		protected void setOwnerNode(CSSElement node) {
			super.setOwnerNode(node);
		}

		@Override
		public ComputedCSSStyle clone() {
			MyDOMComputedStyle styleClone = new MyDOMComputedStyle(this);
			return styleClone;
		}

	}

	class MyAnonymousStyleDeclaration extends AnonymousStyleDeclaration {

		private static final long serialVersionUID = 1L;

		MyAnonymousStyleDeclaration(Node ownerNode) {
			super(ownerNode);
		}

		private MyAnonymousStyleDeclaration(MyAnonymousStyleDeclaration copiedObject) {
			super(copiedObject);
		}

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

		@Override
		public AnonymousStyleDeclaration clone() {
			MyAnonymousStyleDeclaration styleClone = new MyAnonymousStyleDeclaration(this);
			return styleClone;
		}

	}

	class MyHTMLDocument extends HTMLDocument {

		private static final long serialVersionUID = 1L;

		public MyHTMLDocument(DocumentType documentType) {
			super(documentType);
		}

		@Override
		public CSSDOMImplementation getImplementation() {
			return CSSDOMImplementation.this;
		}

		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return url.openConnection();
		}

		@Override
		protected CSSDOMImplementation getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

	}

	class MyXMLDocument extends DOMDocument {

		private static final long serialVersionUID = 1L;

		public MyXMLDocument(DocumentType documentType) {
			super(documentType);
		}

		@Override
		public CSSDOMImplementation getImplementation() {
			return CSSDOMImplementation.this;
		}

		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return url.openConnection();
		}

		@Override
		protected CSSDOMImplementation getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

	}

	/**
	 * Create an error handler.
	 * 
	 * @return the error handler.
	 */
	protected ErrorHandler createErrorHandler() {
		return new MyDefaultErrorHandler();
	}

	class MyDefaultErrorHandler extends DefaultErrorHandler {

		private static final long serialVersionUID = CSSDOMImplementation.serialVersionUID;

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return CSSDOMImplementation.this;
		}

	}

}
