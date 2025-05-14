/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.util.SimpleWriter;

/**
 * Abstract base class for CSS expressions.
 */
abstract class StyleExpression implements CSSExpression, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	StyleExpression parent = null;
	boolean inverseOperation = false;
	transient boolean nextOperandInverse = false;

	StyleExpression() {
		super();
	}

	StyleExpression(StyleExpression copyFrom) {
		super();
		this.parent = copyFrom.parent;
		this.inverseOperation = copyFrom.inverseOperation;
	}

	@Override
	public StyleExpression getParentExpression() {
		return parent;
	}

	void replaceLastExpression(StyleExpression operation) {
		throw new IllegalStateException();
	}

	void setParentExpression(StyleExpression parent) {
		this.parent = parent;
	}

	abstract void addExpression(StyleExpression expr);

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
		if (!(this instanceof StyleExpression))
			return false;
		StyleExpression other = (StyleExpression) obj;
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
	abstract public StyleExpression clone();

	@Override
	abstract public String getCssText();

	@Override
	abstract public String getMinifiedCssText();

	/**
	 * Serialize this expression to a {@link SimpleWriter}.
	 * 
	 * @param wri
	 *            the SimpleWriter.
	 * @throws IOException
	 *            if an error happened while writing.
	 */
	@Override
	abstract public void writeCssText(SimpleWriter wri) throws IOException;

	@Override
	abstract public AlgebraicPart getPartType();

}
