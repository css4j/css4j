/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

interface LexicalProvider extends CSSContentHandler {

	/**
	 * The lexical unit currently processed by the handler/provider.
	 * 
	 * @return the current lexical unit, or {@code null} if none.
	 */
	LexicalUnitImpl getCurrentLexicalUnit();

	/**
	 * Set the current lexical unit.
	 * 
	 * @param currentlu the current lexical unit.
	 */
	void setCurrentLexicalUnit(LexicalUnitImpl currentlu);

	boolean isCurrentUnitAFunction();

	LexicalUnitImpl addPlainLexicalUnit(LexicalUnitImpl lu);

	/**
	 * Add an {@code EMPTY} lexical unit at the end of the current lexical chain.
	 */
	void addEmptyLexicalUnit();

	void endFunctionArgument(int index);

	/**
	 * Legacy IE value compatibility is enabled.
	 * 
	 * @return {@code true} if IE value compatibility is enabled.
	 */
	boolean hasLegacySupport();

}
