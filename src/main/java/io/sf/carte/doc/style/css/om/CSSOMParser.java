/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.EnumSet;

import io.sf.carte.doc.style.css.MediaQueryFactory;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * CSS parser implementing the NSAC API and using an object-model internal factory.
 */
public class CSSOMParser extends CSSParser {

	/**
	 * Instantiate a parser instance with no flags.
	 */
	public CSSOMParser() {
		super();
	}

	/**
	 * Instantiate a parser instance with the given flags.
	 * 
	 * @param parserFlags the flags.
	 */
	public CSSOMParser(EnumSet<Flag> parserFlags) {
		super(parserFlags);
	}

	protected CSSOMParser(CSSParser copyMe) {
		super(copyMe);
	}

	@Override
	protected MediaQueryFactory getMediaQueryFactory() {
		return new CSSValueMediaQueryFactory();
	}

	@Override
	public CSSOMParser clone() {
		CSSOMParser parser = new CSSOMParser(this);
		return parser;
	}

}
