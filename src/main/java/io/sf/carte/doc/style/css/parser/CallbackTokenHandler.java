/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

abstract class CallbackTokenHandler extends BufferTokenHandler {

	final CSSContentHandler caller;

	CallbackTokenHandler(CSSContentHandler caller) {
		super();
		this.caller = caller;
	}

	protected CSSContentHandler getCaller() {
		return caller;
	}

	@Override
	public void handleErrorRecovery() {
		caller.handleErrorRecovery();
	}

	@Override
	public void reportError(CSSParseException ex) throws CSSParseException {
		caller.reportError(ex);
	}

	@Override
	public void handleWarning(int index, byte errCode, String message, Throwable cause) {
		caller.handleWarning(index, errCode, message, cause);
	}

	@Override
	public CSSErrorHandler getErrorHandler() {
		return caller.getErrorHandler();
	}

}
