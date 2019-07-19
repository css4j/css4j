/*

 Copyright (c) 2005-2019, Carlos Amengual.

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
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.PositionalCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition2;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.PositionalCondition2;
import io.sf.carte.doc.style.css.nsac.Selector2;
import io.sf.carte.doc.style.css.parser.CSSParser.SelectorTokenHandler;
import io.sf.carte.uparser.TokenProducer;

public class SelectorParserNSTest {

	static CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseSelectorUniversalNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|*"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("*", ((ElementSelector) sel).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|*", sel.toString());
	}

	@Test
	public void testParseSelectorElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|p", sel.toString());
	}

	@Test
	public void testParseSelectorElementError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg | p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg| p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|, p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefix() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo|p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefix2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo|p div"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefix3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo|p,div"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadPrefixUniversal() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo|*"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementErrorBadIdentifier() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|9p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementNoNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("|p"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("|p", sel.toString());
	}

	@Test
	public void testParseSelectorElementNoNSDefaultNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("|p"));
		SelectorList selist = parseSelectorsNS(source, "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("|p", sel.toString());
	}

	@Test
	public void testParseSelectorElementAllNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("*|p"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementDefaultNS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p"));
		// Set XHTML namespace as default
		SelectorList selist = parseSelectorsNS(source, "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("https://www.w3.org/1999/xhtml/", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementList() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p, svg|span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("span", ((ElementSelector) sel).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) sel).getNamespaceURI());
	}

	@Test
	public void testParseSelectorElementList2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p, svg|p span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) sel).getNamespaceURI());
		assertEquals("svg|p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		DescendantSelector desc = (DescendantSelector) sel;
		Selector anc = desc.getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) anc).getNamespaceURI());
		assertEquals("svg|p", anc.toString());
		SimpleSelector simple = desc.getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
	}

	@Test
	public void testParseSelectorElementList3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p, p svg|span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		DescendantSelector desc = (DescendantSelector) sel;
		Selector anc = desc.getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		assertNull(((ElementSelector) anc).getNamespaceURI());
		assertEquals("p", anc.toString());
		SimpleSelector simple = desc.getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|span", simple.toString());
	}

	@Test
	public void testParseSelectorAttribute2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertFalse(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title = \"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title=hi i]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) cond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title=\"hi\" i]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) cond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI1() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|input[svg|type=text i][dir=auto]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("type", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("text", ((AttributeCondition) firstcond).getValue());
		assertEquals("http://www.w3.org/2000/svg", ((AttributeCondition) firstcond).getNamespaceURI());
		if (firstcond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) firstcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) firstcond).getSpecified());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition2) {
			assertFalse(((AttributeCondition2) secondcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) secondcond).getSpecified());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|input[svg|type=\"text\" i][dir=\"auto\"]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|input[type=text][svg|dir=auto i]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("type", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("text", ((AttributeCondition) firstcond).getValue());
		if (firstcond instanceof AttributeCondition2) {
			assertFalse(((AttributeCondition2) firstcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) firstcond).getSpecified());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		assertEquals("http://www.w3.org/2000/svg", ((AttributeCondition) secondcond).getNamespaceURI());
		if (secondcond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) secondcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) secondcond).getSpecified());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|input[type=\"text\"][svg|dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|input[svg|foo=bar i][type=text i][dir=auto i]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		cond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) secondcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) secondcond).getSpecified());
		//
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition)cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		assertEquals("http://www.w3.org/2000/svg", ((AttributeCondition) firstcond).getNamespaceURI());
		if (firstcond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) firstcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) firstcond).getSpecified());
		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) secondcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) secondcond).getSpecified());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|input[svg|foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNSValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[svg|title=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[svg|title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNSDefaultNSValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\"]"));
		SelectorList selist = parseSelectorsNS(source, "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNoNSValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[|title=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("", ((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[|title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeNoNSDefaultNSValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[|title=\"hi\"]"));
		// Default namespaces should have no effect on attributes
		SelectorList selist = parseSelectorsNS(source, "", "https://www.w3.org/1999/xhtml/");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("", ((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[|title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeAllNSValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[*|title=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOf() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title~=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[title ~=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphen() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[lang|=\"en\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[lang |=\"en\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstring() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[lang*=\"CH\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p[lang *=\"CH\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("svg|p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorLang() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p:lang(en)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_LANG_CONDITION, cond.getConditionType());
		assertEquals("en", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:lang(en)", sel.toString());
	}

	@Test
	public void testParseSelectorLang2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p:lang(zh, \"*-hant\")"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_LANG_CONDITION, cond.getConditionType());
		assertEquals("zh,\"*-hant\"", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:lang(zh,\"*-hant\")", sel.toString());
	}

	@Test
	public void testParseSelectorClass2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p.exampleclass"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("exampleclass", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p.exampleclass", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div.foo\\(-\\.3\\)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo(-.3)", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.foo\\(-\\.3\\)", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div.\\31 foo\\&-.bar"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("1foo&-", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.\\31 foo\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div.\\31 jkl\\&-.bar"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("1jkl&-", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscapedBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div.\\31jkl\\&-.bar"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("1jkl&-", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorChild() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div > span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildNoSpaces() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div>span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttribute() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div>[foo]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("svg|div>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttributeWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div> [foo]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("svg|div>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div>>span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant3WS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div >> span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancestor).getNamespaceURI());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSibling() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div + span:empty"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("empty", ((AttributeCondition) cond).getLocalName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div+span:empty"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("empty", ((AttributeCondition) cond).getLocalName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|div+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div.myclass:foo+.bar"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond2).getLocalName());
		Selector simple = ((ConditionalSelector) first).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div.myclass:foo+.bar", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSibling() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div ~ span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("svg|div~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingNoSpaces() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div~span"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("div", ((ElementSelector) first).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) first).getNamespaceURI());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("svg|div~span", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinator() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|col.foo||td"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancSimple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|col.foo || td"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) ancSimple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("svg|col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinator2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("col.foo||svg|td"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("col.foo||svg|td", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p::first-line"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("first-line", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementOld() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p:first-line"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("first-line", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPseudoclassed() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p::first-letter:hover"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition2.SAC_PSEUDO_ELEMENT_CONDITION, first.getConditionType());
		assertEquals("first-letter", ((AttributeCondition) first).getLocalName());
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, second.getConditionType());
		assertEquals("hover", ((AttributeCondition) second).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p::first-letter:hover", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClass() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div:blank"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("blank", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|div:blank", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassArgument() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p:dir(ltr)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("dir", ((AttributeCondition) cond).getLocalName());
		assertEquals("ltr", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:dir(ltr)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|p:first-child"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|p:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|*:first-child"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals("svg|*:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOf() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(5 of svg|p)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals(":nth-child(5 of svg|p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfUniversal() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(5 of svg|*)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals(":nth-child(5 of svg|*)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not(p.foo, svg|span:first-child, div a)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList args = ((ArgumentCondition) cond).getSelectors();
		assertEquals(3, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		arg = args.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("http://www.w3.org/2000/svg", ((ElementSelector) simple).getNamespaceURI());
		assertEquals(":not(p.foo,svg|span:first-child,div a)", sel.toString());
	}

	@Test
	public void testEquals() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg|div"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		source = new InputSource(new StringReader("svg|div"));
		Selector sel2 = parseSelectors(source).item(0);
		assertTrue(sel.equals(sel2));
		assertEquals(sel.hashCode(), sel2.hashCode());
		source = new InputSource(new StringReader("div"));
		sel2 = parseSelectors(source).item(0);
		assertFalse(sel.equals(sel2));
	}

	@Test
	public void testEquals2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div[svg|title ~=\"hi\"]"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		source = new InputSource(new StringReader("div[svg|title ~=\"hi\"]"));
		Selector sel2 = parseSelectors(source).item(0);
		assertTrue(sel.equals(sel2));
		assertEquals(sel.hashCode(), sel2.hashCode());
		source = new InputSource(new StringReader("div[title ~=\"hi\"]"));
		sel2 = parseSelectors(source).item(0);
		assertFalse(sel.equals(sel2));
	}

	@Test
	public void testEquals3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(5 of svg|p)"));
		SelectorList selist = parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		source = new InputSource(new StringReader(":nth-child(5 of svg|p)"));
		Selector sel2 = parseSelectors(source).item(0);
		assertTrue(sel.equals(sel2));
		assertEquals(sel.hashCode(), sel2.hashCode());
		source = new InputSource(new StringReader(":nth-child(5 of p)"));
		sel2 = parseSelectors(source).item(0);
		assertFalse(sel.equals(sel2));
	}

	public SelectorList parseSelectors(InputSource source) throws CSSException, IOException {
		return parseSelectorsNS(source, null, null);
	}

	public SelectorList parseSelectorsNS(InputSource source, String prefix, String nsuri) throws CSSException, IOException {
		int[] allowInWords = { 45, 95 }; // -_
		SelectorTokenHandler handler = parser.new SelectorTokenHandler(source, null);
		if (prefix != null) {
			handler.factory.registerNamespacePrefix(prefix, nsuri);
		}
		handler.factory.registerNamespacePrefix("svg", "http://www.w3.org/2000/svg");
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(source.getCharacterStream(), "/*", "*/");
		return handler.getSelectorList();
	}

}
