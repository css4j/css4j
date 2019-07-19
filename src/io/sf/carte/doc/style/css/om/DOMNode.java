/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

import io.sf.carte.doc.style.css.CSSNode;

/**
 * Abstract base class for wrapped nodes.
 * 
 * @author Carlos Amengual
 *
 */
abstract class DOMNode implements CSSNode {

	protected Node rawnode;

	DOMNode(Node node) {
		super();
		this.rawnode = node;
	}

	@Override
	public String getNodeName() {
		return rawnode.getNodeName();
	}

	@Override
	public String getNodeValue() throws DOMException {
		return rawnode.getNodeValue();
	}

	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
		rawnode.setNodeValue(nodeValue);
	}

	@Override
	public short getNodeType() {
		return rawnode.getNodeType();
	}

	@Override
	public CSSNode getParentNode() {
		Node parent = rawnode.getParentNode();
		if (parent == null) {
			return null;
		}
		return getCSSNode(parent);
	}

	@Override
	public NodeList getChildNodes() {
		return new MyNodeList(rawnode.getChildNodes());
	}

	@Override
	public CSSNode getFirstChild() {
		Node parent = rawnode.getFirstChild();
		if (parent == null) {
			return null;
		}
		return getCSSNode(parent);
	}

	@Override
	public CSSNode getLastChild() {
		Node parent = rawnode.getLastChild();
		if (parent == null) {
			return null;
		}
		return getCSSNode(parent);
	}

	@Override
	public CSSNode getPreviousSibling() {
		Node parent = rawnode.getPreviousSibling();
		if (parent == null) {
			return null;
		}
		return getCSSNode(parent);
	}

	@Override
	public CSSNode getNextSibling() {
		Node parent = rawnode.getNextSibling();
		if (parent == null) {
			return null;
		}
		return getCSSNode(parent);
	}

	@Override
	public NamedNodeMap getAttributes() {
		NamedNodeMap nnm = rawnode.getAttributes();
		return nnm != null ? new MyNamedNodeMap(nnm) : null;
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public boolean hasChildNodes() {
		return rawnode.hasChildNodes();
	}

	@Override
	public Node cloneNode(boolean deep) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public void normalize() {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public boolean isSupported(String feature, String version) {
		return rawnode.isSupported(feature, version);
	}

	@Override
	public String getNamespaceURI() {
		return rawnode.getNamespaceURI();
	}

	@Override
	public String getPrefix() {
		return rawnode.getPrefix();
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public String getLocalName() {
		return rawnode.getLocalName();
	}

	@Override
	public boolean hasAttributes() {
		return rawnode.hasAttributes();
	}

	@Override
	public String getBaseURI() {
		return rawnode.getBaseURI();
	}

	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
		return rawnode.compareDocumentPosition(other);
	}

	@Override
	public String getTextContent() throws DOMException {
		return rawnode.getTextContent();
	}

	@Override
	public void setTextContent(String textContent) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This is a readonly wrapper.");
	}

	@Override
	public boolean isSameNode(Node other) {
		return equals(other);
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		return rawnode.lookupPrefix(namespaceURI);
	}

	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		return rawnode.isDefaultNamespace(namespaceURI);
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		return rawnode.lookupNamespaceURI(prefix);
	}

	@Override
	public boolean isEqualNode(Node arg) {
		return rawnode.isEqualNode(arg);
	}

	@Override
	public Object getFeature(String feature, String version) {
		return null;
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return rawnode.setUserData(key, data, handler);
	}

	@Override
	public Object getUserData(String key) {
		return rawnode.getUserData(key);
	}

	abstract CSSNode getCSSNode(Node node);

	abstract CSSNode getMappedCSSNode(Node node);

	protected class MyNodeList implements NodeList {

		private NodeList nodelist;

		public MyNodeList(NodeList nlist) {
			super();
			nodelist = nlist;
		}

		@Override
		public CSSNode item(int index) {
			Node nd = nodelist.item(index);
			if (nd == null) {
				return null;
			}
			return getCSSNode(nd);
		}

		@Override
		public int getLength() {
			return nodelist.getLength();
		}

	}

	protected class MyNamedNodeMap implements NamedNodeMap {

		private NamedNodeMap map;

		public MyNamedNodeMap(NamedNodeMap map) {
			super();
			this.map = map;
		}

		@Override
		public Node getNamedItem(String name) {
			Node item = map.getNamedItem(name);
			if (item != null && !(rawnode instanceof CSSNode)) {
				item = getCSSNode(item);
			}
			return item;
		}

		@Override
		public Node setNamedItem(Node arg) throws DOMException {
			Node node = map.setNamedItem(arg);
			if (node != null && !(node instanceof CSSNode)) {
				node = getCSSNode(node);
			}
			return node;
		}

		@Override
		public Node removeNamedItem(String name) throws DOMException {
			Node node = map.removeNamedItem(name);
			if (node != null && !(node instanceof CSSNode)) {
				node = getMappedCSSNode(node);
			}
			return node;
		}

		@Override
		public Node item(int index) {
			Node node = map.item(index);
			if (node != null && !(node instanceof CSSNode)) {
				node = getCSSNode(node);
			}
			return node;
		}

		@Override
		public int getLength() {
			return map.getLength();
		}

		@Override
		public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
			Node node = map.getNamedItemNS(namespaceURI, localName);
			if (node != null && !(node instanceof CSSNode)) {
				node = getCSSNode(node);
			}
			return node;
		}

		@Override
		public Node setNamedItemNS(Node arg) throws DOMException {
			Node node = map.setNamedItemNS(arg);
			if (node != null && !(node instanceof CSSNode)) {
				node = getCSSNode(node);
			}
			return node;
		}

		@Override
		public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
			Node node = map.removeNamedItemNS(namespaceURI, localName);
			if (node != null && !(node instanceof CSSNode)) {
				node = getMappedCSSNode(node);
			}
			return node;
		}

	}

}
