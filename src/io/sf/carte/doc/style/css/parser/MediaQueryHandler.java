/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.css.sac.CSSParseException;

/**
 * Interface to be implemented by handlers that are passed to
 * {@link CSSParser#parseMediaQuery(String, MediaConditionFactory, MediaQueryHandler)}.
 */
public interface MediaQueryHandler {

	/**
	 * A new media query starts being processed.
	 */
	void startQuery();

	/**
	 * A media type was found (e.g. <code>screen</code>) in the current query.
	 * 
	 * @param mediaType the media type.
	 */
	void mediaType(String mediaType);

	/**
	 * Reports that the current query is a negative query.
	 */
	void negativeQuery();

	/**
	 * The current query uses the <code>only</code> prefix.
	 */
	void onlyPrefix();

	/**
	 * If the current query contains a media (feature) condition, set it.
	 * 
	 * @param condition the media condition, which contains one or more media
	 *                  feature predicates and an initial media type predicate.
	 */
	void condition(BooleanCondition condition);

	/**
	 * The current query ends.
	 */
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
