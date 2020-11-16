/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMTokenList;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * Base class for DOMDocument's non-DTD DOM nodes.
 */
abstract class NDTNode extends AbstractDOMNode implements NonDocumentTypeChildNode {

	private static final long serialVersionUID = 1L;

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
			preAddChild(added);
			AbstractDOMNode refChild = nl.getFirst();
			nl.insertBefore(added, refChild);
			postAddChild(added);
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

	static ElementList querySelectorAll(String selectors, Node firstChild) {
		Parser parser = new CSSParser();
		SelectorList selist;
		try {
			selist = parser.parseSelectors(new StringReader(selectors));
		} catch (CSSNamespaceParseException e) {
			throw createDOMException(DOMException.NAMESPACE_ERR, "Namespaces inside the selectors are not supported: " + selectors, e);
		} catch (Exception e) {
			throw createDOMException(DOMException.SYNTAX_ERR, "Unable to parse selector in: " + selectors, e);
		}
		DOMElementLinkedList list = new DOMElementLinkedList();
		list.fillQuerySelectorList(selist, firstChild);
		return list;
	}

	private static DOMException createDOMException(short type, String message, Exception cause) {
		DOMException ex = new DOMException(type, message);
		ex.initCause(cause);
		return ex;
	}

	class DefaultChildNodeList extends LinkedNodeList implements ChildCollections {

		/**
		 * Used by getElementsByTagNameNS: maps a qname-related string to a list of elements.
		 */
		private Map<String, WeakReference<DOMElementLinkedList>> tagListMap = null;

		/**
		 * Used by getElementsByClassName: maps whitespace-separated class names to the
		 * corresponding classLists
		 */
		private Map<String, WeakReference<DOMElementLinkedList>> classListMap = null;

