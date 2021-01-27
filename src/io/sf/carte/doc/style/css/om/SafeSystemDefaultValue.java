/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;

class SafeSystemDefaultValue extends SystemDefaultValue {

	private ExtendedCSSPrimitiveValue defvalue;

	SafeSystemDefaultValue(ExtendedCSSPrimitiveValue defvalue) {
		super();
		this.defvalue = defvalue;
	}

	String getDefaultCssText() {
		return defvalue.getCssText();
	}

	String getDefaultMinifiedCssText(String propertyName) {
		return defvalue.getMinifiedCssText(propertyName);
	}

	@Override
	public short getPrimitiveType() {
		return defvalue.getPrimitiveType();
	}

	@Override
	public String getStringValue() throws DOMException {
		return defvalue.getStringValue();
	}

	@Override
	public RGBAColor getRGBColorValue() throws DOMException {
		return defvalue.getRGBColorValue();
	}

	@Override
	public float getFloatValue(short unitType) throws DOMException {
		return defvalue.getFloatValue(unitType);
	}

}
