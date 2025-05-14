/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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
	public short assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return 0;
		} else if (kwscan == 2) {
			return 2;
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
			return 2;
		}

		flush();

		return 0;
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