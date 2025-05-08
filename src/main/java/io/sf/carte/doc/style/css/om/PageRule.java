/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSPageRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSPageRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class PageRule extends BaseCSSDeclarationRule implements CSSPageRule {

	private static final long serialVersionUID = 1L;

	private PageSelectorList selectorList = null;

	private MarginRuleList marginRules = null;

	public PageRule(AbstractCSSStyleSheet parentSheet, int origin) {
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
				for (MarginRule rule : marginRules) {
					buf.append(rule.getMinifiedCssText());
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
			context.startRule(wri, getPrecedingComments());
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
				for (MarginRule rule : marginRules) {
					rule.writeCssText(wri, context);
				}
				context.endCurrentContext(this);
				context.endRuleList(wri);
			}
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, getTrailingComments());
		}
	}

	public PageSelectorList getSelectorList() {
		return selectorList;
	}

	void setSelectorList(PageSelectorList selectorList) {
		this.selectorList = selectorList;
	}

	/**
	 * Get the page selector text.
	 * 
	 * @return the page selector text.
	 */
	@Override
	public String getSelectorText() {
		return selectorList == null ? "" : selectorList.toString();
	}

	@Override
	public void setSelectorText(String selectorText) throws DOMException {
		CSSParser parser = new CSSParser();
		selectorList = parser.parsePageSelectorList(selectorText);
	}

	void addMarginRule(MarginRule marginRule) {
		if (marginRules == null) {
			marginRules = new MarginRuleList(8);
		}
		marginRules.add(marginRule);
	}

	@Override
	public MarginRuleList getMarginRules() {
		return marginRules;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(marginRules, selectorList);
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
		return Objects.equals(marginRules, other.marginRules) && Objects.equals(selectorList, other.selectorList);
	}

	@Override
	public PageRule clone(AbstractCSSStyleSheet parentSheet) {
		PageRule clon = new PageRule(parentSheet, getOrigin());
		clon.selectorList = selectorList;
		if (this.marginRules != null) {
			clon.marginRules = new MarginRuleList(this.marginRules.size());
			Iterator<MarginRule> it = this.marginRules.iterator();
			while (it.hasNext()) {
				clon.marginRules.add(it.next().clone(parentSheet));
			}
		}
		String oldHrefContext = getParentStyleSheet().getHref();
		clon.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		return clon;
	}

}
