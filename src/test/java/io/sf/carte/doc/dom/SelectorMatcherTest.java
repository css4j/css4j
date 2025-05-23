/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.SelectorMatcher;

/**
 * The DOM implementation uses a slightly modified selector matcher which is
 * checked here.
 */
public class SelectorMatcherTest extends io.sf.carte.doc.style.css.om.SelectorMatcherTest {

	@Override
	protected SelectorMatcher selectorMatcher(CSSElement elm) {
		return elm.getSelectorMatcher();
	}

}
