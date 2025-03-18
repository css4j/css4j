/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.jclf.math.linear3.Matrices;

/**
 * Utility methods for color conversions.
 */
class ColorUtil {

	/**
	 * Given a Hue primitive value, return the hue in radians.
	 * 
	 * @param primihue the Hue primitive.
	 * @return the hue in radians.
	 */
	static double hueRadians(CSSTypedValue primihue) {
		float h;
		short unit = primihue.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			h = primihue.getFloatValue(CSSUnit.CSS_NUMBER);
			h = NumberValue.floatValueConversion(h, CSSUnit.CSS_DEG, CSSUnit.CSS_RAD);
		} else {
			try {
				h = primihue.getFloatValue(CSSUnit.CSS_RAD);
			} catch (DOMException e) {
				// Hue should never be 'none', but let's check
				if (primihue.getPrimitiveType() == Type.IDENT) {
					h = 0f;
				} else {
					throw e;
				}
			}
		}

		double dh = h;
		final double TWOPI = Math.PI + Math.PI;
		if (Math.abs(dh) > TWOPI) {
			dh = Math.IEEEremainder(dh, TWOPI);
		}
		if (dh < 0) {
			dh += TWOPI;
		}
		return dh;
	}

	/**
	 * Given a Hue primitive value, return the hue in degrees.
	 * 
	 * @param primihue the Hue primitive.
	 * @return the hue in degrees.
	 */
	static double hueDegrees(CSSTypedValue primihue) {
		float h;
		short unit = primihue.getUnitType();
		if (unit == CSSUnit.CSS_NUMBER) {
			h = primihue.getFloatValue(CSSUnit.CSS_NUMBER);
		} else {
			try {
				h = primihue.getFloatValue(CSSUnit.CSS_DEG);
			} catch (DOMException e) {
				// Hue should never be 'none', but let's check
				if (primihue.getPrimitiveType() == Type.IDENT) {
					h = 0f;
				} else {
					throw e;
				}
			}
		}

		double dh = h;
		if (Math.abs(dh) > 360d) {
			dh = Math.IEEEremainder(dh, 360d);
		}
		if (dh < 0) {
			dh += 360d;
		}
		return dh;
	}

	static double floatPercent(CSSTypedValue typed) {
		double pcnt;
		short unit = typed.getUnitType();
		if (unit == CSSUnit.CSS_PERCENTAGE) {
			pcnt = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE);
		} else if (unit == CSSUnit.CSS_NUMBER) {
			pcnt = typed.getFloatValue(CSSUnit.CSS_NUMBER);
		} else if (typed.getPrimitiveType() == Type.IDENT) {
			pcnt = 0d;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Wrong component: " + typed.getCssText());
		}
		return pcnt;
	}

	/**
	 * Return the given value as a fraction.
	 * <p>
	 * Plain numbers are assumed to be percentages at percent scale.
	 * </p>
	 * 
	 * @param typed a typed value, either a {@code <number>}, a {@code <percentage>}
	 *              or the {@code none} identifier.
	 * @return the value divided by 100.
	 */
	static float fraction(CSSTypedValue typed) {
		float pcnt;
		short unit = typed.getUnitType();
		if (unit == CSSUnit.CSS_PERCENTAGE) {
			pcnt = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE);
		} else if (unit == CSSUnit.CSS_NUMBER) {
			pcnt = typed.getFloatValue(CSSUnit.CSS_NUMBER);
		} else if (typed.getPrimitiveType() == Type.IDENT) {
			pcnt = 0f;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Wrong component: " + typed.getCssText());
		}
		return pcnt * 0.01f;
	}

	static float floatNumber(CSSTypedValue typed) {
		float value;
		if (typed.getUnitType() == CSSUnit.CSS_NUMBER) {
			value = typed.getFloatValue(CSSUnit.CSS_NUMBER);
		} else if (typed.getPrimitiveType() == Type.IDENT) {
			value = 0f;
		} else {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Wrong component: " + typed.getCssText());
		}
		return value;
	}

	static double[] srgbToHsl(double r, double g, double b) {
		double max;
		boolean maxr = false, maxg = false;
		if (g > r) {
			max = g;
			maxg = true;
		} else {
			max = r;
			maxr = true;
		}
		if (b > max) {
			max = b;
			maxr = false;
			maxg = false;
		}
		double min = Math.min(r, g);
		min = Math.min(min, b);
		double h;
		if (max == min) {
			h = 0d;
		} else if (maxr) {
			h = (g - b) / (max - min) * 60d + 360d;
			h = Math.IEEEremainder(h, 360d);
			if (h < 0d) {
				h += 360f;
			}
		} else if (maxg) {
			h = (b - r) / (max - min) * 60d + 120d;
			if (h < 0d) {
				h += 360d;
			}
		} else {
			h = (r - g) / (max - min) * 60d + 240d;
			if (h < 0d) {
				h += 360d;
			}
		}
		double l = (max + min) * 0.5d;

		double[] hsl = new double[3];
		if (max != min) {
			if (l <= 0.5f) {
				hsl[1] = Math.round((max - min) / l * 50000d) * 0.001d;
			} else {
				hsl[1] = Math.round((max - min) / (1d - l) * 50000d) * 0.001d;
			}
		} else {
			hsl[1] = 0d;
		}
		hsl[0] = Math.round(h * 1000d) * 0.001d;
		if (hsl[0] >= 360d) {
			hsl[0] -= 360d;
		}
		hsl[2] = Math.round(l * 100000d) * 0.001d;
		return hsl;
	}

	static double[] srgbToHwb(double[] rgb) {
		// Adapted from "HWB - A more intuitive hue-based color model"
		double w = Math.min(rgb[0], rgb[1]);
		w = Math.min(w, rgb[2]);
		double v = Math.max(rgb[0], rgb[1]);
		v = Math.max(v, rgb[2]);

		double[] hwb = new double[3];

		if (Math.abs(v - w) < 1e-5) {
			hwb[0] = 0d;
			hwb[1] = w;
			hwb[2] = rgb[2];
			return hwb;
		}

		double f;
		int i;
		if (rgb[0] == w) {
			f = rgb[1] - rgb[2];
			i = 3;
		} else if (rgb[1] == w) {
			f = rgb[2] - rgb[0];
			i = 5;
		} else {
			f = rgb[0] - rgb[1];
			i = 1;
		}

		hwb[0] = (i - f / (v - w)) * 60d;
		hwb[1] = w * 100d;
		hwb[2] = (1d - v) * 100d;

		return hwb;
	}

	static void labToLCh(double[] lab, double[] lch) {
		lch[0] = lab[0];
		lch[1] = Math.sqrt(lab[1] * lab[1] + lab[2] * lab[2]);
		lch[2] = Math.atan2(lab[2], lab[1]) * 180f / Math.PI;
		if (lch[2] < 0d) {
			lch[2] += 360d;
		}
	}

	static void lchToLab(double[] lch, double[] lab) {
		double hue = lch[2] * Math.PI / 180d;
		lab[0] = lch[0];
		lab[1] = lch[1] * Math.cos(hue);
		lab[2] = lch[1] * Math.sin(hue);
	}

	static void labToClampedRGB(double light, double a, double b, boolean clamp,
			ColorProfile profile, double[] rgb) {
		labToRGB(light, a, b, profile, rgb);
		// range check
		if (!rangeRoundCheck(rgb) && clamp) {
			clampRGB(light, a, b, profile, rgb);
		}
	}

	static double[] labToXYZd50(double light, double a, double b) {
		double fy = (light + 16d) / 116d;
		double fx = a / 500d + fy;
		double fz = fy - b / 200d;
		final double eps = 216d / 24389d;
		final double kappa = 24389d / 27d;
		double xr = fx * fx * fx;
		if (xr <= eps) {
			xr = (116d * fx - 16d) / kappa;
		}
		double zr = fz * fz * fz;
		if (zr <= eps) {
			zr = (116d * fz - 16d) / kappa;
		}
		double yr;
		if (light > kappa * eps) {
			yr = (light + 16d) / 116d;
			yr = yr * yr * yr;
		} else {
			yr = light / kappa;
		}
		// D50 reference white (from ASTM E308-01 via Lindbloom)
		double xwhite = 0.96422d;
		double zwhite = 0.82521d;

		double x = xr * xwhite;
		double z = zr * zwhite;

		double[] d50xyz = new double[3];
		d50xyz[0] = x;
		d50xyz[1] = yr;
		d50xyz[2] = z;
		return d50xyz;
	}

	private static void labToRGB(double light, double a, double b, ColorProfile profile,
			double[] rgb) {
		double[] xyz = labToXYZd50(light, a, b);

		if (profile.getIlluminant() == Illuminant.D65) {
			// Chromatic adjustment: D50 to D65
			xyz = d50xyzToD65(xyz);
		}

		// XYZ to RGB
		profile.xyzToRgb(xyz, rgb);
	}

	static void oklabToRGB(double light, double a, double b, boolean clamp,
			ColorProfile profile, double[] rgb) {
		double[] xyz = oklabToXyzD65(light, a, b);

		if (profile.getIlluminant() != Illuminant.D65) {
			// Chromatic adjustment: D65 to D50
			xyz = d65xyzToD50(xyz);
		}

		profile.xyzToRgb(xyz, rgb);

		// range check
		if (!rangeRoundCheck(rgb) && clamp) {
			okClampRGB(light, a, b, profile, rgb);
		}
	}

	static void oklabToLab(double light, double a, double b, double[] lab) {
		double[] xyz65 = oklabToXyzD65(light, a, b);

		// Chromatic adjustment: D65 to D50
		double[] xyz = d65xyzToD50(xyz65);
		xyzD50ToLab(xyz, lab);
	}

	static double[] oklabToXyzD65(double light, double a, double b) {
		double l_p = light + 0.3963377774d * a + 0.2158037573d * b;
		double m_p = light - 0.1055613458d * a - 0.0638541728d * b;
		double s_p = light - 0.0894841775d * a - 1.2914855480d * b;
		double xr = l_p * l_p * l_p;
		double yr = m_p * m_p * m_p;
		double zr = s_p * s_p * s_p;

		double[][] m1inv = { { 1.2270138511035211d, -0.5577999806518222d, 0.28125614896646783d },
				{ -0.04058017842328059d, 1.11225686961683d, -0.07167667866560119d },
				{ -0.0763812845057069d, -0.4214819784180127d, 1.586163220440795d } };
		double[] xyz65 = new double[3];
		Matrices.multiplyByVector3(m1inv, xr, yr, zr, xyz65);
		return xyz65;
	}

	static void xyzD65ToOkLab(double[] xyz, double[] oklab) {
		// XYZ65 to LMS
		// https://bottosson.github.io/posts/oklab/
		double[][] m1 = { { 0.8189330101, 0.3618667424, -0.1288597137 },
				{ 0.0329845436, 0.9293118715, 0.0361456387 },
				{ 0.0482003018, 0.2643662691, 0.6338517070d } };
		double[] lms = new double[3];
		Matrices.multiplyByVector3(m1, xyz, lms);
		// Non-linearity
		final double onethird = 1d / 3d;
		lms[0] = Math.signum(lms[0]) * Math.pow(Math.abs(lms[0]), onethird);
		lms[1] = Math.signum(lms[1]) * Math.pow(Math.abs(lms[1]), onethird);
		lms[2] = Math.signum(lms[2]) * Math.pow(Math.abs(lms[2]), onethird);

		double[][] m2 = { { 0.2104542553d, 0.7936177850d, -0.0040720468d },
				{ 1.9779984951d, -2.4285922050, 0.4505937099d },
				{ 0.0259040371d, 0.7827717662d, -0.8086757660d } };
		Matrices.multiplyByVector3(m2, lms, oklab);
	}

	/**
	 * A rough range-gamut check.
	 * <p>
	 * If the color is very close to gamut, it is adjusted.
	 * </p>
	 * 
	 * @param rgb the RGB to check.
	 * @return true if the RGB is in gamut within 0.0001
	 */
	static boolean rangeRoundCheck(double[] rgb) {
		boolean inRange = true;
		for (int i = 0; i < rgb.length; i++) {
			double comp = rgb[i];
			if (comp < 0d) {
				// Perhaps it is a rounding issue
				if (comp > -1e-4) {
					rgb[i] = 0d;
				} else {
					inRange = false;
				}
			} else if (comp > 1d) {
				if (comp < 1.0001d) {
					rgb[i] = 1d;
				} else {
					inRange = false;
				}
			}
		}
		return inRange;
	}

	static void clampRGB(double light, double a, double b, ColorProfile profile, double[] rgb) {
		// Reduce chromaticity until clipped color is in range within deltaE2000 < 2
		final double h = Math.atan2(b, a);
		final double sinh = Math.sin(h);
		final double cosh = Math.cos(h);
		double current_a = a, current_b = b;

		// Sanity check (for very out-of-range values)
		if (Math.sqrt(a * a + b * b) > 400d) {
			final double upper_c = 400d;
			current_a = upper_c * cosh;
			current_b = upper_c * sinh;
			labToRGB(light, current_a, current_b, profile, rgb);
		}

		// Now look for a clipped color that is close enough according to deltaE2000
		double[] rgbClamped = new double[3];
		double[] labClamped = new double[3];
		if (isInGamut(light, current_a, current_b, rgb, profile, rgbClamped, labClamped)) {
			System.arraycopy(rgbClamped, 0, rgb, 0, rgb.length);
			return;
		}

		// Initial guesstimate
		double c = Math.sqrt(current_a * current_a + current_b * current_b) - labClamped[0];
		current_a = c * cosh;
		current_b = c * sinh;
		labToRGB(light, current_a, current_b, profile, rgb);

		double eps = 0.025d;
		double factor = 0.97d;
		// Refine the value, starting with a progressive reduction.
		// A classical bisection is avoided, as the gamut shape may lead to wrong results.
		do {
			rangeClamp(rgb, rgbClamped);
			rgbToLab(rgbClamped[0], rgbClamped[1], rgbClamped[2], profile, labClamped);
			// Check deltaE2000
			c = Math.sqrt(current_a * current_a + current_b * current_b);
			double dE = deltaE2000ChromaReduction(light, c, current_a, current_b, labClamped);
			if (dE < 2d) {
				if (factor < 1d) {
					if (eps < 9e-5) {
						System.arraycopy(rgbClamped, 0, rgb, 0, rgb.length);
						return;
					}
					// Now drive chromaticity up
					eps *= 0.15d;
					factor = 1d + eps;
				}
			} else if (factor > 1d) {
				eps *= 0.15d;
				factor = 1d - eps;
			}
			// refine chromaticity with a factor, and compute new RGB
			c = c * factor;
			current_a = c * cosh;
			current_b = c * sinh;
			labToRGB(light, current_a, current_b, profile, rgb);
		} while (true);
	}

	private static void okClampRGB(double light, double a, double b, ColorProfile profile,
			double[] rgb) {
		// Reduce chromaticity until clipped color is in range within deltaE2000 < 2

		oklabToLab(light, a, b, rgb);
		light = rgb[0];
		double current_a = rgb[1];
		double current_b = rgb[2];
		final double h = Math.atan2(current_b, current_a);
		final double sinh = Math.sin(h);
		final double cosh = Math.cos(h);
		// Sanity check (for very out-of-range values)
		if (Math.sqrt(current_a * current_a + current_b * current_b) > 400d) {
			final double upper_c = 400d;
			current_a = upper_c * cosh;
			current_b = upper_c * sinh;
			oklabToRGB(light, current_a, current_b, false, profile, rgb);
		}
		// Now look for a clipped color that is close enough according to deltaE2000
		double[] rgbClamped = new double[3];
		double[] labClamped = new double[3];
		if (isInGamut(light, current_a, current_b, rgb, profile, rgbClamped, labClamped)) {
			System.arraycopy(rgbClamped, 0, rgb, 0, rgb.length);
			return;
		}
		// Initial guesstimate
		double c = Math.sqrt(current_a * current_a + current_b * current_b) - labClamped[0];
		current_a = c * cosh;
		current_b = c * sinh;
		oklabToRGB(light, current_a, current_b, false, profile, rgb);

		float eps = 0.025f;
		float factor = 0.97f;
		// Refine the value, starting with a progressive reduction.
		// A classical bisection is avoided, as the gamut shape may lead to wrong results.
		do {
			rangeClamp(rgb, rgbClamped);
			rgbToLab(rgbClamped[0], rgbClamped[1], rgbClamped[2], profile, labClamped);
			// Check deltaE2000
			c = Math.sqrt(current_a * current_a + current_b * current_b);
			double dE = deltaE2000ChromaReduction(light, c, current_a, current_b, labClamped);
			if (dE < 2d) {
				if (factor < 1f) {
					if (eps < 9e-5) {
						System.arraycopy(rgbClamped, 0, rgb, 0, rgb.length);
						return;
					}
					// Now drive chromaticity up
					eps *= 0.15f;
					factor = 1f + eps;
				}
			} else if (factor > 1f) {
				eps *= 0.15f;
				factor = 1f - eps;
			}
			// refine chromaticity with a factor, and compute new RGB
			c = c * factor;
			current_a = c * cosh;
			current_b = c * sinh;
			labToRGB(light, current_a, current_b, profile, rgb);
		} while (true);
	}

	private static void rangeClamp(double[] rgb, double[] rgbClamped) {
		for (int i = 0; i < rgb.length; i++) {
			double comp = rgb[i];
			if (comp > 1d) {
				rgbClamped[i] = 1d;
			} else if (comp < 0d) {
				rgbClamped[i] = 0d;
			} else {
				rgbClamped[i] = rgb[i];
			}
		}
	}

	private static boolean isInGamut(double light, double current_a, double current_b, double[] rgb,
			ColorProfile profile, double[] rgbClamped, double[] labClamped) {
		rangeClamp(rgb, rgbClamped);
		rgbToLab(rgbClamped[0], rgbClamped[1], rgbClamped[2], profile, labClamped);
		// Check deltaE2000
		double c = Math.sqrt(current_a * current_a + current_b * current_b);
		double dE = deltaE2000ChromaReduction(light, c, current_a, current_b, labClamped);
		return dE < 2d;
	}

	static double[] d65xyzToD50(double[] xyz) {
		// Chromatic adjustment: D65 to D50, Bradford
		// See http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
		double[] xyzadj = new double[3];
		xyzadj[0] = 1.0478112436606313d * xyz[0] + 0.022886602481693052d * xyz[1]
				- 0.05012697596852886d * xyz[2];
		xyzadj[1] = 0.029542398290574905d * xyz[0] + 0.9904844034904394d * xyz[1]
				- 0.017049095628961564d * xyz[2];
		xyzadj[2] = -0.009234489723309473d * xyz[0] + 0.015043616793498756d * xyz[1]
				+ 0.7521316354746059d * xyz[2];
		return xyzadj;
	}

	static double[] d50xyzToD65(double[] xyzD50) {
		// Chromatic adjustment: D50 to D65, Bradford
		// See http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
		double[] xyzD65 = new double[3];
		xyzD65[0] = 0.955576615033105d * xyzD50[0] + -0.02303934471607876d * xyzD50[1]
				+ 0.06316363224980126d * xyzD50[2];
		xyzD65[1] = -0.028289544243554895d * xyzD50[0] + 1.0099416173711144d * xyzD50[1]
				+ 0.021007654996190325d * xyzD50[2];
		xyzD65[2] = 0.012298165717207273d * xyzD50[0] + -0.020483025232449423d * xyzD50[1]
				+ 1.329909826449757d * xyzD50[2];
		return xyzD65;
	}

	static void d65xyzToSRGB(double[] xyzD65, double[] rgb) {
		// XYZ to RGB
		// See http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html for explanation
		// but the real figures are from:
		// https://github.com/w3c/csswg-drafts/issues/5922#issue-800549440
		double r = 3.24096994190452 * xyzD65[0] - 1.53738317757 * xyzD65[1] - 0.498610760293 * xyzD65[2];
		double g = -0.96924363628088 * xyzD65[0] + 1.8759675015077 * xyzD65[1] + 0.04155505740718 * xyzD65[2];
		double b = 0.055630079697 * xyzD65[0] - 0.20397695888898 * xyzD65[1] + 1.05697151424288 * xyzD65[2];

		rgb[0] = sRGBCompanding(r);
		rgb[1] = sRGBCompanding(g);
		rgb[2] = sRGBCompanding(b);
	}

	static double sRGBCompanding(double linearComponent) {
		// sRGB Companding
		final double abs = Math.abs(linearComponent);
		double nlComp;
		if (abs <= 0.0031308d) {
			nlComp = 12.92d * linearComponent;
		} else {
			nlComp = 1.055d * Math.signum(linearComponent) * Math.pow(abs, 1d / 2.4d) - 0.055d;
		}
		return nlComp;
	}

	static double[] srgbToXYZd65(double r, double g, double b) {
		r = RGBColor.inverseSRGBCompanding(r);
		g = RGBColor.inverseSRGBCompanding(g);
		b = RGBColor.inverseSRGBCompanding(b);

		return linearSRGBToXYZd65(r, g, b);
	}

	static void rgbToLab(double r, double g, double b, ColorProfile profile, double[] lab) {
		r = profile.linearComponent(r);
		g = profile.linearComponent(g);
		b = profile.linearComponent(b);

		double[] xyz = new double[3];
		profile.linearRgbToXYZ(r, g, b, xyz);
		if (profile.getIlluminant() != Illuminant.D50) {
			// Chromatic adjustment: D65 to D50
			xyz = d65xyzToD50(xyz);
		}
		xyzD50ToLab(xyz, lab);
	}

	static double[] linearSRGBToXYZd65(double r, double g, double b) {
		// RGB to XYZ
		// https://github.com/w3c/csswg-drafts/issues/5922#issue-800549440
		double[] xyzD65 = new double[3];
		xyzD65[0] = 0.41239079926595934 * r + 0.357584339383878 * g + 0.1804807884018343 * b;
		xyzD65[1] = 0.21263900587151027 * r + 0.715168678767756 * g + 0.07219231536073371 * b;
		xyzD65[2] = 0.01933081871559182 * r + 0.11919477979462598 * g + 0.9505321522496607 * b;
		return xyzD65;
	}

	static void xyzD50ToLab(double[] xyz, double[] lab) {
		// XYZ to Lab
		// D50 reference white (from ASTM E308-01 via Lindbloom)
		final double xwhite = Illuminants.whiteD50[0];
		final double zwhite = Illuminants.whiteD50[2];
		xyz[0] /= xwhite;
		xyz[2] /= zwhite;

		double fx = fxyz(xyz[0]);
		double fy = fxyz(xyz[1]);
		double fz = fxyz(xyz[2]);
		lab[0] = 116d * fy - 16d;
		lab[1] = 500d * (fx - fy);
		lab[2] = 200d * (fy - fz);
	}

	private static double fxyz(double xyz) {
		final double eps = 216d / 24389d;
		final double kappa = 24389d / 27d;
		double f;
		if (xyz > eps) {
			f = Math.pow(xyz, 1d / 3d);
		} else {
			f = (kappa * xyz + 16d) / 116d;
		}
		return f;
	}

	static float deltaE2000LCh(float l1, float c1, double h1, float l2, float c2, double h2) {
		final double dL = l2 - l1;
		final double lav = (l1 + l2) * 0.5d;
		final double c_av = (c1 + c2) * 0.5d;
		// a and b
		final double a1 = c1 * Math.cos(h1);
		final double a2 = c2 * Math.cos(h2);
		final double b1 = c1 * Math.sin(h1);
		final double b2 = c2 * Math.sin(h2);

		return deltaE2000(dL, lav, c_av, a1, b1, a2, b2);
	}

	static float deltaE2000Lab(float l1, float a1, float b1, float l2, float a2, float b2) {
		final double dL = l2 - l1;
		final double lav = (l1 + l2) * 0.5d;
		final double c1 = Math.sqrt(a1 * a1 + b1 * b1);
		final double c2 = Math.sqrt(a2 * a2 + b2 * b2);
		final double c_av = (c1 + c2) * 0.5d;

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
		final double cprime_av = (c1prime + c2prime) * 0.5d;

		final double TWOPI = Math.PI + Math.PI;
		double h1prime = Math.atan2(b1, a1prime);
		if (h1prime < 0d) {
			h1prime += TWOPI;
		}
		double h2prime = Math.atan2(b2, a2prime);
		if (h2prime < 0d) {
			h2prime += TWOPI;
		}

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
		hprime_av += (h1prime + h2prime) * 0.5d;
		final double dHprime = 2d * Math.sqrt(c1prime * c2prime) * Math.sin(dhprime * 0.5d);

		final double t = 1d - 0.17d * Math.cos(hprime_av - 0.5235988d) + 0.24d * Math.cos(hprime_av + hprime_av)
				+ 0.32d * Math.cos(3d * hprime_av + 0.1047198d) - 0.2d * Math.cos(4d * hprime_av - 1.099557d);

		double lav_minus50_sq = lav - 50d;
		lav_minus50_sq *= lav_minus50_sq;
		final double sL = 1d + 0.015d * lav_minus50_sq / Math.sqrt(lav_minus50_sq + 20d);
		final double sC = 1d + 0.045d * cprime_av;
		final double sH = 1d + 0.015d * cprime_av * t;

		final double cprime_av_pow7 = Math.pow(cprime_av, 7d);
		final double exp_arg = (hprime_av - 4.799655443d) / 0.436332313d;
		final double rt = -2d * Math.sqrt(cprime_av_pow7 / (cprime_av_pow7 + 6103515625d))
				* Math.sin(1.04719755d * Math.exp(-exp_arg * exp_arg));

		final double l_comp = dL / sL;
		final double c_comp = deltaCprime / sC;
		final double h_comp = dHprime / sH;
		final double hue_rotation = rt * c_comp * h_comp;
		final double dE00 = Math.sqrt(l_comp * l_comp + c_comp * c_comp + h_comp * h_comp + hue_rotation);
		return (float) dE00;
	}

	private static double deltaE2000ChromaReduction(double lav, double c1, double a1, double b1,
			double[] labClamped) {
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
		final double cprime_av = (c1prime + c2prime) * 0.5d;

		final double TWOPI = Math.PI + Math.PI;
		double h1prime = Math.atan2(b1, a1prime);
		if (h1prime < 0d) {
			h1prime += TWOPI;
		}
		double h2prime = Math.atan2(labClamped[2], a2prime);
		if (h2prime < 0d) {
			h2prime += TWOPI;
		}

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
		hprime_av += (h1prime + h2prime) * 0.5d;
		final double dHprime = 2d * Math.sqrt(c1prime * c2prime) * Math.sin(dhprime * 0.5d);

		final double t = 1d - 0.17d * Math.cos(hprime_av - 0.5235988d)
				+ 0.24d * Math.cos(hprime_av + hprime_av)
				+ 0.32d * Math.cos(3d * hprime_av + 0.1047198d)
				- 0.2d * Math.cos(4d * hprime_av - 1.099557d);

		final double sC = 1d + 0.045d * cprime_av;
		final double sH = 1d + 0.015d * cprime_av * t;

		final double cprime_av_pow7 = Math.pow(cprime_av, 7d);
		final double exp_arg = (hprime_av - 4.799655443d) / 0.436332313d;
		final double rt = -2d * Math.sqrt(cprime_av_pow7 / (cprime_av_pow7 + 6103515625d))
				* Math.sin(1.04719755d * Math.exp(-exp_arg * exp_arg));

		final double c_comp = deltaCprime / sC;
		final double h_comp = dHprime / sH;
		final double hue_rotation = rt * c_comp * h_comp;
		final double dE00 = Math.sqrt(c_comp * c_comp + h_comp * h_comp + hue_rotation);
		// To have a wild guess of the next chroma, put adjusted deltaCprime into labClamped[0]
		final double dE00minus2 = dE00 - 2d;
		double sqrtArg = dE00minus2 * dE00minus2 - h_comp * h_comp - hue_rotation;
		if (sqrtArg > 0d) {
			labClamped[0] = sC * Math.sqrt(sqrtArg);
		} else {
			labClamped[0] = 0d;
		}
		return dE00;
	}

	static double deltaEokLab(double[] ok1, double[] ok2) {
		final double dL = ok2[0] - ok1[0];
		final double da = ok2[1] - ok1[1];
		final double db = ok2[2] - ok1[2];
		return Math.sqrt(dL * dL + da * da + db * db);
	}

}
