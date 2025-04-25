/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.uparser.ControlHandler;
import io.sf.carte.uparser.TokenControl;

/**
 * The abstract base class for ParserControl managers.
 */
abstract class ControlTokenHandler implements ParserControl, ControlHandler<RuntimeException> {

	private int line;
	private int prevlinelength;
	private boolean foundCp13andNotYet10or12 = false;
	private transient int column;

	private TokenControl parserctl = null;

	ControlTokenHandler() {
		super();
		line = 1;
		prevlinelength = -1;
	}

	ControlTokenHandler(ControlTokenHandler copyMe) {
		super();
		line = copyMe.getCurrentLine();
		prevlinelength = copyMe.getPrevLineLength();
		foundCp13andNotYet10or12 = copyMe.foundCp13andNotYet10or12;
		parserctl = copyMe.parserctl;
	}

	@Override
	public Locator createLocator() {
		return new LocatorImpl(line, column);
	}

	public int getCurrentLine() {
		return line;
	}

	public int getPrevLineLength() {
		return prevlinelength;
	}

	void setCurrentLocation(int index) {
		this.column = index - prevlinelength;
	}

	public CSSParseException createException(int index, byte errCode, String message) {
		setCurrentLocation(index);
		Locator locator = createLocator();
		if (errCode == ParseHelper.ERR_UNKNOWN_NAMESPACE) {
			return new CSSNamespaceParseException(message, locator);
		}
		return new CSSParseException(message, locator);
	}

	@Override
	public void quotedNewlineChar(int index, int codepoint) {
		if (codepoint == 10) { // LF \n
			if (!getCurrentHandler().isPreviousCp(13)) {
				line++;
				prevlinelength = index;
			}
		} else if (codepoint == 12) { // FF
			line++;
			prevlinelength = index;
		} else if (codepoint == 13) { // CR
			line++;
			prevlinelength = index;
			getCurrentHandler().setHandlerPreviousCp(codepoint);
		}
	}

	@Override
	public void control(int index, int codepoint) {
		/*
		 * Replace any U+000D CARRIAGE RETURN (CR) code points, U+000C FORM FEED (FF)
		 * code points, or pairs of U+000D CARRIAGE RETURN (CR) followed by U+000A LINE
		 * FEED (LF), by a single U+000A LINE FEED (LF) code point.
		 */
		AbstractTokenHandler curHandler = getCurrentHandler();
		if (codepoint == 10) { // LF \n
			curHandler.separator(index, 10);
			if (!foundCp13andNotYet10or12) {
				line++;
				prevlinelength = index;
			} else {
				foundCp13andNotYet10or12 = false;
				prevlinelength++;
			}
			curHandler.setHandlerPreviousCp(10);
		} else if (codepoint == 12) { // FF
			curHandler.separator(index, 10);
			curHandler.setHandlerPreviousCp(10);
			if (!foundCp13andNotYet10or12) {
				line++;
			} else {
				foundCp13andNotYet10or12 = false;
			}
			prevlinelength = index;
		} else if (codepoint == 13) { // CR
			line++;
			prevlinelength = index;
			foundCp13andNotYet10or12 = true;
		} else if (codepoint == 9) { // TAB
			curHandler.separator(index, 9);
		} else if (codepoint < 0x80) {
			curHandler.unexpectedCharError(index, codepoint);
		} else {
			highControl(index, codepoint);
		}
	}

	protected void highControl(int index, int codepoint) {
		// High control characters are excluded in XML and HTML for security reasons
		CSSTokenHandler curHandler = getCurrentHandler();
		if (!curHandler.isInError()) {
			curHandler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
					"Unexpected control: " + codepoint);
		}
	}

	@Override
	public void tokenStart(TokenControl control) {
		setTokenControl(control);
	}

	public void setTokenControl(TokenControl parserctl) {
		this.parserctl = parserctl;
	}

	public TokenControl getTokenControl() {
		return parserctl;
	}

	public CSSTokenHandler getCurrentHandler() {
		return (CSSTokenHandler) parserctl.getContentHandler();
	}

	public void yieldHandling(CSSContentHandler yieldHandler) {
		TokenControl ctl = getTokenControl();
		ctl.setContentHandler(yieldHandler);
		ctl.setErrorHandler(yieldHandler);
	}

	/**
	 * @return {@code true} if the current handler is doing error recovery.
	 */
	boolean isInErrorRecovery() {
		return getCurrentHandler() instanceof BufferTokenHandler.IgnoredDeclarationTokenHandler;
	}

}
