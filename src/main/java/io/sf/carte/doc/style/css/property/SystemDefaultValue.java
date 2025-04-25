/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.util.SimpleWriter;

/**
 * When a system-dependent property must be set but there is no style database,
 * this value can be used.
 * 
 */
public class SystemDefaultValue extends TypedValue {

	private static final long serialVersionUID = 1L;

	private static final SystemDefaultValue strictmode = new SystemDefaultValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	protected SystemDefaultValue() {
		super(Type.UNKNOWN);
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
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StyleValue)) {
			return false;
		}

		StyleValue other = (StyleValue) obj;

		// Assume they are being compared because the property is the same.
		return other.isSystemDefault() || other.getPrimitiveType() == Type.INITIAL;
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
