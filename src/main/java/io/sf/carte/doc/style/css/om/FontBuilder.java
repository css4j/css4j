/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Build a font shorthand from individual properties.
 */
class FontBuilder extends ShorthandBuilder {

	private boolean fontVariantDone = false;

	FontBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("font", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 17;
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
		if (style.isPropertySet(property) && !isPropertyInAnySet(property)) {
			addAssignedProperty(property, style.isPropertyImportant(property));
		}
	}

	@Override
	boolean isInheritedProperty() {
		return true;
	}

	private void appendFontLonghand(StringBuilder buf, String property) {
		buf.append(property).append(':').append(getCSSValue(property).getMinifiedCssText(property));
		appendPriority(buf, isPropertyInImportantSet(property));
	}

	@Override
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		int result = appendFontShorthandSet(buf, declaredSet, important);
		if (result == -1) {
			return 1;
		}
		// Other subproperties
		if (result == 0) {
			if (!fontVariantDone) {
				// Create font-variant builder
				FontVariantBuilder builder = createFontVariantBuilder();
				if (builder.checkValuesForType(CSSValue.Type.INTERNAL, important) != 0) {
					return 1;
				}
				if (!isFontVariantSetToInitialOrCss21(declaredSet)) {
					builder.appendMinifiedCssText(buf);
					setFontVariantDone();
				}
			}
			StyleValue vFontStretch = getCSSValue("font-stretch");
			if (isPropertyAssigned("font-stretch", important) && isNotInitialValue(vFontStretch, "font-stretch")
					&& !isFontStretchCss3(vFontStretch)) {
				buf.append("font-stretch:").append(vFontStretch.getMinifiedCssText("font-stretch"));
				appendPriority(buf, isPropertyInImportantSet("font-stretch"));
			}
			if (isPropertyAssigned("font-kerning", important) && !isInitialValue("font-kerning")) {
				appendFontLonghand(buf, "font-kerning");
			}
			if (isPropertyAssigned("font-optical-sizing", important) && !isInitialValue("font-optical-sizing")) {
				appendFontLonghand(buf, "font-optical-sizing");
			}
			if (isPropertyAssigned("font-feature-settings", important) && !isInitialValue("font-feature-settings")) {
				appendFontLonghand(buf, "font-feature-settings");
			}
			if (isPropertyAssigned("font-variation-settings", important) && !isInitialValue("font-variation-settings")) {
				appendFontLonghand(buf, "font-variation-settings");
			}
			if (isPropertyAssigned("font-size-adjust", important) && !isInitialValue("font-size-adjust")) {
				appendFontLonghand(buf, "font-size-adjust");
			}
		} else {
			Type keyword;
			switch (result) {
			case 1:
				keyword = Type.INHERIT;
				break;
			case 2:
				keyword = Type.UNSET;
				break;
			default:
				keyword = Type.REVERT;
			}
			appendLonghandIfNotKeyword(buf, "font-kerning", keyword, important);
			appendLonghandIfNotKeyword(buf, "font-optical-sizing", keyword, important);
			appendLonghandIfNotKeyword(buf, "font-feature-settings", keyword, important);
			appendLonghandIfNotKeyword(buf, "font-variation-settings", keyword, important);
			appendLonghandIfNotKeyword(buf, "font-size-adjust", keyword, important);
			// Create font-variant builder
			FontVariantBuilder builder = createFontVariantBuilder();
			builder.preprocessSet();
			if (builder.checkValuesForType(Type.INTERNAL, important) != 0) {
				return 1;
			}
			byte fvTypes = builder.checkValuesForType(keyword, important);
			if (fvTypes == 2) {
				return 1;
			}
			if (fvTypes == 0 && !fontVariantDone
					&& !isFontVariantSetToInitialOrCss21(declaredSet)) {
				builder.appendMinifiedCssText(buf);
				setFontVariantDone();
			}
		}
		return 0;
	}

	/**
	 * Try to append the given set to the buffer.
	 * 
	 * @param buf         the buffer to append to.
	 * @param declaredSet the components of the set.
	 * @param important   {@code true} if the set if {@code important}.
	 * @return 0 if a regular set was appended, 1 if it was {@code inherit}, 2 if
	 *         {@code unset}, 3 if {@code revert}, -1 if a shorthand set could not
	 *         be appended.
	 */
	private int appendFontShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Append property name
		buf.append(getShorthandName()).append(':');
		StyleValue vFontVariantCaps = getCSSValue("font-variant-caps");
		StyleValue vFontStyle = getCSSValue("font-style");
		StyleValue vFontWeight = getCSSValue("font-weight");
		StyleValue vFontStretch = getCSSValue("font-stretch");
		StyleValue vFontSize = getCSSValue("font-size");
		StyleValue vFontFamily = getCSSValue("font-family");
		StyleValue vLineHeight = getCSSValue("line-height");
		if (declaredSet.size() >= 7) {
			// Check for inherit
			byte inheritcheck = checkValuesForInherit(vFontVariantCaps, vFontStyle, vFontWeight, vFontStretch,
					vFontSize, vFontFamily, vLineHeight);
			if (inheritcheck == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return 1;
			} else if (inheritcheck == 2) {
				return -1;
			}
			// Check for css-wide keywords
			if (checkValuesForInitial(vFontStyle, vFontVariantCaps, vFontWeight, vFontStretch,
					vFontSize, vFontFamily, vLineHeight)) {
				// All values are initial
				buf.append("normal");
				appendPriority(buf, important);
				return 0;
			}
			// Unset check
			byte kwcheck = checkValuesForKeyword(CSSValue.Type.UNSET, vFontVariantCaps, vFontStyle, vFontWeight,
					vFontStretch, vFontSize, vFontFamily, vLineHeight);
			if (kwcheck == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return 2;
			} else if (kwcheck == 2) {
				return -1;
			}
			// Revert check
			kwcheck = checkValuesForKeyword(CSSValue.Type.REVERT, vFontVariantCaps, vFontStyle, vFontWeight,
					vFontStretch, vFontSize, vFontFamily, vLineHeight);
			if (kwcheck == 1) {
				// All values are revert
				buf.append("revert");
				appendPriority(buf, important);
				return 3;
			} else if (kwcheck == 2) {
				return -1;
			}
		}
		// Now append the values as appropriate
		boolean appended = false;
		if (declaredSet.contains("font-style")) {
			appended = appendValueIfNotInitial(buf, "font-style", vFontStyle, appended);
		}
		if (declaredSet.contains("font-variant-caps") && isFontVariantCss21(vFontVariantCaps)) {
			appended = appendValueIfNotInitial(buf, "font-variant-caps", vFontVariantCaps, appended);
		}
		if (declaredSet.contains("font-weight")) {
			appended = appendValueIfNotInitial(buf, "font-weight", vFontWeight, appended);
		}
		if (declaredSet.contains("font-size") || declaredSet.contains("line-height")
				|| declaredSet.contains("font-family") || declaredSet.contains("font-stretch")) {
			boolean not_initial_lh = isNotInitialValue(vLineHeight, "line-height");
			boolean not_initial_fsz = isNotInitialValue(vFontSize, "font-size");
			boolean not_initial_fst = isNotInitialValue(vFontStretch, "font-stretch")
					&& isFontStretchCss3(vFontStretch);
			// We only want to consider font-family as an initial value if it
			// was not specified or is a keyword.
			boolean not_initial_ff = !vFontFamily.isSystemDefault()
					&& !isEffectiveInitialKeyword(vFontFamily);
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
					return 0;
				}
				if (not_initial_fst && declaredSet.contains("font-stretch")) {
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
				buf.append("font-family:");
				buf.append(vFontFamily.getMinifiedCssText("font-family"));
				appendPriority(buf, important);
				return 0;
			}
		}
		if (!appended) {
			buf.append("normal");
		}
		// Priority
		appendPriority(buf, important);
		return 0;
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

	private void appendLonghandIfNotKeyword(StringBuilder buf, String property, Type keyword, boolean important) {
		StyleValue vLonghand = getCSSValue(property);
		if (vLonghand.getPrimitiveType() != keyword && isPropertyAssigned(property, important)) {
			buf.append(property).append(':');
			buf.append(vLonghand.getMinifiedCssText(property));
			appendPriority(buf, important);
		}
	}

	private byte checkValuesForInherit(StyleValue vFontVariantCaps, StyleValue vFontStyle, StyleValue vFontWeight,
			StyleValue vFontStretch, StyleValue vFontSize, StyleValue vFontFamily, StyleValue vLineHeight) {
		byte count = 0;
		if (isInherit(vFontVariantCaps)) {
			count++;
		}
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
		case 7:
			return 1;
		default:
			return 2;
		}
	}

	private byte checkValuesForKeyword(CSSValue.Type keyword, StyleValue vFontVariantCaps, StyleValue vFontStyle,
			StyleValue vFontWeight, StyleValue vFontStretch, StyleValue vFontSize, StyleValue vFontFamily,
			StyleValue vLineHeight) {
		byte ucount = 0;
		if (isCssValueOfType(keyword, vFontVariantCaps)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, vFontStyle)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, vFontWeight)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, vFontStretch)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, vFontSize)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, vFontFamily)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, vLineHeight)) {
			ucount++;
		}
		switch (ucount) {
		case 0:
			return 0;
		case 7:
			return 1;
		default:
			return 2;
		}
	}

	private boolean checkValuesForInitial(StyleValue vFontStyle, StyleValue vFontVariant,
			StyleValue vFontWeight, StyleValue vFontStretch, StyleValue vFontSize,
			StyleValue vFontFamily, StyleValue vLineHeight) {
		if (!isCssValueOfType(CSSValue.Type.INITIAL, vFontStyle) && isNotInitialValue(vFontStyle, "font-style")) {
			return false;
		}
		if (!isCssValueOfType(CSSValue.Type.INITIAL, vFontVariant) && isNotInitialValue(vFontVariant, "font-variant")) {
			return false;
		}
		if (!isCssValueOfType(CSSValue.Type.INITIAL, vFontWeight) && isNotInitialValue(vFontWeight, "font-weight")) {
			return false;
		}
		if (!isCssValueOfType(CSSValue.Type.INITIAL, vFontStretch) && isNotInitialValue(vFontStretch, "font-stretch")) {
			return false;
		}
		if (!isCssValueOfType(CSSValue.Type.INITIAL, vFontSize) && isNotInitialValue(vFontSize, "font-size")) {
			return false;
		}
		if (!isCssValueOfType(CSSValue.Type.INITIAL, vFontFamily) && isNotInitialValue(vFontFamily, "font-family")) {
			return false;
		}
		return isCssValueOfType(CSSValue.Type.INITIAL, vLineHeight) || !isNotInitialValue(vLineHeight, "line-height");
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

	private boolean isFontVariantSetToInitialOrCss21(Set<String> declaredSet) {
		if (!declaredSet.contains("font-variant-caps")) {
			return true;
		}
		StyleValue cssVal = getCSSValue("font-variant-caps");
		String fvcaps = cssVal.getCssText();
		if (isEffectiveInitialKeyword(cssVal) || fvcaps.equalsIgnoreCase("normal")
				|| fvcaps.equalsIgnoreCase("small-caps")) {
			return (!declaredSet.contains("font-variant-ligatures") || isInitialValue("font-variant-ligatures"))
					&& (!declaredSet.contains("font-variant-position") || isInitialValue("font-variant-position"))
					&& (!declaredSet.contains("font-variant-numeric") || isInitialValue("font-variant-numeric"))
					&& (!declaredSet.contains("font-variant-alternates") || isInitialValue("font-variant-alternates"))
					&& (!declaredSet.contains("font-variant-east-asian") || isInitialValue("font-variant-east-asian"));
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

	private void setFontVariantDone() {
		fontVariantDone = true;
		removeAssignedProperty("font-variant-ligatures");
		removeAssignedProperty("font-variant-caps");
		removeAssignedProperty("font-variant-position");
		removeAssignedProperty("font-variant-numeric");
		removeAssignedProperty("font-variant-alternates");
		removeAssignedProperty("font-variant-east-asian");
	}

	private void addPropertyIfAssigned(ShorthandBuilder builder, String property) {
		BaseCSSStyleDeclaration style = getParentStyle();
		if (style.isPropertySet(property)) {
			builder.addAssignedProperty(property, style.isPropertyImportant(property));
		}
	}
}
