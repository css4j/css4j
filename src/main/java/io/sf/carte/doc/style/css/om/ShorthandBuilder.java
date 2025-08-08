/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueList;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.impl.CSSUtil;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
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
	private final Set<String> impPtySet = new TreeSet<>();
	private final Set<String> ptySet = new TreeSet<>();

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
				defval = parentStyle.getValueFactory().parseProperty("#0000");
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
		switch (appendShorthandText(buf)) {
		case 1:
			buf.setLength(len);
			// pass-through
		case 2:
			appendMinifiedIndividualProperties(buf);
		}
	}

	protected void appendMinifiedIndividualProperties(StringBuilder buf) {
		appendPropertiesInSet(buf, impPtySet, true);
		appendPropertiesInSet(buf, ptySet, false);
	}

	void appendPropertiesInSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();

		int iniLen = buf.length();
		for (String property : declaredSet) {
			StyleValue value = getCSSValue(property);
			if (value.getPrimitiveType() != Type.INTERNAL) {
				buf.append(property).append(':');
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, value, property);
				appendPriority(buf, important);
			} else {
				buf.setLength(iniLen);
				appendPropertiesWithInternal(wri, context, declaredSet, important);
				break;
			}
		}
	}

	private void appendPropertiesWithInternal(BufferSimpleWriter wri,
			DeclarationFormattingContext context, Set<String> declaredSet, boolean important) {
		StringBuilder buf = wri.getBuffer();
		HashSet<String> pendingSet = new HashSet<>();
		HashSet<String> nonPendingSet = new HashSet<>(declaredSet.size() - 1);
		for (String property : declaredSet) {
			StyleValue value = getCSSValue(property);
			if (value.getPrimitiveType() == Type.INTERNAL) {
				String shname = ((PendingSubstitutionValue) value).getShorthandName();
				if (pendingSet.add(shname)
						&& (shname.equals(getShorthandName()) || isResponsibleShorthand(shname))) {
					ShorthandValue shval = (ShorthandValue) getCSSValue(shname);
					parentStyle.appendShorthandMinifiedCssText(buf, shname, shval);
				}
			} else {
				nonPendingSet.add(property);
			}
		}

		for (String property : nonPendingSet) {
			StyleValue value = getCSSValue(property);
			buf.append(property).append(':');
			BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, value, property);
			appendPriority(buf, important);
		}
	}

	protected boolean isResponsibleShorthand(String shname) {
		return true;
	}

	/**
	 * Serialize the shorthand into the given buffer.
	 * 
	 * @param buf the buffer.
	 * @return <code>0</code> if the shorthand was appended successfully,
	 *         <code>1</code> if could not serialize, <code>2</code> if execution
	 *         should proceed like the serialization did not succeed (longhand
	 *         properties should be appended), but the buffer must not be reset.
	 */
	int appendShorthandText(StringBuilder buf) {
		preprocessSet();
		int sz = getMinimumSetSize();
		if (getTotalSetSize() < sz) {
			return 1;
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
		for (String property : declaredSet) {
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
			for (StyleValue item : list) {
				if (isExcludedValue(item)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean isExcludedType(Type type) {
		return type == Type.LEXICAL;
	}

	int processPriorities(StringBuilder buf) {
		int sz = getMinimumSetSize();
		int impsz = impPtySet.size();
		if (impsz < sz) {
			// Declarations to be handled as non-important shorthand
			// with important individual properties.
			int ret = appendShorthandSet(buf, ptySet, false);
			if (ret != 0) {
				return ret;
			}
			if (impsz != 0) {
				appendPropertiesInSet(buf, impPtySet, true);
			}
		} else {
			// Declarations to be handled as important shorthand (if there are enough values)
			// or longhands plus non-important shorthand.
			if (ptySet.size() != 0) {
				int len = buf.length();
				int ret = appendShorthandSet(buf, ptySet, false);
				switch (ret) {
				case 0:
					ptySet.clear();
					break;
				case 1:
					buf.setLength(len);
					appendPropertiesInSet(buf, ptySet, false);
				}
			}
			return appendShorthandSet(buf, impPtySet, true);
		}
		return 0;
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
		CSSValueSyntax syn = SyntaxParser.createSimpleSyntax("image");
		return primi.matches(syn) == Match.TRUE
				|| (primi.getPrimitiveType() == Type.FUNCTION && isImageFunction(primi));
	}

	private static boolean isImageFunction(TypedValue primi) {
		String name = primi.getStringValue().toLowerCase(Locale.ROOT);
		return CSSUtil.isUnimplementedImageFunction(name);
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
		if (value1.getCssValueType() == CssType.TYPED) {
			if (value2.getCssValueType() == CssType.TYPED) {
				TypedValue pvalue1 = (TypedValue) value1;
				Type type1 = pvalue1.getPrimitiveType();
				TypedValue pvalue2 = (TypedValue) value2;
				Type type2 = pvalue2.getPrimitiveType();
				if (type1 == Type.IDENT) {
					if (type2 == Type.COLOR) {
						return testColorIdentifier(pvalue2,
								pvalue1.getStringValue().toLowerCase(Locale.ROOT));
					} else if (type2 == Type.IDENT) {
						return pvalue1.getStringValue().equalsIgnoreCase(pvalue2.getStringValue());
					}
				} else if (type1 == Type.COLOR && type2 == Type.IDENT) {
					return testColorIdentifier(pvalue1, pvalue2.getStringValue());
				}
			}
		} else if (value1.getCssValueType() == CssType.LIST
				&& value2.getCssValueType() == CssType.LIST) {
			ValueList list1 = (ValueList) value1;
			ValueList list2 = (ValueList) value2;
			int len = list1.getLength();
			if (len != list2.getLength()) {
				return false;
			}
			for (int i = 0; i < len; i++) {
				if (!valueEquals(list1.item(i), list2.item(i))) {
					return false;
				}
			}
		}
		return value1.equals(value2);
	}

	private boolean testColorIdentifier(TypedValue color, String ident) {
		String spec;
		if ("transparent".equals(ident)) {
			spec = "#0000";
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
		CSSDeclarationRule prule = getParentStyle().getParentRule();
		if (prule != null) {
			CSSStyleSheet<? extends CSSRule> psheet = prule.getParentStyleSheet();
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
			URI base = new URI(baseuri);
			if (sameTree(base, url)) {
				try {
					return base.relativize(url.toURI()).toString();
				} catch (Exception e) {
				}
			}
		} catch (URISyntaxException e) {
		}
		uri = url.toExternalForm();
		return uri;
	}

	static boolean sameTree(URI base, URL url) {
		if (url.getProtocol().equals(base.getScheme())) {
			String bhost = base.getHost();
			String uhost = url.getHost();
			if (bhost != null) {
				if (bhost.equals(uhost)) {
					int bport = base.getPort();
					int uport = url.getPort();
					if (bport == -1) {
						// Same scheme, so same default port for both
						bport = url.getDefaultPort();
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
	 * Use the given set of properties to append a shorthand serialization to the
	 * given buffer.
	 * 
	 * @param buf         the buffer.
	 * @param declaredSet the set of declared properties relevant to build the
	 *                    shorthand. Other properties could be ignored.
	 * @param important   true if the shorthand to build is of important priority.
	 * @return <code>0</code> if the shorthand was appended successfully,
	 *         <code>1</code> if could not serialize, <code>2</code> if execution
	 *         should proceed like the serialization did not succeed, but the buffer
	 *         must not be reset and normal-priority properties should not be
	 *         appended.
	 */
	abstract int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important);

}
