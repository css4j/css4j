/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorValue.ColorModel;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.property.BaseColor.Space;
import io.sf.carte.doc.style.css.property.ColorProfile.Illuminant;

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
		if (ColorSpace.srgb.equals(colorSpace)) {
			result = source.toSRGB(true);
			if (createValue) {
				this.destColor = new RGBColor();
			}
		} else if (ColorSpace.display_p3.equals(colorSpace)) {
			profile = new DisplayP3ColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.display_p3);
			}
		} else if (ColorSpace.a98_rgb.equals(colorSpace)) {
			profile = new A98RGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.a98_rgb);
			}
		} else if (ColorSpace.prophoto_rgb.equals(colorSpace)) {
			profile = new ProPhotoRGBColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.prophoto_rgb);
			}
		} else if (ColorSpace.rec2020.equals(colorSpace)) {
			profile = new Rec2020ColorProfile();
			result = new double[3];
			convertToProfiled(profile, source, clamp, result);
			if (createValue) {
				this.destColor = new ProfiledRGBColor(ColorSpace.rec2020);
			}
		} else if (ColorSpace.xyz.equals(colorSpace) || "xyz-d65".equals(colorSpace)) {
			result = source.toXYZ(Illuminant.D65);
			if (createValue) {
				this.destColor = new XYZColorImpl(Illuminant.D65);
			}
		} else if (ColorSpace.xyz_d50.equals(colorSpace)) {
			result = source.toXYZ(Illuminant.D50);
			if (createValue) {
				this.destColor = new XYZColorImpl(Illuminant.D50);
			}
		} else if ("hsl".equals(colorSpace) || "hsla".equals(colorSpace)) {
			if (source.getColorModel() == ColorModel.HSL) {
				result = source.toNumberArray();
			} else {
				result = source.toSRGB(true);
				result = ColorUtil.srgbToHsl(result[0], result[1], result[2]);
			}
			if (createValue) {
				this.destColor = new HSLColorImpl();
			}
		} else if ("hwb".equals(colorSpace)) {
			if (source.getColorModel() == ColorModel.HWB) {
				result = source.toNumberArray();
			} else {
				double[] rgb = source.toSRGB(true);
				result = ColorUtil.srgbToHwb(rgb);
			}
			if (createValue) {
				this.destColor = new HWBColorImpl();
			}
		} else if (ColorSpace.cie_lab.equals(colorSpace)) {
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
		} else if (ColorSpace.cie_lch.equals(colorSpace)) {
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
		} else if (ColorSpace.ok_lab.equals(colorSpace)) {
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
		} else if (ColorSpace.ok_lch.equals(colorSpace)) {
			double[] lab;
			if (source.getSpace() == Space.OK_Lab) {
				lab = source.toNumberArray();
			} else {
				double[] xyz = source.toXYZ(Illuminant.D65);
				lab = new double[3];
				ColorUtil.xyzD65ToOkLab(xyz, lab);
			}
			// Lab to LCh
			result = new double[3];
			ColorUtil.labToLCh(lab, result);
			if (createValue) {
				this.destColor = new LCHColorImpl(Space.OK_LCh, ColorSpace.ok_lch);
			}
		} else {
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

		if (clamp && !ColorUtil.rangeRoundCheck(rgb)) {
			double[] lab = new double[3];
			if (profile.getIlluminant() == Illuminant.D65) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			}
			ColorUtil.xyzD50ToLab(xyz, lab);
			ColorUtil.labToClampedRGB(lab[0], lab[1], lab[2], true, profile, rgb);
		}
	}

}
