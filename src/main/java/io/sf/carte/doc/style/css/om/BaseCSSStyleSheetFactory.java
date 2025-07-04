/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.agent.HeadlessDeviceFactory;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.impl.MediaListAccess;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Parser.Flag;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.util.agent.AgentUtil;

/**
 * Base class for CSS style sheet factories.
 *
 * @author Carlos Amengual
 */
abstract public class BaseCSSStyleSheetFactory extends AbstractCSSStyleSheetFactory {

	private static final long serialVersionUID = 1L;

	/**
	 * Default user agent style sheet.
	 */
	private static final String HTML_DEFAULT_SHEET = "/io/sf/carte/doc/style/css/html.css";

	/**
	 * Default user agent style sheet for Quirks mode.
	 */
	private static final String HTML_DEFAULT_SHEET_QUIRKS = "/io/sf/carte/doc/style/css/html-quirks.css";

	private ClassLoader classLoader = null;

	private StyleFormattingFactory formattingFactory;

	private final EnumSet<Parser.Flag> parserFlags;

	private short flags = 0;

	private boolean lenientSystemValues = true;

	private AbstractCSSStyleSheet userImportantSheet = null;

	private AbstractCSSStyleSheet userNormalSheet = null;

	private DeviceFactory deviceFactory = null;

	protected BaseCSSStyleSheetFactory() {
		this(EnumSet.noneOf(Parser.Flag.class));
	}

	protected BaseCSSStyleSheetFactory(EnumSet<Parser.Flag> parserFlags) {
		super();
		this.parserFlags = parserFlags;
		// An empty device factory is default
		// You should provide a real one.
		setDeviceFactory(new HeadlessDeviceFactory());
		setStyleFormattingFactory(createDefaultStyleFormattingFactory());
	}

	protected StyleFormattingFactory createDefaultStyleFormattingFactory() {
		return new DefaultStyleFormattingFactory();
	}

	/**
	 * Creates a Document CSS style sheet for all media.
	 * <p>
	 * 
	 * @param origin
	 *            the origin of the sheet (like
	 *            {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#ORIGIN_AUTHOR author},
	 *            {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#ORIGIN_USER_AGENT user
	 *            agent}, etc.)
	 * @return the style sheet.
	 */
	abstract protected BaseDocumentCSSStyleSheet createDocumentStyleSheet(int origin);

	/**
	 * Creates a stand-alone author style sheet.
	 * <p>
	 * 
	 * @param title
	 *            the advisory title.
	 * @param media
	 *            the target media for the style sheet.
	 * @return the style sheet.
	 */
	@Override
	public AbstractCSSStyleSheet createStyleSheet(String title, MediaQueryList media) {
		if (media == null) {
			media = CSSValueMediaQueryFactory.getAllMediaInstance();
		}
		return createLinkedStyleSheet(null, title, media);
	}

	/**
	 * Creates a CSS style sheet owned by a CSS rule.
	 * <p>
	 * 
	 * @param ownerRule
	 *            the owner rule.
	 * @param title
	 *            the advisory title.
	 * @param mediaList
	 *            the list of target media for style.
	 * @return the style sheet.
	 */
	@Override
	abstract protected AbstractCSSStyleSheet createRuleStyleSheet(AbstractCSSRule ownerRule, String title,
			MediaQueryList mediaList);

	/**
	 * Sets the class loader to be used when loading internal style sheets.
	 * 
	 * @param loader
	 *            the class loader.
	 */
	public void setClassLoader(ClassLoader loader) {
		classLoader = loader;
	}

	@Override
	public CSSPropertyDefinition createPropertyDefinition(String name, CSSValueSyntax syntax, boolean inherited,
			LexicalUnit initialValue) throws DOMException {
		if (name == null) {
			throw new NullPointerException("Null name.");
		}
		if (syntax == null) {
			throw new NullPointerException("Null syntax.");
		}
		if (initialValue == null) {
			if (syntax.getCategory() != Category.universal) {
				throw new DOMInvalidAccessException("Null initial value.");
			}
		} else if (initialValue.matches(syntax) != Match.TRUE) {
			throw new DOMInvalidAccessException(
					"Value " + initialValue.getCssText() + " does not match syntax " + syntax.toString() + '.');
		}

		PropertyDefinition def = new PropertyDefinition(name, syntax, inherited, initialValue);
		return def;
	}

