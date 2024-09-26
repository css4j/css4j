/*

 Copyright (c) 2005-2024, Carlos Amengual.

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMPolicyException;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSNamespaceRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
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
import io.sf.carte.util.Visitor;
import io.sf.carte.util.agent.AgentUtil;

/**
 * CSS Style Sheet Object Model implementation base class.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class BaseCSSStyleSheet extends AbstractCSSStyleSheet {

	private static final long serialVersionUID = 1L;

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
	private Map<String, String> namespaces = new HashMap<>();

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
			this.destinationMedia = CSSValueMediaQueryFactory.getAllMediaInstance();
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
		SheetHandler handler = createSheetHandler(CSSStyleSheet.COMMENTS_IGNORE);
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
		if (cssrule.getType() == CSSRule.NAMESPACE_RULE) {
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
		CSSRule rule;
		try {
			rule = cssRules.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, e.getMessage());
		}
		if (rule.getType() == CSSRule.NAMESPACE_RULE
				&& containsRuleWithNamespace(((CSSNamespaceRule) rule).getNamespaceURI())) {
			throw new DOMException(DOMException.NAMESPACE_ERR, "There are style rules with ");
		}
		cssRules.remove(index);
	}

	private boolean containsRuleWithNamespace(String namespaceURI) {
		for (CSSRule rule : cssRules) {
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
		for (int i = 0; i < selist.getLength(); i++) {
			if (selectorHasNamespace(selist.item(i), namespaceURI)) {
				return true;
			}
		}
		return false;
	}

	private static boolean selectorHasNamespace(Selector sel, String namespaceURI) {
		switch (sel.getSelectorType()) {
		case ELEMENT:
		case UNIVERSAL:
			return namespaceURI.equals(((ElementSelector) sel).getNamespaceURI());
		case CONDITIONAL:
			ConditionalSelector csel = (ConditionalSelector) sel;
			return selectorHasNamespace(csel.getSimpleSelector(), namespaceURI) ||
					conditionHasNamespace(csel.getCondition(), namespaceURI);
		case CHILD:
		case DESCENDANT:
		case DIRECT_ADJACENT:
		case SUBSEQUENT_SIBLING:
		case COLUMN_COMBINATOR:
			CombinatorSelector dsel = (CombinatorSelector) sel;
			return selectorHasNamespace(dsel.getSelector(), namespaceURI) ||
					selectorHasNamespace(dsel.getSecondSelector(), namespaceURI);
		default:
			return false;
		}
	}

	private static boolean conditionHasNamespace(Condition condition, String namespaceURI) {
		switch (condition.getConditionType()) {
		case ATTRIBUTE:
		case BEGIN_HYPHEN_ATTRIBUTE:
		case ONE_OF_ATTRIBUTE:
		case BEGINS_ATTRIBUTE:
		case ENDS_ATTRIBUTE:
		case SUBSTRING_ATTRIBUTE:
			AttributeCondition acond = (AttributeCondition) condition;
			return namespaceURI.equals(acond.getNamespaceURI());
		case AND:
			CombinatorCondition ccond = (CombinatorCondition) condition;
			return conditionHasNamespace(ccond.getFirstCondition(), namespaceURI) ||
					conditionHasNamespace(ccond.getSecondCondition(), namespaceURI);
		case POSITIONAL:
			SelectorList oflist = ((PositionalCondition) condition).getOfList();
			if (oflist != null) {
				return selectorListHasNamespace(oflist, namespaceURI);
			}
			break;
		case SELECTOR_ARGUMENT:
			ArgumentCondition argcond = (ArgumentCondition) condition;
			SelectorList selist = argcond.getSelectors();
			if (selist != null) {
				return selectorListHasNamespace(selist, namespaceURI);
			}
		default:
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
	public PropertyRule createPropertyRule(String name) {
		PropertyRule rule = new PropertyRule(this, getOrigin());
		rule.setName(name);
		return rule;
	}

	@Override
	public StyleRule createStyleRule() {
		return new StyleRule(this, getOrigin());
	}

	@Override
	public SupportsRule createSupportsRule(BooleanCondition condition) {
		return new SupportsRule(this, condition, getOrigin());
	}

	@Override
	public SupportsRule createSupportsRule(String conditionText) throws DOMException {
		SupportsRule cond = new SupportsRule(this, getOrigin());
		cond.setConditionText(conditionText);
		return cond;
	}

	@Override
	@Deprecated
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
	public String getNamespacePrefix(String uri) {
		return namespaces.get(uri);
	}

	String getNamespaceURI(String nsPrefix) {
		for (Entry<String, String> entry : namespaces.entrySet()) {
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
	public boolean hasDefaultNamespace() {
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
	 * @param url            the url to load the style sheet from.
	 * @param referrerPolicy the content of the <code>referrerpolicy</code> content
	 *                       attribute, if any, or the empty string.
	 * @return <code>true</code> if the NSAC parser reported no errors or fatal
	 *         errors, <code>false</code> otherwise.
	 * @throws DOMPolicyException if the style sheet was served with an invalid
	 *                            content type.
	 * @throws DOMException       if there is a serious problem parsing the style
	 *                            sheet.
	 * @throws IOException        if a problem appears fetching the url contents.
	 */
	@Override
	public boolean loadStyleSheet(URL url, String referrerPolicy) throws DOMException, IOException {
		boolean result = false;

		final URLConnection ucon = openConnection(url, referrerPolicy);
		ucon.connect();

		try (InputStream is = ucon.getInputStream()) {
			String contentEncoding = ucon.getContentEncoding();
			String conType = ucon.getContentType();

			// Check that the content type is correct
			if (isInvalidContentType(url, conType) && !isRedirect(ucon)) {
				// Report security error
				String msg;
				if (conType != null) {
					// Sanitize untrusted content-type by removing control characters
					// ('Other, Control' unicode category).
					conType = conType.replaceAll("\\p{Cc}", "*CTRL*");
					msg = "Style sheet at " + url.toExternalForm() + " served with invalid type ("
							+ conType + ").";
				} else {
					msg = "Style sheet at " + url.toExternalForm()
							+ " has no content type nor ends with '.css' extension.";
				}

				getDocumentErrorHandler().policyError(getOwnerNode(), msg);
				throw new DOMPolicyException(msg);
			}

			Reader re = AgentUtil.inputStreamToReader(is, conType, contentEncoding,
					StandardCharsets.UTF_8);

			// Parse
			try {
				setHref(url.toExternalForm());
				result = parseStyleSheet(re);
			} catch (DOMException e) {
				getDocumentErrorHandler().linkedSheetError(e, this);
				throw e;
			}
		}

		if (ucon instanceof HttpURLConnection) {
			((HttpURLConnection) ucon).disconnect();
		}

		return result;
	}

	private boolean isInvalidContentType(URL url, String conType) {
		String proto;
		if (conType != null && !"content/unknown".equalsIgnoreCase(conType)
				&& !"jar".equals(proto = url.getProtocol()) && !"file".equals(proto)) {
			int sepidx = conType.indexOf(';');
			if (sepidx != -1) {
				conType = conType.substring(0, sepidx);
			}
			return !"text/css".equalsIgnoreCase(conType)
					&& !url.getPath().toLowerCase(Locale.ROOT).endsWith(".css");
		}
		return !"file".equals(proto = url.getProtocol()) && !"jar".equals(proto)
				&& !url.getPath().toLowerCase(Locale.ROOT).endsWith(".css");
	}

	private boolean isRedirect(URLConnection ucon) {
		if (ucon instanceof HttpURLConnection) {
			int code;
			try {
				code = ((HttpURLConnection) ucon).getResponseCode();
				return code > 300 && code < 400 && code != 304;
			} catch (IOException e) {
			}
		}
		return false;
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
			case CSSRule.PAGE_RULE: // 'page' property is a property, the rest are descriptors
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (((BaseCSSStyleDeclaration) stylerule.getStyle()).isPropertySet(propertyName)) {
					subset.add(stylerule);
				}
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
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
		LinkedList<Selector> selectors = new LinkedList<>();
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
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (((BaseCSSStyleDeclaration) stylerule.getStyle()).isPropertySet(propertyName)) {
					SelectorList list = stylerule.getSelectorList();
					for (int i = 0; i < list.getLength(); i++) {
						selectors.add(list.item(i));
					}
				}
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
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
		LinkedList<Selector> selectors = new LinkedList<>();
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
				CSSStyleDeclarationRule stylerule = (CSSStyleDeclarationRule) rule;
				if (value.equalsIgnoreCase(stylerule.getStyle().getPropertyValue(propertyName))) {
					SelectorList list = stylerule.getSelectorList();
					for (int i = 0; i < list.getLength(); i++) {
						selectors.add(list.item(i));
					}
				}
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				scanRulesForValue(grouping.getCssRules(), propertyName, value, selectors);
				break;
			}
		}
	}

	/**
	 * Get the first style rule that exactly matches the given selector list, if
	 * any.
	 * <p>
	 * Rules inside grouping rules are also searched.
	 * </p>
	 * 
	 * @param selectorList the selector list.
	 * @return the first style rule that matches, or {@code null} if none.
	 */
	@Override
	public StyleRule getFirstStyleRule(SelectorList selectorList) {
		return scanRulesForSelector(cssRules, selectorList);
	}

	private static StyleRule scanRulesForSelector(CSSRuleArrayList rules,
		SelectorList selectorList) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
				StyleRule stylerule = (StyleRule) rule;
				SelectorList list = stylerule.getSelectorList();
				if (list.equals(selectorList)) {
					return stylerule;
				}
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				stylerule = scanRulesForSelector(grouping.getCssRules(), selectorList);
				if (stylerule != null) {
					return stylerule;
				}
				break;
			}
		}
		return null;
	}

	/**
	 * Get the list of style rules that match the given selector.
	 * <p>
	 * Rules inside grouping rules are also searched.
	 * </p>
	 * 
	 * @param selector the selector.
	 * @return the list of style rule that match, or {@code null} if none.
	 */
	@Override
	public CSSRuleArrayList getStyleRules(Selector selector) {
		CSSRuleArrayList list = new CSSRuleArrayList();
		scanRulesForSelector(cssRules, selector, list);
		if (list.isEmpty()) {
			return null;
		}
		return list;
	}

	private static void scanRulesForSelector(CSSRuleArrayList rules, Selector selector,
		CSSRuleArrayList styleRules) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
				StyleRule stylerule = (StyleRule) rule;
				SelectorList list = stylerule.getSelectorList();
				if (list.contains(selector)) {
					styleRules.add(stylerule);
				}
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				scanRulesForSelector(grouping.getCssRules(), selector, styleRules);
				break;
			}
		}
	}

	/**
	 * Accept a style rule visitor.
	 * 
	 * @param visitor the visitor.
	 */
	@Override
	public void acceptStyleRuleVisitor(Visitor<CSSStyleRule> visitor) {
		acceptStyleRuleVisitor(cssRules, visitor);
	}

	private void acceptStyleRuleVisitor(CSSRuleArrayList rules, Visitor<CSSStyleRule> visitor) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
				StyleRule stylerule = (StyleRule) rule;
				visitor.visit(stylerule);
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				acceptStyleRuleVisitor(grouping.getCssRules(), visitor);
				break;
			}
		}
	}

	/**
	 * Accept a declaration rule visitor.
	 * 
	 * @param visitor the visitor.
	 */
	@Override
	public void acceptDeclarationRuleVisitor(Visitor<CSSDeclarationRule> visitor) {
		acceptDeclarationRuleVisitor(cssRules, visitor);
	}

	private void acceptDeclarationRuleVisitor(AbstractRuleList<? extends CSSRule> rules,
			Visitor<CSSDeclarationRule> visitor) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.STYLE_RULE:
			case CSSRule.FONT_FACE_RULE:
			case CSSRule.KEYFRAME_RULE:
			case CSSRule.MARGIN_RULE:
			case CSSRule.COUNTER_STYLE_RULE:
			case CSSRule.PROPERTY_RULE:
			case CSSRule.VIEWPORT_RULE:
				CSSDeclarationRule declRule = (CSSDeclarationRule) rule;
				visitor.visit(declRule);
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				acceptDeclarationRuleVisitor(grouping.getCssRules(), visitor);
				break;
			case CSSRule.PAGE_RULE:
				PageRule pageRule = (PageRule) rule;
				MarginRuleList marginBoxes = pageRule.getMarginRules();
				if (marginBoxes != null) {
					acceptDeclarationRuleVisitor(marginBoxes, visitor);
				}
				visitor.visit(pageRule);
				break;
			case CSSRule.KEYFRAMES_RULE:
				KeyframesRule kfsRule = (KeyframesRule) rule;
				acceptDeclarationRuleVisitor(kfsRule.getCssRules(), visitor);
				break;
			}
		}
	}

	@Override
	public void acceptDescriptorRuleVisitor(Visitor<CSSDeclarationRule> visitor) {
		acceptDescriptorRuleVisitor(cssRules, visitor);
	}

	private void acceptDescriptorRuleVisitor(AbstractRuleList<? extends CSSRule> rules,
			Visitor<CSSDeclarationRule> visitor) {
		for (CSSRule rule : rules) {
			switch (rule.getType()) {
			case CSSRule.FONT_FACE_RULE:
			case CSSRule.KEYFRAME_RULE:
			case CSSRule.MARGIN_RULE:
			case CSSRule.COUNTER_STYLE_RULE:
			case CSSRule.PROPERTY_RULE:
			case CSSRule.VIEWPORT_RULE:
				CSSDeclarationRule declRule = (CSSDeclarationRule) rule;
				visitor.visit(declRule);
				break;
			case CSSRule.MEDIA_RULE:
			case CSSRule.SUPPORTS_RULE:
				GroupingRule grouping = (GroupingRule) rule;
				acceptDescriptorRuleVisitor(grouping.getCssRules(), visitor);
				break;
			case CSSRule.PAGE_RULE:
				PageRule pageRule = (PageRule) rule;
				MarginRuleList marginBoxes = pageRule.getMarginRules();
				if (marginBoxes != null) {
					acceptDescriptorRuleVisitor(marginBoxes, visitor);
				}
				visitor.visit(pageRule);
				break;
			case CSSRule.KEYFRAMES_RULE:
				KeyframesRule kfsRule = (KeyframesRule) rule;
				acceptDescriptorRuleVisitor(kfsRule.getCssRules(), visitor);
				break;
			}
		}
	}

	protected String getTargetMedium() {
		return null;
	}

	/**
	 * Returns a minified parsable representation of the rule list of this sheet.
	 * <p>
	 * Equivalent to <code>getCssRules().toMinifiedString()</code>.
	 * </p>
	 * 
	 * @return a minified parsable representation of the rule list of this sheet.
	 */
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
	 * Creates a NSAC sheet handler that fills this style sheet.
	 * 
	 * @param commentMode the comment processing mode.
	 * @return the new NSAC sheet handler.
	 */
	SheetHandler createSheetHandler(short commentMode) {
		return new SheetHandler(this, getOrigin(), commentMode);
	}

	/**
	 * Creates a NSAC sheet handler that fills this style sheet.
	 * 
	 * @param origin the origin for this style sheet.
	 * @param commentMode the comment processing mode.
	 * @return the new NSAC sheet handler.
	 */
	SheetHandler createSheetHandler(byte origin, short commentMode) {
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
	 * @throws DOMException if raised by the error handler.
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
		CSSHandler handler = createSheetHandler(origin, commentMode);
		parser.setDocumentHandler(handler);
		parser.setErrorHandler((CSSErrorHandler) handler);
		parseStyleSheet(reader, parser);

		return !getErrorHandler().hasSacErrors();
	}

	private void parseStyleSheet(Reader reader, Parser parser)
			throws DOMException, IOException {
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
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, "Parse error at ["
				+ e.getLineNumber() + ',' + e.getColumnNumber() + "]: " + e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_ACCESS_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (DOMException e) {
			// Handler may produce DOM exceptions
			throw e;
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
	}

	/**
	 * Does the given media query list contain any media present in
	 * <code>media</code>?
	 * 
	 * @param media the media query list to match to.
	 * @param mql   the media query list to test.
	 * @return <code>true</code> if the second media query list contains any media
	 *         which applies to the first <code>media</code> list,
	 *         <code>false</code> otherwise.
	 */
	boolean match(MediaQueryList media, MediaQueryList mql) {
		if (media.isAllMedia()) {
			return true;
		}
		if (mql == null) {
			return !media.isNotAllMedia(); // null list handled as "all"
		}
		if (mql.isAllMedia()) {
			return true;
		}
		return media.matches(mql);
	}

}
