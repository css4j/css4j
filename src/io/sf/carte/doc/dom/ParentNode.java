/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2017-2020, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import java.util.BitSet;
import java.util.Iterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Based on W3C's <code>ParentNode</code> interface.
 */
public interface ParentNode extends DOMNode {

	/**
	 * Gets a <code>DOMNodeList</code> containing all the children of this parent
	 * node.
	 * 
	 * @return the list of child nodes, empty if there are no children.
	 */
	@Override
	DOMNodeList getChildNodes();

	/**
	 * Gets the live ElementList containing all nodes of type Element that are children of
	 * this ParentNode.
	 * 
	 * @return the ElementList containing all nodes of type Element that are children of this
	 *         ParentNode.
	 */
	ElementList getChildren();

	/**
	 * Gets the Element that is the first child of this ParentNode.
	 * 
	 * @return the Element that is the first child of this ParentNode, or <code>null</code> if there is
	 *         none.
	 */
	DOMElement getFirstElementChild();

	/**
	 * Gets the Element that is the last child of this ParentNode.
	 * 
	 * @return the Element that is the last child of this ParentNode, or <code>null</code> if there is
	 *         none.
	 */
	DOMElement getLastElementChild();

	/**
	 * Gets the number of child nodes of type Element that this parent node has.
	 * 
	 * @return the number of child nodes of type Element that this parent node has.
	 */
	int getChildElementCount();

	/**
	 * Inserts the node <code>newChild</code> at the beginning of the list of
	 * children of this node. If the <code>newChild</code> is already in the tree,
	 * it is first removed.
	 * 
	 * @param newChild the node to prepend. If it is a <code>DocumentFragment</code>
	 *                 object, the entire contents of the document fragment are
	 *                 moved into the child list of this node.
	 * @return the prepended node.
	 * @throws DOMException HIERARCHY_REQUEST_ERR: raised if this node is of a type
	 *                      that does not allow children of the type of the
	 *                      <code>newChild</code> node, or if the node to prepend is
	 *                      one of this node's ancestors or this node itself, or if
	 *                      this node is of type <code>Document</code> and the DOM
	 *                      application attempts to prepend a second
	 *                      <code>DocumentType</code> or <code>Element</code>
	 *                      node.<br/>
	 *                      WRONG_DOCUMENT_ERR: if <code>newChild</code> was created
	 *                      from a different document than the one that created this
	 *                      node.<br/>
	 *                      NOT_SUPPORTED_ERR: if this implementation does not support
	 *                      children of the type of <code>newChild</code> at this node.
	 */
	DOMNode prependChild(Node newChild) throws DOMException;

	/**
	 * Creates a new iterator over the child nodes.
	 * 
	 * @return an iterator over the child nodes.
	 */
	Iterator<DOMNode> iterator();

	/**
	 * Creates a new iterator descending over the child nodes, starting from the
	 * last child node.
	 * 
	 * @return an iterator descending over the child nodes.
	 */
	Iterator<DOMNode> descendingIterator();

	/**
	 * Creates a new iterator over the child nodes.
	 * <p>
	 * It only iterates over types set in the whatToShow bit field.
	 * <p>
	 * Example:
	 * <pre>
	 *      BitSet mask = new BitSet(32);
	 *      mask.set(Node.ELEMENT_NODE);
	 *      Iterator<Node> it = node.iterator(mask);
	 * </pre>
	 * @param whatToShow a bit set.
	 * @return an iterator over the child nodes.
	 */
	Iterator<DOMNode> iterator(BitSet whatToShow);

	/**
	 * Creates a new iterator over the child nodes.
	 * <p>
	 * It only iterates over nodes accepted by the filter.
	 * 
	 * @param filter a filter, see {@link NodeFilter}.
	 * @return an iterator over the child nodes.
	 */
	Iterator<DOMNode> iterator(NodeFilter filter);

