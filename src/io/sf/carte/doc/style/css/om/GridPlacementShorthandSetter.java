/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSIdentifierValue;
import io.sf.carte.doc.style.css.property.ValueList;

class GridPlacementShorthandSetter extends ShorthandSetter {

	final String[] subparray;

	GridPlacementShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
		subparray = getPropertyDatabase().getShorthandSubproperties(shorthandName);
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
		AbstractCSSValue firstValue = gridLine();
		if (firstValue != null) {
			setSubpropertyValue(subparray[0], firstValue);
			if (currentValue != null) {
				nextCurrentValue();
				if (currentValue != null) {
					AbstractCSSValue secondValue = gridLine();
					if (secondValue != null && currentValue == null) {
						setSubpropertyValue(subparray[1], secondValue);
						flush();
						return true;
					}
				}
			} else {
				if (firstValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE &&
						((CSSPrimitiveValue) firstValue).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
					setSubpropertyValue(subparray[1], firstValue);
				}
				flush();
				return true;
			}
		}
		return false;
	}

	AbstractCSSValue gridLine() {
		short lut = currentValue.getLexicalUnitType();
		if (lut == LexicalUnit.SAC_IDENT) {
			String sv = currentValue.getStringValue();
			if ("auto".equalsIgnoreCase(sv)) {
				nextCurrentValue();
				CSSIdentifierValue ident = new CSSIdentifierValue("auto");
				ident.setSubproperty(true);
				if (isFinalCurrentValue()) {
					return ident;
				}
			} else if ("span".equalsIgnoreCase(sv)) {
				// span && [ <integer> || <custom-ident> ]
				ValueList list = ValueList.createWSValueList();
				list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
				nextCurrentValue();
				if (currentValue != null) {
					lut = currentValue.getLexicalUnitType();
					if (lut == LexicalUnit.SAC_INTEGER || lut == LexicalUnit.SAC_IDENT) {
						list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
						nextCurrentValue();
						if (isFinalCurrentValue()) {
							return list;
						}
					}
				}
			} else { // custom-ident
				AbstractCSSValue customIdent = createCSSValue(getShorthandName(), currentValue);
				nextCurrentValue();
				if (isFinalCurrentValue()) {
					return customIdent;
				}
			}
		} else if (lut == LexicalUnit.SAC_INTEGER) {
			// <integer> && <custom-ident>?
			AbstractCSSValue cssInt = createCSSValue(getShorthandName(), currentValue);
			nextCurrentValue();
			if (isFinalCurrentValue()) {
				return cssInt;
			} else if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
				ValueList list = ValueList.createWSValueList();
				list.add(cssInt);
				list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
				nextCurrentValue();
				if (isFinalCurrentValue()) {
					return list;
				}
			}
		}
		return null;
	}

	boolean isFinalCurrentValue() {
		return currentValue == null || currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH;
	}

}
