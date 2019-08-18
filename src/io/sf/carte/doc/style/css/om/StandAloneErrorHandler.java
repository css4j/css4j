/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

class StandAloneErrorHandler implements ErrorHandler {

	private boolean cserrors = false, errors = false, warnings = false;

	@Override
	public boolean hasComputedStyleErrors() {
		return cserrors;
	}

	@Override
	public boolean hasErrors() {
		return errors || cserrors;
	}

	@Override
	public boolean hasWarnings() {
		return warnings;
	}

	@Override
	public void linkedStyleError(Node node, String message) {
		errors = true;
	}

	@Override
	public void linkedStyleWarning(Node node, String message) {
		warnings = true;
	}

	@Override
	public void mediaQueryError(Node node, String media) {
		errors = true;
		throw new IllegalStateException("Media query error: " + media);
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
	public void computedStyleError(Node node, String propertyName, String propertyValue, String message) {
		cserrors = true;
	}

	@Override
	public void presentationalHintError(CSSElement elm, DOMException e) {
		errors = true;
	}

	@Override
	public void resetComputedStyleErrors() {
		cserrors = false;
	}

	@Override
	public void reset() {
		errors = true;
		warnings = false;
		resetComputedStyleErrors();
	}

}
