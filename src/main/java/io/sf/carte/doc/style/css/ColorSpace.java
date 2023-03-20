/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Color spaces predefined by CSS.
 */
public interface ColorSpace {

	/**
	 * sRGB
	 */
	String srgb = "srgb";

	/**
	 * Display P3
	 * <p>
	 * Uses DCI-P3 primaries but with a D65 white point and the sRGB transfer curve.
	 * </p>
	 * <p>
	 * Also known as P3D65.
	 * </p>
	 */
	String display_p3 = "display-p3";

	/**
	 * <a href="https://www.w3.org/TR/css-color-4/#valdef-color-a98-rgb">A98 RGB</a>
	 */
	String a98_rgb = "a98-rgb";

	/**
	 * ProPhoto RGB
	 */
	String prophoto_rgb = "prophoto-rgb";

	/**
	 * ITU Recommendation BT.2020
	 */
	String rec2020 = "rec2020";

	/**
	 * CIE XYZ (D65)
	 */
	String xyz = "xyz";

	/**
	 * CIE XYZ (D50)
	 */
	String xyz_d50 = "xyz-d50";

	/**
	 * CIE Lab
	 */
	String cie_lab = "lab";

	/**
	 * CIE LCh
	 */
	String cie_lch = "lch";

	/**
	 * OKLab
	 */
	String ok_lab = "oklab";

	/**
	 * OK LCh
	 */
	String ok_lch = "oklch";

}
