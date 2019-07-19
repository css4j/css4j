/*

 Copyright (c) 2005-2019, Carlos Amengual.

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

import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build an 'animation' shorthand from individual properties.
 */
class AnimationShorthandBuilder extends OrderedShorthandBuilder {

	AnimationShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("animation", parentStyle, "none", "animation-name");
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		AbstractCSSValue masterValue = getCSSValue("animation-name");
		ValueList masterList;
		if (masterValue.getCssValueType() != CSSValue.CSS_VALUE_LIST || !(masterList = (ValueList) masterValue).isCommaSeparated()) {
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
			if (invalidListValueClash(declaredSet, property, (ValueList) getCSSValue(property))) {
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

	boolean checkDeclaredValueListForInherit(Set<String> declaredSet, int listLen) {
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

	boolean checkDeclaredValueListForKeyword(String keyword, Set<String> declaredSet, int listLen) {
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

	boolean invalidListValueClash(Set<String> declaredSet, String property, ValueList list) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			if (invalidValueClash(declaredSet, property, list.item(i))) {
				return true;
			}
		}
		return false;
	}

	boolean appendValueText(int index, StringBuilder buf, String property, boolean appended) {
		AbstractCSSValue cssVal = getCSSListItemValue(property, index);
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

	AbstractCSSValue getCSSListItemValue(String propertyName, int index) {
		return computeCSSItemList(propertyName, index).item(index);
	}

	ValueList computeCSSItemList(String propertyName, int lastIdx) {
		AbstractCSSValue value = getCSSValue(propertyName);
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
			while (items++ < lastIdx) {
				list.add(list.item(j++));
			}
		}
		return list;
	}

	boolean valueClash(int index, String property) {
		String chkProperty = property;
		if ("animation-timing-function".equals(property)) {
			chkProperty = "transition-timing-function";
		}
		AbstractCSSValue freePropertyValue = getCSSListItemValue(freeProperty, index);
		short freeType = freePropertyValue.getCssValueType();
		boolean retval = false;
		if (freeType == CSSValue.CSS_PRIMITIVE_VALUE) {
			retval = isConflictingIdentifier(chkProperty, (CSSPrimitiveValue) freePropertyValue);
		} else if (freeType == CSSValue.CSS_VALUE_LIST) {
			retval = listHasConflictingIdentifiers(chkProperty, (CSSValueList) freePropertyValue);
		}
		if (!retval && property.equals("animation-duration")) {
			AbstractCSSValue delay = getCSSListItemValue("animation-delay", index);
			if (isNotInitialValue(delay, "animation-delay")) {
				retval = true;
			}
		}
		return retval;
	}

	private boolean isConflictingIdentifier(String property, CSSPrimitiveValue freePrimi) {
		// Make sure that 'none' is in animation-fill-mode list in
		// 'identifier.properties'
		return freePrimi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& pdb.isIdentifierValue(property, freePrimi.getStringValue());
	}

	private boolean listHasConflictingIdentifiers(String property, CSSValueList list) {
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

	@Override
	boolean validValueClash(String property) {
		String chkProperty = property;
		if ("animation-timing-function".equals(property)) {
			chkProperty = "transition-timing-function";
		}
		// Make sure that 'none' is in animation-fill-mode list in 'identifier.properties'
		boolean retval = super.validValueClash(chkProperty);
		if (!retval && property.equals("animation-duration")) {
			AbstractCSSValue delay = getCSSValue("animation-delay");
			if (isNotInitialValue(delay, "animation-delay")) {
				retval = true;
			}
		}
		return retval;
	}
}
