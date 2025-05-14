/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
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
	public short assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return 0;
		} else if (kwscan == 2) {
			return 2;
		}

		boolean errorFound = false;
		boolean bisourceUnset = true;
		boolean bisliceUnset = true;
		boolean biwidthUnset = true;
		boolean bioutsetUnset = true;
		boolean birepeatUnset = true;
		while (currentValue != null) {
			LexicalType lut;
			if (isImage()) {
				// border-image-source
				setSubpropertyValue("border-image-source", createCSSValue("border-image-source", currentValue));
				bisourceUnset = false;
				nextCurrentValue();
				continue;
			} else if ((lut = currentValue.getLexicalUnitType()) == LexicalType.IDENT) {
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
				} else if (isPrefixedIdentValue()) {
					setPrefixedValue(currentValue);
					flush();
					return 1;
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
			} else if (ValueFactory.isPercentageOrNumberSACUnit(currentValue)) {
				// slice is positive <percentage> | <number>
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
				} while (lut != LexicalType.OPERATOR_SLASH && lut != LexicalType.IDENT);
				setSubpropertyValue("border-image-slice", list);
				bisliceUnset = false;
				if (currentValue != null) {
					if (lut == LexicalType.IDENT) {
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
					if (lut == LexicalType.OPERATOR_SLASH) {
						// width / outset
						nextCurrentValue();
						lut = currentValue.getLexicalUnitType();
						if (lut == LexicalType.OPERATOR_SLASH) {
							// Empty width: set outset
							nextCurrentValue();
							// outset is positive <length> | <number>
							if (!ValueFactory.isLengthOrNumberSACUnit(currentValue)) {
								if (currentValue.getLexicalUnitType() == LexicalType.PREFIXED_FUNCTION) {
									setPrefixedValue(currentValue);
									flush();
									return 1;
								}
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
								if (!ValueFactory.isLengthOrNumberSACUnit(currentValue)) {
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
							// width is <length-percentage> | <number> | auto
							if (lut == LexicalType.IDENT) {
								// auto
								if (!"auto".equalsIgnoreCase(currentValue.getStringValue())) {
									continue;
								}
								setSubpropertyValue("border-image-width",
										createCSSValue("border-image-width", currentValue));
								biwidthUnset = false;
								nextCurrentValue();
							} else {
								// check <length-percentage> | <number>
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
								} while (lut != LexicalType.OPERATOR_SLASH && lut != LexicalType.IDENT
										&& c < 4);
								if (list.getLength() != 0) {
									setSubpropertyValue("border-image-width", list);
									biwidthUnset = false;
								}
								if (lut == LexicalType.OPERATOR_SLASH) {
									// outset
									nextCurrentValue();
									if (currentValue == null) {
										break;
									}
									list = ValueList.createWSValueList();
									// outset is positive <length> | <number>
									while (ValueFactory.isLengthOrNumberSACUnit(currentValue) && c < 4) {
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
			} else if (currentValue.getLexicalUnitType() == LexicalType.PREFIXED_FUNCTION) {
				setPrefixedValue(currentValue);
				flush();
				return 1;
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
			return 2;
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

		return 0;
	}
}
