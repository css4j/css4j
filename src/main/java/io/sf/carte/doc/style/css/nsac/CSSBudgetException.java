/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 * 
 * Copyright © 2017-2019 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * CSS budget exception.
 * <p>
 * While parsing or processing the style, a processing limit was found.
 * </p>
 */
public class CSSBudgetException extends CSSException {

	private static final long serialVersionUID = 1L;

	public CSSBudgetException() {
		super();
	}

	public CSSBudgetException(Throwable cause) {
		super(cause);
	}

	public CSSBudgetException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSBudgetException(String message) {
		super(message);
	}

}
