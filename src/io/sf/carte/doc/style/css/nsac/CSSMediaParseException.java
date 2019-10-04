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
 * Media-related parse exception.
 */
public class CSSMediaParseException extends CSSParseException {

	private static final long serialVersionUID = 2L;

	/**
	 * Construct a CSS media parse exception that has a cause, a location and a
	 * descriptive message.
	 * 
	 * @param message the descriptive message.
	 * @param locator the location where the error was triggered.
	 * @param cause   the cause.
	 */
	public CSSMediaParseException(String message, Locator locator, Throwable cause) {
		super(message, locator, cause);
	}

	/**
	 * Construct a CSS media parse exception that has a location and a
	 * descriptive message.
	 * 
	 * @param message the descriptive message.
	 * @param locator the location where the error was triggered.
	 */
	public CSSMediaParseException(String message, Locator locator) {
		super(message, locator);
	}

}
