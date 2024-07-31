/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019-2024, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.dom;

import org.w3c.dom.Node;

/**
 * Filter the nodes returned by an iterator, see
 * {@link ParentNode#iterator(NodeFilter)},
 * {@link ParentNode#iterator(int, NodeFilter)} and
 * {@link DOMDocument#createNodeIterator(Node, int, NodeFilter)}.
 * <p>
 * The {@link #acceptNode(Node)} method determines which nodes are accepted to
 * be returned by the iterator.
 * <p>
 * Based on the <a href=
 * "https://www.w3.org/TR/dom/#nodefilter"><code>NodeFilter</code>
 * callback interface in DOM Level 4</a>, but not identical.
 */
public interface NodeFilter {

	// Constants for acceptNode()

	/**
	 * Accept the node.
	 */
	short FILTER_ACCEPT = 1;

	/**
	 * Skip the node.
	 */
	short FILTER_SKIP_NODE = 3;

	/**
	 * Skip the node and its descendants.
	 */
	short FILTER_SKIP_NODE_CHILD = 2;

	// Constants for whatToShow

	int[] maskTable = { NodeFilter.SHOW_ELEMENT, NodeFilter.SHOW_ATTRIBUTE, NodeFilter.SHOW_TEXT,
			NodeFilter.SHOW_CDATA_SECTION, NodeFilter.SHOW_ENTITY_REFERENCE, 0x20, NodeFilter.SHOW_PROCESSING_INSTRUCTION,
			NodeFilter.SHOW_COMMENT, NodeFilter.SHOW_DOCUMENT, NodeFilter.SHOW_DOCUMENT_TYPE,
			NodeFilter.SHOW_DOCUMENT_FRAGMENT };

	int SHOW_ALL = -1;
	int SHOW_ELEMENT = 0x1;
	int SHOW_ATTRIBUTE = 0x2;
	int SHOW_TEXT = 0x4;
	int SHOW_CDATA_SECTION = 0x8;
	int SHOW_ENTITY_REFERENCE = 0x10; // historical
	int SHOW_PROCESSING_INSTRUCTION = 0x40;
	int SHOW_COMMENT = 0x80;
	int SHOW_DOCUMENT = 0x100;
	int SHOW_DOCUMENT_TYPE = 0x200;
	int SHOW_DOCUMENT_FRAGMENT = 0x400;

	/**
	 * Find whether the given node is to be returned (or not) by an iterator.
	 * 
	 * @param node the node to filter.
	 * @return <code>FILTER_ACCEPT</code>, <code>FILTER_SKIP_NODE</code> or
	 *         <code>FILTER_SKIP_NODE_CHILD</code>.
	 */
	short acceptNode(Node node);
}
