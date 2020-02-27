/*

 Copyright (c) 2017-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.uparser;

public class CommentRemovalHandler implements TokenHandler {

	private final StringBuilder buffer;

	public CommentRemovalHandler(int bufSize) {
		super();
		buffer = new StringBuilder(bufSize);
	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	@Override
	public void tokenStart(TokenControl control) {
	}

	@Override
	public void word(int index, CharSequence word) {
		buffer.append(word);
	}

	@Override
	public void separator(int index, int codePoint) {
		buffer.append(Character.toChars(codePoint));
	}

	@Override
	public void quoted(int index, CharSequence quoted, int quoteCp) {
		char[] quote = Character.toChars(quoteCp);
		buffer.append(quote);
		buffer.append(quoted);
		buffer.append(quote);
	}

	@Override
	public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
		quoted(index, quoted, quoteCp);
	}

	@Override
	public void quotedNewlineChar(int index, int codePoint) {
		buffer.append(Character.toChars(codePoint));
	}

	@Override
	public void openGroup(int index, int codePoint) {
		buffer.append(Character.toChars(codePoint));
	}

	@Override
	public void closeGroup(int index, int codePoint) {
		buffer.append(Character.toChars(codePoint));
	}

	@Override
	public void character(int index, int codePoint) {
		buffer.append(Character.toChars(codePoint));
	}

	@Override
	public void escaped(int index, int codePoint) {
		buffer.append('\\').append(Character.toChars(codePoint));
	}

	@Override
	public void control(int index, int codePoint) {
		buffer.append(Character.toChars(codePoint));
	}

	@Override
	public void commented(int index, int commentType, String comment) {
	}

	@Override
	public void endOfStream(int len) {
	}

	@Override
	public void error(int index, byte errCode, CharSequence context) {
	}

}
