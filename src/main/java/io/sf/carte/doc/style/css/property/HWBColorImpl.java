/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.HWBColor;

class HWBColorImpl extends BaseColor implements HWBColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue hue = null;
	private PrimitiveValue whiteness = null;
	private PrimitiveValue blackness = null;

	HWBColorImpl() {
		super();
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.HWB;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);

		HWBColorImpl setfrom = (HWBColorImpl) color;
		this.hue = setfrom.getHue();
		this.whiteness = setfrom.getWhiteness();
		this.blackness = setfrom.getBlackness();
	}

	@Override
	public NumberValue component(String component) {
		NumberValue ret;
		switch (component) {
		case "h":
			ret = hueComponent((CSSTypedValue) getHue());
			break;
		case "w":
			ret = numberComponent((CSSTypedValue) getWhiteness(), 1f);
			break;
		case "b":
			ret = numberComponent((CSSTypedValue) getBlackness(), 1f);
			break;
		case "alpha":
			ret = numberComponent((CSSTypedValue) alpha, 100f);
			break;
		default:
			return null;
		}
		return ret;
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getHue();
		case 2:
			return getWhiteness();
		case 3:
			return getBlackness();
		}
		return null;
	}

	@Override
	void setComponent(int index, PrimitiveValue component) {
		switch (index) {
		case 0:
			setAlpha(component);
			break;
		case 1:
			setHue(component);
			break;
		case 2:
			setWhiteness(component);
			break;
		case 3:
			setBlackness(component);
		}
	}

	@Override
	public PrimitiveValue getHue() {
		return hue;
	}

	public void setHue(PrimitiveValue hue) {
		this.hue = enforceHueComponent(hue);
	}

	@Override
	public PrimitiveValue getWhiteness() {
		return whiteness;
	}

	public void setWhiteness(PrimitiveValue whiteness) {
		this.whiteness = enforcePcntComponent(whiteness);
	}

	@Override
	public PrimitiveValue getBlackness() {
		return blackness;
	}

	public void setBlackness(PrimitiveValue blackness) {
		this.blackness = enforcePcntComponent(blackness);
	}

	@Override
	boolean hasPercentageComponent() {
		return (whiteness != null && whiteness.getUnitType() == CSSUnit.CSS_PERCENTAGE)
				|| (blackness != null && blackness.getUnitType() == CSSUnit.CSS_PERCENTAGE);
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getWhiteness()) && isConvertibleComponent(getHue())
				&& isConvertibleComponent(getBlackness());
	}

	@Override
	void setColorComponents(double[] hwb) {
		NumberValue h = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, (float) hwb[0]);
		h.setSubproperty(true);
		h.setAbsolutizedUnit();
		setHue(h);

		PercentageValue w = new PercentageValue();
		w.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) hwb[1]);
		w.setSubproperty(true);
		w.setAbsolutizedUnit();
		setWhiteness(w);

		PercentageValue b = new PercentageValue();
		b.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) hwb[2]);
		b.setSubproperty(true);
		b.setAbsolutizedUnit();
		setBlackness(b);
	}

	@Override
	public double[] toNumberArray() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double[] hwb = new double[3];
		hwb[0] = ColorUtil.hueDegrees((CSSTypedValue) getHue());
		hwb[1] = ColorUtil.floatPercent((CSSTypedValue) getWhiteness());
		hwb[2] = ColorUtil.floatPercent((CSSTypedValue) getBlackness());
		return hwb;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double hue = ColorUtil.hueDegrees((CSSTypedValue) getHue()) / 360f;
		double whiteness = ColorUtil.fraction((CSSTypedValue) getWhiteness());
		double blackness = ColorUtil.fraction((CSSTypedValue) getBlackness());

		double[] rgb = new double[3];
		hwbToSRGB(hue, whiteness, blackness, rgb);
		return rgb;
	}

	private void hwbToSRGB(double hue, double whiteness, double blackness, double[] rgb) {
		if (hue > 1d) {
			hue -= Math.floor(hue);
		} else if (hue < 0d) {
			hue = hue - Math.floor(hue) + 1d;
		}
		hue *= 6d;
		double fh = Math.floor(hue);
		double f = hue - fh;
		int ifh = (int) fh;
		if (ifh % 2 == 1) {
			f = 1d - f;
		}
		double value = 1d - blackness;
		double wv = whiteness + f * (value - whiteness);
		switch (ifh) {
		case 1:
			rgb[0] = wv;
			rgb[1] = value;
			rgb[2] = whiteness;
			break;
		case 2:
			rgb[0] = whiteness;
			rgb[1] = value;
			rgb[2] = wv;
			break;
		case 3:
			rgb[0] = whiteness;
			rgb[1] = wv;
			rgb[2] = value;
			break;
		case 4:
			rgb[0] = wv;
			rgb[1] = whiteness;
			rgb[2] = value;
			break;
		case 5:
			rgb[0] = value;
			rgb[1] = whiteness;
			rgb[2] = wv;
			break;
		default:
			rgb[0] = value;
			rgb[1] = wv;
			rgb[2] = whiteness;
		}
	}

	@Override
	public String toString() {
		boolean nonOpaque = isNonOpaque();
		StringBuilder buf = new StringBuilder(20);
		buf.append("hwb(");
		appendHue(buf, hue);
		buf.append(' ').append(whiteness.getCssText())
				.append(' ').append(blackness.getCssText());
		if (nonOpaque) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toMinifiedString() {
		boolean nonOpaque = isNonOpaque();
		StringBuilder buf = new StringBuilder(20);
		buf.append("hwb(");
		appendMinifiedHue(buf, hue);
		buf.append(' ').append(whiteness.getMinifiedCssText("color"))
				.append(' ').append(blackness.getMinifiedCssText("color"));
		if (nonOpaque) {
			buf.append('/');
			appendAlphaChannelMinified(buf);
		}
		buf.append(')');
		return buf.toString();
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
	public ColorValue packInValue() {
		return new HWBColorValue(this);
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
