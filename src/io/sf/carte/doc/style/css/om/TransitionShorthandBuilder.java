/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

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
		CssType freeType = freePropertyValue.getCssValueType();
		boolean retval = false;
		if (freeType == CssType.TYPED) {
			retval = isConflictingIdentifier(property, (CSSTypedValue) freePropertyValue);
		} else if (freeType == CssType.LIST) {
			retval = listHasConflictingIdentifiers(property, (ValueList) freePropertyValue);
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
