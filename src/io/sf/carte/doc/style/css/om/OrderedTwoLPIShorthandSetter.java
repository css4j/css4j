/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ValueFactory;

class OrderedTwoLPIShorthandSetter extends OrderedTwoIdentifierShorthandSetter {

	OrderedTwoLPIShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
	}

	@Override
	boolean setFirstValue() {
		AbstractCSSValue cssval;
		if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String sv = currentValue.getStringValue();
			PropertyDatabase pdb = getPropertyDatabase();
			if (pdb.isIdentifierValue(subparray[0], sv)) {
				cssval = createCSSValue(subparray[0], currentValue);
				setSubpropertyValue(subparray[0], cssval);
				if (currentValue.getNextLexicalUnit() == null && pdb.isIdentifierValue(subparray[1], sv)) {
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
		AbstractCSSValue cssval;
		if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String sv = currentValue.getStringValue();
			if (getPropertyDatabase().isIdentifierValue(subparray[1], sv)) {
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
