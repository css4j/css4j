/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2022-2023, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc;

import org.w3c.dom.DOMException;

/**
 * DOM policy exception.
 * <p>
 * Reports a policy exception, including possible security issues.
 */
public class DOMPolicyException extends DOMException {

	private static final long serialVersionUID = 1L;

	public DOMPolicyException() {
		this(null);
	}

	public DOMPolicyException(String message) {
		super(DOMException.INVALID_ACCESS_ERR, message);
	}

	public DOMPolicyException(String message, Throwable cause) {
		super(DOMException.INVALID_ACCESS_ERR, message);
		initCause(cause);
	}

}
