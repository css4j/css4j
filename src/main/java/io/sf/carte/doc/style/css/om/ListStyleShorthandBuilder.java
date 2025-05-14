/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.property.TypedValue;

/**
 * Build a 'list-style' shorthand from individual properties.
 */
class ListStyleShorthandBuilder extends OrderedShorthandBuilder {

	ListStyleShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("list-style", parentStyle, "disc", "list-style-type");
	}

	@Override
	boolean invalidPrimitiveValueClash(Set<String> declaredSet, String propertyName, TypedValue primi) {
		CSSTypedValue.Type type = primi.getPrimitiveType();
		if (type == CSSValue.Type.IDENT) {
			return invalidIdentValueClash(declaredSet, propertyName, primi);
		} else if ("list-style-image".equals(propertyName)) {
			return !isImagePrimitiveValue(primi);
		}
		return false;
	}

	@Override
	boolean isExcludedType(CSSTypedValue.Type primitiveType) {
		return false;
	}

}
