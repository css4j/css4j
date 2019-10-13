/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSGroupingRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.ParserControl;

/**
 * Implementation of CSSGroupingRule.
 * 
 */
abstract public class GroupingRule extends BaseCSSRule implements CSSGroupingRule {

	CSSRuleArrayList cssRules;

	protected GroupingRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super(parentSheet, type, origin);
		cssRules = new CSSRuleArrayList();
	}

	protected GroupingRule(AbstractCSSStyleSheet parentSheet, GroupingRule copyfrom) {
		super(parentSheet, copyfrom.getType(), copyfrom.getOrigin());
		if (copyfrom.precedingComments != null) {
			this.precedingComments = new ArrayList<String>(copyfrom.precedingComments.size());
			this.precedingComments.addAll(copyfrom.precedingComments);
		}
		cssRules = new CSSRuleArrayList(copyfrom.getCssRules().getLength());
		Iterator<AbstractCSSRule> it = copyfrom.getCssRules().iterator();
		while (it.hasNext()) {
			AbstractCSSRule cloned = it.next().clone(parentSheet);
			cloned.setParentRule(this);
			cssRules.add(cloned);
		}
	}

	@Override
	public CSSRuleArrayList getCssRules() {
		return cssRules;
	}

	/**
	 * Inserts a new rule into this grouping rule collection.
	 * 
	 * @param rule
	 *            The parsable text representing the rule.
	 * @param index
	 *            The index within the collection of the rule before which to
	 *            insert the specified rule. If the specified index is equal to
	 *            the length of the rule collection, the rule will be added to
	 *            its end.
	 * @return the index at which the rule was inserted.
	 * @throws DOMException
	 *             if the index is out of bounds or there was a problem parsing
	 *             the rule.
	 */
	@Override
	public int insertRule(String rule, int index) throws DOMException {
		if (index < 0 || index > cssRules.size()) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, "Index out of bounds in rule list");
		}
		Reader re = new StringReader(rule);
		RuleHandler handler = new RuleHandler();
		handler.setCurrentInsertionIndex(index);
		RuleErrorHandler errorHandler = new RuleErrorHandler();
		Parser parser = createSACParser();
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(errorHandler);
		try {
			parser.parseRule(re);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return index;
	}

	@Override
	public void deleteRule(int index) throws DOMException {
		if (index < 0 || index >= cssRules.size()) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR,
					"Could not delete rule in rule list: index out of bounds.");
		}
		cssRules.remove(index);
	}

	/**
	 * Sets the given CSS rule as being owned by this grouping rule, and inserts it
	 * at the given index.
	 * <p>
	 * This method modifies the insertion-index internal state of the rule.
	 * 
	 * @param cssrule
	 *            the rule.
	 * @param index
	 *            the index at which to insert the rule.
	 * @return the index at which the rule was finally inserted.
	 */
	int insertRule(AbstractCSSRule cssrule, int index) {
		cssrule.setParentRule(this);
		return cssRules.insertRule(cssrule, index);
	}

	/**
	 * Adds the given rule to the end of the rule list.
	 * <p>
	 * Does not modify the insertion-index internal state of the rule.
	 * 
	 * @param cssrule
	 *            the rule to add.
	 * @return the index at which the rule was inserted.
	 */
	int addRule(AbstractCSSRule cssrule) {
		int len = cssRules.getLength();
		cssRules.add(cssrule);
		cssrule.setParentRule(this);
		return len;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		AbstractCSSStyleSheet parentSS = getParentStyleSheet();
		if (parentSS == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR,
					"This rule must be added to a sheet first");
		}
		// Create, load & Parse
		AbstractCSSStyleSheet css = parentSS.getStyleSheetFactory().createRuleStyleSheet(this, null, null);
		Reader re = new StringReader(cssText);
		try {
			css.parseStyleSheet(re);
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		CSSRuleArrayList parsedRules = css.getCssRules();
		int len = parsedRules.getLength();
		if (len > 1) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"Attempted to parse more than one rule inside this one");
		}
		if (len == 1) {
			AbstractCSSRule firstRule = parsedRules.item(0);
			if (firstRule.getType() != getType()) {
				throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
						"Attempted to parse a rule of type " + firstRule.getType());
			}
			GroupingRule groupingRule = (GroupingRule) firstRule;
			setGroupingRule(groupingRule);
			this.precedingComments = groupingRule.precedingComments;
			cssRules.clear();
			cssRules.addAll(groupingRule.getCssRules());
			for (AbstractCSSRule rule : cssRules) {
				rule.setParentRule(this);
			}
			if (css.hasRuleErrorsOrWarnings()) {
				parentSS.getErrorHandler().mergeState(css.getErrorHandler());
			}
		} else {
			cssRules.clear();
		}
	}

	protected void setGroupingRule(GroupingRule rule) throws DOMException {
	}

	private class RuleHandler extends SheetHandler {
		private AbstractCSSRule currentRule = null;

		private int currentInsertionIndex = 0;

		private RuleHandler() {
			super((BaseCSSStyleSheet) GroupingRule.this.getParentStyleSheet(), getOrigin(),
					ExtendedCSSStyleSheet.COMMENTS_IGNORE);
		}

		public void setCurrentInsertionIndex(int index) {
			currentInsertionIndex = index;
		}

		@Override
		public void parseStart(ParserControl parserctl) {
			super.parseStart(parserctl);
			currentRule = null;
		}

		@Override
		public void importStyle(String uri, MediaQueryList media, String defaultNamespaceURI) {
			// Ignore any '@import' rule that occurs inside a block (CSS 2.1 §4.1.5)
		}

		@Override
		protected void addLocalRule(AbstractCSSRule rule) throws DOMException {
			if (currentRule != null) {
				throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
						"Attempted to parse more than one rule inside this one");
			}
			currentRule = rule;
			currentInsertionIndex = insertRule(currentRule, currentInsertionIndex);
		}

	}

	private class RuleErrorHandler implements CSSErrorHandler {

		@Override
		public void warning(CSSParseException exception) throws CSSParseException {
			AbstractCSSStyleSheet sheet = getParentStyleSheet();
			if (sheet != null) {
				sheet.getErrorHandler().ruleParseWarning(GroupingRule.this, exception);
			}
		}

		@Override
		public void error(CSSParseException exception) throws CSSParseException {
			AbstractCSSStyleSheet sheet = getParentStyleSheet();
			if (sheet != null) {
				sheet.getErrorHandler().ruleParseError(GroupingRule.this, exception);
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cssRules == null) ? 0 : cssRules.hashCode());
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
		if (!(obj instanceof GroupingRule)) {
			return false;
		}
		GroupingRule other = (GroupingRule) obj;
		if (cssRules == null) {
			if (other.cssRules != null) {
				return false;
			}
		} else if (!cssRules.equals(other.cssRules)) {
			return false;
		}
		return true;
	}

}
