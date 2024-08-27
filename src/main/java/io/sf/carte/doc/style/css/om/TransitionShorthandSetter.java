/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueItem;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>transition</code> property.
 */
class TransitionShorthandSetter extends ShorthandSetter {

	private String cssText = null, minifiedCssText = null;
	private int transitionsCount = 0;

	private final ValueList lstProperty = ValueList.createCSValueList();
	private final ValueList lstDuration = ValueList.createCSValueList();
	private final ValueList lstTiming = ValueList.createCSValueList();
	private final ValueList lstDelay = ValueList.createCSValueList();

	TransitionShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "transition");
	}

	@Override
	public void init(LexicalUnit shorthandValue, boolean important) {
		this.currentValue = shorthandValue;
		setPriority(important);
		// Set Css text
		setCssText(shorthandValue);
		// Count transitions
		countTransitions(shorthandValue);
	}

	private void setCssText(LexicalUnit value) {
		// We create a new factory to avoid warning duplication.
		StringBuilder valueBuffer = new StringBuilder(64);
		StringBuilder miniValueBuffer = new StringBuilder(64);
		do {
			switch (value.getLexicalUnitType()) {
			case OPERATOR_COMMA:
				valueBuffer.append(',');
				miniValueBuffer.append(',');
				break;
			case OPERATOR_SLASH:
				valueBuffer.append('/');
				miniValueBuffer.append('/');
				break;
			case OPERATOR_EXP:
				valueBuffer.append('^');
				miniValueBuffer.append('^');
				break;
			default:
				ValueItem item = valueFactory.createCSSValueItem(value, true);
				StyleValue cssVal = item.getCSSValue();
				int len = valueBuffer.length();
				if (len != 0) {
					char c = valueBuffer.charAt(len - 1);
					if (c != ',') {
						miniValueBuffer.append(' ');
					}
					valueBuffer.append(' ');
				}
				valueBuffer.append(cssVal.getCssText());
				miniValueBuffer.append(cssVal.getMinifiedCssText(getShorthandName()));
				if (cssVal.getCssValueType() == CssType.TYPED) {
					value = item.getNextLexicalUnit();
				} else {
					value = value.getNextLexicalUnit();
				}
				continue;
			}
			value = value.getNextLexicalUnit();
		} while (value != null);

		cssText = valueBuffer.toString();
		minifiedCssText = miniValueBuffer.toString();
	}

	@Override
	public String getCssText() {
		return cssText;
	}

	@Override
	public String getMinifiedCssText() {
		return minifiedCssText;
	}

	private void countTransitions(LexicalUnit shorthandValue) {
		transitionsCount = 0;
		int valueCount = 0;
		for (LexicalUnit value = shorthandValue; value != null; value = value.getNextLexicalUnit()) {
			if (value.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
				if (valueCount > 0) {
					transitionsCount++;
					valueCount = 0;
				}
			} else {
				valueCount++;
			}
		}
		if (valueCount > 0) {
			transitionsCount++;
		}
	}

	@Override
	public boolean assignSubproperties() {
		int i = 0;
		toploop : while (i < transitionsCount && currentValue != null) {
			boolean tpropUnset = true;
			boolean tdurUnset = true;
			boolean ttfUnset = true;
			boolean tdelayUnset = true;
			while (currentValue != null) {
				LexicalType lut = currentValue.getLexicalUnitType();
				if (lut == LexicalType.OPERATOR_COMMA) {
					i++;
					nextCurrentValue();
					break;
				}
				// If a css-wide keyword is found, set the entire layer to it
				LexicalType lutype = currentValue.getLexicalUnitType();
				if (lutype == LexicalType.INHERIT || lutype == LexicalType.REVERT) {
					if (i != 0 || !tpropUnset || !tdurUnset || !ttfUnset || !tdelayUnset
							|| currentValue.getNextLexicalUnit() != null) {
						reportDeclarationError("transition", "Found 'inherit' or 'revert' mixed with other values.");
						return false;
					}
					StyleValue keyword = valueFactory.createCSSValueItem(currentValue, true).getCSSValue();
					addSingleValueLayer(keyword);
					appendValueItemString(keyword);
					break toploop;
				}
				// If initial/unset keyword is found, set the entire layer to it
				if (lutype == LexicalType.INITIAL || lutype == LexicalType.UNSET) {
					LexicalUnit nlu;
					if (!tpropUnset || !tdurUnset || !ttfUnset || !tdelayUnset
							|| ((nlu = currentValue.getNextLexicalUnit()) != null
									&& nlu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA)) {
						reportDeclarationError("transition", "Found a keyword mixed with other values.");
						return false;
					}
					nextCurrentValue();
					continue;
				}
				if ((tdurUnset || tdelayUnset) && ValueFactory.isTimeSACUnit(currentValue)) {
					if (tdurUnset) {
						StyleValue value = createCSSValue("transition-duration", currentValue);
						if (value != null) {
							lstDuration.add(value);
							tdurUnset = false;
							nextCurrentValue();
							continue;
						}
					} else {
						StyleValue value = createCSSValue("transition-delay", currentValue);
						if (value != null) {
							lstDelay.add(value);
							tdelayUnset = false;
							nextCurrentValue();
							continue;
						}
					}
				}
				if (ttfUnset) {
					if (LexicalType.IDENT == lut) {
						if (testIdentifiers("transition-timing-function")) {
							StyleValue value = createCSSValue("transition-timing-function", currentValue);
							lstTiming.add(value);
							ttfUnset = false;
							nextCurrentValue();
							continue;
						}
					} else if (lut == LexicalType.CUBIC_BEZIER_FUNCTION || lut == LexicalType.STEPS_FUNCTION) {
						// transition-timing-function
						StyleValue value = createCSSValue("transition-timing-function", currentValue);
						if (value != null) {
							lstTiming.add(value);
							ttfUnset = false;
							nextCurrentValue();
							continue;
						}
					}
				}
				if (tpropUnset && (lut == LexicalType.IDENT || lut == LexicalType.STRING)) {
					// Assume a property
					StyleValue value = createCSSValue("transition-property", currentValue);
					if (!"none".equals(value.getCssText()) || transitionsCount == 1) {
						lstProperty.add(value);
						tpropUnset = false;
						nextCurrentValue();
						continue;
					} else {
						reportDeclarationError("transition", "Found 'none' in a multiple declaration.");
						return false;
					}
				}
				// Report error
				StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					if (lut == LexicalType.IDENT) {
						errHandler.unknownIdentifier("transition", currentValue.getStringValue());
					} else {
						StyleValue val = createCSSValue("transition", currentValue);
						errHandler.unassignedShorthandValue("transition", val.getCssText());
					}
				}
				return false;
			}
			// Reset subproperties not set by this shorthand at this layer
			if (tpropUnset) {
				lstProperty.add(defaultPropertyValue("transition-property"));
			}
			if (tdurUnset) {
				lstDuration.add(defaultPropertyValue("transition-duration"));
			}
			if (ttfUnset) {
				lstTiming.add(defaultPropertyValue("transition-timing-function"));
			}
			if (tdelayUnset) {
				lstDelay.add(defaultPropertyValue("transition-delay"));
			}
		}

		/*
		 * "If a property is specified multiple times in the value of
		 * ‘transition-property’ (either on its own, via a shorthand that
		 * contains it, or via the ‘all’ value), then the transition that
		 * starts uses the duration, delay, and timing function at the index
		 * corresponding to the last item in the value of
		 * ‘transition-property’ that calls for animating that property."
		 */
		// So if a transition-property is 'all', copy the other subproperties to
		// previous items
		int sz = lstProperty.getLength();
		if (sz > 1) {
			// Scan for 'all'
			i = 1;
			while (i < sz) {
				StyleValue value = lstProperty.item(i);
				if ("all".equals(value.getCssText())) {
					for (int j = i - 1; j != -1; j--) {
						lstDuration.set(j, lstDuration.item(i));
						lstTiming.set(j, lstTiming.item(i));
						lstDelay.set(j, lstDelay.item(i));
					}
				}
				i++;
			}
		}

		// Assign subproperties
		setSubpropertyValueWListCheck("transition-property", lstProperty);
		setSubpropertyValueWListCheck("transition-duration", lstDuration);
		setSubpropertyValueWListCheck("transition-timing-function", lstTiming);
		setSubpropertyValueWListCheck("transition-delay", lstDelay);

		flush();

		return true;
	}

	private void addSingleValueLayer(StyleValue keyword) {
		lstProperty.add(keyword);
		lstDuration.add(keyword);
		lstTiming.add(keyword);
		lstDelay.add(keyword);
	}

	@Override
	void reportDeclarationError(String propertyName, String message) {
		// Reset all properties: declaration is invalid
		lstProperty.clear();
		lstDuration.clear();
		lstTiming.clear();
		lstDelay.clear();
		cssText = "";
		super.reportDeclarationError(propertyName, message);
	}

}