/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

/**
 * Handle CSS errors at the CSSDocument.
 * 
 * @see CSSDocument#getErrorHandler()
 */
public interface ErrorHandler {

	/**
	 * Check whether this handler has processed any policy errors.
	 * 
	 * @return <code>true</code> if this handler processed any policy errors.
	 */
	boolean hasPolicyErrors();

	/**
	 * Check whether this handler has processed computed style errors.
	 * <p>
	 * Presentational hint errors are included in this category, as they are found
	 * during style computation.
	 * 
	 * @return <code>true</code> if this handler processed computed style errors.
	 */
	boolean hasComputedStyleErrors();

	/**
	 * Check whether this handler has processed computed style errors for the given
	 * element.
	 * <p>
	 * Presentational hint errors are included in this category, as they are found
	 * during style computation.
	 * 
	 * @param element the element.
	 * @return <code>true</code> if this handler processed computed style errors.
	 */
	boolean hasComputedStyleErrors(CSSElement element);

	/**
	 * Check whether this handler has processed any media-related errors.
	 * 
	 * @return <code>true</code> if this handler processed any media-related errors.
	 */
	boolean hasMediaErrors();

	/**
	 * Check whether this handler has processed any I/O errors.
	 * <p>
	 * I/O errors are considered transient errors and do not show up in
	 * {@link #hasErrors()}.
	 * 
	 * @return <code>true</code> if this handler processed any I/O errors.
	 */
	boolean hasIOErrors();

	/**
	 * Check whether this handler has processed any non-transient error.
	 * <p>
	 * I/O errors are not taken into account by this method.
	 * 
	 * @return <code>true</code> if this handler processed any non-transient errors.
	 */
	boolean hasErrors();

	/**
	 * Check whether this handler has processed computed style warnings.
	 * 
	 * @return <code>true</code> if this handler processed computed style warnings.
	 */
	boolean hasComputedStyleWarnings();

	/**
	 * Check whether this handler has processed computed style warnings for the given
	 * element.
	 * 
	 * @param element the element.
	 * @return <code>true</code> if this handler processed computed style warnings.
	 */
	boolean hasComputedStyleWarnings(CSSElement element);

	/**
	 * Check whether this handler has processed any media-related warnings.
	 * 
	 * @return <code>true</code> if this handler processed any media-related warnings.
	 */
	boolean hasMediaWarnings();

	/**
	 * Check whether this handler has processed any warnings.
	 * 
	 * @return <code>true</code> if this handler processed any warnings.
	 */
	boolean hasWarnings();

	/**
	 * Report a policy error, including possible security issues.
	 * 
	 * @param node    the node that caused the policy violation, or where it was
	 *                detected.
	 * @param message a message describing the error.
	 */
	void policyError(Node node, String message);

	/**
	 * Report an error related to linked style, where it was not possible to create
	 * the style sheet linked from <code>node</code> due to an issue with the given
	 * node or other node in the document (as opposed to an issue with the linked
	 * style sheet itself).
	 * 
	 * @param ownerNode the node where the error happened.
	 * @param message   a message describing the error.
	 */
	void linkedStyleError(Node ownerNode, String message);

	/**
	 * Report a media query error.
	 * 
	 * @param ownerNode the node that ultimately owns the sheet or rule where the
	 *                  media query appears (even if it is inside a nested
	 *                  sheet/rule, the node responsible is the one that owns the
	 *                  top-most style sheet).
	 * @param exception the exception describing the error.
	 */
	void mediaQueryError(Node ownerNode, CSSMediaException exception);

	/**
	 * Report a media query warning.
	 * 
	 * @param ownerNode the node that ultimately owns the sheet or rule where the
	 *                  media query appears (even if it is inside a nested
	 *                  sheet/rule, the node responsible is the one that owns the
	 *                  top-most style sheet).
	 * @param exception the exception describing the issue.
	 */
	void mediaQueryWarning(Node ownerNode, CSSMediaException exception);

