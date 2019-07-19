/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;

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
		check = checkValuesForKeyword("unset", declaredSet);
		if (check == 1) {
			// All values are unset
			buf.append("unset");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		// Build the shorthand from values
		AbstractCSSValue cssFlexGrow = null;
		if (declaredSet.contains(FLEX_GROW) || declaredSet.contains(FLEX_SHRINK)) {
			cssFlexGrow = getCSSValue(FLEX_GROW);
			// Make sure that it is not a (wrong) list property
			if (cssFlexGrow.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE || 
					invalidFlexGrowShrink((CSSPrimitiveValue) cssFlexGrow)) {
				return false;
			}
		}
		AbstractCSSValue cssFlexShrink = null;
		if (declaredSet.contains(FLEX_SHRINK)) {
			cssFlexShrink = getCSSValue(FLEX_SHRINK);
			// Make sure that it is not a (wrong) list property
			if (cssFlexShrink.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE || 
					invalidFlexGrowShrink((CSSPrimitiveValue) cssFlexShrink)) {
				return false;
			}
		}
		AbstractCSSValue cssFlexBasis = null;
		if (declaredSet.contains(FLEX_BASIS)) {
			cssFlexBasis = getCSSValue(FLEX_BASIS);
			// Make sure that it is not a (wrong) list property
			if (cssFlexBasis.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE ||
					invalidFlexBasis((CSSPrimitiveValue) cssFlexBasis)) {
				return false;
			}
		}
		// Special cases
		AbstractCSSPrimitiveValue primiBasis = (AbstractCSSPrimitiveValue) cssFlexBasis;
		if (primiBasis != null && primiBasis.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& "auto".equalsIgnoreCase(primiBasis.getStringValue())) {
			CSSPrimitiveValue primiGrow = (CSSPrimitiveValue) cssFlexGrow;
			CSSPrimitiveValue primiShrink = (CSSPrimitiveValue) cssFlexShrink;
			float grow;
			float shrink;
			if (primiShrink != null) {
				shrink = primiShrink.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
				if (primiGrow != null) {
					grow = primiGrow.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
				} else {
					grow = shrink;
				}
			} else {
				if (primiGrow != null) {
					grow = primiGrow.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
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
			if (primiBasis.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER || primiBasis.isNumberZero()) {
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

	private boolean invalidFlexGrowShrink(CSSPrimitiveValue primi) {
		if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
			return primi.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) < 0f;
		} else if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			String ident = primi.getStringValue();
			return !"initial".equalsIgnoreCase(ident);
		}
		return true;
	}

	private boolean invalidFlexBasis(CSSPrimitiveValue primi) {
		if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			String ident = primi.getStringValue();
			return !"auto".equalsIgnoreCase(ident) && !"content".equalsIgnoreCase(ident) && !"initial".equalsIgnoreCase(ident);
		} else if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
			return primi.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) != 0f;
		}
		return false;
	}

	/*
	 * This override is optimized for the case where non system-default values can be found
	 */
	@Override
	protected boolean isNotInitialValue(AbstractCSSValue cssVal, String propertyName) {
		return cssVal != null && !isInitialIdentifier(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	private void appendValueText(StringBuilder buf, AbstractCSSValue cssVal, boolean prepend) {
		if (prepend) {
			buf.append(' ');
		}
		buf.append(cssVal.getCssText());
	}

}
