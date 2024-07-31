/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.nsac;

import io.sf.carte.doc.style.css.BooleanCondition;

/**
 * The {@code selector()} function.
 * 
 * See <a href="https://www.w3.org/TR/css-conditional-4/">Conditional Rules
 * Module Level 4</a>.
 */
public interface SelectorFunction extends BooleanCondition {

	/**
	 * The selectors.
	 * 
	 * @return the list of selectors.
	 */
	SelectorList getSelectors();

	@Override
	default Type getType() {
		return Type.SELECTOR_FUNCTION;
	}

}
