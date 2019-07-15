/*
 * This software extends interfaces defined by CSS Fonts Module Level 4
 *  (https://www.w3.org/TR/css-fonts-4/).
 * Copyright © 2018 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */
package io.sf.carte.doc.style.css;

/**
 * {@literal @}font-feature-values rule. See
 * <a href="https://www.w3.org/TR/css-fonts-4/#font-feature-values">CSS Fonts Module Level
 * 4: the {@literal @}font-feature-values rule</a>.
 */
public interface CSSFontFeatureValuesRule extends ExtendedCSSRule {

	/**
	 * Get the list of font families for which a given set of feature values is
	 * defined.
	 * 
	 * @return the list of font families.
	 */
	String[] getFontFamily();

	/**
	 * Get the annotation map.
	 * 
	 * @return the annotation map.
	 */
	CSSFontFeatureValuesMap getAnnotation();

	/**
	 * Get the ornaments map.
	 * 
	 * @return the ornaments map.
	 */
	CSSFontFeatureValuesMap getOrnaments();

	/**
	 * Get the stylistic map.
	 * 
	 * @return the stylistic map.
	 */
	CSSFontFeatureValuesMap getStylistic();

	/**
	 * Get the swash map.
	 * 
	 * @return the swash map.
	 */
	CSSFontFeatureValuesMap getSwash();

	/**
	 * Get the characterVariant map.
	 * 
	 * @return the characterVariant map.
	 */
	CSSFontFeatureValuesMap getCharacterVariant();

	/**
	 * Get the styleset map.
	 * 
	 * @return the styleset map.
	 */
	CSSFontFeatureValuesMap getStyleset();

	/**
	 * Enable a feature values map for the given feature value name. When enabled, a map for
	 * <code>featureValueName</code> will be returned by {@link #getFeatureValuesMap(String)}.
	 * <p>
	 * If a standard feature values name (like 'annotation' or 'stylistic') is enabled, this
	 * method has no effect. The method can be used to enable, for example,
	 * <code>historical-forms</code>.
	 * 
	 * @param featureValueName
	 *            the feature value name to be enabled.
	 */
	void enableMap(String featureValueName);

	/**
	 * Get a feature values map for the given name.
	 * 
	 * @param featureValueName
	 *            the feature value name.
	 * @return the feature values map, or <code>null</code> if the feature value name is not
	 *         standard and has not been enabled by {@link #enableMap(String)}.
	 */
	CSSFontFeatureValuesMap getFeatureValuesMap(String featureValueName);

}