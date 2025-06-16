/*

 Copyright (c) 2020-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.util.Minify.Config;

class MinifyTest {

	@Test
	void testUA_Sheet() throws URISyntaxException, IOException {
		final String HTML_UA_STYLE_SHEET = "/io/sf/carte/doc/style/css/html.css";
		String[] args = new String[1];
		args[0] = MinifyTest.class.getResource(HTML_UA_STYLE_SHEET).toExternalForm();
		final int MINIFIED_LENGTH = 6172;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(0, Minify.main(args, ps, System.err));

		// Test for the expected length
		if (MINIFIED_LENGTH != out.size()) {
			// Check equivalence at OM level, to figure out the issue
			failureCheck(HTML_UA_STYLE_SHEET, out.toByteArray());
		}
	}

	@Test
	void testCommon_Sheet() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/agent/common.css";
		String[] args = new String[3];
		args[0] = MinifyTest.class.getResource(path).toExternalForm();
		args[1] = "--disable-shorthand";
		args[2] = "all";
		final int MINIFIED_LENGTH = 161;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(0, Minify.main(args, ps, System.err));

		// Test for the expected length
		if (MINIFIED_LENGTH != out.size()) {
			// Check equivalence at OM level, to figure out the issue
			failureCheck(path, out.toByteArray());
		}
	}

	@Test
	void testAlter1_Sheet() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/agent/alter1.css";
		String[] args = new String[3];
		args[0] = MinifyTest.class.getResource(path).toExternalForm();
		args[1] = "--disable-shorthand";
		args[2] = "cue";
		final int MINIFIED_LENGTH = 74;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(0, Minify.main(args, ps, System.err));

		// Test for the expected length
		if (MINIFIED_LENGTH != out.size()) {
			// Check equivalence at OM level, to figure out the issue
			failureCheck(path, out.toByteArray());
		}
	}

	@Test
	void testMain() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/style/css/util/minify.css";
		String[] args = new String[1];
		args[0] = MinifyTest.class.getResource(path).toExternalForm();
		final int MINIFIED_LENGTH = 206;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(0, Minify.main(args, ps, System.err));

		String expected = "body{font-family:Verdana,Arial,Helvetica;margin:.2em;color:rgb(from var(--color) r g 90%)}img{border-style:none}.layout{margin-top:0;padding:2px;border-width:1px;border-style:solid;background:url(imag/top_b.png)}";
		// Test for the expected string
		assertEquals(expected, out.toString("utf-8"));
	}

	@Test
	void testDisableShorthands() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/style/css/util/minify.css";
		String[] args = new String[3];
		args[0] = MinifyTest.class.getResource(path).toExternalForm();
		args[1] = "--disable-shorthand";
		args[2] = "background,margin";
		final int MINIFIED_LENGTH = 226;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(0, Minify.main(args, ps, System.err));

		String expected = "body{font-family:Verdana,Arial,Helvetica;margin:.2em .2em .2em .2em;color:rgb(from var(--color) r g 90%)}img{border-style:none}.layout{margin-top:0;padding:2px;border-width:1px;border-style:solid;background:url(imag/top_b.png) top left}";
		// Test for the expected string
		assertEquals(expected, out.toString("utf-8"));
	}

	@Test
	void testDisableAllShorthands() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/style/css/util/minify.css";
		String[] args = new String[3];
		args[0] = MinifyTest.class.getResource(path).toExternalForm();
		args[1] = "--disable-shorthand";
		args[2] = "all";
		final int MINIFIED_LENGTH = 257;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(0, Minify.main(args, ps, System.err));

		// Test for the expected length
		if (MINIFIED_LENGTH != out.size()) {
			// Check equivalence at OM level, to figure out the issue
			failureCheck(path, out.toByteArray());
		}
	}

	@Test
	void testBadMedia_Sheet() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/style/css/parser/badmedia.css";
		String[] args = new String[1];
		args[0] = MinifyTest.class.getResource(path).toExternalForm();
		final int MINIFIED_LENGTH = 865;
		ByteArrayOutputStream out = new ByteArrayOutputStream(MINIFIED_LENGTH);
		ByteArrayOutputStream err = new ByteArrayOutputStream(128);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		PrintStream psErr = new PrintStream(err, false, "utf-8");
		assertEquals(1, Minify.main(args, ps, psErr));
		assertTrue(err.size() > 6000);

		// Test for the expected length
		if (MINIFIED_LENGTH != out.size()) {
			// Check equivalence at OM level, to figure out the issue
			failureCheck(path, out.toByteArray());
		}
	}

	@Test
	void testNormalize_URL() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/style/css/contrib/normalize.css";
		URL url = MinifyTest.class.getResource(path);
		final int MINIFIED_LENGTH = 1845;
		StringBuilder buffer = new StringBuilder(MINIFIED_LENGTH);
		assertTrue(Minify.minifyCSS(url, null, buffer, System.err));

		// Test for the expected length
		int len = buffer.length();
		if (MINIFIED_LENGTH != len) {
			// Check equivalence at OM level, to figure out the issue
			byte[] bytes = buffer.toString().getBytes(StandardCharsets.UTF_8);
			failureCheck(path, bytes);
		}
	}

	@Test
	void testBadMedia_URL() throws URISyntaxException, IOException {
		String path = "/io/sf/carte/doc/style/css/parser/badmedia.css";
		URL url = MinifyTest.class.getResource(path);
		final int MINIFIED_LENGTH = 865;
		StringBuilder buffer = new StringBuilder(MINIFIED_LENGTH);
		ByteArrayOutputStream err = new ByteArrayOutputStream(128);
		PrintStream psErr = new PrintStream(err, false, "utf-8");
		assertFalse(Minify.minifyCSS(url, null, buffer, psErr));
		assertTrue(err.size() > 6000);

		// Test for the expected length
		int len = buffer.length();
		if (MINIFIED_LENGTH != len) {
			// Check equivalence at OM level, to figure out the issue
			byte[] bytes = buffer.toString().getBytes(StandardCharsets.UTF_8);
			failureCheck(path, bytes);
		}
	}

	private void failureCheck(String resourcePath, byte[] cand) throws DOMException, IOException {
		// Instantiate any style sheet factory, with parser flags allowing IE hacks
		AbstractCSSStyleSheetFactory cssFactory = new CSSDOMImplementation(
				EnumSet.allOf(Parser.Flag.class));

		AbstractCSSStyleSheet sheet = cssFactory.createStyleSheet(null, null);
		try (Reader re = new InputStreamReader(MinifyTest.class.getResourceAsStream(resourcePath),
				StandardCharsets.UTF_8)) {
			sheet.parseStyleSheet(re, CSSStyleSheet.COMMENTS_IGNORE);
		}

		ByteArrayInputStream in = new ByteArrayInputStream(cand);
		AbstractCSSStyleSheet minisheet = cssFactory.createStyleSheet(null, null);
		Reader re = new InputStreamReader(in, StandardCharsets.UTF_8);
		minisheet.parseStyleSheet(re, CSSStyleSheet.COMMENTS_IGNORE);

		// Check that original and minified sheets are identical
		if (!sheet.equals(minisheet)) {
			assertEquals(sheet.toString(), minisheet.toString());
		}

		fail("Please fix the value of MINIFIED_LENGTH: " + cand.length);
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
		assertEquals(1, Minify.main(args, ps, pserr));
		assertEquals(FINAL_LENGTH, out.size());
		assertTrue(err.size() >= 5000);
	}

	@Test
	void testMain_Print_Usage_No_args() throws URISyntaxException, IOException {
		String[] args = {};
		ByteArrayOutputStream out = new ByteArrayOutputStream(64);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(2, Minify.main(args, System.out, ps));
		String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
		result = result.replaceAll("\r", "");
		assertEquals(147, result.length());
	}

	@Test
	void testMain_Print_Usage_No_Path() throws URISyntaxException, IOException {
		String[] args = { "--charset", "utf-8" };
		ByteArrayOutputStream out = new ByteArrayOutputStream(64);
		PrintStream ps = new PrintStream(out, false, "utf-8");
		assertEquals(2, Minify.main(args, System.out, ps));
		String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
		result = result.replaceAll("\r", "");
		assertEquals(147, result.length());
	}

	@Test
	void testMain_URI_Error() throws IOException {
		String[] args = new String[1];
		args[0] = ":/";
		assertThrows(URISyntaxException.class, () -> Minify.main(args, System.out, System.err));
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
		assertEquals("p{margin-left:calc(2px +)}",
				Minify.minifyCSS("p { margin-left: calc(2px+)}"));
	}

	@Test
	void testMinifyCSS_Invalid_Validate() {
		TestConfig config = new TestConfig();
		config.validate = true;
		assertEquals("p { margin-left: calc(2px+)}",
				Minify.minifyCSS("p { margin-left: calc(2px+)}", config, null));
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
				"@font-face{font-family:\"SomeFont\";src:local(\"SomeFont\"),url(somefont-COLRv1.otf) format(\"opentype\") tech(color-COLRv1),url(somefont-outline.otf) format(\"opentype\"),url(somefont-outline.woff) format(\"woff\")}",
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
				"@media only screen and (min-width:.002em){nav.foo{display:none}footer .footer .foo{color:#ff0;padding-right:var(--pad,)}h4{font-size:20px}}",
				Minify.minifyCSS(
						"@media only screen and  (min-width: 0.002em){ nav.foo { display:none;}footer .footer .foo { color: rgba(255,255,0,255);padding-right: var(--pad , /*empty*/); } h4 {font-size:20px; }}"));
	}

	@Test
	void testMinifyCSS_Media_Validate() {
		TestConfig config = new TestConfig();
		config.validate = true;
		assertEquals(
				"@media only screen and (min-width:.002em){nav.foo{display:none}footer .footer .foo{color:#ff0;padding-right:var(--pad,/*empty*/)}h4{font-size:20px}}",
				Minify.minifyCSS(
						"@media only screen and  (min-width: 0.002em){ nav.foo { display:none;}footer .footer .foo { color: rgba(255,255,0,255);padding-right: var(--pad , /*empty*/); } h4 {font-size:20px; }}",
						config, null));
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

	@Test
	void testMinifyCSS_Style() {
		assertEquals("p,.cls{background:url('foo?a=b(c)')}div{background:url(imag/img.png) .9em}",
				Minify.minifyCSS(
						"p,*.cls {background:url('foo?a=b(c)');} div{background:url('imag/img.png') 0.9em;}"));
	}

	@Test
	void testMinifyCSS_Style2() {
		assertEquals(
				"body{font-family:Verdana,\"Open Sans\",sans-serif;margin:0 0 .2em 0}img{border-style:none}.layout{margin-top:0;margin-right:auto;padding:0;border-width:0;border-style:none;background:url(imag/top_b.png) repeat-x}#mylinkhome{display:block;background:url(imag/minilogo.png) no-repeat 3px 2px;height:39px}#linkhome{display:block;background:url(imag/minilogo.png) no-repeat 3px 2px;height:39px}#linkhome span,#mylinkhome span{display:none}.container{padding:0;margin-top:0}body .container{margin-left:170px}.menu{width:133px;float:left;margin-left:2px;background-color:#DDEAE4}body .menu{padding-left:6px;margin-left:-170px}.menulist{list-style-type:none;padding:0}.menulist,.menulist li{margin:0}.menulist li{width:inherit;height:24px;font-size:12pt}.menulist a,.menulist div{height:24px;padding:1px 0 0 .2ex}li.menulvl2 a,li.menulvl2 div{height:20px;padding:0 0 0 .7ex}.menulist a{display:block;background:url(imag/mnubg_a.png) no-repeat}li.menulvl2 a{display:block;background:url(imag/mnubg2_a.png) no-repeat}li.menulvl2 a:hover{text-align:right;background:url(imag/mnubg2_b.png) no-repeat}.menulist a:hover{text-align:right;padding-right:16px;background:url(imag/mnubg_b.png) no-repeat}.menulist div{text-align:right;padding-right:16px;background:url(imag/mnubg_b.png) no-repeat}li.menulvl2 div{text-align:right;padding-right:20px;background:url(imag/mnubg_b.png) no-repeat}.beforemain{display:none}.main{float:left;width:90%;font-family:\"Open Sans\",sans-serif}.textheader span{float:right;padding:.2em 1.2ex 1.2em 1.2ex;font-weight:lighter;color:#727272;text-align:left;letter-spacing:.06em;background:url(imag/hdrbg.png) no-repeat 1px .1em}.cos{text-align:justify;border-top:2px dotted #b29e7c;margin:1.9em 1ex 1em 100px}.cos p{line-height:1.4em}html>body .cos{margin-left:.2ex}.cos li{line-height:1.5em;margin:.5em 1.5ex .6em 0}code{font-size:1.3em}pre{font-size:1.4em}pre.code{background-color:#e7e4de}div.tema{margin:2em 0 3.3em 0}div.subtema{margin:1.8em 0}div.seccion{margin:2em 0 3em 0}.seccion ul{margin:1.4em 0 1.8em 0}.seccion li{list-style-type:square}img.diagram{margin:1em 5%}.urlist li{list-style-type:square}.normaltbl td,.normaltbl th{padding:.4em 1em .5em 1.1em}.normaltbl th{white-space:nowrap;text-align:center}.normaltbl td.number{text-align:right}.normaltbl th:first-child{text-align:left}.note,.legalremind{font-size:smaller;font-style:italic}.clausulas li{margin:1em .5em 2em .5em;line-height:1.3em}.footnote{margin-top:4em;margin-bottom:2em;text-align:center;font-size:.8em;font-style:italic;padding:.3em 0 .5em 0;border-top:2px solid #171719}.smallnote{font-style:italic;font-size:smaller}.smpreface{font-style:italic;font-size:smaller;padding:.3em 3em .4em 1.5em;text-align:justify}.explist li{margin:.2em 0 .3em 0;line-height:1.3em}.illustration{margin:.6em 2ex 1em 0;float:left}.imgcredit{font-style:italic;font-size:smaller;display:block}@media only screen and (max-width:640px){body .container{margin-left:0}.menu{width:16ex;float:left}body .menu{padding-left:.6ex;margin:1px 1px}.menulist li{height:1.4em;font-size:1.1em}.menulist a,.menulist div{height:1.4em;padding:0 0 0 .2ex}.menulist a{background:linear-gradient(to right,#95a08c 0%,rgb(255 255 255/0) 100%)}.menulist a:hover{padding-right:1ex;background:linear-gradient(to right,#32491f 0%,rgb(244 246 241/0) 100%)}.menulist div{padding-right:1ex;background:linear-gradient(to right,#32491f 0%,rgb(244 246 241/0) 100%)}.main{width:99%}.cos{margin-right:.4ex}html>body .cos{margin-left:.5ex}}body{background-color:#F4F4F3;color:#2d261a}#hdr01{display:none}a:link{text-decoration:none;color:#045dca;cursor:pointer}a:visited{text-decoration:none;color:#1f42ab}a:active{text-decoration:underline;color:#C10300}a:hover{text-decoration:underline;color:#8E3A2D}.menulist div{color:#FDFFD3}.menulist a{color:#FFFEF0}.menulist a:hover{color:#FDFFD3}.footnote{border-top:none;padding:.3em 0 .5em 0;background:url(imag/footerbg.png) repeat-x}.footnote a:link,.footnote a:visited{text-decoration:underline}.obsrv{font-size:smaller}table .yesno{text-align:center}.normaltbl tr:nth-child(even),tr.evenrow{background-color:#f6f5e5}.normaltbl tr:nth-child(odd),tr.oddrow{background-color:#eeebd8}table.normaltbl>thead>tr:first-child,tr.hdrow{background-color:#e9e9c5}",
				Minify.minifyCSS(
						"body{font-family:Verdana,\"Open Sans\",sans-serif;margin:0 0 .2em 0}img{border-style:none}.layout{margin-top:0;margin-right:auto;padding:0;border-width:0;border-style:none;background:url(imag/top_b.png) repeat-x}#mylinkhome{display:block;background:url('imag/minilogo.png') no-repeat 3px 2px;height:39px}#linkhome{display:block;background:url('imag/minilogo.png') no-repeat 3px 2px;height:39px}#linkhome span,#mylinkhome span{display:none}.container{padding:0;margin-top:0}body .container{margin-left:170px}.menu{width:133px;float:left;margin-left:2px;background-color:#DDEAE4}body .menu{padding-left:6px;margin-left:-170px}.menulist{list-style-type:none;padding:0}.menulist,.menulist li{margin:0}.menulist li{width:inherit;height:24px;font-size:12pt}.menulist a,.menulist div{height:24px;padding:1px 0 0 .2ex}li.menulvl2 a,li.menulvl2 div{height:20px;padding:0 0 0 .7ex}.menulist a{display:block;background:url(imag/mnubg_a.png) no-repeat}li.menulvl2 a{display:block;background:url(imag/mnubg2_a.png) no-repeat}li.menulvl2 a:hover{text-align:right;background:url(imag/mnubg2_b.png) no-repeat}.menulist a:hover{text-align:right;padding-right:16px;background:url(imag/mnubg_b.png) no-repeat}.menulist div{text-align:right;padding-right:16px;background:url(imag/mnubg_b.png) no-repeat}li.menulvl2 div{text-align:right;padding-right:20px;background:url(imag/mnubg_b.png) no-repeat}.beforemain{display:none}.main{float:left;width:90%;font-family:\"Open Sans\",sans-serif}.textheader{}.textheader span{float:right;padding:.2em 1.2ex 1.2em 1.2ex;font-weight:lighter;color:#727272;text-align:left;letter-spacing:.06em;background:url(imag/hdrbg.png) no-repeat 1.0px .1em}.cos{text-align:justify;border-top:2px dotted #b29e7c;margin:1.9em 1ex 1em 100px}.cos p{line-height:1.4em}html>body .cos{margin-left:.2ex}.cos li{line-height:1.5em;margin:.5em 1.5ex .6em 0}code{font-size:1.3em}pre{font-size:1.4em}pre.code{background-color:#e7e4de}div.tema{margin:2em 0 3.3em 0}div.subtema{margin:1.8em 0}div.seccion{margin:2em 0 3em 0}.seccion ul{margin:1.4em 0 1.8em 0}.seccion li{list-style-type:square}img.diagram{margin:1em 5%}.urlist li{list-style-type:square}.normaltbl td,.normaltbl th{padding:.4em 1em .5em 1.1em}.normaltbl th{white-space:nowrap;text-align:center}.normaltbl td.number{text-align:right}.normaltbl th:first-child{text-align:left}.note,.legalremind{font-size:smaller;font-style:italic}.clausulas li{margin:1em .5em 2em .5em;line-height:1.3em}.footnote{margin-top:4em;margin-bottom:2em;text-align:center;font-size:.8em;font-style:italic;padding:.3em 0 .5em 0;border-top:2px solid #171719}.smallnote{font-style:italic;font-size:smaller}.smpreface{font-style:italic;font-size:smaller;padding:.3em 3em .4em 1.5em;text-align:justify}.explist li{margin:.2em 0 .3em 0;line-height:1.3em}.illustration{margin:.6em 2ex 1em 0;float:left}.imgcredit{font-style:italic;font-size:smaller;display:block}@media only screen and (max-width:640px){body .container{margin-left:0}.menu{width:16ex;float:left}body .menu{padding-left:.6ex;margin:1px 1px}.menulist li{height:1.4em;font-size:1.1em}.menulist a,.menulist div{height:1.4em;padding:0 0 0 .2ex}.menulist a{background:linear-gradient(to right,#95a08c 0%,rgb(255 255 255/0) 100%)}.menulist a:hover{padding-right:1ex;background:linear-gradient(to right,#32491f 0%,rgb(244 246 241/0) 100%)}.menulist div{padding-right:1ex;background:linear-gradient(to right,#32491f 0%,rgb(244 246 241/0) 100%)}.main{width:99%}.cos{margin-right:.4ex}html>body .cos{margin-left:.5ex}}body{background-color:#F4F4F3;color:#2d261a}#hdr01{display:none}a:link{text-decoration:none;color:#045dca;cursor:pointer}a:visited{text-decoration:none;color:#1f42ab}a:active{text-decoration:underline;color:#C10300}a:hover{text-decoration:underline;color:#8E3A2D}.menulist div{color:#FDFFD3}.menulist a{color:#FFFEF0}.menulist a:hover{color:#FDFFD3}.footnote{border-top:none;padding:.3em 0 .5em 0;background:url(imag/footerbg.png) repeat-x}.footnote a:link,.footnote a:visited{text-decoration:underline}.obsrv{font-size:smaller}table .yesno{text-align:center}.normaltbl tr:nth-child(even),tr.evenrow{background-color:#f6f5e5}.normaltbl tr:nth-child(odd),tr.oddrow{background-color:#eeebd8}table.normaltbl>thead>tr:first-child,tr.hdrow{background-color:#e9e9c5}"));
	}

	@Test
	void testMinifyCSS_Style_Shorthands() {
		TestConfig config = new TestConfig();
		assertEquals("p,.cls{border-radius:0}div{margin:2px}", Minify.minifyCSS(
				"p,*.cls {border-radius: initial;} div{margin: 2px 2px 2px 2px;}", config, null));

		config.disabledShorthands.add("margin");
		assertEquals("p,.cls{border-radius:0}div{margin:2px 2px 2px 2px}", Minify.minifyCSS(
				"p,*.cls {border-radius: initial;} div{margin: 2px 2px 2px 2px;}", config, null));

		config.disableAllShorthands();
		assertEquals("p,.cls{border-radius:initial}div{margin:2px 2px 2px 2px}", Minify.minifyCSS(
				"p,*.cls {border-radius: initial;} div{margin: 2px 2px 2px 2px;}", config, null));
	}

	@Test
	void testMinifyCSS_Style_Shorthands_Background() {
		TestConfig config = new TestConfig();
		assertEquals("div{background:url(bkg.png) round space local}", Minify.minifyCSS(
				"div{background: url('bkg.png') left top round space padding-box border-box local; }",
				config, null));
	}

	@Test
	void testMinifyCSS_Style_Shorthands_Border() {
		TestConfig config = new TestConfig();
		assertEquals("p,.cls{border:none}div{border-width:2px}", Minify.minifyCSS(
				"p,*.cls {border: initial;} div{border-width: 2px 2px 2px 2px;}", config, null));
	}

	private static class TestConfig implements Config {

		private HashSet<String> disabledShorthands = new HashSet<>();

		private boolean validate;

		TestConfig() {
		}

		@Override
		public Charset getEncoding() {
			return null;
		}

		void disableAllShorthands() {
			disabledShorthands = null;
		}

		@Override
		public boolean isDisabledShorthand(String name) {
			return disabledShorthands == null || disabledShorthands.contains(name);
		}

		@Override
		public boolean validate() {
			return validate;
		}

	}

}
