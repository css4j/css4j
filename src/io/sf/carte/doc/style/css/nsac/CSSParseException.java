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
 * CSS parse exception.
 * <p>
 * This kind of exception is expected to have a locator providing the location
 * (in the CSS document) where the issue was triggered.
 * </p>
 * <p>
 * Based on SAC's {@code CSSParseException} class by Philippe Le Hegaret.
 * </p>
 */
public class CSSParseException extends CSSException {

	private static final long serialVersionUID = 1L;

	private final Locator locator;

	/**
	 * Construct a CSS parse exception that has a cause, a location and a
	 * descriptive message.
	 * 
	 * @param message the descriptive message.
	 * @param locator the location where the error was triggered.
	 * @param cause   the cause. If {@code null}, the cause is unknown.
	 */
	public CSSParseException(String message, Locator locator, Throwable cause) {
		super(message, cause);
		this.locator = locator;
	}

	/**
	 * Construct a CSS parse exception that has a location and a descriptive
	 * message.
	 * 
	 * @param message the descriptive message.
	 * @param locator the location where the error was triggered.
	 */
	public CSSParseException(String message, Locator locator) {
		this(message, locator, null);
	}

	/**
	 * Get the locator pointing to the place where the error was triggered.
	 * 
	 * @return the locator for the error, or null if there is no available locator.
	 */
	public Locator getLocator() {
		return locator;
	}

	/**
	 * Get the column number where the error was triggered, starting at
	 * <code>1</code>.
	 * 
	 * @return the column number where the error was triggered, or <code>-1</code>
	 *         if that information is unavailable.
	 */
	public int getColumnNumber() {
		return locator != null ? locator.getColumnNumber() : -1;
	}

	/**
	 * Get the line number where the error was triggered, starting at
	 * <code>1</code>.
	 * 
	 * @return the line number where the error was triggered, or <code>-1</code> if
	 *         that information is unavailable.
	 */
	public int getLineNumber() {
		return locator != null ? locator.getLineNumber() : -1;
	}

}
