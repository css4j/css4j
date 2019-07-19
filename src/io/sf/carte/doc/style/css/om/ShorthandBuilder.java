/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSValueList;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.URIValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Base class for shorthand builders, that try to build shorthands from a set of
 * individual properties.
 */
abstract class ShorthandBuilder {

	private final String shorthandName;
	private BaseCSSStyleDeclaration parentStyle;
	private String[] subp;
	private Set<String> impPtySet = new TreeSet<String>();
	private Set<String> ptySet = new TreeSet<String>();

	ShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super();
		this.shorthandName = shorthandName;
		this.parentStyle = parentStyle;
		subp = PropertyDatabase.getInstance().getShorthandSubproperties(shorthandName);
	}

	BaseCSSStyleDeclaration getParentStyle() {
		return parentStyle;
	}

	String getShorthandName() {
		return shorthandName;
	}

	String[] getLonghandProperties() {
		return getLonghandProperties(getShorthandName());
	}

	String[] getLonghandProperties(String shorthandName) {
		return PropertyDatabase.getInstance().getLonghandProperties(shorthandName);
	}

	String[] getSubproperties() {
		return subp;
	}

	AbstractCSSValue getCSSValue(String propertyName) {
		return parentStyle.getCSSValue(propertyName);
	}

	AbstractCSSValue getInitialPropertyValue(String propertyName) {
		PropertyDatabase pdb = PropertyDatabase.getInstance();
		if (parentStyle instanceof ComputedCSSStyle) {
			return parentStyle.defaultPropertyValue(propertyName, pdb);
		}
		AbstractCSSValue defval = pdb.getInitialValue(propertyName);
		if (defval == null) {
			if (propertyName.equals("color")) {
				defval = SystemDefaultValue.getInstance();
			} else if (propertyName.equals("font-family")) {
				defval = SystemDefaultValue.getInstance();
			} else if (propertyName.equals("text-align")) {
				String directionValue = parentStyle.getPropertyValue("direction");
				if (directionValue.equals("rtl")) {
					defval = new IdentifierValue("right");
				} else {
					defval = new IdentifierValue("left");
				}
			} else if (propertyName.equals("background-color")) {
				defval = new IdentifierValue("transparent");
			} else if (propertyName.endsWith("-color")) {
				// Do not put getCSSColor() here, to avoid races with 'color'
				defval = new IdentifierValue("currentcolor");
			} else if (propertyName.equals("quotes")) {
				defval = parentStyle.getValueFactory().parseProperty("\" \"");
			}
		}
		return defval;
	}

	boolean isPropertyAssigned(String propertyName, boolean important) {
		return parentStyle.isPropertySet(propertyName, important);
	}

	boolean isPropertyAssigned(String property) {
		return ptySet.contains(property) || impPtySet.contains(property);
	}

	boolean isPropertyImportant(String property) {
		return impPtySet.contains(property);
	}

	void addAssignedProperty(String propertyName, boolean important) {
		if (important) {
			impPtySet.add(propertyName);
		} else {
			ptySet.add(propertyName);
		}
	}

	protected void appendIndividualProperties(StringBuilder buf) {
		Iterator<String> it = impPtySet.iterator();
		while (it.hasNext()) {
			String ptyname = it.next();
			buf.append(ptyname);
			buf.append(':').append(' ');
			BaseCSSStyleDeclaration.appendCssText(buf, getCSSValue(ptyname));
			buf.append(" ! important");
			buf.append(';').append(' ');
		}
		it = ptySet.iterator();
		while (it.hasNext()) {
			String ptyname = it.next();
			buf.append(ptyname);
			buf.append(':').append(' ');
			BaseCSSStyleDeclaration.appendCssText(buf, getCSSValue(ptyname));
			buf.append(';').append(' ');
		}
	}

	void appendMinifiedCssText(StringBuilder buf) {
		int len = buf.length();
		if (!appendShorthandText(buf)) {
			buf.setLength(len);
			appendMinifiedIndividualProperties(buf);
		}
	}

	protected void appendMinifiedIndividualProperties(StringBuilder buf) {
		appendImportantProperties(buf);
		appendNonImportantProperties(buf);
	}

	private void appendImportantProperties(StringBuilder buf) {
		for (String property : impPtySet) {
			buf.append(property).append(':');
			BaseCSSStyleDeclaration.appendMinifiedCssText(buf, getCSSValue(property), property);
			buf.append("!important;");
		}
	}

	private void appendNonImportantProperties(StringBuilder buf) {
		for (String property : ptySet) {
			buf.append(property).append(':');
			BaseCSSStyleDeclaration.appendMinifiedCssText(buf, getCSSValue(property), property);
			buf.append(';');
		}
	}

	boolean appendShorthandText(StringBuilder buf) {
		preprocessSet();
		int sz = getMinimumSetSize();
		if (getTotalSetSize() < sz) {
			return false;
		}
		return processPriorities(buf);
	}

	protected void preprocessSet() {
	}

	/**
	 * The size of the full set of properties that are available to build the shorthand,
	 * including properties of both important and normal priorities.
	 * 
	 * @return the size of the set of all available properties.
	 */
	protected int getTotalSetSize() {
		return impPtySet.size() + ptySet.size();
	}

	boolean hasPropertiesToExclude(Set<String> declaredSet) {
		Iterator<String> it = declaredSet.iterator();
		while (it.hasNext()) {
			String property = it.next();
			if (isPropertyToExclude(property)) {
				return true;
			}
		}
		return false;
	}

	boolean isPropertyToExclude(String property) {
		return isExcludedValue(getCSSValue(property));
	}

	boolean isExcludedValue(AbstractCSSValue cssValue) {
		short type = cssValue.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			return isExcludedType(((CSSPrimitiveValue) cssValue).getPrimitiveType());
		} else if (type == CSSValue.CSS_VALUE_LIST) {
			@SuppressWarnings("unchecked")
			ExtendedCSSValueList<AbstractCSSValue> list = (ExtendedCSSValueList<AbstractCSSValue>) cssValue;
			Iterator<AbstractCSSValue> it = list.iterator();
			while (it.hasNext()) {
				if (isExcludedValue(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	boolean isExcludedType(short primitiveType) {
		return primitiveType == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY;
	}

	boolean processPriorities(StringBuilder buf) {
		int sz = getMinimumSetSize();
		int impsz = impPtySet.size();
		if (impsz < sz) {
			// Declarations to be handled as non-important shorthand
			// with important individual properties.
			if (!appendShorthandSet(buf, ptySet, false)) {
				return false;
			}
			if (impsz != 0) {
				appendImportantProperties(buf);
			}
		} else {
			// Declarations to be handled as important shorthand
			// plus non-important shorthand.
			if (ptySet.size() != 0) {
				int len = buf.length();
				if (!appendShorthandSet(buf, ptySet, false)) {
					// Append individual non-important properties
					buf.setLength(len);
					appendNonImportantProperties(buf);
				}
			}
			if (!appendShorthandSet(buf, impPtySet, true)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Inefficient check for 'inherit' values.
	 * 
	 * @return 0 if no inherit was found, 1 if all values are inherit, 2 if both inherit and
	 *         non-inherit values were found.
	 */
	byte checkValuesForInherit(Set<String> declaredSet) {
		return checkValuesForInherit(getShorthandName(), declaredSet);
	}

	/**
	 * Inefficient check for 'inherit' values.
	 * 
	 * @param declaredSet the declared set.
	 * @param shorthand the shorthand name.
	 * @return 0 if no inherit was found within the declaredSet, 1 if all values are inherit, 2 if both inherit and
	 *         non-inherit values were found.
	 */
	byte checkValuesForInherit(String shorthand, Set<String> declaredSet) {
		byte count = 0, expect = 0;
		String[] properties = getLonghandProperties(shorthand);
		for (String propertyName : properties) {
			if (declaredSet.contains(propertyName)) {
				expect++;
				if (isInherit(getCSSValue(propertyName))) {
					count++;
				}
			}
		}
		if (count == 0) {
			return 0;
		} else if (count == expect) {
			return 1;
		}
		return 2;
	}

	/**
	 * Slightly better check for 'inherit' values.
	 * <p>
	 * Only works if the declaredSet only contains properties that are within the set
	 * that has to be tested.
	 * 
	 * @param declaredSet the declared set.
	 * @return 0 if no inherit was found within the declaredSet, 1 if all values are inherit, 2 if both inherit and
	 *         non-inherit values were found.
	 */
	byte checkDeclaredValuesForInherit(Set<String> declaredSet) {
		byte count = 0;
		for (String propertyName : declaredSet) {
			if (isInherit(getCSSValue(propertyName))) {
				count++;
			}
		}
		if (count == 0) {
			return 0;
		} else if (count == (byte) declaredSet.size()) {
			return 1;
		}
		return 2;
	}

	boolean isInherit(AbstractCSSValue value) {
		return value != null && value.getCssValueType() == CSSValue.CSS_INHERIT;
	}

	/**
	 * Check for keyword identifier values.
	 * <p>
	 * Only works if the declaredSet only contains properties that are within the set
	 * that has to be tested.
	 * @param keyword the keyword.
	 * @param declaredSet the declared set.
	 * 
	 * @return 0 if no keyword was found, 1 if all values are keyword, 2 if both keyword and
	 *         non-keyword values were found.
	 */
	byte checkDeclaredValuesForKeyword(String keyword, Set<String> declaredSet) {
		byte count = 0;
		for (String propertyName : declaredSet) {
			if (isCssKeywordValue(keyword, getCSSValue(propertyName))) {
				count++;
			}
		}
		if (count == 0) {
			return 0;
		} else if (count == (byte) declaredSet.size()) {
			return 1;
		}
		return 2;
	}

	/**
	 * Inefficient check for keyword identifier values.
	 * 
	 * @param keyword the keyword.
	 * @return 0 if no keyword was found, 1 if all values are keyword, 2 if both keyword and
	 *         non-keyword values were found.
	 */
	byte checkValuesForKeyword(String keyword, Set<String> declaredSet) {
		return checkValuesForKeyword(keyword, getShorthandName(), declaredSet);
	}

	/**
	 * Inefficient check for keyword identifier values.
	 * 
	 * @param keyword the keyword.
	 * @param shorthand the shorthand name.
	 * @return 0 if no keyword was found, 1 if all values are keyword, 2 if both keyword and
	 *         non-keyword values were found.
	 */
	byte checkValuesForKeyword(String keyword, String shorthand, Set<String> declaredSet) {
		byte count = 0, expect = 0;
		String[] properties = getLonghandProperties(shorthand);
		for (String propertyName : properties) {
			if (declaredSet.contains(propertyName)) {
				expect++;
				if (isCssKeywordValue(keyword, getCSSValue(propertyName))) {
					count++;
				}
			}
		}
		if (count == 0) {
			return 0;
		} else if (count == expect) {
			return 1;
		}
		return 2;
	}

	boolean isCssKeywordValue(String keyword, AbstractCSSValue cssValue) {
		return cssValue != null && cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) cssValue).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& keyword.equalsIgnoreCase(((CSSPrimitiveValue) cssValue).getStringValue());
	}

	String getValueTextIfNotInitial(String propertyName, AbstractCSSValue cssVal) {
		if (cssVal != null && !cssVal.isSystemDefault() && !isInitialIdentifier(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal)) {
			return cssVal.getMinifiedCssText(propertyName);
		}
		return null;
	}

	static boolean isInitialIdentifier(AbstractCSSValue cssVal) {
		return cssVal.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) cssVal).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& ((CSSPrimitiveValue) cssVal).getStringValue().toLowerCase(Locale.US).equals("initial");
	}

	boolean isInitialValue(String propertyName) {
		AbstractCSSValue cssVal = getCSSValue(propertyName);
		return cssVal.isSystemDefault() || isInitialIdentifier(cssVal)
				|| valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	protected boolean isNotInitialValue(AbstractCSSValue cssVal, String propertyName) {
		return cssVal != null && !cssVal.isSystemDefault() && !isInitialIdentifier(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	boolean containsControl(String ident) {
		int len = ident.length();
		for (int i = 0; i < len; i++) {
			char c = ident.charAt(i);
			if (Character.isISOControl(c)) {
				return true;
			}
		}
		return false;
	}

	boolean isUnknownIdentifier(String propertyName, AbstractCSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primi = (CSSPrimitiveValue) value;
			if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
				String s = primi.getStringValue();
				if (!"none".equalsIgnoreCase(s)) {
					PropertyDatabase pdb = PropertyDatabase.getInstance();
					return !pdb.isIdentifierValue(propertyName, s);
				}
			}
		} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			int len = list.getLength();
			for (int i = 0; i < len; i++) {
				if (isUnknownIdentifier(propertyName, list.item(i))) {
					return true;
				}
			}
		}
		return false;
	}

	boolean isImagePrimitiveValue(AbstractCSSPrimitiveValue primi) {
		short type = primi.getPrimitiveType();
		return type == CSSPrimitiveValue.CSS_URI || type == CSSPrimitiveValue2.CSS_GRADIENT
				|| (type == CSSPrimitiveValue2.CSS_FUNCTION && isImageFunction(primi))
				|| type == CSSPrimitiveValue2.CSS_ELEMENT_REFERENCE || type == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY;
	}

	private boolean isImageFunction(AbstractCSSPrimitiveValue primi) {
		String name = primi.getStringValue();
		return "image".equalsIgnoreCase(name) || "image-set".equalsIgnoreCase(name)
				|| "cross-fade".equalsIgnoreCase(name);
	}

	boolean valueEquals(AbstractCSSValue value1, AbstractCSSValue value2) {
		if (value2 == null) {
			if (value1 == null) {
				return true;
			}
			return false;
		} else if (value1 == null) {
			return false;
		}
		if (value2.isSystemDefault() != value1.isSystemDefault()) {
			return false;
		}
		if (value1.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE &&
				value2.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			AbstractCSSPrimitiveValue pvalue1 = (AbstractCSSPrimitiveValue) value1;
			short type1 = pvalue1.getPrimitiveType();
			AbstractCSSPrimitiveValue pvalue2 = (AbstractCSSPrimitiveValue) value2;
			short type2 = pvalue2.getPrimitiveType();
			if (type1 == CSSPrimitiveValue.CSS_IDENT) {
				if (type2 == CSSPrimitiveValue.CSS_RGBCOLOR) {
					return testColorIdentifier(pvalue2, pvalue1.getStringValue());
				} else if (type2 == CSSPrimitiveValue.CSS_IDENT) {
					return pvalue1.getStringValue().equalsIgnoreCase(pvalue2.getStringValue());
				}
			} else if (type1 == CSSPrimitiveValue.CSS_RGBCOLOR) {
				if (type2 == CSSPrimitiveValue.CSS_IDENT) {
					return testColorIdentifier(pvalue1, pvalue2.getStringValue());
				}
			}
		}
		return value1.equals(value2);
	}

	private boolean testColorIdentifier(AbstractCSSPrimitiveValue color, String ident) {
		String spec;
		if ("transparent".equals(ident)) {
			spec = "rgba(0,0,0,0)";
		} else {
			spec = ColorIdentifiers.getInstance().getColor(ident);
		}
		if (spec != null) {
			ValueFactory factory = new ValueFactory();
			try {
				CSSPrimitiveValue val = (CSSPrimitiveValue) factory.parseProperty(spec);
				return val.getRGBColorValue().equals(color.getRGBColorValue());
			} catch (DOMException e) {
			}
		}
		return false;
	}

	boolean appendValueIfNotInitial(StringBuilder buf, String propertyName, boolean prepend) {
		AbstractCSSValue cssVal = getCSSValue(propertyName);
		if (isNotInitialValue(cssVal, propertyName)) {
			if (prepend) {
				buf.append(' ');
			}
			buf.append(cssVal.getMinifiedCssText(propertyName));
			return true;
		}
		return prepend;
	}

	/**
	 * Append the priority to the buffer, if important. Adds a semicolon in any case.
	 * 
	 * @param buf
	 *            the buffer.
	 * @param important
	 *            true if the property is of important priority.
	 */
	void appendPriority(StringBuilder buf, boolean important) {
		if (important) {
			buf.append("!important;");
		} else {
			buf.append(';');
		}
	}

	boolean appendRelativeURI(StringBuilder buf, boolean prepend, AbstractCSSValue value) {
		String text;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue pvalue = (CSSPrimitiveValue) value;
			short type = pvalue.getPrimitiveType();
			if (type == CSSPrimitiveValue.CSS_URI) {
				URL url = ((URIValue) pvalue).getURLValue();
				if (url != null) {
					String baseuri;
					if ((baseuri = getBaseURI()) != null) {
						text = relativeURI(baseuri, url);
					} else {
						text = url.toExternalForm();
					}
					text = "url('" + text + "')";
				} else {
					text = pvalue.getCssText();
				}
			} else if (type == CSSPrimitiveValue.CSS_IDENT) {
				text = pvalue.getStringValue();
				if ("none".equalsIgnoreCase(text) || "initial".equalsIgnoreCase(text)) {
					return false;
				}
			} else {
				text = value.getMinifiedCssText("background-image");
			}
		} else {
			text = value.getMinifiedCssText("background-image");
		}
		if (prepend) {
			buf.append(' ');
		}
		buf.append(text);
		return true;
	}

	String getBaseURI() {
		BaseCSSDeclarationRule prule = getParentStyle().getParentRule();
		if (prule != null) {
			AbstractCSSStyleSheet psheet = prule.getParentStyleSheet();
			if (psheet != null) {
				return psheet.getHref();
			}
		}
		Node node = getParentStyle().getOwnerNode();
		Document document;
		String baseuri = null;
		if (node != null && (document = node.getOwnerDocument()) != null) {
			baseuri = document.getBaseURI();
		}
		return baseuri;
	}

	static String relativeURI(String baseuri, URL url) {
		String uri;
		try {
			URL base = new URL(baseuri);
			if (sameTree(base, url)) {
				try {
					return base.toURI().relativize(url.toURI()).toString();
				} catch (URISyntaxException e) {
				}
			}
		} catch (MalformedURLException e) {
		}
		uri = url.toExternalForm();
		return uri;
	}

	static boolean sameTree(URL base, URL url) {
		if (base.getProtocol().equals(url.getProtocol())) {
			String bhost = base.getHost();
			String uhost = url.getHost();
			if (bhost != null) {
				if (bhost.equals(uhost)) {
					int bport = base.getPort();
					int uport = url.getPort();
					if (bport == -1) {
						bport = base.getDefaultPort();
					}
					if (uport == -1) {
						uport = url.getDefaultPort();
					}
					return bport == uport;
				}
			} else {
				return uhost == null;
			}
		}
		return false;
	}

	/**
	 * Get the minimum number of shorthand properties that is require to buy a shorthand with
	 * this builder. If the declared properties are less than this, do not attempt to build a
	 * shorthand.
	 * <p>
	 * If this builder can form several shorthands, it refers to the smallest set.
	 * 
	 * @return the minimum number of shorthand properties that it takes to build a shorthand.
	 */
	protected int getMinimumSetSize() {
		return getLonghandProperties().length;
	}

	/**
	 * Use the given set of properties to append a shorthand serialization to the given
	 * buffer.
	 * 
	 * @param buf
	 *            the buffer.
	 * @param declaredSet
	 *            the set of declared properties relevant to build the shorthand. Other
	 *            properties could be ignored.
	 * @param important
	 *            true if the shorthand to build is of important priority.
	 * @return <code>true</code> if the shorthand was appended successfully.
	 */
	abstract boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important);

}
