/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class MinifyTest {

	@Test
	void testMain() throws URISyntaxException, IOException {
		String[] args = new String[1];
		args[0] = MinifyTest.class.getResource("/io/sf/carte/doc/style/css/html.css")
				.toExternalForm();
		final int FINAL_LENGTH = 6196;
		ByteArrayOutputStream out = new ByteArrayOutputStream(FINAL_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		Minify.Main(args, ps, System.err);
		String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
		assertEquals(FINAL_LENGTH, result.length());
	}

	@Test
	void testMain_Print_Usage() throws URISyntaxException, IOException {
		String[] args = new String[0];
		ByteArrayOutputStream out = new ByteArrayOutputStream(64);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		Minify.Main(args, System.out, ps);
		assertEquals(64, out.size());
	}

	@Test
	void testMain_URI_Error() throws IOException {
		String[] args = new String[1];
		args[0] = ":/";
		assertThrows(URISyntaxException.class, () -> Minify.Main(args, System.out, System.err));
	}

	@Test
	void testMain_URI_IllegalArgument() throws IOException {
		String[] args = new String[1];
		args[0] = "/:";
		assertThrows(IllegalArgumentException.class,
				() -> Minify.Main(args, System.out, System.err));
	}

	@Test
	void testMinifyCSS_Empty() {
		assertEquals("", Minify.minifyCSS(""));
	}

	@Test
	void testMinifyCSS() {
		assertEquals("p{margin-top:.3em}", Minify.minifyCSS("p { margin-top: 0.3em}"));
	}

	@Test
	void testMinifyCSS_Calc() {
		assertEquals("p{margin-left:calc(2px + .3em)}",
				Minify.minifyCSS("p { margin-left: calc(2px + 0.3em)}"));
	}

}
