/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.color.Illuminants;

class ChromaticAdaptionTest {

	@Test
	public void testChromaticAdaptionMatrixD50D65() {
		double[][] cam = new double[3][3];
		ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD50, Illuminants.whiteD65, cam);

		assertEquals(0.9555766d, cam[0][0], 1e-7);
		assertEquals(-0.0230393d, cam[0][1], 1e-7);
		assertEquals(0.0631636d, cam[0][2], 1e-7);
		assertEquals(-0.0282895d, cam[1][0], 1e-7);
		assertEquals(1.0099416d, cam[1][1], 1e-7);
		assertEquals(0.0210077d, cam[1][2], 1e-7);
		assertEquals(0.0122982d, cam[2][0], 1e-7);
		assertEquals(-0.0204830d, cam[2][1], 1e-7);
		assertEquals(1.3299098d, cam[2][2], 1e-7);
	}

	@Test
	public void testChromaticAdaptionMatrixD65D50() {
		double[][] cam = new double[3][3];
		ChromaticAdaption.chromaticAdaptionMatrix(Illuminants.whiteD65, Illuminants.whiteD50, cam);

		assertEquals(1.0478112436606313d, cam[0][0], 1e-16);
		assertEquals(0.022886602481693052d, cam[0][1], 1e-18);
		assertEquals(-0.05012697596852886d, cam[0][2], 1e-18);
		assertEquals(0.029542398290574905d, cam[1][0], 1e-18);
		assertEquals(0.9904844034904394d, cam[1][1], 1e-16);
		assertEquals(-0.017049095628961564d, cam[1][2], 1e-18);
		assertEquals(-0.009234489723309473d, cam[2][0], 1e-18);
		assertEquals(0.015043616793498756d, cam[2][1], 1e-17);
		assertEquals(0.7521316354746059d, cam[2][2], 1e-16);
	}

}
