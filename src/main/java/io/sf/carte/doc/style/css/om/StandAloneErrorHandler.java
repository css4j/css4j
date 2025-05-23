/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.WeakHashMap;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

class StandAloneErrorHandler extends AbstractErrorHandler {

	private static final long serialVersionUID = 1L;

	private static final WeakHashMap<AbstractCSSStyleSheet, StandAloneErrorHandler> handlerMap = new WeakHashMap<>(4);

	static StandAloneErrorHandler getInstance(AbstractCSSStyleSheet sheet) {
		StandAloneErrorHandler handler = handlerMap.get(sheet);
		if (handler == null) {
			handler = new StandAloneErrorHandler();
			handlerMap.put(sheet, handler);
		}
		return handler;
	}

	private boolean errors = false, warnings = false;

	private StandAloneErrorHandler() {
		super();
	}

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
	public void linkedSheetError(Exception e, CSSStyleSheet<? extends CSSRule> sheet) {
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
