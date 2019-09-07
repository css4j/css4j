/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Build a shorthand from individual properties that follow a sequence pattern.
 */
class SequenceShorthandBuilder extends ShorthandBuilder {

	SequenceShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super(shorthandName, parentStyle);
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
		// Check for properties with more than one value
		String[] subp = getSubproperties();
		for (int i = 0; i < subp.length; i++) {
			if (declaredSet.contains(subp[i])) {
				StyleValue cssVal = getCSSValue(subp[i]);
				if (cssVal.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
					return false;
				}
			}
		}
		// Now append value(s)
		StyleValue cssVal0 = null;
		if (declaredSet.contains(subp[0])) {
			cssVal0 = getCSSValue(subp[0]);
			buf.append(cssVal0.getMinifiedCssText(subp[0]));
		}
		if (declaredSet.contains(subp[1])) {
			StyleValue cssVal1 = getCSSValue(subp[1]);
			if (!valueEquals(cssVal0, cssVal1)) {
				buf.append(cssVal1.getMinifiedCssText(subp[1]));
			}
		}
		appendPriority(buf, important);
		return true;
	}

}
