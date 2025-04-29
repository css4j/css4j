/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.uparser.TokenProducer;

abstract class CallbackTokenHandler extends BufferTokenHandler {

	final CSSContentHandler caller;

	private CSSContentHandler rootHandler;

	/**
	 * Construct a new CallbackTokenHandler.
	 * 
	 * @param caller the caller handler. By default, the caller handler will also be
	 *               the root handler.
	 */
	CallbackTokenHandler(CSSContentHandler caller) {
		super();
		this.caller = caller;
		this.rootHandler = caller;
	}

	protected CSSContentHandler getCaller() {
		return caller;
	}

	CSSContentHandler getRootHandler() {
		return rootHandler;
	}

	/**
	 * Sets the root handler.
	 * <p>
	 * The root handler is responsible for error recovery.
	 * </p>
	 * 
	 * @param rootHandler the root handler.
	 */
	void setRootHandler(CSSContentHandler rootHandler) {
		this.rootHandler = rootHandler;
	}

	@Override
	public void leftParenthesis(int index) {
		incrParenDepth();
		unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
	}

	@Override
	public void rightParenthesis(int index) {
		processBuffer(index, TokenProducer.CHAR_RIGHT_PAREN);
		caller.rightParenthesis(index);
	}

	@Override
	public void rightSquareBracket(int index) {
		processBuffer(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
		caller.rightSquareBracket(index);
	}

	@Override
	public void rightCurlyBracket(int index) {
		processBuffer(index, TokenProducer.CHAR_RIGHT_PAREN);
		caller.rightCurlyBracket(index);
	}

	@Override
	public void character(int index, int codePoint) {
		if (codePoint == TokenProducer.CHAR_SEMICOLON) {
			handleSemicolon(index);
		} else {
			unexpectedCharError(index, codePoint);
		}
	}

	void handleSemicolon(int index) {
		processBuffer(index, TokenProducer.CHAR_SEMICOLON);
		caller.character(index, TokenProducer.CHAR_SEMICOLON);
	}

	@Override
	public void handleErrorRecovery() {
		yieldHandling(new CallbackIgnoredDeclarationTH());
	}

	private class CallbackIgnoredDeclarationTH extends IgnoredDeclarationTokenHandler {

		CallbackIgnoredDeclarationTH() {
			super();
		}

		@Override
		public void resetHandler() {
			rootHandler.resetHandler();
			rootHandler.resetParseError();
		}

	}

	@Override
	public void setParseError() {
		super.setParseError();
		caller.setParseError();
	}

	@Override
	public void endOfStream(int len) {
		processBuffer(len, 0);
		CSSTokenHandler cur = getControlHandler().getCurrentHandler();
		if (cur != this) {
			cur.endOfStream(len);
		} else {
			caller.endOfStream(len);
		}
	}

	@Override
	public CSSErrorHandler getErrorHandler() {
		return rootHandler.getErrorHandler();
	}

	@Override
	public HandlerManager getManager() {
		return rootHandler.getManager();
	}

	void yieldHandling(CallbackTokenHandler yieldHandler) {
		yieldHandler.setRootHandler(rootHandler);
		super.yieldHandling(yieldHandler);
	}

	void yieldBack() {
		super.yieldHandling(caller);
	}

	class CallbackValueTokenHandler extends ValueTokenHandler {

		CallbackValueTokenHandler() {
			super(false);
			this.parendepth = CallbackTokenHandler.this.parendepth;
		}

		@Override
		public void endFunctionArgument(int index) {
			super.endFunctionArgument(index);
			checkFunctionCallback(index);
		}

		protected void checkFunctionCallback(int index) {
			if (!functionToken && !isInError()) {
				CallbackTokenHandler.this.rightParenthesis(index);
			}
		}

		@Override
		public void handleErrorRecovery() {
			CallbackTokenHandler.this.handleErrorRecovery();
		}

		@Override
		public boolean isInError() {
			return CallbackTokenHandler.this.isInError();
		}

		@Override
		public void reportError(CSSParseException ex) throws CSSParseException {
			CallbackTokenHandler.this.reportError(ex);
		}

		@Override
		protected void handleError(CSSParseException ex) throws CSSParseException {
			CallbackTokenHandler.this.handleError(ex);
		}

		@Override
		public CSSErrorHandler getErrorHandler() {
			return CallbackTokenHandler.this.getErrorHandler();
		}

		@Override
		public HandlerManager getManager() {
			return CallbackTokenHandler.this.getManager();
		}

	}

}
