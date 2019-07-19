/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.util;

/**
 * Represents the differences between two objects.
 * <p>
 * Elements that are only present in the left object are returned by the
 * {@link #getLeftSide()} method, while those only in the right object will be
 * in the array returned by {@link #getRightSide()}. Elements present on both
 * objects but in a different way will be returned by {@link #getDifferent()}.
 * <p>
 * If an object implementing this interface is returned by a call like
 * <code>a.diff(b)</code>, then <code>a</code> is the left object and
 * <code>b</code> the right, the same for a call like <code>diff(a,b)</code>.
 * 
 * @param <E>
 *            the element or element identifier type.
 */
public interface Diff<E> {

	/**
	 * Does this object contain any difference?
	 * <p>
	 * If this method returns <code>false</code>, the <code>equals</code> method
	 * called on the same objects should return <code>true</code>, but that is
	 * not required by this interface.
	 * 
	 * @return <code>true</code> if this object holds information on differences
	 *         between two objects, <code>false</code> if the tested objects are
	 *         equal from the point of view of this diff object.
	 */
	boolean hasDifferences();

	/**
	 * Get an array with the elements that are only present in the left object.
	 * 
	 * @return an array with the elements (or element identifiers) that are only
	 *         present in the left object, or <code>null</code> otherwise.
	 */
	E[] getLeftSide();

	/**
	 * Get an array with the elements that are only present in the right object.
	 * 
	 * @return an array with the elements (or element identifiers) that are only
	 *         present in the right object, or <code>null</code> otherwise.
	 */
	E[] getRightSide();

	/**
	 * Get an array with the elements that are present in both objects, but in a
	 * different way.
	 * 
	 * @return an array with the elements (or element identifiers) that are
	 *         present in both objects but in a different way, or
	 *         <code>null</code> otherwise.
	 */
	E[] getDifferent();
}
