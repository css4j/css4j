/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.om.SimpleBoxModel.MyTableBoxValues;

public class BoxModelHelperTest {

	@Test
	public void testContractSpaces() {
		assertEquals("", BoxModelHelper.contractSpaces(""));
		assertEquals(" Lorem ipsum dolor sit amet ",
				BoxModelHelper.contractSpaces("    Lorem   ipsum      dolor sit   amet   "));
		assertEquals("Lorem ipsum dolor sit amet", BoxModelHelper.contractSpaces("Lorem ipsum dolor sit      amet"));
	}

	@Test
	public void testComputeMinimumCharsWidth() {
		assertEquals(13, BoxModelHelper.computeMinimumCharsWidth(
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"));
		assertEquals(11, BoxModelHelper.computeMinimumCharsWidth("Lorem ipsum dolor sit amet, consectetur"));
		assertEquals(12,
				BoxModelHelper.computeMinimumCharsWidth("exercitation Lorem ipsum dolor sit amet, consectetur"));
		assertEquals(21,
				BoxModelHelper.computeMinimumCharsWidth("exercitation Lorem\u00A0ipsum\u00A0dolor\u00A0sit amet"));
	}

	@Test
	public void testShrinkTo() {
		float[] initial = { 8f, 16f, 21f, 5f, 38f };

		MyTableBoxValues box = new MyTableBoxValues(CSSUnit.CSS_PX);
		box.colwidth = initial;
		float[] minrcw = { 8f, 12f, 10f, 5f, 9f };
		float curwidth = 0f;
		for (float iniColWidth : initial) {
			curwidth += iniColWidth;
		}
		float minwidth = 0f;
		for (float minColWidth : minrcw) {
			minwidth += minColWidth;
		}

		BoxModelHelper.shrinkTo(box, minrcw, minwidth, curwidth, 46f);
		float finalwidth = 0f;
		for (int i = 0; i < minrcw.length; i++) {
			finalwidth += box.colwidth[i];
		}

		assertEquals(46f, finalwidth, 0.1f);
	}

}
