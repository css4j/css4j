/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

abstract class AbstractErrorHandler implements ErrorHandler {

	private HashMap<CSSElement, StyleDeclarationErrorHandler> inlineErrorHandlerMap = null;

	private HashMap<CSSElement, HashMap<String, CSSPropertyValueException>> computedStyleErrors = null;

	private HashMap<CSSElement, List<DOMException>> hintErrors = null;

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
	public void computedStyleError(CSSElement element, String propertyName, CSSPropertyValueException exception) {
		HashMap<String, CSSPropertyValueException> map;
		if (computedStyleErrors == null) {
			computedStyleErrors = new HashMap<CSSElement, HashMap<String, CSSPropertyValueException>>();
			map = new HashMap<String, CSSPropertyValueException>();
			computedStyleErrors.put(element, map);
		} else {
			map = computedStyleErrors.get(element);
			if (map == null) {
				map = new HashMap<String, CSSPropertyValueException>();
				computedStyleErrors.put(element, map);
			}
		}
		map.put(propertyName, exception);
	}

	@Override
	public boolean hasComputedStyleErrors() {
		return (computedStyleErrors != null && !computedStyleErrors.isEmpty())
				|| (hintErrors != null && !hintErrors.isEmpty());
	}

	@Override
	public boolean hasComputedStyleErrors(CSSElement element) {
		if (computedStyleErrors != null) {
			if (computedStyleErrors.containsKey(element)) {
				return true;
			}
		}
		if (hintErrors != null) {
			if (hintErrors.containsKey(element)) {
				return true;
			}
		}
		return false;
	}

	public HashMap<String, CSSPropertyValueException> getComputedStyleErrors(CSSElement element) {
		return computedStyleErrors.get(element);
	}

	public List<DOMException> getHintErrors(CSSElement element) {
		return hintErrors.get(element);
	}

	@Override
	public void resetComputedStyleErrors(CSSElement element) {
		if (computedStyleErrors != null) {
			computedStyleErrors.remove(element);
		}
		if (hintErrors != null) {
			hintErrors.remove(element);
		}
	}

	@Override
	public void resetComputedStyleErrors() {
		computedStyleErrors = null;
		hintErrors = null;
	}

	@Override
	public void presentationalHintError(CSSElement elm, DOMException ex) {
		List<DOMException> exlist;
		if (hintErrors == null) {
			hintErrors = new HashMap<CSSElement, List<DOMException>>();
			exlist = new LinkedList<DOMException>();
			hintErrors.put(elm, exlist);
		} else {
			exlist = hintErrors.get(elm);
			if (exlist == null) {
				exlist = new LinkedList<DOMException>();
			}
		}
		exlist.add(ex);
	}

	@Override
	public void reset() {
		if (inlineErrorHandlerMap != null) {
			Iterator<StyleDeclarationErrorHandler> it = inlineErrorHandlerMap.values().iterator();
			while (it.hasNext()) {
				it.next().reset();
			}
		}
		resetComputedStyleErrors();
	}

	abstract protected AbstractCSSStyleSheetFactory getStyleSheetFactory();

}
