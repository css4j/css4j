/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.LABColor;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Lab color value.
 */
public class LABColorValue extends ColorValue implements io.sf.carte.doc.style.css.LABColorValue {

	private static final long serialVersionUID = 1L;

	private final LABColorImpl labColor;

	public LABColorValue() {
		super();
		labColor = new LABColorImpl();
	}

	LABColorValue(LABColorValue copied) {
		super(copied);
		this.labColor = copied.labColor.clone();
	}

	@Override
	public CSSColorValue.ColorModel getColorModel() {
		return CSSColorValue.ColorModel.LAB;
	}

	@Override
	void set(StyleValue value) {
		super.set(value);
		LABColorValue setfrom = (LABColorValue) value;
		this.labColor.setLightness(setfrom.labColor.getLightness());
		this.labColor.setA(setfrom.labColor.getA());
		this.labColor.setB(setfrom.labColor.getB());
		this.labColor.alpha = setfrom.labColor.alpha;
	}

	@Override
	public String getCssText() {
		return labColor.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyValue) {
		return labColor.toMinifiedString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		labColor.writeCssText(wri);
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		switch (index) {
		case 0:
			return labColor.alpha;
		case 1:
			return labColor.getLightness();
		case 2:
			return labColor.getA();
		case 3:
			return labColor.getB();
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		switch (index) {
		case 0:
			labColor.setAlpha((PrimitiveValue) component);
			break;
		case 1:
			labColor.setLightness((PrimitiveValue) component);
			break;
		case 2:
			labColor.setA((PrimitiveValue) component);
			break;
		case 3:
			labColor.setB((PrimitiveValue) component);
		}
	}

	@Override
	public RGBAColor toRGBColor() throws DOMException {
		return toRGBColor(true);
	}

	@Override
	public RGBAColor toRGBColor(boolean clamp) throws DOMException {
		// Convert to XYZ
		if (!isConvertibleComponent(labColor.getA()) || !isConvertibleComponent(labColor.getB())
				|| !isConvertibleComponent(labColor.getLightness())) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float light = ((CSSTypedValue) labColor.getLightness()).getFloatValue(CSSUnit.CSS_PERCENTAGE);
		float a = ((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER);
		float b = ((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER);
		//
		CSSRGBColor color = new CSSRGBColor();
		labToRGB(light, a, b, clamp, labColor.getAlpha(), color);
		return color;
	}

	static void labToRGB(float light, float a, float b, boolean clamp, PrimitiveValue alpha, CSSRGBColor color) {
		float fy = (light + 16f) / 116f;
		float fx = a / 500f + fy;
		float fz = fy - b / 200f;
		final float eps = 216f / 24389f;
		final float kappa = 24389f / 27f;
		float xr = fx * fx * fx;
		if (xr <= eps) {
			xr = (116f * fx - 16f) / kappa;
		}
		float zr = fz * fz * fz;
		if (zr <= eps) {
			zr = (116f * fz - 16f) / kappa;
		}
		float yr;
		if (light > kappa * eps) {
			yr = (light + 16f) / 116f;
			yr = yr * yr * yr;
		} else {
			yr = light / kappa;
		}
		// D50 reference white (from ASTM E308-01 via Lindbloom)
		float xwhite = 0.96422f;
		float zwhite = 0.82521f;
		//
		float x = xr * xwhite;
		float z = zr * zwhite;
		xyzToRGB(x, yr, z, clamp, alpha, color);
	}

	private static void xyzToRGB(float x, float y, float z, boolean clamp, PrimitiveValue alpha, CSSRGBColor color) {
		// Chromatic adjustment: D50 to D65, Bradford
		// See http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
		double xa = 0.9555766 * x + -0.0230393 * y + 0.0631636 * z;
		double ya = -0.0282895 * x + 1.0099416 * y + 0.0210077 * z;
		double za = 0.0122982 * x + -0.0204830 * y + 1.3299098 * z;
		// XYZ to RGB
		// See http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html for explanation
		// but the real figures are from:
		// https://github.com/w3c/csswg-drafts/issues/5922#issue-800549440
		float r = (float) (3.24096994190452 * xa - 1.53738317757 * ya - 0.498610760293 * za);
		float g = (float) (-0.96924363628088 * xa + 1.8759675015077 * ya + 0.04155505740718 * za);
		float b = (float) (0.055630079697 * xa - 0.20397695888898 * ya + 1.05697151424288 * za);
		//
		r = sRGBCompanding(r, clamp);
		g = sRGBCompanding(g, clamp);
		b = sRGBCompanding(b, clamp);
		//
		color.setAlpha(alpha.clone());
		NumberValue red = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, r * 100f);
		NumberValue green = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, g * 100f);
		NumberValue blue = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, b * 100f);
		red.setAbsolutizedUnit();
		green.setAbsolutizedUnit();
		blue.setAbsolutizedUnit();
		color.setRed(red);
		color.setGreen(green);
		color.setBlue(blue);
	}

	private static float sRGBCompanding(float linearComponent, boolean clamp) {
		// sRGB Companding
		// See http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html
		float nlComp;
		if (linearComponent <= 0.0031308f) {
			nlComp = 12.92f * linearComponent;
		} else {
			nlComp = 1.055f * (float) Math.pow(linearComponent, 1d/2.4d) - 0.055f;
		}
		// range check
		if (clamp) {
			if (nlComp < 0) {
				nlComp = 0f;
			} else if (nlComp > 1f) {
				nlComp = 1f;
			}
		} else if (nlComp < 0 || nlComp > 1f) {
			// Perhaps it is a rounding issue
			nlComp = Math.round(nlComp * 100f) * 0.01f;
		}
		return nlComp;
	}

