/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2005-2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.css.CSSValueList;

/**
 * Augmented {@link CSSValueList} interface.
 *
 * @param <E> the value type.
 */
public interface ExtendedCSSValueList<E extends ExtendedCSSValue> extends CSSValueList, ExtendedCSSValue, Iterable<E> {

	/**
	 * Adds a value to the end of this list.
	 * 
	 * @param value
	 *            the value to be added.
	 * @return <code>true</code> (for consistency with Java collections).
	 */
	boolean add(E value);

	/**
	 * Removes all the items from this list.
	 */
	void clear();

	/**
	 * Is this list empty ?
	 * 
	 * @return <code>true</code> if this list has no items, <code>false</code> otherwise.
	 */
	boolean isEmpty();

	/**
	 * retrieve an <code>ExtendedCSSValue</code> by ordinal index.
	 * 
	 * @param index the index in this list.
	 * @return the value at <code>index</code>, or <code>null</code> if
	 *         <code>index</code> is less than zero, or greater or equal to the list
	 *         length.
	 */
	@Override
	E item(int index);

	/**
	 * Removes the value at the specified index.
	 * 
	 * @param index
	 *            the index of the value to be removed.
	 * @return the list item that was removed.
	 * @throws IndexOutOfBoundsException if the index is invalid.
	 */
	E remove(int index);

	/**
	 * Replaces the value at the specified index with the supplied value.
	 * 
	 * @param index
	 *            the index of the value to be replaced.
	 * @param value
	 *            the value to replace the item at <code>index</code>.
	 * @return the item previously at the specified position.
	 * @throws IndexOutOfBoundsException if the index is invalid.
	 * @throws NullPointerException if the value is <code>null</code>.
	 */
	E set(int index, E value);

	/**
	 * Creates and returns a copy of this object.
	 * <p>
	 * The list is cloned, but its contents are not.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	ExtendedCSSValueList<E> clone();

}