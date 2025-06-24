/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.List;

import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.IdentifierValue;

/**
 * Decompose a shorthand where some identifiers may set more than one property.
 */
class OutlineShorthandSetter extends ShorthandSetter {

	public OutlineShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "outline");
	}

	@Override
	public short assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return 0;
		} else if (kwscan == 2) {
			return 2;
		}

		List<String> subp = subpropertyList();
		boolean identAuto = false;
		while (currentValue != null) {
			if (currentValue.getLexicalUnitType() == LexicalType.IDENT
					&& "auto".equalsIgnoreCase(currentValue.getStringValue())) {
				identAuto = true;
				nextCurrentValue();
				continue;
			}
			boolean assigned = false;
			Iterator<String> it = subp.iterator();
			while (it.hasNext()) {
				String pname = it.next();
				// Try to match this property name with the current value
				if (assignSubproperty(pname)) {
					it.remove();
					assigned = true;
					break;
				}
			}
			if (!assigned) {
				if (checkPrefixedValue()) {
					flush();
					return 1;
				}
				if (currentValue.getLexicalUnitType() != LexicalType.IDENT
						|| !"none".equalsIgnoreCase(currentValue.getStringValue())) {
					return 2;
				}
				currentValue = currentValue.getNextLexicalUnit();
				appendValueItemString();
			}
		}

		if (!subp.isEmpty()) {
			// Add remaining unassigned properties
			if (identAuto) {
				IdentifierValue auto = new IdentifierValue("auto");
				if (subp.remove("outline-style")) {
					auto.setSubproperty(true);
					setSubpropertyValue("outline-style", auto);
					auto = auto.clone();
				}
				if (subp.remove("outline-color")) {
					auto.setSubproperty(true);
					setSubpropertyValue("outline-color", auto);
				}
			}
			Iterator<String> it = subp.iterator();
			while (it.hasNext()) {
				addUnassignedProperty(it.next());
			}
			resetSubproperties();
		}

		flush();

		return 0;
	}

}
