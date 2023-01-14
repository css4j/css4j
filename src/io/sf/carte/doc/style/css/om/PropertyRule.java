/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSPropertyRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS {@literal @}property rule implementation.
 * 
 * @author Carlos Amengual
 * 
 */
public class PropertyRule extends BaseCSSDeclarationRule implements CSSPropertyRule {

	private static final long serialVersionUID = 1L;

	private String name = null;

	public PropertyRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.PROPERTY_RULE, origin);
	}

	@Override
	PropertyDescriptorStyleDeclaration createStyleDeclaration(AbstractCSSStyleSheet parentSheet) {
		return new PropertyDescriptorStyleDeclaration(this);
	}

	/**
	 * Gets the (unescaped) custom property name.
	 * 
	 * @return the custom property name.
	 */
	@Override
	public String getName() {
		return name;
	}

	void setName(String name) throws DOMException {
		this.name = name;
	}

	@Override
	public boolean inherits() {
		String inherits = getStyle().getPropertyValue("inherits").toLowerCase(Locale.ROOT);
		return !inherits.equals("false");
	}

	@Override
	public LexicalValue getInitialValue() {
		return (LexicalValue) getStyle().getPropertyCSSValue("initial-value");
	}

	@Override
	public CSSValueSyntax getSyntax() {
		StyleValue cssVal = getStyle().getPropertyCSSValue("syntax");
		if (cssVal == null || cssVal.getPrimitiveType() != Type.STRING) {
			return null;
		}
		String s = ((CSSTypedValue) cssVal).getStringValue();
		CSSValueSyntax syn;
		try {
			syn = new SyntaxParser().parseSyntax(s);
		} catch (CSSException e) {
			return null;
		}
		return syn;
	}

	@Override
	public String getCssText() {
		PropertyDescriptorStyleDeclaration decl = (PropertyDescriptorStyleDeclaration) getStyle();
		if (decl.isValidDeclaration() && name != null) {
			StyleFormattingContext context = getStyleFormattingContext();
			context.setParentContext(getParentRule());
			BufferSimpleWriter sw = new BufferSimpleWriter(50 + getStyle().getLength() * 24);
			try {
				writeCssText(sw, context);
			} catch (IOException e) {
				throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
			}
			return sw.toString();
		}
		return "";
	}

	@Override
	public String getMinifiedCssText() {
		PropertyDescriptorStyleDeclaration decl = (PropertyDescriptorStyleDeclaration) getStyle();
		if (decl.isValidDeclaration() && name != null) {
			return "@property " + ParseHelper.escape(name) + " {" + getStyle().getMinifiedCssText() + '}';
		}
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		PropertyDescriptorStyleDeclaration decl = (PropertyDescriptorStyleDeclaration) getStyle();
		if (decl.isValidDeclaration() && name != null) {
			context.startRule(wri, getPrecedingComments());
			wri.write("@property ");
			wri.write(ParseHelper.escape(name));
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, getTrailingComments());
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int idx = cssText.indexOf('{');
		if (idx < 11) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad property rule: " + cssText);
		}
		try {
			super.setCssText(cssText);
		} catch (DOMException e) {
			clear();
			throw e;
		}
		PropertyDescriptorStyleDeclaration decl = (PropertyDescriptorStyleDeclaration) getStyle();
		if (!decl.isValidDeclaration()) {
			clear();
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad property rule: " + cssText);
		}
	}

	@Override
	void startAtRule(String name, String pseudoSelector) {
		if (!"property".equalsIgnoreCase(name)) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Cannot set rule of type: " + name);
		}
		if (pseudoSelector == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "No property name.");
		}
		pseudoSelector = ParseHelper.parseIdent(pseudoSelector);
		setName(pseudoSelector);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getType();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyRule other = (PropertyRule) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public PropertyRule clone(AbstractCSSStyleSheet parentSheet) {
		PropertyRule rule = new PropertyRule(parentSheet, getOrigin());
		rule.setName(getName());
		PropertyDescriptorStyleDeclaration cloneStyle = (PropertyDescriptorStyleDeclaration) rule.getStyle();
		cloneStyle.addStyle((BaseCSSStyleDeclaration) getStyle());
		return rule;
	}

}
