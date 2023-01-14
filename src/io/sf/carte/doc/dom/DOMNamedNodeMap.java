/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

abstract class DOMNamedNodeMap<T extends AbstractDOMNode> implements NamedNodeMap, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final short nodeType;
	private final HashMap<String, T> attributeMap;
	private final AbstractDOMNode.RawNodeList attributes;

	DOMNamedNodeMap(short nodeType) {
		super();
		this.nodeType = nodeType;
		attributeMap = new HashMap<String, T>();
		attributes = new MyLinkedNodeList();
	}

	AbstractDOMNode.RawNodeList getNodeList() {
		return attributes;
	}

	private static class MyLinkedNodeList extends LinkedNodeList {

		private static final long serialVersionUID = 1L;

		@Override
		void preAddChild(Node node) {
		}

		@Override
		void postAddChild(AbstractDOMNode node) {
		}

		@Override
		void replaceChild(Node newChild, Node oldChild) {
		}

		@Override
		void postRemoveChild(AbstractDOMNode removed) {
		}

	}

	@Override
	public T getNamedItem(String name) {
		T ret = attributeMap.get(name);
		if (ret == null && name != null && name.indexOf(':') == -1) {
			name = name.toLowerCase(Locale.ROOT);
			ret = attributeMap.get(name);
			if (ret != null && HTMLDocument.HTML_NAMESPACE_URI != ret.getNamespaceURI()
					&& HTMLDocument.HTML_NAMESPACE_URI != getOwnerNode().getNamespaceURI()) {
				ret = null;
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T setNamedItem(Node arg) throws DOMException {
		verifyNewNode(arg);
		T node = (T) arg;
		String name = getMapKeyName(node);
		T oldNode = attributeMap.get(name);
		if (oldNode != null) {
			if (oldNode == arg) {
				return null;
			}
			// Check whether namespace is the same
			if (isSameNamespace(node.getNamespaceURI(), oldNode) || node.getNamespaceURI() == null) {
				attributes.replace(node, oldNode);
			} else {
				throw new DOMException(DOMException.NAMESPACE_ERR, "Bad prefix in " + arg.getNodeName());
			}
		} else {
			attributes.add(node);
		}
		T oldItem = attributeMap.put(name, node);
		registerNode(node);
		return oldItem;
	}

	void setNamedItemUnchecked(Node arg) {
		@SuppressWarnings("unchecked")
		T node = (T) arg;
		String name = getMapKeyName(node);
		attributes.add(node);
		attributeMap.put(name, node);
		registerNode(node);
	}

	private String getMapKeyName(Node node) {
		return node.getNodeName();
	}

	void registerNode(T arg) {
	}

	void verifyNewNode(Node arg) throws DOMException {
		if (getOwnerNode().getOwnerDocument() != arg.getOwnerDocument()) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Node was created by different document.");
		}
	}

	@Override
	public T removeNamedItem(String name) throws DOMException {
		if (!attributeMap.containsKey(name)) {
			if (name != null && name.indexOf(':') == -1) {
				name = name.toLowerCase(Locale.ROOT);
			}
			if (!attributeMap.containsKey(name)) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "No attribute with that name: " + name);
			}
		}
		T removedItem = attributeMap.remove(name);
		unregisterNode(removedItem);
		attributes.remove(removedItem);
		return removedItem;
	}

	T removeItem(Node node) {
		String name = getMapKeyName(node);
		T retval = attributeMap.remove(name);
		if (retval == null) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute from this collection.");
		}
		unregisterNode(retval);
		attributes.remove((AbstractDOMNode) node);
		return retval;
	}

	void replaceItem(T newNode, T old) {
		verifyNewNode(newNode);
		unregisterNode(old);
		attributes.replace(newNode, old);
		String name = getMapKeyName(old);
		attributeMap.remove(name);
		name = getMapKeyName(newNode);
		attributeMap.put(name, newNode);
		registerNode(newNode);
	}

	void unregisterNode(T removedItem) {
	}

	boolean hasAttribute(String lcname) {
		return attributeMap.containsKey(lcname);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T item(int index) {
		return (T) attributes.item(index);
	}

	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	@Override
	public int getLength() {
		return attributeMap.size();
	}

	@SuppressWarnings("unchecked")
	void updatePrefix(Node node, String newPrefix, String oldPrefix) {
		String name = node.getLocalName();
		if (oldPrefix != null && oldPrefix.length() != 0) {
			name = oldPrefix + ':' + name;
		}
		Iterator<DOMNode> it = attributes.iterator();
		while (it.hasNext()) {
			DOMNode n = it.next();
			if (n == node) {
				attributeMap.remove(name);
				attributeMap.put(node.getNodeName(), (T) node);
			}
		}
	}

	@Override
	public T getNamedItemNS(String namespaceURI, String localName) throws DOMException {
		if (HTMLDocument.HTML_NAMESPACE_URI.equals(namespaceURI)
				|| (namespaceURI == null && HTMLDocument.HTML_NAMESPACE_URI == getOwnerNode().getNamespaceURI())) {
			return getCINamedItem(namespaceURI, localName);
		}
		return getCSNamedItem(namespaceURI, localName);
	}

	private T getCINamedItem(String namespaceURI, String localName) {
		Iterator<DOMNode> it = attributes.iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			if (node.getLocalName().equalsIgnoreCase(localName) && isSameNamespace(namespaceURI, node)) {
				@SuppressWarnings("unchecked")
				T node2 = (T) node;
				return node2;
			}
		}
		return null;
	}

	private T getCSNamedItem(String namespaceURI, String localName) {
		Iterator<DOMNode> it = attributes.iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			if (node.getLocalName().equals(localName) && isSameNamespace(namespaceURI, node)) {
				@SuppressWarnings("unchecked")
				T node2 = (T) node;
				return node2;
			}
		}
		return null;
	}

	@Override
	public T setNamedItemNS(Node arg) throws DOMException {
		return setNamedItem(arg);
	}

	@Override
	public T removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
		if (HTMLDocument.HTML_NAMESPACE_URI.equals(namespaceURI)
				|| (namespaceURI == null && HTMLDocument.HTML_NAMESPACE_URI == getOwnerNode().getNamespaceURI())) {
			return removeCINamedItem(namespaceURI, localName);
		}
		return removeCSNamedItem(namespaceURI, localName);
	}

	private T removeCINamedItem(String namespaceURI, String localName) {
		Iterator<DOMNode> it = attributes.iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			if (node.getLocalName().equalsIgnoreCase(localName) && isSameNamespace(namespaceURI, node)) {
				attributes.remove((AbstractDOMNode) node);
				T retval = attributeMap.remove(node.getNodeName());
				unregisterNode(retval);
				return retval;
			}
		}
		return null;
	}

	private T removeCSNamedItem(String namespaceURI, String localName) {
		Iterator<DOMNode> it = attributes.iterator();
		while (it.hasNext()) {
			DOMNode node = it.next();
			if (node.getLocalName().equals(localName) && isSameNamespace(namespaceURI, node)) {
				attributes.remove((AbstractDOMNode) node);
				T retval = attributeMap.remove(node.getNodeName());
				unregisterNode(retval);
				return retval;
			}
		}
		return null;
	}

	void insertAfter(T newNode, AbstractDOMNode refNode) {
		AbstractDOMNode next = refNode.nextSibling;
		if (next != null) {
			attributes.insertBefore(newNode, next);
		} else {
			attributes.add(newNode);
		}
		String name = getMapKeyName(newNode);
		attributeMap.put(name, newNode);
		registerNode(newNode);
	}

	private boolean isSameNamespace(String namespaceURI, Node memberNode) {
		return Objects.equals(namespaceURI, memberNode.getNamespaceURI());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = nodeType;
		SortedSet<String> sorted = new TreeSet<String>(attributeMap.keySet());
		Iterator<String> it = sorted.iterator();
		while (it.hasNext()) {
			String key = it.next();
			result = prime * result + key.hashCode();
			result = prime * result + attributeMap.get(key).hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DOMNamedNodeMap<?> other = (DOMNamedNodeMap<?>) obj;
		if (nodeType != other.nodeType)
			return false;
		if (attributeMap == null) {
			return other.attributeMap == null;
		} else {
			return sameNodes(other.attributeMap);
		}
	}

	private boolean sameNodes(HashMap<String, ?> otherAttr) {
		if (attributeMap.size() != otherAttr.size()) {
			return false;
		}
		Iterator<Entry<String, T>> it = attributeMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, T> me = it.next();
			String key = me.getKey();
			Node node = me.getValue();
			Node otherNode = (Node) otherAttr.get(key);
			if (!node.isEqualNode(otherNode)) {
				return false;
			}
		}
		return true;
	}

	public void appendTo(StringBuilder buf) {
		Iterator<DOMNode> it = attributes.iterator();
		if (it.hasNext()) {
			DOMNode node = it.next();
			buf.append(node.toString());
			while (it.hasNext()) {
				node = it.next();
				buf.append(' ').append(node.toString());
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(attributeMap.size() * 24 + 12);
		appendTo(buf);
		return buf.toString();
	}

	abstract Node getOwnerNode();

}
