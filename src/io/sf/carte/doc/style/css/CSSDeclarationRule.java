/*
 * This software extends interfaces defined by CSS Object Model
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2018 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * A CSS rule that contains style declarations.
 *
 */
public interface CSSDeclarationRule extends ExtendedCSSRule {

	/**
	 * Get the style that is declared by this rule.
	 *
	 * @return the style declaration.
	 */
	ExtendedCSSStyleDeclaration getStyle();

	/**
	 * Gets the error handler.
	 *
	 * @return the error handler.
	 */
	StyleDeclarationErrorHandler getStyleDeclarationErrorHandler();

}
