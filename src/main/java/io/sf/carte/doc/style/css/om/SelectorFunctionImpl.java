/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Objects;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.nsac.SelectorFunction;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SheetContext;

/**
 * A selector function condition.
 * <p>
 * See CSS Conditional Rules Module Level 4 for details.
 * </p>
 * <p>
 * This implementation supports a list of selectors as an argument to the
 * function, something that the specification currently does not have but is a
 * predictable future extension.
 * </p>
 */
class SelectorFunctionImpl extends BooleanConditionImpl implements SelectorFunction {

	private static final long serialVersionUID = 1L;

	private final SheetContext parentSheet;

	private final SelectorList selectors;

	/**
	 * Construct a new selector function condition.
	 * 
	 * @param parentSheet the parent style sheet context.
	 * @param selectors   the selectors.
	 */
	public SelectorFunctionImpl(SheetContext parentSheet, SelectorList selectors) {
		super();
		this.parentSheet = parentSheet;
		this.selectors = selectors;
	}

	/**
	 * The selectors.
	 * 
	 * @return the selectors.
	 */
	@Override
	public SelectorList getSelectors() {
		return selectors;
	}

	@Override
	public void addCondition(BooleanCondition subCondition) {
	}

	@Override
	public BooleanCondition replaceLast(BooleanCondition newCondition) {
		return this;
	}

	@Override
	public void appendText(StringBuilder buf) {
		SelectorSerializer serializer = new SelectorSerializer(parentSheet);
		buf.append("selector(");
		serializer.selectorListText(buf, selectors, false, false);
		buf.append(')');
	}

	@Override
	public int hashCode() {
		return Objects.hash(selectors);
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
		SelectorFunctionImpl other = (SelectorFunctionImpl) obj;
		return Objects.equals(selectors, other.selectors);
	}

}
