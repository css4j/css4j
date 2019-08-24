/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Shorthand setter for the <code>border-image</code> property.
 */
class BorderImageShorthandSetter extends ShorthandSetter {

	BorderImageShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "border-image");
	}

	@Override
	public boolean assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		boolean errorFound = false;
		boolean bisourceUnset = true;
		boolean bisliceUnset = true;
		boolean biwidthUnset = true;
		boolean bioutsetUnset = true;
		boolean birepeatUnset = true;
		while (currentValue != null) {
			short lut;
			if (isImage()) {
				// border-image-source
				setSubpropertyValue("border-image-source", createCSSValue("border-image-source", currentValue));
				bisourceUnset = false;
				nextCurrentValue();
				continue;
			} else if ((lut = currentValue.getLexicalUnitType()) == LexicalUnit.SAC_IDENT) {
				// Test for repeat
				if (testIdentifiers("border-image-repeat")) {
					setSubpropertyValue("border-image-repeat", createCSSValue("border-image-repeat", currentValue));
					birepeatUnset = false;
				} else if ("none".equalsIgnoreCase(currentValue.getStringValue())) {
					// border-image-source
					setSubpropertyValue("border-image-source", createCSSValue("border-image-source", currentValue));
					bisourceUnset = false;
				} else if ("auto".equalsIgnoreCase(currentValue.getStringValue())) {
					// border-image-width: should not be found here
					setSubpropertyValue("border-image-width", createCSSValue("border-image-width", currentValue));
					biwidthUnset = false;
				} else {
					// report error
					StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
					if (errHandler != null) {
						errHandler.unknownIdentifier("border-image", currentValue.getStringValue());
					}
					errorFound = true;
				}
				nextCurrentValue();
				continue;
			} else if (ValueFactory.isPlainNumberOrPercentSACUnit(currentValue)) {
				// slice / width / outset
				ValueList list = ValueList.createWSValueList();
				do {
					// Numeric types can be added without checking for null
					list.add(createCSSValue("border-image-slice", currentValue));
					nextCurrentValue();
					if (currentValue == null) {
						break;
					}
					lut = currentValue.getLexicalUnitType();
				} while (lut != LexicalUnit.SAC_OPERATOR_SLASH && lut != LexicalUnit.SAC_IDENT);
				setSubpropertyValue("border-image-slice", list);
				bisliceUnset = false;
				if (currentValue != null) {
					if (lut == LexicalUnit.SAC_IDENT) {
						if ("fill".equals(currentValue.getStringValue().toLowerCase(Locale.ROOT))) {
							list.add(createCSSValue("border-image-slice", currentValue));
							nextCurrentValue();
							if (currentValue == null) {
								break;
							}
							lut = currentValue.getLexicalUnitType();
						} else {
							continue;
						}
					}
					if (lut == LexicalUnit.SAC_OPERATOR_SLASH) {
						// width / outset
						nextCurrentValue();
						lut = currentValue.getLexicalUnitType();
						if (lut == LexicalUnit.SAC_OPERATOR_SLASH) {
							// Empty width: set outset
							nextCurrentValue();
							if (!ValueFactory.isSizeOrNumberSACUnit(currentValue)) {
								// Report error
								StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
								if (errHandler != null) {
									CSSPropertyValueException ex = new CSSPropertyValueException(
											"Wrong value for border-image: expected width / outset");
									ex.setValueText(BaseCSSStyleDeclaration.lexicalUnitToString(currentValue));
									errHandler.wrongValue("border-image", ex);
									errorFound = true;
								}
								break;
							}
							list = ValueList.createWSValueList();
							byte c = 0;
							do {
								if (!ValueFactory.isSizeOrNumberSACUnit(currentValue)) {
									break;
								}
								list.add(createCSSValue("border-image-outset", currentValue));
								nextCurrentValue();
								c++;
							} while (currentValue != null && c < 4);
							if (list.getLength() != 0) {
								setSubpropertyValue("border-image-outset", list);
								bioutsetUnset = false;
							}
							continue;
						} else {
							// width / outset
							if (lut == LexicalUnit.SAC_IDENT) {
								// auto
								if (!"auto".equalsIgnoreCase(currentValue.getStringValue())) {
									continue;
								}
								setSubpropertyValue("border-image-width",
										createCSSValue("border-image-width", currentValue));
								biwidthUnset = false;
								nextCurrentValue();
							} else {
								byte c = 0;
								list = ValueList.createWSValueList();
								do {
									if (!ValueFactory.isSizeOrNumberSACUnit(currentValue)) {
										break;
									}
									list.add(createCSSValue("border-image-width", currentValue));
									nextCurrentValue();
									if (currentValue == null) {
										break;
									}
									lut = currentValue.getLexicalUnitType();
									c++;
								} while (lut != LexicalUnit.SAC_OPERATOR_SLASH && lut != LexicalUnit.SAC_IDENT
										&& c < 4);
								if (list.getLength() != 0) {
									setSubpropertyValue("border-image-width", list);
									biwidthUnset = false;
								}
								if (lut == LexicalUnit.SAC_OPERATOR_SLASH) {
									// outset
									nextCurrentValue();
									if (currentValue == null) {
										break;
									}
									list = ValueList.createWSValueList();
									while (ValueFactory.isSizeOrNumberSACUnit(currentValue) && c < 4) {
										list.add(createCSSValue("border-image-outset", currentValue));
										nextCurrentValue();
										if (currentValue == null) {
											break;
										}
										c++;
									}
									if (list.getLength() != 0) {
										setSubpropertyValue("border-image-outset", list);
										bioutsetUnset = false;
									}
								}
							}
						}
					}
				}
			} else {
				StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					errHandler.unassignedShorthandValue("border-image", currentValue.toString());
				}
				errorFound = true;
				break;
			}
			if (!bisourceUnset && !bisliceUnset && !biwidthUnset && !bioutsetUnset
					&& !birepeatUnset && currentValue != null) {
				StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					errHandler.shorthandError("border-image", "Unexpected value: " + currentValue.toString());
				}
				errorFound = true;
				break;
			}
		}
		if (errorFound) {
			return false;
		}
		// Unset properties set to defaults
		if (bisourceUnset) {
			setSubpropertyValue("border-image-source", defaultPropertyValue("border-image-source"));
		}
		if (bisliceUnset) {
			setSubpropertyValue("border-image-slice", defaultPropertyValue("border-image-slice"));
		}
		if (biwidthUnset) {
			setSubpropertyValue("border-image-width", defaultPropertyValue("border-image-width"));
		}
		if (bioutsetUnset) {
			setSubpropertyValue("border-image-outset", defaultPropertyValue("border-image-outset"));
		}
		if (birepeatUnset) {
			setSubpropertyValue("border-image-repeat", defaultPropertyValue("border-image-repeat"));
		}
		flush();
		return true;
	}
}