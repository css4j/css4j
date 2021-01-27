/*

 Copyright (c) 2005-2021, Carlos Amengual.

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
 * Build an 'animation' shorthand from individual properties.
 */
class AnimationShorthandBuilder extends ListOrderedShorthandBuilder {

	AnimationShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("animation", parentStyle, "none", "animation-name");
	}

	@Override
	boolean valueClash(int index, String property) {
		String chkProperty = property;
		if ("animation-timing-function".equals(property)) {
			chkProperty = "transition-timing-function";
		}
		StyleValue freePropertyValue = getCSSListItemValue(freeProperty, index);
		short freeType = freePropertyValue.getCssValueType();
		boolean retval = false;
		if (freeType == CSSValue.CSS_PRIMITIVE_VALUE) {
			retval = isConflictingIdentifier(chkProperty, (CSSPrimitiveValue) freePropertyValue);
		} else if (freeType == CSSValue.CSS_VALUE_LIST) {
			retval = listHasConflictingIdentifiers(chkProperty, (CSSValueList) freePropertyValue);
		}
		if (!retval && property.equals("animation-duration")) {
			StyleValue delay = getCSSListItemValue("animation-delay", index);
			if (isNotInitialValue(delay, "animation-delay")) {
				retval = true;
			}
		}
		return retval;
	}

	@Override
	boolean validValueClash(String property) {
		String chkProperty = property;
		if ("animation-timing-function".equals(property)) {
			chkProperty = "transition-timing-function";
		}
		// Make sure that 'none' is in animation-fill-mode list in 'identifier.properties'
		boolean retval = super.validValueClash(chkProperty);
		if (!retval && property.equals("animation-duration")) {
			StyleValue delay = getCSSValue("animation-delay");
			if (isNotInitialValue(delay, "animation-delay")) {
				retval = true;
			}
		}
		return retval;
	}

}
