/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;

/**
 * ProPhoto RGB color profile.
 */
class ProPhotoRGBColorProfile extends ColorProfile {

	public ProPhotoRGBColorProfile() {
		super(0.734699f, 0.265301f, 0.159597f, 0.840403f, 0.036598f, 0.000105f, Illuminants.whiteD50);
	}

	@Override
	public double gammaCompanding(double linearComponent) {
		// ProPhoto Companding
		double comp;
		final double abs = Math.abs(linearComponent);
		if (abs <= 1d / 512d) {
			comp = linearComponent * 16d;
		} else {
			comp = Math.signum(linearComponent) * Math.pow(abs, 1d / 1.8d);
		}
		return comp;
	}

	@Override
	public double linearComponent(double c) {
		final double eps = 16d / 512d;
		final double abs = Math.abs(c);

		double cl;
		if (abs <= eps) {
			cl = c / 16d;
		} else {
			cl = Math.signum(c) * Math.pow(abs, 1.8d);
		}

		return cl;
	}

	@Override
	public Illuminant getIlluminant() {
		return Illuminant.D50;
	}

	@Override
	public double[] getWhitePoint() {
		return Illuminants.whiteD50;
	}

}
