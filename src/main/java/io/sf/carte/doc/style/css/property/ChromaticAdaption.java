/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.jclf.math.linear3.Matrices;

/**
 * Utility class for computing chromatic adaption matrices.
 */
class ChromaticAdaption {

	private static final double[][] bradford = { { 0.8951d, 0.2664d, -0.1614d },
			{ -0.7502d, 1.7135d, 0.0367d }, { 0.0389d, -0.0685d, 1.0296d } };

	/**
	 * Compute a Bradford-based Chromatic Adaption Matrix and put it in the supplied
	 * array.
	 * 
	 * @param whiteSrc  the source white.
	 * @param whiteDest the destination white.
	 * @param cam       the result array.
	 */
	public static void chromaticAdaptionMatrix(double[] whiteSrc, double[] whiteDest,
			double[][] cam) {
		// http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html

		double[][] inverse = new double[3][3];
		// Compute the inverse of the Bradford matrix
		Matrices.inverse3(bradford, inverse);

		// Transform into cone response domain
		double[] coneSrc = new double[3];
		Matrices.multiplyByVector3(bradford, whiteSrc, coneSrc);
		double[] coneDest = new double[3];
		Matrices.multiplyByVector3(bradford, whiteDest, coneDest);

		double[][] wr = new double[3][3];
		wr[0][0] = coneDest[0] / coneSrc[0];
		wr[1][1] = coneDest[1] / coneSrc[1];
		wr[2][2] = coneDest[2] / coneSrc[2];

		double[][] product1 = new double[3][3];
		Matrices.multiplyByMatrix3(wr, bradford, product1);

		Matrices.multiplyByMatrix3(inverse, product1, cam);
	}

}
