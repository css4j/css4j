/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;

/**
 * Build an 'animation' shorthand from individual properties.
 */
class FlexShorthandBuilder extends ShorthandBuilder {

	private final String FLEX_GROW = "flex-grow";
	private final String FLEX_SHRINK = "flex-shrink";
	private final String FLEX_BASIS = "flex-basis";

	FlexShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("flex", parentStyle);
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
		check = checkValuesForType(CSSValue.Type.REVERT, declaredSet);
		if (check == 1) {
			// All values are revert
			buf.append("revert");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		// Build the shorthand from values
		StyleValue cssFlexGrow = null;
		if (declaredSet.contains(FLEX_GROW) || declaredSet.contains(FLEX_SHRINK)) {
			cssFlexGrow = getCSSValue(FLEX_GROW);
			// Make sure that it is not a (wrong) list property
			if (cssFlexGrow.getCssValueType() == CssType.TYPED) {
				if (invalidFlexGrowShrink((CSSTypedValue) cssFlexGrow)) {
					return false;
				}
			} else if (cssFlexGrow.getCssValueType() != CssType.KEYWORD) {
				// not initial or unset
				return false;
			}
		}
		StyleValue cssFlexShrink = null;
		if (declaredSet.contains(FLEX_SHRINK)) {
			cssFlexShrink = getCSSValue(FLEX_SHRINK);
			// Make sure that it is not a (wrong) list property
			if (cssFlexShrink.getCssValueType() == CssType.TYPED) {
				if (invalidFlexGrowShrink((CSSTypedValue) cssFlexShrink)) {
					return false;
				}
			} else if (cssFlexGrow.getCssValueType() != CssType.KEYWORD) {
				// not initial or unset
				return false;
			}
		}
		StyleValue cssFlexBasis = null;
		if (declaredSet.contains(FLEX_BASIS)) {
			cssFlexBasis = getCSSValue(FLEX_BASIS);
			// Make sure that it is not a (wrong) list property
			if (cssFlexBasis.getCssValueType() == CssType.TYPED) {
				if (invalidFlexBasis((CSSTypedValue) cssFlexBasis)) {
					return false;
				}
			} else if (cssFlexGrow.getCssValueType() != CssType.KEYWORD) {
				// not initial or unset
				return false;
			}
		}
		// Special cases
		TypedValue primiBasis;
		if (cssFlexBasis != null && cssFlexBasis.getPrimitiveType() == Type.IDENT
				&& "auto".equalsIgnoreCase((primiBasis = (TypedValue) cssFlexBasis).getStringValue())) {
			CSSTypedValue primiGrow = (CSSTypedValue) cssFlexGrow;
			CSSTypedValue primiShrink = (CSSTypedValue) cssFlexShrink;
			float grow;
			float shrink;
			if (primiShrink != null) {
				shrink = primiShrink.getFloatValue(CSSUnit.CSS_NUMBER);
				if (primiGrow != null) {
					grow = primiGrow.getFloatValue(CSSUnit.CSS_NUMBER);
				} else {
					grow = shrink;
				}
			} else {
				if (primiGrow != null) {
					grow = primiGrow.getFloatValue(CSSUnit.CSS_NUMBER);
				} else {
					grow = 1f;
				}
				shrink = grow;
			}
			if (grow == 1f && shrink == 1f) {
				buf.append("auto");
				appendPriority(buf, important);
				return true;
			} else if (grow == 0f && shrink == 0f) {
				buf.append("none");
				appendPriority(buf, important);
				return true;
			} else if (grow == 0f && shrink == 1f) {
				buf.append("0");
				appendPriority(buf, important);
				return true;
			}
		}
		boolean appended = false;
		boolean shrinkNotInitial = isNotInitialValue(cssFlexShrink, FLEX_SHRINK);
		if (shrinkNotInitial || isNotInitialValue(cssFlexGrow, FLEX_GROW)) {
			appendValueText(buf, cssFlexGrow, false);
			appended = true;
		}
		if (shrinkNotInitial) {
			appendValueText(buf, cssFlexShrink, appended);
			appended = true;
		}
		if (isNotInitialValue(cssFlexBasis, FLEX_BASIS)) {
			primiBasis = (TypedValue) cssFlexBasis;
			if (primiBasis.getUnitType() == CSSUnit.CSS_NUMBER || primiBasis.isNumberZero()) {
				if (appended) {
					buf.append(' ');
				}
				buf.append("0px");
			} else {
				appendValueText(buf, cssFlexBasis, appended);
			}
			appended = true;
		}
		if (!appended) {
			buf.append("0");
		}
		appendPriority(buf, important);
		return true;
	}

	private boolean invalidFlexGrowShrink(CSSTypedValue primi) {
		if (primi.getUnitType() == CSSUnit.CSS_NUMBER) {
			return primi.getFloatValue(CSSUnit.CSS_NUMBER) < 0f;
		}
		return true;
	}

	private boolean invalidFlexBasis(CSSTypedValue primi) {
		if (primi.getPrimitiveType() == Type.IDENT) {
			String ident = primi.getStringValue();
			return !"auto".equalsIgnoreCase(ident) && !"content".equalsIgnoreCase(ident);
		} else if (primi.getUnitType() == CSSUnit.CSS_NUMBER) {
			return primi.getFloatValue(CSSUnit.CSS_NUMBER) != 0f;
		}
		return false;
	}

	/*
	 * This override is optimized for the case where non system-default values can be found
	 */
	@Override
	protected boolean isNotInitialValue(StyleValue cssVal, String propertyName) {
		return cssVal != null && !isEffectiveInitialKeyword(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	private void appendValueText(StringBuilder buf, StyleValue cssVal, boolean prepend) {
		if (prepend) {
			buf.append(' ');
		}
		buf.append(cssVal.getCssText());
	}

}
