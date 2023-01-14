/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueList;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.URIValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Base class for shorthand builders, that try to build shorthands from a set of
 * individual properties.
 */
abstract class ShorthandBuilder {

	private static final ShorthandDatabase shorthandDb = ShorthandDatabase.getInstance();
	private final String shorthandName;
	private final BaseCSSStyleDeclaration parentStyle;
	private final String[] subp;
	private final Set<String> impPtySet = new TreeSet<String>();
	private final Set<String> ptySet = new TreeSet<String>();

	ShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super();
		this.shorthandName = shorthandName;
		this.parentStyle = parentStyle;
		subp = getShorthandDatabase().getShorthandSubproperties(shorthandName);
	}

	BaseCSSStyleDeclaration getParentStyle() {
		return parentStyle;
	}

	String getShorthandName() {
		return shorthandName;
	}

	static ShorthandDatabase getShorthandDatabase() {
		return shorthandDb;
	}

	String[] getLonghandProperties() {
		return getLonghandProperties(getShorthandName());
	}

	String[] getLonghandProperties(String shorthandName) {
		return getShorthandDatabase().getLonghandProperties(shorthandName);
	}

	String[] getSubproperties() {
		return subp;
	}

	StyleValue getCSSValue(String propertyName) {
		return parentStyle.getCSSValue(propertyName);
	}

	StyleValue getInitialPropertyValue(String propertyName) {
		if (parentStyle instanceof ComputedCSSStyle) {
			return parentStyle.defaultPropertyValue(propertyName);
		}
		StyleValue defval = PropertyDatabase.getInstance().getInitialValue(propertyName);
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

	boolean isPropertyAssigned(String propertyName) {
		return parentStyle.isPropertySet(propertyName);
	}

	boolean isPropertyAssigned(String propertyName, boolean important) {
		return parentStyle.isPropertySet(propertyName, important);
	}

	boolean isPropertyInAnySet(String property) {
		return ptySet.contains(property) || impPtySet.contains(property);
	}

	boolean isPropertyInImportantSet(String property) {
		return impPtySet.contains(property);
	}

	void addAssignedProperty(String propertyName, boolean important) {
		if (important) {
			impPtySet.add(propertyName);
		} else {
			ptySet.add(propertyName);
		}
	}

	boolean removeAssignedProperty(String property) {
		return ptySet.remove(property) || impPtySet.remove(property);
	}

	public void appendMinifiedCssText(StringBuilder buf) {
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

	void appendImportantProperties(StringBuilder buf) {
		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();

		int iniLen = buf.length();
		for (String property : impPtySet) {
			StyleValue value = getCSSValue(property);
			if (value.getPrimitiveType() != Type.INTERNAL) {
				buf.append(property).append(':');
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, value, property);
				buf.append("!important;");
			} else {
				buf.setLength(iniLen);
				appendImportantPropertiesWithInternal(buf);
				break;
			}
		}
	}

	private void appendImportantPropertiesWithInternal(StringBuilder buf) {
		HashSet<String> pendingSet = new HashSet<String>();
		HashSet<String> nonPendingSet = new HashSet<String>(impPtySet.size() - 1);
		for (String property : impPtySet) {
			StyleValue value = getCSSValue(property);
			if (value.getPrimitiveType() == Type.INTERNAL) {
				String shname = ((PendingValue) value).getShorthandName();
				if (pendingSet.add(shname) && (shname.equals(getShorthandName())
						|| isResponsibleShorthand(shname))) {
					ShorthandValue shval = (ShorthandValue) getCSSValue(shname);
					parentStyle.appendShorthandMinifiedCssText(buf, shname, shval);
				}
			} else {
				nonPendingSet.add(property);
			}
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		for (String property : nonPendingSet) {
			buf.append(property).append(':');
			BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, getCSSValue(property),
				property);
			buf.append("!important;");
		}
	}

	protected boolean isResponsibleShorthand(String shname) {
		return true;
	}

	void appendNonImportantProperties(StringBuilder buf) {
		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();

		int iniLen = buf.length();
		for (String property : ptySet) {
			StyleValue value = getCSSValue(property);
			if (value.getPrimitiveType() != Type.INTERNAL) {
				buf.append(property).append(':');
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, value, property);
				buf.append(';');
			} else {
				buf.setLength(iniLen);
				appendNonImportantPropertiesWithInternal(buf);
				break;
			}
		}
	}

	private void appendNonImportantPropertiesWithInternal(StringBuilder buf) {
		HashSet<String> pendingSet = new HashSet<String>();
		HashSet<String> nonPendingSet = new HashSet<String>(ptySet.size() - 1);
		for (String property : ptySet) {
			StyleValue value = getCSSValue(property);
			if (value.getPrimitiveType() == Type.INTERNAL) {
				String shname = ((PendingValue) value).getShorthandName();
				if (pendingSet.add(shname) && (shname.equals(getShorthandName())
						|| isResponsibleShorthand(shname))) {
					ShorthandValue shval = (ShorthandValue) getCSSValue(shname);
					parentStyle.appendShorthandMinifiedCssText(buf, shname, shval);
				}
			} else {
				nonPendingSet.add(property);
			}
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		for (String property : nonPendingSet) {
			buf.append(property).append(':');
			BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, getCSSValue(property),
				property);
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

	boolean isExcludedValue(StyleValue cssValue) {
		if (cssValue.isPrimitiveValue()) {
			return isExcludedType(cssValue.getPrimitiveType());
		} else if (cssValue.getCssValueType() == CssType.LIST) {
			@SuppressWarnings("unchecked")
			CSSValueList<StyleValue> list = (CSSValueList<StyleValue>) cssValue;
			Iterator<StyleValue> it = list.iterator();
			while (it.hasNext()) {
				if (isExcludedValue(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	boolean isExcludedType(Type type) {
		return type == Type.VAR;
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
			// Declarations to be handled as important shorthand (if there are enough values)
			// or longhands plus non-important shorthand.
			if (ptySet.size() != 0) {
				int len = buf.length();
				if (!appendShorthandSet(buf, ptySet, false)) {
					// Append individual non-important properties
					buf.setLength(len);
					appendNonImportantProperties(buf);
				}
			}
			return appendShorthandSet(buf, impPtySet, true);
		}
		return true;
	}

	/**
	 * Inefficient check for 'inherit' values.
	 * 
	 * @param declaredSet the declared set.
	 * @return 0 if no inherit was found, 1 if all values are inherit, 2 if both inherit and
	 *         non-inherit values were found.
	 */
	byte checkValuesForInherit(Set<String> declaredSet) {
		return checkValuesForInherit(getShorthandName(), declaredSet);
	}

	/**
	 * Inefficient check for 'inherit' values.
	 * 
	 * @param shorthand the shorthand name.
	 * @param declaredSet the declared set.
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

	boolean isInherit(StyleValue value) {
		return value != null && value.getPrimitiveType() == Type.INHERIT;
	}

	/**
	 * Check for keyword identifier values.
	 * <p>
	 * Only works if the declaredSet only contains properties that are within the
	 * set that has to be tested.
	 * 
	 * @param type        the type to look for.
	 * @param declaredSet the declared set.
	 * 
	 * @return 0 if no {@code type} was found, 1 if all values are {@code type}, 2
	 *         if both {@code type} and non-{@code type} values were found.
	 */
	byte checkDeclaredValuesForKeyword(Type type, Set<String> declaredSet) {
		byte count = 0;
		for (String propertyName : declaredSet) {
			StyleValue val = getCSSValue(propertyName);
			if (type == val.getPrimitiveType()) {
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
	 * Inefficient check for keyword values for the specified property set.
	 * 
	 * @param type the type to look for.
	 * @param important the priority set (important or not).
	 * @return 0 if no value of {@code type} was found, 1 if all values are
	 *         {@code type}, 2 if both {@code type} and non-{@code type} values were
	 *         found.
	 */
	byte checkValuesForType(Type type, boolean important) {
		Set<String> declaredSet;
		if (important) {
			declaredSet = impPtySet;
		} else {
			declaredSet = ptySet;
		}
		return checkValuesForType(type, getShorthandName(), declaredSet);
	}

	/**
	 * Inefficient check for keyword values.
	 * 
	 * @param type the type to look for.
	 * @param declaredSet the declared set.
	 * @return 0 if no value of {@code type} was found, 1 if all values are
	 *         {@code type}, 2 if both {@code type} and non-{@code type} values were
	 *         found.
	 */
	byte checkValuesForType(Type type, Set<String> declaredSet) {
		return checkValuesForType(type, getShorthandName(), declaredSet);
	}

	/**
	 * Inefficient check for keyword identifier values.
	 * 
	 * @param type      the type to look for.
	 * @param shorthand the shorthand name.
	 * @param declaredSet the declared set.
	 * @return 0 if no {@code type} was found, 1 if all values are {@code type}, 2
	 *         if both {@code type} and non-{@code type} values were found.
	 */
	byte checkValuesForType(Type type, String shorthand, Set<String> declaredSet) {
		byte count = 0, expect = 0;
		String[] properties = getLonghandProperties(shorthand);
		for (String propertyName : properties) {
			if (declaredSet.contains(propertyName)) {
				expect++;
				StyleValue cssValue = getCSSValue(propertyName);
				if (cssValue.getPrimitiveType() == type) {
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

	boolean isValueOfType(Type keyword, String propertyName) {
		StyleValue cssValue = getCSSValue(propertyName);
		return cssValue != null && cssValue.getPrimitiveType() == keyword;
	}

	static boolean isCssValueOfType(Type keyword, StyleValue cssValue) {
		return cssValue != null && cssValue.getPrimitiveType() == keyword;
	}

	String getValueTextIfNotInitial(String propertyName, StyleValue cssVal) {
		if (cssVal != null && !cssVal.isSystemDefault() && cssVal.getPrimitiveType() != Type.INITIAL
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal)) {
			return cssVal.getMinifiedCssText(propertyName);
		}
		return null;
	}

	boolean isInitialValue(String propertyName) {
		StyleValue cssVal = getCSSValue(propertyName);
		return cssVal.isSystemDefault() || isEffectiveInitialKeyword(cssVal)
				|| valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	protected boolean isNotInitialValue(StyleValue cssVal, String propertyName) {
		return cssVal != null && !cssVal.isSystemDefault() && !isEffectiveInitialKeyword(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	boolean isEffectiveInitialKeyword(StyleValue cssVal) {
		Type keyword = cssVal.getPrimitiveType();
		return keyword == Type.INITIAL || (!isInheritedProperty() && keyword == Type.UNSET);
	}

	boolean isInheritedProperty() {
		return false;
	}

	static boolean containsControl(String ident) {
		int len = ident.length();
		for (int i = 0; i < len; i++) {
			char c = ident.charAt(i);
			if (Character.isISOControl(c)) {
				return true;
			}
		}
		return false;
	}

	static boolean isIdentOrKeyword(StyleValue value) {
		if (value.getCssValueType() == CSSValue.CssType.LIST) {
			ValueList list = (ValueList) value;
			for (StyleValue item : list) {
				if (item.getPrimitiveType() != Type.IDENT) {
					return false;
				}
			}
			return true;
		} else {
			return value.getPrimitiveType() == Type.IDENT
				|| value.getCssValueType() == CSSValue.CssType.KEYWORD;
		}
	}

	static boolean isUnknownIdentifier(String propertyName, StyleValue value) {
		if (value.getCssValueType() == CssType.TYPED) {
			CSSTypedValue primi = (CSSTypedValue) value;
			if (primi.getPrimitiveType() == Type.IDENT) {
				String s = primi.getStringValue();
				if (!"none".equalsIgnoreCase(s)) {
					return !getShorthandDatabase().isIdentifierValue(propertyName, s);
				}
			}
		} else if (value.getCssValueType() == CssType.LIST) {
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

	/**
	 * Check whether a value is an image or image reference.
	 * 
	 * @param primi the value.
	 * @return false if it is not an image or image reference, or if the value is a
	 *         proxy that could be an image but not necessarily.
	 */
	static boolean isImagePrimitiveValue(TypedValue primi) {
		Type type = primi.getPrimitiveType();
		return type == Type.URI || type == Type.GRADIENT
			|| (type == Type.FUNCTION && isImageFunction(primi)) || type == Type.ELEMENT_REFERENCE;
	}

	private static boolean isImageFunction(TypedValue primi) {
		String name = primi.getStringValue();
		return "image".equalsIgnoreCase(name) || "image-set".equalsIgnoreCase(name)
				|| "cross-fade".equalsIgnoreCase(name);
	}

	boolean valueEquals(StyleValue value1, StyleValue value2) {
		if (value2 == null) {
			return value1 == null;
		} else if (value1 == null) {
			return false;
		}
		if (value2.isSystemDefault() != value1.isSystemDefault()) {
			return false;
		}
		if (value1.getCssValueType() == CssType.TYPED && value2.getCssValueType() == CssType.TYPED) {
			TypedValue pvalue1 = (TypedValue) value1;
			Type type1 = pvalue1.getPrimitiveType();
			TypedValue pvalue2 = (TypedValue) value2;
			Type type2 = pvalue2.getPrimitiveType();
			if (type1 == Type.IDENT) {
				if (type2 == Type.COLOR) {
					return testColorIdentifier(pvalue2, pvalue1.getStringValue().toLowerCase(Locale.ROOT));
				} else if (type2 == Type.IDENT) {
					return pvalue1.getStringValue().equalsIgnoreCase(pvalue2.getStringValue());
				}
			} else if (type1 == Type.COLOR) {
				if (type2 == Type.IDENT) {
					return testColorIdentifier(pvalue1, pvalue2.getStringValue());
				}
			}
		}
		return value1.equals(value2);
	}

	private boolean testColorIdentifier(TypedValue color, String ident) {
		String spec;
		if ("transparent".equals(ident)) {
			spec = "rgba(0,0,0,0)";
		} else {
			spec = ColorIdentifiers.getInstance().getColor(ident);
		}
		if (spec != null) {
			ValueFactory factory = new ValueFactory();
			try {
				CSSTypedValue val = (CSSTypedValue) factory.parseProperty(spec);
				return val.toRGBColor().equals(color.toRGBColor());
			} catch (DOMException e) {
			}
		}
		return false;
	}

	boolean appendValueIfNotInitial(BufferSimpleWriter wri, DeclarationFormattingContext context,
		String propertyName, boolean prepend) {
		StringBuilder buf = wri.getBuffer();
		StyleValue cssVal = getCSSValue(propertyName);
		if (isNotInitialValue(cssVal, propertyName)) {
			if (prepend) {
				buf.append(' ');
			}
			try {
				context.writeMinifiedValue(wri, propertyName, cssVal);
			} catch (IOException e) {
			}
			return true;
		}
		return prepend;
	}

	boolean appendDeclarationIfNotInitial(BufferSimpleWriter wri,
		DeclarationFormattingContext context, String propertyName, boolean importantShorthand) {
		StyleValue cssVal = getCSSValue(propertyName);
		boolean impPty = "important"
			.equalsIgnoreCase(parentStyle.getPropertyPriority(propertyName));
		if (isNotInitialValue(cssVal, propertyName) && impPty == importantShorthand) {
			StringBuilder buf = wri.getBuffer();
			buf.append(propertyName).append(':');
			try {
				context.writeMinifiedValue(wri, propertyName, cssVal);
			} catch (IOException e) {
			}
			// Serialize priority
			if (impPty) {
				buf.append('!').append("important");
			}
			buf.append(';');
			return true;
		}
		return false;
	}

	boolean appendDeclarationIfNotKeyword(Type keyword, BufferSimpleWriter wri,
		DeclarationFormattingContext context, String propertyName, boolean importantShorthand) {
		StyleValue cssVal = getCSSValue(propertyName);
		boolean impPty = "important"
			.equalsIgnoreCase(parentStyle.getPropertyPriority(propertyName));
		if (!isCssValueOfType(keyword, cssVal) && impPty == importantShorthand) {
			StringBuilder buf = wri.getBuffer();
			buf.append(propertyName).append(':');
			try {
				context.writeMinifiedValue(wri, propertyName, cssVal);
			} catch (IOException e) {
			}
			// Serialize priority
			if (impPty) {
				buf.append('!').append("important");
			}
			buf.append(';');
			return true;
		}
		return false;
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

	boolean appendImage(StringBuilder buf, boolean prepend, StyleValue value) {
		// Serialize value as per the formatting context
		StringBuilder sb = new StringBuilder(64);
		BufferSimpleWriter wri = new BufferSimpleWriter(sb);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		String text;
		CssType category = value.getCssValueType();
		if (category == CssType.TYPED) {
			CSSTypedValue pvalue = (CSSTypedValue) value;
			Type type = pvalue.getPrimitiveType();
			if (type == Type.URI) {
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
					try {
						context.writeMinifiedValue(wri, "background-image", value);
					} catch (IOException e) {
					}
					text = sb.toString();
				}
			} else if (type == Type.IDENT) {
				text = pvalue.getStringValue();
				if ("none".equalsIgnoreCase(text)) {
					return false;
				}
			} else {
				try {
					context.writeMinifiedValue(wri, "background-image", value);
				} catch (IOException e) {
				}
				text = sb.toString();
			}
		} else if (category == CssType.KEYWORD) {
			// assume 'initial' or 'unset' with no inheritance
			return false;
		} else {
			try {
				context.writeMinifiedValue(wri, "background-image", value);
			} catch (IOException e) {
			}
			text = sb.toString();
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
