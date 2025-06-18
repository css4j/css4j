/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2022-2025, Carlos Amengual.
 */
/*
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc;

import org.w3c.dom.DOMException;

/**
 * DOM syntax exception.
 * <p>
 * Reports that something does not conform to expected syntax.
 * </p>
 */
public class DOMSyntaxException extends DOMException {

	private static final long serialVersionUID = 1L;

	public DOMSyntaxException() {
		super(DOMException.SYNTAX_ERR, null);
	}

	public DOMSyntaxException(String message) {
		super(DOMException.SYNTAX_ERR, message);
	}

	public DOMSyntaxException(String message, Throwable cause) {
		super(DOMException.SYNTAX_ERR, message);
		initCause(cause);
	}

	public DOMSyntaxException(Throwable cause) {
		super(DOMException.SYNTAX_ERR, null);
		initCause(cause);
	}

}
