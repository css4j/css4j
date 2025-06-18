/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.style.css.CSSTypedValue;

/**
 * Base implementation for CSS typed values.
 * 
 */
abstract public class TypedValue extends PrimitiveValue implements CSSTypedValue {

	private static final long serialVersionUID = 1L;

	protected TypedValue(Type unitType) {
		super(unitType);
	}

	@Override
	public CssType getCssValueType() {
		return CssType.TYPED;
	}

	protected TypedValue(TypedValue copied) {
		super(copied);
	}

	@Override
	public float getFloatValue(short unitType) throws DOMException {
		throw new DOMInvalidAccessException("Not a Float");
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

	static boolean isCSSIdentifier(PrimitiveValue value, String ident) {
		return value.getPrimitiveType() == Type.IDENT
				&& ident.equalsIgnoreCase(((CSSTypedValue) value).getStringValue());
	}

	/**
	 * Get the component at {@code index}.
	 * <p>
	 * This method allows to access the components regardless of them being indexed
	 * or not. It is convenient to perform common tasks at the components (like when
	 * computing values).
	 * </p>
	 * 
	 * @param index the index. For colors, index {@code 0} is always the alpha
	 *              channel.
	 * @return the component, or {@code null} if the index is incorrect.
	 */
	public StyleValue getComponent(int index) {
		return null;
	}

	/**
	 * If this value has components, set the component at {@code index}.
	 * <p>
	 * This method allows to access the components regardless of them being formally
	 * indexed or not. It is convenient to perform common tasks at the components
	 * (like when computing values).
	 * </p>
	 * 
	 * @param index     the index. For colors, index {@code 0} is always the alpha
	 *                  channel. Setting a component at an index that does not exist
	 *                  has no effect.
	 * @param component the new component. Cannot be a {@code KEYWORD} nor a
	 *                  {@code SHORTHAND}. For colors, must be a primitive value
	 *                  (that is, either
	 *                  {@link io.sf.carte.doc.style.css.CSSValue.CssType#TYPED
	 *                  TYPED} or a
	 *                  {@link io.sf.carte.doc.style.css.CSSValue.CssType#PROXY
	 *                  PROXY}).
	 * @throws DOMException         TYPE_MISMATCH_ERR if the value is of the wrong
	 *                              type.
	 * @throws NullPointerException if the index is valid but the {@code component}
	 *                              cannot be {@code null}.
	 */
	public void setComponent(int index, StyleValue component) throws DOMException {
	}

	/**
	 * Get the number of components of this value.
	 * 
	 * @return the number of components, {@code 0} if none.
	 */
	public int getComponentCount() {
		return 0;
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
	abstract public TypedValue clone();

}
