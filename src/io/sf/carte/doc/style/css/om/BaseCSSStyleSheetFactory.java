/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Locale;

import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.agent.HeadlessDeviceFactory;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Parser.Flag;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Base class for CSS style sheet factories.
 *
 * @author Carlos Amengual
 */
abstract public class BaseCSSStyleSheetFactory extends AbstractCSSStyleSheetFactory {

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

	private byte flags = 0;

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
	abstract protected BaseDocumentCSSStyleSheet createDocumentStyleSheet(byte origin);

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
			media = MediaQueryListImpl.createUnmodifiable();
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
	public StyleFormattingFactory getStyleFormattingFactory() {
		return formattingFactory;
	}

	@Override
	public void setStyleFormattingFactory(StyleFormattingFactory factory) {
		formattingFactory = factory;
	}

	/**
	 * Sets the CSS style sheet defined by the end-user.
	 * <p>
	 * The sheet in the supplied reader should contain user preferences, and
	 * will be appropriately merged with the other style sheets.
	 * </p>
	 * 
	 * @param re
	 *            the reader with the user style sheet.
	 * @throws IOException
	 *             if there is a problem retrieving the reader.
	 */
	@Override
	public void setUserStyleSheet(Reader re) throws IOException {
		if (re != null) {
			AbstractCSSStyleSheet cssSheet;
			try {
				cssSheet = createDocumentStyleSheet(ORIGIN_USER);
				cssSheet.parseStyleSheet(re);
			} catch (IOException e) {
				throw e;
			}
			this.userImportantSheet = createDocumentStyleSheet(ORIGIN_USER_IMPORTANT);
			this.userNormalSheet = createDocumentStyleSheet(ORIGIN_USER);
			userNormalSheet.getCssRules().ensureCapacity(cssSheet.getCssRules().getLength());
			prioritySplit(cssSheet, userImportantSheet, userNormalSheet);
		} else {
			this.userImportantSheet = null;
			this.userNormalSheet = null;
		}
	}

	@Override
	protected AbstractCSSStyleSheet getUserImportantStyleSheet() {
		return userImportantSheet;
	}

	@Override
	protected AbstractCSSStyleSheet getUserNormalStyleSheet() {
		return userNormalSheet;
	}

	static void prioritySplit(AbstractCSSStyleSheet cssSheet, AbstractCSSStyleSheet importantSheet,
			AbstractCSSStyleSheet normalSheet) {
		AbstractCSSStyleDeclaration userImportantStyle = cssSheet.createStyleDeclaration();
		AbstractCSSStyleDeclaration userNormalStyle = cssSheet.createStyleDeclaration();
		CSSRuleArrayList rules = cssSheet.getCssRules();
		int rl = rules.getLength();
		for (int i = 0; i < rl; i++) {
			AbstractCSSRule r = rules.item(i);
			if (r.getType() == CSSRule.STYLE_RULE) {
				userImportantStyle.clear();
				userNormalStyle.clear();
				StyleRule rule = (StyleRule) r;
				AbstractCSSStyleDeclaration st = rule.getStyle();
				st.prioritySplit(userImportantStyle, userNormalStyle);
				if (!userImportantStyle.isEmpty()) {
					CSSStyleDeclarationRule newrule = importantSheet.createStyleRule();
					newrule.setSelectorList(rule.getSelectorList());
					BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) newrule.getStyle();
					style.setProperties((BaseCSSStyleDeclaration) userImportantStyle);
					importantSheet.addRule(newrule);
				}
				if (!userNormalStyle.isEmpty()) {
					CSSStyleDeclarationRule newrule = normalSheet.createStyleRule();
					newrule.setSelectorList(rule.getSelectorList());
					BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) newrule.getStyle();
					style.setProperties((BaseCSSStyleDeclaration) userNormalStyle);
					normalSheet.addRule(newrule);
				}
			} else {
				normalSheet.addRule(r.clone(normalSheet));
			}
		}
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
			cssSheet.parseStyleSheet(re, ExtendedCSSStyleSheet.COMMENTS_IGNORE);
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
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				if (classLoader != null) {
					return classLoader.getResourceAsStream(
							resourcePath(BaseCSSStyleSheetFactory.class.getPackage().getName(), cssFilename));
				} else {
					return this.getClass().getResourceAsStream(cssFilename);
				}
			}
		});
	}

	private static String resourcePath(String clase, String filename) {
		return '/' + clase.replace('.', '/') + '/' + filename;
	}

	@Override
	protected ValueFactory getValueFactory() {
		return new ValueFactory(flags);
	}

	@Override
	public void setFactoryFlag(byte flag) {
		if (flag < 0x3) {
			this.flags &= (byte) 0x7c;
			this.flags |= flag;
		} else {
			flag |= this.flags & (byte) 3;
			this.flags = flag;
		}
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
				value = (TypedValue) new ValueFactory().parseProperty("#000000");
				((ColorValue) value).setSystemDefault();
			} else if ("font-family".equals(propertyName)) {
				value = (TypedValue) new ValueFactory().parseProperty("serif");
			}
			if (value != null) {
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
	public SheetErrorHandler createSheetErrorHandler(ExtendedCSSStyleSheet<? extends ExtendedCSSRule> sheet) {
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
	 */
	@Override
	public MediaQueryList createMediaQueryList(String mediaQueryString, Node owner) {
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
	 */
	@Override
	public MediaQueryList createImmutableMediaQueryList(String media, Node owner) {
		if (media == null) {
			return MediaQueryListImpl.createUnmodifiable();
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
	 */
	MediaQueryList parseMediaQueryList(String mediaQueryString, Node owner) {
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
