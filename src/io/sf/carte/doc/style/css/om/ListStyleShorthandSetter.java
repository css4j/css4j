/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

class ListStyleShorthandSetter extends ShorthandSetter {

		ListStyleShorthandSetter(BaseCSSStyleDeclaration style) {
			super(style, "list-style");
		}

	@Override
	public boolean assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		setPropertyToDefault("list-style-image");
		setPropertyToDefault("list-style-type");
		setPropertyToDefault("list-style-position");
		boolean stylePositionUnset = true;
		boolean styleImageUnset = true;
		boolean styleTypeUnset = true;
		while (currentValue != null) {
			short lut;
			if (isImage()) {
				// list-style-image
				setSubpropertyValue("list-style-image", createCSSValue("list-style-image", currentValue));
				styleImageUnset = false;
			} else if ((lut = currentValue.getLexicalUnitType()) == LexicalUnit.SAC_IDENT) {
				// Test for list-style-type
				if (stylePositionUnset && testIdentifiers("list-style-position")) {
					setSubpropertyValue("list-style-position", createCSSValue("list-style-position", currentValue));
					stylePositionUnset = false;
				} else if (styleTypeUnset && testIdentifiers("list-style-type")) {
					setSubpropertyValue("list-style-type", createCSSValue("list-style-type", currentValue));
					styleTypeUnset = false;
				} else if ("none".equalsIgnoreCase(currentValue.getStringValue())) {
					if (styleImageUnset) {
						setSubpropertyValue("list-style-image", createCSSValue("list-style-image", currentValue));
					}
					if (styleTypeUnset) {
						setSubpropertyValue("list-style-type", createCSSValue("list-style-type", currentValue));
					}
				} else if (styleTypeUnset) {
					// Assume counter-style
					setSubpropertyValue("list-style-type", createCSSValue("list-style-type", currentValue));
					styleTypeUnset = false;
				} else {
					return false;
				}
			} else if (styleTypeUnset) {
				if (lut == LexicalUnit.SAC_STRING_VALUE || (lut == LexicalUnit.SAC_FUNCTION
						&& "symbols".equalsIgnoreCase(currentValue.getFunctionName()))) {
					setSubpropertyValue("list-style-type", createCSSValue("list-style-type", currentValue));
					styleTypeUnset = false;
				} else {
					return false;
				}
			} else {
				return false;
			}
			nextCurrentValue();
		}
		flush();
		return true;
	}

}
