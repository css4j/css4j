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
 * CSS runtime exception, normally related to parsing.
 * <p>
 * As it is expected to be used in the context of an event parser, it is a
 * {@code RuntimeException} subclass.
 * </p>
 * <p>
 * Based on SAC's {@code CSSException} class by Philippe Le Hegaret.
 * </p>
 */
public class CSSException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a CSS exception.
	 */
	public CSSException() {
		super();
	}

	/**
	 * Construct a CSS exception that has a descriptive message.
	 * 
	 * @param message the descriptive message.
	 */
	public CSSException(String message) {
		super(message);
	}

	/**
	 * Construct a CSS exception that has a cause.
	 * 
	 * @param cause the cause. If {@code null}, the cause is unknown.
	 */
	public CSSException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a CSS exception that has a cause and a descriptive message.
	 * 
	 * @param message the descriptive message.
	 * @param cause   the cause. If {@code null}, the cause is unknown.
	 */
	public CSSException(String message, Throwable cause) {
		super(message, cause);
	}

}
