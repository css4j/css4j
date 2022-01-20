/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Build a 'transition' shorthand from individual properties.
 */
class TransitionShorthandBuilder extends ListOrderedShorthandBuilder {

	TransitionShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("transition", parentStyle, "0s", "transition-property");
	}

	@Override
	boolean valueClash(int index, String property) {
		StyleValue freePropertyValue = getCSSListItemValue(freeProperty, index);
		short freeType = freePropertyValue.getCssValueType();
		boolean retval = false;
		if (freeType == CSSValue.CSS_PRIMITIVE_VALUE) {
			retval = isConflictingIdentifier(property, (CSSPrimitiveValue) freePropertyValue);
		} else if (freeType == CSSValue.CSS_VALUE_LIST) {
			retval = listHasConflictingIdentifiers(property, (CSSValueList) freePropertyValue);
		}
		if (!retval && property.equals("transition-duration")) {
			StyleValue delay = getCSSListItemValue("transition-delay", index);
			if (isNotInitialValue(delay, "transition-delay")) {
				retval = true;
			}
		}
		return retval;
	}

	@Override
	boolean validValueClash(String property) {
		boolean retval = super.validValueClash(property);
		if (!retval && property.equals("transition-duration")) {
			StyleValue delay = getCSSValue("transition-delay");
			if (isNotInitialValue(delay, "transition-delay")) {
				retval = true;
			}
		}
		return retval;
	}

}
