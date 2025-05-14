/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSValueItem;

/**
 * This interface provides information about the processing of one (or more) CSS
 * lexical unit(s) in order to produce a <code>StyleValue</code>, that can be
 * recovered with {@link #getCSSValue()}.
 */
public interface ValueItem extends CSSValueItem {

	/**
	 * Gets the StyleValue associated to this item.
	 * <p>
	 * If there is an item there must be a value, so this method cannot return null.
	 * 
	 * @return the StyleValue associated to this item.
	 */
	@Override
	StyleValue getCSSValue();

}
