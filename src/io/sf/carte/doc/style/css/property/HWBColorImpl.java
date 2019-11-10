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
import io.sf.carte.doc.style.css.HWBColor;

class HWBColorImpl extends BaseColor implements HWBColor {

	private PrimitiveValue hue = null;
	private PrimitiveValue whiteness = null;
	private PrimitiveValue blackness = null;

	HWBColorImpl() {
		super();
	}

	@Override
	public PrimitiveValue getHue() {
		return hue;
	}

	public void setHue(PrimitiveValue hue) {
		if (hue == null) {
			throw new NullPointerException();
		}
		if (hue.getUnitType() != CSSUnit.CSS_NUMBER && !CSSUnit.isAngleUnitType(hue.getUnitType())
				&& hue.getCssValueType() != CssType.PROXY && hue.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with hue.");
		}
		this.hue = hue;
	}

	@Override
	public PrimitiveValue getWhiteness() {
		return whiteness;
	}

	public void setWhiteness(PrimitiveValue whiteness) {
		if (whiteness == null) {
			throw new NullPointerException();
		}
		if (whiteness.getUnitType() != CSSUnit.CSS_PERCENTAGE && whiteness.getCssValueType() != CssType.PROXY
				 && whiteness.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with saturation.");
		}
		this.whiteness = whiteness;
	}

	@Override
	public PrimitiveValue getBlackness() {
		return blackness;
	}

	public void setBlackness(PrimitiveValue blackness) {
		if (blackness == null) {
			throw new NullPointerException();
		}
		if (blackness.getUnitType() != CSSUnit.CSS_PERCENTAGE && blackness.getCssValueType() != CssType.PROXY
				 && blackness.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Type not compatible with lightness.");
		}
		this.blackness = blackness;
	}

	@Override
	public String toString() {
		boolean nonOpaque = isNonOpaque();
		StringBuilder buf = new StringBuilder(20);
		buf.append("hwb(");
		appendHue(buf);
		buf.append(' ').append(whiteness.getCssText())
				.append(' ').append(blackness.getCssText());
		if (nonOpaque) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	private void appendHue(StringBuilder buf) {
		if (hue.getUnitType() == CSSUnit.CSS_DEG) {
			float val = ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(1);
			String s = format.format(val);
			buf.append(s);
		} else {
			buf.append(hue.getCssText());
		}
	}

	String toMinifiedString() {
		boolean nonOpaque = isNonOpaque();
		StringBuilder buf = new StringBuilder(20);
		buf.append("hwb(");
		appendMinifiedHue(buf);
		buf.append(' ').append(whiteness.getCssText())
				.append(' ').append(blackness.getCssText());
		if (nonOpaque) {
			buf.append('/');
			appendAlphaChannelMinified(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	private void appendMinifiedHue(StringBuilder buf) {
		if (hue.getUnitType() == CSSUnit.CSS_DEG) {
			float val = ((CSSTypedValue) hue).getFloatValue(CSSUnit.CSS_DEG);
			NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
			format.setMinimumIntegerDigits(0);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(1);
			String s = format.format(val);
			buf.append(s);
		} else {
			buf.append(hue.getMinifiedCssText("color"));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blackness == null) ? 0 : blackness.hashCode());
		result = prime * result + ((hue == null) ? 0 : hue.hashCode());
		result = prime * result + ((whiteness == null) ? 0 : whiteness.hashCode());
		result = prime * result + alpha.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HWBColorImpl other = (HWBColorImpl) obj;
		if (blackness == null) {
			if (other.blackness != null) {
				return false;
			}
		} else if (!blackness.equals(other.blackness)) {
			return false;
		}
		if (hue == null) {
			if (other.hue != null) {
				return false;
			}
		} else if (!hue.equals(other.hue)) {
			return false;
		}
		if (whiteness == null) {
			if (other.whiteness != null) {
				return false;
			}
		} else if (!whiteness.equals(other.whiteness)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public HWBColorImpl clone() {
		HWBColorImpl clon = new HWBColorImpl();
		clon.alpha = alpha.clone();
		if (hue != null) {
			clon.hue = hue.clone();
		}
		if (whiteness != null) {
			clon.whiteness = whiteness.clone();
		}
		if (blackness != null) {
			clon.blackness = blackness.clone();
		}
		return clon;
	}

}
