/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.geom;

/**
 * Implementation of {@link DOMRect}.
 */
public class Rect implements DOMRect {

	private double x, y, width, height;

	public Rect() {
		this(0, 0, 0, 0);
	}

	public Rect(double x, double y, double width, double height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public void setX(double value) {
		this.x = value;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setY(double value) {
		this.y = value;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public void setWidth(double width) {
		this.width = width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setHeight(double height) {
		this.height = height;
	}

	@Override
	public double getTop() {
		return Math.min(y, y + height);
	}

	@Override
	public double getRight() {
		return Math.max(x, x + width);
	}

	@Override
	public double getBottom() {
		return Math.max(y, y + height);
	}

	@Override
	public double getLeft() {
		return Math.min(x, x + width);
	}

}
