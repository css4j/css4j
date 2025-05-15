/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class ShorthandDecomposer {

	/**
	 * Assign the longhand properties.
	 * 
	 * @param style        the style declaration where the shorthand belongs.
	 * @param propertyName the property name.
	 * @param value        the value.
	 * @param important    {@code true} if the priority is important.
	 * @param attrTainted  {@code true} if the value is attr-tainted.
	 * @return the setter that successfully assigned the longhands, or {@code null}
	 *         if the shorthand had a special handling and should not be further
	 *         processed.
	 */
	public SubpropertySetter assignLonghands(BaseCSSStyleDeclaration style, String propertyName,
			LexicalUnit value, boolean important, boolean attrTainted) {
		SubpropertySetter setter = createSetter(style, propertyName, value, important);
		return assignLonghands(setter, value, important, attrTainted);
	}

	/**
	 * Create a setter appropriate for the given shorthand property, value and
	 * priority.
	 * 
	 * @param style        the style declaration where the shorthand belongs.
	 * @param propertyName the property name.
	 * @param value        the value. Some setter may require it for special
	 *                     processing.
	 * @param important    {@code true} if the priority is important. Some setter
	 *                     may require it for special processing.
	 * @return the setter.
	 */
	SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
			LexicalUnit value, boolean important) {
		return new ShorthandSetter(style, propertyName);
	}

	/**
	 * Assign the longhand properties with the given setter.
	 * 
	 * @param setter      the shorthand setter.
	 * @param value       the value.
	 * @param important   {@code true} if the priority is important.
	 * @param attrTainted {@code true} if the value is attr-tainted.
	 * @return the setter that successfully assigned the longhands, or {@code null}
	 *         if the shorthand had a special handling and should not be further
	 *         processed.
	 */
	SubpropertySetter assignLonghands(SubpropertySetter setter, LexicalUnit value,
			boolean important, boolean attrTainted) {
		setter.init(value, important);
		setter.setAttrTainted(attrTainted);
		short result = setter.assignSubproperties();
		if (result == 2) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"Invalid property declaration: " + value.toString());
		} else if (result == 0) {
			return setter;
		}
		return null;
	}

}
