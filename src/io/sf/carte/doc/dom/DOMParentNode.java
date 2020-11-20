/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.BitSet;
import java.util.Iterator;

abstract class DOMParentNode extends NDTNode implements ParentNode {

	private final ChildCollections child;

	public DOMParentNode(short nodeType) {
		super(nodeType);
		child = new DefaultChildNodeList();
	}

	@Override
	ChildCollections getNodeList() {
		return child;
	}

	/**
	 * Gets the live ElementList containing all nodes of type Element that are children of
	 * this Element.
	 * 
	 * @return the ElementList containing all nodes of type Element that are children of this
	 *         Element.
	 */
	@Override
	public ElementList getChildren() {
		return child.getChildren();
	}

	/**
	 * Gets a static list of the elements that match any of the specified group of
	 * selectors.
	 * <p>
	 * Unlike methods like {@link #getElementsByTagName(String)} or
	 * {@link #getElementsByClassName(String)}, this is not a live list but a static
	 * one, representing the state of the document when the method was called. If no
	 * elements match, the list will be empty.
	 * 
	 * @param selectors a comma-separated list of selectors.
	 * @return an ElementList with the elements that match any of the specified
	 *         group of selectors.
	 */
	@Override
	public ElementList querySelectorAll(String selectors) {
		return querySelectorAll(selectors, getFirstChild());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator() {
		return child.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> descendingIterator() {
		return child.createDescendingIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator(BitSet whatToShow) {
		return child.createIterator(whatToShow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMElement> elementIterator() {
		return child.elementIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator(int whatToShow, NodeFilter filter) {
		return child.createIterator(whatToShow, filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> typeIterator(short typeToShow) {
		return iterator(NodeFilter.maskTable[typeToShow - 1], null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<DOMNode> iterator(NodeFilter filter) {
		return child.createIterator(-1, filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeListIterator listIterator() {
		return child.createListIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementList getElementsByTagNameNS(String namespaceURI, String localName) {
		return child.getElementsByTagNameNS(namespaceURI, localName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementList getElementsByTagName(String name) {
		return child.getElementsByTagName(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementList getElementsByClassName(String names) {
		return child.getElementsByClassName(names, getOwnerDocument().getComplianceMode());
	}

}
