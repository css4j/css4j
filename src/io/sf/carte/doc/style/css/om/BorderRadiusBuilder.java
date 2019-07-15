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
 * Build a border-radius shorthand from individual properties.
 */
class BorderRadiusBuilder extends ShorthandBuilder {

	BorderRadiusBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("border-radius", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 4;
	}

	/**
	 * Score for finding same values in the box property.
	 * 
	 * @return 21 if all subproperty values are equal; 20 if top right equals to bottom left,
	 *         and top left equals bottom right; 17 if top left equals to top right and bottom
	 *         left; 16 if top right equals to bottom left (bottom right may be equal to
	 *         them); 5 if top right equals bottom right and top left; 4 if top left equals
	 *         bottom right but other values are different (top right could be equal to them),
	 *         1 if top left and top right are the same but other values differ (bottom right
	 *         could be equal to bottom left), 0 if 3 or 4 values are different.
	 */
	private int sameValueScore(Set<String> declaredSet, AbstractCSSValue topLeftValue, AbstractCSSValue topRightValue,
			AbstractCSSValue bottomRightValue, AbstractCSSValue bottomLeftValue) {
		int score = 0;
		if (bottomLeftValue.equals(topRightValue)) {
			score += 16;
		} else {
			if (!declaredSet.contains("border-bottom-left-radius")) {
				bottomLeftValue = topRightValue;
				score += 16;
			} else if (!declaredSet.contains("border-top-right-radius")) {
				topRightValue = bottomLeftValue;
				score += 16;
			}
		}
		if (topLeftValue.equals(bottomRightValue)) {
			score += 4;
		} else {
			if (!declaredSet.contains("border-bottom-right-radius")) {
				bottomRightValue = topLeftValue;
				score += 4;
			} else if (!declaredSet.contains("border-top-left-radius")) {
				topLeftValue = bottomRightValue;
				score += 4;
			}
		}
		if (topLeftValue.equals(topRightValue)
				|| (!declaredSet.contains("border-bottom-right-radius")
						&& !declaredSet.contains("border-top-left-radius"))
				|| (!declaredSet.contains("border-top-right-radius")
						&& !declaredSet.contains("border-bottom-left-radius"))) {
			score += 1;
		}
		return score;
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
		// First, check for inherit
		byte inheritcheck = checkDeclaredValuesForInherit(declaredSet);
		if (inheritcheck == 1) {
			// All values are 'inherit'
			buf.append("inherit");
			appendPriority(buf, important);
			return true;
		} else if (inheritcheck == 2) {
			return false;
		}
		// now unset
		byte unsetcheck = checkDeclaredValuesForKeyword("unset", declaredSet);
		if (unsetcheck == 1) {
			// All values are 'unset'
			buf.append("unset");
			appendPriority(buf, important);
			return true;
		} else if (unsetcheck == 2) {
			return false;
		}
		AbstractCSSValue topLeftValue = getCSSValue("border-top-left-radius");
		AbstractCSSValue topRightValue = getCSSValue("border-top-right-radius");
		AbstractCSSValue bottomRightValue = getCSSValue("border-bottom-right-radius");
		AbstractCSSValue bottomLeftValue = getCSSValue("border-bottom-left-radius");
		// Check for list values
		boolean slash = false;
		AbstractCSSValue topLeftValue0, topLeftValue1;
		if (topLeftValue != null && topLeftValue.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			topLeftValue0 = ((ValueList) topLeftValue).item(0);
			topLeftValue1 = ((ValueList) topLeftValue).item(1);
			slash = true;
		} else {
			topLeftValue0 = topLeftValue;
			topLeftValue1 = null;
		}
		AbstractCSSValue topRightValue0, topRightValue1;
		if (topRightValue != null && topRightValue.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			topRightValue0 = ((ValueList) topRightValue).item(0);
			topRightValue1 = ((ValueList) topRightValue).item(1);
			slash = true;
		} else {
			topRightValue0 = topRightValue;
			topRightValue1 = null;
		}
		AbstractCSSValue bottomRightValue0, bottomRightValue1;
		if (bottomRightValue != null && bottomRightValue.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			bottomRightValue0 = ((ValueList) bottomRightValue).item(0);
			bottomRightValue1 = ((ValueList) bottomRightValue).item(1);
			slash = true;
		} else {
			bottomRightValue0 = bottomRightValue;
			bottomRightValue1 = null;
		}
		AbstractCSSValue bottomLeftValue0, bottomLeftValue1;
		if (bottomLeftValue != null && bottomLeftValue.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			bottomLeftValue0 = ((ValueList) bottomLeftValue).item(0);
			bottomLeftValue1 = ((ValueList) bottomLeftValue).item(1);
			slash = true;
		} else {
			bottomLeftValue0 = bottomLeftValue;
			bottomLeftValue1 = null;
		}
		appendBorderRadiusSide(buf, declaredSet, topLeftValue0, topRightValue0, bottomRightValue0, bottomLeftValue0,
				important);
		if (slash) {
			buf.append('/');
			appendBorderRadiusSide(buf, declaredSet, topLeftValue1, topRightValue1, bottomRightValue1, bottomLeftValue1,
					important);
		}
		appendPriority(buf, important);
		return true;
	}

