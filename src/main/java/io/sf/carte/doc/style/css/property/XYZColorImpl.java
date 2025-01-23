/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Arrays;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.XYZColor;
import io.sf.jclf.math.linear3.Matrices;

class XYZColorImpl extends BaseColor implements XYZColor {

	private static final long serialVersionUID = 1L;

	private PrimitiveValue x = null;
	private PrimitiveValue y = null;
	private PrimitiveValue z = null;

	private final Illuminant refWhite;

	XYZColorImpl(Illuminant white) {
		super();
		this.refWhite = white;
	}

	XYZColorImpl(XYZColorImpl copyMe) {
		super();
		refWhite = copyMe.refWhite;
		if (copyMe.x != null) {
			x = copyMe.x.clone();
		}
		if (copyMe.y != null) {
			y = copyMe.y.clone();
		}
		if (copyMe.z != null) {
			z = copyMe.z.clone();
		}
		alpha = copyMe.alpha.clone();
	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.XYZ;
	}

	@Override
	public String getColorSpace() {
		return refWhite == Illuminant.D50 ? ColorSpace.xyz_d50 : ColorSpace.xyz;
	}

	@Override
	Space getSpace() {
		return refWhite == Illuminant.D50 ? Space.CIE_XYZ_D50 : Space.CIE_XYZ;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		//
		XYZColorImpl xyzcolor = (XYZColorImpl) color;
		this.x = xyzcolor.x;
		this.y = xyzcolor.y;
		this.z = xyzcolor.z;
	}

	@Override
	public PrimitiveValue item(int index) {
		switch (index) {
		case 0:
			return alpha;
		case 1:
			return getX();
		case 2:
			return getY();
		case 3:
			return getZ();
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
			setX(component);
			break;
		case 2:
			setY(component);
			break;
		case 3:
			setZ(component);
		}
	}

	@Override
	public PrimitiveValue getX() {
		return x;
	}

	public void setX(PrimitiveValue x) {
		this.x = normalizeAxisComponent(x);
	}

	@Override
	public PrimitiveValue getY() {
		return y;
	}

	public void setY(PrimitiveValue y) {
		this.y = normalizeAxisComponent(y);
	}

	@Override
	public PrimitiveValue getZ() {
		return z;
	}

	public void setZ(PrimitiveValue z) {
		this.z = normalizeAxisComponent(z);
	}

	private PrimitiveValue normalizeAxisComponent(PrimitiveValue axis) {
		return normalizePcntToNumber(axis, 0.01f, 5, true);
	}

	@Override
	boolean hasConvertibleComponents() {
		return isConvertibleComponent(getX()) && isConvertibleComponent(getY())
				&& isConvertibleComponent(getZ());
	}

	@Override
	void setColorComponents(double[] xyz) {
		NumberValue x = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) xyz[0]);
		x.setSubproperty(true);
		x.setAbsolutizedUnit();
		x.setMaximumFractionDigits(5);
		setX(x);

		NumberValue y = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) xyz[1]);
		y.setSubproperty(true);
		y.setAbsolutizedUnit();
		y.setMaximumFractionDigits(5);
		setY(y);

		NumberValue z = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, (float) xyz[2]);
		z.setSubproperty(true);
		z.setAbsolutizedUnit();
		z.setMaximumFractionDigits(5);
		setZ(z);
	}

	@Override
	public double[] toNumberArray() throws DOMException {
		if (!hasConvertibleComponents()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Cannot convert.");
		}

		double[] xyz = new double[3];
		xyz[0] = ColorUtil.floatNumber((TypedValue) this.x);
		xyz[1] = ColorUtil.floatNumber((TypedValue) this.y);
		xyz[2] = ColorUtil.floatNumber((TypedValue) this.z);
		return xyz;
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		double[] xyz = toNumberArray();

		if (refWhite != white) {
			if (white == Illuminant.D50) {
				// D65 to D50
				xyz = ColorUtil.d65xyzToD50(xyz);
			} else {
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
		double[] xyz = toNumberArray();

		if (refWhite == Illuminant.D50) {
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

	@Override
	double[] toSRGB(boolean clamp) {
		double[] xyz = toNumberArray();

		double[] xyzD65;

		if (refWhite == Illuminant.D50) {
			// Chromatic adjustment: D50 to D65
			xyzD65 = ColorUtil.d50xyzToD65(xyz);
		} else {
			xyzD65 = xyz;
		}

		double[] rgb = new double[3];
		// XYZ to RGB
		ColorUtil.d65xyzToSRGB(xyzD65, rgb);

		// range check
		if (!ColorUtil.rangeRoundCheck(rgb) && clamp) {
			double[] lab = new double[3];
			if (refWhite == Illuminant.D65) {
				xyz = ColorUtil.d65xyzToD50(xyzD65);
			}
			ColorUtil.xyzD50ToLab(xyz, lab);
			ColorProfile profile = new SRGBColorProfile();
			ColorUtil.clampRGB(lab[0], lab[1], lab[2], profile, rgb);
		}
		return rgb;
	}

	RGBAColor toSRGBColor(boolean clamp) {
		double[] rgb = toSRGB(clamp);

		// Set the RGBColor
		RGBColor color = new RGBColor();
		color.setColorComponents(rgb);
		color.setAlpha(getAlpha().clone());

		return color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(refWhite, x, y, z);
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
		XYZColorImpl other = (XYZColorImpl) obj;
		return refWhite == other.refWhite && Objects.equals(x, other.x)
				&& Objects.equals(y, other.y) && Objects.equals(z, other.z);
	}

	@Override
	public ColorValue packInValue() {
		return new ColorFunction(this);
	}

	@Override
	public XYZColorImpl clone() {
		return new XYZColorImpl(this);
	}

}
