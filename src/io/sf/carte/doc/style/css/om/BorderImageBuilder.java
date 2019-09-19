/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a border-image shorthand from individual properties.
 */
class BorderImageBuilder extends ShorthandBuilder {

	private final StringBuilder bibuf = new StringBuilder(64);
	private boolean noslice = true;

	BorderImageBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("border-image", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 5;
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		// Append property name
		buf.append(getShorthandName()).append(':');
		StyleValue biSource = getCSSValue("border-image-source");
		StyleValue biSlice = getCSSValue("border-image-slice");
		StyleValue biWidth = getCSSValue("border-image-width");
		StyleValue biOutset = getCSSValue("border-image-outset");
		StyleValue biRepeat = getCSSValue("border-image-repeat");
		// Check for CSS-wide keywords
		// First, check for inherit
		byte inheritcheck = checkValuesForInherit(declaredSet, biSource, biSlice, biWidth, biOutset, biRepeat);
		if (inheritcheck == 1) {
			// All values are 'inherit'
			buf.append("inherit");
			appendPriority(buf, important);
			return true;
		} else if (inheritcheck == 2) {
			return false;
		}
		// Check for 'initial' & initial values
		if (allValuesAreInitial(declaredSet, biSource, biSlice, biWidth, biOutset, biRepeat)) {
			// All values are initial
			buf.append("none");
			appendPriority(buf, important);
			return true;
		}
		// 'unset'
		byte unsetcheck = checkValuesForUnset(declaredSet, biSource, biSlice, biWidth, biOutset, biRepeat);
		if (unsetcheck == 1) {
			// All values are 'unset'
			buf.append("unset");
			appendPriority(buf, important);
			return true;
		} else if (unsetcheck == 2) {
			return false;
		}
		// Now append the values as appropriate
		if (declaredSet.contains("border-image-source")) {
			appendBorderImageSource(biSource);
		}
		if (declaredSet.contains("border-image-slice")) {
			if (!appendBorderImageSlice(biSlice)) {
				return false;
			}
		}
		if (declaredSet.contains("border-image-width")) {
			if (!appendBorderImageWidth(biWidth)) {
				return false;
			}
		}
		if (declaredSet.contains("border-image-outset")) {
			if (!appendBorderImageOutset(biOutset)) {
				return false;
			}
		}
		if (declaredSet.contains("border-image-repeat")) {
			if (!appendBorderImageRepeat(biRepeat)) {
				return false;
			}
		}
		// We already checked for all values being initial, but only for a full declaredSet
		if (bibuf.length() == 0) {
			bibuf.append("none");
		}
		// Priority
		if (important) {
			bibuf.append("!important");
		}
		buf.append(bibuf).append(';');
		return true;
	}

