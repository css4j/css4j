/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

abstract class AbstractCondition implements Condition, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	AbstractCondition() {
		super();
	}

	Condition replace(SelectorList base, MutableBoolean replaced) {
		return this;
	}

	/**
	 * Perform a shallow cloning of this value.
	 * 
	 * @return the clone;
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

}
