/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Build a mask shorthand from individual properties.
 */
class MaskBuilder extends ShorthandBuilder {

	private static CSSValueSyntax lengthPercentage = new SyntaxParser()
		.parseSyntax("<length-percentage>");

	private StyleValue mskimage;
	private StyleValue mskposition;
	private StyleValue msksize;
	private StyleValue mskrepeat;
	private StyleValue mskorigin;
	private StyleValue mskclip;
	private StyleValue mskcomposite;
	private StyleValue mskmode;

	private boolean appended = false;

	MaskBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("mask", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 14;
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Append property name
		buf.append(getShorthandName()).append(':');
		// Compute property layout
		mskimage = getCSSValue("mask-image"); // master property
		mskposition = computeMultipleSubproperty("mask-image", "mask-position");
		msksize = computeMultipleSubproperty("mask-image", "mask-size");
		mskrepeat = computeMultipleSubproperty("mask-image", "mask-repeat");
		mskorigin = computeMultipleSubproperty("mask-image", "mask-origin");
		mskclip = computeMultipleSubproperty("mask-image", "mask-clip");
		mskcomposite = computeMultipleSubproperty("mask-image", "mask-composite");
		mskmode = computeMultipleSubproperty("mask-image", "mask-mode");
		//
		// Determine whether it is a layered shorthand property
		CssType type = mskimage.getCssValueType();
		if (type == CssType.LIST && ((ValueList) mskimage).isCommaSeparated()) {
			// Layered
			if (!appendLayered(buf, declaredSet, ((ValueList) mskimage).getLength())) {
				return false;
			}
		} else {
			byte inheritcheck = checkForCssKeyword(Type.INHERIT);
			if (inheritcheck == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				BufferSimpleWriter wri = new BufferSimpleWriter(buf);
				DeclarationFormattingContext context = getParentStyle().getFormattingContext();
				serializeMaskBorderIfNot(Type.INHERIT, wri, context, important);
				return true;
			} else if (inheritcheck == 2) {
				// Only some values are inherit, no shorthand possible
				return false;
			}
			byte check = checkForCssKeyword(Type.REVERT);
			if (check == 1) {
				// All values are revert
				buf.append("revert");
				appendPriority(buf, important);
				BufferSimpleWriter wri = new BufferSimpleWriter(buf);
				DeclarationFormattingContext context = getParentStyle().getFormattingContext();
				serializeMaskBorderIfNot(Type.REVERT, wri, context, important);
				return true;
			} else if (check == 2) {
				return false;
			}
			if (!appendImage(buf, mskimage)) {
				return false;
			}
			if (!appendSingleLayer(buf, declaredSet)) {
				return false;
			}
			if (!appended) {
				buf.append("none");
			}
		}
		appendPriority(buf, important);

		// Now mask-border
		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		serializeMaskBorderIfNotInitial(wri, context, important);

		return true;
	}

	private StyleValue computeMultipleSubproperty(String masterProperty, String propertyName) {
		return getParentStyle().computeBoundProperty(masterProperty, propertyName,
			getCSSValue(propertyName));
	}

