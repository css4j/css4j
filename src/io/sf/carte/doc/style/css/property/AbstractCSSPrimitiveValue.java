/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.Rect;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;

/**
 * Base implementation for CSS primitive values.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class AbstractCSSPrimitiveValue extends AbstractCSSValue implements ExtendedCSSPrimitiveValue {

	private short primitiveType = CSSPrimitiveValue.CSS_UNKNOWN;

	private boolean subproperty = false;

	protected AbstractCSSPrimitiveValue(short unitType) {
		super(CSSValue.CSS_PRIMITIVE_VALUE);
		primitiveType = unitType;
	}

	protected AbstractCSSPrimitiveValue() {
		this(CSSPrimitiveValue.CSS_UNKNOWN);
	}

	protected AbstractCSSPrimitiveValue(AbstractCSSPrimitiveValue copied) {
		this(copied.primitiveType);
		this.subproperty = copied.subproperty;
	}

	@Override
	public short getPrimitiveType() {
		return primitiveType;
	}

	@Override
	public void setFloatValue(short unitType, float floatValue) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a Float");
	}

	/**
	 * This method is used to get a float value in a specified unit. If this CSS
	 * value doesn't contain a float value or can't be converted into the
	 * specified unit, a <code>DOMException</code> is raised.
	 * 
	 * @param unitType
	 *            A unit code to get the float value. The unit code can only be
	 *            a float unit type (i.e. <code>CSS_NUMBER</code>,
	 *            <code>CSS_PERCENTAGE</code>, <code>CSS_EMS</code>,
	 *            <code>CSS_EXS</code>, <code>CSS_PX</code>,
	 *            <code>CSS_CM</code>, <code>CSS_MM</code>, <code>CSS_IN</code>,
	 *            <code>CSS_PT</code>, <code>CSS_PC</code>,
	 *            <code>CSS_DEG</code>, <code>CSS_RAD</code>,
	 *            <code>CSS_GRAD</code>, <code>CSS_MS</code>,
	 *            <code>CSS_S</code>, <code>CSS_HZ</code>, <code>CSS_KHZ</code>,
	 *            <code>CSS_DIMENSION</code>).
	 * @return The float value in the specified unit.
	 * @exception DOMException
	 *                INVALID_ACCESS_ERR: Raised if the CSS value doesn't
	 *                contain a float value or if the float value can't be
	 *                converted into the specified unit.
	 */
	@Override
	public float getFloatValue(short unitType) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a Float");
	}

	@Override
	public void setStringValue(short stringType, String stringValue) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a String");
	}

	@Override
	public String getStringValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a String");
	}

	@Override
	public Counter getCounterValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a Counter");
	}

	@Override
	public Rect getRectValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not a Rect");
	}

	@Override
	public RGBAColor getRGBColorValue() throws DOMException {
		if (getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			String ident = getStringValue().toLowerCase(Locale.US);
			String spec;
			if ("transparent".equals(ident)) {
				spec = "rgba(0,0,0,0)";
			} else {
				spec = ColorIdentifiers.getInstance().getColor(ident);
			}
			if (spec != null) {
				ValueFactory factory = new ValueFactory();
				try {
					CSSValue val = factory.parseProperty(spec);
					if (val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
							&& ((AbstractCSSPrimitiveValue) val).getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR) {
						return ((AbstractCSSPrimitiveValue) val).getRGBColorValue();
					}
				} catch (DOMException e) {
				}
			}
		}
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Not an RGB Color");
	}

	public void setSubproperty(boolean subp) {
		subproperty = subp;
	}

	@Override
	public boolean isSubproperty() {
		return subproperty;
	}

	void setCSSUnitType(short cssUnitType) {
		this.primitiveType = cssUnitType;
	}

	boolean isNegativeNumber() {
		return false;
	}

	/**
	 * Is this value a number set to a value of zero, or an absolute value less than 1e-5 ?
	 * 
	 * @return <code>true</code> if this is a number and is set to zero (or equivalently small value).
	 */
	@Override
	public boolean isNumberZero() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getPrimitiveType();
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
		if (!(obj instanceof AbstractCSSPrimitiveValue)) {
			return false;
		}
		AbstractCSSPrimitiveValue other = (AbstractCSSPrimitiveValue) obj;
		if (getPrimitiveType() != other.getPrimitiveType()) {
			return false;
		}
		return true;
	}

	class LexicalSetter implements ValueItem {

		LexicalUnit nextLexicalUnit = null;

		private LinkedList<String> syntaxWarnings = null;

		LexicalSetter() {
			super();
		}

		/**
		 * Set this value according to the given lexical unit.
		 * 
		 * @param lunit
		 *            the given lexical unit.
		 * @throws DOMException
		 *             if an error was encountered setting the value.
		 */
		void setLexicalUnit(LexicalUnit lunit)
				throws DOMException {
			setCSSUnitType(ValueFactory.domPrimitiveType(lunit));
		}
	
		@Override
		public LexicalUnit getNextLexicalUnit() {
			return nextLexicalUnit;
		}

		@Override
		public AbstractCSSPrimitiveValue getCSSValue() {
			return AbstractCSSPrimitiveValue.this;
		}

		void reportSyntaxWarning(String message) {
			if (syntaxWarnings == null) {
				syntaxWarnings  = new LinkedList<String>();
			}
			syntaxWarnings.add(message);
		}

		@Override
		public boolean hasWarnings() {
			return syntaxWarnings != null;
		}

		@Override
		public void handleSyntaxWarnings(StyleDeclarationErrorHandler handler) {
			if (syntaxWarnings != null) {
				Iterator<String> it = syntaxWarnings.iterator();
				while (it.hasNext()) {
					handler.syntaxWarning(it.next());
				}
			}
		}

		@Override
		public String toString() {
			return getCssText();
		}
	}

	abstract LexicalSetter newLexicalSetter();

	/**
	 * Creates and returns a copy of this object.
	 * <p>
	 * The object will be the same except for the <code>subproperty</code> flag,
	 * that will be disabled in the clone object.
	 * </p>
	 * 
	 * @return a copy of this object.
	 */
	@Override
	abstract public AbstractCSSPrimitiveValue clone();

}
