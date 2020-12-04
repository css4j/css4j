/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.text.Normalizer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import io.sf.carte.doc.style.css.CSSDocument;

/**
 * Abstract base class for DOM nodes.
 */
abstract class AbstractDOMNode implements DOMNode, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final short nodeType;

	private AbstractDOMNode parentNode = null;

	AbstractDOMNode previousSibling = null;
	AbstractDOMNode nextSibling = null;

	private Map<String, Object> userData = null;
	private Map<String, UserDataHandler> userDataHandler = null;

	static final RawNodeList emptyNodeList = new EmptyNodeList();

	AbstractDOMNode(short nodeType) {
		super();
		this.nodeType = nodeType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNodeValue() throws DOMException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short getNodeType() {
		return nodeType;
	}

	/**
	 * Gives the local part of the qualified name of this node. For nodes of any
	 * type other than <code>ELEMENT_NODE</code> and <code>ATTRIBUTE_NODE</code>,
	 * this is always null.
	 * 
	 * @return the local part of the qualified name of this node, or null if this
	 *         node is not an <code>ELEMENT_NODE</code> nor an
	 *         <code>ATTRIBUTE_NODE</code>.
	 */
	@Override
	public String getLocalName() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamedNodeMap getAttributes() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasAttributes() {
		return false;
	}

	/**
	 * Sets the parent of this node.
	 * <p>
	 * If called with a non-null argument, this method should be called after the
	 * child node list to which this node pertains has been updated.
	 * 
	 * @param parentNode this node's parent node.
	 */
	void setParentNode(AbstractDOMNode parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * For attributes, we reuse field parentNode to store the owner.
	 * 
	 * @param newOwner the owner.
	 */
	void setAttributeOwner(DOMElement newOwner) {
		this.parentNode = newOwner;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMNode getParentNode() {
		return parentNode;
	}

	AbstractDOMNode parentNode() {
		return parentNode;
	}

	/**
	 * 
	 * @return true if this node is a document node, or a descendant of a document
	 *         node.
	 */
	boolean isDocumentDescendant() {
		AbstractDOMNode node = this;
		while (true) {
			AbstractDOMNode parent = node.parentNode;
			if (parent == null) {
				return node.getNodeType() == Node.DOCUMENT_NODE;
			}
			node = parent;
		}
	}

	@Override
	public DOMNodeList getChildNodes() {
		return getNodeList();
	}

	RawNodeList getNodeList() {
		return emptyNodeList;
	}

	@Override
	public boolean hasChildNodes() {
		return false;
	}

	@Override
	public DOMNode getFirstChild() {
		return getNodeList().getFirst();
	}

	@Override
	public DOMNode getLastChild() {
		return getNodeList().getLast();
	}

	@Override
	public DOMNode getPreviousSibling() {
		return previousSibling;
	}

	@Override
	public DOMNode getNextSibling() {
		return nextSibling;
	}

	@Override
	public DOMNode appendChild(Node newChild) throws DOMException {
		AbstractDOMNode added = (AbstractDOMNode) newChild;
		if (newChild.getNodeType() != Node.DOCUMENT_FRAGMENT_NODE) {
			preAddChild(newChild);
			getNodeList().add(added);
			postAddChild(added);
		} else {
			appendDocumentFragment(newChild);
		}
		return added;
	}

	void preAddChild(Node newChild) {
		checkAppendNode(newChild);
		Node node = newChild.getParentNode();
		if (node != null) {
			node.removeChild(newChild);
		}
	}

	void checkAppendNode(Node newChild) {
		if (newChild.getNodeType() == Node.DOCUMENT_NODE) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot append a document.");
		}
		checkAppendNodeHierarchy(newChild);
		checkDocumentOwner(newChild);
	}

	void checkAppendNodeHierarchy(Node newChild) {
		if (newChild.getNodeType() == Node.ATTRIBUTE_NODE) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Use setAttributeNode to add attribute nodes.");
		}
		Node node = this;
		while (node != null) {
			if (node.isSameNode(newChild)) {
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot insert itself or an ancestor.");
			}
			node = node.getParentNode();
		}
	}

	void checkDocumentOwner(Node newChild) {
		if (newChild.getOwnerDocument() != getOwnerDocument()) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Different document owners.");
		}
	}

	void postAddChild(AbstractDOMNode newChild) {
		newChild.setParentNode(this);
	}

	private void appendDocumentFragment(Node newChild) {
		Node added = newChild.getFirstChild();
		while (added != null) {
			Node next = added.getNextSibling();
			appendChild(added);
			added = next;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMNode insertBefore(Node newChild, Node refChild) throws DOMException {
		AbstractDOMNode inserted = (AbstractDOMNode) newChild;
		AbstractDOMNode refNode = (AbstractDOMNode) refChild;
		if (refNode != null) {
			if (refNode != inserted) {
				if (!getNodeList().contains(refNode)) {
					throw new DOMException(DOMException.NOT_FOUND_ERR, "Not a child of this node.");
				}
				if (inserted.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
					insertDocumentFragment(inserted, refNode);
					return inserted;
				}
				preAddChild(inserted);
				getNodeList().insertBefore(inserted, refNode);
				postAddChild(inserted);
			}
		} else {
			appendChild(inserted);
		}
		return inserted;
	}

	private void insertDocumentFragment(AbstractDOMNode newChild, AbstractDOMNode refNode) {
		AbstractDOMNode curNode = (AbstractDOMNode) newChild.getFirstChild();
		while (curNode != null) {
			AbstractDOMNode next = curNode.nextSibling;
			insertBefore(curNode, refNode);
			curNode = next;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMNode replaceChild(Node newChild, Node oldChild) throws DOMException {
		int index = getNodeList().indexOf(oldChild);
		if (index == -1) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, "Not a child of this node.");
		}
		AbstractDOMNode replaced = (AbstractDOMNode) oldChild;
		AbstractDOMNode newNode = (AbstractDOMNode) newChild;
		if (replaced != newNode) {
			if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
				replaceWithDocumentFragment(newNode, replaced);
			} else {
				replaceWithNonDocumentFragment(newNode, replaced);
			}
			replaced.setParentNode(null);
			postRemoveChild(replaced);
		}
		return replaced;
	}

	private void replaceWithNonDocumentFragment(AbstractDOMNode newNode, AbstractDOMNode replaced) {
		preReplaceChild(newNode, replaced);
		replaced = getNodeList().replace(newNode, replaced);
		callUserHandlers(UserDataHandler.NODE_DELETED, replaced, null);
		newNode.setParentNode(this);
	}

	void preReplaceChild(AbstractDOMNode newNode, AbstractDOMNode replaced) {
		preAddChild(newNode);
	}

	private void replaceWithDocumentFragment(AbstractDOMNode newChild, AbstractDOMNode replaced) {
		AbstractDOMNode curNode = (AbstractDOMNode) newChild.getFirstChild();
		if (curNode != null) {
			AbstractDOMNode next = curNode.nextSibling;
			replaceWithNonDocumentFragment(curNode, replaced);
			AbstractDOMNode lastNode = curNode;
			curNode = next;
			while (curNode != null) {
				next = curNode.nextSibling;
				insertAfter(curNode, lastNode);
				lastNode = curNode;
				curNode = next;
			}
		}
	}

	void insertAfter(AbstractDOMNode newNode, AbstractDOMNode refNode) {
		preAddChild(newNode);
		AbstractDOMNode next = refNode.nextSibling;
		if (next != null) {
			getNodeList().insertBefore(newNode, next);
		} else {
			getNodeList().add(newNode);
		}
		postAddChild(newNode);
	}

	@Override
	public DOMNode removeChild(Node oldChild) throws DOMException {
		if (!getNodeList().contains(oldChild)) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, "Not a child.");
		}
		AbstractDOMNode removed = (AbstractDOMNode) oldChild;
		removed.removeFromParent(getNodeList());
		postRemoveChild(removed);
		return removed;
	}

	void postRemoveChild(AbstractDOMNode removed) {
	}

	/**
	 * Remove this node from the given node list, setting the parent node to {@code null}.
	 * 
	 * @param nodeList the child node list containing this node.
	 */
	void removeFromParent(RawNodeList nodeList) {
		setParentNode(null);
		nodeList.remove(this);
		callUserHandlers(UserDataHandler.NODE_DELETED, this, null);
	}

	static void callUserHandlers(short operation, AbstractDOMNode child, Node destNode) {
		if (child.userDataHandler != null) {
			Iterator<Entry<String, UserDataHandler>> it = child.userDataHandler.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, UserDataHandler> entry = it.next();
				String key = entry.getKey();
				UserDataHandler handler = entry.getValue();
				if (handler != null) {
					handler.handle(operation, key, child.userData.get(key), child, destNode);
				}
			}
		}
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		lazyUserData();
		Object old_data = userData.get(key);
		userData.put(key, data);
		userDataHandler.put(key, handler);
		return old_data;
	}

	private void lazyUserData() {
		if (userData == null) {
			userData = new HashMap<String, Object>(2);
			userDataHandler = new HashMap<String, UserDataHandler>(2);
		}
	}

	@Override
	public Object getUserData(String key) {
		return userData != null ? userData.get(key) : null;
	}

	@Override
	public void normalize() {
		Node node = getFirstChild();
		short lasttype = Node.ELEMENT_NODE;
		CSSDOMConfiguration config = getOwnerDocument().domConfig;
		byte isWhitespacePre = 0; // 0 = not determined, 1 = yes, 2 = no
		if (!config.useComputedStyles) {
			isWhitespacePre = 2;
		}
		if (!config.cssWhitespaceProcessing) {
			isWhitespacePre = 1;
		}
		Node lastnode = null;
		while (node != null) {
			Node next = node.getNextSibling();
			short type = node.getNodeType();
			if (type == Node.TEXT_NODE) {
				Text text = (Text) node;
				String data = text.getData();
				// Remove empty nodes (null value is supposed to not reach this)
				if (data.length() == 0) {
					((AbstractDOMNode) text).removeFromParent(getNodeList());
					node = next;
					continue;
				}
				boolean isECW = config.cssWhitespaceProcessing && text.isElementContentWhitespace();
				if (isECW && isWhitespacePre == 0) {
					if (isWhitespacePre()) {
						isWhitespacePre = 1;
						isECW = false;
					} else {
						isWhitespacePre = 2;
					}
				}
				if (isECW) {
					if (lastnode == null) {
						// This is ECW and is the first node: remove
						((AbstractDOMNode) node).removeFromParent(getNodeList());
						node = next;
						continue;
					} else if (next == null) {
						// We got the last node.
						// Remove trailing ECW Text: first, remove current iteration node
						((AbstractDOMNode) node).removeFromParent(getNodeList());
						// Now, remove previous ECW siblings
						while (lastnode != null && lastnode.getNodeType() == Node.TEXT_NODE
								&& ((Text) lastnode).isElementContentWhitespace()) {
							((AbstractDOMNode) lastnode).removeFromParent(getNodeList());
							lastnode = lastnode.getPreviousSibling();
						}
						break;
					}
					// Normalize ECW as a single whitespace
					text.setData(" ");
					data = " ";
				} else if (config.normalizeCharacters) {
					// Unicode normalization
					String normalized = Normalizer.normalize(data, Normalizer.Form.NFC);
					// Normalization may return the same String
					if (data != normalized) {
						text.setData(normalized);
						data = normalized;
					}
				}
				if (lasttype == Node.TEXT_NODE) {
					// coalesce nodes
					Text prevText = (Text) lastnode;
					if (!prevText.isElementContentWhitespace() || isWhitespacePre == 1
							|| (isWhitespacePre == 0 && (isWhitespacePre = whitespacePre()) == 1)) {
						if (!isECW || (next != null && (next.getNodeType() != Node.TEXT_NODE
								|| !((Text) next).isElementContentWhitespace()))) {
							prevText.setData(prevText.getData() + data);
						}
					} else if (!isECW) {
						prevText.setData(" " + data);
					} // If both this Text and previous one are ECW, do nothing and let it be removed
					((AbstractDOMNode) node).removeFromParent(getNodeList());
					node = next;
					continue;
				}
			} else if (type == Node.ELEMENT_NODE) {
				// Normalize subtree
				node.normalize();
			} else if (type == Node.COMMENT_NODE && !config.keepComments) {
				((AbstractDOMNode) node).removeFromParent(getNodeList());
			}
			lasttype = type;
			lastnode = node;
			node = next;
		}
	}

	private byte whitespacePre() {
		return isWhitespacePre() ? (byte) 1 : 2;
	}

	private boolean isWhitespacePre() {
		if (getNodeType() == Node.ELEMENT_NODE) {
			String value = ((DOMElement) this).getComputedStyle(null).getPropertyValue("white-space");
			return "pre".equalsIgnoreCase(value);
		} else {
			return false;
		}
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
	@Deprecated
	@Override
	public boolean isSupported(String feature, String version) {
		return true;
	}

	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
		if (other == this) {
			return 0;
		}
		if (getOwnerDocument() != other.getOwnerDocument()) {
			return Node.DOCUMENT_POSITION_DISCONNECTED;
		}
		short mask = 0;
		// Check for ancestor
		short mydepth = 1, otherdepth = 1;
		Node node = getParentNode();
		while (node != null) {
			if (node.equals(other)) {
				mask += Node.DOCUMENT_POSITION_CONTAINED_BY;
				mask += Node.DOCUMENT_POSITION_FOLLOWING;
				break;
			}
			mydepth++;
			node = node.getParentNode();
		}
		if (mask == 0) {
			node = other.getParentNode();
			while (node != null) {
				if (node.equals(this)) {
					mask += Node.DOCUMENT_POSITION_CONTAINS;
					mask += Node.DOCUMENT_POSITION_PRECEDING;
					break;
				}
				otherdepth++;
				node = node.getParentNode();
			}
		}
		if (mask == 0) {
			short depth;
			if (mydepth > otherdepth) {
				depth = otherdepth;
			} else {
				depth = mydepth;
			}
			int[] myindex = new int[depth];
			int[] otherindex = new int[depth];
			computeIndexArray(this, myindex);
			computeIndexArray((AbstractDOMNode) other, otherindex);
			for (short k = 0; k < depth; k++) {
				if (myindex[k] == otherindex[k]) {
					continue;
				}
				if (myindex[k] > otherindex[k]) {
					return Node.DOCUMENT_POSITION_FOLLOWING;
				}
				return Node.DOCUMENT_POSITION_PRECEDING;
			}
		}
		return mask;
	}

	private void computeIndexArray(AbstractDOMNode node, int[] myindex) {
		AbstractDOMNode parent = (AbstractDOMNode) node.getParentNode();
		int i = myindex.length - 1;
		while (parent != null) {
			myindex[i] = parent.getNodeList().indexOf(node);
			node = parent;
			parent = (AbstractDOMNode) node.getParentNode();
			i--;
		}
	}

	@Override
	public String getTextContent() throws DOMException {
		String text;
		switch (getNodeType()) {
		case Node.ELEMENT_NODE:
		case Node.ENTITY_NODE:
		case Node.ENTITY_REFERENCE_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
			int sz = getNodeList().getLength();
			if (sz == 0) {
				text = "";
			} else if (sz == 1) {
				text = getNodeList().getFirst().getTextContent();
			} else {
				StringBuilder buf = new StringBuilder(32 + sz * 20);
				Node node = getFirstChild();
				while (node != null) {
					short type = node.getNodeType();
					if (type == Node.COMMENT_NODE || type == Node.PROCESSING_INSTRUCTION_NODE) {
						node = node.getNextSibling();
						continue;
					}
					String tc = node.getTextContent();
					if (tc != null) {
						buf.append(tc);
					}
					node = node.getNextSibling();
				}
				text = buf.toString();
			}
			break;
		case Node.TEXT_NODE:
			if (((Text) this).isElementContentWhitespace()) {
				text = "";
			} else {
				text = getNodeValue();
			}
			break;
		case Node.CDATA_SECTION_NODE:
		case Node.COMMENT_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
		case Node.ATTRIBUTE_NODE:
			text = getNodeValue();
			break;
		default:
			text = null;
		}
		return text;
	}

	@Override
	public void setTextContent(String textContent) throws DOMException {
		getNodeList().clear();
		if (textContent != null && textContent.length() != 0) {
			AbstractDOMNode text = (AbstractDOMNode) getOwnerDocument().createTextNode(textContent);
			getNodeList().add(text);
			text.setParentNode(this);
		}
	}

	/*
	 * Namespace-related methods
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNamespaceURI() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrefix() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPrefix(String prefix) throws DOMException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String lookupNamespaceURI(String prefix) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String lookupPrefix(String namespaceURI) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	 * Document nodes MUST override this method
	 */
	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		return getOwnerDocument().isDefaultNamespace(namespaceURI);
	}

	/*
	 * Generic methods (comparisons, cloning)
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEqualNode(Node arg) {
		if (arg != null) {
			if (getNodeType() == arg.getNodeType() && getNodeName().equals(arg.getNodeName())
					&& stringEquals(getLocalName(), arg.getLocalName())
					&& stringEquals(getNamespaceURI(), arg.getNamespaceURI())
					&& stringEquals(getPrefix(), arg.getPrefix()) && stringEquals(getNodeValue(), arg.getNodeValue())) {
				if (getNodeList().getLength() == arg.getChildNodes().getLength()) {
					Node node = getFirstChild();
					Node othernode = arg.getFirstChild();
					while (node != null) {
						if (!node.isEqualNode(othernode)) {
							return false;
						}
						node = node.getNextSibling();
						othernode = othernode.getNextSibling();
					}
					NamedNodeMap nmap = getAttributes();
					NamedNodeMap othernmap = arg.getAttributes();
					if (nmap == null) {
						return othernmap == null;
					} else {
						int sz = nmap.getLength();
						if (sz == othernmap.getLength()) {
							extloop: for (int i = 0; i < sz; i++) {
								Attr attr = (Attr) nmap.item(i);
								for (int j = 0; j < sz; j++) {
									Attr otherAttr = (Attr) othernmap.item(j);
									if (attr.isEqualNode(otherAttr) && attr.isId() == otherAttr.isId()) {
										continue extloop;
									}
								}
								return false;
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean stringEquals(String str1, String str2) {
		if (str1 == str2) {
			return true;
		}
		if (str1 == null) {
			return false;
		}
		return str1.equals(str2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSameNode(Node other) {
		return other == this;
	}

	/**
	 * Test if <code>node</code> is an inclusive descendant of this node.
	 * 
	 * @param node the node to test.
	 * @return <code>true</code> if <code>node</code> is an inclusive descendant of
	 *         this node, <code>false</code> otherwise (including <code>node</code>
	 *         being <code>null</code>).
	 */
	public boolean contains(Node node) {
		Node n = node;
		do {
			if (n == this) {
				return true;
			}
			n = n.getParentNode();
		} while (n != null);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	abstract public DOMDocument getOwnerDocument();

	/*
	 * This interface is for package use only, and thus it is not defined in its own
	 * file.
	 */
	interface RawNodeList extends DOMNodeList {

		/**
		 * Appends the specified node to the end of this list.
		 * <p>
		 * This method only provides part of the checks that should be made on the node
		 * before it is added.
		 * 
		 * @param node the node to be appended.
		 * @throws DOMException HIERARCHY_REQUEST_ERR if the node cannot be added to
		 *                      this list.
		 */
		void add(AbstractDOMNode node) throws DOMException;

		/**
		 * Insert <code>newChild</code> before <code>refChild</code>. Node
		 * <code>refChild</code> cannot be <code>null</code>, and cannot be equal to
		 * <code>newChild</code>.
		 * 
		 * @param newChild
		 * @param refChild
		 */
		void insertBefore(AbstractDOMNode newChild, AbstractDOMNode refChild);

		/**
		 * Removes all the nodes from this list, without calling user handlers nor doing
		 * any element-related handling.
		 * <p>
		 * This operation is only safe if performed on a list that has no elements nor
		 * document fragments.
		 */
		void clear();

		/**
		 * Get the first child.
		 * 
		 * @return the first child, or null if the list is empty.
		 */
		AbstractDOMNode getFirst();

		/**
		 * Get the last child.
		 * 
		 * @return the last child, or null if the list is empty.
		 */
		AbstractDOMNode getLast();

		/**
		 * Returns the index of the first occurrence of the specified node in this list,
		 * or -1 if this list does not contain the node.
		 * 
		 * @param node the node to search for.
		 * @return the index of the first occurrence of the specified element in this
		 *         list, or -1 if this list does not contain the element.
		 */
		int indexOf(Node node);

		boolean isEmpty();

		/**
		 * Removes the first occurrence of the specified node from this list.
		 * 
		 * @param node the node to be removed.
		 */
		void remove(AbstractDOMNode node);

		/**
		 * Replaces the node oldChild in this list with the newChild node.
		 * 
		 * @param newChild the node to replace oldChild.
		 * @param oldChild index of the node to replace.
		 * @return the replaced node.
		 */
		AbstractDOMNode replace(AbstractDOMNode newChild, AbstractDOMNode oldChild);

		Iterator<DOMElement> elementIterator();

		Iterator<DOMElement> elementIterator(String name) throws DOMException;

		Iterator<DOMElement> elementIteratorNS(String namespaceURI, String localName);

		Iterator<Attr> attributeIterator();

	}

	interface ChildCollections extends RawNodeList {

		ElementList getChildren();

		Iterator<DOMNode> createDescendingIterator();

		Iterator<DOMNode> createIterator(BitSet whatToShow);

		Iterator<DOMNode> createIterator(int whatToShow, NodeFilter filter);

		NodeListIterator createListIterator();

		ElementList getElementsByTagNameNS(String namespaceURI, String localName);

		ElementList getElementsByTagName(String name, boolean isHTML);

		ElementList getElementsByClassName(String names, CSSDocument.ComplianceMode mode);

	}

}
