/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import io.sf.carte.doc.DOMTokenSetImpl;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LangCondition;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.PseudoCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * CSS Selector matcher.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class AbstractSelectorMatcher implements SelectorMatcher {

	private String localName = null;

	private Condition pseudoElt = null;

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
	public Condition getPseudoElement() {
		return pseudoElt;
	}

	/**
	 * Set this selector's pseudo-element.
	 * 
	 * @param pseudoElt
	 *            the pseudo-element, or <code>null</code> if none.
	 */
	@Override
	public void setPseudoElement(Condition pseudoElt) {
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
		case ELEMENT:
			String elname = ((ElementSelector) selector).getLocalName();
			String nsuri = ((ElementSelector) selector).getNamespaceURI();
			if (nsuri == null || nsuri.equals(getNamespaceURI())) {
				return elname == null || localName.equalsIgnoreCase(elname) || elname.equals("*");
			} else if (nsuri.length() == 0 && getNamespaceURI() == null) {
				// Only matches no namespace
				return true;
			}
			break;
		case CONDITIONAL:
			ConditionalSelector condsel = (ConditionalSelector) selector;
			return matchCondition(condsel.getCondition(), condsel.getSimpleSelector());
		case UNIVERSAL:
		case SCOPE_MARKER:
			return true;
		case CHILD:
			SimpleSelector desc = ((CombinatorSelector) selector).getSecondSelector();
			if (matches(desc)) {
				Selector ancestor = ((CombinatorSelector) selector).getSelector();
				SelectorMatcher parentSM;
				parentSM = getParentSelectorMatcher();
				if (parentSM != null && parentSM.matches(ancestor)) {
					return true;
				}
			}
			break;
		case DESCENDANT:
			desc = ((CombinatorSelector) selector).getSecondSelector();
			if (matches(desc)) {
				Selector ancestor = ((CombinatorSelector) selector).getSelector();
				AbstractSelectorMatcher parentSM = getParentSelectorMatcher();
				while (parentSM != null) {
					if (parentSM.matches(ancestor)) {
						return true;
					}
					parentSM = parentSM.getParentSelectorMatcher();
				}
			}
			break;
		case DIRECT_ADJACENT:
			if (matches(((CombinatorSelector) selector).getSecondSelector())) {
				Selector sel = ((CombinatorSelector) selector).getSelector();
				SelectorMatcher siblingSM = getPreviousSiblingSelectorMatcher();
				return siblingSM != null && siblingSM.matches(sel);
			}
			break;
		case SUBSEQUENT_SIBLING:
			CombinatorSelector sibling = (CombinatorSelector) selector;
			if (matches(sibling.getSecondSelector())) {
				Selector sel = sibling.getSelector();
				AbstractSelectorMatcher siblingSM = getPreviousSiblingSelectorMatcher();
				while (siblingSM != null) {
					if (siblingSM.matches(sel)) {
						return true;
					}
					siblingSM = siblingSM.getPreviousSiblingSelectorMatcher();
				}
			}
		default:
		}
		return false;
	}

	boolean matchCondition(Condition cond, SimpleSelector simple) {
		switch (cond.getConditionType()) {
		case CLASS:
			AttributeCondition attrcond = (AttributeCondition) cond;
			String cond_value = attrcond.getValue();
			return matchesClass(cond_value) && matches(simple);
		case ID:
			return matchesId(((AttributeCondition) cond).getValue()) && matches(simple);
		case ATTRIBUTE:
			attrcond = (AttributeCondition) cond;
			String attrName = attrcond.getLocalName();
			if (hasAttribute(attrName) && matches(simple)) {
				cond_value = attrcond.getValue();
				if (cond_value != null) {
					String attribValue = getAttributeValue(attrName);
					if (attrcond.hasFlag(AttributeCondition.Flag.CASE_I)) {
						return attribValue.equalsIgnoreCase(cond_value);
					}
					return attribValue.equals(cond_value);
				} else {
					return true;
				}
			}
			break;
		case ONE_OF_ATTRIBUTE:
			attrcond = (AttributeCondition) cond;
			attrName = attrcond.getLocalName();
			if (hasAttribute(attrName) && matches(simple)) {
				cond_value = attrcond.getValue();
				String attrValue = getAttributeValue(attrName);
				StringTokenizer tok = new StringTokenizer(attrValue, " ");
				while (tok.hasMoreElements()) {
					String token = tok.nextToken();
					if (token.equals(cond_value)) {
						return true;
					}
				}
			}
			break;
		case BEGIN_HYPHEN_ATTRIBUTE:
			attrcond = (AttributeCondition) cond;
			attrName = attrcond.getLocalName();
			if (hasAttribute(attrName) && matches(simple)) {
				String attrValue = getAttributeValue(attrName);
				int attrlen = attrValue.length();
				String condstr = attrcond.getValue();
				int condlen = condstr.length();
				if (condlen == attrlen) {
					return attrValue.equals(condstr);
				} else if (condlen < attrlen) {
					return attrValue.startsWith(condstr) && attrValue.charAt(condlen) == '-';
				}
			}
			break;
		case BEGINS_ATTRIBUTE:
			attrcond = (AttributeCondition) cond;
			attrName = attrcond.getLocalName();
			if (hasAttribute(attrName)) {
				return getAttributeValue(attrName).startsWith(attrcond.getValue()) && matches(simple);
			}
			break;
		case ENDS_ATTRIBUTE:
			attrcond = (AttributeCondition) cond;
			attrName = attrcond.getLocalName();
			if (hasAttribute(attrName)) {
				return getAttributeValue(attrName).endsWith(attrcond.getValue()) && matches(simple);
			}
			break;
		case SUBSTRING_ATTRIBUTE:
			attrcond = (AttributeCondition) cond;
			attrName = attrcond.getLocalName();
			if (hasAttribute(attrName)) {
				return getAttributeValue(attrName).contains(attrcond.getValue()) && matches(simple);
			}
			break;
		case LANG:
			attrName = ((LangCondition) cond).getLang();
			String lang = getLanguage();
			return lang.startsWith(attrName) && matches(simple);
		case PSEUDO_CLASS:
			// Non-state pseudo-classes are generally more expensive than other
			// selectors, so we evaluate the simple selector first.
			if (matches(simple)) {
				PseudoCondition pseudocond = (PseudoCondition) cond;
				String pseudoClassName = pseudocond.getName();
				pseudoClassName = pseudoClassName.toLowerCase(Locale.ROOT).intern();
				if ("only-child".equals(pseudoClassName)) {
					return isOnlyChild();
				} else if ("only-of-type".equals(pseudoClassName)) {
					return isOnlyOfType();
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
				}
				return isActivePseudoClass(pseudoClassName);
			}
			break;
		case PSEUDO_ELEMENT:
			if (matches(simple)) {
				Condition pe = getPseudoElement();
				if (pe != null) {
					PseudoCondition pseudo = (PseudoCondition) cond;
					if (pe.getConditionType() == ConditionType.PSEUDO_ELEMENT) {
						return pseudo.getName().equals(((PseudoCondition) pe).getName());
					}
					if (pe.getConditionType() == ConditionType.AND) {
						CombinatorCondition comb = (CombinatorCondition) pe;
						return pseudo.getName().equals(((PseudoCondition) comb.getFirstCondition()).getName())
								|| pseudo.getName().equals(((PseudoCondition) comb.getSecondCondition()).getName());
					}
				}
			}
			break;
		case AND:
			CombinatorCondition comb = (CombinatorCondition) cond;
			return matchCondition(comb.getFirstCondition(), simple)
					&& matchCondition(comb.getSecondCondition(), simple);
		case ONLY_CHILD:
			return matches(simple) && isOnlyChild();
		case ONLY_TYPE:
			return matches(simple) && isOnlyOfType();
		case POSITIONAL:
			if (matches(simple)) {
				PositionalCondition pcond = (PositionalCondition) cond;
				int pos = pcond.getOffset();
				int factor = pcond.getFactor();
				if (pcond.isOfType()) {
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
		case SELECTOR_ARGUMENT:
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
				} else if ("is".equals(name) || "where".equals(name)) {
					return matches(selist) >= 0;
				}
			}
			break;
		// No more conditions: text-content selectors etc. were deprecated
		}
		return false;
	}

	private boolean matchesId(String value) {
		CSSDocument.ComplianceMode mode = getComplianceMode();
		String idAttr = getId();
		if (mode != CSSDocument.ComplianceMode.STRICT) {
			if (idAttr.length() == 0) {
				idAttr = getQuirksId();
			}
			return idAttr.equalsIgnoreCase(value);
		}
		return idAttr.equals(value);
	}

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
		CSSDocument.ComplianceMode mode = getComplianceMode();
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

	private boolean scopeMatch(Selector selector, SimpleSelector scope) {
		switch (selector.getSelectorType()) {
		case ELEMENT:
		case CONDITIONAL:
			return scopeMatch(new CombinatorSelectorImpl(scope, (SimpleSelector) selector), scope);
		case CHILD:
		case DESCENDANT:
			CombinatorSelector comb = (CombinatorSelector) selector;
			if (comb.getSelector().getSelectorType() == Selector.SelectorType.SCOPE_MARKER) {
				return scopeMatchChild(comb);
			}
			return scopeMatchDescendant(comb);
		case DIRECT_ADJACENT:
		case SUBSEQUENT_SIBLING:
			comb = (CombinatorSelector) selector;
			if (comb.getSelector().getSelectorType() == Selector.SelectorType.SCOPE_MARKER) {
				return scopeMatchDirectAdjacent(comb);
			}
			return scopeMatchDescendant(comb);
		default:
		}
		return false;
	}

	private class CombinatorSelectorImpl implements CombinatorSelector {

		SimpleSelector simpleSelector;
		Selector scope;

		CombinatorSelectorImpl(Selector scope, SimpleSelector simpleSelector) {
			super();
			this.scope = scope;
			this.simpleSelector = simpleSelector;
		}

		@Override
		public SelectorType getSelectorType() {
			return SelectorType.DESCENDANT;
		}

		@Override
		public Selector getSelector() {
			return scope;
		}

		@Override
		public SimpleSelector getSecondSelector() {
			return simpleSelector;
		}
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

	protected boolean isEnabled() {
		return isFormElement() && !isDisabled();
	}

	protected boolean isFormElement() {
		String tagname = getLocalName();
		return tagname.equals("input") || tagname.equals("button") || tagname.equals("select")
				|| tagname.equals("optgroup") || tagname.equals("option") || tagname.equals("textarea")
				|| tagname.equals("keygen") || tagname.equals("fieldset");
	}

	protected boolean isIndeterminate() {
		String s = getAttributeValue("indeterminate");
		return s.length() != 0 && !s.equalsIgnoreCase("false");
	}

	/**
	 * The element in this matcher is the only child?
	 * 
	 * @return <code>true</code> if the element in this matcher is the only child, <code>false</code> if not.
	 */
	protected boolean isOnlyChild() {
		return isFirstChild() && isLastChild();
	}

	/**
	 * The element in this matcher is the only child of its type (tag name)?
	 * 
	 * @return <code>true</code> if the element in this matcher is the only child of its type, <code>false</code> if
	 *         not.
	 */
	protected boolean isOnlyOfType() {
		return isFirstOfType() && isLastOfType();
	}

	protected boolean isPlaceholderShown() {
		return hasAttribute("placeholder");
	}

	protected boolean isReadWrite() {
		if ("true".equalsIgnoreCase(getAttributeValue("contenteditable"))) {
			return true;
		}
		return isEnabled();
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

	abstract protected CSSDocument.ComplianceMode getComplianceMode();

	/**
	 * Gets the namespace URI of the element associated to this selector matcher.
	 * 
	 * @return the namespace URI, or null if the element belongs to no specific
	 *         namespace.
	 */
	abstract protected String getNamespaceURI();

	abstract protected boolean scopeMatchChild(CombinatorSelector selector);

	abstract protected boolean scopeMatchDescendant(CombinatorSelector selector);

	abstract protected boolean scopeMatchDirectAdjacent(CombinatorSelector selector);

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

	abstract protected int indexOf(SelectorList list);

	abstract protected int reverseIndexOf(SelectorList list);

	abstract protected boolean isNthOfType(int step, int offset);

	abstract protected boolean isNthLastOfType(int step, int offset);

	abstract protected boolean isNotVisitedLink();

	abstract protected boolean isVisitedLink();

	abstract protected boolean isTarget();

	abstract protected boolean isRoot();

	abstract protected boolean isEmpty();

	abstract protected boolean isBlank();

	abstract protected boolean isDisabled();

	abstract protected boolean isDefaultButton();

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
		case CONDITIONAL:
			Condition condition = ((ConditionalSelector) selector).getCondition();
			if (condition.getConditionType() == ConditionType.PSEUDO_CLASS) {
				String pseudoClass = ((PseudoCondition) condition).getName();
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
		case CHILD:
		case DESCENDANT:
			findStatePseudoClasses(((CombinatorSelector) selector).getSecondSelector(), statePseudoClasses);
			findStatePseudoClasses(((CombinatorSelector) selector).getSelector(), statePseudoClasses);
			break;
		case DIRECT_ADJACENT:
			findStatePseudoClasses(((CombinatorSelector) selector).getSecondSelector(), statePseudoClasses);
		default:
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
		CSSDocument.ComplianceMode mode = getComplianceMode();
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
