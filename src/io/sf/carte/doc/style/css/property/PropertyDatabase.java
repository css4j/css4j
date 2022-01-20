/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Database of device-independent CSS property information.
 * 
 * @author Carlos Amengual
 *
 */
public final class PropertyDatabase {

	private final Set<String> inherited_properties;

	/**
	 * Map of initial property values.
	 */
	private final Map<String, Object> initialValueMap;

	private final ValueFactory valueFactory = new ValueFactory();

	private static final PropertyDatabase singleton = new PropertyDatabase();

	/**
	 * Construct a property database.
	 */
	PropertyDatabase() {
		super();
		/*
		 * Inherited properties
		 */
		inherited_properties = computeInheritedPropertiesList();
		/*
		 * Initialize map of initial values (all set to null).
		 */
		initialValueMap = createInitialValueMap();
	}

	/**
	 * Gets an instance of this class.
	 * 
	 * @return an instance of PropertyDatabase.
	 */
	public static PropertyDatabase getInstance() {
		return singleton;
	}

	/**
	 * Get a value factory configured with defaults.
	 * 
	 * @return the value factory used by this database.
	 */
	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	/**
	 * Does this property inherit value by default?
	 * 
	 * @param name
	 *            the name of the property.
	 * @return <code>true</code> if inherits by default, <code>false</code> otherwise.
	 */
	public boolean isInherited(String name) {
		return inherited_properties.contains(name);
	}

	private static Set<String> computeInheritedPropertiesList() {
		/**
		 * List of properties that inherit by default.
		 */
		String[] inherit = { "azimuth", "border-collapse", "border-spacing", "caption-side", "color",
				"cursor", "direction", "elevation", "empty-cells", "font-family", "font-feature-settings",
				"font-kerning", "font-language-override", "font-max-size", "font-min-size",
				"font-optical-sizing", "font-size", "font-size-adjust", "font-style", "font-stretch",
				"font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps",
				"font-variant-east-asian", "font-variant-numeric", "font-variant-position",
				"font-variation-settings", "font-weight", "letter-spacing", "line-height",
				"list-style-image", "list-style-position", "list-style-type", "list-style", "orphans",
				"page-break-inside", "pitch-range", "pitch", "quotes", "richness", "speak-header",
				"speak-numeral", "speak-punctuation", "speak", "speak-as", "speech-rate", "stress",
				"text-align", "text-emphasis-color", "text-emphasis-position", "text-emphasis-style",
				"text-indent", "text-shadow", "text-transform", "text-underline-position", "visibility",
				"voice-balance", "voice-family", "voice-pitch", "voice-range", "voice-rate", "voice-stress",
				"voice-volume", "volume", "white-space", "widows", "word-spacing", "writing-mode" };
		HashSet<String> inheritSet = new HashSet<String>(inherit.length);
		Collections.addAll(inheritSet, inherit);
		return inheritSet;
	}

	/**
	 * Gives the initial device-independent initial (default) value for the
	 * given property.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @return the initial CSS value, or null if no device-independent default
	 *         could be found.
	 */
	public StyleValue getInitialValue(String propertyName) {
		StyleValue svalue;
		Object value = initialValueMap.get(propertyName);
		if (value != null && value.getClass() == String.class) {
			svalue = valueFactory.parseProperty((String) value);
			svalue.setReadOnly();
			initialValueMap.put(propertyName, svalue);
		} else {
			svalue = (StyleValue) value;
		}
		return svalue;
	}

