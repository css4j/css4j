/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import java.util.ListIterator;

import org.w3c.dom.Node;

/**
 * Iterates over the document nodes according to a set of parameters.
 * <h3>Interoperability</h3>
 * <p>
 * This interface is based on the <a href=
 * "https://www.w3.org/TR/dom/#interface-nodeiterator"><code>NodeIterator</code>
 * interface in DOM Level 4</a>, although behaviour does not exactly match:
 * </p>
 * <p>
 * Iterators implementing this interface are expected to skip full nodes
 * (descendants included) when the <code>NodeFilter</code> returns
 * {@link NodeFilter#FILTER_SKIP_NODE_CHILD}, while the W3C standard would
 * expect it to behave like {@link org.w3c.dom.traversal.NodeFilter#FILTER_SKIP}
 * (which in this implementation is called {@link NodeFilter#FILTER_SKIP_NODE}),
 * and skip only the node itself.
 * </p>
 * <p>
 * Additionally, beware that this iterator also behaves differently from the
 * Xerces <code>NodeIterator</code> regarding attributes:
 * </p>
 * <nl>
 * <li>Attributes do not always follow the specified order in Xerces.</li>
 * <li>Its iterator of attributes only traverses the root node.</li>
 * </nl>
 * <p>
 * If the filtering done by the <code>NodeFilter</code> changes during the
 * traversal (that is, nodes that were previously accepted now aren't), the
 * behaviour of this iterator may become inconsistent when going backwards. In
 * that case, you may want to try a <code>TreeWalker</code> instead.
 * </p>
 * <h3><code>ListIterator</code> optional operations: <code>add</code>,
 * <code>set</code>, <code>remove</code></h3>
 * <p>
 * This library's iterator implements {@link ListIterator} and its optional
 * operations, but any attempts to <code>remove</code> the root node, to change
 * it with <code>set</code> (or to insert a node before it with
 * <code>add</code>) must fail.
 * </p>
 */
public interface NodeIterator extends ListIterator<Node> {

	/**
	 * The root node to iterate.
	 * <p>
	 * See the description of
	 * <a href= "https://www.w3.org/TR/dom/#dom-nodeiterator-root"><code>root</code>
	 * in DOM Level 4</a>.
	 * 
	 * @return the root node.
	 */
	DOMNode getRoot();

	/**
	 * The bitmask specifying what types of nodes to show.
	 * 
	 * @return the bitmask to control which types of nodes are returned by the
	 *         iterator.
	 */
	int getWhatToShow();

	/**
	 * Get the <code>NodeFilter</code> callback.
	 * <p>
	 * See {@link NodeFilter}.
	 * 
	 * @return a reference to the <code>NodeFilter</code> callback.
	 */
	NodeFilter getNodeFilter();

	@Override
	DOMNode next();

	/**
	 * The same as {@link #next()}, but avoids the
	 * <code>NoSuchElementException</code>. Exists for compatibility with the
	 * <a href=
	 * "https://www.w3.org/TR/dom/#interface-nodeiterator"><code>NodeIterator</code>
	 * in DOM Level 4</a>.
	 * 
	 * @return the next node, or <code>null</code> if there is no next node.
	 */
	DOMNode nextNode();

	@Override
	DOMNode previous();

	/**
	 * The same as {@link #previous()}, but avoids the
	 * <code>NoSuchElementException</code>. Exists for compatibility with the
	 * <a href=
	 * "https://www.w3.org/TR/dom/#interface-nodeiterator"><code>NodeIterator</code>
	 * in DOM Level 4</a>.
	 * 
	 * @return the previous node, or <code>null</code> if there is no previous node.
	 */
	DOMNode previousNode();

}
