/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;

class ShorthandDecomposers {

	private static final HashMap<String, ShorthandDecomposer> decomposers = createShorthandDecomposerMap();

	private static final ShorthandDecomposers instance = new ShorthandDecomposers();

	static {
	}

	private ShorthandDecomposers() {
		super();
	}

	private static HashMap<String, ShorthandDecomposer> createShorthandDecomposerMap() {
		HashMap<String, ShorthandDecomposer> decompMap = new HashMap<>(37);

		decompMap.put("font", new ShorthandDecomposer() {

			@Override
			public SubpropertySetter assignLonghands(BaseCSSStyleDeclaration style,
					String propertyName, LexicalUnit value, boolean important,
					boolean attrTainted) {
				// Check for system font identifier
				if (style.getStyleDatabase() != null
						&& value.getLexicalUnitType() == LexicalType.IDENT
						&& value.getNextLexicalUnit() == null) {
					String decl = style.getStyleDatabase()
							.getSystemFontDeclaration(value.getStringValue());
					if (decl != null) {
						return style.setSystemFont(decl, important);
					}
				}
				FontShorthandSetter setter = new FontShorthandSetter(style);
				return assignLonghands(setter, value, important, attrTainted);
			}

		});

		decompMap.put("margin", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new MarginShorthandSetter(style);
			}

		});

		decompMap.put("padding", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BoxShorthandSetter(style, "padding");
			}

		});

		decompMap.put("border", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderShorthandSetter(style);
			}

		});

		decompMap.put("border-width", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderWidthShorthandSetter(style);
			}

		});

		decompMap.put("border-style", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderStyleShorthandSetter(style);
			}

		});

		decompMap.put("border-color", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderColorShorthandSetter(style);
			}

		});

		decompMap.put("border-top", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderSideShorthandSetter(style, propertyName, "top");
			}

		});

		decompMap.put("border-right", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderSideShorthandSetter(style, propertyName, "right");
			}

		});

		decompMap.put("border-bottom", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderSideShorthandSetter(style, propertyName, "bottom");
			}

		});

		decompMap.put("border-left", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderSideShorthandSetter(style, propertyName, "left");
			}

		});

		decompMap.put("background", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BackgroundShorthandSetter(style);
			}

		});

		decompMap.put("transition", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new TransitionShorthandSetter(style);
			}

		});

		decompMap.put("border-image", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderImageShorthandSetter(style);
			}

		});

		decompMap.put("font-variant", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new FontVariantShorthandSetter(style);
			}

		});

		decompMap.put("border-radius", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new BorderRadiusShorthandSetter(style);
			}

		});

		decompMap.put("list-style", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new ListStyleShorthandSetter(style);
			}

		});

		decompMap.put("animation", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new AnimationShorthandSetter(style);
			}

		});

		decompMap.put("mask", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new MaskShorthandSetter(style);
			}

		});

		decompMap.put("flex", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new FlexShorthandSetter(style);
			}

		});

		decompMap.put("grid", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new GridShorthandSetter(style);
			}

		});

		decompMap.put("grid-template", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new GridTemplateShorthandSetter(style);
			}

		});

		decompMap.put("grid-area", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new GridAreaShorthandSetter(style);
			}

		});

		ShorthandDecomposer gridPlacement = new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new GridPlacementShorthandSetter(style, propertyName);
			}

		};

		decompMap.put("grid-column", gridPlacement);

		decompMap.put("grid-row", gridPlacement);

		decompMap.put("columns", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new ColumnsShorthandSetter(style);
			}

		});

		decompMap.put("column-rule", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new ColumnRuleShorthandSetter(style);
			}

		});

		ShorthandDecomposer orderedTwoLPI = new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new OrderedTwoLPIShorthandSetter(style, propertyName);
			}

		};

		decompMap.put("margin-inline", orderedTwoLPI);

		decompMap.put("padding-inline", orderedTwoLPI);

		decompMap.put("gap", orderedTwoLPI);

		ShorthandDecomposer orderedTwoIdent = new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new OrderedTwoIdentifierShorthandSetter(style, propertyName);
			}

		};

		decompMap.put("place-content", orderedTwoIdent);

		decompMap.put("place-items", orderedTwoIdent);

		decompMap.put("place-self", orderedTwoIdent);

		/*
		 * Not supported by browsers
		 */

		decompMap.put("cue", new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new CueShorthandSetter(style);
			}

		});

		ShorthandDecomposer sequence = new ShorthandDecomposer() {

			@Override
			SubpropertySetter createSetter(BaseCSSStyleDeclaration style, String propertyName,
					LexicalUnit value, boolean important) {
				return new SequenceShorthandSetter(style, propertyName);
			}

		};

		decompMap.put("pause", sequence);

		decompMap.put("rest", sequence);

		return decompMap;
	}

	public static ShorthandDecomposers getInstance() {
		return instance;
	}

	public ShorthandDecomposer get(String propertyName) {
		return decomposers.get(propertyName);
	}

}
