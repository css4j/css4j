/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

class TagnameElementList extends AbstractElementLiveList {

	private static final long serialVersionUID = 1L;

	private final String localName;
	private final String namespaceURI;
	private final boolean matchAll;
	private final boolean matchAllNS;

	TagnameElementList(NDTNode ndtNode, String localName, String namespaceURI, boolean matchAll, boolean matchAllNS) {
		super(ndtNode);
		this.localName = localName;
		this.namespaceURI = namespaceURI;
		this.matchAll = matchAll;
		this.matchAllNS = matchAllNS;
	}

	@Override
	boolean matches(DOMElement element, Node lookFor) {
		return (matchAll || element.getLocalName().equals(localName)) && element == lookFor
				&& isSameNamespace(element);
	}

	@Override
	boolean matches(DOMElement element) {
		return (matchAll || element.getLocalName().equals(localName)) && isSameNamespace(element);
	}

	private boolean isSameNamespace(DOMElement element) {
		if (matchAllNS) {
			return true;
		}
		String ns1 = element.getNamespaceURI();
		if (ns1 == null) {
			return namespaceURI == null || element.isDefaultNamespace(namespaceURI);
		}
		return ns1.equals(namespaceURI) || (namespaceURI == null && element.isDefaultNamespace(ns1));
	}

}
