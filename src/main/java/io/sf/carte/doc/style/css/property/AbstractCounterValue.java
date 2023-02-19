/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSCounterValue;

/**
 * Abstract class for counter() and counters() functions.
 * 
 * @author Carlos Amengual
 *
 */
abstract class AbstractCounterValue extends TypedValue implements CSSCounterValue {

	private static final long serialVersionUID = 1L;

	private String identifier;
	private PrimitiveValue listStyle = null;

	protected AbstractCounterValue(Type primitiveType) {
		super(primitiveType);
	}

	protected AbstractCounterValue(AbstractCounterValue copied) {
		super(copied);
		this.identifier = copied.identifier;
		this.listStyle = copied.listStyle;
		if (listStyle != null) {
			listStyle = listStyle.clone();
		}
	}

	@Override
	public String getName() {
		return identifier;
	}

	public void setName(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public PrimitiveValue getCounterStyle() {
		return listStyle;
	}

	public void setCounterStyle(PrimitiveValue listStyle) {
		this.listStyle = listStyle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((listStyle == null) ? decimalHashCode() : listStyle.hashCode());
		return result;
	}

	private int decimalHashCode() {
		int result = 31 * CssType.TYPED.hashCode() + Type.IDENT.hashCode();
		result = 31 * result + "decimal".hashCode();
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
		AbstractCounterValue other = (AbstractCounterValue) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		if (listStyle == null) {
			return other.listStyle == null || isCSSIdentifier(other.listStyle, "decimal");
		} else if (other.listStyle == null) {
			return isCSSIdentifier(listStyle, "decimal");
		}
		return listStyle.equals(other.listStyle);
	}

	static void quoteSeparator(String separator, StringBuilder buf) {
		if (!separator.contains("'")) {
			buf.append('\'').append(separator).append('\'');
		} else {
			buf.append('"').append(separator).append('"');
		}
	}

}
