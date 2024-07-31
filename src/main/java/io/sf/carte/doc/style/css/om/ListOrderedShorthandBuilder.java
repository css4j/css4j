/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

abstract class ListOrderedShorthandBuilder extends OrderedShorthandBuilder {

	ListOrderedShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle, String initialvalue,
			String freeProperty) {
		super(shorthandName, parentStyle, initialvalue, freeProperty);
	}

	@Override
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return 1;
		}

		StyleValue masterValue = getCSSValue(freeProperty);
		ValueList masterList;
		if (masterValue.getCssValueType() != CssType.LIST
				|| !(masterList = (ValueList) masterValue).isCommaSeparated()) {
			return super.appendShorthandSet(buf, declaredSet, important);
		}

		int listLen = masterList.getLength();

		// Check for CSS-wide keywords
		if (checkDeclaredValueListForInherit(declaredSet, listLen)) {
			return 1;
		}
		if (checkDeclaredValueListForKeyword(CSSValue.Type.REVERT, declaredSet, listLen)) {
			return 1;
		}
		if (isInheritedProperty() && checkDeclaredValueListForKeyword(CSSValue.Type.UNSET, declaredSet, listLen)) {
			return 1;
		}

		// Value sanity check
		for (String property : declaredSet) {
			if (isResetProperty(property)) {
				continue;
			}
			StyleValue value = getCSSValue(property);
			CssType type = value.getCssValueType();
			if (type == CssType.LIST) {
				if (invalidListValueClash(declaredSet, property, (ValueList) value)) {
					return 1;
				}
			} else if (invalidValueClash(declaredSet, property, value)) {
				return 1;
			}
		}

		// Append property name
		buf.append(getShorthandName()).append(':');
		for (int index = 0; index < listLen; index++) {
			if (index != 0) {
				buf.append(',');
			}
			boolean appended = false;
			String[] subp = getSubproperties();
			for (String property : subp) {
				if (declaredSet.contains(property)) {
					appended = appendValueText(index, buf, property, appended);
				}
			}
			if (!appended) {
				buf.append(initialvalue);
			}
		}

		appendPriority(buf, important);

		return 0;
	}

	private boolean checkDeclaredValueListForInherit(Set<String> declaredSet, int listLen) {
		for (String propertyName : declaredSet) {
			ValueList list = computeCSSItemList(propertyName, listLen - 1);
			for (int i = 0; i < listLen; i++) {
				if (isInherit(list.item(i))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkDeclaredValueListForKeyword(CSSValue.Type keyword, Set<String> declaredSet,
			int listLen) {
		for (String propertyName : declaredSet) {
			ValueList list = computeCSSItemList(propertyName, listLen - 1);
			for (int i = 0; i < listLen; i++) {
				if (list.item(i).getPrimitiveType() == keyword) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean invalidListValueClash(Set<String> declaredSet, String property, ValueList list) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			if (invalidValueClash(declaredSet, property, list.item(i))) {
				return true;
			}
		}
		return false;
	}

	private boolean appendValueText(int index, StringBuilder buf, String property, boolean appended) {
		StyleValue cssVal = getCSSListItemValue(property, index);
		if (isNotInitialValue(cssVal, property) ||
				(!freeProperty.equals(property) && valueClash(index, property))) {
			if (appended) {
				buf.append(' ');
			}
			buf.append(cssVal.getMinifiedCssText(property));
			return true;
		}
		return appended;
	}

	StyleValue getCSSListItemValue(String propertyName, int index) {
		return computeCSSItemList(propertyName, index).item(index);
	}

	private ValueList computeCSSItemList(String propertyName, int lastIdx) {
		StyleValue value = getCSSValue(propertyName);
		int items;
		ValueList list;
		if (value.getCssValueType() == CssType.LIST && ((ValueList) value).isCommaSeparated()) {
			list = (ValueList) value.clone();
			items = list.getLength();
		} else {
			list = ValueList.createCSValueList();
			list.add(value);
			value = list;
			items = 1;
		}
		if (lastIdx >= items) {
			int j = 0;
			while (items++ <= lastIdx) {
				list.add(list.item(j++));
			}
		}
		return list;
	}

	boolean isConflictingIdentifier(String property, CSSTypedValue freePrimi) {
		// Make sure that 'none' is in animation-fill-mode list in
		// 'identifier.properties'
		return freePrimi.getPrimitiveType() == CSSValue.Type.IDENT
				&& getShorthandDatabase().isIdentifierValue(property, freePrimi.getStringValue());
	}

	boolean listHasConflictingIdentifiers(String property, ValueList list) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			StyleValue item = list.item(i);
			CssType type = item.getCssValueType();
			if (type == CssType.TYPED) {
				if (isConflictingIdentifier(property, (CSSTypedValue) item)) {
					return true;
				}
			} else if (type == CssType.LIST) {
				return listHasConflictingIdentifiers(property, (ValueList) item);
			}
		}
		return false;
	}

	abstract boolean valueClash(int index, String property);

}
