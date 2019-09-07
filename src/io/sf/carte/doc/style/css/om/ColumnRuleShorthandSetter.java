/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.LexicalUnit;

import io.sf.carte.doc.style.css.property.StyleValue;
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
				StyleValue cssValue = createCSSValue("column-rule-width", currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if ("column-rule-style".equals(subproperty)) {
			short utype = currentValue.getLexicalUnitType();
			if (LexicalUnit.SAC_IDENT == utype && testIdentifiers(subproperty)) {
				StyleValue cssValue = createCSSValue("column-rule-style", currentValue);
				setSubpropertyValue(subproperty, cssValue);
				nextCurrentValue();
				return true;
			}
		} else if ("column-rule-color".equals(subproperty) && BaseCSSStyleDeclaration.testColor(currentValue)) {
			StyleValue cssValue = createCSSValue("column-rule-color", currentValue);
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

}