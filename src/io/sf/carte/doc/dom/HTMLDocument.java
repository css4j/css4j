/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.property.AttributeToStyle;

/**
 * <p>
 * HTML <code>Document</code>.
 * </p>
 */
abstract public class HTMLDocument extends DOMDocument {

	private static final long serialVersionUID = 2L;

	public static final String HTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";

	private URL baseURL = null;

	// Build raw set
	private static final Set<String> rawTextElementsExceptStyle = new HashSet<>(6);

	static {
		/*
		 * We consider as "raw text" both the 'real' raw text (script, style) and elements that
		 * preserve whitespace. We do not include <style> here as it has its own dedicated element.
		 */
		String[] rawText = {"listing", "plaintext", "pre", "script", "textarea", "xmp"};
		Collections.addAll(rawTextElementsExceptStyle, rawText);
	}

	public HTMLDocument(DocumentType documentType) {
		super(documentType);
	}

	@Override
	public HTMLElement getDocumentElement() {
		return (HTMLElement) super.getDocumentElement();
	}

	@Override
	public HTMLDocument getOwnerDocument() {
		return null;
	}

	@Override
	public HTMLDocument cloneNode(boolean deep) {
		return (HTMLDocument) super.cloneNode(deep);
	}

	@Override
	DOMDocument cloneDocument(DocumentType docType) {
		String nsUri = null;
		String qName = null;
		DOMElement docElm = getDocumentElement();
		if (docElm != null) {
			nsUri = docElm.getNamespaceURI();
			qName = docElm.getTagName();
		}
		// We need docType regardless of deep being true, to obtain the right
		// type of Document.
		DOMDocument doc = getImplementation().createDocument(nsUri, qName, docType);
		if (docElm != null) {
			doc.removeChild(doc.getDocumentElement());
		}
		return doc;
	}

	private class MyHTMLElement extends HTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		MyHTMLElement(String localName) {
			this(localName, HTMLDocument.HTML_NAMESPACE_URI);
		}

		MyHTMLElement(String localName, String namespaceURI) {
			super(localName, namespaceURI);
		}

		@Override
		public void setId(String id) {
			setAttribute("id", id);
		}

		@Override
		public boolean isDefaultNamespace(String namespaceURI) {
			return HTMLDocument.this.isDefaultNamespace(namespaceURI);
		}

		@Override
		public HTMLDocument getOwnerDocument() {
			return HTMLDocument.this;
		}

		@Override
		protected BaseCSSStyleSheetFactory getStyleSheetFactory() {
			return HTMLDocument.this.getStyleSheetFactory();
		}

		@Override
		public String getBaseURI() {
			return HTMLDocument.this.getBaseURI();
		}

		@Override
		public HTMLElement cloneNode(boolean deep) {
			return cloneElementNode(new MyHTMLElement(getLocalName(), getNamespaceURI()), deep);
		}

		HTMLElement cloneElementNode(MyHTMLElement my, boolean deep) {
			Iterator<DOMNode> it = this.nodeMap.getNodeList().iterator();
			while (it.hasNext()) {
				Attr attr = (Attr) it.next();
				DOMAttr myattr = (DOMAttr) attr.cloneNode(deep);
				// cloned attributes always have 'specified = true'
				myattr.specified = attr.getSpecified();
				my.setAttributeNode(myattr);
			}
			if (deep) {
				Node node = getFirstChild();
				while (node != null) {
					my.appendChild(node.cloneNode(true));
					node = node.getNextSibling();
				}
			}
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

	}

