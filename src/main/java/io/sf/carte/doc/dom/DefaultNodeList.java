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

class DefaultNodeList extends LinkedList<DOMNode> implements DOMNodeList {

	private static final long serialVersionUID = 2L;

	DefaultNodeList() {
		super();
	}

	@Override
	public boolean contains(Node node) {
		return super.contains(node);
	}

	@Override
	public DOMNode item(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		return get(index);
	}

	@Override
	public final int getLength() {
		return size();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(size() * 32 + 40);
		Iterator<DOMNode> it = iterator();
		while (it.hasNext()) {
			buf.append(it.next().toString());
		}
		return buf.toString();
	}

}
