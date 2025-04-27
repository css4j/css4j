/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.uparser.TokenProducer;

/**
 * The abstract class for CSS token handlers that use a buffer.
 */
abstract class BufferTokenHandler extends CSSTokenHandler implements CSSContentHandler {

	StringBuilder buffer;

	private int escapedTokenIndex = -1;

	BufferTokenHandler() {
		super();
		initializeBuffer();
	}

	protected void initializeBuffer() {
	}

	protected void setEscapedTokenStart(int index) {
		if (escapedTokenIndex == -1) {
			escapedTokenIndex = index - 1;
		}
	}

	/**
	 * Sets the escaped Token index to -1.
	 */
	protected void resetEscapedTokenIndex() {
		this.escapedTokenIndex = -1;
	}

	boolean isEscapedIdent() {
		return getEscapedTokenIndex() != -1;
	}

	/**
	 * @return the escaped Token index
	 */
	protected int getEscapedTokenIndex() {
		return escapedTokenIndex;
	}

	@Override
	public void word(int index, CharSequence word) {
		buffer.append(word);
		prevcp = 65; // A
	}

	@Override
	public void escaped(int index, int codePoint) {
		// We add a backslash if is an hex, \ (0x5c), + (0x2b) , - (0x2d)
		// or whitespace (0x20) to avoid confusions with numbers and
		// operators
		if (isEscapedCodepoint(codePoint)) {
			setEscapedTokenStart(index);
			buffer.append('\\');
		}
		prevcp = 65;
		bufferAppend(codePoint);
	}

	/**
	 * Whether a backslash should be added due to the codepoint being an hex, \
	 * (0x5c), + (0x2b) , - (0x2d) or whitespace (0x20), to avoid confusions with
	 * numbers and operators.
	 * 
	 * @param codepoint the codepoint to check.
	 * @return {@code true} if the given codepoint should be escaped.
	 */
	boolean isEscapedCodepoint(int codepoint) {
		return ParseHelper.isHexCodePoint(codepoint) || codepoint == 0x5c || codepoint == 0x2b
				|| codepoint == 0x2d || codepoint == 0x20;
	}

	@Override
	public void separator(int index, int codepoint) {
		if (getEscapedTokenIndex() != -1 && CSSParser.bufferEndsWithEscapedChar(buffer)) {
			buffer.append(' ');
		} else {
			if (buffer.length() != 0) {
				processBuffer(index, codepoint);
			}
			setWhitespacePrevCp();
		}
	}

	@Override
	public void endOfStream(int len) {
		if (buffer.length() != 0) {
			processBuffer(len, 0);
		}
	}

	abstract void processBuffer(int index, int triggerCp);

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
	 * Buffer operations
	 */

	void bufferAppend(char c) {
		buffer.append(c);
	}

	void bufferAppend(int codepoint) {
		buffer.appendCodePoint(codepoint);
	}

	String rawBuffer() {
		resetEscapedTokenIndex();
		String s = buffer.toString();
		buffer.setLength(0);
		return s;
	}

	/**
	 * Get the result of unescaping the buffer.
	 * <p>
	 * Same as {@link #unescapeStringValue(int)} but also resets the buffer.
	 * </p>
	 * 
	 * @param index the index.
	 * @return the unescaped buffer value.
	 */
	String unescapeBuffer(int index) {
		String s = unescapeStringValue(index);
		buffer.setLength(0);
		resetEscapedTokenIndex();
		return s;
	}

	/**
	 * Get the result of unescaping the buffer, without resetting it.
	 * 
	 * @param index the index.
	 * @return the unescaped buffer value.
	 */
	String unescapeStringValue(int index) {
		String s;
		if (isEscapedIdent()) {
			int escsz = index - escapedTokenIndex;
			int rawlen = buffer.length() - escsz;
			if (rawlen <= 0) {
				s = CSSParser.safeUnescapeIdentifier(index, buffer.toString());
			} else {
				CharSequence rawseq = buffer.subSequence(0, rawlen);
				s = rawseq + CSSParser.safeUnescapeIdentifier(index, buffer.substring(rawlen));
			}
		} else {
			s = buffer.toString();
		}
		return s;
	}

	/*
	 * Reset / End
	 */

	@Override
	protected void resetHandler() {
		super.resetHandler();
		resetEscapedTokenIndex();
	}

	/*
	 * Error management
	 */

	@Override
	public void reportError(CSSParseException ex) throws CSSParseException {
		if (getErrorHandler() != null) {
			getErrorHandler().error(ex);
		} else {
			throw ex;
		}
		setParseError();
	}

	@Override
	public void handleWarning(int index, byte errCode, String message, Throwable cause) {
		if (!isInError() && getErrorHandler() != null) {
			CSSParseException ex = createException(index, errCode, message);
			if (cause != null) {
				ex.initCause(cause);
			}
			getErrorHandler().warning(ex);
		}
	}

