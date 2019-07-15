/*
 * Copyright ©2011 World Wide Web Consortium, (Massachusetts Institute of Technology, European
 * Research Consortium for Informatics and Mathematics, Keio University, Beihang). All Rights
 * Reserved.
 * 
 * This work is distributed under the W3C® Software License [1] in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 * 
 * [1] http://www.w3.org/Consortium/Legal/copyright-software
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * Listen to changes in media queries.
 * <p>
 * No class is implementing this by now, and this interface may be removed in the future.
 */
public interface MediaQueryListListener {
	void onChange (MediaQueryList list);
}
