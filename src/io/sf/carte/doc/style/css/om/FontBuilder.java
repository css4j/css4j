/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Build a font shorthand from individual properties.
 */
class FontBuilder extends ShorthandBuilder {

	FontBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("font", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 14;
	}

	@Override
	protected void preprocessSet() {
		addPropertyIfAssigned("font-variant-caps");
		addPropertyIfAssigned("font-variant-ligatures");
		addPropertyIfAssigned("font-variant-position");
		addPropertyIfAssigned("font-variant-numeric");
		addPropertyIfAssigned("font-variant-alternates");
		addPropertyIfAssigned("font-variant-east-asian");
	}

	private void addPropertyIfAssigned(String property) {
		BaseCSSStyleDeclaration style = getParentStyle();
		if (style.isPropertySet(property)) {
			addAssignedProperty(property, style.isPropertyImportant(property));
		}
	}

	void appendFontKerningValue(StringBuilder buf) {
		buf.append("font-kerning:").append(getCSSValue("font-kerning").getMinifiedCssText("font-kerning"));
		appendPriority(buf, isPropertyImportant("font-kerning"));
	}

	private void appendFontSizeAdjustValue(StringBuilder buf) {
		buf.append("font-size-adjust:").append(getCSSValue("font-size-adjust").getMinifiedCssText("font-size-adjust"));
		appendPriority(buf, isPropertyImportant("font-size-adjust"));
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		if (!appendFontShorthandSet(buf, declaredSet, important)) {
			// Create font-variant builder
			FontVariantBuilder builder = createFontVariantBuilder();
			builder.appendMinifiedCssText(buf);
			return false;
		}
		// Other subproperties
		if (!isFontVariantSetToInitialOrCss21()) {
			// Create font-variant builder
			FontVariantBuilder builder = createFontVariantBuilder();
			builder.appendMinifiedCssText(buf);
		}
		if (isPropertyAssigned("font-kerning", important) && !isInitialValue("font-kerning")) {
			appendFontKerningValue(buf);
		}
		if (isPropertyAssigned("font-size-adjust", important) && !isInitialValue("font-size-adjust")) {
			appendFontSizeAdjustValue(buf);
		}
		return true;
	}

