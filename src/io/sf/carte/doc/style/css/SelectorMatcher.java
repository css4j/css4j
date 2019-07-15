/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

/**
 * CSS Selector matcher.
 * <p>
 * The classes implementing this interface contain the logic for elements to match
 * selectors.
 * <p>
 * Early versions of this library were designed to provide computed styles for both DOM and
 * non-DOM implementations. The idea is that if an object implementing a SelectorMatcher can
 * be built, the infrastructure to compute styles could be directly used. With today's
 * library, providing computed styles for a DOM-like document that does not implement DOM
 * would not be that straightforward, but should still be possible.
 * <p>
 * This interface is a remnant of the early design where the SelectorMatcher was the pivot
 * for all the computed style infrastructure, providing universal support for any document
 * backend; it also packs two <code>matches</code> methods inside it. Modern DOM versions
 * (and subsequently also this library's {@link CSSElement}) directly have
 * <code>matches</code> methods, which make the {@link CSSElement#getSelectorMatcher()}
 * method a bit superfluous, although it is being kept for now.
 * 
 * @author Carlos Amengual
 * 
 */
public interface SelectorMatcher {

	/**
	 * Get the pseudo-element that this matcher will use to match selectors.
	 * 
	 * @return the pseudo-element, or null if no pseudo-element will be used to
	 *         match selectors.
	 */
	public String getPseudoElement();

	/**
	 * Set this selector's pseudo-element.
	 * 
	 * @param pseudoElt
	 *            the pseudo-element, or <code>null</code> if none.
	 */
	public void setPseudoElement(String pseudoElt);

	/**
	 * Does this selector match the given selector list?
	 * 
	 * @param selist
	 *            the list of selectors to which this matcher will compare.
	 * 
	 * @return the index of the highest matching selector, or -1 if none
	 *         matches.
	 */
	public int matches(SelectorList selist);

	/**
	 * Does this matcher match the given selector?
	 * 
	 * @param selector
	 *            the selector to be tested.
	 * 
	 * @return <code>true</code> if the given selector matches this object, <code>false</code> otherwise.
	 */
	public boolean matches(Selector selector);
}
