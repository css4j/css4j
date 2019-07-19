/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>background</code> property.
 */
class BackgroundShorthandSetter extends ShorthandSetter {

	private StringBuilder layerBuffer = null, miniLayerBuffer = null;
	private int layerCount = 0;

	private ValueList lstImage = ValueList.createCSValueList();
	private ValueList lstPosition = ValueList.createCSValueList();
	private ValueList lstSize = ValueList.createCSValueList();
	private ValueList lstRepeat = ValueList.createCSValueList();
	private ValueList lstClip = ValueList.createCSValueList();
	private ValueList lstOrigin = ValueList.createCSValueList();
	private ValueList lstAttachment = ValueList.createCSValueList();

	BackgroundShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "background");
	}

	@Override
	public void init(LexicalUnit shorthandValue, boolean important) {
		this.currentValue = shorthandValue;
		setPriority(important);
		// Count layers
		countLayers(shorthandValue);
	}

	void countLayers(LexicalUnit shorthandValue) {
		layerCount = 0;
		int valueCount = 0;
		for (LexicalUnit value = shorthandValue; value != null; value = value.getNextLexicalUnit()) {
			if (value.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
				if (valueCount > 0) {
					layerCount++;
					valueCount = 0;
				}
			} else {
				valueCount++;
			}
		}
		if (valueCount > 0) {
			layerCount++;
		}
	}

	@Override
	StringBuilder getValueItemBuffer() {
		return layerBuffer;
	}

	@Override
	StringBuilder getValueItemBufferMini() {
		return miniLayerBuffer;
	}

	@Override
	public boolean assignSubproperties() {
		List<LexicalUnit> unknownValues = null;
		//
		layerBuffer = new StringBuilder(64);
		miniLayerBuffer = new StringBuilder(64);
		//
		String[] subparray = getShorthandSubproperties();
		int i = 0;
		topLoop: while (i < layerCount && currentValue != null) {
			boolean validLayer = false;
			Set<String> subp = new HashSet<String>(subparray.length);
			subp.addAll(Arrays.asList(subparray.clone()));
			valueLoop: while (currentValue != null) {
				if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
					if (validLayer) {
						i++;
						appendToValueBuffer(layerBuffer, miniLayerBuffer);
						layerBuffer.setLength(0);
						miniLayerBuffer.setLength(0);
						if (i != layerCount) {
							layerBuffer.append(',');
							miniLayerBuffer.append(',');
						}
						if (unknownValues != null) {
							reportUnknownValues(subp, unknownValues);
							unknownValues = null;
						}
					} else {
						// We do this just in case
						unknownValues = null;
						break valueLoop;
					}
					currentValue = currentValue.getNextLexicalUnit();
					break;
				}
				// If a css-wide keyword is found, set the entire layer to it
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
					clearLayer(subp, i);
					InheritValue inherit = InheritValue.getValue().asSubproperty();
					addSingleValueLayer(inherit);
					// Reset layer buffer to initial state, eventually with comma
					layerBuffer.setLength(0);
					miniLayerBuffer.setLength(0);
					if (i != 1) {
						layerBuffer.append(',');
						miniLayerBuffer.append(',');
					}
					appendValueItemString(inherit);
					appendToValueBuffer(layerBuffer, miniLayerBuffer);
					// Done with the layer
					layerBuffer.setLength(0);
					miniLayerBuffer.setLength(0);
					if (i != layerCount) {
						layerBuffer.append(',');
						miniLayerBuffer.append(',');
					}
					continue topLoop;
				}
				if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
					String sv = currentValue.getStringValue().toLowerCase(Locale.US);
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
						clearLayer(subp, i);
						addSingleValueLayer(keyword);
						// Reset layer buffer to initial state, eventually with comma
						layerBuffer.setLength(0);
						miniLayerBuffer.setLength(0);
						if (i != 1) {
							layerBuffer.append(',');
							miniLayerBuffer.append(',');
						}
						appendValueItemString(keyword);
						appendToValueBuffer(layerBuffer, miniLayerBuffer);
						// Done with the layer
						layerBuffer.setLength(0);
						miniLayerBuffer.setLength(0);
						if (i != layerCount) {
							layerBuffer.append(',');
							miniLayerBuffer.append(',');
						}
						continue topLoop;
					}
				}
				// try to assign the current lexical value to an individual
				// property ...and see the result
				switch (assignLayerValue(i, subp)) {
				case 1:
					if (i != layerCount - 1) {
						validLayer = false;
						break valueLoop;
					}
				case 0:
					validLayer = true;
					break;
				case 2:
					if (unknownValues == null) {
						unknownValues = new LinkedList<LexicalUnit>();
					}
					unknownValues.add(currentValue);
					nextCurrentValue();
				case 3:
					validLayer = false;
					break valueLoop;
				}
			}
			if (unknownValues != null) {
				handleLayerUnknownValues(subp, unknownValues);
				unknownValues = null; // help gc
			}
			if (!validLayer) {
				layerBuffer.setLength(0);
				miniLayerBuffer.setLength(0);
				StringBuilder msgbuf = new StringBuilder(64);
				msgbuf.append("Invalid layer found: ").append(i);
				if (currentValue != null) {
					msgbuf.append(' ').append(currentValue.toString());
				}
				reportDeclarationError("background", msgbuf.toString());
				return false;
			} else if (subp.size() > 0) {
				// Reset subproperties not set by this shorthand at this layer
				resetUnsetProperties(subp);
			}
		}
		appendToValueBuffer(layerBuffer, miniLayerBuffer);
		// Assign subproperties
		setListSubpropertyValue("background-image", lstImage);
		setListSubpropertyValue("background-position", lstPosition);
		setListSubpropertyValue("background-size", lstSize);
		setListSubpropertyValue("background-origin", lstOrigin);
		setListSubpropertyValue("background-clip", lstClip);
		setListSubpropertyValue("background-repeat", lstRepeat);
		setListSubpropertyValue("background-attachment", lstAttachment);
		// color
		if (!isPropertySet("background-color")) {
			AbstractCSSValue iniVal = defaultPropertyValue("background-color");
			setSubpropertyValue("background-color", iniVal);
		}
		// flush the properties
		flush();
		return true;
	}

	private void clearLayer(Set<String> subp, int i) {
		if (!subp.contains("background-image") && lstImage.getLength() == i) {
			lstImage.remove(i - 1);
		}
		if (!subp.contains("background-position") && lstPosition.getLength() == i) {
			lstPosition.remove(i - 1);
		}
		if (!subp.contains("background-size") && lstSize.getLength() == i) {
			lstSize.remove(i - 1);
		}
		if (!subp.contains("background-origin") && lstOrigin.getLength() == i) {
			lstOrigin.remove(i - 1);
		}
		if (!subp.contains("background-clip") && lstClip.getLength() == i) {
			lstClip.remove(i - 1);
		}
		if (!subp.contains("background-repeat") && lstRepeat.getLength() == i) {
			lstRepeat.remove(i - 1);
		}
		if (!subp.contains("background-attachment") && lstAttachment.getLength() == i) {
			lstAttachment.remove(i - 1);
		}
	}

	private void addSingleValueLayer(AbstractCSSValue keyword) {
		lstImage.add(keyword);
		lstPosition.add(keyword);
		lstSize.add(keyword);
		lstRepeat.add(keyword);
		lstOrigin.add(keyword);
		lstClip.add(keyword);
		lstAttachment.add(keyword);
		if (currentValue == null) {
			// Final layer
			setSubpropertyValue("background-color", keyword);
		}
	}

	/**
	 * Try to assign the current lexical value to an individual property.
	 * 
	 * @param i
	 * @param subp
	 * @return 0 if the value was successfully assigned, 1 if the assigned property was
	 *         'background-color', 2 if the current value could not be assigned, 3 if a valid
	 *         size value was not found after a slash.
	 */
	private byte assignLayerValue(int i, Set<String> subp) {
		byte retVal;
		if (subp.contains("background-color") && BaseCSSStyleDeclaration.testColor(currentValue)) {
			AbstractCSSValue cssValue = createCSSValue("background-color", currentValue);
			setSubpropertyValue("background-color", cssValue);
			subp.remove("background-color");
			// background-color means final layer
			retVal = 1;
			nextCurrentValue();
		} else if (subp.contains("background-image") && testBackgroundImage(i, subp)) {
			nextCurrentValue();
			retVal = 0;
		} else if (subp.contains("background-position") && testBackgroundPosition(lstPosition)) {
			subp.remove("background-position");
			nextCurrentValue();
			retVal = 0;
			if (currentValue != null && LexicalUnit.SAC_OPERATOR_SLASH == currentValue.getLexicalUnitType()) {
				// Size
				currentValue = currentValue.getNextLexicalUnit();
				if (currentValue != null && testBackgroundSize(i, subp)) {
					nextCurrentValue();
				} else {
					// Report error: size not found after slash.
					StyleDeclarationErrorHandler eh = styleDeclaration.getStyleDeclarationErrorHandler();
					if (eh != null) {
						eh.shorthandSyntaxError("background", "Size not found after slash");
					}
					retVal = 3;
				}
			}
		} else if (subp.contains("background-attachment")
				&& testIdentifierProperty(i, subp, "background-attachment", lstAttachment)) {
			nextCurrentValue();
			subp.remove("background-attachment");
			retVal = 0;
		} else if (subp.contains("background-repeat") && testBackgroundRepeat(lstRepeat)) {
			subp.remove("background-repeat");
			retVal = 0;
		} else if (subp.contains("background-origin")
				&& testIdentifierProperty(i, subp, "background-origin", lstOrigin)) {
			/*
			 * "If one <box> value is present then it sets both background-origin
			 * and background-clip to that value. If two values are present, then
			 * the first sets background-origin and the second background-clip."
			 */
			LexicalUnit lastValue = currentValue;
			nextCurrentValue();
			subp.remove("background-origin");
			if (currentValue != null && testIdentifierProperty(i, subp, "background-clip", lstClip)) {
				nextCurrentValue();
			} else {
				AbstractCSSValue value = createCSSValue("background-clip", lastValue);
				lstClip.add(value);
			}
			subp.remove("background-clip");
			retVal = 0;
		} else {
			retVal = 2;
		}
		return retVal;
	}

	private void handleLayerUnknownValues(Set<String> subp, List<LexicalUnit> unknownValues) {
		if (subp.size() == 1 && unknownValues.size() == 1) {
			if (lastChanceLayerAssign(subp.iterator().next(), unknownValues.get(0))) {
				subp.clear();
				return;
			}
		}
		reportUnknownValues(subp, unknownValues);
	}

	private boolean lastChanceLayerAssign(String property, LexicalUnit lUnit) {
		if (lUnit.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION && "var".equalsIgnoreCase(lUnit.getFunctionName())) {
			AbstractCSSValue cssValue = createCSSValue(property, lUnit);
			setSubpropertyValue(property, cssValue);
			return true;
		}
		return false;
	}

	private void reportUnknownValues(Set<String> subp, List<LexicalUnit> unknownValues) {
		BaseCSSDeclarationRule prule = styleDeclaration.getParentRule();
		if (prule != null) {
			StyleDeclarationErrorHandler eh = prule.getStyleDeclarationErrorHandler();
			if (unknownValues.size() == 1 && unknownValues.get(0).getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
				eh.unknownIdentifier("background", unknownValues.get(0).getStringValue());
			} else {
				eh.unassignedShorthandValues("background", subp.toArray(new String[0]),
						unknownValues.toArray(new LexicalUnit[0]));
			}
		}
	}

	private void resetUnsetProperties(Set<String> subp) {
		Iterator<String> it = subp.iterator();
		while (it.hasNext()) {
			String pname = it.next();
			AbstractCSSValue cssVal = defaultPropertyValue(pname);
			if ("background-image".equals(pname)) {
				// No background-image: "Note that a value of ‘none’ still creates a layer."
				lstImage.add(cssVal);
			} else if ("background-position".equals(pname)) {
				lstPosition.add(cssVal);
			} else if ("background-size".equals(pname)) {
				ValueList list = ValueList.createWSValueList();
				list.add(cssVal);
				list.add(cssVal);
				lstSize.add(list);
			} else if ("background-origin".equals(pname)) {
				lstOrigin.add(cssVal);
			} else if ("background-clip".equals(pname)) {
				lstClip.add(cssVal);
			} else if ("background-repeat".equals(pname)) {
				lstRepeat.add(cssVal);
			} else if ("background-attachment".equals(pname)) {
				lstAttachment.add(cssVal);
			}
		}
	}

	private void setListSubpropertyValue(String pname, ValueList list) {
		if (list.getLength() == 1) {
			AbstractCSSValue val = list.item(0);
			if (val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				((AbstractCSSPrimitiveValue) val).setSubproperty(true);
			} else if (val.getCssValueType() == CSSValue.CSS_INHERIT) {
				val = ((InheritValue) val).asSubproperty();
			} else if (val.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				((ValueList) val).setSubproperty(true);
			}
			setSubpropertyValue(pname, val);
		} else {
			list.setSubproperty(true);
			setSubpropertyValue(pname, list);
		}
	}

	@Override
	protected void nextCurrentValue() {
		// Add the value string. We do this here in case the shorthand
		// setter decides to stop setting values, to avoid having -in the
		// text representation- values that were not used as subproperties.
		appendValueItemString();
		currentValue = currentValue.getNextLexicalUnit();
	}

	private boolean testBackgroundImage(int i, Set<String> subp) {
		if (isImage()) {
			lstImage.add(createCSSValue("background-image", currentValue));
			subp.remove("background-image");
			return true;
		} else if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT
				&& "none".equals(currentValue.getStringValue())) {
			lstImage.add(createCSSValue("background-image", currentValue));
			subp.remove("background-image");
			return true;
		}
		return false;
	}

	private boolean testBackgroundPosition(ValueList posList) {
		if ((currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT && testIdentifiers("background-position"))
				|| ValueFactory.isSizeSACUnit(currentValue)) {
			ValueList list = ValueList.createWSValueList();
			AbstractCSSValue value = createCSSValue("background-position", currentValue);
			list.add(value);
			short count = 1;
			LexicalUnit nlu = currentValue.getNextLexicalUnit();
			while (nlu != null && count < 4) { // Up to 4 values per layer
				if ((nlu.getLexicalUnitType() == LexicalUnit.SAC_IDENT
						&& getPropertyDatabase().isIdentifierValue("background-position", nlu.getStringValue()))
						|| ValueFactory.isSizeSACUnit(nlu)) {
					value = createCSSValue("background-position", nlu);
					list.add(value);
					count++;
					nextCurrentValue();
					nlu = currentValue.getNextLexicalUnit();
				} else {
					nlu = null;
				}
			}
			if (list.getLength() == 1) {
				posList.add(list.item(0));
			} else {
				if (checkPositionProperty(list)) {
					posList.add(list);
				} else {
					// report error
					BaseCSSDeclarationRule prule = styleDeclaration.getParentRule();
					if (prule != null) {
						CSSPropertyValueException ex = new CSSPropertyValueException(
								"Wrong value for background-position");
						ex.setValueText(list.getCssText());
						prule.getStyleDeclarationErrorHandler().wrongValue("background-position", ex);
					}
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private boolean checkPositionProperty(ValueList list) {
		// list has at least 2 values here
		int count = list.getLength();
		if (count == 2) {
			return true;
		} else if (count == 4) {
			if (((CSSPrimitiveValue) list.item(0)).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
					&& ((CSSPrimitiveValue) list.item(1)).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT
					&& ((CSSPrimitiveValue) list.item(2)).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
					&& ((CSSPrimitiveValue) list.item(3)).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
				return true;
			}
			return false;
		} else { // 3
			if (((CSSPrimitiveValue) list.item(0)).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
				return false;
			}
			if (((CSSPrimitiveValue) list.item(1)).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
					&& ((CSSPrimitiveValue) list.item(2)).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
				return true;
			}
			if (((CSSPrimitiveValue) list.item(1)).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT
					&& ((CSSPrimitiveValue) list.item(2)).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
				return true;
			}
			return false;
		}
	}

	private boolean testBackgroundSize(int i, Set<String> subp) {
		if ((currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT && testIdentifiers("background-size"))
				|| ValueFactory.isSizeSACUnit(currentValue)) {
			ValueList list = ValueList.createWSValueList();
			AbstractCSSValue value = createCSSValue("background-size", currentValue);
			list.add(value);
			layerBuffer.append(" /");
			miniLayerBuffer.append('/');
			LexicalUnit nlu = currentValue.getNextLexicalUnit();
			if (nlu != null) {
				if ((nlu.getLexicalUnitType() == LexicalUnit.SAC_IDENT
						&& getPropertyDatabase().isIdentifierValue("background-size", nlu.getStringValue()))
						|| ValueFactory.isSizeSACUnit(nlu)) {
					value = createCSSValue("background-size", nlu);
					list.add(value);
					nextCurrentValue();
				}
			}
			if (list.getLength() == 1) {
				lstSize.add(list.item(0));
			} else {
				lstSize.add(list);
			}
			subp.remove("background-size");
			return true;
		}
		return false;
	}

	private boolean testBackgroundRepeat(ValueList rptList) {
		if ((LexicalUnit.SAC_IDENT == currentValue.getLexicalUnitType() && testIdentifiers("background-repeat"))) {
			AbstractCSSValue value = createCSSValue("background-repeat", currentValue);
			String s = value.getCssText();
			nextCurrentValue();
			if (s.equals("repeat-y") || s.equals("repeat-x")) {
				rptList.add(value);
			} else if (currentValue != null && LexicalUnit.SAC_IDENT == currentValue.getLexicalUnitType()
					&& testIdentifiers("background-repeat")) {
				ValueList list = ValueList.createWSValueList();
				list.add(value);
				list.add(createCSSValue("background-repeat", currentValue));
				rptList.add(list);
				nextCurrentValue();
			} else {
				rptList.add(value);
			}
			return true;
		}
		return false;
	}

	private boolean testIdentifierProperty(int layer, Set<String> subp, String subpropertyName,
			ValueList lst) {
		if ((LexicalUnit.SAC_IDENT == currentValue.getLexicalUnitType() && testIdentifiers(subpropertyName))) {
			AbstractCSSValue value = createCSSValue(subpropertyName, currentValue);
			lst.add(value);
			return true;
		}
		return false;
	}

}