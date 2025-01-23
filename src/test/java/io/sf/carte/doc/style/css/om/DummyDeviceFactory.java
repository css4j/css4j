/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.agent.AbstractDeviceFactory;
import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSFontFaceRule;
import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Dummy device factory, useful for testing.
 * 
 * @author Carlos Amengual
 *
 */
public class DummyDeviceFactory extends AbstractDeviceFactory {

	private final StyleDatabase dummyDatabase = new DummyStyleDatabase();

	public DummyDeviceFactory() {
		super();
	}

	@Override
	public CSSCanvas createCanvas(String medium, CSSDocument doc) {
		if ("screen".equals(medium)) {
			return new DummyCanvas(doc);
		}
		return null;
	}

	@Override
	public StyleDatabase getStyleDatabase(String medium) {
		return dummyDatabase;
	}

	/**
	 * A dumb style database useful for testing.
	 */
	public static class DummyStyleDatabase extends AbstractStyleDatabase {

		private static final long serialVersionUID = 1L;

		private final HashSet<String> fontfaceNames = new HashSet<>();

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
			return true;
		}

		@Override
		public float getFontSizeFromIdentifier(String familyName, String fontSizeIdentifier) throws DOMException {
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
		public boolean isFontFaceName(String requestedFamily) {
			return fontfaceNames.contains(requestedFamily);
		}

		@Override
		public void loadFontFaceRule(CSSFontFaceRule rule) {
			String familyName = rule.getStyle().getPropertyValue("font-family");
			if (familyName == null) {
				rule.getStyleDeclarationErrorHandler().missingRequiredProperty(familyName);
				return;
			}
			fontfaceNames.add(familyName);
		}

		@Override
		public float getWidthSize(String widthIdentifier, float fontSize) throws DOMException {
			if ("thin".equals(widthIdentifier)) {
				return 0.5f;
			} else if ("thick".equals(widthIdentifier)) {
				return 2f;
			} else if ("medium".equals(widthIdentifier)) {
				return 1f;
			} else {
				throw new DOMException(DOMException.SYNTAX_ERR, "Unknown identifier " + widthIdentifier);
			}
		}

		@Override
		public int getColorDepth() {
			return 24;
		}

		@Override
		public float getDeviceHeight() {
			return 768;
		}

		@Override
		public float getDeviceWidth() {
			return 1024;
		}
	}

	/**
	 * A dumb canvas useful for testing.
	 */
	public class DummyCanvas extends AbstractCSSCanvas {

		private CSSDocument document;

		private final Map<CSSElement, List<String>> statePseudoclasses;

		DummyCanvas(CSSDocument doc) {
			super();
			this.document = doc;
			statePseudoclasses = new HashMap<>();
		}

		@Override
		public CSSDocument getDocument() {
			return document;
		}

		protected void setDocument(CSSDocument doc) {
			this.document = doc;
		}

		@Override
		public StyleDatabase getStyleDatabase() {
			return dummyDatabase;
		}

		public void registerStatePseudoclasses(CSSElement element, List<String> statePseudoclasses) {
			this.statePseudoclasses.put(element, statePseudoclasses);
		}

		@Override
		public boolean isActivePseudoClass(CSSElement element, String pseudoclassName) {
			List<String> pseudoclasses = this.statePseudoclasses.get(element);
			return pseudoclasses == null ? false : pseudoclasses.contains(pseudoclassName);
		}

		@Override
		protected String getOrientation() {
			return "landscape";
		}

		@Override
		protected String getOverflowBlock() {
			return "scroll";
		}

		@Override
		protected String getOverflowInline() {
			return "scroll";
		}

		@Override
		protected String getPointerAccuracy() {
			return "fine";
		}

		@Override
		protected String getUpdateFrequency() {
			return "fast";
		}

		@Override
		protected boolean supportsGamut(String gamut) {
			return "p3".equalsIgnoreCase(gamut) || "srgb".equalsIgnoreCase(gamut);
		}

	}

}
