/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * Interface to be implemented by handlers that are used inside
 * {@link io.sf.carte.doc.style.css.nsac.Parser#parseMediaQueryList(String,org.w3c.dom.Node)
 * Parser.parseMediaQueryList(String,Node)}.
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
	 * Check whether this handler takes the responsibility of reporting errors to an
	 * error handler.
	 * 
	 * @return true if this handler reports errors to an error handler.
	 */
	boolean reportsErrors();

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

	/**
	 * The query list ends.
	 */
	void endQueryList();

	/**
	 * Get the media query list that this object handles.
	 * 
	 * @return the media query list.
	 */
	MediaQueryList getMediaQueryList();

}
