/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.stream.Stream;

import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;

/**
 * Minify a CSS style sheet.
 */
public class Minify {

	/**
	 * Prints to standard output a minification of the CSS style sheet located at
	 * the supplied URI.
	 * <p>
	 * If the given sheet contains error(s) and therefore cannot be reliably
	 * minified, the original source sheet is printed.
	 * </p>
	 * 
	 * @param args the URI to a style sheet.
	 * @throws URISyntaxException       if the URI has wrong syntax.
	 * @throws IllegalArgumentException if the URI is otherwise incorrect.
	 * @throws IOException              if an I/O error happened.
	 */
	public static void Main(String args[]) throws URISyntaxException, IOException {
		Main(args, System.out, System.err);
	}

	/**
	 * Outputs a minification of the CSS style sheet located at the supplied URI.
	 * <p>
	 * If the sheet contains error(s) and therefore cannot be reliably minified, the
	 * original source sheet is printed.
	 * </p>
	 * 
	 * @param args the URI to a style sheet.
	 * @param out  the output stream.
	 * @param err  the error reporting stream.
	 * @throws URISyntaxException       if the URI has wrong syntax.
	 * @throws IllegalArgumentException if the URI is otherwise incorrect.
	 * @throws IOException              if an I/O error happened.
	 */
	public static void Main(String args[], PrintStream out, PrintStream err)
			throws URISyntaxException, IOException {
		if (args == null || args.length != 1) {
			printUsage(err);
			return;
		}

		URI uri = new URI(args[0]);
		Path filePath = Paths.get(uri);

		out.print(minifyCSS(filePath));
	}

	/**
	 * Print the usage.
	 * 
	 * @param err the error reporting stream.
	 */
	private static void printUsage(PrintStream err) {
		err.println("Usage: " + Minify.class.getName() + " <style-sheet-uri>");
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param css the serialized style sheet.
	 * @return the minified serialization, or the original one if an error was
	 *         detected.
	 */
	public static String minifyCSS(String css) {
		AbstractCSSStyleSheet sheet = createStyleSheet();
		// Parse and check for return value
		try {
			if (sheet.parseStyleSheet(new StringReader(css), CSSStyleSheet.COMMENTS_IGNORE)) {
				// Parsed without errors
				return sheet.toMinifiedString();
			}
		} catch (IOException e) {
			// Cannot happen with StringReader
		}
		// Error detected, return the source
		return css;
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to style sheet.
	 * @return the minified serialization, or the original file if an error was
	 *         found.
	 */
	public static String minifyCSS(Path cssPath) throws IOException {
		// Create an empty style sheet
		AbstractCSSStyleSheet sheet = createStyleSheet();

		// Parse and check for return value
		try (BufferedReader cssReader = Files.newBufferedReader(cssPath, StandardCharsets.UTF_8)) {
			if (sheet.parseStyleSheet(cssReader, CSSStyleSheet.COMMENTS_IGNORE)) {
				// Parsed without errors
				return sheet.toMinifiedString();
			}
		}
		// Error detected, return the source
		StringBuilder builder = new StringBuilder(256);

		try (Stream<String> stream = Files.lines(cssPath, StandardCharsets.UTF_8)) {
			stream.forEach(s -> builder.append(s).append(' '));
		}
		return builder.toString();
	}

	private static AbstractCSSStyleSheet createStyleSheet() {
		// Instantiate any style sheet factory, with parser flags allowing IE hacks
		AbstractCSSStyleSheetFactory cssFactory = new CSSDOMImplementation(
				EnumSet.allOf(Parser.Flag.class));
		return cssFactory.createStyleSheet(null, null);
	}

}
