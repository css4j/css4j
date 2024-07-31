/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.LinkedHashMap;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;

abstract public class DefaultErrorHandler extends AbstractErrorHandler {

	private static final long serialVersionUID = 1L;

	private LinkedHashMap<Node, String> linkedStyleErrors = null;
	private LinkedHashMap<Exception, String> inlineStyleErrors = null;
	private LinkedHashMap<Exception, CSSStyleSheet<? extends CSSRule>> linkedSheetErrors = null;

	@Override
	public boolean hasErrors() {
		return linkedStyleErrors != null || inlineStyleErrors != null
				|| linkedSheetErrors != null || super.hasErrors();
	}

	@Override
	public void linkedStyleError(Node node, String message) {
		if (linkedStyleErrors == null) {
			linkedStyleErrors = new LinkedHashMap<>();
		}
		linkedStyleErrors.put(node, message);
	}

	@Override
	public void linkedSheetError(Exception e, CSSStyleSheet<? extends CSSRule> sheet) {
		if (linkedSheetErrors == null) {
			linkedSheetErrors = new LinkedHashMap<>();
		}
		linkedSheetErrors.put(e, sheet);
	}

	@Override
	public void inlineStyleError(CSSElement owner, Exception e, String style) {
		if (inlineStyleErrors == null) {
			inlineStyleErrors = new LinkedHashMap<>();
		}
		inlineStyleErrors.put(e, style);
	}

	@Override
	public void reset() {
		linkedStyleErrors = null;
		inlineStyleErrors = null;
		linkedSheetErrors = null;
		super.reset();
	}

	public LinkedHashMap<Node, String> getLinkedStyleErrors() {
		return linkedStyleErrors;
	}

	public LinkedHashMap<Exception, String> getInlineStyleErrors() {
		return inlineStyleErrors;
	}

	public LinkedHashMap<Exception, CSSStyleSheet<? extends CSSRule>> getLinkedSheetErrors() {
		return linkedSheetErrors;
	}

}
