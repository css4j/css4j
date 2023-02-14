/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSLexicalValue;
import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.LexicalValue;

public class BaseCSSStyleSheetFactoryTest {

	private TestCSSStyleSheetFactory factory;

	@BeforeEach
	public void setUp() {
		factory = new TestCSSStyleSheetFactory();
	}

	@Test
	public void testCreateUnmodifiable() {
		MediaQueryList mql = factory.createImmutableMediaQueryList("screen", null);
		assertNotNull(mql);
		assertEquals(1, mql.getLength());
		assertEquals("screen", mql.getMedia());
		//
		try {
			mql.appendMedium("print");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
		//
		try {
			mql.deleteMedium("print");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
		//
		try {
			mql.setMediaText("print");
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.code);
		}
	}

	@Test
	public void testCreatePropertyDefinition() {
		SyntaxParser parser = new SyntaxParser();
		CSSValueSyntax syntax = parser.parseSyntax("*");
		CSSPropertyDefinition definition = factory.createPropertyDefinition("--my-property", syntax, true, null);
		assertNotNull(definition);
		assertEquals("--my-property", definition.getName());
		assertEquals("*", definition.getSyntax().toString());
		assertTrue(definition.inherits());
		assertNull(definition.getInitialValue());
	}

	@Test
	public void testCreatePropertyDefinition2() throws CSSParseException, IOException {
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syntax = syntaxParser.parseSyntax("<length>");
		CSSOMParser parser = new CSSOMParser();
		LexicalValue value = new LexicalValue();
		value.setLexicalUnit(parser.parsePropertyValue(new StringReader("18px")));
		//
		CSSPropertyDefinition definition = factory.createPropertyDefinition("--my-length", syntax, true, value);
		assertNotNull(definition);
		assertEquals("--my-length", definition.getName());
		assertEquals("<length>", definition.getSyntax().toString());
		assertTrue(definition.inherits());
		CSSLexicalValue initial = definition.getInitialValue();
		assertNotNull(initial);
		assertEquals("18px", initial.getCssText());
	}

	@Test
	public void testCreatePropertyDefinitionError() throws CSSParseException, IOException {
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syntax = syntaxParser.parseSyntax("*");
		//
		try {
			factory.createPropertyDefinition(null, syntax, true, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		try {
			factory.createPropertyDefinition("--foo", null, true, null);
			fail("Must throw exception.");
		} catch (NullPointerException e) {
		}
		//
		syntax = syntaxParser.parseSyntax("<length>");
		try {
			factory.createPropertyDefinition("--my-length", syntax, true, null);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
		//
		CSSOMParser parser = new CSSOMParser();
		LexicalValue value = new LexicalValue();
		value.setLexicalUnit(parser.parsePropertyValue(new StringReader("#bbb")));
		try {
			factory.createPropertyDefinition("--my-length", syntax, true, value);
			fail("Must throw exception.");
		} catch (DOMException e) {
			assertEquals(DOMException.INVALID_ACCESS_ERR, e.code);
		}
	}

}
