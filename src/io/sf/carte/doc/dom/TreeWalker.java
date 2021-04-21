/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

/**
 * Traverse the document's nodes according to a set of parameters.
 * <p>
 * <code>TreeWalker</code> objects can be used to navigate a document tree or
 * subtree using the view of the document defined by their
 * <code>whatToShow</code> flags and filter (if any). The flags and filter
 * define a view of a document (or subtree), a logical view. The methods in this
 * interface allow to traverse that view.
 * </p>
 * <h2>Interoperability</h2>
 * <p>
 * This interface is based on the <a href=
 * "https://www.w3.org/TR/dom/#interface-treewalker"><code>TreeWalker</code>
 * interface in DOM Level 4</a>, and it does not allow setting a
 * <code>currentNode</code> that is not an inclusive descendant of the
 * <code>root</code> node. The <a href=
 * "https://www.w3.org/TR/2000/REC-DOM-Level-2-Traversal-Range-20001113/traversal.html">old
 * specification</a> allows to set any node as <code>currentNode</code>,
 * including nodes outside of the <code>root</code> subtree, but the
 * aforementioned newer specification seems to imply, as of this writing, the
 * opposite.
 * </p>
 * <p>
 * Also, this implementation behaves differently from the Xerces
 * <code>TreeWalker</code> regarding attributes.
 * </p>
 */
public interface TreeWalker extends org.w3c.dom.traversal.TreeWalker {

	/**
	 * Get the <code>NodeFilter</code> callback.
	 * <p>
	 * See {@link NodeFilter}.
	 * 
	 * @return a reference to the <code>NodeFilter</code> callback.
	 */
	NodeFilter getNodeFilter();

	/**
	 * If the filter implements <code>org.w3c.dom.traversal.NodeFilter</code>,
	 * return it.
	 * <p>
	 * This library's <code>NodeFilter</code> interface is essentially the same as
	 * the W3C interface, but the filtering constants are named differently as a
	 * reminder that the filtering behaviour of this library's implementation of
	 * <code>NodeIterator</code> is not exactly what was specified by W3C.
	 * 
	 * @return the filter implementing the W3C's <code>org.w3c.dom.traversal</code>
	 *         interface, or <code>null</code> otherwise.
	 */
	@Override
	org.w3c.dom.traversal.NodeFilter getFilter();

	/**
	 * The root node.
	 * <p>
	 * See the description of
	 * <a href= "https://www.w3.org/TR/dom/#dom-treewalker-root"><code>root</code>
	 * in DOM Level 4</a>.
	 * 
	 * @return the root node.
	 */
	@Override
	DOMNode getRoot();

	/**
	 * The bitmask specifying what types of nodes to show.
	 * 
	 * @return the bitmask to control which types of nodes are returned by the
	 *         <code>TreeWalker</code>.
	 */
	@Override
	int getWhatToShow();

	/**
	 * The node at which the TreeWalker is currently positioned.
	 * <p>
	 * Alterations to the DOM tree may cause the current node to no longer be
	 * accepted by the TreeWalker's associated filter (it may also be explicitly set
	 * to such a location by {@link #setCurrentNode(Node)}).
	 * <p>
	 * Further traversal occurs relative to currentNode even if it is not part of
	 * the current view, by applying the filters in the requested direction; if no
	 * traversal is possible, currentNode is not changed.
	 * 
	 * @return the node at which the TreeWalker is currently positioned.
	 */
	@Override
	DOMNode getCurrentNode();

	/**
	 * Set the node at which the TreeWalker is currently positioned.
	 * <p>
	 * The so-called <code>currentNode</code> may be explicitly set to any inclusive
	 * descendant of the root node, regardless of it being accepted by the filter
	 * and <code>whatToShow</code> flags. This differs from the W3C specification,
	 * where any node can be set.
	 * <p>
	 * Further traversal occurs relative to <code>currentNode</code> even if it is
	 * not part of the logical view, by applying the filters in the requested
	 * direction; if no traversal is possible, currentNode is not changed.
	 * 
	 * @param currentNode the node to locate the TreeWalker at.
	 */
	@Override
	void setCurrentNode(Node currentNode);

	/**
	 * Moves the TreeWalker to the first visible child of the current node, and
	 * returns the new node.
	 * <p>
	 * If the current node has no visible children, returns <code>null</code>, and
	 * retains the current node.
	 * 
	 * @return the new node, or <code>null</code> if the current node has no visible
	 *         children in the logical view.
	 */
	@Override
	DOMNode firstChild();

	/**
	 * Moves the TreeWalker to the last visible child of the current node, and
	 * returns the new node.
	 * <p>
	 * If the current node has no visible children, returns <code>null</code>, and
	 * retains the current node.
	 * 
	 * @return the new node, or <code>null</code> if the current node has no visible
	 *         children in the logical view.
	 */
	@Override
	DOMNode lastChild();

	/**
	 * Moves the TreeWalker to the next visible node in document order relative to
	 * the current node, and returns the new node.
	 * <p>
	 * If the current node has no next node, or if the search for nextNode attempts
	 * to step upward from the TreeWalker's root node, returns <code>null</code>,
	 * and retains the current node.
	 * 
	 * @return the next node, or <code>null</code> if there is no next node.
	 */
	@Override
	DOMNode nextNode();

	/**
	 * Moves the TreeWalker to the previous visible node in document order relative
	 * to the current node, and returns the new node.
	 * <p>
	 * If the current node has no previous node, or if the search for previousNode
	 * attempts to step upward from the TreeWalker's root node, returns
	 * <code>null</code>, and retains the current node.
	 * 
	 * @return the previous node, or <code>null</code> if there is no previous node.
	 */
	@Override
	DOMNode previousNode();

	/**
	 * Moves the TreeWalker to the next sibling of the current node, and returns the
	 * new node.
	 * <p>
	 * If the current node has no visible next sibling, returns <code>null</code>,
	 * and retains the current node.
	 * 
	 * @return the next sibling or <code>null</code> if there is no next sibling in
	 *         the logical view.
	 */
	@Override
	DOMNode nextSibling();

	/**
	 * Moves the TreeWalker to the previous sibling of the current node, and returns
	 * the new node.
	 * <p>
	 * If the current node has no visible previous sibling, returns
	 * <code>null</code>, and retains the current node.
	 * 
	 * @return the previous sibling or <code>null</code> if there is no previous
	 *         sibling in the logical view.
	 */
	@Override
	DOMNode previousSibling();

	/**
	 * Moves to and returns the closest visible ancestor node of the current node.
	 * <p>
	 * If the search for <code>parentNode</code> attempts to step upward from the
	 * TreeWalker's <code>root</code> node, or if it fails to find a visible
	 * ancestor node, this method retains the current position and returns
	 * <code>null</code>.
	 * 
	 * @return the new parent node, or <code>null</code> if the current node has no
	 *         parent in the logical view.
	 */
	@Override
	DOMNode parentNode();

}
