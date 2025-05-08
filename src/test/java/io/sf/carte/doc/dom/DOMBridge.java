/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;

public class DOMBridge {

	public static BaseDocumentCSSStyleSheet createDocumentStyleSheet(CSSDOMImplementation impl, int origin) {
		return impl.createDocumentStyleSheet(origin);
	}

	public static BaseCSSStyleSheet createLinkedStyleSheet(CSSDOMImplementation impl, DOMElement element) {
		return impl.createLinkedStyleSheet(element, null, null);
	}
}
