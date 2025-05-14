/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.nsac;

/**
 * A condition that declares a property name with an object model value.
 */
public interface DeclarationCondition extends DeclarationPredicate {

	/**
	 * Get the value.
	 * 
	 * @return the value.
	 */
	LexicalUnit getValue();

}
