/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * Static factory that creates media query lists for all media, mutable and
 * immutable.
 * <p>
 * Using these methods is a bit faster than going through the style sheet
 * factory and parsing a query for "all".
 * </p>
 */
public class MediaFactory {

	/**
	 * Create a media query list for all media.
	 * 
	 * @return a media query list for all media.
	 */
	public static MediaQueryList createAllMedia() {
		return new MediaQueryListImpl();
	}

	/**
	 * Create an unmodifiable media query list for all media.
	 * 
	 * @return a media query list for all media, unmodifiable.
	 */
	public static MediaQueryList createImmutable() {
		return MediaQueryListImpl.createUnmodifiable();
	}

}
