/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.util;

import java.io.IOException;

/**
 * A simple writer interface.
 * <p>
 * Similar to a subset from {@link java.io.Writer}, but with {@link #newLine()} method
 * added, similar to {@link java.io.BufferedWriter#newLine()} but with the difference that
 * the newline not necessarily uses the <tt>line.separator</tt> system property and
 * instead may target another system.
 *
 */
public interface SimpleWriter {

	/**
	 * Write a newline according to the characteristics of the target platform.
	 * 
	 * @throws IOException
	 *             if an error happens when writing.
	 */
	void newLine() throws IOException;

	/**
	 * Write a character sequence.
	 * 
	 * @param seq the CharSequence.
	 * @throws IOException if an error happens when writing.
	 */
	void write(CharSequence seq) throws IOException;

	/**
	 * Write a sequence of characters from an array.
	 * 
	 * @param cbuf
	 *            the character array.
	 * @param off
	 *            the offset to start writing.
	 * @param len
	 *            the number of characters to write from the array.
	 * @throws IOException
	 *             if an error happens when writing.
	 */
	void write(char[] cbuf, int off, int len) throws IOException;

	/**
	 * Write a character.
	 * 
	 * @param c
	 *            the character.
	 * @throws IOException
	 *             if an error happens when writing.
	 */
	void write(char c) throws IOException;

	/**
	 * Write an integer.
	 * 
	 * @param num
	 *            the integer number.
	 * @throws IOException
	 *             if an error happens when writing.
	 */
	void write(int num) throws IOException;

}
