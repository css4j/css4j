/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.HSLColor;

class HSLColorImpl extends BaseColor implements HSLColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue hue = null;
	private PrimitiveValue saturation = null;
	private PrimitiveValue lightness = null;

	HSLColorImpl() {
		super();
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.HSL;
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getHue();
		case 2:
			return getSaturation();
		case 3:
			return getLightness();
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
			setSaturation(component);
			break;
		case 3:
			setLightness(component);
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
	public PrimitiveValue getSaturation() {
		return saturation;
	}

	public void setSaturation(PrimitiveValue saturation) {
		this.saturation = enforcePcntOrNumberComponent(saturation);
	}

	@Override
	public PrimitiveValue getLightness() {
		return lightness;
	}

	public void setLightness(PrimitiveValue lightness) {
		this.lightness = enforcePcntOrNumberComponent(lightness);
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getSaturation()) && isConvertibleComponent(getHue())
				&& isConvertibleComponent(getLightness());
	}

	@Override
	void setColorComponents(double[] hsl) {
		NumberValue h = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, (float) (hsl[0]));
		h.setSubproperty(true);
		h.setAbsolutizedUnit();
		setHue(h);

		PercentageValue s = new PercentageValue();
		s.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) hsl[1]);
		s.setSubproperty(true);
		s.setAbsolutizedUnit();
		setSaturation(s);

		PercentageValue l = new PercentageValue();
		l.setFloatValue(CSSUnit.CSS_PERCENTAGE, (float) hsl[2]);
		l.setSubproperty(true);
		l.setAbsolutizedUnit();
		setLightness(l);
	}

	@Override
	double[] toArray() {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double[] hsl = new double[3];
		hsl[0] = ColorUtil.hueDegrees((CSSTypedValue) getHue());
		hsl[1] = ColorUtil.floatPercent((CSSTypedValue) getSaturation());
		hsl[2] = ColorUtil.floatPercent((CSSTypedValue) getLightness());
		return hsl;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double hue = ColorUtil.hueDegrees((CSSTypedValue) getHue()) / 360d;
		float sat = ColorUtil.fraction((CSSTypedValue) getSaturation());
		float light = ColorUtil.fraction((CSSTypedValue) getLightness());

		double[] rgb = new double[3];
		hslToSRGB(hue, sat, light, rgb);
		return rgb;
	}

	private static void hslToSRGB(double hue, float sat, float light, double[] rgb) {
		if (hue > 1d) {
			hue -= Math.floor(hue);
		} else if (hue < 0d) {
			hue = hue - Math.floor(hue) + 1d;
		}

		float m2;
		if (light <= 0.5f) {
			m2 = light * (sat + 1f);
		} else {
			m2 = light + sat - light * sat;
		}
		float m1 = light * 2f - m2;

		rgb[0] = hueToRgb(m1, m2, hue + 1d / 3d);
		rgb[1] = hueToRgb(m1, m2, hue);
		rgb[2] = hueToRgb(m1, m2, hue - 1d / 3d);
	}

	private static double hueToRgb(float m1, float m2, double h) {
		if (h < 0d) {
			h = h + 1d;
		} else if (h > 1d) {
			h = h - 1d;
		}
		if (h * 6d < 1d) {
			return m1 + (m2 - m1) * h * 6d;
		}
		if (h * 2d < 1d) {
			return m2;
		}
		if (h * 3d < 2d) {
			return m1 + (m2 - m1) * (2d / 3d - h) * 6d;
		}
		return m1;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	String toString(boolean commaSyntax) {
		boolean nonOpaque = isNonOpaque();
		if (commaSyntax) {
			return oldString(nonOpaque);
		} else {
			return newString(nonOpaque);
		}
	}

	private String oldString(boolean nonOpaque) {
		StringBuilder buf = new StringBuilder(22);
		if (nonOpaque) {
			buf.append("hsla(");
		} else {
			buf.append("hsl(");
		}
		appendHue(buf, hue);
		buf.append(", ").append(saturation.getCssText())
				.append(", ").append(lightness.getCssText());
		if (nonOpaque) {
			buf.append(", ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	private String newString(boolean nonOpaque) {
		StringBuilder buf = new StringBuilder(20);
		buf.append("hsl(");
		appendHue(buf, hue);
		buf.append(' ').append(saturation.getCssText())
				.append(' ').append(lightness.getCssText());
		if (nonOpaque) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toMinifiedString() {
		return toMinifiedString(true);
	}

	String toMinifiedString(boolean commaSyntax) {
		boolean nonOpaque = isNonOpaque();
		if (commaSyntax) {
			return oldMinifiedString(nonOpaque);
		} else {
			return minifiedString(nonOpaque);
		}
	}

	private String oldMinifiedString(boolean nonOpaque) {
		StringBuilder buf = new StringBuilder(21);
		if (nonOpaque) {
			buf.append("hsla(");
		} else {
			buf.append("hsl(");
		}
		appendMinifiedHue(buf, hue);
		buf.append(',').append(saturation.getMinifiedCssText("color"))
				.append(',').append(lightness.getMinifiedCssText("color"));
		if (nonOpaque) {
			buf.append(',');
			appendAlphaChannelMinified(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	private String minifiedString(boolean nonOpaque) {
		StringBuilder buf = new StringBuilder(20);
		buf.append("hsl(");
		appendMinifiedHue(buf, hue);
		buf.append(' ').append(saturation.getMinifiedCssText("color"))
				.append(' ').append(lightness.getMinifiedCssText("color"));
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
		result = prime * result + ((hue == null) ? 0 : hue.hashCode());
		result = prime * result + ((lightness == null) ? 0 : lightness.hashCode());
		result = prime * result + ((saturation == null) ? 0 : saturation.hashCode());
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
		HSLColorImpl other = (HSLColorImpl) obj;
		if (hue == null) {
			if (other.hue != null) {
				return false;
			}
		} else if (!hue.equals(other.hue)) {
			return false;
		}
		if (lightness == null) {
			if (other.lightness != null) {
				return false;
			}
		} else if (!lightness.equals(other.lightness)) {
			return false;
		}
		if (saturation == null) {
			if (other.saturation != null) {
				return false;
			}
		} else if (!saturation.equals(other.saturation)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public ColorValue packInValue() {
		return new HSLColorValue(this);
	}

	@Override
	public HSLColorImpl clone() {
		HSLColorImpl clon = new HSLColorImpl();
		clon.alpha = alpha.clone();
		if (hue != null) {
			clon.hue = hue.clone();
		}
		if (saturation != null) {
			clon.saturation = saturation.clone();
		}
		if (lightness != null) {
			clon.lightness = lightness.clone();
		}
		return clon;
	}

}
