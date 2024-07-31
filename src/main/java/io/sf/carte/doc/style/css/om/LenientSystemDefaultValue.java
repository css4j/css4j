/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSTypedValue;

class LenientSystemDefaultValue extends SafeSystemDefaultValue {

	private static final long serialVersionUID = 1L;

	LenientSystemDefaultValue(CSSTypedValue defvalue) {
		super(defvalue);
	}

	@Override
	public boolean isSystemDefault() {
		return false;
	}

	@Override
	public String getCssText() {
		return getDefaultCssText();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return getDefaultMinifiedCssText(propertyName);
	}

}
