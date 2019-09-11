/*
 * This software extends interfaces defined by CSS Fonts Module Level 4
 * (https://www.w3.org/TR/css-fonts-4/).
 * Copyright © 2013 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.util.List;

/**
 * Based on W3C's CSSFontFeatureValuesMap interface. See the
 * <a href="https://www.w3.org/TR/css-fonts-4/#cssfontfeaturevaluesmap">definition of
 * {@literal @}font-feature-values interface in CSS Fonts Module Level 4.</a>.
 */
public interface CSSFontFeatureValuesMap {

	int[] get(String featureValueName);

	void set(String featureValueName, int... values);

	/**
	 * Get a list of the comments that preceded this map, if any.
	 * 
	 * @return the list of comments, or <code>null</code> if there were no preceding
	 *         comments.
	 */
	List<String> getPrecedingComments();

}
