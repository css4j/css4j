/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.List;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.jclf.math.linear3.Matrices;

/**
 * An RGB color specified through the {@code color()} function.
 */
class ProfiledRGBColor extends RGBColor {

	private static final long serialVersionUID = 1L;

	private final String colorSpace;

	private final Space space;

	// ASTM E308-01 via http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
	private final double[] whiteD65 = { 0.95047d, 1d, 1.08883d };
	private final double[] whiteD50 = { 0.96422d, 1d, 0.82521d };

	ProfiledRGBColor(String colorSpace, List<PrimitiveValue> components) {
		super();
		this.colorSpace = colorSpace;
		space = spaceEnum(colorSpace);
		// Components
		PrimitiveValue comp = components.get(0);
		enforceColorComponentType(comp);
		setRed(comp);
		//
		try {
			comp = components.get(1);
			enforceColorComponentType(comp);
			setGreen(comp);
		} catch (IndexOutOfBoundsException e) {
			comp = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			NumberValue blue = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			setGreen(comp);
			setBlue(blue);
			return;
		}
		//
		try {
			comp = components.get(2);
			enforceColorComponentType(comp);
			setBlue(comp);
		} catch (IndexOutOfBoundsException e) {
			comp = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
			setBlue(comp);
		}
	}

	private Space spaceEnum(String colorSpace) {
		Space space;
		if (colorSpace == ColorSpace.srgb) {
			space = Space.sRGB;
		} else if (colorSpace == ColorSpace.a98_rgb) {
			space = Space.A98_RGB;
		} else if (colorSpace == ColorSpace.display_p3) {
			space = Space.p3;
		} else if (colorSpace == ColorSpace.prophoto_rgb) {
			space = Space.ProPhoto_RGB;
		} else if (colorSpace == ColorSpace.rec2020) {
			space = Space.Rec2020;
		} else {
			throw new IllegalStateException("This value only accepts RGB.");
		}
		return space;
	}

	ProfiledRGBColor(ProfiledRGBColor copyMe) {
		super(copyMe);
		colorSpace = copyMe.colorSpace;
		space = copyMe.space;
	}

	@Override
	public String getColorSpace() {
		return colorSpace;
	}

	@Override
	Space getSpace() {
		return space;
	}

