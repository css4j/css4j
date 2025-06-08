/*

 Copyright (c) 2020-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

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
import java.util.stream.Stream;

import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Minify a CSS style sheet.
 */
public class Minify {

	private static final int DEFAULT_BUFFER_SIZE = 256;

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
	public static void main(String[] args) throws URISyntaxException, IOException {
		main(args, System.out, System.err);
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
	static void main(String[] args, PrintStream out, PrintStream err)
			throws URISyntaxException, IOException {
		if (args == null || args.length != 1) {
			printUsage(err);
			return;
		}

		URI uri = new URI(args[0]);
		Path filePath = Paths.get(uri);

		StringBuilder builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
		minifyCSS(filePath, builder, err);
		out.print(builder);
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
		return minifyCSS(css, null);
	}

	/**
	 * Minifies a CSS style sheet and prints parsing errors to a stream.
	 * 
	 * @param css the serialized style sheet.
	 * @param err the error reporting stream, or {@code null} if no stream.
	 * @return the minified serialization, or the original one if an error was
	 *         detected.
	 */
	public static String minifyCSS(String css, PrintStream err) {
		BufferSimpleWriter wri = new BufferSimpleWriter(DEFAULT_BUFFER_SIZE);
		MinifySheetHandler handler = new MinifySheetHandler(wri);
		CSSOMParser parser = new CSSOMParser();
		parser.setDocumentHandler(handler);

		try {
			parser.parseStyleSheet(new StringReader(css));
		} catch (IOException e) {
			// Cannot happen with StringReader
		} catch (Exception e) {
			// Error detected, return the source
			if (err != null) {
				e.printStackTrace(err);
			}
			return css;
		}

		return wri.toString();
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to style sheet.
	 * @return the minified serialization, or the original file if an error was
	 *         found.
	 */
	public static String minifyCSS(Path cssPath) throws IOException {
		StringBuilder builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
		minifyCSS(cssPath, builder, null);
		return builder.toString();
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to style sheet.
	 * @param buffer  the buffer to write the minified serialization, or the
	 *                original file if an error is found.
	 * @return {@code true} if the style sheet was successfully minified,
	 *         {@code false} if an error was found and the original file was
	 *         returned.
	 */
	public static boolean minifyCSS(Path cssPath, StringBuilder buffer)
			throws IOException {
		return minifyCSS(cssPath, buffer, null);
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to style sheet.
	 * @param buffer  the buffer to write the minified serialization, or the
	 *                original file if an error is found.
	 * @param err     the error reporting stream, or {@code null} if no stream.
	 * @return {@code true} if the style sheet was successfully minified,
	 *         {@code false} if an error was found and the original file was
	 *         returned.
	 */
	public static boolean minifyCSS(Path cssPath, StringBuilder buffer, PrintStream err)
			throws IOException {
		BufferSimpleWriter wri = new BufferSimpleWriter(buffer);
		MinifySheetHandler handler = new MinifySheetHandler(wri);
		CSSOMParser parser = new CSSOMParser();
		parser.setDocumentHandler(handler);

		try (BufferedReader cssReader = Files.newBufferedReader(cssPath, StandardCharsets.UTF_8)) {
			parser.parseStyleSheet(cssReader);
			return true;
		} catch (Exception e) {
			if (err != null) {
				e.printStackTrace(err);
			}
		}

		// Error detected, return the source but replace NLs (maybe 2 bytes)
		// with white space.
		buffer.setLength(0);

		try (Stream<String> stream = Files.lines(cssPath, StandardCharsets.UTF_8)) {
			stream.forEach(s -> buffer.append(s).append(' '));
		}

		return false;
	}

}
