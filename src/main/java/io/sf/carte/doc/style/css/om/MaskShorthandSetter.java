/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>mask</code> property.
 */
class MaskShorthandSetter extends ShorthandSetter {

	private StringBuilder layerBuffer = null, miniLayerBuffer = null;
	private int layerCount = 0;

	private final ValueList lstImage = ValueList.createCSValueList();
	private final ValueList lstPosition = ValueList.createCSValueList();
	private final ValueList lstSize = ValueList.createCSValueList();
	private final ValueList lstRepeat = ValueList.createCSValueList();
	private final ValueList lstClip = ValueList.createCSValueList();
	private final ValueList lstOrigin = ValueList.createCSValueList();
	private final ValueList lstMode = ValueList.createCSValueList();
	private final ValueList lstComposite = ValueList.createCSValueList();

	// The first geometry box value found
	private LexicalUnit geometryBox = null;

	MaskShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "mask");
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
		for (LexicalUnit value = shorthandValue; value != null; value = value
			.getNextLexicalUnit()) {
			if (value.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
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
		layerBuffer = new StringBuilder(64);
		miniLayerBuffer = new StringBuilder(64);

		String[] subparray = getShorthandSubproperties();
		Set<String> subp = new HashSet<>(subparray.length);

		int i = 0;
		while (i < layerCount && currentValue != null) {
			boolean validLayer = false;
			subp.clear();
			Collections.addAll(subp, subparray);
			valueLoop: while (currentValue != null) {
				if (currentValue.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
					if (validLayer) {
						i++;
						appendToValueBuffer(layerBuffer, miniLayerBuffer);
						layerBuffer.setLength(0);
						miniLayerBuffer.setLength(0);
						if (i != layerCount) {
							layerBuffer.append(',');
							miniLayerBuffer.append(',');
						}
					} else {
						break;
					}
					// Avoid nextCurrentValue() here
					currentValue = currentValue.getNextLexicalUnit();
					break;
				}
				// If a css-wide keyword is found, set the properties to it
				LexicalType lutype = currentValue.getLexicalUnitType();
				if (lutype == LexicalType.INHERIT || lutype == LexicalType.INITIAL
					|| lutype == LexicalType.UNSET || lutype == LexicalType.REVERT) {
					if (layerCount != 1 || i != 0 || currentValue.getNextLexicalUnit() != null) {
						validLayer = false;
						break valueLoop;
					}
					StyleValue keyword = valueFactory.createCSSValueItem(currentValue, true)
						.getCSSValue();
					// Set all properties to 'keyword'
					setSubpropertiesToKeyword(keyword);
					appendValueItemString(keyword);
					appendToValueBuffer(layerBuffer, miniLayerBuffer);
					// Done with the layer
					layerBuffer.setLength(0);
					miniLayerBuffer.setLength(0);
					return true;
				}
				// Classify the current lexical value
				validLayer = assignLayerValue(i, subp);
				if (!validLayer) {
					reportUnknownValue(subp);
					break valueLoop;
				}
			}
			if (!validLayer) {
				layerBuffer.setLength(0);
				miniLayerBuffer.setLength(0);
				StringBuilder msgbuf = new StringBuilder(64);
				msgbuf.append("Invalid layer found: ").append(i);
				if (currentValue != null) {
					msgbuf.append(' ').append(currentValue.toString());
				}
				reportDeclarationError("mask", msgbuf.toString());
				return false;
			}
			// Now set the remaining properties
			assignPendingValues(i, subp);
			if (subp.size() > 0) {
				// Reset subproperties not set by this shorthand at this layer
				resetUnsetProperties(subp);
			}
		}

		appendToValueBuffer(layerBuffer, miniLayerBuffer);

		// Assign subproperties
		setListSubpropertyValue("mask-image", lstImage);
		setListSubpropertyValue("mask-position", lstPosition);
		setListSubpropertyValue("mask-size", lstSize);
		setListSubpropertyValue("mask-origin", lstOrigin);
		setListSubpropertyValue("mask-clip", lstClip);
		setListSubpropertyValue("mask-repeat", lstRepeat);
		setListSubpropertyValue("mask-composite", lstComposite);
		setListSubpropertyValue("mask-mode", lstMode);

		// Reset mask-border
		resetMaskBorderProperties();

		// flush the properties
		flush();

		return true;
	}

	/**
	 * Try to assign the current lexical value to an individual property.
	 * 
	 * @param i
	 * @param subp
	 * @return true if the value was successfully assigned.
	 */
	private boolean assignLayerValue(int i, Set<String> subp) {
		boolean retVal = false;
		if (subp.contains("mask-image") && testImage(i, subp)) {
			nextCurrentValue();
			retVal = true;
		} else if (subp.contains("mask-position") && testPosition(lstPosition)) {
			subp.remove("mask-position");
			nextCurrentValue();
			retVal = true;
			if (currentValue != null
				&& LexicalType.OPERATOR_SLASH == currentValue.getLexicalUnitType()) {
				// Size
				currentValue = currentValue.getNextLexicalUnit();
				if (currentValue != null && testSize(i, subp)) {
					nextCurrentValue();
				} else {
					// Report error: size not found after slash.
					StyleDeclarationErrorHandler eh = styleDeclaration
						.getStyleDeclarationErrorHandler();
					if (eh != null) {
						eh.shorthandSyntaxError("mask", "Size not found after slash");
					}
					retVal = false;
				}
			}
		} else if (subp.contains("mask-mode")
			&& testIdentifierProperty(i, subp, "mask-mode", lstMode)) {
			nextCurrentValue();
			subp.remove("mask-mode");
			retVal = true;
		} else if (subp.contains("mask-composite")
			&& testIdentifierProperty(i, subp, "mask-composite", lstComposite)) {
			nextCurrentValue();
			subp.remove("mask-composite");
			retVal = true;
		} else if (subp.contains("mask-repeat") && testRepeat(lstRepeat)) {
			subp.remove("mask-repeat");
			retVal = true;
		} else if (subp.contains("mask-origin")
			&& testIdentifierProperty(i, subp, "mask-origin", lstOrigin)) {
			/*
			 * "If one <geometry-box> value and the no-clip keyword are present then
			 * <geometry-box> sets mask-origin and no-clip sets mask-clip to that value.
			 * 
			 * If one <geometry-box> value and no no-clip keyword are present then
			 * <geometry-box> sets both mask-origin and mask-clip to that value.
			 * 
			 * If two <geometry-box> values are present, then the first sets mask-origin and
			 * the second mask-clip."
			 */
			geometryBox = currentValue;
			nextCurrentValue();
			subp.remove("mask-origin");
			retVal = true;
		} else if (subp.contains("mask-clip")
			&& testIdentifierProperty(i, subp, "mask-clip", lstClip)) {
			// If we got here, mask-origin must have been set already
			nextCurrentValue();
			subp.remove("mask-clip");
			geometryBox = null;
			retVal = true;
		}
		return retVal;
	}

	private void assignPendingValues(int i, Set<String> subp) {
		if (subp.contains("mask-clip") && geometryBox != null) {
			StyleValue value = createCSSValue("mask-clip", geometryBox);
			lstClip.add(value);
			subp.remove("mask-clip");
		}
		geometryBox = null;
	}

	@Override
	protected void nextCurrentValue() {
		// Add the value string.
		appendValueItemString();
		currentValue = currentValue.getNextLexicalUnit();
	}

	private void reportUnknownValue(Set<String> subp) {
		BaseCSSDeclarationRule prule = styleDeclaration.getParentRule();
		if (prule != null) {
			StyleDeclarationErrorHandler eh = prule.getStyleDeclarationErrorHandler();
			if (currentValue.getLexicalUnitType() == LexicalType.IDENT) {
				eh.unknownIdentifier("mask", currentValue.getStringValue());
			} else {
				LexicalUnit[] lua = new LexicalUnit[1];
				lua[0] = currentValue;
				eh.unassignedShorthandValues("mask", subp.toArray(new String[0]), lua);
			}
		}
	}

	private void resetUnsetProperties(Set<String> subp) {
		for (String pname : subp) {
			StyleValue cssVal = defaultPropertyValue(pname);
			if ("mask-image".equals(pname)) {
				// No mask-image: "Note that a value of ‘none’ still creates a layer."
				lstImage.add(cssVal);
			} else if ("mask-position".equals(pname)) {
				lstPosition.add(cssVal);
			} else if ("mask-size".equals(pname)) {
				ValueList list = ValueList.createWSValueList();
				list.add(cssVal);
				list.add(cssVal);
				lstSize.add(list);
			} else if ("mask-origin".equals(pname)) {
				lstOrigin.add(cssVal);
			} else if ("mask-clip".equals(pname)) {
				lstClip.add(cssVal);
			} else if ("mask-repeat".equals(pname)) {
				lstRepeat.add(cssVal);
			} else if ("mask-mode".equals(pname)) {
				lstMode.add(cssVal);
			} else if ("mask-composite".equals(pname)) {
				lstComposite.add(cssVal);
			}
		}
	}

	private boolean testImage(int i, Set<String> subp) {
		if (!isAttrTainted() && isImage()) {
			lstImage.add(createCSSValue("mask-image", currentValue));
			subp.remove("mask-image");
			return true;
		} else if (currentValue.getLexicalUnitType() == LexicalType.IDENT
			&& "none".equals(currentValue.getStringValue())) {
			lstImage.add(createCSSValue("mask-image", currentValue));
			subp.remove("mask-image");
			return true;
		}
		return false;
	}

	private boolean testPosition(ValueList posList) {
		if ((currentValue.getLexicalUnitType() == LexicalType.IDENT
			&& testIdentifiers("mask-position")) || ValueFactory.isLengthPercentageSACUnit(currentValue)) {
			ValueList list = ValueList.createWSValueList();
			StyleValue value = createCSSValue("mask-position", currentValue);
			list.add(value);
			short count = 1;
			LexicalUnit nlu = currentValue.getNextLexicalUnit();
			while (nlu != null && count < 4) { // Up to 4 values per layer
				if ((nlu.getLexicalUnitType() == LexicalType.IDENT && getShorthandDatabase()
					.isIdentifierValue("mask-position", nlu.getStringValue()))
					|| ValueFactory.isLengthPercentageSACUnit(nlu)) {
					value = createCSSValue("mask-position", nlu);
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
							"Wrong value for mask-position");
						ex.setValueText(list.getCssText());
						prule.getStyleDeclarationErrorHandler().wrongValue("mask-position", ex);
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
			return list.item(0).getPrimitiveType() == Type.IDENT
				&& list.item(1).getPrimitiveType() != Type.IDENT
				&& list.item(2).getPrimitiveType() == Type.IDENT
				&& list.item(3).getPrimitiveType() != Type.IDENT;
		} else { // 3
			if (list.item(0).getPrimitiveType() != Type.IDENT) {
				return false;
			}
			if (list.item(1).getPrimitiveType() == Type.IDENT
				&& list.item(2).getPrimitiveType() != Type.IDENT) {
				return true;
			}
			return list.item(1).getPrimitiveType() != Type.IDENT
				&& list.item(2).getPrimitiveType() == Type.IDENT;
		}
	}

	private boolean testSize(int i, Set<String> subp) {
		if ((currentValue.getLexicalUnitType() == LexicalType.IDENT && testIdentifiers("mask-size"))
			|| ValueFactory.isLengthPercentageSACUnit(currentValue)) {
			ValueList list = ValueList.createWSValueList();
			StyleValue value = createCSSValue("mask-size", currentValue);
			list.add(value);
			layerBuffer.append(" /");
			miniLayerBuffer.append('/');
			LexicalUnit nlu = currentValue.getNextLexicalUnit();
			if (nlu != null && ((nlu.getLexicalUnitType() == LexicalType.IDENT
					&& getShorthandDatabase().isIdentifierValue("mask-size", nlu.getStringValue()))
					|| ValueFactory.isLengthPercentageSACUnit(nlu))) {
				value = createCSSValue("mask-size", nlu);
				list.add(value);
				nextCurrentValue();
			}
			if (list.getLength() == 1) {
				lstSize.add(list.item(0));
			} else {
				lstSize.add(list);
			}
			subp.remove("mask-size");
			return true;
		}
		return false;
	}

	private boolean testRepeat(ValueList rptList) {
		if (LexicalType.IDENT == currentValue.getLexicalUnitType()
			&& testIdentifiers("mask-repeat")) {
			StyleValue value = createCSSValue("mask-repeat", currentValue);
			String s = value.getCssText();
			nextCurrentValue();
			if (s.equals("repeat-y") || s.equals("repeat-x")) {
				rptList.add(value);
			} else if (currentValue != null
				&& LexicalType.IDENT == currentValue.getLexicalUnitType()
				&& testIdentifiers("mask-repeat")) {
				ValueList list = ValueList.createWSValueList();
				list.add(value);
				list.add(createCSSValue("mask-repeat", currentValue));
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
		if (LexicalType.IDENT == currentValue.getLexicalUnitType()
			&& testIdentifiers(subpropertyName)) {
			StyleValue value = createCSSValue(subpropertyName, currentValue);
			lst.add(value);
			return true;
		}
		return false;
	}

	@Override
	protected void resetSubproperties() {
		super.resetSubproperties();
		resetMaskBorderProperties();
	}

	private void resetMaskBorderProperties() {
		setPropertyToDefault("mask-border-source");
		setPropertyToDefault("mask-border-slice");
		setPropertyToDefault("mask-border-width");
		setPropertyToDefault("mask-border-outset");
		setPropertyToDefault("mask-border-repeat");
		setPropertyToDefault("mask-border-mode");
	}

	@Override
	protected void setSubpropertiesToKeyword(StyleValue keyword) {
		super.setSubpropertiesToKeyword(keyword);
		setProperty("mask-border-source", keyword, isPriorityImportant());
		setProperty("mask-border-slice", keyword, isPriorityImportant());
		setProperty("mask-border-width", keyword, isPriorityImportant());
		setProperty("mask-border-outset", keyword, isPriorityImportant());
		setProperty("mask-border-repeat", keyword, isPriorityImportant());
		setProperty("mask-border-mode", keyword, isPriorityImportant());
		flush();
	}

}
