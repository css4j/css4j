/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.parser.BooleanCondition;

/**
 * Obtain information on an individual media query.
 */
public interface MediaQuery {

	/**
	 * Get the media type.
	 * 
	 * @return the media type, null means all media.
	 */
	String getMediaType();

	/**
	 * The media feature conditions, if any.
	 * 
	 * @return the media feature conditions, null otherwise.
	 */
	BooleanCondition getCondition();

	/**
	 * Return <code>true</code> if this query is negated, like in
	 * {@code not screen}.
	 * 
	 * @return <code>true</code> if this query is negated.
	 */
	boolean isNegated();

	/**
	 * Is this an all-media query?
	 * 
	 * @return <code>true</code> if this query matches all media, <code>false</code>
	 *         otherwise.
	 */
	boolean isAllMedia();

	/**
	 * Determine if this query evaluates to <code>not all</code>.
	 * 
	 * @return <code>true</code> if this query matches no media, <code>false</code>
	 *         otherwise.
	 */
	boolean isNotAllMedia();

	/**
	 * Get the serialized form of this media query.
	 * 
	 * @return the serialized form of this media query.
	 */
	String getMedia();

	/**
	 * Get a minified serialized form of this media query.
	 * 
	 * @return the minified serialized form of this media query.
	 */
	String getMinifiedMedia();

}
