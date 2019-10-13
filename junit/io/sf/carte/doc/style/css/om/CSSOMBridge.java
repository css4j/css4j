/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;

public class CSSOMBridge {

	public static StyleValue getInitialValue(String propertyName, BaseCSSStyleDeclaration style, PropertyDatabase pdb) {
		return style.defaultPropertyValue(propertyName, pdb);
	}

	public static String getOptimizedCssText(BaseCSSStyleDeclaration style) {
		return style.getOptimizedCssText();
	}

	public static SelectorList getSelectorList(CSSStyleDeclarationRule rule) {
		return rule.getSelectorList();
	}

	public static CSSHandler createDocumentHandler(BaseCSSStyleSheet css, short commentMode) {
		return css.createDocumentHandler(commentMode);
	}

}
