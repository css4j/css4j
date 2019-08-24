/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a simple shorthand from individual properties.
 */
class GenericShorthandBuilder extends ShorthandBuilder {

	final String initialvalue;
	final PropertyDatabase pdb = PropertyDatabase.getInstance();

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
		check = checkValuesForKeyword("unset", declaredSet);
		if (check == 1) {
			// All values are unset
			buf.append("unset");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		boolean appended = false;
		String[] subp = getLonghandProperties();
		for (int i = 0; i < subp.length; i++) {
			String property = subp[i];
			if (declaredSet.contains(property)) {
				// First, make sure that it is not a layered property
				AbstractCSSValue cssVal = getCSSValue(property);
				if ((cssVal.getCssValueType() == CSSValue.CSS_VALUE_LIST &&
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
	boolean invalidValueClash(Set<String> declaredSet, String property, AbstractCSSValue cssVal) {
		if (cssVal.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) cssVal;
			int len = list.getLength();
			for (int i = 0; i < len; i++) {
				if (invalidValueClash(declaredSet, property, list.item(i))) {
					return true;
				}
			}
		} else if (cssVal.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			return invalidPrimitiveValueClash(declaredSet, property, (AbstractCSSPrimitiveValue) cssVal);
		} else {
			return true;
		}
		return false;
	}

	boolean invalidPrimitiveValueClash(Set<String> declaredSet, String propertyName, AbstractCSSPrimitiveValue primi) {
		if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			return invalidIdentValueClash(declaredSet, propertyName, primi);
		}
		return false;
	}

	boolean invalidIdentValueClash(Set<String> declaredSet, String propertyName, AbstractCSSPrimitiveValue primi) {
		String ident = primi.getStringValue().toLowerCase(Locale.ROOT);
		if (!pdb.isIdentifierValue(propertyName, ident) && !ident.equals(initialvalue) && !ident.equals("initial")) {
			if (identifierValuesAreKnown(propertyName) || containsControl(ident)) {
				return true; // Invalid value
			}
			Iterator<String> it = declaredSet.iterator();
			while (it.hasNext()) {
				String property = it.next();
				if (pdb.hasKnownIdentifierValues(property) && pdb.isIdentifierValue(property, ident)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean identifierValuesAreKnown(String propertyName) {
		return pdb.hasKnownIdentifierValues(propertyName);
	}

	boolean appendValueText(StringBuilder buf, String property, boolean appended) {
		return appendValueIfNotInitial(buf, property, appended);
	}

}
