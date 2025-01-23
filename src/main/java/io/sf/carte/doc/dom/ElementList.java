/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2017-2025, Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

/**
 * <code>DOMElement</code>-specific {@link ExtendedNodeList}.
 * <p>
 * Contains the subset of child nodes that are elements.
 */
public interface ElementList extends ExtendedNodeList<DOMElement> {

	/**
	 * Given the subset of child nodes that are elements, access the element at the
	 * <code>index</code> position of this list, with the index starting with
	 * <code>0</code>.
	 * <p>
	 * For better performance, please use an iterator instead of this method.
	 * </p>
	 *
	 * @param index the index.
	 * @return the element at the given position, or <code>null</code> if the
	 *         specified index is beyond the last item or less than zero.
	 */
	@Override
	DOMElement item(int index);
}
