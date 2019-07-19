/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

class TreeWalkerImpl implements TreeWalker {

	private final AbstractDOMNode rootNode;
	private AbstractDOMNode currentNode;
	private boolean begin = true;

	private final int whatToShow;
	private final NodeFilter nodeFilter;

	TreeWalkerImpl(AbstractDOMNode rootNode, int whatToShow, NodeFilter filter) {
		super();
		this.rootNode = rootNode;
		this.nodeFilter = filter;
		this.whatToShow = whatToShow;
		currentNode = rootNode;
	}

	@Override
	public AbstractDOMNode getRoot() {
		return rootNode;
	}

	@Override
	public int getWhatToShow() {
		return whatToShow;
	}

	@Override
	public NodeFilter getNodeFilter() {
		return nodeFilter;
	}

	@Override
	public DOMNode getCurrentNode() {
		return currentNode;
	}

	@Override
	public void setCurrentNode(Node currentNode) {
		if (currentNode == null) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "current node cannot be null");
		}
		Node node = currentNode;
		while (currentNode != null) {
			if (node == rootNode) {
				this.currentNode = (AbstractDOMNode) currentNode;
				if (currentNode == node) {
					begin = true;
				}
				return;
			}
			node = node.getParentNode();
		}
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
				"This implementation does not support setting the current node outside of the root-based tree");
	}

	private boolean isToShow(Node node) {
		int maskBit = NodeFilter.maskTable[node.getNodeType() - 1];
		return (whatToShow & maskBit) == maskBit;
	}

	private short filter(Node node) {
		return nodeFilter == null ? NodeFilter.FILTER_ACCEPT : nodeFilter.acceptNode(node);
	}

	@Override
	public DOMNode nextNode() {
		AbstractDOMNode next;
		if (currentNode == rootNode) {
			if (!begin) {
				return null;
			}
			next = findNext(currentNode);
			if (next != rootNode) {
				currentNode = next;
				begin = false;
			} else {
				next = null;
			}
		} else {
			next = findNext();
			if (next != rootNode) {
				currentNode = next;
			} else {
				next = null;
			}
		}
		return next;
	}

	private AbstractDOMNode findNext() {
		AbstractDOMNode current = lowestVisibleAncestor(currentNode);
		return findNext(current);
	}

	private AbstractDOMNode findNext(AbstractDOMNode current) {
		AbstractDOMNode next = nextNode(current);
		while (next != rootNode) {
			short filter = filter(next);
			if (filter == NodeFilter.FILTER_ACCEPT) {
				if (isToShow(next)) {
					break;
				}
			} else if (filter != NodeFilter.FILTER_SKIP_NODE) {
				next = nextSiblingOrParent(next);
				continue;
			}
			next = nextNode(next);
		}
		return next;
	}

	/**
	 * Find whether the given node is the inclusive descendant of a rejected
	 * node (filtered as <code>NodeFilter.FILTER_SKIP_NODE_CHILD</code>), and
	 * in that case return such ancestor that is closest to root.
	 * 
	 * @param current the node to check.
	 * @return the same node, or the first logically visible ancestor.
	 */
	private AbstractDOMNode lowestVisibleAncestor(AbstractDOMNode current) {
		AbstractDOMNode node = current;
		while (node != rootNode) {
			if (filter(node) == NodeFilter.FILTER_SKIP_NODE_CHILD) {
				current = node;
			}
			node = (AbstractDOMNode) node.getParentNode();
			if (node == null) {
				break;
			}
		}
		return current;
	}

	private AbstractDOMNode nextNode(AbstractDOMNode current) {
		AbstractDOMNode next = current.getNodeList().getFirst();
		if (next == null) {
			if (current != rootNode || rootNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				next = nextSiblingOrParent(current);
			} else {
				next = current;
			}
		}
		return next;
	}

	private AbstractDOMNode nextSiblingOrParent(AbstractDOMNode current) {
		AbstractDOMNode next = current.nextSibling;
		if (next == null) {
			AbstractDOMNode parent = current.parentNode();
			if (parent != null) {
				if (parent != rootNode) {
					next = nextSiblingOrParent(parent);
				} else {
					next = rootNode;
				}
			} else {
				next = rootNode;
			}
		}
		return next;
	}

	private boolean isAccepted(AbstractDOMNode node) {
		return isToShow(node) && filter(node) == NodeFilter.FILTER_ACCEPT;
	}

	@Override
	public DOMNode previousNode() {
		AbstractDOMNode prev = findPrevious();
		if (prev != null) {
			if (prev != rootNode) {
				currentNode = prev;
			} else {
				if (isAccepted(prev)) {
					currentNode = prev;
					begin = true;
				} else {
					prev = null;
				}
			}
		}
		return prev;
	}

	private AbstractDOMNode findPrevious() {
		AbstractDOMNode current = lowestVisibleAncestor(currentNode);
		return findPrevious(current);
	}

	private AbstractDOMNode findPrevious(AbstractDOMNode current) {
		if (current == rootNode) {
			AbstractDOMNode node = null;
			if (!begin) {
				node = current.getNodeList().getLast();
				if (node != null && !isAccepted(node)) {
					node = findPrevious(node);
				}
			}
			return node;
		}
		AbstractDOMNode previous = previousNode(current);
		// The non-null check is for attributes
		while (previous != rootNode && previous != null) {
			short filter = filter(previous);
			if (filter == NodeFilter.FILTER_ACCEPT) {
				if (isToShow(previous)) {
					break;
				}
			}
			previous = previousNode(previous);
		}
		return previous;
	}

	private AbstractDOMNode previousNode(AbstractDOMNode current) {
		AbstractDOMNode previous = current.previousSibling;
		if (previous == null) {
			previous = current.parentNode();
		} else {
			AbstractDOMNode prev;
			// Find the deepest last child
			do {
				short filter = filter(previous);
				if (filter != NodeFilter.FILTER_SKIP_NODE_CHILD) {
					prev = previous.getNodeList().getLast();
					if (prev != null) {
						previous = prev;
					} else {
						break;
					}
				} else {
					prev = previous.previousSibling;
					if (prev != null) {
						previous = prev;
					} else {
						break;
					}
				}
			} while (true);
		}
		return previous;
	}

	@Override
	public DOMNode firstChild() {
		AbstractDOMNode node = findFirstChild();
		if (node != null) {
			currentNode = node;
		}
		return node;
	}

	private AbstractDOMNode findFirstChild() {
		AbstractDOMNode node;
		AbstractDOMNode current = lowestVisibleAncestor(currentNode);
		if (current == currentNode) {
			node = current.getNodeList().getFirst();
			while (node != null && !isAccepted(node)) {
				node = node.nextSibling;
			}
		} else {
			node = null;
		}
		return node;
	}

	@Override
	public DOMNode lastChild() {
		AbstractDOMNode node = findLastChild();
		if (node != null) {
			currentNode = node;
		}
		return node;
	}

	private AbstractDOMNode findLastChild() {
		AbstractDOMNode node;
		AbstractDOMNode current = lowestVisibleAncestor(currentNode);
		if (current == currentNode) {
			node = current.getNodeList().getLast();
			while (node != null && !isAccepted(node)) {
				node = node.previousSibling;
			}
		} else {
			node = null;
		}
		return node;
	}

	@Override
	public DOMNode nextSibling() {
		AbstractDOMNode node;
		if (currentNode != rootNode) {
			node = findNextSibling();
			if (node != null) {
				currentNode = node;
			}
		} else {
			node = null;
		}
		return node;
	}

	private AbstractDOMNode findNextSibling() {
		AbstractDOMNode node;
		AbstractDOMNode current = lowestVisibleAncestor(currentNode);
		if (current == currentNode) {
			node = current.nextSibling;
			while (node != null && !isAccepted(node)) {
				node = node.nextSibling;
			}
		} else {
			node = null;
		}
		return node;
	}

	@Override
	public DOMNode previousSibling() {
		AbstractDOMNode node;
		if (currentNode != rootNode) {
			node = findPreviousSibling();
			if (node != null) {
				currentNode = node;
			}
		} else {
			node = null;
		}
		return node;
	}

	private AbstractDOMNode findPreviousSibling() {
		AbstractDOMNode node;
		AbstractDOMNode current = lowestVisibleAncestor(currentNode);
		if (current == currentNode) {
			node = current.previousSibling;
			while (node != null && !isAccepted(node)) {
				node = node.previousSibling;
			}
		} else {
			node = null;
		}
		return node;
	}

	@Override
	public DOMNode parentNode() {
		AbstractDOMNode node = findParentNode();
		if (node != null) {
			if (node != rootNode) {
				// In this case, we know that the node is accepted
				currentNode = node;
			} else if (isAccepted(node)) {
				currentNode = node;
				begin = true;
			} else {
				node = null;
			}
		}
		return node;
	}

	private AbstractDOMNode findParentNode() {
		AbstractDOMNode node;
		if (currentNode != rootNode) {
			node = (AbstractDOMNode) currentNode.getParentNode();
			if (node != rootNode) {
				node = lowestVisibleAncestor(node);
				while (node != rootNode && !isAccepted(node)) {
					node = (AbstractDOMNode) node.getParentNode();
				}
			}
		} else {
			node = null;
		}
		return node;
	}

	/**
	 * If the filter implements <code>org.w3c.dom.traversal.NodeFilter</code>,
	 * return it.
	 * 
	 * @return the W3C-interfaced filter, <code>null</code> otherwise.
	 */
	@Override
	public org.w3c.dom.traversal.NodeFilter getFilter() {
		if (nodeFilter instanceof org.w3c.dom.traversal.NodeFilter) {
			return (org.w3c.dom.traversal.NodeFilter) nodeFilter;
		}
		return null;
	}

	@Override
	public boolean getExpandEntityReferences() {
		return false;
	}

}
