/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class ColumnsShorthandSetter extends ShorthandSetter {

	ColumnsShorthandSetter(BaseCSSStyleDeclaration style) {
			super(style, "columns");
		}

		@Override
		public short assignSubproperties() {
			byte kwscan = scanForCssWideKeywords(currentValue);
			if (kwscan == 1) {
				return 0;
			} else if (kwscan == 2) {
				return 2;
			}

			CSSValueSyntax syntaxInteger = SyntaxParser.createSimpleSyntax("integer");

			setPropertyToDefault("column-width");
			setPropertyToDefault("column-count");

			boolean columnWidthUnset = true;
			boolean columnCountUnset = true;
			byte count = 0;
			while (currentValue != null) {
				if (count == 2) {
					return 2;
				}

				LexicalType lut;
				if (columnWidthUnset && ValueFactory.isPositiveSizeSACUnit(currentValue)) {
					setSubpropertyValue("column-width", createCSSValue("column-width", currentValue));
					count++;
					columnWidthUnset = false;
				} else if (columnCountUnset && currentValue.getLexicalUnitType() == LexicalType.INTEGER) {
					int intValue = currentValue.getIntegerValue();
					if (intValue < 1) {
						return 2;
					}
					NumberValue number = new NumberValue();
					number.setIntegerValue(intValue);
					number.setSubproperty(true);
					setSubpropertyValue("column-count", number);
					count++;
					columnCountUnset = false;
				} else if ((lut = currentValue.getLexicalUnitType()) == LexicalType.IDENT) {
					// Only 'auto' is acceptable
					String ident = currentValue.getStringValue();
					if (!"auto".equalsIgnoreCase(ident)) {
						if (isPrefixedIdentValue()) {
							setPrefixedValue(currentValue);
							flush();
							return 1;
						}
						return 2;
					}
					count++;
				} else if (columnCountUnset && currentValue.shallowMatch(syntaxInteger) == Match.TRUE) {
					setSubpropertyValue("column-count", createCSSValue("column-count", currentValue));
					count++;
					columnCountUnset = false;
				} else if (lut == LexicalType.PREFIXED_FUNCTION) {
					setPrefixedValue(currentValue);
					flush();
					return 1;
				} else {
					return 2;
				}
				nextCurrentValue();
			}

			flush();

			return 0;
		}

}
