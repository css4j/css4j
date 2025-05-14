/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSEnvVariableValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.impl.CSSUtil;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Environment variable (<code>env</code>).
 * 
 */
public class EnvVariableValue extends ProxyValue implements CSSEnvVariableValue {

	private static final long serialVersionUID = 1L;

	private String name = null;

	private int[] indices = null;

	private LexicalUnit fallback = null;

	EnvVariableValue() {
		super(Type.ENV);
	}

	protected EnvVariableValue(EnvVariableValue copied) {
		super(copied);
		this.name = copied.name;
		if (copied.indices != null) {
			this.indices = copied.indices.clone();
		}
		if (copied.fallback != null) {
			this.fallback = copied.fallback.clone();
		}
	}

	@Override
	public String getCssText() {
		if (name == null) {
			return "";
		}
		BufferSimpleWriter sw = new BufferSimpleWriter(42);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder();
		buf.append("env(").append(name);
		if (indices != null) {
			for (int index : indices) {
				buf.append(' ');
				buf.append(Integer.toString(index));
			}
		}
		if (fallback != null) {
			buf.append(',').append(LexicalValue.serializeMinifiedSequence(fallback));
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("env(");
		wri.write(name);
		if (indices != null) {
			for (int index : indices) {
				wri.write(' ');
				wri.write(Integer.toString(index));
			}
		}
		if (fallback != null) {
			wri.write(", ");
			wri.write(fallback.toString());
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
		result = prime * result + Arrays.hashCode(indices);
		result = prime * result + Objects.hash(fallback, name);
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
		return name.equals(other.name) && Arrays.equals(indices, other.indices)
				&& Objects.equals(fallback, other.fallback);
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
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Variable name must be an identifier");
			}

			name = lu.getStringValue();
			indices = null;

			LinkedList<Integer> indexList = null;

			do {
				lu = lu.getNextLexicalUnit();
				if (lu == null) {
					fallback = null;
					break;
				}
				LexicalType type = lu.getLexicalUnitType();
				if (type == LexicalType.OPERATOR_COMMA) {
					fallback = lu.getNextLexicalUnit();
					if (fallback != null) {
						fallback = fallback.clone();
					}
					break;
				} else if (type == LexicalType.INTEGER) {
					if (indexList == null) {
						indexList = new LinkedList<>();
					}
					indexList.add(lu.getIntegerValue());
				} else {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Unexpected token: " + lu.getCssText());
				}
			} while (true);

			if (indexList != null) {
				indices = new int[indexList.size()];
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indexList.get(i);
				}
			}

			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Get the indices.
	 * 
	 * @return the indices, or {@code null} if none.
	 */
	@Override
	public int[] getIndices() {
		return indices;
	}

	@Override
	public LexicalUnit getFallback() {
		return fallback;
	}

	@Override
	public Match matches(CSSValueSyntax syntax) {
		CSSValueSyntax rootSyntax = syntax;
		if (syntax != null) {
			if (syntax.getCategory() == Category.universal) {
				return Match.TRUE;
			}
			do {
				Match result;
				if ((result = CSSUtil.matchEnv(rootSyntax, syntax, name,
						fallback)) != Match.FALSE) {
					return result;
				}
				syntax = syntax.getNext();
			} while (syntax != null);
		}
		return Match.FALSE;
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		return CSSUtil.matchEnv(syntax, syntax, name, fallback);
	}

	@Override
	public EnvVariableValue clone() {
		return new EnvVariableValue(this);
	}

}
