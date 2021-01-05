/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;

abstract class AbstractElementLiveList implements ElementList, Serializable {

	private static final long serialVersionUID = 1L;

	private final NDTNode contextNode;

	AbstractElementLiveList(NDTNode ndtNode) {
		super();
		this.contextNode = ndtNode;
	}

	@Override
	public boolean contains(Node node) {
		if (node == null) {
			return false;
		}
		return containsChild(contextNode, node);
	}

	private boolean containsChild(NDTNode parentNode, Node lookFor) {
		Node node = parentNode.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement element = (DOMElement) node;
				if (matches(element, lookFor)) {
					return true;
				}
				if (containsChild(element, lookFor)) {
					return true;
				}
			}
			node = node.getNextSibling();
		}
		return false;
	}

	@Override
	public DOMElement item(int index) {
		if (index < 0) {
			return null;
		}
		//
		ElementTuple tuple = new ElementTuple();
		indexChildList(contextNode, index, tuple);
		return tuple.element;
	}

	private void indexChildList(NDTNode parentNode, int targetIndex, ElementTuple tuple) {
		Node node = parentNode.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement element = (DOMElement) node;
				if (matches(element)) {
					if (tuple.index == targetIndex) {
						tuple.element = element;
						return;
					}
					tuple.index++;
				}
				indexChildList(element, targetIndex, tuple);
				if (tuple.element != null) {
					return;
				}
			}
			node = node.getNextSibling();
		}
	}

	@Override
	public int getLength() {
		return countChildList(contextNode, 0);
	}

	private int countChildList(NDTNode parentNode, int counter) {
		Node node = parentNode.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement element = (DOMElement) node;
				if (matches(element)) {
					counter++;
				}
				counter = countChildList(element, counter);
			}
			node = node.getNextSibling();
		}
		return counter;
	}

	@Override
	public boolean isEmpty() {
		ElementTuple tuple = new ElementTuple();
		indexChildList(contextNode, 0, tuple);
		return tuple.element == null;
	}

	abstract boolean matches(DOMElement element, Node lookFor);

	abstract boolean matches(DOMElement element);

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(128);
		Iterator<DOMElement> it = iterator();
		if (it.hasNext()) {
			buf.append(it.next().getStartTag());
		}
		while (it.hasNext()) {
			buf.append(',').append(it.next().getStartTag());
		}
		return buf.toString();
	}

	@Override
	public Iterator<DOMElement> iterator() {
		return new ElementIterator();
	}

	private class ElementIterator implements Iterator<DOMElement> {

		private DOMElement next;

		private ElementIterator() {
			super();
			next = findNext(contextNode);
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public DOMElement next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			DOMElement current = next;
			next = findNext(next);
			return current;
		}

		private DOMElement findNext(NDTNode current) {
			AbstractDOMNode node = nextNode(current);
			while (node != contextNode) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					DOMElement element = (DOMElement) node;
					if (matches(element)) {
						return element;
					}
				}
				node = nextNode(node);
			}
			return null;
		}

		private AbstractDOMNode nextNode(AbstractDOMNode current) {
			AbstractDOMNode next = current.getNodeList().getFirst();
			if (next == null) {
				if (current != contextNode) {
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
				if (parent != contextNode && parent != null) {
					return nextSiblingOrParent(parent);
				}
				next = contextNode;
			}
			return next;
		}

	}

}
