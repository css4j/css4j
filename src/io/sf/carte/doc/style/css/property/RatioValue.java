/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRatioValue;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Ratio value.
 * 
 * @author Carlos Amengual
 *
 */
public class RatioValue extends TypedValue implements CSSRatioValue {

	private PrimitiveValue antecedentValue;
	private PrimitiveValue consequentValue;

	public RatioValue() {
		super(Type.RATIO);
	}

	protected RatioValue(RatioValue copied) {
		super(copied);
		this.antecedentValue = copied.antecedentValue.clone();
		this.consequentValue = copied.consequentValue.clone();
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(32);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		antecedentValue.writeCssText(wri);
		wri.write('/');
		consequentValue.writeCssText(wri);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		ValueFactory vf = new ValueFactory();
		PrimitiveValue value = vf.parseMediaFeature(cssText);
		if (value.getPrimitiveType() != Type.RATIO) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Value is not a ratio.");
		}
		RatioValue ratio = (RatioValue) value;
		this.antecedentValue = ratio.antecedentValue;
		this.consequentValue = ratio.consequentValue;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit nextUnit) {
			nextLexicalUnit = nextUnit;
		}
	}

	@Override
	public PrimitiveValue getAntecedentValue() {
		return antecedentValue;
	}

	@Override
	public PrimitiveValue getConsequentValue() {
		return consequentValue;
	}

	/**
	 * Set the first value in the ratio.
	 * 
	 * @param antecedentValue the first value.
	 * @throws DOMException if the value is <code>null</code> or of the wrong type.
	 */
	public void setAntecedentValue(PrimitiveValue antecedentValue) throws DOMException {
		checkValueType(antecedentValue);
		this.antecedentValue = antecedentValue;
	}

	/**
	 * Set the second value in the ratio.
	 * 
	 * @param consequentValue the second value.
	 * @throws DOMException if the value is <code>null</code> or of the wrong type.
	 */
	public void setConsequentValue(PrimitiveValue consequentValue) throws DOMException {
		checkValueType(consequentValue);
		this.consequentValue = consequentValue;
	}

	private void checkValueType(PrimitiveValue value) throws DOMException {
		if (value == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null value in ratio.");
		}
		CssType cat = value.getCssValueType();
		Type ptype = value.getPrimitiveType();
		if (cat != CssType.PROXY && (cat != CssType.TYPED || (ptype != Type.NUMERIC && ptype != Type.EXPRESSION))) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected type in ratio: " + ptype);
		}
	}

	@Override
	public PrimitiveValue getComponent(int index) {
		switch (index) {
		case 0:
			return antecedentValue;
		case 1:
			return consequentValue;
		}
		return null;
	}

	@Override
	public void setComponent(int index, StyleValue component) {
		if (component == null) {
			throw new NullPointerException();
		}
		PrimitiveValue primi = (PrimitiveValue) component;
		switch (index) {
		case 0:
			antecedentValue = primi;
			break;
		case 1:
			consequentValue = primi;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(antecedentValue, consequentValue);
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
		RatioValue other = (RatioValue) obj;
		return Objects.equals(antecedentValue, other.antecedentValue)
				&& Objects.equals(consequentValue, other.consequentValue);
	}

	@Override
	public RatioValue clone() {
		return new RatioValue(this);
	}

}
