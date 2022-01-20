/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * This interface listens to properties found by SAC event handlers.
 *
 * @author Carlos Amengual
 *
 */
interface LexicalPropertyListener {

	/**
	 * Set a CSS property, based on lexixal value.
	 *
	 * @param propertyName
	 *            the name of the property.
	 * @param value
	 *            the lexical value.
	 * @param priority
	 *            the priority string.
	 * @throws DOMException
	 *             if some error or inconsistency is found in the value.
	 */
	void setProperty(String propertyName, LexicalUnit value, String priority) throws DOMException;

}
