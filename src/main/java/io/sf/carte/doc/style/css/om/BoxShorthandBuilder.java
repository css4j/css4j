/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Build a margin/padding shorthand from individual properties.
 */
class BoxShorthandBuilder extends BaseBoxShorthandBuilder {

	private final String topProperty;
	private final String rightProperty;
	private final String bottomProperty;
	private final String leftProperty;

	private byte keyword_state_top = 0;
	private byte keyword_state_bottom = 0;
	private byte keyword_state_left = 0;
	private byte keyword_state_right = 0;

	private boolean appended = false;

	BoxShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle) {
		super(shorthandName, parentStyle);
		topProperty = shorthandName + "-top";
		rightProperty = shorthandName + "-right";
		bottomProperty = shorthandName + "-bottom";
		leftProperty = shorthandName + "-left";
	}

	@Override
	protected int getMinimumSetSize() {
		return 4;
	}

	@Override
	boolean isExcludedValue(StyleValue cssValue) {
		CssType type = cssValue.getCssValueType();
		if (type == CssType.TYPED) {
			CSSTypedValue primi = (CSSTypedValue) cssValue;
			short ptype = primi.getUnitType();
			return !CSSUnit.isLengthUnitType(ptype) && !primi.isNumberZero() && ptype != CSSUnit.CSS_PERCENTAGE
					&& primi.getPrimitiveType() != Type.IDENT;
		}
		return type == CssType.LIST;
    }

	/**
	 * Score for finding same values in the box property.
	 * 
	 * @return 21 if all subproperty values are equal; 20 if right equals to left, and top
	 *         equals bottom; 17 if left equals to right and top; 16 if right equals to left
	 *         (bottom may be equal to them); 5 if top equals bottom and left; 4 if top equals
	 *         bottom but other values are different (right could be equal to them), 1 if left
	 *         and top are the same but other values differ (right could be equal to bottom),
	 *         0 if 3 or 4 values are different (right could be equal to bottom or top).
	 */
	private int sameValueScore(Set<String> declaredSet, byte live_state) {
		StyleValue topv = getCSSValue(topProperty);
		StyleValue bottomv = getCSSValue(bottomProperty);
		StyleValue leftv = getCSSValue(leftProperty);
		StyleValue rightv = getCSSValue(rightProperty);
		int score = 0;
		if (!declaredSet.contains(leftProperty) || keyword_state_left != live_state) {
			leftv = null;
		}
		if (!declaredSet.contains(rightProperty) || keyword_state_right != live_state) {
			rightv = null;
		}
		if (!declaredSet.contains(bottomProperty) || keyword_state_bottom != live_state) {
			bottomv = null;
		}
		if (!declaredSet.contains(topProperty) || keyword_state_top != live_state) {
			topv = null;
		}
		if (leftv == null || rightv == null || valueEquals(leftv, rightv)) {
			score += 16;
		}
		if (topv == null || bottomv == null || valueEquals(topv, bottomv)) {
			score += 4;
		}
		if (topv == null) {
			topv = bottomv;
		}
		if (leftv == null) {
			leftv = rightv;
		}
		if (topv == null || leftv == null || valueEquals(topv, leftv)) {
			score++;
		}
		return score;
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		// pending value check
		if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
			return false;
		}
		// Compute keyword state
		keyword_state_top = keywordState(getCSSValue(topProperty));
		keyword_state_bottom = keywordState(getCSSValue(bottomProperty));
		keyword_state_left = keywordState(getCSSValue(leftProperty));
		keyword_state_right = keywordState(getCSSValue(rightProperty));
		int keyword_state = keyword_state_top + keyword_state_bottom + keyword_state_left + keyword_state_right;
		byte best_state = 0;
		int inherit_count = keyword_state % 5;
		if (inherit_count > 1) {
			best_state = 1;
		} else if (keyword_state >= 10) {
			best_state = 5;
		}
		if (keyword_state == 0 || keyword_state == 4 || keyword_state == 20) {
			return appendPropertyBoxText(buf, declaredSet, important, best_state);
		}
		if (!appendPropertyBoxText(buf, declaredSet, important, best_state)) {
			return false;
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		// Lexicographic order, to mimic what other builders do
		if (declaredSet.contains(bottomProperty) && keyword_state_bottom != best_state) {
			appendIndividualProperty(wri, context, bottomProperty, important);
		}
		if (declaredSet.contains(leftProperty) && keyword_state_left != best_state) {
			appendIndividualProperty(wri, context, leftProperty, important);
		}
		if (declaredSet.contains(rightProperty) && keyword_state_right != best_state) {
			appendIndividualProperty(wri, context, rightProperty, important);
		}
		if (declaredSet.contains(topProperty) && keyword_state_top != best_state) {
			appendIndividualProperty(wri, context, topProperty, important);
		}
		return true;
	}

	private void appendIndividualProperty(BufferSimpleWriter wri,
		DeclarationFormattingContext context, String propertyName, boolean important) {
		StringBuilder buf = wri.getBuffer();
		buf.append(propertyName).append(':');
		BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, getCSSValue(propertyName),
			propertyName);
		appendPriority(buf, important);
	}

	private boolean appendPropertyBoxText(StringBuilder buf, Set<String> declaredSet, boolean important,
			byte live_state) {
		// Append property name
		buf.append(getShorthandName()).append(':');
		appended = false;
		switch (sameValueScore(declaredSet, live_state)) {
		case 21: // 1 value
			// Find a live property in declared set.
			String property = topProperty;
			if (!declaredSet.contains(property) || keyword_state_top != live_state) {
				property = bottomProperty;
				if (!declaredSet.contains(property) || keyword_state_bottom != live_state) {
					property = rightProperty;
					if (!declaredSet.contains(property) || keyword_state_right != live_state) {
						property = leftProperty;
						if (!declaredSet.contains(property) || keyword_state_left != live_state) {
							buf.append('0');
							break;
						}
					}
				}
			}
			if (!appendValueIfSaneAndNotInitial(buf, property)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			}
			break;
		case 20: // 2 values
			property = topProperty;
			if (!declaredSet.contains(property) || keyword_state_top != live_state) {
				property = bottomProperty;
				if (!declaredSet.contains(property) || keyword_state_bottom != live_state) {
					buf.append('0');
					appended = true;
				}
			}
			if (!appended && !appendValueIfSaneAndNotInitial(buf, property)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			} else {
				appended = false;
			}
			buf.append(' ');
			property = leftProperty;
			if (!declaredSet.contains(property) || keyword_state_left != live_state) {
				property = rightProperty;
				if (!declaredSet.contains(property) || keyword_state_right != live_state) {
					buf.append('0');
					appended = true;
				}
			}
			if (!appended && !appendValueIfSaneAndNotInitial(buf, property)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			}
			break;
		case 16: // 3 values
		case 17:
			if (declaredSet.contains(topProperty) && keyword_state_top == live_state
					&& !appendValueIfSaneAndNotInitial(buf, topProperty)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			} else {
				appended = false;
			}
			buf.append(' ');
			property = leftProperty;
			if (!declaredSet.contains(property) || keyword_state_left != live_state) {
				property = rightProperty;
				if (!declaredSet.contains(property) || keyword_state_right != live_state) {
					buf.append('0');
					appended = true;
				}
			}
			if (!appended && !appendValueIfSaneAndNotInitial(buf, property)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			} else {
				appended = false;
			}
			buf.append(' ');
			if (declaredSet.contains(bottomProperty) && keyword_state_bottom == live_state
					&& !appendValueIfSaneAndNotInitial(buf, bottomProperty)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			}
			break;
		default:
			if (declaredSet.contains(topProperty) && keyword_state_top == live_state
					&& !appendValueIfSaneAndNotInitial(buf, topProperty)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			} else {
				appended = false;
			}
			buf.append(' ');
			if (declaredSet.contains(rightProperty) && keyword_state_right == live_state
					&& !appendValueIfSaneAndNotInitial(buf, rightProperty)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			} else {
				appended = false;
			}
			buf.append(' ');
			if (declaredSet.contains(bottomProperty) && keyword_state_bottom == live_state
					&& !appendValueIfSaneAndNotInitial(buf, bottomProperty)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			} else {
				appended = false;
			}
			buf.append(' ');
			if (declaredSet.contains(leftProperty) && keyword_state_left == live_state
					&& !appendValueIfSaneAndNotInitial(buf, leftProperty)) {
				return false;
			}
			if (!appended) {
				buf.append('0');
			}
		}
		appendPriority(buf, important);
		return true;
	}

	private boolean appendValueIfSaneAndNotInitial(StringBuilder buf, String propertyName) {
		StyleValue cssVal = getCSSValue(propertyName);
		CssType type = cssVal.getCssValueType();
		if (type != CssType.LIST) {
			if (type == CssType.TYPED) {
				CSSTypedValue.Type ptype = cssVal.getPrimitiveType();
				if (ptype != Type.STRING) {
					if (ptype == Type.IDENT) {
						CSSTypedValue primi = (CSSTypedValue) cssVal;
						String s = primi.getStringValue();
						if (!s.equalsIgnoreCase("auto")) {
							return false;
						}
					}
					if (isNotInitialValue(cssVal, propertyName)) {
						if (appended) {
							buf.append(' ');
						}
						buf.append(cssVal.getMinifiedCssText(propertyName));
						appended = true;
					}
				}
			} else if (type == CssType.KEYWORD) {
				if (cssVal.getPrimitiveType() == CSSValue.Type.INHERIT
						|| cssVal.getPrimitiveType() == CSSValue.Type.REVERT) {
					if (appended) {
						buf.append(' ');
					}
					buf.append(cssVal.getCssText());
					appended = true;
				}
			} else if (type == CssType.PROXY) {
				return false;
			}
			return true;
		}
		return false;
	}
}
