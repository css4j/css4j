/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a grid placement shorthand from individual properties.
 */
class GridPlacementShorthandBuilder extends ShorthandBuilder {

	GridPlacementShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super(shorthandName, parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 2;
	}

	/*
	 * If two <grid-line> values are specified, the grid-row-start/grid-column-start longhand
	 * is set to the value before the slash, and the grid-row-end/grid-column-end longhand is
	 * set to the value after the slash.
	 *
	 * When the second value is omitted, if the first value is a <custom-ident>, the
	 * grid-row-end/grid-column-end longhand is also set to that <custom-ident>; otherwise, it
	 * is set to auto.
	 */
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
		String[] subp = getSubproperties();
		// Make sure that it is not a layered property
		StyleValue cssVal0 = getCSSValue(subp[0]);
		if (cssVal0.getCssValueType() == CSSValue.CSS_VALUE_LIST && ((ValueList) cssVal0).isCommaSeparated()) {
			return false;
		}
		appendValueText(buf, cssVal0);
		if (subp.length == 2) {
			StyleValue cssVal = getCSSValue(subp[1]);
			if (isPrintValue(subp[1], cssVal, cssVal0)) {
				buf.append('/');
				appendValueText(buf, cssVal);
			}
		} else {
			StyleValue cssVal3 = getCSSValue(subp[3]);
			StyleValue cssVal2 = getCSSValue(subp[2]);
			StyleValue cssVal1 = getCSSValue(subp[1]);
			boolean p3 = isPrintValue(subp[3], cssVal3, cssVal1);
			// We print cssVal2 if isPrintValue2 or p3
			boolean p2 = isPrintValue(subp[2], cssVal2, cssVal0) || p3;
			// We print cssVal1 if isPrintValue1 or p2
			if (isPrintValue(subp[1], cssVal1, cssVal0) || p2) {
				buf.append('/');
				appendValueText(buf, cssVal1);
				if (p2) {
					buf.append('/');
					appendValueText(buf, cssVal2);
					if (p3) {
						buf.append('/');
						appendValueText(buf, cssVal3);
					}
				}
			}
		}
		appendPriority(buf, important);
		return true;
	}

	private boolean isPrintValue(String propertyName, StyleValue cssValue, StyleValue cssVal0) {
		if (!isIdentifier(cssValue)) {
			return true;
		}
		if (isIdentifier(cssVal0)) {
			return !valueEquals(cssVal0, cssValue);
		} else {
			return isNotInitialValue(cssValue, propertyName);
		}
	}

	/*
	 * This override is optimized for the case where non system-default values cannot be found
	 */
	@Override
	protected boolean isNotInitialValue(StyleValue cssVal, String propertyName) {
		return cssVal != null && !isInitialIdentifier(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	private void appendValueText(StringBuilder buf, StyleValue cssVal) {
		buf.append(cssVal.getMinifiedCssText(getShorthandName()));
	}

	private boolean isIdentifier(StyleValue cssVal) {
		return cssVal.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) cssVal).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
	}

}
