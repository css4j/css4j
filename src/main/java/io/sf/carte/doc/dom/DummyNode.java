/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

class DummyNode extends AbstractDOMNode {

	private static final long serialVersionUID = 1L;

	public DummyNode() {
		super(org.w3c.dom.Node.NOTATION_NODE);
	}

	@Override
	public String getNodeName() {
		return "#dummy";
	}

	@Override
	public DOMNode cloneNode(boolean deep) {
		return this;
	}

	@Override
	public String getBaseURI() {
		return null;
	}

	@Override
	public DOMDocument getOwnerDocument() {
		return null;
	}

}
