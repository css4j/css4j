/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRatioValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Ratio CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class RatioValue extends AbstractCSSPrimitiveValue implements CSSRatioValue {

	private int antecedentValue;
	private int consequentValue;

	RatioValue() {
		super();
	}

	protected RatioValue(RatioValue copied) {
		super(copied);
		this.antecedentValue = copied.antecedentValue;
		this.consequentValue = copied.consequentValue;
	}

	@Override
	public String getCssText() {
		return antecedentValue + "/" + consequentValue;
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(antecedentValue);
		wri.write('/');
		wri.write(consequentValue);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This value is read-only");
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			super.setLexicalUnit(lunit);
			setAntecedentValue(lunit.getIntegerValue());
			LexicalUnit nlu = lunit.getNextLexicalUnit(); // '/'
			nlu = nlu.getNextLexicalUnit();
			setConsequentValue(nlu.getIntegerValue());
			this.nextLexicalUnit = nlu.getNextLexicalUnit();
		}
	}

	@Override
	public int getAntecedentValue() {
		return antecedentValue;
	}

	@Override
	public int getConsequentValue() {
		return consequentValue;
	}

	public void setAntecedentValue(int antecedentValue) {
		if (antecedentValue < 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Value must be positive");
		}
		this.antecedentValue = antecedentValue;
	}

	public void setConsequentValue(int consequentValue) {
		if (consequentValue < 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Value must be positive");
		}
		this.consequentValue = consequentValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + antecedentValue;
		result = prime * result + consequentValue;
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
		if (antecedentValue != other.antecedentValue) {
			return false;
		}
		if (consequentValue != other.consequentValue) {
			return false;
		}
		return true;
	}

	@Override
	public RatioValue clone() {
		return new RatioValue(this);
	}

}
