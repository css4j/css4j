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

import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.agent.HeadlessDeviceFactory;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.doc.style.css.SACParserFactory;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.nsac.Parser2.Flag;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
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

	private final EnumSet<Parser2.Flag> parserFlags;

	private byte flags = 0;

	private boolean lenientSystemValues = true;

	private AbstractCSSStyleSheet userImportantSheet = null;

	private AbstractCSSStyleSheet userNormalSheet = null;

	private DeviceFactory deviceFactory = null;

	protected BaseCSSStyleSheetFactory() {
		this(EnumSet.noneOf(Parser2.Flag.class));
	}

	protected BaseCSSStyleSheetFactory(EnumSet<Parser2.Flag> parserFlags) {
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
			media = MediaList.createUnmodifiable();
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
				org.w3c.css.sac.InputSource source = new org.w3c.css.sac.InputSource(re);
				cssSheet = createDocumentStyleSheet(ORIGIN_USER);
				cssSheet.parseCSSStyleSheet(source);
			} catch (IOException e) {
				throw e;
			}
			this.userImportantSheet = createDocumentStyleSheet(ORIGIN_USER_IMPORTANT);
			this.userNormalSheet = createDocumentStyleSheet(ORIGIN_USER);
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
		AbstractCSSStyleDeclaration userImportantStyle = cssSheet.createCSSStyleDeclaration();
		AbstractCSSStyleDeclaration userNormalStyle = cssSheet.createCSSStyleDeclaration();
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
					CSSStyleDeclarationRule newrule = importantSheet.createCSSStyleRule();
					newrule.setSelectorList(rule.getSelectorList());
					BaseCSSStyleDeclaration style = (BaseCSSStyleDeclaration) newrule.getStyle();
					style.setProperties((BaseCSSStyleDeclaration) userImportantStyle);
					importantSheet.addRule(newrule);
				}
				if (!userNormalStyle.isEmpty()) {
					CSSStyleDeclarationRule newrule = normalSheet.createCSSStyleRule();
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
			InputSource source = new InputSource(re);
			cssSheet = createDocumentStyleSheet(ORIGIN_USER_AGENT);
			cssSheet.parseCSSStyleSheet(source, true);
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
	public PrimitiveValue getSystemDefaultValue(String propertyName) {
		if (lenientSystemValues) {
			CSSPrimitiveValue2 value = null;
			propertyName = propertyName.toLowerCase(Locale.ROOT);
			if ("color".equals(propertyName) || propertyName.endsWith("-color")) {
				value = (CSSPrimitiveValue2) new ValueFactory().parseProperty("#000000");
				((ColorValue) value).setSystemDefault();
			} else if ("font-family".equals(propertyName)) {
				value = (CSSPrimitiveValue2) new ValueFactory().parseProperty("serif");
			}
			if (value != null) {
				return new SafeSystemDefaultValue(value);
			}
		}
		return SystemDefaultValue.getInstance();
	}

	private class SafeSystemDefaultValue extends SystemDefaultValue {

		private CSSPrimitiveValue2 defvalue;

		SafeSystemDefaultValue(CSSPrimitiveValue2 defvalue) {
			super();
			this.defvalue = defvalue;
		}

		@Override
		public boolean isSystemDefault() {
			return false;
		}

		@Override
		public String getCssText() {
			return defvalue.getCssText();
		}

		@Override
		public short getPrimitiveType() {
			return defvalue.getPrimitiveType();
		}

		@Override
		public String getStringValue() throws DOMException {
			return defvalue.getStringValue();
		}

		@Override
		public RGBAColor getRGBColorValue() throws DOMException {
			return defvalue.getRGBColorValue();
		}

		@Override
		public float getFloatValue(short unitType) throws DOMException {
			return defvalue.getFloatValue(unitType);
		}
	}

	/**
	 * Create a SAC Parser specified by the system property
	 * <code>org.w3c.css.sac.parser</code>.
	 * <p>
	 * If that property is not set, the instantiation of a default parser will be attempted.
	 * <p>
	 * If the parser is NSAC 1.1 compliant, the NSAC flags will be enabled.
	 * 
	 * @return the SAC parser.
	 * @throws DOMException
	 *             NOT_SUPPORTED_ERR if the Parser could not be instantiated.
	 */
	@Override
	protected Parser createSACParser() throws DOMException {
		Parser parser = SACParserFactory.createSACParser();
		if (parser instanceof Parser2) {
			Parser2 parser2 = (Parser2) parser;
			EnumSet<Parser2.Flag> flags = getParserFlags();
			for (Parser2.Flag flag : flags) {
				parser2.setFlag(flag);
			}
		}
		return parser;
	}

	/**
	 * Get the parser flags that should be used by NSAC parsers.
	 * 
	 * @return the NSAC parser flags.
	 */
	@Override
	protected EnumSet<Parser2.Flag> getParserFlags() {
		return parserFlags;
	}

	@Override
	protected boolean hasCompatValueFlags() {
		EnumSet<Flag> flags = parserFlags;
		return flags.contains(Parser2.Flag.IEVALUES) || flags.contains(Parser2.Flag.IEPRIO)
				|| flags.contains(Parser2.Flag.IEPRIOCHAR);
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
