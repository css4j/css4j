/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.TreeSet;

import org.w3c.dom.Node;

class ClassnameElementList extends AbstractElementLiveList {

	private static final long serialVersionUID = 1L;

	private final TreeSet<String> sorted;

	ClassnameElementList(NDTNode ndtNode, TreeSet<String> sorted) {
		super(ndtNode);
		this.sorted = sorted;
	}

	@Override
	boolean matches(DOMElement element, Node lookFor) {
		return element == lookFor && element.hasAttribute("class") && element.getClassList().containsAll(sorted);
	}

	@Override
	boolean matches(DOMElement element) {
		return element.hasAttribute("class") && element.getClassList().containsAll(sorted);
	}

}
