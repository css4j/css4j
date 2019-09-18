/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

class StandAloneErrorHandler extends AbstractErrorHandler {

	private boolean errors = false, warnings = false;

	@Override
	public boolean hasComputedStyleErrors(CSSElement element) {
		return false;
	}

	@Override
	public boolean hasErrors() {
		return errors || super.hasErrors();
	}

	@Override
	public boolean hasWarnings() {
		return warnings || super.hasWarnings();
	}

	@Override
	public void linkedStyleError(Node node, String message) {
		errors = true;
	}

	@Override
	public void mediaQueryError(Node node, CSSMediaException exception) {
		errors = true;
		throw new IllegalStateException("Media query error.", exception);
	}

	@Override
	public void linkedSheetError(Exception e, CSSStyleSheet sheet) {
		errors = true;
		throw new IllegalStateException(e);
	}

	@Override
	public StyleDeclarationErrorHandler getInlineStyleErrorHandler(CSSElement owner) {
		return null;
	}

	@Override
	public void inlineStyleError(CSSElement owner, Exception e, String context) {
		errors = true;
		IllegalStateException ex = new IllegalStateException("Error in context: " + context, e);
		throw ex;
	}

	@Override
	public void reset() {
		errors = true;
		warnings = false;
		super.reset();
	}

	@Override
	protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
		return null;
	}

}
