/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.net.URL;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * CSS Style Sheet for DOM.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class DOMCSSStyleSheet extends BaseCSSStyleSheet implements Cloneable {

	private static final long serialVersionUID = 1L;

	private Node ownerNode = null;

	public DOMCSSStyleSheet(String title, Node ownerNode, MediaQueryList media, AbstractCSSRule ownerRule,
			byte origin) {
		super(title, media, ownerRule, origin);
		this.ownerNode = ownerNode;
	}

	@Override
	public Node getOwnerNode() {
		return ownerNode;
	}

	@Override
	public String getHref() {
		String href = super.getHref();
		URL base;
		if (href == null && ownerNode != null
				&& (base = ((CSSDocument) ownerNode.getOwnerDocument()).getBaseURL()) != null) {
			return base.toExternalForm();
		}
		return href;
	}

	abstract protected DOMCSSStyleSheet createCSSStyleSheet(String title, Node ownerNode, MediaQueryList media,
			AbstractCSSRule ownerRule, byte origin);

	/**
	 * Creates and returns a copy of this style sheet.
	 * <p>
	 * The copy is a shallow copy (the rule list is new, but the referenced
	 * rules are the same as in the cloned object.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	public DOMCSSStyleSheet clone() {
		DOMCSSStyleSheet myClone = createCSSStyleSheet(getTitle(), getOwnerNode(), getMedia(), getOwnerRule(),
				getOrigin());
		myClone.setHref(getHref());
		copyAllTo(myClone);
		return myClone;
	}

}
