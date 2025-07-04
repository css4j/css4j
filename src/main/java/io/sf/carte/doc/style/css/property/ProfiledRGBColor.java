/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.util.Arrays;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMNotSupportedException;
import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.jclf.math.linear3.Matrices;

/**
 * An RGB color specified through the {@code color()} function.
 */
class ProfiledRGBColor extends RGBColor {

	private static final long serialVersionUID = 1L;

	private final String colorSpace;

	private final Space space;

	/**
	 * Construct a new profiled RGB color.
	 * 
	 * @param lcColorSpace an interned string with the name of the color space.
	 */
	ProfiledRGBColor(String lcColorSpace) {
		super();
		this.colorSpace = lcColorSpace;
		space = ColorSpaceHelper.rgbSpaceEnum(lcColorSpace);
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
	PrimitiveValue enforceColorComponentType(PrimitiveValue primi) {
		return ColorFunction.enforceColorComponentType(primi);
	}

	@Override
	double rgbComponentNormalized(TypedValue typed) {
		double comp;
		short unit = typed.getUnitType();
		if (unit == CSSUnit.CSS_PERCENTAGE) {
			comp = typed.getFloatValue() / 100d;
		} else if (unit == CSSUnit.CSS_NUMBER) {
			comp = typed.getFloatValue();
		} else if (typed.getPrimitiveType() == Type.IDENT) {
			comp = 0d;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Wrong component: " + typed.getCssText());
		}
		return comp;
	}

	@Override
	int getMaximumFractionDigits() {
		return 6;
	}

	@Override
	void setColorComponents(double[] rgb) {
		NumberValue red = new NumberValue();
		red.setFloatValue(CSSUnit.CSS_NUMBER, (float) rgb[0]);
		red.setSubproperty(true);
		red.setMaximumFractionDigits(getMaximumFractionDigits());
		red.setAbsolutizedUnit();
		setRed(red);

		NumberValue green = new NumberValue();
		green.setFloatValue(CSSUnit.CSS_NUMBER, (float) rgb[1]);
		green.setSubproperty(true);
		green.setMaximumFractionDigits(getMaximumFractionDigits());
		green.setAbsolutizedUnit();
		setGreen(green);

		NumberValue blue = new NumberValue();
		blue.setFloatValue(CSSUnit.CSS_NUMBER, (float) rgb[2]);
		blue.setSubproperty(true);
		blue.setMaximumFractionDigits(getMaximumFractionDigits());
		blue.setAbsolutizedUnit();
		setBlue(blue);
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}
	
		double r = rgbComponentNormalized((TypedValue) getRed());
		double g = rgbComponentNormalized((TypedValue) getGreen());
		double b = rgbComponentNormalized((TypedValue) getBlue());
	
		ColorProfile profile;
		double[] xyz = new double[3];

		switch (space) {
		case p3:
			profile = new DisplayP3ColorProfile();
			r = profile.linearComponent(r);
			g = profile.linearComponent(g);
			b = profile.linearComponent(b);
			profile.linearRgbToXYZ(r, g, b, xyz);
			if (white == Illuminant.D50) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
			break;
		case A98_RGB:
			profile = new A98RGBColorProfile();
			r = profile.linearComponent(r);
			g = profile.linearComponent(g);
			b = profile.linearComponent(b);
			profile.linearRgbToXYZ(r, g, b, xyz);
			if (white == Illuminant.D50) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
			break;
		case ProPhoto_RGB:
			profile = new ProPhotoRGBColorProfile();
			r = profile.linearComponent(r);
			g = profile.linearComponent(g);
			b = profile.linearComponent(b);
			profile.linearRgbToXYZ(r, g, b, xyz);
			if (white == Illuminant.D65) {
				xyz = ColorUtil.d50xyzToD65(xyz);
			}
			break;
		case Rec2020:
			profile = new Rec2020ColorProfile();
			r = profile.linearComponent(r);
			g = profile.linearComponent(g);
			b = profile.linearComponent(b);
			profile.linearRgbToXYZ(r, g, b, xyz);
			if (white == Illuminant.D50) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
			break;
		case Linear_sRGB:
			profile = new LinearSRGBColorProfile();
			profile.linearRgbToXYZ(r, g, b, xyz);
			if (white == Illuminant.D50) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
			break;
		case sRGB:
			return super.toXYZ(white);
		default:
			throw new DOMNotSupportedException("Color space is not supported.");
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
		double[] rgb = toNumberArray();

		ColorProfile profile = getColorProfile();
		double[] xyz = new double[3];

		profile.linearizeComponents(rgb);
		profile.linearRgbToXYZ(rgb, xyz);

		if (profile.getIlluminant() == Illuminant.D50) {
			if (!Arrays.equals(Illuminants.whiteD50, white)) {
				double[][] cam = new double[3][3];
				ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD50, white, cam);
				double[] result = new double[3];
				Matrices.multiplyByVector3(cam, xyz, result);
				xyz = result;
			}
		} else { // refWhite == Illuminant.D65
			if (!Arrays.equals(Illuminants.whiteD65, white)) {
				double[][] cam = new double[3][3];
				ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD65, white, cam);
				double[] result = new double[3];
				Matrices.multiplyByVector3(cam, xyz, result);
				xyz = result;
			}
		}

