/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;

abstract class ListOrderedShorthandBuilder extends OrderedShorthandBuilder {

	ListOrderedShorthandBuilder(String shorthandName, BaseCSSStyleDeclaration parentStyle, String initialvalue,
			String freeProperty) {
		super(shorthandName, parentStyle, initialvalue, freeProperty);
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		StyleValue masterValue = getCSSValue(freeProperty);
		ValueList masterList;
		if (masterValue.getCssValueType() != CSSValue.CSS_VALUE_LIST
				|| !(masterList = (ValueList) masterValue).isCommaSeparated()) {
			return super.appendShorthandSet(buf, declaredSet, important);
		}
		int listLen = masterList.getLength();
		// Check for CSS-wide keywords
		if (checkDeclaredValueListForInherit(declaredSet, listLen)) {
			return false;
		}
		if (checkDeclaredValueListForKeyword("unset", declaredSet, listLen)) {
			return false;
		}
		// Value sanity check
		Iterator<String> it = declaredSet.iterator();
		while (it.hasNext()) {
			String property = it.next();
			StyleValue value = getCSSValue(property);
			short type = value.getCssValueType();
			if (type == CSSValue.CSS_VALUE_LIST) {
				if (invalidListValueClash(declaredSet, property, (ValueList) value)) {
					return false;
				}
			} else if (invalidValueClash(declaredSet, property, value)) {
				return false;
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
			for (int i = 0; i < subp.length; i++) {
				String property = subp[i];
				if (declaredSet.contains(property)) {
					appended = appendValueText(index, buf, property, appended);
				}
			}
			if (!appended) {
				buf.append(initialvalue);
			}
		}
		appendPriority(buf, important);
		return true;
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

	private boolean checkDeclaredValueListForKeyword(String keyword, Set<String> declaredSet, int listLen) {
		for (String propertyName : declaredSet) {
			ValueList list = computeCSSItemList(propertyName, listLen - 1);
			for (int i = 0; i < listLen; i++) {
				if (isCssKeywordValue(keyword, list.item(i))) {
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
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST && ((ValueList) value).isCommaSeparated()) {
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

	boolean isConflictingIdentifier(String property, CSSPrimitiveValue freePrimi) {
		// Make sure that 'none' is in animation-fill-mode list in
		// 'identifier.properties'
		return freePrimi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& getShorthandDatabase().isIdentifierValue(property, freePrimi.getStringValue());
	}

	boolean listHasConflictingIdentifiers(String property, CSSValueList list) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			CSSValue item = list.item(i);
			short type = item.getCssValueType();
			if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
				if (isConflictingIdentifier(property, (CSSPrimitiveValue) item)) {
					return true;
				}
			} else if (type == CSSValue.CSS_VALUE_LIST) {
				return listHasConflictingIdentifiers(property, (CSSValueList) item);
			}
		}
		return false;
	}

	abstract boolean valueClash(int index, String property);

}
