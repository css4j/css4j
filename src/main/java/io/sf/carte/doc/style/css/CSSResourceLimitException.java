/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

/**
 * A resource limit was found.
 */
public class CSSResourceLimitException extends DOMException {

	private static final long serialVersionUID = 2L;

	public CSSResourceLimitException(String message) {
		super(DOMException.INVALID_ACCESS_ERR, message);
	}

	public CSSResourceLimitException(String message, Throwable cause) {
		super(DOMException.INVALID_ACCESS_ERR, message);
		initCause(cause);
	}

}
