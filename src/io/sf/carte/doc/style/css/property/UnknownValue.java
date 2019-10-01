/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Unknown CSS primitive value.
 * 
 * @author Carlos Amengual
 *
 */
public class UnknownValue extends AbstractTextValue {

	private boolean priorityCompat = false;

	public UnknownValue() {
		super(CSS_UNKNOWN);
	}

	protected UnknownValue(UnknownValue copied) {
		super(copied);
		setCssText(copied.getCssText());
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		setPlainCssText(cssText);
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getCssText());
	}

	public boolean isPriorityCompat() {
		return priorityCompat;
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
		UnknownValue other = (UnknownValue) obj;
		if (!getCssText().equals(other.getCssText())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getCssText().hashCode();
		return result;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			nextLexicalUnit = lunit.getNextLexicalUnit();
			String text;
			switch (lunit.getLexicalUnitType()) {
			case LexicalUnit.SAC_OPERATOR_EXP:
				text = "^";
				break;
			case LexicalUnit.SAC_OPERATOR_GE:
				text = ">=";
				break;
			case LexicalUnit.SAC_OPERATOR_GT:
				text = ">";
				break;
			case LexicalUnit.SAC_OPERATOR_LE:
				text = "<=";
				break;
			case LexicalUnit.SAC_OPERATOR_LT:
				text = "<";
				break;
			case LexicalUnit.SAC_OPERATOR_MINUS:
				text = "-";
				break;
			case LexicalUnit.SAC_OPERATOR_MOD:
				text = "%";
				break;
			case LexicalUnit.SAC_OPERATOR_MULTIPLY:
				text = "*";
				break;
			case LexicalUnit.SAC_OPERATOR_PLUS:
				text = "+";
				break;
			case LexicalUnit.SAC_OPERATOR_TILDE:
				text = "~";
				break;
			case LexicalUnit.SAC_COMPAT_PRIO:
				priorityCompat = true;
			case LexicalUnit.SAC_COMPAT_IDENT:
				text = lunit.toString();
				nextLexicalUnit = null;
				break;
			default:
				text = guessCssText(lunit);
				if (text.length() == 0) {
					throw new DOMException(DOMException.SYNTAX_ERR,
							"Unsuitable value: " + lunit.toString());
				}
			}
			setPlainCssText(text);
		}
	}

	private String guessCssText(LexicalUnit lunit) {
		try {
			return lunit.getStringValue();
		} catch (DOMException e) {
		}
		String text = "";
		try {
			float fv = lunit.getFloatValue();
			text = Float.toString(fv);
		} catch (DOMException e1) {
			try {
				int iv = lunit.getIntegerValue();
				text = Integer.toString(iv);
			} catch (DOMException e2) {
			}
		}
		try {
			String unittext = lunit.getDimensionUnitText();
			if (unittext.length() != 0) {
				text += unittext;
			}
		} catch (DOMException e) {
		}
		return text;
	}

	@Override
	public UnknownValue clone() {
		return new UnknownValue(this);
	}

}
