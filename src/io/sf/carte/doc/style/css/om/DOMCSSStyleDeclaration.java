/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Style declaration associated to a DOM node (either a computed style or an
 * anonymous declaration).
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class DOMCSSStyleDeclaration extends ComputedCSSStyle {

	private BaseDocumentCSSStyleSheet parentSheet = null;

	private transient ComputedCSSStyle parentStyle = null;

	protected DOMCSSStyleDeclaration(Node ownerNode) {
		super();
		setOwnerNode(ownerNode);
	}

	protected DOMCSSStyleDeclaration(BaseDocumentCSSStyleSheet parentSheet) {
		super();
		this.parentSheet = parentSheet;
	}

	protected DOMCSSStyleDeclaration(ComputedCSSStyle copiedObject) {
		super(copiedObject);
	}

	@Override
	public ComputedCSSStyle getParentComputedStyle() {
		if (parentStyle == null && parentSheet != null) {
			Node node = getOwnerNode();
			while (node != null) {
				node = node.getParentNode();
				if (node == null) {
					break;
				}
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					parentStyle = parentSheet.getComputedStyle((CSSElement) node, null);
					break;
				}
			}
		}
		return parentStyle;
	}

	/**
	 * Gets the style database which is used to compute the style.
	 * 
	 * @return the style database, or null if no style database has been
	 *         selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		Node node = getOwnerNode();
		if (node != null) {
			CSSDocument doc = (CSSDocument) node.getOwnerDocument();
			return doc.getStyleDatabase();
		}
		return null;
	}

}
