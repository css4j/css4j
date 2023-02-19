/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSEnvVariableValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Environment variable (<code>env</code>).
 * 
 */
public class EnvVariableValue extends ProxyValue implements CSSEnvVariableValue {

	private static final long serialVersionUID = 1L;

	private String name = null;
	private StyleValue fallback = null;

	EnvVariableValue() {
		super(Type.ENV);
	}

	protected EnvVariableValue(EnvVariableValue copied) {
		super(copied);
		this.name = copied.name;
		if (copied.fallback != null) {
			this.fallback = copied.fallback.clone();
		}
	}

	@Override
	public String getCssText() {
		StringBuilder buf = new StringBuilder();
		buf.append("env(").append(name);
		if (fallback != null) {
			buf.append(", ").append(fallback.getCssText());
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder();
		buf.append("env(").append(name);
		if (fallback != null) {
			buf.append(',').append(fallback.getMinifiedCssText(propertyName));
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("env(");
		wri.write(name);
		if (fallback != null) {
			wri.write(", ");
			fallback.writeCssText(wri);
		}
		wri.write(')');
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		ValueFactory factory = new ValueFactory();
		StyleValue cssval = factory.parseProperty(cssText);
		if (cssval == null || cssval.getPrimitiveType() != Type.ENV) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not an environment variable value.");
		}
		EnvVariableValue env = (EnvVariableValue) cssval;
		this.name = env.getName();
		this.fallback = env.fallback;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fallback == null) ? 0 : fallback.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		EnvVariableValue other = (EnvVariableValue) obj;
		if (fallback == null) {
			if (other.fallback != null) {
				return false;
			}
		} else if (!fallback.equals(other.fallback)) {
			return false;
		}
		if (name == null) {
			return other.name == null;
		} else {
			return name.equals(other.name);
		}
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			if (lu == null || lu.getLexicalUnitType() != LexicalUnit.LexicalType.IDENT) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Variable name must be an identifier");
			}
			name = lu.getStringValue();
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				LexicalUnit.LexicalType type = lu.getLexicalUnitType();
				lu = lu.getNextLexicalUnit();
				if (type != LexicalUnit.LexicalType.OPERATOR_COMMA || lu == null) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Unexpected character");
				}
				ValueFactory factory = new ValueFactory();
				fallback = factory.createCSSValue(lu);
			}
			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public StyleValue getFallback() {
		return fallback;
	}

	@Override
	public EnvVariableValue clone() {
		return new EnvVariableValue(this);
	}

}
