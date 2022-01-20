/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Shorthand setter for box-style properties, with top, right, bottom and
 * left possible subproperties.
 */
class BoxShorthandSetter extends ShorthandSetter {

	boolean nonmixed = true;

	BoxShorthandSetter(BaseCSSStyleDeclaration style, String shorthand) {
		super(style, shorthand);
	}

	/*
	 * Return true if all are inherit
	 */
	boolean scanForInherited(LexicalUnit lunit) {
		while (lunit != null) {
			if (lunit.getLexicalUnitType() != LexicalUnit.SAC_INHERIT) {
				return false;
			}
			lunit = lunit.getNextLexicalUnit();
		}
		InheritValue inherit = InheritValue.getValue().asSubproperty();
		setSubpropertiesInherit(inherit);
		initValueString();
		appendValueItemString(inherit);
		return true;
	}

	@Override
	public boolean assignSubproperties() {
		// Separately handle the case of property: inherit
		if (scanForInherited(currentValue)) {
			return true;
		}
		// Get the array of shorthand sub-properties (longhands)
		// Needs to be top/right/bottom/left
		String[] subparray = getShorthandSubproperties();
		// Count valid values
		short vcount = boxValueCount(currentValue);
		// Exit if result of value count is invalid
		if (vcount == 0 || vcount > 4) {
			StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
			if (errHandler != null) {
				if (!hasCompatValue()) {
					errHandler.wrongSubpropertyCount(getShorthandName(), vcount);
				}
			}
			return false;
		}
		// Set values according to value count
		switch (vcount) {
		case 1:
			StyleValue cssValue = createCSSValue(getShorthandName(), currentValue);
			for (int i = 0; i < subparray.length; i++) {
				setSubpropertyValue(subparray[i], cssValue);
			}
			break;
		case 2:
			// top and bottom
			StyleValue top = createCSSValue(subparray[0], currentValue);
			nextCurrentValue();
			// right and left
			StyleValue right_left = createCSSValue(subparray[1], currentValue);
			// set top
			setSubpropertyValue(subparray[0], top);
			// set right
			setSubpropertyValue(subparray[1], right_left);
			// set bottom
			setSubpropertyValue(subparray[2], top);
			// set left
			setSubpropertyValue(subparray[3], right_left);
			break;
		case 3:
			// top
			top = createCSSValue(subparray[0], currentValue);
			nextCurrentValue();
			// right and left
			right_left = createCSSValue(subparray[1], currentValue);
			nextCurrentValue();
			// bottom
			StyleValue bottom = createCSSValue(subparray[2], currentValue);
			// set top
			setSubpropertyValue(subparray[0], top);
			// set right
			setSubpropertyValue(subparray[1], right_left);
			// set bottom
			setSubpropertyValue(subparray[2], bottom);
			// set left
			setSubpropertyValue(subparray[3], right_left);
			break;
		case 4:
			for (int i = 0; i < subparray.length; i++) {
				cssValue = createCSSValue(subparray[i], currentValue);
				setSubpropertyValue(subparray[i], cssValue);
				nextCurrentValue();
			}
			break;
		}
		if (!nonmixed) {
			// The mixture of keywords/values is not supported by browsers
			initValueString();
		}
		return true;
	}

	/**
	 * Counts the number of box-like values in style declarations.
	 *
	 * @param topLevelUnit
	 *            the top lexical unit for the declaration.
	 */
	short boxValueCount(LexicalUnit topLevelUnit) {
		short valueCount = 0;
		for (LexicalUnit value = topLevelUnit; value != null; value = value.getNextLexicalUnit()) {
			if (isValueOfType(value)) {
				valueCount++;
				continue;
			} else if (value.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
				String sv = value.getStringValue();
				// only auto (and css-wide keywords) for margin properties
				String lcsv = sv.toLowerCase(Locale.ROOT).intern();
				if (isIdentifierValue(lcsv)) {
					valueCount++;
					continue;
				} else if ("unset".equals(lcsv) || "initial".equals(lcsv)) {
					nonmixed = false;
					valueCount++;
					continue;
				}
			} else if (value.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
				nonmixed = false;
				valueCount++;
				continue;
			} else if (value.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION) {
				if ("var".equalsIgnoreCase(value.getStringValue())) {
					valueCount++;
					continue;
				}
			}
			// Error found
			break;
		}
		if (!nonmixed && valueCount == 1) {
			nonmixed = true;
		}
		return valueCount;
	}

	boolean isValueOfType(LexicalUnit value) {
		return ValueFactory.isSizeSACUnit(value);
	}

	boolean isIdentifierValue(String lcIdent) {
		return "auto".equals(lcIdent);
	}

	@Override
	protected StyleValue createCSSValue(String propertyName, LexicalUnit lunit) throws DOMException {
		return createCSSValue(propertyName, lunit, nonmixed);
	}

	@Override
	protected void setSubpropertyValue(String subproperty, StyleValue cssValue) {
		styleDeclaration.setProperty(subproperty, cssValue, getPriority());
	}

}