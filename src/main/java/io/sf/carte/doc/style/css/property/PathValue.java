/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSPathValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.SimpleWriter;

/**
 * Path function.
 * 
 */
class PathValue extends ShapeValue implements CSSPathValue {

	private static final long serialVersionUID = 1L;

	private CSSTypedValue fillRule = null;

	private String path;

	PathValue() {
		super(Type.PATH);
	}

	protected PathValue(PathValue copied) {
		super(copied);
		if (copied.fillRule != null) {
			this.fillRule = copied.fillRule.clone();
		}
		this.path = copied.path;
	}

	public void setFillRule(CSSTypedValue fillRule) {
		if (fillRule == null) {
			throw new NullPointerException();
		}
		this.fillRule = fillRule;
	}

	@Override
	public CSSTypedValue getFillRule() {
		return fillRule;
	}

	public void setPath(String path) {
		if (path == null) {
			throw new NullPointerException();
		}
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getStringValue() throws DOMException {
		return path;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			LexicalUnit lu = lunit.getParameters();

			checkProxyValue(lu);
			switch (lu.getLexicalUnitType()) {
			case IDENT:
				ValueFactory factory = new ValueFactory();
				CSSTypedValue fillRule = (CSSTypedValue) factory.createCSSPrimitiveValue(lu, true);
				setFillRule(fillRule);
				// comma
				lu = lu.getNextLexicalUnit();
				if (lu == null || (!checkProxyValue(lu)
						&& lu.getLexicalUnitType() != LexicalUnit.LexicalType.OPERATOR_COMMA)) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Expected comma in path().");
				}
				lu = lu.getNextLexicalUnit();
				checkProxyValue(lu);
				if (lu.getLexicalUnitType() != LexicalType.STRING) {
					throw new DOMException(DOMException.SYNTAX_ERR, "No path in path().");
				}
			case STRING:
				setPath(lu.getStringValue());
				break;
			default:
				throw createUnexpectedArgumentTypeException(lu);
			}

			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	private DOMException createUnexpectedArgumentTypeException(LexicalUnit lu) {
		return new DOMException(DOMException.TYPE_MISMATCH_ERR,
				"Unexpected argument in path(): " + lu.getCssText());
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder(32);
		buf.append("path(");
		if (fillRule != null) {
			buf.append(fillRule.getCssText()).append(',');
		}
		buf.append(ParseHelper.quote(path, '\''));
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("path(");
		if (fillRule != null) {
			fillRule.writeCssText(wri);
			wri.write(',');
			wri.write(' ');
		}
		wri.write(ParseHelper.quote(path, '\''));
		wri.write(')');
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fillRule, path);
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
		PathValue other = (PathValue) obj;
		return Objects.equals(fillRule, other.fillRule) && Objects.equals(path, other.path);
	}

	@Override
	public PathValue clone() {
		return new PathValue(this);
	}

}
