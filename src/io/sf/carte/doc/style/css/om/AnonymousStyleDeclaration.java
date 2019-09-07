/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Anonymous style declaration, associated to a non-element DOM node.
 * 
 */
abstract public class AnonymousStyleDeclaration extends BaseCSSStyleDeclaration {

	private final Node ownerNode;

	protected AnonymousStyleDeclaration(Node ownerNode) {
		super();
		this.ownerNode = ownerNode;
	}

	protected AnonymousStyleDeclaration(AnonymousStyleDeclaration copyme) {
		super();
		this.ownerNode = copyme.ownerNode;
	}

	@Override
	public Node getOwnerNode() {
		return ownerNode;
	}

	/**
	 * Gets the style database which is used to compute the style.
	 * 
	 * @return the style database, or null if no style database has been
	 *         selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		CSSDocument doc = (CSSDocument) ownerNode.getOwnerDocument();
		return doc.getStyleDatabase();
	}

}
