/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.Locale;
import java.util.Objects;

import org.w3c.dom.Node;

class TagnameElementList extends AbstractElementLiveList {

	private static final long serialVersionUID = 1L;

	private final String localName, htmlLocalName;
	private final String prefix;
	private final boolean matchAll;
	private final boolean isHTML;

	TagnameElementList(NDTNode ndtNode, String localName, String prefix, boolean matchAll, boolean isHTML) {
		super(ndtNode);
		this.localName = localName;
		this.htmlLocalName = localName.toLowerCase(Locale.ROOT);
		this.prefix = prefix;
		this.matchAll = matchAll;
		this.isHTML = isHTML;
	}

	@Override
	boolean matches(DOMElement element, Node lookFor) {
		return element == lookFor && (matchAll || matchesLocalName(element))
				&& Objects.equals(prefix, element.getPrefix());
	}

	@Override
	boolean matches(DOMElement element) {
		return matchAll || matchesLocalName(element) && Objects.equals(prefix, element.getPrefix());
	}

	boolean matchesLocalName(DOMElement element) {
		String localNameToMatch = element.getLocalName();
		if (isHTML && element.getNamespaceURI() == HTMLDocument.HTML_NAMESPACE_URI) {
			return localNameToMatch.equals(htmlLocalName);
		}
		return localNameToMatch.equals(localName);
	}

}
