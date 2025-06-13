/*

 Copyright (c) 2020-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.stream.Stream;

import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.doc.style.css.parser.CSSParser;
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
	 * The argument array must contain the style sheet path or URI, and any
	 * additional configuration:
	 * </p>
	 * <ul>
	 * <li><code>--charset</code>, followed by a valid character set name.</li>
	 * <li><code>--disable-shorthand</code>. If followed by a comma-separated list
	 * of shorthand names, it disables the advanced minification for those
	 * shorthands. If alone, all advanced shorthand minification is disabled (same
	 * as <code>--disable-shorthand all</code>).</li>
	 * </ul>
	 * <p>
	 * If the configuration options are invalid, exits with a status code of
	 * {@code 2}.
	 * </p>
	 * <p>
	 * If the given sheet contains error(s) and therefore cannot be reliably
	 * minified, the original source sheet is printed and the process exits with a
	 * status of {@code 1}.
	 * </p>
	 * 
	 * @param args the arguments, including at least the URI or path to a style
	 *             sheet.
	 * @throws URISyntaxException       if the URI has wrong syntax.
	 * @throws IllegalArgumentException if the URI is otherwise incorrect.
	 * @throws IOException              if an I/O error happened.
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		int status = main(args, System.out, System.err);
		System.exit(status);
	}

	/**
	 * Outputs a minification of the CSS style sheet located at the supplied URI.
	 * <p>
	 * The argument array must contain the style sheet path or URI, and any
	 * additional configuration:
	 * </p>
	 * <ul>
	 * <li><code>--charset</code>, followed by a valid character set name.</li>
	 * <li><code>--disable-shorthand</code>. If followed by a comma-separated list
	 * of shorthand names, it disables the advanced minification for those
	 * shorthands. If alone, all advanced shorthand minification is disabled (same
	 * as <code>--disable-shorthand all</code>).</li>
	 * </ul>
	 * <p>
	 * If the sheet contains error(s) and therefore cannot be reliably minified, the
	 * original source sheet is printed.
	 * </p>
	 * 
	 * @param args the arguments, including at least the URI or path to a style
	 *             sheet.
	 * @param out  the output stream.
	 * @param err  the error reporting stream.
	 * @return 0 if minification was successful, 2 if the arguments were incorrect,
	 *         1 if the file was printed without minification.
	 * @throws URISyntaxException       if the URI has wrong syntax.
	 * @throws IllegalArgumentException if the URI is otherwise incorrect.
	 * @throws IOException              if an I/O error happened.
	 */
	static int main(String[] args, PrintStream out, PrintStream err)
			throws URISyntaxException, IOException {
		ConfigImpl config;
		if (args == null || args.length == 0 || (config = readConfig(args)) == null) {
			printUsage(err);
			return 2;
		}

		Path filePath;
		URI uri = new URI(config.path);
		if (uri.isAbsolute()) {
			try {
				filePath = Paths.get(uri);
			} catch (FileSystemNotFoundException e) {
				File file = new File(config.path);
				filePath = file.toPath();
			}
		} else {
			File file = new File(config.path);
			filePath = file.toPath();
		}

		StringBuilder builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
		boolean ret = minifyCSS(filePath, config, builder, err);
		out.print(builder);
		return ret ? 0 : 1;
	}

	/**
	 * Print the usage.
	 * 
	 * @param err the error reporting stream.
	 */
	private static void printUsage(PrintStream err) {
		err.println("Usage: " + Minify.class.getName()
				+ " [--charset <charset>] [--disable-shorthand [<shorthand-list>]] <style-sheet-uri-or-path>");
	}

	private static ConfigImpl readConfig(String[] args) {
		ConfigImpl config = new ConfigImpl();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (!arg.startsWith("--")) {
				if (config.path == null) {
					config.path = arg;
				} else {
					return null;
				}
			} else {
				if ("--charset".equalsIgnoreCase(arg)) {
					i++;
					if (i < args.length) {
						arg = args[i];
						if (!arg.startsWith("--")) {
							try {
								config.encoding = Charset.forName(arg);
							} catch (Exception e) {
								return null;
							}
						} else {
							i--;
						}
					}
				} else if ("--disable-shorthand".equalsIgnoreCase(arg)) {
					i++;
					if (i < args.length) {
						arg = args[i];
						if (!arg.startsWith("--")) {
							String[] shorthands = arg.split(",");
							config.disabledShorthands = new HashSet<>(shorthands.length);
							for (String shorthand : shorthands) {
								if ("all".equalsIgnoreCase(shorthand)) {
									config.disabledShorthands = null;
									break;
								} else {
									config.disabledShorthands.add(shorthand);
								}
							}
						} else {
							config.disabledShorthands = null;
							i--;
						}
					}
				}
			}
		}
		if (config.path == null) {
			config = null;
		}
		return config;
	}

	public interface Config {

		/**
		 * Get the encoding of the style sheet.
		 *
		 * @return the encoding, or {@code null} if {@code UTF-8}.
		 */
		Charset getEncoding();

		boolean isDisabledShorthand(String name);

	}

	private static class ConfigImpl implements Config {

		private HashSet<String> disabledShorthands = null;

		private Charset encoding;

		private String path;

		@Override
		public boolean isDisabledShorthand(String name) {
			return disabledShorthands == null || disabledShorthands.contains(name);
		}

		@Override
		public Charset getEncoding() {
			return encoding;
		}

	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param css the serialized style sheet.
	 * @return the minified serialization, or the original one if an error was
	 *         detected.
	 */
	public static String minifyCSS(String css) {
		return minifyCSS(css, null, null);
	}

	/**
	 * Minifies a CSS style sheet and prints parsing errors to a stream.
	 * 
	 * @param css    the serialized style sheet.
	 * @param config the minification configuration, or {@code null} if defaults
	 *               should be used.
	 * @param err    the error reporting stream, or {@code null} if no stream.
	 * @return the minified serialization, or the original one if an error was
	 *         detected.
	 */
	public static String minifyCSS(String css, Config config, PrintStream err) {
		if (config == null) {
			config = new ConfigImpl();
		}
		BufferSimpleWriter wri = new BufferSimpleWriter(DEFAULT_BUFFER_SIZE);
		MinifySheetHandler handler = new MinifySheetHandler(wri, config);
		CSSParser parser = createCSSParser();
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

	private static CSSParser createCSSParser() {
		// Instantiate a parser with flags allowing IE hacks
		return new CSSOMParser(EnumSet.of(Parser.Flag.IEPRIO, Parser.Flag.IEPRIOCHAR,
				Parser.Flag.IEVALUES, Parser.Flag.STARHACK));
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to a {@code utf-8}-encoded style sheet.
	 * @return the minified serialization, or the original file if an error was
	 *         found.
	 */
	public static String minifyCSS(Path cssPath) throws IOException {
		StringBuilder builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
		minifyCSS(cssPath, null, builder, null);
		return builder.toString();
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to style sheet.
	 * @param config  the minification configuration, or {@code null} if defaults
	 *                should be used.
	 * @param buffer  the buffer to write the minified serialization, or the
	 *                original file if an error is found.
	 * @return {@code true} if the style sheet was successfully minified,
	 *         {@code false} if an error was found and the original file was
	 *         returned.
	 */
	public static boolean minifyCSS(Path cssPath, Config config, StringBuilder buffer)
			throws IOException {
		return minifyCSS(cssPath, config, buffer, null);
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to style sheet.
	 * @param config  the minification configuration, or {@code null} if defaults
	 *                should be used.
	 * @param buffer  the buffer to write the minified serialization, or the
	 *                original file if an error is found.
	 * @param err     the error reporting stream, or {@code null} if no stream.
	 * @return {@code true} if the style sheet was successfully minified,
	 *         {@code false} if an error was found and the original file was
	 *         returned.
	 */
	public static boolean minifyCSS(Path cssPath, Config config, StringBuilder buffer,
			PrintStream err) throws IOException {
		if (config == null) {
			config = new ConfigImpl();
		}
		Charset encoding = config.getEncoding();
		if (encoding == null) {
			encoding = StandardCharsets.UTF_8;
		}
		BufferSimpleWriter wri = new BufferSimpleWriter(buffer);
		MinifySheetHandler handler = new MinifySheetHandler(wri, config);
		CSSParser parser = createCSSParser();
		parser.setDocumentHandler(handler);

		try (BufferedReader cssReader = Files.newBufferedReader(cssPath, encoding)) {
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
