/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSNestedDeclarations;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

/**
 * CSSNestedDeclarations
 * <p>
 * https://drafts.csswg.org/css-nesting/#the-cssnestrule
 * </p>
 */
class NestedDeclarations extends BaseCSSDeclarationRule implements CSSNestedDeclarations {

	private static final long serialVersionUID = 1L;

	protected NestedDeclarations(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.NESTED_DECLARATIONS, origin);
	}

	@Override
	public StyleRule getParentRule() {
		return (StyleRule) super.getParentRule();
	}

	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		return getParentRule().getStyleDeclarationErrorHandler();
	}

}
