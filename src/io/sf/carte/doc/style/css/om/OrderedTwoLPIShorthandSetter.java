/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class OrderedTwoLPIShorthandSetter extends OrderedTwoIdentifierShorthandSetter {

	OrderedTwoLPIShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
	}

	@Override
	boolean setFirstValue() {
		StyleValue cssval;
		if (currentValue.getLexicalUnitType() == LexicalUnit.LexicalType.IDENT) {
			String sv = currentValue.getStringValue();
			if (getShorthandDatabase().isIdentifierValue(subparray[0], sv)) {
				cssval = createCSSValue(subparray[0], currentValue);
				setSubpropertyValue(subparray[0], cssval);
				if (currentValue.getNextLexicalUnit() == null
						&& getShorthandDatabase().isIdentifierValue(subparray[1], sv)) {
					setSubpropertyValue(subparray[1], cssval.clone());
				}
				return true;
			}
		} else if (ValueFactory.isSizeSACUnit(currentValue)) {
			cssval = createCSSValue(subparray[0], currentValue);
			setSubpropertyValue(subparray[0], cssval);
			if (currentValue.getNextLexicalUnit() == null) {
				setSubpropertyValue(subparray[1], cssval.clone());
			}
			return true;
		}
		return false;
	}

	@Override
	boolean setSecondValue() {
		StyleValue cssval;
		if (currentValue.getLexicalUnitType() == LexicalUnit.LexicalType.IDENT) {
			String sv = currentValue.getStringValue();
			if (getShorthandDatabase().isIdentifierValue(subparray[1], sv)) {
				cssval = createCSSValue(subparray[1], currentValue);
				setSubpropertyValue(subparray[1], cssval);
				return true;
			}
		} else if (ValueFactory.isSizeSACUnit(currentValue)) {
			cssval = createCSSValue(subparray[1], currentValue);
			setSubpropertyValue(subparray[1], cssval);
			return true;
		}
		return false;
	}

	@Override
	boolean isValidType(LexicalUnit lu) {
		return true;
	}

}
