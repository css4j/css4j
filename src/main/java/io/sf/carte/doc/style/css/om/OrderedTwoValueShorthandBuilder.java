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
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
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

		// Unset
		check = checkValuesForType(CSSValue.Type.UNSET, declaredSet);
		if (check == 1) {
			// All values are unset
			buf.append("unset");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2 && isInheritedProperty()) {
			return 1;
		}

		// pending value check
		if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
			return 1;
		}

		String[] subp = getSubproperties();
		if (subp.length != 2) {
			throw new IllegalStateException("This class is only for two subproperties");
		}
		String property = subp[0];
		// Make sure that it is not a layered property
		StyleValue cssVal = getCSSValue(property);
		if (cssVal.getCssValueType() == CssType.LIST &&
				((ValueList) cssVal).isCommaSeparated()) {
			return 1;
		}

		boolean appended = false;
		if (isNotInitialValue(cssVal, property)) {
			appendValueText(buf, cssVal, false);
			appended = true;
		}

		StyleValue cssVal2 = getCSSValue(subp[1]);
		if (!valueEquals(cssVal, cssVal2) && (appended || isNotInitialValue(cssVal2, property))) {
			appendValueText(buf, cssVal2, appended);
			appended = true;
		}

		if (!appended) {
			buf.append(initialvalue);
		}

		appendPriority(buf, important);

		return 0;
	}

	@Override
	boolean isInheritedProperty() {
		String ptyname = getLonghandProperties()[0];
		return PropertyDatabase.getInstance().isInherited(ptyname);
	}

	/*
	 * This override is optimized for the case where non system-default values cannot be found
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
		buf.append(cssVal.getMinifiedCssText(getShorthandName()));
	}

}
