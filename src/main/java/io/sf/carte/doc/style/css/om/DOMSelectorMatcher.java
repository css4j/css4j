/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSDocument.ComplianceMode;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.SelectorMatcher;

/**
 * Selector matcher for CSS-enabled DOM.
 * 
 * @author Carlos Amengual
 * 
 */
public class DOMSelectorMatcher extends BaseSelectorMatcher<CSSElement> {

	private static final long serialVersionUID = 2L;

	public DOMSelectorMatcher(CSSElement elm) {
		super(elm);
	}

	@Override
	protected boolean isActivePseudoClass(String pseudoclassName) {
		CSSDocument doc = getElement().getOwnerDocument();
		CSSCanvas canvas;
		if (doc != null && (canvas = doc.getCanvas()) != null) {
			return canvas.isActivePseudoClass(getElement(), pseudoclassName);
		}
		return false;
	}

	@Override
	protected boolean isVisitedURI(String href) {
		return getOwnerDocument().isVisitedURI(href);
	}

	@Override
	protected ComplianceMode getComplianceMode() {
		return getOwnerDocument().getComplianceMode();
	}

	@Override
	protected CSSDocument getOwnerDocument() {
		return getElement().getOwnerDocument();
	}

	@Override
	protected String getId() {
		return getElement().getId();
	}

	@Override
	protected String getElementId(CSSElement element) {
		return element.getId();
	}

	@Override
	protected SelectorMatcher obtainSelectorMatcher(CSSElement element) {
		return element.getSelectorMatcher();
	}

}
