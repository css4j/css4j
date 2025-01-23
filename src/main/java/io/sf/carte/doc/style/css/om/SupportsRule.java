/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSResourceLimitException;
import io.sf.carte.doc.style.css.CSSSupportsRule;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.DeclarationCondition;
import io.sf.carte.doc.style.css.nsac.SelectorFunction;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS {@code @supports} rule.
 * 
 * @author Carlos Amengual
 * 
 */
public class SupportsRule extends GroupingRule implements CSSSupportsRule {

	private static final long serialVersionUID = 1L;

	private BooleanCondition condition = null;

	protected SupportsRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, SUPPORTS_RULE, origin);
	}

	SupportsRule(AbstractCSSStyleSheet parentSheet, SupportsRule copyfrom) {
		super(parentSheet, copyfrom);
		condition = copyfrom.getCondition();
	}

	protected SupportsRule(AbstractCSSStyleSheet parentSheet, BooleanCondition condition, byte origin) {
		super(parentSheet, SUPPORTS_RULE, origin);
		if (condition == null) {
			throw new NullPointerException("Null @supports condition.");
		}
		this.condition = condition;
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
	 * @param conditionText the condition text.
	 */
	private void parseConditionText(String conditionText) throws DOMException {
		CSSParser parser = (CSSParser) createSACParser();
		try {
			condition = parser.parseSupportsCondition(conditionText, null, getParentStyleSheet());
		} catch (CSSBudgetException e) {
			throw new CSSResourceLimitException(
					"Limit found while parsing condition " + conditionText, e);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR,
					"Error parsing condition: " + conditionText);
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
		return condition != null && supports(condition, styleDatabase);
	}

	private boolean supports(BooleanCondition condition, StyleDatabase styleDatabase) {
		switch (condition.getType()) {
		case PREDICATE:
			DeclarationCondition declCond = (DeclarationCondition) condition;
			return styleDatabase.supports(declCond.getName(), declCond.getValue());
		case AND:
			List<BooleanCondition> subcond = condition.getSubConditions();
			if (subcond == null) {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().conditionalRuleError(this.condition,
							"No conditions inside and(): " + this.condition.toString());
				}
				return false;
			}
			Iterator<BooleanCondition> it = subcond.iterator();
			while (it.hasNext()) {
				if (!supports(it.next(), styleDatabase)) {
					return false;
				}
			}
			return true;
		case NOT:
			BooleanCondition nested = condition.getNestedCondition();
			if (nested == null) {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().conditionalRuleError(this.condition,
							"No conditions inside not(): " + this.condition.toString());
				}
				return false;
			}
			return !supports(nested, styleDatabase);
		case OR:
			subcond = condition.getSubConditions();
			if (subcond == null) {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().conditionalRuleError(this.condition,
							"No conditions inside or(): " + this.condition.toString());
				}
				return false;
			}
			it = subcond.iterator();
			while (it.hasNext()) {
				if (supports(it.next(), styleDatabase)) {
					return true;
				}
			}
			break;
		case SELECTOR_FUNCTION:
			SelectorFunction selCond = (SelectorFunction) condition;
			return styleDatabase.supports(selCond.getSelectors());
		case OTHER:
			break;
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
			context.startRule(wri, getPrecedingComments());
			wri.write("@supports ");
			wri.write(getConditionText());
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			getCssRules().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endRuleList(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, getTrailingComments());
		}
	}

	@Override
	protected void setGroupingRule(GroupingRule rule) throws DOMException {
		SupportsRule supportsRule = (SupportsRule) rule;
		this.condition = supportsRule.condition;
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
