/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2017-2023, Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.Node;

/**
 * A node that is related to a CSSDocument.
 * <p>
 * Note that nodes in a valid CSSDocument may or may not implement this interface,
 * <i>i.e.</i> you should not expect all nodes to implement this.
 */
public interface CSSNode extends Node {

	/**
	 * {@inheritDoc}
	 */
	@Override
	CSSDocument getOwnerDocument();
}
