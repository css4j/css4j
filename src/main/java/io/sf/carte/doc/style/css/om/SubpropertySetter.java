/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

interface SubpropertySetter {

	/**
	 * Initialize the setter.
	 * 
	 * @param shorthandValue the shorthand value.
	 * @param important      the priority.
	 */
	void init(LexicalUnit shorthandValue, boolean important);

	/**
	 * @param attrTainted {@code true} if the shorthand is attr()-tainted.
	 */
	default void setAttrTainted(boolean attrTainted) {
	}

	/**
	 * Attempt to assign the shorthand to its longhand subproperties.
	 * 
	 * @return <code>0</code> if the shorthand was successfully parsed into
	 *         longhands and should be processed normally, <code>1</code> if the
	 *         shorthand had a special handling and should not be further processed,
	 *         <code>2</code> if an error was found and an exception should be
	 *         thrown.
	 */
	short assignSubproperties();

	/**
	 * Get a string representation of the shorthand.
	 * 
	 * @return a string representation of the shorthand. If empty, that means the shorthand
	 *         had errors or was browser-unsafe. If {@link #assignSubproperties()} returned
	 *         true but the empty string is still returned, it means that the longhands could
	 *         be set but the shorthand construct is considered to be browser-unsafe.
	 */
	String getCssText();

	ShorthandValue createCSSShorthandValue(LexicalUnit value);

	/**
	 * Get a minified string representation of the shorthand.
	 * 
	 * @return a string representation of the shorthand. If empty, that means the shorthand
	 *         had errors or was browser-unsafe. If {@link #assignSubproperties()} returned
	 *         true but the empty string is still returned, it means that the longhands could
	 *         be set but the shorthand construct is considered to be browser-unsafe.
	 */
	String getMinifiedCssText();

}
