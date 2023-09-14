/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

abstract class AbstractErrorHandler implements ErrorHandler, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private HashMap<Node,String> policyErrorMap = null;

	private HashMap<CSSElement, StyleDeclarationErrorHandler> inlineErrorHandlerMap = null;

	private HashMap<CSSElement, HashMap<String, CSSPropertyValueException>> computedStyleErrors = null;

	private HashMap<CSSElement, List<DOMException>> hintErrors = null;

	private HashMap<Node, CSSMediaException> mediaQueryErrors = null;

	private HashMap<String,IOException> ioErrors = null;

	private HashMap<CSSElement, HashMap<String, CSSPropertyValueException>> computedStyleWarnings = null;

	private HashMap<Node, CSSMediaException> mediaQueryWarnings = null;

	AbstractErrorHandler() {
		super();
	}

	@Override
	public StyleDeclarationErrorHandler getInlineStyleErrorHandler(CSSElement owner) {
		if (inlineErrorHandlerMap == null) {
			inlineErrorHandlerMap = new HashMap<>();
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
			for (StyleDeclarationErrorHandler handler : inlineErrorHandlerMap.values()) {
				if (handler.hasErrors()) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean hasInlineWarnings() {
		if (inlineErrorHandlerMap != null) {
			for (StyleDeclarationErrorHandler handler : inlineErrorHandlerMap.values()) {
				if (handler.hasWarnings()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void policyError(Node node, String message) {
		if (policyErrorMap == null) {
			policyErrorMap = new HashMap<>();
		}
		policyErrorMap.put(node, message);
	}

	@Override
	public void computedStyleError(CSSElement element, String propertyName, CSSPropertyValueException exception) {
		HashMap<String, CSSPropertyValueException> map;
		if (computedStyleErrors == null) {
			computedStyleErrors = new HashMap<>();
			map = new HashMap<>();
			computedStyleErrors.put(element, map);
		} else {
			map = computedStyleErrors.get(element);
			if (map == null) {
				map = new HashMap<>();
				computedStyleErrors.put(element, map);
			}
		}
		map.put(propertyName, exception);
	}

	@Override
	public void mediaQueryError(Node ownerNode, CSSMediaException exception) {
		if (mediaQueryErrors == null) {
			mediaQueryErrors = new HashMap<>(5);
		}
		mediaQueryErrors.put(ownerNode, exception);
	}

	@Override
	public void ioError(String uri, IOException exception) {
		if (ioErrors == null) {
			ioErrors = new HashMap<>();
		}
		ioErrors.put(uri, exception);
	}

	@Override
	public boolean hasErrors() {
		return hasInlineErrors() || hasComputedStyleErrors() || hasMediaErrors() || hasIOErrors() || hasPolicyErrors();
	}

	@Override
	public boolean hasPolicyErrors() {
		return policyErrorMap != null;
	}

	@Override
	public boolean hasIOErrors() {
		return ioErrors != null;
	}

	@Override
	public boolean hasComputedStyleErrors() {
		return (computedStyleErrors != null && !computedStyleErrors.isEmpty())
				|| (hintErrors != null && !hintErrors.isEmpty());
	}

	@Override
	public boolean hasComputedStyleErrors(CSSElement element) {
		if (computedStyleErrors != null && computedStyleErrors.containsKey(element)) {
			return true;
		}
		if (hintErrors != null && hintErrors.containsKey(element)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasMediaErrors() {
		return mediaQueryErrors != null && !mediaQueryErrors.isEmpty();
	}

	@Override
	public boolean hasWarnings() {
		return hasInlineWarnings() || hasComputedStyleWarnings() || hasMediaWarnings();
	}

	@Override
	public boolean hasMediaWarnings() {
		return mediaQueryWarnings != null && !mediaQueryWarnings.isEmpty();
	}

	@Override
	public boolean hasComputedStyleWarnings() {
		return computedStyleWarnings != null && !computedStyleWarnings.isEmpty();
	}

	@Override
	public boolean hasComputedStyleWarnings(CSSElement element) {
		if (computedStyleWarnings != null && computedStyleWarnings.containsKey(element)) {
			return true;
		}
		return false;
	}

	@Override
	public void computedStyleWarning(CSSElement element, String propertyName, CSSPropertyValueException exception) {
		HashMap<String, CSSPropertyValueException> map;
		if (computedStyleWarnings == null) {
			computedStyleWarnings = new HashMap<>();
			map = new HashMap<>();
			computedStyleWarnings.put(element, map);
		} else {
			map = computedStyleWarnings.get(element);
			if (map == null) {
				map = new HashMap<>();
				computedStyleWarnings.put(element, map);
			}
		}
		map.put(propertyName, exception);
	}

	@Override
	public void mediaQueryWarning(Node ownerNode, CSSMediaException exception) {
		if (mediaQueryWarnings == null) {
			mediaQueryWarnings = new HashMap<>(5);
		}
		mediaQueryWarnings.put(ownerNode, exception);
	}

	public String getPolicyError(Node node) {
		return policyErrorMap != null ? policyErrorMap.get(node) : null;
	}

	public HashMap<String, CSSPropertyValueException> getComputedStyleErrors(CSSElement element) {
		return computedStyleErrors != null ? computedStyleErrors.get(element) : null;
	}

	public List<DOMException> getHintErrors(CSSElement element) {
		return hintErrors != null ? hintErrors.get(element) : null;
	}

	public HashMap<String, CSSPropertyValueException> getComputedStyleWarnings(CSSElement element) {
		return computedStyleWarnings != null ? computedStyleWarnings.get(element) : null;
	}

	public HashMap<Node, String> getPolicyErrors() {
		return policyErrorMap;
	}

	public HashMap<Node, CSSMediaException> getMediaErrors() {
		return mediaQueryErrors;
	}

	@Deprecated
	public HashMap<String, IOException> getRuleIOErrors() {
		return getIOErrors();
	}

	public HashMap<String, IOException> getIOErrors() {
		return ioErrors;
	}

	@Override
	public void resetComputedStyleErrors(CSSElement element) {
		if (computedStyleErrors != null) {
			computedStyleErrors.remove(element);
		}
		if (hintErrors != null) {
			hintErrors.remove(element);
		}
		if (computedStyleWarnings != null) {
			computedStyleWarnings.remove(element);
		}
	}

	@Override
	public void resetComputedStyleErrors() {
		computedStyleErrors = null;
		hintErrors = null;
		computedStyleWarnings = null;
	}

	@Override
	public void presentationalHintError(CSSElement elm, DOMException ex) {
		List<DOMException> exlist;
		if (hintErrors == null) {
			hintErrors = new HashMap<>();
			exlist = new LinkedList<>();
			hintErrors.put(elm, exlist);
		} else {
			exlist = hintErrors.get(elm);
			if (exlist == null) {
				exlist = new LinkedList<>();
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
		mediaQueryErrors = null;
		mediaQueryWarnings = null;
		policyErrorMap = null;
		ioErrors = null;
	}

	abstract protected AbstractCSSStyleSheetFactory getStyleSheetFactory();

}