		// Initialization/update locks
		private final ReentrantLock tagListLock = new ReentrantLock();
		private final ReentrantLock classListLock = new ReentrantLock();

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
				childElementRef = new WeakReference<ElementList>(list);
			}
			return list;
		}

		@Override
		public ElementList getElementsByTagNameNS(String namespaceURI, String localName) {
			if (namespaceURI != null && namespaceURI.length() > 0 && !isDefaultNamespace(namespaceURI)) {
				namespaceURI = namespaceURI.intern();
				localName = localName.intern();
				boolean matchAll = "*".equals(localName);
				String qname;
				if (matchAll) {
					qname = namespaceURI;
				} else {
					qname = namespaceURI + ":" + localName;
					qname = qname.intern();
				}
				DOMElementLinkedList list = findList(qname);
				if (list == null) {
					list = createElementNodeList();
					list.fillByTagList(localName, NDTNode.this, namespaceURI, matchAll);
					tagListLock.lock();
					try {
						WeakReference<DOMElementLinkedList> old = tagListMap.putIfAbsent(qname,
								new WeakReference<>(list));
						if (old != null) {
							list = old.get();
						}
					} finally {
						tagListLock.unlock();
					}
				}
				return list;
			}
			return getElementsByTagName(localName);
		}

		DOMElementLinkedList findList(String qname) {
			if (tagListMap == null) {
				tagListLock.lock();
				try {
					if (tagListMap == null) {
						tagListMap = new HashMap<String, WeakReference<DOMElementLinkedList>>(3);
					}
				} finally {
					tagListLock.unlock();
				}
			} else {
				tagListLock.lock();
				try {
					WeakReference<DOMElementLinkedList> ref = tagListMap.get(qname);
					if (ref != null) {
						DOMElementLinkedList list = ref.get();
						if (list == null) {
							tagListMap.remove(qname);
						} else {
							return list;
						}
					}
				} finally {
					tagListLock.unlock();
				}
			}
			return null;
		}

		@Override
		public ElementList getElementsByTagName(String name) {
			name = name.toLowerCase(Locale.ROOT);
			DOMElementLinkedList list = findList(name);
			if (list != null) {
				return list;
			}
			list = createElementNodeList();
			list.fillByTagList(name, NDTNode.this, getNamespaceURI(), "*".equals(name));
			tagListLock.lock();
			try {
				WeakReference<DOMElementLinkedList> old = tagListMap.putIfAbsent(name, new WeakReference<>(list));
				if (old != null) {
					list = old.get();
				}
			} finally {
				tagListLock.unlock();
			}
			return list;
		}

		private DOMElementLinkedList createElementNodeList() {
			return new DOMElementLinkedList();
		}

		@Override
		public void updateTaglistsOnInsert(DOMElement newChild, AbstractDOMNode appendedTo) {
			boolean appendThis = NDTNode.this == appendedTo;
			boolean descendant = isDescendant(appendedTo);
			if (tagListMap != null && (appendThis || descendant)) {
				updateMyTaglistsOnInsert(newChild);
			}
			if (!descendant) {
				DOMElement elm = newChild.getFirstElementChild();
				while (elm != null) {
					newChild.updateTaglistsOnInsert(elm, appendedTo);
					elm = elm.getNextElementSibling();
				}
			}
		}

		private void updateMyTaglistsOnInsert(DOMElement newChild) {
			String newtag = newChild.getLocalName();
			if (!isNullOrDefaultNamespaceURI(newChild.getNamespaceURI())) {
				newtag = newChild.getNamespaceURI() + ":" + newtag;
				newtag = newtag.intern();
			}
			String allTags = allTagsName(newChild);
			tagListLock.lock();
			try {
				Iterator<String> it = tagListMap.keySet().iterator();
				while (it.hasNext()) {
					String tag = it.next();
					if (newtag.equals(tag) || allTags.equals(tag)) {
						DOMElementLinkedList list = tagListMap.get(tag).get();
						if (list == null) {
							it.remove();
							continue;
						}
						list.updateOnInsert(newChild);
					}
				}
			} finally {
				tagListLock.unlock();
			}
		}

		@Override
		public void updateTaglistsOnRemove(DOMElement oldChild, AbstractDOMNode removedFrom) {
			boolean appendThis = NDTNode.this == removedFrom;
			boolean descendant = isDescendant(removedFrom);
			if (tagListMap != null && (appendThis || descendant)) {
				updateMyTaglistsOnRemoveElement(oldChild);
			}
			if (!descendant) {
				DOMElement elm = oldChild.getFirstElementChild();
				while (elm != null) {
					oldChild.updateTaglistsOnRemove(elm, removedFrom);
					elm = elm.getNextElementSibling();
				}
			}
		}

		private boolean isDescendant(Node node) {
			node = node.getParentNode();
			while (node != null) {
				if (NDTNode.this == node) {
					return true;
				}
				node = node.getParentNode();
			}
			return false;
		}

		private void updateMyTaglistsOnRemoveElement(DOMElement oldChild) {
			String newtag = oldChild.getLocalName();
			if (!isNullOrDefaultNamespaceURI(oldChild.getNamespaceURI())) {
				newtag = oldChild.getNamespaceURI() + ":" + newtag;
				newtag = newtag.intern();
			}
			String allTags = allTagsName(oldChild);
			tagListLock.lock();
			try {
				Iterator<String> it = tagListMap.keySet().iterator();
				while (it.hasNext()) {
					String tag = it.next();
					if (newtag.equals(tag) || allTags.equals(tag)) {
						DOMElementLinkedList list = tagListMap.get(tag).get();
						if (list == null) {
							it.remove();
							continue;
						}
						list.updateOnRemove(oldChild);
					}
				}
			} finally {
				tagListLock.unlock();
			}
		}


		/**
		 * Get the key name to be used for 'all tag names' collection for the given
		 * element, which is either the asterisk or the namespace URI of the element
		 * if it is not the default namespace.
		 * 
		 * @param element the element.
		 * @return the key name.
		 */
		String allTagsName(CSSElement element) {
			if (!isNullOrDefaultNamespaceURI(element.getNamespaceURI())) {
				return element.getNamespaceURI();
			}
			return "*";
		}

		private boolean isNullOrDefaultNamespaceURI(String namespaceURI) {
			return namespaceURI == null || isDefaultNamespace(namespaceURI);
		}

		@Override
		public ElementList getElementsByClassName(String names, CSSDocument.ComplianceMode mode) {
			names = names.trim();
			if (mode == CSSDocument.ComplianceMode.QUIRKS) {
				names = names.toLowerCase(Locale.ROOT); // Quirks mode
			}
			TreeSet<String> sorted = new TreeSet<>();
			boolean hasSpace = names.indexOf(' ') != -1;
			if (hasSpace) {
				names = sortClassNames(names, sorted);
			}
			DOMElementLinkedList list = findClassList(names);
			if (list != null) {
				return list;
			}
			if (!hasSpace) {
				sorted.add(names);
			}
			list = new DOMElementLinkedList();
			list.fillByClassList(sorted, NDTNode.this);
			classListLock.lock();
			try {
				WeakReference<DOMElementLinkedList> old = classListMap.putIfAbsent(names, new WeakReference<>(list));
				if (old != null) {
					list = old.get();
				}
			} finally {
				classListLock.unlock();
			}
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

		private DOMElementLinkedList findClassList(String names) {
			if (classListMap == null) {
				classListLock.lock();
				try {
					if (classListMap == null) {
						classListMap = new HashMap<String, WeakReference<DOMElementLinkedList>>(3);
					}
				} finally {
					classListLock.unlock();
				}
			} else {
				classListLock.lock();
				try {
					WeakReference<DOMElementLinkedList> ref = classListMap.get(names);
					if (ref != null) {
						DOMElementLinkedList list = ref.get();
						if (list == null) {
							classListMap.remove(names);
						} else {
							return list;
						}
					}
				} finally {
					classListLock.unlock();
				}
			}
			return null;
		}

		@Override
		public void updateClasslists(DOMElement owner, AbstractDOMNode appendedTo) {
			boolean appendThis = NDTNode.this == appendedTo;
			boolean descendant = isDescendant(appendedTo);
			if (classListMap != null && (appendThis || descendant)) {
				updateMyClasslists(owner);
			}
			if (!descendant) {
				DOMElement elm = owner.getFirstElementChild();
				while (elm != null) {
					owner.updateClasslists(elm, appendedTo);
					elm = elm.getNextElementSibling();
				}
			}
		}

		private void updateMyClasslists(DOMElement owner) {
			DOMTokenList ownerClasses;
			String ownerNames;
			boolean hasSpace;
			boolean hasClasses = owner.hasAttribute("class");
			if (hasClasses) {
				ownerClasses = owner.getClassList();
				ownerNames = ownerClasses.getSortedValue();
				hasSpace = ownerNames.indexOf(' ') != -1;
			} else {
				ownerClasses = null;
				ownerNames = "";
				hasSpace = false;
			}
			//
			TreeSet<String> sorted = new TreeSet<String>();
			classListLock.lock();
			try {
				Iterator<Entry<String, WeakReference<DOMElementLinkedList>>> it = classListMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, WeakReference<DOMElementLinkedList>> entry = it.next();
					String classNames = entry.getKey();
					DOMElementLinkedList list = entry.getValue().get();
					if (list == null) {
						it.remove();
						continue;
					}
					boolean hasOwner = list.contains(owner);
					if (!hasClasses) {
						if (hasOwner) {
							list.updateOnRemove(owner);
						}
						continue;
					}
					if (!classNames.equals(ownerNames) && !hasSpace) {
						if (hasOwner) {
							list.updateOnRemove(owner);
						}
						continue;
					}
					if (hasSpace) {
						sorted.clear();
						StringTokenizer st = new StringTokenizer(classNames);
						while (st.hasMoreTokens()) {
							sorted.add(st.nextToken());
						}
						if (!ownerClasses.containsAll(sorted)) {
							if (hasOwner) {
								list.updateOnRemove(owner);
							}
							continue;
						}
					}
					if (!hasOwner) {
						list.updateOnInsert(owner);
					}
				}
			} finally {
				classListLock.unlock();
			}
		}

		@Override
		public void updateClasslistsOnRemove(DOMElement oldChild, AbstractDOMNode removedFrom) {
			boolean appendThis = NDTNode.this == removedFrom;
			boolean descendant = isDescendant(removedFrom);
			if (classListMap != null && (appendThis || descendant)) {
				updateMyClasslistsOnRemove(oldChild);
			}
			if (!descendant) {
				DOMElement elm = oldChild.getFirstElementChild();
				while (elm != null) {
					oldChild.updateClasslistsOnRemove(elm, removedFrom);
					elm = elm.getNextElementSibling();
				}
			}
		}

		private void updateMyClasslistsOnRemove(DOMElement oldChild) {
			DOMTokenList oldClasses = oldChild.getClassList();
			String oldNames = oldClasses.getSortedValue();
			boolean hasSpace = oldNames.indexOf(' ') != -1;
			TreeSet<String> sorted = new TreeSet<String>();
			classListLock.lock();
			try {
				Iterator<String> it = classListMap.keySet().iterator();
				while (it.hasNext()) {
					String classNames = it.next();
					if (!classNames.equals(oldNames) && !hasSpace) {
						continue;
					}
					if (hasSpace) {
						sorted.clear();
						StringTokenizer st = new StringTokenizer(classNames);
						while (st.hasMoreTokens()) {
							sorted.add(st.nextToken());
						}
						if (!oldClasses.containsAll(sorted)) {
							continue;
						}
					}
					DOMElementLinkedList list = classListMap.get(classNames).get();
					if (list == null) {
						it.remove();
						continue;
					}
					list.updateOnRemove(oldChild);
				}
			} finally {
				classListLock.unlock();
			}
		}

		@Override
		void preRemoveChild(AbstractDOMNode removed) {
			NDTNode.this.preRemoveChild(removed);
		}

		@Override
		void postRemoveChild(AbstractDOMNode removed) {
			NDTNode.this.postRemoveChild(removed);
		}

		@Override
		void preAddChild(Node node) {
			NDTNode.this.preAddChild(node);
		}

		@Override
		void postAddChild(AbstractDOMNode node) {
			NDTNode.this.postAddChild(node);
		}

		@Override
		void replaceChild(Node newChild, Node oldChild) {
			NDTNode.this.replaceChild(newChild, oldChild);
		}

	}

}