	private byte checkValuesForInherit(Set<String> declaredSet, StyleValue biSource, StyleValue biSlice, StyleValue biWidth,
			StyleValue biOutset, StyleValue biRepeat) {
		byte ucount = 0, total = (byte) declaredSet.size();
		if (declaredSet.contains("border-image-source") && isInherit(biSource)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-slice") && isInherit(biSlice)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-width") && isInherit(biWidth)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-outset") && isInherit(biOutset)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-repeat") && isInherit(biRepeat)) {
			ucount++;
		}
		if (ucount == 0) {
			return 0;
		} else if (ucount == total) {
			return 1;
		} else {
			return 2;
		}
	}

	private boolean allValuesAreInitial(Set<String> declaredSet, StyleValue biSource, StyleValue biSlice, StyleValue biWidth,
			StyleValue biOutset, StyleValue biRepeat) {
		if (declaredSet.contains("border-image-source") && !isCssKeywordValue("initial", biSource) && isNotInitialValue(biSource, "border-image-source")) {
			return false;
		}
		if (declaredSet.contains("border-image-slice") && !isCssKeywordValue("initial", biSlice) && isNotInitialValue(biSlice, "border-image-slice")) {
			return false;
		}
		if (declaredSet.contains("border-image-width") && !isCssKeywordValue("initial", biWidth) && isNotInitialValue(biWidth, "border-image-width")) {
			return false;
		}
		if (declaredSet.contains("border-image-outset") && !isCssKeywordValue("initial", biOutset) && isNotInitialValue(biOutset, "border-image-outset")) {
			return false;
		}
		if (declaredSet.contains("border-image-repeat") && !isCssKeywordValue("initial", biRepeat) && isNotInitialValue(biRepeat, "border-image-repeat")) {
			return false;
		}
		return true;
	}

	private byte checkValuesForUnset(Set<String> declaredSet, StyleValue biSource, StyleValue biSlice, StyleValue biWidth,
			StyleValue biOutset, StyleValue biRepeat) {
		byte ucount = 0, total = (byte) declaredSet.size();
		if (declaredSet.contains("border-image-source") && isCssKeywordValue("unset", biSource)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-slice") && isCssKeywordValue("unset", biSlice)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-width") && isCssKeywordValue("unset", biWidth)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-outset") && isCssKeywordValue("unset", biOutset)) {
			ucount++;
		}
		if (declaredSet.contains("border-image-repeat") && isCssKeywordValue("unset", biRepeat)) {
			ucount++;
		}
		if (ucount == 0) {
			return 0;
		} else if (ucount == total) {
			return 1;
		} else {
			return 2;
		}
	}

	private boolean appendValueIfNotInitial(String propertyName, StyleValue cssVal) {
		String text = getValueTextIfNotInitial(propertyName, cssVal);
		if (text != null) {
			appendText(text);
			return true;
		}
		return false;
	}

	private void appendText(CharSequence text) {
		appendText(text, bibuf);
	}

	private static void appendText(CharSequence text, StringBuilder buf) {
		if (buf.length() != 0) {
			buf.append(' ');
		}
		buf.append(text);
	}

	private void appendBorderImageSource(StyleValue biSource) {
		if (isNotInitialValue(biSource, "border-image-source")) {
			appendRelativeURI(bibuf, false, biSource);
		}
	}

	private boolean appendBorderImageSlice(StyleValue value) {
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			appendBorderImageSide1Value(bibuf, "100%", value);
		} else if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				return false;
			}
			boolean fill = false;
			int sides = list.getLength();
			StyleValue lastval = list.item(sides - 1);
			if (lastval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
					&& ((CSSPrimitiveValue) lastval).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
				if ("fill".equals(((CSSPrimitiveValue) lastval).getStringValue().toLowerCase(Locale.ROOT))) {
					sides--;
					fill = true;
				} else {
					return false;
				}
			}
			if (sides > 4) {
				return false;
			}
			boolean slicesAppended = appendBorderImageSides(bibuf, "100%", list, sides);
			if (slicesAppended || fill) {
				noslice = false;
			}
			if (fill) {
				appendText("fill");
			}
		}
		return true;
	}

	private boolean appendBorderImageWidth(StyleValue value) {
		StringBuilder buf = new StringBuilder();
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			appendBorderImageSide1Value(buf, "1", value);
		} else if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				return false;
			}
			int sides = list.getLength();
			if (sides > 4) {
				return false;
			}
			appendBorderImageSides(buf, "1", list, sides);
		}
		if (buf.length() != 0) {
			if (noslice) {
				bibuf.append(getInitialPropertyValue("border-image-slice").getMinifiedCssText(""));
			}
			bibuf.append('/').append(buf);
		}
		return true;
	}

	private boolean appendBorderImageOutset(StyleValue value) {
		StringBuilder buf = new StringBuilder();
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			appendBorderImageSide1Value(buf, "0", value);
		} else if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				return false;
			}
			appendBorderImageSides(buf, "0", list, list.getLength());
		}
		if (buf.length() != 0) {
			bibuf.append('/').append(buf);
		}
		return true;
	}

	private boolean appendBorderImageSides(StringBuilder buf, String initialText, ValueList list, int sides) {
		switch (sides) {
		case 1:
			return appendBorderImageSide1Value(buf, initialText, list.item(0));
		case 2:
			// top right
			return appendBorderImageSide2Values(buf, initialText, list.item(0), list.item(1));
		case 3:
			// top right bottom
			return appendBorderImageSide3Values(buf, initialText, list.item(0), list.item(1), list.item(2));
		case 4:
			// top right bottom left
			return appendBorderImageSide4Values(buf, initialText, list.item(0), list.item(1), list.item(2),
					list.item(3));
		default:
			return false;
		}
	}

	private boolean appendBorderImageSide1Value(StringBuilder buf, String initialText, StyleValue value) {
		String text = value.getMinifiedCssText("");
		if (!initialText.equals(text)) {
			appendText(text, buf);
			return true;
		}
		return false;
	}

	private boolean appendBorderImageSide2Values(StringBuilder buf, String initialText, StyleValue v0,
			StyleValue v1) {
		if (v0.equals(v1)) {
			return appendBorderImageSide1Value(buf, initialText, v0);
		} else {
			appendText(v0.getMinifiedCssText(""), buf);
			buf.append(' ');
			buf.append(v1.getMinifiedCssText(""));
			return true;
		}
	}

	private boolean appendBorderImageSide3Values(StringBuilder buf, String initialText, StyleValue v0,
			StyleValue v1, StyleValue v2) {
		if (v0.equals(v2)) { // bottom = top
			return appendBorderImageSide2Values(buf, initialText, v0, v1);
		} else {
			appendText(v0.getMinifiedCssText(""), buf);
			appendText(v1.getMinifiedCssText(""), buf);
			appendText(v2.getMinifiedCssText(""), buf);
			return true;
		}
	}

	private boolean appendBorderImageSide4Values(StringBuilder buf, String initialText, StyleValue v0,
			StyleValue v1, StyleValue v2, StyleValue v3) {
		if (v1.equals(v3)) { // left = right
			return appendBorderImageSide3Values(buf, initialText, v0, v1, v2);
		} else {
			appendText(v0.getMinifiedCssText(""), buf);
			appendText(v1.getMinifiedCssText(""), buf);
			appendText(v2.getMinifiedCssText(""), buf);
			appendText(v3.getMinifiedCssText(""), buf);
			return true;
		}
	}

	private boolean appendBorderImageRepeat(StyleValue value) {
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			appendValueIfNotInitial("border-image-repeat", value);
		} else if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated() || list.getLength() != 2) {
				return false;
			}
			StyleValue v0 = list.item(0);
			StyleValue v1 = list.item(1);
			if (v0.equals(v1)) {
				appendValueIfNotInitial("border-image-repeat", v0);
			} else {
				appendText(v0.getCssText());
				bibuf.append(v1.getCssText());
			}
		}
		return true;
	}
}
