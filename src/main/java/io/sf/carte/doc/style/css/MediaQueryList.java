/*
 * This software extends interfaces defined by CSSOM View Module (https://www.w3.org/TR/cssom-view-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2017-2025, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.stylesheets.MediaList;

import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * Based on W3C's MediaQueryList interface.
 */
public interface MediaQueryList extends MediaList, EventTarget {

	/**
	 * Get the media query at {@code index}.
	 * 
	 * @param index the index.
	 * @return the media query at the {@code index}-th position in this list, or
	 *         {@code null} if that is not a valid index or the query list is
	 *         invalid.
	 */
	MediaQuery getMediaQuery(int index);

	/**
	 * Get the serialized form of this media query list.
	 * 
	 * @return the serialized form of this media query list.
	 */
	String getMedia();

	/**
	 * Get a minified serialized form of this media query list.
	 * 
	 * @return the minified serialized form of this media query list.
	 */
	String getMinifiedMedia();

	/**
	 * Is this an all-media list?
	 * 
	 * @return <code>true</code> if this list matches all media, <code>false</code>
	 *         otherwise.
	 */
	boolean isAllMedia();

	/**
	 * Determine if this list is composed only of queries that evaluate to
	 * <code>not all</code>.
	 * 
	 * @return <code>true</code> if this list matches no media, <code>false</code>
	 *         otherwise.
	 */
	boolean isNotAllMedia();

	/**
	 * Does the associated media query list match the state of the rendered Document?
	 * 
	 * @param medium
	 *            the lowercase name of the medium to test for, <code>null</code> or
	 *            <code>all</code> if all media.
	 * @param canvas
	 *            the canvas where the document is to be rendered.
	 * @return <code>true</code> if the associated media query list matches the state
	 *         of the rendered Document and <code>false</code> if it does not.
	 */
	boolean matches(String medium, CSSCanvas canvas);

	/**
	 * Does the given media list contain any media present in this list?
	 * <p>
	 * If query A matches B, then if a medium matches B it will also match A. The
	 * opposite may not be true.
	 * 
	 * @param otherMedia the other media list to test.
	 * @return <code>true</code> if the other media contains any media which applies
	 *         to this list, <code>false</code> otherwise.
	 */
	boolean matches(MediaQueryList otherMedia);

	/**
	 * Did this media query list produce errors when being parsed ?
	 * 
	 * @return <code>true</code> if this list come from a media string that produced
	 *         errors when parsed, <code>false</code> otherwise.
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
	 * Appends a listener to the list of media query list listeners, unless it is
	 * already in that list.
	 * 
	 * @param listener the listener to be appended.
	 * @deprecated use {@link #addEventListener(String, EventListener, boolean)}
	 *             instead.
	 */
	@Deprecated
	default void addListener(MediaQueryListListener listener) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "You should use CSSCanvas for this");
	}

	/**
	 * Removes a listener from the list of media query list listeners.
	 * 
	 * @param listener the listener to be removed.
	 * @deprecated use {@link #removeEventListener(String, EventListener, boolean)}
	 *             instead.
	 */
	@Deprecated
	default void removeListener(MediaQueryListListener listener) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "You should use CSSCanvas for this");
	}

	@Override
	default void addEventListener(String type, EventListener listener, boolean useCapture) {
	}

	@Override
	default void removeEventListener(String type, EventListener listener, boolean useCapture) {
	}

	@Override
	default boolean dispatchEvent(Event evt) throws EventException {
		return false;
	}

}
