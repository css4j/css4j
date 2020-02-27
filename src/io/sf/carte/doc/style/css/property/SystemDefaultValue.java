/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.util.SimpleWriter;

/**
 * When a system-dependent property must be set but there is no style database,
 * this value can be used.
 * 
 */
public class SystemDefaultValue extends PrimitiveValue {
	private static final SystemDefaultValue strictmode = new SystemDefaultValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	protected SystemDefaultValue() {
		super(CSSPrimitiveValue.CSS_UNKNOWN);
	}

	public static SystemDefaultValue getInstance() {
		return strictmode;
	}

	@Override
	public String getCssText() {
		return "initial";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getCssText());
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Attempt to modify a system-default value.");
	}

	@Override
	public boolean isSubproperty() {
		/*
		 * These are typically byproducts of a shorthand, unless it was produced as a
		 * consequence of an 'initial' declaration found during a computed-style cascade.
		 * In both cases, returning 'true' does the job.
		 */
		return true;
	}

	@Override
	public boolean isSystemDefault() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof SystemDefaultValue)) {
			return false;
		}
		SystemDefaultValue other = (SystemDefaultValue) obj;
		if (!getCssText().equals(other.getCssText())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getCssText().hashCode();
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return null;
	}

	@Override
	public SystemDefaultValue clone() {
		return this;
	}
}
