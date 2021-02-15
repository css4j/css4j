/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.property.ColorValue.CSSRGBColor;

class ColorUtil {

	static void labToRGB(float light, float a, float b, boolean clamp, PrimitiveValue alpha, CSSRGBColor color) {
		float[] rgb = new float[3];
		labToRGB(light, a, b, rgb);
		// range check
		if (!rangeRoundCheck(rgb) && clamp) {
			rgb = clampRGB(light, a, b, rgb);
		}
		//
		color.setAlpha(alpha.clone());
		NumberValue red = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[0] * 100f);
		NumberValue green = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[1] * 100f);
		NumberValue blue = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, rgb[2] * 100f);
		red.setAbsolutizedUnit();
		green.setAbsolutizedUnit();
		blue.setAbsolutizedUnit();
		color.setRed(red);
		color.setGreen(green);
		color.setBlue(blue);
	}

	private static void labToRGB(float light, float a, float b, float[] rgb) {
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
		xyzToRGB(x, yr, z, rgb);
	}

	/**
	 * A rough range-gamut check.
	 * 
	 * @param rgb the RGB to check.
	 * @return true if the RGB is valid within 0.01
	 */
	private static boolean rangeRoundCheck(float[] rgb) {
		boolean inRange = true;
		for (int i = 0; i < rgb.length; i++) {
			float comp = rgb[i];
			if (comp < 0f || comp > 1f) {
				// Perhaps it is a rounding issue
				comp = Math.round(comp * 100f) * 0.01f;
				if (comp >= 0 && comp <= 1f) {
					rgb[i] = comp;
				} else {
					inRange = false;
				}
			}
		}
		return inRange;
	}

	private static float[] clampRGB(float light, float a, float b, float[] rgb) {
		// Reduce chromaticity until clipped color is in range within deltaE2000 < 2
		final double h = Math.atan2(b, a);
		final float sinh = (float) Math.sin(h);
		final float cosh = (float) Math.cos(h);
		float current_a = a, current_b = b;
		// Sanity check (for very out-of-range values)
		if (Math.sqrt(a * a + b * b) > 400d) {
			final float upper_c = 400f;
			current_a = upper_c * cosh;
			current_b = upper_c * sinh;
			labToRGB(light, current_a, current_b, rgb);
		}
		// Now look for a clipped color that is close enough according to deltaE2000
		float[] rgbClamped = new float[3];
		float[] labClamped = new float[3];
		if (isInGamut(light, current_a, current_b, rgb, rgbClamped, labClamped)) {
			return rgbClamped;
		}
		// Initial guesstimate
		float c = (float) Math.sqrt(current_a * current_a + current_b * current_b) - labClamped[0];
		current_a = c * cosh;
		current_b = c * sinh;
		labToRGB(light, current_a, current_b, rgb);
		//
		float eps = 0.025f;
		float factor = 0.97f;
		// Refine the value, starting with a progressive reduction.
		// A classical bisection is avoided, as the gamut shape may lead to wrong results.
		do {
			rangeClamp(rgb, rgbClamped);
			rgbToLab(rgbClamped[0], rgbClamped[1], rgbClamped[2], labClamped);
			// Check deltaE2000
			c = (float) Math.sqrt(current_a * current_a + current_b * current_b);
			float dE = deltaE2000ChromaReduction(light, c, current_a, current_b, labClamped);
			if (dE < 2f) {
				if (factor < 1f) {
					if (eps < 9e-5) {
						return rgbClamped;
					}
					// Now drive chromaticity up
					eps *= 0.15f;
					factor = 1 + eps;
				}
			} else if (factor > 1f) {
				eps *= 0.15f;
				factor = 1 - eps;
			}
			// refine chromaticity with a factor, and compute new RGB
			c = c * factor;
			current_a = c * cosh;
			current_b = c * sinh;
			labToRGB(light, current_a, current_b, rgb);
		} while (true);
	}

	private static void rangeClamp(float[] rgb, float[] rgbClamped) {
		for (int i = 0; i < rgb.length; i++) {
			float comp = rgb[i];
			if (comp > 1f) {
				rgbClamped[i] = 1f;
			} else if (comp < 0f) {
				rgbClamped[i] = 0f;
			} else {
				rgbClamped[i] = rgb[i];
			}
		}
	}

	private static boolean isInGamut(float light, float current_a, float current_b, float[] rgb, float[] rgbClamped,
			float[] labClamped) {
		rangeClamp(rgb, rgbClamped);
		rgbToLab(rgbClamped[0], rgbClamped[1], rgbClamped[2], labClamped);
		// Check deltaE2000
		float c = (float) Math.sqrt(current_a * current_a + current_b * current_b);
		float dE = deltaE2000ChromaReduction(light, c, current_a, current_b, labClamped);
		return dE < 2f;
	}

	private static void xyzToRGB(float x, float y, float z, float[] rgb) {
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
		rgb[0] = sRGBCompanding(r);
		rgb[1] = sRGBCompanding(g);
		rgb[2] = sRGBCompanding(b);
	}

	private static float sRGBCompanding(float linearComponent) {
		// sRGB Companding
		// See http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html
		float nlComp;
		if (linearComponent <= 0.0031308f) {
			nlComp = 12.92f * linearComponent;
		} else {
			nlComp = 1.055f * (float) Math.pow(linearComponent, 1d/2.4d) - 0.055f;
		}
		return nlComp;
	}

	static void rgbToLab(float r, float g, float b, PrimitiveValue alpha, LABColorImpl labColor) {
		float[] lab = new float[3];
		rgbToLab(r, g, b, lab);
		//
		NumberValue primiL = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, lab[0]);
		NumberValue primia = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, lab[1]);
		NumberValue primib = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, lab[2]);
		primiL.setAbsolutizedUnit();
		primia.setAbsolutizedUnit();
		primib.setAbsolutizedUnit();
		labColor.setLightness(primiL);
		labColor.setA(primia);
		labColor.setB(primib);
		labColor.setAlpha(alpha.clone());
	}

	private static void rgbToLab(float r, float g, float b, float[] lab) {
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
		lab[0] = 116f * fy - 16f;
		lab[1] = 500f * (fx - fy);
		lab[2] = 200f * (fy - fz);
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
		// See http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
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

	static float deltaE2000LCh(float l1, float c1, float h1, float l2, float c2, float h2) {
		final double dL = l2 - l1;
		final double lav = (l1 + l2) * 0.5d;
		final double c_av = (c1 + c2) * 0.5d;
		// a and b
		final double a1 = c1 * Math.cos(h1);
		final double a2 = c2 * Math.cos(h2);
		final double b1 = c1 * Math.sin(h1);
		final double b2 = c2 * Math.sin(h2);
		//
		return deltaE2000(dL, lav, c_av, a1, b1, a2, b2);
	}

	static float deltaE2000Lab(float l1, float a1, float b1, float l2, float a2, float b2) {
		final double dL = l2 - l1;
		final double lav = (l1 + l2) * 0.5d;
		final double c1 = Math.sqrt(a1 * a1 + b1 * b1);
		final double c2 = Math.sqrt(a2 * a2 + b2 * b2);
		final double c_av = (c1 + c2) * 0.5d;
		//
		return deltaE2000(dL, lav, c_av, a1, b1, a2, b2);
	}

	// https://en.wikipedia.org/wiki/Color_difference#CIEDE2000
	// http://www2.ece.rochester.edu/~gsharma/ciede2000/ciede2000noteCRNA.pdf
	private static float deltaE2000(double dL, double lav, double c_av, double a1, double b1, double a2, double b2) {
		final double cav_pow7 = Math.pow(c_av, 7d);
		// 6103515625d = 25^7
		final double gplus1 = 1d + 0.5d * (1d - Math.sqrt(cav_pow7 / (cav_pow7 + 6103515625d)));
		final double a1prime = a1 * gplus1;
		final double a2prime = a2 * gplus1;
		final double c1prime = Math.sqrt(a1prime * a1prime + b1 * b1);
		final double c2prime = Math.sqrt(a2prime * a2prime + b2 * b2);
		final double deltaCprime = c2prime - c1prime;
		final double cprime_av = (c1prime + c2prime) * 0.5f;
		//
		final double TWOPI = Math.PI + Math.PI;
		double h1prime = Math.atan2(b1, a1prime);
		if (h1prime < 0d) {
			h1prime += TWOPI;
		}
		double h2prime = Math.atan2(b2, a2prime);
		if (h2prime < 0d) {
			h2prime += TWOPI;
		}
		//
		double hprime_av = 0d;
		double dhprime = h2prime - h1prime;
		if (Math.abs(h1prime - h2prime) > Math.PI) {
			if (h2prime <= h1prime) {
				dhprime += TWOPI;
			} else {
				dhprime -= TWOPI;
			}
			hprime_av = Math.PI;
		}
		hprime_av += (h1prime + h2prime) * 0.5f;
		final double dHprime = 2d * Math.sqrt(c1prime * c2prime) * Math.sin(dhprime * 0.5d);
		//
		final double t = 1d - 0.17d * Math.cos(hprime_av - 0.5235988d) + 0.24d * Math.cos(hprime_av + hprime_av)
				+ 0.32d * Math.cos(3d * hprime_av + 0.1047198d) - 0.2d * Math.cos(4d * hprime_av - 1.099557d);
		//
		double lav_minus50_sq = lav - 50d;
		lav_minus50_sq *= lav_minus50_sq;
		final double sL = 1d + 0.015d * lav_minus50_sq / Math.sqrt(lav_minus50_sq + 20d);
		final double sC = 1d + 0.045d * cprime_av;
		final double sH = 1d + 0.015d * cprime_av * t;
		//
		final double cprime_av_pow7 = Math.pow(cprime_av, 7d);
		final double exp_arg = (hprime_av - 4.799655443d) / 0.436332313d;
		final double rt = -2d * Math.sqrt(cprime_av_pow7 / (cprime_av_pow7 + 6103515625d))
				* Math.sin(1.04719755d * Math.exp(-exp_arg * exp_arg));
		//
		final double l_comp = dL / sL;
		final double c_comp = deltaCprime / sC;
		final double h_comp = dHprime / sH;
		final double hue_rotation = rt * c_comp * h_comp;
		final double dE00 = Math.sqrt(l_comp * l_comp + c_comp * c_comp + h_comp * h_comp + hue_rotation);
		return (float) dE00;
	}

	private static float deltaE2000ChromaReduction(double lav, double c1, double a1, double b1, float[] labClamped) {
		final double c2 = Math.sqrt(labClamped[1] * labClamped[1] + labClamped[2] * labClamped[2]);
		final double c_av = (c1 + c2) * 0.5d;
		final double cav_pow7 = Math.pow(c_av, 7d);
		// 6103515625d = 25^7
		final double gplus1 = 1d + 0.5d * (1d - Math.sqrt(cav_pow7 / (cav_pow7 + 6103515625d)));
		final double a1prime = a1 * gplus1;
		final double a2prime = labClamped[1] * gplus1;
		final double c1prime = Math.sqrt(a1prime * a1prime + b1 * b1);
		final double c2prime = Math.sqrt(a2prime * a2prime + labClamped[2] * labClamped[2]);
		final double deltaCprime = c2prime - c1prime;
		final double cprime_av = (c1prime + c2prime) * 0.5f;
		//
		final double TWOPI = Math.PI + Math.PI;
		double h1prime = Math.atan2(b1, a1prime);
		if (h1prime < 0d) {
			h1prime += TWOPI;
		}
		double h2prime = Math.atan2(labClamped[2], a2prime);
		if (h2prime < 0d) {
			h2prime += TWOPI;
		}
		//
		double hprime_av = 0d;
		double dhprime = h2prime - h1prime;
		if (Math.abs(h1prime - h2prime) > Math.PI) {
			if (h2prime <= h1prime) {
				dhprime += TWOPI;
			} else {
				dhprime -= TWOPI;
			}
			hprime_av = Math.PI;
		}
		hprime_av += (h1prime + h2prime) * 0.5f;
		final double dHprime = 2d * Math.sqrt(c1prime * c2prime) * Math.sin(dhprime * 0.5d);
		//
		final double t = 1d - 0.17d * Math.cos(hprime_av - 0.5235988d) + 0.24d * Math.cos(hprime_av + hprime_av)
				+ 0.32d * Math.cos(3d * hprime_av + 0.1047198d) - 0.2d * Math.cos(4d * hprime_av - 1.099557d);
		//
		final double sC = 1d + 0.045d * cprime_av;
		final double sH = 1d + 0.015d * cprime_av * t;
		//
		final double cprime_av_pow7 = Math.pow(cprime_av, 7d);
		final double exp_arg = (hprime_av - 4.799655443d) / 0.436332313d;
		final double rt = -2d * Math.sqrt(cprime_av_pow7 / (cprime_av_pow7 + 6103515625d))
				* Math.sin(1.04719755d * Math.exp(-exp_arg * exp_arg));
		//
		final double c_comp = deltaCprime / sC;
		final double h_comp = dHprime / sH;
		final double hue_rotation = rt * c_comp * h_comp;
		final double dE00 = Math.sqrt(c_comp * c_comp + h_comp * h_comp + hue_rotation);
		// To have a wild guess of the next chroma, put adjusted deltaCprime into labClamped[0]
		final double dE00minus2 = dE00 - 2d;
		double sqrtArg = dE00minus2 * dE00minus2 - h_comp * h_comp - hue_rotation;
		if (sqrtArg > 0d) {
			labClamped[0] = (float) (sC * Math.sqrt(sqrtArg));
		} else {
			labClamped[0] = 0f;
		}
		return (float) dE00;
	}

}
