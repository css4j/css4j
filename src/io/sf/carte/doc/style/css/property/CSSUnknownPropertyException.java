/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * Unknown property exception.
 * 
 * @author Carlos Amengual
 *
 */
public class CSSUnknownPropertyException extends CSSPropertyException {

	private static final long serialVersionUID = 2L;

	public CSSUnknownPropertyException() {
		super();
	}

	public CSSUnknownPropertyException(String message) {
		super(message);
	}

	public CSSUnknownPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSUnknownPropertyException(Throwable cause) {
		super(cause);
	}

}
