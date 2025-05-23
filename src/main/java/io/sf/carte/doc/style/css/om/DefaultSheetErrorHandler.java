/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSFontFaceRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

public class DefaultSheetErrorHandler implements SheetErrorHandler, java.io.Serializable {

	private static final long serialVersionUID = 2L;

	private final CSSStyleSheet<?> sheet;

	private LinkedList<String> unknownRules = null;
	private LinkedList<String> ignoredImports = null;
	private LinkedList<RuleParseException> ruleParseErrors = null;

	private LinkedList<MediaQueryList> badMediaLists = null;
	private LinkedList<String> badAtRules = null;
	private LinkedList<String> badConditions = null;
	private List<CSSRule> ruleList = null;

	private List<CSSParseException> sacWarnings = null;

	private List<CSSParseException> sacErrors = null;

	private boolean sacWarningMergedState = false;

	private boolean sacErrorMergedState = false;

	private boolean omWarningMergedState = false;

	private boolean omErrorMergedState = false;

	public DefaultSheetErrorHandler(CSSStyleSheet<? extends CSSRule> sheet) {
		super();
		this.sheet = sheet;
	}

	@Override
	public void badAtRule(DOMException e, String atRule) {
		if (badAtRules == null) {
			badAtRules = new LinkedList<>();
		}
		badAtRules.add(atRule);
	}

	@Override
	public void badMediaList(MediaQueryList media) {
		if (badMediaLists == null) {
			badMediaLists = new LinkedList<>();
		}
		badMediaLists.add(media);
	}

	@Override
	public void handleSacError(CSSParseException exception) {
		if (sacErrors == null) {
			sacErrors = new LinkedList<>();
			ruleList = new LinkedList<>();
		}
		sacErrors.add(exception);
		ruleList.add(null);
	}

	@Override
	public void handleSacWarning(CSSParseException exception) {
		if (sacWarnings == null) {
			sacWarnings = new LinkedList<>();
		}
		sacWarnings.add(exception);
	}

	@Override
	public boolean hasOMErrors() {
		return omErrorMergedState || unknownRules != null || ignoredImports != null || ruleParseErrors != null
				|| badMediaLists != null || badAtRules != null || badConditions != null;
	}

	@Override
	public boolean hasOMWarnings() {
		return omWarningMergedState;
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
			ignoredImports = new LinkedList<>();
		}
		ignoredImports.add(uri);
	}

	@Override
	public void ruleParseError(CSSRule rule, CSSParseException ex) {
		if (ruleParseErrors == null) {
			ruleParseErrors = new LinkedList<>();
		}
		RuleParseException rpe = new RuleParseException(ex, rule);
		ruleParseErrors.add(rpe);
	}

	@Override
	public void ruleParseWarning(CSSRule rule, CSSParseException ex) {
	}

	@Override
	public void conditionalRuleError(BooleanCondition condition, String message) {
		if (badConditions == null) {
			badConditions = new LinkedList<>();
		}
		badConditions.add(message);
	}

	@Override
	public void fontFormatError(CSSFontFaceRule rule, Exception exception) {
	}

	@Override
	public void unknownRule(String rule) {
		if (unknownRules == null) {
			unknownRules = new LinkedList<>();
		}
		unknownRules.add(rule);
	}

	@Override
	public void mapError(CSSParseException exception, CSSRule rule) {
		if (ruleList == null) {
			ruleList = new LinkedList<>();
			sacErrors = new LinkedList<>();
		}
		sacErrors.add(exception);
		ruleList.add(rule);
	}

	public LinkedList<String> getBadAtRules() {
		return badAtRules;
	}

	@Deprecated
	public LinkedList<String> getBadInlineStyles() {
		return null;
	}

	public LinkedList<String> getConditionErrors() {
		return badConditions;
	}

	public LinkedList<MediaQueryList> getBadMediaLists() {
		return badMediaLists;
	}

	public LinkedList<String> getIgnoredImports() {
		return ignoredImports;
	}

	public CSSRule getRuleAtError(int index) {
		return ruleList == null ? null : ruleList.get(index);
	}

	public LinkedList<RuleParseException> getRuleParseErrors() {
		return ruleParseErrors;
	}

	public List<CSSParseException> getSacErrors() {
		return sacErrors;
	}

	public List<CSSParseException> getSacWarnings() {
		return sacWarnings;
	}

	public LinkedList<String> getUnknownRules() {
		return unknownRules;
	}

	/**
	 * Merge the error state from the error handler of another sheet.
	 * <p>
	 * Implementations are only required to merge boolean state of NSAC errors and warnings.
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
		badMediaLists = null;
		badAtRules = null;
		badConditions = null;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		if (hasSacErrors()) {
			buf.append("NSAC Errors:");
			if (sacErrors != null) {
				buf.append('\n');
				for (int i = 0; i < sacErrors.size(); i++) {
					CSSParseException ex = sacErrors.get(i);
					CSSRule rule = ruleList.get(i);
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
			buf.append("No NSAC errors.\n");
		}
		if (sacWarnings != null) {
			buf.append("NSAC Warnings:");
			Iterator<CSSParseException> it = sacWarnings.iterator();
			while (it.hasNext()) {
				buf.append(' ').append(it.next().getMessage());
			}
		} else {
			buf.append("No NSAC warnings.\n");
		}
		return buf.toString();
	}

	@Override
	public void sacMalfunction(String message) {
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
		message = "NSAC malfuntion in sheet " + text + ": " + message;
		throw new IllegalStateException(message);
	}

}
