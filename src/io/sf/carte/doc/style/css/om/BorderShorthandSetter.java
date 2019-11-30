/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Shorthand setter for the <code>border</code> property.
 */
class BorderShorthandSetter extends ShorthandSetter {

	BorderShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "border");
	}

	@Override
	public boolean assignSubproperties() {
		if (super.assignSubproperties()) {
			setDeclarationPropertyToDefault("border-image-source");
			setDeclarationPropertyToDefault("border-image-slice");
			setDeclarationPropertyToDefault("border-image-width");
			setDeclarationPropertyToDefault("border-image-outset");
			setDeclarationPropertyToDefault("border-image-repeat");
			styleDeclaration.getShorthandSet().remove("border-image");
			return true;
		}
		return false;
	}

	@Override
	protected boolean assignSubproperty(String subproperty) {
		if ("border-width".equals(subproperty)) {
			if ((LexicalType.IDENT == currentValue.getLexicalUnitType() && testIdentifiers(subproperty))
					|| ValueFactory.isSizeSACUnit(currentValue)) {
				StyleValue cssValue = createCSSValue("border-width", currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if ("border-style".equals(subproperty)) {
			LexicalType utype = currentValue.getLexicalUnitType();
			if (LexicalType.IDENT == utype && testIdentifiers(subproperty)) {
				StyleValue cssValue = createCSSValue("border-style", currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if ("border-color".equals(subproperty) && BaseCSSStyleDeclaration.testColor(currentValue)) {
			StyleValue cssValue = createCSSValue("border-color", currentValue);
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

	@Override
	protected void setSubpropertyValue(String subproperty, StyleValue cssValue) {
		String[] subparray = getShorthandDatabase().getShorthandSubproperties(subproperty);
		for (int i = 0; i < subparray.length; i++) {
			if (i != 0) {
				cssValue = cssValue.clone();
			}
			super.setSubpropertyValue(subparray[i], cssValue);
		}
	}


}