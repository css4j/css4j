/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.CSSElement;

/**
 * A sample <code>ErrorHandler</code> that uses SLF4J logging.
 */
abstract public class LogErrorHandler extends AbstractErrorHandler {

	static Logger log = LoggerFactory.getLogger(LogErrorHandler.class.getName());

	private boolean cserrors = false, errors = false, warnings = false;

	@Override
	public boolean hasComputedStyleErrors() {
		return cserrors;
	}

	@Override
	public boolean hasErrors() {
		return errors || hasInlineErrors() || hasComputedStyleErrors();
	}

	@Override
	public boolean hasWarnings() {
		return warnings || hasInlineWarnings();
	}

	@Override
	public void linkedStyleError(Node node, String message) {
		log.error("Error processing linked style: " + message);
		errors = true;
	}

	@Override
	public void linkedStyleWarning(Node node, String message) {
		log.warn("Warning processing linked style: " + message);
		warnings = true;
	}

	@Override
	public void mediaQueryError(Node node, String media) {
		log.error("Error parsing media text: " + media);
		errors = true;
	}

	@Override
	public void linkedSheetError(Exception e, CSSStyleSheet sheet) {
		String href = sheet.getHref();
		if (href != null) {
			log.error("Error parsing sheet at " + href);
		}
		log.error(((AbstractCSSStyleSheet) sheet).toStyleString(), e);
		errors = true;
	}

	@Override
	public void inlineStyleError(CSSElement owner, Exception e, String context) {
		log.error(context, e);
		errors = true;
	}

	@Override
	public void computedStyleError(Node node, String propertyName, String propertyValue, String message) {
		log.error("Computed style error [" + propertyName + "]: " + message);
		cserrors = true;
	}

	@Override
	public void presentationalHintError(CSSElement elm, DOMException e) {
		log.error("Presentational hint error (element " + elm.getTagName() + ")", e);
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
		super.reset();
	}

}