	@Override
	void enforceColorComponentType(PrimitiveValue primi) {
		if (primi.getUnitType() != CSSUnit.CSS_NUMBER && primi.getUnitType() != CSSUnit.CSS_PERCENTAGE
				&& primi.getCssValueType() != CssType.PROXY && primi.getPrimitiveType() != Type.EXPRESSION) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Type not compatible with a color component: " + primi.getCssText());
		}
	}

	@Override
	double rgbComponentNormalized(TypedValue number) {
		double comp;
		if (number.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
			comp = number.getFloatValue(CSSUnit.CSS_PERCENTAGE) * 0.01d;
		} else {
			comp = number.getFloatValue(CSSUnit.CSS_NUMBER);
		}
		return comp;
	}

	@Override
	void rgbToLab(double r, double g, double b, PrimitiveValue alpha, LABColorImpl color) {
		if (space == Space.sRGB) {
			super.rgbToLab(r, g, b, alpha, color);
		} else {
			nonLinearRgbToLab(r, g, b, alpha, color);
		}
	}

	private void nonLinearRgbToLab(double r, double g, double b, PrimitiveValue alpha, LABColorImpl labColor)
			throws DOMException {
		double[] xyz = nonLinearRgbToXYZ(r, g, b);
		//
		float[] lab = new float[3];
		ColorUtil.xyzToLab(xyz, lab);
		setLabColor(lab, alpha, labColor);
	}

	double[] nonLinearRgbToXYZ(double r, double g, double b) throws DOMException {
		double[][] m;
		double[] xyz = new double[3];
		switch (space) {
		case p3:
			r = inverseSRGBCompanding(r);
			g = inverseSRGBCompanding(g);
			b = inverseSRGBCompanding(b);
			m = rgb2XYZmatrix(0.680f, 0.320f, 0.265f, 0.690f, 0.150f, 0.060f, whiteD65);
			rgb2XYZ(m, r, g, b, xyz);
			return chromaticAdjustXYZ(xyz);
		case A98_RGB:
			final float gamma = 563f / 256f;
			r = inverseGammaCompanding(r, gamma);
			g = inverseGammaCompanding(g, gamma);
			b = inverseGammaCompanding(b, gamma);
			m = rgb2XYZmatrix(0.6400f, 0.3300f, 0.2100f, 0.7100f, 0.1500f, 0.0600f, whiteD65);
			rgb2XYZ(m, r, g, b, xyz);
			return chromaticAdjustXYZ(xyz);
		case ProPhoto_RGB:
			r = toLinearComponentProphoto(r);
			g = toLinearComponentProphoto(g);
			b = toLinearComponentProphoto(b);
			m = rgb2XYZmatrix(0.734699f, 0.265301f, 0.159597f, 0.840403f, 0.036598f, 0.000105f, whiteD50);
			rgb2XYZ(m, r, g, b, xyz);
			// Chromatic adjustment not required
			return xyz;
		case Rec2020:
			r = toLinearComponentRec2020(r);
			g = toLinearComponentRec2020(g);
			b = toLinearComponentRec2020(b);
			m = rgb2XYZmatrix(0.708f, 0.292f, 0.170f, 0.797f, 0.131f, 0.046f, whiteD65);
			rgb2XYZ(m, r, g, b, xyz);
			return chromaticAdjustXYZ(xyz);
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Custom color profile is not supported.");
		}
	}

	private double inverseGammaCompanding(double c, double gamma) {
		return Math.pow(c, gamma);
	}

	private double toLinearComponentProphoto(double c) {
		final double eps = 16d/512d;
		final double abs = Math.abs(c);

		double cl;
		if (abs <= eps) {
		  cl =  c / 16d;
		} else {
		  cl = Math.signum(c) * Math.pow(c, 1.8d);
		}
		return cl;
	}

	private double toLinearComponentRec2020(double c) {
		final double alpha = 1.09929682680944d ;
		final double beta = 0.018053968510807d;

		final double abs = Math.abs(c);

		double cl;
		if (abs < beta * 4.5d ) {
		  cl = c / 4.5d;
		} else {
		  cl = Math.signum(c) * Math.pow((abs + alpha - 1d) / alpha, 1d/0.45d);
		}
		return cl;
	}

	/*
	 * See http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
	 */
	private double[][] rgb2XYZmatrix(float xr, float yr, float xg, float yg, float xb, float yb,
			double[] white) {
		double[][] a = new double[3][3];
		a[0][0] = xr / yr;
		a[1][0] = 1d;
		a[2][0] = (1d - xr - yr) / yr;
		a[0][1] = xg / yg;
		a[1][1] = 1d;
		a[2][1] = (1d - xg - yg) / yg;
		a[0][2] = xb / yb;
		a[1][2] = 1d;
		a[2][2] = (1d - xb - yb) / yb;
		//
		double[][] ainv = new double[3][3];
		Matrices.inverse3(a, ainv);
		double[] s = new double[3];
		Matrices.multiplyByVector3(ainv, white, s);
		//
		a[0][0] *= s[0];
		a[1][0] *= s[0];
		a[2][0] *= s[0];
		a[0][1] *= s[1];
		a[1][1] *= s[1];
		a[2][1] *= s[1];
		a[0][2] *= s[2];
		a[1][2] *= s[2];
		a[2][2] *= s[2];
		//
		return a;
	}

	private void rgb2XYZ(double[][] m, double r, double g, double b, double[] xyz) {
		// RGB to XYZ
		xyz[0] = m[0][0] * r + m[0][1] * g + m[0][2] * b;
		xyz[1] = m[1][0] * r + m[1][1] * g + m[1][2] * b;
		xyz[2] = m[2][0] * r + m[2][1] * g + m[2][2] * b;
	}

	private double[] chromaticAdjustXYZ(double[] xyz) {
		// Chromatic adjustment: D65 to D50, Bradford
		// See http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
		double[] xyzadj = new double[3];
		xyzadj[0] = 1.0478112 * xyz[0] + 0.0228866 * xyz[1] - 0.0501270 * xyz[2];
		xyzadj[1] = 0.0295424 * xyz[0] + 0.9904844 * xyz[1] - 0.0170491 * xyz[2];
		xyzadj[2] = -0.0092345 * xyz[0] + 0.0150436 * xyz[1] + 0.7521316 * xyz[2];
		return xyzadj;
	}

	RGBColor toSRGB(boolean clamp) {
		if (space == Space.sRGB) {
			return this;
		}
		//
		double r = rgbComponentNormalized((TypedValue) getRed());
		double g = rgbComponentNormalized((TypedValue) getGreen());
		double b = rgbComponentNormalized((TypedValue) getBlue());
		//
		double[][] m;
		double[] xyz = new double[3];
		switch (space) {
		case p3:
			r = inverseSRGBCompanding(r);
			g = inverseSRGBCompanding(g);
			b = inverseSRGBCompanding(b);
			m = rgb2XYZmatrix(0.680f, 0.320f, 0.265f, 0.690f, 0.150f, 0.060f, whiteD65);
			rgb2XYZ(m, r, g, b, xyz);
			xyz = chromaticAdjustXYZ(xyz);
			break;
		case A98_RGB:
			final double gamma = 563d / 256d;
			r = inverseGammaCompanding(r, gamma);
			g = inverseGammaCompanding(g, gamma);
			b = inverseGammaCompanding(b, gamma);
			m = rgb2XYZmatrix(0.6400f, 0.3300f, 0.2100f, 0.7100f, 0.1500f, 0.0600f, whiteD65);
			rgb2XYZ(m, r, g, b, xyz);
			xyz = chromaticAdjustXYZ(xyz);
			break;
		case ProPhoto_RGB:
			r = toLinearComponentProphoto(r);
			g = toLinearComponentProphoto(g);
			b = toLinearComponentProphoto(b);
			m = rgb2XYZmatrix(0.734699f, 0.265301f, 0.159597f, 0.840403f, 0.036598f, 0.000105f, whiteD50);
			rgb2XYZ(m, r, g, b, xyz);
			break;
		case Rec2020:
			r = toLinearComponentRec2020(r);
			g = toLinearComponentRec2020(g);
			b = toLinearComponentRec2020(b);
			m = rgb2XYZmatrix(0.708f, 0.292f, 0.170f, 0.797f, 0.131f, 0.046f, whiteD65);
			rgb2XYZ(m, r, g, b, xyz);
			xyz = chromaticAdjustXYZ(xyz);
			break;
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Color space is not supported.");
		}
		//
		float[] rgb = new float[3];
		ColorUtil.xyzToSRGB(xyz[0], xyz[1], xyz[2], rgb);
		// range check
		if (!ColorUtil.rangeRoundCheck(rgb) && clamp) {
			float[] lab = new float[3];
			ColorUtil.xyzToLab(xyz, lab);
			rgb = ColorUtil.clampRGB(lab[0], lab[1], lab[2], rgb);
		}
		// Set the RGBColor
		RGBColor color = new RGBColor();
		color.alpha = getAlpha().clone();
		NumberValue red = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[0] * 100f);
		NumberValue green = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[1] * 100f);
		NumberValue blue = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[2] * 100f);
		red.setAbsolutizedUnit();
		green.setAbsolutizedUnit();
		blue.setAbsolutizedUnit();
		color.setRed(red);
		color.setGreen(green);
		color.setBlue(blue);
		//
		return color;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		buf.append("color(").append(getColorSpace()).append(' ');
		appendComponentCssText(buf, getRed()).append(' ');
		appendComponentCssText(buf, getGreen()).append(' ');
		appendComponentCssText(buf, getBlue());
		if (isNonOpaque()) {
			buf.append(" / ");
			appendAlphaChannel(buf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String toMinifiedString() {
		StringBuilder buf = new StringBuilder(58);
		buf.append("color(").append(getColorSpace()).append(' ');
		appendComponentMinifiedCssText(buf, getRed()).append(' ');
		appendComponentMinifiedCssText(buf, getGreen()).append(' ');
		appendComponentMinifiedCssText(buf, getBlue());
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
		int result = super.hashCode();
		result = prime * result + Objects.hash(colorSpace);
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
		ProfiledRGBColor other = (ProfiledRGBColor) obj;
		return Objects.equals(colorSpace, other.colorSpace);
	}

	@Override
	public ProfiledRGBColor clone() {
		return new ProfiledRGBColor(this);
	}

}
