/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

import io.sf.carte.doc.DOMTokenList;
import io.sf.carte.doc.DOMTokenSetImpl;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.DOMSelectorMatcher;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * A bare DOM element node.
 */
abstract public class DOMElement extends NamespacedNode implements CSSElement, ParentNode {

	private static final long serialVersionUID = 2L;

	final String localName;

	/**
	 * The attributes NamedNodeMap.
	 */
	final MyNamedNodeMap nodeMap;

	private final ChildCollections child;

	/**
	 * The field backing the getClassList() method.
	 */
	ClassList classList = null;

	private TypeInfo schemaTypeInfo = null;

	/*
	 * Style-related fields
	 */
	// Weak reference to selector matcher
	private transient WeakReference<SelectorMatcher> selectorMatcherRef = null;
	// Map from pseudo-elements to override styles
	private Map<Condition, CSSStyleDeclaration> overrideStyleSet = null;

	private boolean rawTextElement = false;

	DOMElement(String localName, String namespaceUri) {
		super(Node.ELEMENT_NODE, namespaceUri);
		this.localName = localName;
		nodeMap = new MyNamedNodeMap();
		child = new DefaultChildNodeList();
	}

	@Override
	ChildCollections getNodeList() {
		return child;
	}

	boolean isVoid() {
		return false;
	}

	void setRawText() {
		rawTextElement = true;
	}

	boolean isRawText() {
		return rawTextElement || "preserve".equalsIgnoreCase(getAttributeNS(DOMDocument.XML_NAMESPACE_URI, "space"));
	}

	class MyNamedNodeMap extends DOMNamedNodeMap<DOMAttr> implements AttributeNamedNodeMap {

		private static final long serialVersionUID = 1L;

		MyNamedNodeMap() {
			super(Node.ATTRIBUTE_NODE);
		}

		@Override
		public Iterator<Attr> iterator() {
			return getNodeList().attributeIterator();
		}

		@Override
		void registerNode(DOMAttr arg) {
			arg.setAttributeOwner(DOMElement.this);
		}

		@Override
		void unregisterNode(DOMAttr removedItem) {
			removedItem.setAttributeOwner(null);
		}

		@Override
		void verifyNewNode(Node arg) throws DOMException {
			super.verifyNewNode(arg);
			Element owner = ((Attr) arg).getOwnerElement();
			if (owner != null && owner != DOMElement.this) {
				throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, "Attribute is already in use.");
			}
		}

