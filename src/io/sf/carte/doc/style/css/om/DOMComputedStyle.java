/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
 * Computed style associated to a DOM node.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class DOMComputedStyle extends ComputedCSSStyle {

	private static final long serialVersionUID = 1L;

	private transient ComputedCSSStyle parentStyle = null;

	protected DOMComputedStyle(BaseDocumentCSSStyleSheet parentSheet) {
		super(parentSheet);
	}

	protected DOMComputedStyle(ComputedCSSStyle copiedObject) {
		super(copiedObject);
	}

	@Override
	public ComputedCSSStyle getParentComputedStyle() {
		if (parentStyle == null && getOwnerSheet() != null) {
			Node node = getOwnerNode();
			while (node != null) {
				node = node.getParentNode();
				if (node == null) {
					break;
				}
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					parentStyle = getOwnerSheet().getComputedStyle((CSSElement) node, null);
					break;
				}
			}
		}
		return parentStyle;
	}

	/**
	 * Gets the style database which is used to compute the style.
	 * 
	 * @return the style database, or <code>null</code> if no style database has
	 *         been selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		CSSElement node = getOwnerNode();
		if (node != null) {
			CSSDocument doc = node.getOwnerDocument();
			return doc.getStyleDatabase();
		}
		return null;
	}

}
