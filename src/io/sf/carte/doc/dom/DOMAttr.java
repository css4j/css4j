/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import io.sf.carte.util.SimpleWriter;

/**
 * Base class for DOM attribute nodes.
 */
abstract class DOMAttr extends NamespacedNode implements Attr {

	private static final long serialVersionUID = 1L;

	private TypeInfo schemaTypeInfo = null;

	private final String localName;
	boolean specified = true;

	String value = "";

	DOMAttr(String localName, String namespaceURI) {
		super(Node.ATTRIBUTE_NODE, namespaceURI);
		this.localName = localName;
	}

	@Override
	void checkAppendNodeHierarchy(Node newChild) {
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Cannot append to attribute node");
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getName() {
		String name = getLocalName();
		String prefix = getPrefix();
		if (prefix != null) {
			name = prefix + ':' + name;
		}
		return name;
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	@Override
	public String getNodeValue() throws DOMException {
		return getValue();
	}

	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
		setValue(nodeValue);
	}

	@Override
	public DOMElement getOwnerElement() {
		return (DOMElement) super.getParentNode();
	}

	@Override
	public DOMNode getParentNode() throws DOMException {
		return null;
	}

	/* @formatter:off
	 * 
	 * For attributes, this method is used to set the owner element.
	 * 
	 * When setting a new, non-null owner element to a new attribute, the recommended
	 * sequence is the following:
	 * 
	 * 1. Add the attribute node to owner element's node map.
	 * 2. Set the attribute node's value.
	 * 3. Call this method.
	 * 
	 * @formatter:on
	 */
	@Override
	void setParentNode(AbstractDOMNode parentNode) throws DOMException {
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Attributes do no have a parent node");
	}

	@Override
	public TypeInfo getSchemaTypeInfo() {
		if (schemaTypeInfo == null) {
			schemaTypeInfo = new AttributeTypeInfo();
		}
		return schemaTypeInfo;
	}

	@Override
	public boolean getSpecified() {
		return specified;
	}

	/**
	 * Get the attribute value.
	 * <p>
	 * No attempt is made to replace entities or escape reserved characters.
	 * 
	 * @return the attribute value (the empty string if no value).
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * Set the attribute value.
	 * 
	 * @param value the value. If <code>null</code>, it is changed to the empty
	 *              string.
	 */
	@Override
	public void setValue(String value) {
		if (value == null) {
			value = "";
		}
		this.value = value;
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		String namespaceURI = super.lookupNamespaceURI(prefix);
		if (namespaceURI == null) {
			DOMElement owner = getOwnerElement();
			if (owner != null) {
				namespaceURI = owner.lookupNamespaceURI(prefix);
			}
		}
		return namespaceURI;
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			return null;
		}
		if (namespaceURI.equals(getNamespaceURI())) {
			return getPrefix();
		}
		DOMElement parent = getOwnerElement();
		if (parent != null) {
			return parent.lookupPrefix(namespaceURI);
		}
		return null;
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
		if ("xmlns".equals(getLocalName())
				|| ("xmlns".equals(prefix) && !DOMDocument.XMLNS_NAMESPACE_URI.equals(getNamespaceURI()))) {
			throw new DOMException(DOMException.NAMESPACE_ERR, "Cannot set prefix to this node");
		}
		String oldPrefix = getPrefix();
		super.setPrefix(prefix);
		DOMElement parent = getOwnerElement();
		if (parent != null) {
			parent.nodeMap.updatePrefix(this, prefix, oldPrefix);
		}
	}

	@Override
	abstract public Attr cloneNode(boolean deep);

	@Override
	public String toString() {
		String name = getLocalName();
		String prefix = getPrefix();
		String value = getValue();
		int vlen = value.length();
		StringBuilder buf;
		if (prefix != null) {
			buf = new StringBuilder(name.length() + prefix.length() + vlen + 4);
			buf.append(prefix);
			buf.append(':');
		} else {
			buf = new StringBuilder(name.length() + vlen + 3);
		}
		buf.append(name);
		if (vlen != 0 || !isBooleanAttribute()) {
			buf.append("=\"");
			buf.append(escapeAttributeEntities(value));
			buf.append('"');
		}
		return buf.toString();
	}

	/**
	 * Is this a boolean attribute?
	 * 
	 * @return true if this attribute is boolean.
	 */
	boolean isBooleanAttribute() {
		/*
		 * By default, return false if the document is not HTML or the attribute not in
		 * the HTML namespace, because we do not want to maintain a list of boolean
		 * attributes.
		 */
		String nsUri;
		return getOwnerDocument().isHTML()
				&& ((nsUri = getNamespaceURI()) == null || nsUri == HTMLDocument.HTML_NAMESPACE_URI);
	}

	void write(SimpleWriter wri) throws IOException {
		String qname = getLocalName();
		String prefix = getPrefix();
		if (prefix != null) {
			wri.write(prefix);
			wri.write(':');
		}
		wri.write(qname);
		String value = getValue();
		if (value.length() != 0) {
			wri.write("=\"");
			wri.write(escapeAttributeEntities(value));
			wri.write('"');
		}
	}

	static String escapeAttributeEntities(String text) {
		StringBuilder buf = null;
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			// Check whether c is '<', '>', '&', '"' or 'U+00A0'
			if (c == '<') {
				buf = DOMDocument.appendEntityToBuffer(buf, "lt", text, i, len);
			} else if (c == '>') {
				buf = DOMDocument.appendEntityToBuffer(buf, "gt", text, i, len);
			} else if (c == '&') {
				buf = DOMDocument.appendEntityToBuffer(buf, "amp", text, i, len);
			} else if (c == '"') {
				buf = DOMDocument.appendEntityToBuffer(buf, "quot", text, i, len);
			} else if (c == '\u00a0') {
				buf = DOMDocument.appendEntityToBuffer(buf, "nbsp", text, i, len);
			} else if (buf != null) {
				buf.append(c);
			}
		}
		if (buf != null) {
			text = buf.toString();
		}
		return text;
	}

	private class AttributeTypeInfo extends DOMTypeInfo {

		@Override
		public String getTypeName() {
			if (isId()) {
				return "ID";
			}
			return null;
		}

		@Override
		public String getTypeNamespace() {
			return "https://www.w3.org/TR/xml/";
		}

	}

}
