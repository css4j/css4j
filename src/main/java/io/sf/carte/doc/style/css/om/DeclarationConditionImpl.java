/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.DeclarationCondition;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class DeclarationConditionImpl extends BooleanConditionImpl.Predicate implements DeclarationCondition {

	private static final long serialVersionUID = 2L;

	private LexicalUnit value = null;

	public DeclarationConditionImpl(String propertyName) {
		super(propertyName);
	}

	@Override
	public LexicalUnit getValue() {
		return value;
	}

	/**
	 * Set the {@code @supports} condition property value.
	 * 
	 * @param value the value.
	 * @return {@code null} if the value was set correctly, or an exception if the
	 *         value is incompatible with the feature being tested with the
	 *         condition.
	 */
	@Override
	public void setValue(LexicalUnit value) throws DOMException {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getName().hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		DeclarationConditionImpl other = (DeclarationConditionImpl) obj;
		if (!getName().equals(other.getName())) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public void appendText(StringBuilder buf) {
		buf.append('(').append(getName()).append(": ");
		if (value != null) {
			buf.append(value.toString());
		}
		buf.append(')');
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		buf.append('(').append(getName()).append(':');
		if (value != null) {
			ValueFactory vf = new ValueFactory();
			StyleValue cssval;
			try {
				cssval = vf.createCSSValue(value);
				buf.append(cssval.getMinifiedCssText(getName()));
			} catch (Exception e) {
				buf.append(value.toString());
			}
		}
		buf.append(')');
	}

}
