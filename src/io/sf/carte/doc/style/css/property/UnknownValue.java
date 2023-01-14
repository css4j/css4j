/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.util.SimpleWriter;

/**
 * Unknown CSS primitive value.
 * 
 * @author Carlos Amengual
 *
 */
public class UnknownValue extends AbstractTextValue {

	private static final long serialVersionUID = 1L;

	private boolean priorityCompat = false;

	public UnknownValue() {
		super(Type.UNKNOWN);
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
	public Match matches(CSSValueSyntax syntax) {
		if (syntax != null && syntax.getCategory() == Category.universal) {
			return Match.PENDING;
		}
		return Match.FALSE;
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
		return getCssText().equals(other.getCssText());
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
			case OPERATOR_EXP:
				text = "^";
				break;
			case OPERATOR_GE:
				text = ">=";
				break;
			case OPERATOR_GT:
				text = ">";
				break;
			case OPERATOR_LE:
				text = "<=";
				break;
			case OPERATOR_LT:
				text = "<";
				break;
			case OPERATOR_MINUS:
				text = "-";
				break;
			case OPERATOR_MOD:
				text = "%";
				break;
			case OPERATOR_MULTIPLY:
				text = "*";
				break;
			case OPERATOR_PLUS:
				text = "+";
				break;
			case OPERATOR_TILDE:
				text = "~";
				break;
			case COMPAT_PRIO:
				priorityCompat = true;
			case COMPAT_IDENT:
				text = lunit.toString();
				nextLexicalUnit = null;
				break;
			default:
				throw new DOMException(DOMException.SYNTAX_ERR,
						"Unsuitable value: " + lunit.toString());
			}
			setPlainCssText(text);
		}
	}

	@Override
	public UnknownValue clone() {
		return new UnknownValue(this);
	}

}
