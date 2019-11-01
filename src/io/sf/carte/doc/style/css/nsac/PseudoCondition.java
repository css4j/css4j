/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * A <code>Condition</code> for a pseudo-class or pseudo-element.
 */
public interface PseudoCondition extends Condition {

	/**
	 * The name of this pseudo-class or pseudo-element.
	 *
	 * @return the name of this pseudo-class or pseudo-element.
	 */
	String getName();

	/**
	 * If this condition represents a pseudo-class with an argument (in
	 * parentheses), it returns the argument.
	 *
	 * @return the argument of the pseudo-class.
	 */
	String getArgument();

}
