/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.ValueFactory.ListValueItem;
import io.sf.carte.doc.style.css.property.ValueList;

class GridShorthandSetter extends BaseGridShorthandSetter {

	private final String[] subproperties = { "grid-template-columns", "grid-template-rows", "grid-template-areas",
			"grid-auto-rows", "grid-auto-columns", "grid-auto-flow" };

	GridShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "grid");
	}

	@Override
	protected String[] getShorthandSubproperties() {
		return subproperties;
	}

	@Override
	public boolean assignSubproperties() {
		// Keyword scan
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		setSubpropertiesToDefault();
		// Do syntax detection first
		// Other syntaxes. We first test for 'none'
		if (isNoneDeclaration()) {
			appendValueItemString("none");
			flush();
		} else {
			if (fullSyntax()) {
				flush();
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void setSubpropertiesToDefault() {
		String[] subp = getShorthandSubproperties();
		for (String pname : subp) {
			setPropertyToDefault(pname);
		}
	}

	boolean fullSyntax() {
		boolean setTemplateAreas = false;
		boolean gridAutoRows = false;
		LexicalUnit fullValue = currentValue;
		ValueList gridTemplateRows = ValueList.createWSValueList();
		ValueList gridTemplateAreas = ValueList.createWSValueList();
		ValueList lineNames = null;
		byte ret = isAutoflowOrDenseKeyword();
		if (ret == -1) {
			return false;
		}
		if (ret != 0) {
			// set grid-auto-rows
			gridAutoRows = true;
			if (ret == 2) {
				setGridAutoFlow("row", true);
			} else {
				setGridAutoFlow("row", false);
			}
		}
		boolean missSlash = true;
		short lasttype = -1;
		topLoop: do {
			AbstractCSSValue value;
			short type;
			switch (type = currentValue.getLexicalUnitType()) {
			case LexicalUnit2.SAC_LEFT_BRACKET:
				// Line name
				if (lasttype == LexicalUnit.SAC_STRING_VALUE) {
					gridTemplateRows.add(GridAreaShorthandSetter.createAutoValue());
				}
				ListValueItem item = valueFactory.parseBracketList(currentValue.getNextLexicalUnit(), styleDeclaration,
						true);
				ValueList newLineNames = item.getCSSValue();
				if (lineNames == null) {
					lineNames = newLineNames;
					gridTemplateRows.add(lineNames);
				} else {
					lineNames.addAll(newLineNames);
				}
				appendValueItemString(newLineNames);
				currentValue = item.getNextLexicalUnit();
				lasttype = type;
				break;
			case LexicalUnit.SAC_STRING_VALUE:
				if (lasttype == LexicalUnit.SAC_STRING_VALUE) {
					gridTemplateRows.add(GridAreaShorthandSetter.createAutoValue());
				}
				setTemplateAreas = true;
				lineNames = null;
				value = createCSSValue();
				gridTemplateAreas.add(value);
				appendValueItemString(value);
				lasttype = type;
				break;
			case LexicalUnit.SAC_OPERATOR_SLASH:
				if (lasttype != -1) {
					currentValue = currentValue.getNextLexicalUnit();
					if (currentValue != null) {
						getValueItemBuffer().append(" /");
						getValueItemBufferMini().append('/');
						String property;
						ret = isAutoflowOrDenseKeyword();
						if (ret == -1) {
							return false;
						}
						if (ret != 0) {
							// set grid-auto-columns
							property = "grid-auto-columns";
							if (ret == 2) {
								setGridAutoFlow("column", true);
							} else {
								setGridAutoFlow("column", false);
							}
							if (gridAutoRows) {
								// Error
								syntaxError("Found two auto-flow declarations: "
										+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue));
								return false;
							}
						} else {
							property = "grid-template-columns";
						}
						value = valueFactory.createCSSValue(currentValue, styleDeclaration);
						value = subpropertyValue(value);
						setSubpropertyValue(property, value);
						appendValueItemString(value);
						missSlash = false;
						break topLoop;
					}
					syntaxError("Unexpected end of declaration after slash '/' in "
							+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue));
				}
				return false;
			default:
				if (setTemplateAreas && type == LexicalUnit.SAC_FUNCTION
						&& "repeat".equalsIgnoreCase(currentValue.getFunctionName())) {
					syntaxError("This syntax does not allow repeat(): "
							+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue));
					return false;
				}
				lineNames = null;
				value = createCSSValue();
				gridTemplateRows.add(value);
				appendValueItemString(value);
				lasttype = type;
				break;
			}
		} while (currentValue != null);
		// Check for possible error
		if (!setTemplateAreas && missSlash) {
			String message = "Not a correct rows / columns syntax: "
					+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue);
			syntaxError(message);
			return false;
		}
		if (gridTemplateRows.getLength() != 0) {
			// Is it different than a set of 'auto' ?
			if (!isAutoOnly(gridTemplateRows)) {
				AbstractCSSValue value;
				String property;
				if (gridAutoRows) {
					property = "grid-auto-rows";
				} else {
					property = "grid-template-rows";
				}
				if (gridTemplateRows.getLength() != 1) {
					value = gridTemplateRows;
				} else {
					value = gridTemplateRows.item(0);
				}
				setSubpropertyValue(property, value);
			} else {
				setSubpropertyValue("grid-template-rows", gridTemplateRows.item(0));
			}
		} else if (setTemplateAreas) {
			IdentifierValue auto = new IdentifierValue("auto");
			auto.setSubproperty(true);
			setSubpropertyValue("grid-template-rows", auto);
		}
		if (gridTemplateAreas.getLength() != 0) {
			AbstractCSSValue value;
			if (gridTemplateAreas.getLength() != 1) {
				value = gridTemplateAreas;
			} else {
				value = gridTemplateAreas.item(0);
			}
			setSubpropertyValue("grid-template-areas", value);
		}
		return true;
	}

	private void setGridAutoFlow(String ident, boolean dense) {
		AbstractCSSValue value;
		IdentifierValue cssident = new IdentifierValue(ident);
		cssident.setSubproperty(true);
		if (!dense) {
			value = cssident;
		} else {
			IdentifierValue cssdense = new IdentifierValue("dense");
			cssdense.setSubproperty(true);
			ValueList list = ValueList.createWSValueList();
			list.add(cssident);
			list.add(cssdense);
			value = list;
		}
		setSubpropertyValue("grid-auto-flow", value);
	}

	private byte isAutoflowOrDenseKeyword() {
		byte ret = 0;
		if (currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String sv = currentValue.getStringValue();
			if ("auto-flow".equalsIgnoreCase(sv)) {
				StringBuilder buf = getValueItemBuffer();
				if (buf.length() != 0) {
					buf.append(' ');
				}
				currentValue = currentValue.getNextLexicalUnit();
				if (currentValue != null && currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT
						&& "dense".equalsIgnoreCase(currentValue.getStringValue())) {
					appendValueItemString("auto-flow dense");
					ret = 2;
					currentValue = currentValue.getNextLexicalUnit();
				} else {
					appendValueItemString("auto-flow");
					ret = 1;
				}
			}
			if ("dense".equalsIgnoreCase(sv)) {
				StringBuilder buf = getValueItemBuffer();
				if (buf.length() != 0) {
					buf.append(' ');
					getValueItemBufferMini().append(' ');
				}
				currentValue = currentValue.getNextLexicalUnit();
				if (currentValue != null && currentValue.getLexicalUnitType() == LexicalUnit.SAC_IDENT
						&& "auto-flow".equalsIgnoreCase(currentValue.getStringValue())) {
					currentValue = currentValue.getNextLexicalUnit();
					appendValueItemString("auto-flow dense");
					ret = 2;
				} else {
					ret = -1;
				}
			}
		}
		return ret;
	}

}
