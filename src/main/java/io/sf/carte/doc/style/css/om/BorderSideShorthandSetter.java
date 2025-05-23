/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Shorthand setter for the <code>border-top</code>,
 * <code>border-right</code>, <code>border-bottom</code> and
 * <code>border-left</code> properties.
 */
class BorderSideShorthandSetter extends ShorthandSetter {
	private final String pnameWidth;
	private final String pnameStyle;
	private final String pnameColor;

	BorderSideShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName, String side) {
		super(style, shorthandName);
		pnameWidth = "border-" + side + "-width";
		pnameStyle = "border-" + side + "-style";
		pnameColor = "border-" + side + "-color";
	}

	@Override
	protected boolean assignSubproperty(String subproperty) {
		if (pnameWidth.equals(subproperty)) {
			if ((LexicalUnit.LexicalType.IDENT == currentValue.getLexicalUnitType()
					&& testIdentifiers("border-width")) || ValueFactory.isLengthSACUnit(currentValue)) {
				StyleValue cssValue = createCSSValue(subproperty, currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if (pnameStyle.equals(subproperty)) {
			if (LexicalUnit.LexicalType.IDENT == currentValue.getLexicalUnitType()
					&& testIdentifiers("border-style")) {
				StyleValue cssValue = createCSSValue(subproperty, currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if (pnameColor.equals(subproperty) && testColor(currentValue)) {
			StyleValue cssValue = createCSSValue(subproperty, currentValue);
			setSubpropertyValue(subproperty, cssValue);
			// border-side-color must be last
			currentValue = null;
			return true;
		}
		return false;
	}

}