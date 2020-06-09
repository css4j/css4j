/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

public class NSACSelectorFactoryTest {

	private static CSSParser parser;

	@BeforeClass
	public static void setUpBeforeClass() {
		parser = new CSSParser();
	}

	@Test
	public void testEquals() {
		SelectorList selist1 = parser.parseSelectors("*");
		SelectorList selist2 = parser.parseSelectors("*");
		Selector unisel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(unisel.equals(other));
		assertEquals(unisel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		//
		SelectorList otherlist = parser.parseSelectors("p");
		other = otherlist.item(0);
		assertFalse(unisel.equals(other));
		assertFalse(ParseHelper.equalSelectorList(selist1, otherlist));
		Selector elemsel = parser.parseSelectors("p").item(0);
		assertTrue(elemsel.equals(other));
		assertEquals(elemsel.hashCode(), other.hashCode());
		other = parser.parseSelectors("div").item(0);
		assertFalse(elemsel.equals(other));
		assertFalse(other.equals(elemsel));
	}

	@Test
	public void testEqualsClass() {
		SelectorList selist1 = parser.parseSelectors(".foo");
		SelectorList selist2 = parser.parseSelectors(".foo");
		Selector sel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		selist2 = parser.parseSelectors(".bar");
		other = selist2.item(0);
		assertFalse(sel.equals(other));
		assertFalse(ParseHelper.equalSelectorList(selist1, selist2));
		selist2 = parser.parseSelectors(".foo,.bar");
		assertFalse(ParseHelper.equalSelectorList(selist1, selist2));
	}

	@Test
	public void testEqualsClass2() {
		SelectorList selist1 = parser.parseSelectors("p.foo");
		SelectorList selist2 = parser.parseSelectors("p.foo");
		Selector sel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		selist2 = parser.parseSelectors("p.bar");
		other = selist2.item(0);
		assertFalse(sel.equals(other));
		assertFalse(ParseHelper.equalSelectorList(selist1, selist2));
	}

	@Test
	public void testEquals2() {
		SelectorList selist1 = parser.parseSelectors("#exampleid>span");
		SelectorList selist2 = parser.parseSelectors("#exampleid > span");
		Selector sel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		other = parser.parseSelectors("#exampleid>li").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("#fooid>span").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p#exampleid>span").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEquals3() {
		SelectorList selist1 = parser.parseSelectors("#foo");
		SelectorList selist2 = parser.parseSelectors("#foo");
		Selector sel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		other = parser.parseSelectors("#bar").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div#foo").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEquals4() {
		SelectorList selist1 = parser.parseSelectors(":foo");
		SelectorList selist2 = parser.parseSelectors(":foo");
		Selector sel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		other = parser.parseSelectors(":bar").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEquals5() {
		SelectorList selist1 = parser.parseSelectors("p:foo");
		SelectorList selist2 = parser.parseSelectors("p:foo");
		Selector sel = selist1.item(0);
		Selector other = selist2.item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		assertTrue(ParseHelper.equalSelectorList(selist1, selist2));
		other = parser.parseSelectors("div:foo").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEquals6() {
		Selector sel = parser.parseSelectors("::foo").item(0);
		Selector other = parser.parseSelectors("::foo").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("::bar").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEquals7() {
		Selector sel = parser.parseSelectors("p::foo").item(0);
		Selector other = parser.parseSelectors("p::foo").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("div::foo").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsElementNS() throws CSSException, IOException {
		Selector sel = parseSelectorsNS("svg|p::foo").item(0);
		Selector other = parseSelectorsNS("svg|p::foo").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parseSelectorsNS("svg|div::foo").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parseSelectorsNS("p::foo").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		sel = parseSelectorsNS("p::foo").item(0);
		assertTrue(sel.equals(other));
		assertTrue(other.equals(sel));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("p::foo").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsNthChild() {
		Selector sel = parser.parseSelectors(":nth-child(5 of p)").item(0);
		Selector other = parser.parseSelectors(":nth-child(5 of p)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-child(4 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":nth-child(5 of div)").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthChild2() {
		Selector sel = parser.parseSelectors("div:nth-child(5 of p)").item(0);
		Selector other = parser.parseSelectors("div:nth-child(5 of p)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("div:nth-child(4 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div:nth-child(5 of div)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("section:nth-child(5 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":nth-child(5 of p)").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthFirstChild1() {
		Selector sel = parser.parseSelectors(":nth-child(1)").item(0);
		Selector other = parser.parseSelectors(":nth-child(1)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":first-child").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthLastChild1() {
		Selector sel = parser.parseSelectors(":nth-last-child(1)").item(0);
		Selector other = parser.parseSelectors(":nth-last-child(1)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":last-child").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthChildEven() {
		Selector sel = parser.parseSelectors(":nth-child(2n)").item(0);
		Selector other = parser.parseSelectors(":nth-child(2n + 0)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-child(even)").item(0);
		assertFalse(sel.equals(other));
		assertEquals(":nth-child(even)", other.toString());
	}

	@Test
	public void testEqualsNthChildOdd() {
		Selector sel = parser.parseSelectors(":nth-child(2n + 1)").item(0);
		Selector other = parser.parseSelectors(":nth-child(2n + 1)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-child(odd)").item(0);
		assertFalse(sel.equals(other));
		assertEquals(":nth-child(odd)", other.toString());
	}

	@Test
	public void testEqualsNthOfType() {
		Selector sel = parser.parseSelectors(":nth-of-type(5 of p)").item(0);
		Selector other = parser.parseSelectors(":nth-of-type(5 of p)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-of-type(4 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":nth-of-type(5 of div)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":nth-child(5 of p)").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthOfType2() {
		Selector sel = parser.parseSelectors("div:nth-of-type(5 of p)").item(0);
		Selector other = parser.parseSelectors("div:nth-of-type(5 of p )").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("div:nth-of-type(4 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div:nth-of-type(5 of div)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("section:nth-of-type(5 of p)").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":nth-of-type(5 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div:nth-child(5 of p)").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthOfTypeEven() {
		Selector sel = parser.parseSelectors(":nth-of-type(2n)").item(0);
		Selector other = parser.parseSelectors(":nth-of-type(2n + 0)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-of-type(even)").item(0);
		assertFalse(sel.equals(other));
		assertEquals(":nth-of-type(even)", other.toString());
	}

	@Test
	public void testEqualsNthOfTypeOdd() {
		Selector sel = parser.parseSelectors(":nth-of-type(2n + 1)").item(0);
		Selector other = parser.parseSelectors(":nth-of-type(2n + 1)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-of-type(odd)").item(0);
		assertFalse(sel.equals(other));
		assertEquals(":nth-of-type(odd)", other.toString());
	}

	@Test
	public void testEqualsNthFirstOfType1() {
		Selector sel = parser.parseSelectors(":nth-of-type(1)").item(0);
		Selector other = parser.parseSelectors(":nth-of-type(1)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":first-of-type").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthLastOfType1() {
		Selector sel = parser.parseSelectors(":nth-last-of-type(1)").item(0);
		Selector other = parser.parseSelectors(":nth-last-of-type(1)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":last-of-type").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthLastChild() {
		Selector sel = parser.parseSelectors(":nth-last-child(5 of p)").item(0);
		Selector other = parser.parseSelectors(":nth-last-child(5 of p)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":nth-last-child(4 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":nth-last-child(5 of div)").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsNthLastChild2() {
		Selector sel = parser.parseSelectors("div:nth-last-child(5 of p)").item(0);
		Selector other = parser.parseSelectors("div:nth-last-child(5 of p)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("div:nth-last-child(4 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div:nth-last-child(5 of div)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("section:nth-last-child(5 of p)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":nth-last-child(5 of p)").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsFirstChild() {
		Selector sel = parser.parseSelectors(":first-child").item(0);
		Selector other = parser.parseSelectors(":first-child").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":first-child span").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":last-child").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsFirstChild2() {
		Selector sel = parser.parseSelectors("p:first-child").item(0);
		Selector other = parser.parseSelectors("p:first-child").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":first-child").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":last-child").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsAttributeSelector() {
		Selector sel = parser.parseSelectors("[ title ~=\"hi\" ]").item(0);
		Selector other = parser.parseSelectors("[title~=\"hi\"]").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("[ lang ~=\"hi\" ]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("[ title ~=\"foo\" ]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("[title^=\"hi\"]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p[title~=\"hi\"]").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsAttributeSelector2() {
		Selector sel = parser.parseSelectors("p[ title ~=\"hi\" ]").item(0);
		Selector other = parser.parseSelectors("p[title~=\"hi\"]").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("[ title ~=\"hi\" ]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p[title~=\"foo\"]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p[lang~=\"hi\"]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div[title~=\"hi\"]").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsAttributeSelector3() {
		Selector sel = parser.parseSelectors("[ title ]").item(0);
		Selector other = parser.parseSelectors("[title]").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("[lang]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p[title]").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsAttributeSelectorCI() {
		Selector sel = parser.parseSelectors("[title=hi i]").item(0);
		Selector other = parser.parseSelectors("[title=hi i]").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("[title=hi]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("[lang=hi i]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("[title=foo i]").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsAttributeSelectorCI2() {
		Selector sel = parser.parseSelectors("p[title=hi i]").item(0);
		Selector other = parser.parseSelectors("p[title=hi i]").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("[title=hi i]").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors("p[title=hi]").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors("p[lang=hi i]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p[title=foo i]").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("div[title=hi i]").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsAttributeSelectorCombinator() {
		Selector sel = parser.parseSelectors("input[type=text]::first-line").item(0);
		Selector other = parser.parseSelectors("input[ type= text]::first-line").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("[type=text]::first-line").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors("input[type=text]::last-line").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("input::first-line").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsAttributeSelectorNot() {
		Selector sel = parser.parseSelectors(":not(.foo)").item(0);
		Selector other = parser.parseSelectors(":not(.foo)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("p:not(.foo)").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":not(.bar)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":has(.foo)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":not(.foo,.bar)").item(0);
		assertFalse(sel.equals(other));
		sel = parser.parseSelectors(":not(.foo,.bar)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
	}

	@Test
	public void testEqualsAttributeSelectorNotScope() {
		Selector sel = parser.parseSelectors(":not(>.foo)").item(0);
		Selector other = parser.parseSelectors(":not(>.foo)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("p:not(>.foo)").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":not(>.bar)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":has(>.foo)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":not(>.foo,>.bar)").item(0);
		assertFalse(sel.equals(other));
		sel = parser.parseSelectors(":not(>.foo,>.bar)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
	}

	@Test
	public void testEqualsSibling() {
		Selector sel = parser.parseSelectors(".myclass:foo+.bar").item(0);
		Selector other = parser.parseSelectors(".myclass:foo+.bar").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(".myclass:foo+.foo").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p.myclass:foo+.bar").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
	}

	@Test
	public void testEqualsSSibling() {
		Selector sel = parser.parseSelectors(".myclass:foo~.bar").item(0);
		Selector other = parser.parseSelectors(".myclass:foo~.bar").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(".myclass:foo~.foo").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p.myclass:foo~.bar").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(".myclass:foo+.bar").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsDescendantUniversal() {
		Selector sel = parser.parseSelectors(":rtl * ").item(0);
		Selector other = parser.parseSelectors(":rtl *").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":foo * ").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p:rtl *").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":rtl * .class").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsDescendantType() {
		Selector sel = parser.parseSelectors(":rtl p ").item(0);
		Selector other = parser.parseSelectors(":rtl p").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors(":foo p ").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors("p:rtl p").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":rtl p .class").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsPseudoclass() {
		Selector sel = parser.parseSelectors(":dir(ltr)").item(0);
		Selector other = parser.parseSelectors(":dir(ltr)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("p:dir(ltr)").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":dir(foo)").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":dir(\"ltr\")").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":dir('ltr')").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsPseudoclassLang() {
		Selector sel = parser.parseSelectors(":lang(en)").item(0);
		Selector other = parser.parseSelectors(":lang(en)").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("p:lang(en)").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":lang(es)").item(0);
		assertFalse(sel.equals(other));
	}

	@Test
	public void testEqualsPseudoclassLang2() {
		Selector sel = parser.parseSelectors(":lang(zh, \"*-hant\")").item(0);
		Selector other = parser.parseSelectors(":lang(zh, \"*-hant\")").item(0);
		assertTrue(sel.equals(other));
		assertEquals(sel.hashCode(), other.hashCode());
		other = parser.parseSelectors("p:lang(zh, \"*-hant\")").item(0);
		assertFalse(sel.equals(other));
		assertFalse(other.equals(sel));
		other = parser.parseSelectors(":lang(zh, \"hant\")").item(0);
		assertFalse(sel.equals(other));
		other = parser.parseSelectors(":lang(zh)").item(0);
		assertFalse(sel.equals(other));
	}

	private SelectorList parseSelectorsNS(String selist) throws CSSException, IOException {
		return SelectorParserNSTest.parseSelectorsNS(selist, "", "https://www.w3.org/1999/xhtml/", parser);
	}

}
