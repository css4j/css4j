/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValueItem;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

/**
 * Base implementation for primitive values.
 * 
 */
abstract public class PrimitiveValue extends StyleValue implements CSSPrimitiveValue {

	private static final long serialVersionUID = 1L;

	private final Type primitiveType;

	private StringList precedingComments = null;

	private StringList trailingComments = null;

	private boolean subproperty = false;

	protected PrimitiveValue(Type unitType) {
		super();
		primitiveType = unitType;
	}

	protected PrimitiveValue(PrimitiveValue copied) {
		super();
		this.subproperty = copied.subproperty;
		this.primitiveType = copied.primitiveType;
		this.precedingComments = copied.precedingComments;
		this.trailingComments = copied.trailingComments;
	}

	@Override
	public Type getPrimitiveType() {
		return primitiveType;
	}

	@Override
	public boolean isPrimitiveValue() {
		return true;
	}

	@Override
	public void setExpectInteger() throws DOMException {
		throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
				"Expected an integer, found type " + getPrimitiveType());
	}

	public void setSubproperty(boolean subp) {
		subproperty = subp;
	}

	@Override
	public boolean isSubproperty() {
		return subproperty;
	}

	void checkModifiableProperty() throws DOMException {
		if (isSubproperty() || isReadOnly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was either set as a shorthand or as part of a more complex property. Must modify at a higher level (possibly at the style-declaration).");
		}
	}

	@Override
	public StringList getPrecedingComments() {
		return precedingComments;
	}

	void setPrecedingComments(StringList precedingComments) {
		this.precedingComments = precedingComments;
	}

	@Override
	public StringList getTrailingComments() {
		return trailingComments;
	}

	void setTrailingComments(StringList trailingComments) {
		this.trailingComments = trailingComments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + primitiveType.hashCode();
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
		if (!(obj instanceof PrimitiveValue)) {
			return false;
		}
		// Comments aren't taken into account
		PrimitiveValue other = (PrimitiveValue) obj;
		return primitiveType == other.primitiveType;
	}

	class LexicalSetter implements ValueItem, CSSPrimitiveValueItem {

		LexicalUnit nextLexicalUnit = null;

		private LinkedList<String> syntaxWarnings = null;

		LexicalSetter() {
			super();
		}

		/**
		 * Set this value according to the given lexical unit.
		 * 
		 * @param lunit the given lexical unit.
		 * @throws DOMException          if an error was encountered setting the value.
		 * @throws IllegalStateException if the lexical unit does not match the
		 *                               primitive (not all values check explicitly for
		 *                               this).
		 */
		void setLexicalUnit(LexicalUnit lunit)
				throws DOMException {
		}

		@Override
		public LexicalUnit getNextLexicalUnit() {
			return nextLexicalUnit;
		}

		@Override
		public PrimitiveValue getCSSValue() {
			return PrimitiveValue.this;
		}

		void reportSyntaxWarning(String message) {
			if (syntaxWarnings == null) {
				syntaxWarnings = new LinkedList<>();
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

		boolean checkProxyValue(LexicalUnit lunit) {
			LexicalType type = lunit.getLexicalUnitType();
			if (type == LexicalType.VAR || type == LexicalType.ATTR) {
				throw new CSSLexicalProcessingException("Cannot handle this PROXY in this value.");
			}
			return false;
		}

		@Override
		public String toString() {
			return getCssText();
		}
	}

	LexicalSetter newLexicalSetter() {
		return null;
	}

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
	abstract public PrimitiveValue clone();

}
