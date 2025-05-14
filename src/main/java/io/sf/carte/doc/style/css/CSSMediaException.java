/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * CSS media Exception.
 * 
 * @author Carlos Amengual
 * 
 */
public class CSSMediaException extends CSSStyleException {

	private static final long serialVersionUID = 2L;

	public CSSMediaException() {
		super();
	}

	public CSSMediaException(String message) {
		super(message);
	}

	public CSSMediaException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSMediaException(Throwable cause) {
		super(cause);
	}

}
