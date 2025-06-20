/*

 Copyright (c) 2020-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import java.util.Arrays;

import io.sf.carte.uparser.MinificationHandler;

class ShallowMinificationHandler extends MinificationHandler {

	ShallowMinificationHandler(StringBuilder buffer) {
		super(buffer);
	}

	private static final char[] noSepAfter;

	private static final char[] noSepBefore;

	static {
		noSepAfter = new char[] { ' ', '!', '(', ',', '/', ':', ';', '>', '[', '{', '}' };
		noSepBefore = new char[] { '!', ',', '/', ';', '<', '>' };
		assert isOrderedArray(noSepAfter); // Just in case
		assert isOrderedArray(noSepBefore);
	}

	private static boolean isOrderedArray(char[] c) {
		char[] copy = Arrays.copyOf(c, c.length);
		Arrays.sort(copy);
		return Arrays.equals(copy, c);
	}

	private boolean whitespaceRequired() {
		return Arrays.binarySearch(noSepAfter, (char) getPreviousCodepoint()) < 0;
	}

	@Override
	public void separator(int index, int codePoint) {
		if (whitespaceRequired()) {
			getBuffer().append(' ');
			setPreviousCodepoint(32);
		}
	}

	@Override
	public void leftCurlyBracket(int index) {
		removeTrailingWhitespace();
		super.leftCurlyBracket(index);
	}

	@Override
	public void rightParenthesis(int index) {
		removeTrailingWhitespace();
		super.rightParenthesis(index);
	}

	@Override
	public void rightSquareBracket(int index) {
		removeTrailingWhitespace();
		super.rightSquareBracket(index);
	}

	@Override
	public void rightCurlyBracket(int index) {
		removeTrailingWhitespace();
		removeTrailingCharacter(';');
		super.rightCurlyBracket(index);
	}

	@Override
	public void character(int index, int codePoint) {
		if (Arrays.binarySearch(noSepBefore, (char) codePoint) >= 0) {
			removeTrailingWhitespace();
		}
		super.character(index, codePoint);
	}

	@Override
	public void commented(int index, int commentType, String comment) {
		StringBuilder buf = getBuffer();
		if (!comment.isEmpty() && comment.charAt(0) == '!') {
			removeTrailingWhitespace();
			buf.append("/*").append(comment).append("*/");
		} else if (whitespaceRequired()) {
			buf.append(' ');
		}
		setPreviousCodepoint(32);
	}

	private void removeTrailingWhitespace() {
		removeTrailingCharacter(' ');
	}

	private void removeTrailingCharacter(char c) {
		StringBuilder buffer = getBuffer();
		int len = buffer.length();
		if (len > 0) {
			len--;
			if (buffer.charAt(len) == c) {
				buffer.setLength(len);
			}
		}
	}

}
