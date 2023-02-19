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
 * Based on SAC's {@code CombinatorCondition} interface by Philippe Le Hegaret.
 * <p>
 * Represents two chained conditions in a compound selector.
 * </p>
 */
public interface CombinatorCondition extends Condition {
	/**
	 * The first condition.
	 * 
	 * @return the first condition.
	 */
	Condition getFirstCondition();

	/**
	 * The second condition.
	 * 
	 * @return the second condition.
	 */
	Condition getSecondCondition();

}
