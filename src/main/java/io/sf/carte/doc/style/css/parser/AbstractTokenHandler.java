/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.uparser.ContentHandler;
import io.sf.carte.uparser.TokenErrorHandler;
import io.sf.carte.uparser.TokenProducer;

abstract class AbstractTokenHandler
		implements ContentHandler<RuntimeException>, TokenErrorHandler<RuntimeException> {

	static final int ENDCP = -1;

	boolean parseError = false;

	int prevcp = 32;

	protected AbstractTokenHandler() {
		super();
	}

	/*
	 * Track the previous codePoint
	 */

	/**
	 * Return true if previous codepoint is whitespace (codepoints 32 and 10).
	 * 
	 * @return true if previous codepoint is whitespace.
	 */
	boolean isPrevCpWhitespace() {
		return prevcp == 32 || prevcp == 10;
	}

	void setWhitespacePrevCp() {
		if (prevcp != 10) {
			prevcp = 32;
		}
	}

	boolean isPreviousCp(int codePoint) {
		return prevcp == codePoint;
	}

	protected boolean isPreviousCpLF() {
		return prevcp == 10;
	}

	boolean isEndCp() {
		return prevcp == ENDCP;
	}

	protected void setHandlerPreviousCp(int cp) {
		prevcp = cp;
	}

	/*
	 * Error reporting and handling
	 */

	public void handleError(int index, byte errCode, String message)
			throws CSSParseException {
		if (!isInError()) {
			CSSParseException ex;
			if (isEndCp()) {
				ex = createException(index, errCode, "Unexpected end of file");
				reportError(ex);
			} else {
				ex = createException(index, errCode, message);
				handleError(ex);
			}
		}
	}

	void handleError(int index, byte errCode, String message, Throwable cause)
			throws CSSParseException {
		if (!isInError()) {
			CSSParseException ex;
			if (isEndCp()) {
				ex = createException(index, errCode, "Unexpected end of file");
				ex.initCause(cause);
				reportError(ex);
			} else {
				ex = createException(index, errCode, message);
				ex.initCause(cause);
				handleError(ex);
			}
		}
	}

	protected void handleError(CSSParseException ex) throws CSSParseException {
		reportError(ex);
	}

	void reportError(int index, byte errCode, String message) throws CSSParseException {
		CSSParseException ex = createException(index, errCode, message);
		reportError(ex);
	}

	abstract public void reportError(CSSParseException ex) throws CSSParseException;

	void unexpectedLeftCurlyBracketError(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
	}

	void unexpectedCharError(int index, int codepoint) {
		handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
				"Unexpected '" + new String(Character.toChars(codepoint)) + "'");
	}

	void unexpectedTokenError(int index, CharSequence token) {
		handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: " + token);
	}

	public void unexpectedEOFError(int len) {
		unexpectedEOFError(len, "Unexpected end of stream");
	}

	public void unexpectedEOFError(int len, String message) {
		if (!isInError()) {
			CSSParseException ex = createException(len, ParseHelper.ERR_UNEXPECTED_EOF,
					message);
			reportError(ex);
		}
	}

	public final void handleWarning(int index, byte errCode, String message) {
		handleWarning(index, errCode, message, null);
	}

	abstract public void handleWarning(int index, byte errCode, String message, Throwable cause);

	/*
	 * TokenErrorHandler method
	 */
	@Override
	public void error(int index, byte errCode, CharSequence context) {
		handleError(index, errCode, "Syntax error near " + context);
	}

	/**
	 * @return {@code true} if the handler is in error state.
	 */
	boolean isInError() {
		return parseError;
	}

	/**
	 * Sets this manager to be in a state of error.
	 */
	public void setParseError() {
		this.parseError = true;
	}

	void resetParseError() {
		parseError = false;
	}

	protected void resetHandler() {
		prevcp = 32;
	}

	/*
	 * Error-triggering event defaults
	 */

	@Override
	public void quoted(int index, CharSequence quoted, int quote) {
		unexpectedTokenError(index, quoted);
	}

	@Override
	public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
		unexpectedTokenError(index, quoted);
	}

	@Override
	public void leftParenthesis(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
	}

	@Override
	public void leftSquareBracket(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
	}

	@Override
	public void rightParenthesis(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
	}

	@Override
	public void rightSquareBracket(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
	}

	/*
	 * Location-related
	 */

	abstract public int getCurrentLine();

	abstract public int getPrevLineLength();

	abstract void setCurrentLocation(int index);

	abstract CSSParseException createException(int index, byte errCode, String message);

}