/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.uparser.ContentHandler;
import io.sf.carte.uparser.TokenErrorHandler;

interface CSSContentHandler
		extends ContentHandler<RuntimeException>, TokenErrorHandler<RuntimeException> {

	/**
	 * Increase the parentheses depth by one.
	 */
	void incrParenDepth();

	/**
	 * Decrease the parentheses depth by one.
	 */
	void decrParenDepth();

	/**
	 * Unexpected left square bracket error.
	 * 
	 * @param index the index.
	 */
	void unexpectedLeftSquareBracketError(int index);

	/**
	 * Unexpected right square bracket error.
	 * 
	 * @param index the index.
	 */
	void unexpectedRightSquareBracketError(int index);

	/**
	 * Unexpected right curly bracket error.
	 * 
	 * @param index the index.
	 */
	void unexpectedRightCurlyBracketError(int index);

	void setParseError();

	void reportError(int index, byte errCode, String message) throws CSSParseException;

	void reportError(CSSParseException ex) throws CSSParseException;

	void handleError(int index, byte errCode, String message) throws CSSParseException;

	void handleWarning(int index, byte errCode, String message);

	void handleWarning(int index, byte errCode, String message, Throwable cause);

	void unexpectedEOFError(int len) throws CSSParseException;

	boolean isInError();

	void handleErrorRecovery();

	default CSSErrorHandler getErrorHandler() {
		return getManager().getErrorHandler();
	}

	/**
	 * Get the manager.
	 * 
	 * @return the manager.
	 */
	HandlerManager getManager();

	@Override
	void endOfStream(int len);

}
