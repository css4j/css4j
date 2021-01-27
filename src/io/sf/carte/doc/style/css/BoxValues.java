/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Interface giving access to the computed box values.
 */
public interface BoxValues {
	/**
	 * Gets the computed value of the margin-top property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the margin-top property.
	 */
	float getMarginTop();

	/**
	 * Gets the computed value of the margin-right property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the margin-right property.
	 */
	float getMarginRight();

	/**
	 * Gets the computed value of the margin-bottom property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the margin-bottom property.
	 */
	float getMarginBottom();

	/**
	 * Gets the computed value of the margin-left property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the margin-left property.
	 */
	float getMarginLeft();

	/**
	 * Gets the computed value of the padding-top property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the padding-top property.
	 */
	float getPaddingTop();

	/**
	 * Gets the computed value of the padding-right property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the padding-right property.
	 */
	float getPaddingRight();

	/**
	 * Gets the computed value of the padding-bottom property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the padding-bottom property.
	 */
	float getPaddingBottom();

	/**
	 * Gets the computed value of the padding-left property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the padding-left property.
	 */
	float getPaddingLeft();

	/**
	 * Gets the computed value of the border-top-width property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the border-top-width property.
	 */
	float getBorderTopWidth();

	/**
	 * Gets the computed value of the border-right-width property, expressed in
	 * a previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the border-right-width property.
	 */
	float getBorderRightWidth();

	/**
	 * Gets the computed value of the border-bottom-width property, expressed in
	 * a previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the border-bottom-width property.
	 */
	float getBorderBottomWidth();

	/**
	 * Gets the computed value of the border-left-width property, expressed in a
	 * previously specified unit (see the documentation for the class that
	 * returned this object).
	 * 
	 * @return the value of the border-left-width property.
	 */
	float getBorderLeftWidth();

	/**
	 * Gets the computed value of the width property, expressed in a previously
	 * specified unit (see the documentation for the class that returned this
	 * object).
	 * <p>
	 * For non-replaced inline elements, table rows, and row groups the value is
	 * undefined, and an estimated content width is returned instead.
	 * 
	 * @return the value of the width property.
	 */
	float getWidth();

	/**
	 * Sub-interface for tables in the simple box model.
	 */
	public interface TableBoxValues extends BoxValues {

		/**
		 * Gets an array with the content width of table columns.
		 * 
		 * @return the column content widths.
		 */
		float[] getColumnsContentWidth();

	}
}
