/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;

/**
 * CSS Style Sheet for DOM.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class DOMDocumentCSSStyleSheet extends BaseDocumentCSSStyleSheet {

	private CSSDocument ownerNode = null;

	protected DOMDocumentCSSStyleSheet(byte origin) {
		super(null, origin);
	}

	protected DOMDocumentCSSStyleSheet(String medium, byte origin) {
		super(medium, origin);
	}

	@Override
	public CSSDocument getOwnerNode() {
		return ownerNode;
	}

	@Override
	public void setOwnerDocument(CSSDocument ownerNode) {
		this.ownerNode = ownerNode;
	}

	/**
	 * Gets the computed style for the given DOM Element and pseudo-element.
	 * 
	 * @param elm
	 *            the element.
	 * @param pseudoElt
	 *            the pseudo-element.
	 * @return the computed style declaration.
	 */
	@Override
	public ComputedCSSStyle getComputedStyle(CSSElement elm, String pseudoElt) {
		InlineStyle inline = (InlineStyle) elm.getStyle();
		ComputedCSSStyle style = createComputedCSSStyle(this);
		style.setOwnerNode(elm);
		return computeStyle(style, elm.getSelectorMatcher(), pseudoElt, inline);
	}

	abstract protected ComputedCSSStyle createComputedCSSStyle(BaseDocumentCSSStyleSheet parentSheet);

	abstract protected DOMDocumentCSSStyleSheet createDocumentStyleSheet(String medium, byte origin);

	@Override
	abstract public BaseCSSStyleSheetFactory getStyleSheetFactory();

	/**
	 * Creates and returns a copy of this style sheet.
	 * <p>
	 * The copy is a shallow copy (the rule list is new, but the referenced rules are the same
	 * as in the cloned object.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	public DOMDocumentCSSStyleSheet clone() {
		DOMDocumentCSSStyleSheet myClone = createDocumentStyleSheet(getTargetMedium(), getOrigin());
		myClone.setOwnerDocument(ownerNode);
		copyAllTo(myClone);
		return myClone;
	}

	@Override
	public DOMDocumentCSSStyleSheet clone(String targetMedium) {
		DOMDocumentCSSStyleSheet myClone = createDocumentStyleSheet(targetMedium, getOrigin());
		myClone.setOwnerDocument(ownerNode);
		copyToTarget(myClone);
		return myClone;
	}

}
