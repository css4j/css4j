/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.NamespacePrefixMap;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

abstract class AbstractSelector implements Selector, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Set a new namespace prefix map to be used in serialization.
	 * 
	 * @param map the namespace prefix map.
	 * @return {@code true} if the map was changed successfully.
	 */
	boolean setNamespacePrefixMap(NamespacePrefixMap map) {
		return false;
	}

	Selector replace(SelectorList base) {
		return this;
	}

	@Override
	public int hashCode() {
		return getSelectorType().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Selector))
			return false;
		Selector other = (Selector) obj;
		return getSelectorType() == other.getSelectorType();
	}

	/**
	 * Perform a shallow cloning of this value.
	 * 
	 * @return the clone;
	 */
	@Override
	public AbstractSelector clone() {
		try {
			return (AbstractSelector) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	abstract NSACSelectorFactory getSelectorFactory() throws IllegalStateException;

}
