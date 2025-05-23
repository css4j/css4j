/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

/**
 * Create formatting context objects for use with a style sheet factory.
 *
 */
public interface StyleFormattingFactory {

	/**
	 * Create a new StyleFormattingContext.
	 * 
	 * @return a StyleFormattingContext.
	 */
	StyleFormattingContext createStyleFormattingContext();

	/**
	 * Create a new DeclarationFormattingContext to serialize computed styles.
	 * 
	 * @return a DeclarationFormattingContext.
	 */
	DeclarationFormattingContext createComputedStyleFormattingContext();

}
