/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Represents a viewport defined as per the CSS specifications.
 * <p>
 * The <code>Viewport</code>, together with the {@link CSSCanvas}, has a similar role to
 * the W3C's <code>Screen</code>, with the big difference that some of the information
 * that <code>Screen</code> provides is in fact available from the {@link StyleDatabase}.
 * </p>
 */
public interface Viewport {
	/**
	 * The viewport height, in natural units.
	 *
	 * @return the viewport height.
	 */
	int getViewportHeight();

	/**
	 * The viewport width, in natural units.
	 *
	 * @return the viewport width.
	 */
	int getViewportWidth();

	/**
	 * Tests whether the device is currently landscape or portrait.
	 *
	 * @return <code>true</code> if the device is currently landscape, <code>false</code>
	 *         otherwise.
	 */
	boolean isLandscape();

	/**
	 * Gets the width of the scrollbar that appears when a box overflows with
	 * scroll.
	 *
	 * @return the width of the scrollbar.
	 */
	float getScrollbarWidth();
}
