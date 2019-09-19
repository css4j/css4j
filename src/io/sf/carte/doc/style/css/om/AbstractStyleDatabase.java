/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSFontFaceRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Abstract base class for CSS Style databases.
 *
 * @author Carlos Amengual
 */
abstract public class AbstractStyleDatabase implements StyleDatabase {

	public enum FontFormat {
		TRUETYPE, OPENTYPE, EMBEDDED_OPENTYPE, SVG, WOFF, WOFF2
	}

	protected final String DEFAULT_GENERIC_FONT_FAMILY = "serif";

	private static final PrimitiveValue DEFAULT_INITIAL_COLOR;

	static {
		DEFAULT_INITIAL_COLOR = (PrimitiveValue) new ValueFactory().parseProperty("#000000");
		((ColorValue) DEFAULT_INITIAL_COLOR).setSystemDefault();
	}

	private CSSPrimitiveValue initialColor;

	public AbstractStyleDatabase() {
		super();
		initialColor = DEFAULT_INITIAL_COLOR;
	}

	@Override
	public float getExSizeInPt(String familyName, float size) {
		return Math.round(0.5f * size);
	}

	@Override
	public CSSPrimitiveValue getInitialColor() {
		return initialColor;
	}

	@Override
	public void setInitialColor(String initialColor) {
		this.initialColor = (PrimitiveValue) new ValueFactory().parseProperty(initialColor);
		((ColorValue) this.initialColor).setSystemDefault();
	}

	@Override
	public String getDefaultGenericFontFamily() {
		return getDefaultGenericFontFamily(DEFAULT_GENERIC_FONT_FAMILY);
	}

	@Override
	public String getSystemFontDeclaration(String systemFontName) {
		return null;
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
		ExtendedCSSValue value = style.getPropertyCSSValue("font-family");
		String requestedFamily = null;
		if (value != null) {
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList fontList = (ValueList) value;
				Iterator<StyleValue> it = fontList.iterator();
				while (it.hasNext()) {
					StyleValue item = it.next();
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

	private String stringValueOrNull(ExtendedCSSValue value) {
		CSSPrimitiveValue primi;
		short ptype;
		String s;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((ptype = (primi = (CSSPrimitiveValue) value).getPrimitiveType()) == CSSPrimitiveValue.CSS_STRING
						|| ptype == CSSPrimitiveValue.CSS_IDENT)) {
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
	public void loadFontFaceRule(ExtendedCSSFontFaceRule rule) {
		String familyName = rule.getStyle().getPropertyValue("font-family");
		if (familyName == null) {
			rule.getStyleDeclarationErrorHandler().missingRequiredProperty(familyName);
			return;
		}
		familyName = familyName.toLowerCase(Locale.ROOT);
		if (!isFontFaceName(familyName)) {
			ExtendedCSSStyleDeclaration decl = rule.getStyle();
			ExtendedCSSValue value = decl.getPropertyCSSValue("src");
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList list = (ValueList) value;
				if (list.isCommaSeparated()) {
					Iterator<StyleValue> it = list.iterator();
					while (it.hasNext()) {
						StyleValue item = it.next();
						if (item.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
							if (loadFont(familyName, (ValueList) item, rule)) {
								return;
							}
						} else if (loadFont(familyName, (PrimitiveValue) item, null, rule)) {
							return;
						}
					}
				} else if (loadFont(familyName, list, rule)) {
					return;
				}
			} else if (loadFont(familyName, (PrimitiveValue) value, null, rule)) {
				return;
			}
		}
	}

	private boolean loadFont(String familyName, ValueList value, ExtendedCSSFontFaceRule rule) {
		if (!value.isCommaSeparated()) {
			ValueList list = value;
			Iterator<StyleValue> it = list.iterator();
			PrimitiveValue uri = null;
			String fontFormat = null;
			while (it.hasNext()) {
				StyleValue item = it.next();
				if (item.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					PrimitiveValue primi = (PrimitiveValue) item;
					short pType = primi.getPrimitiveType();
					LinkedCSSValueList args;
					if (pType == CSSPrimitiveValue.CSS_URI || pType == CSSPrimitiveValue.CSS_STRING) {
						if (uri == null) {
							uri = primi;
							continue;
						}
					} else if (pType == CSSPrimitiveValue2.CSS_FUNCTION && "format".equalsIgnoreCase(primi.getStringValue()) && (args = ((FunctionValue) primi).getArguments()).size() == 1) {
						StyleValue arg = args.item(0);
						if (arg.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
							primi = (PrimitiveValue) arg;
							pType = primi.getPrimitiveType();
							if (pType == CSSPrimitiveValue.CSS_STRING || pType == CSSPrimitiveValue.CSS_IDENT) {
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
				if (loadFont(familyName, uri, fontFormat, rule)) {
					return true;
				}
			} else {
				errorSrc(value, rule);
				return false;
			}
			return false;
		}
		errorSrc(value, rule);
		return false;
	}

	private void errorSrc(StyleValue value, CSSDeclarationRule rule) {
		CSSPropertyValueException ex = new CSSPropertyValueException("Expected primitive value.");
		ex.setValueText(value.getCssText());
		rule.getStyleDeclarationErrorHandler().wrongValue("src", ex);
	}

	private boolean loadFont(String familyName, PrimitiveValue value, String format, ExtendedCSSFontFaceRule rule) {
		short pType = value.getPrimitiveType();
		if (pType == CSSPrimitiveValue.CSS_URI) {
			String uri = value.getStringValue();
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
				doc.getErrorHandler().ruleIOError(uri, e);
				return false;
			}
			InputStream is = null;
			try {
				FontFormat fontFormat = null;
				URLConnection conn = doc.openConnection(url);
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
				doc.getErrorHandler().ruleIOError(url.toExternalForm(), e);
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
				}
			}
		} else if (pType == CSSPrimitiveValue2.CSS_FUNCTION) {
			String fname = value.getStringValue();
			FunctionValue function = (FunctionValue) value;
			if ("local".equalsIgnoreCase(fname) && function.getArguments().size() == 1) {
				StyleValue arg = function.getArguments().get(0);
				if (arg.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					value = (PrimitiveValue) arg;
					if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
						return isFontFamilyAvailable(value.getStringValue());
					}
				}
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
			ExtendedCSSFontFaceRule rule) throws IOException {
		return false;
	}

	@Override
	public boolean supports(String featureName, CSSValue value) {
		return false;
	}

	abstract protected boolean isFontFamilyAvailable(String fontFamily);

}
