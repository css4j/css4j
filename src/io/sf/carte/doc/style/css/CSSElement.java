/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * Essentially adds CSS-related methods to the DOM's {@link Element} interface.
 */
public interface CSSElement extends Element, CSSNode {

	/**
	 * Gets the computed style declaration that applies to this element.
	 *
	 * @param pseudoElt
	 *            the pseudo-element name (<code>null</code> if no pseudo-element).
	 * @return the computed style declaration.
	 */
	CSSComputedProperties getComputedStyle(String pseudoElt);

	/**
	 * Gets the <code>id</code> attribute of this element.
	 *
	 * @return the <code>id</code> attribute, or the empty string if has no ID.
	 */
	String getId();

	/**
	 * Gets this element's override style declaration for a pseudo-element.
	 * <p>
	 * The getOverrideStyle method provides a mechanism through which a DOM author could
	 * effect immediate change to the style of an element without modifying the explicitly
	 * linked style sheets of a document or the inline style of elements.
	 * </p>
	 * <p>
	 * The override style sheet comes after the author style sheet in the cascade algorithm.
	 * </p>
	 *
	 * @param pseudoElt
	 *            the pseudo-element condition, or <code>null</code> if none.
	 * @return the override style sheet for the given pseudo-element.
	 */
	CSSStyleDeclaration getOverrideStyle(Condition pseudoElt);

	/**
	 * Check whether this element has an override style declaration for the given
	 * pseudo-element.
	 * <p>
	 * This method allows checking for override styles without the overhead of
	 * producing and retrieving one with {@link #getOverrideStyle(Condition)} and
	 * checking its length.
	 *
	 * @param pseudoElt
	 *            the pseudo-element condition, or <code>null</code> if none.
	 * @return <code>true</code> if this element has an override style declaration for
	 *         <code>pseudoElt</code>.
	 */
	boolean hasOverrideStyle(Condition pseudoElt);

	/**
	 * The inline style.
	 *
	 * @return the inline style specified by the <code>style</code> attribute, or
	 *         <code>null</code> if that attribute is not present.
	 */
	CSSStyleDeclaration getStyle();

	/**
	 * Does this element (with the provided pseudo-element, if any) match the provided
	 * selector string ?
	 *
	 * @param selectorString
	 *            the selector string.
	 * @param pseudoElement
	 *            the pseudo-element, or null if none.
	 * @return <code>true</code> if the element would be selected by the specified selector string, false
	 *         otherwise.
	 * @throws DOMException
	 *             SYNTAX_ERR if there was an error parsing the selector string.
	 */
	boolean matches(String selectorString, String pseudoElement) throws DOMException;

	/**
	 * Does this element (with the provided pseudo-element, if any) match the provided
	 * selector list ?
	 *
	 * @param selist
	 *            the selector list.
	 * @param pseudoElement
	 *            the pseudo-element condition, or <code>null</code> if none.
	 * @return <code>true</code> if the element would be selected by at least one selector in the specified
	 *         list, <code>false</code> otherwise.
	 */
	boolean matches(SelectorList selist, Condition pseudoElement);

	/**
	 * Gets a selector matcher associated to this element.
	 *
	 * @return a selector matcher associated to this element.
	 */
	SelectorMatcher getSelectorMatcher();

	/**
	 * Check whether this element has non-CSS presentational hints.
	 * <p>
	 * A document may contain non-CSS presentational hints (like the <code>width</code>
	 * attribute in HTML). This method can return <code>true</code> only if this specific
	 * element do contain such hints, which can be exported to a style declaration by using
	 * the {@link #exportHintsToStyle(CSSStyleDeclaration)} method.
	 *
	 * @return <code>true</code> if this element has presentational hints.
	 */
	boolean hasPresentationalHints();

	/**
	 * Export this element's non-CSS presentational hints (if any) to the supplied
	 * {@link CSSStyleDeclaration}.
	 *
	 * @param style
	 *            the style declaration to export to.
	 */
	void exportHintsToStyle(CSSStyleDeclaration style) throws DOMException;
}
