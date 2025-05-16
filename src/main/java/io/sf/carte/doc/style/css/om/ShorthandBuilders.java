/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;

class ShorthandBuilders {

	private static final HashMap<String, ShorthandBuilderFactory> factories = createFactoryMap();

	private static final ShorthandBuilders instance = new ShorthandBuilders();

	private ShorthandBuilders() {
		super();
	}

	private static HashMap<String, ShorthandBuilderFactory> createFactoryMap() {
		HashMap<String, ShorthandBuilderFactory> factoryMap = new HashMap<>(34);

		factoryMap.put("animation", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new AnimationShorthandBuilder(style);
			}

		});

		factoryMap.put("background", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new BackgroundBuilder(style);
			}

		});

		factoryMap.put("border", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new BorderBuilder(style);
			}

		});

		factoryMap.put("border-image", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new BorderImageBuilder(style);
			}

		});

		factoryMap.put("border-radius", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new BorderRadiusBuilder(style);
			}

		});

		factoryMap.put("columns", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new GenericShorthandBuilder(shorthand, style, "auto");
			}

		});

		ShorthandBuilderFactory generic = new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new GenericShorthandBuilder(shorthand, style, "none");
			}

		};

		factoryMap.put("column-rule", generic);

		factoryMap.put("outline", generic);

		factoryMap.put("text-decoration", generic);

		factoryMap.put("text-emphasis", generic);

		factoryMap.put("flex", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new FlexShorthandBuilder(style);
			}

		});

		factoryMap.put("flex-flow", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new GenericShorthandBuilder(shorthand, style, "row");
			}

		});

		factoryMap.put("font", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new FontBuilder(style);
			}

		});

		factoryMap.put("font-variant", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new FontVariantBuilder(style);
			}

		});

		ShorthandBuilderFactory gridPlacement = new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new GridPlacementShorthandBuilder(shorthand, style);
			}

		};

		factoryMap.put("grid-column", gridPlacement);

		factoryMap.put("grid-row", gridPlacement);

		factoryMap.put("grid-area", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new GridAreaShorthandBuilder(style);
			}

		});

		ShorthandBuilderFactory grid = new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new GridShorthandBuilder(style);
			}

		};

		factoryMap.put("grid", grid);

		factoryMap.put("grid-template", grid);

		factoryMap.put("list-style", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new ListStyleShorthandBuilder(style);
			}

		});

		factoryMap.put("mask", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new MaskBuilder(style);
			}

		});

		ShorthandBuilderFactory orderedTwoNormal = new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new OrderedTwoValueShorthandBuilder(shorthand, style, "normal");
			}

		};

		factoryMap.put("place-content", orderedTwoNormal);

		factoryMap.put("place-items", orderedTwoNormal);

		factoryMap.put("place-self", orderedTwoNormal);

		factoryMap.put("gap", orderedTwoNormal);

		factoryMap.put("margin", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new MarginBuilder(style);
			}

		});

		factoryMap.put("padding", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new PaddingBuilder(style);
			}

		});

		ShorthandBuilderFactory orderedTwoZero = new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new OrderedTwoValueShorthandBuilder(shorthand, style, "0");
			}

		};

		factoryMap.put("margin-inline", orderedTwoZero);

		factoryMap.put("padding-inline", orderedTwoZero);

		factoryMap.put("transition", new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new TransitionShorthandBuilder(style);
			}

		});

		/*
		 * Not supported by browsers
		 */

		ShorthandBuilderFactory sequence = new ShorthandBuilderFactory() {

			@Override
			public ShorthandBuilder createBuilder(BaseCSSStyleDeclaration style, String shorthand) {
				return new SequenceShorthandBuilder(shorthand, style);
			}

		};

		factoryMap.put("cue", sequence);

		factoryMap.put("pause", sequence);

		factoryMap.put("rest", sequence);

		return factoryMap;
	}

	public static ShorthandBuilders getInstance() {
		return instance;
	}

	/**
	 * Get the builder factory for the given shorthand.
	 * 
	 * @param shorthandPropertyName the shorthand property name.
	 * @return the builder factory, or {@code null} if there is none.
	 */
	public ShorthandBuilderFactory get(String shorthandPropertyName) {
		return factories.get(shorthandPropertyName);
	}

}
