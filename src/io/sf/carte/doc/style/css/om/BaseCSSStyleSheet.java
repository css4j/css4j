/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;

import io.sf.carte.doc.agent.AgentUtil;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSMarginRule;
import io.sf.carte.doc.style.css.CSSNamespaceRule;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.nsac.Parser2.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.PositionalCondition2;
import io.sf.carte.doc.style.css.nsac.Selector2;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * CSS Style Sheet Object Model implementation base class.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class BaseCSSStyleSheet extends AbstractCSSStyleSheet {

	private AbstractCSSStyleSheet parent = null;

	private String href = null;

	private AbstractCSSRule ownerRule = null;

	private byte sheetOrigin;

	CSSRuleArrayList cssRules = new CSSRuleArrayList();

	private int currentInsertionIndex = 0;

	private MediaQueryList destinationMedia;

	private Map<String, String> namespaces = new HashMap<String, String>();

	private boolean disabled = false;

	private SheetErrorHandler sheetErrorHandler = null;

	private static final int MAX_IMPORT_RECURSION = 8; // Allows 6 nested imports

	/**
	 * 
	 * @param title
	 *            the advisory title.
	 * @param media
	 *            the media this sheet is for.
	 * @param ownerRule
	 *            the owner rule.
	 * @param origin
	 *            the sheet origin.
	 */
	protected BaseCSSStyleSheet(String title, MediaQueryList media, AbstractCSSRule ownerRule, byte origin) {
		super(title);
		this.ownerRule = ownerRule;
		this.destinationMedia = media;
		sheetOrigin = origin;
	}

	protected void copyAllTo(BaseCSSStyleSheet myCopy) {
		copyFieldsTo(myCopy);
		copyRulesTo(myCopy);
	}

	protected void copyFieldsTo(BaseCSSStyleSheet myCopy) {
		myCopy.currentInsertionIndex = this.currentInsertionIndex;
		myCopy.setDisabled(getDisabled());
		myCopy.namespaces = this.namespaces;
		if (parent != null) {
			myCopy.setParentStyleSheet(parent);
		}
	}

	protected void copyRulesTo(BaseCSSStyleSheet myCopy) {
		myCopy.cssRules = new CSSRuleArrayList(cssRules.getLength());
		Iterator<AbstractCSSRule> it = cssRules.iterator();
		while (it.hasNext()) {
			myCopy.cssRules.add(it.next().clone(myCopy));
		}
	}

	/**
	 * Get the stylesheet factory used to produce this sheet.
	 * 
	 * @return the stylesheet factory.
	 */
	@Override
	abstract public BaseCSSStyleSheetFactory getStyleSheetFactory();

	@Override
	public AbstractCSSRule getOwnerRule() {
		return ownerRule;
	}

	@Override
	public Node getOwnerNode() {
		return null;
	}

	@Override
	public MediaQueryList getMedia() {
		return destinationMedia;
	}

	@Override
	protected void setMedia(MediaQueryList media) throws DOMException {
		if (media.hasErrors()) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, "Media query has errors");
		}
		destinationMedia = media;
	}

	@Override
	public byte getOrigin() {
		return sheetOrigin;
	}

	@Override
	public CSSRuleArrayList getCssRules() {
		return cssRules;
	}

	/**
	 * Used to insert a new rule into the style sheet. The new rule now becomes
	 * part of the cascade.
	 * 
	 * @param rule
	 *            The parsable text representing the rule. For rule sets this
	 *            contains both the selector and the style declaration. For
	 *            at-rules, this specifies both the at-identifier and the rule
	 *            content.
	 * @param index
	 *            The index within the style sheet's rule list of the rule
	 *            before which to insert the specified rule. If the specified
	 *            index is equal to the length of the style sheet's rule
	 *            collection, the rule will be added to the end of the style
	 *            sheet.
	 * @return The index within the style sheet's rule collection of the newly
	 *         inserted rule.
	 * @throws DOMException
	 *             HIERARCHY_REQUEST_ERR: Raised if the rule cannot be inserted
	 *             at the specified index e.g. if an <code>@import</code> rule
	 *             is inserted after a standard rule set or other at-rule. <br>
	 *             INDEX_SIZE_ERR: Raised if the specified index is not a valid
	 *             insertion point. <br>
	 *             NO_MODIFICATION_ALLOWED_ERR: Raised if this style sheet is
	 *             readonly. <br>
	 *             SYNTAX_ERR: Raised if the specified rule has a syntax error
	 *             and is unparsable.
	 */
	@Override
	public int insertRule(String rule, int index) throws DOMException {
		if (index > getCssRules().getLength() || index < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, "Invalid index: " + index);
		}
		InputSource source = new InputSource();
		Reader re = new StringReader(rule);
		source.setCharacterStream(re);
		// The following may cause an (undocumented)
		// DOMException.NOT_SUPPORTED_ERR
		Parser psr = getStyleSheetFactory().createSACParser();
		CSSDocumentHandler handler = createDocumentHandler(getOrigin(), true);
		psr.setDocumentHandler(handler);
		psr.setErrorHandler(handler);
		currentInsertionIndex = index - 1;
		try {
			if (psr instanceof Parser2) {
				((Parser2) psr).parseRule(source, handler);
			} else {
				psr.parseRule(source);
			}
		} catch (CSSNamespaceParseException e) {
			DOMException ex = new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		if (currentInsertionIndex != index && handler.outOfRuleException != null) {
			DOMException ex;
			if (handler.outOfRuleException.getClass() == CSSNamespaceParseException.class) {
				ex = new DOMException(DOMException.NAMESPACE_ERR, handler.outOfRuleException.getMessage());
			} else {
				ex = new DOMException(DOMException.SYNTAX_ERR, handler.outOfRuleException.getMessage());
			}
			ex.initCause(handler.outOfRuleException);
			throw ex;
		}
		return currentInsertionIndex;
	}

	/**
	 * Inserts a rule in the current insertion point (generally after the last rule).
	 * 
	 * @param cssrule
	 *            the rule to be inserted.
	 * @throws DOMException
	 *             NAMESPACE_ERR if the rule could not be added due to a namespace-related
	 *             error.
	 */
	@Override
	public void addRule(AbstractCSSRule cssrule) throws DOMException {
		if (cssrule.getType() == ExtendedCSSRule.NAMESPACE_RULE) {
			CSSNamespaceRule nsrule = (CSSNamespaceRule) cssrule;
			if (namespaces.containsKey(nsrule.getNamespaceURI())) {
				throw new DOMException(DOMException.NAMESPACE_ERR,
						"Rule for this namespace URI already exists: " + nsrule.getNamespaceURI());
			}
			registerNamespace(nsrule);
		}
		cssrule.setParentStyleSheet(this);
		addLocalRule(cssrule);
	}

	@Override
	protected void registerNamespace(CSSNamespaceRule nsrule) {
		namespaces.put(nsrule.getNamespaceURI(), nsrule.getPrefix());
	}

	@Override
	protected void unregisterNamespace(String namespaceURI) {
		namespaces.remove(namespaceURI);
	}

	/**
	 * Inserts a local rule in the current insertion point (generally after the
	 * last rule).
	 * 
	 * @param cssrule
	 *            the rule to be inserted.
	 */
	protected void addLocalRule(CSSRule cssrule) {
		currentInsertionIndex = cssRules.insertRule(cssrule, ++currentInsertionIndex);
	}

	/**
	 * Deletes a rule from the style sheet.
	 * 
	 * @param index
	 *            The index within the style sheet's rule list of the rule to
	 *            remove.
	 * @throws DOMException
	 *             INDEX_SIZE_ERR: Raised if the specified index does not
	 *             correspond to a rule in the style sheet's rule list. <br>
	 *             NAMESPACE_ERR: Raised if the rule is a namespace rule and 
	 *             this style sheet contains style rules with that namespace.
	 */
	@Override
	public void deleteRule(int index) throws DOMException {
		ExtendedCSSRule rule;
		try {
			rule = cssRules.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, e.getMessage());
		}
		if (rule.getType() == ExtendedCSSRule.NAMESPACE_RULE
				&& containsRuleWithNamespace(((CSSNamespaceRule) rule).getNamespaceURI())) {
			throw new DOMException(DOMException.NAMESPACE_ERR, "There are style rules with ");
		}
		cssRules.remove(index);
	}

	private boolean containsRuleWithNamespace(String namespaceURI) {
		Iterator<AbstractCSSRule> it = cssRules.iterator();
		while (it.hasNext()) {
			ExtendedCSSRule rule = it.next();
			if (rule.getType() == CSSRule.STYLE_RULE) {
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (selectorListHasNamespace(stylerule.getSelectorList(), namespaceURI)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds the rules contained by the supplied style sheet, if that sheet is
	 * not disabled.
	 * <p>
	 * If the provided sheet does not target all media, a media rule is created.
	 * 
	 * @param sheet
	 *            the sheet whose rules are to be added.
	 */
	@Override
	public void addStyleSheet(AbstractCSSStyleSheet sheet) {
		if (!sheet.getDisabled()) {
			MediaQueryList mediaList = sheet.getMedia();
			if (mediaList.isAllMedia()) { // all media
				CSSRuleArrayList otherRules = sheet.getCssRules();
				addRuleList(otherRules, sheet, 0);
			} else if (!mediaList.isNotAllMedia()) {
				CSSRuleArrayList otherRules = sheet.getCssRules();
				// Create a Media rule
				MediaRule mrule = createCSSMediaRule(mediaList);
				addToMediaRule(mrule, otherRules, sheet, 0);
				addLocalRule(mrule);
			}
			getErrorHandler().mergeState(sheet.getErrorHandler());
		}
	}

	private void addRuleList(CSSRuleArrayList otherRules, AbstractCSSStyleSheet sheet, int importCount) {
		int orl = otherRules.getLength();
		for (int i = 0; i < orl; i++) {
			AbstractCSSRule oRule = otherRules.item(i);
			if (oRule.getType() != CSSRule.IMPORT_RULE) {
				addLocalRule(oRule.clone(sheet));
			} else {
				importCount++;
				if (importCount == MAX_IMPORT_RECURSION) {
					DOMException ex = new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
							"Too many nested imports");
					getErrorHandler().badAtRule(ex, oRule.getCssText());
					return;
				}
				// We clone with this as parent, to receive the errors
				ImportRule imp = (ImportRule) oRule.clone(this);
				AbstractCSSStyleSheet impSheet = imp.getStyleSheet();
				CSSRuleArrayList impRules = impSheet.getCssRules();
				MediaQueryList media = imp.getMedia();
				if (media.isAllMedia()) {
					addRuleList(impRules, impSheet, importCount);
				} else if (!media.isNotAllMedia()) {
					// Create a Media rule
					MediaRule mrule = createCSSMediaRule(imp.getMedia());
					addToMediaRule(mrule, impRules, impSheet, importCount);
					addLocalRule(mrule);
				}
			}
		}
	}

	private void addToMediaRule(MediaRule mrule, CSSRuleArrayList otherRules, AbstractCSSStyleSheet sheet,
			int importCount) {
		// Fill the media rule
		int orl = otherRules.getLength();
		for (int i = 0; i < orl; i++) {
			AbstractCSSRule oRule = otherRules.item(i);
			if (oRule.getType() != CSSRule.IMPORT_RULE) {
				mrule.addRule(oRule.clone(sheet));
			} else {
				importCount++;
				if (importCount == MAX_IMPORT_RECURSION) {
					DOMException ex = new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
							"Too many nested imports");
					getErrorHandler().badAtRule(ex, oRule.getCssText());
					return;
				}
				// We clone with this as parent, to receive the errors
				ImportRule imp = (ImportRule) oRule.clone(this);
				AbstractCSSStyleSheet impSheet = imp.getStyleSheet();
				CSSRuleArrayList impRules = impSheet.getCssRules();
				MediaQueryList media = imp.getMedia();
				if (mrule.getMedia().equals(media)) {
					addToMediaRule(mrule, impRules, impSheet, importCount);
				} else {
					// Create a Media rule
					MediaRule nestedMRule = createCSSMediaRule(media);
					addToMediaRule(nestedMRule, impRules, impSheet, importCount);
					mrule.addRule(nestedMRule);
				}
			}
		}
	}

	private static boolean selectorListHasNamespace(SelectorList selist, String namespaceURI) {
		for(int i = 0; i < selist.getLength(); i++) {
			if (selectorHasNamespace(selist.item(i), namespaceURI)) {
				return true;
			}
		}
		return false;
	}

	private static boolean selectorHasNamespace(Selector sel, String namespaceURI) {
		switch (sel.getSelectorType()) {
		case Selector.SAC_ELEMENT_NODE_SELECTOR:
		case Selector.SAC_ANY_NODE_SELECTOR:
			return namespaceURI.equals(((ElementSelector) sel).getNamespaceURI());
		case Selector.SAC_CONDITIONAL_SELECTOR:
			ConditionalSelector csel = (ConditionalSelector) sel;
			return selectorHasNamespace(csel.getSimpleSelector(), namespaceURI) ||
					conditionHasNamespace(csel.getCondition(), namespaceURI);
		case Selector.SAC_CHILD_SELECTOR:
		case Selector.SAC_DESCENDANT_SELECTOR:
		case Selector2.SAC_COLUMN_COMBINATOR_SELECTOR:
			DescendantSelector dsel = (DescendantSelector) sel;
			return selectorHasNamespace(dsel.getAncestorSelector(), namespaceURI) ||
					selectorHasNamespace(dsel.getSimpleSelector(), namespaceURI);
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
		case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
			SiblingSelector ssel = (SiblingSelector) sel;
			return selectorHasNamespace(ssel.getSelector(), namespaceURI) ||
					selectorHasNamespace(ssel.getSiblingSelector(), namespaceURI);
		default:
			return false;
		}
	}

	private static boolean conditionHasNamespace(Condition condition, String namespaceURI) {
		switch (condition.getConditionType()) {
		case Condition.SAC_ATTRIBUTE_CONDITION:
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
		case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
		case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
		case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
			AttributeCondition acond = (AttributeCondition) condition;
			return namespaceURI.equals(acond.getNamespaceURI());
		case Condition.SAC_AND_CONDITION:
		case Condition.SAC_OR_CONDITION:
			CombinatorCondition ccond = (CombinatorCondition) condition;
			return conditionHasNamespace(ccond.getFirstCondition(), namespaceURI) ||
					conditionHasNamespace(ccond.getSecondCondition(), namespaceURI);
		case Condition.SAC_POSITIONAL_CONDITION:
			SelectorList oflist = ((PositionalCondition2) condition).getOfList();
			if (oflist != null) {
				return selectorListHasNamespace(oflist, namespaceURI);
			}
			break;
		case Condition2.SAC_SELECTOR_ARGUMENT_CONDITION:
			ArgumentCondition argcond = (ArgumentCondition) condition;
			SelectorList selist = argcond.getSelectors();
			if (selist != null) {
				return selectorListHasNamespace(selist, namespaceURI);
			}
		}
		return false;
	}

	@Override
	public StyleRule createCSSStyleRule() {
		return new StyleRule(this, getOrigin());
	}

	@Override
	public CounterStyleRule createCSSCounterStyleRule() {
		return new CounterStyleRule(this, getOrigin());
	}

	@Override
	public CSSFontFaceRule createCSSFontFaceRule() {
		return new FontFaceRule(this, getOrigin());
	}

	@Override
	public FontFeatureValuesRule createCSSFontFeatureValuesRule() {
		return new FontFeatureValuesRule(this, getOrigin());
	}

	@Override
	public ImportRule createCSSImportRule(MediaQueryList mediaList, String href) {
		if (href == null) {
			throw new NullPointerException("Null @import URI");
		}
		return new ImportRule(this, ((MediaListAccess) mediaList).unmodifiable(), href, getOrigin());
	}

	@Override
	public KeyframesRule createCSSKeyframesRule() {
		return new KeyframesRule(this, getOrigin());
	}

	@Override
	public MarginRule createCSSMarginRule(String name) {
		return new MarginRule(this, getOrigin(), name);
	}

	@Override
	public MediaRule createCSSMediaRule(MediaQueryList mediaList) {
		return new MediaRule(this, mediaList, getOrigin());
	}

	/*
	 * Issues: if the rule is created but not added to the sheet, it is still
	 * accounted for the namespace URI - prefix mapping.
	 */
	@Override
	public NamespaceRule createCSSNamespaceRule(String prefix, String namespaceUri) {
		if (prefix == null || namespaceUri == null) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Null parameter");
		}
		return new NamespaceRule(this, getOrigin(), prefix, namespaceUri);
	}

	@Override
	public PageRule createCSSPageRule() {
		return new PageRule(this, getOrigin());
	}

	@Override
	public SupportsRule createCSSSupportsRule() {
		return new SupportsRule(this, getOrigin());
	}

	@Override
	public ViewportRule createCSSViewportRule() {
		return new ViewportRule(this, getOrigin());
	}

	@Override
	public UnknownRule createCSSUnknownRule() {
		return new UnknownRule(this, getOrigin());
	}

	@Override
	protected BaseCSSStyleDeclaration createCSSStyleDeclaration(BaseCSSDeclarationRule rule) {
		if (rule.getType() == CSSRule.FONT_FACE_RULE) {
			return new WrappedCSSStyleDeclaration(rule);
		}
		BaseCSSStyleSheetFactory factory = getStyleSheetFactory();
		if (!factory.hasCompatValueFlags()) {
			return new BaseCSSStyleDeclaration(rule);
		}
		return new CompatStyleDeclaration(rule);
	}

	@Override
	public BaseCSSStyleDeclaration createCSSStyleDeclaration() {
		return new BaseCSSStyleDeclaration();
	}

	@Override
	public boolean hasRuleErrorsOrWarnings() {
		return hasRuleErrorsOrWarnings(cssRules);
	}

	private static boolean hasRuleErrorsOrWarnings(CSSRuleArrayList rules) {
		for (ExtendedCSSRule rule : rules) {
			if (rule instanceof BaseCSSDeclarationRule) {
				BaseCSSDeclarationRule stylerule = (BaseCSSDeclarationRule) rule;
				if (stylerule.hasErrorsOrWarnings()) {
					return true;
				}
			} else if (rule instanceof GroupingRule) {
				if (hasRuleErrorsOrWarnings(((GroupingRule) rule).getCssRules())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public SheetErrorHandler getErrorHandler() {
		if (sheetErrorHandler == null) {
			sheetErrorHandler = getStyleSheetFactory().createSheetErrorHandler(this);
		}
		return sheetErrorHandler;
	}

	@Override
	protected ErrorHandler getDocumentErrorHandler() {
		ErrorHandler eh;
		Node owner = null;
		AbstractCSSStyleSheet parent = this;
		while (parent != null) {
			owner = parent.getOwnerNode();
			if (owner != null) {
				break;
			}
			parent = parent.getParentStyleSheet();
		}
		if (owner != null) {
			eh = ((CSSDocument) owner.getOwnerDocument()).getErrorHandler();
		} else {
			eh = new StandAloneErrorHandler();
		}
		return eh;
	}

	@Override
	public String getType() {
		return "text/css";
	}

	/**
	 * Gets the namespace prefix associated to the given URI.
	 * 
	 * @param uri
	 *            the namespace URI string.
	 * @return the namespace prefix.
	 */
	@Override
	protected String getNamespacePrefix(String uri) {
		return namespaces.get(uri);
	}

	/**
	 * Has this style sheet defined a default namespace ?
	 * 
	 * @return <code>true</code> if a default namespace was defined, <code>false</code> otherwise.
	 */
	@Override
	protected boolean hasDefaultNamespace() {
		return namespaces.containsValue("");
	}

	@Override
	public boolean getDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public AbstractCSSStyleSheet getParentStyleSheet() {
		return parent;
	}

	protected void setParentStyleSheet(AbstractCSSStyleSheet parent) {
		this.parent = parent;
		sheetErrorHandler = parent.getErrorHandler();
	}

	@Override
	public String getHref() {
		if (href == null && ownerRule != null) {
			return ownerRule.getParentStyleSheet().getHref();
		}
		return href;
	}

	@Override
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * Load the styles from <code>url</code> into this style sheet.
	 * 
	 * @param url
	 *            the url to load the style sheet from.
	 * @param referrerPolicy
	 *            the content of the <code>referrerpolicy</code> content attribute, if any, or
	 *            the empty string.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, <code>false</code> otherwise.
	 * @throws DOMException
	 *             if there is a serious problem parsing the style sheet.
	 * @throws IOException
	 *             if a problem appears fetching the url contents.
	 */
	@Override
	public boolean loadStyleSheet(URL url, String referrerPolicy) throws DOMException, IOException {
		final URLConnection ucon = openConnection(url, referrerPolicy);
		try {
			java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Void>() {
				@Override
				public Void run() throws IOException {
					ucon.connect();
					return null;
				}
			});
		} catch (PrivilegedActionException e) {
			throw (IOException) e.getException();
		}
		InputStream is = ucon.getInputStream();
		String contentEncoding = ucon.getContentEncoding();
		String conType = ucon.getContentType();
		Reader re = AgentUtil.inputStreamToReader(is, conType, contentEncoding, "utf-8");
		InputSource source = new InputSource(re);
		// Parse
		boolean result;
		try {
			setHref(url.toExternalForm());
			result = parseCSSStyleSheet(source);
		} catch (DOMException e) {
			getDocumentErrorHandler().linkedSheetError(e, this);
			throw e;
		} catch (IOException e) {
			getDocumentErrorHandler().linkedSheetError(e, this);
			throw e;
		} catch (CSSException e) {
			getDocumentErrorHandler().linkedSheetError(e, this);
			throw e;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		if (ucon instanceof HttpURLConnection) {
			((HttpURLConnection) ucon).disconnect();
		}
		return result;
	}

	/**
	 * Returns a list of rules that apply to a style where the given longhand property
	 * is set (either explicitly or through a shorthand).
	 * <p>
	 * Grouping rules are scanned too, regardless of the medium or condition.
	 * 
	 * @param longhandPropertyName
	 *            the longhand property name.
	 * @return the list of rules, or <code>null</code> if no rules declare that property,
	 *         or the property is a shorthand.
	 */
	@Override
	public CSSRuleArrayList getRulesForProperty(String longhandPropertyName) {
		CSSRuleArrayList list = new CSSRuleArrayList();
		scanRulesForPropertyDeclaration(cssRules, longhandPropertyName, list);
		if (list.isEmpty()) {
			return null;
		}
		return list;
	}

	private static void scanRulesForPropertyDeclaration(CSSRuleArrayList rules, String propertyName,
			CSSRuleArrayList subset) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
			case CSSRule.PAGE_RULE:
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (((BaseCSSStyleDeclaration) stylerule.getStyle()).isPropertySet(propertyName)) {
					subset.add(stylerule);
				}
				break;
			case CSSRule.MEDIA_RULE:
			case ExtendedCSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				scanRulesForPropertyDeclaration(grouping.getCssRules(), propertyName, subset);
				break;
			}
		}
	}

	/**
	 * Returns an array of selectors that apply to a style where the given longhand property
	 * is set (either explicitly or through a shorthand).
	 * <p>
	 * Grouping rules are scanned too, regardless of the medium or condition.
	 * 
	 * @param longhandPropertyName
	 *            the longhand property name.
	 * @return the array of selectors, or <code>null</code> if no rules declare that property,
	 *         or the property is a shorthand.
	 */
	@Override
	public Selector[] getSelectorsForProperty(String longhandPropertyName) {
		LinkedList<Selector> selectors = new LinkedList<Selector>();
		scanRulesForPropertyDeclaration(cssRules, longhandPropertyName, selectors);
		if (selectors.isEmpty()) {
			return null;
		}
		return selectors.toArray(new Selector[0]);
	}

	private static void scanRulesForPropertyDeclaration(CSSRuleArrayList rules, String propertyName,
			LinkedList<Selector> selectors) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
			case CSSRule.PAGE_RULE:
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (((BaseCSSStyleDeclaration) stylerule.getStyle()).isPropertySet(propertyName)) {
					SelectorList list = stylerule.getSelectorList();
					for (int i = 0; i < list.getLength(); i++) {
						selectors.add(list.item(i));
					}
				}
				break;
			case CSSRule.MEDIA_RULE:
			case ExtendedCSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				scanRulesForPropertyDeclaration(grouping.getCssRules(), propertyName, selectors);
				break;
			}
		}
	}

	/**
	 * Returns an array of selectors that apply to a style where the given property was
	 * explicitly set to the given declared value.
	 * <p>
	 * Media rules are scanned too, regardless of the specific medium.
	 * </p>
	 * <p>
	 * Beware that using this method with computed instead of declared values may not give the
	 * expected results.
	 * </p>
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param declaredValue
	 *            the property's declared value.
	 * @return the array of selectors, or <code>null</code> if no rules contain that
	 *         property-value pair.
	 */
	public Selector[] getSelectorsForPropertyValue(String propertyName, String declaredValue) {
		LinkedList<Selector> selectors = new LinkedList<Selector>();
		scanRulesForValue(cssRules, propertyName, declaredValue, selectors);
		if (selectors.isEmpty()) {
			return null;
		}
		return selectors.toArray(new Selector[0]);
	}

	private static void scanRulesForValue(CSSRuleArrayList rules, String propertyName, String value,
			LinkedList<Selector> selectors) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
			case CSSRule.PAGE_RULE:
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (value.equalsIgnoreCase(stylerule.getStyle().getPropertyValue(propertyName))) {
					SelectorList list = stylerule.getSelectorList();
					for (int i = 0; i < list.getLength(); i++) {
						selectors.add(list.item(i));
					}
				}
				break;
			case CSSRule.MEDIA_RULE:
				MediaRule mediarule = (MediaRule) rule;
				scanRulesForValue(mediarule.getCssRules(), propertyName, value, selectors);
				break;
			}
		}
	}

	protected String getTargetMedium() {
		return null;
	}

	@Override
	public String toMinifiedString() {
		return getCssRules().toMinifiedString();
	}

	@Override
	public String toString() {
		StyleFormattingContext context = getStyleSheetFactory().getStyleFormattingFactory()
				.createStyleFormattingContext();
		BufferSimpleWriter sw = new BufferSimpleWriter(getCssRules().getLength() * 20 + 32);
		try {
			getCssRules().writeCssText(sw, context);
			context.endRuleList(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public String toStyleString() {
		StyleFormattingContext context = getStyleSheetFactory().getStyleFormattingFactory()
				.createStyleFormattingContext();
		BufferSimpleWriter sw = new BufferSimpleWriter(getCssRules().getLength() * 20 + 92);
		try {
			sw.write("<style type=\"text/css\"");
			if (!destinationMedia.isAllMedia()) {
				sw.write(" media=\"");
				sw.write(destinationMedia.getMediaText());
				sw.write('"');
			}
			if (getTitle() != null) {
				sw.write(" title=\"");
				sw.write(getTitle());
				sw.write('"');
			}
			sw.write('>');
			sw.newLine();
			getCssRules().writeCssText(sw, context);
			context.endRuleList(sw);
			sw.newLine();
			sw.write("</style>");
			sw.newLine();
		} catch (IOException e) {
		}
		return sw.toString();
	}

	/**
	 * Creates a SAC document handler implemented by this style sheet.
	 * 
	 * @param origin the origin for this style sheet.
	 * @param ignoreComments true if comments have to be ignored by the handler.
	 * @return the new SAC document handler.
	 */
	CSSDocumentHandler createDocumentHandler(byte origin, boolean ignoreComments) {
		return new CSSDocumentHandler(origin, ignoreComments);
	}

	/**
	 * Parses a style sheet.
	 * 
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with
	 * a highest precedence origin.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param source
	 *            the SAC input source.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws DOMException
	 *             if a problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	@Override
	public boolean parseCSSStyleSheet(InputSource source) throws DOMException, IOException {
		return parseCSSStyleSheet(source, false);
	}

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with
	 * a highest precedence origin.
	 * <p>
	 * If <code>ignoreComments</code> is false, the comments preceding a rule
	 * will be available through {@link AbstractCSSRule#getPrecedingComments()}.
	 * <p>
	 * This method resets the state of this sheet's error handler.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param source
	 *            the SAC input source.
	 * @param ignoreComments
	 *            true if comments have to be ignored.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws DOMException
	 *             if a problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	@Override
	public boolean parseCSSStyleSheet(InputSource source, boolean ignoreComments) throws DOMException, IOException {
		getErrorHandler().reset();
		// Find origin
		byte origin = getOrigin();
		// Scan rules for origins with higher priorities
		for (AbstractCSSRule rule : getCssRules()) {
			byte ruleo = rule.getOrigin();
			if (ruleo < origin) {
				origin = ruleo;
			}
		}
		Parser parser = getStyleSheetFactory().createSACParser();
		DocumentHandler handler = createDocumentHandler(origin, ignoreComments);
		parser.setDocumentHandler(handler);
		parser.setErrorHandler((org.w3c.css.sac.ErrorHandler) handler);
		try {
			parser.parseStyleSheet(source);
		} catch (CSSNamespaceParseException e) {
			DOMException ex = new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSException e) {
			DOMException ex;
			switch (e.getCode()) {
			case CSSException.SAC_NOT_SUPPORTED_ERR:
				ex = new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
				break;
			case CSSException.SAC_SYNTAX_ERR:
				ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
				break;
			default:
				ex = new DOMException(DOMException.INVALID_ACCESS_ERR, e.getMessage());
			}
			ex.initCause(e);
			throw ex;
		} catch (RuntimeException e) {
			String message = e.getMessage();
			String href = getHref();
			if (href != null) {
				message = "Error in stylesheet at " + href + ": " + message;
			}
			DOMException ex = new DOMException(DOMException.INVALID_STATE_ERR, message);
			ex.initCause(e);
			throw ex;
		}
		return !getErrorHandler().hasSacErrors();
	}

	class CSSDocumentHandler implements DocumentHandler, org.w3c.css.sac.ErrorHandler, NamespaceMap {

		private AbstractCSSRule currentRule = null;

		private byte sheetOrigin;

		private final LinkedList<String> comments;

		// switch for ignoring rules based on target media
		private boolean ignoreRulesForMedia = false;

		private boolean ignoreImports = false;

		private CSSParseException outOfRuleException = null;

		CSSDocumentHandler(byte origin, boolean ignoreComments) {
			super();
			this.sheetOrigin = origin;
			if (!ignoreComments) {
				comments = new LinkedList<String>();
			} else {
				comments = null;
			}
		}

		@Override
		public void startDocument(InputSource source) throws CSSException {
			// Starting StyleSheet processing
			currentRule = null;
			ignoreRulesForMedia = false;
			ignoreImports = false;
			if (comments != null) {
				comments.clear();
			}
		}

		@Override
		public void endDocument(InputSource source) throws CSSException {
			// Ending StyleSheet processing
			if (comments != null) {
				comments.clear();
			}
		}

		@Override
		public void comment(String text) throws CSSException {
			if ((currentRule == null || currentRule instanceof GroupingRule) && comments != null) {
				comments.add(text);
			}
		}

		@Override
		public void ignorableAtRule(String atRule) throws CSSException {
			// Ignorable @-rule
			AbstractCSSRule rule;
			int tentNameLen = atRule.length();
			if (tentNameLen > 21) {
				tentNameLen = 21;
			}
			String firstchars = atRule.trim().substring(0, tentNameLen).toLowerCase(Locale.ROOT);
			if (firstchars.startsWith("@supports")) {
				rule = createCSSSupportsRule();
			} else if (firstchars.startsWith("@keyframes ")) {
				rule = createCSSKeyframesRule();
			} else if (firstchars.startsWith("@viewport")) {
				rule = createCSSViewportRule();
			} else if (firstchars.startsWith("@counter-style ")) {
				rule = createCSSCounterStyleRule();
			} else if (firstchars.equals("@font-feature-values ")) {
				rule = createCSSFontFeatureValuesRule();
			} else {
				rule = createCSSUnknownRule();
				if (atRule.charAt(1) != '-') {
					// Unknown non-custom rule
					getErrorHandler().unknownRule(atRule);
				}
			}
			try {
				rule.setCssText(atRule);
			} catch (DOMException e) {
				getErrorHandler().badAtRule(e, atRule);
				return;
			}
			setCommentsToRule(rule);
			if (currentRule != null) {
				addToCurrentRule(rule);
			} else {
				// Inserting rule into sheet
				addLocalRule(rule);
				resetCurrentRule();
			}
		}

		private void addToCurrentRule(AbstractCSSRule rule) {
			try {
				((GroupingRule) currentRule).addRule(rule);
			} catch (ClassCastException e) {
				DOMException ex = new DOMException(DOMException.SYNTAX_ERR,
						"Found @-rule inside a non-grouping rule of type: " + currentRule.getType());
				getErrorHandler().badAtRule(ex, rule.getCssText());
			}
		}

		@Override
		public void namespaceDeclaration(String prefix, String uri) throws CSSException {
			// Setting namespace uri
			namespaces.put(uri, prefix);
			if (!ignoreRulesForMedia) {
				NamespaceRule rule = createCSSNamespaceRule(prefix, uri);
				if (currentRule != null) {
					addToCurrentRule(rule);
				} else {
					// Inserting rule into sheet
					addLocalRule(rule);
				}
				resetCurrentRule();
			}
		}

		@Override
		public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI)
				throws CSSException, DOMException {
			// Ignore any '@import' rule that occurs inside a block or after any
			// non-ignored statement other than an @charset or an @import rule
			// (CSS 2.1 §4.1.5)
			if (ignoreImports) {
				SheetErrorHandler eh = getErrorHandler();
				if (eh != null) {
					eh.ignoredImport(uri);
				}
				return;
			}
			if (((MediaListAccess) destinationMedia).match(media)) {
				MediaQueryList mql = MediaQueryFactory.createMediaList(media);
				if (!mql.isNotAllMedia()) {
					if (currentRule == null) { // That should be always true
						// Importing rule from uri
						ImportRule imp = createCSSImportRule(mql, uri);
						setCommentsToRule(imp);
						addLocalRule(imp);
					}
				} else {
					getErrorHandler().badMediaList(media);
				}
			} else { // Ignoring @import from uri due to target media mismatch
				resetCommentStack();
			}
		}

		@Override
		public void startMedia(SACMediaList media) throws CSSException {
			// Starting @media block for media
			ignoreImports = true;
			SheetErrorHandler eh;
			if (media.getLength() != 0) {
				MediaQueryList mlist = MediaQueryFactory.createMediaList(media);
				if (mlist.hasErrors() && (eh = getErrorHandler()) != null) {
					eh.badMediaList(media);
				}
				if (currentRule != null) {
					if (currentRule.getType() == CSSRule.MEDIA_RULE) {
						MediaRule rule = new MediaRule(BaseCSSStyleSheet.this, mlist, sheetOrigin);
						((GroupingRule) currentRule).addRule(rule);
						currentRule = rule;
						setCommentsToRule(currentRule);
						ignoreRulesForMedia = false;
					} else if ((eh = getErrorHandler()) != null) {
						eh.sacMalfunction("Unexpected media rule inside of: " + currentRule.getCssText());
						ignoreRulesForMedia = true;
						return;
					}
				} else {
					if (mlist.isNotAllMedia() || mlist.hasErrors()) {
						ignoreRulesForMedia = true;
					} else {
						currentRule = new MediaRule(BaseCSSStyleSheet.this, mlist, sheetOrigin);
						setCommentsToRule(currentRule);
						ignoreRulesForMedia = false; // this should not be needed - just in case
					}
				}
			} else {
				// @media rule with empty media list
				ignoreRulesForMedia = true;
			}
		}

		@Override
		public void endMedia(SACMediaList media) throws CSSException {
			if (ignoreRulesForMedia) {
				ignoreRulesForMedia = false;
				resetCommentStack();
			} else {
				if (currentRule != null) {
					AbstractCSSRule pRule = currentRule.getParentRule();
					if (pRule == null) {
						// Inserting @media rule into sheet
						addLocalRule(currentRule);
						resetCurrentRule();
					} else {
						resetCurrentRule();
						// Restore parent rule
						currentRule = pRule;
					}
				}
			}
		}

		@Override
		public void startPage(String name, String pseudo_page) throws CSSException {
			ignoreImports = true;
			if (!ignoreRulesForMedia) {
				if (currentRule instanceof PageRule) {
					// Margin rule or error
					MarginRule marginRule = createCSSMarginRule(name);
					marginRule.setParentRule(currentRule);
					currentRule = marginRule;
				} else {
					PageRule pageRule = createCSSPageRule();
					pageRule.setParentRule(currentRule);
					currentRule = pageRule;
					if (name != null) {
						pageRule.setSelectorText(name);
					}
					if (pseudo_page != null) {
						Parser parser = getStyleSheetFactory().createSACParser();
						InputSource source = new InputSource(new StringReader(pseudo_page));
						try {
							pageRule.setSelectorList(parser.parseSelectors(source));
						} catch (IOException e) {
						}
					} else {
						pageRule.setSelectorText("");
					}
				}
				setCommentsToRule(currentRule);
			}
		}

		@Override
		public void endPage(String name, String pseudo_page) throws CSSException {
			if (!ignoreRulesForMedia) {
				// Inserting @page rule into sheet
				if (currentRule != null) {
					AbstractCSSRule pRule = currentRule.getParentRule();
					if (currentRule instanceof PageRule) {
						if (pRule == null) {
							addLocalRule(currentRule);
							resetCurrentRule();
						} else {
							((GroupingRule) pRule).addRule(currentRule);
							resetCommentStack();
							currentRule = pRule;
						}
					} else { // else if (currentRule instanceof OMCSSMarginRule) {
						PageRule pageRule = (PageRule) pRule;
						pageRule.addMarginRule((CSSMarginRule) currentRule);
						resetCommentStack();
					}
					currentRule = pRule;
				}
			} // else { Ignored @page: target media mismatch
		}

		@Override
		public void startFontFace() throws CSSException {
			ignoreImports = true;
			if (!ignoreRulesForMedia) {
				FontFaceRule rule = new FontFaceRule(BaseCSSStyleSheet.this, sheetOrigin);
				rule.setParentRule(currentRule);
				currentRule = rule;
			} // else { Ignoring @font-face: target media mismatch
			setCommentsToRule(currentRule);
		}

		@Override
		public void endFontFace() throws CSSException {
			if (!ignoreRulesForMedia) {
				if (currentRule != null) {
					AbstractCSSRule pRule = currentRule.getParentRule();
					if (pRule == null) {
						// Inserting @font-face rule into sheet
						addLocalRule(currentRule);
						resetCurrentRule();
					} else {
						addCurrentRuleToRule(pRule);
						resetCommentStack();
						// Restore parent rule
						currentRule = pRule;
					}
				}
			}
		}

		private void addCurrentRuleToRule(AbstractCSSRule rule) {
			try {
				((GroupingRule) rule).addRule(currentRule);
			} catch (ClassCastException e) {
				DOMException ex = new DOMException(DOMException.SYNTAX_ERR,
						"Found @-rule inside a non-grouping rule of type: " + rule.getType());
				getErrorHandler().badAtRule(ex, currentRule.getCssText());
			}
		}

		@Override
		public void startSelector(SelectorList selectors) throws CSSException {
			ignoreImports = true;
			if (!ignoreRulesForMedia) {
				StyleRule styleRule = BaseCSSStyleSheet.this.createCSSStyleRule();
				if (currentRule != null) {
					styleRule.setParentRule(currentRule);
				}
				currentRule = styleRule;
				((CSSStyleDeclarationRule) currentRule).setSelectorList(selectors);
				setCommentsToRule(currentRule);
			} // else { Ignoring rule for these selectors due to target media mismatch
		}

		@Override
		public void endSelector(SelectorList selectors) throws CSSException {
			if (!ignoreRulesForMedia && currentRule != null && currentRule.getType() == CSSRule.STYLE_RULE) {
				BaseCSSRule pRule = (BaseCSSRule) currentRule.getParentRule();
				if (((StyleRule) currentRule).getStyle().getLength() == 0) {
					SheetErrorHandler eh = getErrorHandler();
					if (eh != null) {
						eh.emptyStyleRule(((StyleRule) currentRule).getSelectorText());
					}
				} else {
					if (pRule == null) {
						// Inserting rule into sheet
						if (currentRule != null)
							addLocalRule(currentRule);
					} else {
						((GroupingRule) pRule).addRule(currentRule);
					}
				}
				resetCommentStack();
				currentRule = pRule;
			}
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) throws CSSException {
			if (!ignoreRulesForMedia) {
				String importantString = null;
				if (important) {
					importantString = "important";
				} else {
					importantString = null;
				}
				if (currentRule != null) {
					try {
						((BaseCSSStyleDeclaration) ((BaseCSSDeclarationRule) currentRule).getStyle()).setProperty(name,
								value, importantString);
					} catch (RuntimeException e) {
						CSSPropertyValueException ex = new CSSPropertyValueException(e);
						ex.setValueText(value.toString());
						((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().wrongValue(name, ex);
					}
				} else {
					/*
					 * A property was received for being processed outside of a rule. This should never
					 * happen, and if it happens it means that the SAC parser is malfunctioning.
					 */
					BaseCSSStyleSheet.this.getErrorHandler().sacMalfunction(
							"Unexpected property " + name + ": " + value.toString());
				}
			} // else { Ignoring property due to target media mismatch
		}

		private void resetCurrentRule() {
			if (currentRule != null) {
				currentRule = null;
			}
			resetCommentStack();
		}

		private void setCommentsToRule(AbstractCSSRule rule) {
			if (comments != null && !comments.isEmpty()) {
				ArrayList<String> ruleComments = new ArrayList<String>(comments.size());
				ruleComments.addAll(comments);
				rule.setPrecedingComments(ruleComments);
			}
			resetCommentStack();
		}

		private void resetCommentStack() {
			if (comments != null) {
				comments.clear();
			}
		}

		@Override
		public String getNamespaceURI(String nsPrefix) {
			Iterator<Entry<String, String>> it = BaseCSSStyleSheet.this.namespaces.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String prefix = entry.getValue();
				if (nsPrefix.equals(prefix)) {
					return entry.getKey();
				}
			}
			return null;
		}

		@Override
		public void warning(CSSParseException exception) throws CSSException {
			if (currentRule != null && currentRule instanceof BaseCSSDeclarationRule
					&& ((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler() != null) {
				int previousIndex = -1;
				CSSStyleDeclaration style = ((BaseCSSDeclarationRule) currentRule).getStyle();
				if (style != null) {
					previousIndex = style.getLength() - 1;
				}
				((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().sacWarning(exception, previousIndex);
			} else {
				// Handle as non-specific warning
				BaseCSSStyleSheet.this.getErrorHandler().handleSacWarning(exception);
			}
		}

		@Override
		public void error(CSSParseException exception) throws CSSException {
			if (currentRuleCanHandleError()) {
				int previousIndex = -1;
				CSSStyleDeclaration style = ((BaseCSSDeclarationRule) currentRule).getStyle();
				if (style != null) {
					previousIndex = style.getLength() - 1;
				}
				((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().sacError(exception, previousIndex);
				BaseCSSStyleSheet.this.getErrorHandler().mapError(exception, currentRule);
			} else {
				// Handle as non-specific error
				nonRuleErrorHandling(exception);
			}
		}

		@Override
		public void fatalError(CSSParseException exception) throws CSSException {
			if (currentRuleCanHandleError()) {
				int previousIndex = -1;
				CSSStyleDeclaration style = ((BaseCSSDeclarationRule) currentRule).getStyle();
				if (style != null) {
					previousIndex = style.getLength() - 1;
				}
				((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler().sacFatalError(exception,
						previousIndex);
				BaseCSSStyleSheet.this.getErrorHandler().mapError(exception, currentRule);
			} else {
				// Handle as non-specific error
				nonRuleErrorHandling(exception);
			}
		}

		/*
		 * Current rule can handle the error if it is set (not null), it is a declaration rule and
		 * contains a declaration error handler.
		 */
		private boolean currentRuleCanHandleError() {
			return currentRule != null && currentRule instanceof BaseCSSDeclarationRule
					&& ((BaseCSSDeclarationRule) currentRule).getStyleDeclarationErrorHandler() != null;
		}

		private void nonRuleErrorHandling(CSSParseException exception) {
			BaseCSSStyleSheet.this.getErrorHandler().handleSacError(exception);
			if (outOfRuleException == null) {
				outOfRuleException = exception;
			}
			if (currentRule != null) {
				currentRule = currentRule.getParentRule();
			}
		}

	}
}