	private Map<String, Object> createInitialValueMap() {
		String[][] initialArray = {
			{"align-content", "normal"},
			{"align-items", "normal"},
			{"align-self", "auto"},
			{"animation-name", "none"},
			{"animation-duration", "0s"},
			{"animation-timing-function", "ease"},
			{"animation-iteration-count", "1"},
			{"animation-direction", "normal"},
			{"animation-play-state", "running"},
			{"animation-delay", "0s"},
			{"animation-fill-mode", "none"},
			{"appearance", "auto"},
			{"azimuth", "center"},
			{"background-attachment", "scroll"},
			{"background-blend-mode", "normal"},
			{"background-clip", "border-box"},
			{"background-color", "transparent"},
			{"background-image", "none"},
			{"background-origin", "padding-box"},
			{"background-position", "0% 0%"},
			{"background-repeat", "repeat"},
			{"background-size", "auto"},
			{"border-collapse", "separate"},
			{"border-spacing", "0"},
			{"border-top-style", "none"},
			{"border-right-style", "none"},
			{"border-bottom-style", "none"},
			{"border-left-style", "none"},
			{"border-top-width", "medium"},
			{"border-right-width", "medium"},
			{"border-bottom-width", "medium"},
			{"border-left-width", "medium"},
			{"border-image-outset", "0"},
			{"border-image-repeat", "stretch"},
			{"border-image-slice", "100%"},
			{"border-image-source", "none"},
			{"border-image-width", "1"},
			{"border-top-left-radius", "0"},
			{"border-top-right-radius", "0"},
			{"border-bottom-right-radius", "0"},
			{"border-bottom-left-radius", "0"},
			{"bottom", "auto"},
			{"box-decoration-break", "slice"}, // May be dropped from spec
			{"box-shadow", "none"},
			{"caption-side", "top"},
			{"caret-color", "auto"},
			{"chains", "none"},
			{"clear", "none"},
			{"clip-path", "none"},
			{"clip-rule", "nonzero"},
			{"column-count", "auto"},
			{"column-gap", "normal"},
			{"column-width", "auto"},
			{"column-rule-style", "none"},
			{"column-rule-width", "medium"},
			{"contain", "none"},
			{"content", "normal"},
			{"counter-increment", "none"},
			{"counter-reset", "none"},
			{"counter-set", "none"},
			{"cue-after", "none"},
			{"cue-before", "none"},
			{"cursor", "auto"},
			{"direction", "ltr"},
			{"display", "inline"},
			{"elevation", "level"}, 
			{"empty-cells", "show"},
			{"float", "none"},
			{"flow", "auto"},
			{"flex-direction", "row"},
			{"flex-wrap", "nowrap"},
			{"flex-grow", "0"},
			{"flex-shrink", "1"},
			{"flex-basis", "auto"},
			{"font-feature-settings", "normal"},
			{"font-language-override", "normal"}, // at risk of removal
			{"font-min-size", "0"},
			{"font-kerning", "auto"},
			{"font-optical-sizing", "auto"},
			{"font-size", "medium"},
			{"font-size-adjust", "none"},
			{"font-stretch", "normal"},
			{"font-style", "normal"},
			{"font-synthesis", "weight style"},
			{"font-variant", "normal"}, // this is a shorthand, but special case
			{"font-variant-alternates", "normal"},
			{"font-variant-caps", "normal"},
			{"font-variant-east-asian", "normal"},
			{"font-variant-ligatures", "normal"},
			{"font-variant-position", "normal"},
			{"font-variant-numeric", "normal"},
			{"font-variation-settings", "normal"},
			{"font-weight", "normal"},
			{"grid-auto-columns", "auto"},
			{"grid-auto-rows", "auto"},
			{"grid-auto-flow", "row"},
			{"grid-column-end", "auto"},
			{"grid-column-start", "auto"},
			{"grid-row-end", "auto"},
			{"grid-row-start", "auto"},
			{"grid-template-areas", "none"},
			{"grid-template-columns", "none"},
			{"grid-template-rows", "none"},
			{"height", "auto"},
			{"image-orientation", "0deg"},
			{"image-resolution", "1dppx"},
			{"isolation", "auto"},
			{"justify-content", "normal"},
			{"justify-items", "legacy"},
			{"justify-self", "auto"},
			{"left", "auto"},
			{"letter-spacing", "normal"}, 
			{"line-height", "normal"},
			{"list-style-image", "none"},
			{"list-style-position", "outside"}, 
			{"list-style-type", "disc"},
			{"margin-right", "0"},
			{"margin-left", "0"},
			{"margin-top", "0"},
			{"margin-bottom", "0"},
			{"max-height", "none"},
			{"max-width", "none"},
			{"min-height", "0"},
			{"min-width", "0"},
			{"opacity", "1"},
			{"orphans", "2"},
			{"outline-color", "invert"},
			{"outline-style", "none"},
			{"outline-width", "medium"},
			{"overflow", "visible"},
			{"padding-top", "0"},
			{"padding-right", "0"},
			{"padding-bottom", "0"},
			{"padding-left", "0"},
			{"page-break-after", "auto"}, 
			{"page-break-before", "auto"}, 
			{"page-break-inside", "auto"}, 
			{"pause-after", "0"},
			{"pause-before", "0"},
			{"pitch-range", "50"},
			{"pitch", "medium"},
			{"play-during", "auto"},
			{"position", "static"},
			{"quotes", "auto"},
			{"resize", "none"},
			{"rest-after", "0"},
			{"rest-before", "0"},
			{"richness", "50"},
			{"right", "auto"},
			{"row-gap", "normal"},
			{"speak-header", "once"}, 
			{"speak-numeral", "continuous"},
			{"speak-punctuation", "none"},
			{"speak", "auto"},
			{"speak-as", "normal"},
			{"speech-rate", "medium"}, 
			{"stress", "50"},
			{"table-layout", "auto"},
			{"text-decoration-line", "none"},
			{"text-decoration-style", "solid"},
			{"text-emphasis-style", "none"},
			{"text-emphasis-position", "over right"},
			{"text-indent", "0"},
			{"text-transform", "none"},
			{"text-shadow", "none"},
			{"text-underline-position", "auto"},
			{"top", "auto"},
			{"transition-property", "all"},
			{"transition-duration", "0s"},
			{"transition-timing-function", "ease"},
			{"transition-delay", "0s"},
			{"unicode-bidi", "normal"},
			{"unicode-range", "U+0-10FFFF"},
			{"vertical-align", "baseline"},
			{"visibility", "visible"},
			{"voice-balance", "center"},
			{"voice-duration", "auto"},
			{"voice-family", "male"},
			{"voice-pitch", "medium"},
			{"voice-range", "medium"},
			{"voice-rate", "normal"},
			{"voice-stress", "normal"},
			{"voice-volume", "medium"},
			{"volume", "medium"},
			{"white-space", "normal"},
			{"widows", "2"},
			{"width", "auto"},
			{"will-change", "auto"},
			{"word-spacing", "normal"},
			{"writing-mode", "horizontal-tb"},
			{"z-index", "auto"}
		};
		Map<String, Object> initialValueMap = new HashMap<String, Object>(initialArray.length);
		for (int i=0; i<initialArray.length; i++){
			initialValueMap.put(initialArray[i][0], initialArray[i][1]);
		}
		return initialValueMap;
	}

	/**
	 * Is the supplied string the name of a known CSS property?
	 * 
	 * @param name
	 *            the name to test.
	 * @return <code>true</code> if it is a known CSS property, <code>false</code> otherwise.
	 */
	public boolean isKnownProperty(String name) {
		return initialValueMap.keySet().contains(name);
	}

	Set<String> getKnownPropertySet() {
		return initialValueMap.keySet();
	}

}
