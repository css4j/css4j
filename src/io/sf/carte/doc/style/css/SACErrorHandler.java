/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * High-level handling of SAC errors and warnings.
 */
public interface SACErrorHandler {

	/**
	 * Map a SAC error or fatal error to a specific rule.
	 * 
	 * @param exception
	 *            the parse exception.
	 * @param rule
	 *            the rule having the error.
	 */
	void mapError(CSSParseException exception, CSSRule rule);

	/**
	 * Handle a SAC warning.
	 * 
	 * @param exception
	 *            the parse exception.
	 */
	void handleSacWarning(CSSParseException exception);

	/**
	 * Handle a SAC error or fatal error.
	 * 
	 * @param exception
	 *            the parse exception.
	 */
	void handleSacError(CSSParseException exception);

	/**
	 * Check whether this handler has been notified of SAC errors (or fatal errors).
	 * 
	 * @return <code>true</code> if SAC errors or fatal errors were notified since last {@link SheetErrorHandler#reset()},
	 *         <code>false</code> otherwise.
	 */
	boolean hasSacErrors();

	/**
	 * Check whether this handler has been notified of SAC warnings.
	 * 
	 * @return <code>true</code> if SAC warnings were notified since last {@link SheetErrorHandler#reset()},
	 *         <code>false</code> otherwise.
	 */
	boolean hasSacWarnings();

}
