/*

 Copyright (c) 2020-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		final int FINAL_LENGTH = 6184;
		ByteArrayOutputStream out = new ByteArrayOutputStream(FINAL_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		Minify.main(args, ps, System.err);
		assertEquals(FINAL_LENGTH, out.size());
	}

	@Test
	void testMain_Invalid() throws URISyntaxException, IOException {
		// Parsing HTML as CSS is going to fail
		String[] args = new String[1];
		args[0] = MinifyTest.class.getResource("/io/sf/carte/doc/agent/meta-default-style.html")
				.toExternalForm();
		final int FINAL_LENGTH = 1118;
		ByteArrayOutputStream out = new ByteArrayOutputStream(FINAL_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		ByteArrayOutputStream err = new ByteArrayOutputStream(64);
		PrintStream pserr = new PrintStream(err, false, "utf-8");
		Minify.main(args, ps, pserr);
		assertEquals(FINAL_LENGTH, out.size());
		assertTrue(err.size() >= 5000);
	}

	@Test
	void testMain_Print_Usage() throws URISyntaxException, IOException {
		String[] args = {};
		ByteArrayOutputStream out = new ByteArrayOutputStream(64);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		Minify.main(args, System.out, ps);
		String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
		result = result.replaceAll("\r", "");
		assertEquals(63, result.length());
	}

	@Test
	void testMain_URI_Error() throws IOException {
		String[] args = new String[1];
		args[0] = ":/";
		assertThrows(URISyntaxException.class, () -> Minify.main(args, System.out, System.err));
	}

	@Test
	void testMain_URI_IllegalArgument() throws IOException {
		String[] args = new String[1];
		args[0] = "/:";
		assertThrows(IllegalArgumentException.class,
				() -> Minify.main(args, System.out, System.err));
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

	@Test
	void testMinifyCSS_Invalid() {
		assertEquals("p { margin-left: calc(2px+)}",
				Minify.minifyCSS("p { margin-left: calc(2px+)}"));
	}

	@Test
	void testMinifyCSS_CounterStyle() {
		assertEquals("@counter-style foo{system:cyclic;symbols:\"\\1F44D\";suffix:\" \"}",
				Minify.minifyCSS("@counter-style foo {\n"
						+ "  system: cyclic;   symbols: \"\\1F44D\"; " + "  suffix: \" \"; }"));
	}

	@Test
	void testMinifyCSS_FontFace() {
		assertEquals(
				"@font-face{font-family:\"SomeFont\";src:local(\"SomeFont\"),url(\"somefont-COLRv1.otf\") format(\"opentype\") tech(color-COLRv1),url(\"somefont-outline.otf\") format(\"opentype\"),url(\"somefont-outline.woff\") format(\"woff\")}",
				Minify.minifyCSS("@font-face {\n" + "  font-family: \"SomeFont\"; "
						+ "  src:  local(\"SomeFont\"), "
						+ "    url(\"somefont-COLRv1.otf\") format(\"opentype\") tech(color-COLRv1), "
						+ "    url(\"somefont-outline.otf\") format(\"opentype\"), "
						+ "    url(\"somefont-outline.woff\") format(\"woff\");}"));
	}

	@Test
	void testMinifyCSS_FontFeatureValues() {
		assertEquals("@font-feature-values Font One{@styleset{nice-style:12}}", Minify.minifyCSS(
				"@font-feature-values Font One {\n" + "  @styleset {     nice-style: 12;  } }"));
	}

	@Test
	void testMinifyCSS_Import() {
		assertEquals(
				"@import \"alter2.css\" layer(alter) supports(selector(col||td) and (color:#000)) (width<1200px);",
				Minify.minifyCSS(
						"@import url('alter2.css') layer(alter) supports(selector(col||td) and (color: #000)) (width < 1200px);"));
	}

	@Test
	void testMinifyCSS_Keyframes() {
		assertEquals(
				"@keyframes foo{0,50%{margin-left:100%;width:300%}to{margin-left:0%;width:100%}}",
				Minify.minifyCSS(
						"@keyframes foo {  /* pre-0,50% */0,50% { margin-left: 100%;  width: 300%;}/* post-0,50% */\n to {margin-left: 0%;    width: 100%; }}"));
	}

	@Test
	void testMinifyCSS_Media() {
		assertEquals(
				"@media only screen and (min-width:.002em){nav.foo{display:none}footer .footer .foo{padding-left:0;padding-right:var(--pad,/*empty*/)}h4{font-size:20px}}",
				Minify.minifyCSS(
						"@media only screen and  (min-width: 0.002em){ nav.foo { display:none;}footer .footer .foo { padding-left:0;padding-right: var(--pad , /*empty*/); } h4 {font-size:20px; }}"));
	}

	@Test
	void testMinifyCSS_Namespace() {
		assertEquals("@namespace svg \"http://www.w3.org/2000/svg\";",
				Minify.minifyCSS("@namespace svg url(\"http://www.w3.org/2000/svg\");"));
	}

	@Test
	void testMinifyCSS_Page() {
		assertEquals(
				"@page{size:8.5in 9in;margin-top:4in}@page foo:first,bar:right{margin-top:20%;@top-left{margin-top:.7em;margin-left:1ex}@bottom-center{content:'foo'}}",
				Minify.minifyCSS(
						"@page {  size: 8.5in 9in;   margin-top: 4in; } @page foo:first,bar:right {margin-top: 20%;@top-left {margin-top: 0.7em; margin-left:1ex}@bottom-center {content: 'foo"));
	}

	@Test
	void testMinifyCSS_Property() {
		assertEquals("@property --my-length{syntax:'<length>';inherits:true;initial-value:.9cm}",
				Minify.minifyCSS(
						"@property --my-length {syntax: '<length>'; inherits: true; initial-value: /*centimeters*/0.9cm;}"));
	}

	@Test
	void testMinifyCSS_Supports() {
		assertEquals(
				"@supports (display:flex) and (flex:2 2 .5%){td{display:table-cell}li{display:list-item}}",
				Minify.minifyCSS(
						"@supports /* skip 1 */ (display: flex) and (flex: 2 2 0.5%) /* skip 2 */ {/* pre-td */td {display: table-cell; }/* post-td */ li {display: list-item; }}"));
	}

	@Test
	void testMinifyCSS_Prefixed() {
		assertEquals(
				"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }",
				Minify.minifyCSS(
						"@-webkit-keyframes spin { from { -webkit-transform: rotate(0); transform: rotate(0); } to { -webkit-transform: rotate(360deg); transform: rotate(360deg); } }"));
	}

}
