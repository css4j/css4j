/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.uparser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.junit.Test;

import io.sf.carte.uparser.TokenProducer;

public class TokenProducerTest {

	@Test
	public void testParse() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (display: list-item)", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("display", handler.words.get(3));
		assertEquals("list-item", handler.words.get(4));
		assertEquals("(:)(:)", handler.punctbuffer.toString());
	}

	@Test
	public void testParse2() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (display: list-item)-", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("display", handler.words.get(3));
		assertEquals("list-item", handler.words.get(4));
		assertEquals("(:)(:)-", handler.punctbuffer.toString());
		assertEquals(46, handler.lastCharacterIndex);
	}

	@Test
	public void testParse2Reader() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("(display: table-cell) and (display: list-item)-"), "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("display", handler.words.get(3));
		assertEquals("list-item", handler.words.get(4));
		assertEquals("(:)(:)-", handler.punctbuffer.toString());
		assertEquals(46, handler.lastCharacterIndex);
	}

	@Test
	public void testParse2ReaderMultiComment() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("(display: table-cell) and (display: list-item)-");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("display", handler.words.get(3));
		assertEquals("list-item", handler.words.get(4));
		assertEquals("(:)(:)-", handler.punctbuffer.toString());
		assertEquals(46, handler.lastCharacterIndex);
	}

	@Test
	public void testParseNL() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("only\nscreen", "/*", "*/");
		assertEquals("only", handler.words.get(0));
		assertEquals("screen", handler.words.get(1));
		assertEquals(2, handler.words.size());
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseSimple() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderSimple() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f"), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderMultiCommentSimple() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("f"), opening, closing);
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseSimpleNoComment() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderSimpleNoComment() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f"));
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseSimple2() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("@f", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderSimple2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("@f"), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderMultiCommentSimple2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("@f"), opening, closing);
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseSimple2NoComment() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("@f");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderNoCommentSimple2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("@f"));
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseSimple3() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f@", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderSimple3() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f@"), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseNoCommentSimple3() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f@");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderNoCommentSimple3() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f@"));
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("@", handler.punctbuffer.toString());
	}

	@Test
	public void testParseSimple4() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f g", "/*", "*/");
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(" 1", handler.sepbuffer.toString());
	}

	@Test
	public void testParseNoCommentSimple4() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f g");
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(" 1", handler.sepbuffer.toString());
	}

	@Test
	public void testParseReaderNoCommentSimple4() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f g"));
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(" 1", handler.sepbuffer.toString());
	}

	@Test
	public void testParseSimple5() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f:g", "/*", "*/");
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseNoCommentSimple5() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f:g");
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderNoCommentSimple5() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f:g"));
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseSimple6() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f-g:", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f-g", handler.words.get(0));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseSimple7() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f/", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.lastWordIndex);
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderSimple7() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f/"), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.lastWordIndex);
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple7() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("f/");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(1, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.lastWordIndex);
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSimple8() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("-/", "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals("-/", handler.punctbuffer.toString());
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderSimple8() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("-/"), "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals("-/", handler.punctbuffer.toString());
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple8() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("-/");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals("-/", handler.punctbuffer.toString());
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseNoCommentSimple8() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("-/");
		assertEquals(0, handler.words.size());
		assertEquals("-/", handler.punctbuffer.toString());
		assertEquals(1, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSimple9() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("-/**/", "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(1, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderSimple9() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("-/**/"), "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(1, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple9() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("-/**/");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(1, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSimple10() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("/", "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.comments.size());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderSimple10() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("/"), "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple10() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("/");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseNoCommentSimple10() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("/");
		assertEquals(0, handler.words.size());
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.comments.size());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderNoCommentSimple10() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("/"));
		assertEquals(0, handler.words.size());
		assertEquals("/", handler.punctbuffer.toString());
		assertEquals(0, handler.comments.size());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSimple11() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("/*", "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastCommentIndex);
	}

	@Test
	public void testParseReaderSimple11() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("/*"), "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastCommentIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple11() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("/*");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(1, handler.comments.size());
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastCommentIndex);
	}

	@Test
	public void testParseSimple12() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("<!-", "<!--", "-->");
		assertEquals(0, handler.words.size());
		assertEquals("<!-", handler.punctbuffer.toString());
		assertEquals(0, handler.comments.size());
		assertEquals(2, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderSimple12() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("<!-"), "<!--", "-->");
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("<!-", handler.punctbuffer.toString());
		assertEquals(2, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple12() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("<!-");
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("<!-", handler.punctbuffer.toString());
		assertEquals(2, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSimple13() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f-", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f-", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderSimple13() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f-"), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f-", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple13() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("f-"), opening, closing);
		assertEquals(1, handler.words.size());
		assertEquals("f-", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseNoCommentSimple13() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("f-");
		assertEquals(1, handler.words.size());
		assertEquals("f-", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderNoCommentSimple13() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f-"));
		assertEquals(1, handler.words.size());
		assertEquals("f-", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(0, handler.lastWordIndex);
	}

	@Test
	public void testParseSimple14() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("-", "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderSimple14() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("-"), "/*", "*/");
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple14() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("-"), opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseNoCommentSimple14() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("-");
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderNoCommentSimple14() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("-"));
		assertEquals(0, handler.words.size());
		assertEquals(0, handler.comments.size());
		assertEquals("-", handler.punctbuffer.toString());
		assertEquals(0, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSimple15() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("\\64  ", "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("4", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.escaped.size());
		assertEquals("6", handler.escaped.get(0));
		assertEquals(" 3 4", handler.sepbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderSimple15() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("\\64  "), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("4", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.escaped.size());
		assertEquals("6", handler.escaped.get(0));
		assertEquals(" 3 4", handler.sepbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderMultiCommentSimple15() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("\\64  "), opening, closing);
		assertEquals(1, handler.words.size());
		assertEquals("4", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.escaped.size());
		assertEquals("6", handler.escaped.get(0));
		assertEquals(" 3 4", handler.sepbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseNoCommentSimple15() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("\\64  ");
		assertEquals(1, handler.words.size());
		assertEquals("4", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.escaped.size());
		assertEquals("6", handler.escaped.get(0));
		assertEquals(" 3 4", handler.sepbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseReaderNoCommentSimple15() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("\\64  "));
		assertEquals(1, handler.words.size());
		assertEquals("4", handler.words.get(0));
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.escaped.size());
		assertEquals("6", handler.escaped.get(0));
		assertEquals(" 3 4", handler.sepbuffer.toString());
		assertEquals(2, handler.lastWordIndex);
	}

	@Test
	public void testParseComment() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("/*!rtl:ignore*/-o-foo:-o-bar;/*!rtl:ignore*/foo:bar/*!rtl:ignore*/", "/*", "*/");
		assertEquals(4, handler.words.size());
		assertEquals("-o-foo", handler.words.get(0));
		assertEquals("-o-bar", handler.words.get(1));
		assertEquals("foo", handler.words.get(2));
		assertEquals("bar", handler.words.get(3));
		assertEquals(":;:", handler.punctbuffer.toString());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore", handler.comments.get(0));
		assertEquals("!rtl:ignore", handler.comments.get(1));
		assertEquals("!rtl:ignore", handler.comments.get(2));
		assertEquals(47, handler.lastCharacterIndex);
		assertEquals(48, handler.lastWordIndex);
		assertEquals(51, handler.lastCommentIndex);
		assertEquals(-1, handler.lastControlIndex);
	}

	@Test
	public void testParseCommentNL() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("/*!rtl:ignore\n*/-o-foo:-o-bar;/*!rtl:ignore\n*/foo:bar/*!rtl:ignore\n*/", "/*", "*/");
		assertEquals(4, handler.words.size());
		assertEquals("-o-foo", handler.words.get(0));
		assertEquals("-o-bar", handler.words.get(1));
		assertEquals("foo", handler.words.get(2));
		assertEquals("bar", handler.words.get(3));
		assertEquals(":;:", handler.punctbuffer.toString());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore\n", handler.comments.get(0));
		assertEquals("!rtl:ignore\n", handler.comments.get(1));
		assertEquals("!rtl:ignore\n", handler.comments.get(2));
		assertEquals(49, handler.lastCharacterIndex);
		assertEquals(50, handler.lastWordIndex);
		assertEquals(53, handler.lastCommentIndex);
		assertEquals(66, handler.lastControlIndex);
		assertEquals(3, handler.control10);
	}

	@Test
	public void testParseCommentCDO() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("<!--!rtl:ignore-->-o-foo:-o-bar;<!--!rtl:ignore-->foo:bar<!--!rtl:ignore-->", "<!--", "-->");
		assertEquals(4, handler.words.size());
		assertEquals("-o-foo", handler.words.get(0));
		assertEquals("-o-bar", handler.words.get(1));
		assertEquals("foo", handler.words.get(2));
		assertEquals("bar", handler.words.get(3));
		assertEquals(":;:", handler.punctbuffer.toString());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore", handler.comments.get(0));
		assertEquals("!rtl:ignore", handler.comments.get(1));
		assertEquals("!rtl:ignore", handler.comments.get(2));
	}

	@Test
	public void testParseReaderComment() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("/*!rtl:ignore*/-o-foo:-o-bar;/*!rtl:ignore*/foo:bar/*!rtl:ignore*/"), "/*", "*/");
		assertEquals(4, handler.words.size());
		assertEquals("-o-foo", handler.words.get(0));
		assertEquals("-o-bar", handler.words.get(1));
		assertEquals("foo", handler.words.get(2));
		assertEquals("bar", handler.words.get(3));
		assertEquals(":;:", handler.punctbuffer.toString());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore", handler.comments.get(0));
		assertEquals("!rtl:ignore", handler.comments.get(1));
		assertEquals("!rtl:ignore", handler.comments.get(2));
		assertEquals(47, handler.lastCharacterIndex);
		assertEquals(48, handler.lastWordIndex);
		assertEquals(51, handler.lastCommentIndex);
		assertEquals(-1, handler.lastControlIndex);
	}

	@Test
	public void testParseReaderCommentNL() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("/*!rtl:ignore\n*/-o-foo:-o-bar;/*!rtl:ignore\n*/foo:bar/*!rtl:ignore\n*/"), "/*", "*/");
		assertEquals(4, handler.words.size());
		assertEquals("-o-foo", handler.words.get(0));
		assertEquals("-o-bar", handler.words.get(1));
		assertEquals("foo", handler.words.get(2));
		assertEquals("bar", handler.words.get(3));
		assertEquals(":;:", handler.punctbuffer.toString());
		assertEquals(3, handler.comments.size());
		assertEquals("!rtl:ignore\n", handler.comments.get(0));
		assertEquals("!rtl:ignore\n", handler.comments.get(1));
		assertEquals("!rtl:ignore\n", handler.comments.get(2));
		assertEquals(49, handler.lastCharacterIndex);
		assertEquals(50, handler.lastWordIndex);
		assertEquals(53, handler.lastCommentIndex);
		assertEquals(66, handler.lastControlIndex);
		assertEquals(3, handler.control10);
	}

	@Test
	public void testParseReaderSimple5() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f:g"), "/*", "*/");
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.words.get(0));
		assertEquals("g", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderSimple6() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("f-g:"), "/*", "*/");
		assertEquals(1, handler.words.size());
		assertEquals("f-g", handler.words.get(0));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseHyphen() {
		int[] allowInWords = { 37, 45, 46 }; // %-.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (width: calc(100% - 2em))", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("width", handler.words.get(3));
		assertEquals("calc", handler.words.get(4));
		assertEquals("100%", handler.words.get(5));
		assertEquals("2em", handler.words.get(6));
		assertEquals("(:)(:(-))", handler.punctbuffer.toString());
		assertEquals("(((", handler.openbuffer.toString());
		assertEquals(")))", handler.closebuffer.toString());
		assertEquals(" 9 21 25 33 43 45", handler.sepbuffer.toString());
	}

	@Test
	public void testParseAllowedInComment() {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("<!ELEMENT> <!-- comment -->", "<!--", "-->");
		assertEquals(1, handler.words.size());
		assertEquals("<!ELEMENT", handler.words.get(0));
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals(">", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(" 10", handler.sepbuffer.toString());
	}

	@Test
	public void testParseReaderAllowedInComment() throws IOException {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("<!ELEMENT> <!-- comment -->"), "<!--", "-->");
		assertEquals(1, handler.words.size());
		assertEquals("<!ELEMENT", handler.words.get(0));
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals(">", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(" 10", handler.sepbuffer.toString());
	}

	@Test
	public void testParseReaderMultiAllowedInComment() throws IOException {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("<!ELEMENT> <!-- comment -->"), opening, closing);
		assertEquals(1, handler.words.size());
		assertEquals("<!ELEMENT", handler.words.get(0));
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals(">", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(" 10", handler.sepbuffer.toString());
	}

	@Test
	public void testParseAllowedBeforeComment() {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("<<!-- comment --><", "<!--", "-->");
		assertEquals(0, handler.words.size());
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals("<<", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(0, handler.sepbuffer.length());
	}

	@Test
	public void testParseAllowedBeforeComment2() {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("!<!-- comment -->!", "<!--", "-->");
		assertEquals(0, handler.words.size());
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals("!!", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(0, handler.sepbuffer.length());
	}

	@Test
	public void testParseReaderAllowedBeforeComment() throws IOException {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("<<!-- comment --><"), "<!--", "-->");
		assertEquals(0, handler.words.size());
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals("<<", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(0, handler.sepbuffer.length());
	}

	@Test
	public void testParseReaderAllowedBeforeComment2() throws IOException {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("!<!-- comment -->!"), "<!--", "-->");
		assertEquals(0, handler.words.size());
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals("!!", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(0, handler.sepbuffer.length());
	}

	@Test
	public void testParseReaderMultiCommentAllowedBeforeComment() throws IOException {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("<<!-- comment --><"), opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals("<<", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(0, handler.sepbuffer.length());
	}

	@Test
	public void testParseReaderMultiCommentAllowedBeforeComment2() throws IOException {
		int[] allowInWords = { 33, 60 }; // <!
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(new StringReader("/<!-- comment -->/"), opening, closing);
		assertEquals(0, handler.words.size());
		assertEquals(" comment ", handler.comments.get(0));
		assertEquals("//", handler.punctbuffer.toString());
		assertEquals(0, handler.openbuffer.length());
		assertEquals(0, handler.closebuffer.length());
		assertEquals(0, handler.sepbuffer.length());
	}

	@Test
	public void testParseQuotes() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (content: 'foo bar')", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(" 9 21 25 35", handler.sepbuffer.toString());
		assertEquals(27, handler.lastWordIndex);
	}

	@Test
	public void testParseQuotesAndComment() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) /* This is a comment */ and (content: 'foo bar')",
				"/*", "*/");
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals(" 9 21 45 49 59", handler.sepbuffer.toString());
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(22, handler.lastCommentIndex);
		assertEquals(51, handler.lastWordIndex);
		assertEquals(58, handler.lastCharacterIndex);
	}

	@Test
	public void testParseQuotesAndCommentCDO() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) <!-- This is a comment --> and (content: 'foo bar')",
				"<!--", "-->");
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals(" 9 21 48 52 62", handler.sepbuffer.toString());
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(22, handler.lastCommentIndex);
		assertEquals(54, handler.lastWordIndex);
		assertEquals(61, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderQuotesAndComment() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("(display: table-cell) /* This is a comment */ and (content: 'foo bar')"), "/*",
				"*/");
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(" 9 21 45 49 59", handler.sepbuffer.toString());
	}

	@Test
	public void testParseReaderQuotesAndCommentCDO() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(new StringReader("(display: table-cell) <!-- This is a comment --> and (content: 'foo bar')  "), "<!--",
				"-->");
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(" 9 21 48 52 62 73 74", handler.sepbuffer.toString());
		assertEquals(61, handler.lastCharacterIndex);
	}

	@Test
	public void testParseQuotesAndMultiComment() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) /* This is a comment */ and\n//This too\n (content: 'foo bar')");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "//" };
		String[] closing = { "*/", "\n" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(2, handler.control10);
		assertEquals(60, handler.lastControlIndex);
		assertEquals(63, handler.lastWordIndex);
		assertEquals(70, handler.lastCharacterIndex);
	}

	@Test
	public void testParseQuotesAndMultiComment2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) /* This is a comment */ and (content: 'foo bar')\n//This too\n");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "//" };
		String[] closing = { "*/", "\n" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(51, handler.lastWordIndex);
		assertEquals(58, handler.lastCharacterIndex);
		assertEquals(81, handler.lastControlIndex);
		assertEquals(2, handler.control10);
	}

	@Test
	public void testParseQuotesAndMultiCommentEOL() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) /* This is a comment */ and (content: 'foo bar')\n//This too");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "//" };
		String[] closing = { "*/", "\n" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(" This is a comment ", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(51, handler.lastWordIndex);
		assertEquals(58, handler.lastCharacterIndex);
		assertEquals(70, handler.lastControlIndex);
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseMultiCommentEOLBad() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader("(display: table-cell) /* This begins like a comment");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "//" };
		String[] closing = { "*/", "\n" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(" This begins like a comment", handler.comments.get(0));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("(", handler.openbuffer.toString());
		assertEquals(")", handler.closebuffer.toString());
		assertEquals("(:)", handler.punctbuffer.toString());
		assertEquals(1, handler.errorCounter);
	}

	@Test
	public void testParseCommentEOLBad() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader("(display: table-cell) /* This begins like a comment");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(re, "/*", "*/");
		assertEquals(" This begins like a comment", handler.comments.get(0));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("(", handler.openbuffer.toString());
		assertEquals(")", handler.closebuffer.toString());
		assertEquals("(:)", handler.punctbuffer.toString());
		assertEquals(1, handler.errorCounter);
	}

	@Test
	public void testParseReaderCommentCDOCDC() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) <!--- This is a --comment ---> and (content: 'foo bar')\n<!--This too-->");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String opening = "<!--";
		String closing = "-->";
		tp.parse(re, opening, closing);
		assertEquals("- This is a --comment -", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(58, handler.lastWordIndex);
		assertEquals(65, handler.lastCharacterIndex);
		assertEquals(77, handler.lastControlIndex);
		assertEquals(78, handler.lastCommentIndex);
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseReaderCommentCDOCDC2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) <!--- This is a --comment ----> and (content: 'foo bar')\n<!--This too-->");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String opening = "<!--";
		String closing = "-->";
		tp.parse(re, opening, closing);
		assertEquals("- This is a --comment --", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(59, handler.lastWordIndex);
		assertEquals(66, handler.lastCharacterIndex);
		assertEquals(78, handler.lastControlIndex);
		assertEquals(79, handler.lastCommentIndex);
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseReaderMultiCommentCDOCDC() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) <!--- This is a --comment ---> and (content: 'foo bar')\n/*This too*/");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals("- This is a --comment -", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(58, handler.lastWordIndex);
		assertEquals(65, handler.lastCharacterIndex);
		assertEquals(77, handler.lastControlIndex);
		assertEquals(78, handler.lastCommentIndex);
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseReaderMultiCommentCDOCDCNL() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) <!--- This is a \n--comment ---> and (content: 'foo bar')/*\nThis\n too*/");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals("- This is a \n--comment -", handler.comments.get(0));
		assertEquals("\nThis\n too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(59, handler.lastWordIndex);
		assertEquals(66, handler.lastCharacterIndex);
		assertEquals(78, handler.lastCommentIndex);
		assertEquals(85, handler.lastControlIndex);
		assertEquals(3, handler.control10);
	}

	@Test
	public void testParseReaderMultiCommentCDOCDC2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) <!--- This is a --comment ----> and (content: 'foo bar')\n/*This too*/");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "<!--" };
		String[] closing = { "*/", "-->" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals("- This is a --comment --", handler.comments.get(0));
		assertEquals("This too", handler.comments.get(1));
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(59, handler.lastWordIndex);
		assertEquals(66, handler.lastCharacterIndex);
		assertEquals(78, handler.lastControlIndex);
		assertEquals(79, handler.lastCommentIndex);
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseQuotesEscaped() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (content: 'foo\\' bar')", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo\\' bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
	}

	@Test
	public void testParseQuotesUnescapedNL() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: 'foo\nbar", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("bar", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseQuotesUnescapedNLAccept() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.setAcceptNewlineEndingQuote(true);
		tp.parse("content: 'foo\nbar", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("'foo'", handler.words.get(1));
		assertEquals("bar", handler.words.get(2));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
		assertEquals(14, handler.lastWordIndex);
	}

	@Test
	public void testParseQuotesUnescapedNLEOF() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: 'foo\n", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(1, handler.words.size());
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
		assertEquals(1, handler.errorCounter);
	}

	@Test
	public void testParseQuotesUnescapedNLEOFAccept() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.setAcceptEofEndingQuoted(true);
		tp.parse("content: 'foo", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("'foo'", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.errorCounter);
	}

	@Test
	public void testParseQuotesEscapedControl() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (content: 'foo\\\n bar')", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo\n bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseQuotesEscapedControlCRLF() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("(display: table-cell) and (content: 'foo\\\r\n bar')", "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo\n bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReader() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("(display: table-cell) and (display: list-item)");
		tp.parse(re, "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("display", handler.words.get(3));
		assertEquals("list-item", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
		assertEquals(36, handler.lastWordIndex);
		assertEquals(34, handler.lastCharacterIndex);
	}

	@Test
	public void testParseReaderNL() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("only\nscreen");
		tp.parse(re, "/*", "*/");
		assertEquals("only", handler.words.get(0));
		assertEquals("screen", handler.words.get(1));
		assertEquals(2, handler.words.size());
		assertEquals(0, handler.punctbuffer.length());
		assertEquals(1, handler.control10);
		assertEquals(5, handler.lastWordIndex);
	}

	@Test
	public void testParseEscaped() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: \\f435;", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(2, handler.words.size());
		assertEquals("f", handler.escaped.get(0));
		assertEquals("435", handler.words.get(1));
		assertEquals(":;", handler.punctbuffer.toString());
		assertEquals(14, handler.lastCharacterIndex);
	}

	@Test
	public void testParseEscapedBackslash() {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: \\\\f435;", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(2, handler.words.size());
		assertEquals(1, handler.escaped.size());
		assertEquals("\\", handler.escaped.get(0));
		assertEquals("f435", handler.words.get(1));
		assertEquals(":;", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderHyphen() throws IOException {
		int[] allowInWords = { 45, 37, 46 }; // -%.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("(display: table-cell) and (width: calc(100% - 2em))");
		tp.parse(re, "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("width", handler.words.get(3));
		assertEquals("calc", handler.words.get(4));
		assertEquals("100%", handler.words.get(5));
		assertEquals("2em", handler.words.get(6));
		assertEquals("(((", handler.openbuffer.toString());
		assertEquals(")))", handler.closebuffer.toString());
		assertEquals("(:)(:(-))", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderQuotes() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("(display: table-cell) and (content: 'foo bar')");
		tp.parse(re, "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderQuotesEscaped() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("(display: table-cell) and (content: 'foo\\' bar')");
		tp.parse(re, "/*", "*/");
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("and", handler.words.get(2));
		assertEquals("content", handler.words.get(3));
		assertEquals("'foo\\' bar'", handler.words.get(4));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)(:)", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderQuotesAndCommentDisabled() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader(
				"(display: table-cell) /* no-comment */ and (content: 'foo bar')\n//neither\n");
		MyTokenHandler handler = new DisableCommentsTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		String[] opening = { "/*", "//" };
		String[] closing = { "*/", "\n" };
		tp.parseMultiComment(re, opening, closing);
		assertEquals(0, handler.comments.size());
		assertEquals("display", handler.words.get(0));
		assertEquals("table-cell", handler.words.get(1));
		assertEquals("no-comment", handler.words.get(2));
		assertEquals("and", handler.words.get(3));
		assertEquals("content", handler.words.get(4));
		assertEquals("'foo bar'", handler.words.get(5));
		assertEquals("neither", handler.words.get(6));
		assertEquals("((", handler.openbuffer.toString());
		assertEquals("))", handler.closebuffer.toString());
		assertEquals("(:)/**/(:)//", handler.punctbuffer.toString());
		assertEquals(0, handler.errorCounter);
		assertEquals(-1, handler.lastCommentIndex);
		assertEquals(2, handler.control10);
	}

	@Test
	public void testParseReaderQuotesUnescapedNL() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader("content: 'foo\nbar");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("bar", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
		assertEquals(1, handler.errorCounter);
	}

	@Test
	public void testParseReaderQuotesUnescapedNLAccept() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader("content: 'foo\nbar");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.setAcceptNewlineEndingQuote(true);
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("'foo'", handler.words.get(1));
		assertEquals("bar", handler.words.get(2));
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
	}

	@Test
	public void testParseReaderQuotesUnescapedNLEOF() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader("content: 'foo\n");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(1, handler.words.size());
		assertEquals(":", handler.punctbuffer.toString());
		assertEquals(1, handler.control10);
		assertEquals(1, handler.errorCounter);
	}

	@Test
	public void testParseReaderQuotesUnescapedNLEOFAccept() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		StringReader re = new StringReader("content: 'foo");
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.setAcceptEofEndingQuoted(true);
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("'foo'", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseBackslash() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: \\\\", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("\\", handler.escaped.get(0));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseBackslash2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: \\\\;", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("\\", handler.escaped.get(0));
		assertEquals(":;", handler.punctbuffer.toString());
		assertEquals(11, handler.lastCharacterIndex);
	}

	@Test
	public void testParseQuotedBackslash() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: '\\'", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(1, handler.words.size());
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseQuotedBackslash2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: '\\\\'", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("'\\\\'", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderBackslash() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("content: \\\\");
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("\\", handler.escaped.get(0));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderQuotedBackslash() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("content: '\\'");
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(1, handler.words.size());
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseReaderQuotedBackslash2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		StringReader re = new StringReader("content: '\\\\'");
		tp.parse(re, "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals("'\\\\'", handler.words.get(1));
		assertEquals(":", handler.punctbuffer.toString());
	}

	@Test
	public void testParseSurrogate() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: 🚧;", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(":🚧;", handler.punctbuffer.toString());
		assertEquals(11, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSurrogate2() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: \ud83d\udd0a;", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(":🔊;", handler.punctbuffer.toString());
		assertEquals(11, handler.lastCharacterIndex);
	}

	@Test
	public void testParseSurrogate3() throws IOException {
		int[] allowInWords = { 45, 46 }; // -.
		MyTokenHandler handler = new MyTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse("content: \ud950;", "/*", "*/");
		assertEquals("content", handler.words.get(0));
		assertEquals(":\ud950;", handler.punctbuffer.toString());
		assertEquals(10, handler.lastCharacterIndex);
	}

	private static class MyTokenHandler implements TokenHandler {

		TokenControl control = null;
		LinkedList<String> words = new LinkedList<String>();
		LinkedList<String> escaped = new LinkedList<String>();
		LinkedList<String> comments = new LinkedList<String>();
		StringBuilder punctbuffer = new StringBuilder();
		StringBuilder sepbuffer = new StringBuilder();
		StringBuilder openbuffer = new StringBuilder();
		StringBuilder closebuffer = new StringBuilder();
		int control10 = 0;
		int errorCounter = 0;
		int lastWordIndex = -1;
		int lastCharacterIndex = -1;
		int lastCommentIndex = -1;
		int lastControlIndex = -1;

		@Override
		public void word(int index, CharSequence word) {
			words.add(word.toString());
			lastWordIndex = index;
		}

		@Override
		public void separator(int index, int cp) {
			sepbuffer.append(' ').append(index);
		}

		@Override
		public void openGroup(int index, int codepoint) {
			char[] chars = Character.toChars(codepoint);
			openbuffer.append(chars);
			punctbuffer.append(chars);
		}

		@Override
		public void closeGroup(int index, int codepoint) {
			char[] chars = Character.toChars(codepoint);
			closebuffer.append(chars);
			punctbuffer.append(chars);
		}

		@Override
		public void character(int index, int codepoint) {
			char[] chars = Character.toChars(codepoint);
			punctbuffer.append(chars);
			lastCharacterIndex = index;
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
			char c = (char) quoteCp;
			StringBuilder buf = new StringBuilder(quoted.length() + 2);
			buf.append(c).append(quoted).append(c);
			words.add(buf.toString());
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			quoted(index, quoted, quoteCp);
		}

		@Override
		public void escaped(int index, int codepoint) {
			escaped.add(new String(Character.toChars(codepoint)));
		}

		@Override
		public void control(int index, int codepoint) {
			if (codepoint == 10) {
				control10++;
			}
			lastControlIndex = index;
		}

		@Override
		public void quotedNewlineChar(int index, int codepoint) {
			if (codepoint == 10) {
				control10++;
			}
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			comments.add(comment);
			lastCommentIndex = index;
		}

		@Override
		public void endOfStream(int len) {
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			errorCounter++;
		}

		@Override
		public void tokenControl(TokenControl control) {
			this.control = control;
		}

	}

	class DisableCommentsTokenHandler extends MyTokenHandler {

		@Override
		public void tokenControl(TokenControl control) {
			control.disableAllComments();
		}

	}

}
