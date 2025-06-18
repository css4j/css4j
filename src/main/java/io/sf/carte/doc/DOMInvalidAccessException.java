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
 * DOM invalid access exception.
 * <p>
 * Reports that a parameter or an operation is not supported by the underlying
 * object.
 */
public class DOMInvalidAccessException extends DOMException {

	private static final long serialVersionUID = 1L;

	public DOMInvalidAccessException() {
		this(null);
	}

	public DOMInvalidAccessException(String message) {
		super(DOMException.INVALID_ACCESS_ERR, message);
	}

	public DOMInvalidAccessException(String message, Throwable cause) {
		super(DOMException.INVALID_ACCESS_ERR, message);
		initCause(cause);
	}

}
