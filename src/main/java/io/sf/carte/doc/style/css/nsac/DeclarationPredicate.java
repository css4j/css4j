/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.nsac;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.BooleanCondition;

/**
 * A condition that declares a property name and a value.
 */
public interface DeclarationPredicate extends BooleanCondition {

	/**
	 * The property name.
	 * 
	 * @return the property name.
	 */
	String getName();

	/**
	 * Set the {@code @supports} condition property value.
	 * 
	 * @param value the lexical value.
	 * @throws DOMException if the value is incompatible with the value being tested
	 *                      with the condition.
	 */
	void setValue(LexicalUnit value) throws DOMException;

}
