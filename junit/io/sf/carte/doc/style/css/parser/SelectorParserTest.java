/*

 Copyright (c) 2005-2020, Carlos Amengual.

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
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.DescendantSelectorImpl;

public class SelectorParserTest {

	static CSSParser parser;

	@Before
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseSelectorUniversal() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("*"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("*", sel.toString());
	}

	@Test
	public void testParseSelectorElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("\\61 "));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("\\64 iv"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("9p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementIEHack() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("body*"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementIEHack2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("body\\ "));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorElementList() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p, span"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("p, p span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		DescendantSelector desc = (DescendantSelector) sel;
		Selector anc = desc.getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		SimpleSelector simple = desc.getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
	}

	@Test
	public void testParseSelectorElementList3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p, p .class"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		DescendantSelector desc = (DescendantSelector) sel;
		Selector anc = desc.getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		SimpleSelector simple = desc.getSimpleSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("class", ((AttributeCondition) cond).getValue());
	}

	@Test
	public void testParseSelectorElementListBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p,"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorListDuplicate() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p, p"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorListDuplicate2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".class, .class, ::first-line, ::first-line"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals(Condition2.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("first-line", ((AttributeCondition) cond).getLocalName());
	}

	@Test
	public void testParseStringSelectorElement() throws CSSException, IOException {
		SelectorList selist = parser.parseSelectors("p");
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
		InputSource source = new InputSource(new StringReader("!,p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorElementError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(",p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorElementError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p⁑"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorElementError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("⁑p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorElementError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p*"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorElementError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p* .foo"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorWSError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("display: none;"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorWSError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("# foo"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorWSError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo. bar"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttribute() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("[title],[foo]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertFalse(((AttributeCondition) cond).getSpecified());
		assertEquals("[title]", sel.toString());
		sel = selist.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertFalse(((AttributeCondition) cond).getSpecified());
		assertEquals("[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertFalse(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute2WS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ title ]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertFalse(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".foo[title]"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("*[title]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ti!tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ti$tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError2WS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ ti$tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ti^tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ti*tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ti~tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ti@tle]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[9title]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError8() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[.title]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeError9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[#title]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValue() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
	public void testParseSelectorAttributeValueNQ() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=hi]"));
		SelectorList selist = parser.parseSelectors(source);
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
	public void testParseSelectorAttributeValueNQEscaped() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\\*foo]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"*foo\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS1() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ title=\"hi\" ]"));
		SelectorList selist = parser.parseSelectors(source);
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
	public void testParseSelectorAttributeValue2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi:\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi:\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title = \"hi\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ title = \"hi\" ]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=hi i]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCIWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ title=hi i ]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" i]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCIuc() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" I]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" a]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" .]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" =]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" ;]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\" ii]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title=\"hi\"]()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeValueError7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[foo~class]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI1() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("input[type=text i][dir=auto]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("input[type=\"text\" i][dir=\"auto\"]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("input[type=text][dir=auto i]"));
		SelectorList selist = parser.parseSelectors(source);
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
		if (secondcond instanceof AttributeCondition2) {
			assertTrue(((AttributeCondition2) secondcond).hasFlag(AttributeCondition2.Flag.CASE_I));
		}
		assertTrue(((AttributeCondition) secondcond).getSpecified());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[type=\"text\"][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("input[foo=bar i][type=text i][dir=auto i]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCIDescendant() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div input[foo=bar i][type=text i][dir=auto i]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
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
		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCIAdjacent() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div+input[foo=bar i][type=text i][dir=auto i]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
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
		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div+input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCISibling() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div~input[foo=bar i][type=text i][dir=auto i]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
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
		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div~input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOf() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title~=\"hi\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS1() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[title ~=\"hi\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ title ~=\"hi\" ]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[title~=\"hi\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphen() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang|=\"en\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenNQ() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang|=en]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang |=\"en\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang|=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang | =\"en\"]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeHyphenError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[ lang | =\"en\" ]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorAttributeSubstring() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang*=\"CH\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang *=\"CH\"]"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang*=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffix() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang$=\"CH\"]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_ENDS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang$=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang $= \"CH\"]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_ENDS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang$=\"CH\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefix() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[style^=\"display:\"]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("style", ((AttributeCondition) cond).getLocalName());
		assertEquals("display:", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[style^=\"display:\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p[lang ^= \"en\"]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertTrue(((AttributeCondition) cond).getSpecified());
		assertEquals("p[lang^=\"en\"]", sel.toString());
	}

	@Test
	public void testParseSelectorLang() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:lang(en)"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("p:lang(zh, \"*-hant\")"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("p:lang(zh, '*-hant')"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("p:lang(es, fr, \\*-Latn)"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(":lang(zh, )"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorLangError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":lang( , \"*-hant\")"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(8, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClass() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".exampleclass"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("p.exampleclass"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".exampleclass⁑"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".foo\ud950\udc90"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".foo🚧"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".foo\\/1"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".foo,.bar"));
		SelectorList selist = parser.parseSelectors(source);
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
		SelectorList selist = parser.parseSelectors(".foo,.bar");
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
		InputSource source = new InputSource(new StringReader("p.example&class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.9class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example$class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example%class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example!class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example'class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example(class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError8() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example*class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError10() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example;class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError11() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example<class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError12() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example=class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError13() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example?class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError14() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example@class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError15() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example{class}"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError16() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example]"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError17() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example\\class"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("p.example^class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError19() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example|class"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(11, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError20() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example{"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError21() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example}"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError22() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p.example())"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError23() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".#example"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError24() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("..example"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassEscaped() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".foo\\(-\\.3\\)"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".\\31 foo\\&-.bar"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".\\31 jkl\\&-.bar"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(".\\31jkl\\&-.bar"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("#exampleid"));
		SelectorList selist = parser.parseSelectors(source);
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
	public void testParseSelectorTypeId() throws CSSException, IOException {
		SelectorList selist = parseSelectors("input#submit");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("submit", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input#submit", sel.toString());
	}

	@Test
	public void testParseSelectorIdEscapedChar() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#foo\\/1"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("#example&id"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#9example"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#.example"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("##example"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#example()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChild() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid > span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildNoSpaces() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid>span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttribute() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid>[foo]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("#exampleid>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttributeWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid> [foo]"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("#exampleid>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildUniversal() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("* > span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildUniversal2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("span > *"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("span", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("span>*", sel.toString());
	}

	@Test
	public void testParseSelectorChildError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id:>p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>+p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>*p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>*\\60"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>~p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":>p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError8() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>*+"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>#+"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError10() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>.+"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorDescendantElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("li span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("li", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("\\61  span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("a", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("a span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div \\61"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("a", ((ElementSelector) simple).getLocalName());
		assertEquals("div a", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("body \\64 iv"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("body", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("body div", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#\\31 exampleid\\/2 span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("1exampleid/2", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
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
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
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
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("myattr", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("[myattr] span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("* span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("* span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":rtl * "));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, ancSimple.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_PSEUDO_CLASS_CONDITION, cond.getConditionType());
		assertEquals("rtl", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals(":rtl *", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("* .foo"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		assertEquals("* .foo", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("span *"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("span", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("span *", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>~p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorDescendantError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>>p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorCombinedDescendant() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				"body:not(.foo)[id*=substring] .header"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("body", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, first.getConditionType());
		assertEquals(Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION, second.getConditionType());
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
		simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		Condition simplecond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, simplecond.getConditionType());
		assertEquals("header", ((AttributeCondition) simplecond).getValue());
		assertEquals("body:not(.foo)[id*=\"substring\"] .header", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantCombined() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				".fooclass #descid.barclass"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		AttributeCondition attcond = (AttributeCondition) cond;
		assertEquals("fooclass", attcond.getValue());
		simple = ((DescendantSelector) sel).getSimpleSelector();
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
		InputSource source = new InputSource(new StringReader(
				".fooclass #descid.barclass .someclass"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, ancestor.getSelectorType());
		Selector ancestor2 = ((DescendantSelector) ancestor).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor2.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor2).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor2).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		AttributeCondition attcond = (AttributeCondition) cond;
		assertEquals("fooclass", attcond.getValue());
		simple = ((DescendantSelector) ancestor).getSimpleSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_ID_CONDITION, first.getConditionType());
		assertEquals(Condition.SAC_CLASS_CONDITION, second.getConditionType());
		assertEquals("descid", ((AttributeCondition) first).getValue());
		assertEquals("barclass", ((AttributeCondition) second).getValue());
		simple = ((DescendantSelector) sel).getSimpleSelector();
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
		InputSource source = new InputSource(new StringReader(
				".barclass#otherid.otherclass .someclass"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
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
		simple = ((DescendantSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("someclass", ((AttributeCondition) cond).getValue());
		assertEquals(".barclass#otherid.otherclass .someclass", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("li+span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("li", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSibling() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid + span:empty"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
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
		InputSource source = new InputSource(new StringReader("#exampleid+span:empty"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
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
		InputSource source = new InputSource(new StringReader(".myclass:foo+.bar"));
		SelectorList selist = parser.parseSelectors(source);
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
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
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
		assertEquals(".myclass:foo+.bar", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingUniversal() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("*+span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, first.getSelectorType());
		assertEquals("*", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingUniversalWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("* + span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, first.getSelectorType());
		assertEquals("*", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id ++p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorSubsequentSibling() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid ~ span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("#exampleid~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingNoSpaces() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#exampleid~span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_ID_CONDITION, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("#exampleid~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("li~span"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector first = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, first.getSelectorType());
		assertEquals("li", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSibling2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				".foo .bar.class~.otherclass li:first-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, ancestor.getSelectorType());
		Selector first = ((SiblingSelector) ancestor).getSelector(); // .foo .bar.class
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, first.getSelectorType());
		Selector ancestorfirst = ((DescendantSelector) first).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestorfirst.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestorfirst).getCondition(); // foo
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		Selector simplefirst = ((DescendantSelector) first).getSimpleSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simplefirst.getSelectorType());
		cond = ((ConditionalSelector) simplefirst).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond2.getConditionType());
		assertEquals("class", ((AttributeCondition) cond2).getValue());
		SimpleSelector sibling = ((SiblingSelector) ancestor).getSiblingSelector();
		assertNotNull(sibling);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition condsibling = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, condsibling.getConditionType());
		assertEquals("otherclass", ((AttributeCondition) condsibling).getValue());
		//
		Selector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		PositionalCondition2 pcond = (PositionalCondition2) cond;
		assertEquals(1, pcond.getOffset());
		assertEquals(0, pcond.getFactor());
		simple = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("li", ((ElementSelector) simple).getLocalName());
		assertEquals(".foo .bar.class~.otherclass li:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id~~p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorSubsequentSiblingError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#id>~p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorColumnCombinatorElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("col||td"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("col", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((DescendantSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("col||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorElementError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("col||"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorColumnCombinatorElementError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("col||,p"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorColumnCombinator() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("col.foo||td"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorWS() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("col.foo || td"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorCombinators1() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(
				".ancestor .parent>.child ~ .childsibling:foo"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		SimpleSelector sibling = ((SiblingSelector) sel).getSiblingSelector();
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
		Selector first = ((SiblingSelector) sel).getSelector();
		assertNotNull(first);
		assertEquals(Selector.SAC_CHILD_SELECTOR, first.getSelectorType());
		SimpleSelector simple = ((DescendantSelectorImpl) first).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("child", ((AttributeCondition) cond).getValue());
		first = ((DescendantSelectorImpl) first).getAncestorSelector();
		assertNotNull(first);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, first.getSelectorType());
		simple = ((DescendantSelectorImpl) first).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("parent", ((AttributeCondition) cond).getValue());
		first = ((DescendantSelectorImpl) first).getAncestorSelector();
		assertNotNull(first);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, first.getSelectorType());
		cond = ((ConditionalSelector) first).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("ancestor", ((AttributeCondition) cond).getValue());
		assertEquals(".ancestor .parent>.child~.childsibling:foo", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElement() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p::first-line"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementQuirk() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p::-webkit-foo"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_PSEUDO_ELEMENT_CONDITION, cond.getConditionType());
		assertEquals("-webkit-foo", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::-webkit-foo", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p::first&line"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p::9first-line"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("::first-line()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoElementQuirkError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("::-webkit-foo()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoElementOld() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:first-line"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPseudoclassed() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p::first-letter:hover"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals("p::first-letter:hover", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClass() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div:blank"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("div:blank&"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("div:9blank"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassArgument() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:dir(ltr)"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(":dir()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassDirError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":dir(,)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChild1() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(1)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getPosition());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition2) cond).isOfType());
		assertEquals(":nth-child(1)", sel.toString());
		source = new InputSource(new StringReader(sel.toString()));
		Selector sel2 = parser.parseSelectors(source).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":first-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getPosition());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition2) cond).isOfType());
		assertEquals(":first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:first-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getPosition());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":first-child()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":first-child(even)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassLastChild() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":last-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(-1, ((PositionalCondition) cond).getPosition());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition2) cond).isOfType());
		assertEquals(":last-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildList() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader(":nth-child(1),:first-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		assertEquals(":nth-child(1)", selist.item(0).toString());
		assertEquals(":first-child", selist.item(1).toString());
	}

	@Test
	public void testParseSelectorPseudoClassLastChildList() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader(":nth-last-child(1),:last-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		assertEquals(":nth-last-child(1)", selist.item(0).toString());
		assertEquals(":last-child", selist.item(1).toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyChild() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:only-child"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("*:only-child"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_ONLY_CHILD_CONDITION, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(":only-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child()"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	/*
	 * web-platform-tests/wpt/master/css/selectors/anplusb-selector-parsing.html
	 */
	@Test
	public void testParseSelectorPseudoClassNthChildError2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n - 1 2)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n - b1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n-+1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n+-1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n +-1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n +- 1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError8() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n -+ 1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n + - 1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError10() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n - + 1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError11() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n -1n)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError12() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n - +b1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError13() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n -b1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError14() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n b1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError15() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n 1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError16() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(- - 1)"));
		try {
			parser.parseSelectors(source);
			fail("Must throw an exception");
		} catch (CSSParseException e) {}
	}

	@Test
	public void testParseSelectorPseudoClassNthEven() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(even)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition2) cond).getFactor());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition2) cond).isOfType());
		assertEquals(":nth-child(even)", sel.toString());
		source = new InputSource(new StringReader(sel.toString()));
		Selector sel2 = parser.parseSelectors(source).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassNthOdd() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(odd)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition2) cond).getFactor());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition2) cond).isOfType());
		assertEquals(":nth-child(odd)", sel.toString());
		source = new InputSource(new StringReader(sel.toString()));
		Selector sel2 = parser.parseSelectors(source).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassNthKeywords() throws CSSException, IOException {
		InputSource source = new InputSource(
				new StringReader(":nth-child(even),:nth-child(2n),:nth-child(odd),:nth-child(2n+1)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(4, selist.getLength());
		assertEquals(":nth-child(even)", selist.item(0).toString());
		assertEquals(":nth-child(2n)", selist.item(1).toString());
		assertEquals(":nth-child(odd)", selist.item(2).toString());
		assertEquals(":nth-child(2n+1)", selist.item(3).toString());
	}

	@Test
	public void testParseSelectorPseudoClassNth() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(5)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getPosition());
		assertEquals(5, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-child(5)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAn() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(10n)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertEquals(10, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertEquals(":nth-child(10n)", sel.toString());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnB() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(10n+9)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(9, ((PositionalCondition2) cond).getOffset());
		assertEquals(10, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertEquals(":nth-child(10n+9)", sel.toString());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBcr() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(10n\n+9)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(9, ((PositionalCondition2) cond).getOffset());
		assertEquals(10, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertEquals(":nth-child(10n+9)", sel.toString());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBzero() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(10n+0)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertEquals(10, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertEquals(":nth-child(10n)", sel.toString());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnB2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(n+2)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(2, ((PositionalCondition2) cond).getOffset());
		assertEquals(1, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-child(n+2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-last-child(5)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(-5, ((PositionalCondition) cond).getPosition());
		assertEquals(5, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(5)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-last-child(3n)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertEquals(3, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(3n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-last-child(-n+2)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition2) cond).getOffset());
		assertEquals(-1, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(-n+2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-last-child(2n)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertEquals(2, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition) cond).getType());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOf() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(5 of p)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getPosition());
		assertEquals(5, ((PositionalCondition2) cond).getOffset());
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
		assertEquals(":nth-child(5 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLastOf() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-last-child(5 of p)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(-5, ((PositionalCondition) cond).getPosition());
		assertEquals(5, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-last-child(5 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBOf() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(6n+3 of p)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(3, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(6, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).getType());
		SelectorList oflist = ((PositionalCondition2) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-child(6n+3 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfBad() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-child(5 of)"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassFirstOfType() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":first-of-type"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getPosition());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition2) cond).isOfType());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertEquals(":first-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":nth-of-type(2)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getPosition());
		assertEquals(2, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).getType());
		assertEquals(":nth-of-type(2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:nth-of-type(2)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getPosition());
		assertEquals(2, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).getType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-of-type(2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:nth-of-type(2n)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertEquals(2, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertTrue(((PositionalCondition2) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).getType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-of-type(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassLastOfType() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":last-of-type"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(-1, ((PositionalCondition) cond).getPosition());
		assertEquals(1, ((PositionalCondition2) cond).getOffset());
		assertEquals(0, ((PositionalCondition2) cond).getFactor());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		assertTrue(((PositionalCondition2) cond).isOfType());
		assertEquals(":last-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLastOfType() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("p:nth-last-of-type(2n)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition.SAC_POSITIONAL_CONDITION, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getPosition());
		assertEquals(0, ((PositionalCondition2) cond).getOffset());
		assertEquals(2, ((PositionalCondition2) cond).getFactor());
		assertTrue(((PositionalCondition) cond).getTypeNode());
		assertFalse(((PositionalCondition2) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).getType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-last-of-type(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorCombined() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".exampleclass:first-child"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals(1, ((PositionalCondition) cond2).getPosition());
		assertEquals(1, ((PositionalCondition2) cond2).getOffset());
		assertEquals(".exampleclass:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedIdPseudoclass() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("#example-ID:foo"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("#foo#bar"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader("span[class=\"example\"][foo=\"bar\"],:rtl *"));
		SelectorList selist = parser.parseSelectors(source);
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
		InputSource source = new InputSource(new StringReader(":not(p.foo, span:first-child, div a)"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals(":not(p.foo,span:first-child,div a)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not([disabled],.foo,[type=\"submit\"])"));
		SelectorList selist = parser.parseSelectors(source);
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
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("disabled", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		arg = args.item(1);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		arg = args.item(2);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_ATTRIBUTE_CONDITION, cond.getConditionType());
		assertEquals("type", ((AttributeCondition) cond).getLocalName());
		assertEquals("submit", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals(":not([disabled],.foo,[type=\"submit\"])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".foo .myclass:not(.bar)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((DescendantSelectorImpl) sel).getSimpleSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals(".foo .myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".foo+.myclass:not(.bar)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, sel.getSelectorType());
		Selector sibling = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals(".foo+.myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(".foo~.myclass:not(.bar)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, sel.getSelectorType());
		Selector sibling = ((SiblingSelector) sel).getSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((SiblingSelector) sel).getSiblingSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition.SAC_AND_CONDITION, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals(".foo~.myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not(:visited,:hover)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
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
		InputSource source = new InputSource(new StringReader(":not(:lang(en))"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
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
		InputSource source = new InputSource(new StringReader(":not([style*=\"background\"])"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond0.getConditionType());
		assertEquals("style", ((AttributeCondition) cond0).getLocalName());
		assertEquals("background", ((AttributeCondition) cond0).getValue());
		assertEquals(":not([style*=\"background\"])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot9() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:not(.foo)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html:not(.foo)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot10() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:not(.foo) body:not(.bar)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		simple = ((DescendantSelectorImpl) sel).getSimpleSelector();
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		arg = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(Condition.SAC_CLASS_CONDITION, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ANY_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html:not(.foo) body:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot11() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:not(.foo) body:not(.bar) .myclass.otherclass"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals("html:not(.foo) body:not(.bar) .myclass.otherclass", sel.toString());
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, sel.getSelectorType());
		Selector ancestor = ((DescendantSelector) sel).getAncestorSelector();
		assertEquals(Selector.SAC_DESCENDANT_SELECTOR, ancestor.getSelectorType());
	}

	@Test
	public void testParseSelectorPseudoClassNot12() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not([style*=\\*foo],\\64 iv,.\\39 z,#\\31 23)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(4, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION, cond0.getConditionType());
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
		InputSource source = new InputSource(new StringReader(":not()"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNotEmpty2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("foo:not()"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not:only-child"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("svg:not:only-child"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
			assertEquals(8, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not p"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not::first-letter"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot6() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not.class"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot7() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader(":not (.class)"));
		try {
			parser.parseSelectors(source);
			fail("Should throw an exception");
		} catch(CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassHas() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:has(> img)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((DescendantSelector) arg).getAncestorSelector();
		assertEquals(Selector2.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((DescendantSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(>img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas2() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:has(+ img)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_DIRECT_ADJACENT_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((SiblingSelector) arg).getSelector();
		assertEquals(Selector2.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((SiblingSelector) arg).getSiblingSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(+img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas3() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:has(div>img)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector.SAC_CHILD_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((DescendantSelector) arg).getAncestorSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		simple = ((DescendantSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(div>img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas4() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("html:has(~ img)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((SiblingSelector) arg).getSelector();
		assertEquals(Selector2.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((SiblingSelector) arg).getSiblingSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(~img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas5() throws CSSException, IOException {
		InputSource source = new InputSource(new StringReader("tr:has(|| img)"));
		SelectorList selist = parser.parseSelectors(source);
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(Selector.SAC_CONDITIONAL_SELECTOR, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("tr", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, arg.getSelectorType());
		Selector ancestor = ((DescendantSelector) arg).getAncestorSelector();
		assertEquals(Selector2.SAC_SCOPE_SELECTOR, ancestor.getSelectorType());
		simple = ((DescendantSelector) arg).getSimpleSelector();
		assertEquals(Selector.SAC_ELEMENT_NODE_SELECTOR, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("tr:has(||img)", sel.toString());
	}

	private SelectorList parseSelectors(String selist) throws CSSException, IOException {
		return parser.parseSelectors(new InputSource(new StringReader(selist)));
	}

}
