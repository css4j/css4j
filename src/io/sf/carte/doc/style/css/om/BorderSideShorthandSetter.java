/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Shorthand setter for the <code>border-top</code>,
 * <code>border-right</code>, <code>border-bottom</code> and
 * <code>border-left</code> properties.
 */
class BorderSideShorthandSetter extends ShorthandSetter {
	private String pnameWidth;
	private String pnameStyle;
	private String pnameColor;

	BorderSideShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName, String side) {
		super(style, shorthandName);
		pnameWidth = "border-" + side + "-width";
		pnameStyle = "border-" + side + "-style";
		pnameColor = "border-" + side + "-color";
	}

	@Override
	protected boolean assignSubproperty(String subproperty) {
		if (pnameWidth.equals(subproperty)) {
			if ((LexicalUnit.SAC_IDENT == currentValue.getLexicalUnitType() && testIdentifiers("border-width"))
					|| ValueFactory.isSizeSACUnit(currentValue)) {
				AbstractCSSValue cssValue = createCSSValue(subproperty, currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if (pnameStyle.equals(subproperty)) {
			if (LexicalUnit.SAC_IDENT == currentValue.getLexicalUnitType() && testIdentifiers("border-style")) {
				AbstractCSSValue cssValue = createCSSValue(subproperty, currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if (pnameColor.equals(subproperty) && BaseCSSStyleDeclaration.testColor(currentValue)) {
			AbstractCSSValue cssValue = createCSSValue(subproperty, currentValue);
			setSubpropertyValue(subproperty, cssValue);
			// border-side-color must be last
			currentValue = null;
			return true;
		}
		return false;
	}

}