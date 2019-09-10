/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.style.css.SheetErrorHandler;

public class DefaultSheetErrorHandler implements SheetErrorHandler {

	static Logger log = LoggerFactory.getLogger(DefaultSheetErrorHandler.class.getName());
	private ExtendedCSSStyleSheet<?> sheet;
	private boolean logged = false;

	private LinkedList<String> unknownRules = null;
	private LinkedList<String> ignoredImports = null;
	private LinkedList<RuleParseError> ruleParseErrors = null;
	private HashMap<CSSImportRule,IOException> importIOErrors = null;
	private LinkedList<String> emptyRules = null;

	private LinkedList<SACMediaList> badMediaLists = null;
	private LinkedList<String> badAtRules = null;
	private LinkedList<String> badInlineStyles = null;
	private List<AbstractCSSRule> ruleList = null;

	private List<CSSParseException> sacWarnings = null;

	private List<CSSParseException> sacErrors = null;

	private boolean sacWarningMergedState = false;

	private boolean sacErrorMergedState = false;

	private boolean omWarningMergedState = false;

	private boolean omErrorMergedState = false;

	public DefaultSheetErrorHandler(ExtendedCSSStyleSheet<? extends ExtendedCSSRule> sheet) {
		super();
		this.sheet = sheet;
	}

	@Override
	public void badAtRule(DOMException e, String atRule) {
		if (badAtRules == null) {
			badAtRules = new LinkedList<String>();
		}
		badAtRules.add(atRule);
	}

	@Override
	public void badMediaList(SACMediaList media) {
		if (badMediaLists == null) {
			badMediaLists = new LinkedList<SACMediaList>();
		}
		badMediaLists.add(media);
	}

	private void logSheetInfo() {
		if (!logged) {
			logged = true;
			String text = "unknown";
			Node owner = sheet.getOwnerNode();
			if (owner != null && owner.getNodeType() == Node.ELEMENT_NODE
					&& "style".equalsIgnoreCase(((CSSElement) owner).getTagName())) {
				text = owner.toString();
			} else {
				String uri = sheet.getHref();
				if (uri != null) {
					text = "at " + uri;
				}
			}
			log.warn("Issue(s) with sheet " + text);
		}
	}

	@Override
	public void emptyStyleRule(String selector) {
		if (emptyRules == null) {
			emptyRules = new LinkedList<String>();
		}
		emptyRules.add(selector);
	}

	public LinkedList<String> getBadAtRules() {
		return badAtRules;
	}

	public LinkedList<String> getBadInlineStyles() {
		return badInlineStyles;
	}

	public LinkedList<SACMediaList> getBadMediaLists() {
		return badMediaLists;
	}

	public LinkedList<String> getEmptyStyleRules() {
		return emptyRules;
	}

	public LinkedList<String> getIgnoredImports() {
		return ignoredImports;
	}

	public ExtendedCSSRule getRuleAtError(int index) {
		return ruleList == null ? null : ruleList.get(index);
	}

	public LinkedList<RuleParseError> getRuleParseErrors() {
		return ruleParseErrors;
	}

	public List<CSSParseException> getSacErrors() {
		return sacErrors;
	}

	public HashMap<CSSImportRule, IOException> getImportIOErrors() {
		return importIOErrors;
	}

	public List<CSSParseException> getSacWarnings() {
		return sacWarnings;
	}

	public LinkedList<String> getUnknownRules() {
		return unknownRules;
	}

	@Override
	public void handleSacError(CSSParseException exception) {
		if (sacErrors == null) {
			sacErrors = new LinkedList<CSSParseException>();
			ruleList = new LinkedList<AbstractCSSRule>();
		}
		sacErrors.add(exception);
		ruleList.add(null);
	}

	@Override
	public void handleSacWarning(CSSParseException exception) {
		if (sacWarnings == null) {
			sacWarnings = new LinkedList<CSSParseException>();
		}
		sacWarnings.add(exception);
	}

	@Override
	public boolean hasOMErrors() {
		return omErrorMergedState || unknownRules != null || ignoredImports != null || ruleParseErrors != null
				|| importIOErrors != null || badMediaLists != null || badAtRules != null || badInlineStyles != null;
	}

	@Override
	public boolean hasOMWarnings() {
		return omWarningMergedState || emptyRules != null;
	}

	@Override
	public boolean hasSacErrors() {
		return sacErrorMergedState || sacErrors != null;
	}

	@Override
	public boolean hasSacWarnings() {
		return sacWarningMergedState || sacWarnings != null;
	}

	@Override
	public void ignoredImport(String uri) {
		if (ignoredImports == null) {
			ignoredImports = new LinkedList<String>();
		}
		ignoredImports.add(uri);
	}

	@Override
	public void inlineStyleError(DOMException e, Element elm, String attr) {
		if (badInlineStyles == null) {
			badInlineStyles = new LinkedList<String>();
		}
		badInlineStyles.add(attr);
	}

