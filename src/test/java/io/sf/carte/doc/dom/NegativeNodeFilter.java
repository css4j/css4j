/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

class NegativeNodeFilter implements NodeFilter, org.w3c.dom.traversal.NodeFilter {

	NodeFilter filter;

	NegativeNodeFilter(NodeFilter filter) {
		super();
		this.filter = filter;
	}

	@Override
	public short acceptNode(Node node) {
		return filter.acceptNode(node) == NodeFilter.FILTER_ACCEPT ? NodeFilter.FILTER_SKIP_NODE
				: NodeFilter.FILTER_ACCEPT;
	}

}