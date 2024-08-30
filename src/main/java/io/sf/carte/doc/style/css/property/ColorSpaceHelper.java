/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.style.css.ColorSpace;
import io.sf.carte.doc.style.css.property.BaseColor.Space;

class ColorSpaceHelper {

	enum HueInterpolationMethod {
		SHORTER, LONGER, INCREASING, DECREASING, UNKNOWN;
	}

	/**
	 * Get the color space enum for a space in the RGB color model.
	 * 
	 * @param internedLcColorSpace an interned string with the canonical (lower
	 *                             case) name of the color space.
	 * @return the color space enum.
	 */
	static Space rgbSpaceEnum(String internedLcColorSpace) {
		Space space;
		if (internedLcColorSpace == ColorSpace.srgb) {
			space = Space.sRGB;
		} else if (internedLcColorSpace == ColorSpace.a98_rgb) {
			space = Space.A98_RGB;
		} else if (internedLcColorSpace == ColorSpace.display_p3) {
			space = Space.p3;
		} else if (internedLcColorSpace == ColorSpace.prophoto_rgb) {
			space = Space.ProPhoto_RGB;
		} else if (internedLcColorSpace == ColorSpace.rec2020) {
			space = Space.Rec2020;
		} else if (internedLcColorSpace == ColorSpace.srgb_linear) {
			space = Space.Linear_sRGB;
		} else {
			space = Space.OTHER;
		}
		return space;
	}

	static BaseColor createProfiledColor(String colorSpace) {
		BaseColor color;
		if (ColorSpace.srgb.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.srgb;
			color = new ProfiledRGBColor(colorSpace);
		} else if (ColorSpace.display_p3.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.display_p3;
			color = new ProfiledRGBColor(colorSpace);
		} else if (ColorSpace.a98_rgb.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.a98_rgb;
			color = new ProfiledRGBColor(colorSpace);
		} else if (ColorSpace.prophoto_rgb.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.prophoto_rgb;
			color = new ProfiledRGBColor(colorSpace);
		} else if (ColorSpace.rec2020.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.rec2020;
			color = new ProfiledRGBColor(colorSpace);
		} else if (ColorSpace.srgb_linear.equalsIgnoreCase(colorSpace)) {
			colorSpace = ColorSpace.srgb_linear;
			color = new ProfiledRGBColor(colorSpace);
		} else if (ColorSpace.xyz.equalsIgnoreCase(colorSpace)
				|| "xyz-d65".equalsIgnoreCase(colorSpace)) {
			color = new XYZColorImpl(Illuminant.D65);
		} else if (ColorSpace.xyz_d50.equalsIgnoreCase(colorSpace)) {
			color = new XYZColorImpl(Illuminant.D50);
		} else {
			color = null;
		}
		return color;
	}

	static HueInterpolationMethod parseInterpolationMethod(String s) {
		HueInterpolationMethod method;
		if ("shorter".equalsIgnoreCase(s)) {
			method = HueInterpolationMethod.SHORTER;
		} else if ("longer".equalsIgnoreCase(s)) {
			method = HueInterpolationMethod.LONGER;
		} else if ("increasing".equalsIgnoreCase(s)) {
			method = HueInterpolationMethod.INCREASING;
		} else if ("decreasing".equalsIgnoreCase(s)) {
			method = HueInterpolationMethod.DECREASING;
		} else {
			method = HueInterpolationMethod.UNKNOWN;
		}

		return method;
	}

}
