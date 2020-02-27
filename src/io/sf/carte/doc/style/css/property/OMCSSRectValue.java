/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.Rect;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Rect-specific CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
class OMCSSRectValue extends PrimitiveValue {

	private CSSRect rect = new CSSRect();

	OMCSSRectValue() {
		super(CSSPrimitiveValue.CSS_RECT);
	}

	protected OMCSSRectValue(OMCSSRectValue copied) {
		super(copied);
		this.rect = new CSSRect(copied.rect);
	}

	@Override
	public Rect getRectValue() throws DOMException {
		return rect;
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
			ExtendedCSSPrimitiveValue dimens = factory.createCSSPrimitiveValue(lu, false);
			rect.setTop(dimens);
			// comma
			lu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA) {
				lu = lu.getNextLexicalUnit();
				commaFound = true;
			}
			// right
			dimens = factory.createCSSPrimitiveValue(lu, false);
			rect.setRight(dimens);
			// comma
			lu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
				if (commaFound) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad syntax for rect.");
				}
			} else {
				if (!commaFound) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad syntax for rect.");
				}
				lu = lu.getNextLexicalUnit();
			}
			// bottom
			dimens = factory.createCSSPrimitiveValue(lu, false);
			rect.setBottom(dimens);
			// comma
			lu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA) {
				if (commaFound) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad syntax for rect.");
				}
			} else {
				lu = lu.getNextLexicalUnit();
			}
			// left
			dimens = factory.createCSSPrimitiveValue(lu, false);
			rect.setLeft(dimens);
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(32);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder(32);
		buf.append("rect(").append(rect.getTop().getCssText()).append(',').append(rect.getRight().getCssText())
				.append(',').append(rect.getBottom().getCssText()).append(',').append(rect.getLeft().getCssText())
				.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("rect(");
		rect.getTop().writeCssText(wri);
		wri.write(',');
		wri.write(' ');
		rect.getRight().writeCssText(wri);
		wri.write(',');
		wri.write(' ');
		rect.getBottom().writeCssText(wri);
		wri.write(',');
		wri.write(' ');
		rect.getLeft().writeCssText(wri);
		wri.write(')');
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((rect == null) ? 0 : rect.hashCode());
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
		OMCSSRectValue other = (OMCSSRectValue) obj;
		if (rect == null) {
			if (other.rect != null) {
				return false;
			}
		} else if (!rect.equals(other.rect)) {
			return false;
		}
		return true;
	}

	@Override
	public OMCSSRectValue clone() {
		return new OMCSSRectValue(this);
	}

	class CSSRect implements Rect {
		private ExtendedCSSPrimitiveValue top = null;
		private ExtendedCSSPrimitiveValue right = null;
		private ExtendedCSSPrimitiveValue bottom = null;
		private ExtendedCSSPrimitiveValue left = null;

		CSSRect() {
			super();
		}

		CSSRect(CSSRect copied) {
			super();
			this.left = copied.left;
			this.top = copied.top;
			this.right = copied.right;
			this.bottom = copied.bottom;
		}

		public void setTop(ExtendedCSSPrimitiveValue top) {
			this.top = top;
		}

		@Override
		public ExtendedCSSPrimitiveValue getTop() {
			return top;
		}

		public void setRight(ExtendedCSSPrimitiveValue right) {
			this.right = right;
		}

		@Override
		public ExtendedCSSPrimitiveValue getRight() {
			return right;
		}

		public void setBottom(ExtendedCSSPrimitiveValue bottom) {
			this.bottom = bottom;
		}

		@Override
		public ExtendedCSSPrimitiveValue getBottom() {
			return bottom;
		}

		public void setLeft(ExtendedCSSPrimitiveValue left) {
			this.left = left;
		}

		@Override
		public ExtendedCSSPrimitiveValue getLeft() {
			return left;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CSSRect other = (CSSRect) obj;
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
				if (other.top != null) {
					return false;
				}
			} else if (!top.equals(other.top)) {
				return false;
			}
			return true;
		}

	}
}
