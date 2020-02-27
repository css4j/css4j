/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.ExtendedCSSValue;

/**
 * A condition that declares a property name and a value.
 *
 */
public interface DeclarationCondition {

	/**
	 * The property name.
	 * 
	 * @return the property name.
	 */
	String getName();

	ExtendedCSSValue getValue();

	boolean isParsable();

	/**
	 * Set the condition feature value.
	 * 
	 * @param value the value.
	 * @throws DOMException if the value is incompatible with the feature being
	 *                      tested with the condition.
	 */
	void setValue(ExtendedCSSValue value) throws DOMException;

	/**
	 * Set a serialized value for the property, setting the <code>parsable</code>
	 * flag to <code>false</code>.
	 * <p>
	 * This should be done only when a proper value could not be parsed.
	 * <p>
	 * A condition which has a serialized value but not a real value is never going
	 * to match, although the serialized value shall be used for serializations.
	 * 
	 * @param cssText the serialized value.
	 */
	void setValue(String cssText);

}
