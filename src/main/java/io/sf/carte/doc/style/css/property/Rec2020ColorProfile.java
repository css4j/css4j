/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

class Rec2020ColorProfile extends ColorProfile {

	private static final double ALPHA = 1.09929682680944d;
	private static final double BETA = 0.018053968510807d;

	public Rec2020ColorProfile() {
		super(0.708f, 0.292f, 0.170f, 0.797f, 0.131f, 0.046f, BaseColor.whiteD65);
	}

	@Override
	public double gammaCompanding(double linearComponent) {
		// REC 2020 Companding
		final double abs = Math.abs(linearComponent);

		double comp;
		if (abs < BETA) {
			comp = linearComponent * 4.5d;
		} else {
			comp = Math.signum(linearComponent) * Math.pow(abs, 0.45d) * ALPHA - (ALPHA - 1d);
		}
		return comp;
	}

	@Override
	public double linearComponent(double comp) {
		final double abs = Math.abs(comp);

		double cl;
		if (abs < BETA * 4.5d) {
			cl = comp / 4.5d;
		} else {
			cl = Math.signum(comp) * Math.pow((abs + ALPHA - 1d) / ALPHA, 1d / 0.45d);
		}
		return cl;
	}

	@Override
	public Illuminant getIlluminant() {
		return Illuminant.D65;
	}

}
