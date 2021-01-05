/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * CSS canvas interface.
 */
public interface CSSCanvas {

	/**
	 * Gets the style database for this canvas.
	 *
	 * @return the StyleDatabase.
	 */
	StyleDatabase getStyleDatabase();

	/**
	 * Is the provided pseudo-class active in this canvas?
	 *
	 * @param element
	 *            the element to which the pseudo-class applies.
	 * @param pseudoclassName
	 *            the pseudo-class name.
	 * @return <code>true</code> id the pseudo-class is active, <code>false</code> otherwise.
	 */
	boolean isActivePseudoClass(CSSElement element, String pseudoclassName);

	/**
	 * Provide the value of the requested media feature.
	 *
	 * @param feature
	 *            the media feature.
	 * @return the value of the requested media feature, or <code>null</code> if the
	 *         feature is not known or not supported.
	 */
	CSSTypedValue getFeatureValue(String feature);

	/**
	 * Does this canvas support the given media feature?
	 *
	 * @param featureName
	 *            the media feature name.
	 * @param value
	 *            the optional feature value to be tested against.
	 * @return <code>true</code> if the feature (with the given value, if any) is
	 *         supported, <code>false</code> otherwise.
	 */
	boolean matchesFeature(String featureName, CSSTypedValue value);

	/**
	 * Reload any possible style state/caching that this canvas may have.
	 */
	void reloadStyleState();

	/**
	 * Gets the viewport of this canvas.
	 *
	 * @return the viewport, or null if this canvas does not have a viewport.
	 */
	Viewport getViewport();

	/**
	 * Gives the width, in typographic points, for showing the given text with
	 * the given style.
	 *
	 * @param text
	 *            the text to measure.
	 * @param style
	 *            the style that applies.
	 * @return the advance width for showing the text with the given font.
	 */
	int stringWidth(String text, CSSComputedProperties style);

	/**
	 * Get the cap-height of the first available font, in typographic points.
	 * <p>
	 * If the cap-height is not available, the font's ascent must be used.
	 *
	 * @param style
	 *            the style that applies.
	 * @return the cap-height of the first available font.
	 */
	float getCapHeight(CSSComputedProperties style);

	/**
	 * Gets the document to which this canvas applies.
	 *
	 * @return the document.
	 */
	CSSDocument getDocument();

}
