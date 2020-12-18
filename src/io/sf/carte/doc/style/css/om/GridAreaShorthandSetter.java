/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

class GridAreaShorthandSetter extends GridPlacementShorthandSetter {

	GridAreaShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "grid-area");
	}

	@Override
	public boolean assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		setPropertyToDefault(subparray[0]);
		setPropertyToDefault(subparray[1]);
		setPropertyToDefault(subparray[2]);
		setPropertyToDefault(subparray[3]);
		// Build a list with the grid-lines found.
		ValueList gridLines = ValueList.createWSValueList();
		while (true) {
			StyleValue value = gridLine();
			if (value != null) {
				gridLines.add(value);
			} else {
				return false;
			}
			if (currentValue == null) {
				break;
			}
			nextCurrentValue();
			if (currentValue == null) {
				return false;
			}
		}
		//
		StyleValue cssval0 = gridLines.item(0);
		StyleValue cssval;
		switch (gridLines.getLength()) {
		case 1:
			StyleValue other = omittedValue(cssval0);
			setSubpropertyValue(subparray[0], cssval0);
			setSubpropertyValue(subparray[1], other);
			setSubpropertyValue(subparray[2], other);
			setSubpropertyValue(subparray[3], other);
			break;
		case 2:
			cssval = gridLines.item(1);
			other = omittedValue(cssval);
			setSubpropertyValue(subparray[0], cssval0);
			setSubpropertyValue(subparray[1], cssval);
			setSubpropertyValue(subparray[2], omittedValue(cssval0));
			setSubpropertyValue(subparray[3], other);
			break;
		case 3:
			cssval = gridLines.item(1);
			setSubpropertyValue(subparray[0], cssval0);
			setSubpropertyValue(subparray[1], cssval);
			setSubpropertyValue(subparray[2], gridLines.item(2));
			setSubpropertyValue(subparray[3], omittedValue(cssval));
			break;
		case 4:
			setSubpropertyValue(subparray[0], cssval0);
			setSubpropertyValue(subparray[1], gridLines.item(1));
			setSubpropertyValue(subparray[2], gridLines.item(2));
			setSubpropertyValue(subparray[3], gridLines.item(3));
			break;
		default:
			return false;
		}
		flush();
		return true;
	}

	private StyleValue omittedValue(StyleValue cssval) {
		StyleValue other;
		if (isIdentifier(cssval)) {
			// It must be either a custom-ident or 'auto' (in any case is what we want)
			other = cssval;
		} else {
			other = BaseGridShorthandSetter.createAutoValue();
		}
		return other;
	}

	private boolean isIdentifier(StyleValue cssval) {
		return cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) cssval).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
	}

	static IdentifierValue createAutoValue() {
		IdentifierValue ident = new IdentifierValue("auto");
		ident.setSubproperty(true);
		return ident;
	}

}