	void appendBorderRadiusSide(StringBuilder buf, Set<String> declaredSet, AbstractCSSValue topLeftValue,
			AbstractCSSValue topRightValue, AbstractCSSValue bottomRightValue, AbstractCSSValue bottomLeftValue,
			boolean important) {
		switch (sameValueScore(declaredSet, topLeftValue, topRightValue, bottomRightValue, bottomLeftValue)) {
		case 21: // 1 value
			AbstractCSSValue value;
			if (declaredSet.contains("border-top-left-radius")) {
				value = topLeftValue;
			} else if (declaredSet.contains("border-bottom-right-radius")) {
				value = bottomRightValue;
			} else if (declaredSet.contains("border-top-right-radius")) {
				value = topRightValue;
			} else {
				value = bottomLeftValue;
			}
			appendValue(buf, value);
			break;
		case 20: // 2 values
			if (declaredSet.contains("border-top-left-radius")) {
				appendValue(buf, topLeftValue);
			} else if (declaredSet.contains("border-bottom-right-radius")) {
				appendValue(buf, bottomRightValue);
			} else {
				buf.append('0');
			}
			buf.append(' ');
			if (declaredSet.contains("border-top-right-radius")) {
				appendValue(buf, topRightValue);
			} else if (declaredSet.contains("border-bottom-left-radius")) {
				appendValue(buf, bottomLeftValue);
			} else {
				buf.append('0');
			}
			break;
		case 16: // 3 values
		case 17:
			if (declaredSet.contains("border-top-left-radius")) {
				appendValue(buf, topLeftValue);
			} else {
				buf.append('0');
			}
			buf.append(' ');
			if (declaredSet.contains("border-top-right-radius")) {
				appendValue(buf, topRightValue);
			} else if (declaredSet.contains("border-bottom-left-radius")) {
				appendValue(buf, bottomLeftValue);
			} else {
				buf.append('0');
			}
			buf.append(' ');
			if (declaredSet.contains("border-bottom-right-radius")) {
				appendValue(buf, bottomRightValue);
			} else {
				buf.append('0');
			}
			break;
		default:
			if (declaredSet.contains("border-top-left-radius")) {
				appendValue(buf, topLeftValue);
			} else {
				buf.append('0');
			}
			buf.append(' ');
			if (declaredSet.contains("border-top-right-radius")) {
				appendValue(buf, topRightValue);
			} else {
				buf.append('0');
			}
			buf.append(' ');
			if (declaredSet.contains("border-bottom-right-radius")) {
				appendValue(buf, bottomRightValue);
			} else {
				buf.append('0');
			}
			buf.append(' ');
			if (declaredSet.contains("border-bottom-left-radius")) {
				appendValue(buf, bottomLeftValue);
			} else {
				buf.append('0');
			}
		}
	}

	private void appendValue(StringBuilder buf, AbstractCSSValue cssVal) {
		if (isNotInitialValue(cssVal, "border-top-left-radius")) {
			buf.append(cssVal.getMinifiedCssText("border-radius"));
		} else {
			buf.append('0');
		}
	}
}
