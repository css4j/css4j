/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSStyleException;

/**
 * CSS Property exception.
 * 
 * @author Carlos Amengual
 *
 */
public class CSSPropertyException extends CSSStyleException {

	private static final long serialVersionUID = 2L;

	public CSSPropertyException() {
		super();
	}

	public CSSPropertyException(String message) {
		super(message);
	}

	public CSSPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSPropertyException(Throwable cause) {
		super(cause);
	}

}
