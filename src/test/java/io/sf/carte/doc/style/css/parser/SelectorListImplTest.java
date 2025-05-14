/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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
	public void testReplaceImplicitTypeDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors("span");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p span", replaced.toString());
	}

	@Test
	public void testReplaceImplicitTypeDescendantList() {
		SelectorList base = parseSelectors("div>p.cls,div:last-child");
		SelectorList selist = parseSelectors("span");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(":is(div>p.cls,div:last-child) span", replaced.toString());
	}

	@Test
	public void testReplaceImplicitClassDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors(".cls");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p .cls", replaced.toString());
	}

	@Test
	public void testReplaceImplicitClassDescendantList() {
		SelectorList base = parseSelectors("div>p.cls,div:last-child");
		SelectorList selist = parseSelectors("span.a1");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(":is(div>p.cls,div:last-child) span.a1", replaced.toString());
	}

	@Test
	public void testReplaceImplicitIDDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors("#myid");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p #myid", replaced.toString());
	}

	@Test
	public void testReplaceImplicitPseudoDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors(":first-child");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p :first-child", replaced.toString());
	}

	@Test
	public void testReplaceImplicitTypePseudoDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors("ul:first-child");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p ul:first-child", replaced.toString());
	}

	@Test
	public void testReplaceImplicitAttrDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors("[data-s]");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p [data-s]", replaced.toString());
	}

	@Test
	public void testReplaceImplicitTypeAttrDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors("span[data-s]");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p span[data-s]", replaced.toString());
	}

	@Test
	public void testReplaceImplicitSiblingDescendant() {
		SelectorList base = parseSelectors("div>p");
		SelectorList selist = parseSelectors("b+span");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div>p b+span", replaced.toString());
	}

	@Test
	public void testReplaceSimple() {
		SelectorList base = parseSelectors(":last-child");
		SelectorList selist = parseSelectors("p&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("p:last-child", replaced.toString());
	}

	@Test
	public void testReplaceDescendant() {
		SelectorList base = parseSelectors("p:last-child");
		SelectorList selist = parseSelectors("div &");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div p:last-child", replaced.toString());
	}

	@Test
	public void testReplaceDescendant2() {
		SelectorList base = parseSelectors("div>p+ul");
		SelectorList selist = parseSelectors("body &");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("body :is(div>p)+ul", replaced.toString());
	}

	@Test
	public void testReplaceDescendant2Pseudo() {
		SelectorList base = parseSelectors("div>p:last-child+ul");
		SelectorList selist = parseSelectors("body &");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("body :is(div>p:last-child)+ul", replaced.toString());
	}

	@Test
	public void testReplaceList() {
		SelectorList base = parseSelectors("p:last-child,div.cls");
		SelectorList selist = parseSelectors("div+&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div+:is(p:last-child,div.cls)", replaced.toString());
	}

	@Test
	public void testReplaceListPseudo() {
		SelectorList base = parseSelectors("p:last-child,div.cls");
		SelectorList selist = parseSelectors("&:hover>#myid");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(":is(p:last-child,div.cls):hover>#myid", replaced.toString());
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

	@Test
	public void testReplaceNextSibling() {
		SelectorList base = parseSelectors("p>ul");
		SelectorList selist = parseSelectors("div+&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div+p>ul", replaced.toString());
	}

	@Test
	public void testReplaceClassNextSibling() {
		SelectorList base = parseSelectors("p.cls");
		SelectorList selist = parseSelectors("div+&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("div+p.cls", replaced.toString());
	}

	@Test
	public void testReplaceNested() {
		SelectorList base = parseSelectors("p:last-child");
		SelectorList selist = parseSelectors("&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("p:last-child", replaced.toString());
	}

	@Test
	public void testReplaceNestedTwice() {
		SelectorList base = parseSelectors(".cls");
		SelectorList selist = parseSelectors("&&");

		assertEquals("&&", selist.toString());

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(".cls.cls", replaced.toString());
	}

	@Test
	public void testReplaceAttrNested() {
		SelectorList base = parseSelectors(".cls");
		SelectorList selist = parseSelectors("[data-s]&");

		assertEquals("[data-s]&", selist.toString());

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("[data-s].cls", replaced.toString());
	}

	@Test
	public void testReplaceAttrNestedList() {
		SelectorList base = parseSelectors(".cls,#myid");
		SelectorList selist = parseSelectors("[data-s]&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals("[data-s]:is(.cls,#myid)", replaced.toString());
	}

	@Test
	public void testReplaceNestedList() {
		SelectorList base = parseSelectors("p:last-child,div.cls");
		SelectorList selist = parseSelectors("&");

		SelectorList replaced = selist.replaceNested(base);

		assertEquals(1, replaced.getLength());
		assertEquals(":is(p:last-child,div.cls)", replaced.toString());
	}

	@Test
	public void testEquals() {
		SelectorList selist = parseSelectors("p:last-child,div.cls");
		SelectorList selist2 = parseSelectors("p:last-child,div.cls");

		assertEquals(selist, selist2);
	}

	@Test
	public void testEqualsNested() {
		SelectorList selist = parseSelectors("&:last-child,&.cls");
		SelectorList selist2 = parseSelectors("&:last-child,&.cls");

		assertEquals(selist, selist2);
	}

	private SelectorList parseSelectors(String selist) throws CSSException {
		try {
			return parser.parseSelectors(new StringReader(selist));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
