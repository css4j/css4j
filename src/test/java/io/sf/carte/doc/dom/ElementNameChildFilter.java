/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

class ElementNameChildFilter extends ElementNameFilter {

	ElementNameChildFilter(String nodename) {
		super(nodename);
	}

	@Override
	public short acceptNode(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE && nodename.equals(node.getNodeName())
				? NodeFilter.FILTER_SKIP_NODE_CHILD
				: NodeFilter.FILTER_ACCEPT;
	}

}