/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Build a background shorthand from individual properties.
 */
class BackgroundBuilder extends ShorthandBuilder {

	private static CSSValueSyntax lengthPercentage = new SyntaxParser()
		.parseSyntax("<length-percentage>");

	private StyleValue bgimage;
	private StyleValue bgposition;
	private StyleValue bgsize;
	private StyleValue bgrepeat;
	private StyleValue bgattachment;
	private StyleValue bgclip;
	private StyleValue bgorigin;

	private boolean appended = false;

	BackgroundBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("background", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 8;
	}

	@Override
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Append property name
		buf.append(getShorthandName()).append(':');

		// Compute property layout
		bgimage = getCSSValue("background-image"); // master property
		bgposition = computeMultipleSubproperty("background-image", "background-position");
		bgsize = computeMultipleSubproperty("background-image", "background-size");
		bgrepeat = computeMultipleSubproperty("background-image", "background-repeat");
		bgattachment = computeMultipleSubproperty("background-image", "background-attachment");
		bgclip = computeMultipleSubproperty("background-image", "background-clip");
		bgorigin = computeMultipleSubproperty("background-image", "background-origin");

		// Determine whether it is a layered shorthand property
		CssType type = bgimage.getCssValueType();
		if (type == CssType.LIST && ((ValueList) bgimage).isCommaSeparated()) {
			// Layered
			if (!appendLayeredBackground(buf, declaredSet, ((ValueList) bgimage).getLength())) {
				return 1;
			}
		} else {
			byte inheritcheck = checkForInherit();
			if (inheritcheck == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return 0;
			} else if (inheritcheck == 2) {
				// Only some values are inherit, no shorthand possible
				return 1;
			}
			byte check = checkForRevert();
			if (check == 1) {
				// All values are revert
				buf.append("revert");
				appendPriority(buf, important);
				return 0;
			} else if (check == 2) {
				return 1;
			}
			if (!appendBackgroundImage(buf, bgimage)) {
				return 1;
			}
			if (!appendSingleLayer(buf, declaredSet)) {
				return 1;
			}
			if (!appended) {
				buf.append("none");
			}
		}

		appendPriority(buf, important);