	public void logSacErrors(Logger logger) {
		if (sacErrors != null) {
			ListIterator<CSSParseException> it = sacErrors.listIterator();
			while (it.hasNext()) {
				CSSParseException ex = it.next();
				logger.error(
						"SAC error at [" + ex.getLineNumber() + "," + ex.getColumnNumber() + "]: " + ex.getMessage());
			}
		}
	}

	public void logSacWarnings(Logger logger) {
		if (sacWarnings != null) {
			ListIterator<CSSParseException> it = sacWarnings.listIterator();
			while (it.hasNext()) {
				CSSParseException ex = it.next();
				logger.warn(
						"SAC warning at [" + ex.getLineNumber() + "," + ex.getColumnNumber() + "]: " + ex.getMessage());
			}
		}
	}

	@Override
	public void mapError(CSSParseException exception, AbstractCSSRule rule) {
		if (ruleList == null) {
			ruleList = new LinkedList<AbstractCSSRule>();
			sacErrors = new LinkedList<CSSParseException>();
		}
		sacErrors.add(exception);
		ruleList.add(rule);
	}

	/**
	 * Merge the error state from the error handler of another sheet.
	 * <p>
	 * Implementations are only required to merge boolean state of SAC errors and warnings.
	 * Merging other state is optional.
	 * 
	 * @param other
	 *            the other style sheet error handler.
	 */
	@Override
	public void mergeState(SheetErrorHandler other) {
		sacWarningMergedState = sacWarningMergedState || other.hasSacWarnings();
		sacErrorMergedState = sacErrorMergedState || other.hasSacErrors();
		omErrorMergedState = omErrorMergedState || other.hasOMErrors();
		omWarningMergedState = omWarningMergedState || other.hasOMWarnings();
	}

	@Override
	public void reset() {
		ruleList = null;
		sacErrors = null;
		sacWarnings = null;
		sacErrorMergedState = false;
		sacWarningMergedState = false;
		// OM-level errors
		omErrorMergedState = false;
		unknownRules = null;
		ignoredImports = null;
		ruleParseErrors = null;
		emptyRules = null;
		badMediaLists = null;
		badAtRules = null;
		badInlineStyles = null;
	}

	@Override
	public void ruleParseError(CSSRule rule, CSSParseException ex) {
		if (ruleParseErrors == null) {
			ruleParseErrors = new LinkedList<RuleParseError>();
		}
		MyRuleParseError rpe = new MyRuleParseError();
		rpe.rule = rule;
		rpe.ex = ex;
		ruleParseErrors.add(rpe);
	}

	@Override
	public void ruleParseWarning(CSSRule rule, CSSParseException ex) {
	}

	@Override
	public void ruleIOError(CSSImportRule rule, IOException exception) {
		if (importIOErrors == null) {
			importIOErrors = new HashMap<CSSImportRule,IOException>();
		}
		importIOErrors.put(rule, exception);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		if (hasSacErrors()) {
			buf.append("SAC Errors:");
			if (sacErrors != null) {
				buf.append('\n');
				for (int i = 0; i < sacErrors.size(); i++) {
					CSSParseException ex = sacErrors.get(i);
					ExtendedCSSRule rule = ruleList.get(i);
					buf.append('[').append(ex.getLineNumber()).append(':').append(ex.getColumnNumber()).append("] ")
							.append(ex.getMessage());
					if (rule != null) {
						buf.append(" --> ").append(rule.getCssText());
					}
					buf.append('\n');
				}
			} else {
				buf.append(" only merged.\n");
			}
		} else {
			buf.append("No SAC errors.\n");
		}
		if (sacWarnings != null) {
			buf.append("SAC Warnings:");
			Iterator<CSSParseException> it = sacWarnings.iterator();
			while (it.hasNext()) {
				buf.append(' ').append(it.next().getMessage());
			}
		} else {
			buf.append("No SAC warnings.\n");
		}
		return buf.toString();
	}

	@Override
	public void sacMalfunction(String message) {
		logSheetInfo();
		log.error(message);
	}

	@Override
	public void unknownRule(String rule) {
		if (unknownRules == null) {
			unknownRules = new LinkedList<String>();
		}
		unknownRules.add(rule);
	}

	public interface RuleParseError {

		CSSParseException getException();

		CSSRule getRule();

	}

	private class MyRuleParseError implements RuleParseError {
		CSSRule rule;
		CSSParseException ex;

		@Override
		public CSSParseException getException() {
			return ex;
		}

		@Override
		public CSSRule getRule() {
			return rule;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("Rule: ").append(rule.getType()).append(", [").append(ex.getLineNumber()).append(':')
					.append(ex.getColumnNumber()).append(']').append(' ').append(" Message: ").append(ex.getMessage());
			return buf.toString();
		}

	}
}
