/*

 Copyright (c) 2020-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Parser.Flag;
import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.uparser.MinificationHandler;
import io.sf.carte.uparser.TokenProducer;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.agent.AgentUtil;

/**
 * Minify a CSS style sheet.
 * <p>
 * The regular minification methods first attempt a full minification by using
 * the CSS parser, but in case that an unrecognized syntax is found, a safe,
 * shallow minified serialization is used (with no CSS parsing involved).
 * </p>
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
	 * If the given sheet contains error(s), a shallower minification is performed
	 * and the process exits with a status of {@code 1}.
	 * </p>
	 * 
	 * @param args the arguments, including at least the URI or path to a style
	 *             sheet.
	 * @throws URISyntaxException       if the URI has wrong syntax.
	 * @throws IllegalArgumentException if the URI is otherwise incorrect.
	 * @throws IOException              if an I/O error happened.
	 */
	public static void main(String[] args)
			throws URISyntaxException, IllegalArgumentException, IOException {
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
	 * If the sheet contains error(s), a shallower minification is performed and the
	 * method returns {@code 1}.
	 * </p>
	 * 
	 * @param args the arguments, including at least the URI or path to a style
	 *             sheet.
	 * @param out  the output stream.
	 * @param err  the error reporting stream.
	 * @return 0 if minification was successful, 2 if the arguments were incorrect,
	 *         1 if the minification was only shallow due to unrecognized syntax.
	 * @throws URISyntaxException       if the URI has wrong syntax.
	 * @throws IllegalArgumentException if the URI is otherwise incorrect.
	 * @throws IOException              if an I/O error happened.
	 */
	static int main(String[] args, PrintStream out, PrintStream err)
			throws URISyntaxException, IllegalArgumentException, IOException {
		ConfigImpl config;
		if (args == null || args.length == 0 || (config = readConfig(args)) == null) {
			printUsage(err);
			return 2;
		}

		StringBuilder buffer = new StringBuilder(DEFAULT_BUFFER_SIZE);

		// First, check whether it is a path
		try {
			Path filePath = Paths.get(config.path);
			if (Files.exists(filePath)) {
				boolean ret = minifyCSS(filePath, config, buffer, err);
				out.print(buffer);
				return ret ? 0 : 1;
			}
		} catch (InvalidPathException e) {
		}

		// Probably a URL
		URI uri = new URI(config.path);
		URL url = uri.toURL();

		boolean ret = minifyCSS(url, config, buffer, err);
		out.print(buffer);

		return ret ? 0 : 1;
	}

	/**
	 * Print the usage.
	 * 
	 * @param err the error reporting stream.
	 */
	private static void printUsage(PrintStream err) {
		err.println("Usage: " + Minify.class.getName()
				+ " [--charset <charset>] [--disable-shorthand [<shorthand-list>]] [--validate] <style-sheet-uri-or-path>");
	}

	private static ConfigImpl readConfig(String[] args) {
		boolean enableShorthands = true;
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
					// Declare the (default) encoding
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
					// Disable shorthand optimizations
					enableShorthands = false;
					i++;
					if (i < args.length) {
						arg = args[i];
						if (!arg.startsWith("--")) {
							arg = arg.toLowerCase(Locale.ROOT);
							String[] shorthands = arg.split(",");
							config.disabledShorthands = new HashSet<>(shorthands.length);
							for (String shorthand : shorthands) {
								if ("all".equals(shorthand)) {
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
				} else if ("--validate".equalsIgnoreCase(arg)) {
					// Validate values
					config.validate = true;
				}
			}
		}
		if (config.path == null) {
			config = null;
		} else if (enableShorthands) {
			config.disabledShorthands = Collections.emptySet();
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

		/**
		 * Validate function values.
		 * 
		 * @return {@code true} if the parser should validate function values.
		 */
		boolean validate();

	}

	private static class ConfigImpl implements Config {

		private Set<String> disabledShorthands = null;

		private Charset encoding;

		private boolean validate;

		private String path;

		@Override
		public boolean isDisabledShorthand(String name) {
			return disabledShorthands == null || disabledShorthands.contains(name);
		}

		@Override
		public Charset getEncoding() {
			return encoding;
		}

		@Override
		public boolean validate() {
			return validate;
		}

	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param css the serialized style sheet.
	 * @return the minified serialization.
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
	 * @return the minified serialization.
	 */
	public static String minifyCSS(String css, Config config, PrintStream err) {
		if (config == null) {
			config = new ConfigImpl();
		}
		BufferSimpleWriter wri = new BufferSimpleWriter(DEFAULT_BUFFER_SIZE);
		MinifySheetHandler handler = new MinifySheetHandler(wri, config);
		CSSParser parser = createCSSParser(config);
		parser.setDocumentHandler(handler);

		StringReader cssReader = new StringReader(css);
		try {
			parser.parseStyleSheet(cssReader);
		} catch (IOException e) {
			// Cannot happen with StringReader
		} catch (Exception e) {
			// Error detected, return the source
			if (err != null) {
				e.printStackTrace(err);
			}
			StringBuilder buffer = new StringBuilder(css.length());
			try {
				cssReader.reset();
				shallowMinify(cssReader, buffer);
			} catch (IOException e1) {
				// Cannot happen with StringReader
			}
			return buffer.toString();
		}

		return wri.toString();
	}

	private static CSSParser createCSSParser(Config config) {
		// Instantiate a parser with flags allowing IE hacks
		EnumSet<Flag> flags = EnumSet.of(Parser.Flag.IEPRIO, Parser.Flag.IEPRIOCHAR,
				Parser.Flag.IEVALUES, Parser.Flag.STARHACK);
		if (!config.validate()) {
			flags.add(Parser.Flag.DISABLE_VALUE_VALIDATION);
		}
		return new CSSOMParser(flags);
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to a {@code utf-8}-encoded style sheet.
	 * @return the minified serialization.
	 */
	public static String minifyCSS(Path cssPath) throws IOException {
		StringBuilder builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
		minifyCSS(cssPath, null, builder, null);
		return builder.toString();
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to the style sheet.
	 * @param config  the minification configuration, or {@code null} if defaults
	 *                should be used.
	 * @param buffer  the buffer to write the minified serialization, or the
	 *                original file if an error is found.
	 * @return {@code true} if the style sheet was fully minified, {@code false} if
	 *         an error was found and the minification was shallow.
	 */
	public static boolean minifyCSS(Path cssPath, Config config, StringBuilder buffer)
			throws IOException {
		return minifyCSS(cssPath, config, buffer, null);
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param cssPath the path to the style sheet.
	 * @param config  the minification configuration, or {@code null} if defaults
	 *                should be used.
	 * @param buffer  the buffer to write the minified serialization.
	 * @param err     the error reporting stream, or {@code null} if no stream.
	 * @return {@code true} if the style sheet was fully minified, {@code false} if
	 *         a syntax error was found and the minification was shallow.
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
		CSSParser parser = createCSSParser(config);
		parser.setDocumentHandler(handler);

		try (BufferedReader cssReader = Files.newBufferedReader(cssPath, encoding)) {
			parser.parseStyleSheet(cssReader);
			return true;
		} catch (Exception e) {
			if (err != null) {
				e.printStackTrace(err);
			}
		}

		// Error detected, return the source without comments.
		buffer.setLength(0);

		try (Reader cssReader = Files.newBufferedReader(cssPath, encoding)) {
			shallowMinify(cssReader, buffer);
		}

		return false;
	}

	/**
	 * Minifies a CSS style sheet.
	 * 
	 * @param url    the url to the style sheet.
	 * @param config the minification configuration, or {@code null} if defaults
	 *               should be used.
	 * @param buffer the buffer to write the minified serialization.
	 * @param err    the error reporting stream, or {@code null} if no stream.
	 * @return {@code true} if the style sheet was fully minified, {@code false} if
	 *         a syntax error was found and the minification was shallow.
	 */
	public static boolean minifyCSS(URL url, Config config, StringBuilder buffer, PrintStream err)
			throws IOException {
		if (config == null) {
			config = new ConfigImpl();
		}
		Charset encoding = config.getEncoding();
		if (encoding == null) {
			encoding = StandardCharsets.UTF_8;
		}
		BufferSimpleWriter wri = new BufferSimpleWriter(buffer);
		MinifySheetHandler handler = new MinifySheetHandler(wri, config);
		CSSParser parser = createCSSParser(config);
		parser.setDocumentHandler(handler);

		URLConnection ucon = url.openConnection();
		try (Reader cssReader = inputStreamToReader(ucon, encoding)) {
			parser.parseStyleSheet(cssReader);
			return true;
		} catch (Exception e) {
			if (err != null) {
				e.printStackTrace(err);
			}
		}

		// Error detected, return the source without comments.
		buffer.setLength(0);

		ucon = url.openConnection();
		try (Reader cssReader = inputStreamToReader(ucon, encoding)) {
			shallowMinify(cssReader, buffer);
		}

		return false;
	}

	private static Reader inputStreamToReader(URLConnection ucon, Charset encoding)
			throws IOException {
		InputStream is = ucon.getInputStream();
		String contentEncoding = ucon.getContentEncoding();
		String conType = ucon.getContentType();
		return AgentUtil.inputStreamToReader(is, conType, contentEncoding, encoding);
	}

	/**
	 * Perform a safe, shallow minification that does not attempt to minify values
	 * nor media features.
	 * 
	 * @param cssReader the {@code Reader} with the style sheet.
	 * @param buffer    the output buffer.
	 * @throws IOException if an I/O error happened reading the style sheet.
	 */
	public static void shallowMinify(Reader cssReader, StringBuilder buffer) throws IOException {
		String opening = "/*";
		String closing = "*/";
		MinificationHandler h = new ShallowMinificationHandler(buffer);
		TokenProducer tp = new TokenProducer(h);
		tp.setHandleAllSeparators(false);
		tp.parse(cssReader, opening, closing);
	}

}