		return 0;
	}

	private StyleValue computeMultipleSubproperty(String masterProperty, String propertyName) {
		return getParentStyle().computeBoundProperty(masterProperty, propertyName,
			getCSSValue(propertyName));
	}

	private byte checkForInherit() {
		return checkForInherit(bgimage, bgposition, bgsize, bgrepeat, bgattachment, bgclip, bgorigin,
				getCSSValue("background-color"));
	}

	private byte checkForInherit(StyleValue bimg, StyleValue bpos, StyleValue bsize,
			StyleValue brepeat, StyleValue battach, StyleValue bclip, StyleValue borigin,
			StyleValue bcolor) {
		byte check = checkForInherit(bimg, bpos, bsize, brepeat, battach, bclip, borigin);
		if (check == 2) {
			return 2;
		}

		if (isInherit(bcolor)) {
			if (check == 0) {
				return 2;
			}
		} else if (check == 1) {
			return 2;
		}

		return check;
	}

	private byte checkForInherit(StyleValue bimg, StyleValue bpos, StyleValue bsize,
			StyleValue brepeat, StyleValue battach, StyleValue bclip, StyleValue borigin) {
		byte count = 0;
		if (isInherit(bimg)) {
			count++;
		}
		if (isInherit(bpos)) {
			count++;
		}
		if (isInherit(bsize)) {
			count++;
		}
		if (isInherit(brepeat)) {
			count++;
		}
		if (isInherit(battach)) {
			count++;
		}
		if (isInherit(bclip)) {
			count++;
		}
		if (isInherit(borigin)) {
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

	private byte checkForInherit(int layerIdx, int lastIndex) {
		if (layerIdx != lastIndex) {
			return checkForInherit(((ValueList) bgimage).item(layerIdx),
					((ValueList) bgposition).item(layerIdx), ((ValueList) bgsize).item(layerIdx),
					((ValueList) bgrepeat).item(layerIdx), ((ValueList) bgattachment).item(layerIdx),
					((ValueList) bgclip).item(layerIdx), ((ValueList) bgorigin).item(layerIdx));
		} else {
			return checkForInherit(((ValueList) bgimage).item(layerIdx),
					((ValueList) bgposition).item(layerIdx), ((ValueList) bgsize).item(layerIdx),
					((ValueList) bgrepeat).item(layerIdx), ((ValueList) bgattachment).item(layerIdx),
					((ValueList) bgclip).item(layerIdx), ((ValueList) bgorigin).item(layerIdx),
					getCSSValue("background-color"));
		}
	}

	private byte checkForRevert() {
		return checkForCssKeyword(CSSValue.Type.REVERT);
	}

	private byte checkForCssKeyword(CSSValue.Type keyword) {
		byte ucount = 0;
		if (isCssValueOfType(keyword, bgposition)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, bgsize)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, bgrepeat)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, bgattachment)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, bgclip)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, bgorigin)) {
			ucount++;
		}
		if (isCssValueOfType(keyword, getCSSValue("background-color"))) {
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

	private byte checkForRevert(int layerIdx, int lastIndex) {
		return checkForCssKeyword(CSSValue.Type.REVERT, layerIdx, lastIndex);
	}

	private byte checkForCssKeyword(CSSValue.Type keyword, int layerIdx, int lastIndex) {
		byte ucount = 0;
		if (isCssValueOfType(keyword, ((ValueList) bgposition).item(layerIdx))) {
			ucount++;
		}
		if (isCssValueOfType(keyword, ((ValueList) bgsize).item(layerIdx))) {
			ucount++;
		}
		if (isCssValueOfType(keyword, ((ValueList) bgrepeat).item(layerIdx))) {
			ucount++;
		}
		if (isCssValueOfType(keyword, ((ValueList) bgattachment).item(layerIdx))) {
			ucount++;
		}
		if (isCssValueOfType(keyword, ((ValueList) bgclip).item(layerIdx))) {
			ucount++;
		}
		if (isCssValueOfType(keyword, ((ValueList) bgorigin).item(layerIdx))) {
			ucount++;
		}

		final byte fullset;
		if (layerIdx != lastIndex) {
			fullset = 6;
		} else {
			fullset = 7;
			if (isCssValueOfType(keyword, getCSSValue("background-color"))) {
				ucount++;
			}
		}

		if (ucount == 0) {
			return 0;
		} else if (ucount == fullset) {
			return 1;
		} else {
			return 2;
		}
	}

	private static boolean isRevertValue(StyleValue cssValue) {
		return isCssValueOfType(CSSValue.Type.REVERT, cssValue);
	}

	private void appendText(StringBuilder buf, String text) {
		if (appended) {
			buf.append(' ');
		} else {
			appended = true;
		}
		buf.append(text);
	}

	private boolean appendLayeredBackground(StringBuilder buf, Set<String> declaredSet, int listlen) {
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

	private boolean appendLayer(StringBuilder buf, Set<String> declaredSet, int index, int lastIndex) {
		byte check = checkForInherit(index, lastIndex);
		if (check == 1) {
			appendText(buf, "inherit");
			return true;
		} else if (check == 2) {
			return false;
		}

		check = checkForRevert(index, lastIndex);
		if (check == 1) {
			appendText(buf, "revert");
			return true;
		} else if (check == 2) {
			return false;
		}

		if (declaredSet.contains("background-image")
				&& !appendBackgroundImage(buf, ((ValueList) bgimage).item(index))) {
			return false;
		}

		ValueList list = (ValueList) bgposition;
		StyleValue posval;
		if (declaredSet.contains("background-position")) {
			posval = list.item(index);
		} else {
			posval = null;
		}

		StyleValue sizeval;
		if (declaredSet.contains("background-size")) {
			sizeval = ((ValueList) bgsize).item(index);
		} else {
			sizeval = null;
		}

		if (!appendBackgroundPositionSize(buf, posval, sizeval)) {
			return false;
		}

		if (declaredSet.contains("background-repeat")
				&& !appendBackgroundRepeat(buf, ((ValueList) bgrepeat).item(index))) {
			return false;
		}

		if (declaredSet.contains("background-attachment")) {
			list = (ValueList) bgattachment;
			if (!appendBackgroundAttachment(buf, list.item(index))) {
				return false;
			}
		}

		boolean bcset = declaredSet.contains("background-clip");
		if (declaredSet.contains("background-origin") || bcset) {
			StyleValue origin = ((ValueList) bgorigin).item(index);
			StyleValue clip;
			if (bcset) {
				clip = ((ValueList) bgclip).item(index);
			} else {
				clip = null;
			}
			if (!appendBackgroundOriginClip(buf, origin, clip)) {
				return false;
			}
		}

		if (declaredSet.contains("background-color") && index == lastIndex
				&& !appendBackgroundColor(buf, getCSSValue("background-color"))) {
			return false;
		}

		int buflen = buf.length();
		if (buflen == 11 || buf.charAt(buflen - 1) == ',') {
			buf.append("none");
		}

		return true;
	}

	private boolean appendSingleLayer(StringBuilder buf, Set<String> declaredSet) {
		StyleValue posval;
		if (declaredSet.contains("background-position")) {
			posval = valueOrFirstItem(bgposition);
		} else {
			posval = null;
		}

		StyleValue sizeval;
		if (declaredSet.contains("background-size")) {
			sizeval = valueOrFirstItem(bgsize);
		} else {
			sizeval = null;
		}

		if (!appendBackgroundPositionSize(buf, posval, sizeval)) {
			return false;
		}

		if (declaredSet.contains("background-repeat") && !appendBackgroundRepeat(buf, valueOrFirstItem(bgrepeat))) {
			return false;
		}

		if (declaredSet.contains("background-attachment") && !appendBackgroundAttachment(buf, valueOrFirstItem(bgattachment))) {
			return false;
		}

		boolean bcset = declaredSet.contains("background-clip");
		if (declaredSet.contains("background-origin") || bcset) {
			StyleValue origin = valueOrFirstItem(bgorigin);
			StyleValue clip;
			if (bcset) {
				clip = valueOrFirstItem(bgclip);
			} else {
				clip = null;
			}
			if (!appendBackgroundOriginClip(buf, origin, clip)) {
				return false;
			}
		}

		if (declaredSet.contains("background-color")) {
			StyleValue value = getCSSValue("background-color");
			if (value.getCssValueType() == CssType.LIST) {
				return false;
			}
			return appendBackgroundColor(buf, value);
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

	private boolean appendBackgroundImage(StringBuilder buf, StyleValue value) {
		if (!isRevertValue(value) && possibleBackgroundImage(value)) {
			appended = appendImage(buf, appended, value);
			return true;
		}
		return false;
	}

	private boolean possibleBackgroundImage(StyleValue value) {
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

		return false;
	}

	private boolean appendBackgroundPositionSize(StringBuilder buf, StyleValue posvalue,
			StyleValue sizevalue) {
		boolean appended = false;
		if (posvalue != null) {
			CssType type = posvalue.getCssValueType();
			String text = posvalue.getMinifiedCssText().toLowerCase(Locale.ROOT);
			if (isRevertValue(posvalue)) {
				return false;
			} else if (type == CssType.TYPED) {
				if ((posvalue.getPrimitiveType() != Type.IDENT
					|| isUnknownIdentifier("background-position", posvalue))
					&& posvalue.matches(lengthPercentage) != Match.TRUE) {
					return false;
				}
				appendText(buf, text);
				appended = true;
			} else if (type == CssType.LIST) {
				ValueList list = (ValueList) posvalue;
				if (list.isCommaSeparated() || text.indexOf('\\') != -1) {
					// Either is comma separated or contains a hack
					return false;
				}
				if (!"0% 0%".equals(text) && !"left top".equals(text) && !"top left".equals(text)) {
					if (list.getLength() != 2 || !"center".equalsIgnoreCase(list.item(1).getCssText())) {
						appendText(buf, text);
					} else {
						appendText(buf, list.item(0).getMinifiedCssText());
					}
					appended = true;
				}
			} else if (type == CssType.PROXY) {
				return false;
			}
		}

		// Background-size
		if (sizevalue != null) {
			// Sanity check
			if (isRevertValue(sizevalue) || isUnknownIdentifier("background-size", sizevalue)
				|| sizevalue.getCssValueType() == CSSValue.CssType.PROXY) {
				return false;
			}
			String text = sizevalue.getMinifiedCssText().toLowerCase(Locale.ROOT);
			if (!"auto".equals(text) && !"auto auto".equals(text) && !"initial".equals(text)
				&& !"unset".equals(text)) {
				if (!appended) {
					if (posvalue == null) {
						posvalue = getCSSValue("background-position");
					}
					appendText(buf, posvalue.getMinifiedCssText());
				}
				buf.append('/').append(text);
				appended = true;
			}
		}

		if (appended) {
			this.appended = true;
		}

		return true;
	}

	private boolean appendBackgroundRepeat(StringBuilder buf, StyleValue value) {
		CssType type = value.getCssValueType();
		String text = value.getMinifiedCssText().toLowerCase(Locale.ROOT);
		if (!isIdentOrKeyword(value) || isRevertValue(value) || text.indexOf('\\') != -1) {
			// Either 'unset', wrong typed or contains a hack
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
					appendText(buf, text);
				}
			}
		}
		return true;
	}

	private boolean appendBackgroundAttachment(StringBuilder buf, StyleValue value) {
		if (isIdentOrKeyword(value) && !isRevertValue(value)
			&& !isUnknownIdentifier("background-attachment", value)) {
			String text = value.getMinifiedCssText()
				.toLowerCase(Locale.ROOT);
			if (!"scroll".equals(text) && !"initial".equals(text) && !"unset".equals(text)) {
				appendText(buf, text);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean appendBackgroundOriginClip(StringBuilder buf, StyleValue origin,
			StyleValue clip) {
		/*
		 * "If one <box> value is present then it sets both background-origin and
		 * background-clip to that value. If two values are present, then the first sets
		 * background-origin and the second background-clip."
		 */
		boolean clipIsInitial;
		String clipText;
		if (clip == null) {
			clipIsInitial = true;
			clipText = "border-box";
		} else {
			if (clip.getCssValueType() == CssType.KEYWORD) {
				switch (clip.getPrimitiveType()) {
				case INITIAL:
				case UNSET:
					clipIsInitial = true;
					// Use a canonical initial value
					clipText = "border-box";
					break;
				default:
					// inherit, revert
					return false;
				}
			} else {
				clipText = clip.getMinifiedCssText().toLowerCase(Locale.ROOT);
				clipIsInitial = "border-box".equals(clipText);
			}
		}

		boolean originIsInitial;
		String originText;
		if (origin == null) {
			originIsInitial = true;
			originText = "padding-box";
		} else {
			if (origin.getCssValueType() == CssType.KEYWORD) {
				switch (origin.getPrimitiveType()) {
				case INITIAL:
				case UNSET:
					originIsInitial = true;
					// Use a canonical initial value
					originText = "padding-box";
					break;
				default:
					// inherit, revert
					return false;
				}
			} else {
				originText = origin.getMinifiedCssText().toLowerCase(Locale.ROOT);
				originIsInitial = "padding-box".equals(originText);
			}
		}

		if (!originIsInitial || !clipIsInitial) {
			if (getShorthandDatabase().isIdentifierValue("background-origin", originText)
					&& getShorthandDatabase().isIdentifierValue("background-clip", clipText)) {
				// origin always first
				appendText(buf, originText);
				if (!originText.equals(clipText)) {
					// append clip if different from origin
					appendText(buf, clipText);
				}
			} else {
				return false;
			}
		}

		return true;
	}

	private boolean appendBackgroundColor(StringBuilder buf, StyleValue value) {
		if (!isRevertValue(value) && isValidColor(value)) {
			// Serialize value as per the formatting context
			StringBuilder sb = new StringBuilder();
			BufferSimpleWriter wri = new BufferSimpleWriter(sb);
			DeclarationFormattingContext context = getParentStyle().getFormattingContext();
			String text;
			try {
				context.writeMinifiedValue(wri, "background-color", value);
				text = sb.toString().toLowerCase(Locale.ROOT);
			} catch (Exception e) {
				text = value.getMinifiedCssText().toLowerCase(Locale.ROOT);
			}
			if (!"transparent".equals(text) && !"rgba(0,0,0,0)".equals(text)
				&& !"rgb(0 0 0/0)".equals(text) && !"initial".equals(text)
				&& !"unset".equals(text)) {
				appendText(buf, text);
			}
		} else {
			return false;
		}
		return true;
	}

	private static boolean isValidColor(CSSValue value) {
		CssType category = value.getCssValueType();
		if (category == CssType.TYPED) {
			CSSTypedValue primi = (CSSTypedValue) value;
			Type ptype = primi.getPrimitiveType();
			if (ptype == Type.COLOR || ptype == Type.COLOR_MIX) {
				return true;
			}
			if (ptype == Type.IDENT) {
				String s = primi.getStringValue().toLowerCase(Locale.ROOT);
				return ColorIdentifiers.getInstance().isColorIdentifier(s);
			}
		} else {
			return value.getCssValueType() == CssType.KEYWORD;
		}
		return false;
	}

}