		@Override
		DOMElement getOwnerNode() {
			return DOMElement.this;
		}

	}

	@Override
	public AttributeNamedNodeMap getAttributes() {
		return nodeMap;
	}

	@Override
	public boolean hasAttributes() {
		return !nodeMap.isEmpty();
	}

	@Override
	public String getAttribute(String name) {
		String attrStr;
		Attr attr = nodeMap.getNamedItem(name);
		if (attr == null) {
			attrStr = "";
		} else {
			attrStr = attr.getValue();
		}
		return attrStr;
	}

	@Override
	public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
		String attrStr;
		Attr attr = nodeMap.getNamedItemNS(namespaceURI, localName);
		if (attr == null) {
			attrStr = "";
		} else {
			attrStr = attr.getValue();
		}
		return attrStr;
	}

	@Override
	public Attr getAttributeNode(String name) {
		return nodeMap.getNamedItem(name);
	}

	@Override
	public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
		return nodeMap.getNamedItemNS(namespaceURI, localName);
	}

	@Override
	public boolean hasAttribute(String name) {
		boolean ret  = nodeMap.hasAttribute(name);
		if (!ret && name.indexOf(':') == -1) {
			name = name.toLowerCase(Locale.ROOT);
			ret = nodeMap.hasAttribute(name);
		}
		return ret;
	}

	@Override
	public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
		return nodeMap.getNamedItemNS(namespaceURI, localName) != null;
	}

	@Override
	public void removeAttribute(String name) throws DOMException {
		try {
			nodeMap.removeNamedItem(name);
		} catch (DOMException e) {
			if (e.code != DOMException.NOT_FOUND_ERR) {
				throw e;
			}
		}
	}

	@Override
	public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
		try {
			nodeMap.removeNamedItemNS(namespaceURI, localName);
		} catch (DOMException e) {
			if (e.code != DOMException.NOT_FOUND_ERR) {
				throw e;
			}
		}
	}

	@Override
	public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
		return nodeMap.removeItem(oldAttr);
	}

	@Override
	public void setAttribute(String name, String value) throws DOMException {
		Attr attr = nodeMap.getNamedItem(name);
		if (attr == null) {
			attr = getOwnerDocument().createAttribute(name);
			attr.setValue(value);
			nodeMap.setNamedItem(attr);
		} else {
			attr.setValue(value); // This should trigger the update events
		}
	}

	@Override
	public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
		Attr attr = nodeMap.getNamedItem(qualifiedName);
		if (attr == null || !Objects.equals(namespaceURI, attr.getNamespaceURI())) {
			attr = getOwnerDocument().createAttributeNS(namespaceURI, qualifiedName);
			nodeMap.setNamedItemNS(attr);
		}
		attr.setValue(value); // This should trigger the update events
	}

	@Override
	public Attr setAttributeNode(Attr newAttr) throws DOMException {
		return nodeMap.setNamedItem(newAttr);
	}

	@Override
	public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
		return setAttributeNode(newAttr);
	}

	@Override
	public String getId() {
		if (!nodeMap.isEmpty()) {
			Iterator<DOMNode> it = nodeMap.getNodeList().iterator();
			while(it.hasNext()) {
				Attr attr = (Attr) it.next();
				if (attr.isId()) {
					return attr.getValue();
				}
			}
		}
		return "";
	}

	@Override
	@Deprecated
	public void setIdAttribute(String name, boolean isId) {
	}

	@Override
	@Deprecated
	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) {
	}

	@Override
	@Deprecated
	public void setIdAttributeNode(Attr idAttr, boolean isId) {
	}

	boolean isIdAttribute(String localName) {
		return getOwnerDocument().isIdAttribute(localName);
	}

	@Override
	public ElementList getElementsByTagNameNS(String namespaceURI, String localName) {
		return child.getElementsByTagNameNS(namespaceURI, localName);
	}

	@Override
	public ElementList getElementsByTagName(String name) {
		return child.getElementsByTagName(name, getOwnerDocument().isHTML());
	}

	/*
	 * Start of class list code
	 */

	/**
	 * Gives a live DOMTokenList collection of the class attributes of this element.
	 * <p>
	 * Any modification to the returned list changes the value of the <code>class</code>
	 * attribute of this element, and vice-versa.
	 * 
	 * @return the DOMTokenList ordered collection of the class attributes of this element.
	 */
	public DOMTokenList getClassList() {
		if (classList == null) {
			DOMAttr attr = nodeMap.getNamedItem("class");
			if (getOwnerDocument().getComplianceMode() == CSSDocument.ComplianceMode.STRICT) {
				classList = new ClassList();
			} else {
				classList = new QuirksClassList();
			}
			if (attr != null && attr.value.length() != 0) {
				classList.setValue(attr.value);
			}
		}
		return classList;
	}

	class ClassList extends DOMTokenSetImpl {

		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(String value) throws DOMException {
			if (value == null || value.length() == 0) {
				clear();
				value = "";
			}
			super.setValue(value);
		}

		@Override
		protected void addUnchecked(String token) throws DOMException {
			super.addUnchecked(token);
			if (!DOMElement.this.nodeMap.hasAttribute("class")) {
				DOMAttr attr = (DOMAttr) getOwnerDocument().createAttributeNS(null, "class");
				attr.setValue(token);
				DOMElement.this.nodeMap.setNamedItem(attr);
				attr.setAttributeOwner(DOMElement.this);
			}
		}

	}

	class QuirksClassList extends ClassList {

		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(String value) throws DOMException {
			if (value != null) {
				value = value.toLowerCase(Locale.ROOT);
			}
			super.setValue(value);
		}

		@Override
		public boolean contains(String token) {
			if (token == null) {
				return false;
			}
			token = token.toLowerCase(Locale.ROOT);
			return super.contains(token);
		}

		@Override
		public void add(String token) throws DOMException {
			argumentCheckVoidSpaces(token);
			token = token.toLowerCase(Locale.ROOT);
			addUnchecked(token);
		}

		@Override
		public void remove(String token) throws DOMException {
			argumentCheckVoidSpaces(token);
			token = token.toLowerCase(Locale.ROOT);
			removeUnchecked(token);
		}

		@Override
		public boolean toggle(String token) throws DOMException {
			argumentCheckVoidSpaces(token);
			token = token.toLowerCase(Locale.ROOT);
			return toggleUnchecked(token);
		}

		@Override
		public void replace(String oldToken, String newToken) throws DOMException {
			argumentCheckVoidSpaces(oldToken);
			argumentCheckVoidSpaces(newToken);
			oldToken = oldToken.toLowerCase(Locale.ROOT);
			newToken = newToken.toLowerCase(Locale.ROOT);
			replaceUnchecked(oldToken, newToken);
		}
	}

	/**
	 * Gives a live NodeList containing all child elements which have all of the given class
	 * names under this reference element.
	 * 
	 * @param names
	 *            the names of the classes, separated by whitespace.
	 * @return the live NodeList containing all child elements which have all of the given
	 *         class names under this reference element.
	 */
	@Override
	public ElementList getElementsByClassName(String names) {
		return child.getElementsByClassName(names, getOwnerDocument().getComplianceMode());
	}

	/*
	 * The next two methods are defined by W3C's HTMLElement interface, but makes sense to
	 * define them here
	 */

	public String getClassName() {
		return getAttribute("class");
	}

	public void setClassName(String className) {
		setAttribute("class", className);
	}

	/*
	 * End of class list code
	 */

	/**
	 * Gets a static list of the descendant elements that match any of the specified group of
	 * selectors.
	 * <p>
	 * Unlike methods like {@link #getElementsByTagName(String)} or
	 * {@link #getElementsByClassName(String)}, this is not a live list but a static one,
	 * representing the state of the document when the method was called. If no elements
	 * match, the list will be empty. This element is not included in the query.
	 * 
	 * @param selectors
	 *            a comma-separated list of selectors.
	 * @return an ElementList with the elements that match any of the specified group of
	 *         selectors.
	 */
	@Override
	public ElementList querySelectorAll(String selectors) {
		return querySelectorAll(selectors, getFirstChild());
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	/**
	 * The name of the element (qualified if there is a namespace prefix). The name is
	 * case-preserving.
	 */
	@Override
	public String getTagName() {
		String tagname = localName;
		String prefix = getPrefix();
		if (prefix == null) {
			return tagname;
		}
		StringBuilder buf = new StringBuilder(tagname.length() + prefix.length() + 1);
		buf.append(prefix).append(':').append(tagname);
		return buf.toString();
	}

	@Override
	public String getNodeName() {
		return getTagName();
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		String namespaceURI = super.lookupNamespaceURI(prefix);
		if (namespaceURI == null) {
			AbstractDOMNode.RawNodeList nodelist = this.nodeMap.getNodeList();
			if (!nodelist.isEmpty()) {
				Iterator<DOMNode> it = nodelist.iterator();
				while (it.hasNext()) {
					Attr attr = (Attr) it.next();
					String localName = attr.getLocalName();
					String pre = attr.getPrefix();
					if (DOMDocument.XMLNS_NAMESPACE_URI.equals(attr.getNamespaceURI())) {
						if ("xmlns".equals(localName) && prefix.equals(pre)) {
							return attr.getValue();
						}
						if ("xmlns".equals(pre) && pre.equals(localName)) {
							return attr.getValue();
						}
					}
				}
			}
			Node pnode = this;
			while ((pnode = pnode.getParentNode()) != null && pnode.getNodeType() != Node.ELEMENT_NODE);
			if (pnode != null) {
				namespaceURI = pnode.lookupNamespaceURI(namespaceURI);
			}
		}
		return namespaceURI;
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			return null;
		}
		if (namespaceURI.equals(getNamespaceURI())) {
			return getPrefix();
		}
		RawNodeList nodelist = this.nodeMap.getNodeList();
		if (!nodelist.isEmpty()) {
			Iterator<DOMNode> it = nodelist.iterator();
			while (it.hasNext()) {
				Attr attr = (Attr) it.next();
				if (DOMDocument.XMLNS_NAMESPACE_URI.equals(attr.getNamespaceURI())
						&& namespaceURI.equals(attr.getValue())) {
					String localName = attr.getLocalName();
					String prefix = attr.getPrefix();
					if ("xmlns".equals(localName)) {
						return prefix;
					}
					if ("xmlns".equals(prefix)) {
						return localName;
					}
				}
			}
		}
		Node pnode = this;
		while ((pnode = pnode.getParentNode()) != null && pnode.getNodeType() != Node.ELEMENT_NODE);
		if (pnode != null) {
			return pnode.lookupPrefix(namespaceURI);
		}
		return namespaceURI == DOMDocument.XML_NAMESPACE_URI ? "xml" : null;
	}

	@Override
	void checkAppendNodeHierarchy(Node newChild) {
		super.checkAppendNodeHierarchy(newChild);
		if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Doctype must be added to document.");
		}
	}

	/*
	 * ParentNode code
	 */
	/**
	 * Gets the DOMElement that is the first child of this DOMElement.
	 * 
	 * @return the DOMElement that is the first child of this DOMElement, or null if there is
	 *         none.
	 */
	@Override
	public DOMElement getFirstElementChild() {
		return super.getFirstElementChild();
	}

	/**
	 * Gets the DOMElement that is the last child of this DOMElement.
	 * 
	 * @return the DOMElement that is the last child of this DOMElement, or null if there is
	 *         none.
	 */
	@Override
	public DOMElement getLastElementChild() {
		return super.getLastElementChild();
	}

	/**
	 * Gets the live ElementList containing all nodes of type Element that are children of
	 * this Element.
	 * 
	 * @return the ElementList containing all nodes of type Element that are children of this
	 *         Element.
	 */
	@Override
	public ElementList getChildren() {
		return child.getChildren();
	}

	/**
	 * Gets the number of child nodes of type Element that this parent node has.
	 * 
	 * @return the number of child nodes of type Element that this Element has.
	 */
	@Override
	public int getChildElementCount() {
		return super.getChildElementCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator() {
		return child.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> descendingIterator() {
		return child.createDescendingIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator(BitSet whatToShow) {
		return child.createIterator(whatToShow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMElement> elementIterator() {
		return child.elementIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMElement> elementIterator(String name) {
		return child.elementIterator(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMElement> elementIteratorNS(String namespaceURI, String localName) {
		return child.elementIteratorNS(namespaceURI, localName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator(int whatToShow, NodeFilter filter) {
		return child.createIterator(whatToShow, filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> typeIterator(short typeToShow) {
		return iterator(NodeFilter.maskTable[typeToShow - 1], null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator(NodeFilter filter) {
		return child.createIterator(-1, filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeListIterator listIterator() {
		return child.createListIterator();
	}

	/**
	 * Gives a representation of the text content of an element, approximately as if
	 * it was rendered according to the styling and the document language.
	 * <p>
	 * This method is computationally more expensive than {@link #getTextContent()}.
	 * </p>
	 * 
	 * @return a representation of the text content of an element.
	 * @throws StyleDatabaseRequiredException if style computations require a style
	 *                                        database which is not present.
	 */
	public String getInnerText() {
		StringBuilder buf = new StringBuilder(256);
		addInnerText(this, buf, false);
		return buf.toString();
	}

	private boolean addInnerText(DOMElement element, StringBuilder buf, boolean lastTextPreserved) throws DOMException {
		ComputedCSSStyle style = element.getComputedStyle(null);
		String display = style.getPropertyValue("display");
		String[] displays = display.split(" ");
		if (!element.hasPrintableNodes() || matchesString(displays, "none") || element.isNonPrintableElement()) {
			return lastTextPreserved;
		}
		// Determine text-transform
		String sTextTransform = style.getPropertyValue("text-transform");
		short textTransform = 0;
		if ("uppercase".equalsIgnoreCase(sTextTransform)) {
			textTransform = 2;
		} else if ("lowercase".equalsIgnoreCase(sTextTransform)) {
			textTransform = 1;
		} else if ("capitalize".equalsIgnoreCase(sTextTransform)) {
			textTransform = 3;
		}
		//
		boolean visible = !"hidden".equalsIgnoreCase(style.getPropertyValue("visibility"));
		//
		boolean inline = false;
		boolean isBlock = matchesString(displays, "block") || (matchesString(displays, "list-item")
				|| (matchesString(displays, "table") || matchesString(displays, "table-caption"))
						&& !(inline = matchesString(displays, "inline")));
		if (visible) {
			int buflenM1;
			if (isBlock && (buflenM1 = buf.length() - 1) != -1 && buf.charAt(buflenM1) != '\n') {
				buf.append('\n');
			}
			if (hasPrecedingAnonymousBox(displays, style)) {
				buf.append(' ');
			} else if (matchesString(displays, "table-cell") && element.getPreviousElementSibling() != null) {
				buf.append('\t'); // sort of 'table cell separator'
			}
		}
		//
		boolean firstTextAdded = true;
		Iterator<DOMNode> it = element.getNodeList().iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (!node.getChildNodes().isEmpty()) {
					lastTextPreserved = addInnerText((DOMElement) node, buf, lastTextPreserved);
					firstTextAdded = false;
				} else if (visible) {
					// Element processing was skipped, check for table-cell
					DOMElement childElm = (DOMElement) node;
					ComputedCSSStyle childStyle = childElm.getComputedStyle(null);
					String childDisplay = childStyle.getPropertyValue("display");
					String[] childDisplays = childDisplay.split(" ");
					if (matchesString(childDisplays, "table-cell") && childElm.getPreviousElementSibling() != null) {
						buf.append('\t');
					} else {
						lastTextPreserved = innerTextVoidElement(childElm, lastTextPreserved, buf);
					}
				}
				break;
			case Node.TEXT_NODE:
				if (!visible) {
					continue;
				}
				String text = node.getNodeValue();
				String whiteSpace = style.getPropertyValue("white-space");
				if ("pre".equalsIgnoreCase(whiteSpace) || "pre-wrap".equalsIgnoreCase(whiteSpace)
						|| "break-spaces".equalsIgnoreCase(whiteSpace)) {
					if (textTransform == 0) {
						buf.append(text);
					} else {
						appendTransformedText(text, textTransform, buf);
					}
					lastTextPreserved = true;
				} else {
					appendNormalizedWhitespace(text, "pre-line".equalsIgnoreCase(whiteSpace), isBlock && firstTextAdded,
							textTransform, buf);
					lastTextPreserved = false;
				}
				firstTextAdded = false;
				break;
			case Node.CDATA_SECTION_NODE:
				if (!visible) {
					continue;
				}
				text = node.getNodeValue();
				buf.append(text);
				firstTextAdded = false;
				lastTextPreserved = true;
			default:
			}
		}
		//
		if (visible) {
			boolean isRow = false;
			if (isBlock || ((isRow = matchesString(displays, "table-row")) && !inline)) {
				// If last character is a non-preserved white space, trim it.
				trimBuffer(lastTextPreserved, buf);
				if (!isRow || element.getNextElementSibling() != null) {
					buf.append('\n');
				}
			}
		}
		return lastTextPreserved;
	}

	private static boolean matchesString(String[] values, String value) {
		for (String s : values) {
			if (value.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	boolean isNonPrintableElement() {
		return false;
	}

	private boolean hasPrintableNodes() {
		Iterator<DOMNode> it = getNodeList().iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (((DOMElement) node).hasPrintableNodes()) {
					return true;
				}
				break;
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				return true;
			}
		}
		return false;
	}

	private static boolean hasPrecedingAnonymousBox(String[] displays, ComputedCSSStyle style) {
		return matchesString(displays, "list-item")
				&& "inside".equalsIgnoreCase(style.getPropertyValue("list-style-position"));
	}

	boolean innerTextVoidElement(DOMElement element, boolean lastTextPreserved, StringBuilder buf) {
		return lastTextPreserved;
	}

	private static void appendTransformedText(String text, short textTransform, StringBuilder buf) {
		boolean whitespaceLast = false;
		int len = text.length();
		for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
			int c = text.codePointAt(i);
			if (Character.isWhitespace(c)) {
				if (!whitespaceLast) {
					whitespaceLast = true;
				}
			} else {
				if (whitespaceLast) {
					whitespaceLast = false;
					if (textTransform == 3) {
						c = Character.toUpperCase(c);
						buf.appendCodePoint(c);
						continue;
					}
				}
				if (textTransform == 2) {
					c = Character.toUpperCase(c);
				} else if (textTransform == 1) {
					c = Character.toLowerCase(c);
				}
			}
			buf.appendCodePoint(c);
		}
	}

	private static void appendNormalizedWhitespace(String text, boolean preserveNL, boolean firstText,
			short textTransform, StringBuilder buf) {
		boolean whitespaceLast = firstText;
		int buflen = buf.length();
		char c;
		if (buflen != 0 && Character.isWhitespace(c = buf.charAt(buflen - 1))) {
			if (!preserveNL || c != '\n') {
				whitespaceLast = true;
			}
		}
		//
		int cp;
		int len = text.length();
		for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
			cp = text.codePointAt(i);
			if (Character.isWhitespace(cp)) {
				if (!preserveNL || cp != '\n') {
					if (!whitespaceLast) {
						whitespaceLast = true;
						buf.append(' ');
					}
				} else {
					if (whitespaceLast) {
						int buflenM1 = buf.length() - 1;
						char b = buf.charAt(buflenM1);
						if (b != '\n') {
							buf.setLength(buflenM1);
						}
					} else {
						whitespaceLast = true;
					}
					buf.appendCodePoint(cp);
				}
			} else {
				if (whitespaceLast) {
					whitespaceLast = false;
					if (textTransform == 3) {
						cp = Character.toUpperCase(cp);
						buf.appendCodePoint(cp);
						continue;
					}
				}
				if (textTransform == 2) {
					cp = Character.toUpperCase(cp);
				} else if (textTransform == 1) {
					cp = Character.toLowerCase(cp);
				}
				buf.appendCodePoint(cp);
			}
		}
	}

	void trimBuffer(boolean lastTextPreserved, StringBuilder buf) {
		final int buflenM1;
		if (!lastTextPreserved && (buflenM1 = buf.length() - 1) != -1 && buf.charAt(buflenM1) == ' ') {
			buf.setLength(buflenM1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(SelectorList selist, Condition pseudoElement) {
		SelectorMatcher matcher = getSelectorMatcher();
		matcher.setPseudoElement(pseudoElement);
		return matcher.matches(selist) != -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CSSStyleDeclaration getStyle() {
		DOMDocument.StyleAttr styleAttr = (DOMDocument.StyleAttr) getAttributeNode("style");
		if (styleAttr == null) {
			if (getOwnerDocument().getComplianceMode() == CSSDocument.ComplianceMode.QUIRKS) {
				Iterator<Attr> it = getAttributes().iterator();
				while (it.hasNext()) {
					Attr node = it.next();
					if ("style".equalsIgnoreCase(node.getNodeName())) {
						return ((DOMDocument.StyleAttr) node).getStyle();
					}
				}
			}
			return null;
		}
		return styleAttr.getStyle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasOverrideStyle(Condition pseudoElt) {
		if (overrideStyleSet == null) {
			return false;
		}
		return overrideStyleSet.containsKey(pseudoElt);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CSSStyleDeclaration getOverrideStyle(Condition pseudoElt) {
		CSSStyleDeclaration overrideStyle = null;
		if (overrideStyleSet == null) {
			overrideStyleSet = new HashMap<Condition, CSSStyleDeclaration>(1);
		} else {
			overrideStyle = overrideStyleSet.get(pseudoElt);
		}
		if (overrideStyle == null) {
			overrideStyle = getOwnerDocument().getStyleSheetFactory().createInlineStyle(this);
			overrideStyleSet.put(pseudoElt, overrideStyle);
		}
		return overrideStyle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasPresentationalHints() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportHintsToStyle(CSSStyleDeclaration style) {
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
		return (ComputedCSSStyle) getOwnerDocument().getStyleSheet().getComputedStyle(this, peCond);
	}

	@Override
	abstract public DOMElement cloneNode(boolean deep);

	abstract CSSStyleSheetFactory getStyleSheetFactory();

	@Override
	public TypeInfo getSchemaTypeInfo() {
		if (schemaTypeInfo == null) {
			schemaTypeInfo = new ElementTypeInfo();
		}
		return schemaTypeInfo;
	}

	public String getStartTag() {
		StringBuilder buf = new StringBuilder(128);
		buf.append('<').append(getTagName());
		if (nodeMap.getLength() > 0) {
			buf.append(' ');
			nodeMap.appendTo(buf);
		}
		if (hasChildNodes()) {
			buf.append('>');
		} else {
			buf.append(" />");
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		int bufsz = 32;
		if (hasChildNodes()) {
			bufsz = 720;
		} else if (hasAttributes()) {
			bufsz = 128;
		}
		String tagname = getTagName();
		StringBuilder buf = new StringBuilder(bufsz);
		buf.append('<').append(tagname);
		if (nodeMap.getLength() > 0) {
			buf.append(' ');
			nodeMap.appendTo(buf);
		}
		if (!isVoid()) {
			buf.append('>');
			if (hasChildNodes()) {
				NodeList list = getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
					buf.append(list.item(i).toString());
				}
			}
			buf.append("</").append(tagname).append('>');
		} else {
			buf.append(" />");
		}
		return buf.toString();
	}

	/*
	 * When referenced from an Element node, typeNamespace and typeName are null.
	 */
	static class ElementTypeInfo extends DOMTypeInfo implements java.io.Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTypeNamespace() {
			return null;
		}

	}

}

