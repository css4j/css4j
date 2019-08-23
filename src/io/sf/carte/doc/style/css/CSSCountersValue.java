/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * A CSS <code>counters()</code> function.
 */
public interface CSSCountersValue extends CSSCounterValue {

	/**
	 * Get the separator.
	 * <p>
	 * If no separator was set, returns the empty string.
	 * 
	 * @return the separator.
	 */
	String getSeparator();

}
