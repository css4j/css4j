/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * CSS value conversion exception.
 * 
 * @author Carlos Amengual
 *
 */
public class CSSValueConversionException extends CSSPropertyException {

	private static final long serialVersionUID = 2L;

	public CSSValueConversionException() {
		super();
	}

	public CSSValueConversionException(String message) {
		super(message);
	}

	public CSSValueConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSValueConversionException(Throwable cause) {
		super(cause);
	}

}
