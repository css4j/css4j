/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a simple shorthand from individual properties.
 */
class GenericShorthandBuilder extends ShorthandBuilder {

	final String initialvalue;

	GenericShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle, String initialvalue) {
		super(shorthandName, parentStyle);
		this.initialvalue = initialvalue;
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		// Append property name
		buf.append(getShorthandName()).append(':');
		// Check for CSS-wide keywords
		byte check = checkValuesForInherit(declaredSet);
		if (check == 1) {
			// All values are inherit
			buf.append("inherit");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		// Unset
		if (isInheritedProperty()) {
			check = checkValuesForType(CSSValue.Type.UNSET, declaredSet);
			if (check == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return true;
			} else if (check == 2) {
				return false;
			}
		}
		// Revert
		check = checkValuesForType(CSSValue.Type.REVERT, declaredSet);
		if (check == 1) {
			// All values are revert
			buf.append("revert");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		// pending value check
		if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
			return false;
		}
		boolean appended = false;
		String[] subp = getLonghandProperties();
		for (int i = 0; i < subp.length; i++) {
			String property = subp[i];
			if (declaredSet.contains(property)) {
				// First, make sure that it is not a layered property
				StyleValue cssVal = getCSSValue(property);
				if ((cssVal.getCssValueType() == CssType.LIST &&
						((ValueList) cssVal).isCommaSeparated()) || 
						invalidValueClash(declaredSet, property, cssVal)) {
					return false;
				}
				appended = appendValueText(buf, property, appended);
			}
		}
		if (!appended) {
			buf.append(initialvalue);
		}
		appendPriority(buf, important);
		return true;
	}

	/**
	 * Determine whether any value has an invalid value, or one that could clash
	 * with others.
	 * 
	 * @param declaredSet the declared set.
	 * @param property    the longhand property name.
	 * @param cssVal      the value of that property
	 * @return <code>true</code> if the shorthand should not be built.
	 */
	boolean invalidValueClash(Set<String> declaredSet, String property, StyleValue cssVal) {
		if (cssVal.getCssValueType() == CssType.LIST) {
			ValueList list = (ValueList) cssVal;
			int len = list.getLength();
			for (int i = 0; i < len; i++) {
				if (invalidValueClash(declaredSet, property, list.item(i))) {
					return true;
				}
			}
		} else if (cssVal.getCssValueType() == CssType.TYPED) {
			return invalidPrimitiveValueClash(declaredSet, property, (TypedValue) cssVal);
		} else if (cssVal.getCssValueType() != CssType.KEYWORD) {
			// Problematic keywords have been filtered out already.
			return true;
		}
		return false;
	}

	boolean invalidPrimitiveValueClash(Set<String> declaredSet, String propertyName, TypedValue primi) {
		if (primi.getPrimitiveType() == CSSValue.Type.IDENT) {
			return invalidIdentValueClash(declaredSet, propertyName, primi);
		}
		return false;
	}

	boolean invalidIdentValueClash(Set<String> declaredSet, String propertyName, TypedValue primi) {
		String ident = primi.getStringValue().toLowerCase(Locale.ROOT);
		if (!getShorthandDatabase().isIdentifierValue(propertyName, ident) && !ident.equals(initialvalue)) {
			if (identifierValuesAreKnown(propertyName) || containsControl(ident)) {
				return true; // Invalid value
			}
			Iterator<String> it = declaredSet.iterator();
			while (it.hasNext()) {
				String property = it.next();
				if (getShorthandDatabase().hasKnownIdentifierValues(property)
						&& getShorthandDatabase().isIdentifierValue(property, ident)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean identifierValuesAreKnown(String propertyName) {
		return getShorthandDatabase().hasKnownIdentifierValues(propertyName);
	}

	boolean appendValueText(StringBuilder buf, String property, boolean appended) {
		return appendValueIfNotInitial(buf, property, appended);
	}

	@Override
	boolean isInheritedProperty() {
		String ptyname = getLonghandProperties()[0];
		return PropertyDatabase.getInstance().isInherited(ptyname);
	}

}
