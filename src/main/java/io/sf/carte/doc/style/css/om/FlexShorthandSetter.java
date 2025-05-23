/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class FlexShorthandSetter extends ShorthandSetter {

	FlexShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "flex");
	}

	@Override
	public short assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return 0;
		} else if (kwscan == 2) {
			return 2;
		}

		setPropertyToDefault("flex-grow");
		setPropertyToDefault("flex-shrink");
		setPropertyToDefault("flex-basis");

		boolean flexGrowUnset = true;
		boolean flexBasisUnset = true;
		int count = 0;
		while (currentValue != null) {
			if (count == 2) {
				return 2;
			}
			LexicalType lut = currentValue.getLexicalUnitType();
			if (flexGrowUnset) {
				if (lut == LexicalType.INTEGER) {
					int intValue = currentValue.getIntegerValue();
					if (intValue < 0) {
						return 2;
					}
					setFlexGrow(intValue);
					flexGrowUnset = false;
					count++;
					byte ret = checkFlexShrink();
					if (ret == -1) {
						return 2;
					} else {
						continue;
					}
				} else if (lut == LexicalType.REAL) {
					float floatValue = currentValue.getFloatValue();
					if (floatValue < 0f) {
						return 2;
					}
					setFlexGrow(floatValue);
					flexGrowUnset = false;
					count++;
					byte ret = checkFlexShrink();
					if (ret == -1) {
						return 2;
					} else {
						continue;
					}
				} else if (currentValue
						.shallowMatch(SyntaxParser.createSimpleSyntax("number")) == Match.TRUE) {
					setSubpropertyValue("flex-grow", createCSSValue("flex-grow", currentValue));
					flexGrowUnset = false;
					count++;
				}
			}
			if (flexBasisUnset && ValueFactory.isLengthPercentageSACUnit(currentValue)) {
				TypedValue value = (TypedValue) createCSSValue("flex-basis",
						currentValue);
				if (value.isNumberZero()) {
					NumberValue number = new NumberValue();
					number.setSubproperty(true);
					number.setIntegerValue(0);
					value = number;
				}
				setSubpropertyValue("flex-basis", value);
				count++;
				flexBasisUnset = false;
			} else if (lut == LexicalType.IDENT) {
				// 'auto' and 'none' have specific meanings
				String ident = currentValue.getStringValue();
				if ("none".equalsIgnoreCase(ident)) {
					if (flexGrowUnset) {
						setFlexGrow(0);
						setFlexShrink(0);
					}
					setFlexBasisToAuto();
					count += 2;
					flexBasisUnset = false;
				} else if ("auto".equalsIgnoreCase(ident)) {
					if (flexGrowUnset) {
						setFlexGrow(1);
						setFlexShrink(1);
					}
					setFlexBasisToAuto();
					count += 2;
					flexBasisUnset = false;
				} else if (getShorthandDatabase().isIdentifierValue("flex-basis", ident)) { // flex-basis
					setSubpropertyValue("flex-basis", createCSSValue("flex-basis", currentValue));
					count++;
					flexBasisUnset = false;
				} else if (isPrefixedIdentValue()) {
					setPrefixedValue(currentValue);
					flush();
					return 1;
				} else {
					return 2;
				}
			} else if (currentValue.getLexicalUnitType() == LexicalType.FUNCTION) {
				// fit-content(), anchor-size(), calc-size(), maybe newer functions
				setSubpropertyValue("flex-basis", createCSSValue("flex-basis", currentValue));
				count++;
				flexBasisUnset = false;
			} else if (currentValue.getLexicalUnitType() == LexicalType.PREFIXED_FUNCTION) {
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

	private void setFlexGrow(int grow) {
		NumberValue number = new NumberValue();
		number.setIntegerValue(grow);
		number.setSubproperty(true);
		setSubpropertyValue("flex-grow", number);
	}

	private void setFlexGrow(float grow) {
		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, grow);
		number.setSubproperty(true);
		setSubpropertyValue("flex-grow", number);
	}

	private byte checkFlexShrink() {
		nextCurrentValue();
		if (currentValue != null) {
			LexicalType lut = currentValue.getLexicalUnitType();
			if (lut == LexicalType.INTEGER) {
				int intValue = currentValue.getIntegerValue();
				if (intValue >= 0) {
					setFlexShrink(intValue);
					nextCurrentValue();
					return 1;
				} else {
					return -1;
				}
			} else if (lut == LexicalType.REAL) {
				float floatValue = currentValue.getFloatValue();
				if (floatValue >= 0f) {
					setFlexShrink(floatValue);
					nextCurrentValue();
					return 1;
				} else {
					return -1;
				}
			}
		}
		return 0;
	}

	private void setFlexShrink(int shrink) {
		NumberValue number = new NumberValue();
		number.setIntegerValue(shrink);
		number.setSubproperty(true);
		setSubpropertyValue("flex-shrink", number);
	}

	private void setFlexShrink(float shrink) {
		NumberValue number = new NumberValue();
		number.setFloatValue(CSSUnit.CSS_NUMBER, shrink);
		number.setSubproperty(true);
		setSubpropertyValue("flex-shrink", number);
	}

	private void setFlexBasisToAuto() {
		IdentifierValue ident = new IdentifierValue("auto");
		ident.setSubproperty(true);
		setSubpropertyValue("flex-basis", ident);
	}

	/**
	 * Adds the text representation of the current subproperty value, to be used in setting
	 * the css text for the shorthand.
	 */
	@Override
	protected void appendValueItemString() {
		if (currentValue != null) {
			StyleValue cssValue = valueFactory.createCSSValueItem(currentValue, true).getCSSValue();
			if (cssValue != null) {
				String cssText, miniCssText;
				NumberValue number;
				if (currentValue.getNextLexicalUnit() == null && cssValue instanceof NumberValue
						&& (number = (NumberValue) cssValue).isNumberZero()) {
					cssText = "0" + number.getDimensionUnitText();
					miniCssText = cssText;
				} else {
					cssText = cssValue.getCssText();
					miniCssText = cssValue.getMinifiedCssText(getShorthandName());
				}
				StringBuilder buf = getValueItemBuffer();
				StringBuilder minibuf = getValueItemBufferMini();
				int len = buf.length();
				if (len != 0) {
					minibuf.append(' ');
					buf.append(' ');
				}
				buf.append(cssText);
				minibuf.append(miniCssText);
			}
		}
	}

}
