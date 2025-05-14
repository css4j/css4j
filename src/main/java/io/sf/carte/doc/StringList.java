/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc;

import org.w3c.dom.DOMStringList;

/**
 * Combines {@link java.util.List} and {@link DOMStringList}.
 */
public interface StringList extends java.util.List<String>, DOMStringList, Cloneable {

	/**
	 * Returns a shallow copy of this {@code StringList} instance. (The elements
	 * themselves are not copied.)
	 *
	 * @return a clone of this {@code StringList}.
	 */
	StringList clone();

}
