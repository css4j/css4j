/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.css.sac.CSSParseException;

public interface MediaQueryHandler {

	void startQuery();

	void mediaType(String mediaType);

	void negativeQuery();

	void onlyPrefix();

	void condition(BooleanCondition condition);

	void endQuery();

	/**
	 * Reports that the current media query is invalid.
	 * 
	 * @param queryError the exception describing the error.
	 */
	void invalidQuery(CSSParseException queryError);

	/**
	 * Reports that the current media query is invalid but probably compatible with
	 * a legacy browser.
	 * 
	 * @param exception the exception describing the location where the issue was
	 *                  found.
	 */
	void compatQuery(CSSParseException exception);

}
