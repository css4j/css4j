/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.util.HashMap;
import java.util.Map;

/**
 * CSS Color minifier.
 */
class ColorMinifier {

	private static final ColorMinifier singleton = new ColorMinifier();

	private static final Map<String, String> colorMap = new HashMap<>(37);

	static {
		colorMap.put("#f0ffff", "azure");
		colorMap.put("#f5f5dc", "beige");
		colorMap.put("#ffe4c4", "bisque");
		colorMap.put("#a52a2a", "brown");
		colorMap.put("#ff7f50", "coral");
		colorMap.put("#ffd700", "gold");
		colorMap.put("#008000", "green");
		colorMap.put("#808080", "grey");
		colorMap.put("#4b0082", "indigo");
		colorMap.put("#fffff0", "ivory");
		colorMap.put("#f0e68c", "khaki");
		colorMap.put("#faf0e6", "linen");
		colorMap.put("#800000", "maroon");
		colorMap.put("#000080", "navy");
		colorMap.put("#808000", "olive");
		colorMap.put("#ffa500", "orange");
		colorMap.put("#da70d6", "orchid");
		colorMap.put("#cd853f", "peru");
		colorMap.put("#ffc0cb", "pink");
		colorMap.put("#dda0dd", "plum");
		colorMap.put("#800080", "purple");
		colorMap.put("#ff0000", "red");
		colorMap.put("#f00", "red");
		colorMap.put("#fa8072", "salmon");
		colorMap.put("#a0522d", "sienna");
		colorMap.put("#c0c0c0", "silver");
		colorMap.put("#fffafa", "snow");
		colorMap.put("#d2b48c", "tan");
		colorMap.put("#008080", "teal");
		colorMap.put("#ff6347", "tomato");
		colorMap.put("#ee82ee", "violet");
		colorMap.put("#f5deb3", "wheat");

		colorMap.put("#000000", "#000");
		colorMap.put("#0000ff", "#00f");
		colorMap.put("#00ff00", "#0f0");
		colorMap.put("#00ffff", "#0ff");
		colorMap.put("#ffffff", "#fff");
	}

	private ColorMinifier() {
		super();
	}

	/**
	 * Gets the instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static ColorMinifier getInstance() {
		return singleton;
	}

	public String minifyHex(String hex) {
		String ident = colorMap.get(hex);
		return ident != null ? ident : hex;
	}

}
