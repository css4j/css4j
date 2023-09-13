/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Build a font-variant shorthand from individual properties.
 */
class FontVariantBuilder extends ShorthandBuilder {

	FontVariantBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("font-variant", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 6;
	}

	@Override
	boolean isInheritedProperty() {
		return true;
	}

	@Override
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return 1;
		}
		// Append property name
		buf.append(getShorthandName()).append(':');
		// Check for CSS-wide keywords
		byte check = checkValuesForInherit(declaredSet);
		if (check == 1) {
			// All values are inherit
			buf.append("inherit");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2) {
			return 1;
		}
		// Check for 'unset'
		check = checkValuesForType(CSSValue.Type.UNSET, declaredSet);
		if (check == 1) {
			// All values are unset
			buf.append("unset");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2) {
			return 1;
		}
		// Check for 'revert'
		check = checkValuesForType(CSSValue.Type.REVERT, declaredSet);
		if (check == 1) {
			// All values are revert
			buf.append("revert");
			appendPriority(buf, important);
			return 0;
		} else if (check == 2) {
			return 1;
		}
		// pending value check
		if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
			return 1;
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();
		boolean appended = false;
		if (declaredSet.contains("font-variant-ligatures")) {
			appended = appendValueIfNotInitial(wri, context, "font-variant-ligatures", false);
		}
		if (declaredSet.contains("font-variant-position")) {
			appended = appendValueIfNotInitial(wri, context, "font-variant-position", appended);
		}
		if (declaredSet.contains("font-variant-caps")) {
			appended = appendValueIfNotInitial(wri, context, "font-variant-caps", appended);
		}
		if (declaredSet.contains("font-variant-numeric")) {
			appended = appendValueIfNotInitial(wri, context, "font-variant-numeric", appended);
		}
		if (declaredSet.contains("font-variant-alternates")) {
			appended = appendValueIfNotInitial(wri, context, "font-variant-alternates", appended);
		}
		if (declaredSet.contains("font-variant-east-asian")) {
			appended = appendValueIfNotInitial(wri, context, "font-variant-east-asian", appended);
		}
		if (!appended) {
			buf.append("normal");
		}
		appendPriority(buf, important);
		return 0;
	}

}
