/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * A value that contains a {@code var} value as part of its declaration.
 */
public interface CSSLexicalValue extends CSSPrimitiveValue {

	/**
	 * Get the first lexical unit of this lexical-level value.
	 * 
	 * @return the first lexical unit.
	 */
	LexicalUnit getLexicalUnit();

	/**
	 * Get the expected final type after {@code var} substitution.
	 * 
	 * @return the expected final primitive type, {@code UNKNOWN} if the final value
	 *         is either a list or could not be determined.
	 */
	Type getFinalType();

}
