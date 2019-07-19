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
import io.sf.carte.doc.style.css.property.ValueList;

class OrderedTwoIdentifierShorthandSetter extends ShorthandSetter {

	final String[] subparray;

	OrderedTwoIdentifierShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
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
		boolean firstPropertyUnset = true;
		while (currentValue != null) {
			if (isValidType(currentValue)) {
				if (firstPropertyUnset && setFirstValue()) {
					firstPropertyUnset = false;
				} else {
					if (setSecondValue()) {
						break;
					}
					return false;
				}
			} else {
				return false;
			}
			nextCurrentValue();
		}
		flush();
		return true;
	}

	boolean setFirstValue() {
		String sv = currentValue.getStringValue();
		PropertyDatabase pdb = getPropertyDatabase();
		if (pdb.isIdentifierValue(subparray[0], sv)) {
			AbstractCSSValue cssval = createCSSValue(subparray[0], currentValue);
			setSubpropertyValue(subparray[0], cssval);
			setSingleValueSecondProperty(pdb, cssval, sv);
			return true;
		} else {
			LexicalUnit nlu = currentValue.getNextLexicalUnit();
			if (nlu != null && nlu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
				String nsv = sv + " " + nlu.getStringValue();
				if (pdb.isIdentifierValue(subparray[0], nsv)) {
					ValueList list = ValueList.createWSValueList();
					list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
					nextCurrentValue();
					list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
					setSubpropertyValue(subparray[0], list);
					setSingleValueSecondProperty(pdb, list, nsv);
					return true;
				}
			}
		}
		return false;
	}

	private void setSingleValueSecondProperty(PropertyDatabase pdb, AbstractCSSValue cssval, String sv) {
		if (currentValue.getNextLexicalUnit() == null && pdb.isIdentifierValue(subparray[1], sv)) {
			setSubpropertyValue(subparray[1], cssval.clone());
		}
	}

	boolean setSecondValue() {
		PropertyDatabase pdb = getPropertyDatabase();
		LexicalUnit nlu = currentValue.getNextLexicalUnit();
		if (nlu != null) {
			if (isValidType(nlu)) {
				ValueList list = ValueList.createWSValueList();
				list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
				nextCurrentValue();
				list.add(valueFactory.createCSSValueItem(currentValue, true).getCSSValue());
				if (pdb.isIdentifierValue(subparray[1], list.getCssText())) {
					setSubpropertyValue(subparray[1], list);
					return true;
				}
			}
		} else if (pdb.isIdentifierValue(subparray[1], currentValue.getStringValue())) {
			setSubpropertyValue(subparray[1], createCSSValue(subparray[1], currentValue));
			return true;
		}
		return false;
	}

	boolean isValidType(LexicalUnit lu) {
		return lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT;
	}

}
