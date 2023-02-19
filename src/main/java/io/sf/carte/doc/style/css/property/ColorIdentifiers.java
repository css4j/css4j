/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * CSS Color identifiers map.
 * <p>
 * Per CSS3 color spec, section 4.3.
 * </p>
 * 
 * @author Carlos Amengual
 * 
 */
public class ColorIdentifiers {

	private static final ColorIdentifiers singleton = new ColorIdentifiers();

	private final Map<String, String> colorKeywords = new HashMap<String, String>(147);
	private final HashSet<String> systemColors = new HashSet<String>();

	public ColorIdentifiers() {
		super();
		colorKeywords.put("aliceblue", "#f0f8ff");
		colorKeywords.put("antiquewhite", "#faebd7");
		colorKeywords.put("aqua", "#00ffff");
		colorKeywords.put("aquamarine", "#7fffd4");
		colorKeywords.put("azure", "#f0ffff");
		colorKeywords.put("beige", "#f5f5dc");
		colorKeywords.put("bisque", "#ffe4c4");
		colorKeywords.put("black", "#000000");
		colorKeywords.put("blanchedalmond", "#ffebcd");
		colorKeywords.put("blue", "#0000ff");
		colorKeywords.put("blueviolet", "#8a2be2");
		colorKeywords.put("brown", "#a52a2a");
		colorKeywords.put("burlywood", "#deb887");
		colorKeywords.put("cadetblue", "#5f9ea0");
		colorKeywords.put("chartreuse", "#7fff00");
		colorKeywords.put("chocolate", "#d2691e");
		colorKeywords.put("coral", "#ff7f50");
		colorKeywords.put("cornflowerblue", "#6495ed");
		colorKeywords.put("cornsilk", "#fff8dc");
		colorKeywords.put("crimson", "#dc143c");
		colorKeywords.put("cyan", "#00ffff");
		colorKeywords.put("darkblue", "#00008b");
		colorKeywords.put("darkcyan", "#008b8b");
		colorKeywords.put("darkgoldenrod", "#b8860b");
		colorKeywords.put("darkgray", "#a9a9a9");
		colorKeywords.put("darkgreen", "#006400");
		colorKeywords.put("darkgrey", "#a9a9a9");
		colorKeywords.put("darkkhaki", "#bdb76b");
		colorKeywords.put("darkmagenta", "#8b008b");
		colorKeywords.put("darkolivegreen", "#556b2f");
		colorKeywords.put("darkorange", "#ff8c00");
		colorKeywords.put("darkorchid", "#9932cc");
		colorKeywords.put("darkred", "#8b0000");
		colorKeywords.put("darksalmon", "#e9967a");
		colorKeywords.put("darkseagreen", "#8fbc8f");
		colorKeywords.put("darkslateblue", "#483d8b");
		colorKeywords.put("darkslategray", "#2f4f4f");
		colorKeywords.put("darkslategrey", "#2f4f4f");
		colorKeywords.put("darkturquoise", "#00ced1");
		colorKeywords.put("darkviolet", "#9400d3");
		colorKeywords.put("deeppink", "#ff1493");
		colorKeywords.put("deepskyblue", "#00bfff");
		colorKeywords.put("dimgray", "#696969");
		colorKeywords.put("dimgrey", "#696969");
		colorKeywords.put("dodgerblue", "#1e90ff");
		colorKeywords.put("firebrick", "#b22222");
		colorKeywords.put("floralwhite", "#fffaf0");
		colorKeywords.put("forestgreen", "#228b22");
		colorKeywords.put("fuchsia", "#ff00ff");
		colorKeywords.put("gainsboro", "#dcdcdc");
		colorKeywords.put("ghostwhite", "#f8f8ff");
		colorKeywords.put("gold", "#ffd700");
		colorKeywords.put("goldenrod", "#daa520");
		colorKeywords.put("gray", "#808080");
		colorKeywords.put("green", "#008000");
		colorKeywords.put("greenyellow", "#adff2f");
		colorKeywords.put("grey", "#808080");
		colorKeywords.put("honeydew", "#f0fff0");
		colorKeywords.put("hotpink", "#ff69b4");
		colorKeywords.put("indianred", "#cd5c5c");
		colorKeywords.put("indigo", "#4b0082");
		colorKeywords.put("ivory", "#fffff0");
		colorKeywords.put("khaki", "#f0e68c");
		colorKeywords.put("lavender", "#e6e6fa");
		colorKeywords.put("lavenderblush", "#fff0f5");
		colorKeywords.put("lawngreen", "#7cfc00");
		colorKeywords.put("lemonchiffon", "#fffacd");
		colorKeywords.put("lightblue", "#add8e6");
		colorKeywords.put("lightcoral", "#f08080");
		colorKeywords.put("lightcyan", "#e0ffff");
		colorKeywords.put("lightgoldenrodyellow", "#fafad2");
		colorKeywords.put("lightgray", "#d3d3d3");
		colorKeywords.put("lightgreen", "#90ee90");
		colorKeywords.put("lightgrey", "#d3d3d3");
		colorKeywords.put("lightpink", "#ffb6c1");
		colorKeywords.put("lightsalmon", "#ffa07a");
		colorKeywords.put("lightseagreen", "#20b2aa");
		colorKeywords.put("lightskyblue", "#87cefa");
		colorKeywords.put("lightslategray", "#778899");
		colorKeywords.put("lightslategrey", "#778899");
		colorKeywords.put("lightsteelblue", "#b0c4de");
		colorKeywords.put("lightyellow", "#ffffe0");
		colorKeywords.put("lime", "#00ff00");
		colorKeywords.put("limegreen", "#32cd32");
		colorKeywords.put("linen", "#faf0e6");
		colorKeywords.put("magenta", "#ff00ff");
		colorKeywords.put("maroon", "#800000");
		colorKeywords.put("mediumaquamarine", "#66cdaa");
		colorKeywords.put("mediumblue", "#0000cd");
		colorKeywords.put("mediumorchid", "#ba55d3");
		colorKeywords.put("mediumpurple", "#9370db");
		colorKeywords.put("mediumseagreen", "#3cb371");
		colorKeywords.put("mediumslateblue", "#7b68ee");
		colorKeywords.put("mediumspringgreen", "#00fa9a");
		colorKeywords.put("mediumturquoise", "#48d1cc");
		colorKeywords.put("mediumvioletred", "#c71585");
		colorKeywords.put("midnightblue", "#191970");
		colorKeywords.put("mintcream", "#f5fffa");
		colorKeywords.put("mistyrose", "#ffe4e1");
		colorKeywords.put("moccasin", "#ffe4b5");
		colorKeywords.put("navajowhite", "#ffdead");
		colorKeywords.put("navy", "#000080");
		colorKeywords.put("oldlace", "#fdf5e6");
		colorKeywords.put("olive", "#808000");
		colorKeywords.put("olivedrab", "#6b8e23");
		colorKeywords.put("orange", "#ffa500");
		colorKeywords.put("orangered", "#ff4500");
		colorKeywords.put("orchid", "#da70d6");
		colorKeywords.put("palegoldenrod", "#eee8aa");
		colorKeywords.put("palegreen", "#98fb98");
		colorKeywords.put("paleturquoise", "#afeeee");
		colorKeywords.put("palevioletred", "#db7093");
		colorKeywords.put("papayawhip", "#ffefd5");
		colorKeywords.put("peachpuff", "#ffdab9");
		colorKeywords.put("peru", "#cd853f");
		colorKeywords.put("pink", "#ffc0cb");
		colorKeywords.put("plum", "#dda0dd");
		colorKeywords.put("powderblue", "#b0e0e6");
		colorKeywords.put("purple", "#800080");
		colorKeywords.put("rebeccapurple", "#663399");
		colorKeywords.put("red", "#ff0000");
		colorKeywords.put("rosybrown", "#bc8f8f");
		colorKeywords.put("royalblue", "#4169e1");
		colorKeywords.put("saddlebrown", "#8b4513");
		colorKeywords.put("salmon", "#fa8072");
		colorKeywords.put("sandybrown", "#f4a460");
		colorKeywords.put("seagreen", "#2e8b57");
		colorKeywords.put("seashell", "#fff5ee");
		colorKeywords.put("sienna", "#a0522d");
		colorKeywords.put("silver", "#c0c0c0");
		colorKeywords.put("skyblue", "#87ceeb");
		colorKeywords.put("slateblue", "#6a5acd");
		colorKeywords.put("slategray", "#708090");
		colorKeywords.put("slategrey", "#708090");
		colorKeywords.put("snow", "#fffafa");
		colorKeywords.put("springgreen", "#00ff7f");
		colorKeywords.put("steelblue", "#4682b4");
		colorKeywords.put("tan", "#d2b48c");
		colorKeywords.put("teal", "#008080");
		colorKeywords.put("thistle", "#d8bfd8");
		colorKeywords.put("tomato", "#ff6347");
		colorKeywords.put("turquoise", "#40e0d0");
		colorKeywords.put("violet", "#ee82ee");
		colorKeywords.put("wheat", "#f5deb3");
		colorKeywords.put("white", "#ffffff");
		colorKeywords.put("whitesmoke", "#f5f5f5");
		colorKeywords.put("yellow", "#ffff00");
		colorKeywords.put("yellowgreen", "#9acd32");
		loadSystemColors();
	}

