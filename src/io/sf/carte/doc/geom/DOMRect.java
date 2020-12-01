/*
 * This software includes material derived from Geometry Interfaces Module Level 1
 * Specification (https://www.w3.org/TR/geometry-1/).
 * Copyright © 2018 W3C® (MIT, ERCIM, Keio, Beihang). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2020 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 * 
 */

package io.sf.carte.doc.geom;

/**
 * Interface representing a {@code DOMRect} value.
 */
public interface DOMRect {

	/**
	 * The horizontal distance between the viewport’s left edge and the rectangle’s origin.
	 * 
	 * @return the value of {@code x}.
	 */
	double getX();

	/**
	 * Set the horizontal distance between the viewport’s left edge and the rectangle’s origin.
	 * 
	 * @param value the value of {@code x}.
	 */
	void setX(double value);

	/**
	 * The vertical distance between the viewport’s top edge and the rectangle’s origin.
	 * 
	 * @return the value of {@code y}.
	 */
	double getY();

	/**
	 * Set the vertical distance between the viewport’s top edge and the rectangle’s origin.
	 * 
	 * @param value the value of {@code y}.
	 */
	void setY(double value);

	/**
	 * The width of the rectangle. Can be negative.
	 * 
	 * @return the value of {@code width}.
	 */
	double getWidth();

	/**
	 * Set the width of the rectangle.
	 * 
	 * @param value the value of {@code width}. Can be negative.
	 */
	void setWidth(double width);

	/**
	 * The height of the rectangle. Can be negative.
	 * 
	 * @return the value of {@code height}.
	 */
	double getHeight();

	/**
	 * Set the height of the rectangle.
	 * 
	 * @param value the value of {@code height}. Can be negative.
	 */
	void setHeight(double height);

	/**
	 * The top of the rectangle.
	 * 
	 * @return the top of the rectangle.
	 */
	double getTop();

	/**
	 * The right of the rectangle.
	 * 
	 * @return the right of the rectangle.
	 */
	double getRight();

	/**
	 * The bottom of the rectangle.
	 * 
	 * @return the bottom of the rectangle.
	 */
	double getBottom();

	/**
	 * The left of the rectangle.
	 * 
	 * @return the left of the rectangle.
	 */
	double getLeft();

}
