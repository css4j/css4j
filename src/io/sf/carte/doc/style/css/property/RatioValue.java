/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Objects;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.CSSRatioValue;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Ratio CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class RatioValue extends PrimitiveValue implements CSSRatioValue {

	private PrimitiveValue antecedentValue;
	private PrimitiveValue consequentValue;

	public RatioValue() {
		super();
		setCSSUnitType(CSSPrimitiveValue2.CSS_RATIO);
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
		ValueFactory vf = new ValueFactory();
		PrimitiveValue value = vf.parseMediaFeature(cssText);
		if (value.getPrimitiveType() != CSSPrimitiveValue2.CSS_RATIO) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Value is not a ratio.");
		}
		RatioValue ratio = (RatioValue) value;
		this.antecedentValue = ratio.antecedentValue;
		this.consequentValue = ratio.consequentValue;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This value needs special handling");
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

	private void checkValueType(CSSPrimitiveValue value) throws DOMException {
		if (value == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null value in ratio.");
		}
		short ptype = value.getPrimitiveType();
		if (ptype != CSSPrimitiveValue.CSS_NUMBER && ptype != CSSPrimitiveValue2.CSS_EXPRESSION) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected type in ratio: " + ptype);
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
