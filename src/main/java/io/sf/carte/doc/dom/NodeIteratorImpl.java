/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import java.util.NoSuchElementException;

import org.w3c.dom.Node;

import io.sf.carte.doc.DOMHierarchyRequestException;

class NodeIteratorImpl implements NodeIterator, org.w3c.dom.traversal.NodeIterator {

	private final AbstractDOMNode rootNode;
	private AbstractDOMNode currentNode;
	private AbstractDOMNode last = null;
	private int currentIndex = 0;

	private final int whatToShow;
	private final NodeFilter nodeFilter;

	NodeIteratorImpl(AbstractDOMNode rootNode, int whatToShow, NodeFilter filter) {
		super();
		this.rootNode = rootNode;
		this.nodeFilter = filter;
		this.whatToShow = whatToShow;
		currentNode = null;
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
	public boolean hasNext() {
		AbstractDOMNode next = findNext();
		return next != rootNode || (currentNode == null && (isAccepted(next) || findNext(next) != rootNode));
	}

	private AbstractDOMNode findNext() {
		if (currentNode == null) {
			return rootNode;
		}
		return findNext(currentNode);
	}

	private AbstractDOMNode findNext(AbstractDOMNode current) {
		AbstractDOMNode next = nextNode(current);
		while (next != rootNode) {
			short filter = filter(next);
			if (filter == NodeFilter.FILTER_ACCEPT) {
				break;
			} else if (filter != NodeFilter.FILTER_SKIP_NODE) {
				next = nextSiblingOrParent(next);
				continue;
			}
			next = nextNode(next);
		}
		return next;
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
			if (parent != rootNode && parent != null) {
				return nextSiblingOrParent(parent);
			}
			next = rootNode;
		}
		return next;
	}

	private boolean isToShow(Node node) {
		int maskBit = NodeFilter.maskTable[node.getNodeType() - 1];
		return (whatToShow & maskBit) == maskBit;
	}

	private short filter(Node node) {
		if (!isToShow(node)) {
			return NodeFilter.FILTER_SKIP_NODE;
		}
		return nodeFilter == null ? NodeFilter.FILTER_ACCEPT : nodeFilter.acceptNode(node);
	}

	@Override
	public DOMNode next() {
		AbstractDOMNode next = findNext();
		if (next != rootNode || (currentNode == null && (isAccepted(next) || (next = findNext(next)) != rootNode))) {
			currentIndex++;
			last = next;
			currentNode = next;
			return next;
		}
		throw new NoSuchElementException();
	}

	private boolean isAccepted(AbstractDOMNode node) {
		return filter(node) == NodeFilter.FILTER_ACCEPT;
	}

	@Override
	public DOMNode nextNode() {
		try {
			return next();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public boolean hasPrevious() {
		return currentNode != null;
	}

	private AbstractDOMNode findPrevious() {
		AbstractDOMNode previous;
		if (currentNode != rootNode) {
			previous = previousNode(currentNode);
			while (previous != rootNode) {
				short filter = filter(previous);
				if (filter == NodeFilter.FILTER_ACCEPT) {
					break;
				}
				previous = previousNode(previous);
			}
		} else {
			previous = null;
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
	public DOMNode previous() {
		if (!hasPrevious()) {
			throw new NoSuchElementException();
		}
		currentIndex--;
		last = currentNode;
		AbstractDOMNode prev = findPrevious();
		if (prev != rootNode || isAccepted(prev)) {
			currentNode = prev;
		} else {
			currentNode = null;
		}
		return last;
	}

	@Override
	public DOMNode previousNode() {
		try {
			return previous();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public int nextIndex() {
		return currentIndex;
	}

	@Override
	public int previousIndex() {
		return currentIndex - 1;
	}

	@Override
	public void remove() {
		if (last != null && last != rootNode) {
			AbstractDOMNode parent = last.parentNode();
			if (parent == null) {
				throw new IllegalStateException("Node to remove has no parent.");
			}
			if (currentNode != rootNode) {
				if (currentNode != last) {
					if (last != findNext()) {
						// List was modified elsewhere
						throw new IllegalStateException();
					}
					// previous() was called last time
				} else {
					// next() was called last time
					currentNode = previousNode(last);
					currentIndex--;
				}
			} else if (last != findNext()) {
				// The list was modified outside of this iterator
				throw new IllegalStateException();
			} // previous() was called last time (no further action required)
			if (last.getNodeType() != Node.ATTRIBUTE_NODE) {
				parent.removeChild(last);
			} else {
				parent.getAttributes().removeNamedItemNS(last.getNamespaceURI(), last.getLocalName());
			}
			last = null;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void set(Node node) {
		AbstractDOMNode parent; // parent or owner
		if (last != null && last != rootNode && (parent = last.parentNode()) != null) {
			AbstractDOMNode newNode = (AbstractDOMNode) node;
			if (last.getNodeType() != Node.ATTRIBUTE_NODE) {
				parent.replaceChild(node, last);
				/*
				 * Now that it is part of the tree, check that it is accepted
				 */
				if (!isAccepted(newNode)) {
					// Roll back
					parent.replaceChild(last, newNode);
					throw new IllegalArgumentException("Not part of the logical tree.");
				}
			} else {
				if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
					throw new DOMHierarchyRequestException("Not an attribute.");
				}
				DOMNamedNodeMap<DOMAttr> nnm = ((DOMElement) parent).nodeMap;
				nnm.replaceItem((DOMAttr) node, (DOMAttr) last);
				/*
				 * Now that it is part of the tree, check that it is accepted
				 */
				if (!isAccepted(newNode)) {
					// Roll back
					nnm.replaceItem((DOMAttr) last, (DOMAttr) node);
					throw new IllegalArgumentException("Not part of the logical tree.");
				}
			}
			if (currentNode == last) {
				currentNode = newNode;
			}
			last = null;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void add(Node node) {
		AbstractDOMNode newNode = (AbstractDOMNode) node;
		short type;
		if (currentNode == null) {
			throw new IllegalStateException();
		} else if ((type = currentNode.getNodeType()) != Node.ELEMENT_NODE && type != Node.DOCUMENT_NODE) {
			AbstractDOMNode parent = currentNode.parentNode();
			if (parent != null) {
				if (currentNode.getNodeType() != Node.ATTRIBUTE_NODE) {
					parent.insertAfter(newNode, currentNode);
				} else {
					if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
						throw new DOMHierarchyRequestException("Not an attribute.");
					}
					((DOMElement) parent).nodeMap.insertAfter((DOMAttr) newNode, currentNode);
				}
			} else {
				throw new IllegalStateException("Collection was modified outside of this iterator.");
			}
		} else {
			currentNode.insertBefore(newNode, currentNode.getNodeList().getFirst());
		}
		currentNode = newNode;
		if (!isAccepted(newNode)) {
			currentNode = findPrevious();
		} else {
			currentIndex++;
		}
		last = null;
	}

	/**
	 * If the filter implements <code>org.w3c.dom.traversal.NodeFilter</code>,
	 * return it.
	 * <p>
	 * Note that the behaviour of this iterator is not equivalent to the description
	 * in {@link org.w3c.dom.traversal.NodeFilter}.
	 * 
	 * @return the W3C-compatible filter, <code>null</code> otherwise.
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

	@Override
	public void detach() {
	}

}