	private class HtmlRootElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		private HtmlRootElement() {
			super("html");
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			if (parentNode != null) {
				short type = parentNode.getNodeType();
				if (type != Node.DOCUMENT_NODE && type != Node.DOCUMENT_FRAGMENT_NODE) {
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "A <html> tag cannot be added here.");
				}
			}
			super.setParentNode(parentNode);
		}

		@Override
		void preAddChild(Node newChild) {
			super.preAddChild(newChild);
			if (newChild.getNodeType() == Node.ELEMENT_NODE) {
				String nname = newChild.getNodeName();
				if ("head".equals(nname) || "body".equals(nname)) {
					Node node = getFirstChild();
					while (node != null) {
						if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(nname)) {
							throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
									"<html> already has a " + nname + " child.");
						}
						node = node.getNextSibling();
					}
				}
			}
		}

		@Override
		void preReplaceChild(AbstractDOMNode newChild, AbstractDOMNode replaced) {
			super.preAddChild(newChild);
			if (newChild.getNodeType() == Node.ELEMENT_NODE) {
				String nname = newChild.getNodeName();
				String rname = replaced.getNodeName();
				if (!nname.equalsIgnoreCase(rname) && ("head".equals(nname) || "body".equals(nname))) {
					Node node = getFirstChild();
					while (node != null) {
						if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(nname)) {
							throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
									"<html> already has a " + nname + " child.");
						}
						node = node.getNextSibling();
					}
				}
			}
		}

	}

	private class MetacontentElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		MetacontentElement(String tagName) {
			super(tagName);
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			if (parentNode != null) {
				short type = parentNode.getNodeType();
				if (type != Node.DOCUMENT_FRAGMENT_NODE && (type != Node.ELEMENT_NODE
						|| (!"head".equals(parentNode.getNodeName()) && !"noscript".equals(parentNode.getNodeName())))) {
					String msg = "A <" + getNodeName() + "> tag can occur only in a head or noscript element, not in "
							+ parentNode.toString();
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
				}
			}
			super.setParentNode(parentNode);
		}

	}

	private class BaseElement extends MetacontentElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		BaseElement() {
			super("base");
		}

		@Override
		boolean isVoid() {
			return true;
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			if (parentNode != null) {
				super.setParentNode(parentNode);
				if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
					Node node = parentNode.getFirstChild();
					while (node != null) {
						if (node.getNodeType() == Node.ELEMENT_NODE && "base".equals(node.getNodeName())
								&& node != this) {
							throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
									"A document can have only one base element.");
						}
						node = node.getNextSibling();
					}
				}
				// Set the baseURL field to null so it is re-computed
				getOwnerDocument().baseURL = null;
			} else {
				// Set the baseURL field to null so it is re-computed
				getOwnerDocument().baseURL = null;
				//
				super.setParentNode(parentNode);
			}
		}

	}

	private class LinkElement extends MyHTMLElement implements LinkStyleDefiner {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		private final StyleDefinerElementHelper helper;

		LinkElement() {
			super("link");
			helper = new StyleDefinerElementHelper(this);
		}

		@Override
		boolean isVoid() {
			return true;
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			super.setParentNode(parentNode);
			// Rescan of sheets may be required
			onSheetModify();
		}

		/**
		 * Gets the associated style sheet for the node.
		 * <p>
		 * If you have a sheet returned by this method and then modify any attribute of
		 * this element, be sure to call this method again instead of just using the old
		 * sheet.
		 * </p>
		 * 
		 * @return the associated style sheet for the node, or <code>null</code> if the sheet is
		 *         not CSS or the media attribute was not understood. If the URL is invalid or the
		 *         sheet could not be parsed, the returned sheet will be empty.
		 */
		@Override
		public AbstractCSSStyleSheet getSheet() {
			return helper.getLinkedSheet();
		}

		@Override
		public void resetSheet() {
			helper.resetSheet();
		}

		@Override
		public HTMLElement cloneNode(boolean deep) {
			return cloneElementNode(new LinkElement(), deep);
		}

	}

	private class RawTextElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		RawTextElement(String localName, String namespaceURI) {
			super(localName, namespaceURI);
		}

		@Override
		boolean isRawText() {
			return true;
		}

		@Override
		public HTMLElement cloneNode(boolean deep) {
			return cloneElementNode(new RawTextElement(getLocalName(), getNamespaceURI()), deep);
		}

	}

	private class StyleElement extends MyHTMLElement implements LinkStyleDefiner {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		private final StyleDefinerElementHelper helper;

		StyleElement() {
			super("style");
			helper = new StyleDefinerElementHelper(this);
		}

		@Override
		boolean isRawText() {
			return true;
		}

		/**
		 * Gets the associated style sheet for the node.
		 * <p>
		 * If you have a sheet returned by this method and then modify any attribute of
		 * this element, be sure to call this method again instead of just using the old
		 * sheet.
		 * </p>
		 * 
		 * @return the associated style sheet for the node, or <code>null</code> if the
		 *         sheet is not CSS or the media attribute was not understood. If the
		 *         element is empty or the sheet could not be parsed, the returned sheet
		 *         will be empty.
		 */
		@Override
		public AbstractCSSStyleSheet getSheet() {
			return helper.getInlineSheet();
		}

		@Override
		public void resetSheet() {
			helper.resetSheet();
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			super.setParentNode(parentNode);
			// Rescan of sheets may be required
			onSheetModify();
		}

		@Override
		void postAddChild(AbstractDOMNode newChild) {
			super.postAddChild(newChild);
			helper.postAddChildInline(newChild);
		}

		@Override
		void postRemoveChild(AbstractDOMNode removed) {
			resetSheet();
			getSheet();
		}

		@Override
		public void setTextContent(String textContent) throws DOMException {
			super.setTextContent(textContent);
			resetSheet();
			getSheet();
		}

		@Override
		public void normalize() {
			if (!helper.containsCSS()) {
				super.normalize();
			} else {
				// Local reference to sheet, to avoid race conditions.
				final AbstractCSSStyleSheet sheet = getSheet();
				if (sheet != null) {
					super.setTextContent(sheet.toString());
				} else {
					super.normalize();
				}
			}
		}

		@Override
		public HTMLElement cloneNode(boolean deep) {
			return cloneElementNode(new StyleElement(), deep);
		}

	}

	private class MetaElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		MetaElement() {
			super("meta");
		}

		@Override
		boolean isVoid() {
			return true;
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			if (parentNode == null) {
				String name = getAttribute("http-equiv");
				if (name.length() == 0) {
					name = getAttribute("name");
				}
				getOwnerDocument().onMetaRemoved(name, getAttribute("content"));
				super.setParentNode(null);
			} else {
				short type = parentNode.getNodeType();
				if (getStrictErrorChecking() && type != Node.DOCUMENT_FRAGMENT_NODE
						&& (type != Node.ELEMENT_NODE || !isValidContext(parentNode))) {
					String msg = "A <meta> tag can occur only in the head or noscript element, not in "
							+ parentNode.getNodeName();
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
				}
				super.setParentNode(parentNode);
				String name = getAttribute("http-equiv");
				if (name.length() == 0) {
					name = getAttribute("name");
				}
				getOwnerDocument().onMetaAdded(name, getAttribute("content"));
			}
		}

		private boolean isValidContext(Node parentNode) {
			String parentTag = parentNode.getNodeName();
			return "head".equals(parentTag) || "noscript".equals(parentTag) || hasAttribute("itemprop");
		}
	}

	private class ImgElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		ImgElement() {
			super("img");
		}

		@Override
		boolean isVoid() {
			return true;
		}

		@Override
		public boolean hasPresentationalHints() {
			return hasAttribute("width") || hasAttribute("height") || hasAttribute("border") || hasAttribute("hspace")
					|| hasAttribute("vspace");
		}

		@Override
		public void exportHintsToStyle(CSSStyleDeclaration style) {
			AttributeToStyle.width(getAttribute("width"), style);
			AttributeToStyle.height(getAttribute("height"), style);
			AttributeToStyle.hspace(getAttribute("hspace"), style);
			AttributeToStyle.vspace(getAttribute("vspace"), style);
			if (AttributeToStyle.border(getAttribute("border"), style)) {
				style.setProperty("border-top-style", "solid", null);
				style.setProperty("border-right-style", "solid", null);
				style.setProperty("border-bottom-style", "solid", null);
				style.setProperty("border-left-style", "solid", null);
			}
		}

	}

	private class FontElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		FontElement() {
			super("font");
		}

		@Override
		public boolean hasPresentationalHints() {
			return hasAttribute("face") || hasAttribute("size") || hasAttribute("color");
		}

		@Override
		public void exportHintsToStyle(CSSStyleDeclaration style) {
			AttributeToStyle.face(getAttribute("face"), style);
			AttributeToStyle.size(getAttribute("size"), style);
			AttributeToStyle.color(getAttribute("color"), style);
		}

	}

	private class TableElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		TableElement() {
			super("table");
		}

		@Override
		public boolean hasPresentationalHints() {
			return hasAttribute("width") || hasAttribute("height") || hasAttribute("cellspacing")
					|| hasAttribute("border") || hasAttribute("bordercolor") || hasAttribute("bgcolor")
					|| hasAttribute("background");
		}

		@Override
		public void exportHintsToStyle(CSSStyleDeclaration style) {
			AttributeToStyle.bgcolor(getAttribute("bgcolor"), style);
			AttributeToStyle.cellSpacing(getAttribute("cellspacing"), style);
			AttributeToStyle.width(getAttribute("width"), style);
			AttributeToStyle.height(getAttribute("height"), style);
			AttributeToStyle.border(getAttribute("border"), style);
			AttributeToStyle.borderColor(getAttribute("bordercolor"), style);
			AttributeToStyle.background(getAttribute("background"), style);
		}

	}

	private class TableRowElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		TableRowElement() {
			super("tr");
		}

		@Override
		public boolean hasPresentationalHints() {
			return hasAttribute("bgcolor") || hasAttribute("height") || hasAttribute("background")
					|| hasAttribute("align");
		}

		@Override
		public void exportHintsToStyle(CSSStyleDeclaration style) {
			AttributeToStyle.bgcolor(getAttribute("bgcolor"), style);
			AttributeToStyle.height(getAttribute("height"), style);
			AttributeToStyle.background(getAttribute("background"), style);
			AttributeToStyle.align(getAttribute("align"), style);
		}

	}

	private class TableCellElement extends MyHTMLElement {

		private static final long serialVersionUID = HTMLDocument.serialVersionUID;

		TableCellElement(String localName) {
			super(localName);
		}

		TableCellElement(String localName, String namespaceURI) {
			super(localName, namespaceURI);
		}

		@Override
		public boolean hasPresentationalHints() {
			return hasAttribute("bgcolor") || hasAttribute("width") || hasAttribute("height")
					|| hasAttribute("background") || hasAttribute("align");
		}

		@Override
		public void exportHintsToStyle(CSSStyleDeclaration style) {
			AttributeToStyle.bgcolor(getAttribute("bgcolor"), style);
			AttributeToStyle.width(getAttribute("width"), style);
			AttributeToStyle.height(getAttribute("height"), style);
			AttributeToStyle.background(getAttribute("background"), style);
			AttributeToStyle.align(getAttribute("align"), style);
		}

	}

	@Override
	public DOMElement createElement(String tagName) throws DOMException {
		if (tagName == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null tag name");
		}
		return createElementNS(HTMLDocument.HTML_NAMESPACE_URI, tagName);
	}

	@Override
	public DOMElement createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		if (qualifiedName == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null qualified name");
		}
		String localName;
		String prefix;
		if (namespaceURI != null && !namespaceURI.equals(HTMLDocument.HTML_NAMESPACE_URI)
				&& namespaceURI.length() != 0) {
			namespaceURI = namespaceURI.intern();
			int idx = qualifiedName.indexOf(':');
			if (idx == -1) {
				prefix = lookupPrefix(namespaceURI);
				localName = qualifiedName;
			} else if (idx == qualifiedName.length() - 1) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty local name");
			} else if (idx == 0) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty prefix");
			} else {
				prefix = qualifiedName.substring(0, idx).intern();
				localName = qualifiedName.substring(idx + 1);
			}
		} else {
			namespaceURI = HTMLDocument.HTML_NAMESPACE_URI;
			localName = qualifiedName.toLowerCase(Locale.ROOT);
			prefix = null;
		}
		if (!DOMDocument.isValidName(localName)) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid name: " + localName);
		}
		//
		DOMElement myelem;
		if (namespaceURI == HTMLDocument.HTML_NAMESPACE_URI) {
			if ("link".equals(localName)) {
				myelem = new LinkElement();
			} else if ("style".equals(localName)) {
				myelem = new StyleElement();
			} else if ("meta".equals(localName)) {
				myelem = new MetaElement();
			} else if ("base".equals(localName)) {
				myelem = new BaseElement();
			} else if ("title".equals(localName)) {
				myelem = new MetacontentElement(localName);
			} else if ("html".equals(localName)) {
				myelem = new HtmlRootElement();
			} else if ("img".equals(localName)) {
				myelem = new ImgElement();
			} else if ("font".equals(localName)) {
				myelem = new FontElement();
			} else if ("table".equals(localName)) {
				myelem = new TableElement();
			} else if ("tr".equals(localName)) {
				myelem = new TableRowElement();
			} else if ("td".equals(localName)) {
				myelem = new TableCellElement(localName);
			} else if ("th".equals(localName)) {
				myelem = new TableCellElement(localName);
			} else if (rawTextElementsExceptStyle.contains(localName)) {
				myelem = new RawTextElement(localName, namespaceURI);
			} else {
				myelem = new MyHTMLElement(localName, namespaceURI);
			}
		} else {
			myelem = new MyXMLElement(localName, namespaceURI);
		}
		if (prefix != null) {
			myelem.setPrefix(prefix);
		}
		return myelem;
	}

	private class HrefEventAttr extends EventAttr {

		private static final long serialVersionUID = 2L;

		HrefEventAttr(String namespaceURI) {
			super("href", namespaceURI);
		}

		@Override
		void onAttributeRemoval() {
			DOMElement owner = getOwnerElement();
			// In principle, owner cannot be null here
			String tagname = owner.getTagName();
			if ("base".equals(tagname)) {
				if (owner.isDocumentDescendant()) {
					HTMLDocument doc = (HTMLDocument) getOwnerDocument();
					doc.baseURL = null;
					doc.onBaseModify();
				}
			} else if ("link".equals(tagname)) {
				((LinkElement) owner).resetSheet();
			}
		}

		@Override
		void onDOMChange(DOMElement owner) {
			String tagname = owner.getTagName();
			if ("link".equals(tagname)) {
				((LinkElement) owner).resetSheet();
			} else if ("base".equals(tagname)) {
				HTMLDocument doc = (HTMLDocument) getOwnerDocument();
				String value = getValue();
				if (owner.isDocumentDescendant()) {
					if (!setBaseURL(owner, value)) {
						// Set the baseURL field to null so it is re-computed
						doc.baseURL = null;
					}
					onBaseModify();
				}
			}
		}

	}

	@Override
	public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
		if (qualifiedName == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null name");
		}
		String localName = qualifiedName;
		String prefix = null;
		if (namespaceURI != null) {
			if (namespaceURI.length() != 0) {
				namespaceURI = namespaceURI.intern();
				int idx = qualifiedName.indexOf(':');
				if (idx == -1) {
					prefix = lookupPrefix(namespaceURI);
				} else if (idx == qualifiedName.length() - 1) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty local name");
				} else if (idx == 0) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty prefix");
				} else {
					prefix = qualifiedName.substring(0, idx).intern();
					localName = qualifiedName.substring(idx + 1);
				}
				if (HTMLDocument.HTML_NAMESPACE_URI == namespaceURI) {
					localName = localName.toLowerCase(Locale.ROOT);
				}
			} else {
				namespaceURI = null;
				localName = localName.toLowerCase(Locale.ROOT);
			}
		} else if (qualifiedName.indexOf(':') != -1) {
			throw new DOMException(DOMException.NAMESPACE_ERR, "Prefix with null namespace");
		} else {
			localName = localName.toLowerCase(Locale.ROOT);
		}
		if (!DOMDocument.isValidName(localName)) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid name: " + localName);
		}
		Attr my;
		if (namespaceURI == null || namespaceURI == HTMLDocument.HTML_NAMESPACE_URI) {
			if ("class".equals(localName)) {
				my = new ClassAttr(namespaceURI);
			} else if ("href".equals(localName)) {
				my = new HrefEventAttr(namespaceURI);
			} else if ("style".equals(localName)) {
				my = new MyStyleAttr(localName);
			} else if ("media".equals(localName) || "type".equals(localName)) {
				my = new StyleEventAttr(localName, namespaceURI);
			} else {
				my = new MyAttr(localName, namespaceURI);
			}
		} else if ("xmlns".equals(localName)) {
			if (!"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
				throw new DOMException(DOMException.NAMESPACE_ERR, "xmlns local name but not xmlns namespace");
			}
			my = new XmlnsAttr();
		} else if ("style".equals(localName) && prefix == null) {
			my = new MyStyleAttr(localName);
		} else {
			my = new MyAttr(localName, namespaceURI);
		}
		if (prefix != null) {
			my.setPrefix(prefix);
		}
		return my;
	}

	/**
	 * Set the {@code BASE} URL obtained from the {@code href} attribute of the
	 * given &lt;base&gt; element.
	 * 
	 * @param baseElement the &lt;base&gt; element.
	 * @param base        the value of the {@code href} attribute.
	 * @return {@code true} if the {@code BASE} URL was set.
	 */
	private boolean setBaseURL(DOMElement baseElement, String base) {
		if (base.length() != 0) {
			String docUri = getDocumentURI();
			if (docUri != null) {
				URL docUrl;
				try {
					docUrl = new URL(docUri);
				} catch (MalformedURLException e) {
					return setBaseForNullDocumentURI(base, baseElement);
				}
				URL urlBase;
				try {
					urlBase = new URL(docUrl, base);
				} catch (MalformedURLException e) {
					getErrorHandler().ioError(base, e);
					return false;
				}
				String docscheme = docUrl.getProtocol();
				String bscheme = urlBase.getProtocol();
				if (!docscheme.equals(bscheme)) {
					if (!bscheme.equals("https") && !bscheme.equals("http") && !docscheme.equals("file")
							&& !docscheme.equals("jar")) {
						// Remote document wants to set a non-http base URI
						getErrorHandler().policyError(baseElement,
								"Remote document wants to set a non-http base URL: " + urlBase.toExternalForm());
						return false;
					}
				}
				baseURL = urlBase;
				return true;
			} else {
				return setBaseForNullDocumentURI(base, baseElement);
			}
		}
		return false;
	}

	private boolean setBaseForNullDocumentURI(String base, DOMElement baseElement) {
		try {
			URL urlBase = new URL(base);
			String scheme = urlBase.getProtocol();
			if (scheme.equals("https") || scheme.equals("http")) {
				baseURL = urlBase;
				return true;
			}
			// Remote document wants to set a non-http base URL
			getErrorHandler().policyError(baseElement,
					"Untrusted document wants to set a non-http base URL: " + base);
		} catch (MalformedURLException e) {
		}
		return false;
	}

	/**
	 * Gets the base URL of this Document.
	 * <p>
	 * If the Document's <code>head</code> element has a <code>base</code> child
	 * element, the base URI is computed using the value of the href attribute of
	 * the <code>base</code> element.
	 * 
	 * @return the base URL, or {@code null} if no base URL could be found.
	 */
	@Override
	public URL getBaseURL() {
		if (baseURL == null) {
			String docUri = getDocumentURI();
			ElementList headnl = getElementsByTagName("head");
			if (headnl.getLength() != 0) {
				ElementList nl = headnl.item(0).getElementsByTagName("base");
				if (nl.getLength() != 0) {
					DOMElement elm = nl.item(0);
					String s = elm.getAttribute("href");
					if (setBaseURL(elm, s)) {
						return baseURL;
					}
				}
			}
			try {
				baseURL = new URL(docUri);
			} catch (MalformedURLException e) {
			}
		}
		return baseURL;
	}

	/**
	 * Gets the absolute base URI of this node.
	 * 
	 * @return the absolute base URI of this node, or <code>null</code> if an
	 *         absolute URI could not be obtained.
	 */
	@Override
	public String getBaseURI() {
		// The base uri is a field that is obtained through getBaseURL()
		URL url = getBaseURL();
		if (url != null) {
			return url.toExternalForm();
		}
		return null;
	}

	@Override
	public void setDocumentURI(String documentURI) {
		super.setDocumentURI(documentURI);
		baseURL = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		return HTMLDocument.HTML_NAMESPACE_URI.equals(namespaceURI);
	}

	@Override
	LinkStyleDefiner getEmbeddedStyleDefiner(DOMElement element) {
		if (element != null) {
			return element instanceof LinkStyleDefiner ? (LinkStyleDefiner) element : null;
		} else {
			return null;
		}
	}

	@Override
	ElementList getLinkedStyleNodeList() {
		return getElementsByTagName("link");
	}

	@Override
	ElementList getEmbeddedStyleNodeList() {
		return getElementsByTagName("style");
	}

}
