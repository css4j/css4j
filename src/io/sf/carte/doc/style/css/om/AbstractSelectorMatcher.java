/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.DirectionalityHelper.Directionality;
import io.sf.carte.doc.DOMTokenSetImpl;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.SACParserFactory;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition2;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.PositionalCondition2;
import io.sf.carte.doc.style.css.nsac.Selector2;
import io.sf.carte.doc.style.css.parser.AnBExpression;

/**
 * CSS Selector matcher.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class AbstractSelectorMatcher implements SelectorMatcher {

	private String localName = null;

	private String pseudoElt = null;

	public AbstractSelectorMatcher() {
		super();
	}

	/**
	 * Gets the local name of the element to which this selector matcher applies.
	 * <p>
	 * It is guaranteed to be lowercase.
	 * 
	 * @return the local name.
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Set the local name of the element that this matcher belongs to.
	 * 
	 * @param localname the lowercase local name.
	 */
	protected void setLocalName(String localname) {
		this.localName = localname;
	}

	protected String getClassAttribute(CSSDocument.ComplianceMode mode) {
		String classAttr = getAttributeValue("class");
		if (mode != CSSDocument.ComplianceMode.STRICT) {
			// Native DOM does not need the next conditional but DOM wrapper does
			if (classAttr.length() == 0) {
				classAttr = getAttributeValue("CLASS");
				if (classAttr.length() == 0) {
					classAttr = getAttributeValue("Class");
				}
			}
			if (classAttr.length() != 0) {
				classAttr = classAttr.toLowerCase(Locale.ROOT);
			}
		}
		return classAttr;
	}

	@Override
	public String getPseudoElement() {
		return pseudoElt;
	}

	/**
	 * Set this selector's pseudo-element.
	 * 
	 * @param pseudoElt
	 *            the pseudo-element, or <code>null</code> if none.
	 */
	@Override
	public void setPseudoElement(String pseudoElt) throws DOMException {
		if (pseudoElt != null) {
			if (pseudoElt.length() < 3 || pseudoElt.charAt(0) != ':') {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Bad pseudo-element: " + pseudoElt);
			}
			if (pseudoElt.charAt(1) != ':') {
				pseudoElt = pseudoElt.substring(1);
			} else {
				pseudoElt = pseudoElt.substring(2);
			}
		}
		this.pseudoElt = pseudoElt;
	}

	protected boolean isActivePseudoClass(String pseudoclassName) {
		return false;
	}

	/**
	 * Does this selector match the given selector list?
	 * 
	 * @param selist
	 *            the list of selectors to which this matcher will compare.
	 * 
	 * @return the index of the highest matching selector, or -1 if none matches.
	 */
	@Override
	public int matches(SelectorList selist) {
		int sz = selist.getLength();
		Specificity matchedsp = null;
		int matchedIdx = -1;
		for (int i = 0; i < sz; i++) {
			Selector sel = selist.item(i);
			if (matches(sel)) {
				Specificity sp = new Specificity(sel, this);
				if (matchedsp == null || Specificity.selectorCompare(matchedsp, sp) < 0) {
					matchedsp = sp;
					matchedIdx = i;
				}
			}
		}
		return matchedIdx;
	}

	/**
	 * Does this matcher match the given selector?
	 * 
	 * @param selector
	 *            the selector to be tested.
	 * 
	 * @return <code>true</code> if the given selector matches this object, <code>false</code> otherwise.
	 */
	@Override
	public boolean matches(Selector selector) {
		switch (selector.getSelectorType()) {
		case Selector.SAC_ELEMENT_NODE_SELECTOR:
			String elname = ((ElementSelector) selector).getLocalName();
			String nsuri = ((ElementSelector) selector).getNamespaceURI();
			if (nsuri == null || nsuri.equals(getNamespaceURI())) {
				return elname == null || localName.equalsIgnoreCase(elname) || elname.equals("*");
			} else if (nsuri.length() == 0 && getNamespaceURI() == null) {
				// Only matches no namespace
				return true;
			}
			break;
		case Selector.SAC_CONDITIONAL_SELECTOR:
			ConditionalSelector condsel = (ConditionalSelector) selector;
			return matchCondition(condsel.getCondition(), condsel.getSimpleSelector());
		case Selector.SAC_ANY_NODE_SELECTOR:
		case Selector2.SAC_SCOPE_SELECTOR:
			return true;
		case Selector.SAC_CHILD_SELECTOR:
			SimpleSelector desc = ((DescendantSelector) selector).getSimpleSelector();
			if (matches(desc)) {
				Selector ancestor = ((DescendantSelector) selector).getAncestorSelector();
				SelectorMatcher parentSM;
				if (desc.getSelectorType() != Selector.SAC_PSEUDO_ELEMENT_SELECTOR) {
					parentSM = getParentSelectorMatcher();
				} else {
					return matches(ancestor);
				}
				if (parentSM != null && parentSM.matches(ancestor)) {
					return true;
				}
			}
			break;
		case Selector.SAC_DESCENDANT_SELECTOR:
			desc = ((DescendantSelector) selector).getSimpleSelector();
			if (matches(desc)) {
				Selector ancestor = ((DescendantSelector) selector).getAncestorSelector();
				AbstractSelectorMatcher parentSM;
				if (desc.getSelectorType() == Selector.SAC_PSEUDO_ELEMENT_SELECTOR) {
					return matches(ancestor);
				} else {
					parentSM = getParentSelectorMatcher();
					while (parentSM != null) {
						if (parentSM.matches(ancestor)) {
							return true;
						}
						parentSM = parentSM.getParentSelectorMatcher();
					}
				}
			}
			break;
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
			if (matches(((SiblingSelector) selector).getSiblingSelector())) {
				Selector sel = ((SiblingSelector) selector).getSelector();
				SelectorMatcher siblingSM = getPreviousSiblingSelectorMatcher();
				return siblingSM == null ? false : siblingSM.matches(sel);
			}
			break;
		case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
			SiblingSelector sibling = (SiblingSelector) selector;
			if (matches(sibling.getSiblingSelector())) {
				Selector sel = sibling.getSelector();
				AbstractSelectorMatcher siblingSM = getPreviousSiblingSelectorMatcher();
				while (siblingSM != null) {
					if (siblingSM.matches(sel)) {
						return true;
					}
					siblingSM = siblingSM.getPreviousSiblingSelectorMatcher();
				}
			}
		}
		return false;
	}

	boolean matchCondition(Condition cond, SimpleSelector simple) {
		switch (cond.getConditionType()) {
		case Condition.SAC_CLASS_CONDITION:
			String cond_value = ((AttributeCondition) cond).getValue();
			return matchesClass(cond_value) && matches(simple);
		case Condition.SAC_ID_CONDITION:
			return matchesId(((AttributeCondition) cond).getValue()) && matches(simple);
		case Condition.SAC_ATTRIBUTE_CONDITION:
			return matchesAttribute((AttributeCondition) cond, simple);
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
			return matchesOneOfAttribute((AttributeCondition) cond, simple);
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
			return matchesBeginHyphenAttribute((AttributeCondition) cond, simple);
		case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
			return matchesBeginsAttribute((AttributeCondition2) cond, simple);
		case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
			return matchesEndsAttribute((AttributeCondition2) cond, simple);
		case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
			return matchesSubstringAttribute((AttributeCondition2) cond, simple);
		case Condition.SAC_LANG_CONDITION:
			return matchesLang((LangCondition) cond, simple);
		case Condition.SAC_PSEUDO_CLASS_CONDITION:
			// Non-state pseudo-classes are generally more expensive than other
			// selectors, so we evaluate the simple selector first.
			if (matches(simple)) {
				String pseudoClassName = ((AttributeCondition) cond).getLocalName();
				cond_value = ((AttributeCondition) cond).getValue();
				String argument = null;
				if (pseudoClassName == null) {
					// Batik or SS parser
					int idxparen = cond_value.indexOf('(');
					if (idxparen != -1) {
						int lm1 = cond_value.length() - 1;
						int idxp1 = idxparen + 1;
						if (lm1 <= idxp1 || cond_value.charAt(lm1) != ')') {
							break;
						}
						pseudoClassName = cond_value.substring(0, idxparen);
						argument = cond_value.substring(idxp1, lm1);
					} else {
						pseudoClassName = cond_value;
					}
				} else if (cond_value != null) {
					// Probably this implementation
					argument = cond_value;
				}
				pseudoClassName = pseudoClassName.toLowerCase(Locale.ROOT).intern();
				if ("first-child".equals(pseudoClassName)) {
					return isFirstChild();
				} else if ("last-child".equals(pseudoClassName)) {
					return isLastChild();
				} else if ("only-child".equals(pseudoClassName)) {
					return isOnlyChild();
				} else if ("first-of-type".equals(pseudoClassName)) {
					return isFirstOfType();
				} else if ("last-of-type".equals(pseudoClassName)) {
					return isLastOfType();
				} else if ("only-of-type".equals(pseudoClassName)) {
					return isOnlyOfType();
				} else if ("any-link".equals(pseudoClassName)) {
					return isAnyLink();
				} else if ("link".equals(pseudoClassName)) {
					return isNotVisitedLink();
				} else if ("visited".equals(pseudoClassName)) {
					return isVisitedLink();
				} else if ("target".equals(pseudoClassName)) {
					return isTarget();
				} else if ("root".equals(pseudoClassName)) {
					return isRoot();
				} else if ("empty".equals(pseudoClassName)) {
					return isEmpty();
				} else if ("blank".equals(pseudoClassName)) {
					return isBlank();
				} else if ("disabled".equals(pseudoClassName)) {
					return isDisabled();
				} else if ("enabled".equals(pseudoClassName)) {
					return isEnabled();
				} else if ("read-write".equals(pseudoClassName)) {
					return isReadWrite();
				} else if ("read-only".equals(pseudoClassName)) {
					return !isReadWrite();
				} else if ("placeholder-shown".equals(pseudoClassName)) {
					return isPlaceholderShown();
				} else if ("default".equals(pseudoClassName)) {
					return isUIDefault();
				} else if ("checked".equals(pseudoClassName)) {
					return isChecked();
				} else if ("indeterminate".equals(pseudoClassName)) {
					return isIndeterminate();
				} else if ("dir".equals(pseudoClassName)) {
					return isDir(argument);
				}
				if (pseudoClassName.equals("is") || pseudoClassName.equals("where")) {
					// Parse selector list
					return matches(parseSelector(argument)) >= 0;
				} else if (pseudoClassName.equals("not")) {
					return !(matches(parseSelector(argument)) >= 0);
				} else if (pseudoClassName.equals("has")) {
					return scopeMatch(argument, simple) >= 0;
				} else if (pseudoClassName.equals("nth-child")) {
					return isNthChild(argument);
				} else if (pseudoClassName.equals("nth-last-child")) {
					return isNthLastChild(argument);
				} else if (pseudoClassName.equals("nth-of-type")) {
					return isNthOfType(argument);
				} else if (pseudoClassName.equals("nth-last-of-type")) {
					return isNthLastOfType(argument);
				} else {
					return isActivePseudoClass(pseudoClassName);
				}
			}
			break;
		case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
			if (matches(simple)) {
				return ((AttributeCondition) cond).getLocalName().equals(getPseudoElement());
			}
			break;
		case Condition.SAC_AND_CONDITION:
			CombinatorCondition comb = (CombinatorCondition) cond;
			return matchCondition(comb.getFirstCondition(), simple)
					&& matchCondition(comb.getSecondCondition(), simple);
		case Condition.SAC_ONLY_CHILD_CONDITION: // Only NSAC parser uses this
			return matches(simple) && isOnlyChild();
		case Condition.SAC_ONLY_TYPE_CONDITION: // Only NSAC parser uses this
			return matches(simple) && isOnlyOfType();
		case Condition.SAC_POSITIONAL_CONDITION:
			// SS and Batik use SAC_PSEUDO_CLASS_CONDITION instead of this
			if (matches(simple)) {
				PositionalCondition2 pcond = (PositionalCondition2) cond;
				int pos = pcond.getOffset();
				int factor = pcond.getFactor();
				if (pcond.getType()) {
					// Of type
					if (pcond.isForwardCondition()) {
						return isNthOfType(factor, pos);
					} else {
						return isNthLastOfType(factor, pos);
					}
				} else {
					int idx;
					if (pcond.isForwardCondition()) {
						idx = indexOf(pcond.getOfList());
					} else {
						idx = reverseIndexOf(pcond.getOfList());
					}
					if (idx == -1) {
						return false;
					}
					idx -= pos;
					return factor == 0 ? idx == 0 : Math.floorMod(idx, factor) == 0;
				}
			}
			break;
		case Condition2.SAC_SELECTOR_ARGUMENT_CONDITION:
			if (matches(simple)) {
				String name = ((ArgumentCondition) cond).getName();
				SelectorList selist = ((ArgumentCondition) cond).getSelectors();
				if ("not".equals(name)) {
					for (int i = 0; i < selist.getLength(); i++) {
						if (matches(selist.item(i))) {
							return false;
						}
					}
					return true;
				} else if ("has".equals(name)) {
					for (int i = 0; i < selist.getLength(); i++) {
						if (scopeMatch(selist.item(i), simple)) {
							return true;
						}
					}
					return false;
				} else if ("is".equals(name)) {
					return matches(selist) >= 0;
				}
			}
			break;
		case Condition.SAC_OR_CONDITION:
			comb = (CombinatorCondition) cond;
			return matchCondition(comb.getFirstCondition(), simple)
					|| matchCondition(comb.getSecondCondition(), simple);
		// No more conditions: text-content selectors etc. were deprecated
		}
		return false;
	}

	private boolean matchesId(String value) {
		CSSDocument.ComplianceMode mode = getOwnerDocument().getComplianceMode();
		String idAttr = getId();
		if (mode != CSSDocument.ComplianceMode.STRICT) {
			if (idAttr.length() == 0) {
				idAttr = getQuirksId();
			}
			return idAttr.equalsIgnoreCase(value);
		}
		return idAttr.equals(value);
	}

	/**
	 * Verifies if the selector matches a given class name.
	 * <p>
	 * A case-sensitive comparison is performed for <code>STRICT</code> mode, case-insensitive
	 * for other modes.
	 * </p>
	 * 
	 * @param cond_value
	 *            the class name.
	 * @return <code>true</code> if matches, <code>false</code> otherwise.
	 */
	private boolean matchesClass(String cond_value) {
		CSSDocument.ComplianceMode mode = getOwnerDocument().getComplianceMode();
		String classAttr = getClassAttribute(mode);
		if (!DOMTokenSetImpl.checkMultipleToken(classAttr)) {
			classAttr = classAttr.trim();
			if (mode != CSSDocument.ComplianceMode.STRICT) {
				return classAttr.equalsIgnoreCase(cond_value);
			}
			return classAttr.equals(cond_value);
		} else {
			if (mode != CSSDocument.ComplianceMode.STRICT) {
				StringTokenizer st = new StringTokenizer(classAttr);
				while (st.hasMoreTokens()) {
					classAttr = st.nextToken();
					if (classAttr.equalsIgnoreCase(cond_value)) {
						return true;
					}
				}
			} else {
				StringTokenizer st = new StringTokenizer(classAttr);
				while (st.hasMoreTokens()) {
					classAttr = st.nextToken();
					if (classAttr.equals(cond_value)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private boolean matchesAttribute(AttributeCondition attrcond, SimpleSelector simple) {
		String attrName = attrcond.getLocalName();
		if (hasAttribute(attrName) && matches(simple)) {
			String cond_value = attrcond.getValue();
			if (attrcond.getSpecified() || cond_value != null) {
				String attribValue = getAttributeValue(attrName);
				if (attrcond instanceof AttributeCondition2
						&& ((AttributeCondition2) attrcond).hasFlag(AttributeCondition2.Flag.CASE_I)) {
					return attribValue.equalsIgnoreCase(cond_value);
				}
				return attribValue.equals(cond_value);
			} else {
				return true;
			}
		}
		return false;
	}

	private boolean matchesOneOfAttribute(AttributeCondition attrcond, SimpleSelector simple) {
		String attrName = attrcond.getLocalName();
		if (hasAttribute(attrName) && matches(simple)) {
			boolean ignoreCase = attrcond instanceof AttributeCondition2
					&& ((AttributeCondition2) attrcond).hasFlag(AttributeCondition2.Flag.CASE_I);
			String cond_value = attrcond.getValue();
			String attrValue = getAttributeValue(attrName);
			StringTokenizer tok = new StringTokenizer(attrValue, " ");
			if (ignoreCase) {
				while (tok.hasMoreElements()) {
					String token = tok.nextToken();
					if (token.equalsIgnoreCase(cond_value)) {
						return true;
					}
				}
			} else {
				while (tok.hasMoreElements()) {
					String token = tok.nextToken();
					if (token.equals(cond_value)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean matchesBeginHyphenAttribute(AttributeCondition attrcond, SimpleSelector simple) {
		String attrName = attrcond.getLocalName();
		if (hasAttribute(attrName) && matches(simple)) {
			String attrValue = getAttributeValue(attrName);
			int attrlen = attrValue.length();
			String condValue = attrcond.getValue();
			int condLen = condValue.length();
			boolean ignoreCase = attrcond instanceof AttributeCondition2
					&& ((AttributeCondition2) attrcond).hasFlag(AttributeCondition2.Flag.CASE_I);
			if (condLen == attrlen) {
				return ignoreCase ? attrValue.equalsIgnoreCase(condValue) : attrValue.equals(condValue);
			} else if (condLen < attrlen) {
				return attrValue.regionMatches(ignoreCase, 0, condValue, 0, condLen)
						&& attrValue.charAt(condLen) == '-';
			}
		}
		return false;
	}

	private boolean matchesBeginsAttribute(AttributeCondition2 attrcond, SimpleSelector simple) {
		String attrName = attrcond.getLocalName();
		if (hasAttribute(attrName)) {
			String value = getAttributeValue(attrName);
			if (matches(simple)) {
				boolean ignoreCase = attrcond.hasFlag(AttributeCondition2.Flag.CASE_I);
				String condValue = attrcond.getValue();
				return value.regionMatches(ignoreCase, 0, condValue, 0, condValue.length());
			}
		}
		return false;
	}

	private boolean matchesEndsAttribute(AttributeCondition2 attrcond, SimpleSelector simple) {
		String attrName = attrcond.getLocalName();
		if (hasAttribute(attrName)) {
			String value = getAttributeValue(attrName);
			if (matches(simple)) {
				boolean ignoreCase = attrcond.hasFlag(AttributeCondition2.Flag.CASE_I);
				String condValue = attrcond.getValue();
				int len = value.length();
				int condLen = condValue.length();
				return value.regionMatches(ignoreCase, len - condLen, condValue, 0, condLen);
			}
		}
		return false;
	}

	private boolean matchesSubstringAttribute(AttributeCondition2 attrcond, SimpleSelector simple) {
		String attrName = attrcond.getLocalName();
		if (hasAttribute(attrName) && matches(simple)) {
			String value = getAttributeValue(attrName);
			boolean ignoreCase = attrcond.hasFlag(AttributeCondition2.Flag.CASE_I);
			String condValue = attrcond.getValue();
			if (!ignoreCase) { // That should be a bit faster
				return value.contains(condValue);
			}
			int len = value.length();
			int condLen = condValue.length();
			int startLimit = len - condLen;
			if (startLimit >= 0) {
				for (int i = 0; i <= startLimit; i++) {
					if (value.regionMatches(ignoreCase, i, condValue, 0, condLen)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean matchesLang(LangCondition cond, SimpleSelector simple) {
		String attrName = cond.getLang();
		String lang = getLanguage();
		return lang.startsWith(attrName) && matches(simple);
	}

	protected boolean isDir(String argument) {
		if (argument != null) {
			Directionality dir = getDirectionality();
			if ("rtl".equalsIgnoreCase(argument)) {
				return dir == Directionality.RTL;
			} else if ("ltr".equalsIgnoreCase(argument)) {
				return dir == Directionality.LTR;
			}
		}
		return false;
	}

	private int scopeMatch(String subselector, SimpleSelector scope) {
		// This is intended for non-NSAC parsers. Parser's selectors must have
		// a meaningful toString() method for this to work
		SelectorList selist = parseSelector(scope.toString() + subselector);
		if (selist == null) {
			return -1;
		}
		int sz = selist.getLength();
		Specificity matchedsp = null;
		int matchedIdx = -1;
		for (int i = 0; i < sz; i++) {
			Selector sel = selist.item(i);
			if (scopeMatch(sel, scope)) {
				Specificity sp = new Specificity(sel, this);
				if (matchedsp == null || Specificity.selectorCompare(matchedsp, sp) < 0) {
					matchedsp = sp;
					matchedIdx = i;
				}
			}
		}
		return matchedIdx;
	}

	private boolean scopeMatch(Selector selector, SimpleSelector scope) {
		switch (selector.getSelectorType()) {
		case Selector.SAC_ELEMENT_NODE_SELECTOR:
		case Selector.SAC_CONDITIONAL_SELECTOR:
			return scopeMatch(new DescendantSelectorImpl(scope, (SimpleSelector) selector), scope);
		case Selector.SAC_CHILD_SELECTOR:
		case Selector.SAC_DESCENDANT_SELECTOR:
			return scopeMatchChild((DescendantSelector) selector);
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
		case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
			return scopeMatchDirectAdjacent((SiblingSelector) selector);
		}
		return false;
	}

	private class DescendantSelectorImpl implements DescendantSelector {

		SimpleSelector simpleSelector;
		Selector scope;

		DescendantSelectorImpl(Selector scope, SimpleSelector simpleSelector) {
			super();
			this.scope = scope;
			this.simpleSelector = simpleSelector;
		}

		@Override
		public short getSelectorType() {
			return Selector.SAC_DESCENDANT_SELECTOR;
		}

		@Override
		public Selector getAncestorSelector() {
			return scope;
		}

		@Override
		public SimpleSelector getSimpleSelector() {
			return simpleSelector;
		}
	}

	abstract protected CSSDocument getOwnerDocument();

	abstract protected boolean scopeMatchChild(DescendantSelector selector);

	abstract protected boolean scopeMatchDirectAdjacent(SiblingSelector selector);

	protected boolean isFormElement() {
		String tagname = getLocalName();
		return tagname.equals("input") || tagname.equals("button") || tagname.equals("select")
				|| tagname.equals("optgroup") || tagname.equals("option") || tagname.equals("textarea")
				|| tagname.equals("keygen") || tagname.equals("fieldset");
	}

	protected boolean isAnyLink() {
		String href = getLinkHrefAttribute();
		return href.length() != 0;
	}

	protected boolean isNotVisitedLink() {
		String href = getLinkHrefAttribute();
		if (href.length() != 0) {
			return !getOwnerDocument().isVisitedURI(href);
		} else {
			return false;
		}
	}

	protected boolean isVisitedLink() {
		String href = getLinkHrefAttribute();
		if (href.length() != 0) {
			return getOwnerDocument().isVisitedURI(href);
		} else {
			return false;
		}
	}

	private String getLinkHrefAttribute() {
		String href = getAttributeValue("href");
		if (href.length() == 0 || (!"a".equals(localName) && !"link".equals(localName) && !"area".equals(localName))) {
			href = getAttributeValue("xlink:href");
		}
		return href;
	}

	abstract protected boolean isTarget();

	abstract protected boolean isRoot();

	abstract protected boolean isEmpty();

	abstract protected boolean isBlank();

	abstract protected boolean isDisabled();

	protected boolean isReadWrite() {
		if ("true".equalsIgnoreCase(getAttributeValue("contenteditable"))) {
			return true;
		}
		return isEnabled();
	}

	protected boolean isPlaceholderShown() {
		return hasAttribute("placeholder");
	}

	protected boolean isUIDefault() {
		String tagname = getLocalName();
		if ("button".equals(tagname)) {
			return "submit".equalsIgnoreCase(getAttributeValue("type")) && isDefaultButton();
		} else if ("option".equals(tagname)) {
			return hasAttribute("selected");
		} else if ("input".equals(tagname)) {
			if (hasAttribute("checked")) {
				return true;
			}
			String type = getAttributeValue("type").toLowerCase(Locale.ROOT);
			if ("submit".equals(type) || "image".equals(type)) {
				// Must be their form's default button.
				return isDefaultButton();
			}
		}
		return false;
	}

	protected boolean isChecked() {
		String tagname = getLocalName();
		if ("input".equals(tagname)) {
			String type = getAttributeValue("type");
			return ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) && hasAttribute("checked");
		} else if ("option".equals(tagname)) {
			return hasAttribute("selected");
		}
		return false;
	}

	protected boolean isIndeterminate() {
		String s = getAttributeValue("indeterminate");
		return s.length() != 0 && !s.equalsIgnoreCase("false");
	}

	/**
	 * Gets the selector matcher for the parent element.
	 * 
	 * @return the selector matcher for the parent element, or null if none.
	 */
	abstract protected AbstractSelectorMatcher getParentSelectorMatcher();

	/**
	 * Gets the selector matcher for the previous sibling.
	 * 
	 * @return the selector matcher for the previous sibling, or null if no previous
	 *         sibling.
	 */
	abstract protected AbstractSelectorMatcher getPreviousSiblingSelectorMatcher();

	/**
	 * Get the directionality of the element associated to this matcher.
	 * 
	 * @return the directionality.
	 */
	abstract protected Directionality getDirectionality();

	/**
	 * The element in this matcher is the first child?
	 * 
	 * @return <code>true</code> if the element in this matcher is a first child, <code>false</code> if not.
	 */
	abstract protected boolean isFirstChild();

	/**
	 * The element in this matcher is the last child?
	 * 
	 * @return <code>true</code> if the element in this matcher is the last child, <code>false</code> if not.
	 */
	abstract protected boolean isLastChild();

	/**
	 * The element in this matcher is the only child?
	 * 
	 * @return <code>true</code> if the element in this matcher is the only child, <code>false</code> if not.
	 */
	protected boolean isOnlyChild() {
		return isFirstChild() && isLastChild();
	}

	/**
	 * The element in this matcher is the first child of its type (tag name)?
	 * 
	 * @return <code>true</code> if the element in this matcher is a first child of its type, <code>false</code> if
	 *         not.
	 */
	abstract protected boolean isFirstOfType();

	/**
	 * The element in this matcher is the last child of its type (tag name)?
	 * 
	 * @return <code>true</code> if the element in this matcher is the last child of its type, <code>false</code> if
	 *         not.
	 */
	abstract protected boolean isLastOfType();

	/**
	 * The element in this matcher is the only child of its type (tag name)?
	 * 
	 * @return <code>true</code> if the element in this matcher is the only child of its type, <code>false</code> if
	 *         not.
	 */
	protected boolean isOnlyOfType() {
		return isFirstOfType() && isLastOfType();
	}

	private boolean isNthChild(String expression) {
		AnBExpression expr = new MyAnBExpression();
		try {
			expr.parse(expression);
		} catch (IllegalArgumentException e) {
			return false;
		}
		int idx = indexOf(expr.getSelectorList());
		if (idx == -1) {
			return false;
		}
		idx -= expr.getOffset();
		int step = expr.getStep();
		return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
	}

	private boolean isNthLastChild(String expression) {
		AnBExpression expr = new MyAnBExpression();
		try {
			expr.parse(expression);
		} catch (IllegalArgumentException e) {
			return false;
		}
		int idx = reverseIndexOf(expr.getSelectorList());
		if (idx == -1) {
			return false;
		}
		idx -= expr.getOffset();
		int step = expr.getStep();
		return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
	}

	private boolean isNthOfType(String expression) {
		AnBExpression expr = new MyAnBExpression();
		try {
			expr.parse(expression);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return isNthOfType(expr.getStep(), expr.getOffset());
	}

	private boolean isNthLastOfType(String expression) {
		AnBExpression expr = new MyAnBExpression();
		try {
			expr.parse(expression);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return isNthLastOfType(expr.getStep(), expr.getOffset());
	}

	private static class MyAnBExpression extends AnBExpression {

		@Override
		protected SelectorList parseSelector(String selText) {
			return AbstractSelectorMatcher.parseSelector(selText);
		}
	}

	private static SelectorList parseSelector(String selText) {
		if (selText.length() == 0) {
			return null;
		}
		Parser parser = SACParserFactory.createSACParser();
		InputSource source = new InputSource(new StringReader(selText));
		SelectorList list;
		try {
			list = parser.parseSelectors(source);
		} catch (IOException e) {
			list = null;
		} catch (RuntimeException e) {
			list = null;
		}
		return list;
	}

	protected boolean isEnabled() {
		return isFormElement() && !isDisabled();
	}

	abstract protected int indexOf(SelectorList list);

	abstract protected int reverseIndexOf(SelectorList list);

	abstract protected boolean isNthOfType(int step, int offset);

	abstract protected boolean isNthLastOfType(int step, int offset);

	abstract protected boolean isDefaultButton();

	abstract protected String getNamespaceURI();

	/**
	 * Gets the value of the given attribute in the element associated to this selector
	 * matcher.
	 * 
	 * @param attrName
	 *            the attribute name.
	 * @return the attribute value, or the empty string if the attribute is defined but has
	 *         no value, also if the attribute is not defined. Never returns null.
	 */
	abstract protected String getAttributeValue(String attrName);

	/**
	 * Checks if is defined the given attribute in the element associated to this selector
	 * matcher.
	 * 
	 * @param attrName
	 *            the attribute name.
	 * @return <code>true</code> if the attribute is defined, <code>false</code> if not.
	 */
	abstract protected boolean hasAttribute(String attrName);

	/**
	 * Gets the 'id' attribute of the element associated to this selector matcher.
	 * 
	 * @return the 'id' attribute value, or the empty string if the element has no 'id'.
	 */
	abstract protected String getId();

	private String getQuirksId() {
		String idAttr = getAttributeValue("id");
		if (idAttr.length() == 0) {
			idAttr = getAttributeValue("ID");
			if (idAttr.length() == 0) {
				idAttr = getAttributeValue("Id");
			}
		}
		return idAttr;
	}

	/**
	 * Gets the language of the element associated to this selector matcher.
	 * 
	 * @return the language, or the empty String if the element has no language defined.
	 */
	/*
	 * In (X)HTML, the lang attribute contains the language, but that may not be true for
	 * other XML.
	 */
	abstract protected String getLanguage();

	/**
	 * Add to the list all the state pseudo-classes found in the selector.
	 * <p>
	 * This method is intended to be called from a CSSCanvas object.
	 * 
	 * @param selector
	 *            the selector to be tested.
	 * @param statePseudoClasses
	 *            the list of state pseudo-classes.
	 */
	public static void findStatePseudoClasses(Selector selector, List<String> statePseudoClasses) {
		switch (selector.getSelectorType()) {
		case Selector.SAC_CONDITIONAL_SELECTOR:
			Condition condition = ((ConditionalSelector) selector).getCondition();
			if (condition.getConditionType() == Condition.SAC_PSEUDO_CLASS_CONDITION) {
				String pseudoClass = ((AttributeCondition) condition).getLocalName();
				if (pseudoClass == null) {
					pseudoClass = ((AttributeCondition) condition).getValue();
				}
				int idxp = pseudoClass.indexOf('(');
				if (idxp != -1) {
					pseudoClass = pseudoClass.substring(0, idxp);
				}
				pseudoClass = pseudoClass.toLowerCase(Locale.ROOT).intern();
				if (pseudoClass != "first-child" && pseudoClass != "last-child" && pseudoClass != "only-child"
						&& pseudoClass != "any-link" && pseudoClass != "link" && pseudoClass != "visited"
						&& pseudoClass != "target" && pseudoClass != "root" && pseudoClass != "empty"
						&& pseudoClass != "blank" && pseudoClass != "is" && pseudoClass != "not" && pseudoClass != "has"
						&& pseudoClass != "dir" && pseudoClass != "lang" && pseudoClass != "scope"
						&& pseudoClass != "empty" && pseudoClass != "blank" && pseudoClass != "valid"
						&& pseudoClass != "invalid" && pseudoClass != "in-range" && pseudoClass != "out-of-range"
						&& pseudoClass != "required" && pseudoClass != "optional" && pseudoClass != "user-invalid"
						&& pseudoClass != "nth-child" && pseudoClass != "nth-last-child" && pseudoClass != "nth-of-type"
						&& pseudoClass != "nth-last-of-type" && pseudoClass != "disabled" && pseudoClass != "enabled"
						&& pseudoClass != "read-write" && pseudoClass != "read-only"
						&& pseudoClass != "placeholder-shown" && pseudoClass != "default" && pseudoClass != "checked"
						&& pseudoClass != "indeterminate" && pseudoClass != "nth-col"
						&& pseudoClass != "nth-last-col") {
					statePseudoClasses.add(pseudoClass);
					break;
				}
			}
			findStatePseudoClasses(((ConditionalSelector) selector).getSimpleSelector(), statePseudoClasses);
			break;
		case Selector.SAC_CHILD_SELECTOR:
		case Selector.SAC_DESCENDANT_SELECTOR:
			findStatePseudoClasses(((DescendantSelector) selector).getSimpleSelector(), statePseudoClasses);
			findStatePseudoClasses(((DescendantSelector) selector).getAncestorSelector(), statePseudoClasses);
			break;
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
			findStatePseudoClasses(((SiblingSelector) selector).getSiblingSelector(), statePseudoClasses);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		AbstractSelectorMatcher parentSM = getParentSelectorMatcher();
		if (parentSM != null) {
			sb.append(parentSM.getLocalName()).append(' ').append('>').append(' ');
		}
		if (localName != null) {
			sb.append(localName);
		}
		CSSDocument.ComplianceMode mode = getOwnerDocument().getComplianceMode();
		String classAttr = getClassAttribute(mode);
		if (classAttr.length() != 0) {
			sb.append('.').append(classAttr);
		} else if (getId().length() != 0) {
			sb.append('#').append(getId());
		}
		if (pseudoElt != null) {
			sb.append(':').append(pseudoElt);
		}
		return sb.toString();
	}

}
