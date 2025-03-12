/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

class OrderedTwoIdentifierShorthandSetter extends ShorthandSetter {

	final String[] subparray;

	OrderedTwoIdentifierShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
		subparray = getShorthandDatabase().getShorthandSubproperties(shorthandName);
	}

	@Override
	public short assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return 0;
		} else if (kwscan == 2) {
			return 2;
		}

		setPropertyToDefault(subparray[0]);
		setPropertyToDefault(subparray[1]);

		boolean firstPropertyUnset = true;
		while (currentValue != null) {
			if (isValidType(currentValue)) {
				if (firstPropertyUnset && setFirstValue()) {
					firstPropertyUnset = false;
				} else {
					if (setSecondValue()) {
						break;
					}
					if (isPrefixedIdentValue()) {
						setPrefixedValue(currentValue);
						return 1;
					}
					return 2;
				}
			} else {
				return 2;
			}
			nextCurrentValue();
		}

		flush();

		return 0;
	}

	boolean setFirstValue() {
		String sv = currentValue.getStringValue();
		if (getShorthandDatabase().isIdentifierValue(subparray[0], sv)) {
			StyleValue cssval = createCSSValue(subparray[0], currentValue);
			setSubpropertyValue(subparray[0], cssval);
			setSingleValueSecondProperty(cssval, sv);
			return true;
		} else {
			LexicalUnit nlu = currentValue.getNextLexicalUnit();
			if (nlu != null && nlu.getLexicalUnitType() == LexicalUnit.LexicalType.IDENT) {
				String nsv = sv + " " + nlu.getStringValue();
				if (getShorthandDatabase().isIdentifierValue(subparray[0], nsv)) {
					ValueList list = ValueList.createWSValueList();
					list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
					nextCurrentValue();
					list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
					setSubpropertyValue(subparray[0], list);
					setSingleValueSecondProperty(list, nsv);
					return true;
				}
			}
		}
		return false;
	}

	private void setSingleValueSecondProperty(StyleValue cssval, String sv) {
		if (currentValue.getNextLexicalUnit() == null && getShorthandDatabase().isIdentifierValue(subparray[1], sv)) {
			setSubpropertyValue(subparray[1], cssval.clone());
		}
	}

	boolean setSecondValue() {
		LexicalUnit nlu = currentValue.getNextLexicalUnit();
		if (nlu != null) {
			if (isValidType(nlu)) {
				ValueList list = ValueList.createWSValueList();
				list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
				nextCurrentValue();
				list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
				if (getShorthandDatabase().isIdentifierValue(subparray[1], list.getCssText())) {
					setSubpropertyValue(subparray[1], list);
					return true;
				}
			}
		} else if (getShorthandDatabase().isIdentifierValue(subparray[1], currentValue.getStringValue())) {
			setSubpropertyValue(subparray[1], createCSSValue(subparray[1], currentValue));
			return true;
		}
		return false;
	}

	boolean isValidType(LexicalUnit lu) {
		return lu.getLexicalUnitType() == LexicalUnit.LexicalType.IDENT;
	}

}
