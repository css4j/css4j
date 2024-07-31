/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.sf.carte.doc.dom.HTMLDocument;
import io.sf.carte.doc.style.css.CSSElement;

/**
 * CSS Selector matcher providing (part of) HTML case-insensitive attribute
 * matching for XML-oriented DOM implementations in the DOM wrapper.
 * 
 * @author Carlos Amengual
 * 
 */
class WrapperSelectorMatcher extends DOMSelectorMatcher {

	private static final long serialVersionUID = 1L;

	private final Element wrapped;

	public WrapperSelectorMatcher(CSSElement elm, Element wrapped) {
		super(elm);
		this.wrapped = wrapped;
	}

	@Override
	protected AbstractSelectorMatcher obtainSelectorMatcher(CSSElement element) {
		return new WrapperSelectorMatcher(element, (Element) ((StylableDocumentWrapper.MyElement) element).rawnode);
	}

	@Override
	protected String getAttributeValue(String attrName) {
		return getAttributeValue(wrapped, attrName);
	}

	@Override
	protected boolean hasAttribute(String attrName) {
		Attr attr = wrapped.getAttributeNode(attrName);
		if (attr == null) {
			NamedNodeMap nnm = wrapped.getAttributes();
			for (int i = 0; i < nnm.getLength(); i++) {
				Node item = nnm.item(i);
				String nsuri; // In some configurations, nsuri could be null
				if (attrName.equalsIgnoreCase(item.getNodeName()) && ((nsuri = item.getNamespaceURI()) == null
						|| nsuri.length() == 0 || HTMLDocument.HTML_NAMESPACE_URI.equals(nsuri))) {
					return true;
				}
			}
			return false;
		}
		return attr != null;
	}

	static String getAttributeValue(Element wrapped, String attrName) {
		String value = null;
		Attr attr = wrapped.getAttributeNode(attrName);
		if (attr == null) {
			NamedNodeMap nnm = wrapped.getAttributes();
			for (int i = 0; i < nnm.getLength(); i++) {
				Node item = nnm.item(i);
				String nsuri; // In some configurations, nsuri could be null
				if (attrName.equalsIgnoreCase(item.getNodeName()) && ((nsuri = item.getNamespaceURI()) == null
						|| nsuri.length() == 0 || HTMLDocument.HTML_NAMESPACE_URI.equals(nsuri))) {
					value = item.getNodeValue();
					break;
				}
			}
		} else {
			value = attr.getValue();
		}
		return value != null ? value : "";
	}

}
