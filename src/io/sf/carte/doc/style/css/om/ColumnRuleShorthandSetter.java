/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class ColumnRuleShorthandSetter extends ShorthandSetter {

	ColumnRuleShorthandSetter(BaseCSSStyleDeclaration style) {
			super(style, "column-rule");
		}
		
	@Override
	protected boolean assignSubproperty(String subproperty) {
		if ("column-rule-width".equals(subproperty)) {
			if ((LexicalUnit.SAC_IDENT == currentValue.getLexicalUnitType() && testIdentifiers(subproperty))
					|| ValueFactory.isSizeSACUnit(currentValue)) {
				AbstractCSSValue cssValue = createCSSValue("column-rule-width", currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if ("column-rule-style".equals(subproperty)) {
			short utype = currentValue.getLexicalUnitType();
			if (LexicalUnit.SAC_IDENT == utype && testIdentifiers(subproperty)) {
				AbstractCSSValue cssValue = createCSSValue("column-rule-style", currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if ("column-rule-color".equals(subproperty) && BaseCSSStyleDeclaration.testColor(currentValue)) {
			AbstractCSSValue cssValue = createCSSValue("column-rule-color", currentValue);
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

}