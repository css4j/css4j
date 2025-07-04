/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;
import io.sf.jclf.math.linear3.Matrices;

class LABColorImpl extends BaseColor implements LABColor {

	private static final long serialVersionUID = 2L;

	private final Space colorSpace;
	private final String strSpace;

	private PrimitiveValue lightness = null;
	private PrimitiveValue a = null;
	private PrimitiveValue b = null;

	LABColorImpl(Space colorSpace, String strSpace) {
		super();
		this.colorSpace = colorSpace;
		this.strSpace = strSpace;
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.LAB;
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

		LABColorImpl setfrom = (LABColorImpl) color;
		lightness = setfrom.getLightness();
		a = setfrom.getA();
		b = setfrom.getB();
	}

	@Override
	public NumberValue component(String component) {
		NumberValue ret;
		switch (component) {
		case "l":
			float pcntDiv;
			if (Space.OK_Lab.equals(colorSpace)) {
				pcntDiv = 100f;
			} else {
				pcntDiv = 1f;
			}
			ret = numberComponent((CSSTypedValue) getLightness(), pcntDiv);
			break;
		case "a":
			ret = numberComponent((CSSTypedValue) getA(), 1f);
			break;
		case "b":
			ret = numberComponent((CSSTypedValue) getB(), 1f);
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
			return getLightness();
		case 2:
			return getA();
		case 3:
			return getB();
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
			setA(component);
			break;
		case 3:
			setB(component);
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
		if (Space.OK_Lab.equals(colorSpace)) {
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
	public PrimitiveValue getA() {
		return a;
	}

	public void setA(PrimitiveValue a) {
		float factor;
		if (Space.OK_Lab.equals(colorSpace)) {
			factor = 0.004f;
		} else {
			factor = 1.25f;
		}
		this.a = normalizePcntToNumber(a, factor, 5, false);
	}

	@Override
	public PrimitiveValue getB() {
		return b;
	}

	public void setB(PrimitiveValue b) {
		float factor;
		if (Space.OK_Lab.equals(colorSpace)) {
			factor = 0.004f;
		} else {
			factor = 1.25f;
		}
		this.b = normalizePcntToNumber(b, factor, 5, false);
	}

	@Override
	boolean hasPercentageComponent() {
		return lightness != null && lightness.getUnitType() == CSSUnit.CSS_PERCENTAGE;
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getA()) && isConvertibleComponent(getB())
				&& isConvertibleComponent(getLightness());
	}

	@Override
	int getMaximumFractionDigits() {
		return 5;
	}

	@Override
	void setColorComponents(double[] lab) {
		NumberValue l = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[0]);
		l.setSubproperty(true);
		l.setAbsolutizedUnit();
		if (getSpace() == Space.OK_Lab) {
			l.setMaximumFractionDigits(6);
		} else {
			l.setMaximumFractionDigits(4);
		}
		setLightness(l);

		NumberValue a = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[1]);
		a.setSubproperty(true);
		a.setAbsolutizedUnit();
		a.setMaximumFractionDigits(getMaximumFractionDigits());
		setA(a);

		NumberValue b = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) lab[2]);
		b.setSubproperty(true);
		b.setAbsolutizedUnit();
		b.setMaximumFractionDigits(getMaximumFractionDigits());
		setB(b);
	}

	@Override
	public double[] toNumberArray() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double[] lab = new double[3];
		lab[0] = ColorUtil.floatNumber((CSSTypedValue) getLightness());
		lab[1] = ColorUtil.floatNumber((CSSTypedValue) getA());
		lab[2] = ColorUtil.floatNumber((CSSTypedValue) getB());
		return lab;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float light = ColorUtil.floatNumber((CSSTypedValue) getLightness());
		float a = ColorUtil.floatNumber((CSSTypedValue) getA());
		float b = ColorUtil.floatNumber((CSSTypedValue) getB());

		double[] rgb = new double[3];
		ColorProfile profile = new SRGBColorProfile();
		if (colorSpace == Space.OK_Lab) {
			ColorUtil.oklabToRGB(light, a, b, clamp, profile, rgb);
		} else {
			ColorUtil.labToClampedRGB(light, a, b, clamp, profile, rgb);
		}
		return rgb;
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		// Convert to XYZ
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		float light = ColorUtil.floatNumber((CSSTypedValue) getLightness());
		float a = ColorUtil.floatNumber((CSSTypedValue) getA());
		float b = ColorUtil.floatNumber((CSSTypedValue) getB());

		double[] xyz;

		if (colorSpace == Space.OK_Lab) {
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
		double[] lab = toNumberArray();

		double[] xyz;

		if (colorSpace == Space.OK_Lab) {
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
		if (colorSpace == Space.OK_Lab) {
			wri.write("oklab(");
		} else {
			wri.write("lab(");
		}
		lightness.writeCssText(wri);
		wri.write(' ');
		a.writeCssText(wri);
		wri.write(' ');
		b.writeCssText(wri);
		if (isNonOpaque()) {
			wri.write(" / ");
			appendAlphaChannel(wri);
		}
		wri.write(')');
	}

	@Override
	public String toMinifiedString() {
		StringBuilder buf = new StringBuilder(20);
		if (colorSpace == Space.OK_Lab) {
			buf.append("oklab(");
		} else {
			buf.append("lab(");
		}
		buf.append(lightness.getMinifiedCssText("color")).append(' ')
			.append(a.getMinifiedCssText("color")).append(' ')
			.append(b.getMinifiedCssText("color"));
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
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
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
		LABColorImpl other = (LABColorImpl) obj;
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
		if (a == null) {
			if (other.a != null) {
				return false;
			}
		} else if (!a.equals(other.a)) {
			return false;
		}
		if (b == null) {
			if (other.b != null) {
				return false;
			}
		} else if (!b.equals(other.b)) {
			return false;
		}
		return alpha.equals(other.alpha);
	}

	@Override
	public ColorValue packInValue() {
		if (colorSpace == Space.OK_Lab) {
			return new OKLABColorValue(this);
		}
		return new LABColorValue(this);
	}

	@Override
	public LABColorImpl clone() {
		LABColorImpl clon = new LABColorImpl(colorSpace, strSpace);
		clon.alpha = alpha.clone();
		if (lightness != null) {
			clon.lightness = lightness.clone();
		}
		if (a != null) {
			clon.a = a.clone();
		}
		if (b != null) {
			clon.b = b.clone();
		}
		return clon;
	}

}
