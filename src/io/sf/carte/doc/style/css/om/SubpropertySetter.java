/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

interface SubpropertySetter {

	void init(LexicalUnit shorthandValue, boolean important);

	/**
	 * Attempt to assign the shorthand to its longhand subproperties.
	 * 
	 * @return <code>true</code> if the shorthand was successfully parsed into longhands,
	 *         <code>false</code> otherwise.
	 */
	boolean assignSubproperties();

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
