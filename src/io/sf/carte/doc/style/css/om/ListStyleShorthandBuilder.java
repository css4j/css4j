/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.property.PrimitiveValue;

/**
 * Build a 'list-style' shorthand from individual properties.
 */
class ListStyleShorthandBuilder extends OrderedShorthandBuilder {

	ListStyleShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("list-style", parentStyle, "disc", "list-style-type");
	}

	@Override
	boolean invalidPrimitiveValueClash(Set<String> declaredSet, String propertyName, PrimitiveValue primi) {
		short type = primi.getPrimitiveType();
		if (type == CSSPrimitiveValue.CSS_IDENT) {
			return invalidIdentValueClash(declaredSet, propertyName, primi);
		} else if ("list-style-image".equals(propertyName)) {
			return !isImagePrimitiveValue(primi);
		}
		return false;
	}

	@Override
	boolean isExcludedType(short primitiveType) {
		return false;
	}

}
