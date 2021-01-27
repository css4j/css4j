/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AnimationShorthandBuilderTest {

	private BaseCSSStyleDeclaration emptyStyleDecl;

	@Before
	public void setUp() {
		StyleRule styleRule = new StyleRule();
		emptyStyleDecl = (BaseCSSStyleDeclaration) styleRule.getStyle();
	}

	@Test
	public void testBuilderNoShorthand() {
		assertShorthandText("animation-name:foo;", "animation-name: foo;");
		// Nobody uses IE hacks for animations, but the detection code is shared
		assertShorthandText(
				"animation-delay:1s;animation-duration:3200ms;animation-fill-mode:none;animation-name:foo\\9 ;",
				"animation-duration: 3200ms;animation-delay:1s;animation-fill-mode:none;animation-name:foo\\9;");
	}

	@Test
	public void testBuilder() {
		assertShorthandText("animation:3500ms 5s none backwards;", "animation: 3500ms 5s none backwards");
		assertShorthandText("animation:3500ms 5s reverse 'my anim';", "animation: 3500ms 5s reverse 'my anim';");
		assertShorthandText("animation:none;", "animation: none;");
		assertShorthandText("animation:0s frames(3) 5s reverse none;",
				"animation-duration: 0s; animation-timing-function: frames(3); animation-delay: 5s; animation-iteration-count: 1; animation-direction: reverse; animation-fill-mode: none; animation-play-state: running; animation-name: none;");
		assertShorthandText("animation:3500ms frames(5) 5s reverse 'my anim';",
				"animation: 3500ms 5s frames(5) reverse 'my anim'");
		assertShorthandText("animation:ease-in ease-out;", "animation: ease-in ease-out;");
		assertShorthandText("animation:linear ease;", "animation: linear ease;");
		assertShorthandText("animation:3s none backwards;", "animation: 3s none backwards;");
		assertShorthandText("animation:3s ease-in 1s 2 reverse both paused slidein;",
				"animation: 3s ease-in 1s 2 reverse both paused slidein;");
	}

	@Test
	public void testBuilderList() {
		assertShorthandText("animation:3500ms frames(3) 5s reverse '1st anim',0s 3s '2nd anim',1s '3rd anim';",
				"animation: 3500ms frames(3) reverse 5s '1st anim', 0s 3s '2nd anim', 1s '3rd anim'");
		assertShorthandText("animation:3500ms 5s reverse '1st anim',0s steps(2,start) 3s alternate '2nd anim';",
				"animation: 3500ms 5s reverse '1st anim', 0s 3s steps(2, start) alternate '2nd anim'");
		assertShorthandText("animation:0s frames(3) 5s reverse foo,1s bar;",
				"animation-duration: 0s,1s; animation-timing-function: frames(3),ease; animation-delay: 5s,0s; animation-iteration-count: 1,1; animation-direction: reverse,normal; animation-fill-mode: none,none; animation-play-state: running,running; animation-name: foo,bar;");
	}

	@Test
	public void testBuilderListBad() {
		assertShorthandText(
				"animation-delay:5s,inherit;animation-direction:reverse;animation-duration:0s;animation-fill-mode:none;animation-iteration-count:1;animation-name:none;animation-play-state:running;animation-timing-function:frames(3);",
				"animation-duration: 0s; animation-timing-function: frames(3); animation-delay: 5s,inherit; animation-iteration-count: 1; animation-direction: reverse; animation-fill-mode: none; animation-play-state: running; animation-name: none;");
	}

	@Test
	public void testBuilderListImportant() {
		assertShorthandText(
				"animation:3500ms 5s reverse '1st anim',0s steps(2,start) 3s alternate \"2nd anim\"!important;",
				"animation: 3500ms 5s reverse '1st anim', 0s 3s steps(2, start) alternate \"2nd anim\" ! important");
	}

	@Test
	public void testBuilderImportant() {
		assertShorthandText("animation:3500ms frames(5) 5s reverse 'my anim'!important;",
				"animation: 3500ms 5s frames(5) reverse 'my anim' ! important");
	}

	@Test
	public void testBuilderInherit() {
		assertShorthandText("animation:inherit;", "animation: inherit;");
	}

	@Test
	public void testBuilderInheritImportant() {
		assertShorthandText("animation:inherit!important;", "animation: inherit!important;");
	}

	@Test
	public void testBuilderUnset() {
		assertShorthandText("animation:unset;", "animation: unset;");
	}

	@Test
	public void testBuilderUnsetImportant() {
		assertShorthandText("animation:unset!important;", "animation: unset!important;");
	}

	private void assertShorthandText(String expected, String original) {
		emptyStyleDecl.setCssText(original);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
		emptyStyleDecl.setCssText(expected);
		assertEquals(expected, emptyStyleDecl.getOptimizedCssText());
	}

}
