/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

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

public class SelectorParserTest {

	private CSSParser parser;

	@BeforeEach
	public void setUp() {
		parser = new CSSParser();
	}

	@Test
	public void testParseSelectorError() throws CSSException {
		try {
			parseSelectors("?foo");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorError3() throws CSSException {
		try {
			parseSelectors("&foo");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorError4() throws CSSException {
		try {
			parseSelectors("%foo");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorError5() throws CSSException {
		try {
			parseSelectors("!foo");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorUniversal() throws CSSException {
		SelectorList selist = parseSelectors("*");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.UNIVERSAL, sel.getSelectorType());
		assertEquals("*", sel.toString());
	}

	@Test
	public void testParseSelectorElement() throws CSSException {
		SelectorList selist = parseSelectors("p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementLF() throws CSSException {
		SelectorList selist = parseSelectors("p\n");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementHighChar() throws CSSException {
		SelectorList selist = parseSelectors("\u208c");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("\u208c", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("\u208c", sel.toString());
	}

	@Test
	public void testParseSelectorElementControlHighCharError() throws CSSException {
		// High control characters are excluded in XML and HTML for security reasons
		try {
			parseSelectors("\u009e");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorElementSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("\ud83c\udf52");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("\ud83c\udf52", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("\ud83c\udf52", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped() throws CSSException {
		SelectorList selist = parseSelectors("\\61 ");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("a", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("a", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped2() throws CSSException {
		SelectorList selist = parseSelectors("\\64 iv");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("div", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("div", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped3() throws CSSException {
		SelectorList selist = parseSelectors("\\31 23");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("123", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("\\31 23", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped4() throws CSSException {
		SelectorList selist = parseSelectors("\\.foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals(".foo", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("\\.foo", sel.toString());
	}

	@Test
	public void testParseSelectorElementEscaped5() throws CSSException {
		SelectorList selist = parseSelectors("\\\\foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("\\foo", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("\\\\foo", sel.toString());
	}

	@Test
	public void testParseSelectorElementBad() throws CSSException {
		try {
			parseSelectors("9p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(1, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorElementIEHack() throws CSSException {
		try {
			parseSelectors("body*");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorElementIEHack2() throws CSSException {
		try {
			parseSelectors("body\\ ");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorElementList() throws CSSException {
		SelectorList selist = parseSelectors("p, span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("span", ((ElementSelector) sel).getLocalName());

		SelectorList selist2 = parseSelectors("div");
		// List operations
		assertFalse(selist.contains(selist2.item(0)));
		assertFalse(selist.containsAll(selist2));
		// Replace
		assertEquals("span", selist.replace(1, selist2.item(0)).toString());
		assertEquals("p,div", selist.toString());
		sel = selist.item(1);
		assertSame(sel, selist2.item(0));
		// Replace error
		assertThrows(DOMException.class, () -> {
			selist.replace(-1, selist2.item(0));
		});
		assertThrows(DOMException.class, () -> {
			selist.replace(4, selist2.item(0));
		});
		assertThrows(NullPointerException.class, () -> {
			selist.replace(1, null);
		});
		// List operations
		assertTrue(selist.contains(selist2.item(0)));
		assertTrue(selist.containsAll(selist2));
	}

	@Test
	public void testParseSelectorElementList2() throws CSSException {
		SelectorList selist = parseSelectors("p, p span");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		CombinatorSelector desc = (CombinatorSelector) sel;
		Selector anc = desc.getSelector();
		assertEquals(SelectorType.ELEMENT, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		SimpleSelector simple = desc.getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
	}

	@Test
	public void testParseSelectorElementList3() throws CSSException {
		SelectorList selist = parseSelectors("p, p .class");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		CombinatorSelector desc = (CombinatorSelector) sel;
		Selector anc = desc.getSelector();
		assertEquals(SelectorType.ELEMENT, anc.getSelectorType());
		assertEquals("p", ((ElementSelector) anc).getLocalName());
		SimpleSelector simple = desc.getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("class", ((AttributeCondition) cond).getValue());
	}

	@Test
	public void testParseSelectorElementListBad() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("p,"));
		assertEquals(3, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorListDuplicate() throws CSSException {
		SelectorList selist = parseSelectors("p, p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorListDuplicate2() throws CSSException {
		SelectorList selist = parseSelectors(".class, .class, ::first-line, ::first-line");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("class", ((AttributeCondition) cond).getValue());
		sel = selist.item(1);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("first-line", ((PseudoCondition) cond).getName());
	}

	@Test
	public void testParseStringSelectorElement() throws CSSException {
		SelectorList selist = parseSelectors("p");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("p", ((ElementSelector) sel).getLocalName());
		assertNull(((ElementSelector) sel).getNamespaceURI());
		assertEquals("p", sel.toString());
	}

	@Test
	public void testParseSelectorElementError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("!,p"));
		assertEquals(1, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorElementError2() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors(",p"));
		assertEquals(1, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorElementError3() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("p⁑"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorElementError4() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("⁑p"));
		assertEquals(1, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorElementError5() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("p*"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorElementError6() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("p* .foo"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorElementErrorDoubleEscape() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("\\\\&p"));
		assertEquals(4, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorWSError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("display: none;"));
		assertEquals(9, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorWSError2() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("# foo"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorWSError3() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("foo. bar"));
		assertEquals(5, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorAttribute() throws CSSException {
		SelectorList selist = parseSelectors("[title],[foo]");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("[title]", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute2() throws CSSException {
		SelectorList selist = parseSelectors("p[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute2WS() throws CSSException {
		SelectorList selist = parseSelectors("p[ title ]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute3() throws CSSException {
		SelectorList selist = parseSelectors(".foo[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getValue());
		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("title", ((AttributeCondition) secondcond).getLocalName());
		assertNull(((AttributeCondition) secondcond).getNamespaceURI());
		assertEquals(".foo[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttribute4() throws CSSException {
		SelectorList selist = parseSelectors("p[_a-b]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("_a-b", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_a-b]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeUniversal() throws CSSException {
		SelectorList selist = parseSelectors("*[title]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("[title]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEscaped() throws CSSException {
		SelectorList selist = parseSelectors("\\.p[\\31 23]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("123", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals(".p", ((ElementSelector) simple).getLocalName());
		assertEquals("\\.p[\\31 23]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeError() throws CSSException {
		try {
			parseSelectors("p[ti!tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError2() throws CSSException {
		try {
			parseSelectors("p[ti$tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError2WS() throws CSSException {
		try {
			parseSelectors("p[ ti$tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError3() throws CSSException {
		try {
			parseSelectors("p[ti^tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError4() throws CSSException {
		try {
			parseSelectors("p[ti*tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError5() throws CSSException {
		try {
			parseSelectors("p[ti~tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError6() throws CSSException {
		try {
			parseSelectors("p[ti@tle]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError7() throws CSSException {
		try {
			parseSelectors("p[9title]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError8() throws CSSException {
		try {
			parseSelectors("p[.title]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeError9() throws CSSException {
		try {
			parseSelectors("p[#title]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("[]"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorAttributeHighCharError() throws CSSException {
		try {
			parseSelectors("p[\u26a1]"); // ⚡ high voltage sign
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValue() throws CSSException {
		SelectorList selist = parseSelectors("p[title=\"hi\"]");
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
		assertEquals("p[title=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueNQ() throws CSSException {
		SelectorList selist = parseSelectors("p[title=hi]");
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
		assertEquals("p[title=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueNQEscaped() throws CSSException {
		SelectorList selist = parseSelectors("p[title=\\*foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("*foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"*foo\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS1() throws CSSException {
		SelectorList selist = parseSelectors("p[ title=\"hi\" ]");
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
		assertEquals("p[title=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValue2() throws CSSException {
		SelectorList selist = parseSelectors("p[title=\"hi:\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi:", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi:\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS2() throws CSSException {
		SelectorList selist = parseSelectors("p[title = \"hi\"]");
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
		assertEquals("p[title=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueWS3() throws CSSException {
		SelectorList selist = parseSelectors("p[ title = \"hi\" ]");
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
		assertEquals("p[title=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI() throws CSSException {
		SelectorList selist = parseSelectors("p[title=hi i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCIWS() throws CSSException {
		SelectorList selist = parseSelectors("p[ title=hi i ]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCI2() throws CSSException {
		SelectorList selist = parseSelectors("p[title=\"hi\" i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCIuc() throws CSSException {
		SelectorList selist = parseSelectors("p[title=\"hi\" I]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_I));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCS() throws CSSException {
		SelectorList selist = parseSelectors("p[title=hi s]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_S));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" s]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueCSuc() throws CSSException {
		SelectorList selist = parseSelectors("p[title=hi S]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		if (cond instanceof AttributeCondition) {
			assertTrue(((AttributeCondition) cond).hasFlag(AttributeCondition.Flag.CASE_S));
		}
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"hi\" s]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueEscaped() throws CSSException {
		SelectorList selist = parseSelectors("p[title=\\208c \\68]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("\u208ch", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=₌h]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52=\"\uD83C\uDFAF\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("\ud83c\udfaf", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52=\ud83c\udfaf]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueEmpty() throws CSSException {
		SelectorList selist = parseSelectors("p[ title=\"\" ]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertNull(((AttributeCondition) cond).getNamespaceURI());
		assertEquals(0, ((AttributeCondition) cond).getValue().length());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title=\"\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueFlag_Esc_Error() throws IOException {
		try {
			parseSelectors("p[title=hi \\73]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueFlagUnknownError() throws IOException {
		try {
			parseSelectors("p[title=hi h]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueFlagUnknownError2() throws IOException {
		try {
			parseSelectors("p[title=hi foo]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueBadFlagAsteriskError() throws IOException {
		try {
			parseSelectors("p[title=hi *]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueStrFlagUnknownError() throws IOException {
		try {
			parseSelectors("p[title=\"hi\" a]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueBadFlagDotError() throws IOException {
		try {
			parseSelectors("p[title=\"hi\" .]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueBadFlagEqualsError() throws IOException {
		try {
			parseSelectors("p[title=\"hi\" =]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueBadFlagSemicolonError() throws IOException {
		try {
			parseSelectors("p[title=\"hi\" ;]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueStrFlagUnknownError2() throws IOException {
		try {
			parseSelectors("p[title=\"hi\" ii]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueStrNextSelectorError() throws IOException {
		try {
			parseSelectors("p[title=\"hi\"]()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(14, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeValueCharError() throws IOException {
		try {
			parseSelectors("p[foo~class]");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI1() throws CSSException {
		SelectorList selist = parseSelectors("input[type=text i][dir=auto]");
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
		assertTrue(firstcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		assertTrue(secondcond instanceof AttributeCondition);
		assertFalse(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[type=\"text\" i][dir=auto]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI2() throws CSSException {
		SelectorList selist = parseSelectors("input[type=text][dir=auto i]");
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
		assertTrue(firstcond instanceof AttributeCondition);
		assertFalse(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) secondcond).getValue());
		assertTrue(secondcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[type=text][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCI3() throws CSSException {
		SelectorList selist = parseSelectors("input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());

		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		assertTrue(firstcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();

		secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		assertTrue(secondcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition thirdcond = ((CombinatorCondition) cond).getCondition(2);
		assertEquals(ConditionType.ATTRIBUTE, thirdcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) thirdcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) thirdcond).getValue());
		assertTrue(thirdcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) thirdcond).hasFlag(AttributeCondition.Flag.CASE_I));

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCIDescendant() throws CSSException {
		SelectorList selist = parseSelectors("div input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());

		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		assertTrue(firstcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();

		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		assertTrue(secondcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition thirdcond = ((CombinatorCondition) cond).getCondition(2);
		assertEquals(ConditionType.ATTRIBUTE, thirdcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) thirdcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) thirdcond).getValue());
		assertTrue(thirdcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) thirdcond).hasFlag(AttributeCondition.Flag.CASE_I));

		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCIAdjacent() throws CSSException {
		SelectorList selist = parseSelectors("div+input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());

		Condition firstcond = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, firstcond.getConditionType());
		assertEquals("foo", ((AttributeCondition) firstcond).getLocalName());
		assertEquals("bar", ((AttributeCondition) firstcond).getValue());
		assertTrue(firstcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) firstcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		assertTrue(secondcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition thirdcond = ((CombinatorCondition) cond).getCondition(2);
		assertEquals(ConditionType.ATTRIBUTE, thirdcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) thirdcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) thirdcond).getValue());
		assertTrue(thirdcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) thirdcond).hasFlag(AttributeCondition.Flag.CASE_I));

		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div+input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorCombinatorAttributeValueCISibling() throws CSSException {
		SelectorList selist = parseSelectors("div~input[foo=bar i][type=text i][dir=auto i]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());

		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();

		assertEquals(ConditionType.ATTRIBUTE, cond1.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond1).getLocalName());
		assertEquals("bar", ((AttributeCondition) cond1).getValue());
		assertTrue(cond1 instanceof AttributeCondition);
		assertTrue(((AttributeCondition) cond1).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition secondcond = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, secondcond.getConditionType());
		assertEquals("type", ((AttributeCondition) secondcond).getLocalName());
		assertEquals("text", ((AttributeCondition) secondcond).getValue());
		assertTrue(secondcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) secondcond).hasFlag(AttributeCondition.Flag.CASE_I));

		Condition thirdcond = ((CombinatorCondition) cond).getCondition(2);
		assertEquals(ConditionType.ATTRIBUTE, thirdcond.getConditionType());
		assertEquals("dir", ((AttributeCondition) thirdcond).getLocalName());
		assertEquals("auto", ((AttributeCondition) thirdcond).getValue());
		assertTrue(thirdcond instanceof AttributeCondition);
		assertTrue(((AttributeCondition) thirdcond).hasFlag(AttributeCondition.Flag.CASE_I));

		SimpleSelector simple2 = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple2.getSelectorType());
		assertEquals("input", ((ElementSelector) simple2).getLocalName());
		assertEquals("div~input[foo=\"bar\" i][type=\"text\" i][dir=\"auto\" i]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOf() throws CSSException {
		SelectorList selist = parseSelectors("p[title~=\"hi ho\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("title", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi ho", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[title~=\"hi ho\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS1() throws CSSException {
		SelectorList selist = parseSelectors("p[title ~=\"hi\"]");
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
		assertEquals("p[title~=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfWS2() throws CSSException {
		SelectorList selist = parseSelectors("p[ title ~=\"hi\" ]");
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
		assertEquals("p[title~=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c~=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c~=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52~=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeValueOneOfSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52~=\"hi\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONE_OF_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("hi", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52~=hi]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeOneOfErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("[~=\"hi\"]"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorAttributeHyphen() throws CSSException {
		SelectorList selist = parseSelectors("p[lang|=\"en,fr\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en,fr", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang|=\"en,fr\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenNQ() throws CSSException {
		SelectorList selist = parseSelectors("p[lang|=en]");
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
		assertEquals("p[lang|=en]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenWS() throws CSSException {
		SelectorList selist = parseSelectors("p[lang |=\"en,fr\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en,fr", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang|=\"en,fr\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c|=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c|=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c|=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c|=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52|=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52|=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52|=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGIN_HYPHEN_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52|=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeHyphenError() throws CSSException {
		try {
			parseSelectors("p[lang | =\"en\"]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeHyphenError2() throws CSSException {
		try {
			parseSelectors("p[ lang | =\"en\" ]");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(11, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorAttributeHyphenErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("[|=\"hi\"]"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorAttributeSubstring() throws CSSException {
		SelectorList selist = parseSelectors("p[lang*=\"CH\"]");
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
		assertEquals("p[lang*=CH]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringWS() throws CSSException {
		SelectorList selist = parseSelectors("p[lang *=\"CH\"]");
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
		assertEquals("p[lang*=CH]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c*=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c*=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c*=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c*=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52*=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52*=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52*=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52*=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeSubstringErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("[*=\"hi\"]"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorAttributeEndSuffix() throws CSSException {
		SelectorList selist = parseSelectors("p[lang$=\"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ENDS_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang$=CH]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixWS() throws CSSException {
		SelectorList selist = parseSelectors("p[lang $= \"CH\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ENDS_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("CH", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang$=CH]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c$=\"2/3\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ENDS_ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("2/3", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c$=\"2/3\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c$=\"2/3\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ENDS_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("2/3", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c$=\"2/3\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52$=\"2/3\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ENDS_ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("2/3", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52$=\"2/3\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndSuffixSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52$=\"2/3\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ENDS_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("2/3", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52$=\"2/3\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeEndErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("[$=\"hi\"]"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefix() throws CSSException {
		SelectorList selist = parseSelectors("p[style^=\"display:\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGINS_ATTRIBUTE, cond.getConditionType());
		assertEquals("style", ((AttributeCondition) cond).getLocalName());
		assertEquals("display:", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[style^=\"display:\"]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixWS() throws CSSException {
		SelectorList selist = parseSelectors("p[lang ^= \"en\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGINS_ATTRIBUTE, cond.getConditionType());
		assertEquals("lang", ((AttributeCondition) cond).getLocalName());
		assertEquals("en", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[lang^=en]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixHighChar() throws CSSException {
		SelectorList selist = parseSelectors("p[\u208c^=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGINS_ATTRIBUTE, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\u208c^=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixHighChar2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\u208c^=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGINS_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\u208c", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\u208c^=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("p[\ud83c\udf52^=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGINS_ATTRIBUTE, cond.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[\ud83c\udf52^=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBeginPrefixSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors("p[_\ud83c\udf52^=\"foo\"]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.BEGINS_ATTRIBUTE, cond.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p[_\ud83c\udf52^=foo]", sel.toString());
	}

	@Test
	public void testParseSelectorAttributeBegiErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("[^=\"hi\"]"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorLang() throws CSSException {
		SelectorList selist = parseSelectors("p:lang(en)");
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
		assertEquals("p:lang(en)", sel.toString());
	}

	@Test
	public void testParseSelectorLang2() throws CSSException {
		SelectorList selist = parseSelectors("p:lang(zh, \"*-hant\")");
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
		assertEquals("p:lang(zh,\"*-hant\")", sel.toString());
	}

	@Test
	public void testParseSelectorLang3() throws CSSException {
		SelectorList selist = parseSelectors("p:lang(zh, '*-hant')");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.LANG, cond.getConditionType());
		assertEquals("zh,'*-hant'", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:lang(zh,'*-hant')", sel.toString());
	}

	@Test
	public void testParseSelectorLang4() throws CSSException {
		SelectorList selist = parseSelectors("p:lang(es, fr, \\*-Latn)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.LANG, cond.getConditionType());
		assertEquals("es,fr,*-Latn", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:lang(es,fr,\\*-Latn)", sel.toString());
	}

	@Test
	public void testParseSelectorLang_Variant() throws CSSException {
		SelectorList selist = parseSelectors("p:lang(fr-be)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.LANG, cond.getConditionType());
		assertEquals("fr-be", ((LangCondition) cond).getLang());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:lang(fr-be)", sel.toString());
	}

	@Test
	public void testParseSelectorLangError() throws CSSException {
		try {
			parseSelectors(":lang(zh, )");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorLangError2() throws CSSException {
		try {
			parseSelectors(":lang( , \"*-hant\")");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(8, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorLangErrorString() throws CSSException {
		try {
			parseSelectors(":lang(fr, '*-hant)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(11, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClass() throws CSSException {
		SelectorList selist = parseSelectors(".exampleclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("exampleclass", ((AttributeCondition) cond).getValue());
		assertEquals(".exampleclass", sel.toString());
	}

	@Test
	public void testParseSelectorClass2() throws CSSException {
		SelectorList selist = parseSelectors("p.exampleclass");
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
		assertEquals("p.exampleclass", sel.toString());
	}

	@Test
	public void testParseSelectorClass3() throws CSSException {
		SelectorList selist = parseSelectors("._123");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("_123", ((AttributeCondition) cond).getValue());
		assertEquals("._123", sel.toString());
	}

	@Test
	public void testParseSelectorClassOtherChar() throws CSSException {
		SelectorList selist = parseSelectors(".exampleclass⁑");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("exampleclass⁑", ((AttributeCondition) cond).getValue());
		assertEquals(".exampleclass⁑", sel.toString());
	}

	@Test
	public void testParseSelectorClassHighChar() throws CSSException {
		SelectorList selist = parseSelectors(".foo\u208c");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo\u208c", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\u208c", sel.toString());
	}

	@Test
	public void testParseSelectorClassSurrogate() throws CSSException {
		SelectorList selist = parseSelectors(".foo\ud950\udc90");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo\ud950\udc90", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\ud950\udc90", sel.toString());
	}

	@Test
	public void testParseSelectorClassSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors(".foo🚧");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo🚧", ((AttributeCondition) cond).getValue());
		assertEquals(".foo🚧", sel.toString());
	}

	@Test
	public void testParseSelectorListClass() throws CSSException {
		SelectorList selist = parseSelectors(".foo,.bar");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		assertEquals(".foo", sel.toString());
		sel = selist.item(1);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		assertEquals(".bar", sel.toString());
	}

	@Test
	public void testParseSelectorListClassError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("p.,.bar"));
		assertEquals(3, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorClassError() throws CSSException {
		try {
			parseSelectors("p.example&class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError2() throws CSSException {
		try {
			parseSelectors("p.9class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError3() throws CSSException {
		try {
			parseSelectors("p.example$class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError4() throws CSSException {
		try {
			parseSelectors("p.example%class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError5() throws CSSException {
		try {
			parseSelectors("p.example!class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError6() throws CSSException {
		try {
			parseSelectors("p.example'class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError7() throws CSSException {
		try {
			parseSelectors("p.example(class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError8() throws CSSException {
		try {
			parseSelectors("p.example)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError9() throws CSSException {
		try {
			parseSelectors("p.example*class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError10() throws CSSException {
		try {
			parseSelectors("p.example;class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError11() throws CSSException {
		try {
			parseSelectors("p.example<class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError12() throws CSSException {
		try {
			parseSelectors("p.example=class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError13() throws CSSException {
		try {
			parseSelectors("p.example?class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError14() throws CSSException {
		try {
			parseSelectors("p.example@class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError15() throws CSSException {
		try {
			parseSelectors("p.example{class}");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError16() throws CSSException {
		try {
			parseSelectors("p.example]");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError17() throws CSSException {
		SelectorList selist = parseSelectors("p.example\\class");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("example\u000class", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p.example\\c lass", sel.toString());
	}

	@Test
	public void testParseSelectorClassError18() throws CSSException {
		try {
			parseSelectors("p.example^class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError19() throws CSSException {
		try {
			parseSelectors("p.example|class");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(11, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError20() throws CSSException {
		try {
			parseSelectors("p.example{");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError21() throws CSSException {
		try {
			parseSelectors("p.example}");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError22() throws CSSException {
		try {
			parseSelectors("p.example())");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError23() throws CSSException {
		try {
			parseSelectors(".#example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError24() throws CSSException {
		try {
			parseSelectors("..example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassError25() throws CSSException {
		try {
			parseSelectors("./* */example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorClassEscaped() throws CSSException {
		SelectorList selist = parseSelectors(".foo\\/1");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo/1", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\\/1", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped2() throws CSSException {
		SelectorList selist = parseSelectors(".foo\\:1");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo:1", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\\:1", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped3() throws CSSException {
		SelectorList selist = parseSelectors(".\\31 23");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("123", ((AttributeCondition) cond).getValue());
		assertEquals(".\\31 23", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped4() throws CSSException {
		SelectorList selist = parseSelectors("._\\31 23");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("_123", ((AttributeCondition) cond).getValue());
		assertEquals("._123", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped5() throws CSSException {
		SelectorList selist = parseSelectors(".-\\31 23\\\\a");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("-123\\a", ((AttributeCondition) cond).getValue());
		assertEquals(".\\-123\\\\a", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped5WS() throws CSSException {
		SelectorList selist = parseSelectors(".-\\31 23\\\\a ");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("-123\\a", ((AttributeCondition) cond).getValue());
		assertEquals(".\\-123\\\\a", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped6() throws CSSException {
		SelectorList selist = parseSelectors(".foo\\(-\\.3\\)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo(-.3)", ((AttributeCondition) cond).getValue());
		assertEquals(".foo\\(-\\.3\\)", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped7() throws CSSException {
		SelectorList selist = parseSelectors(".\\31 foo\\&-.bar");
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
		assertEquals(".\\31 foo\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscaped8() throws CSSException {
		SelectorList selist = parseSelectors(".\\31 jkl\\&-.bar");
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
		assertEquals(".\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorClassEscapedBad() throws CSSException {
		SelectorList selist = parseSelectors(".\\31jkl\\&-.bar");
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
		assertEquals(".\\31 jkl\\&-.bar", sel.toString());
	}

	@Test
	public void testParseSelectorId() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		assertEquals("#exampleid", sel.toString());
	}

	@Test
	public void testParseSelectorIdHighChar() throws CSSException {
		SelectorList selist = parseSelectors("#\u208c");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond).getValue());
		assertEquals("#\u208c", sel.toString());
	}

	@Test
	public void testParseSelectorIdSurrogate() throws CSSException {
		SelectorList selist = parseSelectors("#\ud950\udc90");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("\ud950\udc90", ((AttributeCondition) cond).getValue());
		assertEquals("#\ud950\udc90", sel.toString());
	}

	@Test
	public void testParseSelectorIdEscaped() throws CSSException {
		SelectorList selist = parseSelectors("#\\31 23");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("123", ((AttributeCondition) cond).getValue());
		String s = sel.toString();
		assertEquals("#\\31 23", s);
		SelectorList selist2 = parseSelectors(s);
		assertTrue(sel.equals(selist2.item(0)));
	}

	@Test
	public void testParseSelectorTypeId() throws CSSException {
		SelectorList selist = parseSelectors("input#submit");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("submit", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("input", ((ElementSelector) simple).getLocalName());
		assertEquals("input#submit", sel.toString());
	}

	@Test
	public void testParseSelectorIdEscapedChar() throws CSSException {
		SelectorList selist = parseSelectors("#foo\\/1");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("foo/1", ((AttributeCondition) cond).getValue());
		assertEquals("#foo\\/1", sel.toString());
	}

	@Test
	public void testParseSelectorEmptyIdError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("# id"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorDoubleIdError() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("##id"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorIdError() throws CSSException {
		try {
			parseSelectors("#example&id");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError2() throws CSSException {
		try {
			parseSelectors("#9example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError3() throws CSSException {
		try {
			parseSelectors("#.example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError4() throws CSSException {
		try {
			parseSelectors("##example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError5() throws CSSException {
		try {
			parseSelectors("#example()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorIdError6() throws CSSException {
		try {
			parseSelectors("#/* */example");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChild() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid > span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildNoSpaces() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid>span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttribute() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid>[foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("#exampleid>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildAttributeWS() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid> [foo]");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getLocalName());
		assertEquals("#exampleid>[foo]", sel.toString());
	}

	@Test
	public void testParseSelectorChildUniversal() throws CSSException {
		SelectorList selist = parseSelectors("* > span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.UNIVERSAL, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*>span", sel.toString());
	}

	@Test
	public void testParseSelectorChildUniversal2() throws CSSException {
		SelectorList selist = parseSelectors("span > *");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CHILD, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("span", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("span>*", sel.toString());
	}

	@Test
	public void testParseSelectorChildError() throws CSSException {
		try {
			parseSelectors("#id>");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError2() throws CSSException {
		try {
			parseSelectors("#id:>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError3() throws CSSException {
		try {
			parseSelectors("#id>+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError4() throws CSSException {
		try {
			parseSelectors("#id>*p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError5() throws CSSException {
		try {
			parseSelectors("#id>*\\60");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError6() throws CSSException {
		try {
			parseSelectors("#id>~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError7() throws CSSException {
		try {
			parseSelectors(":>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError8() throws CSSException {
		try {
			parseSelectors("#id>*+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(7, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError9() throws CSSException {
		try {
			parseSelectors("#id>#+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError10() throws CSSException {
		try {
			parseSelectors("#id>.+");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError11() throws CSSException {
		try {
			parseSelectors("#id|>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError12() throws CSSException {
		try {
			parseSelectors("#id>>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError13() throws CSSException {
		try {
			parseSelectors("#id&>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError14() throws CSSException {
		try {
			parseSelectors("#id#>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError15() throws CSSException {
		try {
			parseSelectors("#id+>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError16() throws CSSException {
		try {
			parseSelectors("#id~>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError17() throws CSSException {
		try {
			parseSelectors("#id@>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorChildError18() throws CSSException {
		try {
			parseSelectors("#id$>p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorDescendantElement() throws CSSException {
		SelectorList selist = parseSelectors("li span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("li", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped() throws CSSException {
		SelectorList selist = parseSelectors("\\61  span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("a", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("a span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped2() throws CSSException {
		SelectorList selist = parseSelectors("div \\61");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("a", ((ElementSelector) simple).getLocalName());
		assertEquals("div a", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantElementEscaped3() throws CSSException {
		SelectorList selist = parseSelectors("body \\64 iv");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("body", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("div", ((ElementSelector) simple).getLocalName());
		assertEquals("body div", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantComment() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid/* */span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant2() throws CSSException {
		SelectorList selist = parseSelectors("#\\31 exampleid\\/2 span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("1exampleid/2", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#\\31 exampleid\\/2 span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant3() throws CSSException {
		SelectorList selist = parseSelectors(".foo  span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals(".foo span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendant3WS() throws CSSException {
		SelectorList selist = parseSelectors("[myattr]  span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("myattr", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("[myattr] span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal() throws CSSException {
		SelectorList selist = parseSelectors("* span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.UNIVERSAL, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("* span", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal2() throws CSSException {
		SelectorList selist = parseSelectors(":rtl * ");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Selector ancSimple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(ancSimple);
		assertEquals(SelectorType.UNIVERSAL, ancSimple.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("rtl", ((PseudoCondition) cond).getName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals(":rtl *", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal3() throws CSSException {
		SelectorList selist = parseSelectors("* .foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.UNIVERSAL, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		assertEquals("* .foo", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantUniversal4() throws CSSException {
		SelectorList selist = parseSelectors("span *");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("span", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("span *", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantDoubleUniversal() throws CSSException {
		SelectorList selist = parseSelectors("* *");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.UNIVERSAL, ancestor.getSelectorType());
		assertEquals("*", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals("* *", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedDescendant() throws CSSException {
		SelectorList selist = parseSelectors("body:not(.foo)[id*=substring] .header");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("body", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, first.getConditionType());
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, second.getConditionType());
		assertEquals("not", ((ArgumentCondition) first).getName());
		SelectorList notlist = ((ArgumentCondition) first).getSelectors();
		assertNotNull(notlist);
		assertEquals(1, notlist.getLength());
		Selector not1 = notlist.item(0);
		assertEquals(SelectorType.CONDITIONAL, not1.getSelectorType());
		Condition not1cond = ((ConditionalSelector) not1).getCondition();
		assertEquals(ConditionType.CLASS, not1cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) not1cond).getValue());
		assertEquals("id", ((AttributeCondition) second).getLocalName());
		assertEquals("substring", ((AttributeCondition) second).getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		Condition simplecond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, simplecond.getConditionType());
		assertEquals("header", ((AttributeCondition) simplecond).getValue());
		assertEquals("body:not(.foo)[id*=substring] .header", sel.toString());
	}

	@Test
	public void testParseSelectorDescendantCombined() throws CSSException {
		SelectorList selist = parseSelectors(".fooclass #descid.barclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		AttributeCondition attcond = (AttributeCondition) cond;
		assertEquals("fooclass", attcond.getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ID, first.getConditionType());
		assertEquals(ConditionType.CLASS, second.getConditionType());
		assertEquals("descid", ((AttributeCondition) first).getValue());
		assertEquals("barclass", ((AttributeCondition) second).getValue());
		assertEquals(".fooclass #descid.barclass", sel.toString());
	}

	@Test
	public void testParseSelectorComplexCombined() throws CSSException {
		SelectorList selist = parseSelectors(".fooclass #descid.barclass .someclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.DESCENDANT, ancestor.getSelectorType());
		Selector ancestor2 = ((CombinatorSelector) ancestor).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor2.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor2).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		Condition cond = ((ConditionalSelector) ancestor2).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		AttributeCondition attcond = (AttributeCondition) cond;
		assertEquals("fooclass", attcond.getValue());
		simple = ((CombinatorSelector) ancestor).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition first = ((CombinatorCondition) cond).getFirstCondition();
		Condition second = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ID, first.getConditionType());
		assertEquals(ConditionType.CLASS, second.getConditionType());
		assertEquals("descid", ((AttributeCondition) first).getValue());
		assertEquals("barclass", ((AttributeCondition) second).getValue());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("someclass", ((AttributeCondition) cond).getValue());
		attcond = (AttributeCondition) cond;
		assertEquals("someclass", attcond.getValue());
		assertEquals(".fooclass #descid.barclass .someclass", sel.toString());
	}

	@Test
	public void testParseSelectorComplexCombined2() throws CSSException {
		SelectorList selist = parseSelectors(".barclass#otherid.otherclass .someclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertNull(((ElementSelector) simple).getNamespaceURI());

		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		CombinatorCondition combcond = (CombinatorCondition) cond;

		Condition first = combcond.getFirstCondition();
		assertEquals(ConditionType.CLASS, first.getConditionType());
		assertEquals("barclass", ((AttributeCondition) first).getValue());

		Condition second = combcond.getSecondCondition();
		assertEquals(ConditionType.ID, second.getConditionType());
		assertEquals("otherid", ((AttributeCondition) second).getValue());

		assertEquals(3, combcond.getLength());

		Condition third = combcond.getCondition(2);
		assertEquals(ConditionType.CLASS, third.getConditionType());
		assertEquals("otherclass", ((AttributeCondition) third).getValue());

		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("someclass", ((AttributeCondition) cond).getValue());
		assertEquals(".barclass#otherid.otherclass .someclass", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingElement() throws CSSException {
		SelectorList selist = parseSelectors("li+span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, first.getSelectorType());
		assertEquals("li", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSibling() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid + span:empty");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("empty", ((PseudoCondition) cond).getName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid+span:empty");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("empty", ((PseudoCondition) cond).getName());
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("#exampleid+span:empty", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingNoSpaces2() throws CSSException {
		SelectorList selist = parseSelectors(".myclass:foo+.bar");
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
		Selector simple = ((ConditionalSelector) sibling).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
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
		assertEquals(".myclass:foo+.bar", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingUniversal() throws CSSException {
		SelectorList selist = parseSelectors("*+span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.UNIVERSAL, first.getSelectorType());
		assertEquals("*", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingUniversalWS() throws CSSException {
		SelectorList selist = parseSelectors("* + span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.UNIVERSAL, first.getSelectorType());
		assertEquals("*", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("*+span", sel.toString());
	}

	@Test
	public void testParseSelectorNextSiblingError() throws CSSException {
		try {
			parseSelectors("#id ++p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError2() throws CSSException {
		try {
			parseSelectors("#id|+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError3() throws CSSException {
		try {
			parseSelectors("#id@+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError4() throws CSSException {
		try {
			parseSelectors("#id>+p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorNextSiblingError5() throws CSSException {
		try {
			parseSelectors("#id+~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorSubsequentSibling() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid ~ span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.ELEMENT, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("#exampleid~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingNoSpaces() throws CSSException {
		SelectorList selist = parseSelectors("#exampleid~span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, first.getSelectorType());
		Condition cond = ((ConditionalSelector) first).getCondition();
		assertEquals(ConditionType.ID, cond.getConditionType());
		assertEquals("exampleid", ((AttributeCondition) cond).getValue());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.ELEMENT, sibling.getSelectorType());
		assertEquals("span", ((ElementSelector) sibling).getLocalName());
		assertEquals("#exampleid~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingElement() throws CSSException {
		SelectorList selist = parseSelectors("li~span");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, first.getSelectorType());
		assertEquals("li", ((ElementSelector) first).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		assertEquals("li~span", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSibling2() throws CSSException {
		SelectorList selist = parseSelectors(".foo .bar.class~.otherclass li:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, ancestor.getSelectorType());
		Selector first = ((CombinatorSelector) ancestor).getSelector(); // .foo .bar.class
		assertEquals(SelectorType.DESCENDANT, first.getSelectorType());
		Selector ancestorfirst = ((CombinatorSelector) first).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestorfirst.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestorfirst).getCondition(); // foo
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		Selector simplefirst = ((CombinatorSelector) first).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simplefirst.getSelectorType());
		cond = ((ConditionalSelector) simplefirst).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.CLASS, cond2.getConditionType());
		assertEquals("class", ((AttributeCondition) cond2).getValue());
		SimpleSelector sibling = ((CombinatorSelector) ancestor).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition condsibling = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.CLASS, condsibling.getConditionType());
		assertEquals("otherclass", ((AttributeCondition) condsibling).getValue());

		Selector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		PositionalCondition pcond = (PositionalCondition) cond;
		assertEquals(1, pcond.getOffset());
		assertEquals(0, pcond.getFactor());
		simple = ((ConditionalSelector) simple).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("li", ((ElementSelector) simple).getLocalName());
		assertEquals(".foo .bar.class~.otherclass li:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorSubsequentSiblingError() throws CSSException {
		try {
			parseSelectors("#id~~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorSubsequentSiblingError2() throws CSSException {
		try {
			parseSelectors("#id>~p");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorColumnCombinatorElement() throws CSSException {
		SelectorList selist = parseSelectors("col||td");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.COLUMN_COMBINATOR, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("col", ((ElementSelector) ancestor).getLocalName());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("td", ((ElementSelector) simple).getLocalName());
		assertEquals("col||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorElementError() throws CSSException {
		try {
			parseSelectors("col||");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorColumnCombinatorElementError2() throws CSSException {
		try {
			parseSelectors("col||,p");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorColumnCombinator() throws CSSException {
		SelectorList selist = parseSelectors("col.foo||td");
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
		assertEquals("col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorColumnCombinatorWS() throws CSSException {
		SelectorList selist = parseSelectors("col.foo || td");
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
		assertEquals("col.foo||td", sel.toString());
	}

	@Test
	public void testParseSelectorCombinators1() throws CSSException {
		SelectorList selist = parseSelectors(".ancestor .parent>.child ~ .childsibling:foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		SimpleSelector sibling = ((CombinatorSelector) sel).getSecondSelector();
		assertNotNull(sibling);
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("childsibling", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond2.getConditionType());
		assertEquals("foo", ((PseudoCondition) cond2).getName());
		Selector first = ((CombinatorSelector) sel).getSelector();
		assertNotNull(first);
		assertEquals(SelectorType.CHILD, first.getSelectorType());
		SimpleSelector simple = ((CombinatorSelector) first).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("child", ((AttributeCondition) cond).getValue());
		first = ((CombinatorSelector) first).getSelector();
		assertNotNull(first);
		assertEquals(SelectorType.DESCENDANT, first.getSelectorType());
		simple = ((CombinatorSelector) first).getSecondSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("parent", ((AttributeCondition) cond).getValue());
		first = ((CombinatorSelector) first).getSelector();
		assertNotNull(first);
		assertEquals(SelectorType.CONDITIONAL, first.getSelectorType());
		cond = ((ConditionalSelector) first).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("ancestor", ((AttributeCondition) cond).getValue());
		assertEquals(".ancestor .parent>.child~.childsibling:foo", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElement() throws CSSException {
		SelectorList selist = parseSelectors("p::first-line");
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
		assertEquals("p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementHighlight() throws CSSException {
		SelectorList selist = parseSelectors("p::highlight(highlight-name)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("highlight", ((PseudoCondition) cond).getName());
		assertEquals("highlight-name", ((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::highlight(highlight-name)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPart() throws CSSException {
		SelectorList selist = parseSelectors("p::part(base)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("part", ((PseudoCondition) cond).getName());
		assertEquals("base", ((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::part(base)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPicker() throws CSSException {
		SelectorList selist = parseSelectors("p::picker(select)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("picker", ((PseudoCondition) cond).getName());
		assertEquals("select", ((PseudoCondition) cond).getArgument());

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());

		assertEquals("p::picker(select)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementSlotted() throws CSSException {
		SelectorList selist = parseSelectors("p::slotted([slot=icon])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT_PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("slotted", ((ArgumentCondition) cond).getName());
		SelectorList args = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		assertEquals("[slot=icon]", arg.toString());

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());

		assertEquals("p::slotted([slot=icon])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementScrollButton() throws CSSException {
		SelectorList selist = parseSelectors("p::scroll-button(*)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("scroll-button", ((PseudoCondition) cond).getName());
		assertEquals("*", ((PseudoCondition) cond).getArgument());

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());

		assertEquals("p::scroll-button(*)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementViewTransitionGroup() throws CSSException {
		SelectorList selist = parseSelectors("p::view-transition-group(*)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("view-transition-group", ((PseudoCondition) cond).getName());
		assertEquals("*", ((PseudoCondition) cond).getArgument());

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());

		assertEquals("p::view-transition-group(*)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementViewTransitionImagePair() throws CSSException {
		SelectorList selist = parseSelectors("p::view-transition-image-pair(.card)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("view-transition-image-pair", ((PseudoCondition) cond).getName());
		assertEquals(".card", ((PseudoCondition) cond).getArgument());

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());

		assertEquals("p::view-transition-image-pair(.card)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementEscapedBad() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class,
				() -> parseSelectors("p::\\.first-line"));
		assertEquals(4, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorPseudoElementQuirk() throws CSSException {
		SelectorList selist = parseSelectors("p::-webkit-foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_ELEMENT, cond.getConditionType());
		assertEquals("-webkit-foo", ((PseudoCondition) cond).getName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p::-webkit-foo", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementError() throws CSSException {
		try {
			parseSelectors("p::first&line");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoElementError2() throws CSSException {
		try {
			parseSelectors("p::9first-line");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(4, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoElementError3() throws CSSException {
		try {
			parseSelectors("::first-line()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(13, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoElementCommentError() throws CSSException {
		try {
			parseSelectors("::/* */first-line");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoElementQuirkError() throws CSSException {
		try {
			parseSelectors("::-webkit-foo()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(15, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoElementErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors("::()"));
		assertEquals(3, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorPseudoElementOld() throws CSSException {
		SelectorList selist = parseSelectors("p:first-line");
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
		assertEquals("p::first-line", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoElementPseudoclassed() throws CSSException {
		SelectorList selist = parseSelectors("p::first-letter:hover");
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
		assertEquals("p::first-letter:hover", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClass() throws CSSException {
		SelectorList selist = parseSelectors("div:blank");
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
		assertEquals("div:blank", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassPrefixed() throws CSSException {
		SelectorList selist = parseSelectors(":-css4j-blank");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("-css4j-blank", ((PseudoCondition) cond).getName());
		assertNull(((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(":-css4j-blank", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassPrefixedArgument() throws CSSException {
		SelectorList selist = parseSelectors("p:-webkit-any(div,span)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("-webkit-any", ((PseudoCondition) cond).getName());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:-webkit-any(div\\,span)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassErrorCustom() throws CSSException {
		try {
			parseSelectors(":--css4j-blank");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(2, e.getColumnNumber());
		}
		try {
			parseSelectors("::--css4j-blank");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(3, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassError() throws CSSException {
		try {
			parseSelectors("div:blank&");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(10, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassError2() throws CSSException {
		try {
			parseSelectors("div:9blank");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassError3() throws CSSException {
		try {
			parseSelectors("div:-");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassErrorEmpty() throws CSSException {
		CSSParseException ex = assertThrows(CSSParseException.class, () -> parseSelectors(":()"));
		assertEquals(2, ex.getColumnNumber());
	}

	@Test
	public void testParseSelectorPseudoClassCommentError() throws CSSException {
		try {
			parseSelectors("div:/* */blank");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassArgument() throws CSSException {
		SelectorList selist = parseSelectors("p:dir(ltr)");
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
		assertEquals("p:dir(ltr)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassDirError() throws CSSException {
		try {
			parseSelectors(":dir()");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassDirError2() throws CSSException {
		try {
			parseSelectors(":dir(,)");
			fail("Must throw exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChild1() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(1)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(1)", sel.toString());
		Selector sel2 = parseSelectors(sel.toString()).item(0);
		assertTrue(sel.equals(sel2));
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild() throws CSSException {
		SelectorList selist = parseSelectors(":first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChild2() throws CSSException {
		SelectorList selist = parseSelectors("p:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildError() throws CSSException {
		try {
			parseSelectors(":first-child()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(13, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildError2() throws CSSException {
		try {
			parseSelectors(":first-child(even)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(13, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassLastChild() throws CSSException {
		SelectorList selist = parseSelectors(":last-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":last-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassFirstChildList() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(1),:first-child");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		assertEquals(":nth-child(1)", selist.item(0).toString());
		assertEquals(":first-child", selist.item(1).toString());
	}

	@Test
	public void testParseSelectorPseudoClassLastChildList() throws CSSException {
		SelectorList selist = parseSelectors(":nth-last-child(1),:last-child");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		assertEquals(":nth-last-child(1)", selist.item(0).toString());
		assertEquals(":last-child", selist.item(1).toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyChild() throws CSSException {
		SelectorList selist = parseSelectors("p:only-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONLY_CHILD, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:only-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyChild2() throws CSSException {
		SelectorList selist = parseSelectors("*:only-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONLY_CHILD, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(":only-child", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError() throws CSSException {
		try {
			parseSelectors(":nth-child()");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(12, e.getColumnNumber());
		}
	}

	/*
	 * web-platform-tests/wpt/master/css/selectors/anplusb-selector-parsing.html
	 */
	@Test
	public void testParseSelectorPseudoClassNthChildError2() throws CSSException {
		try {
			parseSelectors(":nth-child(n - 1 2)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(19, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError3() throws CSSException {
		try {
			parseSelectors(":nth-child(n - b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(18, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError4() throws CSSException {
		try {
			parseSelectors(":nth-child(n-+1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(16, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError5() throws CSSException {
		try {
			parseSelectors(":nth-child(n+-1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(16, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError6() throws CSSException {
		try {
			parseSelectors(":nth-child(n +-1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError7() throws CSSException {
		try {
			parseSelectors(":nth-child(n +- 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(18, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError8() throws CSSException {
		try {
			parseSelectors(":nth-child(n -+ 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(18, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError9() throws CSSException {
		try {
			parseSelectors(":nth-child(n + - 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(19, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError10() throws CSSException {
		try {
			parseSelectors(":nth-child(n - + 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(19, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError11() throws CSSException {
		try {
			parseSelectors(":nth-child(n -1n)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError12() throws CSSException {
		try {
			parseSelectors(":nth-child(n - +b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(19, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError13() throws CSSException {
		try {
			parseSelectors(":nth-child(n -b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError14() throws CSSException {
		try {
			parseSelectors(":nth-child(n b1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(16, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError15() throws CSSException {
		try {
			parseSelectors(":nth-child(n 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(15, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthChildError16() throws CSSException {
		try {
			parseSelectors(":nth-child(- - 1)");
			fail("Must throw an exception");
		} catch (CSSParseException e) {
			assertEquals(17, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNthEven() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(even)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
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
	public void testParseSelectorPseudoClassNthOdd() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(odd)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
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
	public void testParseSelectorPseudoClassNthKeywords() throws CSSException {
		SelectorList selist = parseSelectors(
				":nth-child(even),:nth-child(2n),:nth-child(odd),:nth-child(2n+1)");
		assertNotNull(selist);
		assertEquals(4, selist.getLength());
		assertEquals(":nth-child(even)", selist.item(0).toString());
		assertEquals(":nth-child(2n)", selist.item(1).toString());
		assertEquals(":nth-child(odd)", selist.item(2).toString());
		assertEquals(":nth-child(2n+1)", selist.item(3).toString());
	}

	@Test
	public void testParseSelectorPseudoClassNth() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(5)");
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
		assertNull(oflist);
		assertEquals(":nth-child(5)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAn() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(10n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnB() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(10n+9)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(9, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n+9)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBcr() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(10n\n+9)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(9, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n+9)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBzero() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(10n+0)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(10, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-child(10n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnB2() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(n+2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(1, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-child(n+2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast() throws CSSException {
		SelectorList selist = parseSelectors(":nth-last-child(5)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(5)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast2() throws CSSException {
		SelectorList selist = parseSelectors(":nth-last-child(3n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(3, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(3n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast3() throws CSSException {
		SelectorList selist = parseSelectors(":nth-last-child(-n+2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(-1, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(-n+2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLast4() throws CSSException {
		SelectorList selist = parseSelectors(":nth-last-child(2n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNull(oflist);
		assertEquals(":nth-last-child(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOf() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(5 of p)");
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
		assertEquals(":nth-child(5 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLastOf() throws CSSException {
		SelectorList selist = parseSelectors(":nth-last-child(5 of p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(5, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-last-child(5 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthAnBOf() throws CSSException {
		SelectorList selist = parseSelectors(":nth-child(6n+3 of p)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(3, ((PositionalCondition) cond).getOffset());
		assertEquals(6, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertFalse(((PositionalCondition) cond).isOfType());
		SelectorList oflist = ((PositionalCondition) cond).getOfList();
		assertNotNull(oflist);
		assertEquals(1, oflist.getLength());
		Selector simple = oflist.item(0);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals(":nth-child(6n+3 of p)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfBad() throws CSSException {
		try {
			parseSelectors(":nth-child(5 of)");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassFirstOfType() throws CSSException {
		SelectorList selist = parseSelectors(":first-of-type");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isOfType());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertEquals(":first-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType() throws CSSException {
		SelectorList selist = parseSelectors(":nth-of-type(2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		assertEquals(":nth-of-type(2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType2() throws CSSException {
		SelectorList selist = parseSelectors("p:nth-of-type(2)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(2, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-of-type(2)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthOfType3() throws CSSException {
		SelectorList selist = parseSelectors("p:nth-of-type(2n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertTrue(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-of-type(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassLastOfType() throws CSSException {
		SelectorList selist = parseSelectors(":last-of-type");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(1, ((PositionalCondition) cond).getOffset());
		assertEquals(0, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		assertEquals(":last-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNthLastOfType() throws CSSException {
		SelectorList selist = parseSelectors("p:nth-last-of-type(2n)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.POSITIONAL, cond.getConditionType());
		assertEquals(0, ((PositionalCondition) cond).getOffset());
		assertEquals(2, ((PositionalCondition) cond).getFactor());
		assertFalse(((PositionalCondition) cond).isForwardCondition());
		assertTrue(((PositionalCondition) cond).isOfType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:nth-last-of-type(2n)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassOnlyOfType() throws CSSException {
		SelectorList selist = parseSelectors("p:only-of-type");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.ONLY_TYPE, cond.getConditionType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("p", ((ElementSelector) simple).getLocalName());
		assertEquals("p:only-of-type", sel.toString());
	}

	@Test
	public void testParseSelectorCombined() throws CSSException {
		SelectorList selist = parseSelectors(".exampleclass:first-child");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("exampleclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.POSITIONAL, cond2.getConditionType());
		assertEquals(1, ((PositionalCondition) cond2).getOffset());
		assertFalse(((PositionalCondition) cond2).isOfType());
		assertEquals(".exampleclass:first-child", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedIdPseudoclass() throws CSSException {
		SelectorList selist = parseSelectors("#example-ID:foo");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ID, cond1.getConditionType());
		assertEquals("example-ID", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond2.getConditionType());
		assertEquals("foo", ((PseudoCondition) cond2).getName());
		assertEquals("#example-ID:foo", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedDoubleId() throws CSSException {
		SelectorList selist = parseSelectors("#foo#bar");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ID, cond1.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ID, cond2.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond2).getValue());
		assertEquals("#foo#bar", sel.toString());
	}

	@Test
	public void testParseSelectorCombinedAttributes() throws CSSException {
		SelectorList selist = parseSelectors("span[class=\"example\"][foo=\"a b\"],:rtl *");
		assertNotNull(selist);
		assertEquals(2, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond1.getConditionType());
		assertEquals("class", ((AttributeCondition) cond1).getLocalName());
		assertEquals("example", ((AttributeCondition) cond1).getValue());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("span", ((ElementSelector) simple).getLocalName());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond2.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond2).getLocalName());
		assertEquals("a b", ((AttributeCondition) cond2).getValue());
		assertEquals("span[class=example][foo=\"a b\"]", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot() throws CSSException {
		SelectorList selist = parseSelectors(":not(p.foo, span:first-child, div a)");
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
		assertEquals(":not(p.foo,span:first-child,div a)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot2() throws CSSException {
		SelectorList selist = parseSelectors(":not([disabled],.foo,[type=\"submit\"])");
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
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("disabled", ((AttributeCondition) cond).getLocalName());
		SimpleSelector simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		arg = args.item(1);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		arg = args.item(2);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond.getConditionType());
		assertEquals("type", ((AttributeCondition) cond).getLocalName());
		assertEquals("submit", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals(":not([disabled],.foo,[type=submit])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot3() throws CSSException {
		SelectorList selist = parseSelectors(".foo .myclass:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals(".foo .myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot4() throws CSSException {
		SelectorList selist = parseSelectors(".foo+.myclass:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, sel.getSelectorType());
		Selector sibling = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals(".foo+.myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot5() throws CSSException {
		SelectorList selist = parseSelectors(".foo~.myclass:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, sel.getSelectorType());
		Selector sibling = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, sibling.getSelectorType());
		Condition cond = ((ConditionalSelector) sibling).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		SimpleSelector simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		Condition cond1 = ((CombinatorCondition) cond).getFirstCondition();
		assertEquals(ConditionType.CLASS, cond1.getConditionType());
		assertEquals("myclass", ((AttributeCondition) cond1).getValue());
		Condition cond2 = ((CombinatorCondition) cond).getSecondCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		SelectorList args = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, args.getLength());
		Selector arg = args.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals(".foo~.myclass:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot6() throws CSSException {
		SelectorList selist = parseSelectors(":not(:visited,:hover)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(2, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond0.getConditionType());
		assertEquals("visited", ((PseudoCondition) cond0).getName());
		Selector item1 = arglist.item(1);
		assertEquals(SelectorType.CONDITIONAL, item1.getSelectorType());
		Condition cond1 = ((ConditionalSelector) item1).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond1.getConditionType());
		assertEquals("hover", ((PseudoCondition) cond1).getName());
		assertEquals(":not(:visited,:hover)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot7() throws CSSException {
		SelectorList selist = parseSelectors(":not(:lang(en))");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.LANG, cond0.getConditionType());
		assertEquals("en", ((LangCondition) cond0).getLang());
		assertEquals(":not(:lang(en))", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot8() throws CSSException {
		SelectorList selist = parseSelectors(":not([style*=\"background\"])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond0.getConditionType());
		assertEquals("style", ((AttributeCondition) cond0).getLocalName());
		assertEquals("background", ((AttributeCondition) cond0).getValue());
		assertEquals(":not([style*=background])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot9() throws CSSException {
		SelectorList selist = parseSelectors("html:not(.foo)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("html:not(.foo)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot10() throws CSSException {
		SelectorList selist = parseSelectors("html:not(.foo) body:not(.bar)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.CONDITIONAL, ancestor.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) ancestor).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) ancestor).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("foo", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		simple = ((CombinatorSelector) sel).getSecondSelector();
		assertEquals(SelectorType.CONDITIONAL, simple.getSelectorType());
		cond = ((ConditionalSelector) simple).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		arg = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		cond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, cond.getConditionType());
		assertEquals("bar", ((AttributeCondition) cond).getValue());
		simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("html:not(.foo) body:not(.bar)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot11() throws CSSException {
		SelectorList selist = parseSelectors("html:not(.foo) body:not(.bar) .myclass.otherclass");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals("html:not(.foo) body:not(.bar) .myclass.otherclass", sel.toString());
		assertEquals(SelectorType.DESCENDANT, sel.getSelectorType());
		Selector ancestor = ((CombinatorSelector) sel).getSelector();
		assertEquals(SelectorType.DESCENDANT, ancestor.getSelectorType());
	}

	@Test
	public void testParseSelectorPseudoClassNot12() throws CSSException {
		SelectorList selist = parseSelectors(":not([style*=\\*foo],\\64 iv,.\\39 z,#\\31 23)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", simple.toString());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(4, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond0.getConditionType());
		assertEquals("style", ((AttributeCondition) cond0).getLocalName());
		assertEquals("*foo", ((AttributeCondition) cond0).getValue());
		Selector item1 = arglist.item(1);
		assertEquals(SelectorType.ELEMENT, item1.getSelectorType());
		assertEquals("div", ((ElementSelector) item1).getLocalName());
		Selector item2 = arglist.item(2);
		assertEquals(SelectorType.CONDITIONAL, item2.getSelectorType());
		Condition cond2 = ((ConditionalSelector) item2).getCondition();
		assertEquals(ConditionType.CLASS, cond2.getConditionType());
		assertEquals("9z", ((AttributeCondition) cond2).getValue());
		Selector item3 = arglist.item(3);
		assertEquals(SelectorType.CONDITIONAL, item3.getSelectorType());
		Condition cond3 = ((ConditionalSelector) item3).getCondition();
		assertEquals(ConditionType.ID, cond3.getConditionType());
		assertEquals("123", ((AttributeCondition) cond3).getValue());
		assertEquals(":not([style*=\"*foo\"],div,.\\39 z,#\\31 23)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNot13() throws CSSException {
		SelectorList selist = parseSelectors(
				"a:not([href]):not([tabindex]),a:not([href]):not([tabindex]):focus,code,pre,div");
		assertNotNull(selist);
		assertEquals(5, selist.getLength());

		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("a", ((ElementSelector) simple).getLocalName());

		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, cond.getConditionType());
		CombinatorCondition comb = (CombinatorCondition) cond;
		Condition cond1 = comb.getFirstCondition();
		Condition cond2 = comb.getSecondCondition();

		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond1.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond1).getName());
		SelectorList arglist = ((ArgumentCondition) cond1).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond0.getConditionType());
		assertEquals("href", ((AttributeCondition) cond0).getLocalName());
		assertNull(((AttributeCondition) cond0).getValue());

		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		arglist = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, arglist.getLength());
		item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond0.getConditionType());
		assertEquals("tabindex", ((AttributeCondition) cond0).getLocalName());
		assertNull(((AttributeCondition) cond0).getValue());

		assertEquals("a:not([href]):not([tabindex])", sel.toString());

		sel = selist.item(1);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		cond = ((ConditionalSelector) sel).getCondition();
		simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("a", ((ElementSelector) simple).getLocalName());
		assertEquals(ConditionType.AND, cond.getConditionType());
		comb = (CombinatorCondition) cond;
		cond1 = comb.getFirstCondition();

		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond1.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond1).getName());
		arglist = ((ArgumentCondition) cond1).getSelectors();
		assertEquals(1, arglist.getLength());
		item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond0.getConditionType());
		assertEquals("href", ((AttributeCondition) cond0).getLocalName());
		assertNull(((AttributeCondition) cond0).getValue());

		cond2 = comb.getSecondCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond2.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond2).getName());
		arglist = ((ArgumentCondition) cond2).getSelectors();
		assertEquals(1, arglist.getLength());

		item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.ATTRIBUTE, cond0.getConditionType());
		assertEquals("tabindex", ((AttributeCondition) cond0).getLocalName());
		assertNull(((AttributeCondition) cond0).getValue());

		Condition cond3 = comb.getCondition(2);
		assertEquals(ConditionType.PSEUDO_CLASS, cond3.getConditionType());
		assertEquals("focus", ((PseudoCondition) cond3).getName());
		assertNull(((PseudoCondition) cond3).getArgument());

		assertEquals("a:not([href]):not([tabindex]):focus", sel.toString());

		sel = selist.item(2);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("code", ((ElementSelector) sel).getLocalName());

		sel = selist.item(3);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("pre", ((ElementSelector) sel).getLocalName());

		sel = selist.item(4);
		assertEquals(SelectorType.ELEMENT, sel.getSelectorType());
		assertEquals("div", ((ElementSelector) sel).getLocalName());

		assertEquals(
				"a:not([href]):not([tabindex]),a:not([href]):not([tabindex]):focus,code,pre,div",
				selist.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNotAttrHighChar() throws CSSException {
		SelectorList selist = parseSelectors(":not([\u208c*=\"foo\"])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond0.getConditionType());
		assertEquals("\u208c", ((AttributeCondition) cond0).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond0).getValue());
		assertEquals(":not([\u208c*=foo])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNotAttrSurrogate() throws CSSException {
		SelectorList selist = parseSelectors(":not([\ud83c\udf52*=\"foo\"])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond0.getConditionType());
		assertEquals("\ud83c\udf52", ((AttributeCondition) cond0).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond0).getValue());
		assertEquals(":not([\ud83c\udf52*=foo])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNotAttrSurrogate2() throws CSSException {
		SelectorList selist = parseSelectors(":not([_\ud83c\udf52*=\"foo\"])");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("not", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector item0 = arglist.item(0);
		assertEquals(SelectorType.CONDITIONAL, item0.getSelectorType());
		Condition cond0 = ((ConditionalSelector) item0).getCondition();
		assertEquals(ConditionType.SUBSTRING_ATTRIBUTE, cond0.getConditionType());
		assertEquals("_\ud83c\udf52", ((AttributeCondition) cond0).getLocalName());
		assertEquals("foo", ((AttributeCondition) cond0).getValue());
		assertEquals(":not([_\ud83c\udf52*=foo])", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassNotAttrHighCharError() throws CSSException {
		try {
			parseSelectors(":not(p[\u26a1])"); // ⚡ high voltage sign
			fail("Must throw an exception");
		} catch (CSSParseException e) {
		}
	}

	@Test
	public void testParseSelectorPseudoClassNotEmpty() throws CSSException {
		try {
			parseSelectors(":not()");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(6, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassNotEmpty2() throws CSSException {
		try {
			parseSelectors("foo:not()");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(9, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot() throws CSSException {
		try {
			parseSelectors(":not");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot2() throws CSSException {
		try {
			parseSelectors(":not:only-child");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot3() throws CSSException {
		try {
			parseSelectors("svg:not:only-child");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(8, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot4() throws CSSException {
		try {
			parseSelectors(":not p");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot5() throws CSSException {
		try {
			parseSelectors(":not::first-letter");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot6() throws CSSException {
		try {
			parseSelectors(":not.class");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassBadNot7() throws CSSException {
		try {
			parseSelectors(":not (.class)");
			fail("Should throw an exception");
		} catch (CSSParseException e) {
			assertEquals(5, e.getColumnNumber());
		}
	}

	@Test
	public void testParseSelectorPseudoClassHas() throws CSSException {
		SelectorList selist = parseSelectors("html:has(> img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.CHILD, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(SelectorType.SCOPE_MARKER, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(>img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas2() throws CSSException {
		SelectorList selist = parseSelectors("html:has(+ img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.DIRECT_ADJACENT, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(SelectorType.SCOPE_MARKER, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(+img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas3() throws CSSException {
		SelectorList selist = parseSelectors("html:has(div>img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.CHILD, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(SelectorType.ELEMENT, ancestor.getSelectorType());
		assertEquals("div", ((ElementSelector) ancestor).getLocalName());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(div>img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas4() throws CSSException {
		SelectorList selist = parseSelectors("html:has(~ img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("html", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.SUBSEQUENT_SIBLING, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(SelectorType.SCOPE_MARKER, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("html:has(~img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHas5() throws CSSException {
		SelectorList selist = parseSelectors("tr:has(|| img)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("tr", ((ElementSelector) simple).getLocalName());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("has", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.COLUMN_COMBINATOR, arg.getSelectorType());
		Selector ancestor = ((CombinatorSelector) arg).getSelector();
		assertEquals(SelectorType.SCOPE_MARKER, ancestor.getSelectorType());
		simple = ((CombinatorSelector) arg).getSecondSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("img", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());
		assertEquals("tr:has(||img)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHasPseudoElem() throws CSSException {
		SelectorList selist = parseSelectors("figure.block-image:has(figcaption)::before");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertEquals(SelectorType.ELEMENT, simple.getSelectorType());
		assertEquals("figure", ((ElementSelector) simple).getLocalName());
		assertNull(((ElementSelector) simple).getNamespaceURI());

		Condition and = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.AND, and.getConditionType());
		Condition first = ((CombinatorCondition) and).getFirstCondition();
		Condition second = ((CombinatorCondition) and).getSecondCondition();

		assertEquals(ConditionType.CLASS, first.getConditionType());
		assertEquals("block-image", ((AttributeCondition) first).getValue());

		assertEquals(ConditionType.SELECTOR_ARGUMENT, second.getConditionType());
		assertEquals("has", ((ArgumentCondition) second).getName());

		SelectorList arglist = ((ArgumentCondition) second).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);
		assertEquals(SelectorType.ELEMENT, arg.getSelectorType());
		assertEquals("figcaption", ((ElementSelector) arg).getLocalName());

		Condition thirdcond = ((CombinatorCondition) and).getCondition(2);
		assertEquals(ConditionType.PSEUDO_ELEMENT, thirdcond.getConditionType());
		assertEquals("before", ((PseudoCondition) thirdcond).getName());

		assertEquals("figure.block-image:has(figcaption)::before", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHasNested() throws CSSException {
		assertThrows(CSSParseException.class, () -> parseSelectors("div:has(.cls,p:has(>img))"));
	}

	@Test
	public void testParseSelectorPseudoElementHasNested() throws CSSException {
		assertThrows(CSSParseException.class, () -> parseSelectors("div:has(.cls,p::marker)"));
	}

	@Test
	public void testParseSelectorPseudoElementHasNestedLegacySyntax() throws CSSException {
		assertThrows(CSSParseException.class, () -> parseSelectors("div:has(p:first-line)"));
	}

	@Test
	public void testParseSelectorPseudoElementHasNestedLegacySyntax2() throws CSSException {
		assertThrows(CSSParseException.class, () -> parseSelectors("div:has(.cls,p:first-letter)"));
	}

	@Test
	public void testParseSelectorPseudoClassHost() throws CSSException {
		SelectorList selist = parseSelectors(":host");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("host", ((PseudoCondition) cond).getName());
		assertNull(((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(":host", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHostArgument() throws CSSException {
		SelectorList selist = parseSelectors(":host(div)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());

		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("host", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);

		assertEquals(SelectorType.ELEMENT, arg.getSelectorType());
		assertEquals("div", ((ElementSelector) arg).getLocalName());

		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());

		assertEquals(":host(div)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassHostContext() throws CSSException {
		SelectorList selist = parseSelectors(":host-context(.cls)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());

		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.SELECTOR_ARGUMENT, cond.getConditionType());
		assertEquals("host-context", ((ArgumentCondition) cond).getName());
		SelectorList arglist = ((ArgumentCondition) cond).getSelectors();
		assertEquals(1, arglist.getLength());
		Selector arg = arglist.item(0);

		assertEquals(SelectorType.CONDITIONAL, arg.getSelectorType());
		Condition argcond = ((ConditionalSelector) arg).getCondition();
		assertEquals(ConditionType.CLASS, argcond.getConditionType());
		assertEquals("cls", ((AttributeCondition) argcond).getValue());

		SimpleSelector simple = ((ConditionalSelector) arg).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());

		assertEquals(":host-context(.cls)", sel.toString());
	}

	@Test
	public void testParseSelectorPseudoClassState() throws CSSException {
		SelectorList selist = parseSelectors(":state(clicked)");
		assertNotNull(selist);
		assertEquals(1, selist.getLength());
		Selector sel = selist.item(0);
		assertEquals(SelectorType.CONDITIONAL, sel.getSelectorType());
		Condition cond = ((ConditionalSelector) sel).getCondition();
		assertEquals(ConditionType.PSEUDO_CLASS, cond.getConditionType());
		assertEquals("state", ((PseudoCondition) cond).getName());
		assertEquals("clicked", ((PseudoCondition) cond).getArgument());
		SimpleSelector simple = ((ConditionalSelector) sel).getSimpleSelector();
		assertNotNull(simple);
		assertEquals(SelectorType.UNIVERSAL, simple.getSelectorType());
		assertEquals("*", ((ElementSelector) simple).getLocalName());
		assertEquals(":state(clicked)", sel.toString());
	}

	private SelectorList parseSelectors(String selist) throws CSSException {
		try {
			return parser.parseSelectors(new StringReader(selist));
		} catch (IOException e) {
			return null;
		}
	}

}
