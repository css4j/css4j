/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.ListIterator;

import org.w3c.dom.Node;

/**
 * A <code>ListIterator</code> that has <code>Node</code> arguments but returns
 * <code>DOMNode</code> references.
 */
public interface NodeListIterator extends ListIterator<Node> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	DOMNode next();

	/**
	 * {@inheritDoc}
	 */
	@Override
	DOMNode previous();

}
