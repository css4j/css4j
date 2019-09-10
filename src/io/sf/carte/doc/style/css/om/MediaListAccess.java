/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.SACMediaList;

import io.sf.carte.doc.style.css.MediaQueryList;

interface MediaListAccess {

	/**
	 * Gives an unmodifiable view of this media query list.
	 * 
	 * @return an unmodifiable view of this media query list.
	 */
	MediaQueryList unmodifiable();

	/**
	 * Does the given SAC media list contain any media present in this list?
	 * 
	 * @param sacMedia
	 *            the SAC media list to test.
	 * @return <code>true</code> if the SAC media contains any media which applies to this
	 *         list, <code>false</code> otherwise.
	 */
	boolean match(SACMediaList sacMedia);

}
