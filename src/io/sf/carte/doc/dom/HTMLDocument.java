/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.css.CSSStyleDeclaration;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.property.AttributeToStyle;

/**
 * <p>
 * HTML <code>Document</code>.
 * </p>
 */
abstract public class HTMLDocument extends DOMDocument {

	public static final String HTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";

	private URL baseURL = null;

	private String idAttrName = "id";

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

	class MyHTMLElement extends HTMLElement {

		MyHTMLElement(String localName) {
			this(localName, HTMLDocument.HTML_NAMESPACE_URI);
		}

		MyHTMLElement(String localName, String namespaceURI) {
			super(localName, namespaceURI);
		}

		@Override
		boolean isIdAttributeNS(String namespaceURI, String localName) {
			return idAttrName.equals(localName);
		}

		@Override
		public void setIdAttribute(String name, boolean isId) throws DOMException {
			if (!isId || !"id".equalsIgnoreCase(name)) {
				throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Id attribute is always 'id'");
			}
			if (!hasAttribute(name)) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute of this element");
			}
			idAttrName = name;
		}

		@Override
		public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
			if (!hasAttributeNS(namespaceURI, localName)) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute of this element");
			}
			if (namespaceURI != null && namespaceURI.length() > 0
					&& !namespaceURI.equals(HTMLDocument.HTML_NAMESPACE_URI)) {
				return;
			}
			if (isId && !"id".equalsIgnoreCase(localName)) {
				throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Id attribute is always 'id'");
			}
			idAttrName = localName;
		}

		@Override
		public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
			String nsuri = idAttr.getNamespaceURI();
			if (idAttr == null || !hasAttributeNS(nsuri, idAttr.getLocalName())) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute of this element");
			}
			if (nsuri == null || nsuri == HTMLDocument.HTML_NAMESPACE_URI) {
				if (!"id".equalsIgnoreCase(idAttr.getName())) {
					throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Id attribute is always 'id'");
				}
				idAttrName = idAttr.getName();
			}
		}

		@Override
		public void setId(String id) {
			setIdAttribute(id, true);
		}

		@Override
		public String getId() {
			return getAttribute(idAttrName);
		}

		@Override
		public boolean isDefaultNamespace(String namespaceURI) {
			return HTMLDocument.this.isDefaultNamespace(namespaceURI);
		}

		@Override
		public DOMDocument getOwnerDocument() {
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

	class HtmlRootElement extends MyHTMLElement {

		HtmlRootElement() {
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
				if (nname == "head" || nname == "body") {
					Node node = getFirstChild();
					while (node != null) {
						if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName() == nname) {
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
				if (!nname.equalsIgnoreCase(rname) && (nname == "head" || nname == "body")) {
					Node node = getFirstChild();
					while (node != null) {
						if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName() == nname) {
							throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
									"<html> already has a " + nname + " child.");
						}
						node = node.getNextSibling();
					}
				}
			}
		}

	}

	class MetacontentElement extends MyHTMLElement {

		MetacontentElement(String tagName) {
			super(tagName);
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			if (parentNode != null) {
				short type = parentNode.getNodeType();
				if (type != Node.DOCUMENT_FRAGMENT_NODE && (type != Node.ELEMENT_NODE
						|| (parentNode.getNodeName() != "head" && parentNode.getNodeName() != "noscript"))) {
					String msg = "A <" + getNodeName() + "> tag can occur only in a head or noscript element, not in "
							+ parentNode.toString();
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, msg);
				}
			}
			super.setParentNode(parentNode);
		}

	}

	class BaseElement extends MetacontentElement {

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
						if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName() == "base" && node != this) {
							throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
									"A document can have only one base element.");
						}
						node = node.getNextSibling();
					}
				}
			}
			super.setParentNode(parentNode);
		}

	}

	abstract class StyleDefinerElement extends MyHTMLElement implements LinkStyleDefiner {
		AbstractCSSStyleSheet definedSheet = null;
		boolean needsUpdate = true;

		StyleDefinerElement(String localName) {
			super(localName, HTMLDocument.HTML_NAMESPACE_URI);
		}

		@Override
		public void resetLinkedSheet() {
			if (definedSheet != null) {
				definedSheet.getCssRules().clear();
				needsUpdate = true;
				getSheet();
			}
			needsUpdate = true;
			getOwnerDocument().onSheetModify();
		}

	}

	class LinkElement extends StyleDefinerElement {
		LinkElement() {
			super("link");
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
		 * 
		 * @return the associated style sheet for the node, or <code>null</code> if the sheet is
		 *         not CSS or the media attribute was not understood. If the URL is invalid or the
		 *         sheet could not be parsed, the returned sheet will be empty.
		 */
		@Override
		public AbstractCSSStyleSheet getSheet() {
			if (needsUpdate) {
				String rel = getAttribute("rel");
				String type = getAttribute("type");
				int typelen = type.length();
				if (typelen == 0) {
					if (rel.length() == 0) {
						return null;
					}
				} else if (!"text/css".equalsIgnoreCase(type)) {
					return null;
				}
				byte relAttr = AbstractCSSStyleSheet.parseRelAttribute(rel);
				if (relAttr != -1) {
					String title = getAttribute("title").trim();
					if (title.length() == 0) {
						title = null;
					}
					String href = getAttribute("href");
					if (href.length() != 0) {
						if (relAttr == 0) {
							if (loadStyleSheet(href, title)) {
								needsUpdate = false;
							}
						} else {
							if (title != null) {
								if (href.length() != 0) {
									// Disable this alternate sheet if it is a new sheet
									// or is not the selected set
									boolean disable = definedSheet == null
											|| !title.equalsIgnoreCase(getSelectedStyleSheetSet());
									if (loadStyleSheet(href, title)) {
										// It is an alternate sheet
										if (disable) {
											definedSheet.setDisabled(true);
										}
										needsUpdate = false;
									}
								}
							} else {
								getErrorHandler().linkedStyleError(this, "Alternate sheet without title");
							}
						}
					} else {
						getErrorHandler().linkedStyleError(this, "Missing or void href attribute.");
					}
				} else {
					definedSheet = null;
				}
			}
			return definedSheet;
		}

		private boolean loadStyleSheet(String href, String title) {
			MediaQueryList media = parseMediaList(getAttribute("media").trim(), this);
			if (media == null) {
				definedSheet = null;
				return false;
			}
			definedSheet = HTMLDocument.this.loadStyleSheet(definedSheet, href, title, media, this);
			return true;
		}

		@Override
		public HTMLElement cloneNode(boolean deep) {
			return cloneElementNode(new LinkElement(), deep);
		}

	}

	class RawTextElement extends MyHTMLElement {

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

	class StyleElement extends StyleDefinerElement {

		StyleElement() {
			super("style");
		}

		@Override
		boolean isRawText() {
			return true;
		}

		/**
		 * Gets the associated style sheet for the node.
		 * 
		 * @return the associated style sheet for the node, or <code>null</code> if the sheet is
		 *         not CSS or the media attribute was not understood. If the element is empty or
		 *         the sheet could not be parsed, the returned sheet will be empty.
		 */
		@Override
		public AbstractCSSStyleSheet getSheet() {
			if (needsUpdate) {
				String type = getAttribute("type");
				if (!"text/css".equals(type)) {
					return null;
				}
				MediaQueryList mediaList = HTMLDocument.this.parseMediaList(getAttribute("media").trim(), this);
				if (mediaList == null) {
					return null;
				}
				String title = getAttribute("title").trim();
				if (title.length() == 0) {
					title = null;
				}
				String styleText = getTextContent().trim();
				definedSheet = HTMLDocument.this.parseEmbeddedStyleSheet(definedSheet, styleText, title, mediaList, this);
				needsUpdate = false;
			}
			return definedSheet;
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
			resetLinkedSheet();
		}

		@Override
		void postRemoveChild(AbstractDOMNode removed) {
			resetLinkedSheet();
		}

		@Override
		public void setTextContent(String textContent) throws DOMException {
			super.setTextContent(textContent);
			resetLinkedSheet();
		}

		@Override
		public void normalize() {
			if (definedSheet == null) {
				super.normalize();
			} else {
				super.setTextContent(definedSheet.toString());
			}
		}

		@Override
		public HTMLElement cloneNode(boolean deep) {
			return cloneElementNode(new StyleElement(), deep);
		}

	}

	class MetaElement extends MyHTMLElement {

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
			return parentTag == "head" || parentTag == "noscript" || hasAttribute("itemprop");
		}
	}

	class ImgElement extends MyHTMLElement {

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

	class FontElement extends MyHTMLElement {

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

	class TableElement extends MyHTMLElement {

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

	class TableRowElement extends MyHTMLElement {

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

	class TableCellElement extends MyHTMLElement {

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
		localName = localName.intern();
		DOMElement myelem;
		if (namespaceURI == HTMLDocument.HTML_NAMESPACE_URI) {
			if ("link" == localName) {
				myelem = new LinkElement();
			} else if ("style" == localName) {
				myelem = new StyleElement();
			} else if ("meta" == localName) {
				myelem = new MetaElement();
			} else if ("base" == localName) {
				myelem = new BaseElement();
			} else if ("title" == localName) {
				myelem = new MetacontentElement(localName);
			} else if ("html" == localName) {
				myelem = new HtmlRootElement();
			} else if ("img" == localName) {
				myelem = new ImgElement();
			} else if ("font" == localName) {
				myelem = new FontElement();
			} else if ("table" == localName) {
				myelem = new TableElement();
			} else if ("tr" == localName) {
				myelem = new TableRowElement();
			} else if ("td" == localName) {
				myelem = new TableCellElement(localName);
			} else if ("th" == localName) {
				myelem = new TableCellElement(localName);
			} else if ("script" == localName) {
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

	class HrefEventAttr extends MyAttr {

		HrefEventAttr(String namespaceURI) {
			super("href", namespaceURI);
		}

		@Override
		void setAttributeOwner(DOMElement newOwner) {
			super.setAttributeOwner(newOwner);
			if (newOwner != null) {
				onDOMChange(newOwner);
			}
		}

		@Override
		public void setValue(String value) throws DOMException {
			super.setValue(value);
			DOMElement owner = getOwnerElement();
			if (owner != null) {
				onDOMChange(owner);
			}
		}

		void onDOMChange(DOMElement owner) {
			String tagname = owner.getTagName();
			if (tagname == "link") {
				((LinkElement) owner).resetLinkedSheet();
			} else if (tagname == "base") {
				HTMLDocument doc = (HTMLDocument) getOwnerDocument();
				String value = getValue();
				if (value.length() != 0) {
					URL base;
					try {
						base = new URL(value);
					} catch (MalformedURLException e) {
						if (doc != null) {
							doc.baseURL = null;
						}
						return;
					}
					if (doc != null) {
						doc.baseURL = base;
					}
				} else {
					if (doc != null) {
						doc.baseURL = null;
					}
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
		localName = localName.intern();
		Attr my;
		if (namespaceURI == null || namespaceURI == HTMLDocument.HTML_NAMESPACE_URI) {
			if (localName == "class") {
				my = new ClassAttr(namespaceURI);
			} else if (localName == "href") {
				my = new HrefEventAttr(namespaceURI);
			} else if (localName == "style" && prefix == null) {
				my = new MyStyleAttr(localName);
			} else if (localName == "media") {
				my = new StyleEventAttr("media", namespaceURI);
			} else {
				my = new MyAttr(localName, namespaceURI);
			}
		} else if (localName == "xmlns") {
			if (!"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
				throw new DOMException(DOMException.NAMESPACE_ERR, "xmlns local name but not xmlns namespace");
			}
			my = new XmlnsAttr();
		} else {
			my = new MyAttr(localName, namespaceURI);
		}
		if (prefix != null) {
			my.setPrefix(prefix);
		}
		return my;
	}

	/**
	 * Gets the base URL of this Document.
	 * <p>
	 * If the Document's <code>head</code> element has a <code>base</code> child
	 * element, the base URI is computed using the value of the href attribute of
	 * the <code>base</code> element.
	 * 
	 * @return the base URL, or null if no base URL could be found.
	 */
	@Override
	public URL getBaseURL() {
		if (baseURL == null) {
			String buri = getDocumentURI();
			NodeList nl = getElementsByTagName("base");
			if (nl.getLength() != 0) { // This code is probably never called
				Element elm = (Element) nl.item(0);
				String s = elm.getAttribute("href");
				if (s.length() > 0) {
					if (buri != null && s.startsWith("//")) {
						try {
							URL url = new URL(buri);
							url = new URL(url, s);
							buri = url.toExternalForm();
						} catch (MalformedURLException e) {
						}
					} else {
						buri = s;
					}
				}
			}
			try {
				baseURL = new URL(buri);
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
