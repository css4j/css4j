/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSFontFaceRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * Error handler that just throws exceptions.
 * <p>
 * Useful for debugging.
 * </p>
 */
public class ExceptionErrorHandler implements SheetErrorHandler {

	@Override
	public void mapError(CSSParseException exception, CSSRule rule) {
		throw exception;
	}

	@Override
	public void handleSacWarning(CSSParseException exception) {
		throw exception;
	}

	@Override
	public void handleSacError(CSSParseException exception) {
		throw exception;
	}

	@Override
	public boolean hasSacErrors() {
		return false;
	}

	@Override
	public boolean hasSacWarnings() {
		return false;
	}

	@Override
	public void badAtRule(DOMException e, String atRule) {
		throw e;
	}

	@Override
	public void badMediaList(MediaQueryList media) {
		throw createException("Bad media list: " + media.getMedia());
	}

	@Override
	public void ignoredImport(String uri) {
	}

	@Override
	public void conditionalRuleError(BooleanCondition condition, String message) {
		throw createException("Conditional rule issue: " + message);
	}

	@Override
	public void ruleParseError(CSSRule rule, CSSParseException ex) {
		throw ex;
	}

	@Override
	public void ruleParseWarning(CSSRule rule, CSSParseException ex) {
		throw ex;
	}

	@Override
	public void fontFormatError(CSSFontFaceRule rule, Exception exception) {
		if (exception instanceof RuntimeException) {
			throw (RuntimeException) exception;
		} else {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void unknownRule(String rule) {
	}

	@Override
	public void sacMalfunction(String message) {
		throw createException("SAC problem: " + message);
	}

	@Override
	public boolean hasOMErrors() {
		return false;
	}

	@Override
	public boolean hasOMWarnings() {
		return false;
	}

	@Override
	public void mergeState(SheetErrorHandler other) {
	}

	@Override
	public void reset() {
	}

	private DOMException createException(String message) {
		DOMException ex = new DOMException(DOMException.SYNTAX_ERR, message);
		return ex;
	}

}
