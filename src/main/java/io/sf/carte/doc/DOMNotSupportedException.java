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
 * DOM not-supported exception.
 * <p>
 * Reports that the implementation does not support the requested type of object
 * or operation.
 */
public class DOMNotSupportedException extends DOMException {

	private static final long serialVersionUID = 1L;

	public DOMNotSupportedException() {
		this(null);
	}

	public DOMNotSupportedException(String message) {
		super(DOMException.NOT_SUPPORTED_ERR, message);
	}

	public DOMNotSupportedException(String message, Throwable cause) {
		super(DOMException.NOT_SUPPORTED_ERR, message);
		initCause(cause);
	}

}