	boolean appendFontShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		if (!isFontKerningAdjustVariantSet(important)) {
			return false;
		}
		// Append property name
		buf.append(getShorthandName()).append(':');
		StyleValue vFontVariant = getCSSValue("font-variant-caps");
		StyleValue vFontStyle = getCSSValue("font-style");
		StyleValue vFontWeight = getCSSValue("font-weight");
		StyleValue vFontStretch = getCSSValue("font-stretch");
		StyleValue vFontSize = getCSSValue("font-size");
		StyleValue vFontFamily = getCSSValue("font-family");
		StyleValue vLineHeight = getCSSValue("line-height");
		if (declaredSet.size() >= 7) {
			// Check for inherit
			byte inheritcheck = checkValuesForInherit(vFontStyle, vFontWeight, vFontStretch, vFontSize, vFontFamily,
					vLineHeight);
			if (inheritcheck == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return true;
			} else if (inheritcheck == 2) {
				return false;
			}
			// Check for css-wide keywords
			if (checkValuesForInitial(vFontStyle, vFontVariant, vFontWeight, vFontStretch, vFontSize, vFontFamily,
					vLineHeight)) {
				// All values are initial
				buf.append("normal");
				appendPriority(buf, important);
				return true;
			}
			byte unsetcheck = checkValuesForUnset(vFontStyle, vFontWeight, vFontStretch, vFontSize, vFontFamily,
					vLineHeight);
			if (unsetcheck == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return true;
			} else if (unsetcheck == 2) {
				return false;
			}
		}
		// Other constraints
		if (!isFontVariantCss21(vFontVariant)) {
			return false;
		}
		if (!isFontStretchCss3(vFontStretch)) {
			return false;
		}
		// Now append the values as appropriate
		boolean appended = false;
		if (declaredSet.contains("font-style")) {
			appended = appendValueIfNotInitial(buf, "font-style", vFontStyle, appended);
		}
		if (declaredSet.contains("font-variant-caps")) {
			appended = appendValueIfNotInitial(buf, "font-variant-caps", vFontVariant, appended);
		}
		if (declaredSet.contains("font-weight")) {
			appended = appendValueIfNotInitial(buf, "font-weight", vFontWeight, appended);
		}
		if (declaredSet.contains("font-size") || declaredSet.contains("line-height")
				|| declaredSet.contains("font-family") || declaredSet.contains("font-stretch")) {
			boolean not_initial_lh = isNotInitialValue(vLineHeight, "line-height");
			boolean not_initial_fsz = isNotInitialValue(vFontSize, "font-size");
			boolean not_initial_fst = isNotInitialValue(vFontStretch, "font-stretch");
			boolean not_initial_ff = isNotInitialValue(vFontFamily, "font-family");
			if (not_initial_fsz || not_initial_lh || not_initial_fst) {
				// We need to serialize font size, perhaps also line height.
				String fontSizeText;
				if (not_initial_fsz) {
					fontSizeText = vFontSize.getMinifiedCssText("font-size");
				} else {
					fontSizeText = "medium";
				}
				// We need a font family here
				if (!not_initial_ff) {
					// We need to terminate here
					if (!appended) {
						buf.append("normal");
					}
					appendPriority(buf, important);
					if (not_initial_fst && declaredSet.contains("font-stretch")) {
						buf.append("font-stretch:");
						buf.append(vFontStretch.getMinifiedCssText("font-stretch"));
						appendPriority(buf, important);
					}
					if (not_initial_fsz && declaredSet.contains("font-size")) {
						buf.append("font-size:");
						buf.append(fontSizeText);
						appendPriority(buf, important);
					}
					if (not_initial_lh && declaredSet.contains("line-height")) {
						buf.append("line-height:");
						buf.append(vLineHeight.getMinifiedCssText("line-height"));
						appendPriority(buf, important);
					}
					return true;
				}
				if (declaredSet.contains("font-stretch")) {
					appended = appendValueIfNotInitial(buf, "font-stretch", vFontStretch, appended);
				}
				if (appended) {
					buf.append(' ');
				} else {
					appended = true;
				}
				buf.append(fontSizeText);
				if (not_initial_lh) {
					buf.append('/').append(vLineHeight.getMinifiedCssText("line-height"));
				}
				buf.append(' ');
				buf.append(vFontFamily.getMinifiedCssText("font-family"));
			} else if (not_initial_ff) {
				if (!appended) {
					buf.append("normal");
				}
				appendPriority(buf, important);
				if (not_initial_fst && declaredSet.contains("font-stretch")) {
					buf.append("font-stretch:");
					buf.append(vFontStretch.getMinifiedCssText("font-stretch"));
					appendPriority(buf, important);
				}
				buf.append("font-family:");
				buf.append(vFontFamily.getMinifiedCssText("font-family"));
				appendPriority(buf, important);
				return true;
			}
		}
		if (!appended) {
			buf.append("normal");
		}
		// Priority
		appendPriority(buf, important);
		return true;
	}

	private boolean appendValueIfNotInitial(StringBuilder buf, String propertyName, StyleValue cssVal,
			boolean appended) {
		String text = getValueTextIfNotInitial(propertyName, cssVal);
		if (text != null) {
			if (appended) {
				buf.append(' ');
			}
			buf.append(text);
			return true;
		}
		return appended;
	}

	private byte checkValuesForInherit(StyleValue vFontStyle, StyleValue vFontWeight,
			StyleValue vFontStretch, StyleValue vFontSize, StyleValue vFontFamily,
			StyleValue vLineHeight) {
		byte count = 0;
		if (isInherit(vFontStyle)) {
			count++;
		}
		if (isInherit(vFontWeight)) {
			count++;
		}
		if (isInherit(vFontStretch)) {
			count++;
		}
		if (isInherit(vFontSize)) {
			count++;
		}
		if (isInherit(vFontFamily)) {
			count++;
		}
		if (isInherit(vLineHeight)) {
			count++;
		}
		switch (count) {
		case 0:
			return 0;
		case 6:
			return 1;
		default:
			return 2;
		}
	}

	private byte checkValuesForUnset(StyleValue vFontStyle, StyleValue vFontWeight,
			StyleValue vFontStretch, StyleValue vFontSize, StyleValue vFontFamily,
			StyleValue vLineHeight) {
		byte ucount = 0;
		if (isCssKeywordValue("unset", vFontStyle)) {
			ucount++;
		}
		if (isCssKeywordValue("unset", vFontWeight)) {
			ucount++;
		}
		if (isCssKeywordValue("unset", vFontStretch)) {
			ucount++;
		}
		if (isCssKeywordValue("unset", vFontSize)) {
			ucount++;
		}
		if (isCssKeywordValue("unset", vFontFamily)) {
			ucount++;
		}
		if (isCssKeywordValue("unset", vLineHeight)) {
			ucount++;
		}
		switch (ucount) {
		case 0:
			return 0;
		case 6:
			return 1;
		default:
			return 2;
		}
	}

	private boolean checkValuesForInitial(StyleValue vFontStyle, StyleValue vFontVariant,
			StyleValue vFontWeight, StyleValue vFontStretch, StyleValue vFontSize,
			StyleValue vFontFamily, StyleValue vLineHeight) {
		if (!isCssKeywordValue("initial", vFontStyle) && isNotInitialValue(vFontStyle, "font-style")) {
			return false;
		}
		if (!isCssKeywordValue("initial", vFontVariant) && isNotInitialValue(vFontVariant, "font-variant")) {
			return false;
		}
		if (!isCssKeywordValue("initial", vFontWeight) && isNotInitialValue(vFontWeight, "font-weight")) {
			return false;
		}
		if (!isCssKeywordValue("initial", vFontStretch) && isNotInitialValue(vFontStretch, "font-stretch")) {
			return false;
		}
		if (!isCssKeywordValue("initial", vFontSize) && isNotInitialValue(vFontSize, "font-size")) {
			return false;
		}
		if (!isCssKeywordValue("initial", vFontFamily) && isNotInitialValue(vFontFamily, "font-family")) {
			return false;
		}
		if (!isCssKeywordValue("initial", vLineHeight) && isNotInitialValue(vLineHeight, "line-height")) {
			return false;
		}
		return true;
	}

	private boolean isFontVariantCss21(StyleValue vFontVariant) {
		String text = vFontVariant.getCssText().toLowerCase(Locale.ROOT);
		return text.equals("normal") || text.equals("small-caps") || text.equals("initial");
	}

	private boolean isFontStretchCss3(StyleValue vFontStretch) {
		String text = vFontStretch.getCssText().toLowerCase(Locale.ROOT);
		return text.equals("normal") || text.equals("ultra-condensed") || text.equals("extra-condensed")
				|| text.equals("condensed") || text.equals("semi-condensed") || text.equals("semi-expanded")
				|| text.equals("expanded") || text.equals("extra-expanded") || text.equals("ultra-expanded")
				|| text.equals("initial");
	}

	private boolean isFontKerningAdjustVariantSet(boolean important) {
		return isPropertyAssigned("font-kerning", important) && isPropertyAssigned("font-size-adjust", important)
				&& isPropertyAssigned("font-variant-ligatures", important)
				&& isPropertyAssigned("font-variant-position", important)
				&& isPropertyAssigned("font-variant-caps", important)
				&& isPropertyAssigned("font-variant-numeric", important)
				&& isPropertyAssigned("font-variant-alternates", important)
				&& isPropertyAssigned("font-variant-east-asian", important);
	}

	private boolean isFontVariantSetToInitialOrCss21() {
		StyleValue cssVal = getCSSValue("font-variant-caps");
		String fvcaps = cssVal.getCssText();
		if (isInitialIdentifier(cssVal) || fvcaps.equalsIgnoreCase("normal") || fvcaps.equalsIgnoreCase("small-caps")) {
			return isInitialValue("font-variant-ligatures") && isInitialValue("font-variant-position")
					&& isInitialValue("font-variant-numeric") && isInitialValue("font-variant-alternates")
					&& isInitialValue("font-variant-east-asian");
		}
		return false;
	}

	private FontVariantBuilder createFontVariantBuilder() {
		BaseCSSStyleDeclaration style = getParentStyle();
		FontVariantBuilder builder = new FontVariantBuilder(style);
		addPropertyIfAssigned(builder, "font-variant-ligatures");
		addPropertyIfAssigned(builder, "font-variant-caps");
		addPropertyIfAssigned(builder, "font-variant-position");
		addPropertyIfAssigned(builder, "font-variant-numeric");
		addPropertyIfAssigned(builder, "font-variant-alternates");
		addPropertyIfAssigned(builder, "font-variant-east-asian");
		return builder;
	}

	private void addPropertyIfAssigned(ShorthandBuilder builder, String property) {
		BaseCSSStyleDeclaration style = getParentStyle();
		if (style.isPropertySet(property)) {
			builder.addAssignedProperty(property, style.isPropertyImportant(property));
		}
	}
}
