/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.util.BufferSimpleWriter;

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
		if (freePropertyValue != null && freePropertyValue.getCssValueType() == CssType.TYPED
				&& freePropertyValue.getPrimitiveType() == CSSValue.Type.IDENT
				&& isNotInitialValue(freePropertyValue, freeProperty)) {
			this.freePropertyStringValue = ((CSSTypedValue) freePropertyValue).getStringValue();
		} else {
			this.freePropertyStringValue = null;
		}
	}

	@Override
	boolean invalidPrimitiveValueClash(Set<String> declaredSet, String propertyName, TypedValue primi) {
		return !freeProperty.equals(propertyName) && super.invalidPrimitiveValueClash(declaredSet, propertyName, primi);
	}

	@Override
	boolean identifierValuesAreKnown(String propertyName) {
		return !freeProperty.equals(propertyName) && super.identifierValuesAreKnown(propertyName);
	}

	@Override
	boolean appendValueText(BufferSimpleWriter wri, DeclarationFormattingContext context,
		String property, boolean appended) {
		StyleValue cssVal = getCSSValue(property);
		if (isNotInitialValue(cssVal, property)
			|| (!freeProperty.equals(property) && validValueClash(property))) {
			if (appended) {
				wri.getBuffer().append(' ');
			}
			try {
				context.writeMinifiedValue(wri, property, cssVal);
			} catch (IOException e) {
			}
			return true;
		}
		return appended;
	}

	boolean validValueClash(String property) {
		return freePropertyStringValue != null
				&& getShorthandDatabase().isIdentifierValue(property, freePropertyStringValue);
	}

}