	private void loadSystemColors() {
		systemColors.add("activeborder");
		systemColors.add("activecaption");
		systemColors.add("appworkspace");
		systemColors.add("background");
		systemColors.add("buttonface");
		systemColors.add("buttonhighlight");
		systemColors.add("buttonshadow");
		systemColors.add("buttontext");
		systemColors.add("captiontext");
		systemColors.add("graytext");
		systemColors.add("highlight");
		systemColors.add("highlighttext");
		systemColors.add("inactiveborder");
		systemColors.add("inactivecaption");
		systemColors.add("inactivecaptiontext");
		systemColors.add("infobackground");
		systemColors.add("infotext");
		systemColors.add("menu");
		systemColors.add("menutext");
		systemColors.add("scrollbar");
		systemColors.add("threeddarkshadow");
		systemColors.add("threedface");
		systemColors.add("threedhighlight");
		systemColors.add("threedlightshadow");
		systemColors.add("threedshadow");
		systemColors.add("window");
		systemColors.add("windowframe");
		systemColors.add("windowtext");
	}

	/**
	 * Gets the instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static ColorIdentifiers getInstance() {
		return singleton;
	}

	/**
	 * Gets the color associated to the given keyword.
	 * 
	 * @param keyword
	 *            the color keyword (orange, blue, etc.). Must be lowercase.
	 * @return the corresponding #-starting hex color specification, or null if
	 *         the keyword could not be recognized as a color.
	 */
	public String getColor(String keyword) {
		return colorKeywords.get(keyword);
	}

	/**
	 * Is this a system color identifier ?
	 * 
	 * @param identifier the lowercase identifier to be tested.
	 * @return <code>true</code> if it is a system color identifier.
	 */
	public boolean isSystemColor(String identifier) {
		return systemColors.contains(identifier);
	}

	/**
	 * Is this a known color identifier ?
	 * <p>
	 * <code>transparent</code> is not tested.
	 * 
	 * @param identifier the lowercase identifier to be tested.
	 * @return <code>true</code> if it is a known color identifier.
	 */
	public boolean isColorIdentifier(String identifier) {
		return colorKeywords.containsKey(identifier) || systemColors.contains(identifier);
	}

}
