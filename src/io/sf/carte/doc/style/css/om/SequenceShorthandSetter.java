/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.StyleValue;

/**
 * Shorthand setter for the shorthand properties whose subproperties are single-value and
 * in specific order.
 */
class SequenceShorthandSetter extends ShorthandSetter {

	SequenceShorthandSetter(BaseCSSStyleDeclaration style, String shorthand) {
		super(style, shorthand);
	}

	@Override
	public boolean assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		String[] subparray = getShorthandSubproperties();
		int vcount = countValues();
		if (vcount == 2) {
			setSubpropertyValue(subparray[0], createCSSValue(subparray[0], currentValue));
			nextCurrentValue();
			setSubpropertyValue(subparray[1], createCSSValue(subparray[1], currentValue));
		} else if (vcount == 1) {
			StyleValue cssval = createCSSValue(subparray[0], currentValue);
			setSubpropertyValue(subparray[0], cssval);
			setSubpropertyValue(subparray[1], cssval);
		} else {
			return false;
		}
		flush();
		return true;
	}

	private int countValues() {
		LexicalUnit lu = currentValue;
		int count = 0;
		while (lu != null) {
			count++;
			lu = lu.getNextLexicalUnit();
		}
		return count;
	}
}