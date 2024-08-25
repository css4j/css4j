/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * sRGB color profile.
 */
class SRGBColorProfile extends ColorProfile {

	public SRGBColorProfile() {
		super(0.6400f, 0.3300f, 0.3000f, 0.6000f, 0.1500f, 0.0600f, BaseColor.whiteD65);
	}

	@Override
	void initialize(float xr, float yr, float xg, float yg, float xb, float yb, double[] white) {
		m[0][0] = 0.41239079926595934d;
		m[0][1] = 0.357584339383878d;
		m[0][2] = 0.1804807884018343d;
		m[1][0] = 0.21263900587151027d;
		m[1][1] = 0.715168678767756d;
		m[1][2] = 0.07219231536073371d;
		m[2][0] = 0.01933081871559182d;
		m[2][1] = 0.11919477979462598d;
		m[2][2] = 0.9505321522496607d;

		minv[0][0] = 3.24096994190452d;
		minv[0][1] = -1.53738317757d;
		minv[0][2] = -0.498610760293d;
		minv[1][0] = -0.96924363628088d;
		minv[1][1] = 1.8759675015077d;
		minv[1][2] = 0.04155505740718d;
		minv[2][0] = 0.055630079697d;
		minv[2][1] = -0.20397695888898d;
		minv[2][2] = 1.05697151424288d;
	}

	@Override
	public Illuminant getIlluminant() {
		return Illuminant.D65;
	}

}
