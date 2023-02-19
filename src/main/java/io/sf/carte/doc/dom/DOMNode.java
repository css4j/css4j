/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019-2023, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * DOM Node.
 * <p>
 * Use this interface or W3C's {@link org.w3c.dom.Node} at your convenience
 * (this one may save you a few type casts).
 */
public interface DOMNode extends io.sf.carte.doc.style.css.CSSNode {

	/**
	 * Adds the node <code>newChild</code> to the end of the list of
	 * children of this node. If the <code>newChild</code> is already in the tree,
	 * it is first removed.
	 * 
	 * @param newChild the node to append. If it is a <code>DocumentFragment</code>
	 *                 object, the entire contents of the document fragment are
	 *                 moved into the child list of this node.
	 * @return the appended node.
	 * @throws DOMException HIERARCHY_REQUEST_ERR: raised if this node is of a type
	 *                      that does not allow children of the type of the
	 *                      <code>newChild</code> node, or if the node to append is
	 *                      one of this node's ancestors or this node itself, or if
	 *                      this node is of type <code>Document</code> and the DOM
	 *                      application attempts to append a second
	 *                      <code>DocumentType</code> or <code>Element</code>
	 *                      node.<br/>
	 *                      WRONG_DOCUMENT_ERR: if <code>newChild</code> was created
	 *                      from a different document than the one that created this
	 *                      node.<br/>
	 *                      NOT_SUPPORTED_ERR: if this implementation does not support
	 *                      children of the type of <code>newChild</code> at this node.
	 */
	@Override
	DOMNode appendChild(Node newChild) throws DOMException;

	/**
	 * Get the children of this node.
	 * 
	 * @return a <code>DOMNodeList</code> with the children of this node. If there
	 *         are no children, an empty list is returned.
	 */
	@Override
	DOMNodeList getChildNodes();

	/**
	 * Get the first child of this node.
	 * 
	 * @return the first child of this node, <code>null</code> if has no child nodes.
	 */
	@Override
	DOMNode getFirstChild();

	/**
	 * Get the last child of this node.
	 * 
	 * @return the last child of this node, <code>null</code> if has no child nodes.
	 */
	@Override
	DOMNode getLastChild();

	/**
	 * Get the node immediately following this node in its parent's child list.
	 * 
	 * @return the node immediately following this node in the child list, or
	 *         <code>null</code> if none.
	 */
	@Override
	DOMNode getNextSibling();

	/**
	 * Get the <code>DOMDocument</code> object related to this node (for all nodes
	 * except <code>DocumentType</code>, it is the document that created it), which
	 * is also the object that should be used to create new nodes for the document.
	 * 
	 * @return the <code>DOMDocument</code> object corresponding to this node. When
	 *         this node is a <code>DOMDocument</code> or a
	 *         <code>DocumentType</code> which was not added to any
	 *         <code>DOMDocument</code> yet, this is <code>null</code>.
	 */
	@Override
	DOMDocument getOwnerDocument();

	/**
	 * {@inheritDoc}
	 */
	@Override
	DOMNode getParentNode();

	/**
	 * Get the node immediately preceding this node in its parent's child list.
	 * 
	 * @return the node immediately preceding this node in the child list, or
	 *         <code>null</code> if none.
	 */
	@Override
	DOMNode getPreviousSibling();

	/**
	 * Does this node have any child nodes ?
	 * 
	 * @return <code>true</code> if this node has child nodes, <code>false</code>
	 *         otherwise.
	 */
	@Override
	boolean hasChildNodes();

	/**
	 * Inserts the node <code>newChild</code> right before node
	 * <code>refChild</code> in the child node list.
	 * <ul>
	 * <li>If <code>refChild</code> is <code>null</code>, <code>newChild</code> is
	 * appended at the end of the child list.</li>
	 * <li>If the <code>newChild</code> is already in the tree, it is first
	 * removed.</li>
	 * <li>Inserting a node before itself has no effect.</li>
	 * </ul>
	 * 
	 * @param newChild the node to put at the child node list, before
	 *                 <code>refChild</code>. If it is a
	 *                 <code>DocumentFragment</code> object, the entire contents of
	 *                 the document fragment are inserted into the child list of
	 *                 this node.
	 * @param refChild the node before which <code>newChild</code> must be inserted.
	 * @return the inserted node.
	 * @throws DOMException HIERARCHY_REQUEST_ERR: raised if this node is of a type
	 *                      that does not allow children of the type of the
	 *                      <code>newChild</code> node, or if the node to append is
	 *                      one of this node's ancestors or this node itself, or if
	 *                      this node is of type <code>Document</code> and the DOM
	 *                      application attempts to append a second
	 *                      <code>DocumentType</code> or <code>Element</code>
	 *                      node.<br/>
	 *                      WRONG_DOCUMENT_ERR: if <code>newChild</code> was created
	 *                      from a different document than the one that created this
	 *                      node.<br/>
	 *                      NOT_FOUND_ERR: if <code>refChild</code> is not a child
	 *                      of this node.<br/>
	 *                      NOT_SUPPORTED_ERR: if this implementation does not
	 *                      support children of the type of <code>newChild</code> at
	 *                      this node.
	 */
	@Override
	DOMNode insertBefore(Node newChild, Node refChild) throws DOMException;

	/**
	 * Removes all the children from this node, if any.
	 */
	void removeAllChild();

	/**
	 * Removes the node <code>oldChild</code> from the children of this node.
	 * 
	 * @param oldChild the node to remove.
	 * @return the removed node.
	 * @throws DOMException NOT_FOUND_ERR: if <code>oldChild</code> is not a child
	 *                      of this node.
	 */
	@Override
	DOMNode removeChild(Node oldChild) throws DOMException;

	/**
	 * Replaces the node <code>oldChild</code> with <code>newChild</code>. If the
	 * <code>newChild</code> is already in the tree, it is first removed.
	 * <p>
	 * Replacing a node with itself has no effect.
	 * 
	 * @param newChild the node to put at the child node list, in place of
	 *                 <code>oldChild</code>. If it is a
	 *                 <code>DocumentFragment</code> object, the entire contents of
	 *                 the document fragment are inserted into the child list of
	 *                 this node.
	 * @param oldChild the node being replaced.
	 * @return the replaced (old) node.
	 * @throws DOMException HIERARCHY_REQUEST_ERR: raised if this node is of a type
	 *                      that does not allow children of the type of the
	 *                      <code>newChild</code> node, or if the node to append is
	 *                      one of this node's ancestors or this node itself, or if
	 *                      this node is of type <code>Document</code> and the DOM
	 *                      application attempts to append a second
	 *                      <code>DocumentType</code> or <code>Element</code>
	 *                      node.<br/>
	 *                      WRONG_DOCUMENT_ERR: if <code>newChild</code> was created
	 *                      from a different document than the one that created this
	 *                      node.<br/>
	 *                      NOT_FOUND_ERR: if <code>oldChild</code> is not a child
	 *                      of this node.<br/>
	 *                      NOT_SUPPORTED_ERR: if this implementation does not
	 *                      support children of the type of <code>newChild</code> at
	 *                      this node.
	 */
	@Override
	DOMNode replaceChild(Node newChild, Node oldChild) throws DOMException;

}
