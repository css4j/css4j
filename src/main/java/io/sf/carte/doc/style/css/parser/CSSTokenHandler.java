/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.uparser.TokenControl;
import io.sf.carte.uparser.TokenProducer;

/**
 * The abstract base class for CSS token handlers.
 */
abstract class CSSTokenHandler extends AbstractTokenHandler implements CSSContentHandler {

	/**
	 * The parentheses depth.
	 */
	short parendepth = 0;

	CSSTokenHandler() {
		super();
	}

	/**
	 * 
	 * @return true if the stream may contain CDO-CDC comments.
	 */
	boolean isTopLevel() {
		return false;
	}

	short getCurrentParenDepth() {
		return parendepth;
	}

	/**
	 * Increase the parentheses depth by one.
	 */
	@Override
	public void incrParenDepth() {
		parendepth++;
	}

	/**
	 * Decrease the parentheses depth by one.
	 */
	@Override
	public void decrParenDepth() {
		parendepth--;
	}

	/**
	 * Decrease the parentheses depth by one.
	 * 
	 * @param index the parsing index.
	 */
	void decrParenDepth(int index) throws CSSParseException {
		decrParenDepth();
		if (parendepth < 0) {
			unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
		}
	}

	@Override
	public void commented(int index, int commentType, String comment) {
		separator(index, 12);
		prevcp = 12;
	}

	/*
	 * Default curly bracket management
	 */

	@Override
	public void leftCurlyBracket(int index) {
		unexpectedLeftCurlyBracketError(index);
	}

	@Override
	public void rightCurlyBracket(int index) {
		getManager().rightCurlyBracket(index);
		prevcp = TokenProducer.CHAR_RIGHT_CURLY_BRACKET;
	}

	/*
	 * Control access
	 */

	@Override
	public int getCurrentLine() {
		return getControlHandler().getCurrentLine();
	}

	@Override
	public int getPrevLineLength() {
		return getControlHandler().getPrevLineLength();
	}

	@Override
	void setCurrentLocation(int index) {
		getControlHandler().setCurrentLocation(index);
	}

	@Override
	CSSParseException createException(int index, byte errCode, String message) {
		return getControlHandler().createException(index, errCode, message);
	}

	/**
	 * Report an unexpected character error, do not do error recovery.
	 * 
	 * @param index the index.
	 * @return {@code true} if the error is recoverable
	 */
	boolean unexpectedSemicolonError(int index) {
		CSSParseException ex = createException(index, ParseHelper.ERR_UNEXPECTED_CHAR,
				"Unexpected ';'");
		reportError(ex);
		resetHandler();
		if (getCurrentParenDepth() > 0) {
			handleErrorRecovery();
			return false;
		}
		return true;
	}

	@Override
	protected void handleError(CSSParseException ex) throws CSSParseException {
		super.handleError(ex);
		handleErrorRecovery();
	}

	@Override
	abstract public void handleErrorRecovery();

	/**
	 * Get the control handler.
	 * 
	 * @return the control handler
	 */
	ControlTokenHandler getControlHandler() {
		return getManager().getControlHandler();
	}

	/**
	 * Get the manager.
	 * 
	 * @return the manager.
	 */
	@Override
	public abstract HandlerManager getManager();

	TokenControl getTokenControl() {
		return getControlHandler().getTokenControl();
	}

	protected void yieldHandling(CSSContentHandler yieldHandler) {
		getControlHandler().yieldHandling(yieldHandler);
	}

	/*
	 * Reset / End
	 */

	@Override
	public void resetHandler() {
		super.resetHandler();
		parendepth = 0;
	}

}
