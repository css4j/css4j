/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Objects;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

class LCHColorImpl extends BaseColor implements LCHColor {

	private static final long serialVersionUID = 2L;

	private final Space colorSpace;
	private final String strSpace;

	private PrimitiveValue lightness = null;
	private PrimitiveValue chroma = null;
	private PrimitiveValue hue = null;

	LCHColorImpl(Space colorSpace, String strSpace) {
		super();
		this.colorSpace = colorSpace;
		this.strSpace = strSpace;
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.LCH;
	}

	@Override
	public String getColorSpace() {
		return strSpace;
	}

	@Override
	Space getSpace() {
		return colorSpace;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		//
		LCHColorImpl setfrom = (LCHColorImpl) color;
		setLightness(setfrom.getLightness());
		setChroma(setfrom.getChroma());
		setHue(setfrom.getHue());
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getLightness();
		case 2:
			return getChroma();
		case 3:
			return getHue();
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
			setLightness(component);
			break;
		case 2:
			setChroma(component);
			break;
		case 3:
			setHue(component);
		}
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
	public PrimitiveValue getChroma() {
		return chroma;
	}

	public void setChroma(PrimitiveValue chroma) {
		checkNumberComponent(chroma);
		this.chroma = chroma;
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
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getChroma()) && isConvertibleComponent(getHue())
				&& isConvertibleComponent(getLightness());
	}

	@Override
	public String toString() {
		BufferSimpleWriter wri = new BufferSimpleWriter();
		try {
			writeCssText(wri);
		} catch (IOException e) {
		}
		return wri.toString();
	}

	void writeCssText(SimpleWriter wri) throws IOException {
		if (colorSpace == Space.OK_LCh) {
			wri.write("oklch(");
		} else {
			wri.write("lch(");
		}
		lightness.writeCssText(wri);
		wri.write(' ');
		chroma.writeCssText(wri);
		wri.write(' ');
		writeHue(wri, hue);
		if (isNonOpaque()) {
			wri.write(" / ");
			appendAlphaChannel(wri);
		}
		wri.write(')');
	}

	@Override
	public String toMinifiedString() {
		StringBuilder buf = new StringBuilder(20);
		if (colorSpace == Space.OK_LCh) {
			buf.append("oklch(");
		} else {
			buf.append("lch(");
		}
		buf.append(lightness.getMinifiedCssText("color"));
		buf.append(' ').append(chroma.getMinifiedCssText("color")).append(' ');
		appendMinifiedHue(buf, hue);
		if (isNonOpaque()) {
			buf.append('/');
			appendAlphaChannelMinified(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = Objects.hash(getSpace());
		result = prime * result + ((lightness == null) ? 0 : lightness.hashCode());
		result = prime * result + ((chroma == null) ? 0 : chroma.hashCode());
		result = prime * result + ((hue == null) ? 0 : hue.hashCode());
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
		LCHColorImpl other = (LCHColorImpl) obj;
		if (colorSpace != other.colorSpace) {
			return false;
		}
		if (lightness == null) {
			if (other.lightness != null) {
				return false;
			}
		} else if (!lightness.equals(other.lightness)) {
			return false;
		}
		if (chroma == null) {
			if (other.chroma != null) {
				return false;
			}
		} else if (!chroma.equals(other.chroma)) {
			return false;
		}
		if (hue == null) {
			if (other.hue != null) {
				return false;
			}
		} else if (!hue.equals(other.hue)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public LCHColorImpl clone() {
		LCHColorImpl clon = new LCHColorImpl(colorSpace, strSpace);
		clon.alpha = alpha.clone();
		if (lightness != null) {
			clon.lightness = lightness.clone();
		}
		if (chroma != null) {
			clon.chroma = chroma.clone();
		}
		if (hue != null) {
			clon.hue = hue.clone();
		}
		return clon;
	}

}
