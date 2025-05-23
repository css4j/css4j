/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

class TestErrorHandler implements CSSErrorHandler {

	private CSSParseException exception = null;
	CSSParseException warningException = null;

	CSSParseException getLastException() {
		return exception;
	}

	CSSParseException getLastWarning() {
		return warningException;
	}

	boolean hasError() {
		return exception != null;
	}

	boolean hasWarning() {
		return warningException != null;
	}

	void reset() {
		exception = null;
		warningException = null;
	}

	@Override
	public void warning(CSSParseException exception) {
		this.warningException = exception;
	}

	@Override
	public void error(CSSParseException exception) {
		this.exception = exception;
	}

}
