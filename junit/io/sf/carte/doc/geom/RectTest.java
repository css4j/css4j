/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.geom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RectTest {

	@Test
	public void testRect() {
		Rect rect = new Rect();
		assertEquals(0d, rect.getX(), 1e-15);
		rect.setX(8d);
		assertEquals(8d, rect.getX(), 1e-15);
		rect = new Rect(8, 12, 40, 30);
		assertEquals(8d, rect.getX(), 1e-15);
		assertEquals(12d, rect.getY(), 1e-15);
		assertEquals(40d, rect.getWidth(), 1e-15);
		assertEquals(30d, rect.getHeight(), 1e-15);
		rect.setY(4d);
		assertEquals(4d, rect.getY(), 1e-15);
		rect.setWidth(160d);
		assertEquals(160d, rect.getWidth(), 1e-15);
		rect.setHeight(90d);
		assertEquals(90d, rect.getHeight(), 1e-15);
		//
		assertEquals(4d, rect.getTop(), 1e-15);
		assertEquals(168d, rect.getRight(), 1e-15);
		assertEquals(94d, rect.getBottom(), 1e-15);
		assertEquals(8d, rect.getLeft(), 1e-15);
		//
		rect.setWidth(-160d);
		rect.setHeight(-90d);
		assertEquals(-86d, rect.getTop(), 1e-15);
		assertEquals(8d, rect.getRight(), 1e-15);
		assertEquals(4d, rect.getBottom(), 1e-15);
		assertEquals(-152d, rect.getLeft(), 1e-15);
	}

}
