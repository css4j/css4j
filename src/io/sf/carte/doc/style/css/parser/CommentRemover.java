/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.io.IOException;
import java.io.StringReader;

import io.sf.carte.uparser.CommentRemovalHandler;
import io.sf.carte.uparser.TokenProducer;

/**
 * Utility class that removes CSS comments from a string.
 */
public class CommentRemover {

	/**
	 * Takes a string and returns a {@link StringBuilder} with the CSS comments
	 * removed.
	 * 
	 * @param cssText the CSS text to remove comments from.
	 * @return a {@code StringBuilder} containing the text with the comments removed.
	 */
	public static StringBuilder removeComments(String cssText) {
		final String[] opening = { "/*", "<!--" };
		final String[] closing = { "*/", "-->" };
		CommentRemovalHandler handler = new MyCommentRemovalHandler(cssText.length());
		TokenProducer tp = new TokenProducer(handler);
		try {
			tp.parseMultiComment(new StringReader(cssText), opening, closing);
		} catch (IOException e) {
		}
		return handler.getBuffer();
	}

	/**
	 * Takes a string and returns a {@link StringBuilder} with the CSS comments
	 * preceding the given index removed.
	 * 
	 * @param cssText        the CSS text to remove comments from.
	 * @param removeUntilIdx the index after which the comments won't be removed.
	 * @return a {@code StringBuilder} containing the text with the comments removed.
	 */
	public static StringBuilder removeCommentsUntil(String cssText, int removeUntilIdx) {
		final String[] opening = { "/*", "<!--" };
		final String[] closing = { "*/", "-->" };
		CommentRemovalHandler handler = new CommentRemoveUntilHandler(cssText.length(), removeUntilIdx);
		TokenProducer tp = new TokenProducer(handler);
		try {
			tp.parseMultiComment(new StringReader(cssText), opening, closing);
		} catch (IOException e) {
		}
		return handler.getBuffer();
	}

	private static class MyCommentRemovalHandler extends CommentRemovalHandler {

		MyCommentRemovalHandler(int bufSize) {
			super(bufSize);
		}

	}

	private static class CommentRemoveUntilHandler extends MyCommentRemovalHandler {

		private final int removeUntilIdx;

		private CommentRemoveUntilHandler(int bufSize, int removeUntilIdx) {
			super(bufSize);
			this.removeUntilIdx = removeUntilIdx;
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (commentType == 0 && index >= removeUntilIdx) {
				StringBuilder buf = getBuffer();
				buf.append("/*");
				buf.append(comment);
				buf.append("*/");
			}
		}

		@Override
		public void control(int index, int codePoint) {
		}

	}

}
