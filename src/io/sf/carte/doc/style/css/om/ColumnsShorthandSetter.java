/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class ColumnsShorthandSetter extends ShorthandSetter {

	ColumnsShorthandSetter(BaseCSSStyleDeclaration style) {
			super(style, "columns");
		}

		@Override
		public boolean assignSubproperties() {
			byte kwscan = scanForCssWideKeywords(currentValue);
			if (kwscan == 1) {
				return true;
			} else if (kwscan == 2) {
				return false;
			}
			setPropertyToDefault("column-width");
			setPropertyToDefault("column-count");
			boolean columnWidthUnset = true;
			boolean columnCountUnset = true;
			byte count = 0;
			while (currentValue != null) {
				if (count == 2) {
					return false;
				}
				LexicalType lut = currentValue.getLexicalUnitType();
				if (columnCountUnset && lut == LexicalType.INTEGER) {
					int intValue = currentValue.getIntegerValue();
					if (intValue < 1) {
						return false;
					}
					NumberValue number = new NumberValue();
					number.setIntegerValue(intValue);
					number.setSubproperty(true);
					setSubpropertyValue("column-count", number);
					count++;
					columnCountUnset = false;
				} else if (columnWidthUnset && ValueFactory.isPositiveSizeSACUnit(currentValue)) {
					setSubpropertyValue("column-width", createCSSValue("column-width", currentValue));
					count++;
					columnWidthUnset = false;
				} else if (lut == LexicalType.IDENT) {
					// Only 'auto' is acceptable
					String ident = currentValue.getStringValue();
					if (!"auto".equalsIgnoreCase(ident)) {
						return false;
					}
					count++;
				} else {
					return false;
				}
				nextCurrentValue();
			}
			flush();
			return true;
		}

}