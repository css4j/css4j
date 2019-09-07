/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;

import io.sf.carte.doc.style.css.CSSCounterValue;

/**
 * Abstract class for counter() and counters() functions.
 * 
 * @author Carlos Amengual
 *
 */
abstract class AbstractCounterValue extends PrimitiveValue implements CSSCounterValue {

	private String identifier;
	private PrimitiveValue listStyle = null;

	protected AbstractCounterValue(short primitiveType) {
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
	public Counter getCounterValue() throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Obsolete API");
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
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((listStyle == null) ? decimalHashCode() : listStyle.hashCode());
		return result;
	}

	private int decimalHashCode() {
		int result = 31 * CSSValue.CSS_PRIMITIVE_VALUE + CSSPrimitiveValue.CSS_IDENT;
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
			if (other.listStyle != null && !isCSSIdentifier(other.listStyle, "decimal")) {
				return false;
			}
		} else if (other.listStyle == null) {
			return isCSSIdentifier(listStyle, "decimal");
		} else if (!listStyle.equals(other.listStyle)) {
			return false;
		}
		return true;
	}

	static void quoteSeparator(String separator, StringBuilder buf) {
		if (!separator.contains("'")) {
			buf.append('\'').append(separator).append('\'');
		} else {
			buf.append('"').append(separator).append('"');
		}
	}

}
