/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

abstract class NamespacedNode extends NDTNode {

	private static final long serialVersionUID = 1L;

	private final String namespaceUri;

	private String prefix = null;

	public NamespacedNode(short nodeType, String namespaceUri) {
		super(nodeType);
		this.namespaceUri = namespaceUri;
	}

	@Override
	void checkAppendNodeHierarchy(Node newChild) {
		super.checkAppendNodeHierarchy(newChild);
		if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot append a document type here.");
		}
	}

	@Override
	public String getNamespaceURI() {
		return namespaceUri;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
		String nsUri = getNamespaceURI();
		if (nsUri != null && !isDefaultNamespace(nsUri)) {
			if ("xml".equals(prefix) && DOMDocument.XML_NAMESPACE_URI != getNamespaceURI()) {
				throw new DOMException(DOMException.NAMESPACE_ERR, "Wrong namespace for prefix xml");
			}
			if (prefix != null && !DOMDocument.isValidName(prefix)) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid prefix");
			}
			this.prefix = prefix;
		} else if (prefix != null && prefix.length() != 0) {
			throw new DOMException(DOMException.NAMESPACE_ERR, "Cannot put a prefix to default namespace.");
		}
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		if (prefix == null) {
			if (getPrefix() == null) {
				return getNamespaceURI();
			}
		} else if (prefix.equals(getPrefix())) {
			return getNamespaceURI();
		} else if ("xml".equals(prefix)) {
			return DOMDocument.XML_NAMESPACE_URI;
		}
		return null;
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			return null;
		} else if (namespaceURI.equals(getNamespaceURI())) {
			return getPrefix();
		}
		return null;
	}

}
