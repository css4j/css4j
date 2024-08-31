/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.color;

/**
 * An RGB color profile.
 */
public interface RGBColorProfile extends ICCColorProfile {

	/**
	 * Perform an inverse gamma companding on the components.
	 * 
	 * @param rgb the non-linear color components.
	 */
	default void linearizeComponents(double[] rgb) {
		rgb[0] = linearComponent(rgb[0]);
		rgb[1] = linearComponent(rgb[1]);
		rgb[2] = linearComponent(rgb[2]);
	}

	/**
	 * Convert from linear RGB to xyz coordinates.
	 * 
	 * @param rgb a 3-vector with the r, g and b components.
	 * @param xyz a 3-vector that can store the resulting xyz coordinates, expressed
	 *            in this profile's chromaticity.
	 */
	void linearRgbToXYZ(double[] rgb, double[] xyz);

	/**
	 * Convert from xyz coordinates to linear RGB.
	 * 
	 * @param xyz       a 3-vector with the xyz coordinates, expressed in this
	 *                  profile's chromaticity.
	 * @param linearRgb a 3-vector that can store the resulting linear r, g and b
	 *                  components.
	 */
	void xyzToLinearRgb(double[] xyz, double[] linearRgb);

	/**
	 * Convert from xyz coordinates to RGB.
	 * 
	 * @param xyz a 3-vector with the xyz coordinates, expressed in this profile's
	 *            chromaticity.
	 * @param rgb a 3-vector that can store the resulting r, g and b components.
	 */
	default void xyzToRgb(double[] xyz, double[] rgb) {
		xyzToLinearRgb(xyz, rgb);
		rgb[0] = gammaCompanding(rgb[0]);
		rgb[1] = gammaCompanding(rgb[1]);
		rgb[2] = gammaCompanding(rgb[2]);
	}

	/**
	 * Apply the transfer function ('gamma companding') to a linear component.
	 * 
	 * @param linearComponent the linear component.
	 * @return the non-linear color component.
	 */
	double gammaCompanding(double linearComponent);

	/**
	 * Perform an inverse gamma companding.
	 * 
	 * @param compandedComponent the non-linear color component.
	 * @return the linear component.
	 */
	double linearComponent(double compandedComponent);

}
