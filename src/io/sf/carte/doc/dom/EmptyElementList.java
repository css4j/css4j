/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.Iterator;

import org.w3c.dom.Node;

class EmptyElementList implements ElementList {

	private static final ElementList singleton = new EmptyElementList();

	static ElementList getInstance() {
		return singleton;
	}

	private EmptyElementList() {
		super();
	}

	@Override
	public boolean contains(Node node) {
		return false;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public DOMElement item(int index) {
		return null;
	}

	@Override
	public Iterator<DOMElement> iterator() {
		return new EmptyNodeList.EmptyElementIterator();
	}

	@Override
	public String toString() {
		return "";
	}

}