	@Override
	void unexpectedLeftCurlyBracketError(int index) {
		if (!isInError()) {
			super.unexpectedLeftCurlyBracketError(index);
		}
		AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
		if (curh != this) {
			curh.leftCurlyBracket(index);
		}
	}

	void sendLeftCurlyBracketEvent(int index, AbstractTokenHandler fromTH) {
		AbstractTokenHandler curTH = getControlHandler().getCurrentHandler();
		if (curTH != fromTH) {
			curTH.leftCurlyBracket(index);
		} else {
			throw new IllegalStateException("Handler sends event to itself.");
		}
	}

	@Override
	public void unexpectedLeftSquareBracketError(int index) {
		if (!isInError()) {
			super.unexpectedLeftSquareBracketError(index);
		}
		AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
		if (curh != this) {
			curh.leftSquareBracket(index);
		}
	}

	@Override
	public void unexpectedRightCurlyBracketError(int index) {
		if (!isInError()) {
			super.unexpectedRightCurlyBracketError(index);
		}
		AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
		if (curh != this) {
			curh.rightCurlyBracket(index);
		}
	}

	@Override
	public void unexpectedRightSquareBracketError(int index) {
		if (!isInError()) {
			super.unexpectedRightSquareBracketError(index);
		}
		AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
		if (curh != this) {
			curh.rightSquareBracket(index);
		}
	}

	@Override
	protected void yieldHandling(CSSContentHandler yieldHandler) {
		ControlTokenHandler ctl = getControlHandler();
		if (ctl.getCurrentHandler() instanceof IgnoredDeclarationTokenHandler) {
			throw new IllegalStateException("Attempting to replace an error handler.");
		}
		ctl.yieldHandling(yieldHandler);
	}

	class IgnoredDeclarationTokenHandler extends CSSTokenHandler {

		/**
		 * The current curly bracket depth.
		 */
		private int curlyBracketDepth = 0;

		private int sqBracketDepth = 0;

		IgnoredDeclarationTokenHandler() {
			super();
			this.parendepth = BufferTokenHandler.this.getCurrentParenDepth();
		}

		@Override
		public void word(int index, CharSequence word) {
		}

		@Override
		public void separator(int index, int codePoint) {
		}

		@Override
		public void commented(int index, int commentType, String comment) {
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quote) {
		}

		@Override
		public void leftParenthesis(int index) {
			parendepth++;
		}

		@Override
		public void leftSquareBracket(int index) {
			sqBracketDepth++;
		}

		@Override
		public void rightParenthesis(int index) {
			parendepth--;
		}

		@Override
		public void rightSquareBracket(int index) {
			sqBracketDepth--;
		}

		@Override
		public void leftCurlyBracket(int index) {
			curlyBracketDepth++;
		}

		@Override
		public void rightCurlyBracket(int index) {
			curlyBracketDepth--;
			if (isRecoverable()) {
				if (curlyBracketDepth < 0) {
					endDeclarationBlock(index);
					BufferTokenHandler.this.resetParseError();
					BufferTokenHandler.this.resetHandler();
				} else if (curlyBracketDepth == 0) {
					resumeDeclarationRuleList();
				}
			}
		}

		boolean isRecoverable() {
			return parendepth == 0 && sqBracketDepth == 0;
		}

		@Override
		public void character(int index, int codePoint) {
			if (codePoint == TokenProducer.CHAR_SEMICOLON && curlyBracketDepth == 0
					&& isRecoverable()) {
				BufferTokenHandler.this.resetHandler();
				BufferTokenHandler.this.resetParseError();
				resumeDeclarationList();
			}
		}

		protected void resumeDeclarationRuleList() {
			BufferTokenHandler.this.resetHandler();
			BufferTokenHandler.this.resetParseError();
			resumeDeclarationList();
		}

		protected void resumeDeclarationList() {
			getManager().restoreInitialHandler();
		}

		@Override
		public void endOfStream(int len) {
			getManager().endOfStream(len);
		}

		protected void endDeclarationBlock(int index) {
			getManager().endManagement(index);
		}

		@Override
		public void escaped(int index, int codePoint) {
		}

		@Override
		public void handleErrorRecovery() {
		}

		@Override
		protected void resetHandler() {
			super.resetHandler();
			sqBracketDepth = 0;
		}

		@Override
		public boolean isInError() {
			return true;
		}

		@Override
		public void reportError(CSSParseException ex) throws CSSParseException {
			BufferTokenHandler.this.reportError(ex);
		}

		@Override
		public void handleWarning(int index, byte errCode, String message, Throwable cause) {
		}

		@Override
		ControlTokenHandler getControlHandler() {
			return BufferTokenHandler.this.getControlHandler();
		}

		@Override
		public HandlerManager getManager() {
			return BufferTokenHandler.this.getManager();
		}

	}

}
