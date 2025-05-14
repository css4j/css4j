/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

class TagnameElementListNS extends AbstractElementLiveList {

	private static final long serialVersionUID = 1L;

	private final String localName;
	private final String namespaceURI;
	private final boolean matchAll;
	private final boolean matchAllNS;

	TagnameElementListNS(NDTNode ndtNode, String localName, String namespaceURI, boolean matchAll, boolean matchAllNS) {
		super(ndtNode);
		this.localName = localName;
		this.namespaceURI = namespaceURI;
		this.matchAll = matchAll;
		this.matchAllNS = matchAllNS;
	}

	@Override
	boolean matches(DOMElement element, Node lookFor) {
		return element == lookFor && (matchAll || element.getLocalName().equals(localName))
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
