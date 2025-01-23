/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.LCHColor;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;
import io.sf.jclf.math.linear3.Matrices;

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
		float factor;
		int maxDigits;
		boolean specified;
		if (Space.OK_LCh.equals(colorSpace)) {
			factor = 0.01f;
			maxDigits = 6;
			specified = false;
		} else {
			factor = 1f;
			maxDigits = 4;
			specified = true;
		}
		this.lightness = normalizePcntToNumber(lightness, factor, maxDigits, specified);
	}

	@Override
	public PrimitiveValue getChroma() {
		return chroma;
	}

	public void setChroma(PrimitiveValue chroma) {
		float factor;
		int maxDigits;
		if (Space.OK_LCh.equals(colorSpace)) {
			factor = 0.004f;
			maxDigits = 5;
		} else {
			factor = 1.5f;
			maxDigits = 4;
		}
		this.chroma = normalizePcntToNumber(chroma, factor, maxDigits, false);
	}

	@Override
	public PrimitiveValue getHue() {
		return hue;
	}

	public void setHue(PrimitiveValue hue) {
		this.hue = enforceHueComponent(hue);
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getChroma()) && isConvertibleComponent(getHue())
				&& isConvertibleComponent(getLightness());
	}

	@Override
	void setColorComponents(double[] lch) {
		NumberValue l = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lch[0]);
		l.setSubproperty(true);
		l.setAbsolutizedUnit();
		if (getSpace() == Space.OK_LCh) {
			l.setMaximumFractionDigits(6);
		} else {
			l.setMaximumFractionDigits(4);
		}
		setLightness(l);

		NumberValue c = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lch[1]);
		c.setSubproperty(true);
		c.setAbsolutizedUnit();
		if (getSpace() == Space.OK_LCh) {
			c.setMaximumFractionDigits(5);
		} else {
			c.setMaximumFractionDigits(4);
		}
		setChroma(c);

		float fhue = (float) lch[2];
		if (fhue < 0f) {
			fhue += 360f;
		}
		NumberValue h = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, fhue);
		h.setSubproperty(true);
		h.setAbsolutizedUnit();
		h.setMaximumFractionDigits(4);
		setHue(h);
	}

	@Override
	public double[] toNumberArray() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double[] lch = new double[3];
		lch[0] = ColorUtil.floatNumber((CSSTypedValue) getLightness());
		lch[1] = ColorUtil.floatNumber((CSSTypedValue) getChroma());
		lch[2] = ColorUtil.hueDegrees((CSSTypedValue) getHue());
		return lch;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		float c = ColorUtil.floatNumber((CSSTypedValue) getChroma());
		double h = ColorUtil.hueRadians((CSSTypedValue) getHue());

		double a = c * Math.cos(h);
		double b = c * Math.sin(h);

		float light = ColorUtil.floatNumber((CSSTypedValue) getLightness());

		double[] rgb = new double[3];
		ColorProfile profile = new SRGBColorProfile();
		if (colorSpace == Space.OK_LCh) {
			ColorUtil.oklabToRGB(light, a, b, clamp, profile, rgb);
		} else {
			ColorUtil.labToClampedRGB(light, a, b, clamp, profile, rgb);
		}
		return rgb;
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		CSSTypedValue primihue = (CSSTypedValue) getHue();
		float c = ColorUtil.floatNumber((CSSTypedValue) getChroma());
		double h = ColorUtil.hueRadians(primihue);

		float a = (float) (c * Math.cos(h));
		float b = (float) (c * Math.sin(h));
		float light = ColorUtil.floatNumber((CSSTypedValue) getLightness());

		double[] xyz;

		if (colorSpace == Space.OK_LCh) {
			xyz = ColorUtil.oklabToXyzD65(light, a, b);
			if (white == Illuminant.D50) {
				// Chromatic adjustment: D65 to D50
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
		} else {
			xyz = ColorUtil.labToXYZd50(light, a, b);
			if (white == Illuminant.D65) {
				// D50 to D65
				xyz = ColorUtil.d50xyzToD65(xyz);
			}
		}

		return xyz;
	}

	/**
	 * Convert this color to the XYZ space using the given reference white.
	 * 
	 * @param white the white point tristimulus value, normalized so the {@code Y}
	 *              component is always {@code 1}.
	 * @return the color expressed in XYZ coordinates with the given white point.
	 */
	@Override
	public double[] toXYZ(double[] white) {
		double[] lab = toLab();

		double[] xyz;

		if (colorSpace == Space.OK_LCh) {
			xyz = ColorUtil.oklabToXyzD65(lab[0], lab[1], lab[2]);
			if (!Arrays.equals(Illuminants.whiteD65, white)) {
				double[][] cam = new double[3][3];
				ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD65, white, cam);
				double[] result = new double[3];
				Matrices.multiplyByVector3(cam, xyz, result);
				xyz = result;
			}
		} else {
			xyz = ColorUtil.labToXYZd50(lab[0], lab[1], lab[2]);
			if (!Arrays.equals(Illuminants.whiteD50, white)) {
				double[][] cam = new double[3][3];
				ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD50, white, cam);
				double[] result = new double[3];
				Matrices.multiplyByVector3(cam, xyz, result);
				xyz = result;
			}
		}

		return xyz;
	}

	private double[] toLab() {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		CSSTypedValue primihue = (CSSTypedValue) getHue();
		float c = ColorUtil.floatNumber((CSSTypedValue) getChroma());
		double h = ColorUtil.hueRadians(primihue);

		double[] lab = new double[3];

		lab[1] = (float) (c * Math.cos(h));
		lab[2] = (float) (c * Math.sin(h));
		lab[0] = ColorUtil.floatNumber((CSSTypedValue) getLightness());

		return lab;
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
	public ColorValue packInValue() {
		if (Space.OK_LCh.equals(colorSpace)) {
			return new OKLCHColorValue(this);
		}
		return new LCHColorValue(this);
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
