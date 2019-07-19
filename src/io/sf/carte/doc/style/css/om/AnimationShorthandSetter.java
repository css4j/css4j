/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>animation</code> property.
 */
class AnimationShorthandSetter extends ShorthandSetter {

	private StringBuilder layerBuffer = null, miniLayerBuffer = null;
	private int layerCount = 0;

	private ValueList lstDuration = ValueList.createCSValueList();
	private ValueList lstTimingFunction = ValueList.createCSValueList();
	private ValueList lstDelay = ValueList.createCSValueList();
	private ValueList lstIterationCount = ValueList.createCSValueList();
	private ValueList lstDirection= ValueList.createCSValueList();
	private ValueList lstFillMode = ValueList.createCSValueList();
	private ValueList lstPlayState = ValueList.createCSValueList();
	private ValueList lstName = ValueList.createCSValueList();

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
					} else {
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
		setListSubpropertyValue("animation-name", lstName);
		// flush the properties
		flush();
		return true;
	}

	private void clearLayer(Set<String> subp, int i) {
		if (!subp.contains("animation-duration") && lstDuration.getLength() == i) {
			lstDuration.remove(i - 1);
		}
		if (!subp.contains("animation-timing-function") && lstTimingFunction.getLength() == i) {
			lstTimingFunction.remove(i - 1);
		}
		if (!subp.contains("animation-delay") && lstDelay.getLength() == i) {
			lstDelay.remove(i - 1);
		}
		if (!subp.contains("animation-iteration-count") && lstIterationCount.getLength() == i) {
			lstIterationCount.remove(i - 1);
		}
		if (!subp.contains("animation-direction") && lstDirection.getLength() == i) {
			lstDirection.remove(i - 1);
		}
		if (!subp.contains("animation-fill-mode") && lstFillMode.getLength() == i) {
			lstFillMode.remove(i - 1);
		}
		if (!subp.contains("animation-play-state") && lstPlayState.getLength() == i) {
			lstPlayState.remove(i - 1);
		}
		if (!subp.contains("animation-name") && lstName.getLength() == i) {
			lstName.remove(i - 1);
		}
	}

	private void addSingleValueLayer(AbstractCSSValue keyword) {
		lstDuration.add(keyword);
		lstTimingFunction.add(keyword);
		lstDelay.add(keyword);
		lstIterationCount.add(keyword);
		lstDirection.add(keyword);
		lstFillMode.add(keyword);
		lstPlayState.add(keyword);
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
		short type = currentValue.getLexicalUnitType();
		if (type == LexicalUnit.SAC_SECOND || type == LexicalUnit.SAC_MILLISECOND) {
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
		} else if (type == LexicalUnit.SAC_INTEGER) {
			int ivalue = currentValue.getIntegerValue();
			if (ivalue < 0) {
				return false;
			}
			if (!setIterationCountValue(subp)) {
				return false;
			}
		} else if (type == LexicalUnit.SAC_REAL) {
			float fvalue = currentValue.getFloatValue();
			if (fvalue < 0f) {
				return false;
			}
			if (!setIterationCountValue(subp)) {
				return false;
			}
		} else if (type == LexicalUnit.SAC_FUNCTION) {
			if (!subp.contains("animation-timing-function")) {
				return false;
			}
			lstTimingFunction.add(createCSSValue("animation-timing-function", currentValue));
			subp.remove("animation-timing-function");
			nextCurrentValue();
		} else if (type == LexicalUnit.SAC_IDENT) {
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
			} else if (subp.contains("animation-name")) {
				lstName.add(createCSSValue("animation-name", currentValue));
				nextCurrentValue();
				subp.remove("animation-name");
			} else {
				return false;
			}
		} else if (type == LexicalUnit.SAC_STRING_VALUE) {
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
			if (unknownValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
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
			AbstractCSSValue cssVal = defaultPropertyValue(pname);
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
			} else if ("animation-name".equals(pname)) {
				lstName.add(cssVal);
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

}