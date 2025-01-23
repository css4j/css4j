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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This library's iterable version of the old {@link NodeList}.
 */
public interface ExtendedNodeList<T extends Node> extends NodeList, Iterable<T> {

	/**
	 * Determine whether this list contains the given node.
	 * 
	 * @param node the node to check for.
	 * @return <code>true</code> if this list contains the node, <code>false</code>
	 *         otherwise, even if <code>node</code> is a descendant of the child
	 *         nodes.
	 */
	boolean contains(Node node);

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
	T item(int index);

	/**
	 * Check whether this list is empty.
	 * 
	 * @return {@code true} if this list is empty.
	 */
	boolean isEmpty();

}
