/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>animation</code> property.
 */
class AnimationShorthandSetter extends ShorthandSetter {

	private StringBuilder layerBuffer = null, miniLayerBuffer = null;
	private int layerCount = 0;

	private final ValueList lstDuration = ValueList.createCSValueList();
	private final ValueList lstTimingFunction = ValueList.createCSValueList();
	private final ValueList lstDelay = ValueList.createCSValueList();
	private final ValueList lstIterationCount = ValueList.createCSValueList();
	private final ValueList lstDirection= ValueList.createCSValueList();
	private final ValueList lstFillMode = ValueList.createCSValueList();
	private final ValueList lstPlayState = ValueList.createCSValueList();
	private final ValueList lstTimeline = ValueList.createCSValueList();
	private final ValueList lstName = ValueList.createCSValueList();

	AnimationShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "animation");
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
		IdentifierValue normalIdent = new IdentifierValue("normal");
		normalIdent.setSubproperty(true);
		StyleValue rangeValue = normalIdent;
		//
		layerBuffer = new StringBuilder(64);
		miniLayerBuffer = new StringBuilder(64);
		//
		String[] subparray = getShorthandSubproperties();
		int i = 0;
		topLoop: while (i < layerCount && currentValue != null) {
			boolean validLayer = false;
			Set<String> subp = new HashSet<String>(subparray.length);
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
						break valueLoop;
					}
					currentValue = currentValue.getNextLexicalUnit();
					break;
				}
				// If a css-wide keyword is found, set the entire layer to it
				LexicalType lutype = currentValue.getLexicalUnitType();
				if (lutype == LexicalType.INHERIT || lutype == LexicalType.INITIAL || lutype == LexicalType.UNSET
						|| lutype == LexicalType.REVERT) {
					StyleValue keyword = valueFactory.createCSSValueItem(currentValue, true).getCSSValue();
					// Full property is 'keyword'
					if (layerCount != 1 || currentValue.getPreviousLexicalUnit() != null || currentValue.getNextLexicalUnit() != null) {
						BaseCSSDeclarationRule prule = styleDeclaration.getParentRule();
						if (prule != null) {
							StyleDeclarationErrorHandler eh = prule
									.getStyleDeclarationErrorHandler();
							eh.shorthandSyntaxError(getShorthandName(),
									"Keyword found mixed with other values.");
						}
						return false;
					}
					// Add a single keyword value
					addSingleValueLayer(keyword);
					// Serialization
					appendValueItemString(keyword);
					// Reset animation range
					rangeValue = keyword;
					break topLoop;
				}
				// try to assign the current lexical value to an individual
				// property ...and see the result
				if (assignLayerValue(subp)) {
					validLayer = true;
				} else {
					reportUnknownValue(subp, currentValue);
					validLayer = false;
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
				reportDeclarationError("animation", msgbuf.toString());
				return false;
			} else if (subp.size() > 0) {
				// Reset subproperties not set by this shorthand at this single
				// animation
				resetUnsetProperties(subp);
			}
		}
		appendToValueBuffer(layerBuffer, miniLayerBuffer);

		// Assign subproperties
		setListSubpropertyValue("animation-duration", lstDuration);
		setListSubpropertyValue("animation-timing-function", lstTimingFunction);
		setListSubpropertyValue("animation-delay", lstDelay);
		setListSubpropertyValue("animation-iteration-count", lstIterationCount);
		setListSubpropertyValue("animation-direction", lstDirection);
		setListSubpropertyValue("animation-fill-mode", lstFillMode);
		setListSubpropertyValue("animation-play-state", lstPlayState);
		setListSubpropertyValue("animation-timeline", lstTimeline);
		setListSubpropertyValue("animation-name", lstName);
		// Reset animation range
		setSubpropertyValue("animation-range-start", rangeValue);
		setSubpropertyValue("animation-range-end", rangeValue);

		// flush the properties
		flush();

		return true;
	}

	private void addSingleValueLayer(StyleValue keyword) {
		lstDuration.add(keyword);
		lstTimingFunction.add(keyword);
		lstDelay.add(keyword);
		lstIterationCount.add(keyword);
		lstDirection.add(keyword);
		lstFillMode.add(keyword);
		lstPlayState.add(keyword);
		lstTimeline.add(keyword);
		lstName.add(keyword);
	}

	/**
	 * Try to assign the current lexical value to an individual property.
	 * 
	 * @param i
	 * @param subp
	 * @return <code>true</code> if the value was successfully assigned.
	 */
	private boolean assignLayerValue(Set<String> subp) {
		short cssunit = currentValue.getCssUnit();
		LexicalType type;
		if (cssunit == CSSUnit.CSS_S || cssunit == CSSUnit.CSS_MS) {
			ValueList list;
			String property = "animation-duration";
			if (!subp.contains(property)) {
				property = "animation-delay";
				if (!subp.contains(property)) {
					return false;
				}
				list = lstDelay;
			} else {
				list = lstDuration;
			}
			list.add(createCSSValue(property, currentValue));
			subp.remove(property);
			nextCurrentValue();
		} else if ((type = currentValue.getLexicalUnitType()) == LexicalType.INTEGER) {
			int ivalue = currentValue.getIntegerValue();
			if (ivalue < 0) {
				return false;
			}
			return setIterationCountValue(subp);
		} else if (type == LexicalType.REAL) {
			float fvalue = currentValue.getFloatValue();
			if (fvalue < 0f) {
				return false;
			}
			return setIterationCountValue(subp);
		} else if (type == LexicalType.CUBIC_BEZIER_FUNCTION
				|| type == LexicalType.STEPS_FUNCTION) {
			if (!subp.contains("animation-timing-function")) {
				return false;
			}
			lstTimingFunction.add(createCSSValue("animation-timing-function", currentValue));
			subp.remove("animation-timing-function");
			nextCurrentValue();
		} else if (type == LexicalType.FUNCTION) {
			if (!subp.contains("animation-timeline")
					|| (!"scroll".equalsIgnoreCase(currentValue.getFunctionName())
							&& !"view".equalsIgnoreCase(currentValue.getFunctionName()))) {
				return false;
			}
			lstTimeline.add(createCSSValue("animation-timeline", currentValue));
			subp.remove("animation-timeline");
			nextCurrentValue();
		} else if (type == LexicalType.IDENT) {
			if (subp.contains("animation-timing-function")
					&& testIdentifiers("transition-timing-function")) {
				lstTimingFunction.add(createCSSValue("animation-timing-function", currentValue));
				nextCurrentValue();
				subp.remove("animation-timing-function");
			} else if (subp.contains("animation-iteration-count")
					&& testIdentifiers("animation-iteration-count")) {
				lstIterationCount.add(createCSSValue("animation-iteration-count", currentValue));
				nextCurrentValue();
				subp.remove("animation-iteration-count");
			} else if (subp.contains("animation-direction")
					&& testIdentifiers("animation-direction")) {
				lstDirection.add(createCSSValue("animation-direction", currentValue));
				nextCurrentValue();
				subp.remove("animation-direction");
			} else if (subp.contains("animation-fill-mode")
					&& testIdentifiers("animation-fill-mode")) {
				lstFillMode.add(createCSSValue("animation-fill-mode", currentValue));
				nextCurrentValue();
				subp.remove("animation-fill-mode");
			} else if (subp.contains("animation-play-state")
					&& testIdentifiers("animation-play-state")) {
				lstPlayState.add(createCSSValue("animation-play-state", currentValue));
				nextCurrentValue();
				subp.remove("animation-play-state");
			} else if ("none".equalsIgnoreCase(currentValue.getStringValue())) {
				// Skip 'none'
				currentValue = currentValue.getNextLexicalUnit();
			} else if (subp.contains("animation-name") && !testIdentifiers("animation-timeline")) {
				lstName.add(createCSSValue("animation-name", currentValue));
				nextCurrentValue();
				subp.remove("animation-name");
			} else if (subp.contains("animation-timeline")) {
				lstTimeline.add(createCSSValue("animation-timeline", currentValue));
				nextCurrentValue();
				subp.remove("animation-timeline");
			} else {
				return false;
			}
		} else if (type == LexicalType.STRING) {
			if (!subp.contains("animation-name")) {
				return false;
			}
			lstName.add(createCSSValue("animation-name", currentValue));
			nextCurrentValue();
			subp.remove("animation-name");
		} else {
			return false;
		}
		return true;
	}

	private boolean setIterationCountValue(Set<String> subp) {
		if (!subp.contains("animation-iteration-count")) {
			return false;
		}
		lstIterationCount.add(createCSSValue("animation-iteration-count", currentValue));
		subp.remove("animation-iteration-count");
		nextCurrentValue();
		return true;
	}

	private void reportUnknownValue(Set<String> subp, LexicalUnit unknownValue) {
		BaseCSSDeclarationRule prule = styleDeclaration.getParentRule();
		if (prule != null) {
			StyleDeclarationErrorHandler eh = prule.getStyleDeclarationErrorHandler();
			if (unknownValue.getLexicalUnitType() == LexicalType.IDENT) {
				eh.unknownIdentifier("animation", unknownValue.getStringValue());
			} else {
				eh.unassignedShorthandValues("animation", subp.toArray(new String[0]),
						new LexicalUnit[]{unknownValue});
			}
		}
	}

	private void resetUnsetProperties(Set<String> subp) {
		Iterator<String> it = subp.iterator();
		while (it.hasNext()) {
			String pname = it.next();
			StyleValue cssVal = defaultPropertyValue(pname);
			if ("animation-duration".equals(pname)) {
				lstDuration.add(cssVal);
			} else if ("animation-timing-function".equals(pname)) {
				lstTimingFunction.add(cssVal);
			} else if ("animation-delay".equals(pname)) {
				lstDelay.add(cssVal);
			} else if ("animation-iteration-count".equals(pname)) {
				lstIterationCount.add(cssVal);
			} else if ("animation-direction".equals(pname)) {
				lstDirection.add(cssVal);
			} else if ("animation-fill-mode".equals(pname)) {
				lstFillMode.add(cssVal);
			} else if ("animation-play-state".equals(pname)) {
				lstPlayState.add(cssVal);
			} else if ("animation-timeline".equals(pname)) {
				lstTimeline.add(cssVal);
			} else if ("animation-name".equals(pname)) {
				lstName.add(cssVal);
			}
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

}