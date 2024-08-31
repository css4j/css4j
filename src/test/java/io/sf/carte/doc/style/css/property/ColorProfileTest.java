/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.color.Illuminants;

class ColorProfileTest {

	@Test
	public void testSRGB() {
		ColorProfile profile = new SRGBColorProfile();

		assertTrue(Arrays.equals(Illuminants.whiteD65, profile.getWhitePoint()));

		double[][] m = profile.m;
		assertEquals(0.41239079926595934, m[0][0], 1e-17);
		assertEquals(0.357584339383878, m[0][1], 1e-17);
		assertEquals(0.1804807884018343, m[0][2], 1e-17);
		assertEquals(0.21263900587151027, m[1][0], 1e-17);
		assertEquals(0.715168678767756, m[1][1], 1e-17);
		assertEquals(0.07219231536073371, m[1][2], 1e-17);
		assertEquals(0.01933081871559182, m[2][0], 1e-17);
		assertEquals(0.11919477979462598, m[2][1], 1e-17);
		assertEquals(0.9505321522496607, m[2][2], 1e-17);

		m = profile.minv;
		assertEquals(3.24096994190452, m[0][0], 1e-18);
		assertEquals(-1.53738317757, m[0][1], 1e-18);
		assertEquals(-0.498610760293, m[0][2], 1e-18);
		assertEquals(-0.96924363628088, m[1][0], 1e-18);
		assertEquals(1.8759675015077, m[1][1], 1e-18);
		assertEquals(0.04155505740718, m[1][2], 1e-18);
		assertEquals(0.055630079697, m[2][0], 1e-18);
		assertEquals(-0.20397695888898, m[2][1], 1e-18);
		assertEquals(1.05697151424288, m[2][2], 1e-18);
	}

	@Test
	public void testP3() {
		ColorProfile profile = new DisplayP3ColorProfile();

		assertTrue(Arrays.equals(Illuminants.whiteD65, profile.getWhitePoint()));

		double[][] m = profile.m;
		assertEquals(0.48663266057836146, m[0][0], 1e-17);
		assertEquals(0.26566313946970616, m[0][1], 1e-17);
		assertEquals(0.19817419995193253, m[0][2], 1e-17);
		assertEquals(0.22900360497805244, m[1][0], 1e-17);

		m = profile.minv;
		assertEquals(2.493180648327966, m[0][0], 1e-18);
		assertEquals(-0.9312653930437527, m[0][1], 1e-18);
		assertEquals(-0.40265975200217624, m[0][2], 1e-18);
		assertEquals(-0.829503102606982, m[1][0], 1e-18);
	}

	@Test
	public void testEquals() {
		ColorProfile srgb = new SRGBColorProfile();
		ColorProfile srgb2 = new SRGBColorProfile();
		ColorProfile p3 = new DisplayP3ColorProfile();
		ColorProfile pro = new ProPhotoRGBColorProfile();

		assertEquals(srgb, srgb);
		assertEquals(srgb, srgb2);
		assertEquals(srgb.hashCode(), srgb2.hashCode());
		assertNotEquals(srgb, null);
		assertNotEquals(srgb, p3);
		assertNotEquals(srgb.hashCode(), p3.hashCode());
		assertNotEquals(pro, p3);
		assertNotEquals(pro.hashCode(), p3.hashCode());
	}

}
