/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

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
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		// Append property name
		buf.append(getShorthandName()).append(':');
		// Check for CSS-wide keywords
		byte check = checkValuesForInherit(declaredSet);
		if (check == 1) {
			// All values are inherit
			buf.append("inherit");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		check = checkValuesForKeyword("unset", declaredSet);
		if (check == 1) {
			// All values are unset
			buf.append("unset");
			appendPriority(buf, important);
			return true;
		} else if (check == 2) {
			return false;
		}
		boolean appended = false;
		if (declaredSet.contains("font-variant-ligatures")) {
			appended = appendValueIfNotInitial(buf, "font-variant-ligatures", false);
		}
		if (declaredSet.contains("font-variant-position")) {
			appended = appendValueIfNotInitial(buf, "font-variant-position", appended);
		}
		if (declaredSet.contains("font-variant-caps")) {
			appended = appendValueIfNotInitial(buf, "font-variant-caps", appended);
		}
		if (declaredSet.contains("font-variant-numeric")) {
			appended = appendValueIfNotInitial(buf, "font-variant-numeric", appended);
		}
		if (declaredSet.contains("font-variant-alternates")) {
			appended = appendValueIfNotInitial(buf, "font-variant-alternates", appended);
		}
		if (declaredSet.contains("font-variant-east-asian")) {
			appended = appendValueIfNotInitial(buf, "font-variant-east-asian", appended);
		}
		if (!appended) {
			buf.append("normal");
		}
		appendPriority(buf, important);
		return true;
	}

}
