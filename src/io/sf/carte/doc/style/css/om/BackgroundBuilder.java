/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSCustomPropertyValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a background shorthand from individual properties.
 */
class BackgroundBuilder extends ShorthandBuilder {

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
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
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
		//
		// Determine whether it is a layered shorthand property
		short type = bgimage.getCssValueType();
		if (type == CSSValue.CSS_VALUE_LIST && ((ValueList) bgimage).isCommaSeparated()) {
			// Layered
			if (!appendLayeredBackground(buf, declaredSet, ((ValueList) bgimage).getLength())) {
				return false;
			}
		} else {
			byte inheritcheck = checkForInherit();
			if (inheritcheck == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return true;
			} else if (inheritcheck == 2) {
				// Only some values are inherit, no shorthand possible
				return false;
			}
			byte check = checkForUnset(buf);
			if (check == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return true;
			} else if (check == 2) {
				return false;
			}
			if (!appendBackgroundImage(buf, bgimage)) {
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
		return true;
	}

	private StyleValue computeMultipleSubproperty(String masterProperty, String propertyName) {
		return getParentStyle().computeBoundProperty(masterProperty, propertyName, getCSSValue(propertyName));
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

	private byte checkForInherit(StringBuilder buf, int layerIdx, int lastIndex) {
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

	private byte checkForUnset(StringBuilder buf) {
		return checkForCssKeyword("unset", buf);
	}

	private byte checkForCssKeyword(String keyword, StringBuilder buf) {
		byte ucount = 0;
		if (isCssKeywordValue(keyword, bgposition)) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, bgsize)) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, bgrepeat)) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, bgattachment)) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, bgclip)) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, bgorigin)) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, getCSSValue("background-color"))) {
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

	private byte checkForUnset(StringBuilder buf, int layerIdx, int lastIndex) {
		return checkForCssKeyword("unset", buf, layerIdx, lastIndex);
	}

	private byte checkForCssKeyword(String keyword, StringBuilder buf, int layerIdx, int lastIndex) {
		byte ucount = 0;
		if (isCssKeywordValue(keyword, ((ValueList) bgposition).item(layerIdx))) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, ((ValueList) bgsize).item(layerIdx))) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, ((ValueList) bgrepeat).item(layerIdx))) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, ((ValueList) bgattachment).item(layerIdx))) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, ((ValueList) bgclip).item(layerIdx))) {
			ucount++;
		}
		if (isCssKeywordValue(keyword, ((ValueList) bgorigin).item(layerIdx))) {
			ucount++;
		}
		final byte fullset;
		if (layerIdx != lastIndex) {
			fullset = 6;
		} else {
			fullset = 7;
			if (isCssKeywordValue(keyword, getCSSValue("background-color"))) {
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

	private boolean isUnsetValue(StyleValue cssValue) {
		return isCssKeywordValue("unset", cssValue);
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
		byte check = checkForInherit(buf, index, lastIndex);
		if (check == 1) {
			appendText(buf, "inherit");
			return true;
		} else if (check == 2) {
			return false;
		}
		check = checkForUnset(buf, index, lastIndex);
		if (check == 1) {
			appendText(buf, "unset");
			return true;
		} else if (check == 2) {
			return false;
		}
		if (declaredSet.contains("background-image")) {
			if (!appendBackgroundImage(buf, ((ValueList) bgimage).item(index))) {
				return false;
			}
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
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				return false;
			}
			if (!appendBackgroundColor(buf, value)) {
				return false;
			}
		}
		return true;
	}

	private StyleValue valueOrFirstItem(StyleValue value) {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				return list.item(0);
			}
		}
		return value;
	}

	private boolean appendBackgroundImage(StringBuilder buf, StyleValue value) {
		if (!isUnsetValue(value) && possibleBackgroundImage(value)) {
			appended = appendRelativeURI(buf, appended, value);
			return true;
		}
		return false;
	}

	private boolean possibleBackgroundImage(StyleValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			PrimitiveValue primi = (PrimitiveValue) value;
			short type = primi.getPrimitiveType();
			if (type == CSSPrimitiveValue.CSS_IDENT) {
				String s = primi.getStringValue();
				return "none".equalsIgnoreCase(s) || "initial".equalsIgnoreCase(s);
			} else if (isImagePrimitiveValue(primi)) {
				return true;
			}
		}
		return false;
	}

	private boolean appendBackgroundPositionSize(StringBuilder buf, StyleValue posvalue,
			StyleValue sizevalue) {
		boolean appended = false;
		if (posvalue != null) {
			short type = posvalue.getCssValueType();
			String text = posvalue.getCssText().toLowerCase(Locale.ROOT);
			if (isUnsetValue(posvalue)) {
				return false;
			} else if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
				appendText(buf, text);
				appended = true;
			} else if (type == CSSValue.CSS_VALUE_LIST) {
				ValueList list = (ValueList) posvalue;
				if (list.isCommaSeparated() || text.indexOf('\\') != -1) {
					// Either is comma separated or contains a hack
					return false;
				}
				if (!"0% 0%".equals(text) && !"left top".equals(text) && !"top left".equals(text)
						&& !"initial".equals(text)) {
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
			if (!isUnsetValue(sizevalue) && !isUnknownIdentifier("background-size", sizevalue)) {
				String text = sizevalue.getMinifiedCssText("background-size").toLowerCase(Locale.ROOT);
				if (!"auto".equals(text) && !"auto auto".equals(text) && !"initial".equals(text)) {
					if (!appended) {
						if (posvalue == null) {
							posvalue = getCSSValue("background-position");
						}
						appendText(buf, posvalue.getMinifiedCssText("background-position"));
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

	private boolean appendBackgroundRepeat(StringBuilder buf, StyleValue value) {
		short type = value.getCssValueType();
		String text = value.getCssText().toLowerCase(Locale.ROOT);
		if (isUnsetValue(value) || text.indexOf('\\') != -1) {
			// Either 'unset' or contains a hack
			return false;
		} else if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			if (!"repeat".equals(text) && !"initial".equals(text)) {
				appendText(buf, text);
			}
		} else if (type == CSSValue.CSS_VALUE_LIST) {
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
		if (!isUnsetValue(value) && !isUnknownIdentifier("background-attachment", value)) {
			String text = value.getMinifiedCssText("background-attachment").toLowerCase(Locale.ROOT);
			if (!"scroll".equals(text) && !"initial".equals(text)) {
				appendText(buf, text);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean appendBackgroundOriginClip(StringBuilder buf, StyleValue origin, StyleValue clip) {
		/*
		 * "If one <box> value is present then it sets both background-origin
		 * and background-clip to that value. If two values are present, then
		 * the first sets background-origin and the second background-clip."
		 */
		boolean clipIsInitial = false;
		String cliptext = clip.getMinifiedCssText("background-clip").toLowerCase(Locale.ROOT);
		if ("border-box".equals(cliptext) || "initial".equals(cliptext)) {
			clipIsInitial = true;
		}
		if (!isUnsetValue(origin) && !isUnknownIdentifier("background-origin", origin)) {
			String text = origin.getCssText().toLowerCase(Locale.ROOT);
			if ((clip != null && !clipIsInitial) || (!"padding-box".equals(text) && !"initial".equals(text))) {
				appendText(buf, text);
			}
		} else {
			return false;
		}
		if (!isUnsetValue(clip) && !isUnknownIdentifier("background-clip", clip)) {
			if (!clipIsInitial) {
				appendText(buf, cliptext);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean appendBackgroundColor(StringBuilder buf, StyleValue value) {
		if (!isUnsetValue(value) && isValidColor(value)) {
			String text = value.getMinifiedCssText("background-color").toLowerCase(Locale.ROOT);
			if (!"transparent".equals(text) && !"rgba(0,0,0,0)".equals(text) && !"rgb(0 0 0/0)".equals(text)
					&& !"initial".equals(text)) {
				appendText(buf, text);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean isValidColor(ExtendedCSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primi = (CSSPrimitiveValue) value;
			short ptype = primi.getPrimitiveType();
			if (ptype == CSSPrimitiveValue.CSS_RGBCOLOR) {
				return true;
			}
			if (ptype == CSSPrimitiveValue.CSS_IDENT) {
				String s = primi.getStringValue().toLowerCase(Locale.ROOT);
				return ColorIdentifiers.getInstance().isColorIdentifier(s) || "transparent".equals(s)
						|| "initial".equals(s);
			} else if (ptype == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
				CSSCustomPropertyValue custom = (CSSCustomPropertyValue) primi;
				ExtendedCSSValue fallback = custom.getFallback();
				if (fallback != null) {
					return isValidColor(fallback);
				}
			} else if (ptype == CSSPrimitiveValue2.CSS_FUNCTION) {
				String fname = primi.getStringValue();
				return "color".equalsIgnoreCase(fname);
			}
		}
		return false;
	}

}
