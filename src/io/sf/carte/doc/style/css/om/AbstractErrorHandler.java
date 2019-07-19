/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

abstract class AbstractErrorHandler implements ErrorHandler {

	private HashMap<CSSElement, StyleDeclarationErrorHandler> inlineErrorHandlerMap = null;

	AbstractErrorHandler() {
		super();
	}

	@Override
	public StyleDeclarationErrorHandler getInlineStyleErrorHandler(CSSElement owner) {
		if (inlineErrorHandlerMap == null) {
			inlineErrorHandlerMap = new HashMap<CSSElement, StyleDeclarationErrorHandler>();
		}
		StyleDeclarationErrorHandler handler = inlineErrorHandlerMap.get(owner);
		if (handler == null) {
			handler = getStyleSheetFactory().createInlineStyleErrorHandler(owner);
			inlineErrorHandlerMap.put(owner, handler);
		}
		return handler;
	}

	/**
	 * Gets the owners of inline styles that have been processed through this handler.
	 * 
	 * @return the owners of the inline styles with error handlers, or <code>null</code> if
	 *         this handler has not processed any inline style.
	 */
	public Set<CSSElement> getInlineStyleOwners() {
		Set<CSSElement> owners;
		if (inlineErrorHandlerMap != null && !(owners = inlineErrorHandlerMap.keySet()).isEmpty()) {
			return owners;
		}
		return null;
	}

	protected boolean hasInlineErrors() {
		if (inlineErrorHandlerMap != null) {
			Iterator<StyleDeclarationErrorHandler> it = inlineErrorHandlerMap.values().iterator();
			while (it.hasNext()) {
				if (it.next().hasErrors()) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean hasInlineWarnings() {
		if (inlineErrorHandlerMap != null) {
			Iterator<StyleDeclarationErrorHandler> it = inlineErrorHandlerMap.values().iterator();
			while (it.hasNext()) {
				if (it.next().hasWarnings()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void reset() {
		if (inlineErrorHandlerMap != null) {
			Iterator<StyleDeclarationErrorHandler> it = inlineErrorHandlerMap.values().iterator();
			while (it.hasNext()) {
				it.next().reset();
			}
		}
	}

	abstract protected AbstractCSSStyleSheetFactory getStyleSheetFactory();

}