	/**
	 * Creates a new iterator over the child nodes.
	 * <p>
	 * It only iterates over types set in the whatToShow mask that satisfy the
	 * custom <code>NodeFilter</code>.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Iterator<Node> it = node.iterator(NodeFilter.SHOW_ELEMENT, null);
	 * </pre>
	 * 
	 * @param whatToShow the bit field mask to apply to the node types, see
	 *                   {@link NodeFilter}. Do not confuse this argument with the
	 *                   <code>short</code> argument of the
	 *                   {@link #typeIterator(short)} method.
	 * @param filter     the filter to use in the iteration. if <code>null</code>,
	 *                   only the <code>whatToShow</code> mask filter is applied.
	 * @return an iterator over the child nodes.
	 */
	Iterator<DOMNode> iterator(int whatToShow, NodeFilter filter);

	/**
	 * Creates a new iterator over the child nodes.
	 * <p>
	 * Do not confuse the <code>typeToShow</code> argument with the <code>int</code>
	 * argument of the {@link #iterator(int, NodeFilter)} method, which is a bit field.
	 * 
	 * @param typeToShow the node type to show (from {@link Node#getNodeType()}).
	 * @return an iterator over the child nodes.
	 */
	Iterator<DOMNode> typeIterator(short typeToShow);

	/**
	 * Creates a new iterator over the child elements.
	 * 
	 * @return an iterator over the child elements.
	 */
	Iterator<DOMElement> elementIterator();

	/**
	 * Creates a new list iterator over the child nodes.
	 * 
	 * @return a list iterator over the child nodes.
	 */
	NodeListIterator listIterator();

	/**
	 * Gets a static <code>ElementList</code> of the descendant elements that match
	 * any of the specified list of selectors.
	 * <p>
	 * Unlike methods like {@link #getElementsByTagName(String)} or
	 * {@link #getElementsByClassName(String)}, this is not a live list but a static
	 * one, representing the state of the document when the method was called. If no
	 * elements match, the list shall be empty.
	 * 
	 * @param selectors a comma-separated list of selectors.
	 * @return an <code>ElementList</code> with the elements that match any of the
	 *         specified group of selectors.
	 */
	ElementList querySelectorAll(String selectors);

	/**
	 * Gives an <code>ElementList</code> of all the elements descending from this
	 * context node that have the given tag name, in document order.
	 * <p>
	 * The list is a live collection, and changes to the document made after calling
	 * this method are reflected in the <code>ElementList</code>.
	 * </p>
	 * <p>
	 * The most efficient way to browse the returned list is to iterate it.
	 * </p>
	 * 
	 * @param name The tag name of the elements to match on. The special value
	 *             "<code>*</code>" matches all tag names.
	 * @return the <code>ElementList</code> object containing all the matched
	 *         elements.
	 */
	ElementList getElementsByTagName(String name);

	/**
	 * Gives an <code>ElementList</code> of all the elements descending from this
	 * context node that have the given local name and namespace URI, in document
	 * order.
	 * <p>
	 * The list is a live collection, and changes to the document made after calling
	 * this method are reflected in the <code>ElementList</code>.
	 * </p>
	 * <p>
	 * The most efficient way to browse the returned list is to iterate it.
	 * </p>
	 * 
	 * @param namespaceURI the namespace URI of the elements to match on. The
	 *                     special value "<code>*</code>" matches all namespaces.
	 * @param localName    The local name of the elements to match on. The special
	 *                     value "<code>*</code>" matches all local names.
	 * @return the <code>ElementList</code> object containing all the matched
	 *         elements.
	 */
	ElementList getElementsByTagNameNS(String namespaceURI, String localName);

	/**
	 * Gives a live <code>ElementList</code> containing all the descending elements
	 * which have all of the given class names under this reference node.
	 * <p>
	 * The list is a live collection, and changes to the document made after calling
	 * this method are reflected in the <code>ElementList</code>.
	 * </p>
	 * <p>
	 * The most efficient way to browse the returned list is to iterate it.
	 * </p>
	 * 
	 * @param names the names of the classes, separated by whitespace.
	 * @return the live <code>ElementList</code> containing all the descending
	 *         elements which have all of the given class names under this reference
	 *         node.
	 */
	ElementList getElementsByClassName(String names);
}
