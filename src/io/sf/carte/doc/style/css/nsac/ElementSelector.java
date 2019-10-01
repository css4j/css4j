/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * Element selector, created by Philippe Le Hegaret
 */
public interface ElementSelector extends SimpleSelector {
	/**
	 * Returns the <a href="http://www.w3.org/TR/REC-xml-names/#dt-NSName">namespace
	 * URI</a> of this element selector.
	 * 
	 * @return the namespace of this element selector, or <code>null</code> if this
	 *         element selector can match any namespace.
	 */
	String getNamespaceURI();

	/**
	 * Returns the <a href="http://www.w3.org/TR/REC-xml-names/#NT-LocalPart">local
	 * part</a> of the
	 * <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified name</a>
	 * of this element.
	 * 
	 * @return the local name of the element that is matched by this selector, or
	 *         <code>null</code> if this element selector can match any element.
	 */
	String getLocalName();

}