	static void rgbToLab(float r, float g, float b, PrimitiveValue alpha, LABColorImpl lab) {
		r = inverseSRGBCompanding(r);
		g = inverseSRGBCompanding(g);
		b = inverseSRGBCompanding(b);
		//
		float[] xyz = rgbToXYZ(r, g, b);
		// XYZ to Lab
		// D50 reference white (from ASTM E308-01 via Lindbloom)
		float xwhite = 0.96422f;
		float zwhite = 0.82521f;
		xyz[0] /= xwhite;
		xyz[2] /= zwhite;
		//
		float fx = fxyz(xyz[0]);
		float fy = fxyz(xyz[1]);
		float fz = fxyz(xyz[2]);
		float labL = 116f * fy - 16f;
		float laba = 500f * (fx - fy);
		float labb = 200f * (fy - fz);
		//
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, labL);
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, laba);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, labb);
		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();
		lab.setLightness(primiL);
		lab.setA(primia);
		lab.setB(primib);
		lab.setAlpha(alpha.clone());
	}

	private static float inverseSRGBCompanding(float compandedComponent) {
		// Inverse sRGB Companding
		// See http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
		float linearComp;
		if (compandedComponent <= 0.04045f) {
			linearComp = compandedComponent / 12.92f;
		} else {
			linearComp = (float) Math.pow((compandedComponent + 0.055f) / 1.055f, 2.4d);
		}
		return linearComp;
	}

	private static float[] rgbToXYZ(float r, float g, float b) {
		// RGB to XYZ
		// https://github.com/w3c/csswg-drafts/issues/5922#issue-800549440
		double x = 0.41239079926595934 * r + 0.357584339383878 * g + 0.1804807884018343 * b;
		double y = 0.21263900587151027 * r + 0.715168678767756 * g + 0.07219231536073371 * b;
		double z = 0.01933081871559182 * r + 0.11919477979462598 * g + 0.9505321522496607 * b;
		//
		// Chromatic adjustment: D65 to D50, Bradford
		// See http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
		float[] xyz = new float[3];
		xyz[0] = (float) (1.0478112 * x + 0.0228866 * y - 0.0501270 * z);
		xyz[1] = (float) (0.0295424 * x + 0.9904844 * y - 0.0170491 * z);
		xyz[2] = (float) (-0.0092345 * x + 0.0150436 * y + 0.7521316 * z);
		return xyz;
	}

	private static float fxyz(float xyz) {
		final float eps = 216f / 24389f;
		final float kappa = 24389f / 27f;
		float f;
		if (xyz > eps) {
			f = (float) Math.pow(xyz, 1d/3d);
		} else {
			f = (kappa * xyz + 16f) / 116f;
		}
		return f;
	}

	@Override
	public LCHColorValue toLCHColorValue() throws DOMException {
		if (!isConvertibleComponent(labColor.getA()) || !isConvertibleComponent(labColor.getB())
				|| !isConvertibleComponent(labColor.getLightness())) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
		float a = ((CSSTypedValue) labColor.getA()).getFloatValue(CSSUnit.CSS_NUMBER);
		float b = ((CSSTypedValue) labColor.getB()).getFloatValue(CSSUnit.CSS_NUMBER);
		//
		float c = (float) Math.sqrt(a * a + b * b);
		float h = (float) (Math.atan2(b, a) * 180f / Math.PI);
		if (h < 0f) {
			h += 360f;
		}
		NumberValue chroma = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, c);
		NumberValue hue = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, h);
		chroma.setAbsolutizedUnit();
		hue.setAbsolutizedUnit();
		//
		LCHColorValue lch = new LCHColorValue();
		lch.setComponent(0, labColor.getAlpha().clone());
		lch.setComponent(1, labColor.getLightness().clone());
		lch.setComponent(2, chroma);
		lch.setComponent(3, hue);
		return lch;
	}

	@Override
	public LABColor getLABColor() {
		return labColor;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			try {
				if (lunit.getLexicalUnitType() == LexicalUnit.LexicalType.LABCOLOR) {
					setLexicalLAB(lunit);
				} else {
					throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "No lab() value: " + lunit.toString());
				}
			} catch (DOMException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
			}
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}

		private void setLexicalLAB(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			ValueFactory factory = new ValueFactory();
			// lightness
			PrimitiveValue primilight = factory.createCSSPrimitiveValue(lu, true);
			checkPcntCompValidity(primilight, lunit);
			// a
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primia = factory.createCSSPrimitiveValue(lu, true);
			checkNumberCompValidity(primia, lunit);
			// b
			lu = lu.getNextLexicalUnit();
			PrimitiveValue primib = factory.createCSSPrimitiveValue(lu, true);
			checkNumberCompValidity(primib, lunit);
			// slash or null
			lu = lu.getNextLexicalUnit();
			PrimitiveValue alpha = null;
			if (lu != null) {
				if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_SLASH) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected slash in: " + lunit.toString());
				}
				lu = lu.getNextLexicalUnit(); // Alpha
				alpha = factory.createCSSPrimitiveValue(lu, true);
				labColor.setAlpha(alpha);
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad value: " + lunit.toString());
				}
			}
			labColor.setLightness(primilight);
			labColor.setA(primia);
			labColor.setB(primib);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + labColor.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LABColorValue other = (LABColorValue) obj;
		return labColor.equals(other.labColor);
	}

	@Override
	public LABColorValue clone() {
		return new LABColorValue(this);
	}

}
