/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.BooleanCondition.Type;
import io.sf.carte.doc.style.css.BooleanConditionFactory;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryFactory;
import io.sf.carte.doc.style.css.MediaQueryHandler;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.impl.CSSUtil;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSMediaParseException;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.DeclarationPredicate;
import io.sf.carte.doc.style.css.nsac.InputSource;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.Selector.SelectorType;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SheetContext;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.SupportsConditionFactory;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.AttributeConditionImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.CombinatorSelectorImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.ElementSelectorImpl;
import io.sf.carte.uparser.ContentHandler;
import io.sf.carte.uparser.TokenControl;
import io.sf.carte.uparser.TokenProducer;
import io.sf.carte.uparser.TokenProducer3.CharacterCheck;
import io.sf.carte.util.agent.AgentUtil;

/**
 * CSS parser implementing the NSAC API.
 * <p>
 * Additionally to NSAC, it includes several other methods.
 * </p>
 * <p>
 * By default, the methods that take a {@link Reader} or an {@link InputSource}
 * as argument can process streams up to {@code 0x6000000} (100MB) in size, and
 * throw a {@link SecurityException} if they hit that limit. See also
 * {@link #setStreamSizeLimit(int)}.
 * </p>
 */
public class CSSParser implements Parser, Cloneable {

	private CSSHandler handler;
	private CSSErrorHandler errorHandler;

	private final EnumSet<Flag> parserFlags;

	private int streamSizeLimit = 0x6000000;

	/**
	 * Instantiate a parser instance with no flags.
	 */
	public CSSParser() {
		super();
		parserFlags = EnumSet.noneOf(Flag.class);
		handler = null;
		errorHandler = null;
	}

	/**
	 * Instantiate a parser instance with the given flags.
	 * 
	 * @param parserFlags the flags.
	 */
	public CSSParser(EnumSet<Flag> parserFlags) {
		super();
		this.parserFlags = parserFlags;
	}

	protected CSSParser(CSSParser copyMe) {
		super();
		parserFlags = copyMe.parserFlags;
		handler = copyMe.handler;
		errorHandler = copyMe.errorHandler;
	}

	@Override
	public void setDocumentHandler(CSSHandler handler) {
		this.handler = handler;
	}

	@Override
	public void setErrorHandler(CSSErrorHandler handler) {
		this.errorHandler = handler;
	}

	/**
	 * Set a parser flag.
	 * 
	 * @param flag the flag.
	 */
	@Override
	public void setFlag(Flag flag) {
		parserFlags.add(flag);
	}

	/**
	 * Unset a parser flag.
	 * 
	 * @param flag the flag.
	 */
	@Override
	public void unsetFlag(Flag flag) {
		parserFlags.remove(flag);
	}

	/**
	 * Set a new limit for the stream size that can be processed.
	 * <p>
	 * Calling this method does not affect the parsing that was already ongoing.
	 * </p>
	 * 
	 * @param streamSizeLimit the new limit to be enforced by new processing by this
	 *                        parser.
	 * @throws IllegalArgumentException if a limit below 64K was used.
	 */
	public void setStreamSizeLimit(int streamSizeLimit) {
		if (streamSizeLimit < 65536) {
			throw new IllegalArgumentException("Limit too low.");
		}
		this.streamSizeLimit = streamSizeLimit;
	}

