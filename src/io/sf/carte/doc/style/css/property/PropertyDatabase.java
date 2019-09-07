/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Database of device-independent CSS property information.
 * 
 * @author Carlos Amengual
 *
 */
public final class PropertyDatabase {

	private final Set<String> inherited_properties;

	/**
	 * Map from shorthand property names to subproperties
	 */
	private final Map<String, String[]> shorthand2subp = new HashMap<String, String[]>();

	/**
	 * Maps shorthand subproperties to their shorthand property name.
	 */
	private final Map<String, String> subp2shorthand = new HashMap<String, String>();

	/**
	 * Identifier properties
	 */
	private final Properties identifiers;

	/**
	 * Map of initial property values.
	 */
	private final Map<String, StyleValue> initialValueMap;

	private static final PropertyDatabase singleton = new PropertyDatabase();

	/**
	 * Construct a property database that loads configuration files from classpath
	 * using this object's <code>ClassLoader</code>.
	 */
	PropertyDatabase() {
		this(null);
	}

	/**
	 * Construct a property database that uses the given <code>ClassLoader</code> to
	 * load files from classpath.
	 * 
	 * @param loader the loader.
	 */
	PropertyDatabase(ClassLoader loader) {
		super();
		/*
		 * Inherited properties
		 */
		inherited_properties = computeInheritedPropertiesList();
		/*
		 * Initial values
		 */
		initialValueMap = computeInitialValueMap();
		/*
		 * Identifiers
		 */
		identifiers = loadPropertiesfromClasspath("identifier.properties", loader);
		/*
		 * Shorthand properties
		 */
		Properties shand = loadPropertiesfromClasspath("shorthand.properties", loader);
		ArrayList<String> array = new ArrayList<String>();
		Iterator<Entry<Object, Object>> it = shand.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> me = it.next();
			String shname = (String) me.getKey();
			String subpties = (String) me.getValue();
			StringTokenizer st = new StringTokenizer(subpties, ",");
			while (st.hasMoreTokens()) {
				array.add(st.nextToken().trim());
			}
			addShorthand(shname, array.toArray(new String[0]));
			array.clear();
		}
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
				"cursor", "direction", "elevation", "empty-cells", "font-family", "font-max-size",
				"font-min-size", "font-size", "font-size-adjust", "font-style", "font-stretch",
				"font-synthesis", "font-variant", "font-weight", "font", "letter-spacing", "line-height",
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
		return initialValueMap.get(propertyName);
	}

	private Map<String, StyleValue> computeInitialValueMap() {
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
			{"font-min-size", "0"},
			{"font-kerning", "auto"},
			{"font-size", "medium"},
			{"font-size-adjust", "none"},
			{"font-stretch", "normal"},
			{"font-style", "normal"},
			{"font-synthesis", "weight style"},
			{"font-variant", "normal"}, // this is a shorthand, but special case
			{"font-variant-ligatures", "normal"},
			{"font-variant-position", "normal"},
			{"font-variant-caps", "normal"},
			{"font-variant-numeric", "normal"},
			{"font-variant-alternates", "normal"},
			{"font-variant-east-asian", "normal"},
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
		ValueFactory valueFactory = new ValueFactory();
		Map<String, StyleValue> initialValueMap =
			new HashMap<String, StyleValue>(initialArray.length);
		for (int i=0; i<initialArray.length; i++){
			StyleValue value = valueFactory.parseProperty(initialArray[i][1]);
			initialValueMap.put(initialArray[i][0], value);
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

	/**
	 * Is this a shorthand property?
	 * 
	 * @param name
	 *            the name of the property.
	 * @return <code>true</code> if is a shorthand, <code>false</code> otherwise.
	 */
	public boolean isShorthand(String name) {
		return shorthand2subp.containsKey(name);
	}

	/**
	 * Is this the subproperty of a shorthand property?
	 * 
	 * @param name
	 *            the name of the property.
	 * @return <code>true</code> if is a shorthand subproperty, <code>false</code> otherwise.
	 */
	public boolean isShorthandSubproperty(String name) {
		return subp2shorthand.containsKey(name);
	}

	/**
	 * Gets the shorthand for this subproperty, if any.
	 * 
	 * @param subproperty
	 *            the subproperty name.
	 * @return the name of the shorthand for this subproperty, or
	 *         <code>null</code> if <code>subproperty</code> is not a recognized
	 *         subproperty.
	 */
	public String getShorthand(String subproperty) {
		return subp2shorthand.get(subproperty);
	}

	/**
	 * Is the given property name a subproperty of the given shorthand property ?
	 * 
	 * @param shorthand
	 *            the name of the shorthand property to test.
	 * @param subpName
	 *            the name of the possible subproperty.
	 * @return <code>true</code> if subpName is a subproperty of the given shorthand, false
	 *         otherwise.
	 */
	public boolean isShorthandSubpropertyOf(String shorthand, String subpName) {
		String sh = subp2shorthand.get(subpName);
		if (sh == null) {
			return false;
		} else {
			if (sh.equals(shorthand)) {
				return true;
			} else {
				// Check for border-width, border-style, border-color
				sh = subp2shorthand.get(sh);
				if (shorthand.equals(sh)) {
					return true;
				} else {
					String[] subp = shorthand2subp.get(shorthand);
					for (int i = 0; i < subp.length; i++) {
						if (subp[i].equals(subpName)) {
							return true;
						}
					}
					return false;
				}
			}
		}
	}

	/**
	 * Get the subproperties of the given shorthand.
	 * <p>
	 * The subproperties may be, in turn, shorthands.
	 * 
	 * @param shorthandName the shorthand name.
	 * @return the array of subproperties, or <code>null</code> if the shorthand
	 *         name is not known.
	 */
	public String[] getShorthandSubproperties(String shorthandName) {
		return shorthand2subp.get(shorthandName);
	}

	/**
	 * Get an array with the names of the longhand subproperties for
	 * <code>shorthandName</code>.
	 * <p>
	 * For convenience of the library's internals, the array is incomplete for the
	 * <code>font</code> shorthand, due to <code>font-variant</code> handling.
	 * 
	 * @param shorthandName the shorthand name.
	 * @return an array with the names of the longhand subproperties, or
	 *         <code>null</code> if the shorthand name is not known.
	 */
	public String[] getLonghandProperties(String shorthandName) {
		String[] subparray = getShorthandSubproperties(shorthandName);
		if (subparray != null) {
			for (String subproperty : subparray) {
				if (isShorthand(subproperty)) {
					return longhandArray(subparray);
				}
			}
		}
		return subparray;
	}

	private String[] longhandArray(String[] subparray) {
		LinkedList<String> list = new LinkedList<String>();
		for (String subproperty : subparray) {
			if (!isShorthand(subproperty)) {
				list.add(subproperty);
			} else {
				addLonghandsToList(list, subproperty);
			}
		}
		return list.toArray(new String[0]);
	}

	private void addLonghandsToList(LinkedList<String> list, String property) {
		if (!isShorthand(property)) {
			list.add(property);
		} else {
			String[] ptysubp = getShorthandSubproperties(property);
			for (String pty : ptysubp) {
				addLonghandsToList(list, pty);
			}
		}
	}

	private void addShorthand(String shorthand, String[] subproperties) {
		shorthand2subp.put(shorthand, subproperties);
		for (int i = 0; i < subproperties.length; i++) {
			String prevSh = subp2shorthand.get(subproperties[i]);
			if (prevSh != null) {
				// Already one map for this key, e.g. we have
				// border-top-width -> border-top and now we got
				// border-top-width -> border-width
				String topShorthand = subp2shorthand.get(shorthand);
				String topPrevSh = subp2shorthand.get(prevSh);
				if (topShorthand == null) {
					if (topPrevSh != null) {
						subp2shorthand.put(shorthand, topPrevSh);
					}
				} else if (topPrevSh == null) {
					subp2shorthand.put(prevSh, topShorthand);
				}
			}
			subp2shorthand.put(subproperties[i], shorthand);
		}
	}

	/**
	 * Determines if the given value is an identifier for the given property
	 * name.
	 * <p>
	 * Generic identifiers such as <code>inherit</code> or <code>none</code> are
	 * not checked.
	 * <p>
	 * If the property name ends with '-color', the value is checked for a valid
	 * color value identifier, regardless of the property name being known or
	 * not.
	 * 
	 * @param propertyName
	 *            the lowercase name of the property.
	 * @param value
	 *            the value that has to be tested to be an identifier for
	 *            propertyName.
	 * @return <code>true</code> if <code>value</code> is recognized as an identifier of
	 *         <code>propertyName</code>, <code>false</code> otherwise.
	 */
	public boolean isIdentifierValue(String propertyName, String value) {
		String csl = identifiers.getProperty(propertyName);
		if (csl == null) {
			// Could not find identifiers for propertyName
			if (propertyName.endsWith("-color")) {
				value = value.toLowerCase(Locale.ROOT);
				return ColorIdentifiers.getInstance().isColorIdentifier(value) || "transparent".equals(value);
			}
			return false;
		}
		StringTokenizer tokp = new StringTokenizer(csl, ",");
		while (tokp.hasMoreTokens()) {
			String s = tokp.nextToken();
			if (s.trim().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if the given property has known identifier values that could be checked with
	 * {@link #isIdentifierValue(String, String)}.
	 * 
	 * @param propertyName
	 *            the lowercase name of the property.
	 * @return <code>true</code> if <code>propertyName</code> has identifiers known to this database.
	 */
	public boolean hasKnownIdentifierValues(String propertyName) {
		return identifiers.containsKey(propertyName);
	}

	Properties loadPropertiesfromClasspath(final String filename, final ClassLoader classLoader) {
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Properties>() {
			@Override
			public Properties run() {
				InputStream is;
				if (classLoader != null) {
					is = classLoader.getResourceAsStream(resourcePath(filename));
				} else {
					is = this.getClass().getResourceAsStream(resourcePath(filename));
				}
				if (is == null) {
					return null;
				}
				Properties p = new Properties();
				try {
					p.load(is);
				} catch (IOException e) {
					return null;
				} finally {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
				return p;
			}
		});
	}

	private String resourcePath(String filename) {
		return '/' + PropertyDatabase.class.getPackage().getName().replace('.', '/') + '/' + filename;
	}

}
