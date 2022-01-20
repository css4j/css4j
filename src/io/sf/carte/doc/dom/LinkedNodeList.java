/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

abstract class LinkedNodeList implements AbstractDOMNode.RawNodeList, Serializable {

	private static final long serialVersionUID = 1L;

	private AbstractDOMNode firstNode = null;
	private AbstractDOMNode lastNode = null;

	LinkedNodeList() {
		super();
	}

	@Override
	public void add(AbstractDOMNode node) throws DOMException {
		if (lastNode != null) {
			lastNode.nextSibling = node;
			node.previousSibling = lastNode;
		} else {
			firstNode = node;
		}
		lastNode = node;
	}

	@Override
	public void clear() {
		AbstractDOMNode curnode = firstNode;
		while (curnode != null) {
			AbstractDOMNode next = curnode.nextSibling;
			curnode.nextSibling = null;
			curnode.previousSibling = null;
			curnode.setParentNode(null);
			curnode = next;
		}
		firstNode = null;
		lastNode = null;
	}

	@Override
	public boolean contains(Node node) {
		AbstractDOMNode curnode = firstNode;
		while (curnode != null) {
			if (curnode == node) {
				return true;
			}
			curnode = curnode.nextSibling;
		}
		return false;
	}

	@Override
	public void insertBefore(AbstractDOMNode newChild, AbstractDOMNode refChild) {
		AbstractDOMNode prev = refChild.previousSibling;
		newChild.nextSibling = refChild;
		newChild.previousSibling = prev;
		if (prev != null) {
			prev.nextSibling = newChild;
		} else {
			firstNode = newChild;
		}
		refChild.previousSibling = newChild;
	}

	@Override
	public DOMNode item(int index) {
		int i = 0;
		AbstractDOMNode node = firstNode;
		while (node != null) {
			if (i == index) {
				return node;
			}
			i++;
			node = node.nextSibling;
		}
		return null;
	}

	@Override
	public AbstractDOMNode getFirst() {
		return firstNode;
	}

	@Override
	public AbstractDOMNode getLast() {
		return lastNode;
	}

