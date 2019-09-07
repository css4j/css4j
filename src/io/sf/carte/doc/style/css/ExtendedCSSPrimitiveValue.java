/*
 * This software includes material derived from Document Object Model (DOM)
 * Level 2 Style Specification (https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * Copyright © 2005-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */

package io.sf.carte.doc.style.css;

/**
 * Extends the CSSValue interface to provide a couple serialization methods.
 *
 * @author Carlos Amengual
 *
 */
public interface ExtendedCSSPrimitiveValue extends ExtendedCSSValue, CSSPrimitiveValue2 {

	/**
	 * Is this value a number set to a value of zero ?
	 *
	 * @return <code>true</code> if this is a number and is set to zero.
	 */
	boolean isNumberZero();
}
