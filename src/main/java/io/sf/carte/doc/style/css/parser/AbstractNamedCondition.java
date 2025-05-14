/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Objects;

abstract class AbstractNamedCondition extends AbstractCondition {

	private static final long serialVersionUID = 1L;

	String name = null;

	AbstractNamedCondition() {
		super();
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AbstractNamedCondition)) {
			return false;
		}
		AbstractNamedCondition other = (AbstractNamedCondition) obj;
		return Objects.equals(name, other.name);
	}

	/**
	 * Perform a shallow cloning of this value.
	 * 
	 * @return the clone;
	 */
	@Override
	public Object clone() {
		AbstractNamedCondition clon = (AbstractNamedCondition) super.clone();
		clon.name = name;
		return clon;
	}

}
