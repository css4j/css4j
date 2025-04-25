/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSNamespaceRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of W3C's CSSNamespaceRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class NamespaceRule extends BaseCSSRule implements CSSNamespaceRule {

	private static final long serialVersionUID = 1L;

	private String namespaceURI = null;

	private String prefix = null;

	protected NamespaceRule(AbstractCSSStyleSheet parentSheet, byte origin, String prefix, String namespaceURI) {
		super(parentSheet, CSSRule.NAMESPACE_RULE, origin);
		this.prefix = prefix;
		this.namespaceURI = namespaceURI;
	}

	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		AbstractCSSStyleSheet parentSheet = getParentStyleSheet();
		if (parentSheet != null) {
			parentSheet.unregisterNamespace(this.namespaceURI);
			parentSheet.registerNamespace(this);
			updateSelectorText(parentSheet.getCssRules());
		}
	}

	private void updateSelectorText(CSSRuleArrayList rules) {
		for (CSSRule rule : rules) {
			short type = rule.getType();
			if (type == CSSRule.STYLE_RULE) {
				((StyleRule) rule).updateSelectorText();
			} else if (rule instanceof GroupingRule) {
				updateSelectorText(((GroupingRule) rule).getCssRules());
			}
		}
	}

	@Override
	public String getCssText() {
		if (namespaceURI == null) {
			return null;
		}
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(namespaceURI.length() + 32);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (namespaceURI == null) {
			return;
		}
		wri.write("@namespace ");
		if (prefix != null) {
			wri.write(prefix);
			wri.write(' ');
		}
		context.writeURL(wri, namespaceURI);
		context.writeSemiColon(wri);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NamespaceRule other = (NamespaceRule) obj;
		if (namespaceURI == null) {
			if (other.namespaceURI != null) {
				return false;
			}
		} else if (!namespaceURI.equals(other.namespaceURI)) {
			return false;
		}
		if (prefix == null) {
			if (other.prefix != null) {
				return false;
			}
		} else if (!prefix.equals(other.prefix)) {
			return false;
		}
		return true;
	}

	@Override
	public NamespaceRule clone(AbstractCSSStyleSheet parentSheet) {
		return new NamespaceRule(parentSheet, getOrigin(), prefix, namespaceURI);
	}

}
