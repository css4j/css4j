/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

class CounterRef {

	public CounterRef() {
		super();
	}


	private static final int MAX_RECURSION = 512;

	// Recursion counter
	private int counter = 0;

	// Counter for replaceBy()
	int replaceCounter = 0;

	boolean increment() {
		counter++;
		if (isInRange()) {
			return true;
		}
		// Give a small margin for further operations
		counter -= 8;
		return false;
	}

	private boolean isInRange() {
		return counter < MAX_RECURSION;
	}

}
