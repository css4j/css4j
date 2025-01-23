/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Implementation of DOM's <code>DocumentType</code>.
 */
class DocumentTypeImpl extends AbstractDOMNode implements DocumentType {

	private static final long serialVersionUID = 1L;

	private final String qualifiedName;
	private final String publicId;
	private final String systemId;

	public DocumentTypeImpl(String qualifiedName, String publicId, String systemId) {
		super(Node.DOCUMENT_TYPE_NODE);
		this.qualifiedName = qualifiedName;
		this.publicId = publicId;
		this.systemId = systemId;
	}

	@Override
	void checkAppendNode(Node newChild) {
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot add children to this node");
	}

	@Override
	public String getBaseURI() {
		Document doc = getOwnerDocument();
		if (doc != null) {
			return doc.getBaseURI();
		}
		return null;
	}

	/**
	 * This method is considered obsolete and this implementation does not support it.
	 * 
	 * @return always <code>null</code>.
	 */
	@Deprecated
	@Override
	public NamedNodeMap getEntities() {
		return null;
	}

	/**
	 * This method is not supported by this implementation.
	 * 
	 * @return always <code>null</code>.
	 */
	@Deprecated
	@Override
	public String getInternalSubset() {
		return null;
	}

	@Override
	public String getName() {
		return qualifiedName;
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	/**
	 * This method is considered obsolete and this implementation does not support it.
	 * 
	 * @return always <code>null</code>.
	 */
	@Deprecated
	@Override
	public NamedNodeMap getNotations() {
		return null;
	}

	@Override
	public DOMDocument getOwnerDocument() {
		return (DOMDocument) getParentNode();
	}

	@Override
	public String getPublicId() {
		return publicId;
	}

	@Override
	public String getSystemId() {
		return systemId;
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		return null;
	}

	@Override
	public DocumentType cloneNode(boolean deep) {
		return new DocumentTypeImpl(qualifiedName, publicId, systemId);
	}

	@Override
	public String toString() {
		boolean hasSystemId = systemId != null && systemId.length() != 0;
		StringBuilder buf = new StringBuilder(128);
		buf.append("<!DOCTYPE ").append(qualifiedName);
		if (publicId != null && publicId.length() != 0) {
			buf.append(" PUBLIC \"");
			buf.append(DOMAttr.escapeAttributeEntities(publicId)).append('"');
		} else if (hasSystemId) {
			buf.append(" SYSTEM");
		}
		if (hasSystemId) {
			buf.append(" \"");
			buf.append(DOMAttr.escapeAttributeEntities(systemId)).append('"');
		}
		buf.append('>');
		return buf.toString();
	}
}
