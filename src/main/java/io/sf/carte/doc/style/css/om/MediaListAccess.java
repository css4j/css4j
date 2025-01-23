/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.MediaQueryList;

interface MediaListAccess {

	/**
	 * Gives an unmodifiable view of this media query list.
	 * 
	 * @return an unmodifiable view of this media query list.
	 */
	MediaQueryList unmodifiable();

}