	/**
	 * Report an error associated to a <code>LinkStyle</code> style sheet (i.e.
	 * <code>LINK</code> or <code>STYLE</code> elements), that caused an exception
	 * to be thrown.
	 * 
	 * @param exception the exception describing the problem.
	 * @param sheet     the linked or embedded style sheet.
	 */
	void linkedSheetError(Exception exception, CSSStyleSheet sheet);

	/**
	 * A I/O error was produced when retrieving a resource while processing a rule,
	 * generally an {@literal @}import or {@literal @}font-face rule.
	 * 
	 * @param uri
	 *            the uri for the resource.
	 * @param exception the exception describing the problem.
	 * @deprecated
	 * @see #ioError(String, IOException)
	 */
	@Deprecated
	void ruleIOError(String uri, IOException exception);

	/**
	 * A I/O error was found when retrieving a resource while processing an
	 * attribute (usually {@code href}) or a rule, generally an {@literal @}import
	 * or {@literal @}font-face rule.
	 * 
	 * @param uri       the uri for the resource.
	 * @param exception the exception describing the problem.
	 */
	void ioError(String uri, IOException exception);

	/**
	 * Get the error handler for the given element's inline style.
	 * 
	 * @param owner
	 *            the element that owns the inline style.
	 * @return the style's error handler. Implementations are allowed to return
	 *         <code>null</code> if the element has no inline style.
	 */
	StyleDeclarationErrorHandler getInlineStyleErrorHandler(CSSElement owner);

	/**
	 * Report a problem with setting an inline style, that caused an exception to be
	 * thrown by lower-level parsing (SAC).
	 * <p>
	 * This method is triggered only in two (unlikely) cases:
	 * <ol>
	 * <li>If a problem appears when instantiating a SAC parser.</li>
	 * <li>When the SAC parser throws a <code>CSSException</code>.</li>
	 * </ol>
	 * <p>
	 * The NSAC parser does not throw a <code>CSSException</code> if there is an
	 * error handler set (and in this implementation there is always one), although
	 * other parsers may.
	 * 
	 * @param owner
	 *            the element owning the inline style.
	 * @param exception
	 *            the exception describing the problem.
	 * @param inlineStyle
	 *            the inline style.
	 */
	void inlineStyleError(CSSElement owner, Exception exception, String inlineStyle);

	/**
	 * Error found in computed style declaration.
	 * <p>
	 * This means that an error could only be found when processing the values for computing
	 * the style.
	 * 
	 * @param element
	 *            the element for which the style was computed.
	 * @param propertyName
	 *            the name of the property involved in error.
	 * @param exception
	 *            the exception describing the error.
	 */
	void computedStyleError(CSSElement element, String propertyName, CSSPropertyValueException exception);

	/**
	 * Error found in computed style declaration.
	 * <p>
	 * This means that an error could only be found when processing the values for computing
	 * the style.
	 * 
	 * @param element
	 *            the element for which the style was computed.
	 * @param propertyName
	 *            the property name.
	 * @param exception
	 *            the exception describing the error.
	 */
	void computedStyleWarning(CSSElement element, String propertyName, CSSPropertyValueException exception);

	/**
	 * While computing a style, an error was found when processing the presentational hints of
	 * an element.
	 * 
	 * @param elm
	 *            the element.
	 * @param exception
	 *            the exception describing the error found.
	 */
	void presentationalHintError(CSSElement elm, DOMException exception);

	/**
	 * Reset the error state about computed styles and presentational hints for the
	 * given element.
	 */
	void resetComputedStyleErrors(CSSElement elm);

	/**
	 * Reset the error state about computed styles and presentational hints.
	 */
	void resetComputedStyleErrors();

	/**
	 * Reset the error handler.
	 * <p>
	 * After calling this method, {@link #hasErrors()} and {@link #hasWarnings()} must return
	 * false;
	 */
	void reset();

}
