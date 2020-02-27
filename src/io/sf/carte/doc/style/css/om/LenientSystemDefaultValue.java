/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;

class LenientSystemDefaultValue extends SafeSystemDefaultValue {

	LenientSystemDefaultValue(ExtendedCSSPrimitiveValue defvalue) {
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
