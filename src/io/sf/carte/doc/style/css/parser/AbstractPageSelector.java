/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Objects;

import io.sf.carte.doc.style.css.nsac.PageSelector;

abstract class AbstractPageSelector implements PageSelector {

	private final String name;

	private PageSelector nextSelector = null;

	AbstractPageSelector(String name) {
		super();
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PageSelector getNext() {
		return nextSelector;
	}

	void setNextSelector(PageSelector nextSelector) {
		this.nextSelector = nextSelector;
	}

	@Override
	public String toString() {
		if (nextSelector == null) {
			return getCssText();
		}
		StringBuilder buf = new StringBuilder();
		buf.append(getCssText());
		PageSelector sel = nextSelector;
		do {
			buf.append(' ');
			buf.append(sel.getCssText());
			sel = sel.getNext();
		} while (sel != null);
		return buf.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSelectorType(), name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractPageSelector other = (AbstractPageSelector) obj;
		return Objects.equals(name, other.name) && getSelectorType() == other.getSelectorType();
	}

}
