/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.List;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StringValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Shorthand setter for the <code>font</code> shorthand property.
 */
class FontShorthandSetter extends ShorthandSetter {

	private boolean lineHeightSet = false;

	FontShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "font");
	}

	@Override
	protected List<String> subpropertyList() {
		List<String> subp = super.subpropertyList();
		subp.remove("line-height");
		subp.remove("font-kerning");
		subp.remove("font-size-adjust");
		subp.add(0, "font-variant-css21");
		return subp;
	}

	@Override
	public void init(LexicalUnit shorthandValue, boolean important) {
		shorthandValue = filterNormalIdentifier(shorthandValue);
		super.init(shorthandValue, important);
	}

	@Override
	public boolean assignSubproperties() {
		if (currentValue == null) {
			// font: normal
			List<String> subp = super.subpropertyList();
			Iterator<String> it = subp.iterator();
			while (it.hasNext()) {
				addUnassignedProperty(it.next());
			}
			resetSubproperties();
			flush();
			getValueItemBuffer().append("normal");
			getValueItemBufferMini().append("normal");
			return true;
		}
		if (draftSubproperties()) {
			if (getUnassignedProperties().contains("font-family")) {
				// No font family
				if (!getUnassignedProperties().contains("font-size")) {
					reportMissingPropertySyntaxError("font-family");
					return false;
				}
				if (!getUnassignedProperties().contains("font-stretch")) {
					reportMissingPropertySyntaxError("font-family");
					return false;
				}
			} else if (getUnassignedProperties().contains("font-size")) {
				reportMissingPropertySyntaxError("font-size");
				return false;
			}
			flush();
			return true;
		}
		return false;
	}

	private void reportMissingPropertySyntaxError(String missedProperty) {
		StringBuilder sb = new StringBuilder(getValueItemBuffer().length() + missedProperty.length() + 40);
		sb.append("This syntax requires ");
		sb.append(missedProperty);
		sb.append(" to be present: ");
		sb.append(getValueItemBuffer());
		reportDeclarationError("font", sb.toString());
	}

	@Override
	protected void nextCurrentValue() {
		if (currentValue != null) {
			currentValue = filterNormalIdentifier(currentValue.getNextLexicalUnit());
			appendValueItemString();
		}
	}

	private LexicalUnit filterNormalIdentifier(LexicalUnit lu) {
		while (lu != null && lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT
				&& "normal".equalsIgnoreCase(lu.getStringValue())) {
			// Ignore 'normal'
			lu = lu.getNextLexicalUnit();
		}
		return lu;
	}

	@Override
	protected boolean assignSubproperty(String subproperty) {
		// font-size is a special case
		if (subproperty.equals("font-size")) {
			if (assignFontSize()) {
				// Check for line-height
				if (currentValue != null && currentValue.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH) {
					// the line-height value
					nextCurrentValue();
					StyleValue cssValue = createCSSValue(subproperty, currentValue);
					setProperty("line-height", cssValue, getPriority());
					nextCurrentValue();
					lineHeightSet = true;
				}
				return true;
			}
		}
		// Rest of properties
		switch (currentValue.getLexicalUnitType()) {
		case LexicalUnit.SAC_IDENT:
			if (subproperty.equals("font-variant-css21")) {
				if ("small-caps".equalsIgnoreCase(currentValue.getStringValue())) {
					setSubpropertyValue("font-variant-caps", createCSSValue("font-variant-caps", currentValue));
					nextCurrentValue();
					return true;
				}
			} else if (super.assignSubproperty(subproperty)) {
				return true;
			} else if (subproperty.equals("font-family")) {
				// Check for font-family
				// CSS Spec, 15.3: Font family names must either be given quoted
				// as strings, or unquoted as a sequence of one or more identifiers.
				consumeFontFamilyIdent();
				while (currentValue != null) {
					short type = currentValue.getLexicalUnitType();
					if (type == LexicalUnit.SAC_OPERATOR_COMMA) {
						nextCurrentValue();
						if (currentValue == null) {
							throw new DOMException(DOMException.SYNTAX_ERR,
									"Unexpected comma at the end of font-family list");
						}
						type = currentValue.getLexicalUnitType();
					}
					if (type == LexicalUnit.SAC_IDENT) {
						consumeFontFamilyIdent();
					} else if (type == LexicalUnit.SAC_STRING_VALUE) {
						consumeFontFamilyString();
					} else {
						break;
					}
				}
				return true;
			}
			break;
		case LexicalUnit.SAC_STRING_VALUE:
			if (subproperty.equals("font-family")) {
				consumeFontFamilyString();
				while (currentValue != null) {
					short type = currentValue.getLexicalUnitType();
					if (type == LexicalUnit.SAC_OPERATOR_COMMA) {
						nextCurrentValue();
						if (currentValue == null) {
							throw new DOMException(DOMException.SYNTAX_ERR,
									"Unexpected comma at the end of font-family list");
						}
						type = currentValue.getLexicalUnitType();
					}
					if (type == LexicalUnit.SAC_IDENT) {
						consumeFontFamilyIdent();
					} else if (type == LexicalUnit.SAC_STRING_VALUE) {
						consumeFontFamilyString();
					} else {
						break;
					}
				}
				return true;
			}
			break;
		case LexicalUnit.SAC_INTEGER:
			if (subproperty.equals("font-weight")) {
				setSubpropertyValue("font-weight", createCSSValue("font-size", currentValue));
				nextCurrentValue();
				return true;
			}
		}
		return false;
	}

	private void consumeFontFamilyIdent() {
		short stringType = CSSPrimitiveValue.CSS_IDENT;
		String str = currentValue.getStringValue();
		super.nextCurrentValue();
		while (currentValue != null && currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String s = currentValue.getStringValue();
			str += " " + s;
			stringType = CSSPrimitiveValue.CSS_STRING;
			super.nextCurrentValue();
		}
		PrimitiveValue value;
		if (stringType == CSSPrimitiveValue.CSS_STRING) {
			value = new StringValue();
		} else {
			value = new IdentifierValue();
		}
		value.setStringValue(stringType, str);
		value.setSubproperty(true);
		addSubpropertyValue("font-family", value, true);
	}

	private void consumeFontFamilyString() {
		StyleValue value = createCSSValue("font-family", currentValue);
		addSubpropertyValue("font-family", value, true);
		super.nextCurrentValue();
	}

	private boolean assignFontSize() {
		if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			return super.assignSubproperty("font-size");
		} else if (ValueFactory.isPositiveSizeSACUnit(currentValue)) {
			StyleValue cssValue = createCSSValue("font-size", currentValue);
			setSubpropertyValue("font-size", cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

	@Override
	protected void resetSubproperties() {
		getUnassignedProperties().remove("font-variant-css21");
		super.resetSubproperties();
		if (!lineHeightSet) {
			setPropertyToDefault("line-height");
		}
		if (!isPropertySet("font-variant-caps")) {
			setPropertyToDefault("font-variant-caps");
		}
		setPropertyToDefault("font-size-adjust");
		setPropertyToDefault("font-kerning");
		setPropertyToDefault("font-variant-ligatures");
		setPropertyToDefault("font-variant-position");
		setPropertyToDefault("font-variant-numeric");
		setPropertyToDefault("font-variant-alternates");
		setPropertyToDefault("font-variant-east-asian");
	}

	@Override
	protected void setSubpropertiesInherit(InheritValue inherit) {
		super.setSubpropertiesInherit(inherit);
		lineHeightSet = true;
		resetSubproperties();
		flush();
	}

	@Override
	protected void setSubpropertiesToKeyword(StyleValue keyword) {
		super.setSubpropertiesToKeyword(keyword);
		lineHeightSet = true;
		resetSubproperties();
		flush();
	}
}