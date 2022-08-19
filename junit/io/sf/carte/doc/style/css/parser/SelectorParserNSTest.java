/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LangCondition;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.PseudoCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.Selector.SelectorType;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;
import io.sf.carte.doc.style.css.parser.CSSParser.SelectorTokenHandler;
import io.sf.carte.uparser.TokenProducer;

public class SelectorParserNSTest {

	private CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseSelectorUniversalNS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|*");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.UNIVERSAL, sel.getSelectorType());
		assertEquals("*", ((ElementSelector) sel).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|*", sel.toString());
	}

	@Test
	public void testParseSelectorElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|p", sel.toString());
	}

	@Test
	public void testParseSelectorElementError() throws CSSException, IOException {
		try {
			parser.parseSelectors("svg | p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError2() throws CSSException, IOException {
		try {
			parser.parseSelectors("svg| p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError3() throws CSSException, IOException {
		try {
			parser.parseSelectors("svg|");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError4() throws CSSException, IOException {
		try {
			parser.parseSelectors("svg|, p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefix() throws CSSException, IOException {
		try {
			parser.parseSelectors("foo|p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefix2() throws CSSException, IOException {
		try {
			parser.parseSelectors("foo|p div");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefix3() throws CSSException, IOException {
		try {
			parser.parseSelectors("foo|p,div");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefixUniversal() throws CSSException, IOException {
		try {
			parser.parseSelectors("foo|*");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadIdentifier() throws CSSException, IOException {
		try {
			parser.parseSelectors("svg|9p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementNoNS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("|p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("|p", sel.toString());
	}

	@Test
	public void testParseSelectorElementNoNSDefaultNS() throws CSSException, IOException {
		SelectorList selist = parseSelectorsNS("|p", "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("|p", sel.toString());
	}

	@Test
	public void testParseSelectorElementAllNS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("*|p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementDefaultNS() throws CSSException, IOException {
		// Set XHTML namespace as default
		SelectorList selist = parseSelectorsNS("p", "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("https://www.w3.org/1999/xhtml/", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementList() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p, svg|span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|p", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("span", ((ElementSelector) sel).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) sel).getNamespaceURI());
	}

	@Test
	public void testParseSelectorElementList2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p, svg|p span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|p", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		CombinatorSelector desc = (CombinatorSelector) sel;
		Selector anc = desc.getSelector();
		assertEquals(SelectorType.ELEMENT, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) anc).getNamespaceURI());
		assertEquals("svg|p", anc.toString());
		SimpleSelector simple = desc.getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
	}

	@Test
	public void testParseSelectorElementList3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p, p svg|span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		CombinatorSelector desc = (CombinatorSelector) sel;
		Selector anc = desc.getSelector();
		assertEquals(SelectorType.ELEMENT, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		assertNull(((ElementSelector) anc).getNamespaceURI());
		assertEquals("p", anc.toString());
		SimpleSelector simple = desc.getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|span", simple.toString());
	}

	@Test
	public void testParseSelectorAttribute2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValue() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title = \"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title=hi i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title=\"hi\" i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI1() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|input[svg|type=text i][dir=auto]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, firstcond.getConditionType());
		assertEquals("type", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("text", ((AttributeCondition) firstcond).getValue());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((AttributeCondition) firstcond).getNamespaceURI());
		assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		assertFalse(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|input[svg|type=\"text\" i][dir=\"auto\"]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|input[type=text][svg|dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, firstcond.getConditionType());
		assertEquals("type", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("text", ((AttributeCondition) firstcond).getValue());
		assertFalse(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((AttributeCondition) secondcond).getNamespaceURI());
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|input[type=\"text\"][svg|dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|input[svg|foo=bar i][type=text I][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		cond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		//
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((AttributeCondition) firstcond).getNamespaceURI());
		assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|input[svg|foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNSValue() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[svg|title=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[svg|title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNSDefaultNSValue() throws CSSException, IOException {
		SelectorList selist = parseSelectorsNS("p[title=\"hi\"]", "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNoNSValue() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[|title=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("", ((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[|title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNoNSDefaultNSValue() throws CSSException, IOException {
		// Default namespaces should have no effect on attributes
		SelectorList selist = parseSelectorsNS("p[|title=\"hi\"]", "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("", ((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[|title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeAllNSValue() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[*|title=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOf() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[title ~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphen() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[lang|=\"en\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[lang |=\"en\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstring() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[lang*=\"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p[lang *=\"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorLang() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p:lang(en)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.LANG, cond.getConditionType());
		assertEquals("en", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:lang(en)", sel.toString());
	}

	@Test
	public void testParseSelectorLang2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p:lang(zh, \"*-hant\")");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.LANG, cond.getConditionType());
		assertEquals("zh,\"*-hant\"", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:lang(zh,\"*-hant\")", sel.toString());
	}

	@Test
	public void testParseSelectorClass2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p.exampleclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("exampleclass", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p.exampleclass", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div.foo\\(-\\.3\\)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo(-.3)", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.foo\\(-\\.3\\)", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div.\\31 foo\\&-.bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("1foo&-", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.CLASS, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.\\31 foo\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div.\\31 jkl\\&-.bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("1jkl&-", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.CLASS, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscapedBad() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div.\\31jkl\\&-.bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("1jkl&-", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.CLASS, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorChild() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div > span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildNoSpaces() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div>span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttribute() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div>[foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("svg|div>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttributeWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div> [foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("svg|div>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSibling() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div + span:empty");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("empty", ((PseudoCondition) cond).getName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div+span:empty");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("empty", ((PseudoCondition) cond).getName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div.myclass:foo+.bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, first.getSelectorType());
		cond = ((ConditionalSelector) first).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		assertEquals(ConditionType.PSEUDO_CLASS, cond2.getConditionType());
		assertEquals("foo", ((PseudoCondition) cond2).getName());
		Selector simple = ((ConditionalSelector) first).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.myclass:foo+.bar", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSibling() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div ~ span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.ELEMENT, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("svg|div~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingNoSpaces() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div~span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.ELEMENT, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("svg|div~span", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinator() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|col.foo||td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.COLUMN_COMBINATOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(SelectorType.ELEMENT, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancSimple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|col.foo || td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.COLUMN_COMBINATOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(SelectorType.ELEMENT, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) ancSimple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinator2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("col.foo||svg|td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.COLUMN_COMBINATOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(SelectorType.ELEMENT, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("col.foo||svg|td", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p::first-line");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("first-line", ((PseudoCondition) cond).getName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementOld() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p:first-line");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("first-line", ((PseudoCondition) cond).getName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPseudoclassed() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p::first-letter:hover");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, first.getConditionType());
		assertEquals("first-letter", ((PseudoCondition) first).getName());
		assertEquals(ConditionType.PSEUDO_CLASS, second.getConditionType());
		assertEquals("hover", ((PseudoCondition) second).getName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p::first-letter:hover", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClass() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div:blank");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("blank", ((PseudoCondition) cond).getName());
		assertNull(((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div:blank", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassArgument() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p:dir(ltr)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("dir", ((PseudoCondition) cond).getName());
		assertEquals("ltr", ((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:dir(ltr)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|p:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|*:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|*:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOf() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(5 of svg|p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals(":nth-child(5 of svg|p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfUniversal() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(5 of svg|*)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals(":nth-child(5 of svg|*)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not(p.foo, svg|span:first-child, div a)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList args = ((ArgumentCondition) cond).getSelectors();
		assertEquals(3, args.getLength());
		Selector arg = args.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		arg = args.item(1);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals(TestConfig.SVG_NAMESPACE_URI, ((ElementSelector) simple).getNamespaceURI());
		assertEquals(":not(p.foo,svg|span:first-child,div a)", sel.toString());
	}

	@Test
	public void testEquals() throws CSSException, IOException {
		SelectorList selist = parseSelectors("svg|div");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		Selector sel2 = parseSelectors("svg|div").item(0);
		assertTrue(sel.equals(sel2));
		assertEquals(sel.hashCode(), sel2.hashCode());
		sel2 = parseSelectors("div").item(0);
		assertFalse(sel.equals(sel2));
	}

	@Test
	public void testEquals2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("div[svg|title ~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		Selector sel2 = parseSelectors("div[svg|title ~=\"hi\"]").item(0);
		assertTrue(sel.equals(sel2));
		assertEquals(sel.hashCode(), sel2.hashCode());
		sel2 = parseSelectors("div[title ~=\"hi\"]").item(0);
		assertFalse(sel.equals(sel2));
	}

	@Test
	public void testEquals3() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(5 of svg|p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		Selector sel2 = parseSelectors(":nth-child(5 of svg|p)").item(0);
		assertTrue(sel.equals(sel2));
		assertEquals(sel.hashCode(), sel2.hashCode());
		sel2 = parseSelectors(":nth-child(5 of p)").item(0);
		assertFalse(sel.equals(sel2));
	}

	private SelectorList parseSelectors(String selist) throws CSSException, IOException {
		return parseSelectorsNS(selist, null, null);
	}

	private SelectorList parseSelectorsNS(String selist, String prefix, String nsuri) throws CSSException, IOException {
		return parseSelectorsNS(selist, prefix, nsuri, parser);
	}

	static SelectorList parseSelectorsNS(String selist, String prefix, String nsuri, CSSParser parser)
			throws CSSException, IOException {
		int[] allowInWords = { 45, 95 }; // -_
		SelectorTokenHandler handler = parser.new SelectorTokenHandler();
		if (prefix != null) {
			handler.factory.registerNamespacePrefix(prefix, nsuri);
		}
		handler.factory.registerNamespacePrefix("svg", TestConfig.SVG_NAMESPACE_URI);
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader(selist);
		tp.parse(re, "/*", "*/");
		return handler.getSelectorList();
	}

}
