/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.agent.AbstractDeviceFactory;
import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.Viewport;
import io.sf.carte.doc.style.css.AbstractStyleDatabase;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Dummy device factory, useful for testing.
 * 
 * @author Carlos Amengual
 *
 */
public class DummyDeviceFactory extends AbstractDeviceFactory {

	private StyleDatabase dummyDatabase = new DummyStyleDatabase();

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
		public int getFontSizeFromIdentifier(String familyName, String fontSizeIdentifier) throws DOMException {
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
				sz = 16f;
			} else if (fontSizeIdentifier.equals("xx-large")) {
				sz = 18f;
			} else {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unknown size identifier: " + fontSizeIdentifier);
			}
			return Math.round(sz);
		}

		@Override
		public short getNaturalUnit() {
			return CSSPrimitiveValue.CSS_PX;
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
		public int getPixelDepth() {
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
	public class DummyCanvas implements CSSCanvas {
		private CSSDocument doc;
		private Map<CSSElement, List<String>> statePseudoclasses;

		DummyCanvas(CSSDocument doc) {
			super();
			this.doc = doc;
			statePseudoclasses = new HashMap<CSSElement, List<String>>();
		}

		@Override
		public StyleDatabase getStyleDatabase() {
			return dummyDatabase;
		}

		@Override
		public void loadFontFace(CSSFontFaceRule rule) {
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
		public boolean supports(String featureName, CSSValue value) {
			return false;
		}

		@Override
		public CSSValue getFeatureValue(String feature) {
			return null;
		}

		/**
		 * Reload any possible style state/caching that this canvas may have.
		 */
		@Override
		public void reloadStyleState() {
		}

		@Override
		public boolean isFontFaceName(String requestedFamily) {
			return false;
		}

		@Override
		public Viewport getViewport() {
			return null;
		}

		/**
		 * Gives the width, in typographic points, for showing the given text
		 * with the given style.
		 * 
		 * @param text
		 *            the text to measure.
		 * @param style
		 *            the style that applies.
		 * @return the advance width for showing the text with the given font.
		 */
		@Override
		public int stringWidth(String text, CSSComputedProperties style) {
			return Math.round(text.length() * style.getComputedFontSize() / 2f);
		}

		@Override
		public float getCapHeight(CSSComputedProperties style) {
			return style.getComputedFontSize() * 0.75f;
		}

		@Override
		public CSSDocument getDocument() {
			return doc;
		}

	}

}
