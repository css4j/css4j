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

/**
 * DOM policy exception.
 * <p>
 * Reports a policy exception, including possible security issues.
 */
public class DOMPolicyException extends DOMInvalidAccessException {

	private static final long serialVersionUID = 2L;

	public DOMPolicyException() {
		this(null);
	}

	public DOMPolicyException(String message) {
		super(message);
	}

	public DOMPolicyException(String message, Throwable cause) {
		super(message, cause);
	}

}