	@Override
	public void parseStyleSheet(Reader reader)
			throws CSSParseException, IOException, IllegalStateException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		NamespaceMap nsMap = null;
		if (handler instanceof NamespaceMap) {
			nsMap = (NamespaceMap) handler;
		}
		RuleListManager manager = new RuleListManager(nsMap, true);
		TokenProducer tp = manager.createTokenProducer();
		tp.setAcceptEofEndingQuoted(true);
		manager.parseStart();
		tp.parse(reader, "/*", "*/");
	}

	/**
	 * Parse a CSS sheet from a URI.
	 * <p>
	 * The sheet is parsed as a rule list, that is, XML's {@code CDO}-{@code CDC}
	 * comments are not expected.
	 * </p>
	 * <p>
	 * The timeout to establish a connection is of 30 seconds.
	 * </p>
	 * <p>
	 * Usage of this method may have security implications. Please make sure that
	 * the URI being passed is safe to use.
	 * </p>
	 *
	 * @param uri The URI locating the sheet.
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws IOException           if {@code uri} is an invalid URL or a I/O error
	 *                               was found while retrieving the sheet.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	@Override
	public void parseStyleSheet(String uri)
			throws CSSParseException, IOException, IllegalStateException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}
		URL url;
		try {
			url = new URI(uri).toURL();
		} catch (Exception e) {
			throw new MalformedURLException(e.getMessage());
		}
		URLConnection ucon = url.openConnection();
		ucon.setConnectTimeout(15000);
		ucon.connect();
		InputStream is = ucon.getInputStream();
		is = new BufferedInputStream(is);
		String contentEncoding = ucon.getContentEncoding();
		String conType = ucon.getContentType();

		// Check that the content type is correct
		if (CSSUtil.isInvalidCSSContentType(url, conType) && !isRedirect(ucon)) {
			// Report security error
			String msg;
			if (conType != null) {
				// Sanitize untrusted content-type by removing control characters
				// ('Other, Control' unicode category).
				conType = conType.replaceAll("\\p{Cc}", "*CTRL*");
				msg = "Style sheet at " + url.toExternalForm() + " served with invalid type ("
						+ conType + ").";
			} else {
				msg = "Style sheet at " + url.toExternalForm() + " has no valid content type.";
			}
			try {
				is.close();
			} catch (IOException e) {
			}
			throw new IOException(msg);
		}

		NamespaceMap nsMap = null;
		if (handler instanceof NamespaceMap) {
			nsMap = (NamespaceMap) handler;
		}
		RuleListManager manager = new RuleListManager(nsMap, false);
		TokenProducer tp = manager.createTokenProducer();
		tp.setAcceptEofEndingQuoted(true);

		try (Reader re = AgentUtil.inputStreamToReader(is, conType, contentEncoding,
				StandardCharsets.UTF_8)) {
			manager.parseStart();
			tp.parse(re, "/*", "*/"); // We do not look for CDO-CDC comments here
		}
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

	@Override
	public void parseStyleSheet(InputSource source)
			throws CSSParseException, IOException, IllegalStateException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Null source.");
		}

		Reader re = source.getCharacterStream();
		if (re == null) {
			InputStream is = source.getByteStream();
			if (is == null) {
				String uri = source.getURI();
				if (uri == null) {
					throw new IllegalArgumentException("Null character stream");
				}
				parseStyleSheet(uri);
				return;
			}
			String charset = source.getEncoding();
			if (charset == null) {
				charset = "UTF-8";
			}
			re = new InputStreamReader(is, charset);
		}

		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		NamespaceMap nsMap = null;
		if (handler instanceof NamespaceMap) {
			nsMap = (NamespaceMap) handler;
		}
		RuleListManager manager = new RuleListManager(nsMap, true);
		TokenProducer tp = manager.createTokenProducer();

		tp.setAcceptEofEndingQuoted(true);
		manager.parseStart();
		tp.parse(re, "/*", "*/");
	}

	@Override
	public void parseStyleDeclaration(Reader reader)
			throws CSSParseException, IOException, IllegalStateException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		DeclarationListManager manager = new DeclarationListManager();
		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(reader, "/*", "*/");
	}

	public void parseStyleDeclaration(InputSource source)
			throws CSSException, IOException, IllegalStateException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		DeclarationListManager manager = new DeclarationListManager();
		TokenProducer tp = manager.createTokenProducer();
		Reader reader = getReaderFromSource(source);
		manager.parseStart();
		tp.parse(reader, "/*", "*/");
	}

	private Reader getReaderFromSource(InputSource source) throws IOException {
		if (source == null) {
			throw new NullPointerException("Null source.");
		}
		Reader re = source.getCharacterStream();
		if (re == null) {
			InputStream is = source.getByteStream();
			if (is != null) {
				String encoding = source.getEncoding();
				Charset charset;
				if (encoding == null) {
					charset = StandardCharsets.UTF_8;
				} else {
					charset = Charset.forName(encoding);
				}
				re = new InputStreamReader(is, charset);
			} else {
				String uri = source.getURI();
				if (uri != null) {
					URL url;
					try {
						url = new URI(uri).toURL();
					} catch (Exception e) {
						throw new MalformedURLException(e.getMessage());
					}
					URLConnection con = url.openConnection();
					con.setConnectTimeout(30000);
					con.connect();
					is = con.getInputStream();
					is = new BufferedInputStream(is);
					String contentEncoding = con.getContentEncoding();
					String conType = con.getContentType();
					String encoding = source.getEncoding();
					Charset charset;
					if (encoding == null) {
						charset = StandardCharsets.UTF_8;
					} else {
						charset = Charset.forName(encoding);
					}
					re = AgentUtil.inputStreamToReader(is, conType, contentEncoding, charset);
				}
			}
		}
		return re;
	}

	/**
	 * Parse any simple (non-nesting) block at-rule containing descriptors, using a
	 * generic {@link DeclarationRuleHandler}.
	 * <p>
	 * In general it is recommended to use {@link #parseRule(Reader)} to parse
	 * individual at-rules, however this method can be useful for generic rules that
	 * are not yet supported by the {@link CSSHandler} interface, as well as for
	 * {@code @keyframe} which isn't a top-level rule.
	 * </p>
	 * <p>
	 * As mentioned, the rule cannot have nested rules.
	 * </p>
	 * <p>
	 * Note: in addition to the listed exceptions, this method may raise runtime
	 * exceptions produced by the {@code DeclarationRuleHandler}.
	 * </p>
	 * 
	 * @param reader the character stream containing the CSS rule.
	 *
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws IOException           if a I/O error was found while retrieving the
	 *                               rule.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set or is not
	 *                               a {@code DeclarationRuleHandler}.
	 */
	public void parseDeclarationRule(Reader reader) throws CSSParseException, IOException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		if (!(this.handler instanceof DeclarationRuleHandler)) {
			throw new IllegalStateException(
					"Document handler needs to implement DeclarationRuleHandler.");
		}

		HandlerManager manager = new GenericBlockAtRuleManager();

		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(reader, "/*", "*/");
	}

	public PageSelectorList parsePageSelectorList(String pageSelectorStr) throws DOMException {
		if (pageSelectorStr == null) {
			throw new NullPointerException("Null page selector");
		}

		PageSelectorListImpl list = new PageSelectorListImpl();
		StringTokenizer commast = new StringTokenizer(pageSelectorStr, ",");
		while (commast.hasMoreTokens()) {
			String selstr = commast.nextToken();
			selstr = ParseHelper.unescapeStringValue(selstr, true, false);
			StringTokenizer st = new StringTokenizer(selstr, ",");
			while (st.hasMoreTokens()) {
				String s = st.nextToken().trim();
				AbstractPageSelector psitem = parsePageSelector(s);
				if (psitem != null) {
					list.add(psitem);
				} else {
					throw new DOMException(DOMException.SYNTAX_ERR, "Bad page selector: " + s);
				}
			}
		}

		return list;
	}

	private AbstractPageSelector parsePageSelector(String s) {
		AbstractPageSelector psitem = null;
		AbstractPageSelector ps = null;
		int colonidx = s.indexOf(':');
		if (colonidx == -1) {
			// Page type selector
			return new PageTypeSelector(s);
		} else if (colonidx != 0) {
			String pts = s.substring(0, colonidx);
			if (!isValidIdentifier(pts)) {
				return null;
			}
			ps = new PageTypeSelector(pts);
			psitem = ps;
		}
		colonidx++;
		final int len = s.length();
		while (colonidx < len) {
			int nextColonIdx = s.indexOf(':', colonidx);
			if (nextColonIdx == colonidx) {
				return null;
			}
			String pp;
			if (nextColonIdx == -1) {
				pp = s.substring(colonidx).toLowerCase(Locale.ROOT);
				colonidx = len;
			} else {
				pp = s.substring(colonidx, nextColonIdx).toLowerCase(Locale.ROOT);
				colonidx = nextColonIdx + 1;
			}
			if (containsOnlyLcLetters(pp)) {
				PseudoPageSelector pps = new PseudoPageSelector(pp);
				if (ps != null) {
					ps.setNextSelector(pps);
				} else {
					psitem = pps;
				}
				ps = pps;
			} else {
				return null;
			}
		}
		return psitem;
	}

	private boolean containsOnlyLcLetters(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c < 'a' || c > 'z') {
				return false;
			}
		}
		return true;
	}

	@Override
	public void parseRule(Reader reader)
			throws CSSParseException, IOException, IllegalStateException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		RuleManager manager = new RuleManager(null);
		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(reader, "/*", "*/");
	}

	@Override
	public void parseRule(Reader reader, NamespaceMap nsmap)
			throws CSSParseException, IOException, IllegalStateException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		RuleManager manager = new RuleManager(nsmap);
		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(reader, "/*", "*/");
	}

	public void parseRule(InputSource source) throws CSSParseException, IOException {
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}

		RuleManager manager = new RuleManager(null);
		TokenProducer tp = manager.createTokenProducer();
		Reader re = getReaderFromSource(source);
		manager.parseStart();
		tp.parse(re, "/*", "*/");
	}

	public void parsePageRuleBody(String blockList) throws CSSParseException {
		PageManager manager = new PageManager();
		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(blockList, "/*", "*/");
	}

	public void parseKeyFramesBody(String blockList) throws CSSParseException {
		KeyframesManager manager = new KeyframesManager();
		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(blockList, "/*", "*/");
	}

	public void parseFontFeatureValuesBody(String blockList) throws CSSParseException {
		FontFeatureValuesManager manager = new FontFeatureValuesManager();
		TokenProducer tp = manager.createTokenProducer();
		manager.parseStart();
		tp.parse(blockList, "/*", "*/");
	}

	/**
	 * Parse the condition text of a <code>{@literal @}supports</code> rule.
	 * 
	 * @param conditionText the condition text.
	 * @param rule          the rule that would process the error. If
	 *                      <code>null</code>, a problem while parsing shall result
	 *                      in an exception. Note that
	 *                      <code>NOT_SUPPORTED_ERR</code> exceptions are always
	 *                      thrown instead of being processed by the rule. Please
	 *                      use
	 *                      {@link #parseSupportsCondition(String, CSSRule, SheetContext)}
	 *                      with a style sheet argument if you do not want to supply
	 *                      a rule, otherwise namespace-related errors may be
	 *                      produced.
	 * @return the <code>{@literal @}supports</code> condition, or <code>null</code>
	 *         if a rule was specified to handle the errors, and an error was
	 *         produced.
	 * @throws CSSParseException  if there is a syntax problem and there is no error
	 *                            handler.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was
	 *                            reached.
	 * @see #parseSupportsCondition(String, CSSRule, SheetContext)
	 */
	public BooleanCondition parseSupportsCondition(String conditionText, CSSRule rule)
			throws CSSParseException, CSSBudgetException {
		AbstractCSSStyleSheet parentStyleSheet = null;
		if (rule != null) {
			parentStyleSheet = (AbstractCSSStyleSheet) rule.getParentStyleSheet();
		}
		return parseSupportsCondition(conditionText, rule, parentStyleSheet);
	}

	/**
	 * Parse the condition text of a <code>{@literal @}supports</code> rule.
	 * 
	 * @param conditionText    the condition text.
	 * @param rule             the rule that would process the error. If
	 *                         <code>null</code>, a problem while parsing shall
	 *                         result in an exception. Note that
	 *                         <code>NOT_SUPPORTED_ERR</code> exceptions are always
	 *                         thrown instead of being processed by the rule.
	 * @param parentStyleSheet the parent style sheet context. It is necessary to
	 *                         provide information related to namespaces, as well as
	 *                         customizing the serialization.
	 * @return the <code>{@literal @}supports</code> condition, or <code>null</code>
	 *         if a rule was specified to handle the errors, and an error was
	 *         produced.
	 * @throws CSSParseException  if there is a syntax problem and there is no error
	 *                            handler.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was
	 *                            reached.
	 */
	public BooleanCondition parseSupportsCondition(String conditionText, CSSRule rule,
			SheetContext parentStyleSheet) throws CSSParseException, CSSBudgetException {
		SupportsManager manager = new SupportsManager(rule, parentStyleSheet);
		TokenProducer tp = manager.createTokenProducer();
		try {
			tp.parse(conditionText, "/*", "*/");
		} catch (IndexOutOfBoundsException e) {
			throw new CSSBudgetException("Nested conditions exceed limit", e);
		}
		SupportsTokenHandler supportsHandler = manager.getInitialTokenHandler();

		if (supportsHandler.errorCode == 0) {
			return supportsHandler.getCondition();
		} else {
			return null;
		}
	}

	private class SupportsManager extends CSSParserHandlerManager {

		private SupportsTokenHandler supportsHandler;

		SupportsManager(CSSRule rule, SheetContext parentStyleSheet) {
			this.supportsHandler = new SupportsTokenHandler(rule, parentStyleSheet) {

				@Override
				public HandlerManager getManager() {
					return SupportsManager.this;
				}

			};
		}

		@Override
		protected SupportsTokenHandler getInitialTokenHandler() {
			return supportsHandler;
		}

	}

	/**
	 * Create a new factory for {@code @supports} conditions.
	 * 
	 * @param parentSheet the {@code @supports} rule's parent style sheet context.
	 * @return the factory.
	 */
	protected io.sf.carte.doc.style.css.SupportsConditionFactory createSupportsConditionFactory(
			SheetContext parentSheet) {
		return new SupportsConditionFactory(parentSheet);
	}

	/**
	 * Parse a media query string into the given handler.
	 * 
	 * @param media        the media query text.
	 * @param queryFactory the query factory.
	 * @param mqhandler    the media query list handler.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was
	 *                            reached.
	 */
	@Override
	public void parseMediaQueryList(String media, MediaQueryFactory queryFactory,
			MediaQueryHandler mqhandler) throws CSSBudgetException {
		MediaQueryManager manager = new MediaQueryManager(queryFactory, mqhandler);
		parseMediaQueryList(media, manager, mqhandler);
	}

	/**
	 * Parse a media query string into the given handler.
	 * 
	 * @param media     the media query text.
	 * @param manager   the query manager.
	 * @param mqhandler the media query list handler.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was
	 *                            reached.
	 */
	private void parseMediaQueryList(String media, MediaQueryManager manager,
			MediaQueryHandler mqhandler) throws CSSBudgetException {
		TokenProducer tp = manager.createTokenProducer();
		mqhandler.startQuery();

		try {
			tp.parse(media, "/*", "*/");
		} catch (IndexOutOfBoundsException e) {
			CSSParseException ex = manager.getInitialTokenHandler().createException(0,
					ParseHelper.ERR_UNSUPPORTED, "Nested queries exceed limit.");
			ex.initCause(e);
			mqhandler.invalidQuery(ex);
			throw new CSSBudgetException("Nested queries exceed limit", e);
		}
	}

	/**
	 * Parse a media query string.
	 * 
	 * @param media the media query text.
	 * @param owner the node that owns the responsibility to handle the errors in
	 *              the query list.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was
	 *                            reached.
	 */
	@Override
	public MediaQueryList parseMediaQueryList(String media, Node owner) throws CSSBudgetException {
		MediaQueryFactory mediaQueryFactory = getMediaQueryFactory();
		MediaQueryHandler mqhandler = mediaQueryFactory.createMediaQueryHandler(owner);
		MediaQueryManager manager = new MediaQueryManager(mediaQueryFactory, mqhandler);
		TokenProducer tp = manager.createTokenProducer();
		mqhandler.startQuery();

		try {
			tp.parse(media, "/*", "*/");
		} catch (IndexOutOfBoundsException e) {
			CSSParseException ex = manager.getInitialTokenHandler().createException(0,
					ParseHelper.ERR_UNSUPPORTED, "Nested queries exceed limit.");
			ex.initCause(e);
			mqhandler.invalidQuery(ex);
			throw new CSSBudgetException("Nested queries exceed limit", e);
		}

		return mqhandler.getMediaQueryList();
	}

	protected MediaQueryFactory getMediaQueryFactory() {
		return new NSACMediaQueryFactory();
	}

	private class MediaQueryManager extends CSSParserHandlerManager {

		private final MediaQueryTokenHandler mqhandler;

		MediaQueryManager(MediaQueryFactory mediaQueryFactory, MediaQueryHandler mqhandler) {
			this.mqhandler = new MediaQueryTokenHandler(mediaQueryFactory, mqhandler) {

				@Override
				public HandlerManager getManager() {
					return MediaQueryManager.this;
				}

			};
		}

		@Override
		protected MediaQueryTokenHandler getInitialTokenHandler() {
			return mqhandler;
		}

	}

	private interface DelegateHandler extends ContentHandler<RuntimeException> {

		default void preBooleanHandling(int index, Type type) {
		}

		@Override
		default void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			// Not called
		}

		@Override
		default void commented(int index, int commentType, String comment) {
			// Not called
		}

		default boolean isAllowedTopLevel() {
			return false;
		}

	}

	abstract private class ConditionTokenHandler<F extends BooleanConditionFactory>
			extends DefaultTokenHandler {

		final F conditionFactory;

		/**
		 * The condition that we are currently working at in this handler.
		 */
		BooleanCondition currentCond = null;

		/**
		 * Index of nested operation's depth.
		 */
		private int opDepthIndex = 0;

		/**
		 * Number of unclosed left parentheses at each operation level (up to
		 * <code>opDepthIndex</code>).
		 */
		private final short[] opParenDepth = new short[32]; // Limited to 32 nested expressions

		private boolean topLevel = true;

		private DelegateHandler predicateHandler;

		/**
		 * Are we reading a predicate instead of processing operation syntax ?
		 */
		boolean readingPredicate = false;

		ConditionTokenHandler(F conditionFactory) {
			super();
			this.conditionFactory = conditionFactory;
		}

		@Override
		protected void initializeBuffer() {
			buffer = new StringBuilder(64);
		}

		DelegateHandler getPredicateHandler() {
			return predicateHandler;
		}

		void setPredicateHandler(DelegateHandler predicateHandler) {
			this.predicateHandler = predicateHandler;
		}

		@Override
		short getCurrentParenDepth() {
			return opParenDepth[opDepthIndex];
		}

		@Override
		boolean isTopLevel() {
			return topLevel;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (!readingPredicate) {
				if (buffer.length() == 0) {
					processWord(index, word.toString());
				} else {
					unexpectedTokenError(index, word);
				}
			} else if (getCurrentParenDepth() > 1 || predicateHandler.isAllowedTopLevel()) {
				predicateHandler.word(index, word);
			} else {
				processWord(index, word.toString());
			}
			prevcp = 65;
		}

		private void processWord(int index, String word) {
			String lctoken = word.toLowerCase(Locale.ROOT);
			if ("not".equals(lctoken)) {
				predicateHandler.preBooleanHandling(index, BooleanCondition.Type.NOT);
				BooleanCondition newCond = conditionFactory.createNotCondition();
				if (currentCond != null) {
					currentCond.addCondition(newCond);
				}
				setNestedCondition(newCond);
			} else if ("and".equals(lctoken)) {
				predicateHandler.preBooleanHandling(index, BooleanCondition.Type.AND);
				if (currentCond != null) {
					processOperation(index, BooleanCondition.Type.AND, word);
				} else {
					processImplicitAnd(index);
				}
			} else if ("or".equals(lctoken)) {
				if (currentCond != null) {
					predicateHandler.preBooleanHandling(index, BooleanCondition.Type.OR);
					processOperation(index, BooleanCondition.Type.OR, word);
				} else {
					unexpectedTokenError(index, word);
				}
			} else {
				readingPredicate = true;
				predicateHandler.word(index, word);
			}
		}

		void processOperation(int index, BooleanCondition.Type opType, String opname) {
			BooleanCondition operation = currentCond.getParentCondition();
			BooleanCondition.Type curType = currentCond.getType();
			if (curType == BooleanCondition.Type.PREDICATE
					|| curType == BooleanCondition.Type.SELECTOR_FUNCTION) {
				if (operation == null) {
					BooleanCondition newCond = createOperation(index, opType);
					newCond.addCondition(currentCond);
					setNestedCondition(newCond);
				} else if (operation.getType() == opType) {
					currentCond = operation;
				} else {
					BooleanCondition newCond = createOperation(index, opType);
					if (getCurrentParenDepth() != 0) {
						BooleanCondition oldCond = operation.replaceLast(newCond);
						newCond.addCondition(oldCond);
					} else {
						newCond.addCondition(operation);
					}
					setNestedCondition(newCond);
				}
			} else if (curType == BooleanCondition.Type.NOT) {
				if (operation != null) {
					BooleanCondition newCond = createOperation(index, opType);
					BooleanCondition oldCond = operation.replaceLast(newCond);
					newCond.addCondition(oldCond);
					setNestedCondition(newCond);
				} else if (opType == BooleanCondition.Type.AND
						|| opType == BooleanCondition.Type.OR) {
					BooleanCondition newCond = createOperation(index, opType);
					newCond.addCondition(currentCond);
					setNestedCondition(newCond);
				} else {
					unexpectedTokenError(index, opname);
				}
			} else if (curType != opType) {
				if (getCurrentParenDepth() != 0 || !topLevel) {
					unexpectedTokenError(index, opname);
				} else {
					BooleanCondition newCond = createOperation(index, opType);
					newCond.addCondition(currentCond);
					setNestedCondition(newCond);
				}
			}
		}

		BooleanCondition createOperation(int index, BooleanCondition.Type opType)
				throws CSSParseException {
			if (opType == BooleanCondition.Type.AND) {
				return conditionFactory.createAndCondition();
			}
			return conditionFactory.createOrCondition();
		}

		private void setNestedCondition(BooleanCondition newCond) {
			currentCond = newCond;
			opDepthIndex++;
		}

		void processImplicitAnd(int index) {
			unexpectedTokenError(index, "and");
		}

		@Override
		void processBuffer(int index, int triggerCp) {
			// Not called
			unexpectedCharError(index, triggerCp);
		}

		@Override
		public void leftParenthesis(int index) {
			opParenDepth[opDepthIndex]++;
			predicateHandler.leftParenthesis(index);
			readingPredicate = true;
			prevcp = TokenProducer.CHAR_LEFT_PAREN;
		}

		@Override
		public void leftSquareBracket(int index) {
			predicateHandler.leftSquareBracket(index);
			readingPredicate = true;
			prevcp = TokenProducer.CHAR_LEFT_SQ_BRACKET;
		}

		@Override
		public void leftCurlyBracket(int index) {
			predicateHandler.leftCurlyBracket(index);
			readingPredicate = true;
			prevcp = TokenProducer.CHAR_LEFT_CURLY_BRACKET;
		}

		void handleLeftCurlyBracket(int index) {
			unexpectedLeftCurlyBracketError(index);
		}

		@Override
		public void rightParenthesis(int index) {
			opParenDepth[opDepthIndex]--;
			if (opParenDepth[opDepthIndex] < 0) {
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
			} else if (readingPredicate) {
				predicateHandler.rightParenthesis(index);
			} else if (buffer.length() != 0) {
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
			}
			topLevel = true;
			if (opParenDepth[opDepthIndex] == 0 && currentCond != null && opDepthIndex != 0) {
				opDepthIndex--;
				if (currentCond.getParentCondition() != null) {
					currentCond = currentCond.getParentCondition();
					topLevel = false;
				}
			}
			prevcp = TokenProducer.CHAR_RIGHT_PAREN;
		}

		@Override
		public void rightSquareBracket(int index) {
			if (readingPredicate) {
				predicateHandler.rightSquareBracket(index);
				prevcp = TokenProducer.CHAR_RIGHT_SQ_BRACKET;
			} else {
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
			}
		}

		@Override
		public void rightCurlyBracket(int index) {
			if (readingPredicate) {
				predicateHandler.rightCurlyBracket(index);
				prevcp = TokenProducer.CHAR_RIGHT_CURLY_BRACKET;
			} else {
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
			}
		}

		@Override
		public void character(int index, int codepoint) {
			if (!isInError()) {
				if (!readingPredicate) {
					if (codepoint == 44) { // ','
						predicateHandler.character(index, codepoint);
					} else {
						if (getCurrentParenDepth() == 0 && opDepthIndex == 0) {
							if (codepoint == TokenProducer.CHAR_SEMICOLON) {
								endOfCondition(index);
								handleSemicolon(index);
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else {
							unexpectedCharError(index, codepoint);
							handleErrorRecovery();
						}
					}
				} else {
					predicateHandler.character(index, codepoint);
				}
			} else if (codepoint == 44) { // ',' may clear error
				predicateHandler.character(index, codepoint);
			}
		}

		protected void handleSemicolon(int index) {
			unexpectedSemicolonError(index);
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
			if (!isInError()) {
				if (readingPredicate) {
					predicateHandler.quoted(index, quoted, quoteCp);
					prevcp = 65;
				} else {
					reportError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected: '" + quoted + '\'');
				}
			}
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			quoted(index, quoted, quoteCp);
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (!isInError()) {
				if (readingPredicate) {
					predicateHandler.escaped(index, codepoint);
				} else if (prevcp == TokenProducer.CHAR_LEFT_PAREN) {
					readingPredicate = true;
					predicateHandler.escaped(index, codepoint);
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected escaped character: \\u" + Integer.toHexString(codepoint));
				}
				prevcp = codepoint;
			}
		}

		@Override
		public void separator(int index, int codepoint) {
			if (!isInError()) {
				if (readingPredicate) {
					predicateHandler.separator(index, codepoint);
				}
				setWhitespacePrevCp();
			}
		}

		/*
		 * @Override public void control(int index, int codepoint) {
		 * super.control(index, codepoint); if (isEscapedIdent() &&
		 * CSSParser.bufferEndsWithEscapedCharOrWS(buffer)) { resetEscapedTokenIndex();
		 * buffer.append(' '); // break the escape } }
		 */

		@Override
		public void commented(int index, int commentType, String comment) {
			separator(index, 32);
			// The above call may have left prevcp as 10
			prevcp = 32;
		}

		@Override
		public void endOfStream(int len) {
			endOfCondition(len);
		}

		void endOfCondition(int index) {
			if (opParenDepth[opDepthIndex] != 0) {
				handleError(index, ParseHelper.ERR_UNMATCHED_PARENTHESIS, "Unmatched parenthesis");
			} else if (!isInError()) {
				predicateHandler.endOfStream(index);
			}
		}

		@Override
		void reportError(int index, byte errCode, String message) throws CSSParseException {
			throw createException(index, errCode, message);
		}

		@Override
		protected void resetHandler() {
			// do not reset parendepth nor opDepthIndex or opParenDepth[].
			prevcp = 32;
			parseError = false;
			currentCond = null;
			buffer.setLength(0);
			resetEscapedTokenIndex();
		}

	}

	abstract private class SupportsTokenHandler
			extends ConditionTokenHandler<io.sf.carte.doc.style.css.SupportsConditionFactory> {

		/*
		 * Error-related fields.
		 */
		private byte errorCode = 0;
		private CSSParseException errorException = null;
		private final CSSRule rule;

		SupportsTokenHandler(CSSRule rule, SheetContext parentStyleSheet) {
			super(createSupportsConditionFactory(parentStyleSheet));
			this.rule = rule;
			setPredicateHandler(new SupportsDelegateHandler());
		}

		SupportsTokenHandler(CSSRule rule) {
			this(rule, (SheetContext) handler.getStyleSheet());
		}

		BooleanCondition getCondition() {
			BooleanCondition condition = currentCond;
			if (condition != null) {
				while (condition.getParentCondition() != null) {
					condition = condition.getParentCondition();
				}
			}
			return condition;
		}

		private SelectorList parseSelectors(String seltext) throws CSSException {
			SelectorTokenHandler selectorHandler = new SelectorTokenHandler(
					new NSACSelectorFactory()) {

				@Override
				public void reportError(CSSParseException ex) throws CSSParseException {
					throw ex;
				}

			};

			selectorHandler.setManager(getManager());
			CharacterCheck ccheck = new IdentCharacterCheck();
			TokenProducer tp = new TokenProducer(ccheck, streamSizeLimit);
			tp.setContentHandler(selectorHandler);
			tp.setErrorHandler(selectorHandler);
			tp.setControlHandler(new ChildControlTokenHandler(getControlHandler(), 0));
			tp.parse(seltext, "/*", "*/");

			return selectorHandler.getTrimmedSelectorList();
		}

		@Override
		void reportError(int index, byte errCode, String message) {
			if (!isInError()) {
				if (errorCode == 0) {
					errorCode = errCode;
					errorException = createException(index, errCode, message);
					reportError(errorException);
				}
				setParseError();
			}
		}

		@Override
		void handleError(int index, byte errCode, String message, Throwable cause) {
			if (!isInError()) {
				if (errorCode == 0) {
					errorCode = errCode;
					errorException = createException(index, errCode, message);
					errorException.initCause(cause);
					handleError(errorException);
				}
				setParseError();
			}
		}

		@Override
		public void reportError(CSSParseException ex) throws CSSParseException {
			if (errorCode == 0) {
				errorCode = ParseHelper.ERR_RULE_SYNTAX;
				errorException = ex;
			}
			if (rule != null) {
				rule.getParentStyleSheet().getErrorHandler().ruleParseError(rule, ex);
				setParseError();
			} else {
				super.reportError(ex);
			}
		}

		@Override
		protected void resetHandler() {
			super.resetHandler();
			errorCode = 0;
		}

		private class SupportsDelegateHandler implements DelegateHandler {

			/**
			 * Are we reading a value instead of processing a property name ?
			 */
			private boolean readingValue = false;

			/**
			 * Are we in a function token?
			 */
			private boolean functionToken = false;

			/**
			 * Number of unclosed left parentheses when starting to read a predicate value
			 * or function.
			 */
			private short valueParendepth;

			SupportsDelegateHandler() {
				super();
			}

			@Override
			public boolean isAllowedTopLevel() {
				return functionToken;
			}

			@Override
			public void word(int index, CharSequence word) {
				if (buffer.length() != 0) {
					if (!readingValue && !functionToken) {
						unexpectedTokenError(index, word);
						return;
					} else if (isPrevCpWhitespace()) {
						buffer.append(' ');
					}
				}
				buffer.append(word);
			}

			@Override
			public void leftParenthesis(int index) {
				if (readingValue || functionToken) {
					buffer.append('(');
				} else if (buffer.length() != 0) {
					if (!isPrevCpWhitespace()) {
						// Function token
						String fname = buffer.toString();
						buffer.setLength(0);
						resetEscapedTokenIndex();
						if (!"selector".equalsIgnoreCase(fname)) {
							unexpectedTokenError(index, "Unknown function: " + fname);
							return;
						}
						functionToken = true;
						valueParendepth = getCurrentParenDepth();
						valueParendepth--;
					} else {
						unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
					}
				}
				prevcp = TokenProducer.CHAR_LEFT_PAREN;
			}

			@Override
			public void leftSquareBracket(int index) {
				if (readingValue || functionToken) {
					buffer.append('[');
				} else {
					unexpectedCharError(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
				}
			}

			@Override
			public void leftCurlyBracket(int index) {
				handleLeftCurlyBracket(index);
			}

			@Override
			public void rightParenthesis(int index) {
				if (readingValue) {
					if (valueParendepth == getCurrentParenDepth()) {
						String svalue = buffer.toString();
						buffer.setLength(0);
						if (!svalue.isEmpty()) {
							setDeclarationPredicate(index, svalue);
						} else {
							unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
						}
						readingValue = false;
						readingPredicate = false;
						resetEscapedTokenIndex();
					} else {
						buffer.append(')');
					}
				} else if (functionToken) {
					if (valueParendepth == getCurrentParenDepth()) {
						functionToken = false;
						readingPredicate = false;
						resetEscapedTokenIndex();
						prevcp = TokenProducer.CHAR_RIGHT_PAREN;

						BooleanCondition newCond;
						SelectorList list;
						String s = buffer.toString();
						buffer.setLength(0);
						try {
							list = parseSelectors(s);
							newCond = conditionFactory.createSelectorFunction(list);
						} catch (CSSBudgetException e) {
							handleError(index, ParseHelper.ERR_UNSUPPORTED,
									"Hit a limit while parsing @supports condition selector.", e);
							newCond = conditionFactory.createFalseCondition("selector(" + s + ')');
						} catch (CSSException e) {
							handleWarning(index, ParseHelper.ERR_UNSUPPORTED,
									"Unkown selector in @supports condition.", e);
							newCond = conditionFactory.createFalseCondition("selector(" + s + ')');
						}
						if (currentCond != null) {
							currentCond.addCondition(newCond);
						}
						currentCond = newCond;
					} else {
						buffer.append(')');
					}
				} else {
					unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
				}
			}

			private void setDeclarationPredicate(int index, String value) {
				String propertyName = ((DeclarationPredicate) currentCond).getName();
				CSSParser parser = new CSSParser(CSSParser.this.parserFlags);
				Reader re = new StringReader(value);
				LexicalUnit lunit;
				try {
					lunit = parser.parsePropertyValue(propertyName, re);
				} catch (Exception e) {
					warnAndSetFalseCondition(index, propertyName, value, e);
					return;
				}
				try {
					((DeclarationPredicate) currentCond).setValue(lunit);
				} catch (Exception e) {
					warnAndSetFalseCondition(index, propertyName, value, e);
				}
			}

			private void warnAndSetFalseCondition(int index, String propertyName, String svalue,
					Exception e) {
				handleWarning(index, ParseHelper.WARN_VALUE, "Bad @supports condition value.", e);
				// Replace the failed condition, maybe it's valid CSS
				StringBuilder buf = new StringBuilder(32);
				buf.append('(').append(propertyName).append(':').append(svalue).append(')');
				BooleanCondition newCond = conditionFactory.createFalseCondition(buf.toString());
				newCond.setParentCondition(currentCond.getParentCondition());
				currentCond = newCond;
			}

			@Override
			public void rightSquareBracket(int index) {
				if (readingValue || functionToken) {
					buffer.append(']');
				} else {
					unexpectedCharError(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
				}
			}

			@Override
			public void rightCurlyBracket(int index) {
				if (readingValue) {
					buffer.append('}');
				} else {
					unexpectedCharError(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
				}
			}

			@Override
			public void character(int index, int codepoint) {
				// ! 33
				// : 58
				// ; 59
				if (readingValue) {
					if (codepoint == 59) {
						unexpectedCharError(index, codepoint);
					} else {
						bufferAppend(codepoint);
					}
				} else if (functionToken) {
					bufferAppend(codepoint);
				} else {
					if (codepoint == 58 && getCurrentParenDepth() > 0) {
						BooleanCondition newCond = conditionFactory
								.createPredicate(buffer.toString());
						if (currentCond != null) {
							currentCond.addCondition(newCond);
						}
						currentCond = newCond;
						buffer.setLength(0);
						valueParendepth = getCurrentParenDepth();
						valueParendepth--;
						readingValue = true;
						resetEscapedTokenIndex();
					} else {
						unexpectedCharError(index, codepoint);
					}
				}
				prevcp = codepoint;
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				if (readingValue || functionToken) {
					if (buffer.length() != 0) {
						buffer.append(' ');
					}
					char c = (char) quoteCp;
					buffer.append(c).append(quoted).append(c);
					prevcp = 65;
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected: '" + quoted + '\'');
				}
			}

			@Override
			public void escaped(int index, int codepoint) {
				if (isEscapedCodepoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				bufferAppend(codepoint);
			}

			@Override
			public void separator(int index, int cp) {
				if (isEscapedIdent() && bufferEndsWithEscapedCharOrWS(buffer)) {
					buffer.append(' ');
				}
			}

			@Override
			public void endOfStream(int len) {
				if (readingPredicate) {
					unexpectedEOFError(len);
				} else if (buffer.length() != 0) {
					reportError(len, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected token: " + buffer);
				} else if (currentCond == null) {
					unexpectedEOFError(len, "No condition found");
				}
			}

		}

	}

	abstract private class MediaQueryTokenHandler extends ConditionTokenHandler<MediaQueryFactory> {

		private final HashSet<String> mediaTypes = new HashSet<>(10);

		MediaQueryTokenHandler(MediaQueryFactory conditionFactory, MediaQueryHandler mqhandler) {
			super(conditionFactory);
			setPredicateHandler(new MediaQueryDelegateHandler(mqhandler));
			// initialize media types
			String[] mediaTypesArray = { "all", "braille", "embossed", "handheld", "print",
					"projection", "screen", "speech", "tty", "tv" };
			Collections.addAll(mediaTypes, mediaTypesArray);
		}

		private boolean isValidMediaType(String lcmedia) {
			return mediaTypes.contains(lcmedia);
		}

		@Override
		MediaQueryDelegateHandler getPredicateHandler() {
			return (MediaQueryDelegateHandler) super.getPredicateHandler();
		}

		@Override
		void processImplicitAnd(int index) {
			MediaQueryDelegateHandler mqhelper = getPredicateHandler();
			String medium = mqhelper.mediaType;
			if (medium == null) {
				if (buffer.length() != 0) {
					mqhelper.processMediaType(index);
				} else {
					unexpectedTokenError(index, "and");
					return;
				}
			}
			currentCond = conditionFactory.createMediaTypePredicate(medium);
			processOperation(index, BooleanCondition.Type.AND, "and");
		}

		@Override
		BooleanCondition createOperation(int index, BooleanCondition.Type opType)
				throws CSSParseException {
			if (opType == BooleanCondition.Type.AND) {
				return conditionFactory.createAndCondition();
			}
			if (getPredicateHandler().mediaType == null) {
				return conditionFactory.createOrCondition();
			}
			throw createException(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected 'OR'");
		}

		void emptyQuery(int index) {
		}

		@Override
		public void unexpectedEOFError(int len, String message) {
			reportError(len, ParseHelper.ERR_UNEXPECTED_EOF, message);
		}

		@Override
		void unexpectedTokenError(int index, CharSequence token) {
			reportError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: " + token);
		}

		@Override
		void reportError(int index, byte errCode, String message) {
			if (!isInError()) {
				CSSParseException ex = createException(index, errCode, message);
				reportError(ex);
			}
		}

		@Override
		public void reportError(CSSParseException ex) throws CSSParseException {
			MediaQueryDelegateHandler mqhelper = getPredicateHandler();
			mqhelper.handler.invalidQuery(ex);
			if (!mqhelper.handler.reportsErrors() && errorHandler != null) {
				super.reportError(ex);
			}
			setParseError();
		}

		@Override
		public void handleError(int index, byte errCode, String message) {
			if (!isInError()) {
				CSSParseException ex = createException(index, errCode, message);
				reportError(ex);
			}
		}

		@Override
		void handleError(int index, byte errCode, String message, Throwable cause) {
			if (!isInError()) {
				CSSParseException ex = createException(index, errCode, message);
				ex.initCause(cause);
				reportError(ex);
			}
		}

		@Override
		public void handleWarning(int index, byte errCode, String message, Throwable cause) {
			if (!isInError()) {
				MediaQueryDelegateHandler mqhelper = getPredicateHandler();
				CSSParseException ex = createException(index, errCode, message);
				if (cause != null) {
					ex.initCause(cause);
				}
				mqhelper.handler.compatQuery(ex);
				if (errorHandler != null) {
					errorHandler.warning(ex);
				}
			}
		}

		@Override
		CSSParseException createException(int index, byte errCode, String message) {
			setCurrentLocation(index);
			Locator locator = getControlHandler().createLocator();
			return new CSSMediaParseException(message, locator);
		}

		class MediaQueryDelegateHandler implements DelegateHandler {

			private final MediaQueryHandler handler;
			private byte stage = 0;
			private boolean negativeQuery = false;
			private boolean spaceFound = false;
			private String mediaType = null;
			private String featureName = null;
			private String firstValue = null;
			private byte rangeType = 0; // Type of range expression, 0 if none
			private boolean functionToken = false;

			private static final int WORD_UNQUOTED = 0;

			private MediaQueryDelegateHandler(MediaQueryHandler handler) {
				super();
				this.handler = handler;
			}

			MediaQueryHandler getMediaQueryHandler() {
				return handler;
			}

			@Override
			public void word(int index, CharSequence word) {
				// @formatter:off
				//
				// Stages:
				// not medium and ( feature : value )
				//        0  | 1 |2|   3     |  4    |1
				// not medium and ( value1  <= feature < value2 )
				//        0  | 1 |2|   3     |5|  6     |7       |1
				// 127 = error
				//
				// @formatter:on
				if (stage == 127) {
					return;
				}
				if (functionToken) {
					if (buffer.length() != 0 && isPrevCpWhitespace()) {
						buffer.append(' ');
					}
					buffer.append(word);
				} else if (ParseHelper.equalsIgnoreCase(word, "not")) {
					if (stage != 0) {
						reportError(index, ParseHelper.ERR_RULE_SYNTAX,
								"Found 'not' at the wrong parsing stage");
					} else {
						negativeQuery = true;
					}
				} else if (ParseHelper.equalsIgnoreCase(word, "only")) {
					if (stage != 0) {
						reportError(index, ParseHelper.ERR_RULE_SYNTAX,
								"Found 'only' at the wrong parsing stage");
					} else {
						handler.onlyPrefix();
					}
				} else if (ParseHelper.equalsIgnoreCase(word, "or")) {
					reportError(index, ParseHelper.ERR_RULE_SYNTAX, "Found 'or'");
				} else { // rest of cases are collected to buffer
					if (!appendWord(index, word, WORD_UNQUOTED)) {
						return;
					}
				}
				prevcp = 65; // A
			}

			private boolean appendWord(int index, CharSequence word, int quote) {
				if (buffer.length() != 0 && !isEscapedIdent() && isPrevCpWhitespace()) {
					if (stage == 1) {
						reportError(index, ParseHelper.ERR_RULE_SYNTAX,
								"Found white space between media");
						return false;
					}
					spaceFound = true;
					buffer.append(' ');
				}
				if (quote == WORD_UNQUOTED) {
					buffer.append(word);
				} else {
					char c = (char) quote;
					buffer.append(c).append(word).append(c);
				}
				if (!functionToken) {
					if (stage == 0) {
						stage = 1;
					} else if (stage == 5) { // after "value [<][=]"
						stage = 6;
					}
				}
				return true;
			}

			@Override
			public void preBooleanHandling(int index, Type type) {
				switch (type) {
				case AND:
					if (stage > 1) {
						reportError(index, ParseHelper.ERR_RULE_SYNTAX,
								"Found 'and' at the wrong parsing stage");
						return;
					}
					if (buffer.length() != 0) {
						processMediaType(index);
					}
				case OR:
					stage = 2;
					break;
				default: // NOT
				}
			}

			/**
			 * Process a media type from buffer.
			 * <p>
			 * stage 0 or 1 is assumed, as well as a non-empty buffer.
			 * 
			 * @param index the index.
			 */
			private void processMediaType(int index) {
				if (mediaType == null && getCurrentParenDepth() == 0) {
					mediaType = rawBuffer().trim();
					if (currentCond != null && isEmptyNotCondition()) {
						currentCond = null;
						negativeQuery = true;
						handler.negativeQuery();
					}
					handler.mediaType(mediaType);
				}
			}

			/**
			 * Checks whether the current condition is a stand-alone, empty <code>NOT</code>
			 * condition. Assumes currentCond != null
			 * 
			 * @return <code>true</code> if the current condition is a stand-alone, empty
			 *         <code>NOT</code> condition.
			 */
			private boolean isEmptyNotCondition() {
				return currentCond.getType() == Type.NOT && currentCond.getParentCondition() == null
						&& currentCond.getNestedCondition() == null;
			}

			@Override
			public void leftParenthesis(int index) {
				if (functionToken) {
					buffer.append('(');
				} else if (buffer.length() != 0) {
					if (!isPrevCpWhitespace()) {
						// Function token
						functionToken = true;
						buffer.append('(');
					} else {
						unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
					}
				} else {
					if (stage == 2 || stage == 0) {
						stage = 3;
					}
				}
				prevcp = TokenProducer.CHAR_LEFT_PAREN;
			}

			@Override
			public void leftCurlyBracket(int index) {
				handleLeftCurlyBracket(index);
				prevcp = TokenProducer.CHAR_LEFT_CURLY_BRACKET;
			}

			@Override
			public void leftSquareBracket(int index) {
				prevcp = TokenProducer.CHAR_LEFT_CURLY_BRACKET;
				unexpectedCharError(index, prevcp);
			}

			@Override
			public void rightParenthesis(int index) {
				if (functionToken) {
					buffer.append(')');
					functionToken = false;
				} else {
					if (stage == 6) {
						processBuffer(index);
						// Need to determine whether we have "value <|>|= feature"
						// or "feature <|>|= value"
						if (firstValue != null && isKnownFeature(firstValue)) {
							String tempstr = firstValue;
							firstValue = featureName;
							featureName = tempstr;
						} else if (!isKnownFeature(featureName)) {
							if (isValidFeatureSyntax(firstValue)) {
								String tempstr = firstValue;
								firstValue = featureName;
								featureName = tempstr;
							} else if (!isValidFeatureSyntax(featureName)) {
								reportError(index, ParseHelper.ERR_RULE_SYNTAX,
										"Wrong feature expression near " + featureName + " "
												+ firstValue + ")");
								prevcp = TokenProducer.CHAR_RIGHT_PAREN;
								return;
							} else {
								reverseRangetype();
							}
						} else {
							reverseRangetype();
						}
						LexicalUnit value1 = parseMediaFeature(index, firstValue);
						handlePredicate(index, featureName, rangeType, value1, firstValue);
					} else if (buffer.length() != 0) {
						if (stage == 4) {
							String valueSer = buffer.toString();
							LexicalUnit value = parseMediaFeature(index, valueSer);
							handlePredicate(index, featureName, (byte) 0, value, valueSer);
						} else if (stage == 7) {
							LexicalUnit value1 = parseMediaFeature(index, firstValue);
							LexicalUnit value2 = parseMediaFeature(index, buffer.toString());
							handlePredicate(index, featureName, rangeType, value1, value2);
						} else if (stage == 3 && !spaceFound) {
							handleMediaPredicate(index, buffer.toString());
						} else {
							reportError(index, ParseHelper.ERR_EXPR_SYNTAX, buffer.toString());
						}
						buffer.setLength(0);
						spaceFound = false;
						resetEscapedTokenIndex();
					} else {
						unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
					}
					if (stage == 5) {
						unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
					} else {
						rangeType = 0;
						stage = 1;
					}
					readingPredicate = false;
				}
				prevcp = TokenProducer.CHAR_RIGHT_PAREN;
			}

			private LexicalUnit parseMediaFeature(int index, String feature) {
				Reader re = new StringReader(feature);
				HandlerManager manager = MediaQueryTokenHandler.this.getManager();
				LexicalUnit lunit;
				try {
					lunit = parsePropertyValue(re, manager, index);
				} catch (RuntimeException e) {
					lunit = null;
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
				LexicalUnit nlu;
				if (lunit == null || ((nlu = lunit.getNextLexicalUnit()) != null
						&& (nlu.getLexicalUnitType() != LexicalType.OPERATOR_SLASH
								|| nlu.getNextLexicalUnit() == null))) {
					reportError(index, ParseHelper.ERR_EXPR_SYNTAX,
							"Invalid feature value: " + feature);
					lunit = null;
				}
				return lunit;
			}

			private void handleMediaPredicate(int index, String featureName) {
				String lcFeatureName;
				if (currentCond == null && mediaType == null
						&& isValidMediaType(lcFeatureName = featureName.toLowerCase(Locale.ROOT))) {
					mediaType = lcFeatureName;
					handler.mediaType(lcFeatureName);
				} else {
					MediaFeaturePredicate predicate = conditionFactory.createPredicate(featureName);
					predicate.setRangeType((byte) 0);
					if (currentCond == null) {
						currentCond = predicate;
					} else {
						currentCond.addCondition(predicate);
					}
				}
				clearPredicate();
			}

			private void handlePredicate(int index, String featureName, byte rangeType,
					LexicalUnit value, String valueSerialization) {
				BooleanCondition condition;
				if (value == null) {
					reportError(index, ParseHelper.ERR_WRONG_VALUE, valueSerialization);
					clearPredicate();
					return;
				} else {
					if (value.getLexicalUnitType() == LexicalType.COMPAT_IDENT) {
						handleWarning(index, ParseHelper.WARN_IDENT_COMPAT,
								"Probable hack in media feature.");
					}

					MediaFeaturePredicate predicate = conditionFactory.createPredicate(featureName);
					predicate.setRangeType(rangeType);
					try {
						predicate.setValue(value);
					} catch (DOMException e) {
						handleError(index, ParseHelper.ERR_WRONG_VALUE,
								e.getMessage() + ": " + valueSerialization, e);
						clearPredicate();
						return;
					}
					condition = predicate;
				}
				if (currentCond == null) {
					currentCond = condition;
				} else {
					currentCond.addCondition(condition);
				}
				clearPredicate();
			}

			private void handlePredicate(int index, String featureName, byte rangeType,
					LexicalUnit value1, LexicalUnit value2) {
				BooleanCondition condition;
				if (value1 == null) {
					reportError(index, ParseHelper.ERR_WRONG_VALUE, firstValue);
					clearPredicate();
					return;
				} else if (value2 == null) {
					String s = buffer.toString();
					reportError(index, ParseHelper.ERR_WRONG_VALUE, s);
					clearPredicate();
					return;
				} else {
					if (value1.getLexicalUnitType() == LexicalType.COMPAT_IDENT
							|| value2.getLexicalUnitType() == LexicalType.COMPAT_IDENT) {
						handleWarning(index, ParseHelper.WARN_IDENT_COMPAT,
								"Probable hack in media feature.");
					}

					MediaFeaturePredicate predicate = conditionFactory.createPredicate(featureName);
					predicate.setRangeType(rangeType);
					try {
						predicate.setValueRange(value1, value2);
					} catch (DOMException e) {
						handleError(index, ParseHelper.ERR_WRONG_VALUE,
								"Invalid value(s) in range media feature.", e);
						clearPredicate();
						return;
					}
					condition = predicate;
				}
				if (currentCond == null) {
					currentCond = condition;
				} else {
					currentCond.addCondition(condition);
				}
				clearPredicate();
			}

			/**
			 * Reverse the current range type.
			 * <p>
			 * Range type is a way to numerically characterize a range like 'a <= foo < b'
			 */
			private void reverseRangetype() {
				if ((rangeType & 2) == 2) {
					rangeType ^= 2;
					rangeType = (byte) (rangeType | 4);
				} else if ((rangeType & 4) == 4) {
					rangeType ^= 4;
					rangeType = (byte) (rangeType | 2);
				}
			}

			@Override
			public void rightSquareBracket(int index) {
				prevcp = TokenProducer.CHAR_RIGHT_SQ_BRACKET;
				unexpectedCharError(index, prevcp);
			}

			@Override
			public void rightCurlyBracket(int index) {
				prevcp = TokenProducer.CHAR_RIGHT_CURLY_BRACKET;
				unexpectedCharError(index, prevcp);
			}

			@Override
			public void character(int index, int codepoint) {
				// ! 33
				// : 58
				// ; 59
				if (functionToken) {
					if (isPrevCpWhitespace()) {
						buffer.append(' ');
					}
					bufferAppend(codepoint);
				} else {
					if (codepoint == 58) { // ':'
						if (buffer.length() != 0) {
							featureName = rawBuffer();
							stage = 4;
						} else {
							reportError(index, ParseHelper.ERR_RULE_SYNTAX, "Empty feature name");
						}
					} else if (codepoint == 44) { // ,
						if (!isInError()) {
							if (getCurrentParenDepth() != 0) {
								reportError(index, ParseHelper.ERR_RULE_SYNTAX,
										"Unmatched parenthesis");
								return;
							} else if (stage == 0) {
								reportError(index, ParseHelper.ERR_RULE_SYNTAX, "No media found");
							}
							processBuffer(index);
							endQuery(index);
						} else if (getCurrentParenDepth() == 0) {
							handler.endQuery();
							clearQuery();
						}
						handler.startQuery();
					} else if (codepoint == 46) { // .
						if (stage == 4 || stage == 3 || stage == 6 || stage == 7 || functionToken) {
							buffer.append('.');
						} else {
							unexpectedCharError(index, '.');
						}
					} else if (codepoint == 47) { // /
						if (stage == 4 || stage == 3 || stage == 6 || stage == 7 || functionToken) {
							buffer.append('/');
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 59) {
						if (stage == 1 && getCurrentParenDepth() == 0) {
							if (!isInError()) {
								processBuffer(index);
								endQuery(index);
							} else {
								handler.endQuery();
								clearQuery();
							}
							handleSemicolon(index);
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 60) { // <
						// rangeType:
						// = 1, < 2, > 4,
						// <= 3, >= 5
						// a <= foo < b ; 19
						// a >= foo > b ; 37
						if (stage < 3 || (rangeType > 3
								&& ((rangeType & 16) != 0 || (rangeType & 4) != 0))) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage != 6 && stage != 7) {
								rangeType = (byte) (rangeType | 2);
								stage = 5;
							} else {
								processBuffer(index);
								rangeType = (byte) (rangeType | 16);
								stage = 7;
							}
						}
					} else if (codepoint == 61) { // =
						if (stage < 3 || (rangeType > 5 && (rangeType & 8) != 0)) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage != 6 && stage != 7) {
								rangeType = (byte) (rangeType | 1);
								stage = 5;
							} else {
								processBuffer(index);
								rangeType = (byte) (rangeType | 8);
								stage = 7;
							}
						}
					} else if (codepoint == 62 || (rangeType >= 4
							&& ((rangeType & 32) != 0 || (rangeType & 2) != 0))) { // >
						if (stage < 3) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage != 6 && stage != 7) {
								rangeType = (byte) (rangeType | 4);
								stage = 5;
							} else {
								processBuffer(index);
								rangeType = (byte) (rangeType | 32);
								stage = 7;
							}
						}
					} else {
						unexpectedCharError(index, codepoint);
					}
					if (stage == 5 && firstValue == null && buffer.length() != 0) {
						firstValue = rawBuffer();
					}
				}
			}

			private void processBuffer(int index) {
				if (buffer.length() != 0) {
					if (stage == 1) {
						processMediaType(index);
						if (mediaType == null) {
							unexpectedTokenError(index, buffer.toString());
							buffer.setLength(0);
						}
						readingPredicate = false;
					} else if (stage == 6) {
						featureName = rawBuffer();
					}
				}
			}

			private void endQuery(int index) {
				if (currentCond != null) {
					while (currentCond.getParentCondition() != null) {
						currentCond = currentCond.getParentCondition();
					}
					handler.condition(currentCond);
				} else if (negativeQuery && mediaType == null) {
					reportError(index, ParseHelper.ERR_EXPR_SYNTAX,
							"Negative query without media.");
				}
				handler.endQuery();
				clearQuery();
			}

			private void clearQuery() {
				currentCond = null;
				mediaType = null;
				stage = 0;
				negativeQuery = false;
				functionToken = false;
				resetHandler();
				clearPredicate();
			}

			private void clearPredicate() {
				featureName = null;
				firstValue = null;
				rangeType = 0;
				spaceFound = false;
			}

			String rawBuffer() {
				spaceFound = false;
				return MediaQueryTokenHandler.this.rawBuffer();
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				reportError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected: '" + quoted + '\'');
			}

			@Override
			public void escaped(int index, int codepoint) {
				if (isEscapedCodepoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				bufferAppend(codepoint);
				if (stage == 5) {
					stage = 6;
				} else if (stage == 0) {
					stage = 1;
				}
			}

			@Override
			public void separator(int index, int cp) {
				if (isEscapedIdent() && bufferEndsWithEscapedCharOrWS(buffer)) {
					buffer.append(' ');
				}
			}

			@Override
			public void endOfStream(int len) {
				if (stage == 1) {
					processBuffer(len);
				}
				if (currentCond == null && mediaType == null) {
					if (buffer.length() != 0) {
						processMediaType(len);
						if (mediaType == null) {
							unexpectedTokenError(len, buffer.toString());
							buffer.setLength(0);
						}
						handler.endQuery();
						clearQuery();
					} else if (stage == 0) {
						emptyQuery(len);
					} else {
						unexpectedEOFError(len, "No valid query found");
					}
				} else if (readingPredicate || stage > 1) {
					unexpectedEOFError(len, "Unexpected end of file");
					handler.endQuery();
				} else if (buffer.length() != 0) {
					reportError(len, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected token: " + buffer);
					handler.endQuery();
				} else if (currentCond != null && isEmptyNotCondition()) {
					unexpectedEOFError(len, "No valid query found");
					handler.endQuery();
				} else {
					endQuery(len);
				}
				handler.endQueryList();
			}

		}

	}

	/**
	 * Determine whether this looks like a media feature (rather than a value).
	 * 
	 * @param string the presumed feature name,
	 * @return <code>true</code> if the string looks like a media feature.
	 */
	private static boolean isKnownFeature(String string) {
		return string.startsWith("min-") || string.startsWith("max-")
				|| MediaQueryDatabase.isMediaFeature(string) || string.startsWith("device-");
	}

	private static boolean isValidFeatureSyntax(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!Character.isLetter(c) && c != '-') {
				return false;
			}
		}
		return true;
	}

	@Override
	public SelectorList parseSelectors(Reader reader)
			throws CSSParseException, CSSBudgetException, IOException {
		SelectorManager manager = new SelectorManager();
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(reader, "/*", "*/");
		return manager.getTrimmedSelectorList();
	}

	@Override
	public SelectorList parseSelectors(String selectorText, NamespaceMap nsmap)
			throws CSSParseException {
		SelectorManager manager = new SelectorManager(nsmap);
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(selectorText);
		return manager.getTrimmedSelectorList();
	}

	public SelectorList parseSelectors(InputSource source) throws CSSParseException, IOException {
		Reader re = getReaderFromSource(source);
		SelectorManager manager = new SelectorManager();
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(re, "/*", "*/");
		return manager.getTrimmedSelectorList();
	}

	public SelectorList parseSelectors(String seltext) throws CSSException {
		SelectorManager manager = new SelectorManager();
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(seltext, "/*", "*/");
		return manager.getTrimmedSelectorList();
	}

	private SelectorList parseSelectors(String seltext, NSACSelectorFactory factory)
			throws CSSParseException {
		SelectorManager manager = new SelectorManager(factory);
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(seltext);
		return manager.getTrimmedSelectorList();
	}

	private SelectorList parseSelectorArgument(String seltext, NSACSelectorFactory factory)
			throws CSSParseException {
		SelectorArgumentManager manager = new SelectorArgumentManager(factory);
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(seltext);
		return manager.getTrimmedSelectorList();
	}

	@Override
	public Condition parsePseudoElement(String pseudoElement) throws CSSException {
		SelectorList peList = parseSelectors(pseudoElement);
		Selector sel;
		if (peList.getLength() == 1
				&& (sel = peList.item(0)).getSelectorType() == SelectorType.CONDITIONAL) {
			Condition cond = ((ConditionalSelector) sel).getCondition();
			ConditionType condType = cond.getConditionType();
			if (condType == ConditionType.PSEUDO_ELEMENT) {
				return cond;
			} else if (condType == ConditionType.AND) {
				CombinatorCondition comb = (CombinatorCondition) cond;
				Condition first = comb.getFirstCondition();
				Condition second = comb.getSecondCondition();
				if (first.getConditionType() == ConditionType.PSEUDO_ELEMENT
						&& second.getConditionType() == ConditionType.PSEUDO_ELEMENT) {
					return cond;
				}
			}
		}
		throw new CSSException("No pseudo-element in: " + pseudoElement);
	}

	@Override
	public LexicalUnit parsePropertyValue(Reader reader) throws CSSParseException, IOException {
		DeclarationValueManager manager = new DeclarationValueManager();
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(reader, "/*", "*/");
		return manager.getLexicalUnit();
	}

	private LexicalUnit parsePropertyValue(Reader reader, HandlerManager parent, int index)
			throws CSSParseException, IOException {
		DeclarationValueManager manager = new DeclarationValueManager() {

			@Override
			protected ControlTokenHandler createControlTokenHandler() {
				return new ChildControlTokenHandler(parent.getControlHandler(), index);
			}

		};
		manager.getControlHandler().setCurrentLocation(index);
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(reader, "/*", "*/");
		return manager.getLexicalUnit();
	}

	public LexicalUnit parsePropertyValue(String propertyName, Reader reader)
			throws CSSParseException, IOException {
		DeclarationValueManager manager = new DeclarationValueManager(propertyName);
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(reader, "/*", "*/");
		return manager.getLexicalUnit();
	}

	public LexicalUnit parsePropertyValue(InputSource source)
			throws CSSParseException, IOException {
		Reader re = getReaderFromSource(source);
		DeclarationValueManager manager = new DeclarationValueManager();
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(re, "/*", "*/");
		return manager.getLexicalUnit();
	}

	@Override
	public boolean parsePriority(Reader reader) throws IOException {
		if (reader == null) {
			throw new NullPointerException("Null character stream");
		}
		int cp = reader.read();
		if (cp != -1) {
			short count = 0;
			StringBuilder buf = new StringBuilder(9);
			byte parsingWord = 0;
			if (isNotSeparator(cp)) {
				buf.appendCodePoint(cp);
				parsingWord = 1;
				count = 1;
			}
			while ((cp = reader.read()) != -1 && parsingWord != 2) {
				if (isNotSeparator(cp)) {
					buf.appendCodePoint(cp);
					count++;
					if (count == 10) {
						return false;
					}
					parsingWord = 1;
				} else if (parsingWord == 1) {
					parsingWord = 2;
				}
			}
			return "important".equals(buf.toString().toLowerCase(Locale.ROOT));
		}
		return false;
	}

	static boolean bufferEndsWithEscapedCharOrWS(StringBuilder buffer) {
		int len = buffer.length();
		if (len > 1) {
			int bufCp = buffer.codePointAt(len - 1);
			if (ParseHelper.isHexCodePoint(bufCp) || bufCp == 32) {
				for (int i = 2; i <= Math.min(len, 6); i++) {
					bufCp = buffer.codePointAt(len - i);
					if (ParseHelper.isHexCodePoint(bufCp)) {
						continue;
					} else if (bufCp == 92) { // \
						return true;
					} else {
						break;
					}
				}
			}
		}
		return false;
	}

	static boolean bufferEndsWithEscapedChar(StringBuilder buffer) {
		final int len = buffer.length();
		if (len > 1) {
			int bufCp = buffer.codePointAt(len - 1);
			if (ParseHelper.isHexCodePoint(bufCp)) {
				for (int i = 2; i <= Math.min(len, 6); i++) {
					bufCp = buffer.codePointAt(len - i);
					if (ParseHelper.isHexCodePoint(bufCp)) {
						continue;
					} else if (bufCp == 92) { // \
						return true;
					} else {
						break;
					}
				}
			}
		}
		return false;
	}

	static boolean isDigit(char c) {
		return c >= 0x30 && c <= 0x39;
	}

	private static boolean isNotSeparator(int cp) {
		return cp != 32 && cp != 9 && cp != 10 && cp != 12 && cp != 13;
	}

	/**
	 * Is the given string a valid CSS identifier?
	 * 
	 * @param s the identifier to test; cannot contain hex escapes.
	 * @return true if is a valid identifier.
	 */
	private static boolean isValidIdentifier(String s) {
		int len = s.length();
		int idx;
		char c = s.charAt(0);
		if (c != '-') {
			if (!isNameStartChar(c) && c != '\\') {
				return false;
			}
			idx = 1;
		} else if (len > 1) {
			c = s.charAt(1);
			if (!isNameStartChar(c) && c != '-' && c != '\\') {
				return false;
			}
			idx = 2;
		} else {
			return false;
		}
		while (idx < len) {
			c = s.charAt(idx);
			if (!isNameChar(c)) {
				return false;
			}
			idx++;
		}
		return true;
	}

	private static boolean isValidPseudoName(String s) {
		int len = s.length();
		int idx;
		char c = s.charAt(0);
		if (c != '-') {
			if (!isNameStartChar(c)) {
				return false;
			}
			idx = 1;
		} else if (len > 1) {
			c = s.charAt(1);
			if (!isNameStartChar(c)) {
				return false;
			}
			idx = 2;
		} else {
			return false;
		}
		while (idx < len) {
			c = s.charAt(idx);
			if (!isNameChar(c)) {
				return false;
			}
			idx++;
		}
		return true;
	}

	private static boolean isNameChar(char cp) {
		return (cp >= 0x61 && cp <= 0x7A) // a-z
				|| (cp >= 0x41 && cp <= 0x5A) // A-Z
				|| (cp >= 0x30 && cp <= 0x39) // 0-9
				|| cp == 0x2d // -
				|| cp == 0x5f // _
				|| cp > 0x80 // non-ASCII code point
				|| cp == 0x5c; // '\'
	}

	private static boolean isNameStartChar(char cp) {
		return (cp >= 0x61 && cp <= 0x7A) // a-z
				|| (cp >= 0x41 && cp <= 0x5A) // A-Z
				|| cp == 0x5f // _
				|| cp > 0x80; // non-ASCII code point
	}

	/**
	 * Check if two {@code CharSequence} objects contain the same characters.
	 * 
	 * @param seq1 the first sequence.
	 * @param seq2 the second sequence.
	 * @return {@code true} if contain the same characters.
	 */
	private static boolean equalSequences(CharSequence seq1, CharSequence seq2) {
		int len = seq1.length();
		if (len != seq2.length()) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			if (seq1.charAt(i) != seq2.charAt(i)) {
				return false;
			}
		}

		return true;
	}

	static String safeUnescapeIdentifier(int index, String inputString) {
		return ParseHelper.unescapeStringValue(inputString, true, true);
	}

	/**
	 * Verify that the given identifier does not start in a way which is forbidden
	 * by the specification.
	 * <p>
	 * If the processing reached this, the rest of the identifier should be fine.
	 * </p>
	 * 
	 * @param s the identifier to test.
	 * @return true if it starts as a valid identifier.
	 */
	static boolean isNotForbiddenIdentStart(String s) {
		char c = s.charAt(0);
		if (c != '-') {
			return !isDigit(c) && c != '+';
		}
		return (s.length() > 1 && !isDigit(c = s.charAt(1))) || c == '\\';
	}

	@Override
	public CSSParser clone() {
		CSSParser parser = new CSSParser(this);
		return parser;
	}

	private static class ConditionWrapper {

		private final Object condition;
		private final ConditionWrapper parent;
		private final boolean mediaCondition;

		ConditionWrapper(MediaQueryList mediaList, ConditionWrapper parent) {
			super();
			this.condition = mediaList;
			this.parent = parent;
			mediaCondition = true;
		}

		ConditionWrapper(BooleanCondition cond, ConditionWrapper parent) {
			super();
			this.condition = cond;
			this.parent = parent;
			mediaCondition = false;
		}

		MediaQueryList getMediaList() {
			return (MediaQueryList) condition;
		}

		BooleanCondition getCondition() {
			return (BooleanCondition) condition;
		}

		ConditionWrapper getParent() {
			return parent;
		}

		boolean isMediaCondition() {
			return mediaCondition;
		}

		@Override
		public String toString() {
			return condition != null ? condition.toString() : "";
		}

	}

	private class PageManager extends DescriptorRuleListManager {

		private PageRuleTH pageTH = new PageRuleTH() {

			@Override
			public void character(int index, int codePoint) {
				if (codePoint != TokenProducer.CHAR_COMMERCIAL_AT || this.stage != 0) {
					super.character(index, codePoint);
				}
			}

		};

		private PageManager() {
			super();
		}

		@Override
		protected PageRuleTH getInitialTokenHandler() {
			return pageTH;
		}

		@Override
		protected void reportRuleEnd(int index) {
			if (pageTH.stage > 0) {
				handler.endPage(pageTH.pageSelectorList);
			}
		}

		@Override
		public void endOfStream(int len) {
		}

	}

	private class KeyframesManager extends DescriptorRuleListManager {

		private KeyframesTH ruleTH = new KeyframesTH() {

			@Override
			public void character(int index, int codePoint) {
				if (codePoint != TokenProducer.CHAR_COMMERCIAL_AT || this.keyframesName != null) {
					super.character(index, codePoint);
				}
			}

		};

		KeyframesManager() {
			super();
		}

		@Override
		protected CSSTokenHandler getInitialTokenHandler() {
			return ruleTH;
		}

		@Override
		protected void reportRuleEnd(int index) {
			if (ruleTH.keyframesName != null) {
				handler.endKeyframes();
			}
		}

		@Override
		public void endOfStream(int len) {
		}

	}

	private class FontFeatureValuesManager extends DescriptorRuleListManager {

		private FontFeatureValuesTH ruleTH = new FontFeatureValuesTH() {

			@Override
			public void character(int index, int codePoint) {
				if (codePoint != TokenProducer.CHAR_COMMERCIAL_AT || this.stage != 0) {
					super.character(index, codePoint);
				}
			}

		};

		FontFeatureValuesManager() {
			super();
		}

		@Override
		protected CSSTokenHandler getInitialTokenHandler() {
			return ruleTH;
		}

		@Override
		protected void reportRuleEnd(int index) {
			if (ruleTH.stage == 4) {
				handler.endFontFeatures();
			}
		}

		@Override
		public void endOfStream(int len) {
		}

	}

	/**
	 * Single-rule manager.
	 */
	private class RuleManager extends RuleListManager {

		RuleManager(NamespaceMap nsMap) {
			super(nsMap, false);
		}

		@Override
		public void endManagement(int index) {
			super.endManagement(index);
			if (getCurrentCondition() == null && rulesFound()) {
				getControlHandler().yieldHandling(new RuleEndContentHandler());
			}
		}

		private class RuleEndContentHandler extends ParseEndContentHandler {

			RuleEndContentHandler() {
				super();
			}

			@Override
			public HandlerManager getManager() {
				return RuleManager.this;
			}

		}

	}

	/**
	 * {@code <rule-list>} manager.
	 */
	private class RuleListManager extends DeclarationRuleListManager {

		private final RuleListDeclarationManager declarationManager;
		private final StyleRuleSelectorTH selectorHandler;

		private ConditionWrapper currentCondition = null;

		private final boolean topLevel;

		// Next field is to check for @charset rules in bad place
		boolean rulesFound = false;

		RuleListManager(NamespaceMap nsMap, boolean topLevel) {
			super();
			this.topLevel = topLevel;
			declarationManager = new RuleListDeclarationManager();
			selectorHandler = createSelectorTokenHandler(nsMap);
			selectorHandler.setManager(this);
		}

		RuleListManager(NamespaceMap nsMap, boolean topLevel, DeclarationRuleListManager parent) {
			super(parent);
			this.topLevel = topLevel;
			this.rulesFound = true;
			declarationManager = new RuleListDeclarationManager();
			selectorHandler = createSelectorTokenHandler(nsMap);
			selectorHandler.setManager(this);
		}

		protected StyleRuleSelectorTH createSelectorTokenHandler(NamespaceMap nsMap) {
			return new StyleRuleSelectorTH(nsMap);
		}

		ConditionWrapper getCurrentCondition() {
			return currentCondition;
		}

		@Override
		void setRulesFound() {
			rulesFound = true;
		}

		@Override
		boolean rulesFound() {
			return rulesFound;
		}

		@Override
		public void restoreInitialHandler() {
			super.restoreInitialHandler();
			selectorHandler.resetHandler();
			selectorHandler.resetParseError();
		}

		@Override
		public void endOfStream(int len) {
			// Mark the end of rule
			SelectorListImpl selist = selectorHandler.getSelectorList();
			if (!selist.isEmpty()) {
				handler.endSelector(selist);
				selectorHandler.selist = selectorHandler.new ParserSelectorListImpl();
			}
			selectorHandler.resetHandler();
			while (currentCondition != null) {
				if (currentCondition.isMediaCondition()) {
					handler.endMedia(currentCondition.getMediaList());
				} else {
					handler.endSupports(currentCondition.getCondition());
				}
				currentCondition = currentCondition.getParent();
			}
			super.endOfStream(len);
		}

		@Override
		public void endManagement(int index) {
			// Mark the end of rule
			SelectorListImpl selist = selectorHandler.getSelectorList();
			if (!selist.isEmpty()) {
				handler.endSelector(selist);
				selectorHandler.selist = selectorHandler.new ParserSelectorListImpl();
			} else {
				selectorHandler.resetHandler();
				if (currentCondition != null) {
					if (currentCondition.isMediaCondition()) {
						handler.endMedia(currentCondition.getMediaList());
					} else {
						handler.endSupports(currentCondition.getCondition());
					}
					currentCondition = currentCondition.getParent();
				} else {
					HandlerManager parentMgr = getParentManager();
					if (parentMgr == null) {
						if (!getControlHandler().isInErrorRecovery()) {
							selectorHandler.unexpectedCharError(index,
									TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
						}
						selectorHandler.resetSelectorHandler(true);
					} else {
						yieldManagement(parentMgr);
					}
					return;
				}
			}
			restoreInitialHandler();
		}

		@Override
		public boolean isTopManager() {
			return super.isTopManager() && currentCondition == null;
		}

		@Override
		protected String defaultNamespaceURI() {
			return selectorHandler.factory.getNamespaceURI("");
		}

		@Override
		protected CSSTokenHandler createNamespaceRuleTH() {
			return new NamespaceRuleTH() {

				@Override
				protected void registerNamespacePrefix(String prefix, String uri) {
					selectorHandler.factory.registerNamespacePrefix(prefix, uri);
				}

			};
		}

		@Override
		protected CSSTokenHandler createUnknownRuleHandler(int index, String ruleName) {
			CSSTokenHandler ruleHandler;
			if ("media".equals(ruleName)) {
				ruleHandler = createMediaQueryHandler();
			} else if ("supports".equals(ruleName)) {
				ruleHandler = new MySupportsRuleTH();
			} else {
				ruleHandler = super.createUnknownRuleHandler(index, ruleName);
			}

			return ruleHandler;
		}

		private class StyleRuleSelectorTH extends SelectorTokenHandler {

			StyleRuleSelectorTH(NamespaceMap nsMap) {
				super(nsMap);
			}

			@Override
			public void leftCurlyBracket(int index) {
				processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET, true);
				if (!isInError()) {
					if (addCurrentSelector(index)) {
						selist.trimToSize();
						handler.startSelector(selist);
						yieldManagement(declarationManager);
						setRulesFound();
					} else {
						unexpectedLeftCurlyBracketError(index);
					}
				} else {
					sendLeftCurlyBracketEvent(index, this);
				}
				buffer.setLength(0);
				resetEscapedTokenIndex();
				stage = STAGE_INITIAL;
			}

			@Override
			public void rightCurlyBracket(int index) {
				processBuffer(index, 32, true);
				// Check whether we got an error in selectors
				if (!parseError) {
					if (!selist.isEmpty() || currentsel != null) {
						// Report error
						unexpectedCharError(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
					}
					resetHandler();
					selist.clear();
				}
				RuleListManager.this.endManagement(index);
			}

			private void resetSelectorHandler(boolean resetSheetStage) {
				RuleListManager.this.restoreInitialHandler();
				if (resetSheetStage) {
					RuleListManager.this.resetHandler();
				}
			}

			@Override
			boolean skipCharacterHandling() {
				return parseError && (prevcp != ';' || curlyBracketDepth != 0);
			}

			@Override
			protected void handleAtKeyword(int index) {
				if (buffer.length() == 0) {
					// At-rule
					RuleListManager.this.handleAtKeyword(index);
				} else {
					super.handleAtKeyword(index);
				}
			}

			@Override
			protected void handleSemicolon(int index) {
				// Report error and resume processing
				unexpectedSemicolonError(index);
				resetHandler();
				resetParseError();
			}

			@Override
			boolean isTopLevel() {
				return topLevel;
			}

			@Override
			public HandlerManager getManager() {
				return RuleListManager.this;
			}

			@Override
			public void endOfStream(int len) {
				processBuffer(len, 32, true);
				// Check whether we got an error in selectors
				if (!isInError()) {
					if (!selist.isEmpty() || currentsel != null) {
						// Report EOF
						unexpectedEOFError(len);
						selist.clear();
						// Manager's endOfStream() resets the handler
						//resetHandler();
					} else if (!getManager().isTopManager()) {
						handleWarning(len, ParseHelper.ERR_UNEXPECTED_EOF,
								"Unexpected end of stream");
					}
				}
				RuleListManager.this.endOfStream(len);
			}

			@Override
			public void handleErrorRecovery() {
				// Ignore declaration
				yieldHandling(new IgnoredDeclarationTokenHandler() {

					@Override
					protected void endDeclarationBlock(int index) {
						yieldHandling(RuleListManager.this.selectorHandler);
					}

				});
			}

		}

		private class RuleListDeclarationManager extends DeclarationRuleListManager {

			RuleListDeclarationManager() {
				super(RuleListManager.this);
			}

			@Override
			protected void expectSelector(int index) {
				yieldToNestedRuleHandler();
			}

			@Override
			protected void expectSelector(int index, int triggerCp) {
				CSSTokenHandler selh = yieldToNestedRuleHandler();
				selh.character(index, triggerCp);
			}

			private SelectorTokenHandler yieldToNestedRuleHandler() {
				NestedRuleManager nested = new NestedRuleManager();
				SelectorTokenHandler selh = nested.getInitialTokenHandler();
				getControlHandler().yieldHandling(selh);
				return selh;
			}

			private class NestedRuleManager extends RuleListManager {

				NestedRuleManager() {
					super(RuleListManager.this.selectorHandler.nsMap, RuleListManager.this.topLevel,
							RuleListManager.this.declarationManager);
				}

				@Override
				protected SelectorTokenHandler getInitialTokenHandler() {
					SelectorTokenHandler selh = super.getInitialTokenHandler();
					NSACSelectorFactory factory = selh.factory;
					Condition cond = factory.createCondition(ConditionType.NESTING);
					selh.currentsel = factory
							.createConditionalSelector((SimpleSelector) selh.currentsel, cond);
					return selh;
				}

				@Override
				protected StyleRuleSelectorTH createSelectorTokenHandler(NamespaceMap nsMap) {
					return new StyleRuleSelectorTH(nsMap) {

						@Override
						void unexpectedCharError(int index, int codepoint) {
							if (codepoint == TokenProducer.CHAR_SEMICOLON
									&& getManager().getParentManager() != null) {
								String msg = "Unexpected '"
										+ new String(Character.toChars(codepoint)) + "'";
								CSSParseException ex = createException(index,
										ParseHelper.ERR_UNEXPECTED_CHAR, msg);
								reportError(ex);
								RuleListManager.this.declarationManager.restoreInitialHandler();
								//this.parseError = false;
							} else {
								super.unexpectedCharError(index, codepoint);
							}
						}

						@Override
						public void handleErrorRecovery() {
							// Handle as a declaration error
							yieldHandling(new IgnoredDeclarationTokenHandler() {

								@Override
								protected void resumeDeclarationList() {
									RuleListManager.this.declarationManager.restoreInitialHandler();
								}

								@Override
								protected void endDeclarationBlock(int index) {
									RuleListManager.this.rightCurlyBracket(index);
								}

							});
						}

						@Override
						protected void handleSemicolon(int index) {
							super.handleSemicolon(index);
							if (getControlHandler().getCurrentHandler() == this) {
								RuleListManager.this.declarationManager.restoreInitialHandler();
							}
						}

					};
				}

			}

			@Override
			public void endManagement(int index) {
				RuleListManager.this.endManagement(index);
			}

			@Override
			public void endOfStream(int len) {
				RuleListManager.this.endOfStream(len);
			}

		}

		private class MySupportsRuleTH extends SupportsTokenHandler {

			MySupportsRuleTH() {
				super(null);
			}

			@Override
			void endOfCondition(int index) {
				super.endOfCondition(index);
				if (!isInError()) {
					BooleanCondition cond = getCondition();
					currentCondition = new ConditionWrapper(cond, currentCondition);
					handler.startSupports(cond);
					RuleListManager.this.restoreInitialHandler();
				}
			}

			@Override
			void handleLeftCurlyBracket(int index) {
				endOfCondition(index);
			}

			@Override
			public void endOfStream(int len) {
				unexpectedEOFError(len);
				RuleListManager.this.endOfStream(len);
			}

			@Override
			public HandlerManager getManager() {
				return RuleListManager.this;
			}

		}

		class MyMediaQueryTokenHandler extends RuleMediaQueryTH {

			MyMediaQueryTokenHandler(MediaQueryFactory conditionFactory,
					MediaQueryHandler mqhandler) {
				super(conditionFactory, mqhandler);
			}

			@Override
			protected void startMedia(MediaQueryList mql) {
				currentCondition = new ConditionWrapper(mql, currentCondition);
				handler.startMedia(currentCondition.getMediaList());
				RuleListManager.this.restoreInitialHandler();
			}

			@Override
			public void endOfStream(int len) {
				super.endOfStream(len);
				RuleListManager.this.endOfStream(len);
			}

			@Override
			public RuleListManager getManager() {
				return RuleListManager.this;
			}

		}

		@Override
		protected SelectorTokenHandler getInitialTokenHandler() {
			return selectorHandler;
		}

		private MediaQueryTokenHandler createMediaQueryHandler() {
			MediaQueryFactory mediaQueryFactory = getMediaQueryFactory();
			MediaQueryHandler mqhandler = mediaQueryFactory.createMediaQueryHandler(null);
			return new MyMediaQueryTokenHandler(mediaQueryFactory, mqhandler);
		}

	}

	/**
	 * Parse either at-rules or descriptors.
	 */
	abstract class DescriptorRuleListManager extends DeclarationRuleListManager {

		DescriptorRuleListManager() {
			super();
		}

		DescriptorRuleListManager(HandlerManager parent) {
			super(parent);
		}

		@Override
		protected ValueTokenHandler createValueTokenHandler() {
			return new DeclValueTokenHandler() {

				@Override
				protected void setPriorityHandler(int index) {
					getControlHandler().getCurrentHandler().handleError(index,
							ParseHelper.ERR_RULE_SYNTAX,
							"Important priorities are invalid in descriptors.");
				}

			};
		}

		@Override
		public void endManagement(int index) {
			reportRuleEnd(index);
			super.endManagement(index);
		}

		@Override
		public void endOfStream(int len) {
			reportRuleEnd(len);
			super.endOfStream(len);
		}

		abstract protected void reportRuleEnd(int index);

	}

	/**
	 * {@code <declaration-rule-list>} manager.
	 */
	private class DeclarationRuleListManager extends DeclarationListManager {

		boolean foundControl = false;

		private DeclarationRuleListManager() {
			super();
		}

		DeclarationRuleListManager(HandlerManager parent) {
			super(parent);
		}

		void setRulesFound() {
		}

		boolean rulesFound() {
			return false;
		}

		@Override
		protected ControlTokenHandler createControlTokenHandler() {
			return new CSSControlTokenHandler() {

				@Override
				public void control(int index, int codepoint) {
					super.control(index, codepoint);
					foundControl = true;
				}

			};
		}

		@Override
		protected CSSTokenHandler getInitialTokenHandler() {
			propertyName = null;
			return super.getInitialTokenHandler();
		}

		protected String defaultNamespaceURI() {
			return null;
		}

		@Override
		protected void handleAtKeyword(int index) {
			if (propertyName == null) {
				getControlHandler().yieldHandling(new AtRuleLauncher());
			} else {
				getControlHandler().getCurrentHandler().unexpectedCharError(index, 64);
			}
		}

		class AtRuleLauncher extends IdentTokenHandler {

			AtRuleLauncher() {
				super();
			}

			@Override
			public void commented(int index, int commentType, String comment) {
				if (buffer.length() > 0) {
					processBuffer(index, 12);
					AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
					if (curh != this) {
						curh.commented(index, commentType, comment);
					}
				} else {
					// Comment right after @
					unexpectedTokenError(index, comment);
				}
			}

			@Override
			public void separator(int index, int codepoint) {
				if (getEscapedTokenIndex() != -1 && bufferEndsWithEscapedChar(buffer)) {
					buffer.append(' ');
				} else {
					if (buffer.length() > 0) {
						processBuffer(index, codepoint);
						AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
						if (curh != this) {
							curh.separator(index, codepoint);
						}
						setWhitespacePrevCp();
					} else {
						// Whitespace right after @
						unexpectedCharError(index, codepoint);
					}
				}
			}

			@Override
			public void character(int index, int codePoint) throws RuntimeException {
				if (codePoint == TokenProducer.CHAR_SEMICOLON) {
					if (unexpectedSemicolonError(index)) {
						getManager().restoreInitialHandler();
					}
				} else if (buffer.length() > 0) {
					processBuffer(index, codePoint);
					AbstractTokenHandler curh = getControlHandler().getCurrentHandler();
					if (curh != this) {
						curh.character(index, codePoint);
					}
				} else {
					unexpectedCharError(index, codePoint);
				}
			}

			@Override
			public void leftCurlyBracket(int index) {
				processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
				AbstractTokenHandler curhnd = getControlHandler().getCurrentHandler();
				if (curhnd != this) {
					curhnd.leftCurlyBracket(index);
				}
			}

			@Override
			public void leftParenthesis(int index) {
				processBuffer(index, TokenProducer.CHAR_LEFT_PAREN);
				AbstractTokenHandler curhnd = getControlHandler().getCurrentHandler();
				if (curhnd != this) {
					curhnd.leftParenthesis(index);
				}
			}

			/**
			 * Please only call this if buffer is not empty.
			 */
			@Override
			void processBuffer(int index, int triggerCp) {
				String atRule = unescapeBuffer(index);
				if (atRule.length() > 2) {
					handleAtRule(index, atRule);
				} else {
					handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Malformed @-rule.");
				}
			}

			@Override
			public void endOfStream(int len) {
				unexpectedEOFError(len);
				DeclarationRuleListManager.this.endOfStream(len);
			}

		}

		private void handleAtRule(int index, String ruleName) {
			CSSTokenHandler rulehandler = createRuleHandler(index, ruleName);
			if (rulehandler != null) {
				getControlHandler().yieldHandling(rulehandler);
				setRulesFound();
			} // If rule handler is null, it was an out-of-place @charset rule
		}

		protected CSSTokenHandler createRuleHandler(int index, String ruleName) {
			CSSTokenHandler ruleHandler;
			if ("charset".equals(ruleName)) {
				if (!rulesFound()) {
					ruleHandler = new CharsetRuleTH();
				} else {
					getControlHandler().getCurrentHandler().handleError(index - 8,
							ParseHelper.ERR_RULE_SYNTAX, "@charset must be the first rule");
					return null;
				}
			} else if ("import".equals(ruleName)) {
				ruleHandler = new ImportRuleTH();
			} else if ("namespace".equals(ruleName)) {
				ruleHandler = createNamespaceRuleTH();
			} else if ("font-face".equals(ruleName)) {
				ruleHandler = new FontFaceTH();
			} else if ("page".equals(ruleName)) {
				ruleHandler = new PageRuleTH();
			} else if ("counter-style".equals(ruleName)) {
				ruleHandler = new CounterStyleTH();
			} else if ("keyframes".equals(ruleName)) {
				ruleHandler = new KeyframesTH();
			} else if ("font-feature-values".equals(ruleName)) {
				ruleHandler = new FontFeatureValuesTH();
			} else if ("property".equals(ruleName)) {
				ruleHandler = new PropertyTH();
			} else {
				ruleHandler = createUnknownRuleHandler(index, ruleName);
			}

			return ruleHandler;
		}

		protected CSSTokenHandler createNamespaceRuleTH() {
			return new NamespaceRuleTH();
		}

		protected CSSTokenHandler createUnknownRuleHandler(int index, String ruleName) {
			return new UnknownRuleTokenHandler(ruleName);
		}

		/**
		 * Abstract rule handler.
		 */
		abstract class AbstractRuleHandler extends DefaultTokenHandler {

			AbstractRuleHandler() {
				super();
			}

			@Override
			public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
				quoted(index, quoted, quoteCp);
			}

			@Override
			public void commented(int index, int commentType, String comment) {
				separator(index, 12);
				prevcp = 12;
			}

			/**
			 * Utility method to trim the buffer tail.
			 */
			void trimBufferTail() {
				int lenm1 = buffer.length() - 1;
				if (buffer.charAt(lenm1) == ' ') {
					buffer.setLength(lenm1);
				}
			}

			@Override
			public HandlerManager getManager() {
				return DeclarationRuleListManager.this;
			}

			@Override
			public void handleErrorRecovery() {
				// Error: ignore declaration
				yieldHandling(new IgnoredDeclarationTokenHandler() {

					@Override
					protected void endDeclarationBlock(int index) {
						reportRuleEnd(index);
						super.endDeclarationBlock(index);
					}

				});
			}

			void endRuleBody(int index) {
				reportRuleEnd(index);
				endRule();
			}

			/**
			 * Report the rule (or the rule end if a start was reported) to the CSS handler
			 * and reset this handler.
			 * 
			 * @param index the index at which the rule end is reported.
			 */
			abstract protected void reportRuleEnd(int index);

			protected void endRule() {
				restoreInitialHandler();
			}

			@Override
			public void endOfStream(int len) {
				getManager().endOfStream(len);
			}

		}

		/**
		 * Charset at-rule handler.
		 */
		class CharsetRuleTH extends AbstractRuleHandler {

			private String charset = null;

			CharsetRuleTH() {
				super();
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quote) {
				if (charset == null) {
					charset = quoted.toString();
				} else {
					unexpectedTokenError(index, quoted);
				}
			}

			@Override
			public void character(int index, int codepoint) {
				prevcp = codepoint;
				if (codepoint == 59) { // ;
					endRuleBody(index);
				} else {
					unexpectedCharError(index, codepoint);
				}
			}

			@Override
			public void commented(int index, int commentType, String comment) {
			}

			@Override
			public void word(int index, CharSequence word) {
				unexpectedTokenError(index, word);
			}

			@Override
			public void escaped(int index, int codePoint) {
				unexpectedCharError(index, '\\');
			}

			@Override
			public void separator(int index, int codepoint) {
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (charset != null) {
					handler.charset(charset);
				}
			}

			@Override
			void processBuffer(int index, int triggerCp) {
			}

			@Override
			public void handleErrorRecovery() {
				// Error: ignore rule
				yieldHandling(new IgnoredDeclarationTokenHandler());
			}

		}

		/**
		 * Statement at-rule handler.
		 */
		abstract class StatementAtRuleHandler extends AbstractRuleHandler {

			StatementAtRuleHandler() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder(100);
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				buffer.setLength(0);
			}

		}

		abstract class RuleMediaQueryTH extends MediaQueryTokenHandler {

			RuleMediaQueryTH(MediaQueryFactory conditionFactory, MediaQueryHandler mqhandler) {
				super(conditionFactory, mqhandler);
				mqhandler.startQuery();
			}

			@Override
			void endOfCondition(int index) {
				super.endOfCondition(index);
				MediaQueryList mql = getPredicateHandler().getMediaQueryHandler()
						.getMediaQueryList();
				startMedia(mql);
			}

			abstract protected void startMedia(MediaQueryList mql);

			@Override
			void handleLeftCurlyBracket(int index) {
				endOfCondition(index);
			}

			@Override
			public DeclarationRuleListManager getManager() {
				return DeclarationRuleListManager.this;
			}

		}

		/**
		 * {@code @import} rule handler.
		 */
		class ImportRuleTH extends StatementAtRuleHandler {

			private String importURL = null;

			private String layerName = null;

			private MediaQueryList mediaQuery = null;

			private BooleanCondition supportsCondition = null;

			ImportRuleTH() {
				super();
			}

			@Override
			public void separator(int index, int codepoint) {
				if (getEscapedTokenIndex() != -1 && bufferEndsWithEscapedChar(buffer)) {
					buffer.append(' ');
				} else {
					processBuffer(index, 32);
					setWhitespacePrevCp();
				}
			}

			@Override
			public void character(int index, int codepoint) {
				if (codepoint == 59) { // ;
					handleSemicolon(index);
					prevcp = codepoint;
				} else if (codepoint == TokenProducer.CHAR_COMMA) { // ,
					processBuffer(index, codepoint);
				} else {
					unexpectedCharError(index, codepoint);
				}
			}

			private void handleSemicolon(int index) {
				// End of rule
				if (parendepth == 0) {
					processBuffer(index, TokenProducer.CHAR_SEMICOLON);
					if (!isInError() && importURL != null) {
						if (mediaQuery == null) {
							// MQ handler did not end the rule body
							endRuleBody(index);
						}
					} else {
						if (unexpectedSemicolonError(index)) {
							restoreInitialHandler();
						}
					}
				} else {
					handleError(index, ParseHelper.ERR_UNMATCHED_PARENTHESIS,
							"Unmatched parentheses in rule.");
				}
				resetHandler();
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				if (importURL == null && buffer.length() == 0) {
					importURL = quoted.toString();
					prevcp = 65;
				} else {
					unexpectedTokenError(index, quoted);
				}
			}

			@Override
			public void leftParenthesis(int index) {
				parendepth++;
				if (prevcp == 65) {
					prevcp = TokenProducer.CHAR_LEFT_PAREN;
					// Trim possible trailing space
					// prevcp 65 implies non-empty buffer
					trimBufferTail();
					if (ParseHelper.equalsIgnoreCase(buffer, "url")) {
						buffer.setLength(0);
						if (importURL == null) {
							yieldHandling(new URLTokenHandler(ImportRuleTH.this) {

								@Override
								protected void setURL(String url, LexicalUnitImpl urlUnit) {
									if (url != null) {
										importURL = url;
									} else {
										ImportRuleTH.this.unexpectedCharError(index,
												TokenProducer.CHAR_RIGHT_PAREN);
									}
								}

							});
							return;
						}
					} else if (ParseHelper.equalsIgnoreCase(buffer, "supports")) {
						buffer.setLength(0);
						if (supportsCondition == null) {
							SupportsTokenHandler th = new SupportsTokenHandler(null) {

								@Override
								public void rightParenthesis(int index) {
									super.rightParenthesis(index);
									if (getCurrentParenDepth() == 0 && isTopLevel()) {
										endOfCondition(index);
									}
								}

								@Override
								void endOfCondition(int index) {
									super.endOfCondition(index);
									if (!isInError()) {
										supportsCondition = getCondition();
										yieldHandling(ImportRuleTH.this);
									}
								}

								@Override
								public void endOfStream(int len) {
									unexpectedEOFError(len);
									ImportRuleTH.this.endOfStream(len);
								}

								@Override
								public HandlerManager getManager() {
									return ImportRuleTH.this.getManager();
								}

							};
							th.leftParenthesis(index);
							yieldHandling(th);
							return;
						}
					} else if (ParseHelper.equalsIgnoreCase(buffer, "layer")) {
						// layer()
						buffer.setLength(0);
						if (layerName == null) {
							IdentTokenHandler th = new IdentTokenHandler() {

								@Override
								public void word(int index, CharSequence word) {
									if (layerName == null) {
										super.word(index, word);
									} else {
										unexpectedTokenError(index, word);
									}
								}

								@Override
								public void character(int index, int codePoint)
										throws RuntimeException {
									if (codePoint == TokenProducer.CHAR_FULL_STOP
											&& layerName == null) {
										buffer.append('.');
									} else {
										super.character(index, codePoint);
									}
								}

								@Override
								public void rightParenthesis(int index) {
									processBuffer(index, TokenProducer.CHAR_RIGHT_PAREN);
									if (layerName != null) {
										ImportRuleTH.this.parendepth--;
										yieldHandling(ImportRuleTH.this);
									} else {
										unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
									}
								}

								@Override
								void processBuffer(int index, int triggerCp) {
									if (buffer.length() != 0) {
										String s = unescapeBuffer(index);
										if (s.indexOf('.') != -1) {
											StringTokenizer st = new StringTokenizer(s, ".");
											while (st.hasMoreElements()) {
												if (!checkValidCustomIdent(index, st.nextToken())) {
													return;
												}
											}
										} else if (!checkValidCustomIdent(index, s)) {
											return;
										}
										layerName = s;
									}
								}

								@Override
								public void endOfStream(int len) {
									processBuffer(len, 0);
									if (!isInError()) {
										unexpectedEOFError(len);
									}
									ImportRuleTH.this.endOfStream(len);
								}

								@Override
								public void handleErrorRecovery() {
									ImportRuleTH.this.handleErrorRecovery();
								}

								@Override
								public HandlerManager getManager() {
									return ImportRuleTH.this.getManager();
								}

							};
							th.parendepth++;
							yieldHandling(th);
							return;
						}
					}
				} else if (importURL != null) {
					// Media query starts
					prevcp = TokenProducer.CHAR_LEFT_PAREN;
					MediaQueryTokenHandler th = createMediaQueryHandler();
					int len = buffer.length();
					if (len != 0) {
						th.word(index - len, buffer);
						buffer.setLength(0);
						resetEscapedTokenIndex();
					}
					th.leftParenthesis(index);
					yieldHandling(th);
					return;
				}
				unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				int len = buffer.length();
				if (len != 0) {
					// Trim possible trailing space
					trimBufferTail();
					String s = unescapeBuffer(index);
					if ("layer".equalsIgnoreCase(s)) {
						if (layerName == null) {
							layerName = "";
							buffer.setLength(0);
							resetEscapedTokenIndex();
							return;
						}
					} else if (importURL != null) {
						// Media query
						MediaQueryTokenHandler th = createMediaQueryHandler();
						th.word(index, s);
						if (triggerCp != 32) {
							th.character(index, triggerCp);
						} else {
							th.separator(index, 32);
						}
						if (triggerCp != TokenProducer.CHAR_SEMICOLON) {
							yieldHandling(th);
						}
						return;
					}
					handleError(index - len, ParseHelper.ERR_RULE_SYNTAX,
							"Unexpected token: '" + s + '\'');
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				processBuffer(index, 32);
				if (!isInError()) {
					if (importURL != null) {
						if (mediaQuery == null) {
							mediaQuery = getMediaQueryFactory().createAllMedia();
						} else if (mediaQuery.hasErrors()) {
							if (mediaQuery.isNotAllMedia()) {
								reportError(index, ParseHelper.ERR_RULE_SYNTAX,
										"Invalid media query.");
								resetHandler();
								return;
							} else {
								handleWarning(index, ParseHelper.ERR_RULE_SYNTAX,
										"Media query has errors.");
							}
						}
						String defaultNSURI = DeclarationRuleListManager.this.defaultNamespaceURI();
						handler.importStyle(importURL, layerName, supportsCondition, mediaQuery,
								defaultNSURI);
					} else {
						reportError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Malformed @-rule.");
					}
				}
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				importURL = null;
				layerName = null;
				mediaQuery = null;
				supportsCondition = null;
			}

			@Override
			public void endOfStream(int len) {
				reportRuleEnd(len);
				if (importURL == null && !isInError()) {
					handleWarning(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of stream");
				}
				super.endOfStream(len);
			}

			private MediaQueryTokenHandler createMediaQueryHandler() {
				MediaQueryFactory mediaQueryFactory = getMediaQueryFactory();
				MediaQueryHandler mqhandler = mediaQueryFactory.createMediaQueryHandler(null);
				return new ImportMediaQueryTokenHandler(mediaQueryFactory, mqhandler);
			}

			class ImportMediaQueryTokenHandler extends RuleMediaQueryTH {

				ImportMediaQueryTokenHandler(MediaQueryFactory conditionFactory,
						MediaQueryHandler mqhandler) {
					super(conditionFactory, mqhandler);
				}

				@Override
				protected void startMedia(MediaQueryList mql) {
					mediaQuery = mql;
					getManager().restoreInitialHandler();
				}

				@Override
				protected void handleSemicolon(int index) {
					mediaQuery = getPredicateHandler().getMediaQueryHandler().getMediaQueryList();
					endRuleBody(index);
				}

				@Override
				public void endOfStream(int len) {
					super.endOfStream(len);
					ImportRuleTH.this.endOfStream(len);
				}

			}

		}

		/**
		 * Generic namespace rule handler.
		 */
		class NamespaceRuleTH extends StatementAtRuleHandler {

			private String nsPrefix = null;
			private String namespaceURI = null;

			NamespaceRuleTH() {
				super();
			}

			@Override
			public void word(int index, CharSequence word) {
				if (namespaceURI == null) {
					super.word(index, word);
				} else {
					unexpectedTokenError(index, word);
				}
			}

			/**
			 * Process the buffer.
			 * <p>
			 * Please call this only if the buffer is not empty.
			 * </p>
			 */
			@Override
			void processBuffer(int index, int triggerCp) {
				// Trim possible trailing space
				trimBufferTail();
				if (nsPrefix == null) {
					nsPrefix = unescapeBuffer(index);
				}
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				prevcp = 65;
				if (namespaceURI == null) {
					if (quoted.length() > 0) {
						namespaceURI = quoted.toString();
						if (nsPrefix == null) {
							nsPrefix = "";
						}
						return;
					} else if (nsPrefix == null) {
						// Legacy sheets could have a '' for the default NS prefix
						nsPrefix = "";
						return;
					}
				}
				unexpectedTokenError(index, quoted);
			}

			@Override
			public void character(int index, int codepoint) {
				prevcp = codepoint;
				if (codepoint == TokenProducer.CHAR_SEMICOLON) { // ;
					if (buffer.length() != 0) {
						processBuffer(index, codepoint);
					}
					handleSemicolon(index);
				} else {
					unexpectedCharError(index, codepoint);
				}
			}

			private void handleSemicolon(int index) {
				// End of rule
				if (namespaceURI != null) {
					endRuleBody(index);
				} else {
					reportError(index, ParseHelper.ERR_RULE_SYNTAX, "Incomplete @-rule.");
					getManager().restoreInitialHandler();
				}
			}

			@Override
			public void leftParenthesis(int index) {
				parendepth++;
				if (namespaceURI == null && prevcp == 65 && bufferEqualsAndClear("url")) {
					if (nsPrefix == null) {
						nsPrefix = "";
					}
					yieldHandling(new URLTokenHandler(NamespaceRuleTH.this) {

						@Override
						protected void setURL(String url, LexicalUnitImpl urlUnit) {
							if (url != null) {
								namespaceURI = url;
							} else {
								NamespaceRuleTH.this.unexpectedCharError(index,
										TokenProducer.CHAR_RIGHT_PAREN);
							}
						}

					});
				} else {
					unexpectedCharError(index, '(');
				}

				prevcp = TokenProducer.CHAR_LEFT_PAREN;
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (!isInError()) {
					if (buffer.length() != 0) {
						processBuffer(index, 32);
						if (isInError()) {
							resetHandler();
							return;
						}
					}
					if (nsPrefix == null) {
						reportError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Malformed @-rule.");
						return;
					}
					if (namespaceURI != null) {
						namespaceDeclaration(nsPrefix, namespaceURI);
					} else {
						reportError(index, ParseHelper.ERR_RULE_SYNTAX, "No URI in namespace rule");
						return;
					}
				}
				resetHandler();
			}

			void namespaceDeclaration(String prefix, String uri) {
				handler.namespaceDeclaration(prefix, uri);
				registerNamespacePrefix(prefix, uri);
			}

			protected void registerNamespacePrefix(String prefix, String uri) {
			}

			@Override
			public void endOfStream(int len) {
				reportRuleEnd(len);
				if (parendepth == 0) {
					if (!isInError()) {
						handleWarning(len, ParseHelper.ERR_UNEXPECTED_EOF,
								"Unexpected end of stream");
					}
				} else if (!isInError()) {
					unexpectedEOFError(len);
				}
				super.endOfStream(len);
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				nsPrefix = null;
				namespaceURI = null;
			}

		}

		/**
		 * Unknown rule handler.
		 */
		class UnknownRuleTokenHandler extends AbstractRuleHandler {

			private int curlyBracketDepth = 0;

			private int sqBracketDepth = 0;

			UnknownRuleTokenHandler(String ruleName) {
				super();
				if (ruleName == null || ruleName.isEmpty()) {
					throw new IllegalArgumentException();
				}
				buffer = new StringBuilder(300);
				buffer.append('@').append(ruleName);
				prevcp = 64;
			}

			@Override
			public void separator(int index, int codepoint) {
				if (!isPrevCpWhitespace()
						|| (isEscapedIdent() && bufferEndsWithEscapedCharOrWS(buffer))) {
					buffer.append(' ');
				}
				setWhitespacePrevCp();
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				char c = (char) quoteCp;
				buffer.append(c).append(quoted).append(c);
				prevcp = 64;
			}

			@Override
			public void leftCurlyBracket(int index) {
				buffer.append('{');
				curlyBracketDepth++;
				prevcp = TokenProducer.CHAR_LEFT_CURLY_BRACKET;
			}

			@Override
			public void leftParenthesis(int index) {
				buffer.append('(');
				parendepth++;
				prevcp = TokenProducer.CHAR_LEFT_PAREN;
			}

			@Override
			public void leftSquareBracket(int index) {
				buffer.append('[');
				sqBracketDepth++;
				prevcp = TokenProducer.CHAR_LEFT_SQ_BRACKET;
			}

			@Override
			public void rightParenthesis(int index) {
				buffer.append(')');
				parendepth--;
				prevcp = TokenProducer.CHAR_RIGHT_PAREN;
			}

			@Override
			public void rightSquareBracket(int index) {
				buffer.append(']');
				sqBracketDepth--;
				prevcp = TokenProducer.CHAR_RIGHT_SQ_BRACKET;
			}

			@Override
			public void rightCurlyBracket(int index) {
				buffer.append('}');
				curlyBracketDepth--;
				if (syntaxCheck()) {
					// Body of rule ends
					endRuleBody(index);
				}
				prevcp = TokenProducer.CHAR_RIGHT_CURLY_BRACKET;
			}

			@Override
			public void character(int index, int codepoint) {
				bufferAppend(codepoint);
				prevcp = codepoint;
				if (codepoint == 59) { // ;
					if (syntaxCheck()) {
						// End of statement at-rule
						endRuleBody(index);
					}
				}
			}

			private boolean syntaxCheck() {
				return curlyBracketDepth == 0 && parendepth == 0 && sqBracketDepth == 0;
			}

			@Override
			public void commented(int index, int commentType, String comment) {
				// Unknown rule
				if (commentType == 0) {
					buffer.append("/*").append(comment).append("*/");
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				trimBufferTail();
				handler.ignorableAtRule(buffer.toString());
				resetHandler();
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				/*
				 * Can only be called from endOfStream()
				 */
				reportRuleEnd(index);
			}

		}

		/**
		 * Block rule handler.
		 */
		abstract class AbstractBlockRuleHandler extends AbstractRuleHandler {

			AbstractBlockRuleHandler() {
				super();
			}

			void expectRuleBody(int index) {
				yieldManagement(new DescriptorListManager(getManager()) {

					@Override
					protected void reportRuleEnd(int index) {
						AbstractBlockRuleHandler.this.reportRuleEnd(index);
					}

				});
			}

			@Override
			public void endOfStream(int len) {
				unexpectedEOFError(len);
				super.endOfStream(len);
			}

		}

		/**
		 * Handles a counter-style at-rule.
		 */
		class CounterStyleTH extends AbstractBlockRuleHandler {

			private String counterStyleName = null;

			CounterStyleTH() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder(64);
			}

			@Override
			public void word(int index, CharSequence word) {
				if (counterStyleName == null) {
					super.word(index, word);
				} else {
					unexpectedTokenError(index, word);
				}
			}

			@Override
			public void escaped(int index, int codePoint) {
				if (counterStyleName == null) {
					super.escaped(index, codePoint);
				} else {
					unexpectedCharError(index, codePoint);
				}
			}

			@Override
			public void character(int index, int codePoint) {
				unexpectedCharError(index, codePoint);
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				int len = buffer.length();
				if (len != 0) {
					String name = unescapeBuffer(index);
					if (checkValidCustomIdent(index, name)) {
						counterStyleName = name;
					}
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (counterStyleName != null) {
					handler.endCounterStyle();
				}
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				this.counterStyleName = null;
			}

			@Override
			public void leftCurlyBracket(int index) {
				if (counterStyleName == null) {
					// The next call does not produce errors
					processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
					if (counterStyleName == null) {
						unexpectedLeftCurlyBracketError(index);
						return;
					}
				}

				handler.startCounterStyle(counterStyleName);

				expectRuleBody(index);
			}

			@Override
			public void reportError(CSSParseException ex) throws CSSParseException {
				super.reportError(ex);
				counterStyleName = null;
			}

		}

		/**
		 * Handles a page at-rule.
		 */
		class PageRuleTH extends AbstractBlockRuleHandler {

			private PageSelectorListImpl pageSelectorList = new PageSelectorListImpl();

			//
			// stage
			//  0 initial / comma found
			//  1 ident found
			//  2 ':' found
			//  1 word appended to ':'
			//  3 whitespace after ident/pseudo-page
			//
			private static final int STAGE_TOKEN_PROCESSED = 1;
			private static final int STAGE_EXPECT_PSEUDOPAGE_NAME = 2;
			private static final int STAGE_EXPECT_RULE_BODY = 3;
			private static final int STAGE_RULE_BODY = 4;

			short stage = 0;

			PageRuleTH() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder(64);
			}

			@Override
			public void word(int index, CharSequence word) {
				switch (stage) {
				case 0:
				case STAGE_EXPECT_PSEUDOPAGE_NAME:
					buffer.append(word);
					stage = STAGE_TOKEN_PROCESSED;
					break;
				default:
					unexpectedTokenError(index, word);
				}
			}

			@Override
			public void separator(int index, int codepoint) {
				switch (stage) {
				case 0:
				case STAGE_EXPECT_RULE_BODY:
					break;
				case STAGE_TOKEN_PROCESSED:
					if (getEscapedTokenIndex() != -1 && bufferEndsWithEscapedChar(buffer)) {
						buffer.append(' ');
					} else {
						stage = STAGE_EXPECT_RULE_BODY;
					}
					break;
				default:
					unexpectedCharError(index, codepoint);
				}
			}

			@Override
			public void character(int index, int codePoint) {
				switch (codePoint) {
				case TokenProducer.CHAR_COLON:
					if (stage <= STAGE_TOKEN_PROCESSED) {
						buffer.append(':');
						stage = STAGE_EXPECT_PSEUDOPAGE_NAME;
						return;
					}
					break;
				case TokenProducer.CHAR_COMMA:
					if (stage == STAGE_TOKEN_PROCESSED) {
						processBuffer(index, codePoint);
						stage = 0;
						return;
					} else if (stage == STAGE_EXPECT_RULE_BODY) {
						stage = 0;
						return;
					}
					break;
				default:
				}
				unexpectedCharError(index, codePoint);
			}

			@Override
			public void leftCurlyBracket(int index) {
				if (stage == STAGE_EXPECT_PSEUDOPAGE_NAME) {
					unexpectedLeftCurlyBracketError(index);
					return;
				}

				processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);

				if (isInError()) {
					sendLeftCurlyBracketEvent(index, this);
					return;
				}

				PageSelectorList psList;
				if (pageSelectorList.isEmpty()) {
					psList = null;
				} else {
					psList = pageSelectorList;
				}

				handler.startPage(psList);

				stage = STAGE_RULE_BODY;

				/*
				 * Now we expect margin rules or the allowed descriptors.
				 */
				expectRuleBody(index);
			}

			@Override
			void expectRuleBody(int index) {
				yieldManagement(new MarginRuleListManager(getManager()));
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				int len = buffer.length();
				if (len != 0) {
					String selector = unescapeBuffer(index);
					AbstractPageSelector sel = parsePageSelector(selector);
					if (sel != null) {
						pageSelectorList.add(sel);
					} else {
						unexpectedTokenError(index - len, selector);
					}
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (stage == STAGE_RULE_BODY) {
					handler.endPage(pageSelectorList);
				}
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				pageSelectorList.clear();
				stage = 0;
			}

			private class MarginRuleListManager extends DescriptorRuleListManager {

				MarginRuleListManager(HandlerManager parent) {
					super(parent);
				}

				@Override
				protected CSSTokenHandler createUnknownRuleHandler(int index, String ruleName) {
					return new MarginRuleTH(ruleName);
				}

				@Override
				protected void reportRuleEnd(int index) {
					PageRuleTH.this.reportRuleEnd(index);
				}

				/**
				 * Margin rule handler.
				 */
				class MarginRuleTH extends NoSelectorRuleHandler {

					private final String ruleName;

					MarginRuleTH(String ruleName) {
						super();
						this.ruleName = ruleName;
					}

					@Override
					public void leftCurlyBracket(int index) {
						handler.startMargin(ruleName);
						super.leftCurlyBracket(index);
					}

					@Override
					protected void reportRuleEnd(int index) {
						handler.endMargin();
					}

					@Override
					public HandlerManager getManager() {
						return MarginRuleListManager.this;
					}

				}

			}

		}

		/**
		 * Handles a an at-rule that has no selector nor preamble.
		 */
		abstract class NoSelectorRuleHandler extends AbstractBlockRuleHandler {

			protected NoSelectorRuleHandler() {
				super();
			}

			@Override
			public void word(int index, CharSequence word) {
				unexpectedTokenError(index, word);
			}

			@Override
			public void escaped(int index, int codepoint) {
				unexpectedCharError(index - 1, '\\');
			}

			@Override
			public void separator(int index, int codepoint) {
			}

			@Override
			void processBuffer(int index, int triggerCp) {
			}

			@Override
			public void character(int index, int codePoint) {
				if (codePoint == TokenProducer.CHAR_SEMICOLON) {
					if (unexpectedSemicolonError(index)) {
						getManager().restoreInitialHandler();
					}
					resetParseError();
				} else {
					unexpectedCharError(index, codePoint);
				}
			}

			@Override
			public void leftCurlyBracket(int index) {
				expectRuleBody(index);
			}

			@Override
			public void handleErrorRecovery() {
				// Avoid calling reportRuleEnd() when recovering
				yieldHandling(new IgnoredDeclarationTokenHandler() {

					@Override
					protected void endDeclarationBlock(int index) {
						NoSelectorRuleHandler.this.endRule();
						super.endDeclarationBlock(index);
					}

				});
			}

		}

		/**
		 * Handles a font-face at-rule.
		 */
		class FontFaceTH extends NoSelectorRuleHandler {

			FontFaceTH() {
				super();
			}

			@Override
			protected void reportRuleEnd(int index) {
				handler.endFontFace();
			}

			@Override
			public void leftCurlyBracket(int index) {
				handler.startFontFace();
				expectRuleBody(index);
			}

		}

		/**
		 * Handles a font-feature-values at-rule, where the preamble is a
		 * comma-separated string list.
		 */
		class FontFeatureValuesTH extends AbstractBlockRuleHandler {

			private LinkedList<String> familyList = new LinkedList<>();

			//
			// stage
			//  0 initial
			//  1 ident found
			//  2 string found
			//  3 comma found
			//  4 processing body
			//
			private static final short STAGE_IDENT_FOUND = 1;
			private static final short STAGE_STRING_FOUND = 2;
			private static final short STAGE_COMMA_FOUND = 3;
			private static final short STAGE_PROCESSING_BODY = 4;

			short stage = 0;

			FontFeatureValuesTH() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder(72);
			}

			@Override
			public void word(int index, CharSequence word) {
				if (stage == 1) {
					buffer.append(' ');
				}
				super.word(index, word);
				stage = STAGE_IDENT_FOUND;
			}

			@Override
			public void separator(int index, int codepoint) {
				if (getEscapedTokenIndex() != -1 && bufferEndsWithEscapedChar(buffer)) {
					buffer.append(' ');
				}
				setWhitespacePrevCp();
			}

			@Override
			public void character(int index, int codePoint) {
				switch (codePoint) {
				case TokenProducer.CHAR_COMMA:
					if (stage == STAGE_IDENT_FOUND) {
						processBuffer(index, codePoint);
						stage = STAGE_COMMA_FOUND;
						break;
					} else if (stage == STAGE_STRING_FOUND) {
						stage = STAGE_COMMA_FOUND;
						break;
					}
				default:
					unexpectedCharError(index, codePoint);
					break;
				case TokenProducer.CHAR_SEMICOLON:
					unexpectedSemicolonError(index);
				}
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quote) {
				if (stage != STAGE_IDENT_FOUND) {
					familyList.add(quoted.toString());
					stage = STAGE_STRING_FOUND;
				} else {
					unexpectedTokenError(index, quoted);
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (stage == STAGE_PROCESSING_BODY) {
					handler.endFontFeatures();
				}
			}

			@Override
			public void leftCurlyBracket(int index) {
				// The next call does not produce any error
				processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);

				if (familyList.isEmpty()) {
					unexpectedLeftCurlyBracketError(index);
					return;
				}

				String[] ff = new String[familyList.size()];
				ff = familyList.toArray(ff);
				handler.startFontFeatures(ff);

				stage = STAGE_PROCESSING_BODY;

				/*
				 * Now we expect font-feature-value-type rules or the font-display descriptor.
				 */
				expectRuleBody(index);
			}

			@Override
			void expectRuleBody(int index) {
				yieldManagement(new FontFeatureListManager(getManager()) {

					@Override
					protected void reportRuleEnd(int index) {
						FontFeatureValuesTH.this.reportRuleEnd(index);
					}

				});
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				if (buffer.length() != 0) {
					String ff = unescapeBuffer(index);
					if (checkValidCustomIdent(index, ff)) {
						familyList.add(ff);
					}
				}
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				familyList.clear();
				stage = 0;
			}

		}

		/**
		 * Parse either font feature at-rules or the 'font-display' descriptor.
		 */
		abstract class FontFeatureListManager extends DescriptorRuleListManager {

			FontFeatureListManager(HandlerManager parent) {
				super(parent);
			}

			@Override
			public void rightCurlyBracket(int index) {
				endManagement(index);
			}

			@Override
			protected CSSTokenHandler createUnknownRuleHandler(int index, String ruleName) {
				return new FontFeatureTH(ruleName);
			}

			/**
			 * font feature handler.
			 */
			class FontFeatureTH extends NoSelectorRuleHandler {

				private String featureMapName;

				FontFeatureTH(String ruleName) {
					super();
					this.featureMapName = ruleName;
				}

				@Override
				public void leftCurlyBracket(int index) {
					handler.startFeatureMap(featureMapName);
					super.leftCurlyBracket(index);
				}

				@Override
				protected void reportRuleEnd(int index) {
					handler.endFeatureMap();
				}

				@Override
				public HandlerManager getManager() {
					return FontFeatureListManager.this;
				}

			}

		}

		/**
		 * Handles a keyframes at-rule, where the preamble is a string or a
		 * custom-ident.
		 */
		class KeyframesTH extends AbstractBlockRuleHandler {

			String keyframesName = null;

			KeyframesTH() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder(64);
			}

			@Override
			public void word(int index, CharSequence word) {
				if (keyframesName == null) {
					super.word(index, word);
				} else {
					unexpectedTokenError(index, word);
				}
			}

			@Override
			public void escaped(int index, int codePoint) {
				if (keyframesName == null) {
					super.escaped(index, codePoint);
				} else {
					unexpectedCharError(index, codePoint);
				}
			}

			@Override
			public void character(int index, int codePoint) {
				unexpectedCharError(index, codePoint);
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quote) {
				String s = quoted.toString();
				if (keyframesName == null) {
					if (checkValidCustomIdent(index, s)) {
						keyframesName = s;
					}
				} else {
					unexpectedTokenError(index, s);
				}
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				int len = buffer.length();
				if (len != 0) {
					if (keyframesName == null) {
						String s = unescapeBuffer(index);
						if (checkValidCustomIdent(index, s)) {
							keyframesName = s;
						}
					} else {
						unexpectedTokenError(index - len, buffer);
					}
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (keyframesName != null) {
					handler.endKeyframes();
				}
			}

			@Override
			public void leftCurlyBracket(int index) {
				processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);

				if (isInError()) {
					sendLeftCurlyBracketEvent(index, this);
					return;
				}

				if (keyframesName == null) {
					unexpectedLeftCurlyBracketError(index);
					return;
				}

				handler.startKeyframes(keyframesName);

				/*
				 * Now we expect keyframe blocks.
				 */
				expectRuleBody(index);
			}

			@Override
			void expectRuleBody(int index) {
				yieldManagement(new KeyframeListManager(getManager()) {

					@Override
					public void endManagement(int index) {
						KeyframesTH.this.reportRuleEnd(index);
						restoreManagement(DeclarationRuleListManager.this);
					}

					@Override
					public void endOfStream(int len) {
						KeyframesTH.this.reportRuleEnd(index);
						DeclarationRuleListManager.this.endOfStream(len);
					}

				});
			}

			@Override
			public void reportError(CSSParseException ex) throws CSSParseException {
				super.reportError(ex);
				this.keyframesName = null;
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				this.keyframesName = null;
			}

		}

		abstract class KeyframeListManager extends CSSParserHandlerManager {

			KeyframeListManager(HandlerManager parent) {
				super(parent);
			}

			@Override
			protected CSSTokenHandler getInitialTokenHandler() {
				return new KeyframeTH();
			}

			@Override
			public void rightCurlyBracket(int index) {
				endManagement(index);
			}

			/**
			 * Keyframe-block handler.
			 */
			class KeyframeTH extends DeclarationIdentTokenHandler {

				private LexicalUnitImpl keyframeSelector = null;

				private LexicalUnitImpl currentlu = null;

				KeyframeTH() {
					super();
				}

				@Override
				protected void initializeBuffer() {
					buffer = new StringBuilder(50);
				}

				@Override
				public void commented(int index, int commentType, String comment) {
					separator(index, 12);
					if (keyframeSelector == null && currentlu == null && commentType == 0) {
						handler.comment(comment, isPreviousCpLF());
					}
					prevcp = 12;
				}

				@Override
				void processBuffer(int index, int triggerCp) {
					int len = buffer.length();
					if (len != 0) {
						String raw = buffer.toString();
						if (isValidIdentifier(raw)) {
							String s = unescapeBuffer(index);
							if (checkValidCustomIdent(index, s)) {
								LexicalUnitImpl sel = new LexicalUnitImpl(LexicalType.IDENT);
								sel.value = s;
								sel.identCssText = raw;
								setCurrentLexicalUnit(sel);
							}
						} else if ("0".equals(raw)) {
							LexicalUnitImpl sel = new LexicalUnitImpl(LexicalType.INTEGER);
							sel.intValue = 0;
							sel.setCssUnit(CSSUnit.CSS_NUMBER);
							setCurrentLexicalUnit(sel);
						} else {
							unexpectedTokenError(index, "Invalid identifier: " + raw);
						}
					}
				}

				@Override
				public void character(int index, int codePoint) {
					switch (codePoint) {
					case '%':
						if (buffer.length() > 0) {
							String s = rawBuffer();
							try {
								float pcnt = Float.parseFloat(s);
								LexicalUnitImpl sel = new LexicalUnitImpl(LexicalType.PERCENTAGE);
								sel.floatValue = pcnt;
								sel.dimensionUnitText = "%";
								sel.setCssUnit(CSSUnit.CSS_PERCENTAGE);
								setCurrentLexicalUnit(sel);
								return;
							} catch (NumberFormatException e) {
							}
						}
						break;
					case ',':
						processBuffer(index, codePoint);
						if (keyframeSelector == null) {
							break;
						}
						setCurrentLexicalUnit(new LexicalUnitImpl(LexicalType.OPERATOR_COMMA));
						prevcp = codePoint;
						return;
					case '-':
						if (buffer.length() == 0) {
							break;
						}
					case '.':
					case '+':
						buffer.append((char) codePoint);
						return;
					default:
					}
					unexpectedCharError(index, codePoint);
				}

				private void setCurrentLexicalUnit(LexicalUnitImpl sel) {
					if (currentlu != null) {
						currentlu.nextLexicalUnit = sel;
						sel.previousLexicalUnit = currentlu;
					} else {
						keyframeSelector = sel;
					}
					currentlu = sel;
				}

				@Override
				public void leftCurlyBracket(int index) {
					if (keyframeSelector == null) {
						processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
						if (isInError()) {
							sendLeftCurlyBracketEvent(index, this);
							return;
						}
						if (keyframeSelector == null) {
							unexpectedLeftCurlyBracketError(index);
							return;
						}
					}

					handler.startKeyframe(keyframeSelector);

					keyframeSelector = null;

					/*
					 * Now we expect a <declaration-list>.
					 */
					yieldManagement(new DescriptorListManager(getManager()) {

						@Override
						public void endManagement(int index) {
							reportRuleEnd(index);
							restoreManagement(KeyframeListManager.this);
						}

						@Override
						public void endOfStream(int len) {
							reportRuleEnd(index);
							KeyframeListManager.this.endOfStream(len);
						}

						@Override
						protected void reportRuleEnd(int index) {
							handler.endKeyframe();
						}

					});
				}

				@Override
				protected void resetHandler() {
					super.resetHandler();
					keyframeSelector = null;
					currentlu = null;
				}

				@Override
				public HandlerManager getManager() {
					return KeyframeListManager.this;
				}

			}

		}

		/**
		 * Handles a property at-rule, where the preamble is a custom property name.
		 */
		class PropertyTH extends AbstractBlockRuleHandler {

			private String customPropertyName = null;

			private CSSValueSyntax syntax = null;

			private boolean isUniversalSyntax, hasInherits;

			private LexicalUnit initialValue = null;

			private boolean ruleStarted = false;

			PropertyTH() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder(64);
			}

			@Override
			public void word(int index, CharSequence word) {
				if (customPropertyName == null) {
					super.word(index, word);
				} else {
					unexpectedTokenError(index, word);
				}
			}

			@Override
			public void escaped(int index, int codePoint) {
				if (customPropertyName == null) {
					super.escaped(index, codePoint);
				} else {
					unexpectedCharError(index, codePoint);
				}
			}

			@Override
			public void character(int index, int codePoint) {
				unexpectedCharError(index, codePoint);
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				int len = buffer.length();
				if (len != 0) {
					if (customPropertyName == null) {
						String name = unescapeBuffer(index);
						if (name.startsWith("--")) {
							customPropertyName = name;
							return;
						}
					}
					unexpectedTokenError(index - len, buffer);
				}
			}

			@Override
			protected void reportRuleEnd(int index) {
				if (ruleStarted) {
					if (syntax == null) {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX,
								"@property rule lacks mandatory 'syntax' descriptor.");
						handler.endProperty(true);
					} else if (!hasInherits) {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX,
								"@property rule lacks mandatory 'inherits' descriptor.");
						handler.endProperty(true);
					} else if (!isUniversalSyntax && (initialValue == null
							|| initialValue.matches(syntax) != CSSValueSyntax.Match.TRUE
							|| (initialValue.getLexicalUnitType() == LexicalType.DIMENSION
									&& CSSUnit.isRelativeLengthUnitType(
											initialValue.getCssUnit())))) {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX,
								"@property rule lacks a valid 'initial-value' descriptor.");
						handler.endProperty(true);
					} else {
						handler.endProperty(false);
					}
				}
			}

			@Override
			public void leftCurlyBracket(int index) {
				if (customPropertyName == null) {
					processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
					if (isInError()) {
						sendLeftCurlyBracketEvent(index, this);
						return;
					}
					if (customPropertyName == null) {
						unexpectedLeftCurlyBracketError(index);
						return;
					}
				}

				ruleStarted = true;

				handler.startProperty(customPropertyName);

				/*
				 * Now we expect a <declaration-list>
				 */
				expectRuleBody(index);
			}

			@Override
			void expectRuleBody(int index) {
				yieldManagement(new DescriptorListManager(getManager()) {

					@Override
					protected void handleProperty(int index, String propertyName,
							LexicalUnitImpl lunit, boolean priorityImportant) {
						if ("syntax".equalsIgnoreCase(propertyName)) {
							if (lunit.getLexicalUnitType() != LexicalType.STRING) {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX,
										"'syntax' descriptor in @property rule must be a string.");
								return;
							}
							String s = lunit.getStringValue().trim();
							SyntaxParser parser = new SyntaxParser();
							try {
								syntax = parser.parseSyntax(s);
							} catch (CSSException e) {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX,
										"Wrong 'syntax' descriptor in @property rule: '" + s
												+ '\'');
								return;
							}
							isUniversalSyntax = syntax
									.getCategory() == CSSValueSyntax.Category.universal;
						} else if ("inherits".equalsIgnoreCase(propertyName)) {
							String s;
							if (lunit.getLexicalUnitType() != LexicalType.IDENT || (!"true"
									.equals(s = lunit.getStringValue().toLowerCase(Locale.ROOT))
									&& !"false".equals(s))) {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX,
										"'inherits' descriptor in @property rule must be either 'true' or 'false'.");
								return;
							}
							hasInherits = true;
						} else if ("initial-value".equalsIgnoreCase(propertyName)) {
							handleLexicalProperty(index, propertyName, lunit, priorityImportant);
							initialValue = lunit;
							return;
						}
						super.handleProperty(index, propertyName, lunit, priorityImportant);
					}

					@Override
					public void endManagement(int index) {
						reportRuleEnd(index);
						restoreManagement(DeclarationRuleListManager.this);
					}

					@Override
					protected void reportRuleEnd(int index) {
						PropertyTH.this.reportRuleEnd(index);
					}

				});
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				customPropertyName = null;
				syntax = null;
				initialValue = null;
				hasInherits = false;
			}

		}

	}

	private class SelectorArgumentTokenHandler extends SelectorTokenHandler {

		SelectorArgumentTokenHandler(NSACSelectorFactory factory) {
			super(factory);
		}

		@Override
		protected void newCombinatorSelector(int index, SelectorType type, int triggerCp) {
			if (currentsel == null) {
				currentsel = factory.createScopeSelector();
			} else if (!isValidCurrentSelector()) {
				unexpectedCharError(index, triggerCp);
				return;
			}
			newCombinatorSelector(type);
		}

	}

	private class SelectorArgumentManager extends SelectorManager {

		SelectorArgumentManager(NSACSelectorFactory factory) {
			super(new SelectorArgumentTokenHandler(factory));
		}

	}

	class SelectorManager extends CSSParserHandlerManager {

		SelectorTokenHandler selectorHandler;

		SelectorManager(SelectorTokenHandler selectorHandler) {
			super();
			this.selectorHandler = selectorHandler;
			this.selectorHandler.setManager(this);
		}

		SelectorManager() {
			this(new NSACSelectorFactory());
		}

		SelectorManager(NamespaceMap nsMap) {
			super();
			if (nsMap == null) {
				this.selectorHandler = new SelectorTokenHandler(new NSACSelectorFactory());
			} else {
				this.selectorHandler = new SelectorTokenHandler(nsMap);
			}
			this.selectorHandler.setManager(this);
		}

		SelectorManager(NSACSelectorFactory factory) {
			super();
			this.selectorHandler = new SelectorTokenHandler(factory);
			this.selectorHandler.setManager(this);
		}

		@Override
		protected SelectorTokenHandler getInitialTokenHandler() {
			return selectorHandler;
		}

		public SelectorList getSelectorList() {
			return selectorHandler.getSelectorList();
		}

		SelectorListImpl getTrimmedSelectorList() {
			return selectorHandler.getTrimmedSelectorList();
		}

		NSACSelectorFactory getSelectorFactory() {
			return selectorHandler.factory;
		}

	}

	class SelectorTokenHandler extends ManagerCallbackTokenHandler {

		NSACSelectorFactory factory;
		NamespaceMap nsMap;
		ParserSelectorListImpl selist = new ParserSelectorListImpl();
		Selector currentsel = null;

		// TODO: handle default namespace if set
		private String namespacePrefix = null;
		byte stage = 0;
		private boolean functionToken;

		static final byte STAGE_INITIAL = 0;
		private static final byte STAGE_COMBINATOR_OR_END = 2;
		private static final byte STAGE_ATTR_START = 4;
		private static final byte STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE = 7;
		private static final byte STAGE_ATTR_SYMBOL = 5;
		private static final byte STAGE_ATTR_POST_SYMBOL = 6;
		private static final byte STAGE_EXPECT_ID_OR_CLASSNAME = 8;
		private static final byte STAGE_EXPECT_PSEUDOELEM_NAME = 9;
		private static final byte STAGE_EXPECT_PSEUDOCLASS_NAME = 10;
		private static final byte STAGE_EXPECT_PSEUDOCLASS_ARGUMENT = 11;

		/**
		 * The curly bracket depth.
		 */
		int curlyBracketDepth;

		SelectorTokenHandler(NamespaceMap nsMap) {
			super();
			factory = new NSACSelectorFactory();
			if (nsMap == null) {
				this.nsMap = factory;
			} else {
				this.nsMap = nsMap;
			}
			buffer = new StringBuilder(64);
		}

		SelectorTokenHandler(NSACSelectorFactory factory) {
			super();
			this.factory = factory;
			this.nsMap = factory;
			buffer = new StringBuilder(100);
		}

		SelectorListImpl getSelectorList() {
			return selist;
		}

		SelectorListImpl getTrimmedSelectorList() {
			selist.trimToSize();
			return selist;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (buffer.length() != 0 && isPrevCpWhitespace()) {
				buffer.append(' ');
			}
			if (stage == STAGE_ATTR_START && prevcp != 65
					&& prevcp != TokenProducer.CHAR_VERTICAL_LINE) {
				unexpectedTokenError(index, word);
			} else {
				if (stage == 0) {
					stage = 1;
					buffer.append(word);
				} else if (stage == STAGE_COMBINATOR_OR_END) {
					buffer.append(word);
					newCombinatorSelector(SelectorType.DESCENDANT);
					stage = 1;
				} else if (stage == STAGE_ATTR_POST_SYMBOL && isPrevCpWhitespace()) {
					if (word.length() == 1) {
						char c = word.charAt(0);
						if (c == 'i' || c == 'I') {
							setAttributeConditionFlag(AttributeCondition.Flag.CASE_I);
						} else if (c == 's' || c == 'S') {
							setAttributeConditionFlag(AttributeCondition.Flag.CASE_S);
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
									"Expected 'i', found: '" + c + '\'');
						}
						if (buffer.length() != 0) {
							handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
									"Expected 'i' or 's', found: '" + buffer.toString() + '\'');
							buffer.setLength(0);
						}
					} else {
						handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
								"Expected 'i', found: '" + word + "'");
					}
				} else if (stage == 1) {
					if (prevcp != '*') {
						buffer.append(word);
					} else {
						unexpectedTokenError(index, word);
					}
				} else if (stage != STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE) {
					buffer.append(word);
				} else {
					unexpectedTokenError(index, word);
				}
			}
			prevcp = 65;
		}

		@Override
		public void separator(int index, int codepoint) {
			if (isEscapedIdent() && bufferEndsWithEscapedChar(buffer)) {
				buffer.append(' ');
				return;
			}
			if (prevcp == ':' || prevcp == '.' || prevcp == '#'
					|| (prevcp == TokenProducer.CHAR_VERTICAL_LINE
							&& getActiveSelector() == null)) {
				unexpectedCharError(index, codepoint);
				return;
			}
			if (stage == STAGE_ATTR_SYMBOL) {
				if (buffer.length() != 0) {
					setAttributeSelectorValue(index, unescapeBuffer(index));
					stage = STAGE_ATTR_POST_SYMBOL;
				}
			} else if (stage == 1 || stage == STAGE_EXPECT_ID_OR_CLASSNAME
					|| stage == STAGE_EXPECT_PSEUDOELEM_NAME
					|| stage == STAGE_EXPECT_PSEUDOCLASS_NAME) {
				processBuffer(index, codepoint, false);
				if (prevcp == 65 || prevcp == 42 || prevcp == 41 || prevcp == 93) {
					// letter-or-digit, *, ), ]
					stage = STAGE_COMBINATOR_OR_END;
				}
			} else if (stage == STAGE_ATTR_START) {
				if (buffer.length() != 0) {
					stage = STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE;
				} else if (namespacePrefix != null) {
					unexpectedCharError(index, codepoint);
				}
				return;
			}
			if (prevcp != 44) {
				// If previous cp was a comma, we keep it
				setWhitespacePrevCp();
			}
		}

		@Override
		String unescapeBuffer(int index) {
			String s;
			if (namespacePrefix == null) {
				s = unescapeStringValue(index);
			} else {
				handleError(index - namespacePrefix.length() - 1, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected token: " + namespacePrefix);
				namespacePrefix = null;
				s = "";
			}
			buffer.setLength(0);
			resetEscapedTokenIndex();
			return s;
		}

		void processBuffer(int index, int triggerCp, boolean lastStage) {
			if (prevcp == 42) { // *
				if (currentsel == null || currentsel.getSelectorType() != SelectorType.UNIVERSAL) {
					setSimpleSelector(index, factory.getUniversalSelector(namespacePrefix));
				}
			} else if (stage == STAGE_COMBINATOR_OR_END) {
				if (!lastStage) {
					newCombinatorSelector(SelectorType.DESCENDANT);
					if (buffer.length() != 0) {
						// Type selectors are identifiers and could be escaped
						ElementSelectorImpl sel = newElementSelector(index);
						String raw = buffer.toString();
						if (isNotForbiddenIdentStart(raw)) {
							String s = unescapeBuffer(index);
							sel.localName = s;
							stage = 1;
						} else {
							handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
									"Invalid identifier: " + raw);
						}
					}
				}
			} else if (buffer.length() != 0) {
				if (stage == 1) {
					String raw = buffer.toString();
					ElementSelectorImpl sel = newElementSelector(index);
					String uri;
					if (namespacePrefix == null) {
						uri = getDefaultNamespaceURI();
					} else {
						uri = getNamespaceURI(index);
						if (parseError) {
							return;
						}
					}
					sel.namespaceUri = uri;
					if (isNotForbiddenIdentStart(raw)) {
						String s = unescapeBuffer(index);
						sel.localName = s;
					} else {
						handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid identifier: " + raw);
					}
				} else if (stage == STAGE_EXPECT_ID_OR_CLASSNAME) {
					String raw = buffer.toString();
					if (isNotForbiddenIdentStart(raw)) {
						String s = unescapeBuffer(index).trim();
						setAttributeSelectorValue(index, s);
						stage = 1;
					} else {
						handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid class name: " + raw);
					}
				} else if (stage == STAGE_EXPECT_PSEUDOCLASS_NAME) {
					newConditionalSelector(index, triggerCp, ConditionType.PSEUDO_CLASS);
					stage = 1;
				} else if (stage == STAGE_EXPECT_PSEUDOELEM_NAME) {
					newConditionalSelector(index, triggerCp, ConditionType.PSEUDO_ELEMENT);
					stage = 1;
				} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				} else if (stage == STAGE_ATTR_POST_SYMBOL) {
					setAttributeSelectorValue(index, unescapeBuffer(index));
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected: <" + buffer + ">");
					buffer.setLength(0);
				}
			} else if (namespacePrefix != null) {
				handleError(index - namespacePrefix.length() - 1, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected: " + namespacePrefix + "|");
				namespacePrefix = null;
			} else if (stage > 1 && stage != 11) {
				unexpectedCharError(index, triggerCp);
			}
		}

		@Override
		void processBuffer(int index, int triggerCp) {
			// DO NOT CALL THIS FROM SELECTOR HANDLER
			throw new IllegalStateException();
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteChar) {
			if (stage == STAGE_ATTR_SYMBOL && currentsel != null) { // Attribute selector
				setAttributeSelectorValue(index, quoted);
				stage = STAGE_ATTR_POST_SYMBOL;
			} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) { // Pseudo-class argument
				if (buffer.length() != 0 && isPrevCpWhitespace()) {
					buffer.append(' ');
				}
				char c = (char) quoteChar;
				buffer.append(c).append(quoted).append(c);
			} else {
				char c = (char) quoteChar;
				StringBuilder buf = new StringBuilder(quoted.length() + 2);
				buf.append(c).append(quoted).append(c);
				unexpectedTokenError(index, buf.toString());
			}
			prevcp = 65;
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			if (stage == STAGE_ATTR_SYMBOL && currentsel != null) { // Attribute selector
				setAttributeSelectorValue(index, quoted);
				stage = STAGE_ATTR_POST_SYMBOL;
			} else {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Quoted string contained unexpected control character: \"" + quoted + '"');
			}
			prevcp = 65;
		}

		private void setAttributeSelectorValue(int index, CharSequence value) {
			Condition cond = null;
			if (currentsel instanceof CombinatorSelectorImpl) {
				Selector simple = ((CombinatorSelectorImpl) currentsel).getSecondSelector();
				if (!(simple instanceof ConditionalSelectorImpl)) {
					throw new IllegalStateException(
							"Descendant selector has no conditional simple selector");
				}
				cond = ((ConditionalSelectorImpl) simple).condition;
			} else if (currentsel instanceof ConditionalSelectorImpl) {
				cond = ((ConditionalSelectorImpl) currentsel).condition;
			}
			if (cond instanceof CombinatorConditionImpl) {
				cond = ((CombinatorConditionImpl) cond).getSecondCondition();
			}
			if (cond instanceof AttributeConditionImpl) {
				AttributeConditionImpl attrcond = (AttributeConditionImpl) cond;
				if (attrcond != null) {
					String oldValue = attrcond.getValue();
					if (oldValue == null) {
						attrcond.setValue(value.toString());
					} else {
						StringBuilder buf = new StringBuilder(
								oldValue.length() + value.length() + 1);
						buf.append(oldValue);
						if (isPrevCpWhitespace()) {
							buf.append(' ');
						}
						attrcond.setValue(buf.append(value).toString());
					}
					return;
				}
			}
			handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
					"Unexpected token in selector: <" + value + ">");
		}

		private void setAttributeConditionFlag(AttributeCondition.Flag flag) {
			Selector simple = getActiveSelector();
			if (simple == null || simple.getSelectorType() != SelectorType.CONDITIONAL) {
				throw new IllegalStateException(
						"Processing attribute modifier of non-conditional selector");
			}
			Condition cond = ((ConditionalSelectorImpl) simple).getCondition();
			if (cond.getConditionType() == ConditionType.AND) {
				// If it wasn't the second condition, we would not be here
				cond = ((CombinatorCondition) cond).getSecondCondition();
			}
			((AttributeConditionImpl) cond).setFlag(flag);
		}

		private Selector getActiveSelector() {
			Selector sel;
			if (currentsel instanceof CombinatorSelectorImpl) {
				sel = ((CombinatorSelectorImpl) currentsel).getSecondSelector();
			} else {
				sel = currentsel;
			}
			return sel;
		}

		private Condition getActiveCondition(Condition cond) {
			while (cond.getConditionType() == ConditionType.AND) {
				cond = ((CombinatorConditionImpl) cond).getSecondCondition();
			}
			return cond;
		}

		@Override
		public void leftParenthesis(int index) {
			parendepth++;
			if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				buffer.append('(');
				prevcp = TokenProducer.CHAR_LEFT_PAREN;
			} else if (!isInError()) {
				if (prevcp != 65 || buffer.length() == 0
						|| stage != STAGE_EXPECT_PSEUDOCLASS_NAME) {
					unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
				} else {
					newConditionalSelector(index, TokenProducer.CHAR_LEFT_PAREN,
							ConditionType.PSEUDO_CLASS);
					if (!isInError()) {
						stage = STAGE_EXPECT_PSEUDOCLASS_ARGUMENT;
						functionToken = true;
					}
				}
			}
			prevcp = TokenProducer.CHAR_LEFT_PAREN;
		}

		@Override
		public void leftSquareBracket(int index) {
			if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				buffer.append('[');
				prevcp = TokenProducer.CHAR_LEFT_SQ_BRACKET;
			} else if (!isInError()) {
				if (prevcp != 65 && isNotSeparator(prevcp) && prevcp != 42 && prevcp != 44
						&& prevcp != 93 && prevcp != 41 && prevcp != 43 && prevcp != 62
						&& prevcp != 125 && prevcp != 126 && prevcp != 124) {
					// Not letter-or-digit nor *,ws)]+}~|>
					unexpectedCharError(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
				} else {
					processBuffer(index, TokenProducer.CHAR_LEFT_SQ_BRACKET, false);
					stage = STAGE_ATTR_START;
					prevcp = 65;
				}
			}
		}

		@Override
		public void leftCurlyBracket(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
		}

		@Override
		public void rightParenthesis(int index) {
			decrParenDepth(index);
			if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				if (parendepth == 0) {
					Selector sel = getActiveSelector();
					if (sel.getSelectorType() == SelectorType.CONDITIONAL) {
						Condition cond = ((ConditionalSelectorImpl) sel).condition;
						cond = getActiveCondition(cond);
						ConditionType condtype = cond.getConditionType();
						if (buffer.length() != 0) {
							if (condtype == ConditionType.SELECTOR_ARGUMENT) {
								try {
									((SelectorArgumentConditionImpl) cond).arguments = parseSelectorArgument(
											rawBuffer(), factory);
								} catch (CSSParseException e) {
									byte errCode;
									if (e.getClass() == CSSNamespaceParseException.class) {
										errCode = ParseHelper.ERR_UNKNOWN_NAMESPACE;
									} else {
										errCode = ParseHelper.ERR_EXPR_SYNTAX;
									}
									CSSParseException ex = createException(index, errCode,
											e.getMessage());
									handleError(ex);
									stage = 127;
								}
							} else if (condtype == ConditionType.POSITIONAL) {
								if (((PositionalConditionImpl) cond).hasArgument()) {
									String arg = rawBuffer();
									if (!parsePositionalArgument((PositionalConditionImpl) cond,
											arg)) {
										handleError(index, ParseHelper.ERR_EXPR_SYNTAX,
												"Wrong subexpression: " + arg);
									}
								}
							} else if (condtype == ConditionType.LANG) {
								String s = unescapeBuffer(index);
								int len = s.length();
								if (s.charAt(len - 1) == ',') {
									handleError(index - 2, ParseHelper.ERR_UNEXPECTED_TOKEN,
											"Unexpected functional argument: " + s);
									return;
								}
								((LangConditionImpl) cond).lang = s;
							} else if (condtype == ConditionType.PSEUDO_CLASS) {
								String s = unescapeBuffer(index);
								char c;
								if ((c = s.charAt(0)) != '"' && c != '\''
										&& !isValidPseudoName(s)) {
									handleError(index - s.length() - 1,
											ParseHelper.ERR_UNEXPECTED_TOKEN,
											"Unexpected functional argument: " + s);
									return;
								}
								((PseudoConditionImpl) cond).argument = s;
							}
							buffer.setLength(0);
							resetEscapedTokenIndex();
							stage = 1;
						} else {
							if (condtype == ConditionType.LANG
									|| condtype == ConditionType.SELECTOR_ARGUMENT
									|| condtype == ConditionType.POSITIONAL) {
								unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
							} else {
								unexpectedCharError(index - 1, TokenProducer.CHAR_RIGHT_PAREN);
							}
						}
					} else {
						unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
					}
				} else {
					buffer.append(')');
					prevcp = TokenProducer.CHAR_RIGHT_PAREN;
					return;
				}
			} else {
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
			}
			if (functionToken) {
				functionToken = false;
			}
			prevcp = TokenProducer.CHAR_RIGHT_PAREN;
		}

		@Override
		public void rightSquareBracket(int index) {
			if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				buffer.append(']');
			} else if (stage == STAGE_ATTR_POST_SYMBOL) {
				if (buffer.length() != 0) {
					setAttributeSelectorValue(index, unescapeBuffer(index));
				}
				stage = 1;
			} else if (stage == STAGE_ATTR_START || stage == STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE) {
				if (buffer.length() != 0) {
					newConditionalSelector(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET,
							ConditionType.ATTRIBUTE);
					stage = 1;
				} else {
					// Error
					handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
							"Unexpected ']', expected attribute name");
				}
			} else if (stage == STAGE_ATTR_SYMBOL) {
				if (buffer.length() != 0) {
					setAttributeSelectorValue(index, unescapeBuffer(index));
					stage = 1;
				} else {
					// Error
					handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
							"Unexpected ']', expected attribute value");
				}
			} else {
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
			}
			prevcp = TokenProducer.CHAR_RIGHT_SQ_BRACKET;
		}

		@Override
		public void rightCurlyBracket(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '}'");
		}

		@Override
		public void character(int index, int codepoint) {
			// @formatter:off
			//
			// stages (stage=1 means "found something", 2 means "ws after something"):
			//  tagname.classname[attr=value i]
			// 0|   1  |8|1      |4   |5|6    |1
			//  tagname tagname
			// 0|   1  |2
			// Stage  7: whitespace after stage 4
			// Stage  9: waiting for pseudo-element name
			// Stage 10: waiting for pseudo-class name
			// Stage 11: waiting for pseudo-class argument
			// Stage  2: waiting for possible descendant selector
			//
			// ! 33
			// # 35
			// % 37
			// + 43
			// , 44
			// . 46
			// : 58
			// ; 59
			// < 60
			// = 61
			// > 62
			// @ 64
			//
			// @formatter:on
			if (!skipCharacterHandling()) {
				if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
					// Special case: comma
					if (codepoint == 44 && (prevcp == 44 || buffer.length() == 0)) {
						unexpectedCharError(index, codepoint);
						return;
					}
					if (isPrevCpWhitespace() && buffer.length() != 0) {
						buffer.append(' ');
					}
					bufferAppend(codepoint);
				} else if (stage == STAGE_ATTR_START
						|| stage == STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE) {
					if (codepoint == TokenProducer.CHAR_VERTICAL_LINE) {
						if (stage == STAGE_ATTR_START) {
							if (namespacePrefix == null) {
								readNamespacePrefix(index, codepoint);
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else if (prevcp != 65) {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 61) { // =
						// If we are in a '|=' case, we should have the attribute name in
						// namespacePrefix
						if (prevcp == TokenProducer.CHAR_VERTICAL_LINE && namespacePrefix != null
								&& buffer.length() == 0) {
							buffer.append(namespacePrefix);
							namespacePrefix = null;
						}
						// Process the buffer according to the previous character
						if (prevcp == TokenProducer.CHAR_VERTICAL_LINE) { // |
							newConditionalSelector(index, codepoint,
									ConditionType.BEGIN_HYPHEN_ATTRIBUTE);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 126) { // ~
							newConditionalSelector(index, codepoint,
									ConditionType.ONE_OF_ATTRIBUTE);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 36) { // $
							newConditionalSelector(index, codepoint, ConditionType.ENDS_ATTRIBUTE);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 94) { // ^
							newConditionalSelector(index, codepoint,
									ConditionType.BEGINS_ATTRIBUTE);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 42) { // *
							newConditionalSelector(index, codepoint,
									ConditionType.SUBSTRING_ATTRIBUTE);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 65) {
							newConditionalSelector(index, codepoint, ConditionType.ATTRIBUTE);
							stage = STAGE_ATTR_SYMBOL;
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (buffer.length() == 0) {
						if (codepoint != TokenProducer.CHAR_TILDE
								&& codepoint != TokenProducer.CHAR_DOLLAR
								&& codepoint != TokenProducer.CHAR_CIRCUMFLEX_ACCENT
								&& codepoint != TokenProducer.CHAR_ASTERISK) {
							if (stage == STAGE_ATTR_START
									&& ParseHelper.isValidXMLStartCharacter(codepoint)) {
								bufferAppend(codepoint);
								prevcp = 65;
								return;
							}
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint != TokenProducer.CHAR_TILDE
							&& codepoint != TokenProducer.CHAR_DOLLAR
							&& codepoint != TokenProducer.CHAR_CIRCUMFLEX_ACCENT
							&& codepoint != TokenProducer.CHAR_ASTERISK) {
						if (stage == STAGE_ATTR_START
								&& ParseHelper.isValidXMLCharacter(codepoint)) {
							bufferAppend(codepoint);
							prevcp = 65;
							return;
						}
						unexpectedCharError(index, codepoint);
					}
				} else if (codepoint == 42) { // *
					if (stage == 0) {
						stage = 1;
					} else if (stage == STAGE_COMBINATOR_OR_END) {
						newCombinatorSelector(SelectorType.DESCENDANT);
						((CombinatorSelectorImpl) currentsel).simpleSelector = factory
								.getUniversalSelector(namespacePrefix);
						namespacePrefix = null;
						stage = 1;
					} else if (stage == 1 && namespacePrefix != null
							&& prevcp == TokenProducer.CHAR_VERTICAL_LINE) {
						setSimpleSelector(index,
								factory.createUniversalSelector(getNamespaceURI(index)));
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else {
					if (prevcp == TokenProducer.CHAR_VERTICAL_LINE) {
						if (codepoint == TokenProducer.CHAR_VERTICAL_LINE) {
							handleColumnCombinator(index);
							prevcp = 32;
							return;
						}
						unexpectedCharError(index, codepoint);
						return;
					}
					switch (codepoint) {
					case TokenProducer.CHAR_TILDE: // ~
						if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						} else {
							processBuffer(index, codepoint, false);
							if (isInError()) {
								return;
							}
						}
						newCombinatorSelector(index, SelectorType.SUBSEQUENT_SIBLING, codepoint);
						break;
					case 46: // .
						if (stage != STAGE_EXPECT_ID_OR_CLASSNAME || buffer.length() != 0) {
							processBuffer(index, codepoint, false);
							if (!isInError()) {
								newConditionalSelector(index, codepoint, ConditionType.CLASS);
								stage = STAGE_EXPECT_ID_OR_CLASSNAME;
							}
						} else {
							unexpectedCharError(index, codepoint);
						}
						break;
					case 35: // #
						if (stage != STAGE_EXPECT_ID_OR_CLASSNAME || buffer.length() != 0) {
							processBuffer(index, codepoint, false);
							if (!isInError()) {
								newConditionalSelector(index, codepoint, ConditionType.ID);
								stage = STAGE_EXPECT_ID_OR_CLASSNAME;
							}
						} else {
							unexpectedCharError(index, codepoint);
						}
						break;
					case 58: // :
						if (prevcp == 58) {
							stage = STAGE_EXPECT_PSEUDOELEM_NAME;
						} else {
							processBuffer(index, codepoint, false);
							stage = STAGE_EXPECT_PSEUDOCLASS_NAME;
						}
						break;
					case TokenProducer.CHAR_GREATER_THAN: // >
						if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						} else if (stage == 1 && equalSequences("--", buffer)) {
							if (isTopLevel() && prevcp == 65 && !isEscapedIdent()
									&& !functionToken) {
								buffer.setLength(0);
								stage = 0;
								prevcp = 32;
								return;
							}
							unexpectedCharError(index, codepoint);
						}
						processBuffer(index, codepoint, false);
						if (stage < 2 && !isInError()) {
							newCombinatorSelector(index, SelectorType.CHILD, codepoint);
						} else {
							unexpectedCharError(index, codepoint);
						}
						break;
					case 43: // +
						if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						}
						processBuffer(index, codepoint, false);
						if (!isInError()) {
							newCombinatorSelector(index, SelectorType.DIRECT_ADJACENT, codepoint);
						}
						break;
					case TokenProducer.CHAR_VERTICAL_LINE:
						// |
						if (stage == STAGE_EXPECT_ID_OR_CLASSNAME
								|| stage == STAGE_EXPECT_PSEUDOCLASS_NAME
								|| stage == STAGE_EXPECT_PSEUDOELEM_NAME) {
							processBuffer(index, codepoint, false);
							if (isInError()) {
								return;
							}
							try {
								int ncp = getTokenControl().skipNextCodepoint();
								if (ncp != TokenProducer.CHAR_VERTICAL_LINE) {
									if (ncp == -1) {
										unexpectedEOFError(index + 1,
												"EOF while processing column combinator selector");
									} else {
										unexpectedCharError(index + 1, ncp);
									}
								} else {
									newCombinatorSelector(index, SelectorType.COLUMN_COMBINATOR,
											TokenProducer.CHAR_VERTICAL_LINE);
									prevcp = 32;
									return;
								}
							} catch (IOException e) {
								handleError(index + 1, ParseHelper.ERR_IO,
										"I/O Error when processing column combinator selector", e);
							}
						} else if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						} else if (stage == 1 && namespacePrefix == null) {
							readNamespacePrefix(index, codepoint);
						} else if (stage == 0 && namespacePrefix == null && buffer.length() == 0) {
							namespacePrefix = "";
						} else {
							unexpectedCharError(index, codepoint);
						}
						break;
					case 44: // ,
						if (functionToken) {
							if (prevcp == 44) { // Consecutive commas
								unexpectedCharError(index, codepoint);
							} else {
								// Probably happening inside [] TODO better error checking
								buffer.append(',');
							}
						} else {
							processBuffer(index, codepoint, true);
							if (!isInError()) {
								if (addCurrentSelector(index)) {
									stage = 0;
								} else {
									unexpectedCharError(index, codepoint);
								}
							}
						}
						break;
					case 64: // @
						if (stage == 0 && selist.isEmpty()) {
							handleAtKeyword(index);
						} else {
							unexpectedCharError(index, 64);
						}
						break;
					case 45: // -
						buffer.append('-');
						break;
					case 95: // _
						buffer.append('_');
						break;
					case TokenProducer.CHAR_AMPERSAND: // &
						if (buffer.length() != 0 || (stage > STAGE_COMBINATOR_OR_END
								&& stage != STAGE_EXPECT_PSEUDOCLASS_ARGUMENT)) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage == STAGE_COMBINATOR_OR_END) {
								stage = 1;
							}
							newConditionalSelector(index, codepoint, ConditionType.NESTING);
						}
						break;
					default:
						if (stage < 8) {
							if (stage == 0) {
								if (ParseHelper.isValidXMLStartCharacter(codepoint)) {
									bufferAppend(codepoint);
									stage = 1;
									prevcp = 65;
									return;
								}
							} else if (ParseHelper.isValidXMLCharacter(codepoint)) {
								bufferAppend(codepoint);
								prevcp = 65;
								return;
							}
							if (codepoint == TokenProducer.CHAR_LESS_THAN) {
								if (isTopLevel()) {
									processBuffer(index, codepoint, false);
									if (!isInError()) {
										handleCDO();
									}
									prevcp = 32;
									return;
								}
							} else if (codepoint == TokenProducer.CHAR_SEMICOLON
									&& parendepth == 0) {
								handleSemicolon(index);
								prevcp = 32;
								return;
							}
						} else if (!isUnexpectedCharacter(codepoint)) {
							bufferAppend(codepoint);
							prevcp = 65;
							return;
						}
						unexpectedCharError(index, codepoint);
					}
				}
			}
			prevcp = codepoint;
		}

		boolean skipCharacterHandling() {
			return parseError;
		}

		private void handleColumnCombinator(int index) {
			if (stage == 1) {
				if (currentsel == null) {
					ElementSelectorImpl sel = newElementSelector(index);
					if (namespacePrefix != null) {
						sel.localName = namespacePrefix;
						namespacePrefix = null;
					} else if (buffer.length() != 0) {
						// Unclear whether this is reachable
						sel.localName = unescapeBuffer(index);
					} else {
						unexpectedCharError(index, TokenProducer.CHAR_VERTICAL_LINE);
						return;
					}
				}
				newCombinatorSelector(index, SelectorType.COLUMN_COMBINATOR,
						TokenProducer.CHAR_VERTICAL_LINE);
			} else if (stage == 0 && buffer.length() == 0 && currentsel == null) {
				namespacePrefix = null;
				newCombinatorSelector(index, SelectorType.COLUMN_COMBINATOR,
						TokenProducer.CHAR_VERTICAL_LINE);
			} else {
				unexpectedCharError(index, TokenProducer.CHAR_VERTICAL_LINE);
			}
		}

		/**
		 * Test whether the given code point represents a special CSS character that
		 * could be processed through this <code>character</code> method (thus excluding
		 * group delimiters like <code>(</code> and <code>)</code>, and code points
		 * previously tested.
		 * <p>
		 * Characters tested so far:
		 * 
		 * <pre>
		 *  * . # : > + ~ | , @ - _
		 * </pre>
		 * 
		 * @param cp the code point to test.
		 * @return <code>true</code> if it is an untested character with special
		 *         meaning, <code>false</code> otherwise.
		 */
		private boolean isUnexpectedCharacter(int cp) {
			return cp == 0x21 || cp == 0x24 || cp == 0x25 || cp == 0x26 || cp == 0x2f
					|| (cp >= 0x3b && cp <= 0x3f) || cp == 0x5e || cp == 0x60;
			// x21 !, x24 $, x25 %, x26 & x2f /, x3b ;, x3c <,
			// x3d =, x3e >, x3f ?, x5e ^, x60 `
		}

		private void handleCDO() {
			CDOTokenHandler cdoCdcTH = new CDOTokenHandler(getManager());
			cdoCdcTH.setYieldHandler(this);
			yieldHandling(cdoCdcTH);
		}

		private void readNamespacePrefix(int index, int codepoint) {
			if (prevcp == 65) {
				namespacePrefix = unescapeBuffer(index);
			} else if (prevcp == TokenProducer.CHAR_ASTERISK && buffer.length() == 0) {
				namespacePrefix = "*";
			} else {
				unexpectedCharError(index, codepoint);
			}
		}

		protected void handleAtKeyword(int index) {
			unexpectedCharError(index, 64);
		}

		protected void handleSemicolon(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_SEMICOLON);
		}

		private void newConditionalSelector(int index, int triggerCp, ConditionType condtype) {
			String name = rawBuffer();
			String lcname = name.toLowerCase(Locale.ROOT).intern();
			Condition condition;
			if (condtype == ConditionType.PSEUDO_CLASS) {
				// See if we can specify the pseudo class.
				if ("lang".equals(lcname)) {
					condition = factory.createCondition(ConditionType.LANG);
				} else if ("first-child".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createPositionalCondition();
				} else if ("nth-child".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
				} else if ("last-child".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createPositionalCondition();
					((PositionalConditionImpl) condition).offset = 1;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("nth-last-child".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
					((PositionalConditionImpl) condition).offset = 1;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("first-of-type".equals(lcname)) {
					condition = factory.createPositionalCondition();
					((PositionalConditionImpl) condition).oftype = true;
					((PositionalConditionImpl) condition).offset = 1;
				} else if ("last-of-type".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createPositionalCondition();
					((PositionalConditionImpl) condition).oftype = true;
					((PositionalConditionImpl) condition).offset = 1;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("nth-of-type".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
					((PositionalConditionImpl) condition).oftype = true;
				} else if ("nth-last-of-type".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
					((PositionalConditionImpl) condition).oftype = true;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("only-child".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createCondition(ConditionType.ONLY_CHILD);
				} else if ("only-of-type".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createCondition(ConditionType.ONLY_TYPE);
				} else if ("not".equals(lcname) || "is".equals(lcname) || "has".equals(lcname)
						|| "where".equals(lcname)) {
					if (triggerCp != TokenProducer.CHAR_LEFT_PAREN) {
						StringBuilder buf = new StringBuilder(name.length() * 2 + 26);
						buf.append("Expected ':").append(name).append("(', found ':").append(name)
								.appendCodePoint(triggerCp).append('\'');
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, buf.toString());
						return;
					}
					condition = factory.createCondition(ConditionType.SELECTOR_ARGUMENT);
					((SelectorArgumentConditionImpl) condition).setName(lcname);
				} else { // Other pseudo-classes
					if ("first-line".equals(lcname) || "first-letter".equals(lcname)
							|| "before".equals(lcname) || "after".equals(lcname)) {
						// Old-syntax pseudo-element
						condtype = ConditionType.PSEUDO_ELEMENT;
					}
					condition = factory.createCondition(condtype);
				}
			} else {
				condition = factory.createCondition(condtype);
			}
			if (condition.getConditionType() == ConditionType.PSEUDO_CLASS) {
				if (isValidPseudoName(name)) {
					((PseudoConditionImpl) condition).name = safeUnescapeIdentifier(index, name);
				} else {
					handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
							"Invalid pseudo-class: " + name);
					return;
				}
			} else if (condition.getConditionType() == ConditionType.PSEUDO_ELEMENT) {
				if (!isValidPseudoName(name)) {
					handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
							"Invalid pseudo-element: " + name);
					return;
				} else if (triggerCp == '(') {
					handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
							"Invalid pseudo-element declaration: " + name + '(');
					return;
				} else {
					((PseudoConditionImpl) condition).name = safeUnescapeIdentifier(index, name);
				}
			}
			if (name.length() != 0 && condition instanceof AttributeConditionImpl) {
				switch (condition.getConditionType()) {
				case ATTRIBUTE:
				case BEGIN_HYPHEN_ATTRIBUTE:
				case ONE_OF_ATTRIBUTE:
				case ENDS_ATTRIBUTE:
				case SUBSTRING_ATTRIBUTE:
				case BEGINS_ATTRIBUTE:
					if (namespacePrefix != null) {
						((AttributeConditionImpl) condition)
								.setNamespaceURI(getNamespaceURI(index));
					}
					if (isNotForbiddenIdentStart(name)) {
						((AttributeConditionImpl) condition)
								.setLocalName(safeUnescapeIdentifier(index, name).trim());
					} else {
						handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid pseudo-class: " + name);
						return;
					}
					break;
				default:
					((AttributeConditionImpl) condition).setValue(lcname.trim());
				}
			}

			if (currentsel instanceof CombinatorSelectorImpl) {
				Selector simple = ((CombinatorSelectorImpl) currentsel).getSecondSelector();
				if (simple != null && simple.getSelectorType() == SelectorType.CONDITIONAL) {
					CombinatorConditionImpl andcond = (CombinatorConditionImpl) factory
							.createCondition(ConditionType.AND);
					andcond.first = ((ConditionalSelectorImpl) simple).getCondition();
					andcond.second = condition;
					((CombinatorSelectorImpl) currentsel).simpleSelector = factory
							.createConditionalSelector(
									((ConditionalSelectorImpl) simple).getSimpleSelector(),
									andcond);
				} else {
					((CombinatorSelectorImpl) currentsel).simpleSelector = factory
							.createConditionalSelector(
									((CombinatorSelectorImpl) currentsel).simpleSelector,
									condition);
				}
			} else {
				if (currentsel != null
						&& currentsel.getSelectorType() == SelectorType.CONDITIONAL) {
					CombinatorConditionImpl andcond = (CombinatorConditionImpl) factory
							.createCondition(ConditionType.AND);
					andcond.first = ((ConditionalSelectorImpl) currentsel).getCondition();
					andcond.second = condition;
					currentsel = factory.createConditionalSelector(
							((ConditionalSelectorImpl) currentsel).getSimpleSelector(), andcond);
				} else {
					currentsel = factory.createConditionalSelector((SimpleSelector) currentsel,
							condition);
				}
			}
		}

		private boolean parsePositionalArgument(PositionalConditionImpl cond, String expression) {
			AnBExpression expr = new MyAnBExpression();
			try {
				expr.parse(expression);
			} catch (IllegalArgumentException e) {
				return false;
			}
			cond.offset = expr.getOffset();
			cond.slope = expr.getStep();
			cond.ofList = expr.getSelectorList();
			cond.hasKeyword = expr.isKeyword();
			return true;
		}

		class MyAnBExpression extends AnBExpression {

			private static final long serialVersionUID = 1L;

			@Override
			protected SelectorList parseSelector(String selText) {
				CSSParser parser = CSSParser.this.clone();
				return parser.parseSelectors(selText, factory);
			}

		}

		private String getDefaultNamespaceURI() {
			String uri = nsMap.getNamespaceURI("");
			if (uri != null && factory != nsMap) {
				factory.registerNamespacePrefix("", uri);
			}
			return uri;
		}

		private String getNamespaceURI(int index) {
			String uri;
			if (namespacePrefix.length() != 0) {
				uri = nsMap.getNamespaceURI(namespacePrefix);
				if (uri != null) {
					if (factory != nsMap) {
						factory.registerNamespacePrefix(namespacePrefix, uri);
					}
				} else if (!namespacePrefix.equals("*")) {
					handleError(index - buffer.length() - namespacePrefix.length() - 1,
							ParseHelper.ERR_UNKNOWN_NAMESPACE,
							"Unknown namespace prefix: " + namespacePrefix);
				}
			} else {
				// |E (elements without a namespace)
				uri = "";
			}
			namespacePrefix = null;
			return uri;
		}

		private ElementSelectorImpl newElementSelector(int index) {
			ElementSelectorImpl elemsel = factory.createElementSelector();
			setSimpleSelector(index, elemsel);
			return elemsel;
		}

		private void setSimpleSelector(int index, SimpleSelector simple) {
			if (currentsel instanceof CombinatorSelectorImpl) {
				((CombinatorSelectorImpl) currentsel).simpleSelector = simple;
			} else if (currentsel != null) {
				handleError(index - buffer.length(), ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected token after '" + currentsel.toString() + "': "
								+ simple.toString());
			} else {
				currentsel = simple;
			}
		}

		protected void newCombinatorSelector(int index, SelectorType type, int triggerCp) {
			if (currentsel != null && isValidCurrentSelector()) {
				newCombinatorSelector(type);
			} else {
				unexpectedCharError(index, triggerCp);
			}
		}

		void newCombinatorSelector(SelectorType type) {
			currentsel = factory.createCombinatorSelector(type, currentsel);
			stage = 0;
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (stage == STAGE_ATTR_START || stage == STAGE_ATTR_SYMBOL
					|| stage == STAGE_EXPECT_ID_OR_CLASSNAME
					|| stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT || stage == 0
					|| stage == STAGE_COMBINATOR_OR_END) {
				if (isEscapedCodepoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				} else if (Character.isISOControl(codepoint)) {
					unexpectedCharError(index, codepoint);
					return;
				} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
					Selector sel = getActiveSelector();
					if (sel.getSelectorType() == SelectorType.CONDITIONAL) {
						Condition cond = ((ConditionalSelectorImpl) sel).condition;
						cond = getActiveCondition(cond);
						if (cond.getConditionType() == ConditionType.SELECTOR_ARGUMENT) {
							buffer.append('\\');
						}
					}
				}
				bufferAppend(codepoint);
				if (stage == 0) {
					stage = 1;
				}
				prevcp = 65;
			} else {
				unexpectedCharError(index - 1, 92);
			}
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (stage == 0 && selist.size() == 0) {
				super.commented(index, commentType, comment);
			} else {
				separator(index, 12);
				if (prevcp != 44) {
					// If previous cp was a comma, we keep it
					prevcp = 12;
				}
			}
		}

		@Override
		public void endOfStream(int len) {
			processBuffer(len, 32, true);
			// Check whether we got an error in selectors
			if (!parseError && !addCurrentSelector(len)) {
				// Report EOF
				unexpectedEOFError(len);
			}
			getManager().endOfStream(len);
		}

		boolean addCurrentSelector(int index) {
			if (currentsel != null) {
				if (isValidCurrentSelector()) {
					this.selist.add(currentsel, index);
					currentsel = null;
					return true;
				} else {
					selist.clear();
					currentsel = null;
				}
			}
			return false;
		}

		boolean isValidCurrentSelector() {
			Selector last = null;
			switch (currentsel.getSelectorType()) {
			case CHILD:
			case DESCENDANT:
			case COLUMN_COMBINATOR:
			case DIRECT_ADJACENT:
			case SUBSEQUENT_SIBLING:
				last = ((CombinatorSelectorImpl) currentsel).getSecondSelector();
				break;
			case CONDITIONAL:
				Condition cond = ((ConditionalSelectorImpl) currentsel).getCondition();
				ConditionType condtype = cond.getConditionType();
				while (condtype == ConditionType.AND) {
					cond = ((CombinatorCondition) cond).getSecondCondition();
					if (cond == null) {
						return false;
					}
					condtype = cond.getConditionType();
				}
				switch (condtype) {
				case ATTRIBUTE:
					if (((AttributeConditionImpl) cond).getLocalName() == null) {
						return false;
					}
					break;
				case PSEUDO_CLASS:
				case PSEUDO_ELEMENT:
					if (((PseudoConditionImpl) cond).name == null) {
						return false;
					}
					break;
				case CLASS:
				case BEGIN_HYPHEN_ATTRIBUTE:
				case ONE_OF_ATTRIBUTE:
				case BEGINS_ATTRIBUTE:
				case ENDS_ATTRIBUTE:
				case SUBSTRING_ATTRIBUTE:
					if (((AttributeConditionImpl) cond).getValue() == null) {
						return false;
					}
					break;
				case LANG:
					if (((LangConditionImpl) cond).getLang() == null) {
						return false;
					}
					break;
				case SELECTOR_ARGUMENT:
					if (((SelectorArgumentConditionImpl) cond).getSelectors() == null) {
						return false;
					}
				default:
				}
			default:
				return true;
			}
			return last != null;
		}

		@Override
		protected void resetHandler() {
			super.resetHandler();
			stage = 0;
			functionToken = false;
			buffer.setLength(0);
			namespacePrefix = null;
			currentsel = null;
			// selist is cleared by 'error', but clearing could be needed here too
			// selist.clear();
		}

		@Override
		public void reportError(CSSParseException ex) throws CSSParseException {
			super.reportError(ex);
			selist.clear();
			resetHandler();
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			if (errCode == TokenProducer.ERR_UNEXPECTED_END_QUOTED) {
				index -= context.length() + 1;
			}
			super.error(index, errCode, context);
			currentsel = null;
			selist.clear();
		}

		class ParserSelectorListImpl extends SelectorListImpl {

			private static final long serialVersionUID = 1L;

			public boolean add(Selector sel, int index) {
				if (add(sel)) {
					return true;
				}
				if (errorHandler != null) {
					int selsz;
					try {
						selsz = sel.toString().length();
					} catch (RuntimeException e) {
						selsz = 1;
					}
					String message = "Duplicate selector in list";
					try {
						message += ": " + sel.toString();
					} catch (RuntimeException e) {
					}
					errorHandler.warning(createException(index - selsz,
							ParseHelper.WARN_DUPLICATE_SELECTOR, message));
				}
				return false;
			}

		}

	}

	private class CDOTokenHandler extends ManagerCallbackTokenHandler {

		CDOTokenHandler(HandlerManager parent) {
			super(parent);
			this.prevcp = TokenProducer.CHAR_LESS_THAN;
		}

		@Override
		void processBuffer(int index, int triggerCp) {
		}

		@Override
		public void word(int index, CharSequence word) {
			if (!equalSequences("--", word) || this.prevcp != TokenProducer.CHAR_EXCLAMATION) {
				unexpectedTokenError(index, word);
			} else {
				yieldHandling();
			}
		}

		@Override
		public void character(int index, int codePoint) {
			if (codePoint == TokenProducer.CHAR_EXCLAMATION
					&& this.prevcp == TokenProducer.CHAR_LESS_THAN) {
				this.prevcp = codePoint;
				return;
			}
			unexpectedCharError(index, codePoint);
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			unexpectedTokenError(index, comment);
		}

		@Override
		public void endOfStream(int len) {
			unexpectedEOFError(len, "EOF while processing CDO/CDC.");
			getManager().endOfStream(len);
		}

		@Override
		public void separator(int index, int codePoint) {
			unexpectedCharError(index, codePoint);
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quote) {
			unexpectedTokenError(index, quoted);
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			quoted(index, quoted, quoteCp);
		}

		@Override
		public void leftParenthesis(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
		}

		@Override
		public void leftSquareBracket(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
		}

		@Override
		public void leftCurlyBracket(int index) {
			getYieldHandler().unexpectedLeftCurlyBracketError(index);
		}

		@Override
		public void rightParenthesis(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
		}

		@Override
		public void rightSquareBracket(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
		}

		@Override
		public void rightCurlyBracket(int index) {
			unexpectedCharError(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
		}

		@Override
		public void escaped(int index, int codePoint) {
			unexpectedCharError(index, codePoint);
		}

		@Override
		void unexpectedCharError(int index, int codepoint) {
			getYieldHandler().unexpectedCharError(index, codepoint);
		}

		@Override
		void unexpectedTokenError(int index, CharSequence token) {
			getYieldHandler().unexpectedTokenError(index, token);
		}

		@Override
		public void unexpectedEOFError(int len, String message) {
			getYieldHandler().unexpectedEOFError(len, message);
		}

	}

	abstract private class ParseEndContentHandler extends DefaultTokenHandler {

		boolean foundControl = false;

		ParseEndContentHandler() {
			super();
		}

		@Override
		void processBuffer(int index, int triggerCp) {
		}

		@Override
		public void word(int index, CharSequence word) {
			reportError(index);
		}

		@Override
		public void separator(int index, int codePoint) {
		}

		@Override
		public void rightCurlyBracket(int index) {
			reportError(index);
		}

		@Override
		public void character(int index, int codePoint) {
			unexpectedCharError(index, codePoint);
		}

		@Override
		public void escaped(int index, int codePoint) {
			reportError(index);
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (!foundControl && !parseError && commentType == 0) {
				handler.comment(comment, isPreviousCpLF());
			}
		}

		@Override
		public void endOfStream(int len) {
			// handler should be checked for not null before instantiation
			handler.endOfStream();
		}

		private void reportError(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Found tokens after rule");
		}

		@Override
		public void handleError(int index, byte errCode, String message) {
			if (!isInError() && errorHandler != null) {
				handleError(createException(index, errCode, message));
			}
			setParseError();
		}

	}

	private class GenericBlockAtRuleManager extends CSSParserHandlerManager {

		GenericBlockAtRuleManager() {
			super();
		}

		void endAtRule() {
			((DeclarationRuleHandler) handler).endAtRule();
		}

		@Override
		public void endOfStream(int len) {
			super.endOfStream(len);
			endDocument();
		}

		@Override
		protected CSSTokenHandler getInitialTokenHandler() {
			return new IdentTokenHandler() {

				private String atRule = null;

				@Override
				public void character(int index, int codePoint) throws RuntimeException {
					if (codePoint == TokenProducer.CHAR_COMMERCIAL_AT
							&& prevcp != TokenProducer.CHAR_COMMERCIAL_AT) {
						prevcp = codePoint;
					} else {
						unexpectedCharError(index, codePoint);
					}
				}

				@Override
				void processBuffer(int index, int triggerCp) {
					atRule = unescapeBuffer(index);
					if (atRule.length() > 2) {
						BlockRuleTH th = new BlockRuleTH(atRule);
						yieldHandling(th);
					} else {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Malformed @-rule.");
					}
				}

				@Override
				public void leftCurlyBracket(int index) {
					processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
					if (!isInError()) {
						sendLeftCurlyBracketEvent(index, this);
					}
				}

				@Override
				public void separator(int index, int codepoint) {
					if (buffer.length() > 0) {
						processBuffer(index, codepoint);
					}
				}

				@Override
				public void commented(int index, int commentType, String comment) {
					separator(index, 12);
					if (!parseError && buffer.length() == 0 && atRule == null && commentType == 0) {
						handler.comment(comment, isPreviousCpLF());
					}
					prevcp = 12;
				}

				@Override
				public void handleErrorRecovery() {
					// Error: ignore declaration
					yieldHandling(new IgnoredDeclarationTokenHandler());
				}

				@Override
				public void endOfStream(int len) {
					if (prevcp != 32 || buffer.length() > 0) {
						unexpectedEOFError(len);
					}
					GenericBlockAtRuleManager.this.endOfStream(len);
				}

			};
		}

		/**
		 * Generic block at-rule preamble + body handler.
		 */
		private class BlockRuleTH extends DefaultTokenHandler {

			private String ruleName = null;

			BlockRuleTH(String ruleName) {
				super();
				this.ruleName = ruleName;
			}

			@Override
			protected void initializeBuffer() {
				this.buffer = new StringBuilder(64);
			}

			@Override
			public void leftCurlyBracket(int index) {
				String rulePreamble = null;
				if (buffer.length() != 0) {
					rulePreamble = unescapeBuffer(index);
				}
				if (!startAtRule(index, ruleName, rulePreamble)) {
					handleErrorRecovery();
					sendLeftCurlyBracketEvent(index, this);
				} else {
					getManager().yieldManagement(new DeclarationListManager(getManager()) {

						@Override
						public void endManagement(int index) {
							GenericBlockAtRuleManager.this.endAtRule();
							getControlHandler().yieldHandling(new RuleEndContentHandler());
						}

						@Override
						public void endOfStream(int len) {
							GenericBlockAtRuleManager.this.endAtRule();
							GenericBlockAtRuleManager.this.endOfStream(len);
						}

					});
				}
			}

			protected boolean startAtRule(int index, String ruleFirstPart, String ruleSecondPart) {
				return ((DeclarationRuleHandler) handler).startAtRule(ruleFirstPart,
						ruleSecondPart);
			}

			@Override
			public void character(int index, int codepoint) {
				bufferAppend(codepoint);
				prevcp = codepoint;
			}

			@Override
			protected void processBuffer(int index, int triggerCp) {
			}

			@Override
			public void commented(int index, int commentType, String comment) {
				if (!parseError && buffer.length() == 0 && ruleName == null && parendepth == 0
						&& commentType == 0) {
					handler.comment(comment, isPreviousCpLF());
					separator(index, 12);
					prevcp = 12;
				} else {
					separator(index, 32);
					// The above call may have left prevcp as 10
					prevcp = 32;
				}
			}

			@Override
			public void endOfStream(int len) {
				unexpectedEOFError(len);
				getManager().endOfStream(len);
			}

			@Override
			public void handleErrorRecovery() {
				// Error: ignore declaration
				yieldHandling(new IgnoredDeclarationTokenHandler());
			}

			@Override
			protected void resetHandler() {
				super.resetHandler();
				this.ruleName = null;
			}

			@Override
			public HandlerManager getManager() {
				return GenericBlockAtRuleManager.this;
			}

		}

		private class RuleEndContentHandler extends ParseEndContentHandler {

			RuleEndContentHandler() {
				super();
			}

			@Override
			public HandlerManager getManager() {
				return GenericBlockAtRuleManager.this;
			}

		}

	}

	/**
	 * Small extension to {@code CSSHandler} to deal with declaration rules.
	 */
	public interface DeclarationRuleHandler extends CSSHandler {

		/**
		 * Marks the start of a declaration rule.
		 * 
		 * @param ruleName the name of the rule.
		 * @param modifier the modifier string (the contents of whatever is after the
		 *                 rule name and before the style declaration), or
		 *                 <code>null</code> if no modifier was found.
		 * @return true if the start was successful.
		 */
		boolean startAtRule(String ruleName, String modifier);

		/**
		 * Marks the end of a declaration rule.
		 */
		void endAtRule();

	}

	/**
	 * Parse descriptors.
	 */
	abstract private class DescriptorListManager extends DeclarationListManager {

		DescriptorListManager(HandlerManager parent) {
			super(parent);
		}

		@Override
		protected ValueTokenHandler createValueTokenHandler() {
			return new DeclValueTokenHandler() {

				@Override
				protected void setPriorityHandler(int index) {
					handleError(index, ParseHelper.ERR_RULE_SYNTAX,
							"Important priorities are invalid in descriptors.");
				}

			};
		}

		@Override
		public void endManagement(int index) {
			reportRuleEnd(index);
			super.endManagement(index);
		}

		@Override
		public void endOfStream(int len) {
			reportRuleEnd(len);
			super.endOfStream(len);
		}

		abstract protected void reportRuleEnd(int index);

	}

	/**
	 * {@code <declaration-list>} manager.
	 */
	private class DeclarationListManager extends ListHandlerManager {

		String propertyName = null;

		private final ValueTokenHandler valueth;

		private boolean priorityImportant = false;

		DeclarationListManager() {
			super();
			// Handler instantiation always last
			valueth = createValueTokenHandler();
		}

		DeclarationListManager(HandlerManager parent) {
			super(parent);
			// Handler instantiation always last
			valueth = createValueTokenHandler();
		}

		protected ValueTokenHandler createValueTokenHandler() {
			return new DeclValueTokenHandler();
		}

		@Override
		protected CSSTokenHandler getInitialTokenHandler() {
			return new PropertyNameTokenHandler();
		}

		@Override
		public void restoreInitialHandler() {
			super.restoreInitialHandler();
			propertyName = null;
			priorityImportant = false;
		}

		class PropertyNameTokenHandler extends DeclarationIdentTokenHandler {

			PropertyNameTokenHandler() {
				super();
			}

			private void yieldHandling() {
				yieldHandling(valueth);
			}

			@Override
			void processBuffer(int index, int triggerCp) {
				setPropertyName(index);
				setWhitespacePrevCp();
			}

			@Override
			public void word(int index, CharSequence word) {
				if (propertyName == null) {
					super.word(index, word);
				} else {
					unexpectedTokenError(index, word);
				}
			}

			@Override
			public void escaped(int index, int codepoint) {
				if (propertyName == null) {
					super.escaped(index, codepoint);
				} else {
					unexpectedCharError(index, codepoint);
				}
			}

			@Override
			public void character(int index, int codepoint) {
				if (propertyName == null) {
					// ! 33
					// : 58
					// ; 59
					switch (codepoint) {
					case TokenProducer.CHAR_HYPHEN_MINUS: // -
					case TokenProducer.CHAR_LOW_LINE: // _
						// TokenProducer is supposed to send only isolated '-' and '_'
						buffer.append((char) codepoint);
						prevcp = 65;
						return;
					case TokenProducer.CHAR_COLON: // :
						// The property name may be in buffer
						if (buffer.length() != 0) {
							processBuffer(index, codepoint);
							if (!isInError()) {
								// Yield to next
								yieldHandling();
							}
							return;
						}
						// pass-through
					case TokenProducer.CHAR_AMPERSAND: // &
						if (buffer.length() == 0) {
							expectSelector(index);
							return;
						}
						break;
					case TokenProducer.CHAR_NUMBER_SIGN: // #
					case TokenProducer.CHAR_ASTERISK: // *
					case TokenProducer.CHAR_PLUS: // +
					case TokenProducer.CHAR_FULL_STOP: // .
					case TokenProducer.CHAR_GREATER_THAN: // >
					case TokenProducer.CHAR_TILDE: // ~
					case TokenProducer.CHAR_VERTICAL_LINE: // |
						if (buffer.length() == 0) {
							expectSelector(index, codepoint);
							return;
						}
						break;
					case TokenProducer.CHAR_COMMERCIAL_AT: // @
						if (buffer.length() == 0) {
							handleAtKeyword(index);
							return;
						}
						break;
					case TokenProducer.CHAR_SEMICOLON: // ;
						if (unexpectedSemicolonError(index)) {
							getManager().restoreInitialHandler();
						}
						resetParseError();
						return;
					default:
						break;
					}
				} else if (codepoint == TokenProducer.CHAR_COLON) {
					// Expect a value, now yield to next
					yieldHandling();
					return;
				}
				unexpectedCharError(index, codepoint);
			}

			/**
			 * Set the property name.
			 * <p>
			 * Buffer must have contents.
			 * </p>
			 * 
			 * @param index the parse index.
			 */
			private void setPropertyName(int index) {
				String raw = buffer.toString();
				if (!isEscapedIdent()) {
					if (isNotForbiddenIdentStart(raw)) {
						propertyName = raw;
						buffer.setLength(0);
						return;
					}
				} else if (isNotForbiddenIdentStart(raw)) {
					propertyName = unescapeBuffer(index);
					if (!parseError && !isValidIdentifier(propertyName)) {
						handleWarning(index - buffer.length(), ParseHelper.WARN_PROPERTY_NAME,
								"Suspicious property name: " + raw);
					}
					return;
				}
				handleError(index - buffer.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
						"Invalid property name: '" + raw + '\'');
			}

			@Override
			public void commented(int index, int commentType, String comment) {
				if (!parseError && buffer.length() == 0 && propertyName == null && parendepth == 0
						&& valueth.getSquareBracketDepth() == 0) {
					super.commented(index, commentType, comment);
					prevcp = 12;
				} else {
					separator(index, 32);
					// The above call may have left prevcp as 10
					prevcp = 32;
				}
			}

			@Override
			public void endOfStream(int len) {
				super.endOfStream(len);
				if (propertyName != null) {
					unexpectedEOFError(len);
				} else if (!getManager().isTopManager() && !isInError()) {
					handleWarning(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of stream");
				}
				DeclarationListManager.this.endOfStream(len);
			}

		}

		private class PriorityTokenHandler extends DeclarationIdentTokenHandler {

			PriorityTokenHandler() {
				super();
			}

			/**
			 * Please call this only if the buffer is not empty
			 */
			@Override
			void processBuffer(int index, int triggerCp) {
				String prio = unescapeBuffer(index);
				if ("important".equalsIgnoreCase(prio) && !priorityImportant) {
					priorityImportant = true;
				} else {
					// Possible legacy IE hack
					checkIEPrioHack(index, prio);
				}
			}

			private void checkIEPrioHack(int index, String prio) {
				String compatText;
				buffer.append('!').append(prio);
				if (parserFlags.contains(Flag.IEPRIO) && "ie".equals(prio)
						&& (compatText = valueth.setFullIdentCompat(rawBuffer())) != null) {
					valueth.warnIdentCompat(index, compatText);
				} else {
					valueth.handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Invalid priority: " + prio);
				}
			}

			@Override
			public void character(int index, int codepoint) {
				// ! 33
				// ; 59
				if (buffer.length() != 0) {
					processBuffer(index, codepoint);
					if (isInError()) {
						return;
					}
				}
				String compatText;
				switch (codepoint) {
				case TokenProducer.CHAR_SEMICOLON: // ;
					if (!priorityImportant) {
						// Previous '!' was an error unless IE
						if (!parserFlags.contains(Flag.IEPRIO) || valueth.getLexicalUnit()
								.getLexicalUnitType() != LexicalType.COMPAT_IDENT) {
							// Error recovery not needed
							valueth.reportError(index - 1, ParseHelper.ERR_UNEXPECTED_CHAR,
									"Unexpected '!'.");
							valueth.resetParseError();
							// Must reset handler as it is not reset by error recovery
							valueth.resetHandler();
							// Now reset this handler
							resetHandler();
							propertyName = null;
							// Restore property name handler
							DeclarationListManager.this.restoreInitialHandler();
							break;
						}
					}
					DeclarationListManager.this.endOfPropertyDeclaration(index);
					DeclarationListManager.this.restoreInitialHandler();
					break;
				case TokenProducer.CHAR_EXCLAMATION: // !
					if (priorityImportant && parserFlags.contains(Flag.IEPRIOCHAR)
							&& (compatText = valueth.setFullIdentCompat(rawBuffer())) != null) {
						valueth.warnIdentCompat(index, compatText);
						LexicalUnitImpl lunit = valueth.getLexicalUnit();
						lunit.setUnitType(LexicalType.COMPAT_PRIO);
						lunit.setCssUnit(CSSUnit.CSS_INVALID);
						break;
					}
					// pass-through
				default:
					valueth.unexpectedCharError(index, codepoint);
				}
			}

			@Override
			public void rightCurlyBracket(int index) {
				if (buffer.length() != 0) {
					processBuffer(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
				}
				super.rightCurlyBracket(index);
			}

			@Override
			public void endOfStream(int len) {
				super.endOfStream(len);
				DeclarationListManager.this.endOfPropertyDeclaration(len);
				if (!isInError() && DeclarationListManager.this.getParentManager() != null) {
					unexpectedEOFError(len);
				}
				DeclarationListManager.this.endOfStream(len);
			}

		}

		abstract class DeclarationIdentTokenHandler extends IdentTokenHandler {

			DeclarationIdentTokenHandler() {
				super();
			}

			@Override
			public void handleErrorRecovery() {
				DeclarationListManager.this.valueth.resetParseError();
				super.handleErrorRecovery();
			}

		}

		protected void handleAtKeyword(int index) {
			getControlHandler().getCurrentHandler().unexpectedCharError(index,
					TokenProducer.CHAR_COMMERCIAL_AT);
		}

		protected void expectSelector(int index) {
			getControlHandler().getCurrentHandler().unexpectedCharError(index,
					TokenProducer.CHAR_AMPERSAND);
		}

		protected void expectSelector(int index, int triggerCp) {
			getControlHandler().getCurrentHandler().unexpectedCharError(index, triggerCp);
		}

		protected void endOfPropertyDeclaration(int index) {
			// Buffer must have been processed before reaching this
			if (propertyName != null) {
				if (!valueth.isInError()) {
					LexicalUnitImpl lunit = valueth.getLexicalUnit();
					if (!isCustomProperty()) {
						if (lunit != null) {
							handleProperty(index, propertyName, lunit, priorityImportant);
						} else {
							getControlHandler().getCurrentHandler().handleError(index,
									ParseHelper.ERR_EXPR_SYNTAX,
									"Found property name (" + propertyName + ") but no value");
						}
					} else {
						if (lunit == null) {
							lunit = new LexicalUnitImpl(LexicalType.EMPTY);
							lunit.value = "";
						}
						handleLexicalProperty(index, propertyName, lunit, priorityImportant);
					}
					valueth.resetHandler();
				}
			}

			// Reset other state fields
			resetHandler();
		}

		private boolean isCustomProperty() {
			return propertyName.startsWith("--");
		}

		protected void handleProperty(int index, String propertyName, LexicalUnitImpl lunit,
				boolean priorityImportant) {
			getControlHandler().setCurrentLocation(index);
			handler.property(propertyName, lunit, priorityImportant);
		}

		void handleLexicalProperty(int index, String propertyName, LexicalUnitImpl lunit,
				boolean priorityImportant) {
			getControlHandler().setCurrentLocation(index);
			handler.lexicalProperty(propertyName, lunit, priorityImportant);
		}

		protected void resetHandler() {
			propertyName = null;
			priorityImportant = false;
		}

		@Override
		public void rightCurlyBracket(int index) {
			endOfPropertyDeclaration(index);
			endManagement(index);
		}

		class DeclValueTokenHandler extends BaseValueTokenHandler {

			DeclValueTokenHandler() {
				super();
			}

			@Override
			String getPropertyName() {
				return propertyName;
			}

			@Override
			public void leftCurlyBracket(int index) {
				// Process buffer first, an error could be produced already
				processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET);
				super.leftCurlyBracket(index);
			}

			@Override
			protected void setPriorityHandler(int index) {
				yieldHandling(new PriorityTokenHandler());
			}

			@Override
			protected void endOfPropertyDeclaration(int index) {
				DeclarationListManager.this.endOfPropertyDeclaration(index);
				// wake up declaration handler
				restoreInitialHandler();
			}

			@Override
			public void endOfStream(int len) {
				super.endOfStream(len);
				if (!getManager().isTopManager() && !isInError()) {
					handleWarning(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of stream");
				}
			}

			@Override
			protected boolean isCustomProperty() {
				return DeclarationListManager.this.isCustomProperty();
			}

			@Override
			public DeclarationListManager getManager() {
				return DeclarationListManager.this;
			}

		}

	}

	/**
	 * Manager that only parses values ({@code <declaration-value>}) .
	 */
	private class DeclarationValueManager extends CSSParserHandlerManager {

		private final String propertyName;

		private ValueTokenHandler valueth = new ValueOnlyTokenHandler();

		DeclarationValueManager() {
			this("");
		}

		DeclarationValueManager(HandlerManager parent) {
			super(parent);
			this.propertyName = "";
		}

		DeclarationValueManager(String propertyName) {
			super();
			this.propertyName = propertyName;
		}

		LexicalUnit getLexicalUnit() {
			return valueth.getLexicalUnit();
		}

		@Override
		protected CSSTokenHandler getInitialTokenHandler() {
			return valueth;
		}

		@Override
		protected ControlTokenHandler createControlTokenHandler() {
			return new CSSControlTokenHandler() {

				@Override
				public void tokenStart(TokenControl control) {
					super.tokenStart(control);
					yieldHandling(valueth);
					valueth.prevcp = 32; // XXX should not be necessary
				}

			};
		}

		private class ValueOnlyTokenHandler extends BaseValueTokenHandler {

			ValueOnlyTokenHandler() {
				super();
			}

			@Override
			String getPropertyName() {
				return propertyName;
			}

			@Override
			protected void endOfValue(int index) {
				unexpectedCharError(index, ';');
			}

			@Override
			public void rightCurlyBracket(int index) {
				// We aren't in declaration context
				// Process buffer first, an error could be produced already
				super.rightCurlyBracket(index);
				unexpectedCharError(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
			}

			@Override
			public void endOfStream(int len) {
				super.endOfStream(len);
				if (!isInError() && getLexicalUnit() == null) {
					handleError(len, ParseHelper.ERR_EXPR_SYNTAX, "No value found");
				}
			}

			@Override
			public void handleErrorRecovery() {
			}

			@Override
			ControlTokenHandler getControlHandler() {
				return DeclarationValueManager.this.getControlHandler();
			}

			@Override
			public HandlerManager getManager() {
				return DeclarationValueManager.this;
			}

		}

	}

	private class URLTokenHandler extends CallbackTokenHandler {

		private String url = null;

		private boolean allowModifiers;

		private LexicalUnitImpl urlUnit = null;

		private LexicalUnitImpl modifier = null;

		private boolean legacySyntax = false;

		URLTokenHandler(CSSContentHandler caller) {
			super(caller);
			parendepth = 1;
			this.allowModifiers = false;
		}

		URLTokenHandler(LexicalProvider caller) {
			super(caller);
			parendepth = 1;
			urlUnit = caller.getCurrentLexicalUnit();
			this.allowModifiers = urlUnit != null;
		}

		@Override
		protected void initializeBuffer() {
			buffer = new StringBuilder(256);
		}

		@Override
		public HandlerManager getManager() {
			return caller.getManager();
		}

		@Override
		public void word(int index, CharSequence word) {
			if (url == null || allowModifiers) {
				super.word(index, word);
			} else {
				unexpectedTokenError(index, word);
			}
		}

		@Override
		public void leftCurlyBracket(int index) {
			if (url == null || allowModifiers) {
				buffer.append('{');
			} else {
				unexpectedLeftCurlyBracketError(index);
			}
		}

		@Override
		public void rightCurlyBracket(int index) {
			appendIfValid(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
		}

		private void appendIfValid(int index, int codePoint) {
			if (url == null || allowModifiers) {
				bufferAppend(codePoint);
			} else {
				unexpectedCharError(index, codePoint);
			}
		}

		@Override
		public void leftSquareBracket(int index) {
			appendIfValid(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
		}

		@Override
		public void rightSquareBracket(int index) {
			appendIfValid(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
		}

		@Override
		void processBuffer(int index, int triggerCp) {
			if (buffer.length() > 0) {
				if (url == null) {
					legacySyntax = true;
					allowModifiers = false;
					url = rawBuffer();
				} else if (allowModifiers) {
					String mod = unescapeBuffer(index);
					LexicalUnitImpl lu = new LexicalUnitImpl(LexicalType.IDENT);
					lu.value = mod;
					addModifier(lu);
				} else {
					unexpectedTokenError(index, buffer);
				}
			}
		}

		private void addModifier(LexicalUnitImpl lu) {
			if (modifier == null) {
				modifier = lu;
				urlUnit.parameters = lu;
			} else {
				modifier.nextLexicalUnit = lu;
				lu.previousLexicalUnit = modifier;
				modifier = lu;
				lu.ownerLexicalUnit = urlUnit;
			}
		}

		@Override
		public void separator(int index, int codepoint) {
			if (isEscapedIdent() && bufferEndsWithEscapedCharOrWS(buffer)) {
				buffer.append(' ');
			} else if (url == null) {
				processBuffer(index, codepoint);
			} else if (legacySyntax) {
				unexpectedCharError(index, codepoint);
			}
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quote) {
			if (url == null) {
				url = quoted.toString();
			} else {
				unexpectedCharError(index, quote);
			}
		}

		@Override
		public void character(int index, int codePoint) {
			appendIfValid(index, codePoint);
		}

		@Override
		public void escaped(int index, int codePoint) {
			if (url != null && !allowModifiers) {
				unexpectedCharError(index, codePoint);
			} else {
				if (isEscapedCodepoint(codePoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				bufferAppend(codePoint);
			}
		}

		@Override
		public void leftParenthesis(int index) {
			parendepth++;
			if (url != null && buffer.length() > 0 && allowModifiers) {
				String mod = unescapeBuffer(index);
				LexicalUnitImpl lu = new GenericFunctionUnitImpl();
				lu.value = mod;
				addModifier(lu);
				yieldHandling(
						new ValueTokenHandler(CSSParser.this.parserFlags.contains(Flag.IEVALUES)) {

							@Override
							void decrParenDepth(int index) {
								parendepth--;
								if (parendepth < 0 && !isInError()) {
									URLTokenHandler.this.parendepth--;
									yieldHandling(URLTokenHandler.this);
								}
							}

							@Override
							public void handleErrorRecovery() {
								URLTokenHandler.this.handleErrorRecovery();
							}

							@Override
							public CSSErrorHandler getErrorHandler() {
								return URLTokenHandler.this.getErrorHandler();
							}

							@Override
							public HandlerManager getManager() {
								return URLTokenHandler.this.getManager();
							}

						});
			} else {
				unexpectedCharError(index, '(');
			}
		}

		@Override
		public void rightParenthesis(int index) {
			parendepth--;
			if (parendepth == 0) {
				processBuffer(index, ')');
				// Decrease caller parentheses depth, which must be 1 or higher,
				// otherwise this handler would have not been instantiated.
				// So we call decrParenDepth() which does not check the depth.
				caller.decrParenDepth();
				endFunctionArgument();
			}
			// Cannot reach this
		}

		private void endFunctionArgument() {
			getControlHandler().yieldHandling(caller);
			setURL(url, urlUnit);
		}

		@Override
		public void endOfStream(int len) {
			super.endOfStream(len);
			if (!isInError()) {
				caller.unexpectedEOFError(len);
			} else {
				caller.setParseError();
			}
			caller.endOfStream(len);
		}

		@Override
		protected void resetHandler() {
			super.resetHandler();
			url = null;
			urlUnit = null;
			modifier = null;
		}

		protected void setURL(String url, LexicalUnitImpl urlUnit) {
		}

	}

	static LexicalUnitImpl findLastValue(LexicalUnitImpl lu) {
		LexicalUnitImpl nextlu;
		while ((nextlu = lu.nextLexicalUnit) != null) {
			lu = nextlu;
		}
		return lu;
	}

	static boolean typeIsAlgebraicOperator(LexicalType type) {
		return type == LexicalType.OPERATOR_PLUS || type == LexicalType.OPERATOR_MINUS
				|| type == LexicalType.OPERATOR_MULTIPLY || type == LexicalType.OPERATOR_SLASH;
	}

	private void endDocument() {
		if (handler != null) {
			handler.endOfStream();
		}
	}

	abstract private class ListHandlerManager extends CSSParserHandlerManager {

		ListHandlerManager() {
			super();
		}

		ListHandlerManager(HandlerManager parent) {
			super(parent);
		}

		@Override
		public void endOfStream(int len) {
			HandlerManager parent = getParentManager();
			if (parent != null) {
				parent.endOfStream(len);
			} else {
				endDocument();
			}
		}

	}

	abstract private class CSSParserHandlerManager extends HandlerManager {

		CSSParserHandlerManager() {
			super();
		}

		CSSParserHandlerManager(HandlerManager parent) {
			super(parent);
		}

		@Override
		protected ControlTokenHandler createControlTokenHandler() {
			return new CSSControlTokenHandler();
		}

		/**
		 * Create a token producer configured for the initial stage of parsing.
		 * 
		 * @return the parser.
		 */
		@Override
		public TokenProducer createTokenProducer() {
			CharacterCheck ccheck = new IdentCharacterCheck();
			TokenProducer tp = new TokenProducer(ccheck, streamSizeLimit);
			CSSTokenHandler ini = getInitialTokenHandler();
			tp.setContentHandler(ini);
			tp.setErrorHandler(ini);
			tp.setControlHandler(getControlHandler());
			return tp;
		}

		/**
		 * Call the {@link CSSHandler#parseStart(ParserControl)} event of the handler.
		 */
		@Override
		public void parseStart() {
			handler.parseStart(getControlHandler());
		}

		@Override
		protected CSSErrorHandler getErrorHandler() {
			return errorHandler;
		}

		abstract class IdentTokenHandler extends DefaultTokenHandler {

			IdentTokenHandler() {
				super();
			}

			@Override
			protected void initializeBuffer() {
				buffer = new StringBuilder();
			}

			@Override
			public void commented(int index, int commentType, String comment) {
				separator(index, 12);
				if (buffer.length() == 0 && commentType == 0) {
					handler.comment(comment, isPreviousCpLF());
				}
				prevcp = 12;
			}

			@Override
			public void character(int index, int codePoint) throws RuntimeException {
				unexpectedCharError(index, codePoint);
			}

			@Override
			ControlTokenHandler getControlHandler() {
				return CSSParserHandlerManager.this.getControlHandler();
			}

			@Override
			public HandlerManager getManager() {
				return CSSParserHandlerManager.this;
			}

		}

		abstract class BaseValueTokenHandler extends ValueTokenHandler {

			BaseValueTokenHandler() {
				super(CSSParser.this.parserFlags.contains(Flag.IEVALUES));
			}

			@Override
			public CSSErrorHandler getErrorHandler() {
				return errorHandler;
			}

			@Override
			public void handleErrorRecovery() {
				yieldHandling(new IgnoredDeclarationTokenHandler());
			}

			@Override
			public HandlerManager getManager() {
				return CSSParserHandlerManager.this;
			}

		}

	}

	abstract private class ManagerCallbackTokenHandler extends DefaultTokenHandler {

		private HandlerManager manager;

		private CSSTokenHandler yieldHandler;

		/**
		 * Instantiate a new handler which has no manager.
		 */
		ManagerCallbackTokenHandler() {
			super();
		}

		/**
		 * Instantiate a new handler.
		 * 
		 * @param manager the manager.
		 */
		ManagerCallbackTokenHandler(HandlerManager manager) {
			super();
			setManager(manager);
		}

		/**
		 * Sets the manager that has the token control and manages handlers.
		 * 
		 * @param manager the manager.
		 */
		public void setManager(HandlerManager manager) {
			this.manager = manager;
		}

		@Override
		public HandlerManager getManager() {
			return manager;
		}

		@Override
		public void endOfStream(int len) {
			super.endOfStream(len);
			manager.endOfStream(len);
		}

		/**
		 * Set the yield handler.
		 * 
		 * @param yieldHandler the handler to yield the control when finished.
		 */
		public void setYieldHandler(CSSTokenHandler yieldHandler) {
			this.yieldHandler = yieldHandler;
		}

		CSSTokenHandler getYieldHandler() {
			return yieldHandler;
		}

		protected void yieldHandling() {
			if (yieldHandler != null) {
				yieldHandling(yieldHandler);
			}
		}

		@Override
		public void handleErrorRecovery() {
			// Error: ignore declaration
			yieldHandling(new IgnoredDeclarationTokenHandler() {

				@Override
				protected void endDeclarationBlock(int index) {
					yieldHandling(ManagerCallbackTokenHandler.this);
				}

			});
		}

	}

	/**
	 * The abstract default class for CSS token handlers that use a buffer.
	 */
	abstract private class DefaultTokenHandler extends BufferTokenHandler {

		DefaultTokenHandler() {
			super();
		}

		/**
		 * Checks the buffer for equality, and clears it.
		 * 
		 * @param lcWord the lowercase word to compare to.
		 * @return true if equals.
		 */
		boolean bufferEqualsAndClear(String lcWord) {
			if (ParseHelper.equalsIgnoreCase(buffer, lcWord)) {
				buffer.setLength(0);
				resetEscapedTokenIndex();
				return true;
			}
			return false;
		}

		boolean checkValidCustomIdent(int index, String name) {
			if ("initial".equalsIgnoreCase(name) || "inherit".equalsIgnoreCase(name)
					|| "unset".equalsIgnoreCase(name) || "none".equalsIgnoreCase(name)
					|| "reset".equalsIgnoreCase(name)) {
				handleError(index, ParseHelper.ERR_INVALID_IDENTIFIER,
						"A CSS keyword is not a valid custom ident.");
				return false;
			}
			return true;
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			separator(index, 12);
			if (commentType == 0) {
				handler.comment(comment, isPreviousCpLF());
			}
			prevcp = 12;
		}

		@Override
		public CSSErrorHandler getErrorHandler() {
			return errorHandler;
		}

		@Override
		public void handleErrorRecovery() {
			yieldHandling(new IgnoredDeclarationTokenHandler());
		}

	}

	/**
	 * The ControlHandler for CSS.
	 */
	private class ChildControlTokenHandler extends CSSControlTokenHandler {

		private final int offset;

		ChildControlTokenHandler(ControlTokenHandler copyMe, int offset) {
			super(copyMe);
			this.offset = offset;
		}

		@Override
		void setCurrentLocation(int index) {
			super.setCurrentLocation(index + offset);
		}

	}

	/**
	 * The ControlHandler for CSS.
	 */
	private class CSSControlTokenHandler extends ControlTokenHandler {

		CSSControlTokenHandler() {
			super();
		}

		CSSControlTokenHandler(ControlTokenHandler copyMe) {
			super(copyMe);
		}

		@Override
		public void setDocumentHandler(CSSHandler handler) {
			CSSParser.this.setDocumentHandler(handler);
		}

		@Override
		public void setErrorHandler(CSSErrorHandler handler) {
			CSSParser.this.setErrorHandler(handler);
		}

		@Override
		public CSSErrorHandler getErrorHandler() {
			return errorHandler;
		}

	}

}
