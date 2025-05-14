/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.property.BaseColor.Space;

class ColorConverter {

	private final boolean createValue;

	private BaseColor destColor;

	public ColorConverter(boolean createValue) {
		super();
		this.createValue = createValue;
	}

	/**
	 * Get the color created from the latest conversion, if {@code createValue} was
	 * true.
	 * 
	 * @return the color created from the latest conversion.
	 */
	public BaseColor getLastColor() {
		return destColor;
	}

	double[] toColorSpace(BaseColor source, String colorSpace, boolean clamp) throws DOMException {
		colorSpace = colorSpace.toLowerCase(Locale.ROOT);
		if (source.getColorSpace().equals(colorSpace)) {
			if (createValue) {
				this.destColor = source;
			}
			return source.toNumberArray();
		}

		double[] result;
		ColorProfile profile;
		switch (colorSpace) {
		case ColorSpace.srgb:
			result = source.toSRGB(clamp);
			if (createValue) {
				this.destColor = new RGBColor();
			}
			break;
		case ColorSpace.srgb_linear:
			profile = new LinearSRGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.srgb_linear);
			}
			break;
		case ColorSpace.display_p3:
			profile = new DisplayP3ColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.display_p3);
			}
			break;
		case ColorSpace.a98_rgb:
			profile = new A98RGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.a98_rgb);
			}
			break;
		case ColorSpace.prophoto_rgb:
			profile = new ProPhotoRGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.prophoto_rgb);
			}
			break;
		case ColorSpace.rec2020:
			profile = new Rec2020ColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.rec2020);
			}
			break;
		case ColorSpace.xyz:
		case "xyz-d65":
			result = source.toXYZ(Illuminant.D65);
			if (createValue) {
				this.destColor = new XYZColorImpl(Illuminant.D65);
			}
			break;
		case ColorSpace.xyz_d50:
			result = source.toXYZ(Illuminant.D50);
			if (createValue) {
				this.destColor = new XYZColorImpl(Illuminant.D50);
			}
			break;
		case "hsl":
		case "hsla":
			if (source.getColorModel() == ColorModel.HSL) {
				result = source.toNumberArray();
			} else {
				result = source.toSRGB(true);
				result = ColorUtil.srgbToHsl(result[0], result[1], result[2]);
			}
			if (createValue) {
				this.destColor = new HSLColorImpl();
			}
			break;
		case "hwb":
			if (source.getColorModel() == ColorModel.HWB) {
				result = source.toNumberArray();
			} else {
				double[] rgb = source.toSRGB(true);
				result = ColorUtil.srgbToHwb(rgb);
			}
			if (createValue) {
				this.destColor = new HWBColorImpl();
			}
			break;
		case ColorSpace.cie_lab:
			result = new double[3];
			if (source.getSpace() == Space.CIE_LCh) {
				// LCh to Lab
				double[] lch = source.toNumberArray();
				ColorUtil.lchToLab(lch, result);
			} else {
				double[] xyz = source.toXYZ(Illuminant.D50);
				ColorUtil.xyzD50ToLab(xyz, result);
			}
			if (createValue) {
				this.destColor = new LABColorImpl(Space.CIE_Lab, ColorSpace.cie_lab);
			}
			break;
		case ColorSpace.cie_lch:
			double[] lab;
			if (source.getSpace() == Space.CIE_Lab) {
				lab = source.toNumberArray();
			} else {
				double[] xyz = source.toXYZ(Illuminant.D50);
				lab = new double[3];
				ColorUtil.xyzD50ToLab(xyz, lab);
			}
			// Lab to LCh
			result = new double[3];
			ColorUtil.labToLCh(lab, result);
			if (createValue) {
				this.destColor = new LCHColorImpl(Space.CIE_LCh, ColorSpace.cie_lch);
			}
			break;
		case ColorSpace.ok_lab:
			result = new double[3];
			if (source.getSpace() == Space.OK_LCh) {
				// LCh to Lab
				double[] lch = source.toNumberArray();
				ColorUtil.lchToLab(lch, result);
			} else {
				double[] xyz = source.toXYZ(Illuminant.D65);
				ColorUtil.xyzD65ToOkLab(xyz, result);
			}
			if (createValue) {
				this.destColor = new LABColorImpl(Space.OK_Lab, ColorSpace.ok_lab);
			}
			break;
		case ColorSpace.ok_lch:
			double[] oklab;
			if (source.getSpace() == Space.OK_Lab) {
				oklab = source.toNumberArray();
			} else {
				double[] xyz = source.toXYZ(Illuminant.D65);
				oklab = new double[3];
				ColorUtil.xyzD65ToOkLab(xyz, oklab);
			}
			// Lab to LCh
			result = new double[3];
			ColorUtil.labToLCh(oklab, result);
			if (createValue) {
				this.destColor = new LCHColorImpl(Space.OK_LCh, ColorSpace.ok_lch);
			}
			break;
		case "--display-p3-linear":
			profile = new LinearDisplayP3ColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledColorImpl("--display-p3-linear", profile, result);
			}
			break;
		case "--a98-rgb-linear":
			profile = new LinearA98RGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledColorImpl("--a98-rgb-linear", profile, result);
			}
			break;
		case "--rec2020-linear":
			profile = new LinearRec2020ColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledColorImpl("--rec2020-linear", profile, result);
			}
			break;
		case "--prophoto-rgb-linear":
			profile = new LinearProPhotoRGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledColorImpl("--prophoto-rgb-linear", profile, result);
			}
			break;
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
					"Unsupported color space: " + colorSpace);
		}

		if (createValue) {
			this.destColor.setAlpha(source.getAlpha().clone());
			this.destColor.setColorComponents(result);
		}

		return result;
	}

	private void convertToProfiled(ColorProfile profile, BaseColor source, boolean clamp,
			double[] rgb) {
		double[] xyz = source.toXYZ(profile.getIlluminant());
		profile.xyzToLinearRgb(xyz, rgb);
		rgb[0] = profile.gammaCompanding(rgb[0]);
		rgb[1] = profile.gammaCompanding(rgb[1]);
		rgb[2] = profile.gammaCompanding(rgb[2]);

		if (!ColorUtil.rangeRoundCheck(rgb) && clamp) {
			double[] lab = new double[3];
			if (profile.getIlluminant() == Illuminant.D65) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
			ColorUtil.xyzD50ToLab(xyz, lab);
			ColorUtil.labToClampedRGB(lab[0], lab[1], lab[2], true, profile, rgb);
		}
	}

}
