/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Handle CSS errors at the CSSDocument.
 * 
 * @see CSSDocument#getErrorHandler()
 */
public interface ErrorHandler {

	/**
	 * Check whether this handler has processed any errors.
	 * 
	 * @return <code>true</code> if this handler processed any errors.
	 */
	boolean hasErrors();

	/**
	 * Check whether this handler has processed any warnings.
	 * 
	 * @return <code>true</code> if this handler processed any warnings.
	 */
	boolean hasWarnings();

	/**
	 * Report an error related to linked style, where it was not possible to create the style
	 * sheet linked from <code>node</code> due to an issue with the given node or other node
	 * in the document (as opposed to an issue with the linked style sheet itself).
	 * 
	 * @param node
	 * @param message
	 */
	void linkedStyleError(Node node, String message);

	/**
	 * Report a warning related to linked style, where it was not possible to create the style
	 * sheet linked from <code>node</code> due to an issue with the given node or other node
	 * in the document (as opposed to an issue with the linked style sheet itself).
	 * 
	 * @param node
	 * @param message
	 */
	void linkedStyleWarning(Node node, String message);

	void mediaQueryError(Node ownerNode, String media);

	void linkedSheetError(Exception e, CSSStyleSheet sheet);

	/**
	 * Get the error handler for the given element's inline style.
	 * 
	 * @param owner
	 *            the element that owns the inline style.
	 * @return the style's error handler. Implementations are allowed to return
	 *         <code>null</code> if the element has no inline style.
	 */
	StyleDeclarationErrorHandler getInlineStyleErrorHandler(CSSElement owner);

	void inlineStyleError(CSSElement owner, Exception e, String inlineStyle);

	/**
	 * Reset the error handler.
	 * <p>
	 * After calling this method, {@link #hasErrors()} and {@link #hasWarnings()} must return
	 * false;
	 */
	void reset();
}
