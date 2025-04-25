/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2025 Carlos Amengual.
 */

/*
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * CSS style declaration.
 */
interface ExtendedCSSStyleDeclaration extends CSSStyleDeclaration, LexicalPropertyListener {

	/**
	 * Gets the object representation of the value of a CSS property if it has been explicitly
	 * set for this declaration block.
	 * <p>
	 * If the declaration was created by a factory with the <code>IEVALUES</code> flag
	 * enabled, the compatibility values shall appear in the cssText serializations, but its
	 * value won't be returned by this method unless no other valid value was previously
	 * specified for the property.
	 *
	 * @param propertyName
	 *            The name of the CSS property.
	 * @return the value of the property if it has been explicitly set for this declaration
	 *         block. Returns <code>null</code> if the property has not been set or is a
	 *         shorthand.
	 */
	@Override
	StyleValue getPropertyCSSValue(String propertyName);

}
