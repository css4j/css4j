/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2017-2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */

package io.sf.carte.doc.style.css;

/**
 * A CSS custom property value ('variable').
 */
public interface CSSCustomPropertyValue extends ExtendedCSSPrimitiveValue {

	/**
	 * Get the name of this custom property.
	 * 
	 * @return the name of this custom property.
	 */
	String getName();

	/**
	 * Get the fallback value.
	 * 
	 * @return the fallback value, or <code>null</code> if there is no fallback.
	 */
	ExtendedCSSValue getFallback();

}