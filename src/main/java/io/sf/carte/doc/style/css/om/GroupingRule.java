/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.DOMNotSupportedException;
import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSGroupingRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * Implementation of CSSGroupingRule.
 * 
 */
abstract public class GroupingRule extends BaseCSSRule implements CSSGroupingRule, RuleStore {

	private static final long serialVersionUID = 1L;

	CSSRuleArrayList cssRules = null;

	protected GroupingRule(AbstractCSSStyleSheet parentSheet, short type, int origin) {
		super(parentSheet, type, origin);
	}

	protected GroupingRule(AbstractCSSStyleSheet parentSheet, GroupingRule copyfrom) {
		super(parentSheet, copyfrom.getType(), copyfrom.getOrigin());
		if (copyfrom.getPrecedingComments() != null) {
			setPrecedingComments(new LinkedStringList());
			getPrecedingComments().addAll(copyfrom.getPrecedingComments());
		}
		cssRules = cloneRuleList(parentSheet, copyfrom.getCssRules());
	}

	CSSRuleArrayList cloneRuleList(AbstractCSSStyleSheet parentSheet,
			CSSRuleArrayList otherRules) {
		CSSRuleArrayList rules = new CSSRuleArrayList(otherRules.getLength());
		for (AbstractCSSRule rule : otherRules) {
			AbstractCSSRule cloned = rule.clone(parentSheet);
			cloned.setParentRule(this);
			rules.add(cloned);
		}
		return rules;
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
		AllowWarningsRuleErrorHandler errorHandler = new AllowWarningsRuleErrorHandler();
		CSSParser parser = (CSSParser) createSACParser();
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(errorHandler);
		try {
			parseRule(re, parser);
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}

		return index;
	}

	void parseRule(Reader reader, Parser parser) throws DOMException, IOException {
		try {
			parser.parseRule(reader);
		} catch (CSSNamespaceParseException e) {
			DOMException ex = new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSBudgetException e) {
			throw new DOMNotSupportedException(e.getMessage(), e);
		} catch (CSSParseException e) {
			throw new DOMSyntaxException("Parse error at [" + e.getLineNumber() + ','
					+ e.getColumnNumber() + "]: " + e.getMessage(), e);
		} catch (CSSException e) {
			throw new DOMInvalidAccessException(e.getMessage(), e);
		} catch (DOMException e) {
			// Handler may produce DOM exceptions
			throw e;
		} catch (RuntimeException e) {
			String message = e.getMessage();
			AbstractCSSStyleSheet parentSS = getParentStyleSheet();
			if (parentSS != null) {
				String href = parentSS.getHref();
				if (href != null) {
					message = "Error in stylesheet at " + href + ": " + message;
				}
			}
			DOMException ex = new DOMException(DOMException.INVALID_STATE_ERR, message);
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * Error handler that allows warnings but no exceptions.
	 */
	class AllowWarningsRuleErrorHandler implements CSSErrorHandler {

		private List<CSSParseException> warnings = null;

		public AllowWarningsRuleErrorHandler() {
			super();
		}

		@Override
		public void warning(CSSParseException exception) throws CSSParseException {
			if (warnings == null) {
				warnings = new LinkedList<>();
			}
			warnings.add(exception);
		}

		@Override
		public void error(CSSParseException exception) throws CSSParseException {
			throw exception;
		}

		public List<CSSParseException> getWarnings() {
			return warnings;
		}

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
	 * Adds the given rule at the current insertion point (generally after the last
	 * rule).
	 * 
	 * @param cssrule the rule to add.
	 */
	@Override
	public void addRule(AbstractCSSRule cssrule) {
		cssRules.add(cssrule);
		cssrule.setParentRule(this);
	}

	@Override
	int addRuleList(CSSRuleArrayList otherRules, int importCount) {
		for (AbstractCSSRule oRule : otherRules) {
			addRule(oRule);
		}
		return importCount;
	}

	void updateDescendantsAbsoluteSelectorList(SelectorList parentList) {
		if (cssRules != null) {
			// Update absolute selectors in descendants
			for (AbstractCSSRule rule : cssRules) {
				if (rule instanceof GroupingRule) {
					if (rule.getType() == CSSRule.STYLE_RULE) {
						StyleRule styleRule = (StyleRule) rule;
						parentList = styleRule.getSelectorList().replaceNested(parentList);
						styleRule.setAbsoluteSelectorList(parentList);
					}
					((GroupingRule) rule).updateDescendantsAbsoluteSelectorList(parentList);
				}
			}
		}
	}

	@Override
	void prioritySplit(AbstractCSSStyleSheet importantSheet, AbstractCSSStyleSheet normalSheet,
			RuleStore importantStore, RuleStore normalStore) {
		/*
		 * This method isn't intended to be used directly, but to be called by
		 * subclasses with the proper stores.
		 */
		for (AbstractCSSRule r : cssRules) {
			r.prioritySplit(importantSheet, normalSheet, importantStore, normalStore);
		}
	}

	private class RuleHandler extends SheetHandler {
		private AbstractCSSRule currentRule = null;

		private int currentInsertionIndex = 0;

		private RuleHandler() {
			super((BaseCSSStyleSheet) GroupingRule.this.getParentStyleSheet(), getOrigin(),
					CSSStyleSheet.COMMENTS_IGNORE);
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
		public void importStyle(String uri, String layer, BooleanCondition supportsCondition,
				MediaQueryList media, String defaultNamespaceURI) {
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

	@Override
	boolean hasErrorsOrWarnings() {
		for (AbstractCSSRule rule : cssRules) {
			if (rule.hasErrorsOrWarnings()) {
				return true;
			}
		}
		return false;
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
