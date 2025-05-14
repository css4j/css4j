/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * CSS static methods.
 */
public class CSS {

	/**
	 * Escape the given string according to CSS syntax.
	 * <p>
	 * For convenience, if the string contains an escape sequence escaping a private
	 * or unassigned character, the corresponding backslash is not escaped.
	 * </p>
	 * 
	 * @param text the text to escape.
	 * @return the escaped text.
	 */
	public static String escape(String text) {
		return ParseHelper.safeEscape(text);
	}

}
