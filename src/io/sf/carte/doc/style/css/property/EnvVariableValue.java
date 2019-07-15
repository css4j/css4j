/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSEnvVariableValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Environment variable (<code>env</code>) CSSPrimitiveValue.
 * 
 */
public class EnvVariableValue extends AbstractCSSPrimitiveValue implements CSSEnvVariableValue {

	private String name = null;
	private AbstractCSSValue fallback = null;

	EnvVariableValue() {
		super(CSSPrimitiveValue2.CSS_ENV_VAR);
	}

	protected EnvVariableValue(EnvVariableValue copied) {
		super(copied);
		this.name = copied.name;
		this.fallback = copied.fallback;
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
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		ValueFactory factory = new ValueFactory();
		AbstractCSSValue cssval = factory.parseProperty(cssText);
		if (cssval == null || cssval.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE ||
				((CSSPrimitiveValue)cssval).getPrimitiveType() != CSSPrimitiveValue2.CSS_ENV_VAR) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not an environment variable value.");
		}
		EnvVariableValue env = (EnvVariableValue) cssval;
		this.name = env.getName();
		this.fallback = env.fallback;
		setPlainCssText(cssval.getCssText());
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
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();
			if (lu == null || lu.getLexicalUnitType() != LexicalUnit.SAC_IDENT) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Variable name must be an identifier");
			}
			name = lu.getStringValue();
			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				short type = lu.getLexicalUnitType();
				lu = lu.getNextLexicalUnit();
				if (type != LexicalUnit.SAC_OPERATOR_COMMA || lu == null) {
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
	public String getStringValue() {
		return name;
	}

	@Override
	public ExtendedCSSValue getFallback() {
		return fallback;
	}

	@Override
	public EnvVariableValue clone() {
		return new EnvVariableValue(this);
	}

}
