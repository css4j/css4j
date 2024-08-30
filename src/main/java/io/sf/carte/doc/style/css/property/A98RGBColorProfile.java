/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.color.Illuminant;
import io.sf.carte.doc.color.Illuminants;

/**
 * A98 RGB color profile.
 */
class A98RGBColorProfile extends ColorProfile {

	public A98RGBColorProfile() {
		super(0.6400f, 0.3300f, 0.2100f, 0.7100f, 0.1500f, 0.0600f, Illuminants.whiteD65);
	}

	@Override
	public double gammaCompanding(double linearComponent) {
		final double invGamma = 256d / 563d;
		// Gamma Companding
		return Math.signum(linearComponent) * Math.pow(Math.abs(linearComponent), invGamma);
	}

	@Override
	public double linearComponent(double compandedComponent) {
		final double gamma = 563d / 256d;
		return Math.signum(compandedComponent) * Math.pow(Math.abs(compandedComponent), gamma);
	}

	@Override
	public Illuminant getIlluminant() {
		return Illuminant.D65;
	}

}
