/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

class TestErrorHandler implements ErrorHandler {

	CSSParseException exception = null;
	CSSParseException warningException = null;

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
	public void warning(CSSParseException exception) throws CSSException {
		this.warningException = exception;
	}

	@Override
	public void error(CSSParseException exception) throws CSSException {
		this.exception = exception;
	}

	@Override
	public void fatalError(CSSParseException exception) throws CSSException {
		this.exception = exception;
	}

}