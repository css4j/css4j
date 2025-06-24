/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.BufferSimpleWriter;

class OutlineShorthandBuilder extends GenericShorthandBuilder {

	OutlineShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("outline", parentStyle, "none");
	}

	@Override
	int appendPostKeywordShorthandValues(StringBuilder buf, Set<String> declaredSet,
			boolean important) {
		byte check = checkValuesForType(CSSValue.Type.UNSET, declaredSet);
		if (check == 1) {
			// All values are unset
			buf.append("none");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2) {
			return 1;
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		boolean appended = false;

		if (declaredSet.contains("outline-color") || declaredSet.contains("outline-style")) {
			StyleValue color = getCSSValue("outline-color");
			if (color.getPrimitiveType() == Type.IDENT) {
				String scolor = ((CSSTypedValue) color).getStringValue();
				StyleValue style = getCSSValue("outline-style");
				if (style.getPrimitiveType() == Type.IDENT) {
					String sstyle = ((CSSTypedValue) style).getStringValue();
					if ("auto".equalsIgnoreCase(scolor) && "auto".equalsIgnoreCase(sstyle)) {
						buf.append("auto");
						appendValueIfNotInitial(wri, context, "outline-width", true);
						appendPriority(buf, important);
						return 0;
					}
				}
			}
		}

		String[] subp = getLonghandProperties();
		for (String property : subp) {
			if (declaredSet.contains(property)) {
				// First, make sure that it is not a layered property
				StyleValue cssVal = getCSSValue(property);
				if (cssVal.getCssValueType() == CssType.LIST) {
					return 1;
				}
				if (!isNotInitialValue(cssVal, property)) {
					continue;
				}
				if (cssVal.getPrimitiveType() == Type.IDENT
						&& getShorthandDatabase().hasKnownIdentifierValues(property)
						&& !getShorthandDatabase().isIdentifierValue(property,
								((CSSTypedValue) cssVal).getStringValue())) {
					return 1;
				}
				appended = appendValueIfNotInitial(wri, context, property, appended);
			}
		}

		if (!appended) {
			buf.append("none");
		}

		appendPriority(buf, important);
		return 0;
	}

}
