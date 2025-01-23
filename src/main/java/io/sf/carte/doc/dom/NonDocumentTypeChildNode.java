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

/**
 * Based on W3C's <code>NonDocumentTypeChildNode</code> interface.
 */
public interface NonDocumentTypeChildNode extends DOMNode {

	/**
	 * Gets the first preceding sibling that is an element.
	 * 
	 * @return the first preceding sibling that is an element, and <code>null</code> otherwise.
	 */
	DOMElement getPreviousElementSibling();

	/**
	 * Gets the first following sibling that is an element.
	 * 
	 * @return the first following sibling that is an element, and <code>null</code> otherwise.
	 */
	DOMElement getNextElementSibling();

}