	@Override
	public StyleFormattingFactory getStyleFormattingFactory() {
		return formattingFactory;
	}

	@Override
	public void setStyleFormattingFactory(StyleFormattingFactory factory) {
		formattingFactory = factory;
	}

	/**
	 * Sets the CSS style sheet defined by the end user.
	 * <p>
	 * The sheet in the supplied reader should contain user preferences, and will be
	 * appropriately merged with the other style sheets.
	 * </p>
	 *
	 * @param url the URL of the style sheet. If {@code null}, the call is
	 *            equivalent to {@link #setUserStyleSheet(Reader)}.
	 * @param re  the reader with the user style sheet. If {@code null}, a
	 *            connection to the URL shall be opened. If both arguments are
	 *            {@code null}, the user style sheet shall be cleared.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if there is a problem opening the URL or reading the
	 *                      reader.
	 */
	@Override
	public void setUserStyleSheet(String url, Reader re) throws DOMException, IOException {
		if (re != null) {
			loadUserStyleSheet(url, re);
		} else if (url != null) {
			URI uri;
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				throw new MalformedURLException(e.getMessage());
			}
			if (!uri.isAbsolute()) {
				throw new MalformedURLException("Cannot convert relative URI " + uri);
			}
			URL href = uri.toURL();
			URLConnection uconn = href.openConnection();
			String conType = uconn.getContentType();
			String contentEncoding = uconn.getContentEncoding();
			try (InputStream is = uconn.getInputStream();
					Reader reader = AgentUtil.inputStreamToReader(is, conType, contentEncoding,
							StandardCharsets.UTF_8)) {
				loadUserStyleSheet(href.toExternalForm(), reader);
			}
		} else {
			this.userImportantSheet = null;
			this.userNormalSheet = null;
		}
	}

	private void loadUserStyleSheet(String url, Reader re) throws DOMException, IOException {
		BaseDocumentCSSStyleSheet cssSheet = createDocumentStyleSheet(ORIGIN_USER);
		cssSheet.parseStyleSheet(re);
		this.userImportantSheet = createDocumentStyleSheet(ORIGIN_USER_IMPORTANT);
		this.userNormalSheet = createDocumentStyleSheet(ORIGIN_USER);
		this.userImportantSheet.setHref(url);
		this.userNormalSheet.setHref(url);
		userNormalSheet.getCssRules().ensureCapacity(cssSheet.getCssRules().getLength());
		cssSheet.prioritySplit(userImportantSheet, userNormalSheet);
	}

	/**
	 * Sets the CSS style sheet defined by the end user.
	 * <p>
	 * The sheet in the supplied reader should contain user preferences, and will be
	 * appropriately merged with the other style sheets.
	 * </p>
	 *
	 * @param re the reader with the user style sheet. If {@code null}, the user
	 *           style sheet shall be cleared.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if there is a problem retrieving the reader.
	 */
	@Override
	public void setUserStyleSheet(Reader re) throws DOMException, IOException {
		setUserStyleSheet(null, re);
	}

	@Override
	protected AbstractCSSStyleSheet getUserImportantStyleSheet() {
		return userImportantSheet;
	}

	@Override
	protected AbstractCSSStyleSheet getUserNormalStyleSheet() {
		return userNormalSheet;
	}

	/**
	 * Returns a default user agent style sheet for (X)HTML documents, strict mode.
	 * 
	 * @return the default style sheet for HTML.
	 * @throws IllegalStateException
	 *             if a problem arose when reading the default sheet.
	 */
	protected BaseDocumentCSSStyleSheet htmlDefaultSheet() throws IllegalStateException {
		InputStream is = loadCSSfromClasspath(HTML_DEFAULT_SHEET);
		try {
			return loadDefaultSheet(is);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the default style sheet", e);
		}
	}

	/**
	 * Returns a default user agent style sheet for (X)HTML documents, quirks mode.
	 * 
	 * @return the default style sheet for HTML.
	 * @throws IllegalStateException
	 *             if a problem arose when reading the default sheet.
	 */
	protected BaseDocumentCSSStyleSheet htmlQuirksDefaultSheet() throws IllegalStateException {
		InputStream is = loadCSSfromClasspath(HTML_DEFAULT_SHEET_QUIRKS);
		try {
			return loadDefaultSheet(is);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the default style sheet", e);
		}
	}

	/**
	 * Loads a default CSS style sheet from an input stream.
	 * 
	 * @return the default style sheet.
	 * @throws IOException
	 *             if a problem arose when reading the default sheet.
	 */
	private BaseDocumentCSSStyleSheet loadDefaultSheet(InputStream is) throws IOException {
		BaseDocumentCSSStyleSheet cssSheet;
		Reader re = null;
		try {
			re = new InputStreamReader(is, "UTF-8");
			cssSheet = createDocumentStyleSheet(ORIGIN_USER_AGENT);
			cssSheet.parseStyleSheet(re, CSSStyleSheet.COMMENTS_IGNORE);
		} catch (IOException e) {
			throw e;
		} finally {
			if (re != null) {
				try {
					re.close();
				} catch (IOException e) {
				}
			}
		}
		return cssSheet;
	}

	private InputStream loadCSSfromClasspath(final String cssFilename) {
		if (classLoader != null) {
			return classLoader.getResourceAsStream(
					resourcePath(BaseCSSStyleSheetFactory.class.getPackage().getName(), cssFilename));
		} else {
			return BaseCSSStyleSheetFactory.class.getResourceAsStream(cssFilename);
		}
	}

	private static String resourcePath(String clase, String filename) {
		return '/' + clase.replace('.', '/') + '/' + filename;
	}

	@Override
	protected ValueFactory getValueFactory() {
		return new ValueFactory(flags);
	}

	@Override
	public void setFactoryFlag(short flag) {
		if (flag < 0x3) {
			this.flags &= (short) 0x7c;
			this.flags |= flag;
		} else {
			flag |= this.flags & (short) 3;
			this.flags = flag;
		}
	}

	@Override
	protected boolean hasFactoryFlag(short flag) {
		return (flags & flag) == flag;
	}

	/**
	 * <p>
	 * The style sheet factories provide system default values, and its behaviour depends on a
	 * <code>lenientSystemValues</code> flag. The lenient flag allows to compute values by
	 * giving reasonable defaults to properties with system-dependent initial values:
	 * '#000000' for color and 'serif' for font-family.
	 * <p>
	 * If you work with style databases you do not need <code>lenientSystemValues</code>.
	 * <p>
	 * Default value is <code>true</code>.
	 * 
	 * @param lenient
	 *            <code>true</code> or <code>false</code> to enable/disable the
	 *            <code>lenientSystemValues</code> flag.
	 */
	@Override
	public void setLenientSystemValues(boolean lenient) {
		this.lenientSystemValues = lenient;
	}

	/**
	 * Get a system default value for the given property.
	 * <p>
	 * If the <code>lenientSystemValues</code> flag is set, returns a reasonable default for the
	 * property. Otherwise, it returns a system default meta-value.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @return the system default css primitive value.
	 */
	@Override
	public TypedValue getSystemDefaultValue(String propertyName) {
		if (lenientSystemValues) {
			TypedValue value = null;
			propertyName = propertyName.toLowerCase(Locale.ROOT);
			if ("color".equals(propertyName) || propertyName.endsWith("-color")) {
				value = (TypedValue) new ValueFactory().parseProperty("#000");
				((ColorValue) value).setSystemDefault();
				return value;
			} else if ("font-family".equals(propertyName)) {
				value = (TypedValue) new ValueFactory().parseProperty("serif");
				return new LenientSystemDefaultValue(value);
			}
		}
		return SystemDefaultValue.getInstance();
	}

	@Override
	protected Parser createSACParser() {
		Parser parser = new CSSOMParser();
		EnumSet<Parser.Flag> flags = getParserFlags();
		for (Parser.Flag flag : flags) {
			parser.setFlag(flag);
		}
		return parser;
	}

	/**
	 * Get the parser flags that should be used by NSAC parsers.
	 * 
	 * @return the NSAC parser flags.
	 */
	@Override
	protected EnumSet<Parser.Flag> getParserFlags() {
		return parserFlags;
	}

	/**
	 * Set the given parser flag.
	 * 
	 * @param flag the flag.
	 * @return {@code true} if the flag was newly set, {@code false} if the flag was
	 *         already set.
	 */
	@Override
	public boolean setFlag(Parser.Flag flag) {
		return parserFlags.add(flag);
	}

	/**
	 * Unset the given parser flag.
	 * 
	 * @param flag the flag.
	 * @return {@code true} if the flag was unset, {@code false} if the flag was not
	 *         set.
	 */
	@Override
	public boolean unsetFlag(Parser.Flag flag) {
		return parserFlags.remove(flag);
	}

	@Override
	protected boolean hasCompatValueFlags() {
		EnumSet<Flag> flags = parserFlags;
		return flags.contains(Parser.Flag.IEVALUES) || flags.contains(Parser.Flag.IEPRIO)
				|| flags.contains(Parser.Flag.IEPRIOCHAR);
	}

	@Override
	public StyleDeclarationErrorHandler createInlineStyleErrorHandler(CSSElement owner) {
		return new DefaultStyleDeclarationErrorHandler();
	}

	/**
	 * Create a new StyleDeclarationErrorHandler for the given style rule.
	 * 
	 * @param rule
	 *            the declaration rule the handler is for.
	 * @return the StyleDeclarationErrorHandler.
	 */
	@Override
	public StyleDeclarationErrorHandler createStyleDeclarationErrorHandler(CSSDeclarationRule rule) {
		return new DefaultStyleDeclarationErrorHandler();
	}

	/**
	 * Creates a style sheet error handler.
	 * @param sheet the sheet that will be handled.
	 * 
	 * @return the error handler.
	 */
	@Override
	public SheetErrorHandler createSheetErrorHandler(CSSStyleSheet<? extends CSSRule> sheet) {
		return new DefaultSheetErrorHandler(sheet);
	}

	/**
	 * Parses <code>mediaQueryString</code> and creates a new media query list.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return a new media list for <code>mediaQueryString</code>.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was reached.
	 */
	@Override
	public MediaQueryList createMediaQueryList(String mediaQueryString, Node owner) throws CSSBudgetException {
		if (mediaQueryString == null) {
			return new MediaQueryListImpl();
		}
		return parseMediaQueryList(mediaQueryString, owner);
	}

	/**
	 * Parses and creates an unmodifiable media query list for the given media.
	 * 
	 * @param media
	 *            the comma-separated list of media. If <code>null</code>, the
	 *            media list will be for all media.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return the unmodifiable media list.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was reached.
	 */
	@Override
	public MediaQueryList createImmutableMediaQueryList(String media, Node owner) throws CSSBudgetException {
		if (media == null) {
			return CSSValueMediaQueryFactory.getAllMediaInstance();
		}
		return ((MediaListAccess) parseMediaQueryList(media, owner)).unmodifiable();
	}

	/**
	 * Parses and creates a new media query list for <code>mediaQueryString</code>.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return a new media query list for <code>mediaQueryString</code>.
	 * @throws CSSBudgetException if a hard-coded limit in nested expressions was reached.
	 */
	MediaQueryList parseMediaQueryList(String mediaQueryString, Node owner) throws CSSBudgetException {
		Parser parser = new CSSOMParser();
		if (getParserFlags().contains(Parser.Flag.IEVALUES)) {
			parser.setFlag(Parser.Flag.IEVALUES);
		}
		return parser.parseMediaQueryList(mediaQueryString, owner);
	}

	/**
	 * Gets the device factory.
	 * 
	 * @return the device factory.
	 */
	@Override
	public DeviceFactory getDeviceFactory() {
		return deviceFactory;
	}

	/**
	 * Sets the device factory.
	 * 
	 * @param deviceFactory
	 *            the device factory.
	 */
	public void setDeviceFactory(DeviceFactory deviceFactory) {
		this.deviceFactory = deviceFactory;
	}

}
