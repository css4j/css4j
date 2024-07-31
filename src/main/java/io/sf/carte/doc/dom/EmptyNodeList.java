/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.dom.AbstractDOMNode.RawNodeList;

class EmptyNodeList implements RawNodeList {

	@Override
	public void add(AbstractDOMNode node) throws DOMException {
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot add children to this node.");
	}

	@Override
	public void insertBefore(AbstractDOMNode newChild, AbstractDOMNode refChild) {
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot add children to this node.");
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean contains(Node node) {
		return false;
	}

	@Override
	public AbstractDOMNode getFirst() {
		return null;
	}

	@Override
	public AbstractDOMNode getLast() {
		return null;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public int indexOf(Node node) {
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public DOMNode item(int index) {
		return null;
	}

	@Override
	public void remove(AbstractDOMNode node) {
	}

	@Override
	public AbstractDOMNode replace(AbstractDOMNode newChild, AbstractDOMNode oldChild) {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Cannot set children to this node.");
	}

	@Override
	public Iterator<DOMNode> iterator() {
		return new EmptyNodeIterator();
	}

	@Override
	public Iterator<DOMElement> elementIterator() {
		return new EmptyElementIterator();
	}

	@Override
	public Iterator<DOMElement> elementIterator(String name) throws DOMException {
		if (name == null || name.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid tag name.");
		}
		return new EmptyElementIterator();
	}

	@Override
	public Iterator<DOMElement> elementIteratorNS(String namespaceURI, String localName) {
		if (localName == null || localName.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid localName.");
		}
		return new EmptyElementIterator();
	}

	@Override
	public Iterator<Attr> attributeIterator() {
		return null;
	}

	@Override
	public String toString() {
		return "";
	}

	abstract static class EmptyIterator<T extends Node> implements Iterator<T> {

		@Override
		public boolean hasNext() {
			return false;
		}

	}

	private static class EmptyNodeIterator extends EmptyNodeList.EmptyIterator<DOMNode> {

		@Override
		public DOMNode next() {
			throw new NoSuchElementException();
		}

	}

	static class EmptyElementIterator extends EmptyNodeList.EmptyIterator<DOMElement> {

		@Override
		public DOMElement next() {
			throw new NoSuchElementException();
		}

	}

}
