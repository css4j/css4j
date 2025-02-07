/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSFontFaceRule;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Abstract base class for CSS Style databases.
 *
 * @author Carlos Amengual
 */
abstract public class AbstractStyleDatabase implements StyleDatabase, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public enum FontFormat {
		TRUETYPE, OPENTYPE, EMBEDDED_OPENTYPE, SVG, WOFF, WOFF2
	}

	protected static final String DEFAULT_GENERIC_FONT_FAMILY = "serif";

	private static final TypedValue DEFAULT_INITIAL_COLOR;

	static {
		DEFAULT_INITIAL_COLOR = (TypedValue) new ValueFactory().parseProperty("#000000");
		((ColorValue) DEFAULT_INITIAL_COLOR).setSystemDefault();
	}

	private CSSTypedValue initialColor;

	public AbstractStyleDatabase() {
		super();
		initialColor = DEFAULT_INITIAL_COLOR;
	}

	@Override
	public CSSTypedValue getInitialColor() {
		return initialColor;
	}

	@Override
	public void setInitialColor(String initialColor) {
		this.initialColor = (TypedValue) new ValueFactory().parseProperty(initialColor);
		((ColorValue) this.initialColor).setSystemDefault();
	}

	@Override
	public String getDefaultGenericFontFamily() {
		return getDefaultGenericFontFamily(DEFAULT_GENERIC_FONT_FAMILY);
	}

	@Override
	public String getUsedFontFamily(CSSComputedProperties computedStyle) {
		String requestedFamily = scanFontFamilyValue(computedStyle);
		if (requestedFamily == null) {
			requestedFamily = getDefaultGenericFontFamily();
		}
		return requestedFamily;
	}

	private String scanFontFamilyValue(CSSComputedProperties style) {
		CSSValue value = style.getPropertyCSSValue("font-family");
		String requestedFamily = null;
		if (value != null) {
			if (value.getCssValueType() == CssType.LIST) {
				ValueList fontList = (ValueList) value;
				for (StyleValue item : fontList) {
					requestedFamily = stringValueOrNull(item);
					if (requestedFamily != null && isFontFamilyAvailable(requestedFamily, style)) {
						return requestedFamily;
					}
				}
			} else {
				requestedFamily = stringValueOrNull(value);
				if (requestedFamily != null && isFontFamilyAvailable(requestedFamily, style)) {
					return requestedFamily;
				}
			}
		}
		CSSComputedProperties ancStyle = style.getParentComputedStyle();
		if (ancStyle != null) {
			requestedFamily = scanFontFamilyValue(ancStyle);
		}
		return requestedFamily;
	}

	private String stringValueOrNull(CSSValue value) {
		CSSTypedValue primi;
		Type ptype;
		String s;
		if (value.getCssValueType() == CssType.TYPED
				&& ((ptype = (primi = (CSSTypedValue) value).getPrimitiveType()) == Type.STRING
						|| ptype == Type.IDENT)) {
			s = primi.getStringValue();
		} else {
			s = null;
		}
		return s;
	}

	protected boolean isFontFamilyAvailable(String requestedFamily, CSSComputedProperties style) {
		requestedFamily = requestedFamily.toLowerCase(Locale.ROOT);
		if (isFontFamilyAvailable(requestedFamily)) {
			return true;
		}
		return isFontFaceName(requestedFamily);
	}

	@Override
	public void loadFontFaceRule(CSSFontFaceRule rule) {
		String familyName = rule.getStyle().getPropertyValue("font-family");
		if (familyName == null) {
			rule.getStyleDeclarationErrorHandler().missingRequiredProperty(familyName);
			return;
		}
		familyName = familyName.toLowerCase(Locale.ROOT);
		if (!isFontFaceName(familyName)) {
			CSSStyleDeclaration decl = rule.getStyle();
			CSSValue value = decl.getPropertyCSSValue("src");
			if (value.getCssValueType() == CssType.LIST) {
				ValueList list = (ValueList) value;
				if (list.isCommaSeparated()) {
					for (StyleValue item : list) {
						if (item.getCssValueType() == CssType.LIST) {
							if (loadFont(familyName, (ValueList) item, rule)) {
								return;
							}
						} else if (loadFont(familyName, (TypedValue) item, null, rule)) {
							return;
						}
					}
				} else if (loadFont(familyName, list, rule)) {
					return;
				}
			/*
			 * The style is supposed to be computed, so all PROXY values should
			 * have been resolved already: cast to TypedValue.
			 */
			} else if (loadFont(familyName, (TypedValue) value, null, rule)) {
				return;
			}
		}
	}

	private boolean loadFont(String familyName, ValueList value, CSSFontFaceRule rule) {
		if (!value.isCommaSeparated()) {
			ValueList list = value;
			TypedValue uri = null;
			String fontFormat = null;
			for (StyleValue item : list) {
				if (item.getCssValueType() == CssType.TYPED) {
					TypedValue primi = (TypedValue) item;
					Type pType = primi.getPrimitiveType();
					LinkedCSSValueList args;
					if (pType == Type.URI || pType == Type.STRING || pType == Type.SRC) {
						if (uri == null) {
							uri = primi;
							continue;
						}
					} else if (pType == Type.FUNCTION
							&& "format".equalsIgnoreCase(primi.getStringValue())
							&& (args = ((FunctionValue) primi).getArguments()).size() == 1) {
						StyleValue arg = args.item(0);
						if (arg.getCssValueType() == CssType.TYPED) {
							primi = (TypedValue) arg;
							pType = primi.getPrimitiveType();
							if (pType == Type.STRING || pType == Type.IDENT) {
								fontFormat = primi.getStringValue();
								continue;
							}
						}
					}
				}
				errorSrc(item, rule);
				return false;
			}
			if (uri != null) {
				return loadFont(familyName, uri, fontFormat, rule);
			} else {
				errorSrc(value, rule);
				return false;
			}
		}
		errorSrc(value, rule);
		return false;
	}

	private void errorSrc(StyleValue value, CSSDeclarationRule rule) {
		CSSPropertyValueException ex = new CSSPropertyValueException("Expected primitive value.");
		ex.setValueText(value.getCssText());
		rule.getStyleDeclarationErrorHandler().wrongValue("src", ex);
	}

	private boolean loadFont(String familyName, TypedValue value, String format, CSSFontFaceRule rule) {
		Type pType = value.getPrimitiveType();
		if (pType == Type.URI) {
			String uri = value.getStringValue();
			return loadFont(familyName, format, rule, uri);
		} else if (pType == Type.SRC) {
			FunctionValue function = (FunctionValue) value;
			if (function.getArguments().size() >= 1) {
				StyleValue first = function.getArguments().getFirst();
				if (first.getPrimitiveType() == Type.STRING) {
					String uri = ((CSSTypedValue) first).getStringValue();
					return loadFont(familyName, format, rule, uri);
				}
			}
		} else if (pType == Type.FUNCTION) {
			String fname = value.getStringValue();
			FunctionValue function = (FunctionValue) value;
			if ("local".equalsIgnoreCase(fname) && function.getArguments().size() == 1) {
				StyleValue arg = function.getArguments().get(0);
				if (arg.getPrimitiveType() == Type.STRING) {
					return isFontFamilyAvailable(value.getStringValue());
				}
			}
		}
		return false;
	}

	private boolean loadFont(String familyName, String format, CSSFontFaceRule rule, String uri) {
		Node node = rule.getParentStyleSheet().getOwnerNode();

		CSSDocument doc;
		if (node.getNodeType() != Node.DOCUMENT_NODE) {
			doc = (CSSDocument) node.getOwnerDocument();
		} else {
			doc = (CSSDocument) node;
		}

		URL url;
		try {
			url = doc.getURL(uri);
		} catch (MalformedURLException e) {
			doc.getErrorHandler().ioError(uri, e);
			return false;
		}

		// Check URL safety
		if (!doc.isAuthorizedOrigin(url)) {
			doc.getErrorHandler().policyError(node, "Unauthorized URL: " + url.toExternalForm());
			return false;
		}

		InputStream is = null;
		try {
			FontFormat fontFormat = null;
			URLConnection conn = doc.openConnection(url);
			conn.setConnectTimeout(AbstractCSSStyleSheet.CONNECT_TIMEOUT);
			conn.connect();
			String conType = conn.getContentType();
			if (conType != null) {
				int scidx = conType.indexOf(';');
				if (scidx != -1) {
					conType = conType.substring(0, scidx);
				}
				conType = conType.toLowerCase(Locale.ROOT);
				fontFormat = fontFormatFromContentType(conType);
			}
			if (fontFormat == null && format != null) {
				fontFormat = fontFormatFromRule(format.toLowerCase(Locale.ROOT));
			}
			is = conn.getInputStream();
			loadFontFace(familyName, fontFormat, is, rule);
			return true;
		} catch (IOException e) {
			doc.getErrorHandler().ioError(url.toExternalForm(), e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}

		return false;
	}

	protected FontFormat fontFormatFromContentType(String conType) {
		FontFormat fontFormat;
		if ("application/font-ttf".equals(conType) || "application/x-font-ttf".equals(conType)
				|| "application/font-sfnt".equals(conType) || "font/ttf".equals(conType)) {
			fontFormat = FontFormat.TRUETYPE;
		} else if ("application/font-woff".equals(conType) || "application/x-font-woff".equals(conType)
				|| "font/woff".equals(conType)) {
			fontFormat = FontFormat.WOFF;
		} else if ("application/font-woff2".equals(conType) || "font/woff2".equals(conType)) {
			fontFormat = FontFormat.WOFF2;
		} else if ("application/font-opentype".equals(conType) || "application/x-font-opentype".equals(conType)
				|| "application/vnd.ms-opentype".equals(conType) || "font/otf".equals(conType)
				|| "font/opentype".equals(conType)) {
			fontFormat = FontFormat.OPENTYPE;
		} else if ("application/vnd.ms-fontobject".equals(conType) || "font/eot".equals(conType)) {
			fontFormat = FontFormat.EMBEDDED_OPENTYPE;
		} else if ("image/svg+xml".equals(conType)) {
			fontFormat = FontFormat.SVG;
		} else {
			fontFormat = null;
		}
		return fontFormat;
	}

	protected FontFormat fontFormatFromRule(String format) {
		FontFormat fontFormat;
		if ("truetype".equals(format)) {
			fontFormat = FontFormat.TRUETYPE;
		} else if ("woff".equals(format)) {
			fontFormat = FontFormat.WOFF;
		} else if ("woff2".equals(format)) {
			fontFormat = FontFormat.WOFF2;
		} else if ("opentype".equals(format)) {
			fontFormat = FontFormat.OPENTYPE;
		} else if ("embedded-opentype".equals(format)) {
			fontFormat = FontFormat.EMBEDDED_OPENTYPE;
		} else if ("opentype".equals(format)) {
			fontFormat = FontFormat.OPENTYPE;
		} else if ("svg".equals(format)) {
			fontFormat = FontFormat.SVG;
		} else {
			fontFormat = null;
		}
		return fontFormat;
	}

	protected boolean loadFontFace(String familyName, FontFormat fontFormat, InputStream is,
			CSSFontFaceRule rule) throws IOException {
		return false;
	}

	abstract protected boolean isFontFamilyAvailable(String fontFamily);

}