	@Override
	public int indexOf(Node node) {
		int index = 0;
		AbstractDOMNode curnode = firstNode;
		while (curnode != null) {
			if (curnode == node) {
				return index;
			}
			index++;
			curnode = curnode.nextSibling;
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return firstNode == null;
	}

	@Override
	public int getLength() {
		int count = 0;
		AbstractDOMNode node = firstNode;
		while (node != null) {
			count++;
			node = node.nextSibling;
		}
		return count;
	}

	@Override
	public Iterator<DOMNode> iterator() {
		return new ChildIterator();
	}

	public Iterator<DOMNode> createDescendingIterator() {
		return new DescendingChildIterator();
	}

	public Iterator<DOMNode> createIterator(BitSet whatToShow) {
		return new BitSetFilteredChildIterator(whatToShow);
	}

	public Iterator<DOMNode> createIterator(int whatToShow, NodeFilter filter) {
		if (filter == null) {
			return new MaskFilteredChildIterator(whatToShow);
		}
		return new CustomFilteredChildIterator(whatToShow, filter);
	}

	public NodeListIterator createListIterator() {
		return new ChildListIterator();
	}

	@Override
	public Iterator<DOMElement> elementIterator() {
		return new ElementIterator();
	}

	@Override
	public Iterator<DOMElement> elementIterator(String name) throws DOMException {
		if (name == null || name.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid tag name.");
		}
		return new ElementNameIterator(name);
	}

	@Override
	public Iterator<DOMElement> elementIteratorNS(String namespaceURI, String localName) {
		if (localName == null || localName.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid localName.");
		}
		return new ElementNameIteratorNS(namespaceURI, localName);
	}

	@Override
	public Iterator<Attr> attributeIterator() {
		return new AttributeIterator();
	}

	private void removeChild(AbstractDOMNode removed) {
		removed.removeFromParent(this);
		postRemoveChild(removed);
	}

	@Override
	public void remove(AbstractDOMNode node) {
		AbstractDOMNode prev = node.previousSibling;
		AbstractDOMNode next = node.nextSibling;
		if (lastNode == node) {
			lastNode = prev;
		} else {
			// next is not null
			next.previousSibling = prev;
		}
		if (firstNode == node) {
			firstNode = next;
		} else {
			// prev is not null
			prev.nextSibling = next;
		}
		node.previousSibling = null;
		node.nextSibling = null;
	}

	@Override
	public AbstractDOMNode replace(AbstractDOMNode newChild, AbstractDOMNode oldChild) {
		AbstractDOMNode prev = oldChild.previousSibling;
		AbstractDOMNode next = oldChild.nextSibling;
		if (lastNode == oldChild) {
			lastNode = newChild;
		} else {
			// next is not null
			next.previousSibling = newChild;
		}
		if (firstNode == oldChild) {
			firstNode = newChild;
		} else {
			// prev is not null
			prev.nextSibling = newChild;
		}
		newChild.previousSibling = prev;
		newChild.nextSibling = next;
		oldChild.previousSibling = null;
		oldChild.nextSibling = null;
		return oldChild;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		AbstractDOMNode node = firstNode;
		while (node != null) {
			buf.append(node.toString());
			node = node.nextSibling;
		}
		return buf.toString();
	}

	abstract void preAddChild(Node node);

	abstract void postAddChild(AbstractDOMNode node);

	abstract void replaceChild(Node newChild, Node oldChild);

	abstract void postRemoveChild(AbstractDOMNode removed);

	private class ChildIterator implements Iterator<DOMNode> {

		private AbstractDOMNode currentNode = firstNode;
		private AbstractDOMNode last = null;

		ChildIterator() {
			super();
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public DOMNode next() {
			if (hasNext()) {
				last = currentNode;
				currentNode = currentNode.nextSibling;
				return last;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			if (last != null) {
				removeChild(last);
				last = null;
			} else {
				throw new IllegalStateException();
			}
		}

	}

	private class ChildListIterator implements NodeListIterator {

		private AbstractDOMNode currentNode = firstNode;
		private AbstractDOMNode last = null;

		private int currentIndex = 0;

		ChildListIterator() {
			super();
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public DOMNode next() {
			if (hasNext()) {
				last = currentNode;
				currentNode = currentNode.nextSibling;
				currentIndex++;
				return last;
			}
			throw new NoSuchElementException();
		}

		@Override
		public boolean hasPrevious() {
			if (currentNode != null) {
				return currentNode.previousSibling != null;
			}
			return lastNode != null;
		}

		@Override
		public DOMNode previous() {
			if (currentNode != null) {
				AbstractDOMNode pre = currentNode.previousSibling;
				if (pre != null) {
					currentNode = pre;
				} else {
					throw new NoSuchElementException();
				}
			} else if (lastNode != null) {
				currentNode = lastNode;
			} else {
				throw new NoSuchElementException();
			}
			last = currentNode;
			currentIndex--;
			return last;
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
			if (last != null) {
				if (currentNode != null) {
					final int index;
					if (currentNode != last) {
						if ((index = currentIndex - 1) < 0 || last != item(index)) {
							// List was modified elsewhere
							throw new IllegalStateException();
						}
						currentIndex = index;
					} else {
						currentNode = last.nextSibling;
					}
					removeChild(last);
				} else {
					/*
					 * either next() was called last time, or the list
					 * was modified outside of this iterator
					 */
					final int index = currentIndex - 1;
					Node curnode = item(index);
					if (curnode != last) {
						// The list was modified elsewhere
						throw new IllegalStateException();
					}
					removeChild(last);
					currentIndex = index;
				}
				last = null;
			} else {
				throw new IllegalStateException();
			}
		}

		@Override
		public void set(Node node) {
			if (last != null) {
				replaceChild(node, last);
				if (currentNode == last) {
					currentNode = (AbstractDOMNode) node;
				}
				last = null;
			} else {
				throw new IllegalStateException();
			}
		}

		@Override
		public void add(Node node) {
			AbstractDOMNode newNode = (AbstractDOMNode) node;
			preAddChild(newNode);
			if (currentNode != null) {
				LinkedNodeList.this.insertBefore(newNode, currentNode);
			} else {
				LinkedNodeList.this.add(newNode);
			}
			currentIndex++;
			postAddChild(newNode);
			last = null;
		}

	}

	private class DescendingChildIterator implements Iterator<DOMNode> {

		AbstractDOMNode currentNode = lastNode;
		private AbstractDOMNode last = null;

		DescendingChildIterator() {
			super();
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public DOMNode next() {
			if (hasNext()) {
				last = currentNode;
				currentNode = currentNode.previousSibling;
				return last;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			if (last != null) {
				removeChild(last);
				last = null;
			} else {
				throw new IllegalStateException();
			}
		}

	}

	abstract private class FilteredChildIterator<T extends Node> implements Iterator<T> {

		AbstractDOMNode currentNode = null;

		abstract boolean isToShow(Node node);

		@Override
		public boolean hasNext() {
			return findNext() != null;
		}

		AbstractDOMNode findNext() {
			AbstractDOMNode next = currentNode;
			if (next == null) {
				next = firstNode;
			} else {
				next = next.nextSibling;
			}
			while (next != null) {
				if (isToShow(next)) {
					break;
				}
				next = next.nextSibling;
			}
			return next;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			AbstractDOMNode next = findNext();
			if (next != null) {
				currentNode = next;
				return (T) next;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			if (currentNode != null && currentNode.parentNode() != null) {
				/*
				 * the second check is to verify if the list was modified outside of this
				 * iterator (also catches double calls to this method)
				 */
				AbstractDOMNode removed = currentNode;
				currentNode = currentNode.previousSibling;
				if (currentNode != null) {
					AbstractDOMNode dummy = new DummyNode();
					dummy.nextSibling = removed.nextSibling;
					dummy.previousSibling = currentNode;
					currentNode = dummy;
				}
				removeItem(removed);
			} else {
				throw new IllegalStateException();
			}
		}

		void removeItem(AbstractDOMNode node) {
			removeChild(node);
		}

	}

	private class BitSetFilteredChildIterator extends FilteredChildIterator<DOMNode> {

		private final BitSet whatToShow;

		BitSetFilteredChildIterator(BitSet whatToShow) {
			super();
			this.whatToShow = whatToShow;
		}

		@Override
		public DOMNode next() {
			AbstractDOMNode next = findNext();
			if (next != null) {
				currentNode = next;
				return next;
			}
			throw new NoSuchElementException();
		}

		@Override
		boolean isToShow(Node node) {
			return whatToShow.get(node.getNodeType());
		}

	}

	private class MaskFilteredChildIterator extends FilteredChildIterator<DOMNode> {

		final int whatToShow;

		MaskFilteredChildIterator(int whatToShow) {
			super();
			this.whatToShow = whatToShow;
		}

		@Override
		public DOMNode next() {
			AbstractDOMNode next = findNext();
			if (next != null) {
				currentNode = next;
				return next;
			}
			throw new NoSuchElementException();
		}

		@Override
		boolean isToShow(Node node) {
			int maskBit = NodeFilter.maskTable[node.getNodeType() - 1];
			return (whatToShow & maskBit) == maskBit;
		}

	}

	private class CustomFilteredChildIterator extends MaskFilteredChildIterator {

		private final NodeFilter nodeFilter;

		CustomFilteredChildIterator(int whatToShow, NodeFilter filter) {
			super(whatToShow);
			this.nodeFilter = filter;
		}

		@Override
		boolean isToShow(Node node) {
			return super.isToShow(node) && nodeFilter.acceptNode(node) == NodeFilter.FILTER_ACCEPT;
		}

	}

	private class ElementIterator extends FilteredChildIterator<DOMElement> {

		ElementIterator() {
			super();
		}

		@Override
		boolean isToShow(Node node) {
			return node.getNodeType() == Node.ELEMENT_NODE;
		}

	}

	private class ElementNameIterator extends FilteredChildIterator<DOMElement> {

		private final String tagname;

		ElementNameIterator(String name) {
			super();
			this.tagname = name;
		}

		@Override
		boolean isToShow(Node node) {
			return node.getNodeType() == Node.ELEMENT_NODE && tagname.equals(node.getNodeName());
		}

	}

	private class ElementNameIteratorNS extends FilteredChildIterator<DOMElement> {

		private final String namespaceURI;
		private final String localName;

		ElementNameIteratorNS(String namespaceURI, String localName) {
			super();
			this.namespaceURI = namespaceURI;
			this.localName = localName;
		}

		@Override
		boolean isToShow(Node node) {
			DOMElement element;
			return node.getNodeType() == Node.ELEMENT_NODE
					&& localName.equals((element = (DOMElement) node).getLocalName())
					&& isSameNamespace(namespaceURI, element);
		}

	}

	private boolean isSameNamespace(String namespaceURI, AbstractDOMNode node) {
		String otherNamespaceURI = node.getNamespaceURI();
		if (namespaceURI == null) {
			return otherNamespaceURI == null || node.isDefaultNamespace(otherNamespaceURI);
		} else {
			return namespaceURI.equals(otherNamespaceURI);
		}
	}

	private class AttributeIterator extends FilteredChildIterator<Attr> {

		AttributeIterator() {
			super();
		}

		@Override
		boolean isToShow(Node node) {
			return node.getNodeType() == Node.ATTRIBUTE_NODE;
		}

		@Override
		void removeItem(AbstractDOMNode node) {
			AbstractDOMNode owner = node.parentNode();
			if (owner == null) {
				throw new IllegalStateException();
			}
			owner.getAttributes().removeNamedItemNS(node.getNamespaceURI(), node.getLocalName());
		}

	}

}
