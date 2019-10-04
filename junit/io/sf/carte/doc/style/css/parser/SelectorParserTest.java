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

import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LangCondition;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

public class SelectorParserTest {

	static CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseSelectorUniversal() throws CSSException, IOException {
		SelectorList selist = parseSelectors("*");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, sel.getSelectorType());
		assertEquals("*", sel.toString());
	}

	@Test
	public void testParseSelectorElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped() throws CSSException, IOException {
		SelectorList selist = parseSelectors("\\61 ");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("a", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("a", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("\\64 iv");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("div", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("div", sel.toString());
	}

	@Test
	public void testParseSelectorElementBad() throws CSSException, IOException {
		try {
			parseSelectors("9p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementIEHack() throws CSSException, IOException {
		try {
			parseSelectors("body*");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementIEHack2() throws CSSException, IOException {
		try {
			parseSelectors("body\\ ");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementList() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p, span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("span", ((ElementSelector) sel).getLocalName());
	}

	@Test
	public void testParseSelectorElementList2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p, p span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		CombinatorSelector desc = (CombinatorSelector) sel;
		Selector anc = desc.getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		SimpleSelector simple = desc.getSecondSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
	}

	@Test
	public void testParseSelectorElementList3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p, p .class");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		CombinatorSelector desc = (CombinatorSelector) sel;
		Selector anc = desc.getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		SimpleSelector simple = desc.getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("class", ((AttributeCondition) cond).getValue());
	}

	@Test
	public void testParseSelectorElementListBad() throws CSSException, IOException {
		try {
			parseSelectors("p,");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorListDuplicate() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p, p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorListDuplicate2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".class, .class, ::first-line, ::first-line");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("class", ((AttributeCondition) cond).getValue());
		sel = selist.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("first-line", ((AttributeCondition) cond).getLocalName());
	}

	@Test
	public void testParseStringSelectorElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementError() throws CSSException, IOException {
		try {
			parseSelectors("!,p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError2() throws CSSException, IOException {
		try {
			parseSelectors(",p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError3() throws CSSException, IOException {
		try {
			parseSelectors("p⁑");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError4() throws CSSException, IOException {
		try {
			parseSelectors("⁑p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError5() throws CSSException, IOException {
		try {
			parseSelectors("p*");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementError6() throws CSSException, IOException {
		try {
			parseSelectors("p* .foo");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorWSError() throws CSSException, IOException {
		try {
			parseSelectors("display: none;");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorWSError2() throws CSSException, IOException {
		try {
			parseSelectors("# foo");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorWSError3() throws CSSException, IOException {
		try {
			parseSelectors("foo. bar");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttribute() throws CSSException, IOException {
		SelectorList selist = parseSelectors("[title],[foo]");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("[title]", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute2WS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[ title ]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute3() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getValue());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("title", ((AttributeCondition) secondcond).getLocalName());
		assertNull(((AttributeCondition) secondcond).getNamespaceURI());
		assertEquals(".foo[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeUniversal() throws CSSException, IOException {
		SelectorList selist = parseSelectors("*[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeError() throws CSSException, IOException {
		try {
			parseSelectors("p[ti!tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError2() throws CSSException, IOException {
		try {
			parseSelectors("p[ti$tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError2WS() throws CSSException, IOException {
		try {
			parseSelectors("p[ ti$tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError3() throws CSSException, IOException {
		try {
			parseSelectors("p[ti^tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError4() throws CSSException, IOException {
		try {
			parseSelectors("p[ti*tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError5() throws CSSException, IOException {
		try {
			parseSelectors("p[ti~tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError6() throws CSSException, IOException {
		try {
			parseSelectors("p[ti@tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError7() throws CSSException, IOException {
		try {
			parseSelectors("p[9title]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError8() throws CSSException, IOException {
		try {
			parseSelectors("p[.title]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError9() throws CSSException, IOException {
		try {
			parseSelectors("p[#title]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValue() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=\"hi\"]");
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
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueNQ() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=hi]");
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
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueNQEscaped() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=\\*foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("*foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"*foo\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS1() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[ title=\"hi\" ]");
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
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValue2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=\"hi:\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi:", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi:\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title = \"hi\"]");
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
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[ title = \"hi\" ]");
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
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=hi i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCIWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[ title=hi i ]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=\"hi\" i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCIuc() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title=\"hi\" I]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueError() throws CSSException, IOException {
		try {
			parseSelectors("p[title=\"hi\" a]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueError2() throws CSSException, IOException {
		try {
			parseSelectors("p[title=\"hi\" .]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError3() throws CSSException, IOException {
		try {
			parseSelectors("p[title=\"hi\" =]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError4() throws CSSException, IOException {
		try {
			parseSelectors("p[title=\"hi\" ;]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError5() throws CSSException, IOException {
		try {
			parseSelectors("p[title=\"hi\" ii]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError6() throws CSSException, IOException {
		try {
			parseSelectors("p[title=\"hi\"]()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError7() throws CSSException, IOException {
		try {
			parseSelectors("p[foo~class]");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI1() throws CSSException, IOException {
		SelectorList selist = parseSelectors("input[type=text i][dir=auto]");
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
		if (firstcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertFalse(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[type=\"text\" i][dir=\"auto\"]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("input[type=text][dir=auto i]");
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
		if (firstcond instanceof AttributeCondition) {
			assertFalse(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[type=\"text\"][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("input[foo=bar i][type=text i][dir=auto i]");
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
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		//
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		if (firstcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCIDescendant() throws CSSException, IOException {
		SelectorList selist = parseSelectors("div input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		cond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		//
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		if (firstcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCIAdjacent() throws CSSException, IOException {
		SelectorList selist = parseSelectors("div+input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		cond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		//
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		if (firstcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div+input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCISibling() throws CSSException, IOException {
		SelectorList selist = parseSelectors("div~input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		cond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		//
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		if (firstcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		if (secondcond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div~input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOf() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title~=\"hi\"]");
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
		assertEquals("p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS1() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[title ~=\"hi\"]");
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
		assertEquals("p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[ title ~=\"hi\" ]");
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
		assertEquals("p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphen() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang|=\"en\"]");
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
		assertEquals("p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenNQ() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang|=en]");
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
		assertEquals("p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang |=\"en\"]");
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
		assertEquals("p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenError() throws CSSException, IOException {
		try {
			parseSelectors("p[lang | =\"en\"]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeHyphenError2() throws CSSException, IOException {
		try {
			parseSelectors("p[ lang | =\"en\" ]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeSubstring() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang*=\"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang *=\"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffix() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang$=\"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ENDS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang$=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang $= \"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ENDS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang$=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefix() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[style^=\"display:\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_BEGINS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("style", ((AttributeCondition) cond).getLocalName());
		assertEquals("display:", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[style^=\"display:\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p[lang ^= \"en\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_BEGINS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang^=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorLang() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:lang(en)");
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
		assertEquals("p:lang(en)", sel.toString());
	}

	@Test
	public void testParseSelectorLang2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:lang(zh, \"*-hant\")");
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
		assertEquals("p:lang(zh,\"*-hant\")", sel.toString());
	}

	@Test
	public void testParseSelectorLang3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:lang(zh, '*-hant')");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_LANG_CONDITION, cond.getConditionType());
		assertEquals("zh,'*-hant'", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:lang(zh,'*-hant')", sel.toString());
	}

	@Test
	public void testParseSelectorLang4() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:lang(es, fr, \\*-Latn)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_LANG_CONDITION, cond.getConditionType());
		assertEquals("es,fr,*-Latn", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:lang(es,fr,\\*-Latn)", sel.toString());
	}

	@Test
	public void testParseSelectorLangError() throws CSSException, IOException {
		try {
			parseSelectors(":lang(zh, )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorLangError2() throws CSSException, IOException {
		try {
			parseSelectors(":lang( , \"*-hant\")");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(8, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClass() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".exampleclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("exampleclass", ((AttributeCondition) cond).getValue());
		assertEquals(".exampleclass", sel.toString());
	}

	@Test
	public void testParseSelectorClass2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p.exampleclass");
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
		assertEquals("p.exampleclass", sel.toString());
	}

	@Test
	public void testParseSelectorClassOtherChar() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".exampleclass⁑");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("exampleclass⁑", ((AttributeCondition) cond).getValue());
		assertEquals(".exampleclass⁑", sel.toString());
	}

	@Test
	public void testParseSelectorClassSurrogate() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo\ud950\udc90");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo\ud950\udc90", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\ud950\udc90", sel.toString());
	}

	@Test
	public void testParseSelectorClassSurrogate2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo🚧");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo🚧", ((AttributeCondition) cond).getValue());
		assertEquals(".foo🚧", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscapedChar() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo\\/1");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo/1", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\\/1", sel.toString());
	}

	@Test
	public void testParseSelectorListClass() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo,.bar");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		assertEquals(".foo", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		assertEquals(".bar", sel.toString());
	}

	@Test
	public void testParseSelectorStringMethodClass() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo,.bar");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		assertEquals(".foo", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		assertEquals(".bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassError() throws CSSException, IOException {
		try {
			parseSelectors("p.example&class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError2() throws CSSException, IOException {
		try {
			parseSelectors("p.9class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError3() throws CSSException, IOException {
		try {
			parseSelectors("p.example$class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError4() throws CSSException, IOException {
		try {
			parseSelectors("p.example%class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError5() throws CSSException, IOException {
		try {
			parseSelectors("p.example!class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError6() throws CSSException, IOException {
		try {
			parseSelectors("p.example'class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError7() throws CSSException, IOException {
		try {
			parseSelectors("p.example(class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError8() throws CSSException, IOException {
		try {
			parseSelectors("p.example)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError9() throws CSSException, IOException {
		try {
			parseSelectors("p.example*class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError10() throws CSSException, IOException {
		try {
			parseSelectors("p.example;class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError11() throws CSSException, IOException {
		try {
			parseSelectors("p.example<class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError12() throws CSSException, IOException {
		try {
			parseSelectors("p.example=class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError13() throws CSSException, IOException {
		try {
			parseSelectors("p.example?class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError14() throws CSSException, IOException {
		try {
			parseSelectors("p.example@class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError15() throws CSSException, IOException {
		try {
			parseSelectors("p.example{class}");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError16() throws CSSException, IOException {
		try {
			parseSelectors("p.example]");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError17() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p.example\\class");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("example\u000class", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p.example\\c lass", sel.toString());
	}

	@Test
	public void testParseSelectorClassError18() throws CSSException, IOException {
		try {
			parseSelectors("p.example^class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError19() throws CSSException, IOException {
		try {
			parseSelectors("p.example|class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(11, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError20() throws CSSException, IOException {
		try {
			parseSelectors("p.example{");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError21() throws CSSException, IOException {
		try {
			parseSelectors("p.example}");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError22() throws CSSException, IOException {
		try {
			parseSelectors("p.example())");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError23() throws CSSException, IOException {
		try {
			parseSelectors(".#example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError24() throws CSSException, IOException {
		try {
			parseSelectors("..example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassEscaped() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo\\(-\\.3\\)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo(-.3)", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\\(-\\.3\\)", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".\\31 foo\\&-.bar");
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
		assertEquals(".\\31 foo\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped3() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".\\31 jkl\\&-.bar");
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
		assertEquals(".\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscapedBad() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".\\31jkl\\&-.bar");
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
		assertEquals(".\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorId() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		assertEquals("#exampleid", sel.toString());
	}

	@Test
	public void testParseSelectorIdEscapedChar() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#foo\\/1");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("foo/1", ((AttributeCondition) cond).getValue());
		assertEquals("#foo\\/1", sel.toString());
	}

	@Test
	public void testParseSelectorIdError() throws CSSException, IOException {
		try {
			parseSelectors("#example&id");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError2() throws CSSException, IOException {
		try {
			parseSelectors("#9example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError3() throws CSSException, IOException {
		try {
			parseSelectors("#.example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError4() throws CSSException, IOException {
		try {
			parseSelectors("##example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError5() throws CSSException, IOException {
		try {
			parseSelectors("#example()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChild() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid > span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildNoSpaces() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid>span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttribute() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid>[foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("#exampleid>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttributeWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid> [foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("#exampleid>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildUniversal() throws CSSException, IOException {
		SelectorList selist = parseSelectors("* > span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildUniversal2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("span > *");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("span", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("span>*", sel.toString());
	}

	@Test
	public void testParseSelectorChildError() throws CSSException, IOException {
		try {
			parseSelectors("#id>");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError2() throws CSSException, IOException {
		try {
			parseSelectors("#id:>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError3() throws CSSException, IOException {
		try {
			parseSelectors("#id>+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError4() throws CSSException, IOException {
		try {
			parseSelectors("#id>*p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError5() throws CSSException, IOException {
		try {
			parseSelectors("#id>*\\60");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError6() throws CSSException, IOException {
		try {
			parseSelectors("#id>~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError7() throws CSSException, IOException {
		try {
			parseSelectors(":>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError8() throws CSSException, IOException {
		try {
			parseSelectors("#id>*+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError9() throws CSSException, IOException {
		try {
			parseSelectors("#id>#+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError10() throws CSSException, IOException {
		try {
			parseSelectors("#id>.+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError11() throws CSSException, IOException {
		try {
			parseSelectors("#id|>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError12() throws CSSException, IOException {
		try {
			parseSelectors("#id>>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError13() throws CSSException, IOException {
		try {
			parseSelectors("#id&>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError14() throws CSSException, IOException {
		try {
			parseSelectors("#id#>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError15() throws CSSException, IOException {
		try {
			parseSelectors("#id+>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError16() throws CSSException, IOException {
		try {
			parseSelectors("#id~>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError17() throws CSSException, IOException {
		try {
			parseSelectors("#id@>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError18() throws CSSException, IOException {
		try {
			parseSelectors("#id$>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorDescendantElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("li span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("li", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped() throws CSSException, IOException {
		SelectorList selist = parseSelectors("\\61  span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("a", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("a span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("div \\61");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("a", ((ElementSelector) simple).getLocalName());
		assertEquals("div a", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("body \\64 iv");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("body", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("body div", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#\\31 exampleid\\/2 span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("1exampleid/2", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#\\31 exampleid\\/2 span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant3() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo  span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals(".foo span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant3WS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("[myattr]  span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("myattr", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("[myattr] span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal() throws CSSException, IOException {
		SelectorList selist = parseSelectors("* span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("* span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":rtl * ");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, ancSimple.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("rtl", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals(":rtl *", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("* .foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		assertEquals("* .foo", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal4() throws CSSException, IOException {
		SelectorList selist = parseSelectors("span *");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("span", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("span *", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedDescendant() throws CSSException, IOException {
		SelectorList selist = parseSelectors("body:not(.foo)[id*=substring] .header");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("body", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, first.getConditionType());
		assertEquals(Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION, second.getConditionType());
		assertEquals("not", ((ArgumentCondition) first).getName());
		SelectorList notlist = ((ArgumentCondition) first).getSelectors();
		assertNotNull(notlist);
		assertEquals(1, notlist.getLength());
		Selector not1 = notlist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, not1.getSelectorType());
		Condition not1cond = ((ConditionalSelector) not1).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, not1cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) not1cond).getValue());
		assertEquals("id", ((AttributeCondition) second).getLocalName());
		assertEquals("substring", ((AttributeCondition) second).getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition simplecond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, simplecond.getConditionType());
		assertEquals("header", ((AttributeCondition) simplecond).getValue());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantCombined() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".fooclass #descid.barclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		AttributeCondition attcond = (AttributeCondition) cond;
		assertEquals("fooclass", attcond.getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ID_CONDITION, first.getConditionType());
		assertEquals(Condition.SAC_CLASS_CONDITION, second.getConditionType());
		assertEquals("descid", ((AttributeCondition) first).getValue());
		assertEquals("barclass", ((AttributeCondition) second).getValue());
		assertEquals(".fooclass #descid.barclass", sel.toString());
	}

	@Test
	public void testParseSelectorComplexCombined() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".fooclass #descid.barclass .someclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, ancestor.getSelectorType());
		Selector ancestor2 = ((CombinatorSelector) ancestor).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor2.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor2).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor2).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		AttributeCondition attcond = (AttributeCondition) cond;
		assertEquals("fooclass", attcond.getValue());
		simple = ((CombinatorSelector) ancestor).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ID_CONDITION, first.getConditionType());
		assertEquals(Condition.SAC_CLASS_CONDITION, second.getConditionType());
		assertEquals("descid", ((AttributeCondition) first).getValue());
		assertEquals("barclass", ((AttributeCondition) second).getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("someclass", ((AttributeCondition) cond).getValue());
		attcond = (AttributeCondition) cond;
		assertEquals("someclass", attcond.getValue());
		assertEquals(".fooclass #descid.barclass .someclass", sel.toString());
	}

	@Test
	public void testParseSelectorComplexCombined2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".barclass#otherid.otherclass .someclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, second.getConditionType());
		assertEquals("otherclass", ((AttributeCondition) second).getValue());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_AND_CONDITION, first.getConditionType());
		CombinatorCondition combcond = (CombinatorCondition) first;
		second = combcond.getSecondCondition();
		assertEquals(Condition.SAC_ID_CONDITION, second.getConditionType());
		assertEquals("otherid", ((AttributeCondition) second).getValue());
		first = combcond.getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, first.getConditionType());
		assertEquals("barclass", ((AttributeCondition) first).getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("someclass", ((AttributeCondition) cond).getValue());
		assertEquals(".barclass#otherid.otherclass .someclass", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("li+span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("li", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSibling() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid + span:empty");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("empty", ((AttributeCondition) cond).getLocalName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid+span:empty");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("empty", ((AttributeCondition) cond).getLocalName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".myclass:foo+.bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond2).getLocalName());
		assertEquals(".myclass:foo+.bar", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingUniversal() throws CSSException, IOException {
		SelectorList selist = parseSelectors("*+span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, first.getSelectorType());
		assertEquals("*", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingUniversalWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("* + span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, first.getSelectorType());
		assertEquals("*", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingError() throws CSSException, IOException {
		try {
			parseSelectors("#id ++p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError2() throws CSSException, IOException {
		try {
			parseSelectors("#id|+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError3() throws CSSException, IOException {
		try {
			parseSelectors("#id@+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError4() throws CSSException, IOException {
		try {
			parseSelectors("#id>+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError5() throws CSSException, IOException {
		try {
			parseSelectors("#id+~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorSubsequentSibling() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid ~ span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("#exampleid~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingNoSpaces() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#exampleid~span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("#exampleid~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("li~span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("li", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSibling2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo .bar.class~.otherclass li:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, ancestor.getSelectorType());
		Selector first = ((CombinatorSelector) ancestor).getSelector(); // .foo .bar.class
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, first.getSelectorType());
		Selector ancestorfirst = ((CombinatorSelector) first).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestorfirst.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestorfirst).getCondition(); // foo
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		Selector simplefirst = ((CombinatorSelector) first).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simplefirst.getSelectorType());
		cond = ((ConditionalSelector) simplefirst).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("class", ((AttributeCondition) cond2).getValue());
		SimpleSelector sibling = ((CombinatorSelector) ancestor).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition condsibling = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, condsibling.getConditionType());
		assertEquals("otherclass", ((AttributeCondition) condsibling).getValue());
		//
		Selector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		PositionalCondition pcond = (PositionalCondition) cond;
		assertEquals(1, pcond.getOffset());
		assertEquals(0, pcond.getFactor());
		simple = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("li", ((ElementSelector) simple).getLocalName());
		assertEquals(".foo .bar.class~.otherclass li:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingError() throws CSSException, IOException {
		try {
			parseSelectors("#id~~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorSubsequentSiblingError2() throws CSSException, IOException {
		try {
			parseSelectors("#id>~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorColumnCombinatorElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("col||td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("col", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("col||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorElementError() throws CSSException, IOException {
		try {
			parseSelectors("col||");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorColumnCombinatorElementError2() throws CSSException, IOException {
		try {
			parseSelectors("col||,p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorColumnCombinator() throws CSSException, IOException {
		SelectorList selist = parseSelectors("col.foo||td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorWS() throws CSSException, IOException {
		SelectorList selist = parseSelectors("col.foo || td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancSimple.getSelectorType());
		assertEquals("col", ((ElementSelector) ancSimple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorCombinators1() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".ancestor .parent>.child ~ .childsibling:foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("childsibling", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond2).getLocalName());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertNotNull(first);
		assertEquals(Selector.SAC_CHILD_SELECTOR, first.getSelectorType());
		SimpleSelector simple = ((CombinatorSelector) first).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("child", ((AttributeCondition) cond).getValue());
		first = ((CombinatorSelector) first).getSelector();
		assertNotNull(first);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, first.getSelectorType());
		simple = ((CombinatorSelector) first).getSecondSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("parent", ((AttributeCondition) cond).getValue());
		first = ((CombinatorSelector) first).getSelector();
		assertNotNull(first);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("ancestor", ((AttributeCondition) cond).getValue());
		assertEquals(".ancestor .parent>.child~.childsibling:foo", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElement() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p::first-line");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("first-line", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementQuirk() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p::-webkit-foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("-webkit-foo", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::-webkit-foo", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementError() throws CSSException, IOException {
		try {
			parseSelectors("p::first&line");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementError2() throws CSSException, IOException {
		try {
			parseSelectors("p::9first-line");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementError3() throws CSSException, IOException {
		try {
			parseSelectors("::first-line()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementQuirkError() throws CSSException, IOException {
		try {
			parseSelectors("::-webkit-foo()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementOld() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:first-line");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("first-line", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPseudoclassed() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p::first-letter:hover");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_PSEUDO_ELEMENT_CONDITION, first.getConditionType());
		assertEquals("first-letter", ((AttributeCondition) first).getLocalName());
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, second.getConditionType());
		assertEquals("hover", ((AttributeCondition) second).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::first-letter:hover", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClass() throws CSSException, IOException {
		SelectorList selist = parseSelectors("div:blank");
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
		assertEquals("div:blank", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassError() throws CSSException, IOException {
		try {
			parseSelectors("div:blank&");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassError2() throws CSSException, IOException {
		try {
			parseSelectors("div:9blank");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassArgument() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:dir(ltr)");
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
		assertEquals("p:dir(ltr)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassDirError() throws CSSException, IOException {
		try {
			parseSelectors(":dir()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassDirError2() throws CSSException, IOException {
		try {
			parseSelectors(":dir(,)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChild1() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(1)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(1)", sel.toString());
		Selector sel2 = parseSelectors(sel.toString()).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildError() throws CSSException, IOException {
		try {
			parseSelectors(":first-child()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildError2() throws CSSException, IOException {
		try {
			parseSelectors(":first-child(even)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassLastChild() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":last-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":last-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildList() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(1),:first-child");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		assertEquals(":nth-child(1)", selist.item(0).toString());
		assertEquals(":first-child", selist.item(1).toString());
	}

	@Test
	public void testParseSelectorPseudoClassLastChildList() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-last-child(1),:last-child");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		assertEquals(":nth-last-child(1)", selist.item(0).toString());
		assertEquals(":last-child", selist.item(1).toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyChild() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:only-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ONLY_CHILD_CONDITION, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:only-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyChild2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("*:only-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ONLY_CHILD_CONDITION, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(":only-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	/*
	 * web-platform-tests/wpt/master/css/selectors/anplusb-selector-parsing.html
	 */
	@Test
	public void testParseSelectorPseudoClassNthChildError2() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n - 1 2)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError3() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n - b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError4() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n-+1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError5() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n+-1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError6() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n +-1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError7() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n +- 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError8() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n -+ 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError9() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n + - 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError10() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n - + 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError11() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n -1n)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError12() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n - +b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError13() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n -b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError14() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError15() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(n 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError16() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(- - 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthEven() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(even)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		String s = sel.toString();
		assertEquals(":nth-child(even)", s);
		Selector sel2 = parseSelectors(s).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassNthOdd() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(odd)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		String s = sel.toString();
		assertEquals(":nth-child(odd)", s);
		Selector sel2 = parseSelectors(s).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassNthKeywords() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(even),:nth-child(2n),:nth-child(odd),:nth-child(2n+1)");
		assertNotNull(selist);
		assertEquals(4, selist.getLength());
		assertEquals(":nth-child(even)", selist.item(0).toString());
		assertEquals(":nth-child(2n)", selist.item(1).toString());
		assertEquals(":nth-child(odd)", selist.item(2).toString());
		assertEquals(":nth-child(2n+1)", selist.item(3).toString());
	}

	@Test
	public void testParseSelectorPseudoClassNth() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(5)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-child(5)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAn() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(10n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnB() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(10n+9)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(9, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n+9)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBcr() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(10n\n+9)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(9, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n+9)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBzero() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(10n+0)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnB2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(n+2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(1, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-child(n+2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-last-child(5)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(5)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-last-child(3n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(3, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(3n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast3() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-last-child(-n+2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(-1, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(-n+2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast4() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-last-child(2n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOf() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(5 of p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-child(5 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLastOf() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-last-child(5 of p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-last-child(5 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBOf() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-child(6n+3 of p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(3, ((PositionalCondition) cond).getOffset());
		assertEquals(6, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-child(6n+3 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfBad() throws CSSException, IOException {
		try {
			parseSelectors(":nth-child(5 of)");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassFirstOfType() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":first-of-type");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isOfType());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertEquals(":first-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":nth-of-type(2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-of-type(2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:nth-of-type(2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-of-type(2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:nth-of-type(2n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-of-type(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassLastOfType() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":last-of-type");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		assertEquals(":last-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLastOfType() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:nth-last-of-type(2n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-last-of-type(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyOfType() throws CSSException, IOException {
		SelectorList selist = parseSelectors("p:only-of-type");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ONLY_TYPE_CONDITION, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:only-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorCombined() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".exampleclass:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("exampleclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond2.getConditionType());
		assertEquals(1, ((PositionalCondition) cond2).getOffset());
		assertFalse(((PositionalCondition) cond2).isOfType());
		assertEquals(".exampleclass:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedIdPseudoclass() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#example-ID:foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond1.getConditionType());
		assertEquals("example-ID", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond2).getLocalName());
		assertEquals("#example-ID:foo", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedDoubleId() throws CSSException, IOException {
		SelectorList selist = parseSelectors("#foo#bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond1.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		assertEquals("#foo#bar", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedAttributes() throws CSSException, IOException {
		SelectorList selist = parseSelectors("span[class=\"example\"][foo=\"bar\"],:rtl *");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond1.getConditionType());
		assertEquals("class", ((AttributeCondition) cond1).getLocalName());
		assertEquals("example", ((AttributeCondition) cond1).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond2.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond2).getLocalName());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		assertEquals("span[class=\"example\"][foo=\"bar\"]", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not(p.foo, span:first-child, div a)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
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
		assertEquals(":not(p.foo,span:first-child,div a)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot2() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not([disabled],.foo,[type=\"submit\"])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList args = ((ArgumentCondition) cond).getSelectors();
		assertEquals(3, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("disabled", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		arg = args.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		arg = args.item(2);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("type", ((AttributeCondition) cond).getLocalName());
		assertEquals("submit", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals(":not([disabled],.foo,[type=\"submit\"])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot3() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo .myclass:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals(".foo .myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot4() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo+.myclass:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector sibling = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals(".foo+.myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot5() throws CSSException, IOException {
		SelectorList selist = parseSelectors(".foo~.myclass:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector sibling = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals(".foo~.myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot6() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not(:visited,:hover)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(2, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond0.getConditionType());
		assertEquals("visited", ((AttributeCondition) cond0).getLocalName());
		Selector item1 = arglist.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item1.getSelectorType());
		Condition cond1 = ((ConditionalSelector) item1).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("hover", ((AttributeCondition) cond1).getLocalName());
		assertEquals(":not(:visited,:hover)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot7() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not(:lang(en))");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(Condition.SAC_LANG_CONDITION, cond0.getConditionType());
		assertEquals("en", ((LangCondition) cond0).getLang());
		assertEquals(":not(:lang(en))", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot8() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not([style*=\"background\"])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond0.getConditionType());
		assertEquals("style", ((AttributeCondition) cond0).getLocalName());
		assertEquals("background", ((AttributeCondition) cond0).getValue());
		assertEquals(":not([style*=\"background\"])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot9() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:not(.foo)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("html:not(.foo)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot10() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:not(.foo) body:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		arg = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_UNIVERSAL_SELECTOR, simple.getSelectorType());
		assertEquals("html:not(.foo) body:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot11() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:not(.foo) body:not(.bar) .myclass.otherclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals("html:not(.foo) body:not(.bar) .myclass.otherclass", sel.toString());
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, ancestor.getSelectorType());
	}

	@Test
	public void testParseSelectorPseudoClassNot12() throws CSSException, IOException {
		SelectorList selist = parseSelectors(":not([style*=\\*foo],\\64 iv,.\\39 z,#\\31 23)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(4, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond0.getConditionType());
		assertEquals("style", ((AttributeCondition) cond0).getLocalName());
		assertEquals("*foo", ((AttributeCondition) cond0).getValue());
		Selector item1 = arglist.item(1);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, item1.getSelectorType());
		assertEquals("div", ((ElementSelector) item1).getLocalName());
		Selector item2 = arglist.item(2);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item2.getSelectorType());
		Condition cond2 = ((ConditionalSelector) item2).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("9z", ((AttributeCondition) cond2).getValue());
		Selector item3 = arglist.item(3);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item3.getSelectorType());
		Condition cond3 = ((ConditionalSelector) item3).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond3.getConditionType());
		assertEquals("123", ((AttributeCondition) cond3).getValue());
		assertEquals(":not([style*=\"*foo\"],div,.\\39 z,#\\31 23)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNotEmpty() throws CSSException, IOException {
		try {
			parseSelectors(":not()");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNotEmpty2() throws CSSException, IOException {
		try {
			parseSelectors("foo:not()");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot() throws CSSException, IOException {
		try {
			parseSelectors(":not");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot2() throws CSSException, IOException {
		try {
			parseSelectors(":not:only-child");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot3() throws CSSException, IOException {
		try {
			parseSelectors("svg:not:only-child");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(8, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot4() throws CSSException, IOException {
		try {
			parseSelectors(":not p");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot5() throws CSSException, IOException {
		try {
			parseSelectors(":not::first-letter");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot6() throws CSSException, IOException {
		try {
			parseSelectors(":not.class");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot7() throws CSSException, IOException {
		try {
			parseSelectors(":not (.class)");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassHas() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:has(> img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(Selector.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(>img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas2() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:has(+ img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(Selector.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(+img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas3() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:has(div>img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(div>img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas4() throws CSSException, IOException {
		SelectorList selist = parseSelectors("html:has(~ img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(Selector.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(~img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas5() throws CSSException, IOException {
		SelectorList selist = parseSelectors("tr:has(|| img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("tr", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_COLUMN_COMBINATOR_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(Selector.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("tr:has(||img)", sel.toString());
	}

	private SelectorList parseSelectors(String selist) throws io.sf.carte.doc.style.css.nsac.CSSException, IOException {
		return parser.parseSelectors(new StringReader(selist));
	}

}
