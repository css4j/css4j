/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;

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
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return 1;
		}

		// Append property name
		buf.append(getShorthandName()).append(':');

		// Check for CSS-wide keywords
		byte check = checkValuesForInherit(declaredSet);
		if (check == 1) {
			// All values are inherit
			buf.append("inherit");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2) {
			return 1;
		}

		// Unset
		if (isInheritedProperty()) {
			check = checkValuesForType(CSSValue.Type.UNSET, declaredSet);
			if (check == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return 0;
			} else if (check == 2) {
				return 1;
			}
		}

		// Revert
		check = checkValuesForType(CSSValue.Type.REVERT, declaredSet);
		if (check == 1) {
			// All values are revert
			buf.append("revert");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2) {
			return 1;
		}

		// pending value check
		if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
			return 1;
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		boolean appended = false;
		String[] subp = getLonghandProperties();
		for (String property : subp) {
			if (declaredSet.contains(property) && !isResetProperty(property)) {
				// First, make sure that it is not a layered property
				StyleValue cssVal = getCSSValue(property);
				if ((cssVal.getCssValueType() == CssType.LIST &&
						((ValueList) cssVal).isCommaSeparated()) ||
						invalidValueClash(declaredSet, property, cssVal)) {
					return 1;
				}
				appended = appendValueText(wri, context, property, appended);
			}
		}

		if (!appended) {
			buf.append(initialvalue);
		}

		appendPriority(buf, important);

		endShorthandSerialization(wri, context, important);

		return 0;
	}

	/**
	 * Check if the property is reset but not serialized.
	 * 
	 * @param property the property name.
	 * @return true if the property is not serialized but only reset.
	 */
	boolean isResetProperty(String property) {
		return false;
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
			for (String property : declaredSet) {
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

	boolean appendValueText(BufferSimpleWriter wri, DeclarationFormattingContext context,
		String property, boolean appended) {
		return appendValueIfNotInitial(wri, context, property, appended);
	}

	@Override
	boolean isInheritedProperty() {
		String ptyname = getLonghandProperties()[0];
		return PropertyDatabase.getInstance().isInherited(ptyname);
	}

	void endShorthandSerialization(BufferSimpleWriter wri, DeclarationFormattingContext context,
			boolean importantShorthand) {
	}

}
