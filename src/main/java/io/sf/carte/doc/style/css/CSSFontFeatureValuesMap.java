/*
 * This software extends interfaces defined by CSS Fonts Module Level 4
 * (https://www.w3.org/TR/css-fonts-4/).
 * Copyright © 2013 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.property.PrimitiveValue;

/**
 * Based on W3C's CSSFontFeatureValuesMap interface. See the
 * <a href="https://www.w3.org/TR/css-fonts-4/#cssfontfeaturevaluesmap">definition of
 * {@literal @}font-feature-values interface in CSS Fonts Module Level 4.</a>.
 */
public interface CSSFontFeatureValuesMap {

	/**
	 * Get the feature values associated with the given feature value name.
	 * 
	 * @param featureValueName the feature value name.
	 * @return the feature values, or <code>null</code> if there are no values for
	 *         that name.
	 */
	PrimitiveValue[] get(String featureValueName);

	/**
	 * Set the feature value(s) associated to the given feature value name.
	 * 
	 * @param featureValueName the feature value name.
	 * @param values           the feature value(s).
	 */
	void set(String featureValueName, PrimitiveValue... values);

	/**
	 * Get a list of the comments that preceded this map, if any.
	 * 
	 * @return the list of comments, or <code>null</code> if there were no preceding
	 *         comments.
	 */
	StringList getPrecedingComments();

	/**
	 * Get a list of the comments that were found right after this map, if any.
	 * 
	 * @return the list of comments, or <code>null</code> if there were no trailing
	 *         comments.
	 */
	StringList getTrailingComments();

}
