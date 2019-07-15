/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSMarginRule;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSPageRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class PageRule extends CSSStyleDeclarationRule implements CSSPageRule, ExtendedCSSRule {

	LinkedList<CSSMarginRule> marginRules = null;

	public PageRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.PAGE_RULE, origin);
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(30 + getStyle().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		String seltext = getSelectorText();
		if (seltext.length() != 0 || getStyle().getLength() != 0 || marginRules != null) {
			StringBuilder buf = new StringBuilder(96);
			buf.append("@page");
			if (seltext.length() != 0) {
				buf.append(' ').append(seltext);
			}
			String styleText = getStyle().getMinifiedCssText();
			buf.append('{').append(styleText);
			if (marginRules != null) {
				if (styleText.length() != 0) {
					buf.append(';');
				}
				Iterator<CSSMarginRule> it = marginRules.iterator();
				while (it.hasNext()) {
					buf.append(((ExtendedCSSRule) it.next()).getMinifiedCssText());
				}
			}
			buf.append('}');
			return buf.toString();
		}
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		String seltext = getSelectorText();
		if (seltext.length() != 0 || getStyle().getLength() != 0 || marginRules != null) {
			context.startRule(wri);
			wri.write("@page");
			if (seltext.length() != 0) {
				wri.write(' ');
				wri.write(seltext);
			}
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			if (marginRules != null) {
				context.updateContext(this);
				Iterator<CSSMarginRule> it = marginRules.iterator();
				while (it.hasNext()) {
					ExtendedCSSRule rule = it.next();
					rule.writeCssText(wri, context);
				}
				context.endCurrentContext(this);
				context.endRuleList(wri);
			}
			context.writeRightCurlyBracket(wri);
			context.endRule(wri);
		}
	}

	/**
	 * Get the page type selector.
	 * 
	 * @return the page type selector.
	 */
	@Override
	public String getSelectorText() {
		return super.getSelectorText();
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int len = cssText.length();
		int idx = cssText.indexOf('{');
		if (len < 10 || idx == -1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @page rule: " + cssText);
		}
		if (!ParseHelper.startsWithIgnoreCase(cssText, "@page")) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a @page rule: " + cssText);
		}
		super.setCssText(cssText);
	}

	@Override
	protected PropertyDocumentHandler createDocumentHandler() {
		return new PageRuleDocumentHandler();
	}

	class PageRuleDocumentHandler extends RuleDocumentHandler {

		private MarginRule currentMarginRule = null;

		@Override
		public void startPage(String name, String pseudo_page) throws CSSException {
			if (name == null) {
				if (pseudo_page != null) {
					Parser parser = createSACParser();
					InputSource source = new InputSource(new StringReader(pseudo_page));
					try {
						setSelectorList(parser.parseSelectors(source));
					} catch (IOException e) {
					}
				} else {
					setSelectorText("");
				}
			} else {
				currentMarginRule = new MarginRule(getParentStyleSheet(), getOrigin(), name);
				currentMarginRule.setParentRule(PageRule.this);
				setLexicalPropertyListener(currentMarginRule.getLexicalPropertyListener());
			}
		}

		@Override
		public void endPage(String name, String pseudo_page) throws CSSException {
			if (name != null) {
				addMarginRule(currentMarginRule);
				currentMarginRule = null;
				setLexicalPropertyListener(getLexicalPropertyListener());
			}
		}

		@Override
		public void startAtRule(String name, String pseudoSelector) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Cannot set rule of type: " + name);
		}

	}

	void addMarginRule(CSSMarginRule marginRule) {
		if (marginRules == null) {
			marginRules = new LinkedList<CSSMarginRule>();
		}
		marginRules.add(marginRule);
	}

	public LinkedList<CSSMarginRule> getMarginRules() {
		return marginRules;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((marginRules == null) ? 0 : marginRules.hashCode());
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
		PageRule other = (PageRule) obj;
		if (marginRules == null) {
			if (other.marginRules != null) {
				return false;
			}
		} else if (!marginRules.equals(other.marginRules)) {
			return false;
		}
		return true;
	}

	@Override
	public PageRule clone(AbstractCSSStyleSheet parentSheet) {
		return (PageRule) super.clone(parentSheet);
	}

}
