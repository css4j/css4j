/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.impl;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.parser.AbstractMediaQuery;

public interface MediaListAccess {

	/**
	 * Gives an unmodifiable view of this media query list if the list does not
	 * contain proxies, otherwise clones it.
	 * 
	 * @return an unmodifiable view of this media query list, or a clone if has
	 *         proxy values.
	 */
	MediaQueryList unmodifiable();

	boolean hasProxy();

	void setMediaQuery(int index, AbstractMediaQuery query);

}
