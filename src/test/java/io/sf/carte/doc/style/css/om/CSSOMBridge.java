/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.property.StyleValue;

public class CSSOMBridge {

	public static StyleValue getInitialValue(String propertyName, BaseCSSStyleDeclaration style) {
		return style.defaultPropertyValue(propertyName);
	}

	public static String getOptimizedCssText(BaseCSSStyleDeclaration style) {
		return style.getOptimizedCssText();
	}

	public static CSSHandler createDocumentHandler(BaseCSSStyleSheet css, short commentMode) {
		return css.createSheetHandler(commentMode);
	}

	public static void assertSpecificity(int idCount, int attribClassesCount,
			int namesPseudoelementsCount, Selector sel, SelectorMatcher matcher) {
		Specificity sp = new Specificity(sel, matcher);
		assertEquals(idCount, sp.id_count, "ID count mismatch");
		assertEquals(attribClassesCount, sp.attrib_classes_count,
				"attribute/classes count mismatch");
		assertEquals(namesPseudoelementsCount, sp.names_pseudoelements_count,
				"names/pseudo-elements count mismatch");
	}

	public static String selectorListToString(SelectorList selist, CSSStyleDeclarationRule rule) {
		if (selist == null) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		buf.append(selectorText(rule, selist.item(0), false));
		int sz = selist.getLength();
		for (int i = 1; i < sz; i++) {
			buf.append(' ').append(selectorText(rule, selist.item(i), false));
		}
		return buf.toString();
	}

	public static String selectorText(CSSStyleDeclarationRule rule, Selector sel, boolean omitUniversal) {
		SelectorSerializer serializer = new SelectorSerializer(rule.getParentStyleSheet());
		return serializer.selectorText(sel, omitUniversal);
	}

}
