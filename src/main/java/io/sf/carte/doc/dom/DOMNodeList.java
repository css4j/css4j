/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019-2025, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import org.w3c.dom.NodeList;

/**
 * <code>DOMNode</code>-specific version of {@link NodeList}.
 */
public interface DOMNodeList extends ExtendedNodeList<DOMNode> {

	/**
	 * Get the node located at the <code>index</code> position in this list.
	 * <p>
	 * For better performance, please use an iterator instead of this method.
	 * </p>
	 * 
	 * @return the node located at the <code>index</code> position in this list, or
	 *         <code>null</code> if <code>index</code> is less than zero or greater
	 *         or equal to the number of nodes in this list.
	 */
	@Override
	DOMNode item(int index);

}
