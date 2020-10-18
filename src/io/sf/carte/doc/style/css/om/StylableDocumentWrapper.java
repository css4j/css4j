/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSNode;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * <p>
 * CSS-enabled wrapper for a DOM <code>Document</code>.
 * </p>
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class StylableDocumentWrapper extends DOMNode implements CSSDocument {

	// The raw document.
	private final Document document;

	// Maps original DOM nodes to CSS-enabled nodes.
	private final Map<Node, CSSNode> nodemap = new HashMap<Node, CSSNode>();

	private DocumentCSSStyleSheet mergedStyleSheet = null;

	Set<LinkStyleDefiner> linkedStyle = new LinkedHashSet<LinkStyleDefiner>(4);

	Set<LinkStyleDefiner> embeddedStyle = new LinkedHashSet<LinkStyleDefiner>(3);

	private final StyleSheetList sheets = new MyOMStyleSheetList(7);

	private final ErrorHandler errorHandler = createErrorHandler();

	/*
	 * Default style set according to 'Default-Style' meta.
	 */
	private String metaDefaultStyleSet = "";

	/*
	 * Default referrer policy according to 'Referrer-Policy' header/meta.
	 */
	private String metaReferrerPolicy = "";

	private String lastStyleSheetSet = null;

	private String targetMedium = null;

	private final Map<String, CSSCanvas> canvases = new HashMap<String, CSSCanvas>(3);

	protected StylableDocumentWrapper(Document document) {
		super(document);
		this.document = document;
		updateStyleLists();
	}

	abstract protected DOMCSSStyleSheetFactory getStyleSheetFactory();

	@Override
	public CSSDocument.ComplianceMode getComplianceMode() {
		DocumentType doctype = document.getDoctype();
		if (doctype != null) {
			return CSSDocument.ComplianceMode.STRICT;
		}
		return CSSDocument.ComplianceMode.QUIRKS;
	}

	@Override
	public CSSDocument getOwnerDocument() {
		return null;
	}

	@Override
	protected CSSNode getMappedCSSNode(Node node) {
		return nodemap.get(node);
	}

	@Override
	protected CSSNode getCSSNode(Node node) {
		CSSNode mynode = getMappedCSSNode(node);
		if (mynode == null) {
			switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				Element owner;
				String name = node.getNodeName();
				if ("style".equalsIgnoreCase(name) && node.getPrefix() == null) {
					mynode = new StyleAttr((Attr) node);
				} else if ((owner = ((Attr) node).getOwnerElement()) != null
						&& ("link".equalsIgnoreCase(owner.getTagName()) || "style".equalsIgnoreCase(owner.getTagName()))
						&& ("href".equalsIgnoreCase(name) || "media".equalsIgnoreCase(name)
								|| "title".equalsIgnoreCase(name) || "rel".equalsIgnoreCase(name)
								|| "type".equalsIgnoreCase(name))
						&& node.getPrefix() == null) {
					mynode = new StyleEventAttr((Attr) node);
				} else {
					mynode = new MyAttr((Attr) node);
				}
				break;
			case Node.ELEMENT_NODE:
				Element el = (Element) node;
				name = el.getNodeName();
				if (name != null) {
					name = name.toLowerCase(Locale.ROOT);
					if ("link".equals(name)) {
						mynode = new LinkElement((Element) node);
						// Rescan of sheets may be required
						onStyleModify();
						break;
					}
					if ("style".equals(name)) {
						mynode = new StyleElement((Element) node);
						// Rescan of sheets may be required
						onStyleModify();
						break;
					}
				}
				mynode = new MyElement((Element) node);
				break;
			case Node.TEXT_NODE:
				mynode = new MyText((Text) node);
				break;
			case Node.CDATA_SECTION_NODE:
				mynode = new MyCDATASection((CDATASection) node);
				break;
			case Node.COMMENT_NODE:
				mynode = new MyComment((Comment) node);
				break;
			case Node.DOCUMENT_TYPE_NODE:
				mynode = new MyDocumentType((DocumentType) node);
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				if ("xml-stylesheet".equals(node.getNodeName())) {
					mynode = new MyStyleProcessingInstruction((ProcessingInstruction) node);
					// Rescan of sheets may be required
					onStyleModify();
				} else {
					mynode = new MyProcessingInstruction((ProcessingInstruction) node);
				}
				break;
			case Node.ENTITY_REFERENCE_NODE:
				mynode = new MyEntityReference((EntityReference) node);
				break;
			default:
				mynode = new MyNode(node);
			}
			nodemap.put(node, mynode);
		}
		return mynode;
	}

	@Override
	public DocumentType getDoctype() {
		return (DocumentType) getCSSNode(document.getDoctype());
	}

	@Override
	public DOMImplementation getImplementation() {
		return document.getImplementation();
	}

	@Override
	public boolean isVisitedURI(String href) {
		return false;
	}

	/**
	 * Set the time at which this document was loaded from origin.
	 * 
	 * @param time
	 *            the time of loading, in milliseconds.
	 */
	abstract public void setLoadingTime(long time);

	@Override
	public CSSElement getDocumentElement() {
		Element elm = document.getDocumentElement();
		return elm != null ? (CSSElement) getCSSNode(elm) : null;
	}

	class MyNode extends DOMNode {

		MyNode(Node node) {
			super(node);
		}

		@Override
		public CSSDocument getOwnerDocument() {
			return StylableDocumentWrapper.this;
		}

		@Override
		protected CSSNode getCSSNode(Node node) {
			return StylableDocumentWrapper.this.getCSSNode(node);
		}

		@Override
		protected CSSNode getMappedCSSNode(Node node) {
			return StylableDocumentWrapper.this.getMappedCSSNode(node);
		}

	}

	class MyDocumentType extends MyNode implements DocumentType {

		MyDocumentType(DocumentType node) {
			super(node);
		}

		@Override
		public String getName() {
			return ((DocumentType) rawnode).getName();
		}

		@Override
		public NamedNodeMap getEntities() {
			return ((DocumentType) rawnode).getEntities();
		}

		@Override
		public NamedNodeMap getNotations() {
			return ((DocumentType) rawnode).getNotations();
		}

		@Override
		public String getPublicId() {
			return ((DocumentType) rawnode).getPublicId();
		}

		@Override
		public String getSystemId() {
			return ((DocumentType) rawnode).getSystemId();
		}

		@Override
		public String getInternalSubset() {
			return ((DocumentType) rawnode).getInternalSubset();
		}

	}

	class MyProcessingInstruction extends MyNode implements ProcessingInstruction {

		MyProcessingInstruction(ProcessingInstruction node) {
			super(node);
		}

		@Override
		public String getData() {
			return ((ProcessingInstruction) rawnode).getData();
		}

		@Override
		public String getTarget() {
			return ((ProcessingInstruction) rawnode).getTarget();
		}

		@Override
		public void setData(String data) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

	}

	interface LinkStyleDefiner extends LinkStyle<AbstractCSSRule>, Node {
		@Override
		AbstractCSSStyleSheet getSheet();

		void resetLinkedSheet();
	}

	interface LinkStyleProcessingInstruction extends LinkStyleDefiner, ProcessingInstruction {
		String getPseudoAttribute(String name);
		boolean isSameSheet(String idOrUri);
	}

	class MyStyleProcessingInstruction extends MyProcessingInstruction implements LinkStyleProcessingInstruction {
		private boolean needsUpdate = true;
		private AbstractCSSStyleSheet linkedSheet = null;
		private final LinkedHashMap<String, String> pseudoAttrs = new LinkedHashMap<String, String>();

		MyStyleProcessingInstruction(ProcessingInstruction node) {
			super(node);
			parseData();
		}

		@Override
		public void setData(String data) throws DOMException {
			rawnode.setNodeValue(data);
			parseData();
			resetLinkedSheet();
			if (getParentNode() != null) {
				onStyleModify();
			}
		}

		private void parseData() throws DOMException {
			DOMUtil.parsePseudoAttributes(getData(), pseudoAttrs);
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			setData(nodeValue);
		}

		@Override
		public AbstractCSSStyleSheet getSheet() {
			if (needsUpdate) {
				String type = getPseudoAttribute("type");
				if (type.length() != 0 && !"text/css".equals(type)) {
					return null;
				}
				String title = getPseudoAttribute("title");
				if (title.length() == 0) {
					title = null;
				}
				boolean alternate = "yes".equalsIgnoreCase(getPseudoAttribute("alternate"));
				if (alternate && title == null) {
					getErrorHandler().linkedStyleError(this, "Alternate sheet without title");
					return null;
				}
				// Disable this alternate sheet if it is a new sheet
				// or is not the selected set
				boolean disable = alternate
						&& (linkedSheet == null || !title.equalsIgnoreCase(getSelectedStyleSheetSet()));
				//
				String href = getPseudoAttribute("href");
				int hreflen = href.length();
				if (hreflen > 1) {
					if (href.charAt(0) != '#') {
						linkedSheet = loadStyleSheet(linkedSheet, href, title, getPseudoAttribute("media"), this);
						if (linkedSheet != null) {
							needsUpdate = false;
						}
					} else {
						String id = href.substring(1);
						Element elm = getElementById(id);
						if (elm != null) {
							String text = elm.getTextContent().trim();
							linkedSheet = parseEmbeddedStyleSheet(linkedSheet, text, title, getPseudoAttribute("media"),
									this);
							if (linkedSheet != null) {
								needsUpdate = false;
							}
						} else {
							linkedSheet = null;
							getErrorHandler().linkedStyleError(this,
									"Could not find element with id: " + id);
						}
					}
					if (disable && linkedSheet != null) {
						linkedSheet.setDisabled(true);
					}
				} else {
					getErrorHandler().linkedStyleError(this, "Missing or void href pseudo-attribute.");
				}
			}
			return linkedSheet;
		}

		@Override
		public String getPseudoAttribute(String attrname) {
			String value = pseudoAttrs.get(attrname);
			if (value == null) {
				value = "";
			}
			return value;
		}

		@Override
		public boolean isSameSheet(String idOrUri) {
			String href = getPseudoAttribute("href");
			int hreflen = href.length();
			if (hreflen > 1) {
				if (href.charAt(0) != '#') {
					return href.equals(idOrUri);
				} else {
					return idOrUri.equals(href.substring(1));
				}
			}
			return false;
		}

		@Override
		public void resetLinkedSheet() {
			needsUpdate = true;
			onStyleModify();
		}

	}

	class MyEntityReference extends MyNode implements EntityReference {

		MyEntityReference(EntityReference node) {
			super(node);
		}

	}

	class MyCharacterData extends MyNode implements CharacterData {

		MyCharacterData(CharacterData cdata) {
			super(cdata);
		}

		@Override
		public String getData() throws DOMException {
			return rawnode.getNodeValue();
		}

		@Override
		public void setData(String data) throws DOMException {
			((CharacterData) rawnode).setData(data);
			onDOMChange();
		}

		@Override
		public String getNodeValue() throws DOMException {
			return getData();
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			setData(nodeValue);
		}

		@Override
		public void setTextContent(String textContent) throws DOMException {
			setNodeValue(textContent);
		}

		@Override
		public int getLength() {
			return ((CharacterData) rawnode).getLength();
		}

		@Override
		public String substringData(int offset, int count) throws DOMException {
			return ((CharacterData) rawnode).substringData(offset, count);
		}

		@Override
		public void appendData(String arg) throws DOMException {
			((CharacterData) rawnode).appendData(arg);
			onDOMChange();
		}

		@Override
		public void insertData(int offset, String arg) throws DOMException {
			((CharacterData) rawnode).insertData(offset, arg);
			onDOMChange();
		}

		@Override
		public void deleteData(int offset, int count) throws DOMException {
			((CharacterData) rawnode).deleteData(offset, count);
			onDOMChange();
		}

		@Override
		public void replaceData(int offset, int count, String arg) throws DOMException {
			((CharacterData) rawnode).replaceData(offset, count, arg);
			onDOMChange();
		}

		void onDOMChange() {
		}
	}

	class MyComment extends MyCharacterData implements Comment {

		MyComment(Comment cdata) {
			super(cdata);
		}

		@Override
		public String toString() {
			return "<!--" + getData() + "-->";
		}

	}

	class MyText extends MyCharacterData implements Text {

		MyText(Text text) {
			super(text);
		}

		@Override
		public Text splitText(int offset) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public boolean isElementContentWhitespace() {
			return ((Text) rawnode).isElementContentWhitespace();
		}

		@Override
		public String getWholeText() {
			return ((Text) rawnode).getWholeText();
		}

		@Override
		public Text replaceWholeText(String content) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public String toString() {
			return getData();
		}

		@Override
		void onDOMChange() {
			LinkStyleDefiner definer = getEmbeddedStyleDefiner((CSSElement) getParentNode());
			if (definer != null) {
				definer.resetLinkedSheet();
				definer.getSheet();
			}
		}

	}

	/**
	 * If the given element is a defined container for embedded style, get the definer for
	 * that style.
	 * <p>
	 * If the document is HTML and the <code>element</code> is a <code>style</code> element,
	 * returns itself.
	 * 
	 * @param element
	 *            the candidate for being a container for embedded style. Could be
	 *            <code>null</code>.
	 * @return the definer for an embedded style container, or <code>null</code> if the
	 *         <code>element</code> is not a defined embedded style container.
	 */
	LinkStyleDefiner getEmbeddedStyleDefiner(CSSElement element) {
		LinkStyleDefiner definer = null;
		if (element != null) {
			definer = getEmbeddedStyleDefiner(element.getId());
			if (definer == null && element instanceof LinkStyleDefiner) {
				definer = (LinkStyleDefiner) element;
			}
		}
		return definer;
	}

	class MyCDATASection extends MyText implements CDATASection {

		MyCDATASection(CDATASection cdata) {
			super(cdata);
		}

		@Override
		void onDOMChange() {
			LinkStyleDefiner definer = getEmbeddedStyleDefiner((CSSElement) getParentNode());
			if (definer != null) {
				definer.resetLinkedSheet();
			}
		}

		@Override
		public String toString() {
			return "<![CDATA[" + getData() + "]]>";
		}
	}

	class MyAttr extends MyNode implements Attr {
		MyAttr(Attr attr) {
			super(attr);
		}

		@Override
		public CSSNode getParentNode() {
			return null;
		}

		@Override
		public CSSNode getPreviousSibling() {
			return null;
		}

		@Override
		public CSSNode getNextSibling() {
			return null;
		}

		@Override
		public String getName() {
			return ((Attr) rawnode).getName();
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			setValue(nodeValue);
		}

		@Override
		public boolean getSpecified() {
			return ((Attr) rawnode).getSpecified();
		}

		@Override
		public String getValue() {
			return ((Attr) rawnode).getValue();
		}

		@Override
		public void setValue(String value) throws DOMException {
			((Attr) rawnode).setValue(value);
		}

		@Override
		public CSSElement getOwnerElement() {
			Element elm = ((Attr) rawnode).getOwnerElement();
			if (elm == null) {
				return null;
			}
			return (CSSElement) getCSSNode(elm);
		}

		@Override
		public TypeInfo getSchemaTypeInfo() {
			return ((Attr) rawnode).getSchemaTypeInfo();
		}

		@Override
		public boolean isId() {
			return ((Attr) rawnode).isId();
		}

		@Override
		public String toString() {
			return getName() + "=\"" + getValue() + '"';
		}

	}

	class StyleAttr extends MyAttr {

		private final AbstractCSSStyleDeclaration inlineStyle;

		StyleAttr(Attr attr) {
			super(attr);
			inlineStyle = getStyleSheetFactory().createInlineStyle(this);
			setInlineStyle(attr.getValue());
		}

		@Override
		public String getNodeValue() throws DOMException {
			return getValue();
		}

		@Override
		public String getValue() {
			return inlineStyle.getCssText();
		}

		@Override
		public void setValue(String value) throws DOMException {
			super.setValue(value);
			setInlineStyle(value);
		}

		public AbstractCSSStyleDeclaration getStyle() {
			return inlineStyle;
		}

		void setInlineStyle(String value) {
			if (value == null) {
				value = "";
			}
			try {
				inlineStyle.setCssText(value);
			} catch (DOMException e) {
				getErrorHandler().inlineStyleError(getOwnerElement(), e, value);
			}
		}

	}

	/**
	 * An attribute that changes the meaning of its style-definer owner element.
	 */
	class StyleEventAttr extends MyAttr {

		StyleEventAttr(Attr attr) {
			super(attr);
		}

		@Override
		public void setValue(String value) throws DOMException {
			super.setValue(value);
			onDOMChange(getOwnerElement());
		}

		void onDOMChange(Node ownerNode) {
			if (ownerNode != null && ownerNode instanceof LinkStyleDefiner) {
				((LinkStyleDefiner) ownerNode).resetLinkedSheet();
			}
		}
	}

	class MyElement extends MyNode implements CSSElement {
		private final Element element;

		WeakReference<SelectorMatcher> selectorMatcherRef = null;

		private Map<Condition, InlineStyle> overrideStyleSet = null;

		MyElement(Element element) {
			super(element);
			this.element = element;
		}

		@Override
		public String getTagName() {
			return element.getTagName();
		}

		@Override
		public String getId() {
			return element.getAttribute("id");
		}

		@Override
		public String getAttribute(String name) {
			Attr anode = element.getAttributeNode(name);
			if (anode == null) {
				return "";
			}
			return getCSSNode(anode).getNodeValue();
		}

		@Override
		public void setAttribute(String name, String value) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public void removeAttribute(String name) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public Attr getAttributeNode(String name) {
			Attr anode = element.getAttributeNode(name);
			if (anode == null) {
				return null;
			}
			return (Attr) getCSSNode(anode);
		}

		@Override
		public Attr setAttributeNode(Attr newAttr) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public NodeList getElementsByTagName(String name) {
			return new MyNodeList(element.getElementsByTagName(name));
		}

		@Override
		public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
			return element.getAttributeNS(namespaceURI, localName);
		}

		@Override
		public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
			return element.getAttributeNodeNS(namespaceURI, localName);
		}

		@Override
		public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
			return new MyNodeList(element.getElementsByTagNameNS(namespaceURI, localName));
		}

		@Override
		public boolean hasAttribute(String name) {
			return element.hasAttribute(name);
		}

		@Override
		public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
			return element.hasAttributeNS(namespaceURI, localName);
		}

		@Override
		public TypeInfo getSchemaTypeInfo() {
			return element.getSchemaTypeInfo();
		}

		@Override
		public void setIdAttribute(String name, boolean isId) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
		}

		@Override
		public SelectorMatcher getSelectorMatcher() {
			SelectorMatcher matcher = null;
			if (selectorMatcherRef != null) {
				matcher = selectorMatcherRef.get();
			}
			if (matcher == null) {
				matcher = new DOMSelectorMatcher(this);
				selectorMatcherRef = new WeakReference<SelectorMatcher>(matcher);
			}
			return matcher;
		}

		@Override
		public boolean matches(String selectorString, String pseudoElement) throws DOMException {
			CSSParser parser = new CSSParser();
			SelectorList list;
			try {
				list = parser.parseSelectors(new StringReader(selectorString));
			} catch (Exception e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Unable to parse selector in: " + selectorString);
			}
			Condition peCond;
			if (pseudoElement != null) {
				try {
					peCond = parser.parsePseudoElement(pseudoElement);
				} catch (Exception e) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Unable to parse pseudo-element in: " + pseudoElement);
				}
			} else {
				peCond = null;
			}
			return matches(list, peCond);
		}

		@Override
		public boolean matches(SelectorList selist, Condition pseudoElement) {
			SelectorMatcher matcher = getSelectorMatcher();
			matcher.setPseudoElement(pseudoElement);
			return matcher.matches(selist) != -1;
		}

		@Override
		public CSSStyleDeclaration getStyle() {
			StyleAttr styleAttr = (StyleAttr) getAttributeNode("style");
			if (styleAttr == null) {
				if (StylableDocumentWrapper.this.getComplianceMode() == CSSDocument.ComplianceMode.QUIRKS) {
					NamedNodeMap nmap = element.getAttributes();
					int len = nmap.getLength();
					for (int i = 0; i < len; i++) {
						Node node = nmap.item(i);
						if ("style".equalsIgnoreCase(node.getNodeName())) {
							return ((StyleAttr) getCSSNode(node)).getStyle();
						}
					}
				}
				return null;
			}
			return styleAttr.getStyle();
		}

		@Override
		public boolean hasPresentationalHints() {
			return false;
		}

		@Override
		public void exportHintsToStyle(CSSStyleDeclaration style) {
		}

		@Override
		public boolean hasOverrideStyle(Condition pseudoElt) {
			if (overrideStyleSet == null) {
				return false;
			}
			return overrideStyleSet.containsKey(pseudoElt);
		}

		@Override
		public CSSStyleDeclaration getOverrideStyle(Condition pseudoElt) {
			InlineStyle overrideStyle = null;
			if (overrideStyleSet == null) {
				overrideStyleSet = new HashMap<Condition, InlineStyle>(1);
			} else {
				overrideStyle = overrideStyleSet.get(pseudoElt);
			}
			if (overrideStyle == null) {
				overrideStyle = getStyleSheetFactory().createInlineStyle(this);
				overrideStyleSet.put(pseudoElt, overrideStyle);
			}
			return overrideStyle;
		}

		/**
		 * Gets the computed style declaration that applies to this element.
		 * 
		 * @param pseudoElt
		 *            the pseudo-element name.
		 * @return the computed style declaration.
		 */
		@Override
		public ComputedCSSStyle getComputedStyle(String pseudoElt) {
			Condition peCond;
			if (pseudoElt != null) {
				CSSParser parser = new CSSParser();
				peCond = parser.parsePseudoElement(pseudoElt);
			} else {
				peCond = null;
			}
			return (ComputedCSSStyle) getStyleSheet().getComputedStyle(this, peCond);
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(128);
			buf.append('<').append(getTagName());
			NamedNodeMap nodeMap = getAttributes();
			int len = nodeMap.getLength();
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					Node node = nodeMap.item(i);
					buf.append(' ');
					buf.append(node.getNodeName()).append("='")
						.append(node.getNodeValue());
				}
			}
			if (hasChildNodes()) {
				buf.append('>');
				NodeList list = getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
					buf.append(list.item(i).toString());
				}
				buf.append("</").append(getTagName()).append('>').append('\n');
			} else {
				buf.append(" />");
			}
			return buf.toString();
		}
	}

	abstract class StyleDefinerElement extends MyElement implements LinkStyleDefiner {
		AbstractCSSStyleSheet definedSheet = null;
		boolean needsUpdate = true;

		StyleDefinerElement(Element element) {
			super(element);
		}

		@Override
		public void resetLinkedSheet() {
			if (definedSheet != null) {
				definedSheet.getCssRules().clear();
			}
			needsUpdate = true;
			onStyleModify();
		}

	}

	class LinkElement extends StyleDefinerElement {
		LinkElement(Element element) {
			super(element);
		}

		/**
		 * Gets the associated style sheet for the node.
		 * 
		 * @return the associated style sheet for the node, or <code>null</code> if there is no
		 *         associated style sheet, or the sheet could not be read or parsed.
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
					String title = getAttribute("title");
					if (title.length() == 0) {
						title = null;
					}
					String href = getAttribute("href");
					if (href.length() != 0) {
						if (relAttr == 0) {
							if (loadDefinedSheet(href, title)) {
								needsUpdate = false;
							}
						} else {
							if (title != null) {
								if (href.length() != 0) {
									// Disable this alternate sheet if it is a new sheet
									// or is not the selected set
									boolean disable = definedSheet == null
											|| !title.equalsIgnoreCase(getSelectedStyleSheetSet());
									if (loadDefinedSheet(href, title)) {
										// It is an alternate sheet
										if (disable) {
											definedSheet.setDisabled(true);
										}
										needsUpdate = false;
									}
								}
							} else {
								getErrorHandler().linkedStyleError(this,
										"Alternate sheet without title.");
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

		private boolean loadDefinedSheet(String href, String title) {
			definedSheet = loadStyleSheet(definedSheet, href, title, getAttribute("media"), this);
			return definedSheet != null;
		}

	}

	class StyleElement extends StyleDefinerElement {

		StyleElement(Element element) {
			super(element);
		}

		/**
		 * Gets the associated style sheet for the node.
		 * 
		 * @return the associated style sheet for the node, or <code>null</code> if there is no
		 *         associated style sheet, or the sheet could not be read or parsed.
		 */
		@Override
		public AbstractCSSStyleSheet getSheet() {
			if (needsUpdate) {
				if (hasAttribute("type") && !"text/css".equalsIgnoreCase(getAttribute("type"))) {
					return null;
				}
				definedSheet = parseEmbeddedStyleSheet(definedSheet, getTextContent().trim(), getAttribute("title"),
						getAttribute("media"), this);
				if (definedSheet != null) {
					needsUpdate = false;
				}
			}
			return definedSheet;
		}

		@Override
		public void resetLinkedSheet() {
			needsUpdate = true;
			onStyleModify();
		}

	}

	private MediaQueryList parseMediaList(String media, Node ownerNode) {
		MediaQueryList mediaList;
		if (media.length() == 0) {
			mediaList = new MediaQueryListImpl().unmodifiable();
		} else {
			mediaList = getStyleSheetFactory().createMediaQueryList(media, ownerNode);
			if (mediaList.isNotAllMedia() && mediaList.hasErrors()) {
				return null;
			}
		}
		return mediaList;
	}

	private AbstractCSSStyleSheet parseEmbeddedStyleSheet(AbstractCSSStyleSheet sheet, String styleText, String title,
			String media, Node ownerNode) {
		MediaQueryList mediaList = parseMediaList(media.trim(), ownerNode);
		if (mediaList != null) {
			if (sheet == null) {
				sheet = getStyleSheetFactory().createLinkedStyleSheet(ownerNode, title, mediaList);
			} else {
				sheet.getCssRules().clear();
				sheet.setTitle(title);
				sheet.setMedia(mediaList);
			}
			if (styleText.length() != 0) {
				sheet.setHref(getBaseURI());
				Reader re = new StringReader(styleText);
				try {
					sheet.parseStyleSheet(re);
				} catch (Exception e) {
					getErrorHandler().linkedSheetError(e, sheet);
				}
			} else {
				sheet.getCssRules().clear();
			}
			return sheet;
		} else {
			return null;
		}
	}

	private AbstractCSSStyleSheet loadStyleSheet(AbstractCSSStyleSheet sheet, String href, String title, String media, Node ownerNode) {
		MediaQueryList mediaList = parseMediaList(media.trim(), ownerNode);
		if (mediaList != null) {
			String referrerPolicy = getReferrerpolicyAttribute(ownerNode);
			if (sheet == null) {
				sheet = getStyleSheetFactory().createLinkedStyleSheet(ownerNode, title, mediaList);
			} else {
				sheet.setTitle(title);
				sheet.setMedia(mediaList);
				sheet.getCssRules().clear();
			}
			try {
				URL url = getURL(href);
				if (isAuthorizedOrigin(url)) {
					sheet.setHref(url.toExternalForm());
					sheet.loadStyleSheet(url, referrerPolicy);
				} else {
					getErrorHandler().policyError(ownerNode, "Unauthorized URL: " + url.toExternalForm());
				}
			} catch (Exception e) {
				getErrorHandler().linkedSheetError(e, sheet);
			}
			return sheet;
		} else {
			return null;
		}
	}

	private String getReferrerpolicyAttribute(Node node) {
		NamedNodeMap nnm = node.getAttributes();
		if (nnm != null) {
			Node rp = nnm.getNamedItem("referrerpolicy");
			if (rp != null) {
				return rp.getNodeValue();
			}
		}
		return "";
	}

	@Override
	public CSSElement createElement(String tagName) throws DOMException {
		return (CSSElement) getCSSNode(document.createElement(tagName));
	}

	@Override
	public DocumentFragment createDocumentFragment() {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public Text createTextNode(String data) {
		return (Text) getCSSNode(document.createTextNode(data));
	}

	@Override
	public Comment createComment(String data) {
		return (Comment) getCSSNode(document.createComment(data));
	}

	@Override
	public CDATASection createCDATASection(String data) throws DOMException {
		return (CDATASection) getCSSNode(document.createCDATASection(data));
	}

	@Override
	public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public Attr createAttribute(String name) throws DOMException {
		return (Attr) getCSSNode(document.createAttribute(name));
	}

	@Override
	public EntityReference createEntityReference(String name) throws DOMException {
		return (EntityReference) getCSSNode(document.createEntityReference(name));
	}

	@Override
	public Node importNode(Node importedNode, boolean deep) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public CSSElement createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return (CSSElement) getCSSNode(document.createElementNS(namespaceURI, qualifiedName));
	}

	@Override
	public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
		return (Attr) getCSSNode(document.createAttributeNS(namespaceURI, qualifiedName));
	}

	@Override
	public NodeList getElementsByTagName(String tagname) {
		return new MyNodeList(document.getElementsByTagName(tagname));
	}

	@Override
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		return new MyNodeList(document.getElementsByTagNameNS(namespaceURI, localName));
	}

	@Override
	public CSSElement getElementById(String elementId) {
		Element elm = document.getElementById(elementId);
		return elm != null ? (CSSElement) getCSSNode(elm) : null;
	}

	@Override
	public String getInputEncoding() {
		return document.getInputEncoding();
	}

	@Override
	public String getXmlEncoding() {
		return document.getXmlEncoding();
	}

	@Override
	public boolean getXmlStandalone() {
		return document.getXmlStandalone();
	}

	@Override
	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public String getXmlVersion() {
		return document.getXmlVersion();
	}

	@Override
	public void setXmlVersion(String xmlVersion) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public boolean getStrictErrorChecking() {
		return document.getStrictErrorChecking();
	}

	@Override
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public String getDocumentURI() {
		return document.getDocumentURI();
	}

	@Override
	public void setDocumentURI(String documentURI) {
		document.setDocumentURI(documentURI);
		onStyleModify();
	}

	@Override
	public Node adoptNode(Node source) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public DOMConfiguration getDomConfig() {
		return document.getDomConfig();
	}

	@Override
	public void normalizeDocument() {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This is a readonly wrapper.");
	}

	/**
	 * A list containing all the style sheets explicitly linked into or embedded
	 * in a document. For HTML documents, this includes external style sheets,
	 * included via the HTML LINK element, and inline STYLE elements. In XML,
	 * this includes external style sheets, included via style sheet processing
	 * instructions (see [XML StyleSheet]).
	 */
	@Override
	public StyleSheetList getStyleSheets() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		return sheets;
	}

	void updateStyleLists() {
		linkedStyle.clear();
		embeddedStyle.clear();
		updateStyleFromProcessingInstructions();
		if ((linkedStyle.isEmpty() && embeddedStyle.isEmpty()) || isHTML()) {
			// Try to load sheets in the old way
			updateStyleFromHTMLElements();
		}
		/*
		 * Add the linked and embedded styles. Must be added in this order, as
		 * mandated by the CSS spec.
		 */
		// Add styles referenced by links
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			addLinkedSheet(links.next().getSheet());
		}
		// Add embedded styles
		Iterator<LinkStyleDefiner> embd = embeddedStyle.iterator();
		while (embd.hasNext()) {
			addLinkedSheet(embd.next().getSheet());
		}
		sheets.setNeedsUpdate(false);
		if (lastStyleSheetSet != null) {
			setSelectedStyleSheetSet(lastStyleSheetSet);
		} else if ((metaDefaultStyleSet = getMetaDefaultStyleSet()).length() > 0) {
			setSelectedStyleSheetSet(metaDefaultStyleSet);
			lastStyleSheetSet = null;
		} else {
			setSelectedStyleSheetSet(sheets.getPreferredStyleSheetSet());
			lastStyleSheetSet = null;
		}
		if (getCanvas() != null) {
			getCanvas().reloadStyleState();
		}
	}

	private void updateStyleFromProcessingInstructions() {
		NodeList child = document.getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node = child.item(i);
			short type = node.getNodeType();
			if (type == Node.PROCESSING_INSTRUCTION_NODE && "xml-stylesheet".equals(node.getNodeName())) {
				CSSNode mynode = getMappedCSSNode(node);
				if (mynode == null) {
					mynode = new MyStyleProcessingInstruction((ProcessingInstruction) node);
					nodemap.put(node, mynode);
				}
				LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) mynode;
				String href = pi.getPseudoAttribute("href");
				if (href.length() > 1) {
					if (href.charAt(0) == '#') {
						embeddedStyle.add(pi);
					} else {
						linkedStyle.add(pi);
					}
				}
			} else if(type == Node.ELEMENT_NODE) {
				break;
			}
		}
	}

	private boolean isHTML() {
		Element docelm = document.getDocumentElement();
		return docelm != null && "html".equalsIgnoreCase(docelm.getTagName());
	}

	private void updateStyleFromHTMLElements() {
		// Find the linked styles
		NodeList nl = document.getElementsByTagName("link");
		int len = nl.getLength();
		for (int i = 0; i < len; i++) {
			Node n = nl.item(i);
			if (!linkedStyle.isEmpty()) {
				// Check whether we already have the sheet
				if (isAlreadyLoaded(linkedStyle, ((Element) n).getAttribute("href"))) {
					continue;
				}
			}
			CSSNode mynode = getMappedCSSNode(n);
			if (mynode == null) {
				mynode = new LinkElement((Element) n);
				nodemap.put(n, mynode);
			}
			LinkStyleDefiner link = (LinkStyleDefiner) mynode;
			if (link.getSheet() != null) {
				linkedStyle.add(link);
			}
		}
		// Find the embedded styles
		nl = document.getElementsByTagName("style");
		len = nl.getLength();
		for (int i = 0; i < len; i++) {
			Node n = nl.item(i);
			if (!embeddedStyle.isEmpty()) {
				// Check whether we already have the sheet
				if (isAlreadyLoaded(embeddedStyle, ((Element) n).getAttribute("id"))) {
					continue;
				}
			}
			CSSNode mynode = getMappedCSSNode(n);
			if (mynode == null) {
				mynode = new StyleElement((Element) n);
				nodemap.put(n, mynode);
			}
			embeddedStyle.add((LinkStyleDefiner) mynode);
		}
	}

	private boolean isAlreadyLoaded(Set<LinkStyleDefiner> definerSet, String href) {
		Iterator<LinkStyleDefiner> it = definerSet.iterator();
		while (it.hasNext()) {
			LinkStyleDefiner definer = it.next();
			if (definer instanceof LinkStyleProcessingInstruction) {
				if (((LinkStyleProcessingInstruction) definer).isSameSheet(href)) {
					return true;
				}
			}
		}
		return false;
	}

	private void addLinkedSheet(AbstractCSSStyleSheet linkedSheet) {
		if (linkedSheet != null) {
			sheets.add(linkedSheet);
		}
	}

	private LinkStyleDefiner getEmbeddedStyleDefiner(String id) {
		NodeList child = getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node = child.item(i);
			short type = node.getNodeType();
			if (type == Node.PROCESSING_INSTRUCTION_NODE && "xml-stylesheet".equals(node.getNodeName())) {
				LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) node;
				String href = pi.getPseudoAttribute("href");
				if (href.length() > 1 && href.charAt(0) == '#' && id.equals(href.substring(1))) {
					return pi;
				}
			} else if (type == Node.ELEMENT_NODE) {
				break;
			}
		}
		return null;
	}

	private String getMetaDefaultStyleSet() {
		NodeList headlist = document.getElementsByTagName("head");
		if (headlist.getLength() != 0) {
			Element head = (Element) headlist.item(0);
			NodeList metalist = head.getElementsByTagName("meta");
			for (int i = 0; i < metalist.getLength(); i++) {
				Element meta = (Element) metalist.item(i);
				NamedNodeMap nnm = meta.getAttributes();
				String content = null;
				String httpEquiv = null;
				for (int j = 0; j < nnm.getLength(); j++) {
					Node attr = nnm.item(j);
					if ("http-equiv".equalsIgnoreCase(attr.getNodeName())) {
						if (httpEquiv != null) {
							// Error
							break;
						}
						httpEquiv = attr.getNodeValue();
					} else if ("content".equalsIgnoreCase(attr.getNodeName())) {
						if (content != null) {
							// Error
							break;
						}
						content = attr.getNodeValue();
					}
				}
				if (httpEquiv != null && content != null && "default-style".equalsIgnoreCase(httpEquiv)) {
					return content;
				}
			}
		}
		return "";
	}

	/**
	 * Gets the merged style sheet that applies to this document, resulting from the merge of
	 * the document's default style sheet, the document linked or embedded style sheets, and
	 * the non-important part of the user style sheet. Does not include overriden styles nor
	 * the 'important' part of the user-defined style sheet.
	 * <p>
	 * The style sheet is lazily built.
	 * 
	 * @return the merged style sheet that applies to this document.
	 */
	@Override
	public DocumentCSSStyleSheet getStyleSheet() {
		if (mergedStyleSheet == null) {
			mergeStyleSheets();
		}
		return mergedStyleSheet;
	}

	private void mergeStyleSheets() {
		getStyleSheets(); // Make sure that sheets is up to date
		DocumentCSSStyleSheet defSheet = getStyleSheetFactory().getDefaultStyleSheet(getComplianceMode());
		if (targetMedium == null) {
			mergedStyleSheet = defSheet.clone();
		} else {
			mergedStyleSheet = defSheet.clone(targetMedium);
		}
		((BaseDocumentCSSStyleSheet) mergedStyleSheet).setOwnerDocument(this);
		// Add styles referenced by link and style elements
		Iterator<AbstractCSSStyleSheet> it = sheets.iterator();
		while (it.hasNext()) {
			mergedStyleSheet.addStyleSheet(it.next());
		}
	}

	/**
	 * Gets the list of available alternate styles.
	 * 
	 * @return the list of available alternate style titles.
	 */
	@Override
	public DOMStringList getStyleSheetSets() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		return sheets.getStyleSheetSets();
	}

	/**
	 * Gets the title of the currently selected style sheet set.
	 * 
	 * @return the title of the currently selected style sheet, the empty string
	 *         if none is selected, or <code>null</code> if there are style
	 *         sheets from different style sheet sets that have their style
	 *         sheet disabled flag unset.
	 */
	@Override
	public String getSelectedStyleSheetSet() {
		String selectedSetName = "";
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			CSSStyleSheet sheet = links.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0) {
				if (!sheet.getDisabled()) {
					if (selectedSetName.length() > 0) {
						if (!selectedSetName.equalsIgnoreCase(title)) {
							return null;
						}
					} else {
						selectedSetName = title;
					}
				}
			}
		}
		return selectedSetName;
	}

	/**
	 * Selects a style sheet set, disabling the other non-persistent sheet sets.
	 * If the name is the empty string, all non-persistent sheets will be
	 * disabled. Otherwise, if the name does not match any of the sets, does
	 * nothing.
	 * 
	 * @param name
	 *            the case-sensitive name of the set to select.
	 */
	@Override
	public void setSelectedStyleSheetSet(String name) {
		if (name == null || (name.length() > 0 && !getStyleSheetSets().contains(name))) {
			return;
		}
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			String title;
			CSSStyleSheet sheet = links.next().getSheet();
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() != 0) {
				if (title.equalsIgnoreCase(name)) {
					sheet.setDisabled(false);
					lastStyleSheetSet = name;
				} else {
					sheet.setDisabled(true);
				}
			}
		}
	}

	/**
	 * Gets the style sheet set that was last selected.
	 * 
	 * @return the last selected style sheet set, or <code>null</code> if none.
	 */
	@Override
	public String getLastStyleSheetSet() {
		return lastStyleSheetSet;
	}

	/**
	 * Enables a style sheet set. If the name does not match any of the sets,
	 * does nothing.
	 * 
	 * @param name
	 *            the case-sensitive name of the set to enable.
	 */
	@Override
	public void enableStyleSheetsForSet(String name) {
		if (name == null || name.length() == 0) {
			return;
		}
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			CSSStyleSheet sheet = links.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0) {
				if (title.equals(name)) {
					sheet.setDisabled(false);
				}
			}
		}
	}

	/**
	 * Notifies the document about any change in style.
	 * 
	 */
	void onStyleModify() {
		if (mergedStyleSheet != null) {
			mergedStyleSheet = null;
			sheets.setNeedsUpdate(true);
		} else if (sheets != null) {
			sheets.setNeedsUpdate(true);
		}
	}

	/**
	 * Gets the style database currently used to apply specific styles to this
	 * document.
	 * 
	 * @return the style database, or null if no style database has been
	 *         selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		StyleDatabase sdb = null;
		if (targetMedium != null) {
			DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
			if (df != null) {
				sdb = df.getStyleDatabase(targetMedium);
			}
		}
		return sdb;
	}

	/**
	 * This document's current target medium name.
	 * 
	 * @return the target medium name of this document.
	 */
	@Override
	public String getTargetMedium() {
		return targetMedium;
	}

	/**
	 * Set the medium that will be used to compute the styles of this document.
	 * 
	 * @param medium
	 *            the name of the target medium, like 'screen' or 'print'.
	 * @throws CSSMediaException
	 *             if the document is unable to target the given medium.
	 */
	@Override
	public void setTargetMedium(String medium) throws CSSMediaException {
		medium = medium.intern();
		if ("all".equals(medium)) {
			targetMedium = null;
		} else {
			targetMedium = medium;
		}
		onStyleModify();
	}

	/**
	 * Gets the document's canvas for the current target medium.
	 * 
	 * @return the canvas, or null if no target medium has been set, or the
	 *         DeviceFactory does not support canvas for the target medium.
	 */
	@Override
	public CSSCanvas getCanvas() {
		if (targetMedium == null) {
			return null;
		}
		if (canvases.containsKey(targetMedium)) {
			return canvases.get(targetMedium);
		}
		CSSCanvas canvas;
		DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
		if (df != null) {
			canvas = df.createCanvas(targetMedium, this);
			canvases.put(targetMedium, canvas);
		} else {
			canvas = null;
		}
		return canvas;
	}

	ErrorHandler createErrorHandler() {
		return new MyDefaultErrorHandler();
	}

	class MyDefaultErrorHandler extends DefaultErrorHandler {

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return StylableDocumentWrapper.this.getStyleSheetFactory();
		}

	}

	@Override
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Has any of the linked or embedded style sheets any error or warning ?
	 * 
	 * @return <code>true</code> if any of the linked or embedded style sheets has any NSAC or rule error
	 *         or warning, <code>false</code> otherwise.
	 */
	@Override
	public boolean hasStyleIssues() {
		return sheets.hasErrorsOrWarnings() || getErrorHandler().hasErrors() || getErrorHandler().hasWarnings();
	}

	/**
	 * Gets the base URL of this Document.
	 * <p>
	 * If the Document's <code>head</code> element has a <code>base</code> child
	 * element, the base URI is computed using the value of the href attribute
	 * of the <code>base</code> element. Otherwise, it is taken from the value
	 * of the <code>getDocumentURI</code> method.
	 * 
	 * @return the base URL, or null if no base URL could be determined.
	 */
	@Override
	public URL getBaseURL() {
		URL url = null;
		String buri = getBaseURI();
		if (buri != null) {
			try {
				url = new URL(buri);
			} catch (MalformedURLException e) {
				try {
					String docuri = document.getDocumentURI();
					if (docuri != null) {
						URL context = new URL(docuri);
						url = new URL(context, buri);
					}
				} catch (MalformedURLException e1) {
				}
			}
		}
		return url;
	}

	@Override
	public String getBaseURI() {
		String buri = null;
		Element elm = getDocumentElement();
		if (elm != null) {
			String attr = elm.getAttribute("xml:base");
			if (attr.length() != 0) {
				buri = attr;
			} else if ("html".equalsIgnoreCase(elm.getTagName())) {
				NodeList nl = document.getElementsByTagName("base");
				if (nl.getLength() != 0) {
					elm = (Element) nl.item(0);
					String s = elm.getAttribute("href").trim();
					if (s.length() != 0) {
						buri = s;
					} else if ((s = elm.getAttribute("HREF")).length() != 0) {
						buri = s;
					}
				}
			}
		}
		if (buri != null) {
			String docUri = document.getDocumentURI();
			if (docUri != null) {
				// Relative url
				URL docUrl;
				try {
					docUrl = new URL(docUri);
				} catch (MalformedURLException e) {
					docUrl = null;
				}
				URL bUrl;
				try {
					if (docUrl != null) {
						bUrl = new URL(docUrl, buri);
					} else {
						bUrl = new URL(buri);
					}
				} catch (MalformedURLException e) {
					return docUrl != null ? docUrl.toExternalForm() : null;
				}
				buri = bUrl.toExternalForm();
				String docscheme = docUrl.getProtocol();
				String bscheme = bUrl.getProtocol();
				if (!docscheme.equals(bscheme)) {
					if (!bscheme.equals("https") && !bscheme.equals("http") && !docscheme.equals("file")
							&& !docscheme.equals("jar")) {
						// Remote document wants to set a non-http base URI
						getErrorHandler().policyError(elm, "Remote document wants to set a non-http base URL: " + buri);
						buri = docUrl != null ? docUrl.toExternalForm() : null;
					}
				}
				return buri;
			} else {
				try {
					URL bUrl = new URL(buri);
					String bscheme = bUrl.getProtocol();
					if (bscheme.equals("https") || bscheme.equals("http")) {
						return buri;
					} else {
						getErrorHandler().policyError(elm,
								"Untrusted document wants to set a non-http base URL: " + buri);
					}
				} catch (MalformedURLException e) {
				}
			}
		}
		buri = document.getBaseURI();
		if (buri == null) {
			buri = document.getDocumentURI();
		}
		return buri;
	}

	/**
	 * Gets an URL for the given URI, taking into account the Base URL if
	 * appropriate.
	 * 
	 * @param uri
	 *            the uri.
	 * @return the absolute URL.
	 * @throws MalformedURLException
	 *             if the uri was wrong.
	 */
	@Override
	public URL getURL(String uri) throws MalformedURLException {
		if (uri.length() == 0) {
			throw new MalformedURLException("Empty URI");
		}
		URL url;
		if (uri.indexOf("://") < 0) {
			url = new URL(getBaseURL(), uri);
		} else {
			url = new URL(uri);
		}
		return url;
	}

	/**
	 * Is the provided URL a safe origin to load certain external resources?
	 * 
	 * @param linkedURL
	 *            the URL of the external resource.
	 * 
	 * @return <code>true</code> if is a safe origin, <code>false</code> otherwise.
	 */
	@Override
	public boolean isSafeOrigin(URL linkedURL) {
		URL base = getBaseURL();
		String docHost = base.getHost();
		int docPort = base.getPort();
		if (docPort == -1) {
			docPort = base.getDefaultPort();
		}
		String linkedHost = linkedURL.getHost();
		int linkedPort = linkedURL.getPort();
		if (linkedPort == -1) {
			linkedPort = linkedURL.getDefaultPort();
		}
		return (docHost.equalsIgnoreCase(linkedHost) || linkedHost.endsWith(docHost)) && docPort == linkedPort;
	}

	/**
	 * Determine whether the retrieval of the given URL is authorized.
	 * <p>
	 * If the URL's protocol is not {@code http} nor {@code https} and document's
	 * base URL's scheme is neither {@code file} nor {@code jar}, it is denied.
	 * </p>
	 * 
	 * @param url the URL to check.
	 * @return {@code true} if allowed.
	 */
	@Override
	public boolean isAuthorizedOrigin(URL url) {
		String scheme = url.getProtocol();
		URL base = getBaseURL();
		if (base != null) {
			String baseScheme = base.getProtocol();
			// To try to speed things up, only the parameter's scheme is compared
			// case-insensitively
			if (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http") && !baseScheme.equals("file")
					&& !baseScheme.equals("jar")) {
				return false;
			}
		} else if (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http")) {
			return false;
		}
		return true;
	}

	/**
	 * Get the referrer policy obtained through the 'Referrer-Policy' header or a meta
	 * element.
	 * 
	 * @return the referrer policy, or the empty string if none was specified.
	 */
	@Override
	public String getReferrerPolicy() {
		NodeList nl = document.getElementsByTagName("meta");
		// The last one takes precedence
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Element el = (Element) nl.item(i);
			if ("referrer".equalsIgnoreCase(el.getAttribute("name"))) {
				String policy = el.getAttribute("content");
				if (policy.length() != 0) {
					metaReferrerPolicy = policy;
					break;
				}
			}
		}
		return metaReferrerPolicy;
	}

	protected void setReferrerPolicyHeader(String policy) {
		if (metaReferrerPolicy.length() == 0) {
			metaReferrerPolicy = policy;
		}
	}

	class MyOMStyleSheetList extends StyleSheetList {

		protected MyOMStyleSheetList(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		protected boolean hasErrorsOrWarnings() {
			boolean hasRuleErrors = false;
			Iterator<AbstractCSSStyleSheet> it = iterator();
			while (it.hasNext()) {
				AbstractCSSStyleSheet sheet = it.next();
				SheetErrorHandler eh = sheet.getErrorHandler();
				if (sheet.hasRuleErrorsOrWarnings() || eh.hasSacErrors() || eh.hasSacWarnings() || eh.hasOMErrors()
						|| eh.hasOMWarnings()) {
					hasRuleErrors = true;
					break;
				}
			}
			return hasRuleErrors;
		}

		@Override
		protected void update() {
			super.update();
			updateStyleLists();
		}

	}
}
