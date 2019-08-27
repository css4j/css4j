/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueItem;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>transition</code> property.
 */
class TransitionShorthandSetter extends ShorthandSetter {

	private String cssText = null, minifiedCssText = null;
	private int transitionsCount = 0;

	private ValueList lstProperty = ValueList.createCSValueList();
	private ValueList lstDuration = ValueList.createCSValueList();
	private ValueList lstTiming = ValueList.createCSValueList();
	private ValueList lstDelay = ValueList.createCSValueList();

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
			case LexicalUnit.SAC_OPERATOR_COMMA:
				valueBuffer.append(',');
				miniValueBuffer.append(',');
				break;
			case LexicalUnit.SAC_OPERATOR_SLASH:
				valueBuffer.append('/');
				miniValueBuffer.append('/');
				break;
			case LexicalUnit.SAC_OPERATOR_EXP:
				valueBuffer.append('^');
				miniValueBuffer.append('^');
				break;
			default:
				ValueItem item = valueFactory.createCSSValueItem(value, true);
				AbstractCSSValue cssVal = item.getCSSValue();
				int len = valueBuffer.length();
				if (len != 0) {
					char c = valueBuffer.charAt(len -1);
					if (c != ',') {
						miniValueBuffer.append(' ');
					}
					valueBuffer.append(' ');
				}
				valueBuffer.append(cssVal.getCssText());
				miniValueBuffer.append(cssVal.getMinifiedCssText(getShorthandName()));
				if (cssVal.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
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
			if (value.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
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
				short lut = currentValue.getLexicalUnitType();
				if (lut == LexicalUnit.SAC_OPERATOR_COMMA) {
					i++;
					nextCurrentValue();
					break;
				}
				if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
					// Full layer is 'inherit'
					while (currentValue != null) {
						boolean commaFound = currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA;
						currentValue = currentValue.getNextLexicalUnit();
						if (commaFound) {
							break;
						}
					}
					i++;
					// First, clear any values set at this layer
					clearLayer(i);
					InheritValue inherit = InheritValue.getValue().asSubproperty();
					addSingleValueLayer(inherit);
					appendValueItemString(inherit);
					continue toploop;
				}
				if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
					String sv = currentValue.getStringValue().toLowerCase(Locale.ROOT);
					if ("initial".equals(sv) || "unset".equals(sv)) {
						AbstractCSSValue keyword = valueFactory.createCSSValueItem(currentValue, true).getCSSValue();
						// Full layer is 'keyword'
						while (currentValue != null) {
							boolean commaFound = currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA;
							currentValue = currentValue.getNextLexicalUnit();
							if (commaFound) {
								break;
							}
						}
						i++;
						// First, clear any values set at this layer
						clearLayer(i);
						addSingleValueLayer(keyword);
						appendValueItemString(keyword);
						continue toploop;
					}
				}
				if ((tdurUnset || tdelayUnset) && ValueFactory.isTimeSACUnit(currentValue)) {
					if (tdurUnset) {
						AbstractCSSValue value = createCSSValue("transition-duration", currentValue);
						if (value != null) {
							lstDuration.add(value);
							tdurUnset = false;
							nextCurrentValue();
							continue;
						}
					} else {
						AbstractCSSValue value = createCSSValue("transition-delay", currentValue);
						if (value != null) {
							lstDelay.add(value);
							tdelayUnset = false;
							nextCurrentValue();
							continue;
						}
					}
				}
				if (ttfUnset) {
					if (LexicalUnit.SAC_IDENT == lut) {
						if (testIdentifiers("transition-timing-function")) {
							AbstractCSSValue value = createCSSValue("transition-timing-function", currentValue);
							lstTiming.add(value);
							ttfUnset = false;
							nextCurrentValue();
							continue;
						}
					} else if (lut == LexicalUnit.SAC_FUNCTION) {
						// transition-timing-function
						AbstractCSSValue value = createCSSValue("transition-timing-function", currentValue);
						if (value != null) {
							lstTiming.add(value);
							ttfUnset = false;
							nextCurrentValue();
							continue;
						}
					}
				}
				if (tpropUnset && (lut == LexicalUnit.SAC_IDENT || lut == LexicalUnit.SAC_STRING_VALUE)) {
					// Assume a property
					AbstractCSSValue value = createCSSValue("transition-property", currentValue);
					if (!"none".equals(value.getCssText()) || transitionsCount == 1) {
						lstProperty.add(value);
						tpropUnset = false;
						nextCurrentValue();
						continue;
					} else {
						// Reset all properties: declaration is invalid
						lstProperty.clear();
						lstDuration.clear();
						lstTiming.clear();
						lstDelay.clear();
						cssText = "";
						StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
						if (errHandler != null) {
							errHandler.shorthandError("transition", "Found 'none' in a multiple declaration");
						}
						return false;
					}
				}
				// Report error
				StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					if (lut == LexicalUnit.SAC_IDENT) {
						errHandler.unknownIdentifier("transition", currentValue.getStringValue());
					} else {
						AbstractCSSValue val = createCSSValue("transition", currentValue);
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
				AbstractCSSValue value = lstProperty.item(i);
				if ("all".equals(value.getCssText())) {
					for (int j = i - 1; j != -1; j--) {
						lstDuration.set(j, lstDuration.item(i));;
						lstTiming.set(j, lstTiming.item(i));;
						lstDelay.set(j, lstDelay.item(i));;
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

	private void clearLayer(int i) {
		if (lstProperty.getLength() == i) {
			lstProperty.remove(i - 1);
		}
		if (lstDuration.getLength() == i) {
			lstDuration.remove(i - 1);
		}
		if (lstTiming.getLength() == i) {
			lstTiming.remove(i - 1);
		}
		if (lstDelay.getLength() == i) {
			lstDelay.remove(i - 1);
		}
	}

	private void addSingleValueLayer(AbstractCSSValue keyword) {
		lstProperty.add(keyword);
		lstDuration.add(keyword);
		lstTiming.add(keyword);
		lstDelay.add(keyword);
	}

}