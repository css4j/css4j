/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Build a shorthand from individual properties when specific order matters, and the
 * final property (<code>freeProperty</code>) can take custom identifier values that may belong to others.
 */
class OrderedShorthandBuilder extends GenericShorthandBuilder {

	final String freeProperty;
	private final String freePropertyStringValue;

	OrderedShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle, String initialvalue,
			String freeProperty) {
		super(shorthandName, parentStyle, initialvalue);
		this.freeProperty = freeProperty.toLowerCase(Locale.ROOT);
		StyleValue freePropertyValue = getCSSValue(freeProperty);
		if (freePropertyValue != null && freePropertyValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE && 
				((CSSPrimitiveValue) freePropertyValue).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			this.freePropertyStringValue = ((CSSPrimitiveValue) freePropertyValue).getStringValue();
		} else {
			this.freePropertyStringValue = null;
		}
	}

	@Override
	boolean invalidPrimitiveValueClash(Set<String> declaredSet, String propertyName, PrimitiveValue primi) {
		return !freeProperty.equals(propertyName) && super.invalidPrimitiveValueClash(declaredSet, propertyName, primi);
	}

	@Override
	boolean identifierValuesAreKnown(String propertyName) {
		return !freeProperty.equals(propertyName) && super.identifierValuesAreKnown(propertyName);
	}

	@Override
	boolean appendValueText(StringBuilder buf, String property, boolean appended) {
		StyleValue cssVal = getCSSValue(property);
		if (isNotInitialValue(cssVal, property) ||
				(!freeProperty.equals(property) && validValueClash(property))) {
			if (appended) {
				buf.append(' ');
			}
			buf.append(cssVal.getMinifiedCssText(property));
			return true;
		}
		return appended;
	}

	boolean validValueClash(String property) {
		return freePropertyStringValue != null
				&& getShorthandDatabase().isIdentifierValue(property, freePropertyStringValue);
	}

}
