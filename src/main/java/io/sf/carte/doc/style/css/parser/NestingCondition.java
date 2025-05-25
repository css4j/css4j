/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.SelectorList;

class NestingCondition extends AbstractCondition {

	private static final long serialVersionUID = 1L;

	private static final NestingCondition instance = new NestingCondition();

	private NestingCondition() {
		super();
	}

	public static NestingCondition getInstance() {
		return instance;
	}

	@Override
	AbstractCondition replace(SelectorList base, MutableBoolean replaced) {
		replaced.setTrueValue();
		SelectorArgumentConditionImpl is = new SelectorArgumentConditionImpl();
		is.arguments = base;
		is.setName("is");
		return is;
	}

	@Override
	public ConditionType getConditionType() {
		return ConditionType.NESTING;
	}

	@Override
	public int hashCode() {
		return ConditionType.NESTING.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return getClass() == obj.getClass();
	}

	@Override
	void serialize(StringBuilder buf) {
		buf.append('&');
	}

	@Override
	public String toString() {
		return "&";
	}

	@Override
	public NestingCondition clone() {
		return this;
	}

}
