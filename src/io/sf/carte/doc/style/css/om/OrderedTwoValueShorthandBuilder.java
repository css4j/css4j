/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a shorthand from two individual properties when specific order matters.
 * <p>
 * If the second value is not present and the first value is valid for the second, it is used for it too.
 */
class OrderedTwoValueShorthandBuilder extends ShorthandBuilder {

	private final String initialvalue;

	OrderedTwoValueShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle, String initialvalue) {
		super(shorthandName, parentStyle);
		this.initialvalue = initialvalue;
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
		String[] subp = getSubproperties();
		if (subp.length != 2) {
			throw new IllegalStateException("This class is only for two subproperties");
		}
		String property = subp[0];
		// Make sure that it is not a layered property
		AbstractCSSValue cssVal = getCSSValue(property);
		if ((cssVal.getCssValueType() == CSSValue.CSS_VALUE_LIST &&
				((ValueList) cssVal).isCommaSeparated())) {
			return false;
		}
		boolean appended = false;
		if (isNotInitialValue(cssVal, property)) {
			appendValueText(buf, cssVal, false);
			appended = true;
		}
		AbstractCSSValue cssVal2 = getCSSValue(subp[1]);
		if (!valueEquals(cssVal, cssVal2)) {
			appendValueText(buf, cssVal2, appended);
			appended = true;
		}
		if (!appended) {
			buf.append(initialvalue);
		}
		appendPriority(buf, important);
		return true;
	}

	/*
	 * This override is optimized for the case where non system-default values cannot be found
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
		buf.append(cssVal.getMinifiedCssText(getShorthandName()));
	}

}
