/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

class RGBColorUnitImpl extends ColorUnitImpl {

	private static final long serialVersionUID = 1L;

	public RGBColorUnitImpl() {
		super(LexicalType.RGBCOLOR);
	}

	@Override
	public int getContextIndex() {
		return 0;
	}

	@Override
	CharSequence currentToString() {
		if (identCssText != null) {
			return identCssText;
		}
		return functionalSerialization(value);
	}

	@Override
	RGBColorUnitImpl instantiateLexicalUnit() {
		return new RGBColorUnitImpl();
	}

}
