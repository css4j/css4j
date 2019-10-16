/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * A page selector like <code>:first</code>.
 */
public interface PageSelector {

	enum Type {
		/**
		 * A page type identifier.
		 */
		PAGE_TYPE,

		/**
		 * <pre class="example">
		 * :left
		 * </pre>
		 *
		 */
		PSEUDO_PAGE
	}

	/**
	 * The type of <code>PageSelector</code>.
	 * 
	 * @return the type of page selector.
	 */
	Type getSelectorType();

	/**
	 * Get the name of the page type or pseudo-page.
	 * 
	 * @return the name of the page type or pseudo-page.
	 */
	String getName();

	/**
	 * Get the next pseudo-page, if any.
	 * 
	 * @return the next pseudo-page, or null if none.
	 */
	PageSelector getNext();

	/**
	 * Get a text representation of this page selector.
	 * 
	 * @return a text representation of this page selector.
	 */
	String getCssText();

}
