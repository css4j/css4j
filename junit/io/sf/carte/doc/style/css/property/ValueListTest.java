/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

public class ValueListTest {

	private ValueFactory factory;

	@Before
	public void setUp() {
		factory = new ValueFactory();
	}

	@Test
	public void testItem() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		ws.add(factory.parseProperty("repeat"));
		ws.add(factory.parseProperty("repeat"));
		assertNull(cs.item(-1));
		assertNull(cs.item(2));
		assertNull(ws.item(-1));
		assertNull(ws.item(2));
		assertEquals("thin", cs.item(0).getCssText());
		assertEquals("repeat", ws.item(0).getCssText());
		assertEquals("thin, thick", cs.getCssText());
		assertEquals("thin,thick", cs.getMinifiedCssText(""));
		assertEquals("repeat repeat", ws.getCssText());
		assertEquals("repeat repeat", ws.getMinifiedCssText(""));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <custom-ident>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <custom-ident>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("repeat+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
	}

	@Test
	public void testMatchLengths() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		cs.add(factory.parseProperty("16px"));
		cs.add(factory.parseProperty("24mm"));
		ws.add(factory.parseProperty("25px"));
		ws.add(factory.parseProperty("18pt"));
		ws.add(factory.parseProperty("2cm"));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
	}

	@Test
	public void testMatchAttrsTrue() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		cs.add(factory.parseProperty("attr(data-length length, 16px)"));
		cs.add(factory.parseProperty("attr(data-length length)"));
		ws.add(factory.parseProperty("attr(data-length length, 25px)"));
		ws.add(factory.parseProperty("attr(data-length length, 18pt)"));
		ws.add(factory.parseProperty("attr(data-length length, 2cm)"));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
	}

	@Test
	public void testMatchAttrsPending() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		cs.add(factory.parseProperty("attr(data-length length, 8%)"));
		cs.add(factory.parseProperty("attr(data-length length)"));
		ws.add(factory.parseProperty("attr(data-length length, 16pt)"));
		ws.add(factory.parseProperty("attr(data-length length)"));
		ws.add(factory.parseProperty("attr(data-length length, 15%)"));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length-percentage>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length-percentage>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length-percentage>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length-percentage>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.PENDING, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident> | <length>#");
		assertEquals(Match.PENDING, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <length>#");
		assertEquals(Match.PENDING, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.PENDING, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<percentage> | <length>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
	}

	@Test
	public void testMatchVar() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("var(--thick)"));
		ws.add(factory.parseProperty("repeat"));
		ws.add(factory.parseProperty("var(--repeat)"));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<custom-ident>#");
		assertEquals(Match.PENDING, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.PENDING, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <custom-ident>#");
		assertEquals(Match.PENDING, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <custom-ident>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.PENDING, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("repeat+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.PENDING, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>#");
		assertEquals(Match.PENDING, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.PENDING, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<string> | <custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		//
		ws.add(factory.parseProperty("18px"));
		syn = syntaxParser.parseSyntax("<custom-ident>+");
		assertEquals(Match.FALSE, ws.matches(syn));
	}

	@Test
	public void testMatchTransformLists() {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		ws.add(factory.parseProperty("translate(-10px, -20px)"));
		ws.add(factory.parseProperty("scale(2)"));
		ws.add(factory.parseProperty("rotate(45deg)"));
		ValueList ws2 = ValueList.createWSValueList();
		ws2.add(factory.parseProperty("rotate(15deg)"));
		ws2.add(factory.parseProperty("scale(2)"));
		ws2.add(factory.parseProperty("translate(20px)"));
		cs.add(ws);
		cs.add(ws2);
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<transform-list>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		assertEquals(Match.TRUE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		assertEquals(Match.TRUE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		assertEquals(Match.TRUE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-function>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		assertEquals(Match.TRUE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-function>#");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		assertEquals(Match.FALSE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-function>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		assertEquals(Match.FALSE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<color>#");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		assertEquals(Match.FALSE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("<color>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		assertEquals(Match.FALSE, ws2.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
	}

	@Test
	public void testMatchLexical() throws CSSParseException, IOException {
		ValueList cs = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		//
		CSSParser parser = new CSSParser();
		LexicalValue length = new LexicalValue();
		length.setLexicalUnit(parser.parsePropertyValue(new StringReader("16px")));
		LexicalValue length2 = new LexicalValue();
		length2.setLexicalUnit(parser.parsePropertyValue(new StringReader("34pt")));
		LexicalValue lengthCS = new LexicalValue();
		lengthCS.setLexicalUnit(parser.parsePropertyValue(new StringReader("2.3mm,22px")));
		LexicalValue lengthCS2 = new LexicalValue();
		lengthCS2.setLexicalUnit(parser.parsePropertyValue(new StringReader("0.4cm,2pt,8px")));
		LexicalValue lengthWS = new LexicalValue();
		lengthWS.setLexicalUnit(parser.parsePropertyValue(new StringReader("14pt 25px")));
		LexicalValue lengthWS2 = new LexicalValue();
		lengthWS2.setLexicalUnit(parser.parsePropertyValue(new StringReader("2pt 2.5px 0.1cm")));
		//
		cs.add(length);
		ws.add(length);
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>+");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		//
		cs.add(length2);
		ws.add(length2);
		//
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		//
		cs.add(lengthCS);
		cs.add(lengthCS2);
		ws.add(lengthWS);
		ws.add(lengthWS2);
		//
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
		//
		cs.add(lengthWS);
		cs.add(lengthWS2);
		ws.add(lengthCS);
		ws.add(lengthCS2);
		//
		syn = syntaxParser.parseSyntax("<length>#");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<length>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("thin+ | thick+");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<custom-ident>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("<color>");
		assertEquals(Match.FALSE, cs.matches(syn));
		assertEquals(Match.FALSE, ws.matches(syn));
		syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		assertEquals(Match.TRUE, ws.matches(syn));
	}

	@Test
	public void testMatchUniversal() {
		ValueList cs = ValueList.createCSValueList();
		cs.add(factory.parseProperty("1"));
		cs.add(factory.parseProperty("16px"));
		cs.add(factory.parseProperty("auto"));
		cs.add(factory.parseProperty("#00f"));
		cs.add(factory.parseProperty("calc(2*3)"));
		cs.add(factory.parseProperty("foo(bar)"));
		cs.add(factory.parseProperty("U+403"));
		cs.add(factory.parseProperty("U+2??"));
		cs.add(factory.parseProperty("U+22-28"));
		cs.add(factory.parseProperty("'Hi'"));
		cs.add(factory.parseProperty("linear-gradient(to top right, red, white, blue)"));
		cs.add(factory.parseProperty("uri('https://www.example.com/file')"));
		cs.add(factory.parseProperty("counters(ListCounter,'. ')"));
		cs.add(factory.parseProperty("counter(ListCounter, decimal)"));
		//
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("*");
		assertEquals(Match.TRUE, cs.matches(syn));
		syn = syntaxParser.parseSyntax("<length>+");
		assertEquals(Match.FALSE, cs.matches(syn));
		syn = syntaxParser.parseSyntax("<transform-list> | <length>#");
		assertEquals(Match.FALSE, cs.matches(syn));
	}

	@Test
	public void testHashCode() {
		ValueList cs = ValueList.createCSValueList();
		ValueList cs2 = ValueList.createCSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		cs.add(factory.parseProperty("medium"));
		cs2.add(factory.parseProperty("thin"));
		cs2.add(factory.parseProperty("thick"));
		cs2.add(factory.parseProperty("medium"));
		assertEquals(cs.hashCode(), cs2.hashCode());
	}

	@Test
	public void testEqualsObject() {
		ValueList cs = ValueList.createCSValueList();
		ValueList cs2 = ValueList.createCSValueList();
		ValueList ws = ValueList.createWSValueList();
		assertFalse(cs.equals(ws));
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		cs.add(factory.parseProperty("medium"));
		assertFalse(cs.equals(ws));
		ws.add(factory.parseProperty("thin"));
		ws.add(factory.parseProperty("thick"));
		ws.add(factory.parseProperty("medium"));
		assertFalse(cs.equals(ws));
		cs2.add(factory.parseProperty("thin"));
		cs2.add(factory.parseProperty("thick"));
		assertFalse(cs.equals(cs2));
		cs2.add(factory.parseProperty("medium"));
		assertTrue(cs.equals(cs2));
	}

	@Test
	public void testClone() {
		ValueList cs = ValueList.createCSValueList();
		cs.add(factory.parseProperty("thin"));
		cs.add(factory.parseProperty("thick"));
		cs.add(factory.parseProperty("medium"));
		ValueList clon = cs.clone();
		assertEquals(cs.getCssValueType(), clon.getCssValueType());
		assertEquals(cs.getPrimitiveType(), clon.getPrimitiveType());
		assertEquals(cs.getLength(), clon.getLength());
		assertEquals(cs.item(0), clon.item(0));
		assertEquals(cs.getCssText(), clon.getCssText());
	}

	@Test
	public void testGetCssTextCSEscaped() {
		ValueList cs = ValueList.createCSValueList();
		cs.add(factory.parseProperty("\\100"));
		cs.add(factory.parseProperty("\\112"));
		cs.add(factory.parseProperty("\\12a"));
		cs.add(factory.parseProperty("\\14c"));
		assertEquals("\\100 , \\112 , \\12a , \\14c ", cs.getCssText());
		assertEquals("\u0100,\u0112,\u012a,\u014c", cs.getMinifiedCssText(""));
	}

	@Test
	public void testGetCssTextWS() {
		ValueList ws = ValueList.createWSValueList();
		ws.add(factory.parseProperty("rgba(120, 47, 253, 0.9)"));
		ws.add(factory.parseProperty("rgb(10, 4, 2)"));
		assertEquals("rgba(120, 47, 253, 0.9) #0a0402", ws.getCssText());
		assertEquals("rgba(120,47,253,.9) #0a0402", ws.getMinifiedCssText(""));
	}

	@Test
	public void testGetCssTextWSEscaped() {
		ValueList ws = ValueList.createWSValueList();
		ws.add(factory.parseProperty("\\100"));
		ws.add(factory.parseProperty("\\112"));
		ws.add(factory.parseProperty("\\12a"));
		ws.add(factory.parseProperty("\\14c"));
		assertEquals("\\100  \\112  \\12a  \\14c ", ws.getCssText());
		assertEquals("Ā Ē Ī Ō", ws.getMinifiedCssText(""));
	}

	@Test
	public void testGetCssTextBracket() {
		ValueList br = ValueList.createBracketValueList();
		br.add(factory.parseProperty("header-\\100"));
		br.add(factory.parseProperty("header-\\112"));
		br.add(factory.parseProperty("main-\\12a"));
		br.add(factory.parseProperty("main-\\14c"));
		assertEquals("[header-\\100  header-\\112  main-\\12a  main-\\14c ]", br.getCssText());
		assertEquals("[header-Ā header-Ē main-Ī main-Ō]", br.getMinifiedCssText(""));
	}

}
