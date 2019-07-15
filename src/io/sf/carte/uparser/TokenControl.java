/*

 Copyright (c) 2017-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.uparser;

/**
 * Offers methods to control the parsing process by the handler.
 */
public interface TokenControl {

	/**
	 * Enable the handling of all types of comments.
	 */
	void enableAllComments();

	/**
	 * If set, quoted strings ending with an unescaped newline (instead of the closing quote)
	 * are processed through the relevant <code>quoted</code> method, albeit an error is
	 * reported in any case. Otherwise, only the error is reported.
	 * <p>
	 * It is set to <code>false</code> by default.
	 * 
	 * @param accept
	 *            true to process quoted strings that ends with an unescaped newline, false
	 *            otherwise.
	 */
	void setAcceptNewlineEndingQuote(boolean accept);

	/**
	 * If set, quoted strings ending with an EOF (End Of File). are processed through the
	 * relevant <code>quoted</code> method, albeit an error is reported in any case.
	 * Otherwise, only the error is reported.
	 * <p>
	 * It is set to <code>false</code> by default.
	 * 
	 * @param accept
	 *            true to process quoted strings that ends with an EOF, <code>false</code> otherwise.
	 */
	void setAcceptEofEndingQuoted(boolean accept);

	/**
	 * Disable the handling of all types of comments.
	 */
	void disableAllComments();

	/**
	 * Enable the handling of comments of the given type.
	 * 
	 * @param type
	 *            the type of comment to enable. If the type does not exist, implementations
	 *            may or may not throw an exception, depending on the intended coupling with
	 *            the <code>TokenHandler</code>.
	 */
	void enableComments(int type);

	/**
	 * Disable the handling of comments of the given type.
	 * 
	 * @param type
	 *            the type of comment to disable. If the type does not exist, implementations
	 *            may or may not throw an exception, depending on the intended coupling with
	 *            the <code>TokenHandler</code>.
	 */
	void disableComments(int type);
}
