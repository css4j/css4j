/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.NodeStyleDeclaration;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * CSS Inline style declaration.
 * 
 */
abstract public class InlineStyle extends BaseCSSStyleDeclaration implements NodeStyleDeclaration {

	private Node node = null;

	protected InlineStyle() {
		super();
	}

	protected InlineStyle(InlineStyle copiedObject) {
		super(copiedObject);
		setOwnerNode(copiedObject.getOwnerNode());
	}

	@Override
	public String getCssText() {
		InlineStyleFormattingContext context = new InlineStyleFormattingContext();
		BufferSimpleWriter sw = new BufferSimpleWriter(50 + getLength() * 16);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public Node getOwnerNode() {
		return node;
	}

	protected void setOwnerNode(Node node) {
		this.node = node;
	}

	/**
	 * Has this style's owner element an override style attached to the given pseudo-element?
	 * 
	 * @param pseudoElt
	 *            the pseudo-element, or <code>null</code> if none.
	 * @return <code>true</code> if this style's owner element has an override style attached, false
	 *         otherwise.
	 */
	public boolean hasOverrideStyle(String pseudoElt) {
		Node node = getOwnerNode();
		if (node != null) {
			short type = node.getNodeType();
			if (type == Node.ATTRIBUTE_NODE) {
				node = ((Attr) node).getOwnerElement();
				if (node == null) {
					return false;
				} else {
					type = node.getNodeType();
				}
			}
			if (type == Node.ELEMENT_NODE) {
				return ((CSSElement) node).hasOverrideStyle(pseudoElt);
			}
		}
		return false;
	}

	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		Node node = getOwnerNode();
		if (node != null) {
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				CSSElement owner = (CSSElement) ((Attr) node).getOwnerElement();
				if (owner != null) {
					return owner.getOwnerDocument().getErrorHandler().getInlineStyleErrorHandler(owner);
				}
			}
		}
		return null;
	}

	@Override
	abstract public InlineStyle clone();

}
