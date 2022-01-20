/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
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
		checkHueComponent(hue);
		this.hue = hue;
	}

	@Override
	public PrimitiveValue getSaturation() {
		return saturation;
	}

	public void setSaturation(PrimitiveValue saturation) {
		checkPcntComponent(saturation);
		this.saturation = saturation;
	}

	@Override
	public PrimitiveValue getLightness() {
		return lightness;
	}

	public void setLightness(PrimitiveValue lightness) {
		checkPcntComponent(lightness);
		this.lightness = lightness;
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getSaturation()) && isConvertibleComponent(getHue())
				&& isConvertibleComponent(getLightness());
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
