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
 * List of selectors.
 * <p>
 * SAC's {@code SelectorList} interface by Philippe Le Hegaret.
 * </p>
 */
public interface SelectorList {

	/**
	 * Get the length of this selector list.
	 * 
	 * @return the length of this selector list, zero if empty.
	 */
	int getLength();

	/**
	 * Get the selector at the specified index.
	 * 
	 * @return the selector at the specified index, or <code>null</code> if the
	 *         index is not valid.
	 */
	Selector item(int index);

}
