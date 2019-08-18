/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.CSSElement;

abstract public class DefaultErrorHandler extends AbstractErrorHandler {

	private LinkedHashMap<Node, String> linkedStyleErrors = null;
	private LinkedHashMap<Node, String> linkedStyleWarnings = null;
	private LinkedHashMap<Node, String> mediaErrors = null;
	private LinkedHashMap<Exception, String> inlineStyleErrors = null;
	private LinkedHashMap<Exception, CSSStyleSheet> linkedSheetErrors = null;
	private LinkedList<ComputedStyleError> computedStyleErrors = null;
	private LinkedHashMap<CSSElement, DOMException> hintErrors;

	@Override
	public boolean hasComputedStyleErrors() {
		return computedStyleErrors != null;
	}

	@Override
	public boolean hasErrors() {
		return linkedStyleErrors != null || mediaErrors != null || inlineStyleErrors != null
				|| linkedSheetErrors != null || hasInlineErrors() || computedStyleErrors != null
				|| hintErrors != null;
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
	public void mediaQueryError(Node node, String media) {
		if (mediaErrors == null) {
			mediaErrors = new LinkedHashMap<Node, String>();
		}
		mediaErrors.put(node, media);
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
	public void computedStyleError(Node node, String propertyName, String propertyValue, String message) {
		if (computedStyleErrors == null) {
			computedStyleErrors = new LinkedList<ComputedStyleError>();
		}
		MyComputedStyleError cse = new MyComputedStyleError();
		cse.node = node;
		cse.propertyName = propertyName;
		cse.propertyValue = propertyValue;
		cse.message = message;
		computedStyleErrors.add(cse);
	}

	public interface ComputedStyleError {

		String getMessage();

		Node getNode();

		String getPropertyName();

		String getPropertyValue();

	}

	private class MyComputedStyleError implements ComputedStyleError {
		private Node node;
		private String propertyName;
		private String propertyValue;
		private String message;

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public Node getNode() {
			return node;
		}

		@Override
		public String getPropertyName() {
			return propertyName;
		}

		@Override
		public String getPropertyValue() {
			return propertyValue;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("Node: <").append(node.getNodeName()).append(">, ").append(propertyName).append(':').append(' ')
					.append(propertyValue).append(" Message: ").append(message);
			return buf.toString();
		}

	}

	@Override
	public void presentationalHintError(CSSElement elm, DOMException e) {
		if (hintErrors == null) {
			hintErrors = new LinkedHashMap<CSSElement, DOMException>();
		}
		hintErrors.put(elm, e);
	}

	@Override
	public void resetComputedStyleErrors() {
		computedStyleErrors = null;
		hintErrors = null;
	}

	@Override
	public void reset() {
		linkedStyleErrors = null;
		linkedStyleWarnings = null;
		inlineStyleErrors = null;
		mediaErrors = null;
		linkedSheetErrors = null;
		resetComputedStyleErrors();
		super.reset();
	}

	public LinkedHashMap<Node, String> getLinkedStyleErrors() {
		return linkedStyleErrors;
	}

	public LinkedHashMap<Node, String> getLinkedStyleWarnings() {
		return linkedStyleWarnings;
	}

	public LinkedHashMap<Node, String> getMediaErrors() {
		return mediaErrors;
	}

	public LinkedHashMap<Exception, String> getInlineStyleErrors() {
		return inlineStyleErrors;
	}

	public LinkedHashMap<Exception, CSSStyleSheet> getLinkedSheetErrors() {
		return linkedSheetErrors;
	}

	public LinkedList<ComputedStyleError> getComputedStyleErrors() {
		return computedStyleErrors;
	}

	public LinkedHashMap<CSSElement, DOMException> getHintErrors() {
		return hintErrors;
	}

}
