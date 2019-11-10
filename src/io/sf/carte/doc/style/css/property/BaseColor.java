/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.text.NumberFormat;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;

class BaseColor {

	PrimitiveValue alpha = ColorValue.opaqueAlpha;

	BaseColor() {
		super();
	}

	public PrimitiveValue getAlpha() {
		return alpha;
	}

	public void setAlpha(PrimitiveValue alpha) {
		if (alpha == null) {
			throw new NullPointerException();
		}
		if (alpha.getUnitType() != CSSUnit.CSS_NUMBER && alpha.getUnitType() != CSSUnit.CSS_PERCENTAGE
				&& alpha.getCssValueType() != CssType.PROXY && alpha.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with saturation.");
		}
		this.alpha = alpha;
	}

	/**
	 * Is this value non-opaque?
	 * 
	 * @return {@code true} if the alpha channel is non-opaque or not a number.
	 */
	boolean isNonOpaque() {
		return alpha.getPrimitiveType() != Type.NUMERIC
				|| ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER) != 1f;
	}

	StringBuilder appendAlphaChannel(StringBuilder buf) {
		String text;
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			float f = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER);
			text = formattedNumber(f);
		} else {
			text = alpha.getCssText();
		}
		return buf.append(text);
	}

	StringBuilder appendAlphaChannelMinified(StringBuilder buf) {
		String text;
		if (alpha.getUnitType() == CSSUnit.CSS_NUMBER) {
			float f = ((CSSTypedValue) alpha).getFloatValue(CSSUnit.CSS_NUMBER);
			text = formattedNumberMinified(f);
		} else {
			text = alpha.getMinifiedCssText("");
		}
		return buf.append(text);
	}

	private String formattedNumber(float f) {
		NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(0);
		return format.format(f);
	}

	private String formattedNumberMinified(float f) {
		NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(0);
		format.setMinimumIntegerDigits(0);
		return format.format(f);
	}

}
