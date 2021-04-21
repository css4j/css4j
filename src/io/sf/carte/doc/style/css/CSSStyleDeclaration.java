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

import org.w3c.dom.DOMException;

import io.sf.carte.util.SimpleWriter;

/**
 * CSS style declaration.
 */
public interface CSSStyleDeclaration {

	/**
	 * A parsable serialization of the declaration.
	 *
	 * @return the textual representation of the declaration.
	 */
	String getCssText();

	/**
	 * A minified parsable textual representation of the declaration.
	 *
	 * @return the minified textual representation of the declaration.
	 */
	String getMinifiedCssText();

	/**
	 * Parse the given text, and set the contents of this declaration according to
	 * it.
	 * 
	 * @param cssText the serialized style declaration.
	 */
	void setCssText(String cssText) throws DOMException;

	/**
	 * Remove the given property from this declaration.
	 * 
	 * @param propertyName the name of the property to remove.
	 * @return the value of the removed property, or the empty string if that
	 *         property was not explicitly set in this declaration.
	 */
	String removeProperty(String propertyName) throws DOMException;

	/**
	 * Get the priority that was set to the given property in this declaration.
	 * 
	 * @param propertyName the name of the property.
	 * @return the priority string, or the empty string if no priority was set.
	 */
	String getPropertyPriority(String propertyName);

	/**
	 * Set the value of a property in this declaration, with the given priority.
	 * 
	 * @param propertyName the name of the property.
	 * @param value the property value.
	 * @param priority the priority.
	 */
	void setProperty(String propertyName, String value, String priority) throws DOMException;

	/**
	 * The number of properties in this declaration.
	 * 
	 * @return the number of properties in this declaration.
	 */
	int getLength();

	/**
	 * The name of the property at index {@code index} in this declaration.
	 * 
	 * @param index the index.
	 * @return the name of the property at the given index, or null if the index is
	 *         less than zero, or greater or equal to the length of this
	 *         declaration.
	 */
	String item(int index);

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
	CSSValue getPropertyCSSValue(String propertyName);

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
	String getPropertyValue(String propertyName);

	/**
	 * Retrieves the CSS rule that contains this declaration block.
	 *
	 * @return the CSS rule that contains this declaration block or <code>null</code> if this
	 *         <code>CSSStyleDeclaration</code> is not attached to a <code>CSSRule</code>.
	 */
	CSSDeclarationRule getParentRule();

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
