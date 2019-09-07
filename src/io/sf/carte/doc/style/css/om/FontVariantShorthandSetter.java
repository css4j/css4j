/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;

/**
 * Shorthand setter for the <code>font-variant</code> shorthand property.
 */
class FontVariantShorthandSetter extends ShorthandSetter {

	FontVariantShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "font-variant");
	}

	@Override
	public boolean assignSubproperties() {
		// Check whether value is 'normal' or 'none'.
		if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String text = currentValue.getStringValue();
			boolean isNone = false;
			if (text.equalsIgnoreCase("normal") || (isNone = text.equalsIgnoreCase("none"))) {
				if (currentValue.getNextLexicalUnit() == null) {
					IdentifierValue normal = new IdentifierValue("normal");
					normal.setSubproperty(true);
					String[] sh = getShorthandSubproperties();
					for (String s : sh) {
						styleDeclaration.setProperty(s, normal, getPriority());
					}
					if (isNone) {
						IdentifierValue none = new IdentifierValue("none");
						none.setSubproperty(true);
						styleDeclaration.setProperty("font-variant-ligatures", none, getPriority());
					}
					initValueString();
					appendValueItemString(text);
					return true;
				} else {
					// 'normal' or 'none' mixed with other values
					StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
					if (errHandler != null) {
						errHandler.shorthandError(getShorthandName(),
								"Found '" + text + "' keyword mixed with other values");
					}
					return false;
				}
			}
		}
		return super.assignSubproperties();
	}

	@Override
	protected boolean assignSubproperty(String subproperty) {
		// Rest of properties
		switch (currentValue.getLexicalUnitType()) {
		case LexicalUnit.SAC_IDENT:
			if (subproperty.equals("font-variant-alternates")) {
				if (testFontVariantAlternates()) {
					return true;
				}
			}
			boolean is_set = false;
			while (currentValue != null) {
				if (testIdentifiers(subproperty)) {
					StyleValue cssValue = createCSSValue(subproperty, currentValue);
					addSubpropertyValue(subproperty, cssValue, false);
					is_set = true;
					nextCurrentValue();
					if (currentValue == null || currentValue.getLexicalUnitType() != LexicalUnit.SAC_IDENT) {
						break;
					}
				} else {
					return is_set;
				}
			}
			return is_set;
		case LexicalUnit.SAC_FUNCTION:
			if (subproperty.equals("font-variant-alternates")) {
				if (testFontVariantAlternates()) {
					return true;
				}
			}
		default:
		}
		return false;
	}

	private boolean testFontVariantAlternates() {
		boolean is_set = false;
		while (currentValue != null) {
			switch (currentValue.getLexicalUnitType()) {
			case LexicalUnit.SAC_IDENT:
				if (testIdentifiers("font-variant-alternates")) {
					StyleValue cssValue = createCSSValue("font-variant-alternates", currentValue);
					addSubpropertyValue("font-variant-alternates", cssValue, false);
					nextCurrentValue();
					is_set = true;
				} else {
					return is_set;
				}
				break;
			case LexicalUnit.SAC_FUNCTION:
				StyleValue cssValue = createCSSValue("font-variant-alternates", currentValue);
				addSubpropertyValue("font-variant-alternates", cssValue, false);
				nextCurrentValue();
				is_set = true;
			}
		}
		return is_set;
	}

}