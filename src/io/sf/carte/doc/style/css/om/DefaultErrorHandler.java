/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.LinkedHashMap;

import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;

abstract public class DefaultErrorHandler extends AbstractErrorHandler {

	private LinkedHashMap<Node, String> linkedStyleErrors = null;
	private LinkedHashMap<Node, String> linkedStyleWarnings = null;
	private LinkedHashMap<Node, CSSMediaException> mediaErrors = null;
	private LinkedHashMap<Exception, String> inlineStyleErrors = null;
	private LinkedHashMap<Exception, CSSStyleSheet> linkedSheetErrors = null;

	@Override
	public boolean hasErrors() {
		return linkedStyleErrors != null || mediaErrors != null || inlineStyleErrors != null
				|| linkedSheetErrors != null || hasInlineErrors() || hasComputedStyleErrors();
	}

	@Override
	public boolean hasWarnings() {
		return linkedStyleWarnings != null || hasInlineWarnings();
	}

	@Override
	public void linkedStyleError(Node node, String message) {
		if (linkedStyleErrors == null) {
			linkedStyleErrors = new LinkedHashMap<Node, String>();
		}
		linkedStyleErrors.put(node, message);
	}

	@Override
	public void linkedStyleWarning(Node node, String message) {
		if (linkedStyleWarnings == null) {
			linkedStyleWarnings = new LinkedHashMap<Node, String>();
		}
		linkedStyleWarnings.put(node, message);
	}

	@Override
	public void mediaQueryError(Node node, CSSMediaException exception) {
		if (mediaErrors == null) {
			mediaErrors = new LinkedHashMap<Node, CSSMediaException>();
		}
		mediaErrors.put(node, exception);
	}

	@Override
	public void linkedSheetError(Exception e, CSSStyleSheet sheet) {
		if (linkedSheetErrors == null) {
			linkedSheetErrors = new LinkedHashMap<Exception, CSSStyleSheet>();
		}
		linkedSheetErrors.put(e, sheet);
	}

	@Override
	public void inlineStyleError(CSSElement owner, Exception e, String style) {
		if (inlineStyleErrors == null) {
			inlineStyleErrors = new LinkedHashMap<Exception, String>();
		}
		inlineStyleErrors.put(e, style);
	}

	@Override
	public void reset() {
		linkedStyleErrors = null;
		linkedStyleWarnings = null;
		inlineStyleErrors = null;
		mediaErrors = null;
		linkedSheetErrors = null;
		super.reset();
	}

	public LinkedHashMap<Node, String> getLinkedStyleErrors() {
		return linkedStyleErrors;
	}

	public LinkedHashMap<Node, String> getLinkedStyleWarnings() {
		return linkedStyleWarnings;
	}

	public LinkedHashMap<Node, CSSMediaException> getMediaErrors() {
		return mediaErrors;
	}

	public LinkedHashMap<Exception, String> getInlineStyleErrors() {
		return inlineStyleErrors;
	}

	public LinkedHashMap<Exception, CSSStyleSheet> getLinkedSheetErrors() {
		return linkedSheetErrors;
	}

}
