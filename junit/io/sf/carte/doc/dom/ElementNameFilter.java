/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

class ElementNameFilter implements NodeFilter, org.w3c.dom.traversal.NodeFilter {

	String nodename;

	ElementNameFilter(String nodename) {
		super();
		this.nodename = nodename;
	}

	@Override
	public short acceptNode(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE && nodename.equals(node.getNodeName())
				? NodeFilter.FILTER_SKIP_NODE
				: NodeFilter.FILTER_ACCEPT;
	}

}