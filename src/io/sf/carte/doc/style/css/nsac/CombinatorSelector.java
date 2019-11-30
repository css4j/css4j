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
 * Combinator selector.
 * <p>
 * This interface is used by selectors like:
 * </p>
 * <ul>
 * <li>{@link Selector#CHILD}</li>
 * <li>{@link Selector#DESCENDANT}</li>
 * <li>{@link Selector#SUBSEQUENT_SIBLING}</li>
 * <li>{@link Selector#DIRECT_ADJACENT}</li>
 */
public interface CombinatorSelector extends Selector {

	/**
	 * Get the first selector.
	 * 
	 * @return the first selector.
	 */
	Selector getSelector();

	/**
	 * Get the second selector.
	 * 
	 * @return the second selector.
	 */
	SimpleSelector getSecondSelector();

}
