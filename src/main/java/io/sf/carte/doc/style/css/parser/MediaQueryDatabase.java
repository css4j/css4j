/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Collections;
import java.util.HashSet;

/**
 * Information useful for parsing media-queries.
 */
class MediaQueryDatabase {

	static final String[] rangeFeatures = { "aspect-ratio", "color", "color-index", "height", "monochrome",
			"resolution", "width" };

	private static final HashSet<String> mediaFeatureSet;

	static {
		final String[] discreteFeatures = { "any-hover", "any-pointer", "color-gamut", "grid", "hover", "orientation",
				"overflow-block", "overflow-inline", "pointer", "scan", "update" };
		mediaFeatureSet = new HashSet<>(rangeFeatures.length + discreteFeatures.length);
		Collections.addAll(mediaFeatureSet, rangeFeatures);
		Collections.addAll(mediaFeatureSet, discreteFeatures);
	}

	static boolean isMediaFeature(String string) {
		return mediaFeatureSet.contains(string);
	}

}
