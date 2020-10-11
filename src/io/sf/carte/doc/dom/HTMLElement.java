/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * HTML-specific element nodes.
 */
abstract public class HTMLElement extends DOMElement implements org.w3c.dom.html.HTMLElement {

	private static final long serialVersionUID = 1L;

	HTMLElement(String tagName) {
		super(tagName, HTMLDocument.HTML_NAMESPACE_URI);
	}

	HTMLElement(String localName, String namespaceUri) {
		super(localName, namespaceUri);
	}

	@Override
	void checkAppendNodeHierarchy(Node newChild) {
		super.checkAppendNodeHierarchy(newChild);
		if (isVoid()) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "This element is void");
		}
	}

	@Override
	boolean isVoid() {
		return "br".equals(localName) || "hr".equals(localName) || "input".equals(localName) || 
				"area".equals(localName) || "col".equals(localName) || "embed".equals(localName) || 
				"param".equals(localName) || "source".equals(localName) || "track".equals(localName) || 
				"wbr".equals(localName);
	}

	@Override
	public String getDir() {
		return getAttribute("dir");
	}

	@Override
	public void setDir(String dir) {
		setAttribute("dir", dir);
	}

	@Override
	public String getLang() {
		return getAttribute("lang");
	}

	@Override
	public void setLang(String lang) {
		setAttribute("lang", lang);
	}

	@Override
	public String getTitle() {
		return getAttribute("title");
	}

	@Override
	public void setTitle(String title) {
		setAttribute("title", title);
	}

}
