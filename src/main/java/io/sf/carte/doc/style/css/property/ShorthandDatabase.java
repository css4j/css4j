/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Database of shorthand-related property information.
 * 
 * @author Carlos Amengual
 *
 */
public final class ShorthandDatabase {

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

	private static final ShorthandDatabase singleton = new ShorthandDatabase();

	/**
	 * Construct a shorthand database that loads configuration files from classpath
	 * using this object's <code>ClassLoader</code>.
	 */
	private ShorthandDatabase() {
		this(null);
	}

	/**
	 * Construct a shorthand database that uses the given <code>ClassLoader</code> to
	 * load files from classpath.
	 * 
	 * @param loader the loader.
	 */
	public ShorthandDatabase(ClassLoader loader) {
		super();
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
	 * @return an instance of ShorthandDatabase.
	 */
	public static ShorthandDatabase getInstance() {
		return singleton;
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
	 * <code>font</code> shorthand, due to <code>font-variant</code> handling,
	 * and also for <code>border</code> due to interaction with <code>border-image</code>.
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
				return ColorIdentifiers.getInstance().isColorIdentifier(value);
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

	private Properties loadPropertiesfromClasspath(final String filename, final ClassLoader classLoader) {
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
		return '/' + ShorthandDatabase.class.getPackage().getName().replace('.', '/') + '/' + filename;
	}

}
