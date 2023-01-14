/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

/**
 * Base implementation for CSS values.
 * 
 */
abstract public class StyleValue implements CSSValue, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private transient boolean readOnly = false;

	protected StyleValue() {
		super();
	}

	/**
	 * Attempts to change this value to match the supplied css text.
	 * <p>
	 * In css4j, it is not recommended to set property values using this method.
	 * 
	 * @exception DOMException
	 *                SYNTAX_ERR: Raised if the specified CSS string value has a
	 *                syntax error (according to the attached property) or is
	 *                unparsable. <br>
	 * 				INVALID_MODIFICATION_ERR: Raised if the specified CSS
	 *                string value represents a different type of values than
	 *                the values allowed by the CSS property. <br>
	 *                NO_MODIFICATION_ALLOWED_ERR: Raised if this value is
	 *                read-only.
	 */
	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"This property can only be modified at the style declaration level.");
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return getCssText();
	}

	/**
	 * Check whether this value is primitive, that is, either a
	 * {@link io.sf.carte.doc.style.css.CSSValue.CssType#TYPED TYPED} or
	 * {@link io.sf.carte.doc.style.css.CSSValue.CssType#PROXY PROXY} value.
	 * 
	 * @return {@code true} if the value is {@code TYPED} or {@code PROXY}.
	 */
	public boolean isPrimitiveValue() {
		return false;
	}

	/**
	 * Is this a subproperty that has been set by a shorthand?
	 * 
	 * @return <code>true</code> if this a subproperty that has been set by a shorthand,
	 *         <code>false</code> otherwise.
	 */
	public boolean isSubproperty() {
		return false;
	}

	/**
	 * Is this value a wildcard for a system-dependent default?
	 * <p>
	 * If that is the case, it cannot be used to compute values.
	 * 
	 * @return <code>true</code> if this is a placeholder for a system-dependent default,
	 *         <code>false</code> otherwise.
	 */
	public boolean isSystemDefault() {
		return false;
	}

	/**
	 * Verify if this value matches the given grammar.
	 * 
	 * @param syntax the syntax.
	 * @return the matching for the syntax.
	 */
	@Override
	public Match matches(CSSValueSyntax syntax) {
		if (syntax != null) {
			if (syntax.getCategory() == Category.universal) {
				return Match.TRUE;
			}
			do {
				Match result;
				if ((result = matchesComponent(syntax)) != Match.FALSE) {
					return result;
				}
				syntax = syntax.getNext();
			} while (syntax != null);
		}
		return Match.FALSE;
	}

	/**
	 * Match a syntax component.
	 * <p>
	 * Although the {@link #matches(CSSValueSyntax)} method should filter calls to
	 * match the universal syntax {@code *}, implementations of this method must
	 * deal with that syntax again, because list implementations do call this method
	 * directly.
	 * </p>
	 * 
	 * @param syntax the syntax component.
	 * @return the match.
	 */
	Match matchesComponent(CSSValueSyntax syntax) {
		return Match.FALSE;
	}

	boolean isReadOnly() {
		return readOnly;
	}

	void setReadOnly() {
		this.readOnly = true;
	}

	@Override
	public int hashCode() {
		return getCssValueType().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StyleValue)) {
			return false;
		}
		StyleValue other = (StyleValue) obj;
		return getCssValueType() == other.getCssValueType();
	}

	@Override
	public String toString() {
		return getCssText();
	}

	/**
	 * Get a string representation of the current value.
	 * 
	 * @return the css text representing the value of this property.
	 */
	@Override
	abstract public String getCssText();

	@Override
	abstract public StyleValue clone();

}
