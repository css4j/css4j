/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.ExtendedCSSValue;

/**
 * Base implementation for CSS values.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class StyleValue implements ExtendedCSSValue, Cloneable {

	private final short valueType;

	private String cssText = null;

	protected StyleValue(short valueType) {
		super();
		this.valueType = valueType;
	}

	protected StyleValue(StyleValue copied) {
		this(copied.valueType);
		this.cssText = copied.cssText;
	}

	/**
	 * Get a string representation of the current value.
	 * 
	 * @return the css text representing the value of this property.
	 */
	@Override
	public String getCssText() {
		return cssText;
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
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		setPlainCssText(cssText);
	}

	void setPlainCssText(String cssText) {
		this.cssText = cssText;
	}

	/**
	 * Gives a code defining the type of the value as defined by <code><a href=
	 * "http://www.w3.org/2003/01/dom2-javadoc/org/w3c/dom/css/CSSValue.html">CSSValue</a>
	 *  </code>.
	 * 
	 * @return the value type according to CSS DOM Level 2.
	 */
	@Override
	public short getCssValueType() {
		return valueType;
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return getCssText();
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

	@Override
	public int hashCode() {
		return valueType;
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
		if (valueType != other.valueType) {
			return false;
		}
		return true;
	}

	@Override
	abstract public StyleValue clone();

	@Override
	public String toString() {
		return getCssText();
	}

}
