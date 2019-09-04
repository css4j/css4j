/*
 * This software extends interfaces defined by CSSOM View Module (https://www.w3.org/TR/cssom-view-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2017-2018, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.util.List;

import org.w3c.css.sac.CSSParseException;
import org.w3c.dom.stylesheets.MediaList;

import io.sf.carte.doc.agent.CSSCanvas;

/**
 * Based on W3C's MediaQueryList interface.
 */
public interface MediaQueryList extends MediaList {

	/**
	 * Get the serialized form of the associated media query list.
	 * 
	 * @return the serialized form of the associated media query list.
	 */
	String getMedia();

	/**
	 * Get a minified serialized form of the associated media query list.
	 * 
	 * @return the minified serialized form of the associated media query list.
	 */
	String getMinifiedMedia();

	/**
	 * Is this an all-media list?
	 * 
	 * @return <code>true</code> if this list matches all media, <code>false</code> otherwise.
	 */
	boolean isAllMedia();

	/**
	 * Determine if this list is composed only of queries that evaluate to <code>not all</code>.
	 * 
	 * @return <code>true</code> if this list matches no media, <code>false</code> otherwise.
	 */
	boolean isNotAllMedia();

	/**
	 * Does the associated media query list match the state of the rendered Document?
	 * 
	 * @param medium
	 *            the lowercase name of the medium to test for.
	 * @param canvas
	 *            the canvas where the document is to be rendered.
	 * @return <code>true</code> if the associated media query list matches the state of the rendered
	 *         Document and <code>false</code> if it does not.
	 */
	boolean matches(String medium, CSSCanvas canvas);

	/**
	 * Did this media query list produce errors when being parsed ?
	 * 
	 * @return <code>true</code> if this list come from a media string that produced errors when
	 *         parsed, <code>false</code> otherwise.
	 */
	boolean hasErrors();

	/**
	 * Get the exceptions found while parsing the query, if any.
	 * 
	 * @return the exceptions found while parsing the query, or <code>null</code> if
	 *         no errors were found while parsing the media query.
	 */
	List<CSSParseException> getExceptions();

	/**
	 * Appends a listener to the list of media query list listeners, unless it is already in
	 * that list.
	 * 
	 * @param listener
	 *            the listener to be appended.
	 */
	void addListener(MediaQueryListListener listener);

	/**
	 * Removes a listener from the list of media query list listeners.
	 * 
	 * @param listener
	 *            the listener to be removed.
	 */
	void removeListener(MediaQueryListListener listener);

}
