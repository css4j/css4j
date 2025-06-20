/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.CSSRectValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Rect function.
 * 
 */
public class RectValue extends ShapeValue implements CSSRectValue {

	private static final long serialVersionUID = 1L;

	private TypedValue top = null;
	private TypedValue right = null;
	private TypedValue bottom = null;
	private TypedValue left = null;

	RectValue() {
		super(Type.RECT);
	}

	protected RectValue(RectValue copied) {
		super(copied);
		this.left = copied.left;
		this.top = copied.top;
		this.right = copied.right;
		this.bottom = copied.bottom;
	}

	public void setTop(TypedValue top) {
		if (top == null) {
			throw new NullPointerException();
		}
		this.top = top;
	}

	@Override
	public TypedValue getTop() {
		return top;
	}

	public void setRight(TypedValue right) {
		if (right == null) {
			throw new NullPointerException();
		}
		this.right = right;
	}

	@Override
	public TypedValue getRight() {
		return right;
	}

	public void setBottom(TypedValue bottom) {
		if (bottom == null) {
			throw new NullPointerException();
		}
		this.bottom = bottom;
	}

	@Override
	public TypedValue getBottom() {
		return bottom;
	}

	public void setLeft(TypedValue left) {
		if (left == null) {
			throw new NullPointerException();
		}
		this.left = left;
	}

	@Override
	public TypedValue getLeft() {
		return left;
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		switch (index) {
		case 0:
			return top;
		case 1:
			return right;
		case 2:
			return bottom;
		case 3:
			return left;
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		if (component == null) {
			throw new NullPointerException();
		}
		TypedValue primi = (TypedValue) component;
		switch (index) {
		case 0:
			top = primi;
			break;
		case 1:
			right = primi;
			break;
		case 2:
			bottom = primi;
			break;
		case 3:
			left = primi;
		}
	}

	@Override
	public int getComponentCount() {
		return 4;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			ValueFactory factory = new ValueFactory();
			boolean commaFound = false;
			LexicalUnit lu = lunit.getParameters();
			// top
			checkProxyValue(lu);
			TypedValue dimens = (TypedValue) factory.createCSSPrimitiveValue(lu, true);
			setTop(dimens);
			// comma
			lu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() == LexicalUnit.LexicalType.OPERATOR_COMMA) {
				lu = lu.getNextLexicalUnit();
				commaFound = true;
			}
			// right
			checkProxyValue(lu);
			dimens = (TypedValue) factory.createCSSPrimitiveValue(lu, true);
			setRight(dimens);
			// comma
			lu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_COMMA) {
				if (commaFound) {
					throw new DOMSyntaxException("Syntax mismatch for rect.");
				}
			} else {
				if (!commaFound) {
					throw new DOMSyntaxException("Syntax mismatch for rect.");
				}
				lu = lu.getNextLexicalUnit();
			}
			// bottom
			checkProxyValue(lu);
			dimens = (TypedValue) factory.createCSSPrimitiveValue(lu, true);
			setBottom(dimens);
			// comma
			lu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_COMMA) {
				if (commaFound) {
					throw new DOMSyntaxException("Syntax mismatch for rect.");
				}
			} else {
				lu = lu.getNextLexicalUnit();
			}
			// left
			checkProxyValue(lu);
			dimens = (TypedValue) factory.createCSSPrimitiveValue(lu, true);
			setLeft(dimens);
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder(32);
		buf.append("rect(").append(getTop().getCssText()).append(',').append(getRight().getCssText())
				.append(',').append(getBottom().getCssText()).append(',').append(getLeft().getCssText())
				.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("rect(");
		getTop().writeCssText(wri);
		wri.write(',');
		wri.write(' ');
		getRight().writeCssText(wri);
		wri.write(',');
		wri.write(' ');
		getBottom().writeCssText(wri);
		wri.write(',');
		wri.write(' ');
		getLeft().writeCssText(wri);
		wri.write(')');
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bottom == null) ? 0 : bottom.hashCode());
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		result = prime * result + ((top == null) ? 0 : top.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RectValue other = (RectValue) obj;
		if (bottom == null) {
			if (other.bottom != null) {
				return false;
			}
		} else if (!bottom.equals(other.bottom)) {
			return false;
		}
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		if (top == null) {
			return other.top == null;
		} else {
			return top.equals(other.top);
		}
	}

	@Override
	public RectValue clone() {
		return new RectValue(this);
	}

}
