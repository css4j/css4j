/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

/**
 * This interface provides information about the processing of one (or more) CSS lexical
 * unit(s) in order to produce a <code>CSSValue</code>, that can be recovered with {@link #getCSSValue()}.
 */
public interface ValueItem {

	/**
	 * Gets the CSSValue associated to this item.
	 * <p>
	 * If there is an item there must be a value, so this method cannot return null.
	 * 
	 * @return the CSSValue associated to this item.
	 */
	StyleValue getCSSValue();

	/**
	 * Get the next lexical unit after processing this item.
	 * <p>
	 * This method is useful as some primitive values (ratio) may take more than one
	 * unit.
	 * 
	 * @return the next lexical unit after this item was processed.
	 */
	LexicalUnit getNextLexicalUnit();

	/**
	 * Has this item any warning to report ?
	 * 
	 * @return <code>true</code> if this item any warning to report.
	 */
	boolean hasWarnings();

	/**
	 * Use the given error handler to handle warnings.
	 * 
	 * @param handler the error handler.
	 */
	void handleSyntaxWarnings(StyleDeclarationErrorHandler handler);
}
