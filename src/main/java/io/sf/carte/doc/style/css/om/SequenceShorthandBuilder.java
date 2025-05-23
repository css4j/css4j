/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Build a shorthand from individual properties that follow a sequence pattern.
 */
class SequenceShorthandBuilder extends ShorthandBuilder {

	SequenceShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super(shorthandName, parentStyle);
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
		if (isInheritedProperty()) {
			// Unset
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

		// Check for properties with more than one value
		String[] subp = getSubproperties();
		for (String property : subp) {
			if (declaredSet.contains(property)) {
				StyleValue cssVal = getCSSValue(property);
				if (cssVal.getCssValueType() == CssType.LIST) {
					return 1;
				}
			}
		}

		// Now append value(s)
		boolean appended = false;
		StyleValue cssVal0 = null;
		if (declaredSet.contains(subp[0])) {
			cssVal0 = getCSSValue(subp[0]);
			if (!isNotInitialValue(cssVal0, subp[0])) {
				// Make sure that we have a typed initial value and not a keyword
				cssVal0 = getInitialPropertyValue(subp[0]);
			}
			String text = cssVal0.getMinifiedCssText(subp[0]);
			buf.append(text);
			appended = true;
		}

		if (declaredSet.contains(subp[1])) {
			StyleValue cssVal1 = getCSSValue(subp[1]);
			if (!isNotInitialValue(cssVal1, subp[1])) {
				// Make sure that we have a typed initial value and not a keyword
				cssVal1 = getInitialPropertyValue(subp[1]);
			}
			if (!valueEquals(cssVal0, cssVal1)) {
				if (appended) {
					buf.append(' ');
				}
				buf.append(cssVal1.getMinifiedCssText(subp[1]));
			}
		}

		appendPriority(buf, important);

		return 0;
	}

}
