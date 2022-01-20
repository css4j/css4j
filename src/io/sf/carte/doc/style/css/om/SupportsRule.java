/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSSupportsRule;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.BooleanCondition;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.DeclarationCondition;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS supports rule.
 * 
 * @author Carlos Amengual
 * 
 */
public class SupportsRule extends GroupingRule implements CSSSupportsRule {

	private BooleanCondition condition = null;

	protected SupportsRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, SUPPORTS_RULE, origin);
	}

	SupportsRule(AbstractCSSStyleSheet parentSheet, SupportsRule copyfrom) {
		super(parentSheet, copyfrom);
		condition = copyfrom.getCondition();
	}

	@Override
	public String getConditionText() {
		return condition != null ? condition.toString() : "";
	}

	@Override
	public void setConditionText(String conditionText) throws DOMException {
		parseConditionText(conditionText);
	}

	/**
	 * Parse the condition text.
	 * 
	 * @param conditionText
	 *            the condition text.
	 * @param rule
	 *            the rule that would process the error. if null, a problem while parsing
	 *            shall result in an exception.
	 */
	private void parseConditionText(String conditionText) throws DOMException {
		CSSParser parser;
		try {
			parser = (CSSParser) createSACParser();
		} catch (ClassCastException e) {
			parser = new CSSParser();
		}
		try {
			condition = parser.parseSupportsCondition(conditionText, null);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public BooleanCondition getCondition() {
		return condition;
	}

	@Override
	public boolean supports(StyleDatabase styleDatabase) {
		return supports(condition, styleDatabase);
	}

	private boolean supports(BooleanCondition condition, StyleDatabase styleDatabase) {
		switch (condition.getType()) {
		case PREDICATE:
			DeclarationCondition declCond = (DeclarationCondition) condition;
			return declCond.isParsable() && styleDatabase.supports(declCond.getName(), declCond.getValue());
		case AND:
			Iterator<BooleanCondition> it = condition.getSubConditions()
					.iterator();
			while (it.hasNext()) {
				if (!supports(it.next(), styleDatabase)) {
					return false;
				}
			}
			return true;
		case NOT:
			return supports(condition.getNestedCondition(), styleDatabase);
		case OR:
			it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				if (supports(it.next(), styleDatabase)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(50 + getCssRules().getLength() * 32);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		if (condition != null || !getCssRules().isEmpty()) {
			StringBuilder sb = new StringBuilder(30 + getCssRules().getLength() * 20);
			sb.append("@supports");
			if (condition != null) {
				condition.appendMinifiedText(sb);
			}
			sb.append('{');
			Iterator<AbstractCSSRule> it = getCssRules().iterator();
			while (it.hasNext()) {
				sb.append(it.next().getMinifiedCssText());
			}
			sb.append('}');
			return sb.toString();
		}
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (condition != null || !getCssRules().isEmpty()) {
			context.startRule(wri);
			wri.write("@supports ");
			wri.write(getConditionText());
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			getCssRules().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endRuleList(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri);
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int lm1 = cssText.length() - 1;
		int idx = cssText.indexOf('{');
		if (idx == -1 || lm1 < 16 || cssText.charAt(lm1) != '}') {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @supports rule: " + cssText);
		}
		CharSequence atkeyword = cssText.subSequence(0, 9);
		if (!ParseHelper.startsWithIgnoreCase(atkeyword, "@supports")) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a @supports rule: " + cssText);
		}
		// Parse the internal rules
		AbstractCSSStyleSheet parentSS = getParentStyleSheet();
		if (parentSS == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "This rule must be added to a sheet first");
		}
		// Create sheet & parse text
		AbstractCSSStyleSheet css = parentSS.getStyleSheetFactory().createRuleStyleSheet(this,
				null, null);
		StringReader re = new StringReader(cssText.substring(idx + 1, lm1));
		InputSource source = new InputSource(re);
		try {
			css.parseStyleSheet(source);
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		// All seems fine, let's parse the condition text
		parseConditionText(cssText.substring(9, idx).trim());
		// If we are reaching this, we can clear the old rules and load the new ones
		cssRules.clear();
		cssRules.addAll(css.getCssRules());
		for (AbstractCSSRule rule : cssRules) {
			rule.setParentRule(this);
		}
		if (css.hasRuleErrorsOrWarnings()) {
			parentSS.getErrorHandler().mergeState(css.getErrorHandler());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
		SupportsRule other = (SupportsRule) obj;
		if (condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!condition.equals(other.condition)) {
			return false;
		}
		return true;
	}

	@Override
	public SupportsRule clone(AbstractCSSStyleSheet parentSheet) {
		return new SupportsRule(parentSheet, this);
	}

}
