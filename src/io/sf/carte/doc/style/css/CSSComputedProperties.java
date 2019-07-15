/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSS2Properties;

/**
 * Interface that allows the retrieval of the primitive, "computed" values of CSS
 * properties.
 * <p>
 * A few convenience methods have been added to handle specific properties.
 * </p>
 * 
 */
public interface CSSComputedProperties extends CSS2Properties, NodeStyleDeclaration {

	/**
	 * Retrieves the computed object representation of the value of a CSS property. This
	 * method returns <code>null</code> if the property is a shorthand property.
	 * 
	 * @param propertyName
	 *            The name of the CSS property.
	 * @return the object value of the property.
	 */
	@Override
	public ExtendedCSSValue getPropertyCSSValue(String propertyName);

	/**
	 * Retrieves the String value of a CSS property.
	 * 
	 * @param propertyName
	 *            The name of the CSS property. See the CSS property index.
	 * @return the value of the property.
	 */
	@Override
	public String getPropertyValue(String propertyName);

	/**
	 * Gets the primitive, computed value for the 'color' property.
	 * 
	 * @return the value for the 'color' property.
	 */
	public ExtendedCSSPrimitiveValue getCSSColor();

	/**
	 * Gets the primitive, computed value for the 'background-color' property.
	 * 
	 * @return the value for the 'background-color' property.
	 */
	public ExtendedCSSPrimitiveValue getCSSBackgroundColor();

	/**
	 * Gets the computed value(s) for the 'background-image' property.
	 * 
	 * @return an array with the values for the layered 'background-image' property, or null
	 *         if no background image was set for the element.
	 */
	public String[] getBackgroundImages();

	/**
	 * Gets the 'used' value for the font-family property.
	 * <p>
	 * This method requires a style database.
	 * </p>
	 * 
	 * @return the value of the font-family property.
	 * @throws IllegalStateException
	 *             if the style database has not been set.
	 */
	public String getUsedFontFamily();

	/**
	 * Gets the computed font weight.
	 * 
	 * @return the font weight.
	 */
	@Override
	public String getFontWeight();

	/**
	 * Gets the computed value of the font-size property.
	 * <p>
	 * May require a style database to work.
	 * </p>
	 * 
	 * @return the value of the font-size property, in typographic points.
	 */
	public int getComputedFontSize();

	/**
	 * Gets the computed line height with the default 'normal' value of 1.16em.
	 * 
	 * @return the default computed line height, in typographic points.
	 */
	public float getComputedLineHeight();

	/**
	 * Get the box values from a simple box model.
	 * 
	 * @param unitType
	 *            the desired unit type.
	 * @return the box values, in the specified unit.
	 * @throws DOMException
	 *             if the document contains features that are not supported by the simple
	 *             model.
	 * @throws StyleDatabaseRequiredException
	 *             when a computation that requires a style database is attempted, but no
	 *             style database has been set.
	 */
	public BoxValues getBoxValues(short unitType) throws DOMException, StyleDatabaseRequiredException;

	/**
	 * Gets the owner/peer node.
	 * 
	 * @return the owner node.
	 */
	@Override
	public Node getOwnerNode();

	/**
	 * Gets the computed style for the parent element.
	 * 
	 * @return the computed style for the parent element, or null if there is no parent
	 *         element.
	 */
	public CSSComputedProperties getParentComputedStyle();

	/**
	 * Gets the style database used to compute the style.
	 * 
	 * @return the style database.
	 */
	public StyleDatabase getStyleDatabase();

}
