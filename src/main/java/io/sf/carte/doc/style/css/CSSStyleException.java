/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * CSS topmost generic Exception.
 * 
 */
public class CSSStyleException extends Exception {

	private static final long serialVersionUID = 2L;

	public CSSStyleException() {
		super();
	}

	public CSSStyleException(String message) {
		super(message);
	}

	public CSSStyleException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSStyleException(Throwable cause) {
		super(cause);
	}

}
