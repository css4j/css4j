/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * Base class for DOMDocument's non-DTD DOM nodes.
 */
abstract class NDTNode extends AbstractDOMNode implements NonDocumentTypeChildNode {

	private static final long serialVersionUID = 2L;

	NDTNode(short nodeType) {
		super(nodeType);
	}

	/*
	 * ParentNode implementation code
	 */
	/**
	 * Gets the Element that is the first child of this ParentNode.
	 * 
	 * @return the Element that is the first child of this ParentNode, or null if there is
	 *         none.
	 */
	public DOMElement getFirstElementChild() {
		Node node = getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return (DOMElement) node;
			}
			node = node.getNextSibling();
		}
		return null;
	}

	/**
	 * Gets the DOMElement that is the last child of this ParentNode.
	 * 
	 * @return the DOMElement that is the last child of this ParentNode, or null if there is
	 *         none.
	 */
	public DOMElement getLastElementChild() {
		Node node = getLastChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return (DOMElement) node;
			}
			node = node.getPreviousSibling();
		}
		return null;
	}

	/**
	 * Gets the number of child nodes of type Element that this parent node has.
	 * 
	 * @return the number of child nodes of type Element that this ParentNode has.
	 */
	public int getChildElementCount() {
		int count = 0;
		Node node = getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				count++;
			}
			node = node.getNextSibling();
		}
		return count;
	}

	@Override
	public boolean hasChildNodes() {
		return !getNodeList().isEmpty();
	}

	class ChildElementList implements ElementList {

		@Override
		public boolean contains(Node node) {
			return getNodeList().contains(node);
		}

		@Override
		public DOMElement item(int index) {
			int idx = 0;
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (idx == index) {
						return (DOMElement) node;
					}
					idx++;
				}
				node = node.getNextSibling();
			}
			return null;
		}

		@Override
		public Iterator<DOMElement> iterator() {
			return getNodeList().elementIterator();
		}

		@Override
		public int getLength() {
			return getChildElementCount();
		}

		@Override
		public boolean isEmpty() {
			return getFirstElementChild() == null;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(getLength() * 32 + 40);
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					buf.append(node.toString());
				}
				node = node.getNextSibling();
			}
			return buf.toString();
		}

	}

	/*
	 * NonDocumentTypeChildNode implementation code
	 */
	@Override
	public DOMElement getPreviousElementSibling() {
		Node next = getPreviousSibling();
		while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
			next = next.getPreviousSibling();
		}
		return (DOMElement) next;
	}

	@Override
	public DOMElement getNextElementSibling() {
		Node next = getNextSibling();
		while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
			next = next.getNextSibling();
		}
		return (DOMElement) next;
	}

	public DOMNode prependChild(Node newChild) throws DOMException {
		RawNodeList nl = getNodeList();
		if (nl.isEmpty()) {
			return appendChild(newChild);
		}
		AbstractDOMNode added = (AbstractDOMNode) newChild;
		if (added.getNodeType() != Node.DOCUMENT_FRAGMENT_NODE) {
			preInsertChild(added, getFirstChild());
			AbstractDOMNode refChild = nl.getFirst();
			nl.insertBefore(added, refChild);
			postInsertChild(added);
		} else {
			prependDocumentFragment(added);
		}
		return added;
	}

	private void prependDocumentFragment(Node newChild) {
		Node added = newChild.getLastChild();
		while (added != null) {
			Node next = added.getPreviousSibling();
			prependChild(added);
			added = next;
		}
	}

	static DOMElement querySelector(String selectors, Node firstChild) {
		SelectorList selist = parseSelectors(selectors);
		return matchQuerySelector(selist, firstChild);
	}

	private static DOMElement matchQuerySelector(SelectorList selist, Node firstChild) {
		Node node = firstChild;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement element = (DOMElement) node;
				if (element.matches(selist, null)) {
					return element;
				}
				DOMElement elt = matchQuerySelector(selist, element.getFirstChild());
				if (elt != null) {
					return elt;
				}
			}
			node = node.getNextSibling();
		}
		return null;
	}

	static ElementList querySelectorAll(String selectors, Node firstChild) {
		SelectorList selist = parseSelectors(selectors);
		DOMElementLinkedList list = new DOMElementLinkedList();
		list.fillQuerySelectorList(selist, firstChild);
		return list;
	}

	private static SelectorList parseSelectors(String selectors) throws DOMException {
		Parser parser = new CSSParser();
		SelectorList selist;
		try {
			selist = parser.parseSelectors(new StringReader(selectors));
		} catch (CSSNamespaceParseException e) {
			throw createDOMException(DOMException.NAMESPACE_ERR,
					"Namespaces inside the selectors are not supported: " + selectors, e);
		} catch (Exception e) {
			throw createDOMException(DOMException.SYNTAX_ERR,
					"Unable to parse selector in: " + selectors, e);
		}
		return selist;
	}

	private static DOMException createDOMException(short type, String message, Exception cause) {
		DOMException ex = new DOMException(type, message);
		ex.initCause(cause);
		return ex;
	}

	class DefaultChildNodeList extends LinkedNodeList implements ChildCollections {

		private static final long serialVersionUID = 1L;

		/**
		 * For use by getChildren().
		 */
		private WeakReference<ElementList> childElementRef = null;

		@Override
		public ElementList getChildren() {
			ElementList list = null;
			if (childElementRef != null) {
				list = childElementRef.get();
			}
			if (list == null) {
				list = new ChildElementList();
				childElementRef = new WeakReference<>(list);
			}
			return list;
		}

		@Override
		public ElementList getElementsByTagNameNS(String namespaceURI, String localName) {
			boolean matchAll = "*".equals(localName);
			boolean matchAllNS = "*".equals(namespaceURI);
			if (namespaceURI != null && !matchAllNS) {
				namespaceURI = namespaceURI.intern();
			}
			TagnameElementListNS list = new TagnameElementListNS(NDTNode.this, localName, namespaceURI, matchAll,
					matchAllNS);
			return list;
		}

		@Override
		public ElementList getElementsByTagName(String name, boolean isHTML) {
			if (name == null) {
				return EmptyElementList.getInstance();
			}
			boolean matchAll = "*".equals(name);
			// Determine prefix
			String prefix = null;
			int idx = name.indexOf(':');
			if (idx != -1) {
				prefix = name.substring(0, idx);
				idx++;
				if (idx == name.length()) {
					return EmptyElementList.getInstance();
				}
				name = name.substring(idx);
			}
			TagnameElementList list = new TagnameElementList(NDTNode.this, name, prefix, matchAll, isHTML);
			return list;
		}

		@Override
		public ElementList getElementsByClassName(String names, CSSDocument.ComplianceMode mode) {
			if (names == null) {
				return EmptyElementList.getInstance();
			}
			names = names.trim();
			if (mode == CSSDocument.ComplianceMode.QUIRKS) {
				names = names.toLowerCase(Locale.ROOT); // Quirks mode
			}
			TreeSet<String> sorted = new TreeSet<>();
			if (names.indexOf(' ') != -1) { // 'names' has a whitespace
				names = sortClassNames(names, sorted);
			} else {
				sorted.add(names);
			}
			ClassnameElementList list = new ClassnameElementList(NDTNode.this, sorted);
			return list;
		}

		private String sortClassNames(String names, SortedSet<String> sorted) {
			StringTokenizer st = new StringTokenizer(names);
			while (st.hasMoreTokens()) {
				sorted.add(st.nextToken());
			}
			StringBuilder buf = new StringBuilder(names.length());
			Iterator<String> it = sorted.iterator();
			buf.append(it.next());
			while (it.hasNext()) {
				buf.append(' ').append(it.next());
			}
			return buf.toString();
		}

		@Override
		void postRemoveChild(AbstractDOMNode removed) {
			NDTNode.this.postRemoveChild(removed);
		}

		@Override
		void preInsertChild(Node node, Node refNode) {
			NDTNode.this.preInsertChild(node, refNode);
		}

		@Override
		void postInsertChild(AbstractDOMNode node) {
			NDTNode.this.postInsertChild(node);
		}

		@Override
		void replaceChild(Node newChild, Node oldChild) {
			NDTNode.this.replaceChild(newChild, oldChild);
		}

	}

}
