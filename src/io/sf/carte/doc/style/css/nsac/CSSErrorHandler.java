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
 * A CSS event handler for low-level parsing errors.
 * <p>
 * Based on SAC's {@code ErrorHandler} interface by Philippe Le Hegaret.
 * </p>
 */
public interface CSSErrorHandler {

	/**
	 * Receive notification of a warning.
	 *
	 * @param exception The warning information encapsulated in a CSS parse
	 *                  exception.
	 * @throws CSSParseException if this handler decides that the parse process
	 *                           cannot continue.
	 */
	void warning(CSSParseException exception) throws CSSParseException;

	/**
	 * Receive notification of an error.
	 *
	 * <p>
	 * This corresponds to the definition of "error" in section 1.2 of the W3C XML
	 * 1.0 Recommendation. For example, a validating parser would use this callback
	 * to report the violation of a validity constraint. The default behaviour is to
	 * take no action.
	 * </p>
	 *
	 * @param exception The error information encapsulated in a CSS parse exception.
	 * @throws CSSParseException if this handler decides that the parse process
	 *                           cannot continue.
	 */
	void error(CSSParseException exception) throws CSSParseException;

}
