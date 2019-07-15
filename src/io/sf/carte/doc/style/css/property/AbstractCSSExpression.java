/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSExpression;

/**
 * Abstract base class for CSS expressions.
 */
abstract public class AbstractCSSExpression implements CSSExpression {

	AbstractCSSExpression parent = null;
	boolean inverseOperation = false;
	transient boolean nextOperandInverse = false;

	AbstractCSSExpression() {
		super();
	}

	AbstractCSSExpression(AbstractCSSExpression copyFrom) {
		super();
		this.parent = copyFrom.parent;
		this.inverseOperation = copyFrom.inverseOperation;
	}

	@Override
	public AbstractCSSExpression getParentExpression() {
		return parent;
	}

	void replaceLastExpression(AbstractCSSExpression operation) {
		throw new IllegalStateException();
	}

	void setParentExpression(AbstractCSSExpression parent) {
		this.parent = parent;
	}

	abstract void addExpression(AbstractCSSExpression expr);

	void setInverseOperation(boolean inverse) {
		inverseOperation = inverse;
	}

	@Override
	public boolean isInverseOperation() {
		return inverseOperation;
	}

	@Override
	public int hashCode() {
		return inverseOperation ? 1231 : 1237;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(this instanceof AbstractCSSExpression))
			return false;
		AbstractCSSExpression other = (AbstractCSSExpression) obj;
		if (getPartType() != other.getPartType())
			return false;
		if (inverseOperation != other.inverseOperation)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (other.parent == null)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getCssText();
	}

	@Override
	abstract public AbstractCSSExpression clone();

	@Override
	abstract public String getCssText();

	@Override
	abstract public String getMinifiedCssText();

	@Override
	abstract public AlgebraicPart getPartType();

}
