/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Arrays;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;
import io.sf.jclf.math.linear3.Matrices;

abstract class ColorProfile implements io.sf.carte.doc.color.RGBColorProfile {

	final double[][] m = new double[3][3];
	final double[][] minv = new double[3][3];

	protected ColorProfile(float xr, float yr, float xg, float yg, float xb, float yb,
			double[] white) {
		super();

		initialize(xr, yr, xg, yg, xb, yb, white);
	}

	void initialize(float xr, float yr, float xg, float yg, float xb, float yb,
			double[] white) {
		rgbToXYZmatrix(xr, yr, xg, yg, xb, yb, white);
		Matrices.inverse3(m, minv);
	}

	void rgbToXYZmatrix(float xr, float yr, float xg, float yg, float xb, float yb,
			double[] white) {
		m[0][0] = xr / yr;
		m[1][0] = 1d;
		m[2][0] = (1d - xr - yr) / yr;
		m[0][1] = xg / yg;
		m[1][1] = 1d;
		m[2][1] = (1d - xg - yg) / yg;
		m[0][2] = xb / yb;
		m[1][2] = 1d;
		m[2][2] = (1d - xb - yb) / yb;

		double[][] minv = new double[3][3];
		Matrices.inverse3(m, minv);
		double[] s = new double[3];
		Matrices.multiplyByVector3(minv, white, s);

		m[0][0] *= s[0];
		m[1][0] *= s[0];
		m[2][0] *= s[0];
		m[0][1] *= s[1];
		m[1][1] *= s[1];
		m[2][1] *= s[1];
		m[0][2] *= s[2];
		m[1][2] *= s[2];
		m[2][2] *= s[2];
	}

	public void linearRgbToXYZ(double r, double g, double b, double[] xyz) {
		// RGB to XYZ
		xyz[0] = m[0][0] * r + m[0][1] * g + m[0][2] * b;
		xyz[1] = m[1][0] * r + m[1][1] * g + m[1][2] * b;
		xyz[2] = m[2][0] * r + m[2][1] * g + m[2][2] * b;
	}

	@Override
	public void linearRgbToXYZ(double[] rgb, double[] xyz) {
		Matrices.multiplyByVector3(m, rgb, xyz);
	}

	@Override
	public void xyzToLinearRgb(double[] xyz, double[] linearRgb) {
		Matrices.multiplyByVector3(minv, xyz, linearRgb);
	}

	/**
	 * Perform a gamma companding.
	 * 
	 * @param linearComponent the linear component.
	 * @return the non-linear color component.
	 */
	@Override
	public double gammaCompanding(double linearComponent) {
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

	/**
	 * Perform an inverse gamma companding.
	 * 
	 * @param compandedComponent the non-linear color component.
	 * @return the linear component.
	 */
	@Override
	public double linearComponent(double compandedComponent) {
		// Inverse sRGB Companding
		final double abs = Math.abs(compandedComponent);
		double linearComp;
		if (abs <= 0.04045d) {
			linearComp = compandedComponent / 12.92d;
		} else {
			linearComp = Math.signum(compandedComponent) * Math.pow((abs + 0.055d) / 1.055d, 2.4d);
		}
		return linearComp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(m);
		result = prime * result + Arrays.hashCode(getWhitePoint());
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
		ColorProfile other = (ColorProfile) obj;
		return Arrays.deepEquals(m, other.m)
				&& Arrays.equals(getWhitePoint(), other.getWhitePoint());
	}

	abstract public Illuminant getIlluminant();

	@Override
	public double[] getWhitePoint() {
		return Illuminants.whiteD65;
	}

}
