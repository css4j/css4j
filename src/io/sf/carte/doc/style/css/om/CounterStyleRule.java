/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSCounterStyleRule;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS counter-style rule implementation.
 * 
 * @author Carlos Amengual
 * 
 */
public class CounterStyleRule extends BaseCSSDeclarationRule implements CSSCounterStyleRule {

	private String name = null;

	public CounterStyleRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, ExtendedCSSRule.COUNTER_STYLE_RULE, origin);
	}

	/**
	 * Gets the counter-style name.
	 * 
	 * @return the counter-style name.
	 */
	@Override
	public String getName() {
		return ParseHelper.escape(name);
	}

	void setName(String name) throws DOMException {
		name = ParseHelper.parseIdent(name);
		if (name == null || "none".equalsIgnoreCase(name) || "disc".equalsIgnoreCase(name)
				|| "decimal".equalsIgnoreCase(name) || "inside".equalsIgnoreCase(name)
				|| "outside".equalsIgnoreCase(name) || "inherit".equalsIgnoreCase(name)
				|| "unset".equalsIgnoreCase(name) || "initial".equalsIgnoreCase(name)) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad counter-style name: " + name);
		}
		this.name = name;
	}

	@Override
	public String getCssText() {
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

	@Override
	public String getMinifiedCssText() {
		if (name != null || getStyle().getLength() != 0) {
			return "@counter-style " + getName() + " {" + getStyle().getMinifiedCssText() + '}';
		}
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (name != null || getStyle().getLength() != 0) {
			context.startRule(wri);
			wri.write("@counter-style ");
			wri.write(getName());
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri);
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int idx = cssText.indexOf('{');
		if (idx < 16) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Bad counter-style rule: " + cssText);
		}
		if (!ParseHelper.startsWithIgnoreCase(cssText, "@counter-style")
				|| !Character.isWhitespace(cssText.charAt(14))) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a @counter-style rule: " + cssText);
		}
		super.setCssText(cssText);
		// All seems OK, so we set the name
		setName(cssText.substring(15, idx).trim());
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
		CounterStyleRule other = (CounterStyleRule) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public CounterStyleRule clone(AbstractCSSStyleSheet parentSheet) {
		CounterStyleRule rule = new CounterStyleRule(parentSheet, getOrigin());
		rule.setName(getName());
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		return rule;
	}

}
