/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.nsac.SelectorList;

class DOMElementLinkedList extends LinkedList<DOMElement> implements ElementList {

	private static final long serialVersionUID = 2L;

	DOMElementLinkedList() {
		super();
	}

	@Override
	public boolean contains(Node node) {
		return super.contains(node);
	}

	@Override
	public DOMElement item(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		return get(index);
	}

	@Override
	public int getLength() {
		return size();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(size() * 32 + 40);
		Iterator<DOMElement> it = iterator();
		if (it.hasNext()) {
			buf.append(it.next().toString());
		}
		while (it.hasNext()) {
			buf.append(',').append(it.next().toString());
		}
		return buf.toString();
	}

	void fillQuerySelectorList(SelectorList selist, Node firstChild) {
		Node node = firstChild;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement element = (DOMElement) node;
				if (element.matches(selist, null)) {
					add(element);
				}
				fillQuerySelectorList(selist, element.getFirstChild());
			}
			node = node.getNextSibling();
		}
	}

}

