/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

import org.w3c.dom.DOMException;

/**
 * List of selectors.
 * <p>
 * Based on SAC's {@code SelectorList} interface by Philippe Le Hegaret.
 * </p>
 */
public interface SelectorList extends Iterable<Selector> {

	/**
	 * Check id this list contains the given selector.
	 * <p>
	 * Comparisons are made according to the {@code equals()} method.
	 * </p>
	 * 
	 * @param selector the selector.
	 * @return {@code true} if this list contains the selector.
	 */
	boolean contains(Selector selector);

	/**
	 * Returns {@code true} if this list contains all of the selectors in the given
	 * list.
	 * 
	 * @param list the list to check.
	 * @return {@code true} if this list contains all of the selectors in the list.
	 */
	boolean containsAll(SelectorList list);

	/**
	 * Get the length of this selector list.
	 * 
	 * @return the length of this selector list, zero if empty.
	 */
	int getLength();

	/**
	 * Get the selector at the specified index.
	 * 
	 * @return the selector at the specified index, or <code>null</code> if the
	 *         index is not valid.
	 */
	Selector item(int index);

	/**
	 * Replace the selector at <code>index</code> with the given selector.
	 * <p>
	 * This method is not intended to trigger any event, so if you modify a a
	 * selector belonging to a rule obtained from the DOM, it is your responsibility
	 * to update the styles of the document.
	 * </p>
	 * 
	 * @param index    the index at which the selector has to be replaced.
	 * @param selector the new selector.
	 * @return the replaced selector.
	 * @throws DOMException         INDEX_SIZE_ERR if the <code>index</code> is less
	 *                              than zero or greater than the largest possible
	 *                              index.<br/>
	 *                              NO_MODIFICATION_ALLOWED_ERR if replacing
	 *                              selectors is not allowed.
	 * @throws NullPointerException if <code>selector</code> is <code>null</code>.
	 */
	default Selector replace(int index, Selector selector) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
	}

}
