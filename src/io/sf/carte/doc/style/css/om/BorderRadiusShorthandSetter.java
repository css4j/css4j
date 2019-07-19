/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for 'border-radius'.
 */
class BorderRadiusShorthandSetter extends ShorthandSetter {

	BorderRadiusShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "border-radius");
	}

	@Override
	public boolean assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		AbstractCSSValue topLeftValue = createBorderRadiusValue(null);
		AbstractCSSValue topRightValue = createBorderRadiusValue(topLeftValue);
		AbstractCSSValue bottomRightValue = createBorderRadiusValue(topLeftValue);
		AbstractCSSValue bottomLeftValue = createBorderRadiusValue(topRightValue);
		if (topRightValue == null || bottomRightValue == null || bottomLeftValue == null) {
			return false;
		}
		if (currentValue != null) {
			if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH) {
				nextCurrentValue();
				AbstractCSSValue topLeftValue2 = createBorderRadiusValue(null);
				AbstractCSSValue topRightValue2 = createBorderRadiusValue(topLeftValue2);
				AbstractCSSValue bottomRightValue2 = createBorderRadiusValue(topLeftValue2);
				AbstractCSSValue bottomLeftValue2 = createBorderRadiusValue(topRightValue2);
				if (topRightValue2 == null || bottomRightValue2 == null || bottomLeftValue2 == null) {
					return false;
				}
				topLeftValue = valuesToList(topLeftValue, topLeftValue2);
				topRightValue = valuesToList(topRightValue, topRightValue2);
				bottomRightValue = valuesToList(bottomRightValue, bottomRightValue2);
				bottomLeftValue = valuesToList(bottomLeftValue, bottomLeftValue2);
			} else {
				return false;
			}
		}
		setSubpropertyValue("border-top-left-radius", topLeftValue);
		setSubpropertyValue("border-top-right-radius", topRightValue);
		setSubpropertyValue("border-bottom-right-radius", bottomRightValue);
		setSubpropertyValue("border-bottom-left-radius", bottomLeftValue);
		flush();
		return true;
	}

	private AbstractCSSValue valuesToList(AbstractCSSValue value, AbstractCSSValue value2) {
		ValueList list = ValueList.createWSValueList();
		list.add(value);
		list.add(value2);
		return list;
	}

	private AbstractCSSValue createBorderRadiusValue(AbstractCSSValue defval) {
		if (currentValue == null) {
			return defval;
		} else {
			if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH) {
				return defval;
			} else if (ValueFactory.isSizeSACUnit(currentValue) || isCustomProperty()) {
				AbstractCSSValue value = createCSSValue(getShorthandName(), currentValue);
				nextCurrentValue();
				return value;
			} else {
				StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					if (!hasCompatValue()) {
						errHandler.shorthandError(getShorthandName(),
							"Wrong border-radius value: " + currentValue.toString());
					}
				}
			}
		}
		return null;
	}

	private boolean isCustomProperty() {
		return currentValue.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION
				&& "var".equals(currentValue.getFunctionName());
	}

}