	private byte checkForCssKeyword(CSSValue.Type keyword) {
		byte ucount = 0;
		if (isCssValueOfType(keyword, mskimage)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, mskposition)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, msksize)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, mskrepeat)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, mskorigin)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, mskclip)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, mskcomposite)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, mskmode)) {
			ucount++;
		}
		switch (ucount) {
		case 0:
			return 0;
		case 8:
			return 1;
		default:
			return 2;
		}
	}

	private void appendText(StringBuilder buf, String text) {
		if (appended) {
			buf.append(' ');
		} else {
			appended = true;
		}
		buf.append(text);
	}

	private boolean appendLayered(StringBuilder buf, Set<String> declaredSet, int listlen) {
		int szm1 = listlen - 1;
		if (!appendLayer(buf, declaredSet, 0, szm1)) {
			return false;
		}
		for (int i = 1; i <= szm1; i++) {
			appended = false;
			buf.append(',');
			if (!appendLayer(buf, declaredSet, i, szm1)) {
				return false;
			}
		}
		return true;
	}

	private boolean appendLayer(StringBuilder buf, Set<String> declaredSet, int index,
			int lastIndex) {
		if (declaredSet.contains("mask-image")
				&& !appendImage(buf, ((ValueList) mskimage).item(index))) {
			return false;
		}
		ValueList list = (ValueList) mskposition;
		StyleValue posval;
		if (declaredSet.contains("mask-position")) {
			posval = list.item(index);
		} else {
			posval = null;
		}
		StyleValue sizeval;
		if (declaredSet.contains("mask-size")) {
			sizeval = ((ValueList) msksize).item(index);
		} else {
			sizeval = null;
		}
		if (!appendPositionSize(buf, posval, sizeval)) {
			return false;
		}
		if (declaredSet.contains("mask-repeat")
			&& !appendRepeat(buf, ((ValueList) mskrepeat).item(index))) {
			return false;
		}
		boolean bcset = declaredSet.contains("mask-clip");
		if (declaredSet.contains("mask-origin") || bcset) {
			StyleValue origin = ((ValueList) mskorigin).item(index);
			StyleValue clip;
			if (bcset) {
				clip = ((ValueList) mskclip).item(index);
			} else {
				clip = null;
			}
			if (!appendOriginClip(buf, origin, clip)) {
				return false;
			}
		}
		if (declaredSet.contains("mask-composite")) {
			list = (ValueList) mskcomposite;
			if (!appendNonInheritedPty(buf, list.item(index), "mask-composite")) {
				return false;
			}
		}
		if (declaredSet.contains("mask-mode")) {
			list = (ValueList) mskmode;
			if (!appendNonInheritedPty(buf, list.item(index), "mask-mode")) {
				return false;
			}
		}
		int buflen = buf.length();
		if (buflen == 5 || buf.charAt(buflen - 1) == ',') {
			buf.append("none");
		}
		return true;
	}

	private boolean appendSingleLayer(StringBuilder buf, Set<String> declaredSet) {
		StyleValue posval;
		if (declaredSet.contains("mask-position")) {
			posval = valueOrFirstItem(mskposition);
		} else {
			posval = null;
		}
		StyleValue sizeval;
		if (declaredSet.contains("mask-size")) {
			sizeval = valueOrFirstItem(msksize);
		} else {
			sizeval = null;
		}
		if (!appendPositionSize(buf, posval, sizeval)) {
			return false;
		}
		if (declaredSet.contains("mask-repeat")
			&& !appendRepeat(buf, valueOrFirstItem(mskrepeat))) {
			return false;
		}
		boolean bcset = declaredSet.contains("mask-clip");
		if (declaredSet.contains("mask-origin") || bcset) {
			StyleValue origin = valueOrFirstItem(mskorigin);
			StyleValue clip;
			if (bcset) {
				clip = valueOrFirstItem(mskclip);
			} else {
				clip = null;
			}
			if (!appendOriginClip(buf, origin, clip)) {
				return false;
			}
		}
		if (declaredSet.contains("mask-composite")
			&& !appendNonInheritedPty(buf, valueOrFirstItem(mskcomposite), "mask-composite")) {
			return false;
		}
		if (declaredSet.contains("mask-mode")
			&& !appendNonInheritedPty(buf, valueOrFirstItem(mskmode), "mask-mode")) {
			return false;
		}
		return true;
	}

	private StyleValue valueOrFirstItem(StyleValue value) {
		if (value.getCssValueType() == CssType.LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				return list.item(0);
			}
		}
		return value;
	}

	private boolean appendImage(StringBuilder buf, StyleValue value) {
		if (!isRevertValue(value) && shorthandMaskImage(value)) {
			appended = appendImage(buf, appended, value);
			return true;
		}
		return false;
	}

	/**
	 * Check whether the given value is compatible with being an image that could be
	 * used to produce a shorthand.
	 * 
	 * @param value the value.
	 * @return false if it is not an image or image reference, or if the value is a
	 *         proxy that could be an image but not necessarily.
	 */
	private boolean shorthandMaskImage(StyleValue value) {
		CssType category = value.getCssValueType();
		if (category == CssType.TYPED) {
			TypedValue primi = (TypedValue) value;
			Type type = primi.getPrimitiveType();
			if (type == Type.IDENT) {
				String s = primi.getStringValue();
				return "none".equalsIgnoreCase(s);
			} else {
				return isImagePrimitiveValue(primi);
			}
		} else if (category == CssType.KEYWORD) {
			return true;
		}
		if (value.getPrimitiveType() == Type.LEXICAL
			&& ((LexicalValue) value).getFinalType() == Type.GRADIENT) {
			return true;
		}
		return false;
	}

	private boolean appendPositionSize(StringBuilder buf, StyleValue posvalue,
		StyleValue sizevalue) {
		boolean appended = false;
		if (posvalue != null) {
			CssType type = posvalue.getCssValueType();
			String text = posvalue.getCssText().toLowerCase(Locale.ROOT);
			if (isRevertValue(posvalue)) {
				return false;
			} else if (type == CssType.TYPED) {
				appendText(buf, text);
				appended = true;
			} else if (type == CssType.LIST) {
				ValueList list = (ValueList) posvalue;
				if (list.isCommaSeparated() || text.indexOf('\\') != -1) {
					// Either is comma separated or contains a hack
					return false;
				}
				if (!"0% 0%".equals(text) && !"left top".equals(text) && !"top left".equals(text)) {
					if (list.getLength() != 2 || !"center".equals(list.item(1).getCssText())) {
						appendText(buf, text);
					} else {
						appendText(buf, list.item(0).getCssText());
					}
					appended = true;
				}
			}
		}
		// Background-size
		if (sizevalue != null) {
			if (!isRevertValue(sizevalue) && !isUnknownIdentifier("mask-size", sizevalue)) {
				String text = sizevalue.getMinifiedCssText("mask-size").toLowerCase(Locale.ROOT);
				if (!"auto".equals(text) && !"auto auto".equals(text) && !"initial".equals(text)
					&& !"unset".equals(text)) {
					if (!appended) {
						if (posvalue == null) {
							posvalue = getCSSValue("mask-position");
						}
						appendText(buf, posvalue.getMinifiedCssText("mask-position"));
					}
					buf.append('/').append(text);
					appended = true;
				}
			} else {
				return false;
			}
		}
		if (appended) {
			this.appended = true;
		}
		return true;
	}

	private boolean appendRepeat(StringBuilder buf, StyleValue value) {
		CssType type = value.getCssValueType();
		String text = value.getCssText().toLowerCase(Locale.ROOT);
		if (!isIdentOrKeyword(value) || isUnknownIdentifier("mask-repeat", value)
			|| isRevertValue(value) || text.indexOf('\\') != -1) {
			// Either 'unset', wrong type, wrong identifier or contains a hack
			return false;
		} else if (type == CssType.TYPED) {
			if (!"repeat".equals(text)) {
				appendText(buf, text);
			}
		} else if (type == CssType.LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				return false;
			}
			if (!"repeat repeat".equals(text)) {
				if ("no-repeat no-repeat".equals(text)) {
					appendText(buf, "no-repeat");
				} else if ("space space".equals(text)) {
					appendText(buf, "space");
				} else if ("round round".equals(text)) {
					appendText(buf, "round");
				} else if ("repeat no-repeat".equals(text)) {
					appendText(buf, "repeat-x");
				} else if ("no-repeat repeat".equals(text)) {
					appendText(buf, "repeat-y");
				} else {
					// Should not happen, but just in case
					appendText(buf, text);
				}
			}
		}
		return true;
	}

	private boolean appendNonInheritedPty(StringBuilder buf, StyleValue value,
		String propertyName) {
		if (!isRevertValue(value) && !isUnknownIdentifier(propertyName, value)
			&& (value.getCssValueType() != CSSValue.CssType.PROXY
				|| value.matches(lengthPercentage) == Match.TRUE)) {
			String text = value.getMinifiedCssText(propertyName).toLowerCase(Locale.ROOT);
			if (isNotInitialValue(value, propertyName) && !"initial".equals(text)
				&& !"unset".equals(text)) {
				appendText(buf, text);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean appendOriginClip(StringBuilder buf, StyleValue origin, StyleValue clip) {
		/*
		 * "If one <box> value is present then it sets both mask-origin and mask-clip to
		 * that value. If two values are present, then the first sets mask-origin and
		 * the second mask-clip."
		 */
		boolean clipIsInitial;
		String cliptext = null;
		if (clip == null) {
			clipIsInitial = true;
		} else {
			cliptext = clip.getMinifiedCssText("mask-clip").toLowerCase(Locale.ROOT);
			if ("border-box".equals(cliptext) || "initial".equals(cliptext)
				|| "unset".equals(cliptext)) {
				clipIsInitial = true;
			} else {
				clipIsInitial = false;
			}
		}
		String originText = origin.getCssText().toLowerCase(Locale.ROOT);
		if (isIdentOrKeyword(origin) && !isRevertValue(origin)
			&& !isUnknownIdentifier("mask-origin", origin)) {
			if ((clip != null && !clipIsInitial) || (!"border-box".equals(originText)
				&& !"initial".equals(originText) && !"unset".equals(originText))) {
				appendText(buf, originText);
			}
		} else {
			return false;
		}
		if (isIdentOrKeyword(clip) && !isRevertValue(clip)
			&& !isUnknownIdentifier("mask-clip", clip)) {
			if (!clipIsInitial && !originText.equalsIgnoreCase(cliptext)) {
				appendText(buf, cliptext);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean isRevertValue(StyleValue cssValue) {
		return isCssValueOfType(CSSValue.Type.REVERT, cssValue);
	}

	private void serializeMaskBorderIfNot(Type keyword, BufferSimpleWriter wri,
		DeclarationFormattingContext context, boolean important) {
		appendDeclarationIfNotKeyword(keyword, wri, context, "mask-border-source", important);
		appendDeclarationIfNotKeyword(keyword, wri, context, "mask-border-slice", important);
		appendDeclarationIfNotKeyword(keyword, wri, context, "mask-border-width", important);
		appendDeclarationIfNotKeyword(keyword, wri, context, "mask-border-outset", important);
		appendDeclarationIfNotKeyword(keyword, wri, context, "mask-border-repeat", important);
		appendDeclarationIfNotKeyword(keyword, wri, context, "mask-border-mode", important);
	}

	private void serializeMaskBorderIfNotInitial(BufferSimpleWriter wri,
		DeclarationFormattingContext context, boolean important) {
		appendDeclarationIfNotInitial(wri, context, "mask-border-source", important);
		appendDeclarationIfNotInitial(wri, context, "mask-border-slice", important);
		appendDeclarationIfNotInitial(wri, context, "mask-border-width", important);
		appendDeclarationIfNotInitial(wri, context, "mask-border-outset", important);
		appendDeclarationIfNotInitial(wri, context, "mask-border-repeat", important);
		appendDeclarationIfNotInitial(wri, context, "mask-border-mode", important);
	}

}