		return xyz;
	}

	private ColorProfile getColorProfile() {
		ColorProfile profile;
		switch (space) {
		case sRGB:
			profile = new SRGBColorProfile();
			break;
		case p3:
			profile = new DisplayP3ColorProfile();
			break;
		case A98_RGB:
			profile = new A98RGBColorProfile();
			break;
		case ProPhoto_RGB:
			profile = new ProPhotoRGBColorProfile();
			break;
		case Rec2020:
			profile = new Rec2020ColorProfile();
			break;
		case Linear_sRGB:
			profile = new LinearSRGBColorProfile();
			break;
		default:
			throw new DOMNotSupportedException("Color space is not supported.");
		}

		return profile;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		double[] xyz, xyzD50 = null;
		if (space == Space.sRGB) {
			return super.toSRGB(clamp);
		} else if (space == Space.ProPhoto_RGB) {
			// D50
			xyzD50 = toXYZ(Illuminant.D50);
			// Chromatic adjustment: D50 to D65
			xyz = ColorUtil.d50xyzToD65(xyzD50);
		} else if (space == Space.Linear_sRGB) {
			return linearToSRGB();
		} else {
			xyz = toXYZ(Illuminant.D65);
		}

		double[] rgb = new double[3];
		ColorUtil.d65xyzToSRGB(xyz, rgb);

		// range check
		if (!ColorUtil.rangeRoundCheck(rgb) && clamp) {
			if (space != Space.ProPhoto_RGB) {
				xyzD50 = ColorUtil.d65xyzToD50(xyz);
			}
			double[] lab = new double[3];
			ColorUtil.xyzD50ToLab(xyzD50, lab);
			ColorProfile profile = new SRGBColorProfile();
			ColorUtil.clampRGB(lab[0], lab[1], lab[2], profile, rgb);
		}

		return rgb;
	}

	private double[] linearToSRGB() {
		double r = rgbComponentNormalized((TypedValue) getRed());
		double g = rgbComponentNormalized((TypedValue) getGreen());
		double b = rgbComponentNormalized((TypedValue) getBlue());
		double[] rgb = new double[3];
		rgb[0] = ColorUtil.sRGBCompanding(r);
		rgb[1] = ColorUtil.sRGBCompanding(g);
		rgb[2] = ColorUtil.sRGBCompanding(b);
		return rgb;
	}

	RGBColor toSRGBColor(boolean clamp) {
		if (space == Space.sRGB) {
			return this;
		}

		// XYZ to RGB
		double[] rgb = toSRGB(clamp);

		// Set the RGBColor
		RGBColor color = new RGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(getAlpha().clone());

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
	public ColorValue packInValue() {
		return new ColorFunction(this);
	}

	@Override
	public ProfiledRGBColor clone() {
		return new ProfiledRGBColor(this);
	}

}
