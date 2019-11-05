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
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.agent.AgentUtil;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSNamespaceRule;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
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

	private final AbstractCSSRule ownerRule;

	private final byte sheetOrigin;

	final CSSRuleArrayList cssRules;

	private int currentInsertionIndex = 0;

	private MediaQueryList destinationMedia;

	/**
	 * URI-to-prefix map.
	 */
	private Map<String, String> namespaces = new HashMap<String, String>();

	private boolean disabled = false;

	private SheetErrorHandler sheetErrorHandler = null;

	private static final int MAX_IMPORT_RECURSION = 8; // Allows 6 nested imports

	/**
	 * Constructs a style sheet.
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
		if (media == null) {
			this.destinationMedia = MediaQueryListImpl.createUnmodifiable();
		} else {
			this.destinationMedia = media;
		}
		sheetOrigin = origin;
		cssRules = new CSSRuleArrayList(64);
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
		myCopy.cssRules.ensureCapacity(cssRules.getLength());
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
		Reader re = new StringReader(rule);
		// The following may cause an (undocumented)
		// DOMException.NOT_SUPPORTED_ERR
		Parser psr = getStyleSheetFactory().createSACParser();
		SheetHandler handler = createDocumentHandler(ExtendedCSSStyleSheet.COMMENTS_IGNORE);
		psr.setDocumentHandler(handler);
		psr.setErrorHandler(handler);
		currentInsertionIndex = index - 1;
		try {
			psr.parseRule(re, handler);
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
		if (currentInsertionIndex != index && handler.getOutOfRuleException() != null) {
			DOMException ex;
			if (handler.getOutOfRuleException().getClass() == CSSNamespaceParseException.class) {
				ex = new DOMException(DOMException.NAMESPACE_ERR, handler.getOutOfRuleException().getMessage());
			} else {
				ex = new DOMException(DOMException.SYNTAX_ERR, handler.getOutOfRuleException().getMessage());
			}
			ex.initCause(handler.getOutOfRuleException());
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

	void setNamespace(String prefix, String uri) {
		namespaces.put(uri, prefix);
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
				MediaRule mrule = createMediaRule(mediaList);
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
					String cssText = oRule.getCssText();
					sheet.getErrorHandler().badAtRule(ex, cssText);
					getErrorHandler().badAtRule(ex, cssText);
					return;
				}
				// We clone with 'sheet' as parent, to receive the errors
				ImportRule imp = (ImportRule) oRule.clone(sheet);
				AbstractCSSStyleSheet impSheet = imp.getStyleSheet();
				CSSRuleArrayList impRules = impSheet.getCssRules();
				MediaQueryList media = imp.getMedia();
				if (media.isAllMedia()) {
					addRuleList(impRules, impSheet, importCount);
				} else if (!media.isNotAllMedia()) {
					// Create a Media rule
					MediaRule mrule = createMediaRule(imp.getMedia());
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
					String cssText = oRule.getCssText();
					sheet.getErrorHandler().badAtRule(ex, cssText);
					getErrorHandler().badAtRule(ex, cssText);
					return;
				}
				// We clone with 'sheet' as parent, to receive the errors
				ImportRule imp = (ImportRule) oRule.clone(sheet);
				AbstractCSSStyleSheet impSheet = imp.getStyleSheet();
				CSSRuleArrayList impRules = impSheet.getCssRules();
				MediaQueryList media = imp.getMedia();
				if (mrule.getMedia().equals(media)) {
					addToMediaRule(mrule, impRules, impSheet, importCount);
				} else {
					// Create a Media rule
					MediaRule nestedMRule = createMediaRule(media);
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
		case Selector.SAC_UNIVERSAL_SELECTOR:
			return namespaceURI.equals(((ElementSelector) sel).getNamespaceURI());
		case Selector.SAC_CONDITIONAL_SELECTOR:
			ConditionalSelector csel = (ConditionalSelector) sel;
			return selectorHasNamespace(csel.getSimpleSelector(), namespaceURI) ||
					conditionHasNamespace(csel.getCondition(), namespaceURI);
		case Selector.SAC_CHILD_SELECTOR:
		case Selector.SAC_DESCENDANT_SELECTOR:
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
		case Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR:
		case Selector.SAC_COLUMN_COMBINATOR_SELECTOR:
			CombinatorSelector dsel = (CombinatorSelector) sel;
			return selectorHasNamespace(dsel.getSelector(), namespaceURI) ||
					selectorHasNamespace(dsel.getSecondSelector(), namespaceURI);
		default:
			return false;
		}
	}

	private static boolean conditionHasNamespace(Condition condition, String namespaceURI) {
		switch (condition.getConditionType()) {
		case Condition.SAC_ATTRIBUTE_CONDITION:
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
		case Condition.SAC_BEGINS_ATTRIBUTE_CONDITION:
		case Condition.SAC_ENDS_ATTRIBUTE_CONDITION:
		case Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
			AttributeCondition acond = (AttributeCondition) condition;
			return namespaceURI.equals(acond.getNamespaceURI());
		case Condition.SAC_AND_CONDITION:
			CombinatorCondition ccond = (CombinatorCondition) condition;
			return conditionHasNamespace(ccond.getFirstCondition(), namespaceURI) ||
					conditionHasNamespace(ccond.getSecondCondition(), namespaceURI);
		case Condition.SAC_POSITIONAL_CONDITION:
			SelectorList oflist = ((PositionalCondition) condition).getOfList();
			if (oflist != null) {
				return selectorListHasNamespace(oflist, namespaceURI);
			}
			break;
		case Condition.SAC_SELECTOR_ARGUMENT_CONDITION:
			ArgumentCondition argcond = (ArgumentCondition) condition;
			SelectorList selist = argcond.getSelectors();
			if (selist != null) {
				return selectorListHasNamespace(selist, namespaceURI);
			}
		}
		return false;
	}

	@Override
	public CounterStyleRule createCounterStyleRule(String name) throws DOMException {
		CounterStyleRule rule = new CounterStyleRule(this, getOrigin());
		rule.setName(name);
		return rule;
	}

	@Override
	public FontFaceRule createFontFaceRule() {
		return new FontFaceRule(this, getOrigin());
	}

	@Override
	public FontFeatureValuesRule createFontFeatureValuesRule(String[] fontFamily) {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(this, getOrigin());
		rule.setFontFamily(fontFamily);
		return rule;
	}

	@Override
	public ImportRule createImportRule(MediaQueryList mediaList, String href) {
		if (href == null) {
			throw new NullPointerException("Null @import URI");
		}
		return new ImportRule(this, ((MediaListAccess) mediaList).unmodifiable(), href, getOrigin());
	}

	@Override
	public KeyframesRule createKeyframesRule(String keyframesName) {
		KeyframesRule rule = new KeyframesRule(this, getOrigin());
		rule.setName(keyframesName);
		return rule;
	}

	@Override
	public MarginRule createMarginRule(String name) {
		return new MarginRule(this, getOrigin(), name);
	}

	@Override
	public MediaRule createMediaRule(MediaQueryList mediaList) {
		return new MediaRule(this, mediaList, getOrigin());
	}

	/*
	 * Issues: if the rule is created but not added to the sheet, it is still
	 * accounted for the namespace URI - prefix mapping.
	 */
	@Override
	public NamespaceRule createNamespaceRule(String prefix, String namespaceUri) {
		if (prefix == null || namespaceUri == null) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Null parameter");
		}
		return new NamespaceRule(this, getOrigin(), prefix, namespaceUri);
	}

	@Override
	public PageRule createPageRule() {
		return new PageRule(this, getOrigin());
	}

	@Override
	public StyleRule createStyleRule() {
		return new StyleRule(this, getOrigin());
	}

	@Override
	public SupportsRule createSupportsRule() {
		return new SupportsRule(this, getOrigin());
	}

	@Override
	public ViewportRule createViewportRule() {
		return new ViewportRule(this, getOrigin());
	}

	@Override
	public UnknownRule createUnknownRule() {
		return new UnknownRule(this, getOrigin());
	}

	@Override
	protected BaseCSSStyleDeclaration createStyleDeclaration(BaseCSSDeclarationRule rule) {
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
	public BaseCSSStyleDeclaration createStyleDeclaration() {
		return new BaseCSSStyleDeclaration();
	}

	@Override
	public boolean hasRuleErrorsOrWarnings() {
		for (AbstractCSSRule rule : cssRules) {
			if (rule.hasErrorsOrWarnings()) {
				return true;
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
			// Stand-alone sheet.
			eh = StandAloneErrorHandler.getInstance(this);
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

	String getNamespaceURI(String nsPrefix) {
		Iterator<Entry<String, String>> it = namespaces.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String prefix = entry.getValue();
			if (nsPrefix.equals(prefix)) {
				return entry.getKey();
			}
		}
		return null;
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

	@Override
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
		Reader re = AgentUtil.inputStreamToReader(is, conType, contentEncoding, StandardCharsets.UTF_8);
		// Parse
		boolean result;
		try {
			setHref(url.toExternalForm());
			result = parseStyleSheet(re);
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
	 * Creates a SAC document handler that fills this style sheet.
	 * 
	 * @param commentMode the comment processing mode.
	 * @return the new SAC document handler.
	 */
	SheetHandler createDocumentHandler(short commentMode) {
		return new SheetHandler(this, getOrigin(), commentMode);
	}

	/**
	 * Creates a SAC document handler that fills this style sheet.
	 * 
	 * @param origin the origin for this style sheet.
	 * @param commentMode the comment processing mode.
	 * @return the new SAC document handler.
	 */
	SheetHandler createDocumentHandler(byte origin, short commentMode) {
		return new SheetHandler(this, origin, commentMode);
	}

	@Override
	public boolean parseStyleSheet(Reader reader) throws DOMException, IOException {
		return parseStyleSheet(reader, COMMENTS_AUTO);
	}

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with a
	 * highest precedence origin.
	 * <p>
	 * If <code>commentMode</code> is not {@code COMMENTS_IGNORE}, the comments
	 * preceding a rule shall be available through
	 * {@link AbstractCSSRule#getPrecedingComments()}, and if {@code COMMENTS_AUTO}
	 * was set also the trailing ones, through the method
	 * {@link AbstractCSSRule#getTrailingComments()}.
	 * <p>
	 * This method resets the state of this sheet's error handler.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param reader      the character stream containing the CSS sheet.
	 * @param commentMode {@code 0} if comments have to be ignored, {@code 1} if all
	 *                    comments are considered as preceding a rule, {@code 2} if
	 *                    the parser should try to figure out which comments are
	 *                    preceding and trailing a rule (auto mode).
	 * @return <code>true</code> if the SAC parser reported no errors or fatal
	 *         errors, false otherwise.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if a problem is found reading the sheet.
	 */
	@Override
	public boolean parseStyleSheet(Reader reader, short commentMode) throws DOMException, IOException {
		if (sheetErrorHandler != null) {
			sheetErrorHandler.reset();
		}
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
		CSSHandler handler = createDocumentHandler(origin, commentMode);
		parser.setDocumentHandler(handler);
		parser.setErrorHandler((CSSErrorHandler) handler);
		try {
			parser.parseStyleSheet(reader);
		} catch (CSSNamespaceParseException e) {
			DOMException ex = new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSBudgetException e) {
			DOMException ex = new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_ACCESS_ERR, e.getMessage());
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

	/**
	 * Does the given SAC media list contain any media present in
	 * <code>media</code>?
	 * 
	 * @param media    the media list to match to.
	 * @param sacMedia the SAC media list to test.
	 * @return <code>true</code> if the SAC media contains any media which applies
	 *         to <code>media</code> list, <code>false</code> otherwise.
	 */
	boolean match(MediaQueryList media, MediaQueryList sacMedia) {
		if (media.isAllMedia()) {
			return true;
		}
		if (sacMedia == null) {
			return !media.isNotAllMedia(); // null list handled as "all"
		}
		if (sacMedia.isAllMedia()) {
			return true;
		}
		return media.matches(sacMedia);
	}

}
