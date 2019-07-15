/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

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
}
