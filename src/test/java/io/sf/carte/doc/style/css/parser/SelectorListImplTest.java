/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class SelectorListImplTest {

	private static CSSParser parser;

	@BeforeAll
	public static void setUpBeforeAll() {
		parser = new CSSParser();
	}

	@Test
	public void testReplaceHas() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors(":has(&+ul)");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(":has(div>p+ul)", replaced.toString());
	}

	@Test
	public void testReplaceNthChildOf() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors(":nth-child(even of & span)");

		assertEquals(":nth-child(even of & span)", selist.toString());

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(":nth-child(even of div>p span)", replaced.toString());
	}

	private SelectorList parseSelectors(String selist) throws CSSException {
		try {
			return parser.parseSelectors(new StringReader(selist));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
