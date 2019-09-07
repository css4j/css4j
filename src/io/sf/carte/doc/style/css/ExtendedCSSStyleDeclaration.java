/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import org.w3c.dom.css.CSSStyleDeclaration;

import io.sf.carte.util.SimpleWriter;

/**
 * Extended CSS style declaration.
 */
public interface ExtendedCSSStyleDeclaration extends CSSStyleDeclaration {

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
	@Override ExtendedCSSValue getPropertyCSSValue(String propertyName);

	/**
	 * Gets the text value of a CSS property if it has been explicitly set within this
	 * declaration block.
	 * <p>
	 * If the declaration was created by a factory with the <code>IEVALUES</code> flag
	 * enabled, the compatibility values shall appear in the cssText serializations, but its
	 * value won't be returned by this method unless no other valid value was previously
	 * specified for the property.
	 *
	 * @param propertyName
	 *            The name of the CSS property.
	 * @return the value of the property if it has been explicitly set for this declaration
	 *         block, or the empty string if the property has not been set or is a shorthand
	 *         that could not be serialized.
	 */
	@Override String getPropertyValue(String propertyName);

	/**
	 * Retrieves the CSS rule that contains this declaration block.
	 *
	 * @return the CSS rule that contains this declaration block or <code>null</code> if this
	 *         <code>CSSStyleDeclaration</code> is not attached to a <code>CSSRule</code>.
	 */
	@Override CSSDeclarationRule getParentRule();

	/**
	 * A minified parsable textual representation of the declaration. This reflects the
	 * current state of the declaration and not its initial value.
	 *
	 * @return the minified textual representation of the declaration.
	 */
	String getMinifiedCssText();

	/**
	 * Writes a textual representation of the declaration block (excluding the surrounding
	 * curly braces) to a <code>SimpleWriter</code>, and according to a
	 * <code>StyleFormattingContext</code>.
	 *
	 * @param wri
	 *            the simple writer to write to.
	 * @param context
	 *            the style formatting context.
	 * @throws IOException
	 *             if a problem occurs while writing the text.
	 */
	void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException;

}
