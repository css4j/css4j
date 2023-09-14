/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.KeywordValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory.ListValueItem;
import io.sf.carte.doc.style.css.property.ValueList;

abstract class BaseGridShorthandSetter extends ShorthandSetter {

	BaseGridShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
	}

	@Override
	public void init(LexicalUnit shorthandValue, boolean important) {
		this.currentValue = shorthandValue;
		setPriority(important);
		initValueString();
		// Bracket-lists are involved here, so we do not add the first value text.
	}

	boolean isNoneDeclaration() {
		return currentValue.getLexicalUnitType() == LexicalType.IDENT
				&& "none".equalsIgnoreCase(currentValue.getStringValue()) && currentValue.getNextLexicalUnit() == null;
	}

	boolean isTemplateAreasSyntax() {
		LexicalUnit lu = currentValue;
		do {
			if (lu.getLexicalUnitType() == LexicalType.STRING) {
				// Has grid-template-areas
				return true;
			}
			lu = lu.getNextLexicalUnit();
		} while (lu != null);
		return false;
	}

	/* @formatter:off
	 * 
	 * grid-template full syntax:
	 * 
	 * [ <line-names>? <string> <track-size>? <line-names>? ]+ [ / <explicit-track-list> ]?
	 * 
	 * <explicit-track-list> = [ <line-names>? <track-size> ]+ <line-names>?
	 * 
	 * -Set grid-template-areas to the strings listed.
	 * -Set grid-template-rows to the <track-size>s following each string (filling in auto
	 *  for any missing sizes), and splicing in the named lines defined before/after each size.
	 * -Set grid-template-columns to the track listing specified after the slash (or none,
	 *  if not specified).
	 * 
	 * If setTemplateAreas is false, syntax is: <‘grid-template-rows’> / <‘grid-template-columns’>
	 * 
	 *  @formatter:on
	 */
	boolean templateSyntax(boolean setTemplateAreas) {
		// Bracket-lists are involved here, so we ignore nextCurrentValue() completely
		LexicalUnit fullValue = currentValue;
		ValueList gridTemplateRows = ValueList.createWSValueList();
		ValueList gridTemplateAreas = ValueList.createWSValueList();
		ValueList lineNames = null;
		boolean missSlash = !setTemplateAreas;
		LexicalType lasttype = LexicalType.UNKNOWN;

		topLoop: do {
			StyleValue value;
			LexicalType type;
			switch (type = currentValue.getLexicalUnitType()) {
			case LEFT_BRACKET:
				// Line name
				if (lasttype == LexicalType.STRING) {
					// We skipped a track-size
					gridTemplateRows.add(createAutoValue());
				}
				LexicalUnit nlu = currentValue.getNextLexicalUnit();
				ListValueItem item = valueFactory.parseBracketList(nlu, styleDeclaration, true);
				if (item != null) {
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
				} else {
					currentValue = nlu.getNextLexicalUnit();
				}
				break;
			case STRING:
				if (lasttype == LexicalType.STRING) {
					// We skipped a track-size
					gridTemplateRows.add(createAutoValue());
				}
				lineNames = null;
				value = createCSSValue();
				gridTemplateAreas.add(value);
				appendValueItemString(value);
				lasttype = type;
				break;
			case OPERATOR_SLASH:
				if (lasttype != LexicalType.UNKNOWN) {
					if (lasttype == LexicalType.STRING) {
						// We skipped a track-size
						gridTemplateRows.add(createAutoValue());
					}
					currentValue = currentValue.getNextLexicalUnit();
					if (currentValue != null) {
						value = valueFactory.createCSSValue(currentValue, styleDeclaration);
						value = subpropertyValue(value);
						setSubpropertyValue("grid-template-columns", value);
						getValueItemBuffer().append(" /");
						getValueItemBufferMini().append('/');
						appendValueItemString(value);
						missSlash = false;
						break topLoop;
					}
					syntaxError("Unexpected end of declaration after slash '/' in "
							+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue));
				} else {
					syntaxError("Slash '/' was the first token in "
							+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue));
				}
				return false;
			default:
				if (setTemplateAreas && type == LexicalType.FUNCTION
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
		if (missSlash) {
			String message = "Not a correct rows / columns syntax: "
					+ BaseCSSStyleDeclaration.lexicalUnitToString(fullValue);
			syntaxError(message);
			return false;
		}

		if (gridTemplateRows.getLength() != 0) {
			StyleValue value;
			if (gridTemplateRows.getLength() != 1) {
				value = gridTemplateRows;
			} else {
				value = gridTemplateRows.item(0);
			}
			setSubpropertyValue("grid-template-rows", value);
		} else if (setTemplateAreas) {
			IdentifierValue auto = new IdentifierValue("auto");
			auto.setSubproperty(true);
			setSubpropertyValue("grid-template-rows", auto);
		}

		if (gridTemplateAreas.getLength() != 0) {
			StyleValue value;
			if (gridTemplateAreas.getLength() != 1) {
				value = gridTemplateAreas;
			} else {
				value = gridTemplateAreas.item(0);
			}
			setSubpropertyValue("grid-template-areas", value);
		} // grid-template-areas: none (initial value)

		return true;
	}

	StyleValue subpropertyValue(StyleValue value) {
		switch (value.getCssValueType()) {
		case TYPED:
		case PROXY:
			((PrimitiveValue) value).setSubproperty(true);
			break;
		case LIST:
			((ValueList) value).setSubproperty(true);
			break;
		case KEYWORD:
			value = ((KeywordValue) value).asSubproperty();
		default:
		}
		return value;
	}

	void syntaxError(String message) {
		BaseCSSDeclarationRule rule = styleDeclaration.getParentRule();
		if (rule != null) {
			rule.getStyleDeclarationErrorHandler().shorthandSyntaxError(getShorthandName(), message);
		}
	}

	@Override
	protected boolean isDelimiterChar(char c, String cssText) {
		return c == ',' || c == '/' || (c == ']' && cssText.charAt(0) == '[');
	}

	/*
	 * Is list different than a set of 'auto' ?
	 */
	static boolean isAutoOnly(ValueList list) {
		Iterator<StyleValue> it = list.iterator();
		while (it.hasNext()) {
			if (!"auto".equalsIgnoreCase(it.next().getCssText())) {
				return false;
			}
		}
		return true;
	}

	static IdentifierValue createAutoValue() {
		IdentifierValue ident = new IdentifierValue("auto");
		ident.setSubproperty(true);
		return ident;
	}

	/* @formatter:off
	 * 
	 * Syntax for grid-template-columns / grid-template-rows (set grid-template-areas to
	 * none):
	 * 
	 * <track-list> | <auto-track-list>
	 * 
	 * <track-list> = [ <line-names>? [ <track-size> | <track-repeat> ] ]+ <line-names>?
	 * <auto-track-list> = [ <line-names>? [ <fixed-size> | <fixed-repeat> ] ]* <line-names>?
	 *        <auto-repeat> [ <line-names>? [ <fixed-size> | <fixed-repeat> ] ]* <line-names>?
	 * <line-names> = '[' <custom-ident>* ']'
	 * <track-size> = <track-breadth> |
	 *                 minmax(<inflexible-breadth> , <track-breadth> ) |
	 *                 fit-content( <length-percentage> )
	 * <track-repeat> = repeat( [ <positive-integer> ] , [ <line-names>? <track-size> ]+ <line-names>? )
	 * <fixed-size> = <fixed-breadth> |
	 *                 minmax( <fixed-breadth> , <track-breadth> ) |
	 *                 minmax( <inflexible-breadth> , <fixed-breadth> )
	 * <fixed-repeat> = repeat( [ <positive-integer> ] , [ <line-names>? <fixed-size> ]+ <line-names>? )
	 * <auto-repeat> = repeat( [ auto-fill | auto-fit ] , [ <line-names>? <fixed-size> ]+ <line-names>? )
	 * <track-breadth> = <length-percentage> | <flex> | min-content | max-content | auto
	 * <inflexible-breadth> = <length-percentage> | min-content | max-content | auto
	 * <fixed-breadth> = <length-percentage>
	 * 
	 *  @formatter:on
	 */
}
