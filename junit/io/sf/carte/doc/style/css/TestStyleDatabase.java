/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Style database mock.
 * 
 * @author carlos
 *
 */
public class TestStyleDatabase extends AbstractStyleDatabase {

	public TestStyleDatabase() {
		super();
	}

	@Override
	public int getColorDepth() {
		return 8;
	}

	@Override
	public float getDeviceHeight() {
		return 842f;
	}

	@Override
	public float getDeviceWidth() {
		return 595f;
	}

	@Override
	public String getDefaultGenericFontFamily(String genericFamily) {
		String fontName = null;
		if (genericFamily.equals("serif")) {
			fontName = "Serif";
		} else if (genericFamily.equals("sans serif")) {
			fontName = "SansSerif";
		} else if (genericFamily.equals("monospace")) {
			fontName = "Monospaced";
		}
		return fontName;
	}

	@Override
	protected boolean isFontFamilyAvailable(String fontFamily) {
		if(fontFamily.equalsIgnoreCase("Courier") || fontFamily.equalsIgnoreCase("Helvetica")
				|| fontFamily.equalsIgnoreCase("Times New Roman") ||
				fontFamily.equalsIgnoreCase("Lucida Typewriter")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public float getFontSizeFromIdentifier(String familyName,
			String fontSizeIdentifier) throws DOMException {
		float sz;
		if (fontSizeIdentifier.equals("medium")) {
			sz = 12f;
		} else if (fontSizeIdentifier.equals("small")) {
			sz = 10f;
		} else if (fontSizeIdentifier.equals("x-small")) {
			sz = 9f;
		} else if (fontSizeIdentifier.equals("xx-small")) {
			sz = 8f;
		} else if (fontSizeIdentifier.equals("large")) {
			sz = 14f;
		} else if (fontSizeIdentifier.equals("x-large")) {
			sz = 18f;
		} else if (fontSizeIdentifier.equals("xx-large")) {
			sz = 24f;
		} else {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"Unknown size identifier: " + fontSizeIdentifier);
		}
		return sz;
	}

	@Override
	public String getSystemFontDeclaration(String systemFontName) {
		if("caption".equals(systemFontName)) {
			return "600 9pt Captionfont";
		} else if("icon".equals(systemFontName)) {
			return "300 7pt Iconfont";
		} else if("menu".equals(systemFontName)) {
			return "800 12pt Menufont";
		} else if("message-box".equals(systemFontName)) {
			return "500 8pt Messageboxfont";
		} else if("small-caption".equals(systemFontName)) {
			return "500 8pt Smallcaptionfont";
		} else if("status-bar".equals(systemFontName)) {
			return "700 10pt Statusbarfont";
		}
		return null;
	}

	@Override
	public float getWidthSize(String widthIdentifier, float fontSize)
			throws DOMException {
		float factor = 0.62f;
		if ("thin".equals(widthIdentifier)) {
			return 0.5f * factor;
		} else if ("thick".equals(widthIdentifier)) {
			return 2f * factor;
		} else if ("medium".equals(widthIdentifier)) {
			return 1f * factor;
		} else {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"Unknown identifier " + widthIdentifier);
		}
	}

	@Override
	public short getNaturalUnit() {
		return CSSPrimitiveValue.CSS_PT;
	}

}
