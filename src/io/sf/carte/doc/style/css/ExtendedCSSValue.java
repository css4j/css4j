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

import java.io.IOException;

import org.w3c.dom.css.CSSValue;

import io.sf.carte.util.SimpleWriter;

/**
 * Extends the CSSValue interface to provide a couple serialization methods, and
 * extend <code>Cloneable</code>.
 * 
 * @author Carlos Amengual
 *
 */
public interface ExtendedCSSValue extends CSSValue, Cloneable {

	/**
	 * Creates and returns a copy of this value.
	 * 
	 * @return a clone of this value.
	 */
	public ExtendedCSSValue clone();

	/**
	 * Gives a minified version of the css text of the property, for the given property name.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @return the minified css text.
	 */
	public String getMinifiedCssText(String propertyName);

	/**
	 * Serialize this value to a {@link SimpleWriter}.
	 * 
	 * @param wri
	 *            the SimpleWriter.
	 * @throws IOException
	 *            if an error happened while writing.
	 */
	public void writeCssText(SimpleWriter wri) throws IOException;
}
